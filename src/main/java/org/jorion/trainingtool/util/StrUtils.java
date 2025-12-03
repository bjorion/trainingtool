package org.jorion.trainingtool.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.math.NumberUtils;

import jakarta.servlet.http.HttpServletRequest;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class StrUtils {

    /**
     * Return the request parameter value from the http request as an int.
     *
     * @param req      the Http request
     * @param name     the parameter name
     * @param defValue the parameter default value
     * @return the value of the parameter if found, otherwise the default value
     */
    public static int getParameterAsInt(HttpServletRequest req, String name, int defValue) {

        return NumberUtils.toInt(req.getParameter(name), defValue);
    }

    /**
     * Modify a string to use sql wild cards instead of standard ones.
     *
     * @param value the string to parse
     * @return the string modified
     */
    public static String likeSqlString(String value) {

        return StringUtils.stripToEmpty(value).toLowerCase()
                .replaceAll("\\*", "%")
                .replaceAll("\\?", "_") + "%";
    }

    /**
     * Validate the given URL.
     *
     * @param url       the URL to validate
     * @param mandatory false if the url can be null
     * @return True if the given URL is valid
     */
    public static boolean isUrlValid(String url, boolean mandatory) {

        boolean valid = !mandatory;
        if (!StringUtils.isBlank(url)) {
            valid = url.startsWith("http://") || url.startsWith("https://");
        }
        return valid;
    }

    /**
     * Parse a value to make sure it's a valid positive decimal number.
     * <p>
     * Return null if the value is blank. Return -1 if the value is not a valid positive decimal number. Return the
     * value formatted as #.## otherwise. Examples:
     * <ul>
     * <li>"" or null : null
     * <li>"abc" : "-1"
     * <li>"123" : "123"
     * <li>"123.45" : "123.45"
     * <li>"123.456" : "123.45"
     * <li>"123.456.789" : "-1"
     * <li>"123,45" : -1
     * <li>"-123" : -1
     * </ul>
     *
     * @param value the value to parse
     * @return the parsed value as a String (null if empty, -1 if invalid)
     */
    public static String parsePositiveFloat(String value) {

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        DecimalFormat df = (DecimalFormat) nf;
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(2);
        df.setGroupingUsed(false);

        // empty values
        final String defValue = "-1";
        String str = StringUtils.trimToNull(value);
        if (str == null) {
            return null;
        }

        // invalid values
        float f = NumberUtils.toFloat(str, -1);
        if (f < 0) {
            return defValue;
        }

        // valid values
        int i = (int) (f * 100);
        f = ((float) i) / 100;
        return df.format(f);
    }

    /**
     * Search a given string in an array of string, retrieve its index.
     *
     * @param propertyNames an array of strings
     * @param search        the string to look for
     * @return the index of the search argument in the string array, or -1 if not found
     */
    public static int findIndex(String[] propertyNames, String search) {

        int index = -1;
        for (int i = 0; i < propertyNames.length; i++) {
            String name = propertyNames[i];
            if (Strings.CS.equals(name, search)) {
                index = i;
                break;
            }
        }
        return index;
    }
}
