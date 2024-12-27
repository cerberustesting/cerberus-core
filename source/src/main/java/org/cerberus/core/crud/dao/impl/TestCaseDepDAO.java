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
import org.cerberus.core.crud.dao.ITestCaseDepDAO;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseDep;
import org.cerberus.core.crud.utils.RequestDbUtils;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.exception.CerberusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class TestCaseDepDAO implements ITestCaseDepDAO {

    @Autowired
    private DatabaseSpring databaseSpring;

    private static final Logger LOG = LogManager.getLogger(TestCaseDepDAO.class);

    @Override
    public TestCaseDep readByKey(String test, String testcase, String dependencyTest, String dependencyTestcase) throws CerberusException {
        String query = "SELECT * FROM `testcasedep` tcd where tcd.Test = ? and tcd.Testcase = ? and tcd.DependencyTest = ? and tcd.DependencyTestcase = ?";

        return RequestDbUtils.executeQuery(databaseSpring, query,
                preparedStatement -> {
                    int i = 1;
                    preparedStatement.setString(i++, test);
                    preparedStatement.setString(i++, testcase);
                    preparedStatement.setString(i++, dependencyTest);
                    preparedStatement.setString(i++, dependencyTestcase);
                },
                resultSet -> this.loadResult(resultSet)
        );

    }

    @Override
    public List<TestCaseDep> readByTestAndTestCase(String test, String testcase) throws CerberusException {
        String query = "SELECT tcd.*, tc.description as TestcaseDescription FROM `testcasedep` tcd "
                + "inner join testcase tc on tcd.DependencyTest = tc.Test and tcd.DependencyTestcase = tc.Testcase "
                + "where tcd.Test = ? and tcd.Testcase = ?";

        return RequestDbUtils.executeQueryList(databaseSpring, query,
                preparedStatement -> {
                    int i = 1;
                    preparedStatement.setString(i++, test);
                    preparedStatement.setString(i++, testcase);
                },
                resultSet -> {
                    TestCaseDep testcaseDependency = this.loadResult(resultSet);
                    testcaseDependency.setTestcaseDescription(resultSet.getString("TestcaseDescription"));
                    return testcaseDependency;
                }
        );
    }

    @Override
    public List<TestCaseDep> readByTestAndTestCase(List<TestCase> testcases) throws CerberusException {
        String query = "SELECT tcd.*, tc.description as TestcaseDescription FROM `testcasedep` tcd "
                + "inner join testcase tc on tcd.DependencyTest = tc.Test and tcd.DependencyTestcase = tc.Testcase "
                + "where 1=1"
                + testcases.stream().map(s -> " and tcd.Test = ? and tcd.Testcase = ?").collect(Collectors.joining());

        return RequestDbUtils.executeQueryList(databaseSpring, query,
                preparedStatement -> {
                    int i = 1;
                    for (TestCase testcase : testcases) {
                        preparedStatement.setString(i++, testcase.getTest());
                        preparedStatement.setString(i++, testcase.getTestcase());
                    }
                },
                resultSet -> {
                    TestCaseDep testcaseDependency = this.loadResult(resultSet);
                    testcaseDependency.setTestcaseDescription(resultSet.getString("TestcaseDescription"));
                    return testcaseDependency;
                }
        );
    }

    @Override
    public void create(TestCaseDep testcaseDependency) throws CerberusException {
        String query = "INSERT INTO `testcasedep`"
                + "(`Test`, `Testcase`, `Type`, `DependencyTest`, `DependencyTestcase`, `DependencyTCDelay`, `DependencyEvent`, `isActive`, `Description`, `UsrCreated`, `DateCreated`, `UsrModif`, `DateModif` )"
                + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        RequestDbUtils.executeUpdate(databaseSpring, query,
                preparedStatement -> this.setRequestData(preparedStatement, testcaseDependency, false)
        );

    }

    @Override
    public void update(TestCaseDep testcaseDependency) throws CerberusException {
        String query = "UPDATE `testcasedep` "
                + " SET `Test` = ?, `Testcase` = ?, `Type` = ?, `DependencyTest` = ?, `DependencyTestcase` = ?, `DependencyTCDelay` = ?, `DependencyEvent` = ?, `isActive` = ?, `Description` = ?, "
                + "`UsrCreated` = ?, `DateCreated` = ?, `UsrModif` = ?, `DateModif` = ? where id=?";

        RequestDbUtils.executeUpdate(databaseSpring, query,
                preparedStatement -> this.setRequestData(preparedStatement, testcaseDependency, true)
        );

    }

    @Override
    public void delete(TestCaseDep testcaseDependency) throws CerberusException {
        String query = "DELETE FROM `testcasedep` where id=?";

        RequestDbUtils.executeUpdate(databaseSpring, query,
                preparedStatement -> {
                    int i = 1;
                    preparedStatement.setLong(i++, testcaseDependency.getId());
                }
        );
    }

    private void setRequestData(PreparedStatement preparedStatement, TestCaseDep testcaseDependency, boolean setId) throws SQLException {
        int i = 1;
        preparedStatement.setString(i++, testcaseDependency.getTest());
        preparedStatement.setString(i++, testcaseDependency.getTestcase());
        preparedStatement.setString(i++, testcaseDependency.getType());
        preparedStatement.setString(i++, testcaseDependency.getDependencyTest());
        preparedStatement.setString(i++, testcaseDependency.getDependencyTestcase());
        preparedStatement.setInt(i++, testcaseDependency.getDependencyTCDelay());
        preparedStatement.setString(i++, testcaseDependency.getDependencyEvent());
        preparedStatement.setBoolean(i++, testcaseDependency.isActive());
        preparedStatement.setString(i++, testcaseDependency.getDescription());
        preparedStatement.setString(i++, testcaseDependency.getUsrCreated());
        preparedStatement.setTimestamp(i++, testcaseDependency.getDateCreated());
        preparedStatement.setString(i++, testcaseDependency.getUsrModif());
        preparedStatement.setTimestamp(i++, testcaseDependency.getDateModif());
        if (setId) {
            preparedStatement.setLong(i++, testcaseDependency.getId());
        }
    }

    private TestCaseDep loadResult(ResultSet resultSet) throws SQLException {

        return TestCaseDep.builder()
                .id(resultSet.getLong("id"))
                .test(resultSet.getString("Test")).testcase(resultSet.getString("Testcase"))
                .type(resultSet.getString("Type"))
                .dependencyTest(resultSet.getString("DependencyTest")).dependencyTestcase(resultSet.getString("DependencyTestCase"))
                .dependencyEvent(resultSet.getString("DependencyEvent")).dependencyTCDelay(resultSet.getInt("DependencyTCDelay"))
                .isActive(resultSet.getBoolean("isActive")).description(resultSet.getString("Description"))
                .usrCreated(resultSet.getString("UsrCreated"))
                .usrModif(resultSet.getString("UsrModif")).dateCreated(resultSet.getTimestamp("DateCreated")).dateModif(resultSet.getTimestamp("DateModif"))
                .build();
    }
}
