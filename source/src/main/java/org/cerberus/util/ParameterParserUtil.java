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

/**
 * Class used in jsp or servlet in order to centralize all the parameter
 * parsing.
 *
 * @author bdumont
 */
public final class ParameterParserUtil {

    private static final String DEFAULT_SQL_STRING_VALUE = "";

    /**
     * To avoid instanciation of utility.
     */
    private ParameterParserUtil() {
    }

    /**
     * @param inString
     * @return "%" if the parameter is empty. This can be used before calling a
     * DAO where LIKE are used.
     */
    public static String wildcardIfEmpty(String inString) {
        if ((inString == null) || (inString.equalsIgnoreCase(""))) {
            return "%";
        }
        return inString;
    }
    
    public static String wildcardOrIsNullIfEmpty(String column, String inString) {
        StringBuilder sb = new StringBuilder();
        if ((inString == null) || (inString.equalsIgnoreCase(""))) {
            sb.append("'%' or ");
            sb.append(column);
            sb.append(" is null");
            return sb.toString();
        }
        sb.append("'");
        sb.append(inString);
        sb.append("'");
        return sb.toString();
    }
    
    /**
     * @param inParam
     * @return an empty string if the inParam is empty or null. It returns
     * inParam if OK.
     */
    public static String parseStringParam(String inParam, String defaultVal) {
        if (inParam != null && inParam.compareTo("") != 0) {
            return inParam;
        }
        return defaultVal;
    }

    /**
     * @param inParam
     * @return an empty string if the inParam is null. It returns inParam if OK.
     */
    public static String returnEmptyStringIfNull(String inParam) {
        if (inParam != null) {
            return inParam;
        }

        return DEFAULT_SQL_STRING_VALUE;
    }

    /**
     * @param inParam
     * @return 0 if the inParam is empty or null. It returns inParam converted
     * to Integer if OK.
     * @throws NumberFormatException if inParam isn't numeric
     */
    public static int parseIntegerParam(String inParam, int defaultVal) {
        if (inParam != null && inParam.compareTo("") != 0) {
            return Integer.valueOf(inParam);
        }
        return defaultVal;
    }

    /**
     * @param inParam
     * @return 0 if the inParam is empty or null. It returns inParam converted
     * to Integer if OK.
     * @throws NumberFormatException if inParam isn't numeric
     */
    public static long parseLongParam(String inParam, long defaultVal) {
        if (inParam != null && inParam.compareTo("") != 0) {
            return Long.parseLong(inParam);
        }
        return defaultVal;
    }

    /**
     * @param inParam
     * @return true if "yes", "true" or "Y", false if "no", "false" or "N" and
     * defaultVal if any over value
     */
    public static boolean parseBooleanParam(String inParam, boolean defaultVal) {
        if (inParam == null) {
            return defaultVal;
        }
        if ((inParam.equalsIgnoreCase("Y") || inParam.equalsIgnoreCase("yes") || inParam.equalsIgnoreCase("true"))) {
            return true;
        }
        if ((inParam.equalsIgnoreCase("N") || inParam.equalsIgnoreCase("no") || inParam.equalsIgnoreCase("false"))) {
            return false;
        }
        return defaultVal;
    }

    /**
     * @param inParam
     * @return true if "yes", "true" or "Y", false if "no", "false" or "N" and
     * defaultVal if any over value
     */
    public static String securePassword(String value, String property) {
        if (property == null) {
            return value;
        }
        if (property.contains("PASS")) {
            return "XXXXXXXXXX";
        } else {
            return value;
        }
    }
}
