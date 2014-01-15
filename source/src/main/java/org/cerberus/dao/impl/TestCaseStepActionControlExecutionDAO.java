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

import org.cerberus.dao.ITestCaseStepActionControlExecutionDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.TestCaseStepActionControlExecution;
import org.cerberus.factory.IFactoryTestCaseStepActionControlExecution;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.cerberus.util.DateUtil;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 02/01/2013
 * @since 2.0.0
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
    public void insertTestCaseStepActionControlExecution(TestCaseStepActionControlExecution testCaseStepActionControlExecution) {

        final String query = "INSERT INTO testcasestepactioncontrolexecution(id, step, sequence, control, returncode, controltype, "
                + "controlproperty, controlvalue, fatal, start, END, startlong, endlong, returnmessage, test, testcase, screenshotfilename) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setLong(1, testCaseStepActionControlExecution.getId());
                preStat.setInt(2, testCaseStepActionControlExecution.getStep());
                preStat.setInt(3, testCaseStepActionControlExecution.getSequence());
                preStat.setInt(4, testCaseStepActionControlExecution.getControl());
                preStat.setString(5, ParameterParserUtil.parseStringParam(testCaseStepActionControlExecution.getReturnCode(), ""));
                preStat.setString(6, StringUtil.getLeftString(testCaseStepActionControlExecution.getControlType(), 200));
                preStat.setString(7, StringUtil.getLeftString(testCaseStepActionControlExecution.getControlProperty(), 2500));
                preStat.setString(8, StringUtil.getLeftString(testCaseStepActionControlExecution.getControlValue(), 200));
                preStat.setString(9, testCaseStepActionControlExecution.getFatal());
                if (testCaseStepActionControlExecution.getStart() != 0) {
                    preStat.setTimestamp(10, new Timestamp(testCaseStepActionControlExecution.getStart()));
                } else {
                    preStat.setString(10, "0000-00-00 00:00:00");
                }
                if (testCaseStepActionControlExecution.getEnd() != 0) {
                    preStat.setTimestamp(11, new Timestamp(testCaseStepActionControlExecution.getEnd()));
                } else {
                    preStat.setString(11, "0000-00-00 00:00:00");
                }
                preStat.setString(12, DateUtil.DATE_FORMAT_TIMESTAMP.format(testCaseStepActionControlExecution.getStart()));
                preStat.setString(13, DateUtil.DATE_FORMAT_TIMESTAMP.format(testCaseStepActionControlExecution.getEnd()));
                preStat.setString(14, StringUtil.getLeftString(ParameterParserUtil.parseStringParam(testCaseStepActionControlExecution.getReturnMessage(), ""), 500));
                preStat.setString(15, testCaseStepActionControlExecution.getTest());
                preStat.setString(16, testCaseStepActionControlExecution.getTestCase());
                preStat.setString(17, testCaseStepActionControlExecution.getScreenshotFilename());

                preStat.executeUpdate();

            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionControlExecutionDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionControlExecutionDAO.class.getName(), Level.ERROR, exception.toString());
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
    public void updateTestCaseStepActionControlExecution(TestCaseStepActionControlExecution testCaseStepActionControlExecution) {

        final String query = "UPDATE testcasestepactioncontrolexecution SET returncode = ?, controltype = ?, "
                + "controlproperty = ?, controlvalue = ?, fatal = ?, start = ?, END = ?, startlong = ?, endlong = ?"
                + ", returnmessage = ?, screenshotfilename = ? "
                + "WHERE id = ? AND test = ? AND testcase = ? AND step = ? AND sequence = ? AND control = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, ParameterParserUtil.parseStringParam(testCaseStepActionControlExecution.getReturnCode(), ""));
                preStat.setString(2, StringUtil.getLeftString(testCaseStepActionControlExecution.getControlType(), 200));
                preStat.setString(3, StringUtil.getLeftString(testCaseStepActionControlExecution.getControlProperty(), 2500));
                preStat.setString(4, StringUtil.getLeftString(testCaseStepActionControlExecution.getControlValue(), 200));
                preStat.setString(5, testCaseStepActionControlExecution.getFatal());
                if (testCaseStepActionControlExecution.getStart() != 0) {
                    preStat.setTimestamp(6, new Timestamp(testCaseStepActionControlExecution.getStart()));
                } else {
                    preStat.setString(6, "0000-00-00 00:00:00");
                }
                if (testCaseStepActionControlExecution.getEnd() != 0) {
                    preStat.setTimestamp(7, new Timestamp(testCaseStepActionControlExecution.getEnd()));
                } else {
                    preStat.setString(7, "0000-00-00 00:00:00");
                }
                preStat.setString(8, DateUtil.DATE_FORMAT_TIMESTAMP.format(testCaseStepActionControlExecution.getStart()));
                preStat.setString(9, DateUtil.DATE_FORMAT_TIMESTAMP.format(testCaseStepActionControlExecution.getEnd()));
                preStat.setString(10, StringUtil.getLeftString(ParameterParserUtil.parseStringParam(testCaseStepActionControlExecution.getReturnMessage(), ""), 500));
                preStat.setString(11, testCaseStepActionControlExecution.getScreenshotFilename());

                preStat.setLong(12, testCaseStepActionControlExecution.getId());
                preStat.setString(13, testCaseStepActionControlExecution.getTest());
                preStat.setString(14, testCaseStepActionControlExecution.getTestCase());
                preStat.setInt(15, testCaseStepActionControlExecution.getStep());
                preStat.setInt(16, testCaseStepActionControlExecution.getSequence());
                preStat.setInt(17, testCaseStepActionControlExecution.getControl());

                preStat.executeUpdate();

            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionControlExecutionDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionControlExecutionDAO.class.getName(), Level.ERROR, exception.toString());
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
    public List<TestCaseStepActionControlExecution> findTestCaseStepActionControlExecutionByCriteria(long id, String test, String testCase, int step, int sequence) {
        List<TestCaseStepActionControlExecution> result = null;
        TestCaseStepActionControlExecution resultData;
        boolean throwEx = false;
        final String query = "SELECT * FROM testcasestepactioncontrolexecution WHERE id = ? AND test = ? AND testcase = ? AND step = ? AND sequence = ? ORDER BY control";

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
                        int control = resultSet.getInt("control");
                        String returnCode = resultSet.getString("returncode");
                        String returnMessage = resultSet.getString("returnmessage");
                        String controlType = resultSet.getString("controlType");
                        String controlProperty = resultSet.getString("ControlProperty");
                        String controlValue = resultSet.getString("controlValue");
                        String fatal = resultSet.getString("fatal");
                        long start = resultSet.getLong("start");
                        long end = resultSet.getLong("end");
                        long startlong = resultSet.getLong("startlong");
                        long endlong = resultSet.getLong("endlong");
                        String screenshot = resultSet.getString("ScreenshotFilename");
                        resultData = factoryTestCaseStepActionControlExecution.create(id, test, testCase, step, sequence, control, returnCode, returnMessage, controlType, controlProperty, controlValue, fatal, start, end, startlong, endlong, screenshot, null, null);
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
