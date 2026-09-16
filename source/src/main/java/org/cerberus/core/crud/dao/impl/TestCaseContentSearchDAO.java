/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.crud.dao.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.ITestCaseContentSearchDAO;
import org.cerberus.core.crud.entity.TestCaseContentMatch;
import org.cerberus.core.database.DatabaseSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Searches the text of testcases with one {@code LIKE} per kind of row.
 *
 * <p>Four statements rather than one union: each level carries different coordinates, and padding
 * them into a common shape would make the query unreadable for no gain — they run against different
 * tables and each one is cheap.</p>
 *
 * <p>Every statement joins {@code testcase} to {@code application} to reach the system. The system
 * is not a column of any of these tables, and searching without it would cross the boundary the
 * rest of the tool layer maintains.</p>
 *
 * <p>The searched table is aliased {@code src} rather than the more obvious {@code row}, which is a
 * reserved word in MySQL 8 and would fail to parse.</p>
 */
@Repository
public class TestCaseContentSearchDAO implements ITestCaseContentSearchDAO {

    private static final Logger LOG = LogManager.getLogger(TestCaseContentSearchDAO.class);

    /** The action columns the engine decodes or displays, in the order a caller reads them. */
    private static final List<String> ACTION_FIELDS = List.of(
            "value1", "value2", "value3", "conditionValue1", "conditionValue2", "conditionValue3", "description");

    /** The control columns, identical in meaning to the action ones. */
    private static final List<String> CONTROL_FIELDS = ACTION_FIELDS;

    private static final List<String> STEP_FIELDS = List.of(
            "description", "conditionValue1", "conditionValue2", "conditionValue3");

    private static final List<String> PROPERTY_FIELDS = List.of("value1", "value2", "value3", "description");

    @Autowired
    private DatabaseSpring databaseSpring;

    @Override
    public List<TestCaseContentMatch> search(String search, List<String> systems, String testFolder, String testcase,
                                             List<TestCaseContentMatch.Scope> scopes, int maxResults) {
        List<TestCaseContentMatch> matches = new ArrayList<>();
        if (search == null || search.isEmpty()) {
            return matches;
        }

        for (TestCaseContentMatch.Scope scope : scopes) {
            if (matches.size() >= maxResults) {
                break;
            }
            matches.addAll(searchScope(scope, search, systems, testFolder, testcase, maxResults - matches.size()));
        }
        // The statement limits rows, and one row can match in several of its fields, so the row limit
        // is not the match limit. Trimming here is what makes the cap the caller asked for the cap
        // they get — and what makes "the result was cut at N" true rather than approximate.
        return matches.size() > maxResults ? new ArrayList<>(matches.subList(0, maxResults)) : matches;
    }

    /**
     * Runs the statement for one scope.
     */
    private List<TestCaseContentMatch> searchScope(TestCaseContentMatch.Scope scope, String search,
                                                   List<String> systems, String testFolder, String testcase,
                                                   int remaining) {
        List<TestCaseContentMatch> matches = new ArrayList<>();

        String table = switch (scope) {
            case ACTION -> "testcasestepaction";
            case CONTROL -> "testcasestepactioncontrol";
            case STEP -> "testcasestep";
            case PROPERTY -> "testcasecountryproperties";
        };
        List<String> fields = switch (scope) {
            case ACTION -> ACTION_FIELDS;
            case CONTROL -> CONTROL_FIELDS;
            case STEP -> STEP_FIELDS;
            case PROPERTY -> PROPERTY_FIELDS;
        };

        StringBuilder query = new StringBuilder();
        query.append("SELECT app.System AS matchedSystem, src.* FROM ").append(table).append(" src ");
        query.append("INNER JOIN testcase tc ON tc.Test = src.Test AND tc.TestCase = src.TestCase ");
        // LEFT JOIN so a testcase whose application was removed still appears: it is exactly the
        // kind of testcase nobody maintains any more, and hiding it from a sweep is how a stale
        // copy of a broken selector survives one.
        query.append("LEFT JOIN application app ON app.Application = tc.Application ");
        query.append("WHERE (");
        for (int i = 0; i < fields.size(); i++) {
            query.append(i == 0 ? "" : " OR ").append("src.`").append(fields.get(i)).append("` LIKE ?");
        }
        query.append(")");
        if (!systems.isEmpty()) {
            query.append(" AND app.System IN (");
            query.append("?,".repeat(systems.size() - 1)).append("?)");
        }
        if (testFolder != null && !testFolder.isEmpty()) {
            query.append(" AND src.Test = ?");
        }
        if (testcase != null && !testcase.isEmpty()) {
            query.append(" AND src.TestCase = ?");
        }
        query.append(" ORDER BY src.Test, src.TestCase");
        query.append(scope == TestCaseContentMatch.Scope.PROPERTY ? ", src.Property" : ", src.Sort");
        query.append(" LIMIT ?");

        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString())) {

            int index = 1;
            String pattern = "%" + escapeForLike(search) + "%";
            for (int i = 0; i < fields.size(); i++) {
                preStat.setString(index++, pattern);
            }
            for (String system : systems) {
                preStat.setString(index++, system);
            }
            if (testFolder != null && !testFolder.isEmpty()) {
                preStat.setString(index++, testFolder);
            }
            if (testcase != null && !testcase.isEmpty()) {
                preStat.setString(index++, testcase);
            }
            preStat.setInt(index, remaining);

            try (ResultSet resultSet = preStat.executeQuery()) {
                while (resultSet.next()) {
                    matches.addAll(rowToMatches(scope, resultSet, fields, search));
                }
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to search {} content : {}", scope, exception.toString());
        }

        return matches;
    }

    /**
     * Turns one matching row into one entry per field that actually contains the string.
     *
     * <p>The statement matches a row as soon as any of its fields does; reporting the row without
     * saying which field matched would leave the caller to search it again by eye, and a selector
     * copied into both {@code value1} and a condition is precisely the case worth seeing twice.</p>
     */
    private List<TestCaseContentMatch> rowToMatches(TestCaseContentMatch.Scope scope, ResultSet resultSet,
                                                    List<String> fields, String search) throws SQLException {
        List<TestCaseContentMatch> matches = new ArrayList<>();

        String system = orEmpty(resultSet.getString("matchedSystem"));
        String test = orEmpty(resultSet.getString("Test"));
        String testcase = orEmpty(resultSet.getString("TestCase"));

        int stepId = scope == TestCaseContentMatch.Scope.PROPERTY ? 0 : resultSet.getInt("StepId");
        int actionId = scope == TestCaseContentMatch.Scope.ACTION || scope == TestCaseContentMatch.Scope.CONTROL
                ? resultSet.getInt("actionId") : 0;
        int controlId = scope == TestCaseContentMatch.Scope.CONTROL ? resultSet.getInt("controlId") : 0;
        String country = scope == TestCaseContentMatch.Scope.PROPERTY ? orEmpty(resultSet.getString("Country")) : "";
        String name = switch (scope) {
            case ACTION -> orEmpty(resultSet.getString("Action"));
            case CONTROL -> orEmpty(resultSet.getString("Control"));
            case PROPERTY -> orEmpty(resultSet.getString("Property"));
            case STEP -> "";
        };

        for (String field : fields) {
            String value = orEmpty(resultSet.getString(field));
            if (!value.toLowerCase().contains(search.toLowerCase())) {
                continue;
            }
            matches.add(new TestCaseContentMatch(scope, system, test, testcase, stepId, actionId, controlId,
                    country, name, field, value));
        }

        return matches;
    }

    /**
     * Neutralises the wildcards of {@code LIKE} so a search for a literal string stays literal.
     *
     * <p>Without this, a search for {@code %property.LOGIN%} — the exact shape of what a caller
     * looks for here — would match on any two characters, because both percent signs are wildcards.
     * The backslash is the default escape character in MySQL, so escaping it first matters.</p>
     */
    private String escapeForLike(String search) {
        return search.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
