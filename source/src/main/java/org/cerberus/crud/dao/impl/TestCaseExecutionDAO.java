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

import com.google.common.base.Strings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.crud.dao.IApplicationDAO;
import org.cerberus.crud.dao.ITestCaseDAO;
import org.cerberus.crud.dao.ITestCaseExecutionDAO;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.factory.IFactoryTestCaseExecution;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.util.DateUtil;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TestCaseExecutionDAO implements ITestCaseExecutionDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseExecution factoryTCExecution;
    @Autowired
    private IApplicationDAO applicationDAO;
    @Autowired
    private ITestCaseDAO testCaseDAO;
    @Autowired
    private IParameterService parameterService;

    private static final Logger LOG = Logger.getLogger(TestCaseExecutionDAO.class);

    private final String OBJECT_NAME = "TestCase Execution";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public long insertTCExecution(TestCaseExecution tCExecution) throws CerberusException {
        boolean throwEx = false;
        final String query = "INSERT INTO testcaseexecution(test, testcase, build, revision, environment, environmentData, country, browser, application, ip, "
                + "url, port, tag, verbose, status, start, controlstatus, controlMessage, crbversion, finished, browserFullVersion, executor, screensize,"
                + "conditionOper, conditionVal1Init, conditionVal2Init, conditionVal1, conditionVal2, manualExecution) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            try {
                int i = 1;
                preStat.setString(i++, tCExecution.getTest());
                preStat.setString(i++, tCExecution.getTestCase());
                preStat.setString(i++, tCExecution.getBuild());
                preStat.setString(i++, tCExecution.getRevision());
                preStat.setString(i++, tCExecution.getEnvironment());
                preStat.setString(i++, tCExecution.getEnvironmentData());
                preStat.setString(i++, tCExecution.getCountry());
                preStat.setString(i++, tCExecution.getBrowser());
                preStat.setString(i++, tCExecution.getApplicationObj().getApplication());
                preStat.setString(i++, tCExecution.getIp());
                preStat.setString(i++, tCExecution.getUrl());
                preStat.setString(i++, tCExecution.getPort());
                preStat.setString(i++, tCExecution.getTag());
                preStat.setInt(i++, tCExecution.getVerbose());
                preStat.setString(i++, tCExecution.getStatus());
                preStat.setTimestamp(i++, new Timestamp(tCExecution.getStart()));
                preStat.setString(i++, tCExecution.getControlStatus());
                preStat.setString(i++, StringUtil.getLeftString(tCExecution.getControlMessage(), 500));
                preStat.setString(i++, tCExecution.getCrbVersion());
                preStat.setString(i++, tCExecution.getFinished());
                preStat.setString(i++, tCExecution.getBrowserFullVersion());
                preStat.setString(i++, tCExecution.getExecutor());
                preStat.setString(i++, tCExecution.getScreenSize());
                preStat.setString(i++, tCExecution.getConditionOper());
                preStat.setString(i++, tCExecution.getConditionVal1Init());
                preStat.setString(i++, tCExecution.getConditionVal2Init());
                preStat.setString(i++, tCExecution.getConditionVal1());
                preStat.setString(i++, tCExecution.getConditionVal2());
                preStat.setString(i++, tCExecution.isManualExecution() ? "Y" : "N");

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
        final String query = "UPDATE testcaseexecution SET test = ?, testcase = ?, build = ?, revision = ?, environment = ?, environmentData = ?, country = ?"
                + ", browser = ?, application = ?, ip = ?, url = ?, port = ?, tag = ?, verbose = ?, status = ?"
                + ", start = ?, end = ? , controlstatus = ?, controlMessage = ?, crbversion = ?, finished = ? "
                + ", browserFullVersion = ?, version = ?, platform = ?, executor = ?, screensize = ? "
                + ", ConditionOper = ?, ConditionVal1Init = ?, ConditionVal2Init = ?, ConditionVal1 = ?, ConditionVal2 = ?, ManualExecution = ? WHERE id = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setString(i++, tCExecution.getTest());
                preStat.setString(i++, tCExecution.getTestCase());
                preStat.setString(i++, tCExecution.getBuild());
                preStat.setString(i++, tCExecution.getRevision());
                preStat.setString(i++, tCExecution.getEnvironment());
                preStat.setString(i++, tCExecution.getEnvironmentData());
                preStat.setString(i++, tCExecution.getCountry());
                preStat.setString(i++, tCExecution.getBrowser());
                preStat.setString(i++, tCExecution.getApplicationObj().getApplication());
                preStat.setString(i++, tCExecution.getIp());
                preStat.setString(i++, tCExecution.getUrl());
                preStat.setString(i++, tCExecution.getPort());
                preStat.setString(i++, tCExecution.getTag());
                preStat.setInt(i++, tCExecution.getVerbose());
                preStat.setString(i++, tCExecution.getStatus());
                preStat.setTimestamp(i++, new Timestamp(tCExecution.getStart()));
                if (tCExecution.getEnd() != 0) {
                    preStat.setTimestamp(i++, new Timestamp(tCExecution.getEnd()));
                } else {
                    preStat.setString(i++, "1970-01-01 01:01:01");
                }
                preStat.setString(i++, tCExecution.getControlStatus());
                preStat.setString(i++, StringUtil.getLeftString(tCExecution.getControlMessage(), 500));
                preStat.setString(i++, tCExecution.getCrbVersion());
                preStat.setString(i++, tCExecution.getFinished());
                preStat.setString(i++, tCExecution.getBrowserFullVersion());
                preStat.setString(i++, tCExecution.getVersion());
                preStat.setString(i++, tCExecution.getPlatform());
                preStat.setString(i++, tCExecution.getExecutor());
                preStat.setString(i++, tCExecution.getScreenSize());
                preStat.setString(i++, tCExecution.getConditionOper());
                preStat.setString(i++, tCExecution.getConditionVal1Init());
                preStat.setString(i++, tCExecution.getConditionVal2Init());
                preStat.setString(i++, tCExecution.getConditionVal1());
                preStat.setString(i++, tCExecution.getConditionVal2());
                preStat.setString(i++, tCExecution.isManualExecution() ? "Y" : "N");
                preStat.setLong(i++, tCExecution.getId());

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
        query.append("select * from testcaseexecution exe ");

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
        final String query = new StringBuffer("SELECT exe.* FROM testcaseexecution exe ")
                .append("WHERE exe.test = ? AND exe.testcase = ? AND exe.environment = ? ")
                .append("AND exe.country = ? AND exe.build = ? AND exe.revision = ? ")
                .append("ORDER BY exe.id DESC").toString();

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
        final String query = new StringBuffer("SELECT exe.* FROM testcaseexecution exe ")
                .append("WHERE exe.test = ? AND exe.testcase = ? ")
                .append("AND exe.environment LIKE ? AND exe.country = ? AND exe.build LIKE ? ")
                .append("AND exe.revision LIKE ? AND exe.browser = ? AND exe.browserfullversion LIKE ? ")
                .append("AND exe.ip LIKE ? AND exe.port LIKE ? AND exe.tag LIKE ? ")
                .append("ORDER BY exe.id DESC").toString();

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
        final String query = new StringBuffer("SELECT exe.*, tec.*, app.* FROM testcaseexecution exe ")
                .append("LEFT JOIN testcase tec ON exe.test = tec.test AND exe.testcase = tec.testcase ")
                .append("LEFT JOIN application app ON exe.application = app.application ")
                .append("WHERE exe.start > ? AND exe.test LIKE ? AND exe.testcase LIKE ? AND exe.environment LIKE ? ")
                .append("AND exe.country LIKE ? AND exe.application LIKE ? AND exe.controlstatus LIKE ? ")
                .append("AND exe.status LIKE ?").toString();

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
                            Execution = this.loadWithDependenciesFromResultSet(resultSet);

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

    @Override
    public TestCaseExecution findTCExecutionByKey(long id) throws CerberusException {
        TestCaseExecution result = null;
        final String query = "SELECT * FROM testcaseexecution exe, application app WHERE exe.application = app.application AND ID = ?";

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

        final String query = new StringBuffer("select exe.*, app.* from ( ")
                .append("select exe.* ")
                .append("from testcaseexecution exe ")
                .append("inner join testbatterycontent tbc ")
                .append("on tbc.Test = exe.Test ")
                .append("and tbc.TestCase = exe.TestCase ")
                .append("inner join campaigncontent cc ")
                .append("on cc.testbattery = tbc.testbattery ")
                .append("where tag is not null ")
                .append("and cc.campaign = ? ")
                .append("and tag = ? ")
                .append("order by test, testcase, ID desc) as exe, application app ")
                .append("where exe.application = app.application ")
                .append("group by exe.test, exe.testcase, exe.Environment, exe.Browser, exe.Country ").toString();

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
        query.append("SELECT exe.*  FROM `testcaseexecution` exe ");
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
                        result = loadFromResultSet(resultSet);
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
        query.append("SELECT exe.* FROM testcaseexecution exe ")
                .append("WHERE exe.test = ? AND exe.testcase = ? AND exe.country = ? AND exe.browser = ? ");
        if (!StringUtil.isNull(environment)) {
            query.append("AND exe.environment IN (");
            query.append(environment);
            query.append(") ");
        }
        if (!StringUtil.isNull(build)) {
            query.append("AND exe.build IN (");
            query.append(build);
            query.append(") ");
        }
        if (!StringUtil.isNull(revision)) {
            query.append("AND exe.revision IN (");
            query.append(revision);
            query.append(") ");
        }
        if (!StringUtil.isNull(browserVersion)) {
            query.append("AND exe.browserfullversion LIKE ? ");
        }
        if (!StringUtil.isNull(ip)) {
            query.append("AND exe.ip LIKE ? ");
        }
        if (!StringUtil.isNull(port)) {
            query.append("AND exe.port LIKE ? ");
        }
        if (!StringUtil.isNull(tag)) {
            query.append("AND exe.tag LIKE ? ");
        }
        query.append("ORDER BY exe.id DESC");

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
        query.append("select distinct tag from testcaseexecution exe ")
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

        query.append("SELECT DISTINCT exe.tag FROM testcaseexecution exe WHERE tag != ''");

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
                        list.add(resultSet.getString("exe.tag"));
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
        final String query = "UPDATE testcaseexecution exe SET exe.tag = ? WHERE exe.id = ?";

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
    public AnswerList readByTagByCriteria(String tag, int start, int amount, String sort, String searchTerm, Map<String, List<String>> individualSearch) throws CerberusException {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        AnswerList answer = new AnswerList();
        List<String> individalColumnSearchValues = new ArrayList<String>();

        final StringBuffer query = new StringBuffer();

        query.append("SELECT * FROM testcaseexecution exe ");
        query.append("left join testcase tec on exe.Test = tec.Test and exe.TestCase = tec.TestCase ");
        query.append("left join application app on tec.application = app.application ");
        query.append("where exe.ID IN ");
        query.append("(select MAX(exe.ID) from testcaseexecution exe ");

        query.append("where 1=1 ");
        if (!StringUtil.isNullOrEmpty(tag)) {
            query.append("and exe.tag = ? ");
        }

        query.append("group by exe.test, exe.testcase, exe.Environment, exe.Browser, exe.Country) ");
        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            query.append("and (exe.`test` like ? ");
            query.append(" or exe.`testCase` like ? ");
            query.append(" or exe.`application` like ? ");
            query.append(" or tec.`bugid` like ? ");
            query.append(" or tec.`priority` like ? ");
            query.append(" or tec.`description` like ? )");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            query.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                query.append(" and ");
                query.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individalColumnSearchValues.addAll(entry.getValue());
            }
            query.append(" ) ");
        }

        if (!StringUtil.isNullOrEmpty(sort)) {
            query.append(" order by ").append(sort);
        }

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param.tag : " + tag);
        }
        List<TestCaseExecution> testCaseExecutionList = new ArrayList<TestCaseExecution>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            int i = 1;
            if (!StringUtil.isNullOrEmpty(tag)) {
                preStat.setString(i++, tag);
            }
            if (!Strings.isNullOrEmpty(searchTerm)) {
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
            }
            for (String individualColumnSearchValue : individalColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testCaseExecutionList.add(this.loadWithDependenciesFromResultSet(resultSet));
                    }
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecution").replace("%OPERATION%", "SELECT"));
//                    answer = new AnswerList(testCaseExecutionList, testCaseExecutionList.size());
                    answer.setTotalRows(testCaseExecutionList.size());
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    testCaseExecutionList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                testCaseExecutionList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            testCaseExecutionList = null;
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

        answer.setResultMessage(msg);
        answer.setDataList(testCaseExecutionList);
        return answer;
    }

    @Override
    public AnswerList readByCriteria(int start, int amount, String sort, String searchTerm, Map<String, List<String>> individualSearch) throws CerberusException {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        AnswerList answer = new AnswerList();
        List<String> individalColumnSearchValues = new ArrayList<String>();

        final StringBuffer query = new StringBuffer();

        query.append("SELECT * FROM testcaseexecution exe ");
        query.append("where exe.`start`> '").append(DateUtil.getMySQLTimestampTodayDeltaMinutes(-360000)).append("' ");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            query.append("and (exe.`id` like ? ");
            query.append(" or exe.`test` like ? ");
            query.append(" or exe.`testCase` like ? ");
            query.append(" or exe.`build` like ? ");
            query.append(" or exe.`revision` like ? ");
            query.append(" or exe.`environment` like ? ");
            query.append(" or exe.`country` like ? ");
            query.append(" or exe.`browser` like ? ");
            query.append(" or exe.`version` like ? ");
            query.append(" or exe.`platform` like ? ");
            query.append(" or exe.`browserfullversion` like ? ");
            query.append(" or exe.`start` like ? ");
            query.append(" or exe.`end` like ? ");
            query.append(" or exe.`controlstatus` like ? ");
            query.append(" or exe.`controlmessage` like ? ");
            query.append(" or exe.`application` like ? ");
            query.append(" or exe.`ip` like ? ");
            query.append(" or exe.`url` like ? ");
            query.append(" or exe.`port` like ? ");
            query.append(" or exe.`tag` like ? ");
            query.append(" or exe.`finished` like ? ");
            query.append(" or exe.`verbose` like ? ");
            query.append(" or exe.`status` like ? ");
            query.append(" or exe.`crbversion` like ? ");
            query.append(" or exe.`executor` like ? ");
            query.append(" or exe.`screensize` like ? )");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            query.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                query.append(" and ");
                query.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individalColumnSearchValues.addAll(entry.getValue());
            }
            query.append(" ) ");
        }

        if (!StringUtil.isNullOrEmpty(sort)) {
            query.append(" order by ").append(sort);
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
        List<TestCaseExecution> testCaseExecutionList = new ArrayList<TestCaseExecution>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            int i = 1;
            if (!Strings.isNullOrEmpty(searchTerm)) {
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
            }
            for (String individualColumnSearchValue : individalColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testCaseExecutionList.add(this.loadFromResultSet(resultSet));
                    }
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecution").replace("%OPERATION%", "SELECT"));
//                    answer = new AnswerList(testCaseExecutionList, testCaseExecutionList.size());
                    answer.setTotalRows(testCaseExecutionList.size());
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    testCaseExecutionList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                testCaseExecutionList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            testCaseExecutionList = null;
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

        answer.setResultMessage(msg);
        answer.setDataList(testCaseExecutionList);
        return answer;
    }

    @Override
    public AnswerList readDistinctEnvCoutnryBrowserByTag(String tag) {
        AnswerList answer = new AnswerList();
        StringBuilder query = new StringBuilder();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);

        query.append("SELECT exe.* FROM testcaseexecution exe WHERE exe.tag = ? GROUP BY exe.Environment, exe.Country, exe.Browser, exe.ControlStatus");

        Connection connection = this.databaseSpring.connect();

        List<TestCaseExecution> testCaseExecutionList = new ArrayList<TestCaseExecution>();

        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());

            preStat.setString(1, tag);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testCaseExecutionList.add(this.loadFromResultSet(resultSet));
                    }
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecution").replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList(testCaseExecutionList, testCaseExecutionList.size());
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    testCaseExecutionList = null;
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestCaseExecutionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                testCaseExecutionList = null;
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
            query.append(" FROM testcaseexecution exe WHERE exe.tag = ? GROUP BY ");
            query.append(distinct.toString());
        } else {
            //If there is no distinct, select nothing
            query.append("SELECT * FROM testcaseexecution exe WHERE 1 = 0 AND exe.tag = ?");
        }

        Connection connection = this.databaseSpring.connect();

        List<TestCaseExecution> column = new ArrayList<TestCaseExecution>();

        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());

            preStat.setString(1, tag);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        TestCaseExecution tmp = new TestCaseExecution();
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

    @Override
    public AnswerList readBySystemByVarious(String system, List<String> testList, List<String> applicationList, List<String> projectList, List<String> tcstatusList,
            List<String> groupList, List<String> tcactiveList, List<String> priorityList, List<String> targetsprintList, List<String> targetrevisionList,
            List<String> creatorList, List<String> implementerList, List<String> buildList, List<String> revisionList, List<String> environmentList,
            List<String> countryList, List<String> browserList, List<String> tcestatusList, String ip, String port, String tag, String browserversion,
            String comment, String bugid, String ticket) {

        AnswerList answer = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        List<TestCaseExecution> tceList = new ArrayList<TestCaseExecution>();
        List<String> whereClauses = new LinkedList<String>();

        StringBuilder query = new StringBuilder();

        int paramNumber = 0;

        query.append(" select t.ID as statusExecutionID, t.* from ( ");
        query.append(" select exe.*, tec.*, app.* ");
        query.append(" from testcaseexecution exe ");
        query.append(" inner join testcase tec on exe.test = tec.test and exe.testcase = tec.testcase ");
        query.append(" inner join application app on exe.application = app.application ");

        String testClause = SqlUtil.generateInClause("exe.test", testList);
        if (!StringUtil.isNullOrEmpty(testClause)) {
            whereClauses.add(testClause);
        }

        String applicationClause = SqlUtil.generateInClause("exe.application", applicationList);
        if (!StringUtil.isNullOrEmpty(applicationClause)) {
            whereClauses.add(applicationClause);
        }

        String projectClause = SqlUtil.generateInClause("tec.project", projectList);
        if (!StringUtil.isNullOrEmpty(projectClause)) {
            whereClauses.add(projectClause);
        }
        //test case status: working, fully_implemented, ...
        String tcsClause = SqlUtil.generateInClause("exe.status", tcstatusList);
        if (!StringUtil.isNullOrEmpty(tcsClause)) {
            whereClauses.add(tcsClause);
        }

        //group 
        String groupClause = SqlUtil.generateInClause("tec.group", groupList);
        if (!StringUtil.isNullOrEmpty(groupClause)) {
            whereClauses.add(groupClause);
        }
        //test case active
        String tcactiveClause = SqlUtil.generateInClause("tec.tcactive", tcactiveList);
        if (!StringUtil.isNullOrEmpty(tcactiveClause)) {
            whereClauses.add(tcactiveClause);
        }

        //test case active
        String priorityClause = SqlUtil.generateInClause("tec.Priority", priorityList);
        if (!StringUtil.isNullOrEmpty(priorityClause)) {
            whereClauses.add(priorityClause);
        }

        //target sprint
        String targetsprintClause = SqlUtil.generateInClause("tec.TargetBuild", targetsprintList);
        if (!StringUtil.isNullOrEmpty(targetsprintClause)) {
            whereClauses.add(targetsprintClause);
        }

        //target revision
        String targetrevisionClause = SqlUtil.generateInClause("tec.TargetRev", targetrevisionList);
        if (!StringUtil.isNullOrEmpty(targetrevisionClause)) {
            whereClauses.add(targetrevisionClause);
        }

        //creator
        String creatorClause = SqlUtil.generateInClause("tec.UsrCreated", creatorList);
        if (!StringUtil.isNullOrEmpty(creatorClause)) {
            whereClauses.add(creatorClause);
        }

        //implementer
        String implementerClause = SqlUtil.generateInClause("tec.Implementer", implementerList);
        if (!StringUtil.isNullOrEmpty(implementerClause)) {
            whereClauses.add(implementerClause);
        }

        //build
        String buildClause = SqlUtil.generateInClause("exe.Build", buildList);
        if (!StringUtil.isNullOrEmpty(buildClause)) {
            whereClauses.add(buildClause);
        }
        //revision
        String revisionClause = SqlUtil.generateInClause("exe.Revision", revisionList);
        if (!StringUtil.isNullOrEmpty(revisionClause)) {
            whereClauses.add(revisionClause);
        }
        //environment
        String environmentClause = SqlUtil.generateInClause("exe.Environment", environmentList);
        if (!StringUtil.isNullOrEmpty(environmentClause)) {
            whereClauses.add(environmentClause);
        }
        //country
        String countryClause = SqlUtil.generateInClause("exe.Country", countryList);
        if (!StringUtil.isNullOrEmpty(countryClause)) {
            whereClauses.add(countryClause);
        }
        //browser
        String browserClause = SqlUtil.generateInClause("exe.Browser", browserList);
        if (!StringUtil.isNullOrEmpty(browserClause)) {
            whereClauses.add(browserClause);
        }
        //test case execution
        String tcestatusClause = SqlUtil.generateInClause("exe.ControlStatus", tcestatusList);
        if (!StringUtil.isNullOrEmpty(tcestatusClause)) {
            whereClauses.add(tcestatusClause);
        }

        if (!StringUtil.isNullOrEmpty(system)) {
            whereClauses.add(" app.system like ? ");
        }
        if (!StringUtil.isNullOrEmpty(ip)) {
            whereClauses.add(" exe.IP like ? ");
        }
        if (!StringUtil.isNullOrEmpty(port)) {
            whereClauses.add(" exe.port like ? ");
        }
        if (!StringUtil.isNullOrEmpty(tag)) {
            whereClauses.add(" exe.tag like ? ");
        }
        if (!StringUtil.isNullOrEmpty(browserversion)) {
            whereClauses.add(" exe.browserfullversion like ? ");
        }
        if (!StringUtil.isNullOrEmpty(comment)) {
            whereClauses.add(" exe.comment like ? ");
        }
        if (!StringUtil.isNullOrEmpty(bugid)) {
            whereClauses.add(" tec.BugID like ? ");
        }
        if (!StringUtil.isNullOrEmpty(ticket)) {
            whereClauses.add(" tec.Ticket like ? ");
        }

        if (whereClauses.size() > 0) {
            query.append("where ");
            String joined = StringUtils.join(whereClauses, " and ");
            query.append(joined);
        }

        query.append(" order by exe.ID desc ");
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
                        tceList.add(loadWithDependenciesFromResultSet(resultSet));
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
        final String query = "SELECT * FROM `testcaseexecution` exe WHERE exe.`id` = ?";
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
        long id = ParameterParserUtil.parseLongParam(resultSet.getString("exe.ID"), 0);
        String test = ParameterParserUtil.parseStringParam(resultSet.getString("exe.test"), "");
        String testcase = ParameterParserUtil.parseStringParam(resultSet.getString("exe.testcase"), "");
        String build = ParameterParserUtil.parseStringParam(resultSet.getString("exe.build"), "");
        String revision = ParameterParserUtil.parseStringParam(resultSet.getString("exe.revision"), "");
        String environment = ParameterParserUtil.parseStringParam(resultSet.getString("exe.environment"), "");
        String environmentData = ParameterParserUtil.parseStringParam(resultSet.getString("exe.environmentData"), "");
        String country = ParameterParserUtil.parseStringParam(resultSet.getString("exe.country"), "");
        String browser = ParameterParserUtil.parseStringParam(resultSet.getString("exe.browser"), "");
        String version = ParameterParserUtil.parseStringParam(resultSet.getString("exe.version"), "");
        String platform = ParameterParserUtil.parseStringParam(resultSet.getString("exe.platform"), "");
        String browserFullVersion = ParameterParserUtil.parseStringParam(resultSet.getString("exe.browserFullVersion"), "");
        long start = ParameterParserUtil.parseLongParam(String.valueOf(resultSet.getTimestamp("exe.start").getTime()), 0);
        long end = ParameterParserUtil.parseLongParam(String.valueOf(resultSet.getTimestamp("exe.end").getTime()), 0);
        String controlStatus = ParameterParserUtil.parseStringParam(resultSet.getString("exe.controlStatus"), "");
        String controlMessage = ParameterParserUtil.parseStringParam(resultSet.getString("exe.controlMessage"), "");
        String application = ParameterParserUtil.parseStringParam(resultSet.getString("exe.application"), "");
        String ip = ParameterParserUtil.parseStringParam(resultSet.getString("exe.ip"), ""); // Host the Selenium IP
        String url = ParameterParserUtil.parseStringParam(resultSet.getString("exe.url"), "");
        String port = ParameterParserUtil.parseStringParam(resultSet.getString("exe.port"), ""); // host the Selenium Port
        String tag = ParameterParserUtil.parseStringParam(resultSet.getString("exe.tag"), "");
        String finished = ParameterParserUtil.parseStringParam(resultSet.getString("exe.finished"), "");
        int verbose = ParameterParserUtil.parseIntegerParam(resultSet.getString("exe.verbose"), 0);
        String status = ParameterParserUtil.parseStringParam(resultSet.getString("exe.status"), "");
        String crbVersion = ParameterParserUtil.parseStringParam(resultSet.getString("exe.crbVersion"), "");
        String executor = ParameterParserUtil.parseStringParam(resultSet.getString("exe.executor"), "");
        String screenSize = ParameterParserUtil.parseStringParam(resultSet.getString("exe.screensize"), "");
        String conditionOper = ParameterParserUtil.parseStringParam(resultSet.getString("exe.conditionOper"), "");
        String conditionVal1 = ParameterParserUtil.parseStringParam(resultSet.getString("exe.conditionVal1"), "");
        String conditionVal1Init = ParameterParserUtil.parseStringParam(resultSet.getString("exe.conditionVal1Init"), "");
        String conditionVal2 = ParameterParserUtil.parseStringParam(resultSet.getString("exe.conditionVal2"), "");
        String conditionVal2Init = ParameterParserUtil.parseStringParam(resultSet.getString("exe.conditionVal2Init"), "");
        boolean manualExecution = ParameterParserUtil.parseBooleanParam(resultSet.getString("exe.manualExecution"), false);
        TestCaseExecution result = factoryTCExecution.create(id, test, testcase, build, revision, environment,
                country, browser, version, platform, browserFullVersion, start, end, controlStatus, controlMessage, application, null, ip, url,
                port, tag, finished, verbose, 0, 0, 0, true, "", "", status, crbVersion, null, null, null,
                false, null, null, null, environmentData, null, null, null, null, executor, 0, screenSize, null,
                conditionOper, conditionVal1Init, conditionVal2Init, conditionVal1, conditionVal2, manualExecution);
        return result;
    }

    /**
     * Uses data of ResultSet to create object {@link TestCaseExecution}
     *
     * @param resultSet ResultSet relative to select from table
     * TestCaseExecution
     * @return object {@link TestCaseExecution} with objects {@link TestCase}
     * and {@link Application}
     * @throws SQLException when trying to get value from
     * {@link java.sql.ResultSet#getString(String)}
     * @see FactoryTestCaseExecution
     */
    private TestCaseExecution loadWithDependenciesFromResultSet(ResultSet resultSet) throws SQLException {
        TestCaseExecution testCaseExecution = new TestCaseExecution();
        testCaseExecution = this.loadFromResultSet(resultSet);
        testCaseExecution.setTestCaseObj(testCaseDAO.loadFromResultSet(resultSet));
        testCaseExecution.setApplicationObj(applicationDAO.loadFromResultSet(resultSet));
        return testCaseExecution;
    }

    private TestCaseExecution loadTestCaseExecutionAndApplicationFromResultSet(ResultSet resultSet) throws SQLException {
        TestCaseExecution testCaseExecution = new TestCaseExecution();
        testCaseExecution = this.loadFromResultSet(resultSet);
        testCaseExecution.setApplicationObj(applicationDAO.loadFromResultSet(resultSet));
        return testCaseExecution;
    }

    @Override
    public AnswerList<List<String>> readDistinctValuesByCriteria(String system, String test, String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        AnswerList answer = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        List<String> individalColumnSearchValues = new ArrayList<String>();

        final StringBuffer query = new StringBuffer();
        
        query.append("SELECT distinct ");
        query.append(columnName);
        query.append(" as distinctValues FROM testcaseexecution exe ");
        query.append("where exe.`start`> '").append(DateUtil.getMySQLTimestampTodayDeltaMinutes(-360000)).append("' ");
        
        if (!StringUtil.isNullOrEmpty(searchParameter)) {
            query.append("and (exe.`id` like ? ");
            query.append(" or exe.`test` like ? ");
            query.append(" or exe.`testCase` like ? ");
            query.append(" or exe.`build` like ? ");
            query.append(" or exe.`revision` like ? ");
            query.append(" or exe.`environment` like ? ");
            query.append(" or exe.`country` like ? ");
            query.append(" or exe.`browser` like ? ");
            query.append(" or exe.`version` like ? ");
            query.append(" or exe.`platform` like ? ");
            query.append(" or exe.`browserfullversion` like ? ");
            query.append(" or exe.`start` like ? ");
            query.append(" or exe.`end` like ? ");
            query.append(" or exe.`controlstatus` like ? ");
            query.append(" or exe.`controlmessage` like ? ");
            query.append(" or exe.`application` like ? ");
            query.append(" or exe.`ip` like ? ");
            query.append(" or exe.`url` like ? ");
            query.append(" or exe.`port` like ? ");
            query.append(" or exe.`tag` like ? ");
            query.append(" or exe.`finished` like ? ");
            query.append(" or exe.`verbose` like ? ");
            query.append(" or exe.`status` like ? ");
            query.append(" or exe.`crbversion` like ? ");
            query.append(" or exe.`executor` like ? ");
            query.append(" or exe.`screensize` like ? )");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            query.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                query.append(" and ");
                query.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individalColumnSearchValues.addAll(entry.getValue());
            }
            query.append(" ) ");
        }

        query.append(" order by ").append(columnName).append(" asc");

        
        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        
        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString())) {
            
            int i = 1;
            if (!Strings.isNullOrEmpty(searchParameter)) {
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
            }
            for (String individualColumnSearchValue : individalColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }

            
                ResultSet resultSet = preStat.executeQuery();
                
                    while (resultSet.next()) {
                distinctValues.add(resultSet.getString("distinctValues") == null ? "" : resultSet.getString("distinctValues"));
            }

                //get the total number of rows
            resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
            int nrTotalRows = 0;

            if (resultSet != null && resultSet.next()) {
                nrTotalRows = resultSet.getInt(1);
            }

            if (distinctValues.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                LOG.error("Partial Result in the query.");
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                answer = new AnswerList(distinctValues, nrTotalRows);
            } else if (distinctValues.size() <= 0) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                answer = new AnswerList(distinctValues, nrTotalRows);
            } else {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                answer = new AnswerList(distinctValues, nrTotalRows);
            }
        } catch (Exception e) {
            LOG.warn("Unable to execute query : " + e.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            // We always set the result message
            answer.setResultMessage(msg);
        }

        answer.setResultMessage(msg);
        answer.setDataList(distinctValues);
        return answer;
        
        
            
    }

}
