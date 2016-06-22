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

import java.math.BigDecimal;
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
import org.cerberus.crud.dao.ITestCaseStepExecutionDAO;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.TestCaseStepExecution;
import org.cerberus.crud.factory.IFactoryTestCaseStepExecution;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.log.MyLogger;
import org.cerberus.util.DateUtil;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.AnswerList;
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

    private static final Logger LOG = Logger.getLogger(TestCaseStepExecutionDAO.class);

    /**
     * Short one line description.
     * <p/>
     * Longer description. If there were any, it would be here.
     * <p>
     * And even more explanations to follow in consecutive paragraphs separated
     * by HTML paragraph breaks.
     *
     * @param variable Description text text text.
     */
    @Override
    public void insertTestCaseStepExecution(TestCaseStepExecution testCaseStepExecution) {
        final String query = "INSERT INTO testcasestepexecution(id, test, testcase, step, sort, batnumexe, returncode, start, fullstart, returnMessage, description) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setLong(1, testCaseStepExecution.getId());
                preStat.setString(2, testCaseStepExecution.getTest());
                preStat.setString(3, testCaseStepExecution.getTestCase());
                preStat.setInt(4, testCaseStepExecution.getStep());
                preStat.setInt(5, testCaseStepExecution.getSort());
                preStat.setString(6, testCaseStepExecution.getBatNumExe());
                preStat.setString(7, testCaseStepExecution.getReturnCode());
                preStat.setTimestamp(8, new Timestamp(testCaseStepExecution.getStart()));
                DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_TIMESTAMP);
                preStat.setString(9, df.format(testCaseStepExecution.getStart()));
                preStat.setString(10, testCaseStepExecution.getReturnMessage());
                preStat.setString(11, testCaseStepExecution.getDescription());
                MyLogger.log(TestCaseStepExecutionDAO.class.getName(), Level.DEBUG, "Insert testcasestepexecution " + testCaseStepExecution.getId() + "-"
                        + testCaseStepExecution.getTest() + "-" + testCaseStepExecution.getTestCase() + "-" + testCaseStepExecution.getStep());

                preStat.executeUpdate();

            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepExecutionDAO.class.getName(), Level.WARN, e.toString());
            }
        }
    }

    @Override
    public void updateTestCaseStepExecution(TestCaseStepExecution testCaseStepExecution) {
        final String query = "UPDATE testcasestepexecution SET returncode = ?, start = ?, fullstart = ?, end = ?, fullend = ?, timeelapsed = ?, returnmessage = ?, description = ?, sort = ? WHERE id = ? AND step = ? AND test = ? AND testcase = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            Timestamp timeStart = new Timestamp(testCaseStepExecution.getStart());
            Timestamp timeEnd = new Timestamp(testCaseStepExecution.getEnd());

            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_TIMESTAMP);
                preStat.setString(1, ParameterParserUtil.parseStringParam(testCaseStepExecution.getReturnCode(), ""));
                preStat.setTimestamp(2, timeStart);
                preStat.setString(3, df.format(timeStart));
                preStat.setTimestamp(4, timeEnd);
                preStat.setString(5, df.format(timeEnd));
                preStat.setFloat(6, (timeEnd.getTime() - timeStart.getTime()) / (float) 1000);
                preStat.setString(7, testCaseStepExecution.getReturnMessage());
                preStat.setString(8, testCaseStepExecution.getDescription());
                preStat.setInt(9, testCaseStepExecution.getSort());
                preStat.setLong(10, testCaseStepExecution.getId());
                preStat.setInt(11, testCaseStepExecution.getStep());
                preStat.setString(12, testCaseStepExecution.getTest());
                preStat.setString(13, testCaseStepExecution.getTestCase());
                MyLogger.log(TestCaseStepExecutionDAO.class.getName(), Level.DEBUG, "Update testcasestepexecution " + testCaseStepExecution.getId() + "-"
                        + testCaseStepExecution.getTest() + "-" + testCaseStepExecution.getTestCase() + "-" + testCaseStepExecution.getStep());

                preStat.executeUpdate();

            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepExecutionDAO.class.getName(), Level.WARN, e.toString());
            }
        }
    }

    @Override
    public List<TestCaseStepExecution> findTestCaseStepExecutionById(long id) {
        List<TestCaseStepExecution> result = null;
        TestCaseStepExecution resultData;
        final String query = "SELECT * FROM testcasestepexecution WHERE id = ? ORDER BY fullstart";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, String.valueOf(id));

                ResultSet resultSet = preStat.executeQuery();
                result = new ArrayList<TestCaseStepExecution>();
                try {
                    while (resultSet.next()) {
                        result.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseStepExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepExecutionDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }

    @Override
    public AnswerList readByVarious1(long executionId, String test, String testcase) {
        MessageEvent msg;
        AnswerList answer = new AnswerList();
        List<TestCaseStepExecution> list = new ArrayList<TestCaseStepExecution>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcasestepexecution a ");
        query.append("where id = ? and test = ? and testcase = ?");
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
                preStat.setString(3, testcase);
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
        int step = resultSet.getInt("step");
        int sort = resultSet.getInt("sort");
        String batNumExe = resultSet.getString("batnumexe");
        long start = resultSet.getTimestamp("start")==null?0:resultSet.getTimestamp("start").getTime();
        long end = resultSet.getTimestamp("end")==null?0:resultSet.getTimestamp("end").getTime();
        long fullstart = resultSet.getLong("fullstart");
        long fullend = resultSet.getLong("Fullend");
        BigDecimal timeelapsed = resultSet.getBigDecimal("timeelapsed");
        String returnCode = resultSet.getString("returncode");
        String returnMessage = resultSet.getString("returnMessage");
        String description = resultSet.getString("description");
        return factoryTestCaseStepExecution.create(id, test, testcase, step, sort, batNumExe, start, end, fullstart, fullend, timeelapsed, returnCode, returnMessage, description);
    }
}
