/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.util;

/**
 * Class used in jsp or servlet in order to centralize all the parameter
 * parsing.
 *
 * @author bdumont
 */
public final class ParameterParserUtil {

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
        if ((inParam != null) && (inParam.equalsIgnoreCase("Y") || inParam.equalsIgnoreCase("yes") || inParam.equalsIgnoreCase("true"))) {
            return true;
        }
        if ((inParam != null) && (inParam.equalsIgnoreCase("N") || inParam.equalsIgnoreCase("no") || inParam.equalsIgnoreCase("false"))) {
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
