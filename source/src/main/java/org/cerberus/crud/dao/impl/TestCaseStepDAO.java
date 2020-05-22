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
import org.cerberus.util.StringUtil;

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
    public TestCaseStep findTestCaseStep(String test, String testcase, Integer step) {
        TestCaseStep result = null;
        final String query = "SELECT * FROM testcasestep WHERE test = ? AND testcase = ? AND step = ?";

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
                preStat.setInt(3, step);

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
        final String query = "DELETE FROM testcasestep WHERE test = ? and testcase = ? and step = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, tcs.getTest());
                preStat.setString(2, tcs.getTestCase());
                preStat.setInt(3, tcs.getStep());

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
        query.append(" `Description` = ?, `useStep`=? ");
        if (!StringUtil.isNullOrEmpty(tcs.getUseStepTest())) {
            query.append(",`useStepTest`=? ");
        }
        if (!StringUtil.isNullOrEmpty(tcs.getUseStepTestCase())) {
            query.append(",`useStepTestCase`=? ");
        }
        if (tcs.getUseStepStep() >= 0) {
            query.append(",`useStepStep`=? ");
        }
        query.append(",`inlibrary` = ?, `Sort` = ?, `loop` = ?, `conditionOperator` = ?, `conditionVal1` = ?, `conditionVal2` = ?, `conditionVal3` = ?, `forceExe` = ?, DateModif = CURRENT_TIMESTAMP, UsrModif = ? WHERE Test = ? AND TestCase = ? AND step = ?");

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
                preStat.setString(i++, tcs.getUseStep() == null ? "N" : tcs.getUseStep());
                if (!StringUtil.isNullOrEmpty(tcs.getUseStepTest())) {
                    preStat.setString(i++, tcs.getUseStepTest());
                }
                if (!StringUtil.isNullOrEmpty(tcs.getUseStepTestCase())) {
                    preStat.setString(i++, tcs.getUseStepTestCase());
                }
                if (tcs.getUseStepStep() >= 0) {
                    preStat.setInt(i++, tcs.getUseStepStep());
                }
                preStat.setString(i++, tcs.getInLibrary() == null ? "N" : tcs.getInLibrary());
                preStat.setInt(i++, tcs.getSort());
                preStat.setString(i++, tcs.getLoop() == null ? "" : tcs.getLoop());
                preStat.setString(i++, tcs.getConditionOperator() == null ? "" : tcs.getConditionOperator());
                preStat.setString(i++, tcs.getConditionVal1() == null ? "" : tcs.getConditionVal1());
                preStat.setString(i++, tcs.getConditionVal2() == null ? "" : tcs.getConditionVal2());
                preStat.setString(i++, tcs.getConditionVal3() == null ? "" : tcs.getConditionVal3());
                preStat.setString(i++, tcs.getForceExe() == null ? "N" : tcs.getForceExe());
                preStat.setString(i++, tcs.getUsrModif() == null ? "" : tcs.getUsrModif());

                preStat.setString(i++, tcs.getTest());
                preStat.setString(i++, tcs.getTestCase());
                preStat.setInt(i++, tcs.getStep());

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
    public List<TestCaseStep> getTestCaseStepUsingStepInParamter(String test, String testCase, int step) throws CerberusException {
        List<TestCaseStep> list = new ArrayList<TestCaseStep>();
        final String query = "SELECT * FROM testcasestep WHERE usestep='Y' AND usesteptest = ? AND usesteptestcase = ? AND usestepstep = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);
                preStat.setInt(3, step);

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
    public List<TestCaseStep> getTestCaseStepUsingTestCaseInParamter(String test, String testCase) throws CerberusException {
        List<TestCaseStep> list = null;
        final String query = "SELECT * FROM testcasestep WHERE usestep='Y' AND usesteptest = ? AND usesteptestcase = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);

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
                final PreparedStatement statement = connection.prepareStatement("SELECT * FROM testcasestep WHERE usestep='Y' AND usesteptest = ?")) {
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
        query.append("SELECT tcs.usesteptest, tcs.usesteptestcase,tcs.usestepstep,tcs.sort, tcs2.description FROM testcasestep tcs ");
        query.append("join testcase tc on tc.test=tcs.test and tc.testcase=tcs.testcase ");
        query.append("join testcasestep tcs2 on tcs.test=tcs2.test and tcs.testcase=tcs2.testcase and tcs.step=tcs2.step ");
        query.append("where tcs.usestep = 'Y' and tc.application = ?  ");
        query.append("group by tcs.usesteptest, tcs.usesteptestcase, tcs.usestepstep ");

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
                        String t = resultSet.getString("usesteptest");
                        String tc = resultSet.getString("usesteptestcase");
                        int s = resultSet.getInt("usestepstep");
                        int sort = resultSet.getInt("sort");
                        String description = resultSet.getString("description");
                        list.add(factoryTestCaseStep.create(t, tc, s, sort, null, null, null, null, null, description, null, null, null, 0, null, null, null, null, null, null));
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
        query.append("SELECT tcs.test, tcs.testcase, tcs.step, tcs.sort, tcs.description, tc.description as tcdesc FROM testcasestep tcs ");
        query.append("join testcase tc on tc.test=tcs.test and tc.testcase=tcs.testcase ");
        query.append("join application app  on tc.application=app.application ");
        query.append("where tcs.inlibrary = 'Y' and app.system = ?  ");
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
                        int s = resultSet.getInt("step");
                        int sort = resultSet.getInt("sort");
                        String description = resultSet.getString("description");
                        TestCaseStep tcs = factoryTestCaseStep.create(t, tc, s, sort, null, null, null, null, null, description, null, null, null, 0, null, null, null, null, null, null);
                        TestCase tcObj = factoryTestCase.create(t, tc, tcdesc);
                        tcs.setTestCaseObj(tcObj);
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
        query.append("SELECT tcs.test, tcs.testcase,tcs.step, tcs.sort, tcs.description, tc.description as tcdesc, tc.application as tcapp FROM testcasestep tcs ");
        query.append("join testcase tc on tc.test=tcs.test and tc.testcase=tcs.testcase ");
        query.append("join application app  on tc.application=app.application ");
        query.append("where tcs.inlibrary = 'Y' ");
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
                        int s = resultSet.getInt("step");
                        int sort = resultSet.getInt("sort");
                        String description = resultSet.getString("description");
                        String tcdesc = resultSet.getString("tcdesc");
                        TestCase tcToAdd = factoryTestCase.create(t, tc, tcdesc);
                        tcToAdd.setApplication(resultSet.getString("tcapp"));
                        TestCaseStep tcsToAdd = factoryTestCaseStep.create(t, tc, s, sort, null, null, null, null, null, description, null, null, null, 0, null, null, null, null, null, null);
                        tcsToAdd.setTestCaseObj(tcToAdd);
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
    public List<TestCaseStep> getStepLibraryBySystemTestTestCase(String system, String test, String testCase) throws CerberusException {
        List<TestCaseStep> list = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT tcs.test, tcs.testcase,tcs.step, tcs.sort, tcs.description FROM testcasestep tcs ");
        query.append("join testcase tc on tc.test=tcs.test and tc.testcase=tcs.testcase ");
        query.append("join application app  on tc.application=app.application ");
        query.append("where tcs.inlibrary = 'Y' ");
        if (system != null) {
            query.append("and app.system = ? ");
        }
        if (test != null) {
            query.append("and tcs.test = ? ");
        }
        if (testCase != null) {
            query.append("and tcs.testcase = ? ");
        }
        query.append("order by tcs.test, tcs.testcase, tcs.sort");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param.system : " + system);
            LOG.debug("SQL.param.test : " + test);
            LOG.debug("SQL.param.testcase : " + testCase);
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
                if (testCase != null) {
                    preStat.setString(i++, testCase);
                }

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestCaseStep>();
                try {
                    while (resultSet.next()) {
                        String t = resultSet.getString("test");
                        String tc = resultSet.getString("testcase");
                        int s = resultSet.getInt("step");
                        int sort = resultSet.getInt("sort");
                        String description = resultSet.getString("description");
                        list.add(factoryTestCaseStep.create(t, tc, s, sort, null, null, null, null, null, description, null, null, null, 0, null, null, null, null, null, null));
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
        query.append("SELECT tcs.*, CASE WHEN tcs1.test + tcs1.testcase + tcs1.step is NULL THEN 0 ELSE 1 END as isStepInUseByOtherTestCase FROM testcasestep tcs LEFT JOIN testcasestep tcs1 ON tcs1.useStep = 'Y' AND tcs1.useStepTest = ? AND tcs1.useStepTestCase = ? AND tcs1.useStepStep = tcs.step WHERE tcs.test = ? AND tcs.testcase = ? GROUP BY tcs.test, tcs.testcase, tcs.step ORDER BY tcs.sort");

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
    public AnswerList readByLibraryUsed(String test, String testcase, int step) {
        AnswerList response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCaseStep> stepList = new ArrayList<TestCaseStep>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcasestep tcs WHERE tcs.useStep = 'Y' AND tcs.useStepTest = ? AND tcs.useStepTestCase = ? AND tcs.useStepStep = ?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                preStat.setInt(3, step);
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
        query.append("INSERT INTO `testcasestep` (`Test`,`TestCase`,`Step`,`Sort`,`Description`,`useStep` ");

        if (!StringUtil.isNullOrEmpty(testCaseStep.getUseStepTest())) {
            query.append(",`useStepTest` ");
        }
        if (!StringUtil.isNullOrEmpty(testCaseStep.getUseStepTestCase())) {
            query.append(",`useStepTestCase` ");
        }
        if (testCaseStep.getUseStepStep() >= 0) {
            query.append(",`useStepStep` ");
        }
        query.append(", `inLibrary`, `loop`, `conditionOperator`, `conditionVal1`, `conditionVal2`, `conditionVal3`, `forceExe`, `usrCreated`) ");
        query.append("VALUES (?,?,?,?,?,?,?");
        if (!StringUtil.isNullOrEmpty(testCaseStep.getUseStepTest())) {
            query.append(",?");
        }
        if (!StringUtil.isNullOrEmpty(testCaseStep.getUseStepTestCase())) {
            query.append(",?");
        }
        if (testCaseStep.getUseStepStep() >= 0) {
            query.append(",?");
        }
        query.append(",?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param.usestepTest : " + testCaseStep.getUseStepTest());
            LOG.debug("SQL.param.usestepTestCase : " + testCaseStep.getUseStepTestCase());
            LOG.debug("SQL.param.usestepStep : " + testCaseStep.getUseStepStep());
        }

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString())) {
            // Prepare and execute query
            int i = 1;
            preStat.setString(i++, testCaseStep.getTest());
            preStat.setString(i++, testCaseStep.getTestCase());
            preStat.setInt(i++, testCaseStep.getStep());
            preStat.setInt(i++, testCaseStep.getSort());
            preStat.setString(i++, testCaseStep.getDescription());
            preStat.setString(i++, testCaseStep.getUseStep() == null ? "N" : testCaseStep.getUseStep());
            if (!StringUtil.isNullOrEmpty(testCaseStep.getUseStepTest())) {
                preStat.setString(i++, testCaseStep.getUseStepTest());
            }
            if (!StringUtil.isNullOrEmpty(testCaseStep.getUseStepTestCase())) {
                preStat.setString(i++, testCaseStep.getUseStepTestCase());
            }
            if (testCaseStep.getUseStepStep() >= 0) {
                preStat.setInt(i++, testCaseStep.getUseStepStep());
            }
            preStat.setString(i++, testCaseStep.getInLibrary() == null ? "N" : testCaseStep.getInLibrary());
            preStat.setString(i++, testCaseStep.getLoop() == null ? "" : testCaseStep.getLoop());
            preStat.setString(i++, testCaseStep.getConditionOperator() == null ? "" : testCaseStep.getConditionOperator());
            preStat.setString(i++, testCaseStep.getConditionVal1() == null ? "" : testCaseStep.getConditionVal1());
            preStat.setString(i++, testCaseStep.getConditionVal2() == null ? "" : testCaseStep.getConditionVal2());
            preStat.setString(i++, testCaseStep.getConditionVal3() == null ? "" : testCaseStep.getConditionVal3());
            preStat.setString(i++, testCaseStep.getForceExe() == null ? "N" : testCaseStep.getForceExe());
            preStat.setString(i++, testCaseStep.getUsrCreated() == null ? "" : testCaseStep.getUsrCreated());
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "CREATE");
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
        int step = resultSet.getInt("step") == 0 ? 0 : resultSet.getInt("step");
        int sort = resultSet.getInt("sort");
        String loop = resultSet.getString("loop") == null ? "" : resultSet.getString("loop");
        String conditionOperator = resultSet.getString("conditionOperator") == null ? "" : resultSet.getString("conditionOperator");
        String conditionVal1 = resultSet.getString("conditionVal1") == null ? "" : resultSet.getString("conditionVal1");
        String conditionVal2 = resultSet.getString("conditionVal2") == null ? "" : resultSet.getString("conditionVal2");
        String conditionVal3 = resultSet.getString("conditionVal3") == null ? "" : resultSet.getString("conditionVal3");
        String description = resultSet.getString("description") == null ? "" : resultSet.getString("description");
        String useStep = resultSet.getString("useStep") == null ? "" : resultSet.getString("useStep");
        String useStepTest = resultSet.getString("useStepTest") == null ? "" : resultSet.getString("useStepTest");
        String useStepTestCase = resultSet.getString("useStepTestCase") == null ? "" : resultSet.getString("useStepTestCase");
        int useStepStep = resultSet.getInt("useStepStep") == 0 ? 0 : resultSet.getInt("useStepStep");
        String inLibrary = resultSet.getString("inLibrary") == null ? "" : resultSet.getString("inLibrary");
        String forceExe = resultSet.getString("ForceExe");
        String usrCreated = resultSet.getString("UsrCreated");
        Timestamp dateCreated = resultSet.getTimestamp("DateCreated");
        String usrModif = resultSet.getString("UsrModif");
        Timestamp dateModif = resultSet.getTimestamp("DateModif");

        TestCaseStep tcs = factoryTestCaseStep.create(test, testcase, step, sort, loop, conditionOperator, conditionVal1, conditionVal2, conditionVal3, description, useStep, useStepTest, useStepTestCase, useStepStep,
                inLibrary, forceExe, usrCreated, dateCreated, usrModif, dateModif);

        try {
            resultSet.findColumn("isStepInUseByOtherTestCase");
            boolean isStepInUseByOtherTestCase = resultSet.getInt("isStepInUseByOtherTestCase") == 1 ? true : false;
            tcs.setIsStepInUseByOtherTestCase(isStepInUseByOtherTestCase);
        } catch (SQLException sqlex) {
            // That means there is not this column, so nothing to do
        }

        return tcs;

    }
}
