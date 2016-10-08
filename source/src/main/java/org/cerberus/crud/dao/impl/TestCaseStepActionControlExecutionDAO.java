/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.crud.dao.ITestCaseStepActionControlExecutionDAO;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.crud.factory.IFactoryTestCaseStepActionControlExecution;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.log.MyLogger;
import org.cerberus.util.DateUtil;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 02/01/2013
 * @since 0.9.0
 */
@Repository
public class TestCaseStepActionControlExecutionDAO implements ITestCaseStepActionControlExecutionDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseStepActionControlExecution factoryTestCaseStepActionControlExecution;

    private static final Logger LOG = Logger.getLogger(TestCaseStepActionControlExecutionDAO.class);

    @Override
    public void insertTestCaseStepActionControlExecution(TestCaseStepActionControlExecution testCaseStepActionControlExecution) {

        final String query = "INSERT INTO testcasestepactioncontrolexecution(id, step, sequence, controlsequence, sort, returncode, control, "
                + "value1Init, value2Init, value1, value2, fatal, start, END, startlong, endlong, returnmessage, test, testcase, description)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i=1;
                preStat.setLong(i++, testCaseStepActionControlExecution.getId());
                preStat.setInt(i++, testCaseStepActionControlExecution.getStep());
                preStat.setInt(i++, testCaseStepActionControlExecution.getSequence());
                preStat.setInt(i++, testCaseStepActionControlExecution.getControlSequence());
                preStat.setInt(i++, testCaseStepActionControlExecution.getSort());
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCaseStepActionControlExecution.getReturnCode(), ""));
                preStat.setString(i++, StringUtil.getLeftString(testCaseStepActionControlExecution.getControl(), 200));
                preStat.setString(i++, testCaseStepActionControlExecution.getValue1Init());
                preStat.setString(i++, testCaseStepActionControlExecution.getValue2Init());
                preStat.setString(i++, testCaseStepActionControlExecution.getValue1());
                preStat.setString(i++, testCaseStepActionControlExecution.getValue2());
                preStat.setString(i++, testCaseStepActionControlExecution.getFatal());
                if (testCaseStepActionControlExecution.getStart() != 0) {
                    preStat.setTimestamp(i++, new Timestamp(testCaseStepActionControlExecution.getStart()));
                } else {
                    preStat.setString(i++, "1970-01-01 01:01:01");
                }
                if (testCaseStepActionControlExecution.getEnd() != 0) {
                    preStat.setTimestamp(i++, new Timestamp(testCaseStepActionControlExecution.getEnd()));
                } else {
                    preStat.setString(i++, "1970-01-01 01:01:01");
                }
                DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_TIMESTAMP);
                preStat.setString(i++, df.format(testCaseStepActionControlExecution.getStart()));
                preStat.setString(i++, df.format(testCaseStepActionControlExecution.getEnd()));
                preStat.setString(i++, StringUtil.getLeftString(ParameterParserUtil.parseStringParam(testCaseStepActionControlExecution.getReturnMessage(), ""), 500));
                preStat.setString(i++, testCaseStepActionControlExecution.getTest());
                preStat.setString(i++, testCaseStepActionControlExecution.getTestCase());
                preStat.setString(i++, testCaseStepActionControlExecution.getDescription());
                preStat.executeUpdate();

            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionControlExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionControlExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionControlExecutionDAO.class.getName(), Level.WARN, e.toString());
            }
        }
    }

    @Override
    public void updateTestCaseStepActionControlExecution(TestCaseStepActionControlExecution testCaseStepActionControlExecution) {

        final String query = "UPDATE testcasestepactioncontrolexecution SET returncode = ?, control = ?, "
                + "value1Init = ?, value2Init = ?, value1 = ?, value2 = ?, fatal = ?, start = ?, END = ?, startlong = ?, endlong = ?"
                + ", returnmessage = ?, description = ?, sort = ? "
                + "WHERE id = ? AND test = ? AND testcase = ? AND step = ? AND sequence = ? AND controlsequence = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i=1;
                preStat.setString(i++, ParameterParserUtil.parseStringParam(testCaseStepActionControlExecution.getReturnCode(), ""));
                preStat.setString(i++, StringUtil.getLeftString(testCaseStepActionControlExecution.getControl(), 200));
                preStat.setString(i++, testCaseStepActionControlExecution.getValue1Init());
                preStat.setString(i++, testCaseStepActionControlExecution.getValue2Init());
                preStat.setString(i++, testCaseStepActionControlExecution.getValue1());
                preStat.setString(i++, testCaseStepActionControlExecution.getValue2());
                preStat.setString(i++, testCaseStepActionControlExecution.getFatal());
                if (testCaseStepActionControlExecution.getStart() != 0) {
                    preStat.setTimestamp(i++, new Timestamp(testCaseStepActionControlExecution.getStart()));
                } else {
                    preStat.setString(i++, "1970-01-01 01:01:01");
                }
                if (testCaseStepActionControlExecution.getEnd() != 0) {
                    preStat.setTimestamp(i++, new Timestamp(testCaseStepActionControlExecution.getEnd()));
                } else {
                    preStat.setString(i++, "1970-01-01 01:01:01");
                }
                DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_TIMESTAMP);
                preStat.setString(i++, df.format(testCaseStepActionControlExecution.getStart()));
                preStat.setString(i++, df.format(testCaseStepActionControlExecution.getEnd()));
                preStat.setString(i++, StringUtil.getLeftString(ParameterParserUtil.parseStringParam(testCaseStepActionControlExecution.getReturnMessage(), ""), 500));
                preStat.setString(i++, testCaseStepActionControlExecution.getDescription());
                preStat.setInt(i++, testCaseStepActionControlExecution.getSort());
                preStat.setLong(i++, testCaseStepActionControlExecution.getId());
                preStat.setString(i++, testCaseStepActionControlExecution.getTest());
                preStat.setString(i++, testCaseStepActionControlExecution.getTestCase());
                preStat.setInt(i++, testCaseStepActionControlExecution.getStep());
                preStat.setInt(i++, testCaseStepActionControlExecution.getSequence());
                preStat.setInt(i++, testCaseStepActionControlExecution.getControlSequence());

                preStat.executeUpdate();

            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionControlExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionControlExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionControlExecutionDAO.class.getName(), Level.WARN, e.toString());
            }
        }
    }

    @Override
    public List<TestCaseStepActionControlExecution> findTestCaseStepActionControlExecutionByCriteria(long id, String test, String testCase, int step, int sequence) {
        List<TestCaseStepActionControlExecution> result = null;
        TestCaseStepActionControlExecution resultData;
        boolean throwEx = false;
        final String query = "SELECT * FROM testcasestepactioncontrolexecution WHERE id = ? AND test = ? AND testcase = ? AND step = ? AND sequence = ? ORDER BY sort";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, String.valueOf(id));
                preStat.setString(2, test);
                preStat.setString(3, testCase);
                preStat.setInt(4, step);
                preStat.setInt(5, sequence);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    result = new ArrayList<TestCaseStepActionControlExecution>();

                    while (resultSet.next()) {
                        result.add(this.loadFromResultset(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseStepActionControlExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionControlExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionControlExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionControlExecutionDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }

    @Override
    public AnswerList readByVarious1(long executionId, String test, String testCase, int step, int sequence) {
        MessageEvent msg;
        AnswerList answer = new AnswerList();
        List<TestCaseStepActionControlExecution> list = new ArrayList<TestCaseStepActionControlExecution>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcasestepactioncontrolexecution a ");
        query.append("where id = ? and test = ? and testcase = ? and step = ? ");
        query.append("and sequence = ?");
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
                preStat.setInt(4, step);
                preStat.setInt(5, sequence);
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
    public TestCaseStepActionControlExecution loadFromResultset(ResultSet resultSet) throws SQLException {
        long id = resultSet.getInt("id");
        String test = resultSet.getString("test");
        String testCase = resultSet.getString("testcase");
        int step = resultSet.getInt("step");
        int sequence = resultSet.getInt("sequence");
        int controlSequence = resultSet.getInt("controlSequence");
        int sort = resultSet.getInt("sort");
        String returnCode = resultSet.getString("returncode");
        String returnMessage = resultSet.getString("returnmessage");
        String control = resultSet.getString("control");
        String value1 = resultSet.getString("value1");
        String value2 = resultSet.getString("value2");
        String value1Init = resultSet.getString("value1Init");
        String value2Init = resultSet.getString("value2Init");
        String fatal = resultSet.getString("fatal");
        long start = resultSet.getTimestamp("start") == null ? 0 : resultSet.getTimestamp("start").getTime();
        long end = resultSet.getTimestamp("end") == null ? 0 : resultSet.getTimestamp("end").getTime();
        long startlong = resultSet.getLong("startlong");
        long endlong = resultSet.getLong("endlong");
        String description = resultSet.getString("description");
        return factoryTestCaseStepActionControlExecution.create(id, test, testCase, step,
                sequence, controlSequence, sort, returnCode, returnMessage, control, value1Init, value2Init, value1, value2,
                fatal, start, end, startlong, endlong, description, null, null);
    }
}
