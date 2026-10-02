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
package org.cerberus.core.mcpdelta.store;

import org.cerberus.core.mcpdelta.db.Db;
import org.cerberus.core.mcpdelta.db.Db.Row;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Loads, diffs and writes testcase aggregates. Every method runs on the caller's connection and transaction. */
public final class AggregateStore {

    /** What a write did, counted per table and kind, for the answer and the journal. */
    public static final class Stats {
        public int inserted;
        public int updated;
        public int deleted;
        public final Map<String, Integer> byTable = new LinkedHashMap<>();

        void count(Table t, String kind) {
            count(t.sql, kind);
        }

        public void count(String table, String kind) {
            byTable.merge(table.toLowerCase() + "." + kind, 1, Integer::sum);
            switch (kind) {
                case "insert" -> inserted++;
                case "update" -> updated++;
                default -> deleted++;
            }
        }

        public boolean isEmpty() {
            return inserted + updated + deleted == 0;
        }
    }

    /** Marks a value written as the database's NOW() rather than bound as a parameter. */
    private static final Object NOW = new Object();

    private AggregateStore() {
    }

    /** Loads a testcase with all its rows, or null when it does not exist. {@code lock} takes row locks. */
    public static Aggregate load(Connection c, String test, String testcase, boolean lock) throws SQLException {
        String suffix = lock ? " FOR UPDATE" : "";
        Row tc = Db.one(c, "SELECT * FROM testcase WHERE Test=? AND Testcase=?" + suffix, test, testcase);
        if (tc == null) {
            return null;
        }
        Aggregate a = new Aggregate(tc.s("Test"), tc.s("Testcase"));
        a.get(Table.TESTCASE).add(tc);
        a.get(Table.COUNTRY).addAll(Db.query(c, "SELECT * FROM testcasecountry WHERE Test=? AND Testcase=? ORDER BY Country" + suffix, test, testcase));
        a.get(Table.PROPERTY).addAll(Db.query(c, "SELECT * FROM testcasecountryproperties WHERE Test=? AND Testcase=? ORDER BY Property, Country" + suffix, test, testcase));
        a.get(Table.STEP).addAll(Db.query(c, "SELECT * FROM testcasestep WHERE Test=? AND Testcase=? ORDER BY Sort, StepId" + suffix, test, testcase));
        a.get(Table.ACTION).addAll(Db.query(c, "SELECT * FROM testcasestepaction WHERE Test=? AND Testcase=? ORDER BY StepId, Sort, ActionId" + suffix, test, testcase));
        a.get(Table.CONTROL).addAll(Db.query(c, "SELECT * FROM testcasestepactioncontrol WHERE Test=? AND Testcase=? ORDER BY StepId, ActionId, Sort, ControlId" + suffix, test, testcase));
        a.get(Table.LABEL).addAll(Db.query(c, "SELECT * FROM testcaselabel WHERE Test=? AND Testcase=? ORDER BY LabelId" + suffix, test, testcase));
        a.get(Table.DEPENDENCY).addAll(Db.query(c, "SELECT * FROM testcasedep WHERE Test=? AND Testcase=? ORDER BY ID" + suffix, test, testcase));
        return a;
    }

    /**
     * Brings the database from {@code base} to {@code target}, touching only the rows that differ.
     * {@code base} null creates the testcase, {@code target} null deletes it. Rows keep their identity, so
     * unchanged actions keep their ids, their creation stamp and their history links.
     */
    public static Stats apply(Connection c, Aggregate base, Aggregate target, String user) throws SQLException {
        Stats stats = new Stats();
        if (target == null) {
            if (base != null) {
                Db.update(c, "DELETE FROM testcase WHERE Test=? AND Testcase=?", base.test, base.testcase);
                stats.count(Table.TESTCASE, "delete");
            }
            return stats;
        }
        Table[] childrenFirst = {Table.CONTROL, Table.ACTION, Table.STEP, Table.PROPERTY, Table.LABEL, Table.COUNTRY};
        Table[] parentsFirst = {Table.COUNTRY, Table.PROPERTY, Table.STEP, Table.ACTION, Table.CONTROL, Table.LABEL};
        // Deletions first, deepest tables first, so a row whose key is reused never collides with itself.
        if (base != null) {
            for (Table t : childrenFirst) {
                Map<String, Row> wanted = index(t, target.get(t));
                for (Row r : base.get(t)) {
                    if (!wanted.containsKey(t.keyOf(r))) {
                        delete(c, t, r);
                        stats.count(t, "delete");
                    }
                }
            }
        }
        Row tcBase = base == null ? null : base.testcaseRow();
        Row tcTarget = target.testcaseRow();
        if (tcBase == null) {
            insert(c, Table.TESTCASE, tcTarget, user, false);
            stats.count(Table.TESTCASE, "insert");
        }
        for (Table t : parentsFirst) {
            Map<String, Row> existing = base == null ? Map.of() : index(t, base.get(t));
            for (Row r : target.get(t)) {
                Row old = existing.get(t.keyOf(r));
                if (old == null) {
                    insert(c, t, r, user, false);
                    stats.count(t, "insert");
                } else {
                    List<String> changed = changedColumns(t, old, r);
                    if (!changed.isEmpty()) {
                        updateColumns(c, t, r, changed, user);
                        stats.count(t, "update");
                    }
                }
            }
        }
        if (tcBase != null) {
            List<String> changed = changedColumns(Table.TESTCASE, tcBase, tcTarget);
            if (!changed.isEmpty() || !stats.isEmpty()) {
                // The testcase row records that its content changed, as the editor does: a new version
                // number and a modification stamp, even when only a step or a property moved.
                List<String> cols = new ArrayList<>(changed);
                Row r = tcTarget.copy();
                r.put("Version", String.valueOf(tcBase.i("Version") + 1));
                cols.add("Version");
                updateColumns(c, Table.TESTCASE, r, cols, user);
                stats.count(Table.TESTCASE, "update");
            }
        }
        return stats;
    }

    /** The row changes {@link #apply} would make, without making them: "table key: columns". */
    public static List<String> pendingChanges(Aggregate base, Aggregate target) {
        List<String> out = new ArrayList<>();
        for (Table t : Table.values()) {
            if (t == Table.DEPENDENCY) {
                continue;
            }
            Map<String, Row> before = base == null ? Map.of() : index(t, base.get(t));
            Map<String, Row> after = target == null ? Map.of() : index(t, target.get(t));
            for (Map.Entry<String, Row> e : before.entrySet()) {
                if (!after.containsKey(e.getKey())) {
                    out.add(t.name() + " delete " + e.getKey().replace('\u0001', '/'));
                }
            }
            for (Map.Entry<String, Row> e : after.entrySet()) {
                Row old = before.get(e.getKey());
                if (old == null) {
                    out.add(t.name() + " insert " + e.getKey().replace('\u0001', '/'));
                } else {
                    List<String> changed = changedColumns(t, old, e.getValue());
                    if (!changed.isEmpty()) {
                        StringBuilder sb = new StringBuilder(t.name() + " update " + e.getKey().replace('\u0001', '/') + ":");
                        for (String col : changed) {
                            sb.append(' ').append(col).append(" '").append(old.get(col)).append("'→'").append(e.getValue().get(col)).append('\'');
                        }
                        out.add(sb.toString());
                    }
                }
            }
        }
        return out;
    }

    /**
     * Puts a testcase back exactly as in {@code snapshot}, audit columns included: the undo path. A null
     * snapshot deletes the testcase (undo of a creation).
     */
    public static void restore(Connection c, String test, String testcase, Aggregate snapshot) throws SQLException {
        Row current = Db.one(c, "SELECT * FROM testcase WHERE Test=? AND Testcase=? FOR UPDATE", test, testcase);
        if (snapshot == null) {
            if (current != null) {
                Db.update(c, "DELETE FROM testcase WHERE Test=? AND Testcase=?", test, testcase);
            }
            return;
        }
        if (current == null) {
            insert(c, Table.TESTCASE, snapshot.testcaseRow(), null, true);
        } else {
            Row snap = snapshot.testcaseRow();
            updateColumns(c, Table.TESTCASE, snap, new ArrayList<>(snap.keySet()), null);
            Db.update(c, "DELETE FROM testcasestep WHERE Test=? AND Testcase=?", test, testcase);
            Db.update(c, "DELETE FROM testcasecountry WHERE Test=? AND Testcase=?", test, testcase);
            Db.update(c, "DELETE FROM testcaselabel WHERE Test=? AND Testcase=?", test, testcase);
        }
        for (Table t : new Table[]{Table.COUNTRY, Table.PROPERTY, Table.STEP, Table.ACTION, Table.CONTROL, Table.LABEL}) {
            for (Row r : snapshot.get(t)) {
                insert(c, t, r, null, true);
            }
        }
        if (current == null) {
            for (Row r : snapshot.get(Table.DEPENDENCY)) {
                if (Db.one(c, "SELECT ID FROM testcasedep WHERE ID=?", r.s("ID")) == null) {
                    insert(c, Table.DEPENDENCY, r, null, true);
                }
            }
        }
    }

    // ---------------------------------------------------------------- SQL

    private static Map<String, Row> index(Table t, List<Row> rows) {
        Map<String, Row> m = new LinkedHashMap<>();
        for (Row r : rows) {
            m.put(t.keyOf(r), r);
        }
        return m;
    }

    static List<String> changedColumns(Table t, Row before, Row after) {
        List<String> out = new ArrayList<>();
        for (Map.Entry<String, String> e : after.entrySet()) {
            if (!t.compared(e.getKey())) {
                continue;
            }
            String b = before.get(e.getKey());
            String a = e.getValue();
            if (b == null ? a != null : !b.equals(a)) {
                out.add(e.getKey());
            }
        }
        return out;
    }

    private static void insert(Connection c, Table t, Row r, String user, boolean exact) throws SQLException {
        List<String> cols = new ArrayList<>();
        List<Object> vals = new ArrayList<>();
        for (Map.Entry<String, String> e : r.entrySet()) {
            String col = e.getKey();
            if (!exact && (Table.AUDIT.contains(col.toLowerCase()) || (t == Table.LABEL && col.equalsIgnoreCase("Id")))) {
                continue;
            }
            cols.add(col);
            vals.add(e.getValue());
        }
        if (!exact) {
            cols.add("UsrCreated");
            vals.add(user);
            cols.add("DateCreated");
            vals.add(NOW);
        }
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(t.sql).append(" (");
        StringBuilder q = new StringBuilder();
        for (int i = 0; i < cols.size(); i++) {
            if (i > 0) {
                sql.append(',');
                q.append(',');
            }
            sql.append('`').append(cols.get(i)).append('`');
            q.append(vals.get(i) == NOW ? "NOW()" : "?");
        }
        sql.append(") VALUES (").append(q).append(')');
        Db.update(c, sql.toString(), vals.stream().filter(v -> v != NOW).toArray());
    }

    private static void updateColumns(Connection c, Table t, Row r, List<String> columns, String user) throws SQLException {
        List<Object> vals = new ArrayList<>();
        StringBuilder sql = new StringBuilder("UPDATE ").append(t.sql).append(" SET ");
        boolean first = true;
        for (String col : columns) {
            if (t.key.stream().anyMatch(k -> k.equalsIgnoreCase(col))) {
                continue;
            }
            if (user != null && Table.AUDIT.contains(col.toLowerCase())) {
                continue;
            }
            if (!first) {
                sql.append(',');
            }
            sql.append('`').append(col).append("`=?");
            vals.add(r.get(col));
            first = false;
        }
        if (user != null) {
            if (!first) {
                sql.append(',');
            }
            // Database clock, like every other stamp Cerberus writes.
            sql.append("`UsrModif`=?,`DateModif`=NOW()");
            vals.add(user);
            first = false;
        }
        if (first) {
            return;
        }
        sql.append(" WHERE ");
        for (int i = 0; i < t.key.size(); i++) {
            if (i > 0) {
                sql.append(" AND ");
            }
            sql.append('`').append(t.key.get(i)).append("`=?");
            vals.add(r.get(t.key.get(i)));
        }
        Db.update(c, sql.toString(), vals.toArray());
    }

    private static void delete(Connection c, Table t, Row r) throws SQLException {
        StringBuilder sql = new StringBuilder("DELETE FROM ").append(t.sql).append(" WHERE ");
        List<Object> vals = new ArrayList<>();
        for (int i = 0; i < t.key.size(); i++) {
            if (i > 0) {
                sql.append(" AND ");
            }
            sql.append('`').append(t.key.get(i)).append("`=?");
            vals.add(r.get(t.key.get(i)));
        }
        Db.update(c, sql.toString(), vals.toArray());
    }
}
