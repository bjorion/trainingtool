package org.jorion.trainingtool.report;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.jorion.trainingtool.common.controller.Constants;
import org.jorion.trainingtool.report.json.StatDTO;
import org.jorion.trainingtool.registration.Registration;
import org.jorion.trainingtool.registration.RegistrationDTO;
import org.jorion.trainingtool.registration.RegistrationMapper;
import org.jorion.trainingtool.registration.RegistrationService;
import org.jorion.trainingtool.training.Training;
import org.jorion.trainingtool.training.TrainingDTO;
import org.jorion.trainingtool.training.TrainingMapper;
import org.jorion.trainingtool.training.TrainingService;
import org.jorion.trainingtool.type.RegistrationStatus;
import org.jorion.trainingtool.user.User;
import org.jorion.trainingtool.user.UserDTO;
import org.jorion.trainingtool.user.UserMapper;
import org.jorion.trainingtool.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Rest Controller.
 * <p>
 * All users calling the methods below must be authenticated and have the right level of security.
 */
@Slf4j
@RestController
@RequestMapping("/REST/v1")
public class WSController {

    @Autowired
    private UserService userService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private TrainingService trainingService;

    @Operation(tags = "users", summary = "Return all users (without their registrations)")
    @GetMapping(value = "/users", produces = "application/json")
    public CollectionResource<UserDTO> getUsers() {

        var users = userService.findAll();
        return new CollectionResource<>(UserMapper.INSTANCE.toUserDTO(users));
    }


    @Operation(tags = "users", summary = "Return a specific user", responses = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "User not found", content = {@Content()})
    })
    @GetMapping(value = "/users/{username}")
    public ResponseEntity<UserDTO> getUser(@PathVariable("username") String userName) {

        if ("error".equals(userName)) {
            throw new IllegalStateException("custom error");
        }

        var user = userService.findUserByUserName(userName);
        if (user == null) {
            log.info("User [{}] not found", userName);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(UserMapper.INSTANCE.toUserDTO(user));
    }


    @Operation(tags = "users", summary = "Return all registrations for a specific user", responses = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "403", description = "Request not authorized", content = {@Content()})
    })
    @GetMapping(value = "/users/{username}/regs")
    public ResponseEntity<CollectionResource<RegistrationDTO>> getRegistrationsByUser(
            @PathVariable("username") String memberName,
            @RequestParam(required = false) Integer page) {

        int start = (page == null) ? 0 : page;
        int size = Constants.SIZE_DEF;
        var pr = PageRequest.of(start, size, Sort.by(Direction.DESC, "id"));

        var user = userService.findPrincipalAsUser();
        Page<Registration> regs;
        try {
            regs = registrationService.findAllByMemberName(user, memberName, pr);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<RegistrationDTO> dtos = regs.stream().map(RegistrationMapper.INSTANCE::toRegistrationDTO).collect(Collectors.toList());
        return ResponseEntity.ok(new CollectionResource<>(dtos));
    }

    @Operation(tags = "stats", summary = "Return statistical data (subject to change)", responses = {
            @ApiResponse(responseCode = "200")
    })
    @GetMapping(value = "/stats", produces = "application/json")
    public ResponseEntity<CollectionResource<StatDTO>> getStats() {

        var users = userService.findAll();
        var stats = new ArrayList<StatDTO>();
        for (User user : users) {
            Set<Registration> set = user.getRegistrations();
            if (set.isEmpty()) {
                continue;
            }
            Map<String, Integer> map = new HashMap<>();
            for (Registration r : set) {
                RegistrationStatus st = r.getStatus();
                int count = map.getOrDefault(st.name(), 0);
                map.put(st.name(), ++count);
            }
            StatDTO stat = new StatDTO(user.getUserName(), map);
            stats.add(stat);
        }
        return ResponseEntity.ok(new CollectionResource<>(stats));
    }

    @Operation(tags = "stats", summary = "Return statistical data (subject to change)", responses = {
            @ApiResponse(responseCode = "200")
    })
    @GetMapping(value = "/stats/summaries", produces = "application/json")
    public Map<RegistrationStatus, Integer> getRegSummaries() {

        var stats = new TreeMap<RegistrationStatus, Integer>(Enum::compareTo);
        stats.putAll(registrationService.computeStats());
        return stats;
    }

    @Operation(tags = "trainings", summary = "Return trainings", responses = {
            @ApiResponse(responseCode = "200")
    })
    @GetMapping(value = "/trainings", produces = "application/json")
    public ResponseEntity<CollectionResource<TrainingDTO>> getTrainings(
            @RequestParam(value = "enabledOnly", required = false) Boolean enabledOnly) {

        boolean enabled = BooleanUtils.toBoolean(enabledOnly);
        var trainings = trainingService.findAllTrainings(enabled);
        var dtos = trainings.stream().map(TrainingMapper.INSTANCE::toTrainingDTO).collect(Collectors.toList());
        return ResponseEntity.ok(new CollectionResource<>(dtos));
    }

}