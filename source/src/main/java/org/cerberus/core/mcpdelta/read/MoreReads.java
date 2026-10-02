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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cerberus.core.mcpdelta.Context;
import org.cerberus.core.mcpdelta.McpEndpoint;
import org.cerberus.core.mcpdelta.db.Db;
import org.cerberus.core.mcpdelta.db.Db.Row;
import org.cerberus.core.mcpdelta.doc.DocRenderer;
import org.cerberus.core.mcpdelta.doc.RowMapper;
import org.cerberus.core.mcpdelta.doc.TcDoc;
import org.cerberus.core.mcpdelta.store.Aggregate;
import org.cerberus.core.mcpdelta.store.AggregateStore;
import org.cerberus.core.mcpdelta.store.DbResolver;
import org.cerberus.core.mcpdelta.util.Text;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The reads that are not documents: execution files, executions by criteria, the step library with its
 * duplicates, Cerberus variables, versions and systems.
 */
final class MoreReads {

    private final Context ctx;
    private List<Map<String, String>> variables;

    MoreReads(Context ctx) {
        this.ctx = ctx;
    }

    /** "file:<id>" (a window of a text file, or the path of an image), "file:<id> <search>" (matching lines only). */
    String file(String arg, int room) {
        String[] parts = arg.trim().split("\\s+", 2);
        long id;
        try {
            id = Long.parseLong(parts[0].replace("#", ""));
        } catch (NumberFormatException e) {
            return "!! file:<file id> [text to search], ids are listed by read run:<id> full";
        }
        String search = parts.length > 1 ? parts[1] : null;
        return ctx.db.read(c -> {
            Row f = Db.one(c, "SELECT ID, ExeID, Level, FileDesc, Filename, FileType FROM testcaseexecutionfile WHERE ID=?", id);
            if (f == null) {
                return "!! no execution file " + id;
            }
            return render(f, search, room);
        });
    }

    private String render(Row f, String search, int room) {
        Path p = Path.of(ctx.config.mediaDir, f.s("Filename"));
        String head = "file " + f.s("ID") + " · #" + f.s("ExeID") + " · " + f.s("FileDesc") + " · " + p;
        try {
            if (!Files.exists(p)) {
                return head + "\n!! not found on disk";
            }
            String type = f.s("FileType").toUpperCase(Locale.ROOT);
            if (List.of("PNG", "JPG", "JPEG", "MP4", "BIN", "PDF").contains(type)) {
                return head + " · " + Files.size(p) + " bytes (binary: open the path to see it)";
            }
            String text = Files.readString(p, StandardCharsets.UTF_8);
            if (search != null) {
                StringBuilder sb = new StringBuilder(head).append(" · lines containing ").append(Text.quote(search)).append('\n');
                int n = 0;
                String[] lines = text.split("\n");
                for (int i = 0; i < lines.length && sb.length() < room; i++) {
                    if (lines[i].toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))) {
                        sb.append(i + 1).append(": ").append(Text.truncate(lines[i].strip(), 300)).append('\n');
                        n++;
                    }
                }
                return n == 0 ? head + "\nno line contains " + Text.quote(search) : sb.toString();
            }
            if ("HTML".equals(type) || f.s("FileDesc").toLowerCase(Locale.ROOT).contains("page source")) {
                return head + "\n" + PageHints.outline(text, 150) + "(outline of the page; add a search text to see raw lines)";
            }
            String window = text.length() > room ? text.substring(0, room) : text;
            return head + " · " + text.length() + " chars" + (text.length() > room ? " (first " + room + " shown; add a search text)" : "")
                    + "\n" + window;
        } catch (java.io.IOException e) {
            return head + "\n!! cannot read: " + e.getMessage();
        }
    }

    /** Executions by criteria: "executions status=FA env=QA folder=DemoShop testcase=DEMO-001 tag=x country=FR limit=30". */
    String executions(String arg) {
        Map<String, String> f = new LinkedHashMap<>();
        for (String tok : arg.trim().split("\\s+")) {
            int eq = tok.indexOf('=');
            if (eq > 0) {
                f.put(tok.substring(0, eq).toLowerCase(Locale.ROOT), tok.substring(eq + 1));
            }
        }
        StringBuilder sql = new StringBuilder("SELECT ID, Test, TestCase, Environment, Country, ControlStatus, ControlMessage, Tag, Start FROM testcaseexecution WHERE 1=1");
        List<Object> params = new ArrayList<>();
        Map<String, String> cols = Map.of("status", "ControlStatus", "env", "Environment", "environment", "Environment",
                "folder", "Test", "test", "Test", "testcase", "TestCase", "tag", "Tag", "country", "Country", "robot", "robot");
        for (Map.Entry<String, String> e : f.entrySet()) {
            String col = cols.get(e.getKey());
            if (col != null) {
                sql.append(" AND `").append(col).append("`=?");
                params.add(e.getValue());
            }
        }
        int limit = Math.max(1, Math.min(200, parseInt(f.get("limit"), 30)));
        sql.append(" ORDER BY ID DESC LIMIT ").append(limit);
        return ctx.db.read(c -> {
            List<Row> rows = Db.query(c, sql.toString(), params.toArray());
            if (rows.isEmpty()) {
                return "no execution matches " + f;
            }
            StringBuilder sb = new StringBuilder("executions ").append(f.isEmpty() ? "" : f.toString()).append(" — latest ").append(rows.size()).append('\n');
            for (Row r : rows) {
                sb.append(String.format("%-3s", r.s("ControlStatus"))).append(" #").append(r.s("ID")).append(' ').append(r.s("Test")).append('/')
                        .append(r.s("TestCase")).append(' ').append(r.s("Environment")).append('/').append(r.s("Country"))
                        .append(" · ").append(r.s("Tag")).append("  ").append(Text.truncate(Text.oneLine(r.s("ControlMessage")), 80)).append('\n');
            }
            return sb.toString();
        });
    }

    /** Library steps, who uses them, and the ones that duplicate each other. */
    String library(String filter) {
        return ctx.db.read(c -> {
            StringBuilder sql = new StringBuilder("SELECT s.Test, s.Testcase, s.StepId, s.Description, t.Application, a.`System`,"
                    + " (SELECT COUNT(*) FROM testcasestep u WHERE u.IsUsingLibraryStep=1 AND u.LibraryStepTest=s.Test"
                    + " AND u.LibraryStepTestcase=s.Testcase AND u.LibraryStepStepId=s.StepId) used"
                    + " FROM testcasestep s JOIN testcase t ON t.Test=s.Test AND t.Testcase=s.Testcase"
                    + " LEFT JOIN application a ON a.Application=t.Application WHERE s.IsLibraryStep=1");
            List<Object> params = new ArrayList<>();
            if (!Text.isBlank(filter)) {
                sql.append(" AND (s.Test=? OR a.`System`=? OR s.Description LIKE ?)");
                params.add(filter);
                params.add(filter);
                params.add("%" + filter + "%");
            }
            sql.append(" ORDER BY s.Test, s.Testcase, s.Sort LIMIT 500");
            List<Row> rows = Db.query(c, sql.toString(), params.toArray());
            if (rows.isEmpty()) {
                return "no library step" + (Text.isBlank(filter) ? "" : " matching " + filter);
            }
            DbResolver resolver = new DbResolver(c, ctx.catalog);
            Map<String, List<String>> bySignature = new LinkedHashMap<>();
            StringBuilder sb = new StringBuilder("library steps — ").append(rows.size()).append('\n');
            Map<String, Aggregate> loaded = new LinkedHashMap<>();
            for (Row r : rows) {
                Integer pos = resolver.stepPosition(r.s("Test"), r.s("Testcase"), r.i("StepId"));
                String use = r.s("Test") + "/" + r.s("Testcase") + "#" + (pos == null ? "?" : pos);
                sb.append("  ").append(use).append("  ").append(Text.truncate(r.s("Description"), 60)).append("  · used ")
                        .append(r.s("used")).append("×").append(r.s("System").isEmpty() ? "" : " · " + r.s("System")).append('\n');
                Aggregate a = loaded.computeIfAbsent(r.s("Test") + "/" + r.s("Testcase"), k -> {
                    try {
                        return AggregateStore.load(c, r.s("Test"), r.s("Testcase"), false);
                    } catch (SQLException e) {
                        throw new Db.DbException(e);
                    }
                });
                if (a != null && pos != null) {
                    TcDoc d = RowMapper.toDoc(a, resolver, false);
                    TcDoc.Step st = d.steps.get(pos - 1);
                    StringBuilder sig = new StringBuilder();
                    for (TcDoc.Item it : st.actions) {
                        sig.append(DocRenderer.itemBlock(it)).append('\n');
                    }
                    if (sig.length() > 0) {
                        bySignature.computeIfAbsent(sig.toString(), k -> new ArrayList<>()).add(use);
                    }
                }
            }
            List<String> dups = new ArrayList<>();
            for (List<String> same : bySignature.values()) {
                if (same.size() > 1) {
                    dups.add(String.join(" = ", same));
                }
            }
            if (!dups.isEmpty()) {
                sb.append("duplicates (same actions): ").append(String.join("; ", dups)).append('\n');
            }
            return sb.append("use one in a step: step N use=\"Folder/Testcase#n\": title").toString();
        });
    }

    /** Cerberus variables (%system.X%, %object.x.value%...), all or by family / search text. */
    String variables(String filter) {
        if (variables == null) {
            try (InputStream in = MoreReads.class.getClassLoader().getResourceAsStream("mcpdelta/catalog/variables.json")) {
                variables = new ObjectMapper().readValue(in, new TypeReference<>() {
                });
            } catch (Exception e) {
                return "!! variable catalog unavailable: " + e.getMessage();
            }
        }
        String q = Text.nz(filter).toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder();
        String family = null;
        int n = 0;
        for (Map<String, String> v : variables) {
            boolean match = q.isEmpty() || v.get("family").equalsIgnoreCase(q) || v.get("name").toLowerCase(Locale.ROOT).contains(q)
                    || Text.nz(v.get("description")).toLowerCase(Locale.ROOT).contains(q);
            if (!match) {
                continue;
            }
            if (!v.get("family").equals(family)) {
                family = v.get("family");
                sb.append(family).append('\n');
            }
            sb.append("  ").append(v.get("name")).append(" — ").append(v.get("description"));
            if (!Text.isBlank(v.get("availability")) && !"Resolved on every execution.".equals(v.get("availability"))) {
                sb.append(" (").append(v.get("availability")).append(')');
            }
            sb.append('\n');
            n++;
        }
        return n == 0 ? "no variable matches " + filter : sb.toString();
    }

    String version() {
        return ctx.db.read(c -> {
            Row v = Db.one(c, "SELECT Value FROM myversion WHERE `Key`='database'");
            return "MCP Delta " + McpEndpoint.VERSION + " · Cerberus database version " + (v == null ? "?" : v.s("Value"));
        });
    }

    String systems() {
        return ctx.db.read(c -> {
            StringBuilder sb = new StringBuilder("systems\n");
            for (Row r : Db.query(c, "SELECT i.value sys, (SELECT COUNT(*) FROM application a WHERE a.`System`=i.value) apps FROM invariant i"
                    + " WHERE i.idname='SYSTEM' ORDER BY i.sort, i.value")) {
                sb.append("  ").append(r.s("sys")).append("  ").append(r.s("apps")).append(" application(s)\n");
            }
            return sb.append("a user's active systems: read context:<login>; testcases of a system: read system:<name>").toString();
        });
    }

    private static int parseInt(String v, int def) {
        try {
            return v == null ? def : Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
