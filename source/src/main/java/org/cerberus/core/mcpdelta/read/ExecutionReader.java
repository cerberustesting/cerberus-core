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
import org.cerberus.core.mcpdelta.store.Index;
import org.cerberus.core.mcpdelta.util.Text;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Executions read the way a tester reads a report: the verdict, the path to the failure, the failing line
 * with the values it really used, and what the page contained instead. Passing steps fold into one line.
 */
public final class ExecutionReader {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final List<String> NOT_FOUND = List.of("could not find", "not found", "no such element", "unable to locate",
            "not present", "is not visible", "not clickable", "element not");

    private final Context ctx;

    public ExecutionReader(Context ctx) {
        this.ctx = ctx;
    }

    /** One execution, folded unless {@code full}. */
    public String execution(String id, boolean full) {
        long exeId;
        try {
            exeId = Long.parseLong(id.replace("#", "").trim());
        } catch (NumberFormatException e) {
            return "!! run:<execution id>, got '" + id + "'";
        }
        return ctx.db.read(c -> render(c, exeId, full));
    }

    private String render(Connection c, long id, boolean full) throws SQLException {
        Row e = Db.one(c, "SELECT * FROM testcaseexecution WHERE ID=?", id);
        if (e == null) {
            return "!! execution " + id + " does not exist";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('#').append(id).append(' ').append(e.s("Test")).append('/').append(e.s("TestCase")).append(' ')
                .append(e.s("ControlStatus")).append(" · ").append(e.s("Environment")).append('/').append(e.s("Country"))
                .append(" · ").append((e.s("robot") + " " + e.s("Browser")).isBlank() ? "" : (e.s("robot") + " " + e.s("Browser")).trim() + " · ")
                .append(time(e.s("Start"))).append(" · ").append(duration(e)).append(" · tag ").append(e.s("Tag")).append('\n');
        if (!e.s("ControlMessage").isBlank()) {
            sb.append(Text.truncate(Text.oneLine(e.s("ControlMessage")), 220)).append('\n');
        }
        Details d = details(c, id);
        for (Row s : d.steps) {
            String stepKey = s.s("Step") + "/" + s.s("index");
            // Cerberus can leave a step OK when one of its actions failed: look at what is inside it.
            boolean failing = !"OK".equals(s.s("ReturnCode")) || hasBad(d, stepKey);
            sb.append("step ").append(s.s("Sort")).append(s.i("index") > 1 ? "." + s.s("index") : "").append(": ")
                    .append(Text.oneLine(s.s("Description"))).append("  ").append(failing && "OK".equals(s.s("ReturnCode")) ? worst(d, stepKey) : s.s("ReturnCode"));
            if (failing && !s.s("ReturnMessage").isBlank() && !s.s("ReturnMessage").equals(e.s("ControlMessage"))) {
                sb.append(" — ").append(Text.truncate(Text.oneLine(s.s("ReturnMessage")), 120));
            }
            sb.append('\n');
            if (!failing && !full) {
                continue;
            }
            for (Row a : d.actions.getOrDefault(stepKey, List.of())) {
                sb.append("  ").append(line(a, "Action")).append("  ").append(a.s("ReturnCode"));
                boolean bad = isBad(a.s("ReturnCode"));
                if (bad || full) {
                    message(sb, a.s("ReturnMessage"), bad);
                }
                sb.append('\n');
                if (bad) {
                    hints(sb, d, e, a, null, "    ");
                }
                for (Row ct : d.controls.getOrDefault(stepKey + "/" + a.s("Sequence"), List.of())) {
                    boolean cbad = isBad(ct.s("ReturnCode"));
                    sb.append("    ").append(line(ct, "Control")).append("  ").append(ct.s("ReturnCode"));
                    if (cbad || full) {
                        message(sb, ct.s("ReturnMessage"), cbad);
                    }
                    sb.append('\n');
                    if (cbad) {
                        hints(sb, d, e, a, ct, "      ");
                    }
                }
            }
        }
        if (!d.data.isEmpty()) {
            sb.append("properties: ").append(d.data.stream().map(r -> r.s("Property") + "=" + Text.truncate(Text.oneLine(r.s("Value")), 40)
                    + (isBad(r.s("RC")) ? " (" + r.s("RC") + ": " + Text.truncate(Text.oneLine(r.s("RMessage")), 80) + ")" : ""))
                    .collect(Collectors.joining(", "))).append('\n');
        }
        List<String> files = new ArrayList<>();
        for (Row f : d.files) {
            String desc = f.s("FileDesc");
            if (full || desc.toLowerCase(Locale.ROOT).contains("screenshot") || desc.toLowerCase(Locale.ROOT).contains("page source")) {
                files.add(desc + " " + mediaPath(f.s("Filename")));
            }
        }
        if (!files.isEmpty()) {
            sb.append("files: ").append(String.join(" · ", files)).append('\n');
        }
        return sb.toString();
    }

    /** Why an execution failed: a cause key shared by identical failures, the text, and where it sits. */
    private record Failure(String cause, String text, String where) {
    }

    private Failure failure(Connection c, long id) throws SQLException {
        String line = failureLine(c, id);
        if (line.startsWith("step ")) {
            int dot = line.indexOf(" · ");
            String where = line.substring(0, dot);
            String text = line.substring(dot + 3);
            // Same line, same message: same cause, wherever it sits. Expected values may differ ("1", "2").
            String cause = text.replaceAll("\"[^\"]*\"(?=[^→]*→)", "\"\"").replaceAll("[0-9]+", "#");
            return new Failure(cause, text, where);
        }
        return new Failure(line.replaceAll("[0-9]+", "#"), line, "");
    }

    /** A one-line verdict for run and tag summaries: the failing line, its message and the best hint. */
    public String failureLine(Connection c, long id) throws SQLException {
        Row e = Db.one(c, "SELECT * FROM testcaseexecution WHERE ID=?", id);
        if (e == null) {
            return "";
        }
        Details d = details(c, id);
        for (Row s : d.steps) {
            String stepKey = s.s("Step") + "/" + s.s("index");
            for (Row a : d.actions.getOrDefault(stepKey, List.of())) {
                Row failing = null;
                if (isBad(a.s("ReturnCode"))) {
                    failing = a;
                } else {
                    for (Row ct : d.controls.getOrDefault(stepKey + "/" + a.s("Sequence"), List.of())) {
                        if (isBad(ct.s("ReturnCode"))) {
                            failing = ct;
                            break;
                        }
                    }
                }
                if (failing == null) {
                    continue;
                }
                boolean control = failing != a;
                StringBuilder sb = new StringBuilder("step ").append(s.s("Sort")).append(" · ")
                        .append(line(failing, control ? "Control" : "Action")).append(" → ")
                        .append(Text.truncate(Text.oneLine(failing.s("ReturnMessage")), 140));
                StringBuilder h = new StringBuilder();
                hints(h, d, e, a, control ? failing : null, "");
                if (h.length() > 0) {
                    sb.append(" ").append(h.toString().trim().replace("\n", " "));
                }
                return sb.toString();
            }
        }
        return Text.truncate(Text.oneLine(e.s("ControlMessage")), 200);
    }

    /** Executions of a tag, with the queue state: what a campaign or a run produced. */
    public String tag(String tag, boolean full) {
        return ctx.db.read(c -> {
            // The latest execution of each testcase: a testcase run again under the same tag (a retry) shows once.
            List<Row> exes = Db.query(c, "SELECT e.ID, e.Test, e.TestCase, e.Environment, e.Country, e.ControlStatus, e.ControlMessage, e.Start, e.End"
                    + " FROM testcaseexecution e JOIN (SELECT MAX(ID) mx FROM testcaseexecution WHERE Tag=? GROUP BY Test, TestCase, Country, Environment) m"
                    + " ON m.mx=e.ID ORDER BY e.ID", tag);
            List<Row> queue = Db.query(c, "SELECT State, COUNT(*) n FROM testcaseexecutionqueue q WHERE Tag=? AND q.ID=(SELECT MAX(q2.ID)"
                    + " FROM testcaseexecutionqueue q2 WHERE q2.Tag=q.Tag AND q2.Test=q.Test AND q2.TestCase=q.TestCase AND q2.Country=q.Country"
                    + " AND q2.Environment=q.Environment) GROUP BY State", tag);
            List<Row> queueErrors = Db.query(c, "SELECT q.ID, q.Test, q.TestCase, q.Comment FROM testcaseexecutionqueue q WHERE q.Tag=? AND q.State='ERROR'"
                    + " AND q.ID=(SELECT MAX(q2.ID) FROM testcaseexecutionqueue q2 WHERE q2.Tag=q.Tag AND q2.Test=q.Test AND q2.TestCase=q.TestCase"
                    + " AND q2.Country=q.Country AND q2.Environment=q.Environment) LIMIT 10", tag);
            // An execution row left in PE by an engine error, whose queue entry failed, is not running.
            exes.removeIf(r -> "PE".equals(r.s("ControlStatus")) && queueErrors.stream().anyMatch(
                    q -> q.s("Test").equals(r.s("Test")) && q.s("TestCase").equals(r.s("TestCase"))));
            if (exes.isEmpty() && queue.isEmpty()) {
                return "!! tag " + tag + " has no execution and nothing in the queue";
            }
            return summarize(c, tag, exes, queue, queueErrors, full);
        });
    }

    public String summarize(Connection c, String tag, List<Row> exes, List<Row> queue, List<Row> queueErrors, boolean full) throws SQLException {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Row r : exes) {
            counts.merge(r.s("ControlStatus"), 1, Integer::sum);
        }
        StringBuilder sb = new StringBuilder("tag ").append(tag).append(" · ").append(exes.size()).append(" executions");
        if (!counts.isEmpty()) {
            sb.append(": ").append(counts.entrySet().stream().map(x -> x.getValue() + " " + x.getKey()).collect(Collectors.joining(", ")));
        }
        String pending = queue.stream().filter(q -> !List.of("DONE", "ERROR", "CANCELLED").contains(q.s("State")))
                .map(q -> q.s("n") + " " + q.s("State")).collect(Collectors.joining(", "));
        if (!pending.isEmpty()) {
            sb.append(" · queue: ").append(pending);
        }
        sb.append('\n');
        // Failures are grouped by cause: the same broken line in five testcases is one finding, not five.
        Map<String, List<String>> byCause = new LinkedHashMap<>();
        Map<String, String> causeText = new LinkedHashMap<>();
        List<String> passed = new ArrayList<>();
        List<String> other = new ArrayList<>();
        for (Row r : exes) {
            String status = r.s("ControlStatus");
            String who = r.s("Test") + "/" + r.s("TestCase") + " #" + r.s("ID");
            if ("OK".equals(status) && !full) {
                passed.add(who);
                continue;
            }
            if (!isBad(status) && !full) {
                other.add(status + " " + who);
                continue;
            }
            Failure f = failure(c, r.l("ID"));
            String key = status + "\u0001" + f.cause;
            byCause.computeIfAbsent(key, k -> new ArrayList<>()).add(who + (f.where.isEmpty() ? "" : " " + f.where));
            causeText.putIfAbsent(key, f.text);
        }
        if (!passed.isEmpty()) {
            sb.append("OK  ").append(String.join(", ", passed)).append('\n');
        }
        for (Map.Entry<String, List<String>> e : byCause.entrySet()) {
            String status = e.getKey().substring(0, e.getKey().indexOf('\u0001'));
            List<String> who = e.getValue();
            sb.append(String.format("%-3s", status)).append(' ');
            if (who.size() == 1) {
                sb.append(who.get(0)).append("\n    ").append(causeText.get(e.getKey())).append('\n');
            } else {
                sb.append("×").append(who.size()).append(" same cause: ").append(causeText.get(e.getKey()))
                        .append("\n    in ").append(String.join(", ", who)).append('\n');
            }
        }
        for (String o : other) {
            sb.append(o).append('\n');
        }
        for (Row q : queueErrors) {
            sb.append("ERR ").append(q.s("Test")).append('/').append(q.s("TestCase")).append(" not executed (queue ").append(q.s("ID"))
                    .append("): ").append(Text.truncate(Text.oneLine(q.s("Comment")), 160)).append('\n');
        }
        return sb.toString();
    }

    /**
     * The page the robot saved during an execution, as an outline. {@code ref} is "<exeId>" for the last
     * saved page, or "<exeId>#<n>" for the n-th.
     */
    public String page(String ref) {
        String r = ref.replace("#", " ").trim();
        String[] parts = r.split("\\s+");
        long exeId;
        try {
            exeId = Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
            return "!! page:<execution id>[#n], got '" + ref + "'";
        }
        return ctx.db.read(c -> {
            List<Row> pages = new ArrayList<>();
            for (Row f : Db.query(c, "SELECT Level, FileDesc, Filename FROM testcaseexecutionfile WHERE ExeID=? ORDER BY ID", exeId)) {
                if (f.s("FileDesc").toLowerCase(Locale.ROOT).contains("page source")) {
                    pages.add(f);
                }
            }
            if (pages.isEmpty()) {
                return "!! execution " + exeId + " saved no page (pages are saved on failure, or with shot=after on an action)";
            }
            int index = pages.size();
            if (parts.length > 1) {
                try {
                    index = Math.max(1, Math.min(pages.size(), Integer.parseInt(parts[1])));
                } catch (NumberFormatException ignored) {
                    // keep the last page
                }
            }
            Row f = pages.get(index - 1);
            Path p = Path.of(ctx.config.mediaDir, f.s("Filename"));
            String html;
            try {
                html = Files.readString(p, StandardCharsets.UTF_8);
            } catch (IOException e) {
                return "!! cannot read " + p;
            }
            return "page saved by #" + exeId + " at " + f.s("Level") + " (" + index + "/" + pages.size() + ")\n" + PageHints.outline(html, 120);
        });
    }

    /** Last executions of a testcase. */
    public String history(String ref) {
        String[] p = Index.splitRef(ref);
        if (p == null) {
            return "!! runs:<Folder>/<Testcase>";
        }
        return ctx.db.read(c -> {
            List<Row> rows = Db.query(c, "SELECT ID, Environment, Country, ControlStatus, ControlMessage, Start, Tag FROM testcaseexecution"
                    + " WHERE Test=? AND TestCase=? ORDER BY ID DESC LIMIT 12", p[0], p[1]);
            if (rows.isEmpty()) {
                return ref + " has never been executed";
            }
            StringBuilder sb = new StringBuilder(ref).append(" — last ").append(rows.size()).append(" executions\n");
            for (Row r : rows) {
                sb.append(String.format("%-3s", r.s("ControlStatus"))).append(" #").append(r.s("ID")).append(' ')
                        .append(r.s("Environment")).append('/').append(r.s("Country")).append(' ').append(time(r.s("Start")))
                        .append("  ").append(Text.truncate(Text.oneLine(r.s("ControlMessage")), 90)).append('\n');
            }
            return sb.toString();
        });
    }

    // ---------------------------------------------------------------- details

    private record Details(List<Row> steps, Map<String, List<Row>> actions, Map<String, List<Row>> controls, List<Row> data, List<Row> files) {
    }

    private Details details(Connection c, long id) throws SQLException {
        List<Row> steps = Db.query(c, "SELECT Step, `index`, Sort, ReturnCode, ReturnMessage, Description FROM testcasestepexecution"
                + " WHERE ID=? ORDER BY Start, Sort, `index`", id);
        Map<String, List<Row>> actions = new LinkedHashMap<>();
        for (Row a : Db.query(c, "SELECT * FROM testcasestepactionexecution WHERE ID=? ORDER BY Step, `index`, Sort, Sequence", id)) {
            actions.computeIfAbsent(a.s("Step") + "/" + a.s("index"), k -> new ArrayList<>()).add(a);
        }
        Map<String, List<Row>> controls = new LinkedHashMap<>();
        for (Row ct : Db.query(c, "SELECT * FROM testcasestepactioncontrolexecution WHERE ID=? ORDER BY Step, `index`, Sequence, Sort, ControlSequence", id)) {
            controls.computeIfAbsent(ct.s("Step") + "/" + ct.s("index") + "/" + ct.s("Sequence"), k -> new ArrayList<>()).add(ct);
        }
        List<Row> data = Db.query(c, "SELECT Property, `Index`, Value, RC, RMessage FROM testcaseexecutiondata WHERE ID=? ORDER BY Property, `Index`", id);
        List<Row> files = Db.query(c, "SELECT Level, FileDesc, Filename, FileType FROM testcaseexecutionfile WHERE ExeID=? ORDER BY ID", id);
        return new Details(steps, actions, controls, data, files);
    }

    private static boolean hasBad(Details d, String stepKey) {
        return !worst(d, stepKey).equals("OK");
    }

    /** The worst return code among a step's actions and controls. */
    private static String worst(Details d, String stepKey) {
        String worst = "OK";
        for (Row a : d.actions.getOrDefault(stepKey, List.of())) {
            if (isBad(a.s("ReturnCode"))) {
                worst = a.s("ReturnCode");
            }
            for (Row ct : d.controls.getOrDefault(stepKey + "/" + a.s("Sequence"), List.of())) {
                if (isBad(ct.s("ReturnCode"))) {
                    worst = ct.s("ReturnCode");
                }
            }
        }
        return worst;
    }

    private static String line(Row r, String nameColumn) {
        StringBuilder sb = new StringBuilder(r.s(nameColumn));
        List<String> values = new ArrayList<>(List.of(r.s("Value1"), r.s("Value2"), r.s("Value3")));
        while (!values.isEmpty() && values.get(values.size() - 1).isEmpty()) {
            values.remove(values.size() - 1);
        }
        for (String v : values) {
            sb.append(' ').append(Text.quote(Text.truncate(v, 80)));
        }
        return sb.toString();
    }

    private static void message(StringBuilder sb, String message, boolean bad) {
        String m = Text.oneLine(message);
        if (!m.isEmpty()) {
            sb.append(" — ").append(Text.truncate(m, bad ? 220 : 90));
        }
    }

    private void hints(StringBuilder sb, Details d, Row exe, Row action, Row control, String indent) {
        Row failing = control != null ? control : action;
        String message = failing.s("ReturnMessage").toLowerCase(Locale.ROOT);
        String selector = failing.s("Value1");
        if (selector.isBlank() || !selector.contains("=")) {
            return;
        }
        String html = pageSource(d, exe, action, control);
        if (html == null) {
            return;
        }
        boolean notFound = NOT_FOUND.stream().anyMatch(message::contains);
        // The selector itself, evaluated on the saved page, before any look-alike.
        String diagnosis = PageHints.diagnose(selector, html, notFound);
        if (diagnosis != null && !diagnosis.isEmpty()) {
            sb.append(indent).append("↳ ").append(diagnosis).append(" (read page:").append(exe.s("ID")).append(")\n");
        } else if (notFound && PageHints.typography(selector, html) != null) {
            sb.append(indent).append("↳ ").append(PageHints.typography(selector, html)).append(" (read page:").append(exe.s("ID")).append(")\n");
        } else if (notFound) {
            List<PageHints.Hint> hints = PageHints.forSelector(selector, html, 3);
            if (hints.isEmpty()) {
                sb.append(indent).append("↳ nothing in the saved page resembles ").append(selector).append('\n');
            } else {
                sb.append(indent).append("↳ ").append(selector).append(" is not in the saved page (read page:").append(exe.s("ID")).append("); closest: ")
                        .append(hints.stream().map(h -> h.element() + (h.selector() == null ? "" : " (" + h.selector() + ")"))
                                .collect(Collectors.joining(" | "))).append('\n');
            }
        }
        // What the element reads, only where a text was compared.
        if (control != null && !notFound && control.s("Control").matches("(?i).*(text|string|value|number|attribute|title|url).*")) {
            String text = PageHints.textOf(selector, html);
            if (text != null && !text.isEmpty()) {
                sb.append(indent).append("↳ in the saved page, ").append(selector).append(" reads ").append(Text.quote(text)).append('\n');
            }
        }
    }

    private String pageSource(Details d, Row exe, Row action, Row control) {
        String level = exe.s("Test") + "-" + exe.s("TestCase") + "-" + action.s("Step") + "-" + action.s("index") + "-" + action.s("Sequence")
                + (control != null ? "-" + control.s("ControlSequence") : "");
        Row best = null;
        for (Row f : d.files) {
            if (!f.s("FileDesc").toLowerCase(Locale.ROOT).contains("page source")) {
                continue;
            }
            if (f.s("Level").equals(level)) {
                best = f;
                break;
            }
            if (best == null || f.s("Level").startsWith(level)) {
                best = f;
            }
        }
        if (best == null) {
            return null;
        }
        try {
            Path p = Path.of(ctx.config.mediaDir, best.s("Filename"));
            if (!Files.exists(p) || Files.size(p) > 5_000_000) {
                return null;
            }
            return Files.readString(p, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    private String mediaPath(String filename) {
        Path p = Path.of(ctx.config.mediaDir, filename);
        return Files.exists(p) ? p.toString() : filename;
    }

    static boolean isBad(String rc) {
        return List.of("FA", "KO", "NA").contains(rc);
    }

    /** Start and End are timestamps in this schema ("2026-10-01 20:39:26.345"); older ones were epoch ms. */
    private static String time(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        if (value.matches("\\d{12,}")) {
            return TIME.format(Instant.ofEpochMilli(Long.parseLong(value)));
        }
        return value.length() >= 19 ? value.substring(5, 19) : value;
    }

    private static String duration(Row e) {
        long ms = e.l("DurationMs");
        if (ms <= 0) {
            Long start = epoch(e.s("Start"));
            Long end = epoch(e.s("End"));
            if (start == null || end == null || end < start) {
                return "PE".equals(e.s("ControlStatus")) ? "running" : "?";
            }
            ms = end - start;
        }
        return String.format(Locale.ROOT, "%.1fs", ms / 1000.0);
    }

    private static Long epoch(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.matches("\\d{12,}")) {
            return Long.parseLong(value);
        }
        try {
            return java.sql.Timestamp.valueOf(value.length() > 23 ? value.substring(0, 23) : value).getTime();
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
