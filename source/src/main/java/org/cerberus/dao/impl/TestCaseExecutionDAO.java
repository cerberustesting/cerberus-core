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

import org.apache.log4j.Logger;
import org.cerberus.dao.IApplicationDAO;
import org.cerberus.dao.ITestCaseExecutionDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.dto.TestCaseWithExecution;
import org.cerberus.entity.Application;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.TestCaseExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTestCaseExecution;
import org.cerberus.log.MyLogger;
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

    private static final Logger LOG = Logger.getLogger(TestCaseExecutionDAO.class);

    @Override
    public long insertTCExecution(TestCaseExecution tCExecution) throws CerberusException {
        boolean throwEx = false;
        final String query = "INSERT INTO testcaseexecution(test, testcase, build, revision, environment, country, browser, application, ip, "
                + "url, port, tag, verbose, status, start, end, controlstatus, controlMessage, crbversion, finished, browserFullVersion, executor, screensize) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
                preStat.setString(23, tCExecution.getScreenSize());

                preStat.executeUpdate();
                ResultSet resultSet = preStat.getGeneratedKeys();
                try {
                    if (resultSet.first()) {
                        return resultSet.getInt(1);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    throwEx = true;
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                throwEx = true;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            throwEx = true;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
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
                + ", start = ?, end = ? , controlstatus = ?, controlMessage = ?, crbversion = ?, finished = ? , browserFullVersion = ?, version = ?, platform = ?, executor = ?, screensize = ? WHERE id = ?";

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
                preStat.setString(25, tCExecution.getScreenSize());
                preStat.setLong(26, tCExecution.getId());

                preStat.executeUpdate();
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                throwEx = true;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
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
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
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
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
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
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        return result;
    }

    @Override
    public List<TestCaseExecution> findExecutionbyCriteria1(String dateLimit, String test, String testCase, String application, String country, String environment, String controlStatus, String status) throws CerberusException {
        List<TestCaseExecution> myTestCaseExecutions = null;
        TestCaseExecution Execution;
        boolean throwException = false;
        final String query = new StringBuffer("SELECT * FROM testcaseexecution  tce LEFT JOIN application app ")
                .append("ON tce.application = app.application WHERE start > ? ")
                .append("AND test LIKE ? AND testcase LIKE ? AND environment LIKE ? ")
                .append("AND country LIKE ? AND tce.application LIKE ? AND controlstatus LIKE ? ")
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
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    if (!(resultSet == null)) {
                        resultSet.close();
                    }
                }
            } catch (Exception exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (Exception exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
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
        Application application = null;
        try {
            application = applicationDAO.loadFromResultSet(resultSet);
        } catch (Exception e) {
            LOG.warn("No Application found for theses testcaseexecution " + e.toString());
        }
        String ip = resultSet.getString("ip"); // Host the Selenium IP
        String url = resultSet.getString("url");
        String port = resultSet.getString("port"); // host the Selenium Port
        String tag = resultSet.getString("tag");
        String finished = resultSet.getString("finished");
        int verbose = resultSet.getInt("verbose");
        String status = resultSet.getString("status");
        String crbVersion = resultSet.getString("crbVersion");
        String executor = resultSet.getString("executor");
        String screenSize = resultSet.getString("screensize");
        return factoryTCExecution.create(id, test, testcase, build, revision, environment,
                country, browser, version, platform, browserFullVersion, start, end, controlStatus, controlMessage, application, ip, url,
                port, tag, finished, verbose, 0, 0, 0, true, "", "", status, crbVersion, null, null, null,
                false, null, null, null, null, null, null, null, null, executor, 0, screenSize);
    }

    @Override
    public TestCaseExecution findTCExecutionByKey(long id) throws CerberusException {
        TestCaseExecution result = null;
        final String query = "SELECT * FROM testcaseexecution tce, application app WHERE tce.application = app.application AND ID = ?";

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
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
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
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    if (!(resultSet == null)) {
                        resultSet.close();
                    }
                }
            } catch (Exception exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (Exception exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
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
        query.append(" WHERE Test = ? and TestCase= ? and ID = ");
        query.append(" (SELECT MAX(ID) from `testcaseexecution` ");
        query.append("WHERE Test= ? and TestCase= ? and ControlStatus!='PE')");

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
                        long id = resultSet.getLong("ID");
                        String build = resultSet.getString("build");
                        String revision = resultSet.getString("revision");
                        String environment = resultSet.getString("environment");
                        String country = resultSet.getString("country");
                        long end;
                        if (resultSet.getLong("end") != 0L) {
                            end = resultSet.getTimestamp("end").getTime();
                        } else {
                            end = 0L;
                        }
                        String controlStatus = resultSet.getString("controlStatus");

                        result = factoryTCExecution.create(id, test, testCase, build, revision, environment,
                                country, null, null, null, null, 0, end, controlStatus, null, null, null, null,
                                null, null, null, 0, 0, 0, 0, true, "", "", null, null, null, null, null,
                                false, null, null, null, null, null, null, null, null, null);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        return result;
    }

    @Override
    public TestCaseExecution findLastTCExecutionInGroup(String test, String testCase, String environment, String country,
            String build, String revision, String browser, String browserVersion,
            String ip, String port, String tag) {

        TestCaseExecution result = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcaseexecution tce, application app WHERE tce.application = app.application ");
        query.append("AND test = ? AND testcase = ? AND country = ? AND browser = ? ");
        if (!StringUtil.isNull(environment)) {
            query.append("AND environment IN (");
            query.append(environment);
            query.append(") ");
        }
        if (!StringUtil.isNull(build)) {
            query.append("AND build IN (");
            query.append(build);
            query.append(") ");
        }
        if (!StringUtil.isNull(revision)) {
            query.append("AND revision IN (");
            query.append(revision);
            query.append(") ");
        }
        if (!StringUtil.isNull(browserVersion)) {
            query.append("AND browserfullversion LIKE ? ");
        }
        if (!StringUtil.isNull(ip)) {
            query.append("AND ip LIKE ? ");
        }
        if (!StringUtil.isNull(port)) {
            query.append("AND port LIKE ? ");
        }
        if (!StringUtil.isNull(tag)) {
            query.append("AND tag LIKE ? ");
        }
        query.append("ORDER BY id DESC");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, test);
            preStat.setString(2, testCase);
            preStat.setString(3, country);
            preStat.setString(4, browser);
            int i = 5;
            if (!StringUtil.isNull(browserVersion)) {
                preStat.setString(i, browserVersion);
                i++;
            }
            if (!StringUtil.isNull(ip)) {
                preStat.setString(i, ip);
                i++;
            }
            if (!StringUtil.isNull(port)) {
                preStat.setString(i, port);
                i++;
            }
            if (!StringUtil.isNull(tag)) {
                preStat.setString(i, tag);
            }

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadFromResultSet(resultSet);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        return result;
    }

    @Override
    public List<String> findDistinctTag(boolean withUUIDTag) throws CerberusException {
        List<String> list = null;
        StringBuilder query = new StringBuilder();
        query.append("select distinct tag from testcaseexecution tce ")
                .append("where tag != '' ");
        if (!withUUIDTag) {
            query.append(" and length(tag) != length('c3888898-c65a-11e3-9b3e-0000004047e0')");
        }
        query.append(" UNION select distinct tag from testcaseexecutionqueue where tag !='' ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<String>();

                    while (resultSet.next()) {
                        list.add(resultSet.getString("tag"));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }

        return list;
    }

    @Override
    public AnswerList findTagList() throws CerberusException {
        AnswerList response = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        List<String> list = null;
        StringBuilder query = new StringBuilder();
        query.append("select distinct tag from testcaseexecution tce ")
                .append("where tag != '' ");
        query.append(" UNION select distinct tag from testcaseexecutionqueue where tag !='' ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<String>();

                    while (resultSet.next()) {
                        list.add(resultSet.getString("tag"));
                    }
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TagList").replace("%OPERATION%", "SELECT"));
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }

        response.setResultMessage(msg);
        response.setDataList(list);
        return response;
    }

    @Override
    public void setTagToExecution(long id, String tag) throws CerberusException {
        boolean throwEx = false;
        final String query = "UPDATE testcaseexecution SET tag = ? WHERE id = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, tag);
                preStat.setLong(2, id);

                preStat.executeUpdate();
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                throwEx = true;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public AnswerList getTestCaseExecution(int start, int amount, String column, String dir, String searchTerm, String individualSearch, String tag) throws CerberusException {
        StringBuilder gSearch = new StringBuilder();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        AnswerList answer = new AnswerList();
        final StringBuffer query = new StringBuffer("SELECT SQL_CALC_FOUND_ROWS * FROM ( select tc.*, tce.Start, tce.End, tce.ID as statusExecutionID, tce.ControlStatus, tce.ControlMessage, tce.Environment, tce.Country, tce.Browser ")
                .append("from testcase tc ")
                .append("left join testcaseexecution tce ")
                .append("on tce.Test = tc.Test ")
                .append("and tce.TestCase = tc.TestCase ")
                .append("where tce.tag = ? ");

        query.append(" order by test, testcase, ID desc) as tce, application app ")
                .append("where tce.application = app.application ");

        gSearch.append("and (tce.`test` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or tce.`testCase` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or tce.`application` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or tce.`status` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or tce.`description` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or tce.`bugId` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or tce.`function` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%')");

        if (!searchTerm.equals("")) {
            query.append(gSearch.toString());
        }
        query.append("group by tce.test, tce.testcase, tce.Environment, tce.Browser, tce.Country ");
        query.append(" order by tce.`");
        query.append(column);
        query.append("` ");
        query.append(dir);
        query.append(" limit ");
        query.append(start);
        query.append(" , ");
        query.append(amount);

        List<TestCaseWithExecution> testCaseWithExecutionList = new ArrayList<TestCaseWithExecution>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());

            preStat.setString(1, tag);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testCaseWithExecutionList.add(this.loadTestCaseWithExecutionFromResultSet(resultSet));
                    }

                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecution").replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList(testCaseWithExecutionList, nrTotalRows);
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    testCaseWithExecutionList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                testCaseWithExecutionList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            testCaseWithExecutionList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.WARN, e.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            }
        }

        return answer;
    }

    public TestCaseWithExecution loadTestCaseWithExecutionFromResultSet(ResultSet resultSet) throws SQLException {
        TestCaseWithExecution testCaseWithExecution = new TestCaseWithExecution();

        testCaseWithExecution.setTest(resultSet.getString("Test"));
        testCaseWithExecution.setTestCase(resultSet.getString("TestCase"));
        testCaseWithExecution.setApplication(resultSet.getString("Application"));
        testCaseWithExecution.setProject(resultSet.getString("Project"));
        testCaseWithExecution.setTicket(resultSet.getString("Ticket"));
        testCaseWithExecution.setShortDescription(resultSet.getString("Description"));
        testCaseWithExecution.setDescription(resultSet.getString("BehaviorOrValueExpected"));
        testCaseWithExecution.setPriority(resultSet.getInt("Priority"));
        testCaseWithExecution.setStatus(resultSet.getString("Status"));
        testCaseWithExecution.setActive(resultSet.getString("TcActive"));
        testCaseWithExecution.setGroup(resultSet.getString("Group"));
        testCaseWithExecution.setOrigin(resultSet.getString("Origine"));
        testCaseWithExecution.setRefOrigin(resultSet.getString("RefOrigine"));
        testCaseWithExecution.setHowTo(resultSet.getString("HowTo"));
        testCaseWithExecution.setComment(resultSet.getString("Comment"));
        testCaseWithExecution.setFromSprint(resultSet.getString("FromBuild"));
        testCaseWithExecution.setFromRevision(resultSet.getString("FromRev"));
        testCaseWithExecution.setToSprint(resultSet.getString("ToBuild"));
        testCaseWithExecution.setToRevision(resultSet.getString("ToRev"));
        testCaseWithExecution.setBugID(resultSet.getString("BugID"));
        testCaseWithExecution.setTargetSprint(resultSet.getString("TargetBuild"));
        testCaseWithExecution.setTargetRevision(resultSet.getString("TargetRev"));
        testCaseWithExecution.setCreator(resultSet.getString("Creator"));
        testCaseWithExecution.setImplementer(resultSet.getString("Implementer"));
        testCaseWithExecution.setLastModifier(resultSet.getString("LastModifier"));
        testCaseWithExecution.setRunQA(resultSet.getString("activeQA"));
        testCaseWithExecution.setRunUAT(resultSet.getString("activeUAT"));
        testCaseWithExecution.setRunPROD(resultSet.getString("activePROD"));
        testCaseWithExecution.setFunction(resultSet.getString("function"));
        String start = resultSet.getString("Start");
        if (start.endsWith(".0")) {
            testCaseWithExecution.setStart(start.replace(".0", ""));
        } else {
            testCaseWithExecution.setStart(start);
        }
        try { // Managing the case where the date is 0000-00-00 00:00:00 inside MySQL
            testCaseWithExecution.setEnd(resultSet.getString("End"));
        } catch (SQLException e) {
            testCaseWithExecution.setEnd("0000-00-00 00:00:00");
        }
        testCaseWithExecution.setStatusExecutionID(resultSet.getLong("statusExecutionID"));
        testCaseWithExecution.setControlStatus(resultSet.getString("ControlStatus"));
        testCaseWithExecution.setControlMessage(resultSet.getString("ControlMessage"));
        testCaseWithExecution.setEnvironment(resultSet.getString("Environment"));
        testCaseWithExecution.setCountry(resultSet.getString("Country"));
        testCaseWithExecution.setBrowser(resultSet.getString("Browser"));

        testCaseWithExecution.setApplicationObject(applicationDAO.loadFromResultSet(resultSet));

        return testCaseWithExecution;
    }
}
