/*
 * Cerberus  Copyright (C) 2013  vertigo17
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.util;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

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

    private static final Pattern urlMatch = Pattern.compile("([a-zA-Z]*://[^<>[:space:]]+[[:alnum:]/])");

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
        return (str == null || NULL.equalsIgnoreCase(str) || "".equalsIgnoreCase(str));
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
     * @param testIn String that needs to be cleaned
     * @return a clean string.
     */
    public static String getCleanCSVTextField(String textIn) {
        return textIn.replaceAll("\"", "\"\"");
    }
    
    
    
    public static String sanitize(String inputString) {
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        return policy.sanitize(inputString);
    }

    public static String replaceUrlByLinkInString(String text) {
        if (text != null && text.length() > 0) {
            Matcher matcher = urlMatch.matcher(text);
            if (matcher.matches()) {
                return matcher.replaceAll("<a href=\"$1\">$1</a>");
            }
        }
        return text;
    }
}
