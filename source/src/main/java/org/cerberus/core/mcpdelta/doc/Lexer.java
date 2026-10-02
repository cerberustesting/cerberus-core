/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
package org.cerberus.core.mcpdelta.doc;

import org.cerberus.core.mcpdelta.util.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * The lexical layer shared by every Delta document (testcases and other Cerberus objects): logical lines
 * (with \"\"\" blocks spanning several physical lines), quoted strings with escapes, key=value tokens and the
 * trailing "// description".
 */
public final class Lexer {

    private final String where;
    private final List<Diagnostic> diagnostics;

    public Lexer(String where, List<Diagnostic> diagnostics) {
        this.where = where;
        this.diagnostics = diagnostics;
    }

    public record Line(int number, int indent, String text) {
    }

    public record Token(Kind kind, String text, String key, int col) {
        public boolean isWord(String w) {
            return kind == Kind.WORD && text.equals(w);
        }
    }

    public enum Kind { WORD, STR, KV }

    public List<Line> logicalLines(String text) {
        List<Line> lines = new ArrayList<>();
        String[] physical = text.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        int i = 0;
        while (i < physical.length) {
            int start = i;
            StringBuilder sb = new StringBuilder(physical[i]);
            // Join the following physical lines while a """ block is still open.
            while (openTriple(sb) && i + 1 < physical.length) {
                i++;
                sb.append('\n').append(physical[i]);
            }
            if (openTriple(sb)) {
                diagnostics.add(Diagnostic.error(where, start + 1, "unclosed \"\"\" block", "close it with \"\"\""));
            }
            String raw = sb.toString();
            int indent = 0;
            while (indent < raw.length() && (raw.charAt(indent) == ' ' || raw.charAt(indent) == '\t')) {
                indent += 1;
            }
            int width = 0;
            for (int k = 0; k < indent; k++) {
                width += raw.charAt(k) == '\t' ? 2 : 1;
            }
            String body = raw.substring(indent);
            if (!body.isBlank() && !body.startsWith("#")) {
                lines.add(new Line(start + 1, width, body.stripTrailing()));
            }
            i++;
        }
        return lines;
    }

    private static boolean openTriple(CharSequence s) {
        boolean inTriple = false;
        boolean inString = false;
        for (int i = 0; i < s.length(); i++) {
            if (!inString && i + 2 < s.length() + 0 && i + 3 <= s.length() && s.subSequence(i, i + 3).toString().equals("\"\"\"")) {
                inTriple = !inTriple;
                i += 2;
                continue;
            }
            if (inTriple) {
                continue;
            }
            char c = s.charAt(i);
            if (c == '\\' && inString) {
                i++;
            } else if (c == '"') {
                inString = !inString;
            }
        }
        return inTriple;
    }

    /** Tokenises a line segment; the trailing "// description" is returned in out[0]. */
    public List<Token> tokens(String s, int lineNo, String[] description) {
        List<Token> out = new ArrayList<>();
        int pos = 0;
        int n = s.length();
        while (pos < n) {
            char c = s.charAt(pos);
            if (Character.isWhitespace(c)) {
                pos++;
                continue;
            }
            if (c == '/' && pos + 1 < n && s.charAt(pos + 1) == '/') {
                if (description != null) {
                    description[0] = s.substring(pos + 2).trim();
                }
                return out;
            }
            if (c == '"') {
                int[] end = new int[1];
                String value = readString(s, pos, end, lineNo);
                out.add(new Token(Kind.STR, value, null, pos));
                pos = end[0];
                continue;
            }
            int start = pos;
            while (pos < n && !Character.isWhitespace(s.charAt(pos)) && s.charAt(pos) != '=' && s.charAt(pos) != '"') {
                pos++;
            }
            if (pos < n && s.charAt(pos) == '=' && pos > start) {
                String key = s.substring(start, pos);
                pos++;
                if (pos < n && s.charAt(pos) == '"') {
                    int[] end = new int[1];
                    String value = readString(s, pos, end, lineNo);
                    out.add(new Token(Kind.KV, value, key, start));
                    pos = end[0];
                } else {
                    int vs = pos;
                    while (pos < n && !Character.isWhitespace(s.charAt(pos))) {
                        pos++;
                    }
                    out.add(new Token(Kind.KV, s.substring(vs, pos), key, start));
                }
                continue;
            }
            if (pos < n && s.charAt(pos) == '=' && pos == start) {
                pos++;
                out.add(new Token(Kind.WORD, "=", null, start));
                continue;
            }
            if (pos < n && s.charAt(pos) == '"') {
                // A word glued to a quote, like abc"def": treat the word, the quote starts a new token.
                out.add(new Token(Kind.WORD, s.substring(start, pos), null, start));
                continue;
            }
            out.add(new Token(Kind.WORD, s.substring(start, pos), null, start));
        }
        return out;
    }

    public String readString(String s, int start, int[] end, int lineNo) {
        if (s.startsWith("\"\"\"", start)) {
            int close = s.indexOf("\"\"\"", start + 3);
            if (close < 0) {
                end[0] = s.length();
                return s.substring(start + 3);
            }
            String body = s.substring(start + 3, close);
            if (body.startsWith("\n")) {
                body = body.substring(1);
            }
            int lastNl = body.lastIndexOf('\n');
            if (lastNl >= 0 && body.substring(lastNl + 1).isBlank()) {
                body = body.substring(0, lastNl);
            }
            end[0] = close + 3;
            return body;
        }
        StringBuilder sb = new StringBuilder();
        int i = start + 1;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char e = s.charAt(i + 1);
                switch (e) {
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'r' -> sb.append('\r');
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case 'u' -> {
                        if (i + 5 < s.length() && s.substring(i + 2, i + 6).matches("[0-9a-fA-F]{4}")) {
                            sb.append((char) Integer.parseInt(s.substring(i + 2, i + 6), 16));
                            i += 4;
                        } else {
                            sb.append('\\').append(e);
                        }
                    }
                    default -> sb.append('\\').append(e);
                }
                i += 2;
                continue;
            }
            if (c == '"') {
                end[0] = i + 1;
                return sb.toString();
            }
            if (c == '\n') {
                break;
            }
            sb.append(c);
            i++;
        }
        diagnostics.add(Diagnostic.error(where, lineNo, "unclosed quote in " + Text.truncate(s.substring(start), 40),
                "close the string with \", and write a quote inside a value as \\\""));
        end[0] = s.length();
        return sb.toString();
    }

    /** Splits "left: title" at the first colon that is not inside quotes. */
    public static int titleColon(String s) {
        boolean inString = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && inString) {
                i++;
            } else if (c == '"') {
                inString = !inString;
            } else if (c == ':' && !inString) {
                return i;
            }
        }
        return -1;
    }

}
