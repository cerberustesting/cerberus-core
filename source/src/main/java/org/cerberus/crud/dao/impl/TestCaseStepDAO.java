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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.crud.dao.ITestCaseStepDAO;
import org.cerberus.crud.entity.Test;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryTestCaseStep;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.cerberus.crud.factory.IFactoryTestCase;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.StringUtil;
import org.json.JSONArray;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 29/12/2012
 * @since 2.0.0
 */
@Repository
public class TestCaseStepDAO implements ITestCaseStepDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseStep factoryTestCaseStep;
    @Autowired
    private IFactoryTestCase factoryTestCase;

    private static final Logger LOG = LogManager.getLogger(TestCaseStepDAO.class);

    private final String OBJECT_NAME = "TestCaseStep";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public List<TestCaseStep> findTestCaseStepByTestCase(String test, String testcase) {
        List<TestCaseStep> list = null;
        final String query = "SELECT * FROM testcasestep WHERE test = ? AND testcase = ? ORDER BY sort";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.test : " + test);
            LOG.debug("SQL.param.testcase : " + testcase);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestCaseStep>();
                try {
                    while (resultSet.next()) {
                        list.add(loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception Closing the connection : " + e.toString());
            }
        }
        return list;
    }

    @Override
    public TestCaseStep findTestCaseStep(String test, String testcase, Integer stepId) {
        TestCaseStep result = null;
        final String query = "SELECT * FROM testcasestep WHERE test = ? AND testcase = ? AND stepId = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                preStat.setInt(3, stepId);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = loadFromResultSet(resultSet);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception Closing the connection : " + e.toString());
            }
        }
        return result;
    }

    @Override
    public void deleteTestCaseStep(TestCaseStep tcs) throws CerberusException {
        boolean throwExcep = false;
        final String query = "DELETE FROM testcasestep WHERE test = ? and testcase = ? and stepId = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, tcs.getTest());
                preStat.setString(2, tcs.getTestcase());
                preStat.setInt(3, tcs.getStepId());

                throwExcep = preStat.executeUpdate() == 0;
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception Closing the connection : " + e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void updateTestCaseStep(TestCaseStep tcs) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("UPDATE testcasestep SET ");
        query.append(" `Description` = ?, `isUsingLibraryStep`=? ");
        if (!StringUtil.isNullOrEmpty(tcs.getLibraryStepTest())) {
            query.append(",`libraryStepTest`=? ");
        }
        if (!StringUtil.isNullOrEmpty(tcs.getLibraryStepTestcase())) {
            query.append(",`libraryStepTestcase`=? ");
        }
        if (tcs.getLibraryStepStepId() >= 0) {
            query.append(",`libraryStepStepId`=? ");
        }
        query.append(",`isLibraryStep` = ?, `Sort` = ?, `loop` = ?, `conditionOperator` = ?, `conditionOptions` = ?, `conditionValue1` = ?, `conditionValue2` = ?, `conditionValue3` = ?, `isExecutionForced` = ?, DateModif = CURRENT_TIMESTAMP, UsrModif = ? WHERE Test = ? AND testcase = ? AND stepId = ?");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                preStat.setString(i++, tcs.getDescription());
                preStat.setBoolean(i++, tcs.isUsingLibraryStep());
                if (!StringUtil.isNullOrEmpty(tcs.getLibraryStepTest())) {
                    preStat.setString(i++, tcs.getLibraryStepTest());
                }
                if (!StringUtil.isNullOrEmpty(tcs.getLibraryStepTestcase())) {
                    preStat.setString(i++, tcs.getLibraryStepTestcase());
                }
                if (tcs.getLibraryStepStepId() >= 0) {
                    preStat.setInt(i++, tcs.getLibraryStepStepId());
                }
                preStat.setBoolean(i++, tcs.isLibraryStep());
                preStat.setInt(i++, tcs.getSort());
                preStat.setString(i++, tcs.getLoop() == null ? "" : tcs.getLoop());
                preStat.setString(i++, tcs.getConditionOperator() == null ? "" : tcs.getConditionOperator());
                preStat.setString(i++, tcs.getConditionOptions() == null ? "[]" : tcs.getConditionOptions().toString());
                preStat.setString(i++, tcs.getConditionValue1() == null ? "" : tcs.getConditionValue1());
                preStat.setString(i++, tcs.getConditionValue2() == null ? "" : tcs.getConditionValue2());
                preStat.setString(i++, tcs.getConditionValue3() == null ? "" : tcs.getConditionValue3());
                preStat.setBoolean(i++, tcs.isExecutionForced());
                preStat.setString(i++, tcs.getUsrModif() == null ? "" : tcs.getUsrModif());

                preStat.setString(i++, tcs.getTest());
                preStat.setString(i++, tcs.getTestcase());
                preStat.setInt(i++, tcs.getStepId());

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception Closing the connection : " + e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public List<TestCaseStep> getTestCaseStepUsingStepInParamter(String test, String testcase, int stepId) throws CerberusException {
        List<TestCaseStep> list = new ArrayList<TestCaseStep>();
        final String query = "SELECT * FROM testcasestep WHERE isUsingLibraryStep IS true AND libraryStepTest = ? AND libraryStepTestcase = ? AND libraryStepStepId = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                preStat.setInt(3, stepId);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        list.add(loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception Closing the connection : " + e.toString());
            }
        }
        return list;
    }

    @Override
    public List<TestCaseStep> getTestCaseStepUsingTestCaseInParamter(String test, String testcase) throws CerberusException {
        List<TestCaseStep> list = null;
        final String query = "SELECT * FROM testcasestep WHERE isUsingLibraryStep IS true AND libraryStepTest = ? AND libraryStepTestcase = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestCaseStep>();
                try {
                    while (resultSet.next()) {
                        list.add(loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception Closing the connection : " + e.toString());
            }
        }
        return list;
    }

    @Override
    public List<TestCaseStep> getTestCaseStepsUsingTestInParameter(final String test) throws CerberusException {
        try (
                final Connection connection = databaseSpring.connect();
                final PreparedStatement statement = connection.prepareStatement("SELECT * FROM testcasestep WHERE isUsingLibraryStep IS true AND libraryStepTest = ?")) {
            statement.setString(1, test);

            try (ResultSet resultSet = statement.executeQuery();) {
                final List<TestCaseStep> steps = new ArrayList<>();
                while (resultSet.next()) {
                    steps.add(loadFromResultSet(resultSet));
                }
                return steps;
            } catch (SQLException e) {
                LOG.error(e.getMessage(), e);
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
        }
    }

    @Override
    public List<TestCaseStep> getStepUsedAsLibraryInOtherTestCaseByApplication(String application) throws CerberusException {
        List<TestCaseStep> list = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT tcs.libraryStepTest, tcs.libraryStepTestcase,tcs.libraryStepStepId,tcs.sort, tcs2.description FROM testcasestep tcs ");
        query.append("join testcase tc on tc.test=tcs.test and tc.testcase=tcs.testcase ");
        query.append("join testcasestep tcs2 on tcs.test=tcs2.test and tcs.testcase=tcs2.testcase and tcs.stepId=tcs2.stepId ");
        query.append("where tcs.isUsingLibraryStep IS true and tc.application = ?  ");
        query.append("group by tcs.libraryStepTest, tcs.libraryStepTestcase, tcs.libraryStepStepId ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, application);

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestCaseStep>();
                try {
                    while (resultSet.next()) {
                        String t = resultSet.getString("libraryStepTest");
                        String tc = resultSet.getString("libraryStepTestcase");
                        int s = resultSet.getInt("libraryStepStepId");
                        int sort = resultSet.getInt("sort");
                        String description = resultSet.getString("description");
                        list.add(factoryTestCaseStep.create(t, tc, s, sort, null, null, null, null, null, null, description, false, null, null, 0, false, false, null, null, null, null));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception Closing the connection : " + e.toString());
            }
        }
        return list;

    }

    @Override
    public List<TestCaseStep> getStepLibraryBySystem(String system) throws CerberusException {
        List<TestCaseStep> list = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT tcs.test, tcs.testcase, tcs.stepId, tcs.sort, tcs.description, tc.description as tcdesc FROM testcasestep tcs ");
        query.append("join testcase tc on tc.test=tcs.test and tc.testcase=tcs.testcase ");
        query.append("join application app  on tc.application=app.application ");
        query.append("where tcs.islibrarystep IS true and app.system = ?  ");
        query.append("order by tcs.test, tcs.testcase, tcs.sort");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, system);

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestCaseStep>();
                try {
                    while (resultSet.next()) {
                        String t = resultSet.getString("test");
                        String tc = resultSet.getString("testcase");
                        String tcdesc = resultSet.getString("tcdesc");
                        int s = resultSet.getInt("stepId");
                        int sort = resultSet.getInt("sort");
                        String description = resultSet.getString("description");
                        TestCaseStep tcs = factoryTestCaseStep.create(t, tc, s, sort, null, null, null, null, null, null, description, false, null, null, 0, false, false, null, null, null, null);
                        TestCase tcObj = factoryTestCase.create(t, tc, tcdesc);
                        tcs.setTestcaseObj(tcObj);
                        list.add(tcs);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception Closing the connection : " + e.toString());
            }
        }
        return list;
    }

    @Override
    public List<TestCaseStep> getStepLibraryBySystemTest(String system, String test) throws CerberusException {
        List<TestCaseStep> list = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT tcs.test, tcs.testcase,tcs.stepId, tcs.sort, tcs.description, tc.description as tcdesc, tc.application as tcapp FROM testcasestep tcs ");
        query.append("join testcase tc on tc.test=tcs.test and tc.testcase=tcs.testcase ");
        query.append("join application app  on tc.application=app.application ");
        query.append("where tcs.islibrarystep IS true ");
        if (system != null) {
            query.append("and app.system = ? ");
        }
        if (test != null) {
            query.append("and tcs.test = ? ");
        }
        query.append("order by tcs.test, tcs.testcase, tcs.sort");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param.system : " + system);
            LOG.debug("SQL.param.test : " + test);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                if (system != null) {
                    preStat.setString(i++, system);
                }
                if (test != null) {
                    preStat.setString(i++, test);
                }

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestCaseStep>();
                try {
                    while (resultSet.next()) {
                        String t = resultSet.getString("test");
                        String tc = resultSet.getString("testcase");
                        int s = resultSet.getInt("stepId");
                        int sort = resultSet.getInt("sort");
                        String description = resultSet.getString("description");
                        String tcdesc = resultSet.getString("tcdesc");
                        TestCase tcToAdd = factoryTestCase.create(t, tc, tcdesc);
                        tcToAdd.setApplication(resultSet.getString("tcapp"));
                        TestCaseStep tcsToAdd = factoryTestCaseStep.create(t, tc, s, sort, null, null, null, null, null, null, description, false, null, null, 0, false, false, null, null, null, null);
                        tcsToAdd.setTestcaseObj(tcToAdd);
                        list.add(tcsToAdd);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception Closing the connection : " + e.toString());
            }
        }
        return list;
    }

    @Override
    public List<TestCaseStep> getStepLibraryBySystemTestTestCase(String system, String test, String testcase) throws CerberusException {
        List<TestCaseStep> list = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT tcs.test, tcs.testcase,tcs.stepId, tcs.sort, tcs.description FROM testcasestep tcs ");
        query.append("join testcase tc on tc.test=tcs.test and tc.testcase=tcs.testcase ");
        query.append("join application app  on tc.application=app.application ");
        query.append("where tcs.islibrarystep IS true ");
        if (system != null) {
            query.append("and app.system = ? ");
        }
        if (test != null) {
            query.append("and tcs.test = ? ");
        }
        if (testcase != null) {
            query.append("and tcs.testcase = ? ");
        }
        query.append("order by tcs.test, tcs.testcase, tcs.sort");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param.system : " + system);
            LOG.debug("SQL.param.test : " + test);
            LOG.debug("SQL.param.testcase : " + testcase);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                if (system != null) {
                    preStat.setString(i++, system);
                }
                if (test != null) {
                    preStat.setString(i++, test);
                }
                if (testcase != null) {
                    preStat.setString(i++, testcase);
                }

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<>();
                try {
                    while (resultSet.next()) {
                        String t = resultSet.getString("test");
                        String tc = resultSet.getString("testcase");
                        int s = resultSet.getInt("stepId");
                        int sort = resultSet.getInt("sort");
                        String description = resultSet.getString("description");
                        list.add(factoryTestCaseStep.create(t, tc, s, sort, null, null, null, null, null, null, description, false, null, null, 0, false, false, null, null, null, null));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception Closing the connection : " + e.toString());
            }
        }
        return list;
    }

    @Override
    public AnswerList<TestCaseStep> readByTestTestCase(String test, String testcase) {
        AnswerList<TestCaseStep> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCaseStep> stepList = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT tcs.*, tcs2.sort as libraryStepSort, CASE WHEN tcs1.test + tcs1.testcase + tcs1.stepId is NULL THEN 0 ELSE 1 END as isStepInUseByOtherTestCase "
                + "FROM testcasestep tcs "
                + "LEFT JOIN testcasestep tcs1 "
                + "ON tcs1.isUsingLibraryStep = true AND tcs1.libraryStepTest = ? AND tcs1.libraryStepTestcase = ? AND tcs1.libraryStepStepId = tcs.stepId "
                + "LEFT OUTER JOIN testcasestep tcs2 "
                + "ON tcs2.Test = tcs.libraryStepTest AND tcs2.Testcase = tcs.libraryStepTestcase AND tcs2.stepId = tcs.libraryStepStepId "
                + "WHERE tcs.test = ? AND tcs.testcase = ? "
                + "GROUP BY tcs.test, tcs.testcase, tcs.stepId ORDER BY tcs.sort");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param.test : " + test);
            LOG.debug("SQL.param.testcase : " + testcase);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                preStat.setString(3, test);
                preStat.setString(4, testcase);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        stepList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (stepList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList<>(stepList, stepList.size());
                    } else if (stepList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList<>(stepList, stepList.size());
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList<>(stepList, stepList.size());
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        response.setResultMessage(msg);
        return response;
    }

    @Override
    public AnswerList<TestCaseStep> readByLibraryUsed(String test, String testcase, int stepId) {
        AnswerList<TestCaseStep> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCaseStep> stepList = new ArrayList<TestCaseStep>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcasestep tcs WHERE tcs.isUsingLibraryStep = true AND tcs.libraryStepTest = ? AND tcs.libraryStepTestcase = ? AND tcs.libraryStepStepId = ?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                preStat.setInt(3, stepId);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        stepList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (stepList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList<>(stepList, stepList.size());
                    } else if (stepList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList<>(stepList, stepList.size());
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList<>(stepList, stepList.size());
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        response.setResultMessage(msg);
        return response;
    }

    @Override
    public Answer create(TestCaseStep testCaseStep) {
        Answer ans = new Answer();
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO `testcasestep` (`Test`,`TestCase`,`StepId`,`Sort`,`Description`,`isUsingLibraryStep` ");

        if (!StringUtil.isNullOrEmpty(testCaseStep.getLibraryStepTest())) {
            query.append(",`libraryStepTest` ");
        }
        if (!StringUtil.isNullOrEmpty(testCaseStep.getLibraryStepTestcase())) {
            query.append(",`libraryStepTestcase` ");
        }
        if (testCaseStep.getLibraryStepStepId() >= 0) {
            query.append(",`libraryStepStepId` ");
        }
        query.append(", `isLibraryStep`, `loop`, `conditionOperator`, `conditionOptions`, `conditionValue1`, `conditionValue2`, `conditionValue3`, `isExecutionForced`, `usrCreated`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?");
        if (!StringUtil.isNullOrEmpty(testCaseStep.getLibraryStepTest())) {
            query.append(",?");
        }
        if (!StringUtil.isNullOrEmpty(testCaseStep.getLibraryStepTestcase())) {
            query.append(",?");
        }
        if (testCaseStep.getLibraryStepStepId() >= 0) {
            query.append(",?");
        }
        query.append(",?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param.libraryStepTest : " + testCaseStep.getLibraryStepTest());
            LOG.debug("SQL.param.libraryStepTestcase : " + testCaseStep.getLibraryStepTestcase());
            LOG.debug("SQL.param.libraryStepStepId : " + testCaseStep.getLibraryStepStepId());
        }

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString())) {
            // Prepare and execute query
            int i = 1;
            preStat.setString(i++, testCaseStep.getTest());
            preStat.setString(i++, testCaseStep.getTestcase());
            preStat.setInt(i++, testCaseStep.getStepId());
            preStat.setInt(i++, testCaseStep.getSort());
            preStat.setString(i++, testCaseStep.getDescription());
            preStat.setBoolean(i++, testCaseStep.isUsingLibraryStep());
            if (!StringUtil.isNullOrEmpty(testCaseStep.getLibraryStepTest())) {
                preStat.setString(i++, testCaseStep.getLibraryStepTest());
            }
            if (!StringUtil.isNullOrEmpty(testCaseStep.getLibraryStepTestcase())) {
                preStat.setString(i++, testCaseStep.getLibraryStepTestcase());
            }
            if (testCaseStep.getLibraryStepStepId() >= 0) {
                preStat.setInt(i++, testCaseStep.getLibraryStepStepId());
            }
            preStat.setBoolean(i++, testCaseStep.isLibraryStep());
            preStat.setString(i++, testCaseStep.getLoop() == null ? "" : testCaseStep.getLoop());
            preStat.setString(i++, testCaseStep.getConditionOperator() == null ? "" : testCaseStep.getConditionOperator());
            preStat.setString(i++, testCaseStep.getConditionOptions() == null ? "[]" : testCaseStep.getConditionOptions().toString());
            preStat.setString(i++, testCaseStep.getConditionValue1() == null ? "" : testCaseStep.getConditionValue1());
            preStat.setString(i++, testCaseStep.getConditionValue2() == null ? "" : testCaseStep.getConditionValue2());
            preStat.setString(i++, testCaseStep.getConditionValue3() == null ? "" : testCaseStep.getConditionValue3());
            preStat.setBoolean(i++, testCaseStep.isExecutionForced());
            preStat.setString(i++, testCaseStep.getUsrCreated() == null ? "" : testCaseStep.getUsrCreated());
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "CREATE");
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        } catch (Exception e) {
            LOG.warn("Unable to create TestCaseStep: " + e.getMessage(), e);
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    private TestCaseStep loadFromResultSet(ResultSet resultSet) throws SQLException {
        if (resultSet == null) {
            return null;
        }

        String test = resultSet.getString("test") == null ? "" : resultSet.getString("test");
        String testcase = resultSet.getString("testcase") == null ? "" : resultSet.getString("testcase");
        int stepId = resultSet.getInt("stepId") == 0 ? 0 : resultSet.getInt("stepId");
        int sort = resultSet.getInt("sort");
        String loop = resultSet.getString("loop") == null ? "" : resultSet.getString("loop");
        String conditionOperator = resultSet.getString("conditionOperator") == null ? "" : resultSet.getString("conditionOperator");
        String conditionValue1 = resultSet.getString("conditionValue1") == null ? "" : resultSet.getString("conditionValue1");
        String conditionValue2 = resultSet.getString("conditionValue2") == null ? "" : resultSet.getString("conditionValue2");
        String conditionValue3 = resultSet.getString("conditionValue3") == null ? "" : resultSet.getString("conditionValue3");
        JSONArray conditionOptions = SqlUtil.getJSONArrayFromColumn(resultSet, "conditionOptions");
        String description = resultSet.getString("description") == null ? "" : resultSet.getString("description");
        boolean isUsingLibraryStep = resultSet.getBoolean("isUsingLibraryStep");
        String libraryStepTest = resultSet.getString("libraryStepTest") == null ? "" : resultSet.getString("libraryStepTest");
        String libraryStepTestcase = resultSet.getString("libraryStepTestcase") == null ? "" : resultSet.getString("libraryStepTestcase");
        int libraryStepStepId = resultSet.getInt("libraryStepStepId") == 0 ? 0 : resultSet.getInt("libraryStepStepId");
        boolean isLibraryStep = resultSet.getBoolean("isLibraryStep");
        boolean isExecutionForced = resultSet.getBoolean("isExecutionForced");
        String usrCreated = resultSet.getString("UsrCreated");
        Timestamp dateCreated = resultSet.getTimestamp("DateCreated");
        String usrModif = resultSet.getString("UsrModif");
        Timestamp dateModif = resultSet.getTimestamp("DateModif");

        TestCaseStep tcs = factoryTestCaseStep.create(test, testcase, stepId, sort, loop, conditionOperator, conditionValue1, conditionValue2, conditionValue3, conditionOptions, description, isUsingLibraryStep, libraryStepTest, libraryStepTestcase, libraryStepStepId,
                isLibraryStep, isExecutionForced, usrCreated, dateCreated, usrModif, dateModif);

        try {
//            resultSet.findColumn("isStepInUseByOtherTestCase");
            boolean isStepInUseByOtherTestCase = resultSet.getInt("isStepInUseByOtherTestCase") == 1 ? true : false;
            tcs.setIsStepInUseByOtherTestcase(isStepInUseByOtherTestCase);
        } catch (SQLException sqlex) {
            // That means there is not this column, so nothing to do
        }

        try {
//            resultSet.findColumn("isStepInUseByOtherTestCase");
            int libraryStepSort = resultSet.getInt("libraryStepSort");
            tcs.setLibraryStepSort(libraryStepSort);
        } catch (SQLException sqlex) {
            // That means there is not this column, so nothing to do
        }

        LOG.debug(tcs.toJson());

        return tcs;

    }
}
