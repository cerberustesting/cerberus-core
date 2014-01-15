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
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.cerberus.dao.ITestCaseExecutionDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.TCExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTCExecution;
import org.cerberus.log.MyLogger;
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
public class TestCaseExecutionDAO implements ITestCaseExecutionDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTCExecution factoryTCExecution;

    @Override
    public void insertTCExecution(TCExecution tCExecution) throws CerberusException {
        boolean throwEx = false;
        final String query = "INSERT INTO testcaseexecution(test, testcase, build, revision, environment, country, browser, application, ip, "
                + "url, port, tag, verbose, status, start, end, controlstatus, controlMessage, crbversion, finished) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            try {
                preStat.setString(1, tCExecution.getTest());
                preStat.setString(2, tCExecution.getTestCase());
                preStat.setString(3, tCExecution.getBuild());
                preStat.setString(4, tCExecution.getRevision());
                preStat.setString(5, tCExecution.getEnvironment());
                preStat.setString(6, tCExecution.getCountry());
                preStat.setString(7, tCExecution.getBrowser());
                preStat.setString(8, tCExecution.getApplication().getApplication());
                preStat.setString(9, tCExecution.getIp());
                preStat.setString(10, tCExecution.getUrl());
                preStat.setString(11, tCExecution.getPort());
                preStat.setString(12, tCExecution.getTag());
                preStat.setInt(13, tCExecution.getVerbose());
                preStat.setString(14, tCExecution.getStatus());
                if (tCExecution.getStart() != 0) {
                    preStat.setTimestamp(15, new Timestamp(tCExecution.getStart()));
                } else {
                    preStat.setString(15, "0000-00-00 00:00:00");
                }
                if (tCExecution.getEnd() != 0) {
                    preStat.setTimestamp(16, new Timestamp(tCExecution.getEnd()));
                } else {
                    preStat.setString(16, "0000-00-00 00:00:00");
                }
                preStat.setString(17, tCExecution.getControlStatus());
                preStat.setString(18, StringUtil.getLeftString(tCExecution.getControlMessage(), 500));
                preStat.setString(19, tCExecution.getCrbVersion());
                preStat.setString(20, tCExecution.getFinished());

                preStat.executeUpdate();
                ResultSet resultSet = preStat.getGeneratedKeys();
                try {
                    if (resultSet.first()) {
                        tCExecution.setId(resultSet.getInt(1));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, exception.toString());
                throwEx = true;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            tCExecution.setId(-1);
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
        }
    }

    @Override
    public void updateTCExecution(TCExecution tCExecution) throws CerberusException {
        boolean throwEx = false;
        final String query = "UPDATE testcaseexecution SET test = ?, testcase = ?, build = ?, revision = ?, environment = ?, country = ?"
                + ", browser = ?, application = ?, ip = ?, url = ?, port = ?, tag = ?, verbose = ?, status = ?"
                + ", start = ?, end = ? , controlstatus = ?, controlMessage = ?, crbversion = ?, finished = ? WHERE id = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, tCExecution.getTest());
                preStat.setString(2, tCExecution.getTestCase());
                preStat.setString(3, tCExecution.getBuild());
                preStat.setString(4, tCExecution.getRevision());
                preStat.setString(5, tCExecution.getEnvironment());
                preStat.setString(6, tCExecution.getCountry());
                preStat.setString(7, tCExecution.getBrowser());
                preStat.setString(8, tCExecution.getApplication().getApplication());
                preStat.setString(9, tCExecution.getIp());
                preStat.setString(10, tCExecution.getUrl());
                preStat.setString(11, tCExecution.getPort());
                preStat.setString(12, tCExecution.getTag());
                preStat.setInt(13, tCExecution.getVerbose());
                preStat.setString(14, tCExecution.getStatus());
                if (tCExecution.getStart() != 0) {
                    preStat.setTimestamp(15, new Timestamp(tCExecution.getStart()));
                } else {
                    preStat.setString(15, "0000-00-00 00:00:00");
                }
                if (tCExecution.getEnd() != 0) {
                    preStat.setTimestamp(16, new Timestamp(tCExecution.getEnd()));
                } else {
                    preStat.setString(16, "0000-00-00 00:00:00");
                }
                preStat.setString(17, tCExecution.getControlStatus());
                preStat.setString(18, StringUtil.getLeftString(tCExecution.getControlMessage(), 500));
                preStat.setString(19, tCExecution.getCrbVersion());
                preStat.setString(20, tCExecution.getFinished());
                preStat.setLong(21, tCExecution.getId());

                preStat.executeUpdate();
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, exception.toString());
                throwEx = true;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
        }
    }

    @Override
    public List<String> getIDListOfLastExecutions(String test, String testcase, String country) {
        List<String> list = null;
        final String query = "SELECT ID FROM testcaseexecution WHERE test = ? AND testcase = ? AND country = ? AND controlStatus='OK' ORDER BY id DESC LIMIT 200";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                preStat.setString(3, country);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<String>();

                    while (resultSet.next()) {
                        list.add(resultSet.getString("ID"));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return list;
    }

    @Override
    public TCExecution findLastTCExecutionByCriteria(String test, String testcase, String environment, String country,
                                                     String build, String revision) throws CerberusException {
        TCExecution result = null;
        final String query = "SELECT * FROM testcaseexecution WHERE test = ? AND testcase = ? AND environment = ? AND " +
                "country = ? AND build = ? AND revision = ? ORDER BY id DESC";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setString(1, test);
            preStat.setString(2, testcase);
            preStat.setString(3, environment);
            preStat.setString(4, country);
            preStat.setString(5, build);
            preStat.setString(6, revision);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadFromResultSet(resultSet);
                    } else {
                        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }

    @Override
    public List<TCExecution> findExecutionbyCriteria1(String dateLimit, String test, String testCase, String application, String country, String environment, String controlStatus, String status) throws CerberusException {
        List<TCExecution> myTestCaseExecutions = null;
        TCExecution Execution;
        boolean throwException = false;
        final String query = "SELECT * FROM testcaseexecution WHERE start > ? AND test LIKE ? AND testcase LIKE ? AND environment LIKE ? AND country LIKE ? "
                + " AND application LIKE ? AND controlstatus LIKE ? AND status LIKE ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, dateLimit);
                preStat.setString(2, test);
                preStat.setString(3, testCase);
                preStat.setString(4, environment);
                preStat.setString(5, country);
                preStat.setString(6, application);
                preStat.setString(7, controlStatus);
                preStat.setString(8, status);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (!(resultSet.first())) {
                        throwException = true;
                    } else {
                        myTestCaseExecutions = new ArrayList<TCExecution>();
                        do {
                            Execution = this.loadFromResultSet(resultSet);

                            myTestCaseExecutions.add(Execution);
                        } while (resultSet.next());
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    if (!(resultSet == null)) {
                        resultSet.close();
                    }
                }
            } catch (Exception exception) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (Exception exception) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwException) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
        }

        return myTestCaseExecutions;
    }

    private TCExecution loadFromResultSet(ResultSet resultSet) throws SQLException{
        long id = resultSet.getLong("ID");
        String test = resultSet.getString("test");
        String testcase = resultSet.getString("testcase");
        String build = resultSet.getString("build");
        String revision = resultSet.getString("revision");
        String environment = resultSet.getString("environment");
        String country = resultSet.getString("country");
        String browser = resultSet.getString("browser");
        long start = resultSet.getTimestamp("start").getTime();
        long end = resultSet.getTimestamp("end").getTime();
        String controlStatus = resultSet.getString("controlStatus");
        String controlMessage = resultSet.getString("controlMessage");
        //TODO get Application
//        String application = resultSet.getString("application");
        String ip = resultSet.getString("ip"); // Host the Selenium IP
        String url = resultSet.getString("url");
        String port = resultSet.getString("port"); // host the Selenium Port
        String tag = resultSet.getString("tag");
        String finished = resultSet.getString("finished");
        int verbose = resultSet.getInt("verbose");
        String status = resultSet.getString("status");
        String crbVersion = resultSet.getString("crbVersion");
        return factoryTCExecution.create(id, test, testcase, build, revision, environment,
                country, browser, start, end, controlStatus, controlMessage, null, ip, url,
                port, tag, finished, verbose, 0, "", status, crbVersion, null, null, null,
                false, null, null, null, null, null, null, null, null);
    }
}
