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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.cerberus.dao.ITestCaseExecutionDataDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.TestCaseExecutionData;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTestCaseExecutionData;
import org.cerberus.log.MyLogger;
import org.cerberus.util.DateUtil;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 02/01/2013
 * @since 2.0.0
 */
@Repository
public class TestCaseExecutionDataDAO implements ITestCaseExecutionDataDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseExecutionData factoryTestCaseExecutionData;

    @Override
    public TestCaseExecutionData findTestCaseExecutionDataByKey(long id, String property) {
        TestCaseExecutionData result = null;
        final String query = "SELECT * FROM testcaseexecutiondata WHERE id = ? AND property = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, String.valueOf(id));
                preStat.setString(2, property);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        String value = resultSet.getString("value");
                        String type = resultSet.getString("type");
                        String object = resultSet.getString("object");
                        String returnCode = resultSet.getString("rc");
                        String returnMessage = resultSet.getString("rmessage");
                        long start = resultSet.getLong("start");
                        long end = resultSet.getLong("end");
                        long startLong = resultSet.getLong("startlong");
                        long endLong = resultSet.getLong("endlong");
                        result = factoryTestCaseExecutionData.create(id, property, value, type, object, returnCode, returnMessage,
                                start, end, startLong, endLong, null);
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
                MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }

    @Override
    public void insertTestCaseExecutionData(TestCaseExecutionData testCaseExecutionData) throws CerberusException {
        boolean throwException = true;
        final String query = "INSERT INTO testcaseexecutiondata(id, property, VALUE, TYPE, object, rc, rmessage, start, END, startlong, endlong) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setLong(1, testCaseExecutionData.getId());
                preStat.setString(2, testCaseExecutionData.getProperty());
                preStat.setString(3, ParameterParserUtil.securePassword(StringUtil.getLeftString(testCaseExecutionData.getValue(), 3000), testCaseExecutionData.getProperty()));
                preStat.setString(4, testCaseExecutionData.getType());
                preStat.setString(5, ParameterParserUtil.securePassword(StringUtil.getLeftString(testCaseExecutionData.getObject(), 2500), testCaseExecutionData.getProperty()));
                preStat.setString(6, testCaseExecutionData.getRC());
                preStat.setString(7, StringUtil.getLeftString(testCaseExecutionData.getrMessage(), 3000));
                preStat.setTimestamp(8, new Timestamp(testCaseExecutionData.getStart()));
                preStat.setTimestamp(9, new Timestamp(testCaseExecutionData.getEnd()));
                preStat.setString(10, DateUtil.DATE_FORMAT_TIMESTAMP.format(testCaseExecutionData.getStart()));
                preStat.setString(11, DateUtil.DATE_FORMAT_TIMESTAMP.format(testCaseExecutionData.getEnd()));

                preStat.executeUpdate();
                throwException = false;
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
                MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwException) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
    }

    @Override
    public void updateTestCaseExecutionData(TestCaseExecutionData testCaseExecutionData) throws CerberusException {
        boolean throwException = true;
        final String query = "UPDATE testcaseexecutiondata SET VALUE = ?, TYPE = ?, object = ?, rc = ?, rmessage = ?, start = ?, END = ?, startlong = ?, endlong = ? "
                + "WHERE id = ? AND property = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, ParameterParserUtil.securePassword(StringUtil.getLeftString(testCaseExecutionData.getValue(), 3000), testCaseExecutionData.getProperty()));
                preStat.setString(2, testCaseExecutionData.getType());
                preStat.setString(3, StringUtil.getLeftString(testCaseExecutionData.getObject(), 2500));
                preStat.setString(4, testCaseExecutionData.getRC());
                preStat.setString(5, StringUtil.getLeftString(testCaseExecutionData.getrMessage(), 3000));
                preStat.setTimestamp(6, new Timestamp(testCaseExecutionData.getStart()));
                preStat.setTimestamp(7, new Timestamp(testCaseExecutionData.getEnd()));
                preStat.setString(8, DateUtil.DATE_FORMAT_TIMESTAMP.format(testCaseExecutionData.getStart()));
                preStat.setString(9, DateUtil.DATE_FORMAT_TIMESTAMP.format(testCaseExecutionData.getEnd()));
                preStat.setLong(10, testCaseExecutionData.getId());
                preStat.setString(11, testCaseExecutionData.getProperty());

                preStat.executeUpdate();
                throwException = false;
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
                MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwException) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
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
    public List<String> getPastValuesOfProperty(String propName, String test, String testCase, String build, String environment, String country) {
        List<String> list = null;
        final String query = "SELECT VALUE FROM testcaseexecutiondata WHERE Property = ? AND ID IN "
                + "(SELECT id FROM testcaseexecution WHERE test = ? AND testcase = ? AND build = ? AND environment = ? AND country = ?) "
                + "ORDER BY ID DESC";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, propName);
                preStat.setString(2, test);
                preStat.setString(3, testCase);
                preStat.setString(4, build);
                preStat.setString(5, environment);
                preStat.setString(6, country);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<String>();

                    while (resultSet.next()) {
                        list.add(resultSet.getString("value"));
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
                MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public List<TestCaseExecutionData> findTestCaseExecutionDataById(long id) {
        List<TestCaseExecutionData> result = null;
        TestCaseExecutionData resultData;
        boolean throwEx = false;
        final String query = "SELECT * FROM testcaseexecutiondata WHERE id = ? ORDER BY startlong";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, String.valueOf(id));

                ResultSet resultSet = preStat.executeQuery();
                try {
                    result = new ArrayList<TestCaseExecutionData>();

                    while (resultSet.next()) {
                        String value = resultSet.getString("value");
                        String property = resultSet.getString("property");
                        String type = resultSet.getString("type");
                        String object = resultSet.getString("object");
                        String returnCode = resultSet.getString("rc");
                        String returnMessage = resultSet.getString("rmessage");
                        long start = resultSet.getLong("start");
                        long end = resultSet.getLong("end");
                        long startLong = resultSet.getLong("startlong");
                        long endLong = resultSet.getLong("endlong");
                        resultData = factoryTestCaseExecutionData.create(id, property, value, type, object, returnCode, returnMessage,
                                start, end, startLong, endLong, null);
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
                MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }
}
