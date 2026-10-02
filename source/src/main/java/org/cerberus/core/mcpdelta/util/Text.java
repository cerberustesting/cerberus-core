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
package org.cerberus.core.mcpdelta.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Small text helpers shared by the document format, the diagnostics and the diff. */
public final class Text {

    private Text() {
    }

    public static String nz(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** Quotes a value the way the document format reads it back: "..." with \" \\ \n \t escapes. */
    public static String quote(String value) {
        String v = nz(value);
        StringBuilder sb = new StringBuilder(v.length() + 2).append('"');
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                // Invisible look-alikes of a space would make a copied value silently differ.
                case '\u00a0', '\u202f', '\u2007', '\u200b' -> sb.append(String.format("\\u%04x", (int) c));
                default -> sb.append(c);
            }
        }
        return sb.append('"').toString();
    }

    /** A bare word needs no quotes when it has no space, quote, comma or special leading char. */
    public static String quoteIfNeeded(String value) {
        String v = nz(value);
        if (v.isEmpty()) {
            return "\"\"";
        }
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            if (Character.isWhitespace(c) || c == '"' || c == '\\' || c == '=' || c == '#') {
                return quote(v);
            }
        }
        if (v.startsWith("//")) {
            return quote(v);
        }
        return v;
    }

    public static int levenshtein(String a, String b) {
        String s = a.toLowerCase(Locale.ROOT);
        String t = b.toLowerCase(Locale.ROOT);
        int[] prev = new int[t.length() + 1];
        int[] cur = new int[t.length() + 1];
        for (int j = 0; j <= t.length(); j++) {
            prev[j] = j;
        }
        for (int i = 1; i <= s.length(); i++) {
            cur[0] = i;
            for (int j = 1; j <= t.length(); j++) {
                int cost = s.charAt(i - 1) == t.charAt(j - 1) ? 0 : 1;
                cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev;
            prev = cur;
            cur = tmp;
        }
        return prev[t.length()];
    }

    /** Closest known names, best first: what a "did you mean" needs. */
    public static List<String> closest(String wanted, Collection<String> known, int max) {
        String w = nz(wanted).toLowerCase(Locale.ROOT);
        return known.stream()
                .sorted(Comparator.comparingInt((String k) -> {
                    String kl = k.toLowerCase(Locale.ROOT);
                    int d = levenshtein(w, kl);
                    // A name that contains what was typed is a much better guess than its edit distance says.
                    if (!w.isEmpty() && (kl.contains(w) || w.contains(kl))) {
                        d = Math.min(d, 1 + Math.abs(kl.length() - w.length()) / 4);
                    }
                    return d;
                }).thenComparing(k -> k))
                .limit(max)
                .toList();
    }

    public static String truncate(String value, int max) {
        String v = nz(value);
        return v.length() <= max ? v : v.substring(0, Math.max(0, max - 1)) + "…";
    }

    public static String oneLine(String value) {
        return nz(value).replace("\r", "").replace('\n', ' ').trim();
    }

    public static int count(String haystack, String needle) {
        if (needle.isEmpty()) {
            return 0;
        }
        int n = 0;
        int from = 0;
        while (true) {
            int i = haystack.indexOf(needle, from);
            if (i < 0) {
                return n;
            }
            n++;
            from = i + needle.length();
        }
    }

    /** Rough token estimate (4 characters per token), used for read budgets. */
    public static int tokens(String value) {
        return (nz(value).length() + 3) / 4;
    }

    /** One line of a diff: ' ' kept, '-' removed, '+' added. */
    public record DiffLine(char kind, String text) {
    }

    /** Line diff by longest common subsequence. Documents are small, so the quadratic table is fine. */
    public static List<DiffLine> diffLines(String before, String after) {
        String[] a = before.isEmpty() ? new String[0] : before.split("\n", -1);
        String[] b = after.isEmpty() ? new String[0] : after.split("\n", -1);
        int n = a.length;
        int m = b.length;
        int[][] lcs = new int[n + 1][m + 1];
        for (int i = n - 1; i >= 0; i--) {
            for (int j = m - 1; j >= 0; j--) {
                lcs[i][j] = a[i].equals(b[j]) ? lcs[i + 1][j + 1] + 1 : Math.max(lcs[i + 1][j], lcs[i][j + 1]);
            }
        }
        List<DiffLine> ops = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < n || j < m) {
            if (i < n && j < m && a[i].equals(b[j])) {
                ops.add(new DiffLine(' ', a[i]));
                i++;
                j++;
            } else if (i < n && (j == m || lcs[i + 1][j] >= lcs[i][j + 1])) {
                ops.add(new DiffLine('-', a[i]));
                i++;
            } else {
                ops.add(new DiffLine('+', b[j]));
                j++;
            }
        }
        return ops;
    }

    /**
     * A document diff a model can read at a glance: only changed lines, each group preceded by the step (or
     * testcase line) it belongs to, so it is clear where the change landed.
     */
    public static List<String> docDiff(String before, String after) {
        List<DiffLine> ops = diffLines(before, after);
        List<String> out = new ArrayList<>();
        String header = null;
        boolean headerShown = false;
        for (DiffLine d : ops) {
            String t = d.text();
            boolean isHeader = t.startsWith("step ") || t.startsWith("testcase ");
            if (d.kind() == ' ') {
                if (isHeader) {
                    header = t;
                    headerShown = false;
                }
                continue;
            }
            if (isHeader && d.kind() == '+') {
                header = t;
                headerShown = true;
            }
            if (!headerShown && header != null && !isHeader) {
                out.add("  " + header);
                headerShown = true;
            }
            if (t.isEmpty()) {
                continue;
            }
            out.add(d.kind() + " " + t);
        }
        return out;
    }
}
