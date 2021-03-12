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
    public TestCaseStepAction readByKey(String test, String testcase, int stepId, int actionId) {
        TestCaseStepAction testCaseStepAction = null;
        final String query = "SELECT * FROM testcasestepaction tca WHERE tca.test = ? AND tca.testcase = ? AND tca.stepId = ? AND tca.actionId = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query)) {

            preStat.setString(1, test);
            preStat.setString(2, testcase);
            preStat.setInt(3, stepId);
            preStat.setInt(4, actionId);

            try (ResultSet resultSet = preStat.executeQuery()) {
                if (resultSet.first()) {
                    testCaseStepAction = this.loadFromResultSet(resultSet);
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            }

        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        }

        return testCaseStepAction;
    }

    @Override
    public List<TestCaseStepAction> findTestCaseStepActionbyTestTestCase(String test, String testcase) throws CerberusException {
        List<TestCaseStepAction> list = null;
        final StringBuilder query = new StringBuilder();
        query.append("SELECT tca.* ");
        query.append("FROM testcasestepaction AS tca ");
        query.append("RIGHT JOIN testcasestep AS tcs ON tca.Test = tcs.Test AND tca.Testcase = tcs.Testcase AND tca.StepId = tcs.StepId ");
        query.append("WHERE tca.Test = ? AND tca.Testcase = ? ");
        query.append("GROUP BY tca.Test, tca.Testcase, tca.StepId, tca.actionId ");
        query.append("ORDER BY tcs.Sort, tca.Sort ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());) {

            preStat.setString(1, test);
            preStat.setString(2, testcase);

            try (ResultSet resultSet = preStat.executeQuery()) {
                list = new ArrayList<>();
                while (resultSet.next()) {
                    list.add(this.loadFromResultSet(resultSet));
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        }
        return list;
    }

    @Override
    public List<TestCaseStepAction> findActionByTestTestCaseStep(String test, String testcase, int stepId) {
        List<TestCaseStepAction> list = null;
        final String query = "SELECT * FROM testcasestepaction tca WHERE tca.test = ? AND tca.testcase = ? AND tca.stepId = ? ORDER BY tca.sort";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.test : " + test);
            LOG.debug("SQL.param.testcase : " + testcase);
            LOG.debug("SQL.param.stepId : " + stepId);
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query);) {

            preStat.setString(1, test);
            preStat.setString(2, testcase);
            preStat.setInt(3, stepId);

            try (ResultSet resultSet = preStat.executeQuery();) {
                list = new ArrayList<>();
                while (resultSet.next()) {
                    list.add(this.loadFromResultSet(resultSet));
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            }

        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        }
        return list;
    }

    @Override
    public AnswerList<TestCaseStepAction> readByTestTestCase(String test, String testcase) {
        AnswerList<TestCaseStepAction> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCaseStepAction> actionList = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcasestepaction tca WHERE tca.test = ? AND tca.testcase = ?");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString())) {

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
        }

        response.setResultMessage(msg);
        return response;
    }

    @Override
    public AnswerList<TestCaseStepAction> readByVarious1(String test, String testcase, int stepId) {

        AnswerList<TestCaseStepAction> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCaseStepAction> actionList = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcasestepaction tca WHERE tca.test = ? AND tca.testcase = ? AND tca.stepId = ?");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());) {

            preStat.setString(1, test);
            preStat.setString(2, testcase);
            preStat.setInt(3, stepId);

            try {

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
        }

        response.setResultMessage(msg);
        return response;
    }

    @Override
    public void createTestCaseStepAction(TestCaseStepAction testCaseStepAction) throws CerberusException {
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcasestepaction (`test`, `testcase`, `stepId`, `actionId`, `sort`, ")
                .append("`conditionOperator`, `conditionValue1`, `conditionValue2`, `conditionValue3`, `action`, ")
                .append("`value1`, `value2`, `value3`, `IsFatal`, `description`, `screenshotfilename`, `usrCreated`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());) {

            int i = 1;
            preStat.setString(i++, testCaseStepAction.getTest());
            preStat.setString(i++, testCaseStepAction.getTestcase());
            preStat.setInt(i++, testCaseStepAction.getStepId());
            preStat.setInt(i++, testCaseStepAction.getActionId());
            preStat.setInt(i++, testCaseStepAction.getSort());
            preStat.setString(i++, testCaseStepAction.getConditionOperator());
            preStat.setString(i++, testCaseStepAction.getConditionValue1());
            preStat.setString(i++, testCaseStepAction.getConditionValue2());
            preStat.setString(i++, testCaseStepAction.getConditionValue3());
            preStat.setString(i++, testCaseStepAction.getAction());
            preStat.setString(i++, testCaseStepAction.getValue1());
            preStat.setString(i++, testCaseStepAction.getValue2());
            preStat.setString(i++, testCaseStepAction.getValue3());
            preStat.setBoolean(i++, testCaseStepAction.isFatal());
            preStat.setString(i++, testCaseStepAction.getDescription());
            preStat.setString(i++, testCaseStepAction.getScreenshotFilename());
            preStat.setString(i++, testCaseStepAction.getUsrCreated() == null ? "" : testCaseStepAction.getUsrCreated());

            preStat.executeUpdate();

        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        }
    }

    @Override
    public void update(TestCaseStepAction testCaseStepAction) throws CerberusException {
        final String query = new StringBuilder("UPDATE `testcasestepaction` ")
                .append("SET ")
                .append("`Test` = ?, ")
                .append("`Testcase` = ?, ")
                .append("`StepId` = ?, ")
                .append("`actionId` = ?, ")
                .append("`Sort` = ?, ")
                .append("`conditionOperator` = ?, ")
                .append("`ConditionValue1` = ?, ")
                .append("`ConditionValue2` = ?, ")
                .append("`ConditionValue3` = ?, ")
                .append("`Action` = ?, ")
                .append("`Value1` = ?, ")
                .append("`Value2` = ?, ")
                .append("`Value3` = ?, ")
                .append("`IsFatal` = ?, ")
                .append("`Description` = ?, ")
                .append("`ScreenshotFilename` = ?, ")
                .append("`UsrModif` = ?, ")
                .append("`dateModif` = CURRENT_TIMESTAMP ")
                .append("WHERE `Test` = ? AND `Testcase` = ? AND `StepId` = ? AND `actionId` = ? ")
                .toString();

        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL " + query);
            LOG.debug("SQL.param.conditionOperator " + testCaseStepAction.getConditionOperator());
            LOG.debug("SQL.param.conditionValue1 " + testCaseStepAction.getConditionValue1());
            LOG.debug("SQL.param.conditionValue2 " + testCaseStepAction.getConditionValue2());
            LOG.debug("SQL.param.conditionValue3 " + testCaseStepAction.getConditionValue3());
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query);) {

            int i = 1;
            preStat.setString(i++, testCaseStepAction.getTest());
            preStat.setString(i++, testCaseStepAction.getTestcase());
            preStat.setInt(i++, testCaseStepAction.getStepId());
            preStat.setInt(i++, testCaseStepAction.getActionId());
            preStat.setInt(i++, testCaseStepAction.getSort());
            preStat.setString(i++, testCaseStepAction.getConditionOperator());
            preStat.setString(i++, testCaseStepAction.getConditionValue1());
            preStat.setString(i++, testCaseStepAction.getConditionValue2());
            preStat.setString(i++, testCaseStepAction.getConditionValue3());
            preStat.setString(i++, testCaseStepAction.getAction());
            preStat.setString(i++, testCaseStepAction.getValue1());
            preStat.setString(i++, testCaseStepAction.getValue2());
            preStat.setString(i++, testCaseStepAction.getValue3());
            preStat.setBoolean(i++, testCaseStepAction.isFatal());
            preStat.setString(i++, testCaseStepAction.getDescription());
            preStat.setString(i++, testCaseStepAction.getScreenshotFilename());
            preStat.setString(i++, testCaseStepAction.getUsrModif() == null ? "" : testCaseStepAction.getUsrModif());

            preStat.setString(i++, testCaseStepAction.getTest());
            preStat.setString(i++, testCaseStepAction.getTestcase());
            preStat.setInt(i++, testCaseStepAction.getStepId());
            preStat.setInt(i++, testCaseStepAction.getActionId());

            preStat.executeUpdate();
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        }
    }

    @Override
    public void delete(TestCaseStepAction tcsa) throws CerberusException {
        boolean throwExcep = false;
        final String query = "DELETE FROM testcasestepaction WHERE test = ? and testcase = ? and stepId = ? and `actionId` = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query);) {

            preStat.setString(1, tcsa.getTest());
            preStat.setString(2, tcsa.getTestcase());
            preStat.setInt(3, tcsa.getStepId());
            preStat.setInt(4, tcsa.getActionId());

            throwExcep = preStat.executeUpdate() == 0;

        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        }

        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public boolean changeTestCaseStepActionActionId(String test, String testcase, int stepId, int oldActionId, int newActionId) {
        final String query = "update testcasestepaction set actionId = ? WHERE test = ? AND testcase = ? AND stepId = ? AND actionId = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query);) {

            preStat.setInt(1, newActionId);
            preStat.setString(2, test);
            preStat.setString(3, testcase);
            preStat.setInt(4, stepId);
            preStat.setInt(5, oldActionId);

            int lines = preStat.executeUpdate();
            return (lines > 0);

        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        }
        return false;
    }

    @Override
    public Answer create(TestCaseStepAction testCaseStepAction) {
        Answer ans = new Answer();
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcasestepaction (`test`, `testcase`, `stepId`, `actionId`, `sort`, ")
                .append("`conditionOperator`, `conditionValue1`, `conditionValue2`, `conditionValue3`, `action`, `Value1`, `Value2`, `Value3`, `IsFatal`, `description`, `screenshotfilename`, `usrCreated`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString())) {
            // Prepare and execute query
            int i = 1;
            preStat.setString(i++, testCaseStepAction.getTest());
            preStat.setString(i++, testCaseStepAction.getTestcase());
            preStat.setInt(i++, testCaseStepAction.getStepId());
            preStat.setInt(i++, testCaseStepAction.getActionId());
            preStat.setInt(i++, testCaseStepAction.getSort());
            preStat.setString(i++, testCaseStepAction.getConditionOperator());
            preStat.setString(i++, testCaseStepAction.getConditionValue1());
            preStat.setString(i++, testCaseStepAction.getConditionValue2());
            preStat.setString(i++, testCaseStepAction.getConditionValue3());
            preStat.setString(i++, testCaseStepAction.getAction());
            preStat.setString(i++, testCaseStepAction.getValue1());
            preStat.setString(i++, testCaseStepAction.getValue2());
            preStat.setString(i++, testCaseStepAction.getValue3());
            preStat.setBoolean(i++, testCaseStepAction.isFatal());
            preStat.setString(i++, testCaseStepAction.getDescription());
            preStat.setString(i++, testCaseStepAction.getScreenshotFilename());
            preStat.setString(i++, testCaseStepAction.getUsrCreated() == null ? "" : testCaseStepAction.getUsrCreated());
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
        String testcase = resultSet.getString("tca.Testcase");
        Integer stepId = resultSet.getInt("tca.StepId");
        Integer actionId = resultSet.getInt("tca.actionId");
        Integer sort = resultSet.getInt("tca.Sort");
        String conditionOperator = resultSet.getString("tca.conditionOperator");
        String conditionValue1 = resultSet.getString("tca.ConditionValue1");
        String conditionValue2 = resultSet.getString("tca.ConditionValue2");
        String conditionValue3 = resultSet.getString("tca.conditionValue3");
        String action = resultSet.getString("tca.Action");
        String value1 = resultSet.getString("tca.Value1");
        String value2 = resultSet.getString("tca.Value2");
        String value3 = resultSet.getString("tca.Value3");
        String description = resultSet.getString("tca.description");
        String screenshotFilename = resultSet.getString("tca.screenshotFilename");
        boolean isFatal = resultSet.getBoolean("tca.isFatal");

        return factoryTestCaseStepAction.create(test, testcase, stepId, actionId, sort, conditionOperator, conditionValue1, conditionValue2, conditionValue3, action, value1, value2, value3, isFatal, description, screenshotFilename);
    }

}
