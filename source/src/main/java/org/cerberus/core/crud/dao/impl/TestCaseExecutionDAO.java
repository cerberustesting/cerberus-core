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

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.IApplicationDAO;
import org.cerberus.core.crud.dao.ITestCaseDAO;
import org.cerberus.core.crud.dao.ITestCaseExecutionDAO;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecution;
import org.cerberus.core.crud.utils.RequestDbUtils;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.SqlUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.security.UserSecurity;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Repository
public class TestCaseExecutionDAO implements ITestCaseExecutionDAO {

    private final DatabaseSpring databaseSpring;
    private final IFactoryTestCaseExecution factoryTCExecution;
    private final IApplicationDAO applicationDAO;
    private final ITestCaseDAO testCaseDAO;

    private static final Logger LOG = LogManager.getLogger(TestCaseExecutionDAO.class);
    private static final String OBJECT_NAME = "TestCase Execution";
    private static final int MAX_ROW_SELECTED = 100000;

    @Override
    public long insertTCExecution(TestCaseExecution tCExecution) throws CerberusException {
        final StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcaseexecution(test, testcase, description, build, revision, environment, ");
        query.append("  environmentData, country, browser, application, robothost, ");
        query.append("  url, robotport, tag, status, start, controlstatus, controlMessage, crbversion, ");
        query.append("  executor, screensize, conditionOperator, conditionVal1Init, conditionVal2Init, ");
        query.append("  conditionVal3Init, conditionVal1, conditionVal2, conditionVal3, ");
        query.append("  manualExecution, UserAgent, queueId, testCaseVersion, TestCasePriority, TestCaseIsMuted, `system`, robotdecli, ");
        query.append("  robot, robotexecutor, RobotProvider, RobotProviderSessionId, RobotSessionId, UsrCreated) ");
        query.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.id : {}", tCExecution.getId());
        LOG.debug("SQL.param.robotexecutor : {}", tCExecution.getRobotExecutor());

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS)) {

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
            preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(tCExecution.getUrl(), 350), tCExecution.getSecrets()));
            preStat.setString(i++, tCExecution.getRobotPort());
            preStat.setString(i++, tCExecution.getTag());
            preStat.setString(i++, tCExecution.getStatus());
            preStat.setTimestamp(i++, new Timestamp(tCExecution.getStart()));
            preStat.setString(i++, tCExecution.getControlStatus());
            preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(tCExecution.getControlMessage(), 65000), tCExecution.getSecrets()));
            preStat.setString(i++, tCExecution.getCrbVersion());
            preStat.setString(i++, tCExecution.getExecutor());
            preStat.setString(i++, tCExecution.getScreenSize());
            preStat.setString(i++, tCExecution.getConditionOperator());
            preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(tCExecution.getConditionVal1Init(), 65000), tCExecution.getSecrets()));
            preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(tCExecution.getConditionVal2Init(), 65000), tCExecution.getSecrets()));
            preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(tCExecution.getConditionVal3Init(), 65000), tCExecution.getSecrets()));
            preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(tCExecution.getConditionVal1(), 65000), tCExecution.getSecrets()));
            preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(tCExecution.getConditionVal2(), 65000), tCExecution.getSecrets()));
            preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(tCExecution.getConditionVal3(), 65000), tCExecution.getSecrets()));
            preStat.setString(i++, tCExecution.getManualExecution());
            preStat.setString(i++, tCExecution.getUserAgent());
            preStat.setLong(i++, tCExecution.getQueueID());
            preStat.setInt(i++, tCExecution.getTestCaseVersion());
            preStat.setInt(i++, tCExecution.getTestCasePriority());
            preStat.setBoolean(i++, tCExecution.isTestCaseIsMuted());
            preStat.setString(i++, tCExecution.getSystem());
            preStat.setString(i++, tCExecution.getRobotDecli());
            preStat.setString(i++, tCExecution.getRobot());
            preStat.setString(i++, tCExecution.getRobotExecutor());
            preStat.setString(i++, tCExecution.getRobotProvider());
            preStat.setString(i++, tCExecution.getRobotProviderSessionID());
            preStat.setString(i++, tCExecution.getRobotSessionID());
            preStat.setString(i, tCExecution.getUsrCreated());
            preStat.executeUpdate();
            try (ResultSet resultSet = preStat.getGeneratedKeys()) {
                if (resultSet.first()) {
                    return resultSet.getLong(1);
                }
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
        }
        return 0;
    }

    @Override
    public void updateTCExecution(TestCaseExecution tCExecution) throws CerberusException {
        final StringBuilder query = new StringBuilder();
        query.append("UPDATE testcaseexecution SET ");
        query.append("  test = ?, testcase = ?, description = ?, build = ?, ");
        query.append("  revision = ?, environment = ?, environmentData = ?, country = ?, ");
        query.append("  browser = ?, application = ?, robothost = ?, url = ?, robotport = ?, tag = ?, status = ?, ");
        query.append("  start = ?, end = ? , durationMs = ? , controlstatus = ?, controlMessage = ?, crbversion = ?, ");
        query.append("  version = ?, platform = ?, executor = ?, screensize = ?, ");
        query.append("  conditionOperator = ?, ConditionVal1Init = ?, ConditionVal2Init = ?, ConditionVal3Init = ?, ");
        query.append("  ConditionVal1 = ?, ConditionVal2 = ?, ConditionVal3 = ?, ManualExecution = ?, UserAgent = ?, ");
        query.append("  queueId = ?, testCaseVersion = ?, testCasePriority = ?, testCaseIsMuted = ?, `system` = ?, ");
        query.append("  robotdecli = ?, robot = ?, robotexecutor = ?, RobotProvider = ?, RobotSessionId = ?, ");
        query.append("  RobotProviderSessionId = ?, UsrModif = ?, DateModif = NOW() ");
        query.append("WHERE id = ?");

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.id : {}", tCExecution.getId());

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(
                query.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

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
            preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(tCExecution.getUrl(), 350), tCExecution.getSecrets()));
            preStat.setString(i++, tCExecution.getRobotPort());
            preStat.setString(i++, tCExecution.getTag());
            preStat.setString(i++, tCExecution.getStatus());
            preStat.setTimestamp(i++, new Timestamp(tCExecution.getStart()));
            if (tCExecution.getEnd() != 0) {
                preStat.setTimestamp(i++, new Timestamp(tCExecution.getEnd()));
            } else {
                preStat.setString(i++, "1970-01-01 01:01:01");
            }
            preStat.setLong(i++, tCExecution.getDurationMs());
            preStat.setString(i++, tCExecution.getControlStatus());
            preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(tCExecution.getControlMessage(), 65000), tCExecution.getSecrets()));
            preStat.setString(i++, tCExecution.getCrbVersion());
            preStat.setString(i++, tCExecution.getVersion());
            preStat.setString(i++, tCExecution.getPlatform());
            preStat.setString(i++, tCExecution.getExecutor());
            preStat.setString(i++, tCExecution.getScreenSize());
            preStat.setString(i++, tCExecution.getConditionOperator());
            preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(tCExecution.getConditionVal1Init(), 65000), tCExecution.getSecrets()));
            preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(tCExecution.getConditionVal2Init(), 65000), tCExecution.getSecrets()));
            preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(tCExecution.getConditionVal3Init(), 65000), tCExecution.getSecrets()));
            preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(tCExecution.getConditionVal1(), 65000), tCExecution.getSecrets()));
            preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(tCExecution.getConditionVal2(), 65000), tCExecution.getSecrets()));
            preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(tCExecution.getConditionVal3(), 65000), tCExecution.getSecrets()));
            preStat.setString(i++, tCExecution.getManualExecution());
            preStat.setString(i++, tCExecution.getUserAgent());
            preStat.setLong(i++, tCExecution.getQueueID());
            preStat.setInt(i++, tCExecution.getTestCaseVersion());
            preStat.setInt(i++, tCExecution.getTestCasePriority());
            preStat.setBoolean(i++, tCExecution.isTestCaseIsMuted());
            preStat.setString(i++, tCExecution.getSystem());
            preStat.setString(i++, tCExecution.getRobotDecli());
            preStat.setString(i++, tCExecution.getRobot());
            preStat.setString(i++, tCExecution.getRobotExecutor());
            preStat.setString(i++, tCExecution.getRobotProvider());
            preStat.setString(i++, tCExecution.getRobotSessionID());
            preStat.setString(i++, tCExecution.getRobotProviderSessionID());
            preStat.setString(i++, tCExecution.getUsrModif());
            preStat.setLong(i, tCExecution.getId());
            preStat.executeUpdate();
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
        }
    }

    @Override
    public List<String> getIDListOfLastExecutions(String test, String testcase, String country) {
        List<String> list = null;
        final String query = "SELECT ID FROM testcaseexecution WHERE test = ? AND testcase = ? "
                + "AND country = ? AND controlStatus='OK' ORDER BY id DESC LIMIT 200";

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            preStat.setString(1, test);
            preStat.setString(2, testcase);
            preStat.setString(3, country);

            try (ResultSet resultSet = preStat.executeQuery()) {
                list = new ArrayList<>();
                while (resultSet.next()) {
                    list.add(resultSet.getString("ID"));
                }
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
        }
        return list;
    }

    @Override
    public AnswerItem<TestCaseExecution> readLastByCriteria(String application) {
        AnswerItem<TestCaseExecution> ans = new AnswerItem<>();
        TestCaseExecution result;
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        StringBuilder searchSQL = new StringBuilder();
        StringBuilder query = new StringBuilder();
        query.append("select * from testcaseexecution exe ");
        searchSQL.append(" where 1=1 ");
        if (StringUtil.isNotEmptyOrNull(application)) {
            searchSQL.append(" and (`application` = ? )");
        }
        query.append(searchSQL);
        query.append(" order by id DESC limit 1 ");

        LOG.debug("SQL : {}", query);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(
                query.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

            if (StringUtil.isNotEmptyOrNull(application)) {
                preStat.setString(1, application);
            }
            try (ResultSet resultSet = preStat.executeQuery()) {
                if (resultSet.first()) {
                    result = loadFromResultSet(resultSet);
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    ans.setItem(result);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                }
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public TestCaseExecution findLastTCExecutionByCriteria(String test, String testcase, String environment, String country,
            String build, String revision) throws CerberusException {
        TestCaseExecution result = null;
        final StringBuilder query = new StringBuilder()
                .append("SELECT exe.* FROM testcaseexecution exe ")
                .append("WHERE exe.test = ? AND exe.testcase = ? AND exe.environment = ? ")
                .append("AND exe.country = ? AND exe.build = ? AND exe.revision = ? ")
                .append("ORDER BY exe.id DESC");

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(
                query.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            int i = 1;
            preStat.setString(i++, test);
            preStat.setString(i++, testcase);
            preStat.setString(i++, environment);
            preStat.setString(i++, country);
            preStat.setString(i++, build);
            preStat.setString(i, revision);
            try (ResultSet resultSet = preStat.executeQuery()) {
                if (resultSet.first()) {
                    result = this.loadFromResultSet(resultSet);
                } else {
                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                }
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
        }
        return result;
    }

    @Override
    public TestCaseExecution findLastTCExecutionByCriteria(String test, String testCase, String environment, String country,
            String build, String revision, String browser, String browserVersion,
            String ip, String port, String tag) {
        TestCaseExecution result = null;
        final StringBuilder query = new StringBuilder()
                .append("SELECT exe.* FROM testcaseexecution exe ")
                .append("WHERE exe.test = ? AND exe.testcase = ? ")
                .append("AND exe.environment LIKE ? AND exe.country = ? AND exe.build LIKE ? ")
                .append("AND exe.revision LIKE ? AND exe.browser = ? AND exe.version LIKE ? ")
                .append("AND exe.ip LIKE ? AND exe.port LIKE ? AND exe.tag LIKE ? ")
                .append("ORDER BY exe.id DESC");

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(
                query.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            int i = 1;
            preStat.setString(i++, test);
            preStat.setString(i++, testCase);
            preStat.setString(i++, ParameterParserUtil.wildcardIfEmpty(environment));
            preStat.setString(i++, country);
            preStat.setString(i++, ParameterParserUtil.wildcardIfEmpty(build));
            preStat.setString(i++, ParameterParserUtil.wildcardIfEmpty(revision));
            preStat.setString(i++, browser);
            preStat.setString(i++, ParameterParserUtil.wildcardIfEmpty(browserVersion));
            preStat.setString(i++, ParameterParserUtil.wildcardIfEmpty(ip));
            preStat.setString(i++, ParameterParserUtil.wildcardIfEmpty(port));
            preStat.setString(i, ParameterParserUtil.wildcardIfEmpty(tag));
            try (ResultSet resultSet = preStat.executeQuery()) {
                if (resultSet.first()) {
                    result = this.loadFromResultSet(resultSet);
                }
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
        }
        return result;
    }

    @Override
    public List<TestCaseExecution> findExecutionByCriteria1(String dateLimit, String test, String testCase,
            String application, String country, String environment,
            String controlStatus, String status) {
        List<TestCaseExecution> myTestCaseExecutions = null;
        TestCaseExecution execution;
        final StringBuilder query = new StringBuilder()
                .append("SELECT exe.*, tec.*, app.* FROM testcaseexecution exe ")
                .append("LEFT JOIN testcase tec ON exe.test = tec.test AND exe.testcase = tec.testcase ")
                .append("LEFT JOIN application app ON exe.application = app.application ")
                .append("WHERE exe.start > ? AND exe.test LIKE ? AND exe.testcase LIKE ? AND exe.environment LIKE ? ")
                .append("AND exe.country LIKE ? AND exe.application LIKE ? AND exe.controlstatus LIKE ? ")
                .append("AND exe.status LIKE ?");

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(
                query.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            int i = 1;
            preStat.setString(i++, dateLimit);
            preStat.setString(i++, test);
            preStat.setString(i++, testCase);
            preStat.setString(i++, environment);
            preStat.setString(i++, country);
            preStat.setString(i++, application);
            preStat.setString(i++, controlStatus);
            preStat.setString(i, status);

            try (ResultSet resultSet = preStat.executeQuery()) {
                if (!resultSet.first()) {
                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
                } else {
                    myTestCaseExecutions = new ArrayList<>();
                    while (resultSet.next()) {
                        execution = this.loadWithDependenciesFromResultSet(resultSet);

                        myTestCaseExecutions.add(execution);
                    }
                }
            }
        } catch (Exception exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
        }
        return myTestCaseExecutions;
    }

    @Override
    public TestCaseExecution findTCExecutionByKey(long id) {
        TestCaseExecution result = null;
        final String query = "SELECT * FROM testcaseexecution exe, application app "
                + "WHERE exe.application = app.application AND ID = ?";

        LOG.debug("SQL : {}", query);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(
                query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            preStat.setLong(1, id);
            try (ResultSet resultSet = preStat.executeQuery()) {
                if (resultSet.first()) {
                    result = this.loadTestCaseExecutionAndApplicationFromResultSet(resultSet);
                }
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
        }
        return result;
    }

    @Override
    public TestCaseExecution findLastTestCaseExecutionNotPE(String test, String testCase) {
        TestCaseExecution result = null;
        StringBuilder query = new StringBuilder()
                .append("SELECT exe.*  FROM `testcaseexecution` exe ")
                .append(" WHERE Test = ? and TestCase= ? and ID = ")
                .append(" (SELECT MAX(ID) from `testcaseexecution` ")
                .append("WHERE Test= ? and TestCase= ? and ControlStatus!='PE')");

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(
                query.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            int i = 1;
            preStat.setString(i++, test);
            preStat.setString(i++, testCase);
            preStat.setString(i++, test);
            preStat.setString(i, testCase);
            try (ResultSet resultSet = preStat.executeQuery()) {
                if (resultSet.first()) {
                    result = loadFromResultSet(resultSet);
                }
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
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
        if (StringUtil.isNotEmptyOrNULLString(environment)) {
            query.append("AND exe.environment IN (");
            query.append(environment);
            query.append(") ");
        }
        if (StringUtil.isNotEmptyOrNULLString(build)) {
            query.append("AND exe.build IN (");
            query.append(build);
            query.append(") ");
        }
        if (StringUtil.isNotEmptyOrNULLString(revision)) {
            query.append("AND exe.revision IN (");
            query.append(revision);
            query.append(") ");
        }
        if (StringUtil.isNotEmptyOrNULLString(browserVersion)) {
            query.append("AND exe.version LIKE ? ");
        }
        if (StringUtil.isNotEmptyOrNULLString(ip)) {
            query.append("AND exe.ip LIKE ? ");
        }
        if (StringUtil.isNotEmptyOrNULLString(port)) {
            query.append("AND exe.port LIKE ? ");
        }
        if (StringUtil.isNotEmptyOrNULLString(tag)) {
            query.append("AND exe.tag LIKE ? ");
        }
        query.append("ORDER BY exe.id DESC");

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(
                query.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            int i = 1;
            preStat.setString(i++, test);
            preStat.setString(i++, testCase);
            preStat.setString(i++, country);
            preStat.setString(i++, browser);
            if (StringUtil.isNotEmptyOrNULLString(browserVersion)) {
                preStat.setString(i++, browserVersion);
            }
            if (StringUtil.isNotEmptyOrNULLString(ip)) {
                preStat.setString(i++, ip);
            }
            if (StringUtil.isNotEmptyOrNULLString(port)) {
                preStat.setString(i++, port);
            }
            if (StringUtil.isNotEmptyOrNULLString(tag)) {
                preStat.setString(i, tag);
            }
            try (ResultSet resultSet = preStat.executeQuery()) {
                if (resultSet.first()) {
                    result = this.loadFromResultSet(resultSet);
                }
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
        }
        return result;
    }

    @Override
    public List<String> findDistinctTag(boolean withUUIDTag) {
        List<String> list = null;
        StringBuilder query = new StringBuilder()
                .append("select distinct tag from testcaseexecution exe ")
                .append("where tag != '' ");
        if (!withUUIDTag) {
            query.append(" and length(tag) != length('c3888898-c65a-11e3-9b3e-0000004047e0')");
        }
        query.append(" UNION select distinct tag from testcaseexecutionqueue where tag !='' ");
        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); ResultSet resultSet = preStat.executeQuery()) {
            list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(resultSet.getString("tag"));
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
        }
        return list;
    }

    @Override
    public void setTagToExecution(long id, String tag) throws CerberusException {
        final String query = "UPDATE testcaseexecution exe SET exe.tag = ? WHERE exe.id = ?";
        LOG.debug("SQL : {}", query);
        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {
            preStat.setString(1, tag);
            preStat.setLong(2, id);
            preStat.executeUpdate();

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void updateFalseNegative(long id, boolean falseNegative, String usrModif) throws CerberusException {
        final String query = "UPDATE testcaseexecution exe SET exe.FalseNegative = ?, dateModif = NOW(), usrModif= ? WHERE exe.id = ?";
        LOG.debug("SQL : {}", query);
        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {
            preStat.setBoolean(1, falseNegative);
            preStat.setString(2, usrModif);
            preStat.setLong(3, id);
            preStat.executeUpdate();

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void updateLastAndFlaky(long id, boolean last, boolean flaky, String usrModif) throws CerberusException {
        final String query = "UPDATE testcaseexecution exe SET exe.IsUseful = ?, exe.IsFlaky = ?, dateModif = NOW(), usrModif= ? WHERE exe.id = ?";
        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.id : {}", id);
        LOG.debug("SQL.param.last : {}", last);
        LOG.debug("SQL.param.falcky : {}", flaky);
        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {
            preStat.setBoolean(1, last);
            preStat.setBoolean(2, flaky);
            preStat.setString(3, usrModif);
            preStat.setLong(4, id);
            preStat.executeUpdate();

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public List<TestCaseExecution> readByTagByCriteria(String tag, int start, int amount, String sort,
            String searchTerm, Map<String, List<String>> individualSearch) throws CerberusException {
        List<String> individualColumnSearchValues = new ArrayList<>();

        final StringBuilder query = new StringBuilder()
                .append("SELECT * FROM testcaseexecution exe ")
                .append("left join testcase tec on exe.Test = tec.Test and exe.TestCase = tec.TestCase ")
                .append("left join application app on tec.application = app.application ")
                .append("where exe.ID IN ")
                .append("(select MAX(exe.ID) from testcaseexecution exe ")
                .append("where 1=1 ");
        if (StringUtil.isNotEmptyOrNull(tag)) {
            query.append("and exe.tag = ? ");
        }
        query.append("group by exe.test, exe.testcase, exe.Environment, exe.Browser, exe.Country) ");
        if (StringUtil.isNotEmptyOrNull(searchTerm)) {
            query.append("and (exe.`test` like ? ");
            query.append(" or exe.`testCase` like ? ");
            query.append(" or exe.`application` like ? ");
            query.append(" or tec.`bugs` like ? ");
            query.append(" or tec.`priority` like ? ");
            query.append(" or tec.`description` like ? )");
        }
        if (MapUtils.isNotEmpty(individualSearch)) {
            query.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                query.append(" and ");
                query.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individualColumnSearchValues.addAll(entry.getValue());
            }
            query.append(" ) ");
        }
        if (StringUtil.isNotEmptyOrNull(sort)) {
            query.append(" order by ").append(sort);
        }
        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }
        LOG.debug("SQL.param.tag : {}", tag);
        return RequestDbUtils.executeQueryList(databaseSpring, query.toString(),
                preStat -> {
                    int i = 1;
                    if (StringUtil.isNotEmptyOrNull(tag)) {
                        preStat.setString(i++, tag);
                    }

                    if (StringUtil.isNotEmptyOrNull(searchTerm)) {
                        preStat.setString(i++, "%" + searchTerm + "%");
                        preStat.setString(i++, "%" + searchTerm + "%");
                        preStat.setString(i++, "%" + searchTerm + "%");
                        preStat.setString(i++, "%" + searchTerm + "%");
                        preStat.setString(i++, "%" + searchTerm + "%");
                        preStat.setString(i++, "%" + searchTerm + "%");
                    }
                    for (String individualColumnSearchValue : individualColumnSearchValues) {
                        preStat.setString(i++, individualColumnSearchValue);
                    }
                },
                this::loadWithDependenciesFromResultSet
        );
    }

    @Override
    public AnswerList<TestCaseExecution> readByCriteria(List<String> systems, List<String> countries, List<String> environments,
            List<String> robotDeclis, List<TestCase> testcases, Date from, Date to) {
        AnswerList<TestCaseExecution> response = new AnswerList<>();
        List<TestCaseExecution> objectList = new ArrayList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        Timestamp t1;

        StringBuilder searchSQL = new StringBuilder();
        StringBuilder query = new StringBuilder();
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM testcaseexecution exe ");
        searchSQL.append(" where 1=1 ");

        if (CollectionUtils.isNotEmpty(systems)) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`System`", systems));
        }
        if (CollectionUtils.isNotEmpty(countries)) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`Country`", countries));
        }
        if (CollectionUtils.isNotEmpty(environments)) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`Environment`", environments));
        }
        if (CollectionUtils.isNotEmpty(robotDeclis)) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`RobotDecli`", robotDeclis));
        }
        searchSQL.append(" and start >= ? and start <= ? ");
        StringBuilder testcaseSQL = new StringBuilder();
        testcases.forEach(testCase -> testcaseSQL.append(" (test = ? and testcase = ?) or "));
        if (StringUtil.isNotEmptyOrNull(testcaseSQL.toString())) {
            searchSQL.append("and (").append(testcaseSQL).append(" (0=1) ").append(")");
        }
        query.append(searchSQL);
        query.append(" limit ").append(MAX_ROW_SELECTED);

        LOG.debug("SQL : {}", query);
        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); Statement stm = connection.createStatement()) {

            int i = 1;
            if (CollectionUtils.isNotEmpty(systems)) {
                for (String system : systems) {
                    preStat.setString(i++, system);
                }
            }
            if (CollectionUtils.isNotEmpty(countries)) {
                for (String country : countries) {
                    preStat.setString(i++, country);
                }
            }
            if (CollectionUtils.isNotEmpty(environments)) {
                for (String environment : environments) {
                    preStat.setString(i++, environment);
                }
            }
            if (CollectionUtils.isNotEmpty(robotDeclis)) {
                for (String robotDecli : robotDeclis) {
                    preStat.setString(i++, robotDecli);
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

            try (ResultSet resultSet = preStat.executeQuery(); ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {

                while (resultSet.next()) {
                    objectList.add(this.loadFromResultSet(resultSet));
                }

                int nrTotalRows = 0;
                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }

                if (objectList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                } else if (objectList.isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                }
                response = new AnswerList<>(objectList, nrTotalRows);
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        response.setResultMessage(msg);
        response.setDataList(objectList);
        return response;
    }

    @Override
    public AnswerList<TestCaseExecution> readByCriteria(List<String> systems, List<String> tags, Date from, Date to) {
        AnswerList<TestCaseExecution> response = new AnswerList<>();
        List<TestCaseExecution> objectList = new ArrayList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        Timestamp t1;

        StringBuilder searchSQL = new StringBuilder();
        StringBuilder query = new StringBuilder();
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM testcaseexecution exe ");
        searchSQL.append(" where 1=1 ");

        if (CollectionUtils.isNotEmpty(systems)) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`System`", systems));
        }
        if (CollectionUtils.isNotEmpty(tags)) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`Tag`", tags));
        }
        searchSQL.append(" and start >= ? and start <= ? ");
        searchSQL.append(" and isUseful = true ");
        query.append(searchSQL);
        query.append(" limit ").append(MAX_ROW_SELECTED);

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.systems : {}", systems);
        LOG.debug("SQL.param.tags : {}", tags);
        LOG.debug("SQL.param.from : {}", from);
        LOG.debug("SQL.param.to : {}", to);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); Statement stm = connection.createStatement()) {

            int i = 1;
            if (CollectionUtils.isNotEmpty(systems)) {
                for (String system : systems) {
                    preStat.setString(i++, system);
                }
            }
            if (CollectionUtils.isNotEmpty(tags)) {
                for (String tag : tags) {
                    preStat.setString(i++, tag);
                }
            }
            t1 = new Timestamp(from.getTime());
            preStat.setTimestamp(i++, t1);
            t1 = new Timestamp(to.getTime());
            preStat.setTimestamp(i++, t1);

            try (ResultSet resultSet = preStat.executeQuery(); ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {

                while (resultSet.next()) {
                    objectList.add(this.loadFromResultSet(resultSet));
                }

                int nrTotalRows = 0;
                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }

                if (objectList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                } else if (objectList.isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                }
                response = new AnswerList<>(objectList, nrTotalRows);
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        response.setResultMessage(msg);
        response.setDataList(objectList);
        return response;
    }

    @Override
    public Integer getNbExecutions(List<String> systems) {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        StringBuilder query = new StringBuilder();

        query.append("SELECT SQL_CALC_FOUND_ROWS count(*)  FROM testcaseexecution exe ");
        query.append(" where 1=1 ");

        if (CollectionUtils.isNotEmpty(systems)) {
            query.append(" and ");
            query.append(SqlUtil.generateInClause("`System`", systems));
        }

        LOG.debug("SQL : {}", query);
        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); Statement stm = connection.createStatement()) {

            int i = 1;
            if (CollectionUtils.isNotEmpty(systems)) {
                for (String system : systems) {
                    preStat.setString(i++, system);
                }
            }

            try (ResultSet resultSet = preStat.executeQuery(); ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {

                while (resultSet.next()) {
                    return resultSet.getInt(1);
                }

            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        return 0;
    }

    @Override
    public AnswerList<TestCaseExecution> readByTag(String tag) {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        AnswerList<TestCaseExecution> answer = new AnswerList<>();

        final StringBuilder query = new StringBuilder()
                .append("SELECT * FROM testcaseexecution exe ")
                .append("left join testcase tec on exe.Test = tec.Test and exe.TestCase = tec.TestCase ")
                .append("left join application as app on tec.application = app.application ")
                .append("where 1=1 and exe.tag = ? ");

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.tag : {}", tag);
        List<TestCaseExecution> testCaseExecutionList = new ArrayList<>();

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString())) {
            preStat.setString(1, tag);

            try (ResultSet resultSet = preStat.executeQuery()) {
                while (resultSet.next()) {
                    testCaseExecutionList.add(this.loadWithDependenciesFromResultSet(resultSet));
                }
                msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecution").replace("%OPERATION%", "SELECT"));
                answer.setTotalRows(testCaseExecutionList.size());
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            testCaseExecutionList = null;
        }
        answer.setResultMessage(msg);
        answer.setDataList(testCaseExecutionList);
        return answer;
    }

    @Override
    public Integer readNbByTag(String tag) {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        int result = 0;
        final StringBuilder query = new StringBuilder()
                .append("SELECT count(*) FROM testcaseexecution exe ")
                .append("where 1=1 and exe.tag = ? ");

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.tag : {}", tag);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString())) {
            preStat.setString(1, tag);
            try (ResultSet resultSet = preStat.executeQuery()) {
                while (resultSet.next()) {
                    result = resultSet.getInt(1);
                }
                msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecution").replace("%OPERATION%", "SELECT"));
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        }
        return result;
    }

    @Override
    public AnswerList<TestCaseExecution> readByCriteria(int start, int amount, String sort, String searchTerm,
            Map<String, List<String>> individualSearch, List<String> individualLike,
            List<String> systems) throws CerberusException {
        MessageEvent msg;
        AnswerList<TestCaseExecution> response = new AnswerList<>();
        List<String> individualColumnSearchValues = new ArrayList<>();
        List<TestCaseExecution> objectList = new ArrayList<>();

        final StringBuilder query = new StringBuilder()
                .append("SELECT SQL_CALC_FOUND_ROWS * FROM testcaseexecution exe ")
                .append("where 1=1 ");

        if (StringUtil.isNotEmptyOrNull(searchTerm)) {
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
        if (MapUtils.isNotEmpty(individualSearch)) {
            query.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                query.append(" and ");
                query.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individualColumnSearchValues.addAll(entry.getValue());
            }
            query.append(" ) ");
        }
        if (CollectionUtils.isNotEmpty(systems)) {
            query.append(" and ").append(SqlUtil.generateInClause("exe.`system`", systems)).append(" ");
        }
        query.append(" AND ").append(UserSecurity.getSystemAllowForSQL("exe.`system`"));
        if (StringUtil.isNotEmptyOrNull(sort)) {
            query.append(" order by ").append(sort);
        }
        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }
        LOG.debug("SQL : {}", query);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); Statement stm = connection.createStatement()) {

            int i = 1;
            if (StringUtil.isNotEmptyOrNull(searchTerm)) {
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
            for (String individualColumnSearchValue : individualColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }
            if (CollectionUtils.isNotEmpty(systems)) {
                for (String system : systems) {
                    preStat.setString(i++, system);
                }
            }

            try (ResultSet resultSet = preStat.executeQuery(); ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {

                while (resultSet.next()) {
                    objectList.add(this.loadFromResultSet(resultSet));
                }

                int nrTotalRows = 0;
                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }

                if (objectList.size() >= MAX_ROW_SELECTED) {
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                } else if (objectList.isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                }
                response = new AnswerList<>(objectList, nrTotalRows);
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }

        response.setResultMessage(msg);
        response.setDataList(objectList);
        return response;
    }

    @Override
    public AnswerList<TestCaseExecution> readDistinctEnvCountryBrowserByTag(String tag) {
        AnswerList<TestCaseExecution> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);

        StringBuilder query = new StringBuilder()
                .append("SELECT exe.* FROM testcaseexecution exe WHERE exe.tag = ? ")
                .append("GROUP BY exe.Environment, exe.Country, exe.Browser, exe.ControlStatus");

        List<TestCaseExecution> testCaseExecutionList = new ArrayList<>();

        LOG.debug("SQL : {}", query);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString())) {
            preStat.setString(1, tag);
            try (ResultSet resultSet = preStat.executeQuery()) {
                while (resultSet.next()) {
                    testCaseExecutionList.add(this.loadFromResultSet(resultSet));
                }
                msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecution").replace("%OPERATION%", "SELECT"));
                answer = new AnswerList<>(testCaseExecutionList, testCaseExecutionList.size());
            }
        } catch (SQLException ex) {
            LOG.warn(ex.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        }
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerItem<TestCaseExecution> readByKey(long executionId) {
        AnswerItem<TestCaseExecution> ans = new AnswerItem<>();
        TestCaseExecution result;
        final String query = "SELECT * FROM `testcaseexecution` exe WHERE exe.`id` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        LOG.debug("SQL : {}", query);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(
                query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            preStat.setLong(1, executionId);
            try (ResultSet resultSet = preStat.executeQuery()) {
                if (resultSet.first()) {
                    result = loadFromResultSet(resultSet);
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    ans.setItem(result);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                }
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public AnswerItem<TestCaseExecution> readLastByCriteria(String test, String testCase, String country, String environment, String tag) {
        AnswerItem<TestCaseExecution> ans = new AnswerItem<>();
        TestCaseExecution result;
        final String query = "select * from testcaseexecution exe where Test=? and TestCase=? and country=? and Environment=?  and tag=? order by id desc limit 1;";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.test : {}", test);
        LOG.debug("SQL.param.testcase : {}", testCase);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(
                query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            int i = 1;
            preStat.setString(i++, test);
            preStat.setString(i++, testCase);
            preStat.setString(i++, country);
            preStat.setString(i++, environment);
            preStat.setString(i++, tag);
            try (ResultSet resultSet = preStat.executeQuery()) {
                if (resultSet.first()) {
                    result = loadFromResultSet(resultSet);
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    ans.setItem(result);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                }
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(List<String> system, String test, String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        AnswerList<String> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        List<String> individualColumnSearchValues = new ArrayList<>();

        final StringBuilder query = new StringBuilder()
                .append("SELECT distinct ")
                .append(columnName)
                .append(" as distinctValues FROM testcaseexecution exe ")
                .append("where 1=1 ");

        if (StringUtil.isNotEmptyOrNull(searchParameter)) {
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
        if (MapUtils.isNotEmpty(individualSearch)) {
            query.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                query.append(" and ");
                query.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individualColumnSearchValues.addAll(entry.getValue());
            }
            query.append(" ) ");
        }
        query.append(" order by ").append(columnName).append(" asc");
        LOG.debug("SQL : {}", query);
        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); Statement stm = connection.createStatement()) {

            int i = 1;
            if (StringUtil.isNotEmptyOrNull(searchParameter)) {
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
            for (String individualColumnSearchValue : individualColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }
            try (ResultSet resultSet = preStat.executeQuery(); ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {
                while (resultSet.next()) {
                    distinctValues.add(resultSet.getString("distinctValues") == null ? "" : resultSet.getString("distinctValues"));
                }
                int nrTotalRows = 0;
                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }

                if (distinctValues.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                } else if (distinctValues.isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                }
            }
        } catch (Exception e) {
            LOG.error("Unable to execute query : {}", e.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        }
        answer.setResultMessage(msg);
        answer.setDataList(distinctValues);
        return answer;
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
        boolean falseNegative = resultSet.getBoolean("exe.FalseNegative");
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
        int testCaseVersion = resultSet.getInt("exe.testCaseVersion");
        int testCasePriority = resultSet.getInt("exe.testCasePriority");
        boolean testCaseIsMuted = resultSet.getBoolean("exe.testCaseIsMuted");
        boolean isFlaky = resultSet.getBoolean("exe.IsFlaky");
        boolean isUseful = resultSet.getBoolean("exe.IsUseful");
        long durationMs = ParameterParserUtil.parseLongParam(resultSet.getString("exe.DurationMs"), 0);
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
        result.setFlaky(isFlaky);
        result.setUseful(isUseful);
        result.setDurationMs(durationMs);
        result.setQueueID(queueId);
        result.setRobotProviderSessionID(robotProviderSessionId);
        result.setFalseNegative(falseNegative);
        result.setTestCaseIsMuted(testCaseIsMuted);

        return result;
    }

    private TestCaseExecution loadWithDependenciesFromResultSet(ResultSet resultSet) throws SQLException {
        TestCaseExecution testCaseExecution;
        testCaseExecution = this.loadFromResultSet(resultSet);
        testCaseExecution.setTestCaseObj(testCaseDAO.loadFromResultSet(resultSet));
        testCaseExecution.setApplicationObj(applicationDAO.loadFromResultSet(resultSet));
        return testCaseExecution;
    }

    private TestCaseExecution loadTestCaseExecutionAndApplicationFromResultSet(ResultSet resultSet) throws SQLException {
        TestCaseExecution testCaseExecution;
        testCaseExecution = this.loadFromResultSet(resultSet);
        testCaseExecution.setApplicationObj(applicationDAO.loadFromResultSet(resultSet));
        return testCaseExecution;
    }
}
