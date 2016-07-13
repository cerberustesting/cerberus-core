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
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.crud.dao.IApplicationDAO;
import org.cerberus.crud.dao.ITestCaseExecutionDAO;
import org.cerberus.crud.entity.Application;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.factory.IFactoryTestCaseExecution;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.dto.TestCaseWithExecution;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
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

    private final String OBJECT_NAME = "TestCase Execution";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public long insertTCExecution(TestCaseExecution tCExecution) throws CerberusException {
        boolean throwEx = false;
        final String query = "INSERT INTO testcaseexecution(test, testcase, build, revision, environment, country, browser, application, ip, "
                + "url, port, tag, verbose, status, start, controlstatus, controlMessage, crbversion, finished, browserFullVersion, executor, screensize) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
                preStat.setTimestamp(15, new Timestamp(tCExecution.getStart()));
                preStat.setString(16, tCExecution.getControlStatus());
                preStat.setString(17, StringUtil.getLeftString(tCExecution.getControlMessage(), 500));
                preStat.setString(18, tCExecution.getCrbVersion());
                preStat.setString(19, tCExecution.getFinished());
                preStat.setString(20, tCExecution.getBrowserFullVersion());
                preStat.setString(21, tCExecution.getExecutor());
                preStat.setString(22, tCExecution.getScreenSize());

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
                + ", start = ?, end = ? , controlstatus = ?, controlMessage = ?, crbversion = ?, finished = ? "
                + ", browserFullVersion = ?, version = ?, platform = ?, executor = ?, screensize = ? WHERE id = ?";

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
                preStat.setTimestamp(15, new Timestamp(tCExecution.getStart()));
                if (tCExecution.getEnd() != 0) {
                    preStat.setTimestamp(16, new Timestamp(tCExecution.getEnd()));
                } else {
                    preStat.setString(16, "1970-01-01 01:01:01");
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
    public AnswerItem readLastByCriteria(String application) {
        AnswerItem ans = new AnswerItem();
        TestCaseExecution result = null;
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("select * from testcaseexecution ");

        searchSQL.append(" where 1=1 ");

        if (!StringUtil.isNullOrEmpty(application)) {
            searchSQL.append(" and (`application` = ? )");
        }
        query.append(searchSQL);

        query.append(" order by id DESC limit 1 ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                if (!StringUtil.isNullOrEmpty(application)) {
                    preStat.setString(i++, application);
                }
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = loadFromResultSet(resultSet);
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        ans.setItem(result);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
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

        //sets the message
        ans.setResultMessage(msg);
        return ans;
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
                        result = this.loadTestCaseExecutionAndApplicationFromResultSet(resultSet);
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
                        result = this.loadTestCaseExecutionAndApplicationFromResultSet(resultSet);
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
                            Execution = this.loadTestCaseExecutionAndApplicationFromResultSet(resultSet);

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

    private TestCaseExecution loadTestCaseExecutionAndApplicationFromResultSet(ResultSet resultSet) throws SQLException {
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
        long start = resultSet.getTimestamp("start") == null ? 0 : resultSet.getTimestamp("start").getTime();
        long end = resultSet.getTimestamp("end") == null ? 0 : resultSet.getTimestamp("end").getTime();
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

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setLong(1, id);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadTestCaseExecutionAndApplicationFromResultSet(resultSet);
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
                            campaignTestCaseExecutions.add(this.loadTestCaseExecutionAndApplicationFromResultSet(resultSet));
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
                        result = this.loadTestCaseExecutionAndApplicationFromResultSet(resultSet);
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
    public AnswerList findTagList(int tagnumber) {
        AnswerList response = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        List<String> list = null;
        StringBuilder query = new StringBuilder();

        query.append("SELECT DISTINCT tag FROM testcaseexecution WHERE tag != ''");

        if (tagnumber != 0) {
            query.append("ORDER BY id desc LIMIT ");
            query.append(tagnumber);
        }

        query.append(";");
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
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
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
    public AnswerList readByTagByCriteria(String tag, int start, int amount, String column, String dir, String searchTerm, String individualSearch) throws CerberusException {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        AnswerList answer = new AnswerList();

        final StringBuffer query = new StringBuffer();

        query.append("SELECT * FROM ");
        query.append("( select tc.*, tce.Start, tce.End, tce.ID as statusExecutionID, tce.ControlStatus, tce.ControlMessage, tce.Environment, tce.Country, tce.Browser ");
        query.append("from testcase tc ");
        query.append("left join testcaseexecution tce ");
        query.append("on tce.Test = tc.Test ");
        query.append("and tce.TestCase = tc.TestCase ");

        query.append(" where 1=1 ");

        if (!StringUtil.isNullOrEmpty(tag)) {
            query.append("and tce.tag = ? ");
        }

        query.append(" order by test, testcase, ID desc) as tce");
        query.append(" , application app where tce.application = app.application ");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            query.append("and (tce.`test` like '%").append(searchTerm).append("%'");
            query.append(" or tce.`testCase` like '%").append(searchTerm).append("%'");
            query.append(" or tce.`application` like '%").append(searchTerm).append("%'");
            query.append(" or tce.`description` like '%").append(searchTerm).append("%')");
        }

        query.append("group by tce.test, tce.testcase, tce.Environment, tce.Browser, tce.Country ");

        if (!StringUtil.isNullOrEmpty(column)) {
            query.append(" order by tce.`").append(column).append("` ").append(dir);
        }

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        List<TestCaseWithExecution> testCaseWithExecutionList = new ArrayList<TestCaseWithExecution>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            if (!StringUtil.isNullOrEmpty(tag)) {
                preStat.setString(1, tag);
            }

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testCaseWithExecutionList.add(this.loadTestCaseWithExecutionFromResultSet(resultSet));
                    }

//                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
//                    int nrTotalRows = 0;
//
//                    if (resultSet != null && resultSet.next()) {
//                        nrTotalRows = resultSet.getInt(1);
//                    }
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecution").replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList(testCaseWithExecutionList, testCaseWithExecutionList.size());
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    testCaseWithExecutionList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                testCaseWithExecutionList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            testCaseWithExecutionList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.WARN, e.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            }
        }

        return answer;
    }

    @Override
    public AnswerList readDistinctEnvCoutnryBrowserByTag(String tag) {
        AnswerList answer = new AnswerList();
        StringBuilder query = new StringBuilder();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);

        query.append("SELECT Environment, Country, Browser, ControlStatus FROM testcaseexecution WHERE tag = ? GROUP BY Environment, Country, Browser, ControlStatus");

        Connection connection = this.databaseSpring.connect();

        List<TestCaseWithExecution> EnvCountryBrowserList = new ArrayList<TestCaseWithExecution>();

        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());

            preStat.setString(1, tag);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        EnvCountryBrowserList.add(this.loadEnvCountryBrowserFromResultSet(resultSet));
                    }
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecution").replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList(EnvCountryBrowserList, EnvCountryBrowserList.size());
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    EnvCountryBrowserList = null;
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                EnvCountryBrowserList = null;
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException ex) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.WARN, ex.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerList readDistinctColumnByTag(String tag, boolean env, boolean country, boolean browser, boolean app) {
        AnswerList answer = new AnswerList();
        StringBuilder query = new StringBuilder();
        StringBuilder distinct = new StringBuilder();
        int prev = 0;
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);

        if (!(!env && !country && !app && !browser)) {
            if (env) {
                distinct.append("Environment");
                prev++;
            }
            if (country) {
                if (prev != 0) {
                    prev = 0;
                    distinct.append(",");
                }
                distinct.append("Country");
                prev++;
            }
            if (browser) {
                if (prev != 0) {
                    prev = 0;
                    distinct.append(",");
                }
                distinct.append("Browser");
                prev++;
            }
            if (app) {
                if (prev != 0) {
                    prev = 0;
                    distinct.append(",");
                }
                distinct.append("Application");
            }

            query.append("SELECT ");
            query.append(distinct.toString());
            query.append(" FROM testcaseexecution WHERE tag = ? GROUP BY ");
            query.append(distinct.toString());
        } else {
            //If there is no distinct, select nothing
            query.append("SELECT * FROM testcaseexecution WHERE 1 = 0 AND tag = ?");
        }

        Connection connection = this.databaseSpring.connect();

        List<TestCaseWithExecution> column = new ArrayList<TestCaseWithExecution>();

        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());

            preStat.setString(1, tag);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        TestCaseWithExecution tmp = new TestCaseWithExecution();
                        if (env) {
                            tmp.setEnvironment(resultSet.getString("Environment"));
                        } else {
                            tmp.setEnvironment("");
                        }
                        if (country) {
                            tmp.setCountry(resultSet.getString("Country"));
                        } else {
                            tmp.setCountry("");
                        }
                        if (browser) {
                            tmp.setBrowser(resultSet.getString("Browser"));
                        } else {
                            tmp.setBrowser("");
                        }
                        if (app) {
                            tmp.setApplication(resultSet.getString("Application"));
                        } else {
                            tmp.setApplication("");
                        }
                        column.add(tmp);
                    }
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecution").replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList(column, column.size());
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    column = null;
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                column = null;
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException ex) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.WARN, ex.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }

        answer.setResultMessage(msg);
        return answer;
    }

    public TestCaseWithExecution loadEnvCountryBrowserFromResultSet(ResultSet resultSet) throws SQLException {
        TestCaseWithExecution testCaseWithExecution = new TestCaseWithExecution();

        testCaseWithExecution.setEnvironment(resultSet.getString("Environment"));
        testCaseWithExecution.setCountry(resultSet.getString("Country"));
        testCaseWithExecution.setBrowser(resultSet.getString("Browser"));
        testCaseWithExecution.setControlStatus(resultSet.getString("ControlStatus"));

        return testCaseWithExecution;
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
        if (!("PE".equals(resultSet.getString("ControlStatus")))) { // When execution is still PE End is not feeded correctly.
            testCaseWithExecution.setEnd(resultSet.getString("End"));
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

    @Override
    public AnswerList readBySystemByVarious(String system, List<String> testList, List<String> applicationList, List<String> projectList, List<String> tcstatusList,
            List<String> groupList, List<String> tcactiveList, List<String> priorityList, List<String> targetsprintList, List<String> targetrevisionList,
            List<String> creatorList, List<String> implementerList, List<String> buildList, List<String> revisionList, List<String> environmentList,
            List<String> countryList, List<String> browserList, List<String> tcestatusList, String ip, String port, String tag, String browserversion,
            String comment, String bugid, String ticket) {

        AnswerList answer = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        List<TestCaseWithExecution> tceList = new ArrayList<TestCaseWithExecution>();
        List<String> whereClauses = new LinkedList<String>();

        StringBuilder query = new StringBuilder();

        int paramNumber = 0;

        query.append(" select t.ID as statusExecutionID, t.* from ( ");
        query.append(" select tce.*, tc.Project, tc.Ticket, tc.Description, tc.BehaviorOrValueExpected, ");
        query.append(" tc.Priority, tc.`Group`, tc.Origine, tc.RefOrigine, tc.HowTo, tc.`Comment`, ");
        query.append(" tc.FromBuild, tc.FromRev, tc.ToBuild, tc.ToRev, tc.BugID, tc.TargetBuild, ");
        query.append(" tc.TargetRev, tc.Creator, tc.Implementer, tc.LastModifier, tc.activeQA, ");
        query.append(" tc.activeUAT, tc.activePROD, tc.`function`, tc.TcActive, ");
        query.append(" a.sort, a.`type`, a.`system`, a.SubSystem, a.svnurl, a.deploytype, ");
        query.append(" a.mavengroupid, a.BugTrackerUrl, a.BugTrackerNewUrl ");
        query.append(" from testcaseexecution tce ");
        query.append(" inner join testcase tc on tce.test = tc.test and tce.testcase = tc.testcase ");
        query.append(" inner join application a on tce.application = a.application ");

        String testClause = SqlUtil.generateInClause("tce.test", testList);
        if (!StringUtil.isNullOrEmpty(testClause)) {
            whereClauses.add(testClause);
        }

        String applicationClause = SqlUtil.generateInClause("tce.application", applicationList);
        if (!StringUtil.isNullOrEmpty(applicationClause)) {
            whereClauses.add(applicationClause);
        }

        String projectClause = SqlUtil.generateInClause("tc.project", projectList);
        if (!StringUtil.isNullOrEmpty(projectClause)) {
            whereClauses.add(projectClause);
        }
        //test case status: working, fully_implemented, ...
        String tcsClause = SqlUtil.generateInClause("tce.status", tcstatusList);
        if (!StringUtil.isNullOrEmpty(tcsClause)) {
            whereClauses.add(tcsClause);
        }

        //group 
        String groupClause = SqlUtil.generateInClause("tc.group", groupList);
        if (!StringUtil.isNullOrEmpty(groupClause)) {
            whereClauses.add(groupClause);
        }
        //test case active
        String tcactiveClause = SqlUtil.generateInClause("tc.tcactive", tcactiveList);
        if (!StringUtil.isNullOrEmpty(tcactiveClause)) {
            whereClauses.add(tcactiveClause);
        }

        //test case active
        String priorityClause = SqlUtil.generateInClause("tc.Priority", priorityList);
        if (!StringUtil.isNullOrEmpty(priorityClause)) {
            whereClauses.add(priorityClause);
        }

        //target sprint
        String targetsprintClause = SqlUtil.generateInClause("tc.TargetBuild", targetsprintList);
        if (!StringUtil.isNullOrEmpty(targetsprintClause)) {
            whereClauses.add(targetsprintClause);
        }

        //target revision
        String targetrevisionClause = SqlUtil.generateInClause("tc.TargetRev", targetrevisionList);
        if (!StringUtil.isNullOrEmpty(targetrevisionClause)) {
            whereClauses.add(targetrevisionClause);
        }

        //creator
        String creatorClause = SqlUtil.generateInClause("tc.Creator", creatorList);
        if (!StringUtil.isNullOrEmpty(creatorClause)) {
            whereClauses.add(creatorClause);
        }

        //implementer
        String implementerClause = SqlUtil.generateInClause("tc.Implementer", implementerList);
        if (!StringUtil.isNullOrEmpty(implementerClause)) {
            whereClauses.add(implementerClause);
        }

        //build
        String buildClause = SqlUtil.generateInClause("tce.Build", buildList);
        if (!StringUtil.isNullOrEmpty(buildClause)) {
            whereClauses.add(buildClause);
        }
        //revision
        String revisionClause = SqlUtil.generateInClause("tce.Revision", revisionList);
        if (!StringUtil.isNullOrEmpty(revisionClause)) {
            whereClauses.add(revisionClause);
        }
        //environment
        String environmentClause = SqlUtil.generateInClause("tce.Environment", environmentList);
        if (!StringUtil.isNullOrEmpty(environmentClause)) {
            whereClauses.add(environmentClause);
        }
        //country
        String countryClause = SqlUtil.generateInClause("tce.Country", countryList);
        if (!StringUtil.isNullOrEmpty(countryClause)) {
            whereClauses.add(countryClause);
        }
        //browser
        String browserClause = SqlUtil.generateInClause("tce.Browser", browserList);
        if (!StringUtil.isNullOrEmpty(browserClause)) {
            whereClauses.add(browserClause);
        }
        //test case execution
        String tcestatusClause = SqlUtil.generateInClause("tce.ControlStatus", tcestatusList);
        if (!StringUtil.isNullOrEmpty(tcestatusClause)) {
            whereClauses.add(tcestatusClause);
        }

        if (!StringUtil.isNullOrEmpty(system)) {
            whereClauses.add(" a.system like ? ");
        }
        if (!StringUtil.isNullOrEmpty(ip)) {
            whereClauses.add(" tce.IP like ? ");
        }
        if (!StringUtil.isNullOrEmpty(port)) {
            whereClauses.add(" tce.port like ? ");
        }
        if (!StringUtil.isNullOrEmpty(tag)) {
            whereClauses.add(" tce.tag like ? ");
        }
        if (!StringUtil.isNullOrEmpty(browserversion)) {
            whereClauses.add(" tce.browserfullversion like ? ");
        }
        if (!StringUtil.isNullOrEmpty(comment)) {
            whereClauses.add(" tce.comment like ? ");
        }
        if (!StringUtil.isNullOrEmpty(bugid)) {
            whereClauses.add(" tc.BugID like ? ");
        }
        if (!StringUtil.isNullOrEmpty(ticket)) {
            whereClauses.add(" tc.Ticket like ? ");
        }

        if (whereClauses.size() > 0) {
            query.append("where ");
            String joined = StringUtils.join(whereClauses, " and ");
            query.append(joined);
        }

        query.append(" order by tce.ID desc ");
        query.append(" ) as t group by t.test, t.testcase, t.environment, t.browser, t.country");
        Connection connection = this.databaseSpring.connect();

        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            if (testList != null) {
                for (String param : testList) {
                    preStat.setString(++paramNumber, param);
                }
            }
            if (applicationList != null) {
                for (String param : applicationList) {
                    preStat.setString(++paramNumber, param);
                }
            }
            if (projectList != null) {
                for (String param : projectList) {
                    preStat.setString(++paramNumber, param);
                }
            }
            if (tcstatusList != null) {
                for (String param : tcstatusList) {
                    preStat.setString(++paramNumber, param);
                }
            }
            if (groupList != null) {
                for (String param : groupList) {
                    preStat.setString(++paramNumber, param);
                }
            }

            if (tcactiveList != null) {
                for (String param : tcactiveList) {
                    preStat.setString(++paramNumber, param);
                }
            }
            if (priorityList != null) {
                for (String param : priorityList) {
                    preStat.setString(++paramNumber, param);
                }
            }
            if (targetsprintList != null) {
                for (String param : targetsprintList) {
                    preStat.setString(++paramNumber, param);
                }
            }
            if (targetrevisionList != null) {
                for (String param : targetrevisionList) {
                    preStat.setString(++paramNumber, param);
                }
            }
            if (creatorList != null) {
                for (String param : creatorList) {
                    preStat.setString(++paramNumber, param);
                }
            }
            if (implementerList != null) {
                for (String param : implementerList) {
                    preStat.setString(++paramNumber, param);
                }
            }
            if (buildList != null) {
                for (String param : buildList) {
                    preStat.setString(++paramNumber, param);
                }
            }
            if (revisionList != null) {
                for (String param : revisionList) {
                    preStat.setString(++paramNumber, param);
                }
            }
            //environment
            if (environmentList != null) {
                for (String param : environmentList) {
                    preStat.setString(++paramNumber, param);
                }
            }
            //country
            if (countryList != null) {
                for (String param : countryList) {
                    preStat.setString(++paramNumber, param);
                }
            }
            //browser            
            if (browserList != null) {
                for (String param : browserList) {
                    preStat.setString(++paramNumber, param);
                }
            }
            //controlstatus
            if (tcestatusList != null) {
                for (String param : tcestatusList) {
                    preStat.setString(++paramNumber, param);
                }
            }

            if (!StringUtil.isNullOrEmpty(system)) {
                preStat.setString(++paramNumber, system);
            }

            if (!StringUtil.isNullOrEmpty(ip)) {
                preStat.setString(++paramNumber, "%" + ip + "%");
            }
            if (!StringUtil.isNullOrEmpty(port)) {
                preStat.setString(++paramNumber, "%" + port + "%");
            }
            if (!StringUtil.isNullOrEmpty(tag)) {
                preStat.setString(++paramNumber, "%" + tag + "%");
            }
            if (!StringUtil.isNullOrEmpty(browserversion)) {
                preStat.setString(++paramNumber, "%" + browserversion + "%");
            }
            if (!StringUtil.isNullOrEmpty(comment)) {
                preStat.setString(++paramNumber, "%" + comment + "%");
            }
            if (!StringUtil.isNullOrEmpty(bugid)) {
                preStat.setString(++paramNumber, "%" + bugid + "%");
            }
            if (!StringUtil.isNullOrEmpty(ticket)) {
                preStat.setString(++paramNumber, "%" + ticket + "%");
            }

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        tceList.add(loadTestCaseWithExecutionFromResultSet(resultSet));
                    }
                    if (tceList.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecution").replace("%OPERATION%", "SELECT"));
                    }

                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    tceList.clear();
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException ex) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.WARN, ex.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }
        answer.setTotalRows(tceList.size());
        answer.setDataList(tceList);
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerItem readByKey(long executionId) {
        AnswerItem ans = new AnswerItem();
        TestCaseExecution result = null;
        final String query = "SELECT * FROM `testcaseexecution` WHERE `id` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setLong(1, executionId);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = loadFromResultSet(resultSet);
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        ans.setItem(result);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
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

        //sets the message
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public TestCaseExecution loadFromResultSet(ResultSet resultSet) throws SQLException {
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
        long start = resultSet.getTimestamp("start").getTime();
        long end = resultSet.getTimestamp("end") == null ? 0 : resultSet.getTimestamp("end").getTime();
        String controlStatus = resultSet.getString("controlStatus");
        String controlMessage = resultSet.getString("controlMessage");
        String application = resultSet.getString("application");
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
                country, browser, version, platform, browserFullVersion, start, end, controlStatus, controlMessage, null, ip, url,
                port, tag, finished, verbose, 0, 0, 0, true, "", "", status, crbVersion, null, null, null,
                false, null, null, null, null, null, null, null, null, executor, 0, screenSize);
    }

}
