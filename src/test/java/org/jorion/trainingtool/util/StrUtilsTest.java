package org.jorion.trainingtool.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StrUtilsTest {

    @Test
    public void testLikeSqlString() {

        assertEquals("%", StrUtils.likeSqlString(null));
        assertEquals("%", StrUtils.likeSqlString(""));
        assertEquals("john%", StrUtils.likeSqlString("John"));
        assertEquals("jo_n%", StrUtils.likeSqlString("Jo?n"));
        assertEquals("john%%", StrUtils.likeSqlString("John*"));
    }

    @Test
    public void testIsUrlValid() {

        assertFalse(StrUtils.isUrlValid(" ", true));
        assertTrue(StrUtils.isUrlValid(" ", false));
        assertTrue(StrUtils.isUrlValid("http://www.example.org", false));
        assertTrue(StrUtils.isUrlValid("https://www.cevora.be/fr", false));
        assertFalse(StrUtils.isUrlValid("www.example.org", false));
    }

    @Test
    public void testParsePositiveFloat() {

        // empty values
        assertNull(StrUtils.parsePositiveFloat(null));
        assertNull(StrUtils.parsePositiveFloat(" "));

        // invalid values
        assertEquals("-1", StrUtils.parsePositiveFloat("abc"));
        assertEquals("-1", StrUtils.parsePositiveFloat("123.456abc"));
        assertEquals("-1", StrUtils.parsePositiveFloat("123.456.789"));
        assertEquals("-1", StrUtils.parsePositiveFloat("123,456"));
        assertEquals("-1", StrUtils.parsePositiveFloat("-123"));

        // valid values
        assertEquals("0.00", StrUtils.parsePositiveFloat("0"));
        assertEquals("123.00", StrUtils.parsePositiveFloat("123"));
        assertEquals("123.45", StrUtils.parsePositiveFloat("123.45"));
        assertEquals("123.45", StrUtils.parsePositiveFloat("123.459"));
        assertEquals("1234.56", StrUtils.parsePositiveFloat("1234.567"));
    }

    @Test
    public void testFindIndex() {

        String[] letters = {"alpha", null, "beta"};
        assertEquals(0, StrUtils.findIndex(letters, "alpha"));
        assertEquals(1, StrUtils.findIndex(letters, null));
        assertEquals(2, StrUtils.findIndex(letters, "beta"));
        assertEquals(-1, StrUtils.findIndex(letters, "omega"));
    }
}
