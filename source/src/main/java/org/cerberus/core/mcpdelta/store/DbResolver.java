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

import org.cerberus.core.mcpdelta.catalog.Catalog;
import org.cerberus.core.mcpdelta.db.Db;
import org.cerberus.core.mcpdelta.db.Db.Row;
import org.cerberus.core.mcpdelta.write.RowBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Resolves step positions, library steps and labels on one connection, with a per-request cache. */
public final class DbResolver implements RowBuilder.Resolver {

    private final Connection c;
    private final Catalog catalog;
    private final Map<String, List<Row>> stepsCache = new HashMap<>();

    public DbResolver(Connection c, Catalog catalog) {
        this.c = c;
        this.catalog = catalog;
    }

    private List<Row> steps(String test, String testcase) {
        return stepsCache.computeIfAbsent(test + "\u0001" + testcase, k -> {
            try {
                return Db.query(c, "SELECT StepId, Sort, IsLibraryStep FROM testcasestep WHERE Test=? AND Testcase=? ORDER BY Sort, StepId", test, testcase);
            } catch (SQLException e) {
                throw new Db.DbException(e);
            }
        });
    }

    /** Steps change inside a write; positions must be read again afterwards. */
    public void forget(String test, String testcase) {
        stepsCache.remove(test + "\u0001" + testcase);
    }

    @Override
    public Integer stepPosition(String test, String testcase, int stepId) {
        List<Row> rows = steps(test, testcase);
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).i("StepId") == stepId) {
                return i + 1;
            }
        }
        return null;
    }

    @Override
    public Integer stepIdAt(String test, String testcase, int position) {
        List<Row> rows = steps(test, testcase);
        return position >= 1 && position <= rows.size() ? rows.get(position - 1).i("StepId") : null;
    }

    @Override
    public Boolean isLibraryStep(String test, String testcase, int stepId) {
        for (Row r : steps(test, testcase)) {
            if (r.i("StepId") == stepId) {
                return r.i("IsLibraryStep") == 1;
            }
        }
        return null;
    }

    @Override
    public String labelName(String labelId) {
        return org.cerberus.core.mcpdelta.entity.ObjService.labels(c).name(labelId);
    }

    /** Read on the write's own connection, so a label created in the same write is found. */
    @Override
    public String labelId(String name, String system) {
        return org.cerberus.core.mcpdelta.entity.ObjService.labels(c).id(name, system);
    }

    @Override
    public String applicationSystem(String application) {
        Catalog.Application a = catalog.applications().get(application);
        return a == null ? "" : a.system();
    }

    @Override
    public List<String> countryOrder() {
        return catalog.countryOrder();
    }
}
