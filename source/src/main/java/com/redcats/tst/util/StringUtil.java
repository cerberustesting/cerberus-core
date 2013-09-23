package com.redcats.tst.util;

import java.util.List;
import java.util.Random;

/**
 * Utility class centralizing string utility methods
 *
 * @author Tiago Bernardes
 * @version 1.0, 10/01/2013
 * @since 2.0.0
 */
public final class StringUtil {

    /**
     * Represent null string
     */
    public static final String NULL = "null";

    /**
     * To avoid instanciation of utility class
     */
    private StringUtil() {
    }

    /**
     * Determine if the passed parameter boolean value
     *
     * @param parse to check for boolean validity
     * @return true if parse is one of the following : Y, yes, true
     */
    public static boolean parseBoolean(String parse) {
        return (parse.equalsIgnoreCase("Y") || parse.equalsIgnoreCase("yes") || parse.equalsIgnoreCase("true"));
    }

    /**
     * Check for numeric data type
     *
     * @param str
     * @return true if str is a numeric value, else false
     */
    public static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * Check for null string content
     *
     * @param str
     * @return true if the parameter is a "null"
     */
    public static boolean isNull(String str) {
        return (str.equalsIgnoreCase(NULL) || str.equalsIgnoreCase(""));
    }

    /**
     * Check for null or empty string content
     *
     * @param str
     * @return true if the parameter is a "null" or empty string.
     */
    public static boolean isNullOrEmpty(String str) {
        if ((str == null) || (str.trim().equalsIgnoreCase(""))) {
            return true;
        }
        return false;
    }

    /**
     * Check for null or empty string content
     *
     * @param str
     * @return true if the parameter is a "null" or empty string.
     */
    public static boolean isNullOrEmptyOrNull(String str) {
        if ((str == null) || (str.trim().equalsIgnoreCase("")) || (str.trim().equalsIgnoreCase("null"))) {
            return true;
        }
        return false;
    }

    /**
     * Check for null string
     *
     * @param str
     * @param formula
     * @param replacement
     * @return if replacement not null, replace in str, all formula with
     * replacement
     */
    public static String replaceAllProperties(String str, String formula, String replacement) {
        if (replacement != null) {
            return str.replaceAll(formula, replacement);
        }
        return str;
    }

    /**
     * Generate a random string using current time and charset
     *
     * @param length of the random string to generate
     * @param charset use to generate random value
     * @return random string, empty if charset is null or length <= 0
     */
    public static String getRandomString(int length, String charset) {
        Random rand = new Random(System.currentTimeMillis());
        StringBuilder sb = new StringBuilder();

        if (charset == null) {
            return sb.toString();
        }

        for (int i = 0; i < length; i++) {
            int pos = rand.nextInt(charset.length());
            sb.append(charset.charAt(pos));
        }

        return sb.toString();
    }

    /**
     * Generate a random string using current time and charset
     *
     * @param length of the random string to generate
     * @param charset use to generate random value
     * @return random string, empty if charset is null or length <= 0
     */
    public static String getLeftString(String string1, int length) {
        if (string1 == null) {
            return "";
        } else {
            if (length >= string1.length()) {
                return string1;
            } else {
                return string1.substring(0, length);
            }
        }
    }

    /**
     *
     * @param obj List of generic object that have a toString Method
     * implementation
     * @return a String that has all obj.toString values separated by , and
     * surounded by '
     */
    public static String getInSQLClause(List<?> obj) {
        if (obj == null) {
            return "";
        }
        if (obj.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        result.append("in (");
        for (Object myObj : obj) {
            result.append("'");
            result.append(myObj.toString());
            result.append("',");
        }
        String res = result.toString().substring(0, (result.length() - 1));
        return res + ")";
    }
}
