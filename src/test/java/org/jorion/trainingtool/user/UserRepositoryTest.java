package org.jorion.trainingtool.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("local")
public class UserRepositoryTest {

    @Autowired
    private IUserRepository userRepository;

    @Test
    void testFindUserByUsername() {

        final String name = "john.doe";
        var user = userRepository.findUserByUserName(name).orElse(null);
        assertNotNull(user);
        assertEquals(name, user.getUserName());
    }

    @Test
    void testFindAllByExample() {

        var users = userRepository.findAllByExample("JOH%", "DO%");
        assertEquals(1, users.size());
    }
}
