package org.jorion.trainingtool.services;

import static org.jorion.trainingtool.util.MiscUtils.likeSqlString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import org.jorion.trainingtool.entities.User;
import org.jorion.trainingtool.repositories.IUserRepository;

/**
 * Methods used to handle user information.
 * <p>
 * This class may NOT depend upon {@link RegistrationService}, otherwise we would have a cyclic dependency.
 */
@Service
public class UserService
{
    // --- Constants ---
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    // --- Variables ---
    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private LdapService ldapService;

    @Value("${app.reg.sectors}")
    private String[] sectors;

    // --- Methods ---
    /**
     * @return A collection of sectors
     */
    public List<String> getSectors()
    {
        return Collections.unmodifiableList(Arrays.asList(sectors));
    }

    /**
     * Read User and related data (registrations) from the DB.
     */
    public User findUserByUsername(String username)
    {
        return userRepository.findUserByUsername(username).orElseGet(() -> null);
    }

    /**
     * Read User and related data (registrations) from the DB, or from LDAP if not found. If the user does not yet exist
     * in the DB, it will be inserted. User data are automatically refreshed if they are older than {@code deltaDays}
     * days.
     * 
     * @param username the username used for the search
     * @return the user, or null if not found
     */
    public User findUserByUsernameOrCreate(String username)
    {
        int deltaDays = 1;

        User user = findUserByUsername(username);
        if (user != null) {
            LOG.debug("User retrieved from the DB: id [{}] username [{}]", user.getId(), user.getUsername());
        }

        boolean ldap = (user == null || user.getModifiedOn().isBefore(LocalDateTime.now().minusDays(deltaDays)));
        if (ldap) {
            User ldapUser = ldapService.searchByName(username);
            if (ldapUser != null) {
                // User does not exist yet in the DB
                if (user == null) {
                    LOG.debug("LDAP User not yet in the DB [{}]", ldapUser.getUsername());
                    user = userRepository.save(ldapUser);
                }
                // User already in the DB, we update his attributes
                else {
                    LOG.debug("User already in the DB: id [{}] username [{}]", user.getId(), user.getUsername());
                    // excluded: id, pnr, username, ssin
                    user.setEmail(ldapUser.getEmail());
                    user.setFirstname(ldapUser.getFirstname());
                    user.setLastname(ldapUser.getLastname());
                    user.setFunction(ldapUser.getFunction());
                    user.setManagername(ldapUser.getManagername());
                    user.setPhoneNumber(ldapUser.getPhoneNumber());
                    user.setRoles(ldapUser.getRoles());
                    user = userRepository.save(user);
                }
            }
            else {
                LOG.info("User [{}] not found in LDAP", username);
            }
        }
        return user;
    }

    /**
     * @param user the user containing the data to use
     * @param limit the max number of users to return
     * @return A collection of users based on the given example.
     */
    public Set<User> findUsersByExample(User user, int limit)
    {
        // Search LDAP
        List<User> ldapUsers = ldapService.searchByExample(user);
        // Search DB
        List<User> dbUsers = userRepository.findAllByExample(likeSqlString(user.getFirstname()), likeSqlString(user.getLastname()));

        // keep unique users (based on their username)
        // if a username exists in both list, the first instance will be kept
        Set<User> set = new HashSet<>();
        set.addAll(ldapUsers);
        set.addAll(dbUsers);
        return set.stream().limit(limit).collect(Collectors.toSet());
    }

    /**
     * Retrieve from the LDAP all users who have the given manager.
     * 
     * @param manager the manager username
     * @return A collection of users with the given manager, ordered by the fullname
     */
    public Set<User> findUsersByManager(String manager)
    {
        List<User> ldapUsers = ldapService.searchByManager(manager);
        Set<User> set = new TreeSet<>((u1, u2) -> {
            return u1.getFullname().compareTo(u2.getFullname());
        });
        set.addAll(ldapUsers);
        return set;
    }

    /**
     * Save the given user.
     * 
     * @param user the user to save
     * @return the saved user
     */
    public User saveUser(User user)
    {
        return userRepository.save(user);
    }

    /**
     * Update the user corresponding to the given data.
     *
     * @throws AccessDeniedException if the user is not found
     */
    public User updateUser(User dtoUser)
    {
        Assert.notNull(dtoUser.getUsername(), "Username should not be null");
        User user = userRepository.findUserByUsername(dtoUser.getUsername()).orElseThrow(() -> {
            return new AccessDeniedException("User not found [" + dtoUser.getUsername() + "]");
        });
        user.convertFrom(dtoUser);
        user = userRepository.save(user);
        LOG.debug("Updated User [{}]", user.getUsername());
        return user;
    }

    // Static
    /**
     * Retrieve the name of the Principal.
     * <p>
     * If the authentication happens via LDAP, the Principal is an instance of
     * {@link org.springframework.security.ldap.userdetails.LdapUserDetailsImpl}.
     * <p>
     * If the authentication uses an in-memory user (for testing purposes only), the Principal is an instance of
     * {@link org.springframework.security.core.userdetails.User}.
     * 
     * @return The Principal name, or null if not found
     */
    public static String getPrincipalName()
    {
        String name = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof LdapUserDetailsImpl) {
            LdapUserDetailsImpl user = (LdapUserDetailsImpl) principal;
            name = user.getUsername();
        }
        else if (principal instanceof org.springframework.security.core.userdetails.User) {
            org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) principal;
            name = user.getUsername();
        }
        else {
            LOG.error("Unexpected Principal {}", principal);
        }
        return name;
    }

    /**
     * @param user the user for the registration 
     * @return A collection of errors in case of business errors, or an empty collection otherwise.
     */
    public static List<String> checkBusinessErrors(User user)
    {
        List<String> errors = new ArrayList<>();
        boolean fnBlank = StringUtils.isBlank(user.getFirstname()) || "*".equals(user.getFirstname().trim());
        boolean lnBlank = StringUtils.isBlank(user.getLastname()) || "*".equals(user.getLastname().trim());
        if (fnBlank && lnBlank) {
            errors.add("missing");
        }
        return errors;
    }
}
