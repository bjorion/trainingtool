package org.jorion.trainingtool.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for {@link YesNo}.
 */
public class YesNoTest
{
    // --- Methods ---
    @Test
    public void testIsYes()
    {
        assertFalse(YesNo.isYes(null));
        assertTrue(YesNo.isYes(YesNo.YES));
        assertFalse(YesNo.isYes(YesNo.NO));
    }

}
