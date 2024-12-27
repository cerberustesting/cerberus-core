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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.ITestCaseStepActionDAO;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.factory.IFactoryTestCaseStepAction;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.SqlUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

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

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query);) {

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

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString())) {

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

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString());) {

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
                .append("`conditionOperator`, `conditionValue1`, `conditionValue2`, `conditionValue3`, `conditionOptions`, `action`, ")
                .append("`value1`, `value2`, `value3`, `options`, `IsFatal`, `description`, `screenshotfilename`, `waitBefore`, `waitAfter`, `doScreenshotBefore`, `doScreenshotAfter`, `usrCreated`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString());) {

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
            preStat.setString(i++, testCaseStepAction.getConditionOptions() == null ? "[]" : testCaseStepAction.getConditionOptions().toString());
            preStat.setString(i++, testCaseStepAction.getAction());
            preStat.setString(i++, testCaseStepAction.getValue1());
            preStat.setString(i++, testCaseStepAction.getValue2());
            preStat.setString(i++, testCaseStepAction.getValue3());
            preStat.setString(i++, testCaseStepAction.getOptions().toString());
            preStat.setBoolean(i++, testCaseStepAction.isFatal());
            preStat.setString(i++, testCaseStepAction.getDescription());
            preStat.setString(i++, testCaseStepAction.getScreenshotFilename());
            preStat.setInt(i++, testCaseStepAction.getWaitBefore());
            preStat.setInt(i++, testCaseStepAction.getWaitAfter());
            preStat.setBoolean(i++, testCaseStepAction.isDoScreenshotBefore());
            preStat.setBoolean(i++, testCaseStepAction.isDoScreenshotAfter());
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
                .append("`ConditionOptions` = ?, ")
                .append("`Action` = ?, ")
                .append("`Value1` = ?, ")
                .append("`Value2` = ?, ")
                .append("`Value3` = ?, ")
                .append("`Options` = ?, ")
                .append("`IsFatal` = ?, ")
                .append("`Description` = ?, ")
                .append("`ScreenshotFilename` = ?, ")
                .append("`waitBefore` = ?, ")
                .append("`waitAfter` = ?, ")
                .append("`doScreenshotBefore` = ?, ")
                .append("`doScreenshotAfter` = ?, ")
                .append("`UsrModif` = ?, ")
                .append("`dateModif` = CURRENT_TIMESTAMP ")
                .append("WHERE `Test` = ? AND `Testcase` = ? AND `StepId` = ? AND `actionId` = ? ")
                .toString();

        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL " + query);
            LOG.debug("SQL.param.conditionOperator " + testCaseStepAction.getConditionOperator());
            LOG.debug("SQL.param.conditionValue1 " + testCaseStepAction.getConditionValue1());
            LOG.debug("SQL.param.conditionValue2 " + testCaseStepAction.getConditionValue2());
            LOG.debug("SQL.param.options " + testCaseStepAction.getOptions().toString());
        }

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query);) {

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
            preStat.setString(i++, testCaseStepAction.getConditionOptions() == null ? "[]" : testCaseStepAction.getConditionOptions().toString());
            preStat.setString(i++, testCaseStepAction.getAction());
            preStat.setString(i++, testCaseStepAction.getValue1());
            preStat.setString(i++, testCaseStepAction.getValue2());
            preStat.setString(i++, testCaseStepAction.getValue3());
            preStat.setString(i++, testCaseStepAction.getOptions() == null ? "[]" : testCaseStepAction.getOptions().toString());
            preStat.setBoolean(i++, testCaseStepAction.isFatal());
            preStat.setString(i++, testCaseStepAction.getDescription());
            preStat.setString(i++, testCaseStepAction.getScreenshotFilename());
            preStat.setInt(i++, testCaseStepAction.getWaitBefore());
            preStat.setInt(i++, testCaseStepAction.getWaitAfter());
            preStat.setBoolean(i++, testCaseStepAction.isDoScreenshotBefore());
            preStat.setBoolean(i++, testCaseStepAction.isDoScreenshotAfter());
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
    public void updateService(String oldService, String service) throws CerberusException {
        final String query = new StringBuilder("UPDATE `testcasestepaction` ")
                .append("SET ")
                .append("`Value1` = ?, ")
                .append("`dateModif` = CURRENT_TIMESTAMP ")
                .append("WHERE `Value1` = ? AND action ='" + TestCaseStepAction.ACTION_CALLSERVICE + "'")
                .toString();

        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL " + query);
            LOG.debug("SQL.param.service " + service);
        }

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query);) {

            int i = 1;
            preStat.setString(i++, service);
            preStat.setString(i++, oldService);

            preStat.executeUpdate();
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        }
    }

    @Override
    public void updateApplicationObject(String field, String application, String oldObject, String newObject) throws CerberusException {
        final String query = new StringBuilder("UPDATE testcasestepaction tca ")
                .append("INNER JOIN testcase tc ON tc.test = tca.test AND tc.testcase = tca.testcase ")
                .append("SET tca.").append(field).append(" = replace(tca." + field + ", '%object." + oldObject + ".', '%object." + newObject + ".'), tca.`dateModif` = CURRENT_TIMESTAMP ")
                .append("where tc.application = ? and tca.").append(field).append(" like ? ;")
                .toString();

        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL " + query);
            LOG.debug("SQL.param.service " + field);
            LOG.debug("SQL.param.service " + application);
            LOG.debug("SQL.param.service " + "%\\%object." + oldObject + ".%");
        }

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query);) {

            int i = 1;
            preStat.setString(i++, application);
            preStat.setString(i++, "%\\%object." + oldObject + ".%");

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

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query);) {

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

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query);) {

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
                .append("`conditionOperator`, `conditionValue1`, `conditionValue2`, `conditionValue3`, `conditionOptions`, `action`, `Value1`, `Value2`, `Value3`, `Options`"
                        + ", `IsFatal`, `description`, `screenshotfilename`, `waitBefore`, `waitAfter`, `doScreenshotBefore`, `doScreenshotAfter`, `usrCreated`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString())) {
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
            preStat.setString(i++, testCaseStepAction.getConditionOptions() == null ? "[]" : testCaseStepAction.getConditionOptions().toString());
            preStat.setString(i++, testCaseStepAction.getAction());
            preStat.setString(i++, testCaseStepAction.getValue1());
            preStat.setString(i++, testCaseStepAction.getValue2());
            preStat.setString(i++, testCaseStepAction.getValue3());
            preStat.setString(i++, testCaseStepAction.getOptions() == null ? "[]" : testCaseStepAction.getOptions().toString());
            preStat.setBoolean(i++, testCaseStepAction.isFatal());
            preStat.setString(i++, testCaseStepAction.getDescription());
            preStat.setString(i++, testCaseStepAction.getScreenshotFilename());
            preStat.setInt(i++, testCaseStepAction.getWaitBefore());
            preStat.setInt(i++, testCaseStepAction.getWaitAfter());
            preStat.setBoolean(i++, testCaseStepAction.isDoScreenshotBefore());
            preStat.setBoolean(i++, testCaseStepAction.isDoScreenshotAfter());
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
        JSONArray conditionOptions = SqlUtil.getJSONArrayFromColumn(resultSet, "tca.ConditionOptions");
        String action = resultSet.getString("tca.Action");
        String value1 = resultSet.getString("tca.Value1");
        String value2 = resultSet.getString("tca.Value2");
        String value3 = resultSet.getString("tca.Value3");
        JSONArray options = SqlUtil.getJSONArrayFromColumn(resultSet, "tca.Options");
        String description = resultSet.getString("tca.description");
        String screenshotFilename = resultSet.getString("tca.screenshotFilename");
        boolean isFatal = resultSet.getBoolean("tca.isFatal");
        boolean doScreenshotBefore = resultSet.getBoolean("tca.doScreenshotBefore");
        boolean doScreenshotAfter = resultSet.getBoolean("tca.doScreenshotAfter");
        int waitBefore = resultSet.getInt("tca.waitBefore");
        int waitAfter = resultSet.getInt("tca.waitAfter");
        String usrCreated = resultSet.getString("tca.UsrCreated");
        Timestamp dateCreated = resultSet.getTimestamp("tca.DateCreated");
        String usrModif = resultSet.getString("tca.UsrModif");
        Timestamp dateModif = resultSet.getTimestamp("tca.DateModif");

        return factoryTestCaseStepAction.create(test, testcase, stepId, actionId, sort, conditionOperator,
                conditionValue1, conditionValue2, conditionValue3, conditionOptions, action, value1,
                value2, value3, options, isFatal, description, screenshotFilename,
                doScreenshotBefore, doScreenshotAfter, waitBefore, waitAfter,
                usrCreated, dateCreated, usrModif, dateModif);
    }

}
