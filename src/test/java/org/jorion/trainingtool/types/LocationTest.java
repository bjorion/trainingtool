package org.jorion.trainingtool.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for {@link Location}.
 */
public class LocationTest
{
    // --- Methods ---
    @Test
    public void testGetName()
    {
        assertEquals(Location.CLASSROOM.name(), Location.CLASSROOM.getName());
    }

    @Test
    public void testGetTitle()
    {
        assertTrue(Location.CLASSROOM.name().equalsIgnoreCase(Location.CLASSROOM.getTitle()));
    }

}
