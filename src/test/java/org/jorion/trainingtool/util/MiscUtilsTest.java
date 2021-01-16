package org.jorion.trainingtool.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.Test;

/**
 * Unit test for {@link MiscUtils}.
 */
public class MiscUtilsTest
{
    // --- Methods ---
    @Test
    public void testLikeSqlString()
    {
        assertEquals("%", MiscUtils.likeSqlString(null));
        assertEquals("%", MiscUtils.likeSqlString(""));
        assertEquals("john%", MiscUtils.likeSqlString("John"));
        assertEquals("jo_n%", MiscUtils.likeSqlString("Jo?n"));
        assertEquals("john%%", MiscUtils.likeSqlString("John*"));
    }

    @Test
    public void testIsUrlValid()
    {
        assertFalse(MiscUtils.isUrlValid(" ", true));
        assertTrue(MiscUtils.isUrlValid(" ", false));
        assertTrue(MiscUtils.isUrlValid("http://www.microsoft.com", false));
        assertTrue(MiscUtils.isUrlValid("https://www.cevora.be/fr", false));
        assertFalse(MiscUtils.isUrlValid("www.microsoft.com", false));
    }

    @Test
    public void testIsDateBefore()
    {
        assertTrue(MiscUtils.isDateBefore(null, null, false, false));
        assertTrue(MiscUtils.isDateBefore(LocalDate.now(), null, false, false));
        assertFalse(MiscUtils.isDateBefore(LocalDate.now(), null, false, true));
        assertFalse(MiscUtils.isDateBefore(LocalDate.now(), LocalDate.now(), false, true));
        assertTrue(MiscUtils.isDateBefore(LocalDate.now(), LocalDate.now().plusDays(1), false, true));
        assertFalse(MiscUtils.isDateBefore(LocalDate.now(), LocalDate.now().minusDays(1), false, true));

        assertTrue(MiscUtils.isDateBefore(LocalDate.now(), LocalDate.now(), true, true));
        assertTrue(MiscUtils.isDateBefore(LocalDate.now(), LocalDate.now().plusDays(1), true, true));
        assertFalse(MiscUtils.isDateBefore(LocalDate.now(), LocalDate.now().minusDays(1), true, true));
    }

    @Test
    public void testParsePositiveFloat()
    {
        // empty values
        assertNull(MiscUtils.parsePositiveFloat(null));
        assertNull(MiscUtils.parsePositiveFloat(" "));

        // invalid values
        assertEquals("-1", MiscUtils.parsePositiveFloat("abc"));
        assertEquals("-1", MiscUtils.parsePositiveFloat("123.456abc"));
        assertEquals("-1", MiscUtils.parsePositiveFloat("123.456.789"));
        assertEquals("-1", MiscUtils.parsePositiveFloat("123,456"));
        assertEquals("-1", MiscUtils.parsePositiveFloat("-123"));

        // valid values
        assertEquals("0.00", MiscUtils.parsePositiveFloat("0"));
        assertEquals("123.00", MiscUtils.parsePositiveFloat("123"));
        assertEquals("123.45", MiscUtils.parsePositiveFloat("123.45"));
        assertEquals("123.45", MiscUtils.parsePositiveFloat("123.459"));
        assertEquals("1234.56", MiscUtils.parsePositiveFloat("1234.567"));
    }

    @Test
    public void testFindIndex()
    {
        String[] letters = { "alpha", null, "beta" };
        assertEquals(0, MiscUtils.findIndex(letters, "alpha"));
        assertEquals(1, MiscUtils.findIndex(letters, null));
        assertEquals(2, MiscUtils.findIndex(letters, "beta"));
        assertEquals(-1, MiscUtils.findIndex(letters, "omega"));
    }
}
