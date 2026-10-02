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
package org.cerberus.core.mcpdelta.entity;

import org.cerberus.core.mcpdelta.catalog.Catalog;
import org.cerberus.core.mcpdelta.db.Db;
import org.cerberus.core.mcpdelta.db.Db.Row;
import org.cerberus.core.mcpdelta.doc.Diagnostic;
import org.cerberus.core.mcpdelta.entity.Spec.Child;
import org.cerberus.core.mcpdelta.entity.Spec.Entity;
import org.cerberus.core.mcpdelta.entity.Spec.Field;
import org.cerberus.core.mcpdelta.store.AggregateStore;
import org.cerberus.core.mcpdelta.util.Text;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The generic engine behind every object document: load an object with its child rows, render it, build the
 * rows a document stands for, apply the difference in the caller's transaction, snapshot and restore it.
 */
public final class ObjStore {

    /** An object and its child rows, as stored. {@code root} is null for a collection document. */
    public static final class Agg {
        public final Entity spec;
        public final List<String> key;
        public Row root;
        public final Map<String, List<Row>> children = new LinkedHashMap<>();

        public Agg(Entity spec, List<String> key) {
            this.spec = spec;
            this.key = new ArrayList<>(key);
            for (Child c : spec.children) {
                children.put(c.kind, new ArrayList<>());
            }
        }

        public String ref() {
            return spec.kind + ":" + String.join("/", trimKey(key));
        }

        public boolean isEmpty() {
            return root == null && children.values().stream().allMatch(List::isEmpty);
        }
    }

    /** Names labels for the documents (label ids are not meaningful to a reader). */
    public interface Labels {
        String name(String id);

        String id(String name, String system);
    }

    private ObjStore() {
    }

    static List<String> trimKey(List<String> key) {
        List<String> k = new ArrayList<>(key);
        while (!k.isEmpty() && k.get(k.size() - 1).isEmpty()) {
            k.remove(k.size() - 1);
        }
        return k;
    }

    // ---------------------------------------------------------------- load

    public static Agg load(Connection c, Entity e, List<String> key, boolean lock) throws SQLException {
        Agg a = new Agg(e, key);
        String suffix = lock ? " FOR UPDATE" : "";
        if (!e.isCollection()) {
            StringBuilder sql = new StringBuilder("SELECT ").append(select(e)).append(" FROM ").append(e.table).append(" WHERE ");
            List<Object> params = new ArrayList<>();
            for (int i = 0; i < e.keys.size(); i++) {
                if (i > 0) {
                    sql.append(" AND ");
                }
                sql.append('`').append(e.keys.get(i).column).append("`=?");
                params.add(i < key.size() ? key.get(i) : "");
            }
            a.root = Db.one(c, sql + suffix, params.toArray());
            if (a.root == null) {
                return null;
            }
        }
        for (Child ch : e.children) {
            a.children.get(ch.kind).addAll(childRows(c, a, ch, suffix));
        }
        if (e.isCollection() && a.isEmpty()) {
            return null;
        }
        return a;
    }

    private static List<Row> childRows(Connection c, Agg a, Child ch, String suffix) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(ch.table).append(" WHERE ");
        List<Object> params = new ArrayList<>();
        int i = 0;
        for (String[] l : ch.link) {
            if (i++ > 0) {
                sql.append(" AND ");
            }
            sql.append('`').append(l[0]).append("`=?");
            params.add(linkValue(a, l[1]));
        }
        if (ch.order != null) {
            sql.append(" ORDER BY ").append(ch.order);
        }
        return Db.query(c, sql + suffix, params.toArray());
    }

    /** The value a child's link column takes: a constant, a column of the parent row, or a key of a collection. */
    static String linkValue(Agg a, String parentColumnOrConstant) {
        if (parentColumnOrConstant.startsWith("=")) {
            return parentColumnOrConstant.substring(1);
        }
        if (a.root != null) {
            return a.root.get(parentColumnOrConstant);
        }
        for (int i = 0; i < a.spec.keys.size(); i++) {
            if (a.spec.keys.get(i).column.equalsIgnoreCase(parentColumnOrConstant)) {
                return i < a.key.size() ? a.key.get(i) : "";
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- rows → document

    public static ObjDoc toDoc(Agg a, Labels labels) {
        ObjDoc d = new ObjDoc();
        d.kind = a.spec.kind;
        d.key.addAll(a.key);
        if (a.root != null) {
            if (a.spec.title != null) {
                d.title = a.root.s(a.spec.title);
            }
            for (Field f : a.spec.fields) {
                String v = display(f, a.root.get(f.column), labels);
                if (show(f, a.root.get(f.column), v)) {
                    d.attrs.put(f.attr, v);
                }
            }
        }
        for (Child ch : a.spec.children) {
            for (Row r : a.children.get(ch.kind)) {
                ObjDoc.Line l = new ObjDoc.Line();
                l.kind = ch.kind;
                for (Field k : ch.keys) {
                    l.keys.add(display(k, r.get(k.column), labels));
                }
                for (Field f : ch.fields) {
                    String v = display(f, r.get(f.column), labels);
                    if (show(f, r.get(f.column), v)) {
                        l.attrs.put(f.attr, v);
                    }
                }
                if (ch.description != null) {
                    l.description = r.s(ch.description);
                }
                d.lines.add(l);
            }
        }
        return d;
    }

    private static boolean show(Field f, String raw, String display) {
        if (f.always) {
            return true;
        }
        if (raw == null) {
            return false;
        }
        String def = f.def == null ? "" : f.def;
        return switch (f.type) {
            case BOOL -> !Text.nz(raw).equals(def) && !(raw.isEmpty());
            case YN -> !Text.nz(raw).equalsIgnoreCase(def) && !raw.isEmpty();
            case INT -> !raw.isEmpty() && !raw.equals(def);
            default -> !display.isEmpty() && !raw.equals(def);
        };
    }

    static String display(Field f, String raw, java.util.function.Function<String, String> labelName, Object unused) {
        if (f.type == Spec.Type.LABEL) {
            return raw == null ? "" : Text.nz(labelName.apply(raw));
        }
        return display(f, raw, (Labels) null);
    }

    static String display(Field f, String raw, Labels labels) {
        if (raw == null) {
            return "";
        }
        return switch (f.type) {
            case BOOL -> "1".equals(raw) || "true".equalsIgnoreCase(raw) ? "yes" : "no";
            case YN -> "Y".equalsIgnoreCase(raw) ? "yes" : "no";
            case SECRET -> raw.isEmpty() ? "" : "***";
            case LIST -> {
                String t = raw.trim();
                if (t.startsWith("[")) {
                    try {
                        List<String> items = new com.fasterxml.jackson.databind.ObjectMapper().readValue(t,
                                new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {
                                });
                        yield String.join(",", items);
                    } catch (Exception ex) {
                        yield raw;
                    }
                }
                yield raw;
            }
            case LABEL -> {
                String n = labels == null ? null : labels.name(raw);
                yield n == null ? (raw.isEmpty() || "0".equals(raw) ? "" : "#" + raw) : n;
            }
            default -> raw;
        };
    }

    // ---------------------------------------------------------------- document → rows

    /**
     * The rows a document stands for, on top of what is stored. Header attributes left out keep their value;
     * child lines are the whole truth for their object (a line not written is a row removed).
     */
    public static Agg build(Entity e, ObjDoc d, Agg base, Labels labels, Catalog catalog, List<Diagnostic> diags, String where) {
        Agg t = new Agg(e, d.key);
        String system = null;
        if (!e.isCollection()) {
            Row r = base != null ? base.root.copy() : defaults(e.fields);
            for (int i = 0; i < e.keys.size(); i++) {
                r.put(e.keys.get(i).column, i < d.key.size() ? d.key.get(i) : "");
            }
            if (e.title != null && d.title != null) {
                r.put(e.title, d.title);
            }
            for (Map.Entry<String, String> a : d.attrs.entrySet()) {
                Field f = e.field(a.getKey());
                if (f == null) {
                    diags.add(Diagnostic.error(where, d.line, "unknown " + e.kind + " attribute '" + a.getKey() + "'",
                            "known: " + String.join(", ", Text.closest(a.getKey(), e.fields.stream().map(x -> x.attr).toList(), 5))));
                    continue;
                }
                set(r, f, a.getValue(), base == null ? null : base.root, labels, catalog, diags, where, d.line, null);
            }
            if (base == null) {
                for (Field f : e.fields) {
                    if (f.always && f.values != null && Text.isBlank(r.get(f.column)) && !d.attrs.containsKey(f.attr)) {
                        diags.add(Diagnostic.error(where, d.line, "a new " + e.kind + " needs " + f.attr,
                                f.attr + "=" + first(catalog.invariant(f.values))));
                    }
                }
            }
            t.root = r;
            system = r.get("System") != null ? r.get("System") : r.get("system");
        } else {
            system = e.keys.get(0).column.equalsIgnoreCase("System") && !d.key.isEmpty() ? d.key.get(0) : null;
        }
        Set<String> known = new LinkedHashSet<>();
        for (Child ch : e.children) {
            known.add(ch.kind);
        }
        for (ObjDoc.Line l : d.lines) {
            if (!known.contains(l.kind)) {
                diags.add(Diagnostic.error(where, l.line, "unknown line '" + l.kind + "' in a " + e.kind,
                        "lines of a " + e.kind + ": " + String.join(", ", known)));
            }
        }
        for (Child ch : e.children) {
            Map<String, Row> baseRows = new LinkedHashMap<>();
            if (base != null) {
                for (Row r : base.children.get(ch.kind)) {
                    baseRows.put(childKey(ch, r), r);
                }
            }
            Set<String> seen = new LinkedHashSet<>();
            for (ObjDoc.Line l : d.lines) {
                if (!l.kind.equals(ch.kind)) {
                    continue;
                }
                if (l.keys.size() != ch.keys.size()) {
                    diags.add(Diagnostic.error(where, l.line, "'" + ch.kind + "' takes " + ch.keys.size() + " value(s) before its attributes: "
                            + String.join(" ", ch.keys.stream().map(k -> "<" + k.attr + ">").toList()), null));
                    continue;
                }
                Row probe = new Row();
                for (int i = 0; i < ch.keys.size(); i++) {
                    set(probe, ch.keys.get(i), l.keys.get(i), null, labels, catalog, diags, where, l.line, system);
                }
                String k = childKey(ch, probe);
                if (!seen.add(k)) {
                    diags.add(Diagnostic.error(where, l.line, ch.kind + " " + String.join(" ", l.keys) + " is written twice", "keep one line"));
                    continue;
                }
                Row base0 = baseRows.get(k);
                Row r = base0 != null ? base0.copy() : defaults(ch.fields);
                String quotedDesc = l.attrs.remove("desc");
                if (quotedDesc != null) {
                    l.description = quotedDesc;
                }
                r.putAll(probe);
                for (Field f : ch.fields) {
                    String v = l.attrs.get(f.attr);
                    if (v == null) {
                        // The line is the whole child: an attribute left out takes its default, except a secret,
                        // which stays as it is unless written.
                        if (f.type == Spec.Type.SECRET) {
                            continue;
                        }
                        if (base0 != null && display(f, base0.get(f.column), labels).isEmpty()
                                && (f.def == null || f.def.isEmpty() || f.def.equals(base0.get(f.column)))) {
                            continue; // an empty value (NULL, "", 0) stays exactly as stored
                        }
                        if (f.def != null) {
                            r.put(f.column, f.def);
                        } else if (f.type == Spec.Type.LABEL) {
                            r.put(f.column, "0");
                        } else if (base0 == null || base0.get(f.column) == null) {
                            r.put(f.column, null);
                        } else {
                            r.put(f.column, f.type == Spec.Type.INT ? null : "");
                        }
                        continue;
                    }
                    set(r, f, v, base0, labels, catalog, diags, where, l.line, system);
                }
                for (String attr : l.attrs.keySet()) {
                    if (ch.fields.stream().noneMatch(f -> f.attr.equalsIgnoreCase(attr) || f.column.equalsIgnoreCase(attr))) {
                        diags.add(Diagnostic.error(where, l.line, "unknown attribute '" + attr + "' on " + ch.kind,
                                "known: " + String.join(", ", ch.fields.stream().map(f -> f.attr).toList())));
                    }
                }
                if (ch.description != null) {
                    if (!(l.description.isEmpty() && base0 != null && base0.get(ch.description) == null)) {
                        r.put(ch.description, l.description);
                    }
                }
                t.children.get(ch.kind).add(r);
            }
        }
        return t;
    }

    private static Row defaults(List<Field> fields) {
        Row r = new Row();
        for (Field f : fields) {
            if (f.def != null) {
                r.put(f.column, f.def);
            }
        }
        return r;
    }

    private static void set(Row r, Field f, String value, Row base, Labels labels, Catalog catalog, List<Diagnostic> diags,
                            String where, int line, String system) {
        String v = Text.nz(value);
        if (base != null && base.containsKey(f.column) && display(f, base.get(f.column), labels).equals(v)) {
            // Writing back what was read keeps the stored form exactly (a legacy "DEFAULT" stays, not ["DEFAULT"]).
            r.put(f.column, base.get(f.column));
            return;
        }
        switch (f.type) {
            case BOOL, YN -> {
                String b = switch (v.trim().toLowerCase()) {
                    case "yes", "y", "true", "1", "on" -> f.type == Spec.Type.BOOL ? "1" : "Y";
                    case "no", "n", "false", "0", "off" -> f.type == Spec.Type.BOOL ? "0" : "N";
                    default -> null;
                };
                if (b == null) {
                    diags.add(Diagnostic.error(where, line, f.attr + " must be yes or no, got '" + v + "'", f.attr + "=yes"));
                    return;
                }
                r.put(f.column, b);
            }
            case INT -> {
                if (v.isBlank()) {
                    r.put(f.column, f.def == null ? null : f.def);
                } else if (!v.trim().matches("-?\\d+")) {
                    diags.add(Diagnostic.error(where, line, f.attr + " must be a whole number, got '" + v + "'", null));
                } else {
                    r.put(f.column, v.trim());
                }
            }
            case SECRET -> {
                if (!v.equals("***")) {
                    r.put(f.column, v);
                } else if (base == null) {
                    diags.add(Diagnostic.error(where, line, f.attr + " is written as *** only to keep the stored secret", "write the value"));
                }
            }
            case LIST -> {
                List<String> items = new ArrayList<>();
                for (String p : v.split(",")) {
                    if (!p.trim().isEmpty()) {
                        items.add(p.trim());
                    }
                }
                try {
                    r.put(f.column, new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(items));
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
            }
            case LABEL -> {
                if (v.isEmpty()) {
                    r.put(f.column, f.column.equalsIgnoreCase("ParentLabelID") ? "0" : null);
                    return;
                }
                String id = v.startsWith("#") ? v.substring(1) : labels.id(v, system);
                if (id == null) {
                    diags.add(Diagnostic.error(where, line, "unknown label '" + v + "'", "read labels:<system> to see them, or create it there first"));
                    return;
                }
                r.put(f.column, id);
            }
            default -> {
                if (v.isEmpty() && f.def == null) {
                    // A nullable column left empty stays NULL: an empty string would break the foreign keys
                    // (an appservice "" application) and churn legacy rows.
                    r.put(f.column, null);
                    return;
                }
                if (f.values != null && !v.isEmpty()) {
                    List<String> allowed = catalog.invariant(f.values);
                    if (!allowed.isEmpty() && !allowed.contains(v)) {
                        diags.add(Diagnostic.error(where, line, f.attr + " '" + v + "' is not valid",
                                "one of: " + String.join(", ", allowed)));
                        return;
                    }
                }
                r.put(f.column, v);
            }
        }
    }

    static String childKey(Child ch, Row r) {
        StringBuilder sb = new StringBuilder();
        for (Field k : ch.keys) {
            sb.append(Text.nz(r.get(k.column))).append('\u0001');
        }
        return sb.toString();
    }

    private static String first(List<String> values) {
        return values.isEmpty() ? "..." : values.get(0);
    }

    // ---------------------------------------------------------------- apply, snapshot, restore

    /** Writes the difference between {@code base} and {@code target}; target null deletes the object. */
    public static AggregateStore.Stats apply(Connection c, Agg base, Agg target, String user) throws SQLException {
        AggregateStore.Stats stats = new AggregateStore.Stats();
        Entity e = (target != null ? target : base).spec;
        if (target == null) {
            for (Child ch : e.children) {
                for (Row r : base.children.get(ch.kind)) {
                    deleteRow(c, ch.table, childWhere(ch), r);
                    stats.count(ch.table, "delete");
                }
            }
            if (base.root != null) {
                deleteRow(c, e.table, keyColumns(e), base.root);
                stats.count(e.table, "delete");
            }
            return stats;
        }
        if (!e.isCollection()) {
            if (base == null) {
                insertRow(c, e.table, target.root, e.audit, user, e.id);
                stats.count(e.table, "insert");
                if (e.id != null && target.root.get(e.id) == null) {
                    Row fresh = loadRoot(c, e, target.key);
                    target.root.put(e.id, fresh == null ? null : fresh.get(e.id));
                }
            } else {
                List<String> changed = changed(base.root, target.root, e.audit, e.id);
                if (!changed.isEmpty()) {
                    updateRow(c, e.table, keyColumns(e), target.root, changed, e.audit, user);
                    stats.count(e.table, "update");
                }
            }
        }
        for (Child ch : e.children) {
            Map<String, Row> before = new LinkedHashMap<>();
            if (base != null) {
                for (Row r : base.children.get(ch.kind)) {
                    before.put(childKey(ch, r), r);
                }
            }
            Map<String, Row> after = new LinkedHashMap<>();
            for (Row r : target.children.get(ch.kind)) {
                for (String[] l : ch.link) {
                    r.put(l[0], linkValue(target, l[1]));
                }
                after.put(childKey(ch, r), r);
            }
            for (Map.Entry<String, Row> b : before.entrySet()) {
                if (!after.containsKey(b.getKey())) {
                    deleteRow(c, ch.table, childWhere(ch), b.getValue());
                    stats.count(ch.table, "delete");
                }
            }
            for (Map.Entry<String, Row> a : after.entrySet()) {
                Row old = before.get(a.getKey());
                if (old == null) {
                    insertRow(c, ch.table, a.getValue(), ch.audit, user, ch.id);
                    stats.count(ch.table, "insert");
                } else {
                    List<String> changed = changed(old, a.getValue(), ch.audit, ch.id);
                    if (!changed.isEmpty()) {
                        Row withId = a.getValue();
                        updateRow(c, ch.table, childWhere(ch), withId, changed, ch.audit, user);
                        stats.count(ch.table, "update");
                    }
                }
            }
        }
        return stats;
    }

    static String select(Entity e) {
        if (e.columns == null) {
            return "*";
        }
        return String.join(",", e.columns.stream().map(col -> "`" + col + "`").toList());
    }

    private static Row loadRoot(Connection c, Entity e, List<String> key) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT ").append(select(e)).append(" FROM ").append(e.table).append(" WHERE ");
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < e.keys.size(); i++) {
            if (i > 0) {
                sql.append(" AND ");
            }
            sql.append('`').append(e.keys.get(i).column).append("`=?");
            params.add(i < key.size() ? key.get(i) : "");
        }
        return Db.one(c, sql.toString(), params.toArray());
    }

    private static List<String> keyColumns(Entity e) {
        List<String> k = new ArrayList<>();
        for (Field f : e.keys) {
            k.add(f.column);
        }
        return k;
    }

    /** A child row is addressed by its id when it has one, otherwise by its link and key columns. */
    private static List<String> childWhere(Child ch) {
        if (ch.id != null) {
            return List.of(ch.id);
        }
        List<String> cols = new ArrayList<>();
        for (String[] l : ch.link) {
            cols.add(l[0]);
        }
        for (Field k : ch.keys) {
            cols.add(k.column);
        }
        return cols;
    }

    private static List<String> changed(Row before, Row after, Spec.Audit audit, String id) {
        List<String> out = new ArrayList<>();
        for (Map.Entry<String, String> e : after.entrySet()) {
            String col = e.getKey();
            if (audit.has(col) || (id != null && id.equalsIgnoreCase(col))) {
                continue;
            }
            String b = before.get(col);
            String a = e.getValue();
            if (b == null ? a != null : !b.equals(a)) {
                out.add(col);
            }
        }
        return out;
    }

    private static final Object NOW = new Object();

    private static void insertRow(Connection c, String table, Row r, Spec.Audit audit, String user, String id) throws SQLException {
        List<String> cols = new ArrayList<>();
        List<Object> vals = new ArrayList<>();
        for (Map.Entry<String, String> e : r.entrySet()) {
            if (audit.has(e.getKey()) || (id != null && id.equalsIgnoreCase(e.getKey()) && e.getValue() == null)) {
                continue;
            }
            if (id != null && id.equalsIgnoreCase(e.getKey()) && user != null) {
                continue; // a new row gets a fresh id
            }
            cols.add(e.getKey());
            vals.add(e.getValue());
        }
        if (user != null) {
            if (audit.usrCreated() != null) {
                cols.add(audit.usrCreated());
                vals.add(user);
            }
            if (audit.dateCreated() != null) {
                cols.add(audit.dateCreated());
                vals.add(NOW);
            }
        } else {
            for (Map.Entry<String, String> e : r.entrySet()) {
                if (audit.has(e.getKey())) {
                    cols.add(e.getKey());
                    vals.add(e.getValue());
                }
            }
        }
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(table).append(" (");
        StringBuilder q = new StringBuilder();
        List<Object> bind = new ArrayList<>();
        for (int i = 0; i < cols.size(); i++) {
            if (i > 0) {
                sql.append(',');
                q.append(',');
            }
            sql.append('`').append(cols.get(i)).append('`');
            if (vals.get(i) == NOW) {
                q.append("NOW()");
            } else {
                q.append('?');
                bind.add(vals.get(i));
            }
        }
        Db.update(c, sql.append(") VALUES (").append(q).append(')').toString(), bind.toArray());
    }

    private static void updateRow(Connection c, String table, List<String> where, Row r, List<String> columns, Spec.Audit audit,
                                  String user) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE ").append(table).append(" SET ");
        List<Object> bind = new ArrayList<>();
        boolean first = true;
        for (String col : columns) {
            if (where.stream().anyMatch(w -> w.equalsIgnoreCase(col))) {
                continue;
            }
            if (!first) {
                sql.append(',');
            }
            sql.append('`').append(col).append("`=?");
            bind.add(r.get(col));
            first = false;
        }
        if (user != null && audit.usrModif() != null) {
            sql.append(first ? "" : ",").append('`').append(audit.usrModif()).append("`=?,`").append(audit.dateModif()).append("`=NOW()");
            bind.add(user);
            first = false;
        }
        if (first) {
            return;
        }
        sql.append(" WHERE ");
        for (int i = 0; i < where.size(); i++) {
            if (i > 0) {
                sql.append(" AND ");
            }
            sql.append('`').append(where.get(i)).append("`=?");
            bind.add(r.get(where.get(i)));
        }
        Db.update(c, sql.toString(), bind.toArray());
    }

    private static void deleteRow(Connection c, String table, List<String> where, Row r) throws SQLException {
        StringBuilder sql = new StringBuilder("DELETE FROM ").append(table).append(" WHERE ");
        List<Object> bind = new ArrayList<>();
        for (int i = 0; i < where.size(); i++) {
            if (i > 0) {
                sql.append(" AND ");
            }
            sql.append('`').append(where.get(i)).append("`=?");
            bind.add(r.get(where.get(i)));
        }
        Db.update(c, sql.toString(), bind.toArray());
    }

    public static Map<String, List<Map<String, String>>> snapshot(Agg a) {
        if (a == null) {
            return null;
        }
        Map<String, List<Map<String, String>>> out = new LinkedHashMap<>();
        if (a.root != null) {
            out.put("__root", List.of(new LinkedHashMap<>(a.root)));
        }
        for (Map.Entry<String, List<Row>> e : a.children.entrySet()) {
            List<Map<String, String>> rows = new ArrayList<>();
            for (Row r : e.getValue()) {
                rows.add(new LinkedHashMap<>(r));
            }
            out.put(e.getKey(), rows);
        }
        return out;
    }

    /**
     * Puts an object back exactly as in a snapshot (null: removes it, the undo of a creation). Rows are matched
     * (by id when they have one), and only what differs is deleted, inserted or updated in place: a label still
     * used by testcases is never deleted and re-created, so nothing that points at it breaks or cascades.
     */
    public static void restore(Connection c, Entity e, List<String> key, Map<String, List<Map<String, String>>> snap) throws SQLException {
        Agg current = load(c, e, key, true);
        if (snap == null) {
            if (current != null) {
                apply(c, current, null, null);
            }
            return;
        }
        Agg target = new Agg(e, key);
        if (snap.containsKey("__root")) {
            target.root = new Row(snap.get("__root").get(0));
        }
        for (Child ch : e.children) {
            for (Map<String, String> m : snap.getOrDefault(ch.kind, List.of())) {
                target.children.get(ch.kind).add(new Row(m));
            }
        }
        if (!e.isCollection()) {
            if (current == null) {
                insertRow(c, e.table, target.root, e.audit, null, e.id);
            } else {
                List<String> cols = exactChanges(current.root, target.root);
                if (!cols.isEmpty()) {
                    updateRow(c, e.table, keyColumns(e), target.root, cols, Spec.Audit.NONE, null);
                }
            }
        }
        for (Child ch : e.children) {
            Map<String, Row> now = new LinkedHashMap<>();
            if (current != null) {
                for (Row r : current.children.get(ch.kind)) {
                    now.put(restoreKey(ch, r), r);
                }
            }
            Map<String, Row> want = new LinkedHashMap<>();
            for (Row r : target.children.get(ch.kind)) {
                want.put(restoreKey(ch, r), r);
            }
            for (Map.Entry<String, Row> n : now.entrySet()) {
                if (!want.containsKey(n.getKey())) {
                    deleteRow(c, ch.table, childWhere(ch), n.getValue());
                }
            }
            for (Map.Entry<String, Row> w : want.entrySet()) {
                Row old = now.get(w.getKey());
                if (old == null) {
                    insertRow(c, ch.table, w.getValue(), ch.audit, null, ch.id);
                } else {
                    List<String> cols = exactChanges(old, w.getValue());
                    if (!cols.isEmpty()) {
                        updateRow(c, ch.table, childWhere(ch), w.getValue(), cols, Spec.Audit.NONE, null);
                    }
                }
            }
        }
    }

    private static String restoreKey(Child ch, Row r) {
        return ch.id != null && r.get(ch.id) != null ? "#" + r.get(ch.id) : childKey(ch, r);
    }

    private static List<String> exactChanges(Row before, Row after) {
        List<String> out = new ArrayList<>();
        for (Map.Entry<String, String> e : after.entrySet()) {
            String b = before.get(e.getKey());
            if (b == null ? e.getValue() != null : !b.equals(e.getValue())) {
                out.add(e.getKey());
            }
        }
        return out;
    }

    /** Content fingerprint (audit columns and row ids left out): the precondition of undo and plans. */
    public static String fingerprint(Agg a) {
        if (a == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (a.root != null) {
            sb.append(canon(a.root, a.spec.audit, a.spec.id)).append('\n');
        }
        for (Child ch : a.spec.children) {
            Map<String, String> sorted = new TreeMap<>();
            for (Row r : a.children.get(ch.kind)) {
                sorted.put(childKey(ch, r), canon(r, ch.audit, ch.id));
            }
            sb.append(ch.kind).append(sorted).append('\n');
        }
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(sb.toString().getBytes(StandardCharsets.UTF_8)))
                    .substring(0, 12);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String canon(Row r, Spec.Audit audit, String id) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : new TreeMap<>(r).entrySet()) {
            if (audit.has(e.getKey()) || (id != null && id.equalsIgnoreCase(e.getKey()))) {
                continue;
            }
            sb.append(e.getKey().toLowerCase()).append('=').append(e.getValue() == null ? "∅" : e.getValue()).append(';');
        }
        return sb.toString();
    }

    /** Dependents that forbid a deletion, as messages; empty when it may go ahead. */
    public static List<String> guards(Connection c, Entity e, List<String> key) throws SQLException {
        List<String> out = new ArrayList<>();
        for (Spec.Guard g : e.guards) {
            int n = g.countSql().length() - g.countSql().replace("?", "").length();
            Object[] params = new Object[n];
            for (int i = 0; i < n; i++) {
                params[i] = key.get(i % e.keys.size());
            }
            Row r = Db.one(c, "SELECT (" + g.countSql() + ") AS n", params);
            int count = r == null ? 0 : r.i("n");
            if (count > 0) {
                out.add(count + " " + g.message());
            }
        }
        return out;
    }

    // ---------------------------------------------------------------- listings

    public static List<Row> list(Connection c, Entity e, String filter, int limit) throws SQLException {
        if (e.isCollection()) {
            Child ch = e.children.get(0);
            String group = ch.link.get(0)[0];
            String where = Text.isBlank(filter) ? "" : " WHERE `" + group + "` LIKE ?";
            return Db.query(c, "SELECT `" + group + "` AS k0, COUNT(*) AS n FROM " + ch.table + where + " GROUP BY `" + group + "` ORDER BY `" + group
                    + "` LIMIT " + limit, Text.isBlank(filter) ? new Object[0] : new Object[]{"%" + filter + "%"});
        }
        StringBuilder sql = new StringBuilder("SELECT ").append(select(e)).append(" FROM ").append(e.table);
        List<Object> params = new ArrayList<>();
        if (!Text.isBlank(filter)) {
            sql.append(" WHERE (");
            boolean first = true;
            List<String> cols = new ArrayList<>(keyColumns(e));
            if (e.title != null) {
                cols.add(e.title);
            }
            for (String col : cols) {
                sql.append(first ? "" : " OR ").append('`').append(col).append("` LIKE ?");
                params.add("%" + filter + "%");
                first = false;
            }
            if (e.filterColumn != null) {
                sql.append(" OR `").append(e.filterColumn).append("`=?");
                params.add(filter);
            }
            sql.append(')');
        }
        if (e.order != null) {
            sql.append(" ORDER BY ").append(e.order);
        }
        sql.append(" LIMIT ").append(limit);
        return Db.query(c, sql.toString(), params.toArray());
    }
}
