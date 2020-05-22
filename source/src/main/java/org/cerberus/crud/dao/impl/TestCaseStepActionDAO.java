/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.crud.dao.ITestCaseStepActionDAO;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryTestCaseStepAction;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 31/12/2012
 * @since 2.0.0
 */
@Repository
public class TestCaseStepActionDAO implements ITestCaseStepActionDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseStepAction factoryTestCaseStepAction;

    private static final Logger LOG = LogManager.getLogger(TestCaseStepActionDAO.class);

    private final String OBJECT_NAME = "TestCaseStepAction";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public TestCaseStepAction readByKey(String test, String testCase, int step, int sequence) {
        TestCaseStepAction testCaseStepAction = null;
        final String query = "SELECT * FROM testcasestepaction tca WHERE tca.test = ? AND tca.testcase = ? AND tca.step = ? AND tca.sequence = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);
                preStat.setInt(3, step);
                preStat.setInt(4, sequence);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        testCaseStepAction = this.loadFromResultSet(resultSet);
                    }
                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
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
        return testCaseStepAction;
    }

    @Override
    public List<TestCaseStepAction> findTestCaseStepActionbyTestTestCase(String test, String testCase) throws CerberusException {
        List<TestCaseStepAction> list = null;
        final StringBuilder query = new StringBuilder();
        query.append("SELECT tca.* ");
        query.append("FROM testcasestepaction AS tca ");
        query.append("RIGHT JOIN testcasestep AS tcs ON tca.Test = tcs.Test AND tca.TestCase = tcs.TestCase AND tca.Step = tcs.Step ");
        query.append("WHERE tca.Test = ? AND tca.TestCase = ? ");
        query.append("GROUP BY tca.Test, tca.TestCase, tca.Step, tca.Sequence ");
        query.append("ORDER BY tcs.Sort, tca.Sort ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<TestCaseStepAction>();
                    while (resultSet.next()) {
                        list.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
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
        return list;
    }

    @Override
    public List<TestCaseStepAction> findActionByTestTestCaseStep(String test, String testcase, int stepNumber) {
        List<TestCaseStepAction> list = null;
        final String query = "SELECT * FROM testcasestepaction tca WHERE tca.test = ? AND tca.testcase = ? AND tca.step = ? ORDER BY tca.sort";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.test : " + test);
            LOG.debug("SQL.param.testcase : " + testcase);
            LOG.debug("SQL.param.step : " + stepNumber);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                preStat.setInt(3, stepNumber);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<TestCaseStepAction>();
                    while (resultSet.next()) {
                        list.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
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
        return list;
    }

    @Override
    public AnswerList<TestCaseStepAction> readByTestTestCase(String test, String testcase) {
        AnswerList<TestCaseStepAction> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCaseStepAction> actionList = new ArrayList<TestCaseStepAction>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcasestepaction tca WHERE tca.test = ? AND tca.testcase = ?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        actionList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (actionList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList<>(actionList, actionList.size());
                    } else if (actionList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList<>(actionList, actionList.size());
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList<>(actionList, actionList.size());
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
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
        return response;
    }

    @Override
    public AnswerList<TestCaseStepAction> readByVarious1(String test, String testcase, int step) {
        AnswerList<TestCaseStepAction> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCaseStepAction> actionList = new ArrayList<TestCaseStepAction>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcasestepaction tca WHERE tca.test = ? AND tca.testcase = ? AND tca.step = ?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                preStat.setInt(3, step);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        actionList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (actionList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList<>(actionList, actionList.size());
                    } else if (actionList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList<>(actionList, actionList.size());
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList<>(actionList, actionList.size());
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
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
        return response;
    }

    @Override
    public void createTestCaseStepAction(TestCaseStepAction testCaseStepAction) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcasestepaction (`test`, `testCase`, `step`, `sequence`, `sort`, ")
                .append("`conditionOperator`, `conditionVal1`, `conditionVal2`, `conditionVal3`, `action`, ")
                .append("`value1`, `value2`, `value3`, `ForceExeStatus`, `description`, `screenshotfilename`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                preStat.setString(i++, testCaseStepAction.getTest());
                preStat.setString(i++, testCaseStepAction.getTestCase());
                preStat.setInt(i++, testCaseStepAction.getStep());
                preStat.setInt(i++, testCaseStepAction.getSequence());
                preStat.setInt(i++, testCaseStepAction.getSort());
                preStat.setString(i++, testCaseStepAction.getConditionOperator());
                preStat.setString(i++, testCaseStepAction.getConditionVal1());
                preStat.setString(i++, testCaseStepAction.getConditionVal2());
                preStat.setString(i++, testCaseStepAction.getConditionVal3());
                preStat.setString(i++, testCaseStepAction.getAction());
                preStat.setString(i++, testCaseStepAction.getValue1());
                preStat.setString(i++, testCaseStepAction.getValue2());
                preStat.setString(i++, testCaseStepAction.getValue3());
                preStat.setString(i++, testCaseStepAction.getForceExeStatus());
                preStat.setString(i++, testCaseStepAction.getDescription());
                preStat.setString(i++, testCaseStepAction.getScreenshotFilename());

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
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
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void update(TestCaseStepAction testCaseStepAction) throws CerberusException {
        boolean throwExcep = false;
        final String query = new StringBuilder("UPDATE `testcasestepaction` ")
                .append("SET ")
                .append("`Test` = ?, ")
                .append("`TestCase` = ?, ")
                .append("`Step` = ?, ")
                .append("`Sequence` = ?, ")
                .append("`Sort` = ?, ")
                .append("`conditionOperator` = ?, ")
                .append("`ConditionVal1` = ?, ")
                .append("`ConditionVal2` = ?, ")
                .append("`ConditionVal3` = ?, ")
                .append("`Action` = ?, ")
                .append("`Value1` = ?, ")
                .append("`Value2` = ?, ")
                .append("`Value3` = ?, ")
                .append("`ForceExeStatus` = ?, ")
                .append("`Description` = ?, ")
                .append("`ScreenshotFilename` = ? ")
                .append("WHERE `Test` = ? AND `TestCase` = ? AND `Step` = ? AND `Sequence` = ? ")
                .toString();

        LOG.debug("SQL " + query);
        LOG.debug("SQL.param.conditionOperator " + testCaseStepAction.getConditionOperator());
        LOG.debug("SQL.param.conditionVal1 " + testCaseStepAction.getConditionVal1());
        LOG.debug("SQL.param.conditionVal2 " + testCaseStepAction.getConditionVal2());
        LOG.debug("SQL.param.conditionVal3 " + testCaseStepAction.getConditionVal3());

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setString(i++, testCaseStepAction.getTest());
                preStat.setString(i++, testCaseStepAction.getTestCase());
                preStat.setInt(i++, testCaseStepAction.getStep());
                preStat.setInt(i++, testCaseStepAction.getSequence());
                preStat.setInt(i++, testCaseStepAction.getSort());
                preStat.setString(i++, testCaseStepAction.getConditionOperator());
                preStat.setString(i++, testCaseStepAction.getConditionVal1());
                preStat.setString(i++, testCaseStepAction.getConditionVal2());
                preStat.setString(i++, testCaseStepAction.getConditionVal3());
                preStat.setString(i++, testCaseStepAction.getAction());
                preStat.setString(i++, testCaseStepAction.getValue1());
                preStat.setString(i++, testCaseStepAction.getValue2());
                preStat.setString(i++, testCaseStepAction.getValue3());
                preStat.setString(i++, testCaseStepAction.getForceExeStatus());
                preStat.setString(i++, testCaseStepAction.getDescription());
                preStat.setString(i++, testCaseStepAction.getScreenshotFilename());

                preStat.setString(i++, testCaseStepAction.getTest());
                preStat.setString(i++, testCaseStepAction.getTestCase());
                preStat.setInt(i++, testCaseStepAction.getStep());
                preStat.setInt(i++, testCaseStepAction.getSequence());

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
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
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void delete(TestCaseStepAction tcsa) throws CerberusException {
        boolean throwExcep = false;
        final String query = "DELETE FROM testcasestepaction WHERE test = ? and testcase = ? and step = ? and `sequence` = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, tcsa.getTest());
                preStat.setString(2, tcsa.getTestCase());
                preStat.setInt(3, tcsa.getStep());
                preStat.setInt(4, tcsa.getSequence());

                throwExcep = preStat.executeUpdate() == 0;
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
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
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public boolean changeTestCaseStepActionSequence(String test, String testCase, int step, int oldSequence, int newSequence) {
        TestCaseStepAction testCaseStepAction = null;
        final String query = "update testcasestepaction set sequence = ? WHERE test = ? AND testcase = ? AND step = ? AND sequence = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setInt(1, newSequence);
                preStat.setString(2, test);
                preStat.setString(3, testCase);
                preStat.setInt(4, step);
                preStat.setInt(5, oldSequence);

                int lines = preStat.executeUpdate();
                return (lines > 0);
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
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
        return false;
    }

    @Override
    public Answer create(TestCaseStepAction testCaseStepAction) {
        Answer ans = new Answer();
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcasestepaction (`test`, `testCase`, `step`, `sequence`, `sort`, ")
                .append("`conditionOperator`, `conditionVal1`, `conditionVal2`, `conditionVal3`, `action`, `Value1`, `Value2`, `Value3`, `ForceExeStatus`, `description`, `screenshotfilename`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString())) {
            // Prepare and execute query
            int i = 1;
            preStat.setString(i++, testCaseStepAction.getTest());
            preStat.setString(i++, testCaseStepAction.getTestCase());
            preStat.setInt(i++, testCaseStepAction.getStep());
            preStat.setInt(i++, testCaseStepAction.getSequence());
            preStat.setInt(i++, testCaseStepAction.getSort());
            preStat.setString(i++, testCaseStepAction.getConditionOperator());
            preStat.setString(i++, testCaseStepAction.getConditionVal1());
            preStat.setString(i++, testCaseStepAction.getConditionVal2());
            preStat.setString(i++, testCaseStepAction.getConditionVal3());
            preStat.setString(i++, testCaseStepAction.getAction());
            preStat.setString(i++, testCaseStepAction.getValue1());
            preStat.setString(i++, testCaseStepAction.getValue2());
            preStat.setString(i++, testCaseStepAction.getValue3());
            preStat.setString(i++, testCaseStepAction.getForceExeStatus());
            preStat.setString(i++, testCaseStepAction.getDescription());
            preStat.setString(i++, testCaseStepAction.getScreenshotFilename());
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "CREATE");
        } catch (Exception e) {
            LOG.warn("Unable to create TestCaseStepAction: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    private TestCaseStepAction loadFromResultSet(ResultSet resultSet) throws SQLException {
        String test = resultSet.getString("tca.Test");
        String testCase = resultSet.getString("tca.TestCase");
        Integer step = resultSet.getInt("tca.Step");
        Integer sequence = resultSet.getInt("tca.Sequence");
        Integer sort = resultSet.getInt("tca.Sort");
        String conditionOperator = resultSet.getString("tca.conditionOperator");
        String conditionVal1 = resultSet.getString("tca.ConditionVal1");
        String conditionVal2 = resultSet.getString("tca.ConditionVal2");
        String conditionVal3 = resultSet.getString("tca.conditionVal3");
        String action = resultSet.getString("tca.Action");
        String value1 = resultSet.getString("tca.Value1");
        String value2 = resultSet.getString("tca.Value2");
        String value3 = resultSet.getString("tca.Value3");
        String description = resultSet.getString("tca.description");
        String screenshotFilename = resultSet.getString("tca.screenshotFilename");
        String forceExeStatus = resultSet.getString("tca.forceExeStatus");

        return factoryTestCaseStepAction.create(test, testCase, step, sequence, sort, conditionOperator, conditionVal1, conditionVal2, conditionVal3, action, value1, value2, value3, forceExeStatus, description, screenshotFilename);
    }

}
