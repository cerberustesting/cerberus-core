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
import org.cerberus.crud.dao.ITestCaseExecutionDataDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.crud.entity.TestCaseExecutionData;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryTestCaseExecutionData;
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
 * @since 0.9.0
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

    private static final Logger LOG = Logger.getLogger(TestCaseExecutionDataDAO.class);

    private final String OBJECT_NAME = "TestCase Execution Data";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public TestCaseExecutionData findTestCaseExecutionDataByKey(long id, String property) {
        TestCaseExecutionData result = null;
        final String query = "SELECT * FROM testcaseexecutiondata WHERE id = ? AND property = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, String.valueOf(id));
                preStat.setString(2, property);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        String description = resultSet.getString("description");
                        String value = resultSet.getString("value");
                        String type = resultSet.getString("type");
                        String value1 = resultSet.getString("value1");
                        String value2 = resultSet.getString("value2");
                        String returnCode = resultSet.getString("rc");
                        String returnMessage = resultSet.getString("rmessage");
                        long start = resultSet.getTimestamp("start").getTime();
                        long end = resultSet.getTimestamp("end").getTime();
                        long startLong = resultSet.getLong("startlong");
                        long endLong = resultSet.getLong("endlong");
                        result = factoryTestCaseExecutionData.create(id, property, description, value, type, value1, value2, returnCode, returnMessage,
                                start, end, startLong, endLong, null);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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
    public List<TestCaseExecutionData> findTestCaseExecutionDataById(long id) {
        List<TestCaseExecutionData> result = null;
        TestCaseExecutionData resultData;
        final String query = "SELECT * FROM testcaseexecutiondata WHERE id = ? ORDER BY startlong";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, String.valueOf(id));

                ResultSet resultSet = preStat.executeQuery();
                try {
                    result = new ArrayList<TestCaseExecutionData>();

                    while (resultSet.next()) {
                        String description = resultSet.getString("description");
                        String value = resultSet.getString("value");
                        String property = resultSet.getString("property");
                        String type = resultSet.getString("type");
                        String value1 = resultSet.getString("value1");
                        String value2 = resultSet.getString("value2");
                        String returnCode = resultSet.getString("rc");
                        String returnMessage = resultSet.getString("rmessage");
                        long start = resultSet.getTimestamp("start")==null?0:resultSet.getTimestamp("start").getTime();
                        long end = resultSet.getTimestamp("end")==null?0:resultSet.getTimestamp("end").getTime();
                        long startLong = resultSet.getLong("startlong");
                        long endLong = resultSet.getLong("endlong");
                        resultData = factoryTestCaseExecutionData.create(id, property, description, value, type, value1, value2, returnCode, returnMessage,
                                start, end, startLong, endLong, null);
                        result.add(resultData);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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
    public List<String> getPastValuesOfProperty(long id, String propName, String test, String testCase, String build, String environment, String country) {
        List<String> list = null;
        final String query = "SELECT distinct exd.`VALUE` FROM testcaseexecution exe "
                + "JOIN testcaseexecutiondata exd ON exd.Property = ? and exd.ID = exe.ID "
                + "WHERE exe.test = ? AND exe.testcase = ? AND exe.build = ? AND exe.environment = ? "
                + "AND exe.country = ? AND exe.id <> ? ;";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param : " + propName);
            LOG.debug("SQL.param : " + test);
            LOG.debug("SQL.param : " + testCase);
            LOG.debug("SQL.param : " + build);
            LOG.debug("SQL.param : " + environment);
            LOG.debug("SQL.param : " + country);
            LOG.debug("SQL.param : " + id);
        }

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
                preStat.setLong(7, id);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<String>();

                    while (resultSet.next()) {
                        list.add(resultSet.getString("value"));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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
    public List<String> getInUseValuesOfProperty(long id, String propName, String environment, String country, Integer timeoutInSecond) {
        List<String> list = null;
        final String query = "SELECT distinct exd.`VALUE` FROM testcaseexecution exe "
                + "JOIN testcaseexecutiondata exd ON exd.Property = ? and exd.ID = exe.ID "
                + "WHERE exe.environment = ? AND exe.country = ? AND exe.ControlSTATUS = 'PE' "
                + "AND TO_SECONDS(NOW()) - TO_SECONDS(exe.start) < ? AND exe.ID <> ? ;";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param : " + propName);
            LOG.debug("SQL.param : " + environment);
            LOG.debug("SQL.param : " + country);
            LOG.debug("SQL.param : " + String.valueOf(timeoutInSecond));
            LOG.debug("SQL.param : " + id);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, propName);
                preStat.setString(2, environment);
                preStat.setString(3, country);
                preStat.setInt(4, timeoutInSecond);
                preStat.setLong(5, id);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<String>();

                    while (resultSet.next()) {
                        list.add(resultSet.getString("value"));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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
    public void insertTestCaseExecutionData(TestCaseExecutionData testCaseExecutionData) throws CerberusException {
        boolean throwException = true;
        final String query = "INSERT INTO testcaseexecutiondata(id, property, description, VALUE, TYPE, VALUE1,VALUE2, rc, rmessage, start, END, startlong, endlong) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_TIMESTAMP);
                preStat.setLong(1, testCaseExecutionData.getId());
                preStat.setString(2, testCaseExecutionData.getProperty());
                preStat.setString(3, testCaseExecutionData.getDescription());
                preStat.setString(4, ParameterParserUtil.securePassword(StringUtil.getLeftString(testCaseExecutionData.getValue(), 3000), testCaseExecutionData.getProperty()));
                preStat.setString(5, testCaseExecutionData.getType());
                preStat.setString(6, ParameterParserUtil.securePassword(StringUtil.getLeftString(testCaseExecutionData.getValue1(), 3000), testCaseExecutionData.getProperty()));
                preStat.setString(7, ParameterParserUtil.securePassword(StringUtil.getLeftString(testCaseExecutionData.getValue2(), 2500), testCaseExecutionData.getProperty()));
                preStat.setString(8, testCaseExecutionData.getRC());
                preStat.setString(9, StringUtil.getLeftString(testCaseExecutionData.getrMessage(), 3000));
                preStat.setTimestamp(10, new Timestamp(testCaseExecutionData.getStart()));
                preStat.setTimestamp(11, new Timestamp(testCaseExecutionData.getEnd()));
                preStat.setString(12, df.format(testCaseExecutionData.getStart()));
                preStat.setString(13, df.format(testCaseExecutionData.getEnd()));

                preStat.executeUpdate();
                throwException = false;
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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
        final String query = "UPDATE testcaseexecutiondata SET DESCRIPTION = ?, VALUE = ?, TYPE = ?, VALUE1 = ?, VALUE2 = ?, rc = ?, rmessage = ?, start = ?, END = ?, startlong = ?, endlong = ? "
                + "WHERE id = ? AND property = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_TIMESTAMP);
                preStat.setString(1, testCaseExecutionData.getDescription());
                preStat.setString(2, ParameterParserUtil.securePassword(StringUtil.getLeftString(testCaseExecutionData.getValue(), 3000), testCaseExecutionData.getProperty()));
                preStat.setString(3, testCaseExecutionData.getType());
                preStat.setString(4, ParameterParserUtil.securePassword(StringUtil.getLeftString(testCaseExecutionData.getValue1(), 3000), testCaseExecutionData.getProperty()));
                preStat.setString(5, StringUtil.getLeftString(testCaseExecutionData.getValue2(), 2500));
                preStat.setString(6, testCaseExecutionData.getRC());
                preStat.setString(7, StringUtil.getLeftString(testCaseExecutionData.getrMessage(), 3000));
                preStat.setTimestamp(8, new Timestamp(testCaseExecutionData.getStart()));
                preStat.setTimestamp(9, new Timestamp(testCaseExecutionData.getEnd()));
                preStat.setString(10, df.format(testCaseExecutionData.getStart()));
                preStat.setString(11, df.format(testCaseExecutionData.getEnd()));
                preStat.setLong(12, testCaseExecutionData.getId());
                preStat.setString(13, testCaseExecutionData.getProperty());

                preStat.executeUpdate();
                throwException = false;
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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

}
