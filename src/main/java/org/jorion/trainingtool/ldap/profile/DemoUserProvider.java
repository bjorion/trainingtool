package org.jorion.trainingtool.ldap.profile;

import org.jorion.trainingtool.user.User;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * This service is useful only for testing purposes and should NOT be instantiated in production.
 */
@Component
@Profile("local")
public class DemoUserProvider {

    public List<User> provideShortUsers() {

        List<User> users = new ArrayList<>();
        User user;
        user = User.builder().userName("alice.doe").firstName("Alice").lastName("Doe").function("Analyst").build();
        users.add(user);
        user = User.builder().userName("bob.doe").firstName("Bob").lastName("Doe").function("Developer").build();
        users.add(user);
        user = User.builder().userName("charles.doe").firstName("Charles").lastName("Doe").function("Architect").build();
        users.add(user);
        user = User.builder().userName("john.doe").firstName("John").lastName("Doe").function("Analyst").build();
        users.add(user);
        user = User.builder().userName("mi.mathieu").firstName("Michael").lastName("Mathieu").function("Developer").build();
        users.add(user);
        return users;
    }
}
