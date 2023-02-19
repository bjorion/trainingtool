package org.jorion.trainingtool.repository;

import org.jorion.trainingtool.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("local")
public class UserRepositoryTest {

    @Autowired
    @SuppressWarnings("unused")
    private TestEntityManager em;

    @Autowired
    private IUserRepository userRepository;

    @Test
    void testFindUserByUsername() {

        final String name = "john.doe";
        User user = userRepository.findUserByUserName(name).orElseGet(() -> null);
        assertEquals(name, user.getUserName());
    }

    @Test
    void testFindAllByExample() {

        List<User> users = userRepository.findAllByExample("JOH%", "DO%");
        assertEquals(1, users.size());
    }
}
