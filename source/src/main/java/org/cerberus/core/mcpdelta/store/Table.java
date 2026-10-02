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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** The tables that make up one testcase, with the key that identifies a row in each. */
public enum Table {
    TESTCASE("testcase", List.of("Test", "Testcase")),
    COUNTRY("testcasecountry", List.of("Test", "Testcase", "Country")),
    PROPERTY("testcasecountryproperties", List.of("Test", "Testcase", "Country", "Property")),
    STEP("testcasestep", List.of("Test", "Testcase", "StepId")),
    ACTION("testcasestepaction", List.of("Test", "Testcase", "StepId", "ActionId")),
    CONTROL("testcasestepactioncontrol", List.of("Test", "Testcase", "StepId", "ActionId", "ControlId")),
    LABEL("testcaselabel", List.of("Test", "Testcase", "LabelId")),
    /** Read-only for MCP Delta: kept in snapshots so undoing a testcase deletion restores it too. */
    DEPENDENCY("testcasedep", List.of("ID"));

    /** Audit columns: never compared, set by the writer. */
    public static final Set<String> AUDIT = Set.of("usrcreated", "datecreated", "usrmodif", "datemodif");
    /** Columns of testcase that change on their own (execution stamp, version counter). */
    public static final Set<String> TESTCASE_VOLATILE = Set.of("version", "datelastexecuted");

    public final String sql;
    public final List<String> key;

    Table(String sql, List<String> key) {
        this.sql = sql;
        this.key = key;
    }

    public String keyOf(Row row) {
        return key.stream().map(row::s).collect(Collectors.joining("\u0001"));
    }

    public boolean compared(String column) {
        String c = column.toLowerCase();
        if (AUDIT.contains(c)) {
            return false;
        }
        if (this == TESTCASE && TESTCASE_VOLATILE.contains(c)) {
            return false;
        }
        return !(this == LABEL && c.equals("id"));
    }
}
