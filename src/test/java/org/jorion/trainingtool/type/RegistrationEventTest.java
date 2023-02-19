package org.jorion.trainingtool.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RegistrationEventTest {

    @Test
    public void testFindByValue() {

        assertNull(RegistrationEvent.findByValue(null));
        assertNull(RegistrationEvent.findByValue(" "));
        assertNull(RegistrationEvent.findByValue("dummy"));
        assertEquals(RegistrationEvent.ASSIGN, RegistrationEvent.findByValue(RegistrationEvent.ASSIGN.name()));
    }

}
