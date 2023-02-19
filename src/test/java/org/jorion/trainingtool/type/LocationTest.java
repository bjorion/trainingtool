package org.jorion.trainingtool.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LocationTest {

    @Test
    public void testGetName() {
        assertEquals(Location.CLASSROOM.name(), Location.CLASSROOM.getName());
    }

    @Test
    public void testGetTitle() {

        assertTrue(Location.CLASSROOM.name().equalsIgnoreCase(Location.CLASSROOM.getTitle()));
    }

}
