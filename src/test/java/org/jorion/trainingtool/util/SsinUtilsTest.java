package org.jorion.trainingtool.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SsinUtilsTest {

    @Test
    public void testIsSsinValid() {

        assertFalse(SsinUtils.isSsinValid(null, true));
        assertFalse(SsinUtils.isSsinValid("  ", true));
        assertTrue(SsinUtils.isSsinValid("  ", false));
        assertFalse(SsinUtils.isSsinValid("0123", true));
        assertFalse(SsinUtils.isSsinValid("01234567890123", true));

        // < year 2000
        assertTrue(SsinUtils.isSsinValid("85073003328", true));
        // >= year 2000
        assertTrue(SsinUtils.isSsinValid("17073003384", true));
    }

    @Test
    public void testFormatSsin() {

        assertEquals("850730-033.28", SsinUtils.formatSsin("85073003328"));
        assertEquals("170730-033.84", SsinUtils.formatSsin("17073003384"));
    }
}
