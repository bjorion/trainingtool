package org.jorion.trainingtool.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("local")
@RequiredArgsConstructor
class UserRepositoryTest {

    private final IUserRepository userRepository;

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
