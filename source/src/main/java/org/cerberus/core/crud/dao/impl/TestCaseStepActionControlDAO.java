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
import org.cerberus.core.crud.dao.ITestCaseStepActionControlDAO;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.cerberus.core.crud.factory.IFactoryTestCaseStepActionControl;
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
import java.sql.Statement;
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
public class TestCaseStepActionControlDAO implements ITestCaseStepActionControlDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseStepActionControl factoryTestCaseStepActionControl;

    private static final Logger LOG = LogManager.getLogger(TestCaseStepActionControlDAO.class);

    private final String OBJECT_NAME = "TestCaseStepActionControl";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public TestCaseStepActionControl findTestCaseStepActionControlByKey(String test, String testcase, int stepId, int actionId, int controlId) {
        TestCaseStepActionControl actionControl = null;
        final String query = "SELECT * FROM testcasestepactioncontrol WHERE test = ? AND testcase = ? AND stepId = ? AND actionId = ? AND control = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {

            preStat.setString(1, test);
            preStat.setString(2, testcase);
            preStat.setInt(3, stepId);
            preStat.setInt(4, actionId);
            preStat.setInt(5, controlId);

            try (ResultSet resultSet = preStat.executeQuery();) {
                if (resultSet.first()) {
                    actionControl = loadFromResultSet(resultSet);
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        }
        return actionControl;
    }

    @Override
    public List<TestCaseStepActionControl> findControlByTestTestCaseStepIdActionId(String test, String testcase, int stepId, int actionId) {
        List<TestCaseStepActionControl> list = null;
        final String query = "SELECT * FROM testcasestepactioncontrol WHERE test = ? AND testcase = ? AND stepId = ? AND actionId = ? ORDER BY sort";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.test : " + test);
            LOG.debug("SQL.param.testcase : " + testcase);
            LOG.debug("SQL.param.stepId : " + stepId);
            LOG.debug("SQL.param.actionId : " + actionId);
        }

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query);) {

            preStat.setString(1, test);
            preStat.setString(2, testcase);
            preStat.setInt(3, stepId);
            preStat.setInt(4, actionId);

            try (ResultSet resultSet = preStat.executeQuery();) {
                list = new ArrayList<>();

                while (resultSet.next()) {
                    list.add(loadFromResultSet(resultSet));
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
    public void insertTestCaseStepActionControl(TestCaseStepActionControl testCaseStepActionControl) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcasestepactioncontrol (`test`, `testcase`, `stepId`, `actionId`, `controlId`, `sort`, ");
        query.append("`conditionOperator`, `conditionValue1`, `conditionValue2`, `conditionValue3`, `conditionOptions`, `control`, `value1`, `value2`, `value3`, `options`, `isFatal`, `Description`, `screenshotfilename`, `waitBefore`, `waitAfter`, `doScreenshotBefore`, `doScreenshotAfter`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString());) {

            int i = 1;
            preStat.setString(i++, testCaseStepActionControl.getTest());
            preStat.setString(i++, testCaseStepActionControl.getTestcase());
            preStat.setInt(i++, testCaseStepActionControl.getStepId());
            preStat.setInt(i++, testCaseStepActionControl.getActionId());
            preStat.setInt(i++, testCaseStepActionControl.getControlId());
            preStat.setInt(i++, testCaseStepActionControl.getSort());
            preStat.setString(i++, testCaseStepActionControl.getConditionOperator());
            preStat.setString(i++, testCaseStepActionControl.getConditionValue1());
            preStat.setString(i++, testCaseStepActionControl.getConditionValue2());
            preStat.setString(i++, testCaseStepActionControl.getConditionValue3());
            preStat.setString(i++, testCaseStepActionControl.getConditionOptions() == null ? "[]" : testCaseStepActionControl.getConditionOptions().toString());
            preStat.setString(i++, testCaseStepActionControl.getControl());
            preStat.setString(i++, testCaseStepActionControl.getValue1());
            preStat.setString(i++, testCaseStepActionControl.getValue2());
            preStat.setString(i++, testCaseStepActionControl.getValue3());
            preStat.setString(i++, testCaseStepActionControl.getOptions() == null ? "[]" : testCaseStepActionControl.getOptions().toString());
            preStat.setBoolean(i++, testCaseStepActionControl.isFatal());
            preStat.setString(i++, testCaseStepActionControl.getDescription());
            preStat.setString(i++, testCaseStepActionControl.getScreenshotFilename());
            preStat.setInt(i++, testCaseStepActionControl.getWaitBefore());
            preStat.setInt(i++, testCaseStepActionControl.getWaitAfter());
            preStat.setBoolean(i++, testCaseStepActionControl.isDoScreenshotBefore());
            preStat.setBoolean(i++, testCaseStepActionControl.isDoScreenshotAfter());
            throwExcep = preStat.executeUpdate() == 0;
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public List<TestCaseStepActionControl> findControlByTestTestCaseStepId(String test, String testcase, int stepId) {
        List<TestCaseStepActionControl> list = null;
        final String query = "SELECT * FROM testcasestepactioncontrol WHERE test = ? AND testcase = ? AND stepId = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query);) {

            preStat.setString(1, test);
            preStat.setString(2, testcase);
            preStat.setInt(3, stepId);

            try (ResultSet resultSet = preStat.executeQuery();) {
                list = new ArrayList<>();

                while (resultSet.next()) {
                    list.add(loadFromResultSet(resultSet));
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
    public void updateTestCaseStepActionControl(TestCaseStepActionControl testCaseStepActionControl) throws CerberusException {
        boolean throwExcep = false;

        final String query = new StringBuilder("UPDATE `testcasestepactioncontrol` SET ")
                .append("`Test` = ?, ")
                .append("`Testcase` = ?, ")
                .append("`StepId` = ?, ")
                .append("`ActionId` = ?, ")
                .append("`ControlId` = ?, ")
                .append("`Sort` = ?, ")
                .append("`conditionOperator` = ?, ")
                .append("`conditionValue1` = ?, ")
                .append("`conditionValue2` = ?, ")
                .append("`conditionValue3` = ?, ")
                .append("`conditionOptions` = ?, ")
                .append("`Control` = ?, ")
                .append("`Value1` = ?, ")
                .append("`Value2` = ?, ")
                .append("`Value3` = ?, ")
                .append("`Options` = ?, ")
                .append("`Description` = ?, ")
                .append("`IsFatal` = ?, ")
                .append("`screenshotFilename` = ?, ")
                .append("`waitBefore` = ?, ")
                .append("`waitAfter` = ?, ")
                .append("`doScreenshotBefore` = ?, ")
                .append("`doScreenshotAfter` = ?, ")
                .append("`usrModif` = ?,")
                .append("`dateModif` = CURRENT_TIMESTAMP ")
                .append("WHERE `Test` = ? AND `Testcase` = ? AND `StepId` = ? AND `ActionId` = ? AND `ControlId` = ? ")
                .toString();

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.conditionoptions : " + testCaseStepActionControl.getConditionOptions().toString());
            LOG.debug("SQL.param.options : " + testCaseStepActionControl.getOptions().toString());
        }

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query);) {

            int i = 1;
            preStat.setString(i++, testCaseStepActionControl.getTest());
            preStat.setString(i++, testCaseStepActionControl.getTestcase());
            preStat.setInt(i++, testCaseStepActionControl.getStepId());
            preStat.setInt(i++, testCaseStepActionControl.getActionId());
            preStat.setInt(i++, testCaseStepActionControl.getControlId());
            preStat.setInt(i++, testCaseStepActionControl.getSort());
            preStat.setString(i++, testCaseStepActionControl.getConditionOperator());
            preStat.setString(i++, testCaseStepActionControl.getConditionValue1());
            preStat.setString(i++, testCaseStepActionControl.getConditionValue2());
            preStat.setString(i++, testCaseStepActionControl.getConditionValue3());
            preStat.setString(i++, testCaseStepActionControl.getConditionOptions() == null ? "[]" : testCaseStepActionControl.getConditionOptions().toString());
            preStat.setString(i++, testCaseStepActionControl.getControl());
            preStat.setString(i++, testCaseStepActionControl.getValue1());
            preStat.setString(i++, testCaseStepActionControl.getValue2());
            preStat.setString(i++, testCaseStepActionControl.getValue3());
            preStat.setString(i++, testCaseStepActionControl.getOptions() == null ? "[]" : testCaseStepActionControl.getOptions().toString());
            preStat.setString(i++, testCaseStepActionControl.getDescription());
            preStat.setBoolean(i++, testCaseStepActionControl.isFatal());
            preStat.setString(i++, testCaseStepActionControl.getScreenshotFilename());
            preStat.setInt(i++, testCaseStepActionControl.getWaitBefore());
            preStat.setInt(i++, testCaseStepActionControl.getWaitAfter());
            preStat.setBoolean(i++, testCaseStepActionControl.isDoScreenshotBefore());
            preStat.setBoolean(i++, testCaseStepActionControl.isDoScreenshotAfter());
            preStat.setString(i++, testCaseStepActionControl.getUsrModif() == null ? "" : testCaseStepActionControl.getUsrModif());
            preStat.setString(i++, testCaseStepActionControl.getTest());
            preStat.setString(i++, testCaseStepActionControl.getTestcase());
            preStat.setInt(i++, testCaseStepActionControl.getStepId());
            preStat.setInt(i++, testCaseStepActionControl.getActionId());
            preStat.setInt(i++, testCaseStepActionControl.getControlId());
            throwExcep = preStat.executeUpdate() == 0;
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void updateApplicationObject(String field, String application, String oldObject, String newObject) throws CerberusException {
        final String query = new StringBuilder("UPDATE testcasestepactioncontrol tcc ")
                .append("INNER JOIN testcase tc ON tc.test = tcc.test AND tc.testcase = tcc.testcase ")
                .append("SET tcc.").append(field).append(" = replace(tcc." + field + ", '%object." + oldObject + ".', '%object." + newObject + ".'), tcc.`dateModif` = CURRENT_TIMESTAMP ")
                .append("where tc.application = ? and tcc.").append(field).append(" like ? ;")
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
    public void deleteTestCaseStepActionControl(TestCaseStepActionControl tcsac) throws CerberusException {
        boolean throwExcep = false;
        final String query = "DELETE FROM testcasestepactioncontrol WHERE test = ? and testcase = ? and stepId = ? and `actionId` = ? and `controlId` = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query);) {
            preStat.setString(1, tcsac.getTest());
            preStat.setString(2, tcsac.getTestcase());
            preStat.setInt(3, tcsac.getStepId());
            preStat.setInt(4, tcsac.getActionId());
            preStat.setInt(5, tcsac.getControlId());
            throwExcep = preStat.executeUpdate() == 0;
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public List<TestCaseStepActionControl> findControlByTestTestCase(String test, String testcase) throws CerberusException {
        List<TestCaseStepActionControl> list = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT tcsac.* ");
        query.append("FROM testcasestepactioncontrol AS tcsac ");
        query.append("RIGHT JOIN testcasestepaction AS tcsa ON tcsac.Test = tcsa.Test AND tcsac.Testcase = tcsa.Testcase AND tcsac.StepId = tcsa.StepId AND tcsac.ActionId = tcsa.ActionId ");
        query.append("RIGHT JOIN testcasestep AS tcs ON tcsac.Test = tcs.Test AND tcsac.Testcase = tcs.Testcase AND tcsac.StepId = tcs.StepId ");
        query.append("WHERE tcsac.Test = ? AND tcsac.Testcase = ? ");
        query.append("GROUP BY tcsac.Test, tcsac.Testcase, tcsac.StepId, tcsac.ActionId, tcsac.ControlId ");
        query.append("ORDER BY tcs.Sort, tcsa.Sort, tcsac.Sort ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString());) {

            preStat.setString(1, test);
            preStat.setString(2, testcase);

            try (ResultSet resultSet = preStat.executeQuery();) {
                list = new ArrayList<>();

                while (resultSet.next()) {
                    list.add(loadFromResultSet(resultSet));
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
    public AnswerList<TestCaseStepActionControl> readByTestTestCase(String test, String testcase) {
        AnswerList<TestCaseStepActionControl> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCaseStepActionControl> controlList = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcasestepactioncontrol WHERE test = ? AND testcase = ?");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); Statement stm = connection.createStatement();) {

            preStat.setString(1, test);
            preStat.setString(2, testcase);

            try (ResultSet resultSet = preStat.executeQuery(); ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()");) {
                //gets the data
                while (resultSet.next()) {
                    controlList.add(this.loadFromResultSet(resultSet));
                }

                //get the total number of rows
                int nrTotalRows = 0;

                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }

                if (controlList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                    response = new AnswerList<>(controlList, controlList.size());
                } else if (controlList.size() <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    response = new AnswerList<>(controlList, controlList.size());
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    response = new AnswerList<>(controlList, controlList.size());
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
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
    public AnswerList<TestCaseStepActionControl> readByVarious1(String test, String testcase, int stepId, int actionId) {
        AnswerList<TestCaseStepActionControl> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCaseStepActionControl> controlList = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcasestepactioncontrol WHERE test = ? AND testcase = ? AND stepId = ? AND actionId = ?");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); Statement stm = connection.createStatement();) {

            preStat.setString(1, test);
            preStat.setString(2, testcase);
            preStat.setInt(3, stepId);
            preStat.setInt(4, actionId);

            try (ResultSet resultSet = preStat.executeQuery(); ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()");) {
                //gets the data
                while (resultSet.next()) {
                    controlList.add(this.loadFromResultSet(resultSet));
                }

                //get the total number of rows
                int nrTotalRows = 0;

                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }

                if (controlList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                    response = new AnswerList<>(controlList, controlList.size());
                } else if (controlList.size() <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    response = new AnswerList<>(controlList, controlList.size());
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    response = new AnswerList<>(controlList, controlList.size());
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));

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
    public Answer create(TestCaseStepActionControl testCaseStepActionControl) {
        Answer ans = new Answer();
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcasestepactioncontrol (`test`, `testcase`, `stepId`, `actionId`, `controlId`, `sort`, ");
        query.append("`conditionOperator`, `conditionValue1`, `conditionValue2`, `conditionValue3`, `conditionOptions`, `control`, ");
        query.append("`value1`, `value2`, `value3`, `Options`, `isFatal`, `Description`, `screenshotfilename`, `waitBefore`, `waitAfter`, `doScreenshotBefore`, `doScreenshotAfter`, `usrCreated`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString())) {
            // Prepare and execute query
            int i = 1;
            preStat.setString(i++, testCaseStepActionControl.getTest());
            preStat.setString(i++, testCaseStepActionControl.getTestcase());
            preStat.setInt(i++, testCaseStepActionControl.getStepId());
            preStat.setInt(i++, testCaseStepActionControl.getActionId());
            preStat.setInt(i++, testCaseStepActionControl.getControlId());
            preStat.setInt(i++, testCaseStepActionControl.getSort());
            preStat.setString(i++, testCaseStepActionControl.getConditionOperator());
            preStat.setString(i++, testCaseStepActionControl.getConditionValue1());
            preStat.setString(i++, testCaseStepActionControl.getConditionValue2());
            preStat.setString(i++, testCaseStepActionControl.getConditionValue3());
            preStat.setString(i++, testCaseStepActionControl.getConditionOptions() == null ? "[]" : testCaseStepActionControl.getConditionOptions().toString());
            preStat.setString(i++, testCaseStepActionControl.getControl());
            preStat.setString(i++, testCaseStepActionControl.getValue1());
            preStat.setString(i++, testCaseStepActionControl.getValue2());
            preStat.setString(i++, testCaseStepActionControl.getValue3());
            preStat.setString(i++, testCaseStepActionControl.getOptions() == null ? "[]" : testCaseStepActionControl.getOptions().toString());
            preStat.setBoolean(i++, testCaseStepActionControl.isFatal());
            preStat.setString(i++, testCaseStepActionControl.getDescription());
            preStat.setString(i++, testCaseStepActionControl.getScreenshotFilename());
            preStat.setInt(i++, testCaseStepActionControl.getWaitBefore());
            preStat.setInt(i++, testCaseStepActionControl.getWaitAfter());
            preStat.setBoolean(i++, testCaseStepActionControl.isDoScreenshotBefore());
            preStat.setBoolean(i++, testCaseStepActionControl.isDoScreenshotAfter());
            preStat.setString(i++, testCaseStepActionControl.getUsrCreated() == null ? "" : testCaseStepActionControl.getUsrCreated());
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "CREATE");
        } catch (Exception e) {
            LOG.warn("Unable to TestCaseStepActionControl: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    private TestCaseStepActionControl loadFromResultSet(ResultSet resultSet) throws SQLException {
        String test = resultSet.getString("Test");
        String testcase = resultSet.getString("Testcase");
        Integer stepId = resultSet.getInt("StepId");
        Integer actionId = resultSet.getInt("ActionId");
        Integer controlId = resultSet.getInt("controlId");
        Integer sort = resultSet.getInt("Sort");
        String conditionOperator = resultSet.getString("conditionOperator");
        String conditionValue1 = resultSet.getString("conditionValue1");
        String conditionValue2 = resultSet.getString("conditionValue2");
        String conditionValue3 = resultSet.getString("conditionValue3");
        JSONArray conditionOptions = SqlUtil.getJSONArrayFromColumn(resultSet, "conditionOptions");
        String control = resultSet.getString("Control");
        boolean isFatal = resultSet.getBoolean("isFatal");
        String value1 = resultSet.getString("Value1");
        String value2 = resultSet.getString("Value2");
        String value3 = resultSet.getString("Value3");
        JSONArray options = SqlUtil.getJSONArrayFromColumn(resultSet, "options");
        String description = resultSet.getString("Description");
        boolean doScreenshotBefore = resultSet.getBoolean("doScreenshotBefore");
        boolean doScreenshotAfter = resultSet.getBoolean("doScreenshotAfter");
        int waitBefore = resultSet.getInt("waitBefore");
        int waitAfter = resultSet.getInt("waitAfter");
        String screenshotFilename = resultSet.getString("screenshotFilename");
        String usrCreated = resultSet.getString("UsrCreated");
        Timestamp dateCreated = resultSet.getTimestamp("DateCreated");
        String usrModif = resultSet.getString("UsrModif");
        Timestamp dateModif = resultSet.getTimestamp("DateModif");

        return factoryTestCaseStepActionControl.create(test, testcase, stepId, actionId, controlId, sort, conditionOperator, conditionValue1, conditionValue2, conditionValue3, conditionOptions,
                control, value1, value2, value3,
                options, isFatal, description, screenshotFilename,
                doScreenshotBefore, doScreenshotAfter, waitBefore, waitAfter,
                usrCreated, dateCreated, usrModif, dateModif);
    }
}
