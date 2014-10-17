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
package org.cerberus.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.cerberus.dao.ITestCaseExecutionInQueueDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.TestCaseExecutionInQueue;
import org.cerberus.exception.CerberusException;
import org.cerberus.exception.FactoryCreationException;
import org.cerberus.factory.IFactoryTestCaseExecutionInQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TestCaseExecutionInQueueDAO implements ITestCaseExecutionInQueueDAO {

	/** The associated {@link Logger} to this class */
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

	private static final String VALUE_PROCEEDED_FALSE = "0";
	private static final String VALUE_PROCEEDED_TRUE = "1";

	@Autowired
	private DatabaseSpring databaseSpring;

	@Autowired
	private IFactoryTestCaseExecutionInQueue factoryTestCaseExecutionInQueue;

	private String queryInsert;
	private String querySelectNext;
	private String queryProceed;
	private String queryGetProceeded;
	private String queryGetProceededByTag;
	private String queryRemove;

	public String getQueryInsert() {
		if (queryInsert == null) {
			queryInsert = "INSERT INTO `" + TABLE + "` (`" + COLUMN_TEST + "`, `" + COLUMN_TEST_CASE + "`, `" + COLUMN_COUNTRY + "`, `" + COLUMN_ENVIRONMENT + "`, `"
					+ COLUMN_ROBOT + "`, `" + COLUMN_ROBOT_IP + "`, `" + COLUMN_ROBOT_PORT + "`, `" + COLUMN_BROWSER + "`, `" + COLUMN_BROWSER_VERSION + "`, `" + COLUMN_PLATFORM
					+ "`, `" + COLUMN_MANUAL_URL + "`, `" + COLUMN_MANUAL_HOST + "`, `" + COLUMN_MANUAL_CONTEXT_ROOT + "`, `" + COLUMN_MANUAL_LOGIN_RELATIVE_URL + "`, `"
					+ COLUMN_MANUAL_ENV_DATA + "`, `" + COLUMN_TAG + "`, `" + COLUMN_OUTPUT_FORMAT + "`, `" + COLUMN_SCREENSHOT + "`, `" + COLUMN_VERBOSE + "`, `" + COLUMN_TIMEOUT
					+ "`, `" + COLUMN_SYNCHRONEOUS + "`, `" + COLUMN_PAGE_SOURCE + "`, `" + COLUMN_SELENIUM_LOG + "`, `" + COLUMN_REQUEST_DATE + "`) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
		}
		return queryInsert;
	}

	private String getQuerySelectNext() {
		if (querySelectNext == null) {
			querySelectNext = "SELECT * FROM `" + TABLE + "` WHERE `" + COLUMN_PROCEEDED + "` = '" + VALUE_PROCEEDED_FALSE + "' ORDER BY `" + COLUMN_ID + "` ASC LIMIT 1";
		}
		return querySelectNext;
	}

	private String getQueryProceed() {
		if (queryProceed == null) {
			queryProceed = "UPDATE `" + TABLE + "` SET `" + COLUMN_PROCEEDED + "` = '" + VALUE_PROCEEDED_TRUE + "' WHERE `" + COLUMN_ID + "` = ?";
		}
		return queryProceed;
	}

	private String getQueryGetProceeded() {
		if (queryGetProceeded == null) {
			queryGetProceeded = "SELECT * FROM `" + TABLE + "` WHERE `" + COLUMN_PROCEEDED + "` = '" + VALUE_PROCEEDED_TRUE + "' ORDER BY `" + COLUMN_ID + "` ASC";
		}
		return queryGetProceeded;
	}

	private String getQueryGetProceededByTag() {
		if (queryGetProceededByTag == null) {
			queryGetProceededByTag = "SELECT * FROM `" + TABLE + "` WHERE `" + COLUMN_PROCEEDED + "` = '" + VALUE_PROCEEDED_TRUE + "' AND `" + COLUMN_TAG + "` = ? ORDER BY `"
					+ COLUMN_ID + "` ASC";
		}
		return queryGetProceededByTag;
	}

	private String getQueryRemove() {
		if (queryRemove == null) {
			queryRemove = "DELETE FROM `" + TABLE + "` WHERE `" + COLUMN_ID + "` = ?";
		}
		return queryRemove;
	}

	private TestCaseExecutionInQueue fromResultSet(ResultSet resultSet) throws FactoryCreationException, SQLException {
		return factoryTestCaseExecutionInQueue.create(resultSet.getLong(COLUMN_ID), resultSet.getString(COLUMN_TEST), resultSet.getString(COLUMN_TEST_CASE),
				resultSet.getString(COLUMN_COUNTRY), resultSet.getString(COLUMN_ENVIRONMENT), resultSet.getString(COLUMN_ROBOT), resultSet.getString(COLUMN_ROBOT_IP),
				resultSet.getString(COLUMN_ROBOT_PORT), resultSet.getString(COLUMN_BROWSER), resultSet.getString(COLUMN_BROWSER_VERSION), resultSet.getString(COLUMN_PLATFORM),
				resultSet.getBoolean(COLUMN_MANUAL_URL), resultSet.getString(COLUMN_MANUAL_HOST), resultSet.getString(COLUMN_MANUAL_CONTEXT_ROOT),
				resultSet.getString(COLUMN_MANUAL_LOGIN_RELATIVE_URL), resultSet.getString(COLUMN_MANUAL_ENV_DATA), resultSet.getString(COLUMN_TAG),
				resultSet.getString(COLUMN_OUTPUT_FORMAT), resultSet.getInt(COLUMN_SCREENSHOT), resultSet.getInt(COLUMN_VERBOSE), resultSet.getLong(COLUMN_TIMEOUT),
				resultSet.getBoolean(COLUMN_SYNCHRONEOUS), resultSet.getInt(COLUMN_PAGE_SOURCE), resultSet.getInt(COLUMN_SELENIUM_LOG),
				new Date(resultSet.getTimestamp(COLUMN_REQUEST_DATE).getTime()));
	}

	@Override
	public void insert(TestCaseExecutionInQueue inQueue) throws CerberusException {
		Connection connection = this.databaseSpring.connect();
		if (connection == null) {
			throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
		}

		PreparedStatement statementInsert = null;

		try {
			statementInsert = connection.prepareStatement(getQueryInsert());
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
			statementSelectNext = connection.prepareStatement(getQuerySelectNext());
			ResultSet resultSelect = statementSelectNext.executeQuery();

			// If there is no record then return null
			if (!resultSelect.next()) {
				return null;
			}

			// Create a TestCaseExecutionInQueue based on the fetched record
			result = fromResultSet(resultSelect);

			// Make the actual record as proceeded
			statementProceed = connection.prepareStatement(getQueryProceed());
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
				statement = connection.prepareStatement(getQueryGetProceeded());
			} else {
				statement = connection.prepareStatement(getQueryGetProceededByTag());
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
			statementRemove = connection.prepareStatement(getQueryRemove());
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
}
