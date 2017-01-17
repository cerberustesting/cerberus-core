/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.crud.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.crud.dao.IApplicationDAO;
import org.cerberus.crud.dao.ITestCaseDAO;
import org.cerberus.crud.dao.ITestCaseExecutionInQueueDAO;
import org.cerberus.crud.entity.Application;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.crud.entity.TestCaseExecutionInQueue;
import org.cerberus.crud.factory.IFactoryApplication;
import org.cerberus.exception.CerberusException;
import org.cerberus.exception.FactoryCreationException;
import org.cerberus.crud.factory.IFactoryTestCaseExecutionInQueue;
import org.cerberus.log.MyLogger;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TestCaseExecutionInQueueDAO implements ITestCaseExecutionInQueueDAO {

    /**
     * The associated {@link Logger} to this class
     */
    private static final Logger LOG = Logger.getLogger(TestCaseExecutionInQueueDAO.class);

    private static final String TABLE = "testcaseexecutionqueue";
    private static final String TABLE_TEST_CASE = "testcase";
    private static final String TABLE_APPLICATION = "application";

    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_TEST = "Test";
    private static final String COLUMN_TEST_CASE = "TestCase";
    private static final String COLUMN_COUNTRY = "Country";
    private static final String COLUMN_ENVIRONMENT = "Environment";
    private static final String COLUMN_ROBOT = "Robot";
    private static final String COLUMN_ROBOT_IP = "RobotIP";
    private static final String COLUMN_ROBOT_PORT = "RobotPort";
    private static final String COLUMN_BROWSER = "Browser";
    private static final String COLUMN_BROWSER_VERSION = "BrowserVersion";
    private static final String COLUMN_PLATFORM = "Platform";
    private static final String COLUMN_MANUAL_URL = "ManualURL";
    private static final String COLUMN_MANUAL_HOST = "ManualHost";
    private static final String COLUMN_MANUAL_CONTEXT_ROOT = "ManualContextRoot";
    private static final String COLUMN_MANUAL_LOGIN_RELATIVE_URL = "ManualLoginRelativeURL";
    private static final String COLUMN_MANUAL_ENV_DATA = "ManualEnvData";
    private static final String COLUMN_TAG = "Tag";
    private static final String COLUMN_OUTPUT_FORMAT = "OutputFormat";
    private static final String COLUMN_SCREENSHOT = "Screenshot";
    private static final String COLUMN_VERBOSE = "Verbose";
    private static final String COLUMN_TIMEOUT = "Timeout";
    private static final String COLUMN_SYNCHRONEOUS = "Synchroneous";
    private static final String COLUMN_PAGE_SOURCE = "PageSource";
    private static final String COLUMN_SELENIUM_LOG = "SeleniumLog";
    private static final String COLUMN_REQUEST_DATE = "RequestDate";
    private static final String COLUMN_COMMENT = "Comment";
    private static final String COLUMN_RETRIES = "Retries";
    private static final String COLUMN_MANUAL_EXECUTION = "ManualExecution";
    private static final String COLUMN_STATE = "State";

    private static final String VALUE_MANUAL_EXECUTION_FALSE = "N";

    private static final String QUERY_INSERT = "INSERT INTO `" + TABLE + "` (`" + COLUMN_TEST + "`, `" + COLUMN_TEST_CASE + "`, `" + COLUMN_COUNTRY + "`, `" + COLUMN_ENVIRONMENT + "`, `" + COLUMN_ROBOT + "`, `" + COLUMN_ROBOT_IP + "`, `" + COLUMN_ROBOT_PORT + "`, `" + COLUMN_BROWSER + "`, `" + COLUMN_BROWSER_VERSION + "`, `" + COLUMN_PLATFORM + "`, `" + COLUMN_MANUAL_URL + "`, `" + COLUMN_MANUAL_HOST + "`, `" + COLUMN_MANUAL_CONTEXT_ROOT + "`, `" + COLUMN_MANUAL_LOGIN_RELATIVE_URL + "`, `" + COLUMN_MANUAL_ENV_DATA + "`, `" + COLUMN_TAG + "`, `" + COLUMN_OUTPUT_FORMAT + "`, `" + COLUMN_SCREENSHOT + "`, `" + COLUMN_VERBOSE + "`, `" + COLUMN_TIMEOUT + "`, `" + COLUMN_SYNCHRONEOUS + "`, `" + COLUMN_PAGE_SOURCE + "`, `" + COLUMN_SELENIUM_LOG + "`, `" + COLUMN_REQUEST_DATE + "`, `" + COLUMN_RETRIES + "`, `" + COLUMN_MANUAL_EXECUTION + "`, `" + COLUMN_STATE + "`) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
    private static final String QUERY_REMOVE = "DELETE FROM `" + TABLE + "` WHERE `" + COLUMN_ID + "` = ?";
    private static final String QUERY_FIND_BY_KEY = "SELECT * FROM `" + TABLE + "` WHERE `" + COLUMN_ID + "` = ?";
    private static final String QUERY_FIND_BY_KEY_WITH_DEPENDENCIES =
            "SELECT * FROM `" + TABLE + "` exq " +
                    "INNER JOIN `" + TABLE_TEST_CASE + "` tec ON (exq.`" + COLUMN_TEST + "` = tec.`Test` AND exq.`" + COLUMN_TEST_CASE + "` = tec.`TestCase`) " +
                    "INNER JOIN `" + TABLE_APPLICATION + "` app ON (tec.`Application` = app.`Application`) " +
                    "WHERE `" + COLUMN_ID + "` = ?";
    private static final String QUERY_FIND_BY_STATE = "SELECT * FROM `" + TABLE + "` WHERE `" + COLUMN_STATE + "` = ? ORDER BY `" + COLUMN_ID + "` ASC";
    private static final String QUERY_FIND_BY_STATE_LIMIT = "SELECT * FROM `" + TABLE + "` WHERE `" + COLUMN_STATE + "` = ? ORDER BY `" + COLUMN_ID + "` ASC LIMIT ?";
    private static final String QUERY_GET_ALL = "SELECT * FROM `" + TABLE + "`;";
    private static final String QUERY_UPDATE_STATE = "UPDATE `" + TABLE + "` SET `" + COLUMN_STATE + "` = ? WHERE `" + COLUMN_ID + "` = ?";
    private static final String QUERY_UPDATE_STATE_AND_COMMENT = "UPDATE `" + TABLE + "` SET `" + COLUMN_STATE + "` = ?, `" + COLUMN_COMMENT +  "` = ? WHERE `" + COLUMN_ID + "` = ?";

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseExecutionInQueue factoryTestCaseExecutionInQueue;
    @Autowired
    private IFactoryApplication factoryApplication;
    @Autowired
    private ITestCaseDAO testCaseDAO;
    @Autowired
    private IApplicationDAO applicationDAO;

    private final int MAX_ROW_SELECTED = 100000;

    private final String OBJECT_NAME = "TestCaseExecutionInQueue";

    /**
     * Declare SQL queries used by this {@link RobotCapabilityDAO}
     *
     * @author Aurelien Bourdon
     */
    private static interface Query {

        /**
         * Create a new {@link TestCaseExecutionInQueueDAO}
         */
        String CREATE = "INSERT INTO `testcaseexecutionqueue` (`test`, `testCase`, `country`, `environment`, `robot`, `robotIP`, `robotPort`, `browser`, `browserVersion`, `platform`, `manualURL`, `manualContextRoot`, `manualLoginRelativeURL`, `manualEnvData`, `tag`, `outputFormat`, `screenshot`, `verbose`, `timeout`, `synchroneous`, `pageSource`, `seleniumLog`, `requestDate`, `proceeded`, `comment`, `retries`, `manualexecution`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        /**
         * Update an existing {@link TestCaseExecutionInQueueDAO}
         */
        String UPDATE = "UPDATE `testcaseexecutionqueue` SET `test` = ? and `testCase` = ? and `country` = ? and `environment` = ? and `robot` = ? and `robotIP` = ? and `robotPort` = ? and `browser` = ? and `browserVersion` = ? and `platform` = ? and `manualURL` = ? and `manualContextRoot` = ? and `manualLoginRelativeURL` = ? and `manualEnvData` = ? and `tag` = ? and `outputFormat` = ? and `screenshot` = ? and `verbose` = ? and `timeout` = ? and `synchroneous` = ? and `pageSource` = ? and `seleniumLog` = ? and `requestDate` = ? and `proceeded` = ? and `comment` = ? and `retries` = ? and `manualexecution` = ? WHERE `id` = ? ";

        /**
         * Remove an existing {@link TestCaseExecutionInQueueDAO}
         */
        String DELETE = "DELETE FROM `testcaseexecutionqueue` WHERE `ID` = ? ";
    }

    @Override
    public void insert(TestCaseExecutionInQueue inQueue) throws CerberusException {
        Connection connection = this.databaseSpring.connect();
        if (connection == null) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }

        PreparedStatement statementInsert = null;

        try {
            statementInsert = connection.prepareStatement(QUERY_INSERT);
            statementInsert.setString(1, inQueue.getTest());
            statementInsert.setString(2, inQueue.getTestCase());
            statementInsert.setString(3, inQueue.getCountry());
            statementInsert.setString(4, inQueue.getEnvironment());
            statementInsert.setString(5, inQueue.getRobot());
            statementInsert.setString(6, inQueue.getRobotIP());
            statementInsert.setString(7, inQueue.getRobotPort());
            statementInsert.setString(8, inQueue.getBrowser());
            statementInsert.setString(9, inQueue.getBrowserVersion());
            statementInsert.setString(10, inQueue.getPlatform());
            statementInsert.setBoolean(11, inQueue.isManualURL());
            statementInsert.setString(12, inQueue.getManualHost());
            statementInsert.setString(13, inQueue.getManualContextRoot());
            statementInsert.setString(14, inQueue.getManualLoginRelativeURL());
            statementInsert.setString(15, inQueue.getManualEnvData());
            statementInsert.setString(16, inQueue.getTag());
            statementInsert.setString(17, inQueue.getOutputFormat());
            statementInsert.setInt(18, inQueue.getScreenshot());
            statementInsert.setInt(19, inQueue.getVerbose());
            statementInsert.setString(20, inQueue.getTimeout());
            statementInsert.setBoolean(21, inQueue.isSynchroneous());
            statementInsert.setInt(22, inQueue.getPageSource());
            statementInsert.setInt(23, inQueue.getSeleniumLog());
            statementInsert.setTimestamp(24, new Timestamp(inQueue.getRequestDate().getTime()));
            statementInsert.setInt(25, inQueue.getRetries());
            statementInsert.setString(26, inQueue.isManualExecution() ? "Y" : "N");
            statementInsert.setString(27, inQueue.getState() == null ? TestCaseExecutionInQueue.State.WAITING.name() : inQueue.getState().name());

            statementInsert.executeUpdate();
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.getMessage());
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        } finally {
            if (statementInsert != null) {
                try {
                    statementInsert.close();
                } catch (SQLException e) {
                    LOG.warn("Unable to close insert statement due to " + e.getMessage());
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOG.warn("Unable to close connection due to " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void remove(long id) throws CerberusException {
        Connection connection = this.databaseSpring.connect();
        if (connection == null) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }

        PreparedStatement statementRemove = null;

        try {
            statementRemove = connection.prepareStatement(QUERY_REMOVE);
            statementRemove.setLong(1, id);

            statementRemove.executeUpdate();
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.getMessage());
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        } finally {
            if (statementRemove != null) {
                try {
                    statementRemove.close();
                } catch (SQLException e) {
                    LOG.warn("Unable to close remove statement due to " + e.getMessage());
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOG.warn("Unable to close connection due to " + e.getMessage());
                }
            }
        }
    }

    @Override
    public List<TestCaseExecutionInQueue> findTestCaseExecutionInQueuebyTag(String tag) throws CerberusException {
        boolean throwEx = false;
        final StringBuilder query = new StringBuilder("select exq.*, tec.*, app.* from ( select exq.* ")
                .append("from testcaseexecutionqueue exq ")
                .append("where exq.tag = ? ")
                .append(" order by exq.test, exq.testcase, exq.ID desc) as exq ")
                .append("LEFT JOIN testcase tec on exq.Test = tec.Test and exq.TestCase = tec.TestCase ")
                .append("LEFT JOIN application app ON tec.application = app.application ")
                .append("GROUP BY exq.test, exq.testcase, exq.Environment, exq.Browser, exq.Country ");

        List<TestCaseExecutionInQueue> testCaseExecutionInQueueList = new ArrayList<TestCaseExecutionInQueue>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());

            preStat.setString(1, tag);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testCaseExecutionInQueueList.add(this.loadWithDependenciesFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    testCaseExecutionInQueueList = null;
                } catch (FactoryCreationException ex) {
                    LOG.error("Unable to execute query : " + ex.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                testCaseExecutionInQueueList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            testCaseExecutionInQueueList = null;
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
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return testCaseExecutionInQueueList;
    }

    @Override
    public TestCaseExecutionInQueue findByKey(long id) throws CerberusException {
        final Connection connection = this.databaseSpring.connect();
        if (connection == null) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }

        PreparedStatement statement = null;

        TestCaseExecutionInQueue result = null;

        try {
            statement = connection.prepareStatement(QUERY_FIND_BY_KEY);
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                try {
                    result = (loadFromResultSet(resultSet));
                } catch (FactoryCreationException fce) {
                    LOG.warn("Unable to get malformed record from database", fce);
                }
            }

            return result;
        } catch (SQLException sqle) {
            LOG.warn("Unable to execute query : " + sqle.getMessage() + ". Trying to rollback");
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e) {
                    LOG.error("Unable to rollback due to " + e.getMessage());
                }
                LOG.warn("Rollback done");
            }
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOG.warn("Unable to close statement due to " + e.getMessage());
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOG.warn("Unable to close connection due to " + e.getMessage());
                }
            }
        }
    }

    @Override
    public TestCaseExecutionInQueue findByKeyWithDependencies(long key) throws CerberusException {
        try (
                Connection connection = databaseSpring.connect();
                PreparedStatement statement = connection.prepareStatement(QUERY_FIND_BY_KEY_WITH_DEPENDENCIES)
        ) {
            statement.setLong(1, key);
            ResultSet result = statement.executeQuery();
            if (result == null || !result.next()) {
                throw new FactoryCreationException("Unknown key");
            }
            return loadWithDependenciesFromResultSet(result);
        } catch (SQLException | FactoryCreationException e) {
            LOG.warn("Unable to find execution in queue with key " + key, e);
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
        }
    }

    @Override
    public List<TestCaseExecutionInQueue> toQueued(int fetchSize) throws CerberusException {
        try (
                Connection connection = this.databaseSpring.connect()
        ) {
            List<TestCaseExecutionInQueue> result = new ArrayList<TestCaseExecutionInQueue>();
            final String selectByStateQuery = UNLIMITED_FETCH_SIZE == fetchSize ? QUERY_FIND_BY_STATE : QUERY_FIND_BY_STATE_LIMIT;

            // Begin transaction
            connection.setAutoCommit(false);
            try (
                    PreparedStatement selectWaitingsStatement = connection.prepareStatement(selectByStateQuery);
                    PreparedStatement updateStateStatement = connection.prepareStatement(QUERY_UPDATE_STATE)
            ) {
                // Select all executions in queue in WAITING state
                selectWaitingsStatement.setString(1, TestCaseExecutionInQueue.State.WAITING.name());
                if (UNLIMITED_FETCH_SIZE != fetchSize) {
                    selectWaitingsStatement.setInt(2, fetchSize);
                }
                ResultSet waitings = selectWaitingsStatement.executeQuery();

                // Then set their state to QUEUED
                while (waitings.next()) {
                    try {
                        TestCaseExecutionInQueue waiting = loadFromResultSet(waitings);
                        updateStateStatement.setString(1, TestCaseExecutionInQueue.State.QUEUED.name());
                        updateStateStatement.setLong(2, waiting.getId());
                        updateStateStatement.addBatch();
                        result.add(waiting);
                    } catch (SQLException | FactoryCreationException e) {
                        LOG.warn("Unable to forward execution id " + waitings.getLong(COLUMN_ID), e);
                    }
                }
                if (!result.isEmpty()) {
                    updateStateStatement.executeBatch();
                }

                // Commit transaction
                connection.commit();
                return result;
            } catch (SQLException e) {
                handleRollback(connection);
                throw e;
            }
        } catch (SQLException e) {
            LOG.warn("Unable to find or forward WAITING executions in queue to the QUEUED state", e);
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
        }
    }

    @Override
    public void toExecuting(long id) throws CerberusException {
        try (
                Connection connection = databaseSpring.connect()
        ) {
            // Begin transaction
            connection.setAutoCommit(false);
            try (
                    PreparedStatement findByKeyStatement = connection.prepareStatement(QUERY_FIND_BY_KEY);
                    PreparedStatement updateStateStatement = connection.prepareStatement(QUERY_UPDATE_STATE)
            ) {
                // First, find the execution
                TestCaseExecutionInQueue inqueue = findByKey(id, findByKeyStatement);

                // Then, check if its current state is QUEUED
                if (!TestCaseExecutionInQueue.State.QUEUED.equals(inqueue.getState())) {
                    LOG.warn("Execution in queue id " + id + " is not on the QUEUED state (" + inqueue.getState().name());
                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
                }

                // Finally try to update execution
                updateState(id, TestCaseExecutionInQueue.State.EXECUTING, updateStateStatement);

                // Commit transaction
                connection.commit();
            } catch (SQLException | FactoryCreationException e) {
                handleRollback(connection);
                throw e;
            }
        } catch (SQLException | FactoryCreationException e) {
            LOG.warn("Unable to set state to EXECUTING for execution in queue id " + id, e);
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
        }
    }

    @Override
    public void toWaiting(long id) throws CerberusException {
        try (
                Connection connection = databaseSpring.connect()
        ) {
            // Begin transaction
            connection.setAutoCommit(false);
            try (
                    PreparedStatement findByKeyStatement = connection.prepareStatement(QUERY_FIND_BY_KEY);
                    PreparedStatement updateStateStatement = connection.prepareStatement(QUERY_UPDATE_STATE)
            ) {
                // First, find the execution
                TestCaseExecutionInQueue inqueue = findByKey(id, findByKeyStatement);

                // Then, check if its current state is not EXECUTING
                if (TestCaseExecutionInQueue.State.EXECUTING.equals(inqueue.getState())) {
                    LOG.warn("Execution in queue id " + id + " cannot be WAITING as currently EXECUTING");
                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
                }

                // Finally, try to update its state
                updateState(id, TestCaseExecutionInQueue.State.WAITING, updateStateStatement);

                // Commit transaction
                connection.commit();
            } catch (SQLException | FactoryCreationException e) {
                handleRollback(connection);
                throw e;
            }
        } catch (SQLException | FactoryCreationException e) {
            LOG.warn("Unable to set state to WAITING for execution in queue id " + id, e);
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
        }
    }

    @Override
    public void toError(long id, String comment) throws CerberusException {
        try (
                Connection connection = databaseSpring.connect()
        ) {
            // Begin transaction
            connection.setAutoCommit(false);
            try (
                    PreparedStatement findByKeyStatement = connection.prepareStatement(QUERY_FIND_BY_KEY);
                    PreparedStatement updateStateAndCommentStatement = connection.prepareStatement(QUERY_UPDATE_STATE_AND_COMMENT)
            ) {
                // First, find the execution
                findByKey(id, findByKeyStatement);

                // Then try to update its state to error
                updateStateAndComment(id, TestCaseExecutionInQueue.State.ERROR, comment, updateStateAndCommentStatement);

                // Commit transaction
                connection.commit();
            } catch (SQLException | FactoryCreationException e) {
                handleRollback(connection);
                throw e;
            }
        } catch (SQLException | FactoryCreationException e) {
            LOG.warn("Unable to set state to ERROR for execution in queue id " + id, e);
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
        }
    }

    @Override
    public void toCancelled(long id) throws CerberusException {
        try (
                Connection connection = databaseSpring.connect()
        ) {
            // Begin transaction
            connection.setAutoCommit(false);
            try (
                    PreparedStatement findByKeyStatement = connection.prepareStatement(QUERY_FIND_BY_KEY);
                    PreparedStatement updateStateStatement = connection.prepareStatement(QUERY_UPDATE_STATE)
            ) {
                // First, find the execution
                TestCaseExecutionInQueue inqueue = findByKey(id, findByKeyStatement);

                // Then, check if its current state is not EXECUTING or ERROR
                if (TestCaseExecutionInQueue.State.EXECUTING.equals(inqueue.getState()) || TestCaseExecutionInQueue.State.ERROR.equals(inqueue.getState())) {
                    LOG.warn("Execution in queue id " + id + " cannot be WAITING as currently EXECUTING");
                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
                }

                // Finally, try to update its state
                updateState(id, TestCaseExecutionInQueue.State.CANCELLED, updateStateStatement);

                // Commit transaction
                connection.commit();
            } catch (SQLException | FactoryCreationException e) {
                handleRollback(connection);
                throw e;
            }
        } catch (SQLException | FactoryCreationException e) {
            LOG.warn("Unable to set state to CANCELLED for execution in queue id " + id, e);
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
        }
    }


    @Override
    public List<TestCaseExecutionInQueue> findAll() throws CerberusException {
        final Connection connection = this.databaseSpring.connect();
        if (connection == null) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }

        PreparedStatement statement = null;

        List<TestCaseExecutionInQueue> result = new ArrayList<TestCaseExecutionInQueue>();

        try {
            statement = connection.prepareStatement(QUERY_GET_ALL);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                try {
                    result.add(loadFromResultSet(resultSet));
                } catch (FactoryCreationException fce) {
                    LOG.warn("Unable to get malformed record from database", fce);
                }
            }

            return result;
        } catch (SQLException sqle) {
            LOG.warn("Unable to execute query : " + sqle.getMessage() + ". Trying to rollback");
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e) {
                    LOG.error("Unable to rollback due to " + e.getMessage());
                }
                LOG.warn("Rollback done");
            }
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOG.warn("Unable to close statement due to " + e.getMessage());
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOG.warn("Unable to close connection due to " + e.getMessage());
                }
            }
        }
    }

    @Override
    public AnswerList readByTagByCriteria(String tag, int start, int amount, String sort, String searchTerm, Map<String, List<String>> individualSearch) throws CerberusException {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        AnswerList answer = new AnswerList();
        List<String> individalColumnSearchValues = new ArrayList<String>();

        final StringBuilder query = new StringBuilder();

        query.append("SELECT * FROM testcaseexecutionqueue exq ");
        query.append("left join testcase tec on exq.Test = tec.Test and exq.TestCase = tec.TestCase ");
        query.append("left join application app on tec.application = app.application ");
        query.append("where exq.ID IN ");
        query.append("(select MAX(exq.ID) from testcaseexecutionqueue exq ");

        query.append("where 1=1 ");
        if (!StringUtil.isNullOrEmpty(tag)) {
            query.append("and exq.tag = ? ");
        }

        query.append("group by exq.test, exq.testcase, exq.Environment, exq.Browser, exq.Country) ");
        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            query.append("and (exq.`test` like ? ");
            query.append(" or exq.`testCase` like ? ");
            query.append(" or exq.`application` like ? ");
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
        }

        List<TestCaseExecutionInQueue> testCaseExecutionInQueueList = new ArrayList<TestCaseExecutionInQueue>();
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
                        testCaseExecutionInQueueList.add(this.loadWithDependenciesFromResultSet(resultSet));
                    }

                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecutionInQueue").replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList(testCaseExecutionInQueueList, testCaseExecutionInQueueList.size());
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
    public AnswerList readByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {

        AnswerList response = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCaseExecutionInQueue> objectList = new ArrayList<TestCaseExecutionInQueue>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<String>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM testcaseexecutionqueue exq ");

        query.append(" WHERE 1=1");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
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
                String key = "IFNULL(exq." + entry.getKey() + ",'')";
                String q = SqlUtil.getInSQLClauseForPreparedStatement(key, entry.getValue());
                if (q == null || q == "") {
                    q = "(exq." + entry.getKey() + " IS NULL OR " + entry.getKey() + " = '')";
                }
                searchSQL.append(q);
                individalColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }

        query.append(searchSQL);

        if (!StringUtil.isNullOrEmpty(column)) {
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

                if (!StringUtil.isNullOrEmpty(searchTerm)) {
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
                        response = new AnswerList(objectList, nrTotalRows);
                    } else if (objectList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList(objectList, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList(objectList, nrTotalRows);
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
    public AnswerList readDistinctEnvCoutnryBrowserByTag(String tag) {
        AnswerList answer = new AnswerList();
        StringBuilder query = new StringBuilder();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);

        query.append("SELECT exq.* FROM testcaseexecutionqueue exq WHERE tag = ? GROUP BY Environment, Country, Browser");

        Connection connection = this.databaseSpring.connect();

        List<TestCaseExecutionInQueue> EnvCountryBrowserList = new ArrayList<TestCaseExecutionInQueue>();

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
                    answer = new AnswerList(EnvCountryBrowserList, EnvCountryBrowserList.size());
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionInQueueDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    EnvCountryBrowserList = null;
                } catch (FactoryCreationException ex) {
                    MyLogger.log(TestCaseExecutionInQueueDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    EnvCountryBrowserList = null;
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestCaseExecutionInQueueDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                EnvCountryBrowserList = null;
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException ex) {
            MyLogger.log(TestCaseExecutionInQueueDAO.class.getName(), Level.WARN, ex.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                MyLogger.log(TestCaseExecutionInQueueDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
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
                distinct.append("exq.Environment");
                prev++;
            }
            if (country) {
                if (prev != 0) {
                    prev = 0;
                    distinct.append(",");
                }
                distinct.append("exq.Country");
                prev++;
            }
            if (browser) {
                if (prev != 0) {
                    prev = 0;
                    distinct.append(",");
                }
                distinct.append("exq.Browser");
                prev++;
            }
            if (app) {
                if (prev != 0) {
                    prev = 0;
                    distinct.append(",");
                }
                distinct.append("tec.Application");
            }

            query.append("SELECT tec.test, tec.testcase, exq.tag,  ");
            query.append(distinct.toString());
            query.append(" FROM testcase tec LEFT JOIN testcaseexecutionqueue exq ON exq.Test = tec.Test AND exq.TestCase = tec.TestCase WHERE tag = ? GROUP BY ");
            query.append(distinct.toString());
        } else {
            //If there is no distinct, select nothing
            query.append("SELECT * FROM testcaseexecutionqueue exq WHERE 1=0 AND tag = ?");
        }

        Connection connection = this.databaseSpring.connect();

        List<TestCaseExecutionInQueue> column = new ArrayList<TestCaseExecutionInQueue>();

        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());

            preStat.setString(1, tag);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        TestCaseExecutionInQueue tmp = new TestCaseExecutionInQueue();
                        tmp.setTest(resultSet.getString("tec.test"));
                        tmp.setTestCase(resultSet.getString("tec.testcase"));
                        tmp.setTag(resultSet.getString("exq.tag"));
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
                            Application application = factoryApplication.create(resultSet.getString("Application"));
                            tmp.setApplicationObj(application);
                        }
                        column.add(tmp);
                    }
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecution").replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList(column, column.size());
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionInQueueDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    column = null;
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestCaseExecutionInQueueDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                column = null;
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException ex) {
            MyLogger.log(TestCaseExecutionInQueueDAO.class.getName(), Level.WARN, ex.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                MyLogger.log(TestCaseExecutionInQueueDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }

        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerList findTagList(int tagnumber) {
        AnswerList response = new AnswerList();
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
    public AnswerList readBySystemByVarious(String system, List<String> testList, List<String> applicationList, List<String> projectList, List<String> tcstatusList, List<String> groupList, List<String> tcactiveList, List<String> priorityList, List<String> targetsprintList, List<String> targetrevisionList, List<String> creatorList, List<String> implementerList, List<String> buildList, List<String> revisionList, List<String> environmentList, List<String> countryList, List<String> browserList, List<String> tcestatusList, String ip, String port, String tag, String browserversion, String comment, String bugid, String ticket) {
        AnswerList answer = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        List<TestCaseExecutionInQueue> tceList = new ArrayList<TestCaseExecutionInQueue>();
        List<String> whereClauses = new LinkedList<String>();

        StringBuilder query = new StringBuilder();

        int paramNumber = 0;

        query.append(" select t.ID as statusExecutionID, t.* from ( ");
        query.append(" select exq.*, tec.*, app.* ");
        query.append(" from testcaseexecutionqueue exq ");
        query.append(" inner join testcase tec on exq.test = tec.test and exq.testcase = tec.testcase ");
        query.append(" inner join application app on exq.application = app.application ");

        String testClause = SqlUtil.generateInClause("exq.test", testList);
        if (!StringUtil.isNullOrEmpty(testClause)) {
            whereClauses.add(testClause);
        }

        String applicationClause = SqlUtil.generateInClause("exq.application", applicationList);
        if (!StringUtil.isNullOrEmpty(applicationClause)) {
            whereClauses.add(applicationClause);
        }

        String projectClause = SqlUtil.generateInClause("tec.project", projectList);
        if (!StringUtil.isNullOrEmpty(projectClause)) {
            whereClauses.add(projectClause);
        }
        //test case status: working, fully_implemented, ...
        String tcsClause = SqlUtil.generateInClause("exq.status", tcstatusList);
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
        String buildClause = SqlUtil.generateInClause("exq.Build", buildList);
        if (!StringUtil.isNullOrEmpty(buildClause)) {
            whereClauses.add(buildClause);
        }
        //revision
        String revisionClause = SqlUtil.generateInClause("exq.Revision", revisionList);
        if (!StringUtil.isNullOrEmpty(revisionClause)) {
            whereClauses.add(revisionClause);
        }
        //environment
        String environmentClause = SqlUtil.generateInClause("exq.Environment", environmentList);
        if (!StringUtil.isNullOrEmpty(environmentClause)) {
            whereClauses.add(environmentClause);
        }
        //country
        String countryClause = SqlUtil.generateInClause("exq.Country", countryList);
        if (!StringUtil.isNullOrEmpty(countryClause)) {
            whereClauses.add(countryClause);
        }
        //browser
        String browserClause = SqlUtil.generateInClause("exq.Browser", browserList);
        if (!StringUtil.isNullOrEmpty(browserClause)) {
            whereClauses.add(browserClause);
        }
        //test case execution
        String tcestatusClause = SqlUtil.generateInClause("exq.ControlStatus", tcestatusList);
        if (!StringUtil.isNullOrEmpty(tcestatusClause)) {
            whereClauses.add(tcestatusClause);
        }

        if (!StringUtil.isNullOrEmpty(system)) {
            whereClauses.add(" app.system like ? ");
        }
        if (!StringUtil.isNullOrEmpty(ip)) {
            whereClauses.add(" exq.IP like ? ");
        }
        if (!StringUtil.isNullOrEmpty(port)) {
            whereClauses.add(" exq.port like ? ");
        }
        if (!StringUtil.isNullOrEmpty(tag)) {
            whereClauses.add(" exq.tag like ? ");
        }
        if (!StringUtil.isNullOrEmpty(browserversion)) {
            whereClauses.add(" exq.browserfullversion like ? ");
        }
        if (!StringUtil.isNullOrEmpty(comment)) {
            whereClauses.add(" exq.comment like ? ");
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
                        tceList.add(this.loadWithDependenciesFromResultSet(resultSet));
                    }
                    if (tceList.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecutionInQueue").replace("%OPERATION%", "SELECT"));
                    }

                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionInQueueDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    tceList.clear();
                } catch (FactoryCreationException ex) {
                    MyLogger.log(TestCaseExecutionInQueueDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    tceList.clear();
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestCaseExecutionInQueueDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException ex) {
            MyLogger.log(TestCaseExecutionInQueueDAO.class.getName(), Level.WARN, ex.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                MyLogger.log(TestCaseExecutionInQueueDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }
        answer.setTotalRows(tceList.size());
        answer.setDataList(tceList);
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerList readDistinctValuesByCriteria(String columnName, String sort, String searchTerm, Map<String, List<String>> individualSearch, String column) {
        AnswerList answer = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<String>();

        StringBuilder query = new StringBuilder();

        query.append("SELECT distinct exq.");
        query.append(columnName);
        query.append(" as distinctValues FROM testcaseexecutionqueue exq");
        query.append(" where 1=1");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
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
        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString())) {

            int i = 1;
            if (!StringUtil.isNullOrEmpty(searchTerm)) {
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

            //gets the data
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

    @Override
    public TestCaseExecutionInQueue loadFromResultSet(ResultSet resultSet) throws FactoryCreationException, SQLException {
        return factoryTestCaseExecutionInQueue.create(resultSet.getLong(COLUMN_ID),
                resultSet.getString(COLUMN_TEST),
                resultSet.getString(COLUMN_TEST_CASE),
                resultSet.getString(COLUMN_COUNTRY),
                resultSet.getString(COLUMN_ENVIRONMENT),
                resultSet.getString(COLUMN_ROBOT),
                resultSet.getString(COLUMN_ROBOT_IP),
                resultSet.getString(COLUMN_ROBOT_PORT),
                resultSet.getString(COLUMN_BROWSER),
                resultSet.getString(COLUMN_BROWSER_VERSION),
                resultSet.getString(COLUMN_PLATFORM),
                resultSet.getBoolean(COLUMN_MANUAL_URL),
                resultSet.getString(COLUMN_MANUAL_HOST),
                resultSet.getString(COLUMN_MANUAL_CONTEXT_ROOT),
                resultSet.getString(COLUMN_MANUAL_LOGIN_RELATIVE_URL),
                resultSet.getString(COLUMN_MANUAL_ENV_DATA),
                resultSet.getString(COLUMN_TAG),
                resultSet.getString(COLUMN_OUTPUT_FORMAT),
                resultSet.getInt(COLUMN_SCREENSHOT),
                resultSet.getInt(COLUMN_VERBOSE),
                resultSet.getString(COLUMN_TIMEOUT),
                resultSet.getBoolean(COLUMN_SYNCHRONEOUS),
                resultSet.getInt(COLUMN_PAGE_SOURCE),
                resultSet.getInt(COLUMN_SELENIUM_LOG),
                new Date(resultSet.getTimestamp(COLUMN_REQUEST_DATE).getTime()),
                TestCaseExecutionInQueue.State.valueOf(resultSet.getString(COLUMN_STATE)),
                resultSet.getString(COLUMN_COMMENT),
                resultSet.getInt(COLUMN_RETRIES),
                resultSet.getString(COLUMN_MANUAL_EXECUTION).equals("Y"));
    }

    @Override
    public Answer create(TestCaseExecutionInQueue test) {
        Answer ans = new Answer();
        MessageEvent msg = null;

        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(Query.CREATE)) {
            // Prepare and execute query
            preStat.setString(1, test.getTest());
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "CREATE");
        } catch (Exception e) {
            LOG.warn("Unable to create TestCase Execution In Queue: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public Answer update(TestCaseExecutionInQueue test) {
        Answer ans = new Answer();
        MessageEvent msg = null;

        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(Query.UPDATE)) {
            // Prepare and execute query
            preStat.setString(1, test.getTest());
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "UPDATE");
        } catch (Exception e) {
            LOG.warn("Unable to update TestCase Execution in Queue " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public Answer delete(TestCaseExecutionInQueue test) {
        Answer ans = new Answer();
        MessageEvent msg = null;

        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(Query.DELETE)) {
            // Prepare and execute query
            preStat.setLong(1, test.getId());
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "DELETE");
        } catch (Exception e) {
            LOG.warn("Unable to delete testCase Execution in queue: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    private TestCaseExecutionInQueue findByKey(long id, PreparedStatement findByKeyStatement) throws SQLException, FactoryCreationException {
        findByKeyStatement.setLong(1, id);

        ResultSet result = findByKeyStatement.executeQuery();
        if (result == null || !result.next()) {
            throw new SQLException("Execution queue " + id + " does not exist");
        }
        return loadFromResultSet(result);
    }

    private void updateState(long id, TestCaseExecutionInQueue.State state, PreparedStatement updateStateStatement) throws SQLException {
        updateStateStatement.setString(1, state.name());
        updateStateStatement.setLong(2, id);
        updateStateStatement.executeUpdate();
    }

    private void updateStateAndComment(long id, TestCaseExecutionInQueue.State state, String comment, PreparedStatement updateStateAndCommentStatement) throws SQLException {
        updateStateAndCommentStatement.setString(1, state.name());
        updateStateAndCommentStatement.setString(2, comment);
        updateStateAndCommentStatement.setLong(3, id);
        updateStateAndCommentStatement.executeUpdate();
    }

    private void handleRollback(Connection connection) {
        LOG.warn("Exception occurs, try to rollback...");
        try {
            connection.rollback();
            LOG.warn("Rollback done.");
        } catch (SQLException again) {
            LOG.warn("Unable to rollback", again);
        }
    }

    /**
     * Uses data of ResultSet to create object {@link TestCaseExecutionInQueue}
     *
     * @param resultSet ResultSet relative to select from table
     *                  TestCaseExecutionInQueue
     * @return object {@link TestCaseExecutionInQueue} with objects
     * {@link ResultSet} and {@link Application}
     * @throws SQLException when trying to get value from
     *                      {@link java.sql.ResultSet#getString(String)}
     * @see TestCaseExecutionInQueue
     */
    private TestCaseExecutionInQueue loadWithDependenciesFromResultSet(ResultSet resultSet) throws SQLException, FactoryCreationException {
        TestCaseExecutionInQueue testCaseExecutionInQueue = this.loadFromResultSet(resultSet);
        testCaseExecutionInQueue.setTestCaseObj(testCaseDAO.loadFromResultSet(resultSet));
        testCaseExecutionInQueue.setApplicationObj(applicationDAO.loadFromResultSet(resultSet));
        return testCaseExecutionInQueue;
    }

}
