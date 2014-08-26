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
import org.cerberus.dao.IApplicationDAO;
import org.cerberus.dao.ITestCaseExecutionDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.Application;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.TestCaseExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTestCaseExecution;
import org.cerberus.log.MyLogger;
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
public class TestCaseExecutionDAO implements ITestCaseExecutionDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseExecution factoryTCExecution;
    @Autowired
    private IApplicationDAO applicationDAO;

    @Override
    public long insertTCExecution(TestCaseExecution tCExecution) throws CerberusException {
        boolean throwEx = false;
        final String query = "INSERT INTO testcaseexecution(test, testcase, build, revision, environment, country, browser, application, ip, "
                + "url, port, tag, verbose, status, start, end, controlstatus, controlMessage, crbversion, finished, browserFullVersion, executor) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
                preStat.setString(21, tCExecution.getBrowserFullVersion());
                preStat.setString(22, tCExecution.getExecutor());

                preStat.executeUpdate();
                ResultSet resultSet = preStat.getGeneratedKeys();
                try {
                    if (resultSet.first()) {
                        return resultSet.getInt(1);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    throwEx = true;
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                throwEx = true;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            throwEx = true;
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
        return 0;
    }

    @Override
    public void updateTCExecution(TestCaseExecution tCExecution) throws CerberusException {
        boolean throwEx = false;
        final String query = "UPDATE testcaseexecution SET test = ?, testcase = ?, build = ?, revision = ?, environment = ?, country = ?"
                + ", browser = ?, application = ?, ip = ?, url = ?, port = ?, tag = ?, verbose = ?, status = ?"
                + ", start = ?, end = ? , controlstatus = ?, controlMessage = ?, crbversion = ?, finished = ? , browserFullVersion = ?, version = ?, platform = ?, executor = ? WHERE id = ?";

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
                preStat.setString(21, tCExecution.getBrowserFullVersion());
                preStat.setString(22, tCExecution.getVersion());
                preStat.setString(23, tCExecution.getPlatform());
                preStat.setString(24, tCExecution.getExecutor());
                preStat.setLong(25, tCExecution.getId());

                preStat.executeUpdate();
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                throwEx = true;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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
                    MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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
    public TestCaseExecution findLastTCExecutionByCriteria(String test, String testcase, String environment, String country,
            String build, String revision) throws CerberusException {
        TestCaseExecution result = null;
        final String query = new StringBuffer("SELECT * FROM testcaseexecution tce, application app ")
                .append("WHERE tce.application = app.application ")
                .append("AND test = ? AND testcase = ? AND environment = ? ")
                .append("AND country = ? AND build = ? AND revision = ? ")
                .append("ORDER BY id DESC").toString();

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
                    MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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
    public TestCaseExecution findLastTCExecutionByCriteria(String test, String testCase, String environment, String country,
            String build, String revision, String browser, String browserVersion,
            String ip, String port, String tag) {
        TestCaseExecution result = null;
        final String query = new StringBuffer("SELECT * FROM testcaseexecution tce, application app ")
                .append("WHERE tce.application = app.application AND test = ? AND testcase = ? ")
                .append("AND environment LIKE ? AND country = ? AND build LIKE ? ")
                .append("AND revision LIKE ? AND browser = ? AND browserfullversion LIKE ? ")
                .append("AND ip LIKE ? AND port LIKE ? AND tag LIKE ? ")
                .append("ORDER BY id DESC").toString();

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setString(1, test);
            preStat.setString(2, testCase);
            preStat.setString(3, ParameterParserUtil.wildcardIfEmpty(environment));
            preStat.setString(4, country);
            preStat.setString(5, ParameterParserUtil.wildcardIfEmpty(build));
            preStat.setString(6, ParameterParserUtil.wildcardIfEmpty(revision));
            preStat.setString(7, browser);
            preStat.setString(8, ParameterParserUtil.wildcardIfEmpty(browserVersion));
            preStat.setString(9, ParameterParserUtil.wildcardIfEmpty(ip));
            preStat.setString(10, ParameterParserUtil.wildcardIfEmpty(port));
            preStat.setString(11, ParameterParserUtil.wildcardIfEmpty(tag));

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadFromResultSet(resultSet);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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
    public List<TestCaseExecution> findExecutionbyCriteria1(String dateLimit, String test, String testCase, String application, String country, String environment, String controlStatus, String status) throws CerberusException {
        List<TestCaseExecution> myTestCaseExecutions = null;
        TestCaseExecution Execution;
        boolean throwException = false;
        final String query = new StringBuffer("SELECT * FROM testcaseexecution  tce, application app ")
                .append("WHERE tce.application = app.application AND start > ? ")
                .append("AND test LIKE ? AND testcase LIKE ? AND environment LIKE ? ")
                .append("AND country LIKE ? AND application LIKE ? AND controlstatus LIKE ? ")
                .append("AND status LIKE ?").toString();

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
                        myTestCaseExecutions = new ArrayList<TestCaseExecution>();
                        do {
                            Execution = this.loadFromResultSet(resultSet);

                            myTestCaseExecutions.add(Execution);
                        } while (resultSet.next());
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    if (!(resultSet == null)) {
                        resultSet.close();
                    }
                }
            } catch (Exception exception) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (Exception exception) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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

    private TestCaseExecution loadFromResultSet(ResultSet resultSet) throws SQLException {
        long id = resultSet.getLong("ID");
        String test = resultSet.getString("test");
        String testcase = resultSet.getString("testcase");
        String build = resultSet.getString("build");
        String revision = resultSet.getString("revision");
        String environment = resultSet.getString("environment");
        String country = resultSet.getString("country");
        String browser = resultSet.getString("browser");
        String version = resultSet.getString("version");
        String platform = resultSet.getString("platform");
        String browserFullVersion = resultSet.getString("browserFullVersion");
        long start;
        if (resultSet.getLong("start") != 0L) {
            start = resultSet.getTimestamp("start").getTime();
        } else {
            start = 0L;
        }
        long end;
        if (resultSet.getLong("end") != 0L) {
            end = resultSet.getTimestamp("end").getTime();
        } else {
            end = 0L;
        }
        String controlStatus = resultSet.getString("controlStatus");
        String controlMessage = resultSet.getString("controlMessage");
        //TODO get Application
        Application application = applicationDAO.loadApplicationFromResultSet(resultSet);
        String ip = resultSet.getString("ip"); // Host the Selenium IP
        String url = resultSet.getString("url");
        String port = resultSet.getString("port"); // host the Selenium Port
        String tag = resultSet.getString("tag");
        String finished = resultSet.getString("finished");
        int verbose = resultSet.getInt("verbose");
        String status = resultSet.getString("status");
        String crbVersion = resultSet.getString("crbVersion");
        String executor = resultSet.getString("executor");
        return factoryTCExecution.create(id, test, testcase, build, revision, environment,
                country, browser, version, platform, browserFullVersion, start, end, controlStatus, controlMessage, application, ip, url,
                port, tag, finished, verbose, 0, 0,0, true, "", "", status, crbVersion, null, null, null,
                false, null, null, null, null, null, null, null, null, executor);
    }

    @Override
    public TestCaseExecution findTCExecutionByKey(long id) throws CerberusException {
        TestCaseExecution result = null;
        final String query = "SELECT * FROM testcaseexecution tce, application app WHERE tce.application = app.application and ID = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setLong(1, id);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadFromResultSet(resultSet);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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
    public List<TestCaseExecution> findExecutionsByCampaignNameAndTag(String campaign, String tag) throws CerberusException {
        List<TestCaseExecution> campaignTestCaseExecutions = null;
        boolean throwException = false;

        final String query = new StringBuffer("select tce.*, app.* from ( ")
                .append("select tce.* ")
                .append("from testcaseexecution tce ")
                .append("inner join testbatterycontent tbc ")
                .append("on tbc.Test = tce.Test ")
                .append("and tbc.TestCase = tce.TestCase ")
                .append("inner join campaigncontent cc ")
                .append("on cc.testbattery = tbc.testbattery ")
                .append("where tag is not null ")
                .append("and cc.campaign = ? ")
                .append("and tag = ? ")
                .append("order by test, testcase, ID desc) as tce, application app ")
                .append("where tce.application = app.application ")
                .append("group by tce.test, tce.testcase, tce.Environment, tce.Browser, tce.Country ").toString();

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, campaign);
                preStat.setString(2, tag);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (!(resultSet.first())) {
                        throwException = true;
                    } else {
                        campaignTestCaseExecutions = new ArrayList<TestCaseExecution>();
                        do {
                            campaignTestCaseExecutions.add(this.loadFromResultSet(resultSet));
                        } while (resultSet.next());
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    if (!(resultSet == null)) {
                        resultSet.close();
                    }
                }
            } catch (Exception exception) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (Exception exception) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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

        return campaignTestCaseExecutions;
    }

    @Override
    public TestCaseExecution findLastTestCaseExecutionNotPE(String test, String testCase) throws CerberusException {
        TestCaseExecution result = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT ID, Environment, Country, Build, Revision, End, ControlStatus  FROM `testcaseexecution` ");
        query.append(" WHERE test= ? and TestCase= ? and ID = ");
        query.append(" (SELECT MAX(ID) from `testcaseexecution` ");
        query.append("WHERE test= ? and TestCase= ? and ControlStatus!='PE')");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, test);
            preStat.setString(2, testCase);
            preStat.setString(3, test);
            preStat.setString(4, testCase);
            
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadFromResultSet(resultSet);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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
    public TestCaseExecution findLastTCExecutionInGroup(String test, String testCase, String environment, String country,
                                                        String build, String revision, String browser, String browserVersion,
                                                        String ip, String port, String tag) {
        TestCaseExecution result = null;
        final String query = "SELECT * FROM testcaseexecution tce, application app WHERE tce.application = app.application " +
                "AND test = ? AND testcase = ? AND environment IN (?) AND country = ? AND build IN (?) AND revision IN (?) " +
                "AND browser = ? AND browserfullversion LIKE ? AND ip LIKE ? AND port LIKE ? AND tag LIKE ? ORDER BY id DESC";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setString(1, test);
            preStat.setString(2, testCase);
            preStat.setString(3, ParameterParserUtil.wildcardIfEmpty(environment));
            preStat.setString(4, country);
            preStat.setString(5, ParameterParserUtil.wildcardIfEmpty(build));
            preStat.setString(6, ParameterParserUtil.wildcardIfEmpty(revision));
            preStat.setString(7, browser);
            preStat.setString(8, ParameterParserUtil.wildcardIfEmpty(browserVersion));
            preStat.setString(9, ParameterParserUtil.wildcardIfEmpty(ip));
            preStat.setString(10, ParameterParserUtil.wildcardIfEmpty(port));
            preStat.setString(11, ParameterParserUtil.wildcardIfEmpty(tag));

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadFromResultSet(resultSet);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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

}
