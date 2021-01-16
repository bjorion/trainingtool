package org.jorion.trainingtool.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.jorion.trainingtool.dto.CollectionResource;
import org.jorion.trainingtool.entities.Registration;
import org.jorion.trainingtool.entities.User;
import org.jorion.trainingtool.repositories.IUserRepository;
import org.jorion.trainingtool.types.RegistrationStatus;

import io.swagger.annotations.ApiOperation;

/**
 * Web Service Controller. This class is used for experiments.
 */
@RestController
@RequestMapping("/REST/v1")
public class WSController
{
    // --- Constants ---
    private static final Logger LOG = LoggerFactory.getLogger(WSController.class);

    // --- Variables ---
    @Autowired
    private IUserRepository userRepository;

    // --- Methods ---
    @ApiOperation(value = "Return all users")
    @GetMapping(value = "/users", produces = "application/json")
    public CollectionResource<User> getUsers()
    {
        List<User> users = userRepository.findAll();
        return new CollectionResource<>(users);
    }

    @ApiOperation(value = "Return a specific user")
    @GetMapping(value = "/users/{username}")
    public User getUser(@PathVariable("username") String username)
    {
        User user = userRepository.findUserByUsername(username).orElseGet(() -> null);
        if (user == null) {
            LOG.info("User [{}] not found", username);
        }
        return user;
    }

    @ApiOperation(value = "Return registration statistical data")
    @GetMapping(value = "/regs", produces = "application/json")
    public CollectionResource<StatDTO> getRegs()
    {
        LOG.debug("getRegs()");
        List<User> users = userRepository.findAll();
        List<StatDTO> stats = new ArrayList<>();
        for (User user : users) {
            Set<Registration> set = user.getRegistrations();
            Map<String, Integer> map = new HashMap<>();
            for (Registration r : set) {
                RegistrationStatus st = r.getStatus();
                int count = map.getOrDefault(st.name(), 0);
                map.put(st.name(), ++count);
            }
            StatDTO stat = new StatDTO(user.getUsername(), map);
            stats.add(stat);
        }

        return new CollectionResource<>(stats);
    }

    static class RegDTO
    {
        public final String status;

        public final int count;

        public RegDTO(String status, int count)
        {
            this.status = status;
            this.count = count;
        }

        public String getStatus()
        {
            return status;
        }

        public int getCount()
        {
            return count;
        }
    }

    static class StatDTO
    {
        public final String username;

        public final List<RegDTO> registrations = new ArrayList<>();

        public StatDTO(String username, Map<String, Integer> map)
        {
            this.username = username;
            for (Entry<String, Integer> entry : map.entrySet()) {
                RegDTO dto = new RegDTO(entry.getKey(), entry.getValue());
                registrations.add(dto);
            }
        }

        public String getUsername()
        {
            return username;
        }

        public List<RegDTO> getRegistrations()
        {
            return registrations;
        }
    }
}