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
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.crud.dao.ITestCaseExecutionInQueueDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.dto.TestCaseWithExecution;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.crud.entity.TestCaseExecutionInQueue;
import org.cerberus.exception.CerberusException;
import org.cerberus.exception.FactoryCreationException;
import org.cerberus.crud.factory.IFactoryTestCaseExecutionInQueue;
import org.cerberus.log.MyLogger;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.StringUtil;
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
    private static final String COLUMN_PROCEEDED = "Proceeded";
    private static final String COLUMN_COMMENT = "Comment";
    private static final String COLUMN_RETRIES = "Retries";
    private static final String COLUMN_MANUAL_EXECUTION = "ManualExecution";

    private static final String VALUE_PROCEEDED_FALSE = "0";
    private static final String VALUE_PROCEEDED_TRUE = "1";
    private static final String VALUE_MANUAL_EXECUTION_FALSE = "N";

    private static final String QUERY_INSERT = "INSERT INTO `" + TABLE + "` (`" + COLUMN_TEST + "`, `" + COLUMN_TEST_CASE + "`, `" + COLUMN_COUNTRY + "`, `" + COLUMN_ENVIRONMENT + "`, `" + COLUMN_ROBOT + "`, `" + COLUMN_ROBOT_IP + "`, `" + COLUMN_ROBOT_PORT + "`, `" + COLUMN_BROWSER + "`, `" + COLUMN_BROWSER_VERSION + "`, `" + COLUMN_PLATFORM + "`, `" + COLUMN_MANUAL_URL + "`, `" + COLUMN_MANUAL_HOST + "`, `" + COLUMN_MANUAL_CONTEXT_ROOT + "`, `" + COLUMN_MANUAL_LOGIN_RELATIVE_URL + "`, `" + COLUMN_MANUAL_ENV_DATA + "`, `" + COLUMN_TAG + "`, `" + COLUMN_OUTPUT_FORMAT + "`, `" + COLUMN_SCREENSHOT + "`, `" + COLUMN_VERBOSE + "`, `" + COLUMN_TIMEOUT + "`, `" + COLUMN_SYNCHRONEOUS + "`, `" + COLUMN_PAGE_SOURCE + "`, `" + COLUMN_SELENIUM_LOG + "`, `" + COLUMN_REQUEST_DATE + "`, `" + COLUMN_RETRIES + "`, `" + COLUMN_MANUAL_EXECUTION + "`) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
    private static final String QUERY_SELECT_NEXT = "SELECT * FROM `" + TABLE + "` WHERE `" + COLUMN_PROCEEDED + "` = '" + VALUE_PROCEEDED_FALSE + "' ORDER BY `" + COLUMN_ID + "` ASC LIMIT 1";
    private static final String QUERY_PROCEED = "UPDATE `" + TABLE + "` SET `" + COLUMN_PROCEEDED + "` = '" + VALUE_PROCEEDED_TRUE + "' WHERE `" + COLUMN_ID + "` = ?";
    private static final String QUERY_GET_PROCEEDED = "SELECT * FROM `" + TABLE + "` WHERE `" + COLUMN_PROCEEDED + "` = '" + VALUE_PROCEEDED_TRUE + "' ORDER BY `" + COLUMN_ID + "` ASC";
    private static final String QUERY_GET_PROCEEDED_BY_TAG = "SELECT * FROM `" + TABLE + "` WHERE `" + COLUMN_PROCEEDED + "` = '" + VALUE_PROCEEDED_TRUE + "' AND `" + COLUMN_TAG + "` = ? ORDER BY `" + COLUMN_ID + "` ASC";
    private static final String QUERY_GET_NOT_PROCEEDED = "SELECT * FROM `" + TABLE + "` WHERE `" + COLUMN_PROCEEDED + "` = '" + VALUE_PROCEEDED_FALSE + "' AND `" + COLUMN_MANUAL_EXECUTION + "` = '" + VALUE_MANUAL_EXECUTION_FALSE + "' ORDER BY `" + COLUMN_ID + "` ASC";
    private static final String QUERY_GET_NOT_PROCEEDED_BY_TAG = "SELECT * FROM `" + TABLE + "` WHERE `" + COLUMN_PROCEEDED + "` = '" + VALUE_PROCEEDED_FALSE + "' AND `" + COLUMN_TAG + "` = ? ORDER BY `" + COLUMN_ID + "` ASC";
    private static final String QUERY_REMOVE = "DELETE FROM `" + TABLE + "` WHERE `" + COLUMN_ID + "` = ?";
    private static final String QUERY_FIND_BY_KEY = "SELECT * FROM `" + TABLE + "` WHERE `" + COLUMN_ID + "` = ?";
    private static final String QUERY_GET_ALL = "SELECT * FROM `" + TABLE + "`;";
    private static final String QUERY_NOT_PROCEEDED = "UPDATE `" + TABLE + "` SET `" + COLUMN_PROCEEDED + "` = '" + VALUE_PROCEEDED_FALSE + "' WHERE `" + COLUMN_ID + "` = ?";
    private static final String QUERY_UPDATE_COMMENT = "UPDATE `" + TABLE + "` SET `" + COLUMN_COMMENT + "` = ? WHERE `" + COLUMN_ID + "` = ?";

    @Autowired
    private DatabaseSpring databaseSpring;

    @Autowired
    private IFactoryTestCaseExecutionInQueue factoryTestCaseExecutionInQueue;

    @Autowired
    private CampaignDAO campaignDAO;

    private TestCaseExecutionInQueue fromResultSet(ResultSet resultSet) throws FactoryCreationException, SQLException {
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
                resultSet.getLong(COLUMN_TIMEOUT),
                resultSet.getBoolean(COLUMN_SYNCHRONEOUS),
                resultSet.getInt(COLUMN_PAGE_SOURCE),
                resultSet.getInt(COLUMN_SELENIUM_LOG),
                new Date(resultSet.getTimestamp(COLUMN_REQUEST_DATE).getTime()),
                resultSet.getString(COLUMN_PROCEEDED),
                resultSet.getString(COLUMN_COMMENT),
                resultSet.getInt(COLUMN_RETRIES),
                resultSet.getString(COLUMN_MANUAL_EXECUTION).equals("Y"));
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
            statementInsert.setLong(20, inQueue.getTimeout());
            statementInsert.setBoolean(21, inQueue.isSynchroneous());
            statementInsert.setInt(22, inQueue.getPageSource());
            statementInsert.setInt(23, inQueue.getSeleniumLog());
            statementInsert.setTimestamp(24, new Timestamp(inQueue.getRequestDate().getTime()));
            statementInsert.setInt(25, inQueue.getRetries());
            statementInsert.setString(26, inQueue.isManualExecution() ? "Y" : "N");

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
    public TestCaseExecutionInQueue getNextAndProceed() throws CerberusException {
        final Connection connection = this.databaseSpring.connect();
        if (connection == null) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }

        PreparedStatement statementSelectNext = null;
        PreparedStatement statementProceed = null;

        TestCaseExecutionInQueue result = null;

        try {
            // Begin transaction
            connection.setAutoCommit(false);

            // Select the next record to be proceeded
            statementSelectNext = connection.prepareStatement(QUERY_SELECT_NEXT);
            ResultSet resultSelect = statementSelectNext.executeQuery();

            // If there is no record then return null
            if (!resultSelect.next()) {
                return null;
            }

            // Create a TestCaseExecutionInQueue based on the fetched record
            result = fromResultSet(resultSelect);

            // Make the actual record as proceeded
            statementProceed = connection.prepareStatement(QUERY_PROCEED);
            statementProceed.setLong(1, resultSelect.getLong(COLUMN_ID));
            statementProceed.executeUpdate();

            // Commit transaction
            connection.commit();
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
        } catch (FactoryCreationException fce) {
            LOG.warn("Unable to execute query : " + fce.toString() + ". Trying to rollback");
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
            if (statementSelectNext != null) {
                try {
                    statementSelectNext.close();
                } catch (SQLException e) {
                    LOG.warn("Unable to close selectNext statement due to " + e.getMessage());
                }
            }
            if (statementProceed != null) {
                try {
                    statementProceed.close();
                } catch (SQLException e) {
                    LOG.warn("Unable to close proceed statement due to " + e.getMessage());
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
    public List<TestCaseExecutionInQueue> getProceededByTag(String tag) throws CerberusException {
        final Connection connection = this.databaseSpring.connect();
        if (connection == null) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }

        PreparedStatement statement = null;

        List<TestCaseExecutionInQueue> result = new ArrayList<TestCaseExecutionInQueue>();

        try {
            if (tag == null) {
                statement = connection.prepareStatement(QUERY_GET_PROCEEDED);
            } else {
                statement = connection.prepareStatement(QUERY_GET_PROCEEDED_BY_TAG);
                statement.setString(1, tag);
            }
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                try {
                    result.add(fromResultSet(resultSet));
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

    public List<TestCaseWithExecution> findTestCaseWithExecutionInQueuebyTag(String tag) throws CerberusException {
        boolean throwEx = false;
        final StringBuffer query = new StringBuffer("select * from ( select tc.*, RequestDate as Start, '' as End, tce.ID as statusExecutionID, 'NE' as ControlStatus, 'Not Executed' as ControlMessage, tce.Environment, tce.Country, tce.Browser ")
                .append("from testcase tc ")
                .append("left join testcaseexecutionqueue tce ")
                .append("on tce.Test = tc.Test ")
                .append("and tce.TestCase = tc.TestCase ")
                .append("where tce.tag = ? ");

        query.append(" order by test, testcase, ID desc) as tce, application app ")
                .append("where tce.application = app.application ")
                .append("group by tce.test, tce.testcase, tce.Environment, tce.Browser, tce.Country ").toString();

        List<TestCaseWithExecution> testCaseWithExecutionList = new ArrayList<TestCaseWithExecution>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());

            preStat.setString(1, tag);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testCaseWithExecutionList.add(campaignDAO.loadTestCaseWithExecutionFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    testCaseWithExecutionList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                testCaseWithExecutionList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            testCaseWithExecutionList = null;
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
        return testCaseWithExecutionList;
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
                    result = (fromResultSet(resultSet));
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
    public List<TestCaseExecutionInQueue> getNotProceededAndProceed() throws CerberusException {
        final Connection connection = this.databaseSpring.connect();
        if (connection == null) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }

        PreparedStatement statementSelectNext = null;
        PreparedStatement statementProceed = null;
        List<TestCaseExecutionInQueue> result = new ArrayList<TestCaseExecutionInQueue>();

        try {
            // Begin transaction
            connection.setAutoCommit(false);

            // Select the next record to be proceeded
            statementSelectNext = connection.prepareStatement(QUERY_GET_NOT_PROCEEDED);
            ResultSet resultSelect = statementSelectNext.executeQuery();

            // If there is no record then return null
            if (!resultSelect.next()) {
                return null;
            }

            // Create a TestCaseExecutionInQueue based on the fetched record
            while (resultSelect.next()) {
                result.add(fromResultSet(resultSelect));
                // Make the actual record as proceeded
                statementProceed = connection.prepareStatement(QUERY_PROCEED);
                statementProceed.setLong(1, resultSelect.getLong(COLUMN_ID));
                statementProceed.executeUpdate();
            }

            // Commit transaction
            connection.commit();
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
        } catch (FactoryCreationException fce) {
            LOG.warn("Unable to execute query : " + fce.toString() + ". Trying to rollback");
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
            if (statementSelectNext != null) {
                try {
                    statementSelectNext.close();
                } catch (SQLException e) {
                    LOG.warn("Unable to close selectNext statement due to " + e.getMessage());
                }
            }
            if (statementProceed != null) {
                try {
                    statementProceed.close();
                } catch (SQLException e) {
                    LOG.warn("Unable to close proceed statement due to " + e.getMessage());
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
                    result.add(fromResultSet(resultSet));
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
    public void setProcessedTo(Long l, String changeTo) throws CerberusException {
        final Connection connection = this.databaseSpring.connect();
        if (connection == null) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }

        PreparedStatement statementProceed = null;

        TestCaseExecutionInQueue result = null;

        try {
            // Make the actual record as proceeded
            if (!changeTo.equals("0")) {
                statementProceed = connection.prepareStatement(QUERY_PROCEED);
            } else {
                statementProceed = connection.prepareStatement(QUERY_NOT_PROCEEDED);
            }
            statementProceed.setLong(1, l);
            statementProceed.executeUpdate();

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
            if (statementProceed != null) {
                try {
                    statementProceed.close();
                } catch (SQLException e) {
                    LOG.warn("Unable to close proceed statement due to " + e.getMessage());
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
    public void updateComment(Long queueId, String comment) throws CerberusException {
        final Connection connection = this.databaseSpring.connect();
        if (connection == null) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }

        PreparedStatement statementComment = null;

        TestCaseExecutionInQueue result = null;

        try {
            // Make the actual record as proceeded
            statementComment = connection.prepareStatement(QUERY_UPDATE_COMMENT);
            statementComment.setString(1, comment);
            statementComment.setLong(2, queueId);
            statementComment.executeUpdate();

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
            if (statementComment != null) {
                try {
                    statementComment.close();
                } catch (SQLException e) {
                    LOG.warn("Unable to close proceed statement due to " + e.getMessage());
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
    public AnswerList readByTagByCriteria(String tag, int start, int amount, String column, String dir, String searchTerm, String individualSearch) throws CerberusException {
        boolean throwEx = false;
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        AnswerList answer = new AnswerList();
        StringBuilder gSearch = new StringBuilder();
        final StringBuffer query = new StringBuffer("SELECT * FROM ( select tc.*, RequestDate as Start, '' as End, tce.ID as statusExecutionID, 'NE' as ControlStatus, 'Not Executed' as ControlMessage, tce.Environment, tce.Country, tce.Browser ")
                .append("from testcase tc ")
                .append("left join testcaseexecutionqueue tce ")
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
        gSearch.append(" or tce.`description` like '%");
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

        List<TestCaseWithExecution> testCaseWithExecutionList = new ArrayList<TestCaseWithExecution>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());

            preStat.setString(1, tag);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testCaseWithExecutionList.add(campaignDAO.loadTestCaseWithExecutionFromResultSet(resultSet));
                    }

//                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
//                    int nrTotalRows = 0;
//
//                    if (resultSet != null && resultSet.next()) {
//                        nrTotalRows = resultSet.getInt(1);
//                    }

                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecutionInQueue").replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList(testCaseWithExecutionList, testCaseWithExecutionList.size());
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                    testCaseWithExecutionList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                testCaseWithExecutionList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            testCaseWithExecutionList = null;
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
    public AnswerList readDistinctEnvCoutnryBrowserByTag(String tag) {
        AnswerList answer = new AnswerList();
        StringBuilder query = new StringBuilder();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);

        query.append("SELECT Environment, Country, Browser FROM testcaseexecutionqueue WHERE tag = ? GROUP BY Environment, Country, Browser");

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
                    MyLogger.log(TestCaseExecutionInQueueDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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
                distinct.append("tce.Environment");
                prev++;
            }
            if (country) {
                if (prev != 0) {
                    prev = 0;
                    distinct.append(",");
                }
                distinct.append("tce.Country");
                prev++;
            }
            if (browser) {
                if (prev != 0) {
                    prev = 0;
                    distinct.append(",");
                }
                distinct.append("tce.Browser");
                prev++;
            }
            if (app) {
                if (prev != 0) {
                    prev = 0;
                    distinct.append(",");
                }
                distinct.append("tc.Application");
            }

            query.append("SELECT tc.test, tc.testcase, ");
            query.append(distinct.toString());
            query.append(" FROM testcase tc LEFT JOIN testcaseexecutionqueue tce ON tce.Test = tc.Test AND tce.TestCase = tc.TestCase WHERE tag = ? GROUP BY ");
            query.append(distinct.toString());
        } else {
             //If there is no distinct, select nothing
            query.append("SELECT * FROM testcaseexecutionqueue WHERE 1=0 AND tag = ?");
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

    public TestCaseWithExecution loadEnvCountryBrowserFromResultSet(ResultSet resultSet) throws SQLException {
        TestCaseWithExecution testCaseWithExecution = new TestCaseWithExecution();

        testCaseWithExecution.setEnvironment(resultSet.getString("Environment"));
        testCaseWithExecution.setCountry(resultSet.getString("Country"));
        testCaseWithExecution.setBrowser(resultSet.getString("Browser"));
        testCaseWithExecution.setControlStatus("NE");

        return testCaseWithExecution;
    }

    @Override
    public AnswerList readBySystemByVarious(String system, List<String> testList, List<String> applicationList, List<String> projectList, List<String> tcstatusList
            , List<String> groupList, List<String> tcactiveList, List<String> priorityList, List<String> targetsprintList, List<String> targetrevisionList
            , List<String> creatorList, List<String> implementerList, List<String> buildList, List<String> revisionList, List<String> environmentList, List<String> countryList
            , List<String> browserList, List<String> tcestatusList, String ip, String port, String tag, String browserversion, String comment, String bugid, String ticket) {
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
        query.append(" tc.TargetRev, tc.UsrCreated, tc.Implementer, tc.LastModifier, tc.activeQA, ");
        query.append(" tc.activeUAT, tc.activePROD, tc.`function`, tc.TcActive, ");
        query.append(" a.sort, a.`type`, a.`system`, a.SubSystem, a.svnurl, a.deploytype, ");
        query.append(" a.mavengroupid, a.BugTrackerUrl, a.BugTrackerNewUrl ");
        query.append(" from testcaseexecutionqueue tce ");
        query.append(" inner join testcase tc on tce.test = tc.test and tce.testcase = tc.testcase ");
        query.append(" inner join application a on tce.application = a.application ");
   
        
        String testClause = SqlUtil.generateInClause("tce.test", testList);
        if(!StringUtil.isNullOrEmpty(testClause)){
            whereClauses.add(testClause);
        }
        
        String applicationClause = SqlUtil.generateInClause("tce.application", applicationList);
        if(!StringUtil.isNullOrEmpty(applicationClause)){
            whereClauses.add(applicationClause);
        }
        
        String projectClause = SqlUtil.generateInClause("tc.project", projectList);
        if(!StringUtil.isNullOrEmpty(projectClause)){
            whereClauses.add(projectClause);
        }
        //test case status: working, fully_implemented, ...
        String tcsClause = SqlUtil.generateInClause("tce.status", tcstatusList);
        if(!StringUtil.isNullOrEmpty(tcsClause)){
            whereClauses.add(tcsClause);
        }
        
        //group 
        String groupClause = SqlUtil.generateInClause("tc.group", groupList);
        if(!StringUtil.isNullOrEmpty(groupClause)){
            whereClauses.add(groupClause);
        }
        //test case active
        String tcactiveClause = SqlUtil.generateInClause("tc.tcactive", tcactiveList);
        if(!StringUtil.isNullOrEmpty(tcactiveClause)){
            whereClauses.add(tcactiveClause);
        }
        
        //test case active
        String priorityClause = SqlUtil.generateInClause("tc.Priority", priorityList);
        if(!StringUtil.isNullOrEmpty(priorityClause)){
            whereClauses.add(priorityClause);
        }
        
        //target sprint
        String targetsprintClause = SqlUtil.generateInClause("tc.TargetBuild", targetsprintList);
        if(!StringUtil.isNullOrEmpty(targetsprintClause)){
            whereClauses.add(targetsprintClause);
        }
        
        //target revision
        String targetrevisionClause = SqlUtil.generateInClause("tc.TargetRev", targetrevisionList);
        if(!StringUtil.isNullOrEmpty(targetrevisionClause)){
            whereClauses.add(targetrevisionClause);
        }
        
        //creator
        String creatorClause = SqlUtil.generateInClause("tc.UsrCreated", creatorList);
        if(!StringUtil.isNullOrEmpty(creatorClause)){
            whereClauses.add(creatorClause);
        }
        
        //implementer
        String implementerClause = SqlUtil.generateInClause("tc.Implementer", implementerList);
        if(!StringUtil.isNullOrEmpty(implementerClause)){
            whereClauses.add(implementerClause);
        }
        
        //build
        String buildClause = SqlUtil.generateInClause("tce.Build", buildList);
        if(!StringUtil.isNullOrEmpty(buildClause)){
            whereClauses.add(buildClause);
        }
        //revision
        String revisionClause = SqlUtil.generateInClause("tce.Revision", revisionList);
        if(!StringUtil.isNullOrEmpty(revisionClause)){
            whereClauses.add(revisionClause);
        }
        //environment
        String environmentClause = SqlUtil.generateInClause("tce.Environment", environmentList);
        if(!StringUtil.isNullOrEmpty(environmentClause)){
            whereClauses.add(environmentClause);
        }
        //country
        String countryClause = SqlUtil.generateInClause("tce.Country", countryList);
        if(!StringUtil.isNullOrEmpty(countryClause)){
            whereClauses.add(countryClause);
        }
        //browser
        String browserClause = SqlUtil.generateInClause("tce.Browser", browserList);
        if(!StringUtil.isNullOrEmpty(browserClause)){
            whereClauses.add(browserClause);
        }
        //test case execution
        String tcestatusClause = SqlUtil.generateInClause("tce.ControlStatus", tcestatusList);
        if(!StringUtil.isNullOrEmpty(tcestatusClause)){
            whereClauses.add(tcestatusClause);
        }
        
        if(!StringUtil.isNullOrEmpty(system)){
            whereClauses.add(" a.system like ? ");
        }
        if(!StringUtil.isNullOrEmpty(ip)){
            whereClauses.add(" tce.IP like ? ");
        }
        if(!StringUtil.isNullOrEmpty(port)){
            whereClauses.add(" tce.port like ? ");
        }
        if(!StringUtil.isNullOrEmpty(tag)){
            whereClauses.add(" tce.tag like ? ");
        }
        if(!StringUtil.isNullOrEmpty(browserversion)){
            whereClauses.add(" tce.browserfullversion like ? ");
        }
        if(!StringUtil.isNullOrEmpty(comment)){
            whereClauses.add(" tce.comment like ? ");
        }
        if(!StringUtil.isNullOrEmpty(bugid)){
            whereClauses.add(" tc.BugID like ? ");
        }
        if(!StringUtil.isNullOrEmpty(ticket)){
            whereClauses.add(" tc.Ticket like ? ");
        }
        
        if(whereClauses.size() > 0 ){
            query.append("where ");
            String joined = StringUtils.join(whereClauses, " and "); 
            query.append(joined);
        }
        
        query.append(" order by tce.ID desc ");
        query.append(" ) as t group by t.test, t.testcase, t.environment, t.browser, t.country");
        Connection connection = this.databaseSpring.connect();

        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            if(testList != null){
                for(String param : testList){
                    preStat.setString(++paramNumber, param);
                }
            }
            if(applicationList != null){
                for(String param : applicationList){
                    preStat.setString(++paramNumber, param);
                }
            }
            if(projectList != null){
                for(String param : projectList){
                    preStat.setString(++paramNumber, param);
                }
            }
            if(tcstatusList != null){
                for(String param : tcstatusList){
                    preStat.setString(++paramNumber, param);
                }
            } 
            if(groupList != null){
                for(String param : groupList){
                    preStat.setString(++paramNumber, param);
                }
            }
            
            if(tcactiveList != null){
                for(String param : tcactiveList){
                    preStat.setString(++paramNumber, param);
                }
            }
            if(priorityList != null){
                for(String param : priorityList){
                    preStat.setString(++paramNumber, param);
                }
            }
            if(targetsprintList != null){
                for(String param : targetsprintList){
                    preStat.setString(++paramNumber, param);
                }
            }
            if(targetrevisionList != null){
                for(String param : targetrevisionList){
                    preStat.setString(++paramNumber, param);
                }
            }
            if(creatorList != null){
                for(String param : creatorList){
                    preStat.setString(++paramNumber, param);
                }
            }
            if(implementerList != null){
                for(String param : implementerList){
                    preStat.setString(++paramNumber, param);
                }
            }
            if(buildList != null){
                for(String param : buildList){
                    preStat.setString(++paramNumber, param);
                }
            }
            if(revisionList != null){
                for(String param : revisionList){
                    preStat.setString(++paramNumber, param);
                }
            }
            //environment
            if(environmentList != null){
                for(String param : environmentList){
                    preStat.setString(++paramNumber, param);
                }
            }
            //country
            if(countryList != null){
                for(String param : countryList){
                    preStat.setString(++paramNumber, param);
                }
            }
            //browser            
            if(browserList != null){
                for(String param : browserList){
                    preStat.setString(++paramNumber, param);
                }
            }
            //controlstatus
            if(tcestatusList != null){
                for(String param : tcestatusList){
                    preStat.setString(++paramNumber, param);
                }
            }
            
            if(!StringUtil.isNullOrEmpty(system)){
                preStat.setString(++paramNumber, system);
            }
            
            if(!StringUtil.isNullOrEmpty(ip)){
                preStat.setString(++paramNumber, "%" + ip + "%");
            }
            if(!StringUtil.isNullOrEmpty(port)){
                preStat.setString(++paramNumber, "%" +port + "%");
            }
            if(!StringUtil.isNullOrEmpty(tag)){
                preStat.setString(++paramNumber, "%" + tag + "%");
            }
            if(!StringUtil.isNullOrEmpty(browserversion)){
                preStat.setString(++paramNumber, "%" + browserversion + "%");
            }
            if(!StringUtil.isNullOrEmpty(comment)){
                preStat.setString(++paramNumber, "%" + comment + "%");
            }
            if(!StringUtil.isNullOrEmpty(bugid)){
                preStat.setString(++paramNumber, "%" + bugid + "%");
            }
            if(!StringUtil.isNullOrEmpty(ticket)){
                preStat.setString(++paramNumber, "%" + ticket + "%");
            }
            
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        tceList.add(campaignDAO.loadTestCaseWithExecutionFromResultSet(resultSet));
                    }
                    if(tceList.isEmpty()){
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    }else{
                        msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecutionInQueue").replace("%OPERATION%", "SELECT"));
                    }
                                        
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseExecutionInQueueDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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

}
