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

import com.google.common.base.Strings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.crud.dao.IApplicationDAO;
import org.cerberus.crud.dao.ITestCaseDAO;
import org.cerberus.crud.dao.ITestCaseExecutionDAO;
import org.cerberus.crud.entity.*;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.utils.RequestDbUtils;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.crud.factory.IFactoryTestCaseExecution;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.security.UserSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TestCaseExecutionDAO implements ITestCaseExecutionDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseExecution factoryTCExecution;
    @Autowired
    private IApplicationDAO applicationDAO;
    @Autowired
    private ITestCaseDAO testCaseDAO;
    @Autowired
    private IParameterService parameterService;

    private static final Logger LOG = LogManager.getLogger(TestCaseExecutionDAO.class);

    private final String OBJECT_NAME = "TestCase Execution";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public long insertTCExecution(TestCaseExecution tCExecution) throws CerberusException {
        boolean throwEx = false;
        final String query = "INSERT INTO testcaseexecution(test, testcase, description, build, revision, environment, environmentData, country, browser, application, robothost, "
                + "url, robotport, tag, status, start, controlstatus, controlMessage, crbversion, executor, screensize, conditionOperator, conditionVal1Init, conditionVal2Init, conditionVal3Init, conditionVal1, conditionVal2, conditionVal3, "
                + "manualExecution, UserAgent, queueId, testCaseVersion, TestCasePriority, `system`, robotdecli, robot, robotexecutor, RobotProvider, RobotProviderSessionId, RobotSessionId, UsrCreated) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.id : " + tCExecution.getId());
            LOG.debug("SQL.param.robotexecutor : " + tCExecution.getRobotExecutor());
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            try {
                int i = 1;
                preStat.setString(i++, tCExecution.getTest());
                preStat.setString(i++, tCExecution.getTestCase());
                preStat.setString(i++, tCExecution.getDescription());
                preStat.setString(i++, tCExecution.getBuild());
                preStat.setString(i++, tCExecution.getRevision());
                preStat.setString(i++, tCExecution.getEnvironment());
                preStat.setString(i++, tCExecution.getEnvironmentData());
                preStat.setString(i++, tCExecution.getCountry());
                preStat.setString(i++, tCExecution.getBrowser());
                preStat.setString(i++, tCExecution.getApplicationObj().getApplication());
                preStat.setString(i++, tCExecution.getRobotHost());
                preStat.setString(i++, tCExecution.getUrl());
                preStat.setString(i++, tCExecution.getRobotPort());
                preStat.setString(i++, tCExecution.getTag());
                preStat.setString(i++, tCExecution.getStatus());
                preStat.setTimestamp(i++, new Timestamp(tCExecution.getStart()));
                preStat.setString(i++, tCExecution.getControlStatus());
                preStat.setString(i++, StringUtil.getLeftString(tCExecution.getControlMessage(), 65000));
                preStat.setString(i++, tCExecution.getCrbVersion());
                preStat.setString(i++, tCExecution.getExecutor());
                preStat.setString(i++, tCExecution.getScreenSize());
                preStat.setString(i++, tCExecution.getConditionOperator());
                preStat.setString(i++, StringUtil.getLeftString(tCExecution.getConditionVal1Init(), 65000));
                preStat.setString(i++, StringUtil.getLeftString(tCExecution.getConditionVal2Init(), 65000));
                preStat.setString(i++, StringUtil.getLeftString(tCExecution.getConditionVal3Init(), 65000));
                preStat.setString(i++, StringUtil.getLeftString(tCExecution.getConditionVal1(), 65000));
                preStat.setString(i++, StringUtil.getLeftString(tCExecution.getConditionVal2(), 65000));
                preStat.setString(i++, StringUtil.getLeftString(tCExecution.getConditionVal3(), 65000));
                preStat.setString(i++, tCExecution.getManualExecution());
                preStat.setString(i++, tCExecution.getUserAgent());
                preStat.setLong(i++, tCExecution.getQueueID());
                preStat.setInt(i++, tCExecution.getTestCaseVersion());
                preStat.setInt(i++, tCExecution.getTestCasePriority());
                preStat.setString(i++, tCExecution.getSystem());
                preStat.setString(i++, tCExecution.getRobotDecli());
                preStat.setString(i++, tCExecution.getRobot());
                preStat.setString(i++, tCExecution.getRobotExecutor());
                preStat.setString(i++, tCExecution.getRobotProvider());
                preStat.setString(i++, tCExecution.getRobotProviderSessionID());
                preStat.setString(i++, tCExecution.getRobotSessionID());
                preStat.setString(i++, tCExecution.getUsrCreated());

                preStat.executeUpdate();
                ResultSet resultSet = preStat.getGeneratedKeys();
                try {
                    if (resultSet.first()) {
                        return resultSet.getLong(1);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString(), exception);
                    throwEx = true;
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString(), exception);
                throwEx = true;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString(), exception);
            throwEx = true;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
        }
        return 0;
    }

    @Override
    public void updateTCExecution(TestCaseExecution tCExecution) throws CerberusException {
        boolean throwEx = false;
        final String query = "UPDATE testcaseexecution SET test = ?, testcase = ?, description = ?, build = ?, revision = ?, environment = ?, environmentData = ?, country = ?"
                + ", browser = ?, application = ?, robothost = ?, url = ?, robotport = ?, tag = ?, status = ?"
                + ", start = ?, end = ? , controlstatus = ?, controlMessage = ?, crbversion = ? "
                + ", version = ?, platform = ?, executor = ?, screensize = ? "
                + ", conditionOperator = ?, ConditionVal1Init = ?, ConditionVal2Init = ?, ConditionVal3Init = ?, ConditionVal1 = ?, ConditionVal2 = ?, ConditionVal3 = ?, ManualExecution = ?, UserAgent = ?, queueId = ?, testCaseVersion = ?, testCasePriority = ?, `system` = ? "
                + ", robotdecli = ?, robot = ?, robotexecutor = ?, RobotProvider = ?, RobotSessionId = ?, RobotProviderSessionId = ?, UsrModif = ?, DateModif = NOW() WHERE id = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.id : " + tCExecution.getId());
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            try {
                int i = 1;
                preStat.setString(i++, tCExecution.getTest());
                preStat.setString(i++, tCExecution.getTestCase());
                preStat.setString(i++, tCExecution.getDescription());
                preStat.setString(i++, tCExecution.getBuild());
                preStat.setString(i++, tCExecution.getRevision());
                preStat.setString(i++, tCExecution.getEnvironment());
                preStat.setString(i++, tCExecution.getEnvironmentData());
                preStat.setString(i++, tCExecution.getCountry());
                preStat.setString(i++, tCExecution.getBrowser());
                preStat.setString(i++, tCExecution.getApplicationObj().getApplication());
                preStat.setString(i++, tCExecution.getRobotHost());
                preStat.setString(i++, tCExecution.getUrl());
                preStat.setString(i++, tCExecution.getRobotPort());
                preStat.setString(i++, tCExecution.getTag());
                preStat.setString(i++, tCExecution.getStatus());
                preStat.setTimestamp(i++, new Timestamp(tCExecution.getStart()));
                if (tCExecution.getEnd() != 0) {
                    preStat.setTimestamp(i++, new Timestamp(tCExecution.getEnd()));
                } else {
                    preStat.setString(i++, "1970-01-01 01:01:01");
                }
                preStat.setString(i++, tCExecution.getControlStatus());
                preStat.setString(i++, StringUtil.getLeftString(tCExecution.getControlMessage(), 65000));
                preStat.setString(i++, tCExecution.getCrbVersion());
                preStat.setString(i++, tCExecution.getVersion());
                preStat.setString(i++, tCExecution.getPlatform());
                preStat.setString(i++, tCExecution.getExecutor());
                preStat.setString(i++, tCExecution.getScreenSize());
                preStat.setString(i++, tCExecution.getConditionOperator());
                preStat.setString(i++, StringUtil.getLeftString(tCExecution.getConditionVal1Init(), 65000));
                preStat.setString(i++, StringUtil.getLeftString(tCExecution.getConditionVal2Init(), 65000));
                preStat.setString(i++, StringUtil.getLeftString(tCExecution.getConditionVal3Init(), 65000));
                preStat.setString(i++, StringUtil.getLeftString(tCExecution.getConditionVal1(), 65000));
                preStat.setString(i++, StringUtil.getLeftString(tCExecution.getConditionVal2(), 65000));
                preStat.setString(i++, StringUtil.getLeftString(tCExecution.getConditionVal3(), 65000));
                preStat.setString(i++, tCExecution.getManualExecution());
                preStat.setString(i++, tCExecution.getUserAgent());
                preStat.setLong(i++, tCExecution.getQueueID());
                preStat.setInt(i++, tCExecution.getTestCaseVersion());
                preStat.setInt(i++, tCExecution.getTestCasePriority());
                preStat.setString(i++, tCExecution.getSystem());
                preStat.setString(i++, tCExecution.getRobotDecli());
                preStat.setString(i++, tCExecution.getRobot());
                preStat.setString(i++, tCExecution.getRobotExecutor());
                preStat.setString(i++, tCExecution.getRobotProvider());
                preStat.setString(i++, tCExecution.getRobotSessionID());
                preStat.setString(i++, tCExecution.getRobotProviderSessionID());
                preStat.setString(i++, tCExecution.getUsrModif());
                preStat.setLong(i++, tCExecution.getId());

                preStat.executeUpdate();
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                throwEx = true;
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
                LOG.warn(e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
        }
    }

    @Override
    public List<String> getIDListOfLastExecutions(String test, String testcase, String country) {
        List<String> list = null;
        final String query = "SELECT ID FROM testcaseexecution WHERE test = ? AND testcase = ? AND country = ? AND controlStatus='OK' ORDER BY id DESC LIMIT 200";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                preStat.setString(3, country);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<>();

                    while (resultSet.next()) {
                        list.add(resultSet.getString("ID"));
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
                LOG.warn(e.toString());
            }
        }

        return list;
    }

    @Override
    public AnswerItem<TestCaseExecution> readLastByCriteria(String application) {
        AnswerItem<TestCaseExecution> ans = new AnswerItem<>();
        TestCaseExecution result = null;
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that
        //were applied -- used for pagination p
        query.append("select * from testcaseexecution exe ");

        searchSQL.append(" where 1=1 ");

        if (!StringUtil.isNullOrEmpty(application)) {
            searchSQL.append(" and (`application` = ? )");
        }
        query.append(searchSQL);

        query.append(" order by id DESC limit 1 ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            try {
                int i = 1;
                if (!StringUtil.isNullOrEmpty(application)) {
                    preStat.setString(i++, application);
                }
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = loadFromResultSet(resultSet);
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        ans.setItem(result);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        //sets the message
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public TestCaseExecution findLastTCExecutionByCriteria(String test, String testcase, String environment, String country,
            String build, String revision) throws CerberusException {
        TestCaseExecution result = null;
        final String query = new StringBuffer("SELECT exe.* FROM testcaseexecution exe ")
                .append("WHERE exe.test = ? AND exe.testcase = ? AND exe.environment = ? ")
                .append("AND exe.country = ? AND exe.build = ? AND exe.revision = ? ")
                .append("ORDER BY exe.id DESC").toString();

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {
            preStat.setString(1, test);
            preStat.setString(2, testcase);
            preStat.setString(3, environment);
            preStat.setString(4, country);
            preStat.setString(5, build);
            preStat.setString(6, revision);
            try (ResultSet resultSet = preStat.executeQuery();) {
                if (resultSet.first()) {
                    result = this.loadFromResultSet(resultSet);
                } else {
                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        }
        return result;
    }

    @Override
    public TestCaseExecution findLastTCExecutionByCriteria(String test, String testCase, String environment, String country,
            String build, String revision, String browser, String browserVersion,
            String ip, String port, String tag) {
        TestCaseExecution result = null;
        final String query = new StringBuffer("SELECT exe.* FROM testcaseexecution exe ")
                .append("WHERE exe.test = ? AND exe.testcase = ? ")
                .append("AND exe.environment LIKE ? AND exe.country = ? AND exe.build LIKE ? ")
                .append("AND exe.revision LIKE ? AND exe.browser = ? AND exe.browserfullversion LIKE ? ")
                .append("AND exe.ip LIKE ? AND exe.port LIKE ? AND exe.tag LIKE ? ")
                .append("ORDER BY exe.id DESC").toString();

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {
            preStat.setString(1, test);
            preStat.setString(2, testCase);
            preStat.setString(3, ParameterParserUtil.wildcardIfEmpty(environment));
            preStat.setString(4, country);
            preStat.setString(5, ParameterParserUtil.wildcardIfEmpty(build));
            preStat.setString(6, ParameterParserUtil.wildcardIfEmpty(revision));
            preStat.setString(7, browser);
            preStat.setString(8, ParameterParserUtil.wildcardIfEmpty(browserVersion));
            preStat.setString(9, ParameterParserUtil.wildcardIfEmpty(ip));
            preStat.setString(10, ParameterParserUtil.wildcardIfEmpty(port));
            preStat.setString(11, ParameterParserUtil.wildcardIfEmpty(tag));
            try (ResultSet resultSet = preStat.executeQuery();) {
                if (resultSet.first()) {
                    result = this.loadFromResultSet(resultSet);
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        }
        return result;
    }

    @Override
    public List<TestCaseExecution> findExecutionbyCriteria1(String dateLimit, String test, String testCase, String application, String country, String environment, String controlStatus, String status) throws CerberusException {
        List<TestCaseExecution> myTestCaseExecutions = null;
        TestCaseExecution Execution;
        boolean throwException = false;
        final String query = new StringBuffer("SELECT exe.*, tec.*, app.* FROM testcaseexecution exe ")
                .append("LEFT JOIN testcase tec ON exe.test = tec.test AND exe.testcase = tec.testcase ")
                .append("LEFT JOIN application app ON exe.application = app.application ")
                .append("WHERE exe.start > ? AND exe.test LIKE ? AND exe.testcase LIKE ? AND exe.environment LIKE ? ")
                .append("AND exe.country LIKE ? AND exe.application LIKE ? AND exe.controlstatus LIKE ? ")
                .append("AND exe.status LIKE ?").toString();

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {
            preStat.setString(1, dateLimit);
            preStat.setString(2, test);
            preStat.setString(3, testCase);
            preStat.setString(4, environment);
            preStat.setString(5, country);
            preStat.setString(6, application);
            preStat.setString(7, controlStatus);
            preStat.setString(8, status);

            try (ResultSet resultSet = preStat.executeQuery();) {
                if (!(resultSet.first())) {
                    throwException = true;
                } else {
                    myTestCaseExecutions = new ArrayList<>();
                    do {
                        Execution = this.loadWithDependenciesFromResultSet(resultSet);

                        myTestCaseExecutions.add(Execution);
                    } while (resultSet.next());
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            }
        } catch (Exception exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        }
        if (throwException) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
        }
        return myTestCaseExecutions;
    }

    @Override
    public TestCaseExecution findTCExecutionByKey(long id) throws CerberusException {
        TestCaseExecution result = null;
        final String query = "SELECT * FROM testcaseexecution exe, application app WHERE exe.application = app.application AND ID = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {
            preStat.setLong(1, id);
            try (ResultSet resultSet = preStat.executeQuery();) {
                if (resultSet.first()) {
                    result = this.loadTestCaseExecutionAndApplicationFromResultSet(resultSet);
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        }
        return result;
    }

    @Override
    public TestCaseExecution findLastTestCaseExecutionNotPE(String test, String testCase) throws CerberusException {
        TestCaseExecution result = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT exe.*  FROM `testcaseexecution` exe ");
        query.append(" WHERE Test = ? and TestCase= ? and ID = ");
        query.append(" (SELECT MAX(ID) from `testcaseexecution` ");
        query.append("WHERE Test= ? and TestCase= ? and ControlStatus!='PE')");

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {

            preStat.setString(1, test);
            preStat.setString(2, testCase);
            preStat.setString(3, test);
            preStat.setString(4, testCase);

            try (ResultSet resultSet = preStat.executeQuery();) {
                if (resultSet.first()) {
                    result = loadFromResultSet(resultSet);
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        }
        return result;
    }

    @Override
    public TestCaseExecution findLastTCExecutionInGroup(String test, String testCase, String environment, String country,
            String build, String revision, String browser, String browserVersion,
            String ip, String port, String tag) {

        TestCaseExecution result = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT exe.* FROM testcaseexecution exe ")
                .append("WHERE exe.test = ? AND exe.testcase = ? AND exe.country = ? AND exe.browser = ? ");
        if (!StringUtil.isNull(environment)) {
            query.append("AND exe.environment IN (");
            query.append(environment);
            query.append(") ");
        }
        if (!StringUtil.isNull(build)) {
            query.append("AND exe.build IN (");
            query.append(build);
            query.append(") ");
        }
        if (!StringUtil.isNull(revision)) {
            query.append("AND exe.revision IN (");
            query.append(revision);
            query.append(") ");
        }
        if (!StringUtil.isNull(browserVersion)) {
            query.append("AND exe.browserfullversion LIKE ? ");
        }
        if (!StringUtil.isNull(ip)) {
            query.append("AND exe.ip LIKE ? ");
        }
        if (!StringUtil.isNull(port)) {
            query.append("AND exe.port LIKE ? ");
        }
        if (!StringUtil.isNull(tag)) {
            query.append("AND exe.tag LIKE ? ");
        }
        query.append("ORDER BY exe.id DESC");

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());) {
            preStat.setString(1, test);
            preStat.setString(2, testCase);
            preStat.setString(3, country);
            preStat.setString(4, browser);
            int i = 5;
            if (!StringUtil.isNull(browserVersion)) {
                preStat.setString(i, browserVersion);
                i++;
            }
            if (!StringUtil.isNull(ip)) {
                preStat.setString(i, ip);
                i++;
            }
            if (!StringUtil.isNull(port)) {
                preStat.setString(i, port);
                i++;
            }
            if (!StringUtil.isNull(tag)) {
                preStat.setString(i, tag);
            }
            try (ResultSet resultSet = preStat.executeQuery();) {
                if (resultSet.first()) {
                    result = this.loadFromResultSet(resultSet);
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        }
        return result;
    }

    @Override
    public List<String> findDistinctTag(boolean withUUIDTag) throws CerberusException {
        List<String> list = null;
        StringBuilder query = new StringBuilder();
        query.append("select distinct tag from testcaseexecution exe ")
                .append("where tag != '' ");
        if (!withUUIDTag) {
            query.append(" and length(tag) != length('c3888898-c65a-11e3-9b3e-0000004047e0')");
        }
        query.append(" UNION select distinct tag from testcaseexecutionqueue where tag !='' ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<>();

                    while (resultSet.next()) {
                        list.add(resultSet.getString("tag"));
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
                LOG.warn(e.toString());
            }
        }

        return list;
    }

    @Override
    public void setTagToExecution(long id, String tag) throws CerberusException {
        boolean throwEx = false;
        final String query = "UPDATE testcaseexecution exe SET exe.tag = ? WHERE exe.id = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, tag);
                preStat.setLong(2, id);

                preStat.executeUpdate();
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                throwEx = true;
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
                LOG.warn(e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public List<TestCaseExecution> readByTagByCriteria(String tag, int start, int amount, String sort, String searchTerm, Map<String, List<String>> individualSearch) throws CerberusException {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        List<String> individalColumnSearchValues = new ArrayList<>();

        final StringBuffer query = new StringBuffer();

        query.append("SELECT * FROM testcaseexecution exe ");
        query.append("left join testcase tec on exe.Test = tec.Test and exe.TestCase = tec.TestCase ");
        query.append("left join application app on tec.application = app.application ");
        query.append("where exe.ID IN ");
        query.append("(select MAX(exe.ID) from testcaseexecution exe ");

        query.append("where 1=1 ");
        if (!StringUtil.isNullOrEmpty(tag)) {
            query.append("and exe.tag = ? ");
        }

        query.append("group by exe.test, exe.testcase, exe.Environment, exe.Browser, exe.Country) ");
        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            query.append("and (exe.`test` like ? ");
            query.append(" or exe.`testCase` like ? ");
            query.append(" or exe.`application` like ? ");
            query.append(" or tec.`bugs` like ? ");
            query.append(" or tec.`priority` like ? ");
            query.append(" or tec.`description` like ? )");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            query.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                query.append(" and ");
                query.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individalColumnSearchValues.addAll(entry.getValue());
            }
            query.append(" ) ");
        }

        if (!StringUtil.isNullOrEmpty(sort)) {
            query.append(" order by ").append(sort);
        }

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL.param.tag : " + tag);
        }

        return RequestDbUtils.executeQueryList(databaseSpring, query.toString(),
                preStat -> {
                    int i = 1;
                    if (!StringUtil.isNullOrEmpty(tag)) {
                        preStat.setString(i++, tag);
                    }

                    if (!Strings.isNullOrEmpty(searchTerm)) {
                        preStat.setString(i++, "%" + searchTerm + "%");
                        preStat.setString(i++, "%" + searchTerm + "%");
                        preStat.setString(i++, "%" + searchTerm + "%");
                        preStat.setString(i++, "%" + searchTerm + "%");
                        preStat.setString(i++, "%" + searchTerm + "%");
                        preStat.setString(i++, "%" + searchTerm + "%");
                    }
                    for (String individualColumnSearchValue : individalColumnSearchValues) {
                        preStat.setString(i++, individualColumnSearchValue);
                    }
                },
                rs -> loadWithDependenciesFromResultSet(rs)
        );

    }

    @Override
    public AnswerList<TestCaseExecution> readByCriteria(List<String> system, List<String> countries, List<String> environments, List<String> robotDecli, List<TestCase> testcases, Date from, Date to) {
        AnswerList<TestCaseExecution> response = new AnswerList<>();
        List<TestCaseExecution> objectList = new ArrayList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        StringBuilder searchSQL = new StringBuilder();
        Timestamp t1;

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM testcaseexecution exe ");

        searchSQL.append(" where 1=1 ");

        // System
        if (system != null && !system.isEmpty()) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`System`", system));
        }
        // Country
        if (countries != null && !countries.isEmpty()) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`Country`", countries));
        }
        // System
        if (environments != null && !environments.isEmpty()) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`Environment`", environments));
        }
        // System
        if (robotDecli != null && !robotDecli.isEmpty()) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`RobotDecli`", robotDecli));
        }
        // from and to
        searchSQL.append(" and start >= ? and start <= ? ");
        // testCase
        StringBuilder testcaseSQL = new StringBuilder();
        for (TestCase testcase : testcases) {
            testcaseSQL.append(" (test = ? and testcase = ?) or ");
        }
        if (!StringUtil.isNullOrEmpty(testcaseSQL.toString())) {
            searchSQL.append("and (").append(testcaseSQL).append(" (0=1) ").append(")");
        }

        query.append(searchSQL);

        query.append(" limit ").append(MAX_ROW_SELECTED);

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                if (system != null && !system.isEmpty()) {
                    for (String syst : system) {
                        preStat.setString(i++, syst);
                    }
                }
                if (countries != null && !countries.isEmpty()) {
                    for (String val : countries) {
                        preStat.setString(i++, val);
                    }
                }
                if (environments != null && !environments.isEmpty()) {
                    for (String val : environments) {
                        preStat.setString(i++, val);
                    }
                }
                if (robotDecli != null && !robotDecli.isEmpty()) {
                    for (String val : robotDecli) {
                        preStat.setString(i++, val);
                    }
                }
                t1 = new Timestamp(from.getTime());
                preStat.setTimestamp(i++, t1);
                t1 = new Timestamp(to.getTime());
                preStat.setTimestamp(i++, t1);
                for (TestCase testcase : testcases) {
                    preStat.setString(i++, testcase.getTest());
                    preStat.setString(i++, testcase.getTestcase());
                }

                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        objectList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (objectList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList<>(objectList, nrTotalRows);
                    } else if (objectList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList<>(objectList, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList<>(objectList, nrTotalRows);
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
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
        response.setDataList(objectList);
        return response;
    }

    @Override
    public AnswerList<TestCaseExecution> readByTag(String tag) throws CerberusException {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        AnswerList<TestCaseExecution> answer = new AnswerList<>();

        final StringBuffer query = new StringBuffer();

        query.append("SELECT * FROM testcaseexecution exe ");
        query.append("left join testcase tec on exe.Test = tec.Test and exe.TestCase = tec.TestCase ");
        query.append("left join application as app on tec.application = app.application ");
        query.append("where 1=1 and exe.tag = ? ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param.tag : " + tag);
        }
        List<TestCaseExecution> testCaseExecutionList = new ArrayList<>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, tag);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testCaseExecutionList.add(this.loadWithDependenciesFromResultSet(resultSet));
                    }
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecution").replace("%OPERATION%", "SELECT"));
                    answer.setTotalRows(testCaseExecutionList.size());
                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    testCaseExecutionList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                testCaseExecutionList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            testCaseExecutionList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            }
        }

        answer.setResultMessage(msg);
        answer.setDataList(testCaseExecutionList);
        return answer;
    }

    @Override
    public Integer readNbByTag(String tag) throws CerberusException {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        Integer result = 0;

        final StringBuffer query = new StringBuffer();

        query.append("SELECT count(*) FROM testcaseexecution exe ");
        query.append("where 1=1 and exe.tag = ? ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param.tag : " + tag);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, tag);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        result = resultSet.getInt(1);
                    }
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecution").replace("%OPERATION%", "SELECT"));
                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            }
        }

        return result;
    }

    @Override
    public AnswerList<TestCaseExecution> readByCriteria(int start, int amount, String sort, String searchTerm, Map<String, List<String>> individualSearch, List<String> individualLike, List<String> system) throws CerberusException {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        AnswerList<TestCaseExecution> response = new AnswerList<>();
        List<String> individalColumnSearchValues = new ArrayList<>();
        List<TestCaseExecution> objectList = new ArrayList<>();

        final StringBuffer query = new StringBuffer();

        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM testcaseexecution exe ");
        query.append("where 1=1 ");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            query.append("and (exe.`id` like ? ");
            query.append(" or exe.`test` like ? ");
            query.append(" or exe.`testCase` like ? ");
            query.append(" or exe.`build` like ? ");
            query.append(" or exe.`revision` like ? ");
            query.append(" or exe.`environment` like ? ");
            query.append(" or exe.`country` like ? ");
            query.append(" or exe.`browser` like ? ");
            query.append(" or exe.`version` like ? ");
            query.append(" or exe.`platform` like ? ");
            query.append(" or exe.`browserfullversion` like ? ");
            query.append(" or exe.`start` like ? ");
            query.append(" or exe.`end` like ? ");
            query.append(" or exe.`controlstatus` like ? ");
            query.append(" or exe.`controlmessage` like ? ");
            query.append(" or exe.`application` like ? ");
            query.append(" or exe.`url` like ? ");
            query.append(" or exe.`robot` like ? ");
            query.append(" or exe.`robotexecutor` like ? ");
            query.append(" or exe.`robothost` like ? ");
            query.append(" or exe.`robotport` like ? ");
            query.append(" or exe.`tag` like ? ");
            query.append(" or exe.`end` like ? ");
            query.append(" or exe.`status` like ? ");
            query.append(" or exe.`crbversion` like ? ");
            query.append(" or exe.`executor` like ? ");
            query.append(" or exe.`screensize` like ? ");
            query.append(" or exe.`userAgent` like ? )");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            query.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                query.append(" and ");
                query.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individalColumnSearchValues.addAll(entry.getValue());
            }
            query.append(" ) ");
        }

        if (system != null && !system.isEmpty()) {
            query.append(" and " + SqlUtil.generateInClause("exe.`system`", system) + " ");
        }

        query.append(" AND " + UserSecurity.getSystemAllowForSQL("exe.`system`"));

        if (!StringUtil.isNullOrEmpty(sort)) {
            query.append(" order by ").append(sort);
        }

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                if (!Strings.isNullOrEmpty(searchTerm)) {
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");

                }
                for (String individualColumnSearchValue : individalColumnSearchValues) {
                    preStat.setString(i++, individualColumnSearchValue);
                }

                if (system != null && !system.isEmpty()) {
                    for (String sys : system) {
                        preStat.setString(i++, sys);
                    }
                }

                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        objectList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (objectList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList<>(objectList, nrTotalRows);
                    } else if (objectList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList<>(objectList, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList<>(objectList, nrTotalRows);
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString(), exception);
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString(), exception);
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString(), exception);
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
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
        response.setDataList(objectList);
        return response;
    }

    @Override
    public AnswerList<TestCaseExecution> readDistinctEnvCoutnryBrowserByTag(String tag) {
        AnswerList<TestCaseExecution> answer = new AnswerList<>();
        StringBuilder query = new StringBuilder();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);

        query.append("SELECT exe.* FROM testcaseexecution exe WHERE exe.tag = ? GROUP BY exe.Environment, exe.Country, exe.Browser, exe.ControlStatus");

        Connection connection = this.databaseSpring.connect();

        List<TestCaseExecution> testCaseExecutionList = new ArrayList<>();

        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());

            preStat.setString(1, tag);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testCaseExecutionList.add(this.loadFromResultSet(resultSet));
                    }
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecution").replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList<>(testCaseExecutionList, testCaseExecutionList.size());
                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    testCaseExecutionList = null;
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException ex) {
                LOG.warn("Unable to execute query : " + ex.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                testCaseExecutionList = null;
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException ex) {
            LOG.warn(ex.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                LOG.warn("Unable to execute query : " + ex.toString());
            }
        }
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerList<TestCaseExecution> readDistinctColumnByTag(String tag, boolean env, boolean country, boolean browser, boolean app) {
        AnswerList<TestCaseExecution> answer = new AnswerList<>();
        StringBuilder query = new StringBuilder();
        StringBuilder distinct = new StringBuilder();
        int prev = 0;
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);

        if (!(!env && !country && !app && !browser)) {
            if (env) {
                distinct.append("Environment");
                prev++;
            }
            if (country) {
                if (prev != 0) {
                    prev = 0;
                    distinct.append(",");
                }
                distinct.append("Country");
                prev++;
            }
            if (browser) {
                if (prev != 0) {
                    prev = 0;
                    distinct.append(",");
                }
                distinct.append("Browser");
                prev++;
            }
            if (app) {
                if (prev != 0) {
                    prev = 0;
                    distinct.append(",");
                }
                distinct.append("Application");
            }

            query.append("SELECT ");
            query.append(distinct.toString());
            query.append(" FROM testcaseexecution exe WHERE exe.tag = ? GROUP BY ");
            query.append(distinct.toString());
        } else {
            //If there is no distinct, select nothing
            query.append("SELECT * FROM testcaseexecution exe WHERE 1 = 0 AND exe.tag = ?");
        }

        Connection connection = this.databaseSpring.connect();

        List<TestCaseExecution> column = new ArrayList<>();

        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());

            preStat.setString(1, tag);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        TestCaseExecution tmp = new TestCaseExecution();
                        if (env) {
                            tmp.setEnvironment(resultSet.getString("Environment"));
                        } else {
                            tmp.setEnvironment("");
                        }
                        if (country) {
                            tmp.setCountry(resultSet.getString("Country"));
                        } else {
                            tmp.setCountry("");
                        }
                        if (browser) {
                            tmp.setBrowser(resultSet.getString("Browser"));
                        } else {
                            tmp.setBrowser("");
                        }
                        if (app) {
                            tmp.setApplication(resultSet.getString("Application"));
                        } else {
                            tmp.setApplication("");
                        }
                        column.add(tmp);
                    }
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecution").replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList<>(column, column.size());
                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    column = null;
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException ex) {
                LOG.warn("Unable to execute query : " + ex.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                column = null;
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException ex) {
            LOG.warn(ex.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                LOG.warn("Unable to execute query : " + ex.toString());
            }
        }

        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerItem<TestCaseExecution> readByKey(long executionId) {
        AnswerItem<TestCaseExecution> ans = new AnswerItem<>();
        TestCaseExecution result = null;
        final String query = "SELECT * FROM `testcaseexecution` exe WHERE exe.`id` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setLong(1, executionId);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = loadFromResultSet(resultSet);
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        ans.setItem(result);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        //sets the message
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public TestCaseExecution loadFromResultSet(ResultSet resultSet) throws SQLException {
        long id = ParameterParserUtil.parseLongParam(resultSet.getString("exe.ID"), 0);
        String test = ParameterParserUtil.parseStringParam(resultSet.getString("exe.test"), "");
        String testcase = ParameterParserUtil.parseStringParam(resultSet.getString("exe.testcase"), "");
        String description = ParameterParserUtil.parseStringParam(resultSet.getString("exe.description"), "");
        String build = ParameterParserUtil.parseStringParam(resultSet.getString("exe.build"), "");
        String revision = ParameterParserUtil.parseStringParam(resultSet.getString("exe.revision"), "");
        String environment = ParameterParserUtil.parseStringParam(resultSet.getString("exe.environment"), "");
        String environmentData = ParameterParserUtil.parseStringParam(resultSet.getString("exe.environmentData"), "");
        String country = ParameterParserUtil.parseStringParam(resultSet.getString("exe.country"), "");
        String robot = ParameterParserUtil.parseStringParam(resultSet.getString("exe.robot"), ""); // Host the Selenium IP
        String robotExecutor = ParameterParserUtil.parseStringParam(resultSet.getString("exe.robotExecutor"), ""); // Host the Selenium IP
        String robotHost = ParameterParserUtil.parseStringParam(resultSet.getString("exe.robotHost"), ""); // Host the Selenium IP
        String robotPort = ParameterParserUtil.parseStringParam(resultSet.getString("exe.robotPort"), ""); // host the Selenium Port
        String robotDecli = ParameterParserUtil.parseStringParam(resultSet.getString("exe.robotdecli"), "");
        String browser = ParameterParserUtil.parseStringParam(resultSet.getString("exe.browser"), "");
        String version = ParameterParserUtil.parseStringParam(resultSet.getString("exe.version"), "");
        String platform = ParameterParserUtil.parseStringParam(resultSet.getString("exe.platform"), "");
        long start = ParameterParserUtil.parseLongParam(String.valueOf(resultSet.getTimestamp("exe.start").getTime()), 0);
        long end = ParameterParserUtil.parseLongParam(String.valueOf(resultSet.getTimestamp("exe.end").getTime()), 0);
        String controlStatus = ParameterParserUtil.parseStringParam(resultSet.getString("exe.controlStatus"), "");
        String controlMessage = ParameterParserUtil.parseStringParam(resultSet.getString("exe.controlMessage"), "");
        String application = ParameterParserUtil.parseStringParam(resultSet.getString("exe.application"), "");
        String url = ParameterParserUtil.parseStringParam(resultSet.getString("exe.url"), "");
        String tag = ParameterParserUtil.parseStringParam(resultSet.getString("exe.tag"), "");
        String status = ParameterParserUtil.parseStringParam(resultSet.getString("exe.status"), "");
        String crbVersion = ParameterParserUtil.parseStringParam(resultSet.getString("exe.crbVersion"), "");
        String executor = ParameterParserUtil.parseStringParam(resultSet.getString("exe.executor"), "");
        String screenSize = ParameterParserUtil.parseStringParam(resultSet.getString("exe.screensize"), "");
        String conditionOperator = ParameterParserUtil.parseStringParam(resultSet.getString("exe.conditionOperator"), "");
        String conditionVal1 = ParameterParserUtil.parseStringParam(resultSet.getString("exe.conditionVal1"), "");
        String conditionVal1Init = ParameterParserUtil.parseStringParam(resultSet.getString("exe.conditionVal1Init"), "");
        String conditionVal2 = ParameterParserUtil.parseStringParam(resultSet.getString("exe.conditionVal2"), "");
        String conditionVal2Init = ParameterParserUtil.parseStringParam(resultSet.getString("exe.conditionVal2Init"), "");
        String conditionVal3 = ParameterParserUtil.parseStringParam(resultSet.getString("exe.conditionVal3"), "");
        String conditionVal3Init = ParameterParserUtil.parseStringParam(resultSet.getString("exe.conditionVal3Init"), "");
        String manualExecution = ParameterParserUtil.parseStringParam(resultSet.getString("exe.manualExecution"), "N");
        String userAgent = ParameterParserUtil.parseStringParam(resultSet.getString("exe.userAgent"), "");
        String system = ParameterParserUtil.parseStringParam(resultSet.getString("exe.System"), "");
        long queueId = ParameterParserUtil.parseLongParam(resultSet.getString("exe.queueId"), 0);
        int testCaseVersion = ParameterParserUtil.parseIntegerParam(resultSet.getInt("exe.testCaseVersion"), 0);
        int testCasePriority = ParameterParserUtil.parseIntegerParam(resultSet.getInt("exe.testCasePriority"), 0);
        String robotProvider = ParameterParserUtil.parseStringParam(resultSet.getString("exe.RobotProvider"), "");
        String robotSessionId = ParameterParserUtil.parseStringParam(resultSet.getString("exe.RobotSessionId"), "");
        String robotProviderSessionId = ParameterParserUtil.parseStringParam(resultSet.getString("exe.RobotProviderSessionId"), "");
        String usrModif = ParameterParserUtil.parseStringParam(resultSet.getString("exe.UsrModif"), "");
        String usrCreated = ParameterParserUtil.parseStringParam(resultSet.getString("exe.UsrCreated"), "");
        Timestamp dateCreated = resultSet.getTimestamp("exe.DateCreated");
        Timestamp dateModif = resultSet.getTimestamp("exe.DateModif");
        TestCaseExecution result = factoryTCExecution.create(id, test, testcase, description, build, revision, environment,
                country, robot, robotExecutor, robotHost, robotPort, robotDecli, browser, version, platform, start, end, controlStatus, controlMessage, application, null, url,
                tag, 0, 0, 0, 0, 0, 0, true, "", "", status, crbVersion, null, null, null,
                0, null, null, null, environmentData, null, null, null, null, executor, 0, screenSize, null, robotProvider, robotSessionId,
                conditionOperator, conditionVal1Init, conditionVal2Init, conditionVal3Init, conditionVal1, conditionVal2, conditionVal3, manualExecution, userAgent, testCaseVersion, testCasePriority, system,
                usrCreated, dateCreated, usrModif, dateModif);
        result.setQueueID(queueId);
        result.setRobotProviderSessionID(robotProviderSessionId);
        return result;
    }

    private TestCaseExecution loadWithDependenciesFromResultSet(ResultSet resultSet) throws SQLException {
        TestCaseExecution testCaseExecution = new TestCaseExecution();
        testCaseExecution = this.loadFromResultSet(resultSet);
        testCaseExecution.setTestCaseObj(testCaseDAO.loadFromResultSet(resultSet));
        testCaseExecution.setApplicationObj(applicationDAO.loadFromResultSet(resultSet));
        return testCaseExecution;
    }

    private TestCaseExecution loadWithTestCaseFromResultSet(ResultSet resultSet) throws SQLException {
        TestCaseExecution testCaseExecution = new TestCaseExecution();
        testCaseExecution = this.loadFromResultSet(resultSet);
        testCaseExecution.setTestCaseObj(testCaseDAO.loadFromResultSet(resultSet));
        return testCaseExecution;
    }

    private TestCaseExecution loadTestCaseExecutionAndApplicationFromResultSet(ResultSet resultSet) throws SQLException {
        TestCaseExecution testCaseExecution = new TestCaseExecution();
        testCaseExecution = this.loadFromResultSet(resultSet);
        testCaseExecution.setApplicationObj(applicationDAO.loadFromResultSet(resultSet));
        return testCaseExecution;
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(List<String> system, String test, String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        AnswerList<String> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        List<String> individalColumnSearchValues = new ArrayList<>();

        final StringBuffer query = new StringBuffer();

        query.append("SELECT distinct ");
        query.append(columnName);
        query.append(" as distinctValues FROM testcaseexecution exe ");
        query.append("where 1=1 ");

        if (!StringUtil.isNullOrEmpty(searchParameter)) {
            query.append("and (exe.`id` like ? ");
            query.append(" or exe.`test` like ? ");
            query.append(" or exe.`testCase` like ? ");
            query.append(" or exe.`build` like ? ");
            query.append(" or exe.`revision` like ? ");
            query.append(" or exe.`environment` like ? ");
            query.append(" or exe.`country` like ? ");
            query.append(" or exe.`browser` like ? ");
            query.append(" or exe.`version` like ? ");
            query.append(" or exe.`platform` like ? ");
            query.append(" or exe.`browserfullversion` like ? ");
            query.append(" or exe.`start` like ? ");
            query.append(" or exe.`end` like ? ");
            query.append(" or exe.`controlstatus` like ? ");
            query.append(" or exe.`controlmessage` like ? ");
            query.append(" or exe.`application` like ? ");
            query.append(" or exe.`url` like ? ");
            query.append(" or exe.`robot` like ? ");
            query.append(" or exe.`robotexecutor` like ? ");
            query.append(" or exe.`robothost` like ? ");
            query.append(" or exe.`robotport` like ? ");
            query.append(" or exe.`tag` like ? ");
            query.append(" or exe.`finished` like ? ");
            query.append(" or exe.`status` like ? ");
            query.append(" or exe.`crbversion` like ? ");
            query.append(" or exe.`executor` like ? ");
            query.append(" or exe.`screensize` like ? )");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            query.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                query.append(" and ");
                query.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individalColumnSearchValues.addAll(entry.getValue());
            }
            query.append(" ) ");
        }

        query.append(" order by ").append(columnName).append(" asc");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());
                Statement stm = connection.createStatement();) {

            int i = 1;

            if (!Strings.isNullOrEmpty(searchParameter)) {
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
            }
            for (String individualColumnSearchValue : individalColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }

            try (ResultSet resultSet = preStat.executeQuery();
                    ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()");) {
                while (resultSet.next()) {
                    distinctValues.add(resultSet.getString("distinctValues") == null ? "" : resultSet.getString("distinctValues"));
                }

                //get the total number of rows
                int nrTotalRows = 0;

                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }

                if (distinctValues.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                } else if (distinctValues.size() <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            }
        } catch (Exception e) {
            LOG.warn("Unable to execute query : " + e.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            // We always set the result message
            answer.setResultMessage(msg);
        }
        answer.setResultMessage(msg);
        answer.setDataList(distinctValues);
        return answer;
    }

}
