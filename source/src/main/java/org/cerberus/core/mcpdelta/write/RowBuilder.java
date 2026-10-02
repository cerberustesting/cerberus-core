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
package org.cerberus.core.mcpdelta.write;

import org.cerberus.core.mcpdelta.db.Db.Row;
import org.cerberus.core.mcpdelta.doc.Diagnostic;
import org.cerberus.core.mcpdelta.doc.DocParser;
import org.cerberus.core.mcpdelta.doc.DocRenderer;
import org.cerberus.core.mcpdelta.doc.RowMapper;
import org.cerberus.core.mcpdelta.doc.TcDoc;
import org.cerberus.core.mcpdelta.store.Aggregate;
import org.cerberus.core.mcpdelta.store.Table;
import org.cerberus.core.mcpdelta.util.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds the rows a document stands for, on top of the testcase as it is. Rows that continue an existing
 * item start from that row, so columns the document does not speak about (creation stamp, ids) are kept;
 * new rows take the editor's defaults.
 */
public final class RowBuilder {

    /** What the builder needs to resolve names into rows. */
    public interface Resolver extends RowMapper.Lookup {
        /** stepId of the step at a 1-based position, or null. */
        Integer stepIdAt(String test, String testcase, int position);

        /** Whether that step exists and is marked as a library step. Null when it does not exist. */
        Boolean isLibraryStep(String test, String testcase, int stepId);

        /** Label id for a name, preferring the given system; null when unknown. */
        String labelId(String name, String system);

        String applicationSystem(String application);
    }

    private final Resolver resolver;
    private final List<Diagnostic> diagnostics;
    private final String where;

    public RowBuilder(Resolver resolver, List<Diagnostic> diagnostics, String where) {
        this.resolver = resolver;
        this.diagnostics = diagnostics;
        this.where = where;
    }

    public Aggregate build(TcDoc doc, Aggregate base) {
        Aggregate t = new Aggregate(doc.test, doc.testcase);
        Row tc = base == null ? newTestcaseRow(doc) : base.testcaseRow().copy();
        tc.put("Description", doc.title);
        applyAttributes(doc, tc);
        if (doc.condition != null || base != null) {
            putCondition(tc, doc.condition, "ConditionOperator", "ConditionValue1", "ConditionValue2", "ConditionValue3", "ConditionOptions");
        }
        t.get(Table.TESTCASE).add(tc);

        // Countries: the document's list when it gives one, otherwise unchanged.
        List<String> countries;
        if (doc.attrs.containsKey("countries")) {
            countries = new ArrayList<>(new LinkedHashSet<>(DocParser.splitList(doc.attrs.get("countries"))));
        } else if (base != null) {
            countries = new ArrayList<>();
            for (Row r : base.get(Table.COUNTRY)) {
                countries.add(r.s("Country"));
            }
        } else {
            countries = List.of();
            diagnostics.add(Diagnostic.error(where, doc.line, "a new testcase needs its countries", "countries=FR,BE"));
        }
        Map<String, Row> baseCountries = index(base, Table.COUNTRY, "Country");
        for (String country : countries) {
            Row r = baseCountries.containsKey(country) ? baseCountries.get(country).copy() : keyRow(doc);
            r.put("Country", country);
            t.get(Table.COUNTRY).add(r);
        }

        labels(doc, base, tc, t);
        properties(doc, base, countries, t);
        steps(doc, base, t);
        return t;
    }

    // ---------------------------------------------------------------- testcase row

    private Row newTestcaseRow(TcDoc doc) {
        Row r = keyRow(doc);
        r.put("Application", "");
        r.put("DetailedDescription", "");
        r.put("Priority", "1");
        r.put("isMuted", "0");
        r.put("Status", "STANDBY");
        r.put("isActive", "1");
        r.put("ConditionOperator", "always");
        r.put("ConditionValue1", "");
        r.put("ConditionValue2", "");
        r.put("ConditionValue3", "");
        r.put("ConditionOptions", "[]");
        r.put("Type", "AUTOMATED");
        r.put("Origine", "");
        r.put("RefOrigine", "");
        r.put("Comment", "");
        r.put("Bugs", "[]");
        r.put("Implementer", "");
        r.put("Executor", "");
        r.put("isActiveQA", "1");
        r.put("isActiveUAT", "1");
        r.put("isActivePROD", "1");
        r.put("useragent", "");
        r.put("screensize", "");
        r.put("Version", "0");
        return r;
    }

    private void applyAttributes(TcDoc doc, Row tc) {
        Map<String, String> columns = Map.ofEntries(
                Map.entry("application", "Application"), Map.entry("status", "Status"), Map.entry("priority", "Priority"),
                Map.entry("type", "Type"), Map.entry("details", "DetailedDescription"), Map.entry("comment", "Comment"),
                Map.entry("bugs", "Bugs"), Map.entry("origin", "Origine"), Map.entry("refOrigin", "RefOrigine"),
                Map.entry("implementer", "Implementer"), Map.entry("executor", "Executor"), Map.entry("fromMajor", "FromMajor"),
                Map.entry("fromMinor", "FromMinor"), Map.entry("toMajor", "ToMajor"), Map.entry("toMinor", "ToMinor"),
                Map.entry("targetMajor", "TargetMajor"), Map.entry("targetMinor", "TargetMinor"),
                Map.entry("useragent", "useragent"), Map.entry("screensize", "screensize"));
        Map<String, String> flags = Map.of("active", "isActive", "muted", "isMuted", "activeQA", "isActiveQA",
                "activeUAT", "isActiveUAT", "activePROD", "isActivePROD");
        for (Map.Entry<String, String> e : doc.attrs.entrySet()) {
            String key = e.getKey();
            String value = e.getValue();
            if (columns.containsKey(key)) {
                if (key.equals("bugs") && value.isBlank()) {
                    value = "[]";
                }
                put(tc, columns.get(key), value);
            } else if (flags.containsKey(key)) {
                Boolean b = bool(value);
                if (b == null) {
                    diagnostics.add(Diagnostic.error(where, doc.line, key + " must be yes or no, got '" + value + "'", key + "=yes"));
                } else {
                    tc.put(flags.get(key), b ? "1" : "0");
                }
            }
        }
    }

    // ---------------------------------------------------------------- labels and properties

    private void labels(TcDoc doc, Aggregate base, Row tc, Aggregate t) {
        if (!doc.attrs.containsKey("labels")) {
            if (base != null) {
                for (Row r : base.get(Table.LABEL)) {
                    t.get(Table.LABEL).add(r.copy());
                }
            }
            return;
        }
        Map<String, Row> baseLabels = index(base, Table.LABEL, "LabelId");
        String system = resolver.applicationSystem(tc.s("Application"));
        Set<String> seen = new LinkedHashSet<>();
        for (String name : DocParser.splitList(doc.attrs.get("labels"))) {
            String id = name.startsWith("#") ? name.substring(1) : resolver.labelId(name, system);
            if (id == null) {
                diagnostics.add(Diagnostic.error(where, doc.line, "unknown label '" + name + "'",
                        "use an existing label (read catalog:labels) or create it in Cerberus first"));
                continue;
            }
            if (!seen.add(id)) {
                continue;
            }
            Row r = baseLabels.containsKey(id) ? baseLabels.get(id).copy() : keyRow(doc);
            r.put("LabelId", id);
            t.get(Table.LABEL).add(r);
        }
    }

    private void properties(TcDoc doc, Aggregate base, List<String> countries, Aggregate t) {
        Map<String, Row> baseProps = new HashMap<>();
        if (base != null) {
            for (Row r : base.get(Table.PROPERTY)) {
                baseProps.put(r.s("Country") + "\u0001" + r.s("Property"), r);
            }
        }
        Map<String, Integer> definedAt = new HashMap<>();
        for (TcDoc.Prop p : doc.props) {
            List<String> targets = p.countries == null ? countries : p.countries;
            for (String country : targets) {
                if (!countries.contains(country)) {
                    diagnostics.add(Diagnostic.error(where, p.line, "property " + p.name + " is set for " + country
                            + ", which is not a country of this testcase", "add it to countries= or remove it from the property"));
                    continue;
                }
                String key = country + "\u0001" + p.name;
                Integer previous = definedAt.put(key, p.line);
                if (previous != null) {
                    diagnostics.add(Diagnostic.error(where, p.line, "property " + p.name + " is defined twice for " + country
                            + " (also line " + previous + ")", "keep one line, or split them with countries="));
                    continue;
                }
                Row r = baseProps.containsKey(key) ? baseProps.get(key).copy() : keyRow(doc);
                r.put("Country", country);
                r.put("Property", p.name);
                r.put("Type", p.type);
                put(r, "Database", p.flags.getOrDefault("db", ""));
                put(r, "Value1", value(p.values, 0));
                put(r, "Value2", value(p.values, 1));
                put(r, "Value3", value(p.values, 2));
                put(r, "Length", p.flags.getOrDefault("length", "0"));
                r.put("RowLimit", number(p.flags.getOrDefault("rows", "0"), "rows", p.line));
                r.put("Nature", p.flags.getOrDefault("nature", "STATIC"));
                r.put("CacheExpire", number(p.flags.getOrDefault("cache", "0"), "cache", p.line));
                r.put("RetryNb", number(p.flags.getOrDefault("retry", "0"), "retry", p.line));
                String period = p.flags.get("retryPeriod");
                if (period != null) {
                    r.put("RetryPeriod", number(period, "retryPeriod", p.line));
                } else if (!r.containsKey("RetryPeriod")) {
                    r.put("RetryPeriod", "10000");
                }
                put(r, "Description", p.description);
                r.put("Rank", number(p.flags.getOrDefault("rank", "0"), "rank", p.line));
                t.get(Table.PROPERTY).add(r);
            }
        }
    }

    // ---------------------------------------------------------------- steps, actions, controls

    private void steps(TcDoc doc, Aggregate base, Aggregate t) {
        List<Row> baseSteps = base == null ? List.of() : base.get(Table.STEP);
        TcDoc baseDoc = base == null ? null : RowMapper.toDoc(base, resolver, false);
        List<String> baseSigs = new ArrayList<>();
        if (baseDoc != null) {
            for (TcDoc.Step s : baseDoc.steps) {
                baseSigs.add(DocRenderer.stepBlock(s));
            }
        }
        List<String> nextSigs = new ArrayList<>();
        for (TcDoc.Step s : doc.steps) {
            nextSigs.add(DocRenderer.stepBlock(s));
        }
        int[] match = Matcher.align(baseSigs, nextSigs);
        int nextStepId = 0;
        for (Row r : baseSteps) {
            nextStepId = Math.max(nextStepId, r.i("StepId"));
        }
        Sorts stepSorts = new Sorts();
        for (int j = 0; j < doc.steps.size(); j++) {
            TcDoc.Step s = doc.steps.get(j);
            Row baseRow = match[j] >= 0 ? baseSteps.get(match[j]) : null;
            Row r = baseRow != null ? baseRow.copy() : keyRow(doc);
            String stepId = baseRow != null ? baseRow.s("StepId") : String.valueOf(++nextStepId);
            r.put("StepId", stepId);
            r.put("Sort", stepSorts.next(baseRow));
            r.put("Description", s.title);
            r.put("Loop", s.flags.getOrDefault("loop", RowMapper.DEFAULT_LOOP));
            putCondition(r, s.condition, "ConditionOperator", "ConditionValue1", "ConditionValue2", "ConditionValue3", "ConditionOptions");
            r.put("IsLibraryStep", s.flags.containsKey("library") ? "1" : "0");
            r.put("IsExecutionForced", s.flags.containsKey("forced") ? "1" : "0");
            String use = s.flags.get("use");
            if (use != null) {
                resolveUse(r, use, s, doc);
                if (!s.actions.isEmpty()) {
                    diagnostics.add(Diagnostic.error(where, s.line, "step " + (j + 1) + " uses a library step, so it runs that step's actions and cannot have its own",
                            "remove its actions, or drop use= to give it actions of its own"));
                }
            } else {
                r.put("IsUsingLibraryStep", "0");
                // Cerberus leaves the old target in place when a step stops using a library step; the
                // columns are inert then, so an existing row keeps them and a new one gets the defaults.
                if (baseRow == null) {
                    r.put("LibraryStepTest", null);
                    r.put("LibraryStepTestcase", null);
                    r.put("LibraryStepStepId", "0");
                }
            }
            t.get(Table.STEP).add(r);
            TcDoc.Step baseStep = match[j] >= 0 && baseDoc != null ? baseDoc.steps.get(match[j]) : null;
            actions(doc, base, baseStep, stepId, s, t);
        }
    }

    private void resolveUse(Row r, String use, TcDoc.Step s, TcDoc doc) {
        int hash = use.lastIndexOf('#');
        int at = use.lastIndexOf('@');
        int cut = Math.max(hash, at);
        int slash = cut < 0 ? -1 : use.lastIndexOf('/', cut);
        if (cut < 0 || slash <= 0) {
            diagnostics.add(Diagnostic.error(where, s.line, "use= reads \"Folder/Testcase#n\" (n = step position in that testcase)",
                    "use=\"DemoShop/LIB-001#2\""));
            return;
        }
        String test = use.substring(0, slash);
        String testcase = use.substring(slash + 1, cut);
        int n;
        try {
            n = Integer.parseInt(use.substring(cut + 1).trim());
        } catch (NumberFormatException e) {
            diagnostics.add(Diagnostic.error(where, s.line, "use= must end with #<step position>", "use=\"" + test + "/" + testcase + "#1\""));
            return;
        }
        Integer stepId = hash > at ? resolver.stepIdAt(test, testcase, n) : Integer.valueOf(n);
        Boolean library = stepId == null ? null : resolver.isLibraryStep(test, testcase, stepId);
        if (library == null) {
            diagnostics.add(Diagnostic.error(where, s.line, "library step " + use + " does not exist",
                    "read " + test + "/" + testcase + " to see its steps"));
            return;
        }
        if (!library) {
            diagnostics.add(Diagnostic.error(where, s.line, "step " + use + " is not marked as a library step",
                    "mark it with 'library' in " + test + "/" + testcase + " first"));
            return;
        }
        r.put("IsUsingLibraryStep", "1");
        r.put("LibraryStepTest", test);
        r.put("LibraryStepTestcase", testcase);
        // Legacy rows can point at a library testcase with a NULL step id; reading it as step 0 must not
        // rewrite it.
        if (!(stepId == 0 && r.containsKey("LibraryStepStepId") && r.get("LibraryStepStepId") == null)) {
            r.put("LibraryStepStepId", String.valueOf(stepId));
        }
    }

    private void actions(TcDoc doc, Aggregate base, TcDoc.Step baseStep, String stepId, TcDoc.Step s, Aggregate t) {
        List<Row> baseRows = new ArrayList<>();
        if (base != null && baseStep != null) {
            for (Row r : base.get(Table.ACTION)) {
                if (r.s("StepId").equals(stepId)) {
                    baseRows.add(r);
                }
            }
        }
        List<String> baseSigs = new ArrayList<>();
        if (baseStep != null) {
            for (TcDoc.Item a : baseStep.actions) {
                baseSigs.add(DocRenderer.itemBlock(a));
            }
        }
        List<String> nextSigs = new ArrayList<>();
        for (TcDoc.Item a : s.actions) {
            nextSigs.add(DocRenderer.itemBlock(a));
        }
        int[] match = baseRows.size() == baseSigs.size() ? Matcher.align(baseSigs, nextSigs) : new int[s.actions.size()];
        if (baseRows.size() != baseSigs.size()) {
            java.util.Arrays.fill(match, -1);
        }
        int nextId = 0;
        for (Row r : baseRows) {
            nextId = Math.max(nextId, r.i("ActionId"));
        }
        Sorts actionSorts = new Sorts();
        for (int j = 0; j < s.actions.size(); j++) {
            TcDoc.Item a = s.actions.get(j);
            Row baseRow = match[j] >= 0 ? baseRows.get(match[j]) : null;
            Row r = baseRow != null ? baseRow.copy() : keyRow(doc);
            String actionId = baseRow != null ? baseRow.s("ActionId") : String.valueOf(nextId = roundUp(nextId));
            r.put("StepId", stepId);
            r.put("ActionId", actionId);
            r.put("Sort", actionSorts.next(baseRow));
            r.put("Action", a.name);
            fillItem(r, a, baseRow);
            t.get(Table.ACTION).add(r);
            TcDoc.Item baseAction = match[j] >= 0 ? baseStep.actions.get(match[j]) : null;
            controls(doc, base, baseAction, stepId, actionId, a, t);
        }
    }

    private void controls(TcDoc doc, Aggregate base, TcDoc.Item baseAction, String stepId, String actionId, TcDoc.Item a, Aggregate t) {
        List<Row> baseRows = new ArrayList<>();
        if (base != null && baseAction != null) {
            for (Row r : base.get(Table.CONTROL)) {
                if (r.s("StepId").equals(stepId) && r.s("ActionId").equals(actionId)) {
                    baseRows.add(r);
                }
            }
        }
        List<String> baseSigs = new ArrayList<>();
        if (baseAction != null) {
            for (TcDoc.Item c : baseAction.controls) {
                baseSigs.add(DocRenderer.item(c));
            }
        }
        List<String> nextSigs = new ArrayList<>();
        for (TcDoc.Item c : a.controls) {
            nextSigs.add(DocRenderer.item(c));
        }
        int[] match = baseRows.size() == baseSigs.size() ? Matcher.align(baseSigs, nextSigs) : new int[a.controls.size()];
        if (baseRows.size() != baseSigs.size()) {
            java.util.Arrays.fill(match, -1);
        }
        int nextId = 0;
        for (Row r : baseRows) {
            nextId = Math.max(nextId, r.i("ControlId"));
        }
        Sorts controlSorts = new Sorts();
        for (int j = 0; j < a.controls.size(); j++) {
            TcDoc.Item c = a.controls.get(j);
            Row baseRow = match[j] >= 0 ? baseRows.get(match[j]) : null;
            Row r = baseRow != null ? baseRow.copy() : keyRow(doc);
            r.put("StepId", stepId);
            r.put("ActionId", actionId);
            r.put("ControlId", baseRow != null ? baseRow.s("ControlId") : String.valueOf(nextId = roundUp(nextId)));
            r.put("Sort", controlSorts.next(baseRow));
            r.put("Control", c.name);
            fillItem(r, c, baseRow);
            t.get(Table.CONTROL).add(r);
        }
    }

    /**
     * Sort values in document order. An existing row keeps its value while it is still above the previous
     * one, so a gap left by the editor (1, 4, 5) is not renumbered for nothing; otherwise it takes the next one.
     */
    private static final class Sorts {
        private int last;

        String next(Row baseRow) {
            int wanted = baseRow == null ? 0 : baseRow.i("Sort");
            last = wanted > last ? wanted : last + 1;
            return String.valueOf(last);
        }
    }

    /** Ids advance in tens, as the editor numbers them. */
    private static int roundUp(int current) {
        return (current / 10 + 1) * 10;
    }

    private void fillItem(Row r, TcDoc.Item it, Row baseRow) {
        put(r, "Value1", value(it.values, 0));
        put(r, "Value2", value(it.values, 1));
        put(r, "Value3", value(it.values, 2));
        String fatal = it.flags.get("fatal");
        Boolean isFatal = fatal == null ? Boolean.TRUE : bool(fatal);
        if (isFatal == null) {
            diagnostics.add(Diagnostic.error(where, it.line, "fatal must be yes or no", "fatal=no"));
            isFatal = Boolean.TRUE;
        }
        r.put("IsFatal", isFatal ? "1" : "0");
        Map<String, String> options = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : it.flags.entrySet()) {
            switch (e.getKey()) {
                case "timeout", "minSimilarity", "typeDelay" -> options.put(e.getKey(), e.getValue());
                case "highlight" -> options.put("highlightElement", e.getValue());
                default -> {
                    if (e.getKey().startsWith("opt.")) {
                        options.put(e.getKey().substring(4), e.getValue());
                    }
                }
            }
        }
        for (String key : new String[]{"timeout", "highlightElement"}) {
            String v = options.get(key);
            if (v != null && !v.matches("\\d+")) {
                diagnostics.add(Diagnostic.error(where, it.line, (key.equals("highlightElement") ? "highlight" : key)
                        + " must be a whole number of milliseconds, got '" + v + "'", key.equals("timeout") ? "timeout=5000" : "highlight=500"));
            }
        }
        r.put("Options", RowMapper.writeOptions(baseRow == null ? "[]" : baseRow.get("Options"), options));
        r.put("waitBefore", number(it.flags.getOrDefault("waitBefore", "0"), "waitBefore", it.line));
        r.put("waitAfter", number(it.flags.getOrDefault("waitAfter", "0"), "waitAfter", it.line));
        String shot = it.flags.getOrDefault("shot", "");
        if (!List.of("", "before", "after", "both").contains(shot)) {
            diagnostics.add(Diagnostic.error(where, it.line, "shot must be before, after or both", "shot=after"));
        }
        r.put("doScreenshotBefore", shot.equals("before") || shot.equals("both") ? "1" : "0");
        r.put("doScreenshotAfter", shot.equals("after") || shot.equals("both") ? "1" : "0");
        put(r, "ScreenshotFileName", it.flags.getOrDefault("shotName", ""));
        put(r, "Description", it.description);
        putCondition(r, it.condition, "ConditionOperator", "ConditionValue1", "ConditionValue2", "ConditionValue3", "ConditionOptions");
    }

    private void putCondition(Row r, TcDoc.Cond c, String op, String v1, String v2, String v3, String options) {
        if (c == null || c.isAlways()) {
            r.put(op, "always");
            put(r, v1, "");
            put(r, v2, "");
            put(r, v3, "");
            r.put(options, RowMapper.writeOptions(r.get(options), Map.of()));
            if (r.get(options) == null) {
                r.put(options, "[]");
            }
            return;
        }
        r.put(op, c.operator);
        put(r, v1, value(c.values, 0));
        put(r, v2, value(c.values, 1));
        put(r, v3, value(c.values, 2));
        Map<String, String> opts = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : c.options.entrySet()) {
            opts.put(e.getKey().equals("highlight") ? "highlightElement" : e.getKey(), e.getValue());
        }
        r.put(options, RowMapper.writeOptions(r.get(options), opts));
    }

    // ---------------------------------------------------------------- helpers

    private static Row keyRow(TcDoc doc) {
        Row r = new Row();
        r.put("Test", doc.test);
        r.put("Testcase", doc.testcase);
        return r;
    }

    private static Map<String, Row> index(Aggregate base, Table table, String column) {
        Map<String, Row> m = new LinkedHashMap<>();
        if (base != null) {
            for (Row r : base.get(table)) {
                m.put(r.s(column), r);
            }
        }
        return m;
    }

    private static String value(List<String> values, int index) {
        return index < values.size() ? Text.nz(values.get(index)) : "";
    }

    /** Sets a column, leaving a NULL alone when the wanted value is empty: no churn on legacy rows. */
    private static void put(Row r, String column, String value) {
        if ((value == null || value.isEmpty()) && r.containsKey(column) && r.get(column) == null) {
            return;
        }
        r.put(column, value == null ? "" : value);
    }

    private String number(String value, String name, int line) {
        String v = Text.nz(value).trim();
        if (!v.matches("-?\\d+")) {
            diagnostics.add(Diagnostic.error(where, line, name + " must be a whole number, got '" + value + "'", name + "=0"));
            return "0";
        }
        return v;
    }

    static Boolean bool(String value) {
        String v = Text.nz(value).trim().toLowerCase();
        return switch (v) {
            case "yes", "y", "true", "1", "on" -> Boolean.TRUE;
            case "no", "n", "false", "0", "off" -> Boolean.FALSE;
            default -> null;
        };
    }
}
