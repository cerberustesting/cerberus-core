/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.crud.dao.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.dao.ITestCaseDepDAO;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseDep;
import org.cerberus.crud.utils.RequestDbUtils;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.exception.CerberusException;
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
    public TestCaseDep readByKey(String test, String testcase, String testDep, String testcaseDep) throws CerberusException {
        String query = "SELECT * FROM `testcasedep` tcd where tcd.Test = ? and tcd.TestCase = ? and tcd.DepTest = ? and tcd.DepTestCase = ?";

        return RequestDbUtils.executeQuery(databaseSpring, query,
                ps -> {
                    int idx = 1;
                    ps.setString(idx++, test);
                    ps.setString(idx++, testcase);
                    ps.setString(idx++, testDep);
                    ps.setString(idx++, testcaseDep);
                },
                rs -> this.loadResult(rs)
        );

    }

    @Override
    public List<TestCaseDep> readByTestAndTestCase(String test, String testcase) throws CerberusException {
        String query = "SELECT tcd.*, tc.description as depDescription FROM `testcasedep` tcd " +
                "inner join testcase tc on tcd.DepTest = tc.Test and tcd.DepTestCase = tc.TestCase " +
                "where tcd.Test = ? and tcd.TestCase = ?";

            return RequestDbUtils.executeQueryList(databaseSpring, query,
                    ps -> {
                        int idx = 1;
                        ps.setString(idx++, test);
                        ps.setString(idx++, testcase);
                    },
                    rs -> {
                        TestCaseDep dep = this.loadResult(rs);
                        dep.setDepDescription(rs.getString("depDescription"));
                        return dep;
                    }
            );
    }

    @Override
    public List<TestCaseDep> readByTestAndTestCase(List<TestCase> testCaseList) throws CerberusException {
        String query = "SELECT tcd.*, tc.description as depDescription FROM `testcasedep` tcd " +
                "inner join testcase tc on tcd.DepTest = tc.Test and tcd.DepTestCase = tc.TestCase " +
                "where 1=1" +
                testCaseList.stream().map(s -> " and tcd.Test = ? and tcd.TestCase = ?").collect(Collectors.joining());

        return RequestDbUtils.executeQueryList(databaseSpring, query,
                ps -> {
                    int idx = 1;
                    for(TestCase tc : testCaseList) {
                        ps.setString(idx++, tc.getTest());
                        ps.setString(idx++, tc.getTestCase());
                    }
                },
                rs -> {
                    TestCaseDep dep = this.loadResult(rs);
                    dep.setDepDescription(rs.getString("depDescription"));
                    return dep;
                }
         );
    }


    @Override
    public void create(TestCaseDep testCaseDep) throws CerberusException {
        String query = "INSERT INTO `testcasedep`" +
                "(`Test`, `TestCase`, `Type`, `DepTest`, `DepTestCase`, `DepEvent`, `Active`, `Description`, `UsrCreated`, `DateCreated`, `UsrModif`, `DateModif` )" +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            RequestDbUtils.executeUpdate(databaseSpring, query,
                    ps -> this.setRequestData(ps,testCaseDep,false)
            );


    }


    @Override
    public void update(TestCaseDep testCaseDep) throws CerberusException {
        String query = "UPDATE `testcasedep` " +
                " SET `Test` = ?, `TestCase` = ?, `Type` = ?, `DepTest` = ?, `DepTestCase` = ?, `DepEvent` = ?, `Active` = ?, `Description` = ?, " +
                "`UsrCreated` = ?, `DateCreated` = ?, `UsrModif` = ?, `DateModif` = ? where id=?";

        RequestDbUtils.executeUpdate(databaseSpring, query,
                ps -> this.setRequestData(ps,testCaseDep,true)
        );

    }


    @Override
    public void delete(TestCaseDep testCaseDep) throws CerberusException {
        String query = "DELETE FROM `testcasedep` where id=?";

        RequestDbUtils.executeUpdate(databaseSpring, query,
                ps -> {
                    int idx = 1;
                    ps.setLong(idx++, testCaseDep.getId());
                }
        );
    }

    private void setRequestData(PreparedStatement ps, TestCaseDep testCaseDep, boolean setId) throws SQLException {
        int idx = 1;
        ps.setString(idx++, testCaseDep.getTest());
        ps.setString(idx++, testCaseDep.getTestCase());
        ps.setString(idx++, testCaseDep.getType());
        ps.setString(idx++, testCaseDep.getDepTest());
        ps.setString(idx++, testCaseDep.getDepTestCase());
        ps.setString(idx++, testCaseDep.getDepEvent());
        ps.setString(idx++, testCaseDep.getActive());
        ps.setString(idx++, testCaseDep.getDescription());
        ps.setString(idx++, testCaseDep.getUsrCreated());
        ps.setTimestamp(idx++, testCaseDep.getDateCreated());
        ps.setString(idx++, testCaseDep.getUsrModif());
        ps.setTimestamp(idx++, testCaseDep.getDateModif());
        if(setId)
            ps.setLong(idx++, testCaseDep.getId());
    }

    private TestCaseDep loadResult(ResultSet rs) throws SQLException {
        TestCaseDep testCaseDep = new TestCaseDep();

        testCaseDep.setId(rs.getLong("id"));
        testCaseDep.setTest(rs.getString("Test"));
        testCaseDep.setTestCase(rs.getString("TestCase"));
        testCaseDep.setType(rs.getString("Type"));
        testCaseDep.setDepTest(rs.getString("DepTest"));
        testCaseDep.setDepTestCase(rs.getString("DepTestCase"));
        testCaseDep.setDepEvent(rs.getString("DepEvent"));
        testCaseDep.setActive(rs.getString("Active"));
        testCaseDep.setDescription(rs.getString("Description"));
        testCaseDep.setUsrCreated(rs.getString("UsrCreated"));
        testCaseDep.setDateCreated(rs.getTimestamp("DateCreated"));
        testCaseDep.setUsrModif(rs.getString("UsrModif"));
        testCaseDep.setDateModif(rs.getTimestamp("DateModif"));

        return testCaseDep;
    }
}
