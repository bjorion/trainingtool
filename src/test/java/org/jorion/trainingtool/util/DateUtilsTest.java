package org.jorion.trainingtool.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DateUtilsTest {

    @Test
    public void testIsDateBefore() {

        assertTrue(DateUtils.isDateBefore(null, null, false, false));
        assertTrue(DateUtils.isDateBefore(LocalDate.now(), null, false, false));
        assertFalse(DateUtils.isDateBefore(LocalDate.now(), null, false, true));
        assertFalse(DateUtils.isDateBefore(LocalDate.now(), LocalDate.now(), false, true));
        assertTrue(DateUtils.isDateBefore(LocalDate.now(), LocalDate.now().plusDays(1), false, true));
        assertFalse(DateUtils.isDateBefore(LocalDate.now(), LocalDate.now().minusDays(1), false, true));

        assertTrue(DateUtils.isDateBefore(LocalDate.now(), LocalDate.now(), true, true));
        assertTrue(DateUtils.isDateBefore(LocalDate.now(), LocalDate.now().plusDays(1), true, true));
        assertFalse(DateUtils.isDateBefore(LocalDate.now(), LocalDate.now().minusDays(1), true, true));
    }
}
