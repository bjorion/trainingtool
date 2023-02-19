package org.jorion.trainingtool.service;

import lombok.extern.slf4j.Slf4j;
import org.jorion.trainingtool.entity.User;
import org.jorion.trainingtool.mapper.UserMapper;
import org.jorion.trainingtool.repository.IUserRepository;
import org.jorion.trainingtool.service.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.jorion.trainingtool.util.StrUtils.likeSqlString;

/**
 * Methods used to handle user information.
 * <p>
 * This class may NOT depend upon {@link RegistrationService}, otherwise we would have a cyclic dependency.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class UserService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private LdapService ldapService;

    @Value("${app.reg.sectors}")
    private String[] sectors;

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
    public static String getPrincipalName() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : null;
    }

    /**
     * A principal is authorized access to the user's data only if
     * <ul>
     * <li>the principal has the rank "office"
     * <li>the principal is a manager, and is responsible for the given user
     * <li>the principal is the same entity than the user
     * </ul>
     */
    public static boolean isAuthorized(User principal, User user) {

        boolean authorized;
        if (principal == null || user == null) {
            authorized = false;
        } else if (principal.getUserName().equals(user.getUserName())) {
            authorized = true;
        } else if (principal.isOffice()) {
            authorized = true;
        } else if (principal.isManager()) {
            authorized = principal.getUserName().equals(user.getManagerName());
        } else {
            authorized = false;
        }
        return authorized;
    }

    public List<String> getSectors() {
        return Collections.unmodifiableList(Arrays.asList(sectors));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Read User and related data (registrations) from the DB.
     */
    public User findUserByUserName(String userName) {

        return userRepository.findUserByUserName(userName).orElseGet(() -> null);
    }

    public User findPrincipalAsUser() {

        String principalName = UserService.getPrincipalName();
        return findUserByUserName(principalName);
    }

    /**
     * Read User and related data (registrations) from the DB, or from LDAP if not found. If the user does not yet exist
     * in the DB, it will be inserted. User data are automatically refreshed if they are older than {@code deltaDays}
     * days.
     *
     * @param userName the userName used for the search
     * @return the user, or null if not found
     */
    public User findUserByUserNameOrCreate(String userName) {

        int deltaDays = 1;

        final User dbUser = findUserByUserName(userName);
        if (dbUser != null) {
            log.debug("User retrieved from the DB: id [{}] userName [{}]", dbUser.getId(), dbUser.getUserName());
        }

        User user = dbUser;
        boolean searchLdap = (dbUser == null || dbUser.getModifiedOn().isBefore(LocalDateTime.now().minusDays(deltaDays)));
        if (searchLdap) {
            final User ldapUser = ldapService.searchByName(userName);
            if (ldapUser != null) {
                // User does not exist yet in the DB
                if (dbUser == null) {
                    log.debug("LDAP User not yet in the DB [{}]", ldapUser.getUserName());
                    user = userRepository.save(ldapUser);
                }
                // User already in the DB, we update his attributes
                else {
                    log.debug("User already in the DB: id [{}] userName [{}]", dbUser.getId(), dbUser.getUserName());
                    UserMapper.INSTANCE.updateUserFromLdap(ldapUser, dbUser);
                    user = userRepository.save(dbUser);
                }
            } else {
                log.warn("User [{}] not found in LDAP", userName);
            }
        }
        return user;
    }

    /**
     * @param user  the user containing the data to use
     * @param limit the max number of users to return
     * @return A collection of users based on the given example.
     */
    public Set<User> findUsersByExample(User user, int limit) {

        // Search LDAP
        List<User> ldapUsers = ldapService.searchByExample(user);
        // Search DB
        List<User> dbUsers = userRepository.findAllByExample(likeSqlString(user.getFirstName()), likeSqlString(user.getLastName()));

        // keep unique users (based on their userName)
        // if a userName exists in both list, the first instance will be kept
        Set<User> set = new HashSet<>();
        set.addAll(ldapUsers);
        set.addAll(dbUsers);
        return set.stream().limit(limit).collect(Collectors.toSet());
    }

    /**
     * Retrieve from the LDAP all users who have the given manager.
     *
     * @param manager the manager userName
     * @return A collection of users with the given manager, ordered by the fullname
     */
    public Set<User> findUsersByManager(String manager) {

        List<User> ldapUsers = ldapService.searchByManager(manager);
        Set<User> set = new TreeSet<>((u1, u2) -> {
            return u1.getFullName().compareTo(u2.getFullName());
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
    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    // Static

    /**
     * Update the user corresponding to the given data.
     *
     * @throws AccessDeniedException if the user is not found
     */
    @Transactional
    public User updateUser(User dtoUser) {

        Assert.notNull(dtoUser.getUserName(), "Username should not be null");
        User user = userRepository.findUserByUserName(dtoUser.getUserName()).orElseThrow(() -> {
            return new AccessDeniedException("User not found [" + dtoUser.getUserName() + "]");
        });
        user.convertFrom(dtoUser);
        user = userRepository.save(user);
        log.debug("Updated User [{}]", user.getUserName());
        return user;
    }

    /**
     * Validate the content of the given user.
     *
     * @param user the user to validate
     * @return A collection of errors (can be empty)
     */
    public List<String> checkBusinessErrors(User user) {
        return new UserValidator(user).validate();
    }
}
