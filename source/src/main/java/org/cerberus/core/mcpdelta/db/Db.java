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
package org.cerberus.core.mcpdelta.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Direct access to the Cerberus database. This is what lets MCP Delta work in "native mode": every change
 * runs in one real transaction, so a plan is the real statements rolled back, and an apply is all or
 * nothing — which the Cerberus DAOs, one connection per call and no transaction, cannot offer.
 *
 * <p>Every value is read as a string: it round-trips exactly through the journal and back into MySQL,
 * timestamps included, and comparisons never trip over Integer vs Long vs Boolean.</p>
 */
public final class Db {

    /** A row, keyed by column name, case-insensitively. */
    public static final class Row extends TreeMap<String, String> {
        public Row() {
            super(String.CASE_INSENSITIVE_ORDER);
        }

        public Row(Map<String, String> source) {
            this();
            putAll(source);
        }

        public String s(String column) {
            String v = get(column);
            return v == null ? "" : v;
        }

        public int i(String column) {
            String v = get(column);
            if (v == null || v.isBlank()) {
                return 0;
            }
            try {
                return Integer.parseInt(v.trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        public long l(String column) {
            String v = get(column);
            try {
                return v == null || v.isBlank() ? 0L : Long.parseLong(v.trim());
            } catch (NumberFormatException e) {
                return 0L;
            }
        }

        public Row copy() {
            return new Row(this);
        }
    }

    @FunctionalInterface
    public interface Work<T> {
        T run(Connection c) throws SQLException;
    }

    private final String url;
    private final String user;
    private final String password;
    private final javax.sql.DataSource dataSource;

    public Db(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.dataSource = null;
    }

    /** Inside the Cerberus webapp: its own connection pool, no credentials of its own. */
    public Db(javax.sql.DataSource dataSource) {
        this.url = null;
        this.user = null;
        this.password = null;
        this.dataSource = dataSource;
    }

    public Connection open() throws SQLException {
        return dataSource != null ? dataSource.getConnection() : DriverManager.getConnection(url, user, password);
    }

    /** Runs read-only work on a fresh connection. */
    public <T> T read(Work<T> work) {
        try (Connection c = open()) {
            return work.run(c);
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    /**
     * Runs work in one transaction. When {@code commit} is false the transaction is rolled back after the
     * work: that is how a plan runs the real statements without keeping any of them.
     */
    public <T> T tx(boolean commit, Work<T> work) {
        try (Connection c = open()) {
            boolean autoCommit = c.getAutoCommit();
            int isolation = c.getTransactionIsolation();
            c.setAutoCommit(false);
            c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            try {
                T result = work.run(c);
                if (commit) {
                    c.commit();
                } else {
                    c.rollback();
                }
                return result;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            } finally {
                // A pooled connection goes back exactly as it was lent.
                c.setTransactionIsolation(isolation);
                c.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    public static List<Row> query(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int n = md.getColumnCount();
                List<Row> rows = new ArrayList<>();
                while (rs.next()) {
                    Row row = new Row();
                    for (int i = 1; i <= n; i++) {
                        row.put(md.getColumnLabel(i), rs.getString(i));
                    }
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    public static Row one(Connection c, String sql, Object... params) throws SQLException {
        List<Row> rows = query(c, sql, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public static int update(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        }
    }

    private static void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    /** Unchecked wrapper, so database failures surface as a clean tool error. */
    public static final class DbException extends RuntimeException {
        public DbException(SQLException cause) {
            super(cause.getMessage(), cause);
        }
    }
}
