/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
package org.cerberus.core.crud.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.IApplicationDAO;
import org.cerberus.core.crud.dao.ITestCaseDAO;
import org.cerberus.core.crud.dao.ITestCaseExecutionQueueDAO;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.TestCaseExecutionQueue;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecutionQueue;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.engine.queuemanagement.entity.TestCaseExecutionQueueToTreat;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.exception.FactoryCreationException;
import org.cerberus.core.util.SqlUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Repository
public class TestCaseExecutionQueueDAO implements ITestCaseExecutionQueueDAO {

    private static final Logger LOG = LogManager.getLogger(TestCaseExecutionQueueDAO.class);

    private static final String TABLE = "testcaseexecutionqueue";
    private static final String TABLE_TEST_CASE = "testcase";
    private static final String TABLE_APPLICATION = "application";

    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_SYSTEM = "System";
    private static final String COLUMN_TEST = "Test";
    private static final String COLUMN_TEST_CASE = "TestCase";
    private static final String COLUMN_COUNTRY = "Country";
    private static final String COLUMN_ENVIRONMENT = "Environment";
    private static final String COLUMN_ROBOT = "Robot";
    private static final String COLUMN_ROBOTDECLI = "RobotDecli";
    private static final String COLUMN_ROBOT_IP = "RobotIP";
    private static final String COLUMN_ROBOT_PORT = "RobotPort";
    private static final String COLUMN_BROWSER = "Browser";
    private static final String COLUMN_BROWSER_VERSION = "BrowserVersion";
    private static final String COLUMN_PLATFORM = "Platform";
    private static final String COLUMN_SCREENSIZE = "ScreenSize";
    private static final String COLUMN_MANUAL_URL = "ManualURL";
    private static final String COLUMN_MANUAL_HOST = "ManualHost";
    private static final String COLUMN_MANUAL_CONTEXT_ROOT = "ManualContextRoot";
    private static final String COLUMN_MANUAL_LOGIN_RELATIVE_URL = "ManualLoginRelativeURL";
    private static final String COLUMN_MANUAL_ENV_DATA = "ManualEnvData";
    private static final String COLUMN_TAG = "Tag";
    private static final String COLUMN_SCREENSHOT = "Screenshot";
    private static final String COLUMN_VIDEO = "Video";
    private static final String COLUMN_VERBOSE = "Verbose";
    private static final String COLUMN_TIMEOUT = "Timeout";
    private static final String COLUMN_PAGE_SOURCE = "PageSource";
    private static final String COLUMN_ROBOT_LOG = "RobotLog";
    private static final String COLUMN_CONSOLE_LOG = "ConsoleLog";
    private static final String COLUMN_REQUEST_DATE = "RequestDate";
    private static final String COLUMN_COMMENT = "Comment";
    private static final String COLUMN_RETRIES = "Retries";
    private static final String COLUMN_MANUAL_EXECUTION = "ManualExecution";
    private static final String COLUMN_STATE = "State";
    private static final String COLUMN_PRIORITY = "Priority";
    private static final String COLUMN_DEBUGFLAG = "DebugFlag";
    private static final String COLUMN_SELECTEDROBOTHOST = "SelectedRobotHost";
    private static final String COLUMN_SELECTEDROBOTEXTHOST = "SelectedExtensionHost";
    private static final String COLUMN_EXEID = "ExeId";
    private static final String COLUMN_USRCREATED = "UsrCreated";
    private static final String COLUMN_DATECREATED = "DateCreated";
    private static final String COLUMN_USRMODIF = "UsrModif";
    private static final String COLUMN_DATEMODIF = "DateModif";

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseExecutionQueue factoryTestCaseExecutionInQueue;
    @Autowired
    private ITestCaseDAO testCaseDAO;
    @Autowired
    private IApplicationDAO applicationDAO;

    private final int MAX_ROW_SELECTED = 100000;
    private final String SQL_DUPLICATED_CODE = "23000";
    private final String OBJECT_NAME = "TestCaseExecutionQueue";

    @Override
    public AnswerItem<TestCaseExecutionQueue> readByKey(long queueid) {
        AnswerItem<TestCaseExecutionQueue> ans = new AnswerItem<>();
        TestCaseExecutionQueue result = null;
        final String query = "SELECT * FROM `testcaseexecutionqueue` WHERE `ID` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.queueid : " + queueid);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            try {
                preStat.setLong(1, queueid);
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
                } catch (FactoryCreationException ex) {
                    LOG.error("Error in factory : " + ex.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ex.toString()));
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
    public AnswerList<Long> readMaxIdListByTag(String tag) throws CerberusException {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        AnswerList<Long> answer = new AnswerList<>();
        final StringBuilder query = new StringBuilder();

        query.append("SELECT MAX(exq.ID) from testcaseexecutionqueue exq ");
        query.append("where 1=1 ");
        if (!StringUtil.isEmptyOrNull(tag)) {
            query.append("and exq.tag = ? ");
        }
        query.append("group by exq.test, exq.testcase, exq.Environment, exq.Browser, exq.Country ");
        query.append(" limit ").append(MAX_ROW_SELECTED);

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param.tag : " + tag);
        }

        List<Long> testCaseExecutionInQueueList = new ArrayList<>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());

            int i = 1;
            if (!StringUtil.isEmptyOrNull(tag)) {
                preStat.setString(i++, tag);
            }

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testCaseExecutionInQueueList.add(resultSet.getLong(1));
                    }
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecutionInQueue").replace("%OPERATION%", "SELECT"));
                    answer.setDataList(testCaseExecutionInQueueList);
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    testCaseExecutionInQueueList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                testCaseExecutionInQueueList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            testCaseExecutionInQueueList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            }
        }
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerList<TestCaseExecutionQueue> readByQueueIdList(List<Long> queueIDList) throws CerberusException {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        AnswerList<TestCaseExecutionQueue> answer = new AnswerList<>();

        final StringBuilder query = new StringBuilder();

        query.append("SELECT * FROM testcaseexecutionqueue exq ");
        query.append("left join testcase tec on exq.Test = tec.Test and exq.TestCase = tec.TestCase ");
        query.append("left join application app on tec.application = app.application ");
        query.append("where ");
        query.append(SqlUtil.createWhereInClauseLong("exq.ID", queueIDList, "", ""));
        query.append(" limit 10000");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }

        List<TestCaseExecutionQueue> testCaseExecutionInQueueList = new ArrayList<>();
        Connection connection = this.databaseSpring.connect();

        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testCaseExecutionInQueueList.add(this.loadWithDependenciesFromResultSet(resultSet));
                    }

                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecutionInQueue").replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList<>(testCaseExecutionInQueueList, testCaseExecutionInQueueList.size());
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    testCaseExecutionInQueueList = null;
                } catch (FactoryCreationException ex) {
                    LOG.error("Unable to execute query : " + ex.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                testCaseExecutionInQueueList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            testCaseExecutionInQueueList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            }
        }
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerList<TestCaseExecutionQueue> readByVarious1(String tag, List<String> stateList, boolean withDependencies) throws CerberusException {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        AnswerList<TestCaseExecutionQueue> answer = new AnswerList<>();

        final StringBuilder query = new StringBuilder();

        query.append("SELECT * FROM testcaseexecutionqueue exq ");
        if (withDependencies) {
            query.append("left join testcase tec on exq.Test = tec.Test and exq.TestCase = tec.TestCase ");
            query.append("left join application app on tec.application = app.application ");
        }
        query.append("where exq.tag = ? ");
        query.append(SqlUtil.createWhereInClause(" AND exq.state", stateList, true));

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param.tag : " + tag);
        }

        List<TestCaseExecutionQueue> testCaseExecutionInQueueList = new ArrayList<>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());

            preStat.setString(1, tag);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        if (withDependencies) {
                            testCaseExecutionInQueueList.add(this.loadWithDependenciesFromResultSet(resultSet));
                        } else {
                            testCaseExecutionInQueueList.add(this.loadFromResultSet(resultSet));
                        }
                    }

                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecutionInQueue").replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList<>(testCaseExecutionInQueueList, testCaseExecutionInQueueList.size());
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    testCaseExecutionInQueueList = null;
                } catch (FactoryCreationException ex) {
                    LOG.error("Unable to execute query : " + ex.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                testCaseExecutionInQueueList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            testCaseExecutionInQueueList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            }
        }
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerList<TestCaseExecutionQueueToTreat> readByVarious2(List<String> stateList) throws CerberusException {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        AnswerList<TestCaseExecutionQueueToTreat> answer = new AnswerList<>();

        final StringBuilder query = new StringBuilder();

        query.append("SELECT exq.id, exq.manualexecution, app.System, app.poolSize, cea.environment, cea.country, cea.application, cea.poolsize, exq.robot, exq.robotIP, exq.robotPort, exq.DebugFlag, exq.selectedRobotHost, exq.selectedExtensionHost, app.type ");
        query.append("from testcaseexecutionqueue exq ");
        query.append("left join testcase tec on tec.test=exq.test and tec.testcase=exq.testcase ");
        query.append("left join application app on app.application=tec.application ");
        query.append("left join countryenvironmentparameters cea on cea.system=app.system and cea.environment=exq.environment and cea.country=exq.country and cea.application=tec.application ");
        query.append("WHERE 1=1 ");
        query.append(SqlUtil.createWhereInClause(" AND exq.state", stateList, true));
        query.append("order by exq.priority, exq.id asc;");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }

        List<TestCaseExecutionQueueToTreat> testCaseExecutionInQueueList = new ArrayList<>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testCaseExecutionInQueueList.add(loadQueueToTreatFromResultSet(resultSet));
                    }

                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecutionInQueue").replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList<>(testCaseExecutionInQueueList, testCaseExecutionInQueueList.size());
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    testCaseExecutionInQueueList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                testCaseExecutionInQueueList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            testCaseExecutionInQueueList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            }
        }
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerList<TestCaseExecutionQueue> readByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {

        AnswerList<TestCaseExecutionQueue> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCaseExecutionQueue> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM testcaseexecutionqueue exq ");

        query.append(" WHERE 1=1");

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (exq.ID like ?");
            searchSQL.append(" or exq.Test like ?");
            searchSQL.append(" or exq.TestCase like ?");
            searchSQL.append(" or exq.Country like ?");
            searchSQL.append(" or exq.Environment like ?");
            searchSQL.append(" or exq.Browser like ?");
            searchSQL.append(" or exq.Tag like ?");
            searchSQL.append(" or exq.State like ?)");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and ");
                String q = SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue());
                if (q == null || "".equals(q)) {
                    q = "(exq." + entry.getKey() + " IS NULL OR " + entry.getKey() + " = '')";
                }
                searchSQL.append(q);
                individalColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }

        query.append(searchSQL);

        if (!StringUtil.isEmptyOrNull(column)) {
            query.append(" order by exq.").append(column).append(" ").append(dir);
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
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;

                if (!StringUtil.isEmptyOrNull(searchTerm)) {
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

                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        objectList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (objectList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList<>(objectList, nrTotalRows);
                    } else if (objectList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList<>(objectList, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList<>(objectList, nrTotalRows);
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

                } catch (FactoryCreationException exception) {
                    LOG.error("Unable to create Test Case Execution In Queue from Factory : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        response.setResultMessage(msg);
        response.setDataList(objectList);
        return response;
    }

    @Override
    public int getNbEntryToGo(long id, int prio) {
        AnswerItem<TestCaseExecutionQueue> ans = new AnswerItem<>();
        TestCaseExecutionQueue result = null;
        final String query = "SELECT count(*)  FROM testcaseexecutionqueue WHERE State = 'QUEUED' and (ID < ? and Priority <= ?);";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            try {
                int i = 1;
                preStat.setLong(i++, id);
                preStat.setInt(i++, prio);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        return resultSet.getInt(1);
                    } else {
                        LOG.error("No record found : " + query);
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
        return 999999;
    }

    @Override
    public AnswerList<TestCaseExecutionQueue> readDistinctEnvCountryBrowserByTag(String tag) {
        AnswerList<TestCaseExecutionQueue> answer = new AnswerList<>();
        StringBuilder query = new StringBuilder();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);

        query.append("SELECT exq.* FROM testcaseexecutionqueue exq WHERE tag = ? GROUP BY Environment, Country, Browser");

        Connection connection = this.databaseSpring.connect();

        List<TestCaseExecutionQueue> EnvCountryBrowserList = new ArrayList<>();

        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());

            preStat.setString(1, tag);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        EnvCountryBrowserList.add(this.loadFromResultSet(resultSet));
                    }
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecutionInQueue").replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList<>(EnvCountryBrowserList, EnvCountryBrowserList.size());
                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    EnvCountryBrowserList = null;
                } catch (FactoryCreationException ex) {
                    LOG.warn("Unable to execute query : " + ex.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    EnvCountryBrowserList = null;
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException ex) {
                LOG.warn("Unable to execute query : " + ex.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                EnvCountryBrowserList = null;
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException ex) {
            LOG.warn(ex.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                LOG.warn("Unable to execute query : " + ex.toString());
            }
        }

        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String columnName, String sort, String searchTerm, Map<String, List<String>> individualSearch, String column) {
        AnswerList<String> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();

        query.append("SELECT distinct exq.");
        query.append(columnName);
        query.append(" as distinctValues FROM testcaseexecutionqueue exq");
        query.append(" where 1=1");

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (exq.ID like ?");
            searchSQL.append(" or exq.Test like ?");
            searchSQL.append(" or exq.TestCase like ?");
            searchSQL.append(" or exq.Country like ?");
            searchSQL.append(" or exq.Environment like ?");
            searchSQL.append(" or exq.Browser like ?");
            searchSQL.append(" or exq.Tag like ?");
            searchSQL.append(" or exq.State like ?)");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and exq.");
                searchSQL.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individalColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }
        query.append(searchSQL);
        query.append(" group by ifnull(exq.").append(columnName).append(",'')");
        query.append(" order by exq.").append(columnName).append(" asc");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); Statement stm = connection.createStatement();) {

            int i = 1;
            if (!StringUtil.isEmptyOrNull(searchTerm)) {
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

            try (ResultSet resultSet = preStat.executeQuery(); ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()");) {
                //gets the data
                while (resultSet.next()) {
                    distinctValues.add(resultSet.getString("distinctValues") == null ? "" : resultSet.getString("distinctValues"));
                }

                //get the total number of rows
                int nrTotalRows = 0;

                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }

                if (distinctValues.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                } else if (distinctValues.size() <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                }
            } catch (SQLException e) {
                LOG.warn("Unable to execute query : " + e.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                        e.toString());
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

    @Override
    public AnswerList<String> findTagList(int tagnumber) {
        AnswerList<String> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        List<String> list = null;
        StringBuilder query = new StringBuilder();

        query.append("SELECT DISTINCT tag FROM testcaseexecutionqueue WHERE tag != ''");

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
                    list = new ArrayList<>();

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
    public AnswerList<TestCaseExecutionQueue> readBySystemByVarious(String system, List<String> testList, List<String> applicationList, List<String> tcstatusList, List<String> typeList, List<String> isActiveList, List<String> priorityList, List<String> targetMajorList, List<String> targetMinorList, List<String> creatorList, List<String> implementerList, List<String> buildList, List<String> revisionList, List<String> environmentList, List<String> countryList, List<String> browserList, List<String> tcestatusList, String ip, String port, String tag, String browserversion, String comment, String bugs, String ticket) {
        AnswerList<TestCaseExecutionQueue> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        List<TestCaseExecutionQueue> tceqList = new ArrayList<>();
        List<String> whereClauses = new LinkedList<>();

        StringBuilder query = new StringBuilder();

        int paramNumber = 0;

        query.append(" select t.ID as statusExecutionID, t.* from ( ");
        query.append(" select exq.*, tec.*, app.* ");
        query.append(" from testcaseexecutionqueue exq ");
        query.append(" inner join testcase tec on exq.test = tec.test and exq.testcase = tec.testcase ");
        query.append(" inner join application app on tec.application = app.application ");

        String testClause = SqlUtil.generateInClause("exq.test", testList);
        if (!StringUtil.isEmptyOrNull(testClause)) {
            whereClauses.add(testClause);
        }

        String applicationClause = SqlUtil.generateInClause("tec.application", applicationList);
        if (!StringUtil.isEmptyOrNull(applicationClause)) {
            whereClauses.add(applicationClause);
        }

        //test case status: working, fully_implemented, ...
        String tcsClause = SqlUtil.generateInClause("exq.status", tcstatusList);
        if (!StringUtil.isEmptyOrNull(tcsClause)) {
            whereClauses.add(tcsClause);
        }

        //type
        String typeClause = SqlUtil.generateInClause("tec.type", typeList);
        if (!StringUtil.isEmptyOrNull(typeClause)) {
            whereClauses.add(typeClause);
        }
        //test case active
        String isActiveClause = SqlUtil.generateInClause("tec.isActive", isActiveList);
        if (!StringUtil.isEmptyOrNull(isActiveClause)) {
            whereClauses.add(isActiveClause);
        }

        //test case active
        String priorityClause = SqlUtil.generateInClause("tec.Priority", priorityList);
        if (!StringUtil.isEmptyOrNull(priorityClause)) {
            whereClauses.add(priorityClause);
        }

        //target sprint
        String targetMajorClause = SqlUtil.generateInClause("tec.TargetMajor", targetMajorList);
        if (!StringUtil.isEmptyOrNull(targetMajorClause)) {
            whereClauses.add(targetMajorClause);
        }

        //target revision
        String targetMinorClause = SqlUtil.generateInClause("tec.TargetMinor", targetMinorList);
        if (!StringUtil.isEmptyOrNull(targetMinorClause)) {
            whereClauses.add(targetMinorClause);
        }

        //creator
        String creatorClause = SqlUtil.generateInClause("tec.UsrCreated", creatorList);
        if (!StringUtil.isEmptyOrNull(creatorClause)) {
            whereClauses.add(creatorClause);
        }

        //implementer
        String implementerClause = SqlUtil.generateInClause("tec.Implementer", implementerList);
        if (!StringUtil.isEmptyOrNull(implementerClause)) {
            whereClauses.add(implementerClause);
        }

        //build
        String buildClause = SqlUtil.generateInClause("exq.Build", buildList);
        if (!StringUtil.isEmptyOrNull(buildClause)) {
            whereClauses.add(buildClause);
        }
        //revision
        String revisionClause = SqlUtil.generateInClause("exq.Revision", revisionList);
        if (!StringUtil.isEmptyOrNull(revisionClause)) {
            whereClauses.add(revisionClause);
        }
        //environment
        String environmentClause = SqlUtil.generateInClause("exq.Environment", environmentList);
        if (!StringUtil.isEmptyOrNull(environmentClause)) {
            whereClauses.add(environmentClause);
        }
        //country
        String countryClause = SqlUtil.generateInClause("exq.Country", countryList);
        if (!StringUtil.isEmptyOrNull(countryClause)) {
            whereClauses.add(countryClause);
        }
        //browser
        String browserClause = SqlUtil.generateInClause("exq.Browser", browserList);
        if (!StringUtil.isEmptyOrNull(browserClause)) {
            whereClauses.add(browserClause);
        }
        //test case execution
        String tcestatusClause = SqlUtil.generateInClause("exq.ControlStatus", tcestatusList);
        if (!StringUtil.isEmptyOrNull(tcestatusClause)) {
            whereClauses.add(tcestatusClause);
        }

        if (!StringUtil.isEmptyOrNull(system)) {
            whereClauses.add(" app.system like ? ");
        }
        if (!StringUtil.isEmptyOrNull(ip)) {
            whereClauses.add(" exq.IP like ? ");
        }
        if (!StringUtil.isEmptyOrNull(port)) {
            whereClauses.add(" exq.port like ? ");
        }
        if (!StringUtil.isEmptyOrNull(tag)) {
            whereClauses.add(" exq.tag like ? ");
        }
        if (!StringUtil.isEmptyOrNull(browserversion)) {
            whereClauses.add(" exq.browserfullversion like ? ");
        }
        if (!StringUtil.isEmptyOrNull(comment)) {
            whereClauses.add(" exq.comment like ? ");
        }
        if (!StringUtil.isEmptyOrNull(bugs)) {
            whereClauses.add(" tec.bugs like ? ");
        }
        if (!StringUtil.isEmptyOrNull(ticket)) {
            whereClauses.add(" tec.Ticket like ? ");
        }

        if (whereClauses.size() > 0) {
            query.append("where ");
            String joined = StringUtils.join(whereClauses, " and ");
            query.append(joined);
        }

        query.append(" order by exq.ID desc ");
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
            if (tcstatusList != null) {
                for (String param : tcstatusList) {
                    preStat.setString(++paramNumber, param);
                }
            }
            if (typeList != null) {
                for (String param : typeList) {
                    preStat.setString(++paramNumber, param);
                }
            }

            if (isActiveList != null) {
                for (String param : isActiveList) {
                    preStat.setString(++paramNumber, param);
                }
            }
            if (priorityList != null) {
                for (String param : priorityList) {
                    preStat.setString(++paramNumber, param);
                }
            }
            if (targetMajorList != null) {
                for (String param : targetMajorList) {
                    preStat.setString(++paramNumber, param);
                }
            }
            if (targetMinorList != null) {
                for (String param : targetMinorList) {
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

            if (!StringUtil.isEmptyOrNull(system)) {
                preStat.setString(++paramNumber, system);
            }

            if (!StringUtil.isEmptyOrNull(ip)) {
                preStat.setString(++paramNumber, "%" + ip + "%");
            }
            if (!StringUtil.isEmptyOrNull(port)) {
                preStat.setString(++paramNumber, "%" + port + "%");
            }
            if (!StringUtil.isEmptyOrNull(tag)) {
                preStat.setString(++paramNumber, "%" + tag + "%");
            }
            if (!StringUtil.isEmptyOrNull(browserversion)) {
                preStat.setString(++paramNumber, "%" + browserversion + "%");
            }
            if (!StringUtil.isEmptyOrNull(comment)) {
                preStat.setString(++paramNumber, "%" + comment + "%");
            }
            if (!StringUtil.isEmptyOrNull(bugs)) {
                preStat.setString(++paramNumber, "%" + bugs + "%");
            }
            if (!StringUtil.isEmptyOrNull(ticket)) {
                preStat.setString(++paramNumber, "%" + ticket + "%");
            }

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        tceqList.add(this.loadWithDependenciesFromResultSet(resultSet));
                    }
                    if (tceqList.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecutionInQueue").replace("%OPERATION%", "SELECT"));
                    }

                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    tceqList.clear();
                } catch (FactoryCreationException ex) {
                    LOG.warn("Unable to execute query : " + ex.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    tceqList.clear();
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException ex) {
                LOG.warn("Unable to execute query : " + ex.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException ex) {
            LOG.warn(ex.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                LOG.warn("Unable to execute query : " + ex.toString());
            }
        }
        answer.setTotalRows(tceqList.size());
        answer.setDataList(tceqList);
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public TestCaseExecutionQueue findByKeyWithDependencies(long id) throws CerberusException {
        String query = "SELECT * FROM `" + TABLE + "` exq "
                + "INNER JOIN `" + TABLE_TEST_CASE + "` tec ON (exq.`" + COLUMN_TEST + "` = tec.`Test` AND exq.`" + COLUMN_TEST_CASE + "` = tec.`TestCase`) "
                + "INNER JOIN `" + TABLE_APPLICATION + "` app ON (tec.`Application` = app.`Application`) "
                + "WHERE `" + COLUMN_ID + "` = ?";

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement selectStatement = connection.prepareStatement(query);) {
            selectStatement.setLong(1, id);
            try (ResultSet result = selectStatement.executeQuery();) {
                if (!result.next()) {
                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                }
                return loadWithDependenciesFromResultSet(result);
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
            }
        } catch (SQLException | FactoryCreationException e) {
            LOG.warn("Unable to find test case execution in queue " + id, e);
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
        }
    }

    @Override
    public AnswerItem<TestCaseExecutionQueue> create(TestCaseExecutionQueue object) {
        TestCaseExecutionQueue newObject = object;
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO `" + TABLE + "` (`" + COLUMN_SYSTEM + "`, `" + COLUMN_TEST + "`, `" + COLUMN_TEST_CASE + "`, `" + COLUMN_COUNTRY + "`, `" + COLUMN_ENVIRONMENT + "`, `" + COLUMN_ROBOT
                + "`, `" + COLUMN_ROBOTDECLI + "`, `" + COLUMN_ROBOT_IP + "`, `" + COLUMN_ROBOT_PORT + "`, `" + COLUMN_BROWSER + "`, `" + COLUMN_BROWSER_VERSION + "`, `" + COLUMN_PLATFORM
                + "`, `" + COLUMN_SCREENSIZE + "`, `" + COLUMN_MANUAL_URL + "`, `" + COLUMN_MANUAL_HOST + "`, `" + COLUMN_MANUAL_CONTEXT_ROOT + "`, `"
                + COLUMN_MANUAL_LOGIN_RELATIVE_URL + "`, `" + COLUMN_MANUAL_ENV_DATA + "`, `" + COLUMN_TAG + "`, `" + COLUMN_SCREENSHOT + "`, `" + COLUMN_VIDEO + "`, `" + COLUMN_VERBOSE + "`, `"
                + COLUMN_TIMEOUT + "`, `" + COLUMN_PAGE_SOURCE + "`, `" + COLUMN_ROBOT_LOG + "`, `" + COLUMN_CONSOLE_LOG + "`, `" + COLUMN_RETRIES + "`, `"
                + COLUMN_MANUAL_EXECUTION + "`, `" + COLUMN_USRCREATED + "`, `" + COLUMN_STATE + "`, `" + COLUMN_COMMENT + "`, `" + COLUMN_DEBUGFLAG + "`, `" + COLUMN_PRIORITY + "`, `AlreadyExecuted`) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param.prio : " + object.getPriority());
            LOG.debug("SQL.param.debug : " + object.getDebugFlag());
            LOG.debug("SQL.param.comment : " + object.getComment());
            LOG.debug("SQL.param.state : " + object.getState());
            LOG.debug("SQL.param.ManualExecution : " + object.getManualExecution());
        }

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);) {

            int i = 1;
            preStat.setString(i++, object.getSystem());
            preStat.setString(i++, object.getTest());
            preStat.setString(i++, object.getTestCase());
            preStat.setString(i++, object.getCountry());
            preStat.setString(i++, object.getEnvironment());
            preStat.setString(i++, object.getRobot());
            preStat.setString(i++, object.getRobotDecli() == null ? "" : object.getRobotDecli());
            preStat.setString(i++, object.getRobotIP());
            preStat.setString(i++, object.getRobotPort());
            preStat.setString(i++, object.getBrowser());
            preStat.setString(i++, object.getBrowserVersion());
            preStat.setString(i++, object.getPlatform());
            preStat.setString(i++, object.getScreenSize());
            preStat.setInt(i++, object.getManualURL());
            preStat.setString(i++, object.getManualHost());
            preStat.setString(i++, object.getManualContextRoot());
            preStat.setString(i++, object.getManualLoginRelativeURL());
            preStat.setString(i++, object.getManualEnvData());
            preStat.setString(i++, object.getTag());
            preStat.setInt(i++, object.getScreenshot());
            preStat.setInt(i++, object.getVideo());
            preStat.setInt(i++, object.getVerbose());
            preStat.setString(i++, object.getTimeout());
            preStat.setInt(i++, object.getPageSource());
            preStat.setInt(i++, object.getRobotLog());
            preStat.setInt(i++, object.getConsoleLog());
            preStat.setInt(i++, object.getRetries());
            preStat.setString(i++, object.getManualExecution() == null ? "N" : object.getManualExecution());
            String user = object.getUsrCreated() == null ? "" : object.getUsrCreated();
            preStat.setString(i++, user);
            if (object.getState() == null) {
                preStat.setString(i++, object.getState().WAITING.name());
            } else {
                preStat.setString(i++, object.getState().name());
            }
            preStat.setString(i++, object.getComment());
            preStat.setString(i++, object.getDebugFlag());
            preStat.setInt(i++, object.getPriority());
            preStat.setInt(i++, object.getAlreadyExecuted());

            preStat.executeUpdate();

            try (ResultSet resultSet = preStat.getGeneratedKeys()) {
                if (resultSet.first()) {
                    newObject.setId(resultSet.getInt(1));
                }
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            }

            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());

            if (exception.getSQLState().equals(SQL_DUPLICATED_CODE)) { //23000 is the sql state for duplicate entries
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_DUPLICATE);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT").replace("%REASON%", exception.toString()));
            } else {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            }
        }
        return new AnswerItem<>(newObject, msg);
    }

    @Override
    public Answer update(TestCaseExecutionQueue object) {
        MessageEvent msg = null;
        String query = "UPDATE testcaseexecutionqueue exq SET `System` = ?, `Test` = ?, `TestCase` = ?, `Country` = ?, Environment = ?, Robot = ?, RobotDecli = ?, "
                + "RobotIP = ?, `RobotPort` = ?, Browser = ?, BrowserVersion = ?, `Platform`= ?, `ScreenSize` = ?, "
                + "ManualURL = ?, `ManualHost` = ?, ManualContextRoot = ?, `ManualLoginRelativeUrl`= ?, `ManualEnvData` = ?, "
                + "Tag = ?, `Screenshot` = ?, `Video` = ?, Verbose = ?, `Timeout`= ?, `PageSource` = ?, `debugFlag` = ?, `priority` = ?, "
                + "RobotLog = ?, ConsoleLog = ?, `Retries`= ?, `ManualExecution` = ?, "
                + "`UsrModif`= ?, `DateModif` = now() ";
        query += " WHERE `ID` = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.id : " + object.getId());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setString(i++, object.getSystem());
                preStat.setString(i++, object.getTest());
                preStat.setString(i++, object.getTestCase());
                preStat.setString(i++, object.getCountry());
                preStat.setString(i++, object.getEnvironment());
                preStat.setString(i++, object.getRobot());
                preStat.setString(i++, object.getRobotDecli());
                preStat.setString(i++, object.getRobotIP());
                preStat.setString(i++, object.getRobotPort());
                preStat.setString(i++, object.getBrowser());
                preStat.setString(i++, object.getBrowserVersion());
                preStat.setString(i++, object.getPlatform());
                preStat.setString(i++, object.getScreenSize());
                preStat.setInt(i++, object.getManualURL());
                preStat.setString(i++, object.getManualHost());
                preStat.setString(i++, object.getManualContextRoot());
                preStat.setString(i++, object.getManualLoginRelativeURL());
                preStat.setString(i++, object.getManualEnvData());
                preStat.setString(i++, object.getTag());
                preStat.setInt(i++, object.getScreenshot());
                preStat.setInt(i++, object.getVideo());
                preStat.setInt(i++, object.getVerbose());
                preStat.setString(i++, object.getTimeout());
                preStat.setInt(i++, object.getPageSource());
                preStat.setString(i++, object.getDebugFlag());
                preStat.setInt(i++, object.getPriority());
                preStat.setInt(i++, object.getRobotLog());
                preStat.setInt(i++, object.getConsoleLog());
                preStat.setInt(i++, object.getRetries());
                preStat.setString(i++, object.getManualExecution());
                preStat.setString(i++, object.getUsrModif());
                preStat.setLong(i++, object.getId());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));
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
        return new Answer(msg);
    }

    @Override
    public Answer updatePriority(long id, int priority) {
        MessageEvent msg = null;
        String query
                = "UPDATE `" + TABLE + "` "
                + "SET `" + COLUMN_DATEMODIF + "` = now(), `" + COLUMN_PRIORITY + "` = ? "
                + "WHERE `" + COLUMN_ID + "` = ? ;";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.id : " + id);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setInt(i++, priority);
                preStat.setLong(i++, id);

                int updateResult = preStat.executeUpdate();
                if (updateResult <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_NOUPDATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%DESCRIPTION%", "Unable to update priority for execution in queue " + id + " (update result: " + updateResult + ")."));
                    LOG.warn("Unable to update priority for execution in queue " + id + " (update result: " + updateResult + ").");
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));

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
        return new Answer(msg);
    }

    @Override
    public Answer updateComment(long id, String comment) {
        MessageEvent msg = null;
        String query
                = "UPDATE `" + TABLE + "` "
                + "SET `" + COLUMN_REQUEST_DATE + "` = now(), `" + COLUMN_DATEMODIF + "` = now(), `" + COLUMN_COMMENT + "` = ? "
                + "WHERE `" + COLUMN_ID + "` = ? ;";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.id : " + id);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setString(i++, comment);
                preStat.setLong(i++, id);

                int updateResult = preStat.executeUpdate();
                if (updateResult <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_NOUPDATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%DESCRIPTION%", "Unable to update comment for execution in queue " + id + " (update result: " + updateResult + ")."));
                    LOG.warn("Unable to update comment for execution in queue " + id + " (update result: " + updateResult + ").");
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));

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
        return new Answer(msg);
    }

    @Override
    public Answer updateToState(long id, String comment, TestCaseExecutionQueue.State targetState) {
        MessageEvent msg = null;
        String query
                = "UPDATE `" + TABLE + "` "
                + "SET `" + COLUMN_STATE + "` = ?, `" + COLUMN_REQUEST_DATE + "` = now(), `" + COLUMN_DATEMODIF + "` = now(), `" + COLUMN_COMMENT + "` = ? "
                + "WHERE `" + COLUMN_ID + "` = ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.id : " + id);
            LOG.debug("SQL.param.targetState : " + targetState.toString());
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setString(i++, targetState.toString());
                preStat.setString(i++, comment);
                preStat.setLong(i++, id);

                int updateResult = preStat.executeUpdate();
                if (updateResult <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_NOUPDATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%DESCRIPTION%", "Unable to move state to " + targetState.toString() + " for execution in queue " + id + " (update result: " + updateResult + ")."));
                    LOG.warn("Unable to move state to " + targetState.toString() + " for execution in queue " + id + " (update result: " + updateResult + ").");
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));

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
        return new Answer(msg);
    }

    @Override
    public Answer updateToQueued(long id, String comment) {
        MessageEvent msg = null;
        String query
                = "UPDATE `" + TABLE + "` "
                + "SET `" + COLUMN_STATE + "` = 'QUEUED', `" + COLUMN_REQUEST_DATE + "` = now(), `" + COLUMN_DATEMODIF + "` = now(), `" + COLUMN_COMMENT + "` = ? "
                + "WHERE `" + COLUMN_ID + "` = ? "
                + "AND `" + COLUMN_STATE + "` IN ('CANCELLED', 'ERROR', 'QUWITHDEP')";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.id : " + id);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setString(i++, comment);
                preStat.setLong(i++, id);

                int updateResult = preStat.executeUpdate();
                if (updateResult <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_NOUPDATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%DESCRIPTION%", "Unable to move state to QUEUD for execution in queue " + id + " (update result: " + updateResult + "). Maybe execution is no longuer in CANCELLED or ERROR ?"));
                    LOG.warn("Unable to move state to QUEUED for execution in queue " + id + " (update result: " + updateResult + "). Maybe execution is no longuer in CANCELLED or ERROR or QUWITHDEP ?");
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));

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
        return new Answer(msg);
    }

    @Override
    public Answer updateAllTagToQueuedFromQuTemp(String tag, List<Long> queueIds) {
        MessageEvent msg = null;

        String query
                = "UPDATE `" + TABLE + "` "
                + "SET `" + COLUMN_STATE + "` = 'QUEUED', `" + COLUMN_REQUEST_DATE + "` = now(), `" + COLUMN_DATEMODIF + "` = now() "
                + "WHERE `" + COLUMN_TAG + "` = ? "
                + "AND `" + COLUMN_STATE + "` IN ('QUTEMP')"
                + SqlUtil.createWhereInClauseLong(COLUMN_ID, queueIds, " AND ", "");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.tag : " + tag);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setString(i++, tag);

                int updateResult = preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));

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
        return new Answer(msg);
    }

    @Override
    public Answer updateToQueuedFromQuWithDep(long id, String comment) {
        MessageEvent msg = null;
        String query
                = "UPDATE `" + TABLE + "` "
                + "SET `" + COLUMN_STATE + "` = 'QUEUED', `" + COLUMN_REQUEST_DATE + "` = now(), `" + COLUMN_DATEMODIF + "` = now(), `" + COLUMN_COMMENT + "` = ? "
                + "WHERE `" + COLUMN_ID + "` = ? "
                + "AND `" + COLUMN_STATE + "` IN ('QUWITHDEP')";

        // Debug message on SQL.
        LOG.debug("SQL : " + query);
        LOG.debug("SQL.param.id : " + id);

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setString(i++, comment);
                preStat.setLong(i++, id);

                int updateResult = preStat.executeUpdate();
                if (updateResult <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_NOUPDATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%DESCRIPTION%", "Unable to move state to QUEUD for execution in queue " + id + " (update result: " + updateResult + "). Maybe execution is no longuer in QUWITHDEP ?"));
                    LOG.warn("Unable to move state to QUEUED for execution in queue " + id + " (update result: " + updateResult + "). Maybe execution is no longuer in QUWITHDEP ?");
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));

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
        return new Answer(msg);
    }

    @Override
    public boolean updateToWaiting(final Long id) throws CerberusException {

        String queryUpdate = "UPDATE `" + TABLE + "` "
                + "SET `" + COLUMN_STATE + "` = 'WAITING', `" + COLUMN_REQUEST_DATE + "` = now(), `" + COLUMN_DATEMODIF + "` = now() "
                + "WHERE `" + COLUMN_ID + "` = ? "
                + "AND `" + COLUMN_STATE + "` = 'QUEUED'";

        try (
                Connection connection = this.databaseSpring.connect(); PreparedStatement updateStateStatement = connection.prepareStatement(queryUpdate)) {

            try {
                // Debug message on SQL.
                LOG.debug("SQL : " + queryUpdate);
                LOG.debug("SQL.param.id : " + id);

                updateStateStatement.setLong(1, id);

                int updateResult = updateStateStatement.executeUpdate();
                if (updateResult <= 0) {
                    LOG.warn("Unable to move state to WAITING for execution in queue " + id + " (update result: " + updateResult + "). Maybe execution is not in QUEUED ?");
                    return false;
                }

                return true;

            } catch (SQLException e) {
                LOG.warn("Unable to move state from QUEUED to WAITING for execution in queue " + id + ".", e);
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
            }

        } catch (SQLException e) {
            LOG.warn("Unable to state from QUEUED to WAITING state for executions in queue", e);
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
        }
    }

    @Override
    public void updateToExecuting(long id, String comment, long exeId) throws CerberusException {
        String queryUpdate = "UPDATE `" + TABLE + "` "
                + "SET `" + COLUMN_STATE + "` = 'EXECUTING', `" + COLUMN_EXEID + "` = ?, `" + COLUMN_COMMENT + "` = ?, `" + COLUMN_REQUEST_DATE + "` = now(), `" + COLUMN_DATEMODIF + "` = now() "
                + "WHERE `" + COLUMN_ID + "` = ? "
                + "AND `" + COLUMN_STATE + "` in ('STARTING')";

        // Debug message on SQL.
        LOG.debug("SQL : " + queryUpdate);
        LOG.debug("SQL.param.id : " + id);

        try (
                Connection connection = databaseSpring.connect(); PreparedStatement updateStateStatement = connection.prepareStatement(queryUpdate)) {

            updateStateStatement.setLong(1, exeId);
            updateStateStatement.setString(2, comment);
            updateStateStatement.setLong(3, id);

            int updateResult = updateStateStatement.executeUpdate();
            if (updateResult <= 0) {
                LOG.warn("Unable to move state from STARTING to EXECUTING for execution in queue " + id + " (update result: " + updateResult + ").");
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
            }
        } catch (SQLException e) {
            LOG.warn("Unable to move state from STARTING to EXECUTING for execution in queue " + id, e);
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
        }
    }

    @Override
    public void updateToStarting(long id, String selectedRobot, String selectedRobotExt) throws CerberusException {
        String queryUpdate = "UPDATE `" + TABLE + "` "
                + "SET `" + COLUMN_STATE + "` = 'STARTING', `" + COLUMN_SELECTEDROBOTHOST + "` = ?, `" + COLUMN_SELECTEDROBOTEXTHOST + "` = ?, `" + COLUMN_REQUEST_DATE + "` = now(), `" + COLUMN_DATEMODIF + "` = now() "
                + "WHERE `" + COLUMN_ID + "` = ? "
                + "AND `" + COLUMN_STATE + "` = 'WAITING'";

        // Debug message on SQL.
        LOG.debug("SQL : " + queryUpdate);
        LOG.debug("SQL.param.id : " + id);
        LOG.debug("SQL.param.SelectedRobotHost : " + selectedRobot);
        LOG.debug("SQL.param.SelectedRobotExtHost : " + selectedRobotExt);

        try (
                Connection connection = databaseSpring.connect(); PreparedStatement updateStateStatement = connection.prepareStatement(queryUpdate)) {

            updateStateStatement.setString(1, selectedRobot);
            updateStateStatement.setString(2, selectedRobotExt);
            updateStateStatement.setLong(3, id);

            int updateResult = updateStateStatement.executeUpdate();
            if (updateResult <= 0) {
                LOG.warn("Unable to move state from WAITING to STARTING for execution in queue " + id + " (update result: " + updateResult + ").");
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
            }
        } catch (SQLException e) {
            LOG.warn("Unable to move state from WAITING to STARTING for execution in queue " + id, e);
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
        }
    }

    @Override
    public void updateToError(long id, String comment) throws CerberusException {
        String query
                = "UPDATE `" + TABLE + "` "
                + "SET `" + COLUMN_STATE + "` = 'ERROR', `" + COLUMN_COMMENT + "` = ?, `" + COLUMN_REQUEST_DATE + "` = now(), `" + COLUMN_DATEMODIF + "` = now() "
                + "WHERE `" + COLUMN_ID + "` = ? "
                + "AND `" + COLUMN_STATE + "` in ('STARTING', 'EXECUTING')";

        // Debug message on SQL.
        LOG.debug("SQL : " + query);
        LOG.debug("SQL.param.id : " + id);

        try (Connection connection = databaseSpring.connect(); PreparedStatement updateStateAndCommentStatement = connection.prepareStatement(query)) {

            updateStateAndCommentStatement.setString(1, comment);
            updateStateAndCommentStatement.setLong(2, id);

            int updateResult = updateStateAndCommentStatement.executeUpdate();
            if (updateResult <= 0) {
                LOG.warn("Unable to move state to ERROR for execution in queue " + id + " (update result: " + updateResult + ")");
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
            }
        } catch (SQLException e) {
            LOG.warn("Unable to set move to ERROR for execution in queue id " + id, e);
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
        }
    }

    @Override
    public void updateToErrorFromQuWithDep(long id, String comment) throws CerberusException {
        String query
                = "UPDATE `" + TABLE + "` "
                + "SET `" + COLUMN_STATE + "` = 'ERROR', `" + COLUMN_COMMENT + "` = ?, `" + COLUMN_REQUEST_DATE + "` = now(), `" + COLUMN_DATEMODIF + "` = now() "
                + "WHERE `" + COLUMN_ID + "` = ? "
                + "AND `" + COLUMN_STATE + "` = 'QUWITHDEP'";

        // Debug message on SQL.
        LOG.debug("SQL : " + query);
        LOG.debug("SQL.param.id : " + id);

        try (Connection connection = databaseSpring.connect(); PreparedStatement updateStateAndCommentStatement = connection.prepareStatement(query)) {

            updateStateAndCommentStatement.setString(1, comment);
            updateStateAndCommentStatement.setLong(2, id);

            int updateResult = updateStateAndCommentStatement.executeUpdate();
            if (updateResult <= 0) {
                // LOG is only only in debug mode as this situation can happen if entry was already put in ERROR state.
                // Ex : When many executions end in QUEUE ERROR and generate the same child entry to be in ERROR.
                LOG.debug("Unable to move state to ERROR for execution in queue " + id + " (update result: " + updateResult + ")");
            }
        } catch (SQLException e) {
            LOG.warn("Unable to set move to ERROR for execution in queue id " + id, e);
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
        }
    }

    @Override
    public void updateToDone(long id, String comment, long exeId) throws CerberusException {
        String query
                = "UPDATE `" + TABLE + "` "
                + "SET `" + COLUMN_STATE + "` = 'DONE', `" + COLUMN_EXEID + "` = ?, `" + COLUMN_COMMENT + "` = ?, `" + COLUMN_REQUEST_DATE + "` = now(), `" + COLUMN_DATEMODIF + "` = now() "
                + "WHERE `" + COLUMN_ID + "` = ? ";

        // Debug message on SQL.
        LOG.debug("SQL : " + query);
        LOG.debug("SQL.param.id : " + id);

        try (
                Connection connection = databaseSpring.connect(); PreparedStatement updateStateAndCommentStatement = connection.prepareStatement(query)) {

            updateStateAndCommentStatement.setLong(1, exeId);
            updateStateAndCommentStatement.setString(2, comment);
            updateStateAndCommentStatement.setLong(3, id);

            int updateResult = updateStateAndCommentStatement.executeUpdate();
            if (updateResult <= 0) {
                LOG.warn("Unable to move state to DONE for execution in queue " + id + " (update result: " + updateResult + ")");
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
            }
        } catch (SQLException e) {
            LOG.warn("Unable to set move to DONE for execution in queue id " + id, e);
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
        }
    }

    @Override
    public Answer updateToCancelled(long id, String comment) {
        MessageEvent msg = null;
        String query
                = "UPDATE `" + TABLE + "` "
                + "SET `" + COLUMN_STATE + "` = 'CANCELLED', `" + COLUMN_REQUEST_DATE + "` = now(), `" + COLUMN_DATEMODIF + "` = now(), `" + COLUMN_COMMENT + "` = ? "
                + "WHERE `" + COLUMN_ID + "` = ? "
                + "AND `" + COLUMN_STATE + "` IN ('ERROR','QUEUED','QUWITHDEP','QUEUED_PAUSED','QUWITHDEP_PAUSED')";

        // Debug message on SQL.
        LOG.debug("SQL : " + query);
        LOG.debug("SQL.param.id : " + id);

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setString(i++, comment);
                preStat.setLong(i++, id);

                int updateResult = preStat.executeUpdate();
                if (updateResult <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_NOUPDATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%DESCRIPTION%", "Unable to move state to CANCELLED for execution in queue " + id + " (update result: " + updateResult + "). Maybe execution is no longuer in ERROR, QUEUED, QUWITHDEP, QUEUED_PAUSED or QUWITHDEP_PAUSED ?"));
                    LOG.warn("Unable to move state to CANCELLED for execution in queue " + id + " (update result: " + updateResult + "). Maybe execution is no longuer in ERROR, QUEUED, QUWITHDEP, QUEUED_PAUSED or QUWITHDEP_PAUSED ?");
//                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));

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
        return new Answer(msg);
    }

    @Override
    public Answer updateToCancelledForce(long id, String comment) {
        MessageEvent msg = null;
        String query
                = "UPDATE `" + TABLE + "` "
                + "SET `" + COLUMN_STATE + "` = 'CANCELLED', `" + COLUMN_REQUEST_DATE + "` = now(), `" + COLUMN_DATEMODIF + "` = now(), `" + COLUMN_COMMENT + "` = ? "
                + "WHERE `" + COLUMN_ID + "` = ? "
                + "AND `" + COLUMN_STATE + "` IN ('WAITING','STARTING','EXECUTING','ERROR','QUEUED','QUWITHDEP','QUEUED_PAUSED','QUWITHDEP_PAUSED')";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setString(i++, comment);
                preStat.setLong(i++, id);

                int updateResult = preStat.executeUpdate();
                if (updateResult <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_NOUPDATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%DESCRIPTION%", "Unable to move state to CANCELLED (forced) for execution in queue " + id + " (update result: " + updateResult + "). Maybe execution is no longuer in WAITING, STARTING, EXECUTING, ERROR, QUEUED, QUWITHDEP, QUEUED_PAUSED or QUWITHDEP_PAUSED ?"));
                    LOG.warn("Unable to move state to CANCELLED (forced) for execution in queue " + id + " (update result: " + updateResult + "). Maybe execution is no longuer in WAITING, STARTING, EXECUTING, ERROR, QUEUED, QUWITHDEP, QUEUED_PAUSED or QUWITHDEP_PAUSED ?");
//                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));

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
        return new Answer(msg);
    }

    @Override
    public AnswerItem<Integer> updateToCancelledOldRecord(Integer timeOutInS, String comment) {

        MessageEvent msg = null;
        String query
                = "UPDATE testcaseexecutionqueue "
                + "SET `" + COLUMN_STATE + "` = 'CANCELLED', `" + COLUMN_REQUEST_DATE + "` = now(), `" + COLUMN_DATEMODIF + "` = now(), `" + COLUMN_COMMENT + "` = ? "
                + "WHERE TO_SECONDS(now()) - TO_SECONDS(`" + COLUMN_DATEMODIF + "`) > ? "
                + "AND `" + COLUMN_STATE + "` IN ('WAITING','STARTING','EXECUTING')";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setString(i++, comment);
                preStat.setLong(i++, timeOutInS);

                int updateResult = preStat.executeUpdate();
                if (updateResult <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_NOUPDATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%DESCRIPTION%", "Unable to move state to CANCELLED (forced) for execution in queue (update result: " + updateResult + "). Maybe execution is no longuer in WAITING or STARTING or EXECUTING ?"));
                    LOG.info("No 'old' queue entries to force CANCELLED. (timeout = " + timeOutInS + ").");
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));
                    LOG.info(updateResult + " 'old' queue entries forced to CANCELLED. (timeout = " + timeOutInS + ").");
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
        return new AnswerItem<>(msg);
    }

    @Override
    public AnswerItem<Integer> updateToCancelledPendingRecord(String tag, String user, String comment) {

        MessageEvent msg = null;
        AnswerItem<Integer> ansReturn = new AnswerItem<>();
        String query
                = "UPDATE testcaseexecutionqueue "
                + "SET `" + COLUMN_STATE + "` = 'CANCELLED', `" + COLUMN_REQUEST_DATE + "` = now(), `" + COLUMN_DATEMODIF + "` = now(), `" + COLUMN_COMMENT + "` = ?, `" + COLUMN_USRMODIF + "` = ? "
                + "WHERE Tag = ? "
                + "AND `" + COLUMN_STATE + "` IN ('QUEUED','QUWITHDEP','QUEUED_PAUSED','QUWITHDEP_PAUSED')";

        // Debug message on SQL.
        LOG.debug("SQL : " + query);
        LOG.debug("SQL.param.tag : " + tag);
        LOG.debug("SQL.param.comment : " + comment);

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setString(i++, comment);
                preStat.setString(i++, user);
                preStat.setString(i++, tag);

                int updateResult = preStat.executeUpdate();
                ansReturn.setItem(updateResult);
                if (updateResult <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_NOUPDATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%DESCRIPTION%", "Unable to move state to CANCELLED (forced) for execution in queue (update result: " + updateResult + "). Maybe execution is no longuer in QUEUE or QUWITHDEP ?"));
                    LOG.info("No queue entries to cancel.");
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK)
                            .resolveDescription("ITEM", OBJECT_NAME)
                            .resolveDescription("OPERATION", "UPDATE");
                    msg.setDescription(msg.getDescription() + " - " + updateResult + " queue entry(ies) was(were) cancelled.");
                    LOG.info(updateResult + " - " + updateResult + " queue entry(ies) was(were) cancelled.");
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
        ansReturn.setResultMessage(msg);
        return ansReturn;
    }

    @Override
    public AnswerItem<Integer> updateToPausedPendingRecord(String tag, String user, String comment) {

        MessageEvent msg = null;
        AnswerItem<Integer> ansReturn = new AnswerItem<>();
        String query
                = "UPDATE testcaseexecutionqueue "
                + "SET `" + COLUMN_STATE + "` = concat(`" + COLUMN_STATE + "`,'_PAUSED'), `" + COLUMN_REQUEST_DATE + "` = now(), `" + COLUMN_DATEMODIF + "` = now(), `" + COLUMN_COMMENT + "` = ?, `" + COLUMN_USRMODIF + "` = ? "
                + "WHERE Tag = ? "
                + "AND `" + COLUMN_STATE + "` IN ('QUEUED','QUWITHDEP')";

        // Debug message on SQL.
        LOG.debug("SQL : " + query);
        LOG.debug("SQL.param.tag : " + tag);
        LOG.debug("SQL.param.comment : " + comment);

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setString(i++, comment);
                preStat.setString(i++, user);
                preStat.setString(i++, tag);

                int updateResult = preStat.executeUpdate();
                ansReturn.setItem(updateResult);
                if (updateResult <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_NOUPDATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%DESCRIPTION%", "Unable to move state to *_PAUSED for execution in queue (update result: " + updateResult + "). Maybe execution is no longuer in QUEUE or QUWITHDEP ?"));
                    LOG.info("No queue entries to cancel.");
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK)
                            .resolveDescription("ITEM", OBJECT_NAME)
                            .resolveDescription("OPERATION", "UPDATE");
                    msg.setDescription(msg.getDescription() + " - " + updateResult + " queue entry(ies) was(were) paused.");
                    LOG.info(updateResult + " - " + updateResult + " queue entry(ies) was(were) paused.");
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
        ansReturn.setResultMessage(msg);
        return ansReturn;
    }

    @Override
    public AnswerItem<Integer> updateToNonPausedPendingRecord(String tag, String user, String comment) {

        MessageEvent msg = null;
        AnswerItem<Integer> ansReturn = new AnswerItem<>();
        String query
                = "UPDATE testcaseexecutionqueue "
                + "SET `" + COLUMN_STATE + "` = REPLACE(`" + COLUMN_STATE + "`, '_PAUSED',''), `" + COLUMN_REQUEST_DATE + "` = now(), `" + COLUMN_DATEMODIF + "` = now(), `" + COLUMN_COMMENT + "` = ?, `" + COLUMN_USRMODIF + "` = ? "
                + "WHERE Tag = ? "
                + "AND `" + COLUMN_STATE + "` LIKE '%_PAUSED'";

        // Debug message on SQL.
        LOG.debug("SQL : " + query);
        LOG.debug("SQL.param.tag : " + tag);
        LOG.debug("SQL.param.comment : " + comment);

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setString(i++, comment);
                preStat.setString(i++, user);
                preStat.setString(i++, tag);

                int updateResult = preStat.executeUpdate();
                ansReturn.setItem(updateResult);
                if (updateResult <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_NOUPDATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%DESCRIPTION%", "Unable to move state from *_PAUSED for execution in queue (update result: " + updateResult + "). Maybe execution is no longuer in *_PAUSED ?"));
                    LOG.info("No queue entries to cancel.");
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK)
                            .resolveDescription("ITEM", OBJECT_NAME)
                            .resolveDescription("OPERATION", "UPDATE");
                    msg.setDescription(msg.getDescription() + " - " + updateResult + " queue entry(ies) was(were) resumed.");
                    LOG.info(updateResult + " - " + updateResult + " queue entry(ies) was(were) resumed.");
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
        ansReturn.setResultMessage(msg);
        return ansReturn;
    }

    @Override
    public Answer updateToErrorForce(long id, String comment) {
        MessageEvent msg = null;
        String query
                = "UPDATE `" + TABLE + "` "
                + "SET `" + COLUMN_STATE + "` = 'ERROR', `" + COLUMN_REQUEST_DATE + "` = now(), `" + COLUMN_DATEMODIF + "` = now(), `" + COLUMN_COMMENT + "` = ? "
                + "WHERE `" + COLUMN_ID + "` = ? "
                + "AND `" + COLUMN_STATE + "` IN ('QUEUED','QUWITHDEP')";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setString(i++, comment);
                preStat.setLong(i++, id);

                int updateResult = preStat.executeUpdate();
                if (updateResult <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_NOUPDATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%DESCRIPTION%", "Unable to move state to ERROR (forced) for execution in queue " + id + " (update result: " + updateResult + "). Maybe execution is no longuer in QUEUED or QUWITHDEP state ?"));
                    LOG.warn("Unable to move state to ERROR (forced) for execution in queue " + id + " (update result: " + updateResult + "). Maybe execution is no longuer in QUEUED or QUWITHDEP state ?");
//                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));

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
        return new Answer(msg);
    }

    @Override
    public Answer delete(TestCaseExecutionQueue object) {
        MessageEvent msg = null;
        final String query = "DELETE FROM testcaseexecutionqueue WHERE `ID` = ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setLong(1, object.getId());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "DELETE"));
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
        return new Answer(msg);
    }

    @Override
    public Answer delete(Long id) {
        MessageEvent msg = null;
        final String query = "DELETE FROM testcaseexecutionqueue WHERE `ID` = ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setLong(1, id);

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "DELETE"));
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
        return new Answer(msg);
    }

    @Override
    public TestCaseExecutionQueue loadFromResultSet(ResultSet resultSet) throws FactoryCreationException, SQLException {
        return factoryTestCaseExecutionInQueue.create(
                resultSet.getLong(COLUMN_ID),
                resultSet.getString(COLUMN_SYSTEM),
                resultSet.getString(COLUMN_TEST),
                resultSet.getString(COLUMN_TEST_CASE),
                resultSet.getString(COLUMN_COUNTRY),
                resultSet.getString(COLUMN_ENVIRONMENT),
                resultSet.getString(COLUMN_ROBOT),
                resultSet.getString(COLUMN_ROBOTDECLI),
                resultSet.getString(COLUMN_ROBOT_IP),
                resultSet.getString(COLUMN_ROBOT_PORT),
                resultSet.getString(COLUMN_BROWSER),
                resultSet.getString(COLUMN_BROWSER_VERSION),
                resultSet.getString(COLUMN_PLATFORM),
                resultSet.getString(COLUMN_SCREENSIZE),
                resultSet.getInt(COLUMN_MANUAL_URL),
                resultSet.getString(COLUMN_MANUAL_HOST),
                resultSet.getString(COLUMN_MANUAL_CONTEXT_ROOT),
                resultSet.getString(COLUMN_MANUAL_LOGIN_RELATIVE_URL),
                resultSet.getString(COLUMN_MANUAL_ENV_DATA),
                resultSet.getString(COLUMN_TAG),
                resultSet.getInt(COLUMN_SCREENSHOT),
                resultSet.getInt(COLUMN_VIDEO),
                resultSet.getInt(COLUMN_VERBOSE),
                resultSet.getString(COLUMN_TIMEOUT),
                resultSet.getInt(COLUMN_PAGE_SOURCE),
                resultSet.getInt(COLUMN_ROBOT_LOG),
                resultSet.getInt(COLUMN_CONSOLE_LOG),
                new Date(resultSet.getTimestamp(COLUMN_REQUEST_DATE).getTime()),
                TestCaseExecutionQueue.State.valueOf(resultSet.getString(COLUMN_STATE)),
                resultSet.getInt(COLUMN_PRIORITY),
                resultSet.getString(COLUMN_COMMENT),
                resultSet.getString(COLUMN_DEBUGFLAG),
                resultSet.getInt(COLUMN_RETRIES),
                resultSet.getInt("AlreadyExecuted"),
                resultSet.getString(COLUMN_MANUAL_EXECUTION),
                resultSet.getLong(COLUMN_EXEID),
                resultSet.getString(COLUMN_USRCREATED),
                resultSet.getTimestamp(COLUMN_DATECREATED),
                resultSet.getString(COLUMN_USRMODIF),
                resultSet.getTimestamp(COLUMN_DATEMODIF)
        );
    }

    private TestCaseExecutionQueueToTreat loadQueueToTreatFromResultSet(ResultSet resultSet) throws SQLException {
        TestCaseExecutionQueueToTreat inQueue = new TestCaseExecutionQueueToTreat();
        try {

            inQueue.setId(resultSet.getInt("exq.id"));
            inQueue.setManualExecution(resultSet.getString("exq.manualexecution"));
            inQueue.setSystem(resultSet.getString("app.system"));
            inQueue.setEnvironment(resultSet.getString("cea.environment"));
            inQueue.setCountry(resultSet.getString("cea.country"));
            inQueue.setApplication(resultSet.getString("cea.application"));
            inQueue.setPoolSizeAppEnvironment(resultSet.getInt("cea.poolsize"));
            inQueue.setPoolSizeApplication(resultSet.getInt("app.poolsize"));
            inQueue.setDebugFlag(resultSet.getString("exq.DebugFlag"));
            /**
             * Robot host is feed only if application type really required a
             * robot. data comes from robot by priority or exe when exist.
             */
            String queueRobot = "";
            String queueRobotHost = "";
            String queueRobotPort = "";
            String appType = resultSet.getString("app.type");
            if (appType == null) {
                appType = "";
            }
            inQueue.setAppType(appType);

            // If application type require a selenium/appium/sikuli server, we get the robot host from robot and not execution queue.
            if ((appType.equals(Application.TYPE_APK)) || (appType.equals(Application.TYPE_GUI)) || (appType.equals(Application.TYPE_FAT)) || (appType.equals(Application.TYPE_IPA))) {
//                robotHost = resultSet.getString("rbt.host");
                queueRobot = resultSet.getString("exq.robot");
                if (StringUtil.isEmptyOrNull(queueRobotHost)) {
                    queueRobotHost = resultSet.getString("exq.robotIP");
                    queueRobotPort = resultSet.getString("exq.robotPort");
                }
            }
            inQueue.setQueueRobot(queueRobot);
            inQueue.setQueueRobotHost(queueRobotHost);
            inQueue.setQueueRobotPort(queueRobotPort);
            inQueue.setSelectedRobotHost(resultSet.getString("exq.SelectedRobotHost"));
            inQueue.setSelectedRobotExtensionHost(resultSet.getString("exq.SelectedExtensionHost"));

        } catch (Exception e) {
            LOG.debug("Exception in load queue from resultset : " + e.toString());
        }
        return inQueue;
    }

    /**
     * Uses data of ResultSet to create object {@link TestCaseExecutionQueue}
     *
     * @param resultSet ResultSet relative to select from table
     * TestCaseExecutionInQueue
     * @return object {@link TestCaseExecutionQueue} with objects
     * {@link ResultSet} and {@link Application}
     * @throws SQLException when trying to get value from
     * {@link java.sql.ResultSet#getString(String)}
     * @see TestCaseExecutionQueue
     */
    private TestCaseExecutionQueue loadWithDependenciesFromResultSet(ResultSet resultSet) throws SQLException, FactoryCreationException {
        TestCaseExecutionQueue testCaseExecutionInQueue = this.loadFromResultSet(resultSet);
        testCaseExecutionInQueue.setTestCaseObj(testCaseDAO.loadFromResultSet(resultSet));
        testCaseExecutionInQueue.setApplicationObj(applicationDAO.loadFromResultSet(resultSet));
        return testCaseExecutionInQueue;
    }

}
