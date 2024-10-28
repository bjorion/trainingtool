package org.jorion.trainingtool.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Set of utility methods for the SSIN.
 */
public class SsinUtils {

    private static final int SSIN_LEN = 11;

    /**
     * Validate the given SSIN.
     *
     * @param ssin the SSIN to validate
     * @return True if the given SSIN is valid
     */
    public static boolean isSsinValid(String ssin, boolean mandatory) {

        String newSsin = StringUtils.trimToEmpty(ssin);
        if (newSsin.isEmpty() && !mandatory) {
            return true;
        }
        if (newSsin.length() != SSIN_LEN) {
            return false;
        }

        long value = NumberUtils.toLong(newSsin);

        // First, check positive number, but not too big.
        if ((value <= 0) || (value > 999999_999_99L)) {
            return false;
        }

        // 1) check the check code (last 2 digits --- modulo 97).
        // 2) we have to retrieve radix and suffix
        long radix = value / 100;
        long suffix = value % 100;

        // 3) we have to verify checksum so ...
        // 3.1) calculate checksum
        // 3.1.1) First case: born before year 2000
        // 3.1.2) is checksum correct?
        if (97 - (radix % 97) != suffix) { // problem
            // 3.2.1) Let's try the second case: born after year 2000
            // The convention is to add a "2" in front of the radix for checksum verification
            // problem
            return 97 - ((2_00_00_00_000 + radix) % 97) == suffix;
        }
        return true;
    }

    public static String formatSsin(String ssin) {

        String newSsin = StringUtils.trimToEmpty(ssin);
        if (newSsin.length() != SSIN_LEN) {
            return newSsin;
        }

        String date = newSsin.substring(0, 6);
        String index = newSsin.substring(6, 9);
        String checksum = newSsin.substring(9, 11);
        return String.format("%s-%s.%s", date, index, checksum);
    }
}
