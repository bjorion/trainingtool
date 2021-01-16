package org.jorion.trainingtool.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Unit test for {@link RegistrationEvent}.
 */
public class RegistrationEventTest
{
    // --- Methods ---
    @Test
    public void testFindByValue()
    {
        assertNull(RegistrationEvent.findByValue(null));
        assertNull(RegistrationEvent.findByValue(" "));
        assertNull(RegistrationEvent.findByValue("dummy"));
        assertEquals(RegistrationEvent.ASSIGN, RegistrationEvent.findByValue(RegistrationEvent.ASSIGN.name()));
    }

}
