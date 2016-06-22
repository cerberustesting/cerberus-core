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
import org.cerberus.crud.dao.ITestCaseStepActionExecutionDAO;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.crud.factory.IFactoryTestCaseStepActionExecution;
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
public class TestCaseStepActionExecutionDAO implements ITestCaseStepActionExecutionDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseStepActionExecution factoryTestCaseStepActionExecution;

    private static final Logger LOG = Logger.getLogger(TestCaseStepActionExecutionDAO.class);

    @Override
    public void updateTestCaseStepActionExecution(TestCaseStepActionExecution testCaseStepActionExecution) {

        final String query = "UPDATE testcasestepactionexecution SET ACTION = ?, object = ?, property = ?, start = ?, END = ?"
                + ", startlong = ?, endlong = ?, returnCode = ?, returnMessage = ?, screenshotfilename = ?, pageSourceFilename = ?, description = ?, sort = ?"
                + " WHERE id = ? AND test = ? AND testcase = ? AND step = ? AND sequence = ? ;";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, testCaseStepActionExecution.getAction());
                preStat.setString(2, StringUtil.getLeftString(testCaseStepActionExecution.getObject(), 200));
                preStat.setString(3, StringUtil.getLeftString(ParameterParserUtil.securePassword(testCaseStepActionExecution.getProperty(), testCaseStepActionExecution.getPropertyName()), 200));
                if (testCaseStepActionExecution.getStart() != 0) {
                    preStat.setTimestamp(4, new Timestamp(testCaseStepActionExecution.getStart()));
                } else {
                    preStat.setString(4, "1970-01-01 01:01:01");
                }
                if (testCaseStepActionExecution.getEnd() != 0) {
                    preStat.setTimestamp(5, new Timestamp(testCaseStepActionExecution.getEnd()));
                } else {
                    preStat.setString(5, "1970-01-01 01:01:01");
                }
                DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_TIMESTAMP);
                preStat.setString(6, df.format(testCaseStepActionExecution.getStart()));
                preStat.setString(7, df.format(testCaseStepActionExecution.getEnd()));
                preStat.setString(8, testCaseStepActionExecution.getReturnCode());
                preStat.setString(9, StringUtil.getLeftString(testCaseStepActionExecution.getReturnMessage(), 500));
                preStat.setString(10, testCaseStepActionExecution.getScreenshotFilename());
                preStat.setString(11, testCaseStepActionExecution.getPageSourceFilename());
                preStat.setString(12, testCaseStepActionExecution.getDescription());
                preStat.setInt(13, testCaseStepActionExecution.getSort());
                preStat.setLong(14, testCaseStepActionExecution.getId());
                preStat.setString(15, testCaseStepActionExecution.getTest());
                preStat.setString(16, testCaseStepActionExecution.getTestCase());
                preStat.setInt(17, testCaseStepActionExecution.getStep());
                preStat.setInt(18, testCaseStepActionExecution.getSequence());

                preStat.executeUpdate();

            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionExecutionDAO.class.getName(), Level.WARN, e.toString());
            }
        }
    }

    @Override
    public void insertTestCaseStepActionExecution(TestCaseStepActionExecution testCaseStepActionExecution) {

        final String query = "INSERT INTO testcasestepactionexecution(id, step, sequence, sort, ACTION, object, property, start, END, startlong, endlong, returnCode, returnMessage, test, testcase, screenshotfilename, pagesourcefilename, description) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setLong(1, testCaseStepActionExecution.getId());
                preStat.setInt(2, testCaseStepActionExecution.getStep());
                preStat.setInt(3, testCaseStepActionExecution.getSequence());
                preStat.setInt(4, testCaseStepActionExecution.getSort());
                preStat.setString(5, testCaseStepActionExecution.getAction());
                preStat.setString(6, StringUtil.getLeftString(testCaseStepActionExecution.getObject(), 200));
                preStat.setString(7, StringUtil.getLeftString(ParameterParserUtil.securePassword(testCaseStepActionExecution.getProperty(), testCaseStepActionExecution.getPropertyName()), 200));
                if (testCaseStepActionExecution.getStart() != 0) {
                    preStat.setTimestamp(8, new Timestamp(testCaseStepActionExecution.getStart()));
                } else {
                    preStat.setString(8, "1970-01-01 01:01:01");
                }
                if (testCaseStepActionExecution.getEnd() != 0) {
                    preStat.setTimestamp(9, new Timestamp(testCaseStepActionExecution.getEnd()));
                } else {
                    preStat.setString(9, "1970-01-01 01:01:01");
                }
                DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_TIMESTAMP);
                preStat.setString(10, df.format(testCaseStepActionExecution.getStart()));
                preStat.setString(11, df.format(testCaseStepActionExecution.getEnd()));
                preStat.setString(12, testCaseStepActionExecution.getReturnCode());
                preStat.setString(13, StringUtil.getLeftString(testCaseStepActionExecution.getReturnMessage(), 500));
                preStat.setString(14, testCaseStepActionExecution.getTest());
                preStat.setString(15, testCaseStepActionExecution.getTestCase());
                preStat.setString(16, testCaseStepActionExecution.getScreenshotFilename());
                preStat.setString(17, testCaseStepActionExecution.getPageSourceFilename());
                preStat.setString(18, testCaseStepActionExecution.getDescription());
                preStat.executeUpdate();

            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionExecutionDAO.class.getName(), Level.WARN, e.toString());
            }
        }
    }

    /**
     * @param idList
     * @return
     * @deprecated because no longer consistency with actual step/action/control sort attribute
     */
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
        query.append(" from testcasestepactionexecution a join testcaseexecution b on a.id=b.id where step != '0' and a.test!='Pre Testing' and a.id in (?)");
        query.append(" union select c.ID, c.Step, c.Sequence, 'Control', d.Start,");
        query.append("concat(substr(EndLong,1,4),'-',");
        query.append("substr(EndLong,5,2),'-',substr(EndLong,7,2),' ',substr(EndLong,9,2),");
        query.append("':',substr(EndLong,11,2),':',substr(EndLong,13,2),'.',");
        query.append("substr(EndLong,15,3)) as testEnd, concat(substr(StartLong,1,4),'-',");
        query.append("substr(StartLong,5,2),'-',substr(StartLong,7,2),' ',");
        query.append("substr(StartLong,9,2),':',substr(StartLong,11,2),':',");
        query.append("substr(StartLong,13,2),'.',substr(StartLong,15,3)) as testStart, c.`control` as ctrl ");
        query.append(" from testcasestepactioncontrolexecution c join testcaseexecution d on c.id=d.id where step != '0' and c.test!='Pre Testing' and c.id in (?)");
        query.append(" order by step, sequence,ctrl,  type, ID");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, idList);
            preStat.setString(2, idList);
            MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.WARN, query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<List<String>>();
                try {
                    while (resultSet.next()) {
                        List<String> array = new ArrayList<String>();
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
                    MyLogger.log(TestCaseStepActionExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionExecutionDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public List<TestCaseStepActionExecution> findTestCaseStepActionExecutionByCriteria(long id, String test, String testCase, int step) {
        List<TestCaseStepActionExecution> result = null;
        TestCaseStepActionExecution resultData;
        final String query = "SELECT * FROM testcasestepactionexecution WHERE id = ? AND test = ? AND testcase = ? AND step = ? ORDER BY sort";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, String.valueOf(id));
                preStat.setString(2, test);
                preStat.setString(3, testCase);
                preStat.setInt(4, step);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    result = new ArrayList<TestCaseStepActionExecution>();

                    while (resultSet.next()) {
                        result.add(this.loadFromResultset(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseStepActionExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionExecutionDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }

    @Override
    public AnswerList readByVarious1(long executionId, String test, String testCase, int step) {
        MessageEvent msg;
        AnswerList answer = new AnswerList();
        List<TestCaseStepActionExecution> list = new ArrayList<TestCaseStepActionExecution>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcasestepactionexecution a ");
        query.append("where id = ? and test = ? and testcase = ? and step = ? ");
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
    public TestCaseStepActionExecution loadFromResultset(ResultSet resultSet) throws SQLException {
        long id = resultSet.getInt("id");
        String test = resultSet.getString("test");
        String testCase = resultSet.getString("testcase");
        int step = resultSet.getInt("step");
        int seq = resultSet.getInt("sequence");
        int sort = resultSet.getInt("sort");
        String returnCode = resultSet.getString("returncode");
        String returnMessage = resultSet.getString("returnmessage");
        String action = resultSet.getString("action");
        String object = resultSet.getString("object");
        String property = resultSet.getString("property");
        long start = resultSet.getTimestamp("start")==null?0:resultSet.getTimestamp("start").getTime();
        long end = resultSet.getTimestamp("end")==null?0:resultSet.getTimestamp("end").getTime();
        long startlong = resultSet.getLong("startlong");
        long endlong = resultSet.getLong("endlong");
        String screenshot = resultSet.getString("ScreenshotFilename");
        String pageSource = resultSet.getString("PageSourceFilename");
        String description = resultSet.getString("description");
        return factoryTestCaseStepActionExecution.create(id, test, testCase, step, seq, sort, returnCode, returnMessage, action, object, property, start, end, startlong, endlong, screenshot, pageSource, null,description, null, null);

    }

}
