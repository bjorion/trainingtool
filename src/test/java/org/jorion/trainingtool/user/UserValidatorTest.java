package org.jorion.trainingtool.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserValidatorTest {

    @Test
    void testCheckBusinessErrors() {

        User user = new User();
        assertFalse(new UserValidator(user).validate().isEmpty());

        user = new User();
        user.setFirstName(" * ");
        user.setLastName(" * ");
        assertFalse(new UserValidator(user).validate().isEmpty());

        user = new User();
        user.setFirstName("John");
        user.setLastName(" * ");
        assertTrue(new UserValidator(user).validate().isEmpty());

        user = new User();
        user.setFirstName(" * ");
        user.setLastName(" Doe ");
        assertTrue(new UserValidator(user).validate().isEmpty());

        user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        assertTrue(new UserValidator(user).validate().isEmpty());
    }
}
