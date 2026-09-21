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
package org.cerberus.core.mcp.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a {@code curl} command line into the pieces Cerberus needs to describe a REST service.
 *
 * <p>This is the server-side counterpart of the {@code parseCurlCommand} function used by the
 * App Service screen ({@code include/transversal/AppService.html}). The tokenizer, the recognised
 * flags and the method-inference rule are deliberately identical, so that importing the same
 * command from the GUI and through the MCP tool yields the same service.</p>
 *
 * <p>The one intentional difference is that the request body is never reformatted here. The GUI
 * pretty-prints a JSON body because a human is about to read and edit it in the editor; over MCP
 * nobody sees the result before it is stored, and re-serialising a payload would silently change
 * its bytes — which breaks any request whose body is signed or whitespace-sensitive.</p>
 */
public final class CurlCommandParser {

    /**
     * Matches one shell token: a double-quoted run, a single-quoted run, or a bare run of
     * non-space characters. Mirrors the regular expression used by the GUI parser.
     */
    private static final Pattern TOKEN = Pattern.compile(
            "\"((?:\\\\.|[^\"\\\\])*)\"|'((?:\\\\.|[^'\\\\])*)'|(\\S+)");

    /** Unescapes {@code \"} and {@code \\} inside a double-quoted token. */
    private static final Pattern DOUBLE_QUOTE_ESCAPE = Pattern.compile("\\\\([\"\\\\])");

    /** A backslash at the end of a line continues the command on the next one. */
    private static final Pattern LINE_CONTINUATION = Pattern.compile("\\\\\\r?\\n");

    /** An interactive shell prompt sometimes copied along with the command. */
    private static final Pattern LEADING_PROMPT = Pattern.compile("^[$>]\\s+");

    private CurlCommandParser() {
    }

    /**
     * One header parsed from a {@code -H} flag.
     *
     * @param key   the header name, trimmed.
     * @param value the header value, trimmed.
     */
    public record CurlHeader(String key, String value) {
    }

    /**
     * The outcome of parsing a curl command.
     *
     * @param method  the HTTP method, always upper case, inferred when the command did not say.
     * @param url     the request URL, empty when the command carried none.
     * @param headers the headers, in the order they appeared.
     * @param body    the request body, verbatim, empty when the command carried none.
     * @param user    the raw {@code -u} argument ({@code user:password}), empty when absent.
     */
    public record ParsedCurl(String method, String url, List<CurlHeader> headers, String body, String user) {
    }

    /**
     * Parses a curl command line.
     *
     * @param command the command, which may span several lines using backslash continuations.
     * @return the parsed command, or {@code null} when the input is not a curl command or carries
     *         nothing usable.
     */
    public static ParsedCurl parse(String command) {
        if (command == null) {
            return null;
        }

        String normalised = LINE_CONTINUATION.matcher(command.trim()).replaceAll(" ").trim();
        normalised = LEADING_PROMPT.matcher(normalised).replaceFirst("");

        if (!normalised.toLowerCase().startsWith("curl")) {
            return null;
        }
        // Guard against a word merely starting with "curl" (e.g. "curlopts=…").
        if (normalised.length() > 4 && !Character.isWhitespace(normalised.charAt(4))) {
            return null;
        }

        List<String> tokens = tokenize(normalised);
        if (tokens.isEmpty()) {
            return null;
        }
        tokens.remove(0); // drop the leading "curl"

        String method = "";
        String url = "";
        String body = "";
        String user = "";
        List<CurlHeader> headers = new ArrayList<>();

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);

            switch (token) {
                case "-X", "--request" -> method = next(tokens, ++i).toUpperCase();
                case "-H", "--header" -> {
                    String header = next(tokens, ++i);
                    int separator = header.indexOf(':');
                    if (separator > 0) {
                        headers.add(new CurlHeader(
                                header.substring(0, separator).trim(),
                                header.substring(separator + 1).trim()));
                    }
                }
                case "-d", "--data", "--data-raw", "--data-binary", "--data-ascii" -> body = next(tokens, ++i);
                case "--data-urlencode", "-F", "--form" -> {
                    String part = next(tokens, ++i);
                    body = body.isEmpty() ? part : body + "&" + part;
                }
                case "-u", "--user" -> user = next(tokens, ++i);
                case "--url" -> url = next(tokens, ++i);
                // No-argument flags that describe transport behaviour Cerberus does not store.
                case "--compressed", "-s", "--silent", "-k", "--insecure", "-L", "--location",
                     "-i", "--include", "-v", "--verbose", "-G", "--get" -> {
                }
                // Flags whose argument is irrelevant here, skipped along with their value so the
                // value is never mistaken for the URL.
                case "-o", "--output", "--connect-timeout", "-m", "--max-time",
                     "-A", "--user-agent", "-e", "--referer", "-b", "--cookie" -> i++;
                default -> {
                    if (url.isEmpty() && !token.startsWith("-")) {
                        url = token;
                    }
                }
            }
        }

        // curl itself switches to POST as soon as a body is present, so the same inference is
        // applied here rather than defaulting to GET and producing a request that sends nothing.
        if (method.isEmpty()) {
            method = body.isEmpty() ? "GET" : "POST";
        }

        if (url.isEmpty() && headers.isEmpty() && body.isEmpty()) {
            return null;
        }

        return new ParsedCurl(method, url, headers, body, user);
    }

    /**
     * Splits a command into shell tokens, honouring both quoting styles and unescaping the
     * contents of double-quoted tokens.
     */
    private static List<String> tokenize(String command) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = TOKEN.matcher(command);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                tokens.add(DOUBLE_QUOTE_ESCAPE.matcher(matcher.group(1)).replaceAll("$1"));
            } else if (matcher.group(2) != null) {
                // Single quotes are literal in a shell, so the content is taken as-is.
                tokens.add(matcher.group(2));
            } else {
                tokens.add(matcher.group(3));
            }
        }

        return tokens;
    }

    /**
     * Returns the token at {@code index}, or an empty string when the command ended on a flag
     * that expects a value.
     */
    private static String next(List<String> tokens, int index) {
        return index < tokens.size() ? tokens.get(index) : "";
    }
}
