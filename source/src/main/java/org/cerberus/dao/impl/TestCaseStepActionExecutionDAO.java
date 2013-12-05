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
package org.cerberus.dao.impl;

import org.cerberus.dao.ITestCaseStepActionExecutionDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.TestCaseStepActionExecution;
import org.cerberus.factory.IFactoryTestCaseStepActionExecution;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 02/01/2013
 * @since 2.0.0
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

    @Override
    public void updateTestCaseStepActionExecution(TestCaseStepActionExecution testCaseStepActionExecution) {

        final String query = "UPDATE testcasestepactionexecution SET ACTION = ?, object = ?, property = ?, start = ?, END = ?"
                + ", startlong = ?, endlong = ?, returnCode = ?, returnMessage = ?, screenshotfilename = ? "
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
                    preStat.setString(4, "0000-00-00 00:00:00");
                }
                if (testCaseStepActionExecution.getEnd() != 0) {
                    preStat.setTimestamp(5, new Timestamp(testCaseStepActionExecution.getEnd()));
                } else {
                    preStat.setString(5, "0000-00-00 00:00:00");
                }
                preStat.setString(6, new SimpleDateFormat("yyyyMMddHHmmssSSS").format(testCaseStepActionExecution.getStart()));
                preStat.setString(7, new SimpleDateFormat("yyyyMMddHHmmssSSS").format(testCaseStepActionExecution.getEnd()));
                preStat.setString(8, testCaseStepActionExecution.getReturnCode());
                preStat.setString(9, StringUtil.getLeftString(testCaseStepActionExecution.getReturnMessage(), 500));
                preStat.setString(10, testCaseStepActionExecution.getScreenshotFilename());

                preStat.setLong(11, testCaseStepActionExecution.getId());
                preStat.setString(12, testCaseStepActionExecution.getTest());
                preStat.setString(13, testCaseStepActionExecution.getTestCase());
                preStat.setInt(14, testCaseStepActionExecution.getStep());
                preStat.setInt(15, testCaseStepActionExecution.getSequence());

                preStat.executeUpdate();

            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionExecutionDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionExecutionDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.WARN, e.toString());
            }
        }
    }

    @Override
    public void insertTestCaseStepActionExecution(TestCaseStepActionExecution testCaseStepActionExecution) {

        final String query = "INSERT INTO testcasestepactionexecution(id, step, sequence, ACTION, object, property, start, END, startlong, endlong, returnCode, returnMessage, test, testcase, screenshotfilename) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setLong(1, testCaseStepActionExecution.getId());
                preStat.setInt(2, testCaseStepActionExecution.getStep());
                preStat.setInt(3, testCaseStepActionExecution.getSequence());
                preStat.setString(4, testCaseStepActionExecution.getAction());
                preStat.setString(5, StringUtil.getLeftString(testCaseStepActionExecution.getObject(), 200));
                preStat.setString(6, StringUtil.getLeftString(ParameterParserUtil.securePassword(testCaseStepActionExecution.getProperty(), testCaseStepActionExecution.getPropertyName()), 200));
                if (testCaseStepActionExecution.getStart() != 0) {
                    preStat.setTimestamp(7, new Timestamp(testCaseStepActionExecution.getStart()));
                } else {
                    preStat.setString(7, "0000-00-00 00:00:00");
                }
                if (testCaseStepActionExecution.getEnd() != 0) {
                    preStat.setTimestamp(8, new Timestamp(testCaseStepActionExecution.getEnd()));
                } else {
                    preStat.setString(8, "0000-00-00 00:00:00");
                }
                preStat.setString(9, new SimpleDateFormat("yyyyMMddHHmmssSSS").format(testCaseStepActionExecution.getStart()));
                preStat.setString(10, new SimpleDateFormat("yyyyMMddHHmmssSSS").format(testCaseStepActionExecution.getEnd()));
                preStat.setString(11, testCaseStepActionExecution.getReturnCode());
                preStat.setString(12, StringUtil.getLeftString(testCaseStepActionExecution.getReturnMessage(), 500));
                preStat.setString(13, testCaseStepActionExecution.getTest());
                preStat.setString(14, testCaseStepActionExecution.getTestCase());
                preStat.setString(15, testCaseStepActionExecution.getScreenshotFilename());

                preStat.executeUpdate();

            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionExecutionDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionExecutionDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.WARN, e.toString());
            }
        }
    }

    @Override
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
                    MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public List<TestCaseStepActionExecution> findTestCaseStepActionExecutionByCriteria(long id, String test, String testCase, int step) {
        List<TestCaseStepActionExecution> result = null;
        TestCaseStepActionExecution resultData;
        final String query = "SELECT * FROM testcasestepactionexecution WHERE id = ? AND test = ? AND testcase = ? AND step = ? ORDER BY sequence";

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
                        int seq = resultSet.getInt("sequence");
                        String returnCode = resultSet.getString("returncode");
                        String returnMessage = resultSet.getString("returnmessage");
                        String action = resultSet.getString("action");
                        String object = resultSet.getString("object");
                        String property = resultSet.getString("property");
                        long start = resultSet.getLong("start");
                        long end = resultSet.getLong("end");
                        long startlong = resultSet.getLong("startlong");
                        long endlong = resultSet.getLong("endlong");
                        String screenshot = resultSet.getString("ScreenshotFilename");
                        resultData = factoryTestCaseStepActionExecution.create(id, test, testCase, step, seq, returnCode, returnMessage, action, object, property, start, end, startlong, endlong, screenshot, null, null, null);
                        result.add(resultData);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }


}
