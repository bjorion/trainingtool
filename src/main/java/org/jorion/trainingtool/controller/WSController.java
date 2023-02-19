package org.jorion.trainingtool.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.jorion.trainingtool.dto.CollectionResource;
import org.jorion.trainingtool.dto.json.RegistrationDTO;
import org.jorion.trainingtool.dto.json.StatDTO;
import org.jorion.trainingtool.dto.json.TrainingDTO;
import org.jorion.trainingtool.dto.json.UserDTO;
import org.jorion.trainingtool.entity.Registration;
import org.jorion.trainingtool.entity.Training;
import org.jorion.trainingtool.entity.User;
import org.jorion.trainingtool.mapper.RegistrationMapper;
import org.jorion.trainingtool.mapper.TrainingMapper;
import org.jorion.trainingtool.mapper.UserMapper;
import org.jorion.trainingtool.service.RegistrationService;
import org.jorion.trainingtool.service.TrainingService;
import org.jorion.trainingtool.service.UserService;
import org.jorion.trainingtool.type.RegistrationStatus;
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

        List<User> users = userService.findAll();
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

        User user = userService.findUserByUserName(userName);
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

        int start = (page == null) ? 0 : page.intValue();
        int size = Constants.SIZE_DEF;
        PageRequest pr = PageRequest.of(start, size, Sort.by(Direction.DESC, "id"));

        User user = userService.findPrincipalAsUser();
        Page<Registration> regs = null;
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

        List<User> users = userService.findAll();
        List<StatDTO> stats = new ArrayList<>();
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

        Map<RegistrationStatus, Integer> stats = new TreeMap<RegistrationStatus, Integer>((rs1, rs2) -> rs1.compareTo(rs2));
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
        List<Training> trainings = trainingService.findAllTrainings(enabled);
        List<TrainingDTO> dtos = trainings.stream().map(TrainingMapper.INSTANCE::toTrainingDTO).collect(Collectors.toList());
        return ResponseEntity.ok(new CollectionResource<>(dtos));
    }

}