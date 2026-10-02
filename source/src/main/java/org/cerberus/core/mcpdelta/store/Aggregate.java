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

import org.cerberus.core.mcpdelta.db.Db.Row;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Every row of one testcase, across its tables. It is the unit MCP Delta locks, diffs, journals and
 * restores: a testcase is changed, or put back, as a whole or not at all.
 */
public final class Aggregate {

    public final String test;
    public final String testcase;
    public final Map<Table, List<Row>> rows = new EnumMap<>(Table.class);

    public Aggregate(String test, String testcase) {
        this.test = test;
        this.testcase = testcase;
        for (Table t : Table.values()) {
            rows.put(t, new ArrayList<>());
        }
    }

    public Row testcaseRow() {
        List<Row> r = rows.get(Table.TESTCASE);
        return r.isEmpty() ? null : r.get(0);
    }

    public List<Row> get(Table t) {
        return rows.get(t);
    }

    public String ref() {
        return test + "/" + testcase;
    }

    /**
     * Fingerprint of the testcase content (audit columns excluded). Two aggregates with the same
     * fingerprint describe the same testcase; it is the precondition checked before undo and apply.
     */
    public String fingerprint() {
        StringBuilder sb = new StringBuilder();
        for (Table t : Table.values()) {
            if (t == Table.DEPENDENCY) {
                continue;
            }
            Map<String, Row> sorted = new TreeMap<>();
            for (Row r : rows.get(t)) {
                sorted.put(t.keyOf(r), r);
            }
            for (Row r : sorted.values()) {
                sb.append(t.name()).append('|');
                for (Map.Entry<String, String> e : r.entrySet()) {
                    if (t.compared(e.getKey())) {
                        sb.append(e.getKey().toLowerCase()).append('=').append(e.getValue() == null ? "∅" : e.getValue()).append(';');
                    }
                }
                sb.append('\n');
            }
        }
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d).substring(0, 12);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
