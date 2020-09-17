/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    /**
     * Represent null string
     */
    public static final String NULL = "null";

    private static final Pattern urlMatch = Pattern.compile("(.*[<>' \"^]+)([a-zA-Z]+://[^<>[:space:]]+[[:alnum:]/]*)([$<> ' \"].*)");

    private static final Logger LOG = LogManager.getLogger(StringUtil.class);

    /**
     * To avoid instantiation of utility class
     */
    private StringUtil() {
    }

    /**
     *
     * @param ex
     * @return
     */
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
     * @param str
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
     * @param str
     * @return true if str is "true" or "false"
     */
    public static boolean isBoolean(String str) {
        return (str.equalsIgnoreCase("false") || str.equalsIgnoreCase("true"));
    }

    /**
     * This method just reformat a string in order to increase the change it can
     * get converted to float. For ex, it replace , with .
     *
     * @param str
     * @return
     */
    public static String prepareToNumeric(String str) {
        if (str.contains(",")) {
            return str.replace(",", ".");
        }
        return str;
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
     * @return the {length} first caracter of the string1.
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
     * Return left part of the string adding ... at the end.
     *
     * @param string1 String to treat.
     * @param length nb of characters to keep.
     * @return the {length} first caracter of the string1.
     */
    public static String getLeftStringPretty(String string1, int length) {
        int lengthminus3 = length - 3;
        if (string1 == null) {
            return "";
        } else if (length >= string1.length()) {
            return string1;
        } else {
            return string1.substring(0, lengthminus3) + "...";
        }
    }

    /**
     * Remove last n char from a string
     *
     * @param s String to treat.
     * @param length nb of characters to remove.
     * @return a string on which the last {length} characters has been removed.
     */
    public static String removeLastChar(String s, int length) {
        if (s == null || s.length() == 0) {
            return s;
        }
        return s.substring(0, s.length() - 1);
    }

    /**
     *
     * @param textIn
     * @return a clean string.
     */
    public static String getCleanCSVTextField(String textIn) {
        return textIn.replaceAll("\"", "\"\"");
    }

    /**
     *
     * @param inputString
     * @return
     */
    public static String sanitize(String inputString) {
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        return policy.sanitize(inputString);
    }

    /**
     *
     * @param text
     * @return
     */
    public static String replaceUrlByLinkInString(String text) {
        if (text != null && !text.isEmpty()) {
            Matcher matcher = urlMatch.matcher(text);
            if (matcher.matches()) {
                return matcher.replaceAll("$1<a href=\\\"$2\\\">$2</a>$3");
            }
        }
        return text;
    }

    /**
     *
     * @param text
     * @return
     */
    public static String replaceInvisibleCharbyString(String text) {
        if (text != null && !text.isEmpty()) {
            return text.replace("\n", "\\n");
        }
        return text;
    }

    /**
     *
     * @param text
     * @return
     */
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
            LOG.warn(ex);
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
        return url.startsWith("http://")
                || url.startsWith("https://")
                // File scheme can have no authority component, then only one slash is necessary
                || url.startsWith("file:/")
                || url.startsWith("ftp://");
    }

    /**
     *
     * This method is used to build an URL from host, contextroot and uri by
     * managing the /.<br>
     * For Ex : host = www.laredoute.fr/, contextroot = /fr/, uri = /toto.jsp
     * will provide the result : www.laredoute.fr/fr/toto.jsp<br>
     * in stead of www.laredoute.fr//fr//toto.jsp<br>
     * host = www.laredoute.fr, contextroot = fr, uri = toto.jsp will provide
     * the result : www.laredoute.fr/fr/toto.jsp<br>
     * in stead of www.laredoute.frfrtoto.jsp<br>
     * Protocol will be added in case host did not already have the protocol.
     *
     * @param host
     * @param contextRoot
     * @param uri
     * @param protocol
     * @return URL correctly formated.
     */
    public static String getURLFromString(String host, String contextRoot, String uri, String protocol) {
        String result = "";
        if (!isNullOrEmpty(host)) {
            result += StringUtil.addSuffixIfNotAlready(host, "/");
        }
        if (!isNullOrEmpty(contextRoot)) {
            if (contextRoot.startsWith("/")) {
                contextRoot = contextRoot.substring(1);
            }
            result += StringUtil.addSuffixIfNotAlready(contextRoot, "/");
        }
        if (!isNullOrEmpty(uri)) {
            if (uri.startsWith("/")) {
                uri = uri.substring(1);
            }
            result += uri;
        }
        if (!(StringUtil.isURL(result))) { // If still does not look like an URL, we add protocol string ( ex : http://) by default.
            result = protocol + result;
        }
        return result;
    }

    public static String addQueryString(String URL, String queryString) {
        String result = "";
        if (isNullOrEmpty(queryString)) {
            return URL;
        }
        URL = URL.trim();
        if (URL.endsWith("?")) {
            result = URL + queryString;
        } else if (URL.contains("?")) {
            result = URL + "&" + queryString;
        } else {
            result = URL + "?" + queryString;
        }
        return result;
    }

    public static String formatURLCredential(String user, String pass, String url) {
        String credential = "";
        if (!StringUtil.isNullOrEmpty(user)) {
            if (!StringUtil.isNullOrEmpty(pass)) {
                credential = user + ":" + pass + "@";
            } else {
                credential = user + "@";
            }
        }

        String firstPart = "";
        String seccondPart = url;

        if (url.contains("http://")) {
            firstPart = "http://";
            seccondPart = url.split("http://")[1];
        } else if (url.contains("https://")) {
            firstPart = "https://";
            seccondPart = url.split("https://")[1];
        }

        return firstPart + credential + seccondPart;
    }

    /**
     *
     * @param text
     * @param suffix
     * @return
     */
    public static String addSuffixIfNotAlready(String text, String suffix) {
        if (text.toUpperCase().endsWith(suffix.toUpperCase())) {
            return text;
        } else {
            return text + suffix;
        }
    }

    /**
     *
     * @param text
     * @param prefix
     * @return
     */
    public static String addPrefixIfNotAlready(String text, String prefix) {
        if (text.toUpperCase().startsWith((prefix.toUpperCase()))) {
            return text;
        } else {
            return prefix + text;
        }
    }

    /**
     *
     * @param jsonResult
     * @param separator
     * @return
     */
    public static String convertToString(JSONArray jsonResult, String separator) {
        String result = "";
        if (separator == null) {
            separator = ",";
        }
        try {
            if (jsonResult.length() >= 1) {
                for (int i = 0; i < jsonResult.length(); i++) {
                    if (i == 0) {
                        result = jsonResult.getString(i);
                    } else {
                        result += separator + jsonResult.getString(i);
                    }
                }
            }
        } catch (JSONException ex) {
            LOG.error("JSONException in convertToString.", ex);
        }
        return result;
    }

    /**
     *
     * @param listString
     * @param separator
     * @return
     */
    public static String convertToString(List<String> listString, String separator) {
        String result = "";
        if (separator == null) {
            separator = ",";
        }
        boolean first = true;
        if (listString == null) {
            return "";
        }
        for (String string : listString) {
            if (first == true) {
                first = false;
                result = string;
            } else {
                result += separator + string;
            }

        }
        return result;
    }

    public static String getDomainFromUrl(String appURL) {
        URL appMyURL = null;
        try {
            appMyURL = new URL(StringUtil.getURLFromString(appURL, "", "", "http://"));
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
}
