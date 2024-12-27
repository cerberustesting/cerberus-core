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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.core.crud.dao.ITestCaseStepExecutionDAO;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.crud.entity.TestCaseStepExecution;
import org.cerberus.core.crud.factory.IFactoryTestCaseStepExecution;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.DateUtil;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 03/01/2013
 * @since 0.9.0
 */
@Repository
public class TestCaseStepExecutionDAO implements ITestCaseStepExecutionDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseStepExecution factoryTestCaseStepExecution;

    private static final Logger LOG = LogManager.getLogger(TestCaseStepExecutionDAO.class);

    @Override
    public void insertTestCaseStepExecution(TestCaseStepExecution stepExecution, HashMap<String, String> secrets) {
        final String query = "INSERT INTO testcasestepexecution(id, test, testcase, step, `index`, sort, `loop`, batnumexe, returncode, start, fullstart, end, fullend, "
                + "returnMessage, description, conditionOperator, conditionVal1Init, conditionVal2Init, conditionVal3Init, conditionVal1, conditionVal2, conditionVal3) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.id : " + stepExecution.getId());
            LOG.debug("SQL.param.test : " + stepExecution.getTest());
            LOG.debug("SQL.param.testcase : " + stepExecution.getTestCase());
            LOG.debug("SQL.param.step : " + stepExecution.getStepId());
            LOG.debug("SQL.param.index : " + stepExecution.getIndex());
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setLong(i++, stepExecution.getId());
                preStat.setString(i++, stepExecution.getTest());
                preStat.setString(i++, stepExecution.getTestCase());
                preStat.setInt(i++, stepExecution.getStepId());
                preStat.setInt(i++, stepExecution.getIndex());
                preStat.setInt(i++, stepExecution.getSort());
                preStat.setString(i++, stepExecution.getLoop());
                preStat.setString(i++, stepExecution.getBatNumExe());
                preStat.setString(i++, stepExecution.getReturnCode());
                preStat.setTimestamp(i++, new Timestamp(stepExecution.getStart()));
                DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_TIMESTAMP);
                preStat.setString(i++, df.format(stepExecution.getStart()));
                preStat.setTimestamp(i++, new Timestamp(stepExecution.getEnd()));
                preStat.setString(i++, df.format(stepExecution.getEnd()));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(stepExecution.getReturnMessage(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(stepExecution.getDescription(), secrets));
                preStat.setString(i++, stepExecution.getConditionOperator());
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(stepExecution.getConditionValue1Init(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(stepExecution.getConditionValue2Init(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(stepExecution.getConditionValue3Init(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(stepExecution.getConditionValue1(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(stepExecution.getConditionValue2(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(stepExecution.getConditionValue3(), 65000), secrets));

                preStat.executeUpdate();

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
    }

    @Override
    public void updateTestCaseStepExecution(TestCaseStepExecution stepExecution, HashMap<String, String> secrets) {
        final String query = "UPDATE testcasestepexecution SET returncode = ?, start = ?, fullstart = ?, end = ?, fullend = ?, timeelapsed = ?, "
                + "returnmessage = ?, description = ?, sort = ?, `loop` = ?, conditionOperator = ?, conditionVal1Init = ?, conditionVal2Init = ?, conditionVal3Init = ?, "
                + "conditionVal1 = ?, conditionVal2 = ?, conditionVal3 = ? "
                + "WHERE id = ? AND step = ? AND `index` = ? AND test = ? AND testcase = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.id : " + stepExecution.getId());
            LOG.debug("SQL.param.test : " + stepExecution.getTest());
            LOG.debug("SQL.param.testcase : " + stepExecution.getTestCase());
            LOG.debug("SQL.param.step : " + stepExecution.getStepId());
            LOG.debug("SQL.param.index : " + stepExecution.getIndex());
        }

        Connection connection = this.databaseSpring.connect();
        try {
            Timestamp timeStart = new Timestamp(stepExecution.getStart());
            Timestamp timeEnd = new Timestamp(stepExecution.getEnd());

            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_TIMESTAMP);
                preStat.setString(i++, ParameterParserUtil.parseStringParam(stepExecution.getReturnCode(), ""));
                preStat.setTimestamp(i++, timeStart);
                preStat.setString(i++, df.format(timeStart));
                preStat.setTimestamp(i++, timeEnd);
                preStat.setString(i++, df.format(timeEnd));
                preStat.setFloat(i++, (timeEnd.getTime() - timeStart.getTime()) / (float) 1000);
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(stepExecution.getReturnMessage(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(stepExecution.getDescription(), secrets));
                preStat.setInt(i++, stepExecution.getSort());
                preStat.setString(i++, stepExecution.getLoop());
                preStat.setString(i++, stepExecution.getConditionOperator());
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(stepExecution.getConditionValue1Init(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(stepExecution.getConditionValue2Init(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(stepExecution.getConditionValue3Init(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(stepExecution.getConditionValue1(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(stepExecution.getConditionValue2(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(stepExecution.getConditionValue3(), 65000), secrets));
                preStat.setLong(i++, stepExecution.getId());
                preStat.setInt(i++, stepExecution.getStepId());
                preStat.setInt(i++, stepExecution.getIndex());
                preStat.setString(i++, stepExecution.getTest());
                preStat.setString(i++, stepExecution.getTestCase());

                preStat.executeUpdate();
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
    }

    @Override
    public List<TestCaseStepExecution> findTestCaseStepExecutionById(long id) {
        List<TestCaseStepExecution> result = null;
        TestCaseStepExecution resultData;
        final String query = "SELECT * FROM testcasestepexecution WHERE id = ? ORDER BY fullstart, sort";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, String.valueOf(id));

                ResultSet resultSet = preStat.executeQuery();
                result = new ArrayList<>();
                try {
                    while (resultSet.next()) {
                        result.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        return result;
    }

    @Override
    public AnswerList<TestCaseStepExecution> readByVarious1(long executionId, String test, String testcase) {
        MessageEvent msg;
        AnswerList<TestCaseStepExecution> answer = new AnswerList<>();
        List<TestCaseStepExecution> list = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcasestepexecution a ");
        query.append("where 1=1 and id = ? ");
        if (!(StringUtil.isEmptyOrNull(test))) {
            query.append("and test = ? ");
        }
        if (!(StringUtil.isEmptyOrNull(testcase))) {
            query.append("and testcase = ? ");
        }
        query.append(" order by start ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param.id : " + executionId);
            LOG.debug("SQL.param.test : " + test);
            LOG.debug("SQL.param.testcase : " + testcase);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                preStat.setLong(i++, executionId);
                if (!(StringUtil.isEmptyOrNull(test))) {
                    preStat.setString(i++, test);
                }
                if (!(StringUtil.isEmptyOrNull(testcase))) {
                    preStat.setString(i++, testcase);
                }
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadFromResultSet(resultSet));
                    }
                    if (list.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                    list.clear();
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
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        answer.setTotalRows(list.size());
        answer.setDataList(list);
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public TestCaseStepExecution loadFromResultSet(ResultSet resultSet) throws SQLException {
        long id = resultSet.getInt("id");
        String test = resultSet.getString("test");
        String testcase = resultSet.getString("testcase");
        int stepId = resultSet.getInt("step");
        int index = resultSet.getInt("index");
        int sort = resultSet.getInt("sort");
        String loop = resultSet.getString("loop");
        String conditionOperator = resultSet.getString("conditionOperator");
        String conditionValue1 = resultSet.getString("conditionVal1");
        String conditionValue2 = resultSet.getString("conditionVal2");
        String conditionValue3 = resultSet.getString("conditionVal3");
        String conditionValue1Init = resultSet.getString("conditionVal1Init");
        String conditionValue2Init = resultSet.getString("conditionVal2Init");
        String conditionValue3Init = resultSet.getString("conditionVal3Init");
        String batNumExe = resultSet.getString("batnumexe");
        long start = resultSet.getTimestamp("start") == null ? 0 : resultSet.getTimestamp("start").getTime();
        long end = resultSet.getTimestamp("end") == null ? 0 : resultSet.getTimestamp("end").getTime();
        long fullstart = resultSet.getLong("fullstart");
        long fullend = resultSet.getLong("Fullend");
        BigDecimal timeelapsed = resultSet.getBigDecimal("timeelapsed");
        String returnCode = resultSet.getString("returncode");
        String returnMessage = resultSet.getString("returnMessage");
        String description = resultSet.getString("description");
        return factoryTestCaseStepExecution.create(id, test, testcase, stepId, index, sort, loop, conditionOperator, conditionValue1Init, conditionValue2Init, conditionValue3Init, conditionValue1, conditionValue2, conditionValue3, batNumExe, start, end, fullstart, fullend, timeelapsed, returnCode, returnMessage, description);
    }
}
