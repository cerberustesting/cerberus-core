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
import org.cerberus.crud.dao.ITestCaseStepExecutionDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.TestCaseStepExecution;
import org.cerberus.crud.factory.IFactoryTestCaseStepExecution;
import org.cerberus.log.MyLogger;
import org.cerberus.util.DateUtil;
import org.cerberus.util.ParameterParserUtil;
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

    /**
     * Short one line description.
     * <p/>
     * Longer description. If there were any, it would be here. <p> And even
     * more explanations to follow in consecutive paragraphs separated by HTML
     * paragraph breaks.
     *
     * @param variable Description text text text.
     */
    @Override
    public void insertTestCaseStepExecution(TestCaseStepExecution testCaseStepExecution) {
        final String query = "INSERT INTO testcasestepexecution(id, test, testcase, step, batnumexe, returncode, start, END, fullstart, fullend, returnMessage) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setLong(1, testCaseStepExecution.getId());
                preStat.setString(2, testCaseStepExecution.getTest());
                preStat.setString(3, testCaseStepExecution.getTestCase());
                preStat.setInt(4, testCaseStepExecution.getStep());
                preStat.setString(5, testCaseStepExecution.getBatNumExe());
                preStat.setString(6, testCaseStepExecution.getReturnCode());
                if (testCaseStepExecution.getStart() != 0) {
                    preStat.setTimestamp(7, new Timestamp(testCaseStepExecution.getStart()));
                } else {
                    preStat.setString(7, "0000-00-00 00:00:00");
                }
                if (testCaseStepExecution.getEnd() != 0) {
                    preStat.setTimestamp(8, new Timestamp(testCaseStepExecution.getEnd()));
                } else {
                    preStat.setString(8, "0000-00-00 00:00:00");
                }
                DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_TIMESTAMP);
                preStat.setString(9, df.format(testCaseStepExecution.getStart()));
                preStat.setString(10, df.format(testCaseStepExecution.getEnd()));
                preStat.setString(11, testCaseStepExecution.getReturnMessage());
                MyLogger.log(TestCaseStepExecutionDAO.class.getName(), Level.DEBUG, "Insert testcasestepexecution " + testCaseStepExecution.getId() + "-"
                        + testCaseStepExecution.getTest() + "-" + testCaseStepExecution.getTestCase() + "-" + testCaseStepExecution.getStep());

                preStat.executeUpdate();

            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
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
    public void updateTestCaseStepExecution(TestCaseStepExecution testCaseStepExecution) {
        final String query = "UPDATE testcasestepexecution SET returncode = ?, start = ?, fullstart = ?, end = ?, fullend = ?, timeelapsed = ?, returnmessage = ? WHERE id = ? AND step = ? AND test = ? AND testcase = ?";

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
                preStat.setLong(8, testCaseStepExecution.getId());
                preStat.setInt(9, testCaseStepExecution.getStep());
                preStat.setString(10, testCaseStepExecution.getTest());
                preStat.setString(11, testCaseStepExecution.getTestCase());
                MyLogger.log(TestCaseStepExecutionDAO.class.getName(), Level.DEBUG, "Update testcasestepexecution " + testCaseStepExecution.getId() + "-"
                        + testCaseStepExecution.getTest() + "-" + testCaseStepExecution.getTestCase() + "-" + testCaseStepExecution.getStep());

                preStat.executeUpdate();

            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
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
    public List<TestCaseStepExecution> findTestCaseStepExecutionById(long id) {
        List<TestCaseStepExecution> result = null;
        TestCaseStepExecution resultData;
        final String query = "SELECT * FROM testcasestepexecution WHERE id = ? ORDER BY start";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, String.valueOf(id));


                ResultSet resultSet = preStat.executeQuery();
                result = new ArrayList<TestCaseStepExecution>();
                try {
                    while (resultSet.next()) {
                        String test = resultSet.getString("test");
                        String testcase = resultSet.getString("testcase");
                        int step = resultSet.getInt("step");
                        String batNumExe = resultSet.getString("batnumexe");
                        long start = resultSet.getLong("start");
                        long end = resultSet.getLong("end");
                        long fullstart = resultSet.getLong("fullstart");
                        long fullend = resultSet.getLong("Fullend");
                        long timeelapsed = resultSet.getLong("timeelapsed");
                        String returnCode = resultSet.getString("returncode");
                        String returnMessage  = resultSet.getString("returnMessage");
                        resultData = factoryTestCaseStepExecution.create(id, test, testcase, step, batNumExe, start, end, fullstart, fullend, timeelapsed, returnCode, returnMessage);
                        result.add(resultData);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
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
