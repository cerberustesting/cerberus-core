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
import org.cerberus.core.mcpdelta.Guide;
import org.cerberus.core.mcpdelta.catalog.Catalog;
import org.cerberus.core.mcpdelta.db.Db;
import org.cerberus.core.mcpdelta.db.Db.Row;
import org.cerberus.core.mcpdelta.doc.DocRenderer;
import org.cerberus.core.mcpdelta.doc.RowMapper;
import org.cerberus.core.mcpdelta.entity.ObjService;
import org.cerberus.core.mcpdelta.journal.Journal;
import org.cerberus.core.mcpdelta.store.Aggregate;
import org.cerberus.core.mcpdelta.store.AggregateStore;
import org.cerberus.core.mcpdelta.store.DbResolver;
import org.cerberus.core.mcpdelta.store.Index;
import org.cerberus.core.mcpdelta.store.Table;
import org.cerberus.core.mcpdelta.util.Text;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Reading, always free and never cut silently: each ref is rendered as clean text; when an answer would
 * exceed the budget, what is left out is named so the model can ask for it.
 */
public final class ReadService {

    private final Context ctx;
    private final ExecutionReader executions;
    private final ObjService objects;
    private final MoreReads more;

    public ReadService(Context ctx, ExecutionReader executions) {
        this.ctx = ctx;
        this.executions = executions;
        this.objects = new ObjService(ctx);
        this.more = new MoreReads(ctx);
    }

    public String read(List<String> refs, boolean full, int budgetTokens) {
        int budget = Math.max(500, budgetTokens) * 4;
        StringBuilder out = new StringBuilder();
        List<String> skipped = new ArrayList<>();
        // live:<url> refs share one browser session: several pages for the price of one start.
        List<String> live = new ArrayList<>();
        List<String> rest = new ArrayList<>();
        for (String raw : refs) {
            String r = Text.nz(raw).trim();
            if (r.toLowerCase().startsWith("live:")) {
                String url = r.substring(5).trim();
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    out.append(out.length() > 0 ? "\n\n" : "").append("!! live:<absolute URL>, got '").append(url).append("'");
                } else {
                    live.add(url);
                }
            } else {
                rest.add(r);
            }
        }
        if (!live.isEmpty()) {
            try {
                out.append(out.length() > 0 ? "\n\n" : "").append(new org.cerberus.core.mcpdelta.run.LiveOutline(ctx.gridUrl()).probe(live, budget));
            } catch (RuntimeException e) {
                out.append(out.length() > 0 ? "\n\n" : "").append("!! live: ").append(e.getMessage());
            }
        }
        for (String raw : rest) {
            String ref = Text.nz(raw).trim();
            if (ref.isEmpty()) {
                continue;
            }
            if (out.length() >= budget) {
                skipped.add(ref);
                continue;
            }
            String part;
            try {
                part = readOne(ref, full, budget - out.length());
            } catch (RuntimeException e) {
                part = "!! " + ref + ": " + e.getMessage();
            }
            if (out.length() > 0) {
                out.append("\n\n");
            }
            out.append(part.stripTrailing());
        }
        if (!skipped.isEmpty()) {
            out.append("\n\n… budget reached; not read yet: ").append(String.join(", ", skipped));
        }
        return out.toString();
    }

    private String readOne(String ref, boolean full, int room) {
        String lower = ref.toLowerCase();
        if (lower.equals("guide")) {
            return Guide.FULL;
        }
        if (lower.startsWith("catalog:variables")) {
            String rest = ref.substring("catalog:variables".length());
            return more.variables(rest.startsWith(":") ? rest.substring(1).trim() : rest.trim());
        }
        if (lower.equals("catalog") || lower.startsWith("catalog:")) {
            return catalog(ref.contains(":") ? ref.substring(ref.indexOf(':') + 1).trim() : "");
        }
        if (lower.equals("targets") || lower.startsWith("targets:")) {
            return ctx.db.read(c -> targets(c, ref.contains(":") ? ref.substring(ref.indexOf(':') + 1).trim() : ""));
        }
        if (lower.equals("journal") || lower.startsWith("delta:")) {
            return journal(lower.startsWith("delta:") ? ref.substring(6).trim() : null);
        }
        if (lower.startsWith("run:") || lower.startsWith("exe:")) {
            return executions.execution(ref.substring(4).trim(), full);
        }
        if (lower.startsWith("tag:")) {
            return executions.tag(ref.substring(4).trim(), full);
        }
        if (lower.startsWith("file:")) {
            return more.file(ref.substring(5), room);
        }
        if (lower.equals("executions") || lower.startsWith("executions ") || lower.startsWith("executions:")) {
            return more.executions(ref.substring("executions".length()).replace(':', ' '));
        }
        if (lower.equals("library") || lower.startsWith("library:")) {
            return more.library(ref.contains(":") ? ref.substring(ref.indexOf(':') + 1).trim() : "");
        }
        if (lower.equals("version")) {
            return more.version();
        }
        if (lower.equals("systems")) {
            return more.systems();
        }
        if (ObjService.isListing(ref) || ObjService.parseRef(ref) != null) {
            return ctx.db.read(c -> objects.read(c, ref));
        }
        if (lower.startsWith("page:")) {
            return executions.page(ref.substring(5).trim());
        }
        if (lower.startsWith("runs:")) {
            return executions.history(ref.substring(5).trim());
        }
        String[] parts = Index.splitRef(ref);
        boolean scope = ref.equals("*") || lower.startsWith("app:") || lower.startsWith("label:") || lower.startsWith("status:")
                || lower.startsWith("system:") || lower.startsWith("type:") || lower.startsWith("q:")
                || parts == null || ref.endsWith("/*") || ref.contains("*");
        if (!scope) {
            return ctx.db.read(c -> testcase(c, parts[0], parts[1], full));
        }
        return ctx.db.read(c -> listing(c, ref, room));
    }

    // ---------------------------------------------------------------- testcases

    private String testcase(Connection c, String test, String testcase, boolean full) throws SQLException {
        Aggregate a = AggregateStore.load(c, test, testcase, false);
        if (a == null) {
            List<Index.Tc> near = Index.resolve(c, test);
            String hint = near.isEmpty()
                    ? "folder " + test + " has no testcase (folders: " + String.join(", ", Text.closest(test, ctx.catalog.tests(), 4)) + ")"
                    : "closest in " + test + ": " + String.join(", ", Text.closest(testcase,
                    near.stream().map(Index.Tc::testcase).toList(), 4));
            return "!! " + test + "/" + testcase + " does not exist — " + hint;
        }
        DbResolver resolver = new DbResolver(c, ctx.catalog);
        String doc = DocRenderer.render(RowMapper.toDoc(a, resolver, full));
        int actions = a.get(Table.ACTION).size();
        int controls = a.get(Table.CONTROL).size();
        StringBuilder meta = new StringBuilder("# v").append(a.fingerprint()).append(" · ")
                .append(a.get(Table.STEP).size()).append(" steps, ").append(actions).append(" actions, ")
                .append(controls).append(" controls");
        String runs = lastRuns(c, test, testcase);
        if (!runs.isEmpty()) {
            meta.append(" · last runs: ").append(runs);
        }
        String usedBy = usedBy(c, test, testcase);
        if (!usedBy.isEmpty()) {
            meta.append("\n# library steps used by: ").append(usedBy);
        }
        return meta + "\n" + doc;
    }

    private static String lastRuns(Connection c, String test, String testcase) throws SQLException {
        List<Row> rows = Db.query(c, "SELECT e.ID, e.Environment, e.Country, e.ControlStatus, e.Start FROM testcaseexecution e"
                + " JOIN (SELECT Environment, Country, MAX(ID) mx FROM testcaseexecution WHERE Test=? AND TestCase=?"
                + " GROUP BY Environment, Country) m ON m.mx=e.ID ORDER BY e.ID DESC LIMIT 4", test, testcase);
        return rows.stream().map(r -> r.s("Environment") + "/" + r.s("Country") + " " + r.s("ControlStatus") + " #" + r.s("ID"))
                .collect(Collectors.joining(", "));
    }

    private static String usedBy(Connection c, String test, String testcase) throws SQLException {
        List<Row> rows = Db.query(c, "SELECT Test, Testcase, COUNT(*) n FROM testcasestep WHERE LibraryStepTest=? AND LibraryStepTestcase=?"
                + " AND IsUsingLibraryStep=1 GROUP BY Test, Testcase ORDER BY Test, Testcase LIMIT 30", test, testcase);
        return rows.stream().map(r -> r.s("Test") + "/" + r.s("Testcase")).collect(Collectors.joining(", "));
    }

    private String listing(Connection c, String scope, int room) throws SQLException {
        List<Index.Tc> list = Index.resolve(c, scope);
        if (list.isEmpty()) {
            String hint = Text.closest(scope, ctx.catalog.tests(), 4).stream().collect(Collectors.joining(", "));
            return "!! nothing in " + scope + " — folders: " + hint + " (or app:<application>, label:<label>, *)";
        }
        Map<String, String> last = new LinkedHashMap<>();
        Map<String, Boolean> library = new LinkedHashMap<>();
        if (list.size() <= 2000) {
            for (Row r : Db.query(c, "SELECT e.Test, e.TestCase, e.ID, e.Environment, e.ControlStatus FROM testcaseexecution e JOIN"
                    + " (SELECT Test, TestCase, MAX(ID) mx FROM testcaseexecution GROUP BY Test, TestCase) m ON m.mx=e.ID")) {
                last.put(r.s("Test") + "/" + r.s("TestCase"), r.s("Environment") + " " + r.s("ControlStatus") + " #" + r.s("ID"));
            }
            for (Row r : Db.query(c, "SELECT DISTINCT Test, Testcase FROM testcasestep WHERE IsLibraryStep=1")) {
                library.put(r.s("Test") + "/" + r.s("Testcase"), true);
            }
        }
        StringBuilder sb = new StringBuilder();
        Map<String, List<Index.Tc>> byFolder = new LinkedHashMap<>();
        for (Index.Tc t : list) {
            byFolder.computeIfAbsent(t.test(), k -> new ArrayList<>()).add(t);
        }
        int width = list.stream().mapToInt(t -> t.testcase().length()).max().orElse(8);
        int shown = 0;
        for (Map.Entry<String, List<Index.Tc>> e : byFolder.entrySet()) {
            sb.append(e.getKey()).append(" — ").append(e.getValue().size()).append(" testcases\n");
            for (Index.Tc t : e.getValue()) {
                if (sb.length() > room) {
                    sb.append("… ").append(list.size() - shown).append(" more; narrow the scope (Folder/PREFIX-*, app:, status:)\n");
                    return sb.toString();
                }
                shown++;
                sb.append("  ").append(String.format("%-" + width + "s", t.testcase())).append("  ")
                        .append(t.status()).append("  ").append(t.application()).append("  ").append(Text.truncate(t.title(), 70));
                if (!t.active()) {
                    sb.append("  [inactive]");
                }
                if (library.containsKey(t.ref())) {
                    sb.append("  [library]");
                }
                String run = last.get(t.ref());
                if (run != null) {
                    sb.append("  · ").append(run);
                }
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------- catalog, targets, journal

    private String catalog(String what) {
        Catalog cat = ctx.catalog;
        if (what.isEmpty()) {
            return "actions: " + String.join(" ", cat.actions()) + "\n"
                    + "controls: " + String.join(" ", cat.controls()) + "\n"
                    + "conditions: " + String.join(" ", cat.conditions("ACTION")) + "\n"
                    + "property types: " + String.join(" ", cat.invariant("PROPERTYTYPE")) + "\n"
                    + "property databases: " + String.join(" ", cat.invariant("PROPERTYDATABASE")) + "\n"
                    + "statuses: " + String.join(", ", cat.invariant("TCSTATUS")) + " · countries: " + String.join(" ", cat.countryOrder())
                    + " · step loops: " + String.join(" ", cat.invariant("STEPLOOP")) + "\n"
                    + "details: catalog:<name> (one), catalog:actions, catalog:controls, catalog:conditions, catalog:properties, catalog:labels, catalog:apps";
        }
        switch (what.toLowerCase()) {
            case "actions" -> {
                return specs("action", cat.actionSpecs(), cat.actions());
            }
            case "controls" -> {
                return specs("control", cat.controlSpecs(), cat.controls());
            }
            case "conditions" -> {
                return specs("condition", cat.conditionSpecs(), cat.conditions("ACTION"));
            }
            case "properties" -> {
                return specs("property type", cat.propertySpecs(), cat.invariant("PROPERTYTYPE"));
            }
            case "labels" -> {
                return cat.labels().stream().map(l -> l.label() + " (" + l.type() + (l.system().isEmpty() ? "" : ", " + l.system()) + ")")
                        .collect(Collectors.joining("\n"));
            }
            case "apps", "applications" -> {
                return cat.applications().values().stream().map(a -> a.name() + " (" + a.type() + ", system " + a.system() + ")")
                        .collect(Collectors.joining("\n"));
            }
            default -> {
                for (Map<String, Catalog.Spec> m : List.of(cat.actionSpecs(), cat.controlSpecs(), cat.conditionSpecs(), cat.propertySpecs())) {
                    Catalog.Spec s = m.get(what);
                    if (s != null) {
                        return s.name() + " — " + Text.nz(s.description()) + "\n  values: " + s.params()
                                + "\n  application types: " + String.join(", ", s.appTypes());
                    }
                }
                List<String> all = new ArrayList<>(cat.actions());
                all.addAll(cat.controls());
                all.addAll(cat.conditions("ACTION"));
                return "!! unknown catalog entry '" + what + "' — did you mean " + String.join(", ", Text.closest(what, all, 4)) + "?";
            }
        }
    }

    private static String specs(String kind, Map<String, Catalog.Spec> specs, java.util.Collection<String> names) {
        StringBuilder sb = new StringBuilder();
        for (String n : names) {
            Catalog.Spec s = specs.get(n);
            sb.append(n);
            if (s != null && !s.params().isEmpty()) {
                sb.append(" — ").append(s.params());
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private String targets(Connection c, String app) throws SQLException {
        StringBuilder sb = new StringBuilder();
        List<Row> envs = Db.query(c, "SELECT cep.Application, cep.Country, cep.Environment, cep.IP, cep.URL FROM countryenvironmentparameters cep"
                + " JOIN application a ON a.Application=cep.Application"
                + " JOIN countryenvparam ce ON ce.`system`=cep.`system` AND ce.Country=cep.Country AND ce.Environment=cep.Environment"
                + " WHERE cep.IsActive=1 AND ce.active='Y'" + (app.isEmpty() ? "" : " AND cep.Application=?")
                + " ORDER BY cep.Application, cep.Environment, cep.Country", app.isEmpty() ? new Object[0] : new Object[]{app});
        Map<String, List<String>> byApp = new LinkedHashMap<>();
        for (Row r : envs) {
            byApp.computeIfAbsent(r.s("Application"), k -> new ArrayList<>())
                    .add(r.s("Environment") + "/" + r.s("Country") + " → " + r.s("IP") + r.s("URL"));
        }
        if (byApp.isEmpty()) {
            sb.append("no active environment").append(app.isEmpty() ? "" : " for " + app).append('\n');
        }
        for (Map.Entry<String, List<String>> e : byApp.entrySet()) {
            Catalog.Application a = ctx.catalog.applications().get(e.getKey());
            sb.append(e.getKey()).append(a == null ? "" : " (" + a.type() + ")").append(": ").append(String.join(", ", e.getValue())).append('\n');
        }
        List<Row> robots = Db.query(c, "SELECT r.robot, r.browser, r.platform, r.type, GROUP_CONCAT(CONCAT(x.host, ':', x.Port) SEPARATOR ' ') hosts"
                + " FROM robot r LEFT JOIN robotexecutor x ON x.robot=r.robot AND x.IsActive=1 WHERE r.IsActive=1 GROUP BY r.robot, r.browser, r.platform, r.type ORDER BY r.robot");
        sb.append("robots: ");
        sb.append(robots.stream().map(r -> r.s("robot") + " (" + r.s("browser") + (r.s("hosts").isBlank() || r.s("hosts").equals(":") ? ", no executor" : " → " + r.s("hosts")) + ")")
                .collect(Collectors.joining(", ")));
        return sb.toString();
    }

    private String journal(String id) {
        if (id != null) {
            Journal.Delta d = ctx.journal.load(id);
            if (d == null) {
                return "!! unknown delta " + id;
            }
            StringBuilder sb = new StringBuilder(d.id + " " + d.time + " by " + d.user + " — " + Text.nz(d.intent));
            if (d.undoes != null) {
                sb.append(" (undoes ").append(d.undoes).append(')');
            }
            if (d.undoneBy != null) {
                sb.append(" [undone by ").append(d.undoneBy).append(']');
            }
            for (Journal.Entry e : d.entries) {
                sb.append("\n  ").append(e.ref).append(' ').append(e.summary);
            }
            return sb.toString();
        }
        List<Journal.Delta> recent = ctx.journal.recent(20);
        if (recent.isEmpty()) {
            return "journal is empty";
        }
        return recent.stream().map(d -> d.id + "  " + d.time.substring(0, Math.min(16, d.time.length())).replace('T', ' ') + "  "
                + Text.truncate(Text.nz(d.intent), 60) + "  · " + d.entries.size() + " testcase(s)"
                + (d.undoneBy != null ? "  [undone by " + d.undoneBy + "]" : "")).collect(Collectors.joining("\n"));
    }
}
