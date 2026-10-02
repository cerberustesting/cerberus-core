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

import org.cerberus.core.mcpdelta.catalog.Catalog;
import org.cerberus.core.mcpdelta.doc.Diagnostic;
import org.cerberus.core.mcpdelta.doc.DocParser;
import org.cerberus.core.mcpdelta.doc.TcDoc;
import org.cerberus.core.mcpdelta.util.Text;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The compiler of MCP Delta: checks a document against what this Cerberus instance knows, before anything
 * is written. Errors stop the write; warnings travel with a successful one. Each message says where, what,
 * and what to write instead.
 */
public final class Validator {

    private static final Pattern PROPERTY_USE = Pattern.compile("%(?:property\\.)?([A-Za-z0-9_\\-]+)%");
    /** Variables that are not properties: %system.x%, %object.x%, %service.x%, %datalib.x%, %SYS_...%... */
    private static final Pattern NON_PROPERTY = Pattern.compile("%(system|object|service|datalib|execution|tag|country|environment|robot|step|action|control|SYS_)[^%]*%",
            Pattern.CASE_INSENSITIVE);

    private final Catalog catalog;
    private final List<Diagnostic> out;
    private final String where;
    /** Folders and applications created earlier in the same write: they exist for this document. */
    private java.util.Set<String> extraFolders = java.util.Set.of();
    private java.util.Set<String> extraApps = java.util.Set.of();

    public Validator known(java.util.Set<String> folders, java.util.Set<String> apps) {
        this.extraFolders = folders;
        this.extraApps = apps;
        return this;
    }

    public Validator(Catalog catalog, List<Diagnostic> out, String where) {
        this.catalog = catalog;
        this.out = out;
        this.where = where;
    }

    public void check(TcDoc doc, boolean creating) {
        if (!catalog.tests().contains(doc.test) && !extraFolders.contains(doc.test)) {
            error(doc.line, "test folder '" + doc.test + "' does not exist", suggest(doc.test, catalog.tests()));
        }
        if (Text.isBlank(doc.title)) {
            error(doc.line, "the testcase needs a title", "testcase " + doc.ref() + ": <what it checks>");
        }
        String app = doc.attrs.get("application");
        if (app == null && creating) {
            error(doc.line, "a new testcase needs its application", "application=" + first(catalog.applications().keySet()));
        } else if (app != null && !catalog.applications().containsKey(app) && !extraApps.contains(app)) {
            error(doc.line, "application '" + app + "' does not exist", suggest(app, catalog.applications().keySet()));
        }
        oneOf(doc.line, "status", doc.attrs.get("status"), catalog.invariant("TCSTATUS"));
        oneOf(doc.line, "priority", doc.attrs.get("priority"), catalog.invariant("PRIORITY"));
        oneOf(doc.line, "type", doc.attrs.get("type"), catalog.invariant("TESTCASE_TYPE"));
        if (doc.attrs.containsKey("countries")) {
            List<String> countries = DocParser.splitList(doc.attrs.get("countries"));
            if (countries.isEmpty()) {
                error(doc.line, "countries cannot be empty", "countries=" + first(catalog.countryOrder()));
            }
            for (String c : countries) {
                oneOf(doc.line, "country", c, catalog.countryOrder());
            }
        }
        if (doc.condition != null && !doc.condition.isAlways()) {
            condition(doc.line, doc.condition, catalog.conditions("TESTCASE"));
        }

        Set<String> defined = new HashSet<>();
        for (TcDoc.Prop p : doc.props) {
            defined.add(p.name);
            if (!catalog.invariant("PROPERTYTYPE").isEmpty() && !catalog.invariant("PROPERTYTYPE").contains(p.type)) {
                error(p.line, "unknown property type '" + p.type + "'", suggest(p.type, catalog.invariant("PROPERTYTYPE")));
            }
            String nature = p.flags.get("nature");
            if (nature != null) {
                oneOf(p.line, "nature", nature, catalog.invariant("PROPERTYNATURE"));
            }
            String db = p.flags.get("db");
            List<String> databases = catalog.invariant("PROPERTYDATABASE");
            if ("getFromSql".equals(p.type)) {
                if (Text.isBlank(db)) {
                    error(p.line, "getFromSql property " + p.name + " needs its database", "db=" + first(databases));
                } else if (!databases.isEmpty() && !databases.contains(db)) {
                    error(p.line, "database '" + db + "' is not declared in this Cerberus (PROPERTYDATABASE)",
                            "use one of: " + String.join(", ", databases) + " — a new one must be added by an administrator");
                }
                if (p.values.isEmpty() || Text.isBlank(p.values.get(0))) {
                    error(p.line, "getFromSql property " + p.name + " needs its SQL query as first value", null);
                }
            }
        }

        Set<String> referenced = new LinkedHashSet<>();
        int stepNo = 0;
        for (TcDoc.Step s : doc.steps) {
            stepNo++;
            if (Text.isBlank(s.title)) {
                error(s.line, "step " + stepNo + " needs a title", "step " + stepNo + ": <what it does>");
            }
            String loop = s.flags.get("loop");
            if (loop != null) {
                oneOf(s.line, "loop", loop, catalog.invariant("STEPLOOP"));
            }
            if (s.condition != null && !s.condition.isAlways()) {
                condition(s.line, s.condition, catalog.conditions("STEP"));
                collect(referenced, s.condition.values);
            }
            for (TcDoc.Item a : s.actions) {
                item(a, true);
                baseUrl(a, app != null ? app : null);
                collect(referenced, a.values);
                if (a.condition != null) {
                    collect(referenced, a.condition.values);
                }
                for (TcDoc.Item c : a.controls) {
                    item(c, false);
                    baseUrl(c, app);
                    collect(referenced, c.values);
                    if (c.condition != null) {
                        collect(referenced, c.condition.values);
                    }
                }
            }
        }
        for (TcDoc.Prop p : doc.props) {
            // SQL and scripts use % for their own purposes (LIKE '%x%'): only plain values are scanned.
            if ("text".equals(p.type)) {
                collect(referenced, p.values);
            }
        }
        for (String name : referenced) {
            if (!defined.contains(name)) {
                warning(0, "uses %property." + name + "% but this testcase defines no property '" + name + "'",
                        "fine if it comes from a library step's caller or a datalib; otherwise add: prop " + name + " = text \"...\"");
            }
        }
    }

    /**
     * Addresses are relative to the environment URL, as the engine sees them: openUrlWithBase prefixes it
     * (repeating its path opens a page that does not exist), and the verifyUrl controls compare the address
     * with it removed (Cerberus' getCurrentUrl: https://host/fr/tarifs/ reads "/tarifs/" when the
     * environment URL is https://host/fr/), so "/fr/tarifs/" or a full URL can never match.
     */
    private void baseUrl(TcDoc.Item it, String app) {
        boolean open = "openUrlWithBase".equals(it.name);
        boolean url = it.name.startsWith("verifyUrl") && !it.name.equals("verifyUrlMatchRegex");
        if (!(open || url) || it.values.isEmpty() || app == null) {
            return;
        }
        String raw = it.values.get(0);
        for (String base : catalog.baseUrls(app)) {
            String host = base.substring(0, base.indexOf('|')).replaceAll("/+$", "");
            String path = base.substring(base.indexOf('|') + 1).replaceAll("^/+|/+$", "");
            String v = !host.isEmpty() && raw.startsWith(host) ? raw.substring(host.length()) : raw;
            String vv = v.replaceAll("^/+", "");
            boolean doubled = !path.isEmpty() && (vv.equals(path) || vv.startsWith(path + "/"));
            String rest = doubled ? vv.substring(path.length()).replaceAll("^/+", "") : vv;
            if (open && doubled) {
                warning(it.line, "openUrlWithBase adds the environment URL " + base.replace("|", "") + " itself; \"" + raw
                        + "\" would open " + base.replace("|", "") + vv, "write \"" + rest + "\"");
                return;
            }
            if (url && (doubled || !v.equals(raw))) {
                warning(it.line, it.name + " compares the address without the environment URL " + base.replace("|", "")
                        + "; \"" + raw + "\" can never match", "write \"/" + rest + "\"");
                return;
            }
        }
    }

    /**
     * An address or a selector built on a generated id (a product ULID, a UUID, a database counter) works today
     * and breaks the day the application's data is recreated — a reset, another environment, a reseed.
     */
    private void generatedId(TcDoc.Item it) {
        if (it.values.isEmpty() || !(it.name.startsWith("openUrl") || it.values.get(0).contains("=") || it.values.get(0).startsWith("/"))) {
            return;
        }
        String part = org.cerberus.core.mcpdelta.read.PageHints.generatedPart(it.values.get(0));
        if (part != null) {
            warning(it.line, it.name + " relies on a generated id (" + Text.truncate(part, 30) + "): it changes when the application's data is recreated",
                    "reach it by search or navigation, or read the id from the page into a property first");
        }
    }

    private void item(TcDoc.Item it, boolean action) {
        generatedId(it);
        Collection<String> known = action ? catalog.actions() : catalog.controls();
        String kind = action ? "action" : "control";
        if (!known.contains(it.name)) {
            Collection<String> other = action ? catalog.controls() : catalog.actions();
            String hint = other.contains(it.name)
                    ? it.name + " is a" + (action ? " control: indent it by 4 under the action it checks" : "n action: indent it by 2, directly under the step")
                    : suggest(it.name, known);
            error(it.line, "unknown " + kind + " '" + it.name + "'", hint);
            return;
        }
        Catalog.Spec spec = action ? catalog.actionSpec(it.name) : catalog.controlSpec(it.name);
        if (spec != null) {
            for (int i = 1; i <= 3; i++) {
                String v = i <= it.values.size() ? it.values.get(i - 1) : "";
                // openUrlWithBase "" opens the environment URL itself: a home page, not a missing value.
                String param = i == 1 ? spec.p1() : i == 2 ? spec.p2() : spec.p3();
                // A value that only applies to one technology or platform ("Nb Evt (Kafka)", "[APK,IPA only]") is not missing.
                boolean conditional = param != null && param.matches("(?i).*(\\((kafka|mongodb|ftp|soap)\\)|only[\\])]).*");
                if (spec.requires(i) && Text.isBlank(v) && action && !it.name.equals("openUrlWithBase") && !conditional) {
                    warning(it.line, it.name + " usually needs value " + i + " (" + (i == 1 ? spec.p1() : i == 2 ? spec.p2() : spec.p3()) + ")", null);
                }
            }
            if (spec.description() != null && spec.description().toUpperCase().contains("DEPRECATED")) {
                warning(it.line, it.name + " is deprecated", null);
            }
        }
        if (it.condition != null && !it.condition.isAlways()) {
            condition(it.line, it.condition, catalog.conditions(action ? "ACTION" : "CONTROL"));
        }
    }

    private void condition(int line, TcDoc.Cond c, Collection<String> known) {
        if (!known.isEmpty() && !known.contains(c.operator)) {
            error(line, "unknown condition '" + c.operator + "'", suggest(c.operator, known));
        }
    }

    private static void collect(Set<String> into, List<String> values) {
        for (String v : values) {
            if (v == null || v.indexOf('%') < 0) {
                continue;
            }
            String cleaned = NON_PROPERTY.matcher(v).replaceAll("");
            Matcher m = PROPERTY_USE.matcher(cleaned);
            while (m.find()) {
                into.add(m.group(1));
            }
        }
    }

    private void oneOf(int line, String what, String value, List<String> allowed) {
        if (value == null || allowed.isEmpty() || allowed.contains(value)) {
            return;
        }
        error(line, what + " '" + value + "' is not valid", "one of: " + String.join(", ", allowed));
    }

    private static String suggest(String wanted, Collection<String> known) {
        List<String> best = Text.closest(wanted, known, 3);
        return best.isEmpty() ? null : "did you mean " + String.join(", ", best) + "?";
    }

    private static String first(Collection<String> values) {
        return values.isEmpty() ? "..." : values.iterator().next();
    }

    private void error(int line, String message, String fix) {
        out.add(Diagnostic.error(where, line, message, fix));
    }

    private void warning(int line, String message, String fix) {
        out.add(Diagnostic.warning(where, line, message, fix));
    }
}
