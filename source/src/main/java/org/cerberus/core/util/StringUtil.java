/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
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

    // Represent null string
    public static final String NULL = "null";
    public static final String HTTP_PREFIX = "http://";
    public static final String HTTPS_PREFIX = "https://";
    public static final String FILE_PREFIX = "file:/";
    public static final String FTP_PREFIX = "ftp://";
    public static final String FTPS_PREFIX = "ftps://";
    public static final String SFTP_PREFIX = "sftp://";
    private static final Logger LOG = LogManager.getLogger(StringUtil.class);
    private static final int MAX_STRING_SIZE_IN_MESSAGE = 300;
    public static final String SECRET_STRING = "XXXXXXXXXX";

    // To avoid instantiation of utility class
    private StringUtil() {
    }

    public static String getShortenVersionOfString(String in) {

        if (in.length() > MAX_STRING_SIZE_IN_MESSAGE) {
            return in.substring(0, MAX_STRING_SIZE_IN_MESSAGE) + "... (TOO LONG TO DISPLAY !! Please check Action, Control or Property detail)";
        }
        return in;
    }

    public static String getExceptionCauseFromString(Throwable ex) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String sStackTrace = sw.toString();
        String[] exString = sStackTrace.split("\n");

        StringBuilder result = new StringBuilder();

        for (String string : exString) {
            if (string.contains("Caused by")) {
                result.append(string);
                result.append(" ");
            }
        }
        return result.toString().trim();
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
     * @return true if str is a numeric value, else false
     */
    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * Check for boolean data type
     *
     * @return true if str is "true" or "false"
     */
    public static boolean isBoolean(String str) {
        return (str.equalsIgnoreCase("false") || str.equalsIgnoreCase("true"));
    }

    /**
     * This method just reformat a string in order to increase the chance it can
     * get converted to float. For ex, it replace , with .
     */
    public static String prepareToNumeric(String str) {
        String result = str.replaceAll("[^0-9.,-]", "");
        if (result.contains(",")) {
            result = result.replace(",", ".");
        }
        if (result.startsWith("-")) {
            result = "-" + result.replace("-", "");
        } else {
            result = result.replace("-", "");
        }
        int i = 0;
        while (nbChars(result, ".") > 1 && i++ < 100) {
            result = result.replaceFirst("\\.", "");
            LOG.debug("replaced " + result);
        }
        LOG.debug("Cleaned string from {} to {}", str, result);

        return result;
    }

    public static int nbChars(String str, String substr) {
        LOG.debug(str.length() - str.replace(substr, "").length());
        return str.length() - str.replace(substr, "").length();

    }

    /**
     * Check for "null" string or empty string content
     *
     * @return null safe method that returns true if the parameter is a "null"
     * string or an empty string
     */
    public static boolean isEmptyOrNULLString(String str) {
        return (isEmptyOrNull(str) || NULL.equalsIgnoreCase(str.trim()));
    }

    /**
     * Check for not "null" string or not empty string content
     *
     * @return null safe method that returns true if the parameter is NOT a
     * "null" string or empty string.
     */
    public static boolean isNotEmptyOrNULLString(String str) {
        return isNotEmptyOrNull(str) && !NULL.equalsIgnoreCase(str.trim());
    }

    /**
     * Check for null or empty string content
     *
     * @return Null safe method that returns true if the parameter is null or an
     * empty string.
     */
    public static boolean isEmptyOrNull(String str) {
        return (str == null) || (str.trim().isEmpty());
    }

    /**
     * Check for not null or empty string content
     *
     * @return Null safe method that returns true if the parameter is NOT null
     * or an empty string.
     */
    public static boolean isNotEmptyOrNull(String str) {
        return (str != null) && !str.trim().isEmpty();
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
     * Return left part of the String.
     *
     * @param string1 String to treat.
     * @param length nb of characters to keep.
     * @return the {length} first character of the string1.
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
     * Return x part of offset.
     *
     * @param offset
     * @return the {length} first character of the string1.
     */
    public static Integer[] getxFromOffset(String offset) throws NumberFormatException {
        Integer[] res = new Integer[2];
        if (offset.contains(",")) {
            String x = offset.split(",")[0];
            res[0] = Integer.valueOf(x);
            String y = offset.split(",")[1];
            res[1] = Integer.valueOf(y);
            return res;
        } else {
            throw new NumberFormatException();
        }
    }

    /**
     * Return left part of the string adding ... at the end.
     *
     * @param string1 String to treat.
     * @param length nb of characters to keep.
     * @return the {length} first character of the string1.
     */
    public static String getLeftStringPretty(String string1, int length) {
        int lengthMinus3 = length - 3;
        if (string1 == null) {
            return "";
        } else if (length >= string1.length()) {
            return string1;
        } else {
            return string1.substring(0, lengthMinus3) + "...";
        }
    }

    /**
     * Remove last n char from a string
     *
     * @param s String to treat.
     * @return a string on which the last {length} characters has been removed.
     */
    public static String removeLastChar(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        return s.substring(0, s.length() - 1);
    }

    public static String sanitize(String inputString) {
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        return policy.sanitize(inputString);
    }

    public static String secureFromSecrets(String text, Map<String, String> secrets) {
        if (secrets == null) {
            return text;
        }
        if (isEmptyOrNull(text)) {
            return text;
        }
        for (Map.Entry<String, String> entry : secrets.entrySet()) {
            /*
             * Secrets with less than 3 Characters are not really secrets. We
             * avoid to clean text with secrets that are lower than 3 characters
             * in order to replace some relevant parts of text by mistake.
             */
            if (entry.getKey().length() > 3) {
                text = text.replace(entry.getKey(), SECRET_STRING);
            }
        }
        return text;
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
            //characters with special meaning need to be encoded before applying
            //the javascript function
            stringToEncode = stringToEncode.replace("\"", "%22");
            stringToEncode = stringToEncode.replace("&", "%26");
            stringToEncode = stringToEncode.replace("#", "%23");
            stringToEncode = stringToEncode.replace("+", "%2B");
            stringToEncode = engine.eval("encodeURIComponent(\"" + stringToEncode + "\")").toString();
            //the previous special characters were encoded and additional %25 were added, therefore
            //we need to restore them and replace each additional %25 with the decoded character %
            stringToEncode = stringToEncode.replace("%2522", "%22");
            stringToEncode = stringToEncode.replace("%2526", "%26");
            stringToEncode = stringToEncode.replace("%2523", "%23");
            stringToEncode = stringToEncode.replace("%252B", "%2B");
        } catch (ScriptException ex) {
            LOG.warn(ex);
        }
        return stringToEncode;
    }

    /**
     * This method is used in order to clean the host to make it compatible with
     * Selenium. Selenium require a fully qualified host (including prefix
     * http://). Cerberus is more flexible and allow simple host such as
     * www.laredoute.fr This function checks that the host is prefixed by
     * http:// or https:// or ftp://. If protocol prefix is missing it adds by
     * default http:// if not it leave the existing host.
     *
     * @return formatted host
     */
    public static String cleanHostURL(String host) {
        String newHost = host;
        if (!(host.startsWith(HTTP_PREFIX) || host.startsWith(HTTPS_PREFIX) || host.startsWith(FTP_PREFIX) || host.startsWith(FTPS_PREFIX) || host.startsWith(FILE_PREFIX))) {
            // No prefix so we put http:// by default.
            newHost = HTTP_PREFIX + host;
        }
        LOG.debug("Cleaned host from {} to {}", host, newHost);
        return newHost;

    }

    /**
     * This method is used in order to remove from full URL host the protocol
     * part. Ex if host = http://www.laredoute.fr/ Method return
     * www.laredoute.fr
     *
     * @return formatted host
     */
    public static String removeProtocolFromHostURL(String host) {
        String newHost = host.replace(HTTP_PREFIX, "").replace(HTTPS_PREFIX, "").replace(FTP_PREFIX, "").replace(FTPS_PREFIX, "").replace(FILE_PREFIX, "");
        LOG.debug("Removed protocol host from {} to {}", host, newHost);
        return newHost;

    }

    /**
     * This method is used in determine if an url is relevant.
     *
     * @return true is URL looks OK and false on any other cases.
     */
    public static boolean isURL(String url) {
        return url.startsWith(HTTP_PREFIX)
                || url.startsWith(HTTPS_PREFIX)
                // File scheme can have no authority component, then only one slash is necessary
                || url.startsWith(FILE_PREFIX)
                || url.startsWith(FTP_PREFIX)
                || url.startsWith(FTPS_PREFIX)
                || url.startsWith(SFTP_PREFIX);
    }

    /**
     * This method is used to build an url from host, contextRoot and uri by
     * managing the /.<br>
     * For Ex : host = www.laredoute.fr/, contextRoot = /fr/, uri = /toto.jsp
     * will provide the result : www.laredoute.fr/fr/toto.jsp<br>
     * in stead of www.laredoute.fr//fr//toto.jsp<br>
     * host = www.laredoute.fr, contextRoot = fr, uri = toto.jsp will provide
     * the result : www.laredoute.fr/fr/toto.jsp<br>
     * in stead of www.laredoute.frfrtoto.jsp<br>
     * Protocol will be added in case host did not already have the protocol.
     *
     * @param host
     * @param contextRoot
     * @param uri
     * @param protocol
     * @return URL correctly formatted.
     */
    public static String getURLFromString(String host, String contextRoot, String uri, String protocol) {
        String result = "";
        if (!isEmptyOrNull(host)) {
            result += StringUtil.addSuffixIfNotAlready(host, "/");
        }
        if (!isEmptyOrNull(contextRoot)) {
            if (contextRoot.startsWith("/")) {
                contextRoot = contextRoot.substring(1);
            }
            result += StringUtil.addSuffixIfNotAlready(contextRoot, "/");
        }
        if (!isEmptyOrNull(uri)) {
            if (uri.startsWith("/")) {
                uri = uri.substring(1);
            }
            result += uri;
        }
        if (!(StringUtil.isURL(result))) { // If still does not look like an url, we add protocol string ( ex : http://) by default.
            result = protocol + result;
        }
        return result;
    }

    public static String addQueryString(String url, String queryString) {
        String result;
        if (isEmptyOrNull(queryString)) {
            return url;
        }
        url = url.trim();
        if (url.endsWith("?")) {
            result = url + queryString;
        } else if (url.contains("?")) {
            result = url + "&" + queryString;
        } else {
            result = url + "?" + queryString;
        }
        return result;
    }

    /**
     * This method is converting any part of an url string to encoded
     * considering the partial conversion of + character.
     *
     * @param url
     * @return URL correctly encoded.
     */
    public static String encodeURL(String url) throws UnsupportedEncodingException {
        return URLEncoder.encode(url, "UTF-8").replace("+", "%20");
    }

    public static String formatURLCredential(String user, String pass, String url) {
        String credential = "";
        if (!StringUtil.isEmptyOrNull(user)) {
            if (!StringUtil.isEmptyOrNull(pass)) {
                credential = user + ":" + pass + "@";
            } else {
                credential = user + "@";
            }
        }

        String firstPart = "";
        String secondPart = url;

        if (url.contains(HTTP_PREFIX)) {
            firstPart = HTTP_PREFIX;
            secondPart = url.split(HTTP_PREFIX)[1];
        } else if (url.contains(HTTPS_PREFIX)) {
            firstPart = HTTPS_PREFIX;
            secondPart = url.split(HTTPS_PREFIX)[1];
        }

        return firstPart + credential + secondPart;
    }

    public static String addSuffixIfNotAlready(String text, String suffix) {
        if (text.toUpperCase().endsWith(suffix.toUpperCase())) {
            return text;
        } else {
            return text + suffix;
        }
    }

    public static String addPrefixIfNotAlready(String text, String prefix) {
        if (text.toUpperCase().startsWith((prefix.toUpperCase()))) {
            return text;
        } else {
            return prefix + text;
        }
    }

    public static String convertToString(JSONArray jsonResult, String separator) {
        StringBuilder result = new StringBuilder();
        if (separator == null) {
            separator = ",";
        }
        try {
            if (jsonResult.length() >= 1) {
                for (int i = 0; i < jsonResult.length(); i++) {
                    if (i == 0) {
                        result.append(jsonResult.getString(i));
                    } else {
                        result.append(separator).append(jsonResult.getString(i));
                    }
                }
            }
        } catch (JSONException ex) {
            LOG.error("JSONException in convertToString.", ex);
        }
        return result.toString();
    }

    public static String convertHtmlToString(String html) {
        return HtmlUtils.htmlUnescape(html.replaceAll("\\<.*?\\>", "").replaceAll("\\n", "").replaceAll("\\r", "").replaceAll("\\t", " ").replaceAll("  ", ""));
    }

    public static String convertToString(List<String> listString, String separator) {
        StringBuilder result = new StringBuilder();
        if (separator == null) {
            separator = ",";
        }
        if (listString == null) {
            return "";
        }
        boolean first = true;
        for (String str : listString) {
            if (first) {
                first = false;
                result.append(str);
            } else {
                result.append(separator).append(str);
            }
        }
        return result.toString();
    }

    public static String getDomainFromUrl(String appURL) {
        URL appMyURL = null;
        try {
            appMyURL = new URL(StringUtil.getURLFromString(appURL, "", "", HTTP_PREFIX));
        } catch (MalformedURLException ex) {
            LOG.warn("Exception when parsing Application URL.", ex);
        }
        if (appMyURL != null) {
            String[] sURL = appMyURL.getHost().split("\\.");
            if (sURL.length > 2) {
                String fURL;
                fURL = sURL[sURL.length - 2] + "." + sURL[sURL.length - 1];
                return fURL;
            } else {
                return appMyURL.getHost();
            }
        }
        return "";
    }

    public static String getPasswordFromUrl(String appURL) {
        URL appMyURL = null;
        try {
            appMyURL = new URL(StringUtil.getURLFromString(appURL, "", "", HTTP_PREFIX));
        } catch (MalformedURLException ex) {
            LOG.warn("Exception when parsing Application URL.", ex);
        }
        if ((appMyURL != null) && (appMyURL.getUserInfo() != null)) {
            String[] userInfoArray = appMyURL.getUserInfo().split(":", 2);
            if (userInfoArray.length > 1) {
                return userInfoArray[1];
            }
        }
        return null;
    }

    public static String getPasswordFromAnyUrl(String appURL) {
        if (appURL.contains("://")) {
            appURL = appURL.split("://")[1];
            if (appURL.contains("@")) {
                appURL = appURL.split("@")[0];
                return appURL.substring(appURL.indexOf(':') + 1);
            }
        }
        return null;
    }

    /**
     * Convert a string structure in a list
     *
     * @param array Structure of the list in string format
     * @return String mapped to a list of strings
     * @throws JsonProcessingException When the array structure is not correct
     */
    public static List<String> convertStringToStringArray(String array) throws JsonProcessingException {
        return new ObjectMapper().readValue(array, new TypeReference<ArrayList<String>>() {
        });
    }

    /**
     * Convert a string structure in a list
     *
     * @param array Structure of the list in string format
     * @return String mapped to a list of doubles
     * @throws JsonProcessingException When the array structure is not correct
     * @throws NumberFormatException When an element of the list is a text and
     * not a number
     */
    public static List<Double> convertStringToDoubleArray(String array) throws NumberFormatException, JsonProcessingException {
        List<String> strings = convertStringToStringArray(array);
        return strings.stream()
                .map(Double::parseDouble)
                .collect(Collectors.toList());
    }
}
