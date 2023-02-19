package org.jorion.trainingtool.util;

import java.time.LocalDate;

public class DateUtils {

    /**
     * Return true if the given start date is before (or equal) to the given end date.
     *
     * @param startDate the start date
     * @param endDate   the start date
     * @param equal     true if equality is ok as well
     * @param mandatory false if any date can be null, in which case it returns true
     * @return True if the start date is before the end date
     */
    public static boolean isDateBefore(LocalDate startDate, LocalDate endDate, boolean equal, boolean mandatory) {

        if (startDate == null || endDate == null) {
            return !mandatory;
        }
        return (equal) ? !startDate.isAfter(endDate) : startDate.isBefore(endDate);
    }
}
