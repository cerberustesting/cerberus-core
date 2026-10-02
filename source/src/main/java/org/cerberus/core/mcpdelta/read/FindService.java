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
package org.cerberus.core.mcpdelta.read;

import org.cerberus.core.mcpdelta.Context;
import org.cerberus.core.mcpdelta.db.Db;
import org.cerberus.core.mcpdelta.db.Db.Row;
import org.cerberus.core.mcpdelta.doc.DocRenderer;
import org.cerberus.core.mcpdelta.doc.RowMapper;
import org.cerberus.core.mcpdelta.store.Aggregate;
import org.cerberus.core.mcpdelta.store.AggregateStore;
import org.cerberus.core.mcpdelta.store.DbResolver;
import org.cerberus.core.mcpdelta.store.Index;
import org.cerberus.core.mcpdelta.tools.Tool;
import org.cerberus.core.mcpdelta.util.Text;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Searches testcase content and answers with the matching document lines, grouped by testcase and placed
 * in their step. The database narrows the candidates; the match itself runs on the documents, so what is
 * found is exactly what the model would see and edit.
 */
public final class FindService {

    private final Context ctx;

    public FindService(Context ctx) {
        this.ctx = ctx;
    }

    public String find(String text, String scope, boolean regex, boolean caseSensitive, int limit) {
        if (Text.isBlank(text)) {
            throw new Tool.ToolError("find needs text");
        }
        Pattern p;
        try {
            int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
            p = regex ? Pattern.compile(text, flags) : Pattern.compile(Pattern.quote(text), flags);
        } catch (PatternSyntaxException e) {
            throw new Tool.ToolError("invalid regex: " + e.getDescription());
        }
        String in = Text.isBlank(scope) ? "*" : scope;
        return ctx.db.read(c -> {
            List<Index.Tc> inScope = Index.resolve(c, in);
            Set<String> allowed = new LinkedHashSet<>();
            for (Index.Tc t : inScope) {
                allowed.add(t.ref());
            }
            List<String[]> candidates = candidates(c, text, regex);
            DbResolver resolver = new DbResolver(c, ctx.catalog);
            Map<String, List<String>> hits = new LinkedHashMap<>();
            int lines = 0;
            int truncated = 0;
            for (String[] cand : candidates) {
                String ref = cand[0] + "/" + cand[1];
                if (!allowed.contains(ref)) {
                    continue;
                }
                Aggregate a = AggregateStore.load(c, cand[0], cand[1], false);
                if (a == null) {
                    continue;
                }
                String doc = DocRenderer.render(RowMapper.toDoc(a, resolver, false));
                String step = null;
                for (String line : doc.split("\n")) {
                    if (line.startsWith("step ")) {
                        step = line.substring(0, line.indexOf(':') > 0 ? line.indexOf(':') : line.length());
                    }
                    if (!p.matcher(line).find()) {
                        continue;
                    }
                    if (lines >= limit) {
                        truncated++;
                        continue;
                    }
                    // Actions and controls are placed in their step; header, prop and step lines speak for themselves.
                    String where = line.startsWith("  ") && step != null ? step + " › " : "";
                    hits.computeIfAbsent(ref, k -> new ArrayList<>()).add(where + line.strip());
                    lines++;
                }
            }
            if (hits.isEmpty()) {
                return "no match for " + Text.quote(text) + " in " + in + " (" + inScope.size() + " testcases)";
            }
            StringBuilder sb = new StringBuilder(Text.quote(text)).append(": ").append(lines + truncated).append(" lines in ")
                    .append(hits.size()).append(" testcases (scope ").append(in).append(", ").append(inScope.size()).append(" searched)\n");
            for (Map.Entry<String, List<String>> e : hits.entrySet()) {
                sb.append(e.getKey()).append('\n');
                for (String l : e.getValue()) {
                    sb.append("  ").append(l).append('\n');
                }
            }
            if (truncated > 0) {
                sb.append("… ").append(truncated).append(" more lines; narrow the scope or raise limit\n");
            }
            return sb.toString();
        });
    }

    /** Testcases whose rows contain the text somewhere: a cheap first cut, refined on the documents. */
    private static List<String[]> candidates(Connection c, String text, boolean regex) throws SQLException {
        String op = regex ? " REGEXP ?" : " LIKE ?";
        String v = regex ? text : "%" + text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        String item = "Value1" + op + " OR Value2" + op + " OR Value3" + op + " OR ConditionValue1" + op + " OR ConditionValue2" + op
                + " OR Description" + op;
        String sql = "SELECT DISTINCT Test, Testcase FROM testcasestepaction WHERE " + item + " OR Action" + op
                + " UNION SELECT DISTINCT Test, Testcase FROM testcasestepactioncontrol WHERE " + item + " OR Control" + op
                + " UNION SELECT DISTINCT Test, Testcase FROM testcasecountryproperties WHERE Value1" + op + " OR Value2" + op
                + " OR Value3" + op + " OR Property" + op + " OR Type" + op
                + " UNION SELECT DISTINCT Test, Testcase FROM testcasestep WHERE Description" + op + " OR ConditionValue1" + op
                + " OR LibraryStepTestcase" + op
                + " UNION SELECT Test, Testcase FROM testcase WHERE Description" + op + " OR Testcase" + op + " OR Application" + op
                + " OR Status" + op;
        int params = sql.length() - sql.replace("?", "").length();
        Object[] values = new Object[params];
        java.util.Arrays.fill(values, v);
        List<String[]> out = new ArrayList<>();
        for (Row r : Db.query(c, sql + " ORDER BY Test, Testcase", values)) {
            out.add(new String[]{r.s("Test"), r.s("Testcase")});
        }
        return out;
    }
}
