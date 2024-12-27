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
import org.cerberus.core.crud.dao.ITestCaseStepActionExecutionDAO;
import org.cerberus.core.crud.entity.Test;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.crud.entity.TestCaseStepActionExecution;
import org.cerberus.core.crud.factory.IFactoryTestCaseStepActionExecution;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.DateUtil;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TestCaseStepActionExecutionDAO implements ITestCaseStepActionExecutionDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseStepActionExecution factoryTestCaseStepActionExecution;

    private static final Logger LOG = LogManager.getLogger(TestCaseStepActionExecutionDAO.class);

    private final String OBJECT_NAME = "TestCaseStepActionExecution";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public List<TestCaseStepActionExecution> findTestCaseStepActionExecutionByCriteria(long id, String test, String testCase, int stepId, int index) {
        List<TestCaseStepActionExecution> result = null;
        TestCaseStepActionExecution resultData;
        final String query = "SELECT * FROM testcasestepactionexecution exa WHERE exa.id = ? AND exa.test = ? AND exa.testcase = ? AND exa.step = ? AND exa.index = ? ORDER BY exa.sort";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, String.valueOf(id));
                preStat.setString(2, test);
                preStat.setString(3, testCase);
                preStat.setInt(4, stepId);
                preStat.setInt(5, index);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    result = new ArrayList<>();

                    while (resultSet.next()) {
                        result.add(this.loadFromResultset(resultSet));
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
    public AnswerList<TestCaseStepActionExecution> readByVarious1(long executionId, String test, String testCase, int stepId, int index) {
        MessageEvent msg;
        AnswerList<TestCaseStepActionExecution> answer = new AnswerList<>();
        List<TestCaseStepActionExecution> list = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcasestepactionexecution exa ");
        query.append("where exa.id = ? and exa.test = ? and exa.testcase = ? and exa.step = ? and exa.index = ? order by `sort`");
        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setLong(1, executionId);
                preStat.setString(2, test);
                preStat.setString(3, testCase);
                preStat.setInt(4, stepId);
                preStat.setInt(5, index);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadFromResultset(resultSet));
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
    public AnswerItem<TestCaseStepActionExecution> readByKey(long executionId, String test, String testCase, int stepId, int index, int sequence) {
        MessageEvent msg;
        AnswerItem<TestCaseStepActionExecution> answer = new AnswerItem<>();
        TestCaseStepActionExecution tcsa = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcasestepactionexecution exa ");
        query.append("where exa.id = ? and exa.test = ? and exa.testcase = ? and exa.step = ? and exa.index = ? and exa.sequence = .");
        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setLong(1, executionId);
                preStat.setString(2, test);
                preStat.setString(3, testCase);
                preStat.setInt(4, stepId);
                preStat.setInt(5, index);
                preStat.setInt(6, sequence);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        tcsa = this.loadFromResultset(resultSet);
                    }
                    if (tcsa == null) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
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
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        answer.setItem(tcsa);
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    @Deprecated
    public List<List<String>> getListOfSequenceDuration(String idList) {
        List<List<String>> list = null;
        StringBuilder query = new StringBuilder();
        query.append("select a.ID, Step, Sequence, 'Action' as type, b.Start,");
        query.append("concat(substr(EndLong,1,4),'-',");
        query.append("substr(EndLong,5,2),'-',substr(EndLong,7,2),' ',substr(EndLong,9,2),");
        query.append("':',substr(EndLong,11,2),':',substr(EndLong,13,2),'.',");
        query.append("substr(EndLong,15,3)) as testEnd, concat(substr(StartLong,1,4),'-',");
        query.append("substr(StartLong,5,2),'-',substr(StartLong,7,2),' ',");
        query.append("substr(StartLong,9,2),':',substr(StartLong,11,2),':',");
        query.append("substr(StartLong,13,2),'.',substr(StartLong,15,3)) as testStart, a.`action` as ctrl ");
        query.append(" from testcasestepactionexecution a join testcaseexecution b on a.id=b.id where step != '0' and a.test!='" + Test.TEST_PRETESTING + "' and a.id in (?)");
        query.append(" union select c.ID, c.Step, c.Sequence, 'Control', d.Start,");
        query.append("concat(substr(EndLong,1,4),'-',");
        query.append("substr(EndLong,5,2),'-',substr(EndLong,7,2),' ',substr(EndLong,9,2),");
        query.append("':',substr(EndLong,11,2),':',substr(EndLong,13,2),'.',");
        query.append("substr(EndLong,15,3)) as testEnd, concat(substr(StartLong,1,4),'-',");
        query.append("substr(StartLong,5,2),'-',substr(StartLong,7,2),' ',");
        query.append("substr(StartLong,9,2),':',substr(StartLong,11,2),':',");
        query.append("substr(StartLong,13,2),'.',substr(StartLong,15,3)) as testStart, c.`control` as ctrl ");
        query.append(" from testcasestepactioncontrolexecution c join testcaseexecution d on c.id=d.id where step != '0' and c.test!='" + Test.TEST_PRETESTING + "' and c.id in (?)");
        query.append(" order by step, sequence,ctrl,  type, ID");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, idList);
            preStat.setString(2, idList);
            LOG.warn(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<>();
                try {
                    while (resultSet.next()) {
                        List<String> array = new ArrayList<>();
                        array.add(resultSet.getString(1));
                        array.add(resultSet.getString(2));
                        array.add(resultSet.getString(3));
                        array.add(resultSet.getString(4));
                        array.add(resultSet.getString(5));
                        array.add(resultSet.getString(6));
                        array.add(resultSet.getString(7));
                        array.add(resultSet.getString(8));
                        list.add(array);
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
        return list;
    }

    @Override
    public void insertTestCaseStepActionExecution(TestCaseStepActionExecution testCaseStepActionExecution, HashMap<String, String> secrets) {

        final String query = "INSERT INTO testcasestepactionexecution(id, step, `index`, sequence, sort, "
                + "conditionOperator, conditionVal1Init, conditionVal2Init, conditionVal3Init, conditionVal1, conditionVal2, conditionVal3, ACTION, "
                + "value1Init, value2Init, value3Init, value1, value2, value3, forceExeStatus, "
                + "start, END, startlong, endlong, returnCode, returnMessage, test, testcase, description) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.id : " + testCaseStepActionExecution.getId());
            LOG.debug("SQL.param.test : " + testCaseStepActionExecution.getTest());
            LOG.debug("SQL.param.testcase : " + testCaseStepActionExecution.getTestCase());
            LOG.debug("SQL.param.step : " + testCaseStepActionExecution.getStepId());
            LOG.debug("SQL.param.index : " + testCaseStepActionExecution.getIndex());
            LOG.debug("SQL.param.sequence : " + testCaseStepActionExecution.getSequence());
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setLong(i++, testCaseStepActionExecution.getId());
                preStat.setInt(i++, testCaseStepActionExecution.getStepId());
                preStat.setInt(i++, testCaseStepActionExecution.getIndex());
                preStat.setInt(i++, testCaseStepActionExecution.getSequence());
                preStat.setInt(i++, testCaseStepActionExecution.getSort());
                preStat.setString(i++, testCaseStepActionExecution.getConditionOperator());
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getConditionVal1Init(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getConditionVal2Init(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getConditionVal3Init(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getConditionVal1(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getConditionVal2(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getConditionVal3(), 65000), secrets));
                preStat.setString(i++, testCaseStepActionExecution.getAction());
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getValue1Init(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getValue2Init(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getValue3Init(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getValue1(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getValue2(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getValue3(), 65000), secrets));
                preStat.setString(i++, testCaseStepActionExecution.isFatal());
                if (testCaseStepActionExecution.getStart() != 0) {
                    preStat.setTimestamp(i++, new Timestamp(testCaseStepActionExecution.getStart()));
                } else {
                    preStat.setString(i++, "1970-01-01 01:01:01");
                }
                if (testCaseStepActionExecution.getEnd() != 0) {
                    preStat.setTimestamp(i++, new Timestamp(testCaseStepActionExecution.getEnd()));
                } else {
                    preStat.setString(i++, "1970-01-01 01:01:01");
                }
                DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_TIMESTAMP);
                preStat.setString(i++, df.format(testCaseStepActionExecution.getStart()));
                preStat.setString(i++, df.format(testCaseStepActionExecution.getEnd()));
                preStat.setString(i++, testCaseStepActionExecution.getReturnCode());
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getReturnMessage(), 65000), secrets));
                preStat.setString(i++, testCaseStepActionExecution.getTest());
                preStat.setString(i++, testCaseStepActionExecution.getTestCase());
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getDescription(), 65000), secrets));
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
    public void updateTestCaseStepActionExecution(TestCaseStepActionExecution testCaseStepActionExecution, HashMap<String, String> secrets) {

        final String query = "UPDATE testcasestepactionexecution SET ACTION = ?, value1 = ?, value2 = ?, value3 = ?, forceExeStatus = ?, start = ?, END = ?"
                + ", startlong = ?, endlong = ?, returnCode = ?, returnMessage = ?, description = ?, sort = ?"
                + ", value1Init = ?, Value2Init = ?, value3Init = ?, conditionOperator = ?, "
                + "conditionVal1 = ?, conditionVal2 = ?, conditionVal3 = ?, conditionVal1Init = ?, conditionVal2Init = ?, conditionVal3Init = ?"
                + " WHERE id = ? AND test = ? AND testcase = ? AND step = ? AND `index` = ? AND sequence = ? ;";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.id : " + testCaseStepActionExecution.getId());
            LOG.debug("SQL.param.test : " + testCaseStepActionExecution.getTest());
            LOG.debug("SQL.param.testcase : " + testCaseStepActionExecution.getTestCase());
            LOG.debug("SQL.param.step : " + testCaseStepActionExecution.getStepId());
            LOG.debug("SQL.param.index : " + testCaseStepActionExecution.getIndex());
            LOG.debug("SQL.param.sequence : " + testCaseStepActionExecution.getSequence());
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setString(i++, testCaseStepActionExecution.getAction());
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getValue1(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getValue2(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getValue3(), 65000), secrets));
                preStat.setString(i++, testCaseStepActionExecution.isFatal());
                if (testCaseStepActionExecution.getStart() != 0) {
                    preStat.setTimestamp(i++, new Timestamp(testCaseStepActionExecution.getStart()));
                } else {
                    preStat.setString(i++, "1970-01-01 01:01:01");
                }
                if (testCaseStepActionExecution.getEnd() != 0) {
                    preStat.setTimestamp(i++, new Timestamp(testCaseStepActionExecution.getEnd()));
                } else {
                    preStat.setString(i++, "1970-01-01 01:01:01");
                }
                DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_TIMESTAMP);
                preStat.setString(i++, df.format(testCaseStepActionExecution.getStart()));
                preStat.setString(i++, df.format(testCaseStepActionExecution.getEnd()));
                preStat.setString(i++, testCaseStepActionExecution.getReturnCode());
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getReturnMessage(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getDescription(), 150), secrets));
                preStat.setInt(i++, testCaseStepActionExecution.getSort());
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getValue1Init(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getValue2Init(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getValue3Init(), 65000), secrets));
                preStat.setString(i++, testCaseStepActionExecution.getConditionOperator());
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getConditionVal1(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getConditionVal2(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getConditionVal3(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getConditionVal1Init(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getConditionVal2Init(), 65000), secrets));
                preStat.setString(i++, StringUtil.secureFromSecrets(StringUtil.getLeftString(testCaseStepActionExecution.getConditionVal3Init(), 65000), secrets));
                preStat.setLong(i++, testCaseStepActionExecution.getId());
                preStat.setString(i++, testCaseStepActionExecution.getTest());
                preStat.setString(i++, testCaseStepActionExecution.getTestCase());
                preStat.setInt(i++, testCaseStepActionExecution.getStepId());
                preStat.setInt(i++, testCaseStepActionExecution.getIndex());
                preStat.setInt(i++, testCaseStepActionExecution.getSequence());

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
    public TestCaseStepActionExecution loadFromResultset(ResultSet resultSet) throws SQLException {
        long id = resultSet.getInt("exa.id");
        String test = resultSet.getString("exa.test");
        String testCase = resultSet.getString("exa.testcase");
        int stepId = resultSet.getInt("exa.step");
        int index = resultSet.getInt("exa.index");
        int seq = resultSet.getInt("exa.sequence");
        int sort = resultSet.getInt("exa.sort");
        String returnCode = resultSet.getString("exa.returncode");
        String returnMessage = resultSet.getString("exa.returnmessage");
        String conditionOperator = resultSet.getString("exa.conditionOperator");
        String conditionVal1Init = resultSet.getString("exa.ConditionVal1Init");
        String conditionVal2Init = resultSet.getString("exa.ConditionVal2Init");
        String conditionVal3Init = resultSet.getString("exa.ConditionVal3Init");
        String conditionVal1 = resultSet.getString("exa.ConditionVal1");
        String conditionVal2 = resultSet.getString("exa.ConditionVal2");
        String conditionVal3 = resultSet.getString("exa.ConditionVal3");
        String action = resultSet.getString("exa.action");
        String value1 = resultSet.getString("exa.value1");
        String value2 = resultSet.getString("exa.value2");
        String value3 = resultSet.getString("exa.value3");
        String value1Init = resultSet.getString("exa.value1Init");
        String value2Init = resultSet.getString("exa.value2Init");
        String value3Init = resultSet.getString("exa.value3Init");
        String forceExeStatus = resultSet.getString("exa.forceExeStatus");
        long start = resultSet.getTimestamp("exa.start") == null ? 0 : resultSet.getTimestamp("exa.start").getTime();
        long end = resultSet.getTimestamp("exa.end") == null ? 0 : resultSet.getTimestamp("exa.end").getTime();
        long startlong = resultSet.getLong("exa.startlong");
        long endlong = resultSet.getLong("exa.endlong");
        String description = resultSet.getString("exa.description");
        return factoryTestCaseStepActionExecution.create(id, test, testCase, stepId, index, seq, sort, returnCode, returnMessage,
                conditionOperator, conditionVal1Init, conditionVal2Init, conditionVal3Init, conditionVal1, conditionVal2, conditionVal3, action, value1Init, value2Init, value3Init, value1, value2, value3, forceExeStatus, start, end, startlong, endlong, null, description, null, null);

    }

}
