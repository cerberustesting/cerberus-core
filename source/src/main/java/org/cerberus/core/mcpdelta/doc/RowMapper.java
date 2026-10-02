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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cerberus.core.mcpdelta.db.Db.Row;
import org.cerberus.core.mcpdelta.store.Aggregate;
import org.cerberus.core.mcpdelta.store.Table;
import org.cerberus.core.mcpdelta.util.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Turns the rows of a testcase into its document. Defaults are left out — an action shows only what makes it
 * different from a plain action — which is most of why a document is a fraction of the size of the JSON the
 * classic tools return.
 */
public final class RowMapper {

    /** Lookups a document needs beyond the testcase's own rows. */
    public interface Lookup {
        /** 1-based position of a step in its testcase, or null when that step does not exist. */
        Integer stepPosition(String test, String testcase, int stepId);

        String labelName(String labelId);

        List<String> countryOrder();
    }

    public static final String DEFAULT_LOOP = "onceIfConditionTrue";
    /** The four settings of an action, control or condition, in the order the editor writes them. */
    public static final List<String> OPTION_ORDER = List.of("timeout", "minSimilarity", "highlightElement", "typeDelay");
    private static final ObjectMapper JSON = new ObjectMapper();

    private RowMapper() {
    }

    public static TcDoc toDoc(Aggregate a, Lookup lookup, boolean full) {
        Row tc = a.testcaseRow();
        TcDoc doc = new TcDoc();
        doc.test = tc.s("Test");
        doc.testcase = tc.s("Testcase");
        doc.title = tc.s("Description");
        doc.attrs.put("application", tc.s("Application"));
        doc.attrs.put("status", tc.s("Status"));
        doc.attrs.put("priority", tc.s("Priority"));
        List<String> countries = orderedCountries(a, lookup);
        doc.attrs.put("countries", String.join(",", countries));
        List<String> labels = new ArrayList<>();
        for (Row r : a.get(Table.LABEL)) {
            String name = lookup.labelName(r.s("LabelId"));
            labels.add(name == null ? "#" + r.s("LabelId") : name);
        }
        if (!labels.isEmpty()) {
            labels.sort(String::compareTo);
            doc.attrs.put("labels", String.join(",", labels));
        }
        nonDefault(doc.attrs, "type", tc.s("Type"), "AUTOMATED");
        nonDefault(doc.attrs, "active", yesNo(tc.s("isActive")), "yes");
        nonDefault(doc.attrs, "muted", yesNo(tc.s("isMuted")), "no");
        nonDefault(doc.attrs, "activeQA", yesNo(tc.s("isActiveQA")), "yes");
        nonDefault(doc.attrs, "activeUAT", yesNo(tc.s("isActiveUAT")), "yes");
        nonDefault(doc.attrs, "activePROD", yesNo(tc.s("isActivePROD")), "yes");
        nonDefault(doc.attrs, "details", tc.s("DetailedDescription"), "");
        nonDefault(doc.attrs, "comment", tc.s("Comment"), "");
        String bugs = tc.s("Bugs");
        if (!bugs.isBlank() && !bugs.trim().equals("[]")) {
            doc.attrs.put("bugs", bugs);
        }
        for (String[] m : new String[][]{{"fromMajor", "FromMajor"}, {"fromMinor", "FromMinor"}, {"toMajor", "ToMajor"},
                {"toMinor", "ToMinor"}, {"targetMajor", "TargetMajor"}, {"targetMinor", "TargetMinor"},
                {"useragent", "useragent"}, {"screensize", "screensize"}}) {
            nonDefault(doc.attrs, m[0], tc.s(m[1]), "");
        }
        if (full) {
            for (String[] m : new String[][]{{"origin", "Origine"}, {"refOrigin", "RefOrigine"}, {"implementer", "Implementer"},
                    {"executor", "Executor"}}) {
                nonDefault(doc.attrs, m[0], tc.s(m[1]), "");
            }
        }
        doc.condition = condition(tc, "ConditionOperator", "ConditionValue1", "ConditionValue2", "ConditionValue3", "ConditionOptions");

        doc.props.addAll(props(a.get(Table.PROPERTY), countries));

        for (Row s : a.get(Table.STEP)) {
            TcDoc.Step step = new TcDoc.Step();
            step.title = s.s("Description");
            if (s.i("IsUsingLibraryStep") == 1) {
                Integer pos = lookup.stepPosition(s.s("LibraryStepTest"), s.s("LibraryStepTestcase"), s.i("LibraryStepStepId"));
                String target = s.s("LibraryStepTest") + "/" + s.s("LibraryStepTestcase");
                step.flags.put("use", target + (pos == null ? "@" + s.i("LibraryStepStepId") : "#" + pos));
            }
            if (s.i("IsLibraryStep") == 1) {
                step.flags.put("library", "true");
            }
            if (s.i("IsExecutionForced") == 1) {
                step.flags.put("forced", "true");
            }
            String loop = s.s("Loop");
            if (!loop.isEmpty() && !loop.equals(DEFAULT_LOOP)) {
                step.flags.put("loop", loop);
            }
            step.condition = condition(s, "ConditionOperator", "ConditionValue1", "ConditionValue2", "ConditionValue3", "ConditionOptions");
            for (Row ar : a.get(Table.ACTION)) {
                if (!ar.s("StepId").equals(s.s("StepId"))) {
                    continue;
                }
                TcDoc.Item action = item(ar, "Action");
                for (Row cr : a.get(Table.CONTROL)) {
                    if (cr.s("StepId").equals(ar.s("StepId")) && cr.s("ActionId").equals(ar.s("ActionId"))) {
                        action.controls.add(item(cr, "Control"));
                    }
                }
                step.actions.add(action);
            }
            doc.steps.add(step);
        }
        return doc;
    }

    public static List<String> orderedCountries(Aggregate a, Lookup lookup) {
        List<String> order = lookup.countryOrder();
        List<String> countries = new ArrayList<>();
        for (Row r : a.get(Table.COUNTRY)) {
            countries.add(r.s("Country"));
        }
        countries.sort(Comparator.comparingInt((String c) -> {
            int i = order.indexOf(c);
            return i < 0 ? Integer.MAX_VALUE : i;
        }).thenComparing(c -> c));
        return countries;
    }

    private static List<TcDoc.Prop> props(List<Row> rows, List<String> countries) {
        // One line per distinct definition: a property defined the same way in every country is one line.
        Map<String, TcDoc.Prop> byDefinition = new LinkedHashMap<>();
        Map<String, List<String>> countriesOf = new LinkedHashMap<>();
        List<Row> sorted = new ArrayList<>(rows);
        sorted.sort(Comparator.comparingInt((Row r) -> r.i("Rank")).thenComparing(r -> r.s("Property"))
                .thenComparingInt(r -> {
                    int i = countries.indexOf(r.s("Country"));
                    return i < 0 ? Integer.MAX_VALUE : i;
                }));
        for (Row r : sorted) {
            TcDoc.Prop p = new TcDoc.Prop();
            p.name = r.s("Property");
            p.type = r.s("Type");
            p.values.add(r.s("Value1"));
            p.values.add(r.s("Value2"));
            p.values.add(r.s("Value3"));
            nonDefault(p.flags, "db", r.s("Database"), "");
            nonDefault(p.flags, "length", r.s("Length"), "0");
            nonDefault(p.flags, "rows", r.s("RowLimit"), "0");
            nonDefault(p.flags, "nature", r.s("Nature"), "STATIC");
            nonDefault(p.flags, "cache", r.s("CacheExpire"), "0");
            nonDefault(p.flags, "retry", r.s("RetryNb"), "0");
            if (r.i("RetryNb") > 0) {
                nonDefault(p.flags, "retryPeriod", r.s("RetryPeriod"), "10000");
            }
            nonDefault(p.flags, "rank", r.s("Rank"), "0");
            p.description = r.s("Description");
            String key = DocRenderer.prop(p);
            byDefinition.putIfAbsent(key, p);
            countriesOf.computeIfAbsent(key, k -> new ArrayList<>()).add(r.s("Country"));
        }
        List<TcDoc.Prop> out = new ArrayList<>();
        for (Map.Entry<String, TcDoc.Prop> e : byDefinition.entrySet()) {
            TcDoc.Prop p = e.getValue();
            List<String> cs = countriesOf.get(e.getKey());
            if (!(cs.size() == countries.size() && cs.containsAll(countries))) {
                p.countries = cs;
            }
            out.add(p);
        }
        return out;
    }

    private static TcDoc.Item item(Row r, String nameColumn) {
        TcDoc.Item it = new TcDoc.Item();
        it.name = r.s(nameColumn);
        it.values.add(r.s("Value1"));
        it.values.add(r.s("Value2"));
        it.values.add(r.s("Value3"));
        if (r.containsKey("IsFatal") && r.i("IsFatal") == 0) {
            it.flags.put("fatal", "no");
        }
        for (Map.Entry<String, String> o : activeOptions(r.s("Options")).entrySet()) {
            String key = switch (o.getKey()) {
                case "highlightElement" -> "highlight";
                case "timeout", "minSimilarity", "typeDelay" -> o.getKey();
                default -> "opt." + o.getKey();
            };
            it.flags.put(key, o.getValue());
        }
        nonDefault(it.flags, "waitBefore", r.s("waitBefore"), "0");
        nonDefault(it.flags, "waitAfter", r.s("waitAfter"), "0");
        boolean before = r.i("doScreenshotBefore") == 1;
        boolean after = r.i("doScreenshotAfter") == 1;
        if (before || after) {
            it.flags.put("shot", before && after ? "both" : before ? "before" : "after");
        }
        nonDefault(it.flags, "shotName", r.s("ScreenshotFileName"), "");
        it.condition = condition(r, "ConditionOperator", "ConditionValue1", "ConditionValue2", "ConditionValue3", "ConditionOptions");
        it.description = r.s("Description");
        return it;
    }

    private static TcDoc.Cond condition(Row r, String op, String v1, String v2, String v3, String options) {
        String operator = r.s(op);
        if (operator.isEmpty() || operator.equals("always")) {
            return null;
        }
        TcDoc.Cond c = new TcDoc.Cond();
        c.operator = operator;
        c.values.add(r.s(v1));
        c.values.add(r.s(v2));
        c.values.add(r.s(v3));
        for (Map.Entry<String, String> o : activeOptions(r.s(options)).entrySet()) {
            c.options.put(o.getKey().equals("highlightElement") ? "highlight" : o.getKey(), o.getValue());
        }
        return c;
    }

    /** The options switched on in a stored options array, in their stored order. */
    public static Map<String, String> activeOptions(String json) {
        Map<String, String> out = new LinkedHashMap<>();
        for (Map<String, Object> o : parseOptions(json)) {
            if (Boolean.TRUE.equals(o.get("act")) || "true".equals(String.valueOf(o.get("act")))) {
                out.put(String.valueOf(o.get("option")), Text.nz(o.get("value")));
            }
        }
        return out;
    }

    public static List<Map<String, Object>> parseOptions(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            List<Map<String, Object>> list = JSON.readValue(json, new TypeReference<>() {
            });
            List<Map<String, Object>> out = new ArrayList<>();
            for (Map<String, Object> m : list) {
                if (m != null && m.get("option") != null) {
                    out.add(new LinkedHashMap<>(m));
                }
            }
            return out;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Writes the wanted options into a stored options array: wanted ones switched on with their value, the
     * others switched off but kept, unknown ones preserved. Returns the base text untouched when nothing is
     * on before or after, so a plain action never churns.
     */
    public static String writeOptions(String baseJson, Map<String, String> wanted) {
        List<Map<String, Object>> entries = parseOptions(baseJson);
        boolean baseActive = entries.stream().anyMatch(o -> Boolean.TRUE.equals(o.get("act")));
        if (wanted.isEmpty() && !baseActive) {
            return baseJson == null || baseJson.isBlank() ? "[]" : baseJson;
        }
        if (entries.isEmpty()) {
            for (String name : OPTION_ORDER) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("option", name);
                m.put("value", "");
                m.put("act", false);
                entries.add(m);
            }
        }
        Map<String, String> remaining = new LinkedHashMap<>(wanted);
        for (Map<String, Object> o : entries) {
            String name = String.valueOf(o.get("option"));
            if (remaining.containsKey(name)) {
                o.put("value", remaining.remove(name));
                o.put("act", true);
            } else {
                o.put("act", false);
            }
        }
        for (Map.Entry<String, String> e : remaining.entrySet()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("option", e.getKey());
            m.put("value", e.getValue());
            m.put("act", true);
            entries.add(m);
        }
        try {
            String out = JSON.writeValueAsString(entries);
            return Objects.equals(normalizeOptions(out), normalizeOptions(baseJson)) ? baseJson : out;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String normalizeOptions(String json) {
        try {
            return JSON.writeValueAsString(parseOptions(json));
        } catch (Exception e) {
            return json;
        }
    }

    /** Null for an empty column, so an unset flag reads as its default. */
    private static String yesNo(String flag) {
        if (flag == null || flag.isEmpty()) {
            return null;
        }
        return "1".equals(flag) || "Y".equalsIgnoreCase(flag) || "true".equalsIgnoreCase(flag) ? "yes" : "no";
    }

    private static void nonDefault(Map<String, String> map, String key, String value, String def) {
        if (value != null && !value.equals(def) && !(def.equals("0") && value.isEmpty())) {
            map.put(key, value);
        }
    }
}
