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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.web.util.HtmlUtils;

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

    private static final Pattern urlMatch = Pattern.compile("(.*[<>' \"^]+)([a-zA-Z]+://[^<>[:space:]]+[[:alnum:]/]*)([$<> ' \"].*)");

    /**
     * The property variable {@link Pattern}
     */
    public static final Pattern PROPERTY_VARIABLE_PATTERN = Pattern.compile("%[^%]+%");

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(StringUtil.class);

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
        //replaceAll uses regex, therefore the syntax of the subdata entries ENTRY(SUBDATA) will be handled as part of the regex expression,
        //namely the characters '(' and ')'. As a consequence we need to escape those characteres and ensure that the replaceAll method
        //will consider the correct property name.
        if (formula.contains("(") || formula.contains(")")) { //Its used becuse of the replacement of subdataentries'values. e.g., Person(Name)
            formula = Pattern.quote(formula);
        }

        if (replacement != null) {
            return str.replaceAll(formula, replacement);
        }
        return str;
    }

    /**
     * Gets all properties contained into the given {@link String}
     *
     * <p>
     * A property is defined by including its name between two '%' character.
     * </p>
     *
     * @see #PROPERTY_VARIABLE_PATTERN
     * @param str the {@link String} to get all properties
     * @return a list of properties contained into the given {@link String}
     */
    public static List<String> getAllProperties(String str) {
        List<String> properties = new ArrayList<String>();
        if (str == null) {
            return properties;
        }

        Matcher propertyMatcher = PROPERTY_VARIABLE_PATTERN.matcher(str);
        while (propertyMatcher.find()) {
            String rawProperty = propertyMatcher.group();
            // Removes the first and last '%' character to only get the property name
            properties.add(rawProperty.substring(1, rawProperty.length() - 1));
        }
        return properties;
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
        } else if (length >= string1.length()) {
            return string1;
        } else {
            return string1.substring(0, length);
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
        if (text != null && !text.isEmpty()) {
            Matcher matcher = urlMatch.matcher(text);
            if (matcher.matches()) {
                return matcher.replaceAll("$1<a href=\\\"$2\\\">$2</a>$3");
            }
        }
        return text;
    }

    public static String textToHtmlConvertingURLsToLinks(String text) {
        if (text == null) {
            return text;
        }

        String escapedText = HtmlUtils.htmlEscape(text);

        return escapedText.replaceAll("(\\A|\\s)((http|https|ftp|mailto):\\S+)(\\s|\\z)",
                "$1<a href=\"$2\">$2</a>$4");
    }

    /**
     * Java function that encodes as string as the JavaScript function:
     * encodeURIComponent() Some characters (", &, #, +) need to be
     *
     * @param stringToEncode string to be encoded
     * @return string encoded
     */
    public static String encodeAsJavaScriptURIComponent(String stringToEncode) {
        try {
            ScriptEngineManager factory = new ScriptEngineManager();
            ScriptEngine engine = factory.getEngineByName("JavaScript");
            //characters we special meaning need to be encoded before applying
            //the javascript function
            stringToEncode = stringToEncode.replace("\"", "%22");
            stringToEncode = stringToEncode.replace("&", "%26");
            stringToEncode = stringToEncode.replace("#", "%23");
            stringToEncode = stringToEncode.replace("+", "%2B");
            stringToEncode = engine.eval("encodeURIComponent(\"" + stringToEncode + "\")").toString();
            //the previous special characteres were encoded and additional %25 were added, therefore 
            //we need to restore them and replace the each adicional %25 with the decoded character %
            stringToEncode = stringToEncode.replace("%2522", "%22");
            stringToEncode = stringToEncode.replace("%2526", "%26");
            stringToEncode = stringToEncode.replace("%2523", "%23");
            stringToEncode = stringToEncode.replace("%252B", "%2B");
        } catch (ScriptException ex) {
            Logger.getLogger(StringUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return stringToEncode;
    }

    /**
     *
     * This method is used in order to clean the host to make it compatible with
     * Selenium. Selenium require a fully qualitied host (including prefix
     * http://). Cerberus is more flexible and allow simple host such as
     * www.laredoute.fr This function checks that the host is prefixed by
     * http:// or https:// or ftp://. If protocol prefix is missing it adds by
     * default http:// if not it leave the existing host.
     *
     * @param host
     * @return formated host
     */
    public static String cleanHostURL(String host) {
        String newHost = host;
        if (!(host.startsWith("http://") || host.startsWith("https://") || host.startsWith("ftp://") || host.startsWith("ftps://"))) {
            // No refix so we put http:// by default.
            newHost = "http://" + host;
        }
        LOG.debug("Cleaned host from " + host + " to " + newHost);
        return newHost;

    }

    /**
     *
     * This method is used in order to remove from full URL host the protocol
     * part. Ex if host = http://www.laredoute.fr/ Method return
     * www.laredoute.fr
     *
     * @param host
     * @return formated host
     */
    public static String removeProtocolFromHostURL(String host) {
        String newHost = host;
        newHost = host.replace("http://", "").replace("https://", "").replace("ftp://", "").replace("ftps://", "");
        LOG.debug("Removed protocol host from " + host + " to " + newHost);
        return newHost;

    }

    /**
     *
     * This method is used in determine if an URL is relevant.
     *
     * @param url
     * @return true is URL looks OK and false on any other cases.
     */
    public static boolean isURL(String url) {
        return url.startsWith("http://") || url.startsWith("https://");

    }

    public static String addSuffixIfNotAlready(String text, String suffix) {
        if (text.toUpperCase().endsWith(suffix.toUpperCase())) {
            return text;
        } else {
            return text + suffix;
        }
    }

}
