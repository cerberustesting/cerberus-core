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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.crud.dao.ITestCaseExecutionQueueDepDAO;
import org.cerberus.crud.entity.TestCaseExecutionQueueDep;
import org.cerberus.crud.factory.IFactoryApplicationObject;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Implements methods defined on IApplicationDAO
 *
 * @author tbernardes
 * @version 1.0, 15/10/13
 * @since 0.9.0
 */
@Repository
public class TestCaseExecutionQueueDepDAO implements ITestCaseExecutionQueueDepDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryApplicationObject factoryApplicationObject;
    @Autowired
    private IParameterService parameterService;

    private static final Logger LOG = LogManager.getLogger(TestCaseExecutionQueueDepDAO.class);

    private final String OBJECT_NAME = "TestCaseExecutionQueueDep";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public AnswerItem<TestCaseExecutionQueueDep> readByKey(long id) {
        AnswerItem ans = new AnswerItem<>();
        MessageEvent msg = null;

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement("SELECT * FROM `testcaseexecutionqueuedep` WHERE `ID` = ?")) {
            TestCaseExecutionQueueDep ao = null;
            // Prepare and execute query
            preStat.setLong(1, id);
            ResultSet rs = preStat.executeQuery();
            try {
                while (rs.next()) {
                    ao = loadFromResultSet(rs);
                }
                ans.setItem(ao);
                // Set the final message
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                        .resolveDescription("OPERATION", "READ_BY_KEY");
            } catch (Exception e) {
                LOG.warn("Unable to execute query : " + e.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                        e.toString());
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
        } catch (Exception e) {
            LOG.warn("Unable to read by key: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }
        return ans;
    }

    @Override
    public AnswerList<TestCaseExecutionQueueDep> readByExeId(long exeId) {
        AnswerList ans = new AnswerList<>();
        MessageEvent msg = null;

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement("SELECT * FROM `testcaseexecutionqueuedep` WHERE `ExeID` = ?")) {
            // Prepare and execute query
            preStat.setLong(1, exeId);
            ResultSet rs = preStat.executeQuery();
            try {
                List<TestCaseExecutionQueueDep> al = new ArrayList<>();
                while (rs.next()) {
                    al.add(loadFromResultSet(rs));
                }
                ans.setDataList(al);
                // Set the final message
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                        .resolveDescription("OPERATION", "READ_BY_APP");
            } catch (Exception e) {
                LOG.warn("Unable to execute query : " + e.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                        e.toString());
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
        } catch (Exception e) {
            LOG.warn("Unable to read by app: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }
        return ans;
    }

    @Override
    public AnswerItem<Integer> readNbWaitingByExeQueue(long exeQueueId) {
        AnswerItem<Integer> ans = new AnswerItem<>();
        MessageEvent msg = null;

        final String query = "SELECT ID FROM testcaseexecutionqueuedep WHERE `ExeQueueID` = ? and Status = 'WAITING';";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.exeQueueId : " + exeQueueId);
        }

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query)) {
            // Prepare and execute query
            preStat.setLong(1, exeQueueId);
            ResultSet rs = preStat.executeQuery();
            try {
                List<Long> al = new ArrayList<>();
                int nbRow = 0;
                while (rs.next()) {
                    nbRow++;
                }
                ans.setItem(nbRow);
                // Set the final message
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME).resolveDescription("OPERATION", "SELECT");
            } catch (Exception e) {
                LOG.error("Unable to execute query : " + e.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION", e.toString());
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
        } catch (Exception e) {
            LOG.error("Unable to read by exeId : " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION", e.toString());
        } finally {
            ans.setResultMessage(msg);
        }
        return ans;
    }

    @Override
    public AnswerItem<Integer> readNbReleasedWithNOKByExeQueue(long exeQueueId) {
        AnswerItem<Integer> ans = new AnswerItem<>();
        MessageEvent msg = null;

        final String query = "SELECT tce.controlstatus FROM testcaseexecutionqueuedep tcd JOIN testcaseexecution tce ON tcd.exeid=tce.id WHERE tcd.`ExeQueueID` = ? and tcd.Status = 'RELEASED' and tce.controlstatus != 'OK';";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.exeQueueId : " + exeQueueId);
        }

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query)) {
            // Prepare and execute query
            preStat.setLong(1, exeQueueId);
            ResultSet rs = preStat.executeQuery();
            try {
                List<Long> al = new ArrayList<>();
                int nbRow = 0;
                while (rs.next()) {
                    nbRow++;
                }
                ans.setItem(nbRow);
                // Set the final message
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME).resolveDescription("OPERATION", "SELECT");
            } catch (Exception e) {
                LOG.error("Unable to execute query : " + e.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION", e.toString());
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
        } catch (Exception e) {
            LOG.error("Unable to read by exeId : " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION", e.toString());
        } finally {
            ans.setResultMessage(msg);
        }
        return ans;
    }

    @Override
    public AnswerList<Long> readExeQueueIdByExeId(long exeId) {
        AnswerList ans = new AnswerList<>();
        MessageEvent msg = null;

        final String query = "SELECT DISTINCT ExeQueueID FROM testcaseexecutionqueuedep WHERE `ExeID` = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.exeId : " + exeId);
        }

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query)) {
            // Prepare and execute query
            preStat.setLong(1, exeId);
            ResultSet rs = preStat.executeQuery();
            try {
                List<Long> al = new ArrayList<>();
                while (rs.next()) {
                    al.add(rs.getLong("ExeQueueID"));
                }
                ans.setDataList(al);
                // Set the final message
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME).resolveDescription("OPERATION", "SELECT");
            } catch (Exception e) {
                LOG.error("Unable to execute query : " + e.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION", e.toString());
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
        } catch (Exception e) {
            LOG.error("Unable to read by exeId : " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION", e.toString());
        } finally {
            ans.setResultMessage(msg);
        }
        return ans;
    }

    @Override
    public AnswerList<TestCaseExecutionQueueDep> readByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        AnswerList response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCaseExecutionQueueDep> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM testcaseexecutionqueuedep ");

        searchSQL.append(" where 1=1 ");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (`Application` like ?");
            searchSQL.append(" or `Object` like ?");
            searchSQL.append(" or `Value` like ?");
            searchSQL.append(" or `ScreenshotFileName` like ?");
            searchSQL.append(" or `UsrCreated` like ?");
            searchSQL.append(" or `DateCreated` like ?");
            searchSQL.append(" or `UsrModif` like ?");
            searchSQL.append(" or `DateModif` like ?)");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and ");
                searchSQL.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individalColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }

        query.append(searchSQL);

        if (!StringUtil.isNullOrEmpty(column)) {
            query.append(" order by `").append(column).append("` ").append(dir);
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

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());
                Statement stm = connection.createStatement();) {

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

            try (ResultSet resultSet = preStat.executeQuery();
                    ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()");) {
                //gets the data
                while (resultSet.next()) {
                    objectList.add(this.loadFromResultSet(resultSet));
                }

                //get the total number of rows
                int nrTotalRows = 0;

                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
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

            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        response.setResultMessage(msg);
        response.setDataList(objectList);
        return response;
    }

    @Override
    public AnswerItem<Integer> insertFromTCDep(long queueId, String env, String country, String tag, String test, String testcase) {
        AnswerItem ans = new AnswerItem<>();
        MessageEvent msg = null;
        final String query = "INSERT INTO testcaseexecutionqueuedep(ExeQueueID, Environment, Country, Tag, Type, DepTest, DepTestCase, DepEvent, Status) "
                + "SELECT ?, ?, ?, ?, Type, DepTest, DepTestCase, DepEvent, 'WAITING' FROM testcasedep "
                + "WHERE Test=? and TestCase=? and active='Y';";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.test : " + test);
            LOG.debug("SQL.param.testcase : " + testcase);
        }

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query)) {
            TestCaseExecutionQueueDep ao = null;
            // Prepare and execute query
            int i = 1;
            preStat.setLong(i++, queueId);
            preStat.setString(i++, env);
            preStat.setString(i++, country);
            preStat.setString(i++, tag);
            preStat.setString(i++, test);
            preStat.setString(i++, testcase);
            try {
                int rs = preStat.executeUpdate();
                ans.setItem(rs);
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME).resolveDescription("OPERATION", "READ_BY_KEY");
                // Set the final message
            } catch (Exception e) {
                LOG.error("Unable to execute query : " + e.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION", e.toString());
            }
        } catch (Exception e) {
            LOG.error("Unable to insert from table: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION", e.toString());
        } finally {
            ans.setResultMessage(msg);
        }
        return ans;
    }

    @Override
    public AnswerItem<Integer> insertFromQueueExeDep(long queueId, long fromQueueId) {
        AnswerItem ans = new AnswerItem<>();
        MessageEvent msg = null;
        final String query = "INSERT INTO testcaseexecutionqueuedep(ExeQueueID, Environment, Country, Tag, Type, DepTest, DepTestCase, DepEvent, Status, ReleaseDate, Comment, ExeId) "
                + "SELECT ?, Environment, Country, Tag, Type, DepTest, DepTestCase, DepEvent, Status, ReleaseDate, Comment, ExeId FROM testcaseexecutionqueuedep "
                + "WHERE ExeQueueID=?;";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.test : " + fromQueueId);
        }

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query)) {
            TestCaseExecutionQueueDep ao = null;
            // Prepare and execute query
            int i = 1;
            preStat.setLong(i++, queueId);
            preStat.setLong(i++, fromQueueId);
            try {
                int rs = preStat.executeUpdate();
                ans.setItem(rs);
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME).resolveDescription("OPERATION", "READ_BY_KEY");
                // Set the final message
            } catch (Exception e) {
                LOG.error("Unable to execute query : " + e.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION", e.toString());
            }
        } catch (Exception e) {
            LOG.error("Unable to insert from table: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION", e.toString());
        } finally {
            ans.setResultMessage(msg);
        }
        return ans;
    }

    @Override
    public AnswerItem<Integer> updateStatusToRelease(String env, String Country, String tag, String type, String test, String testCase, String comment, long exeId) {
        AnswerItem<Integer> ans = new AnswerItem();
        MessageEvent msg = null;
        String query = "UPDATE `testcaseexecutionqueuedep` SET `Status` = 'RELEASED', `Comment` = ? , `ExeId` = ?, ReleaseDate = NOW(), DateModif = NOW() "
                + " WHERE `Status` = 'WAITING' and `Type` = ? and `DepTest` = ? and `DepTestCase` = ? and `Tag` = ? and `Environment` = ? and `Country` = ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query)) {
            // Prepare and execute query
            int i = 1;
            preStat.setString(i++, comment);
            preStat.setLong(i++, exeId);
            preStat.setString(i++, type);
            preStat.setString(i++, test);
            preStat.setString(i++, testCase);
            preStat.setString(i++, tag);
            preStat.setString(i++, env);
            preStat.setString(i++, Country);
            Integer resnb = preStat.executeUpdate();
            ans.setItem(resnb);

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME).resolveDescription("OPERATION", "UPDATE");
        } catch (Exception e) {
            LOG.error("Unable to update object: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION", e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        AnswerList answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<String>();

        StringBuilder query = new StringBuilder();

        query.append("SELECT distinct `");
        query.append(columnName);
        query.append("` as distinctValues FROM testcaseexecutionqueuedep ");

        searchSQL.append("WHERE 1=1 ");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (`Application` like ?");
            searchSQL.append(" or `Object` like ?");
            searchSQL.append(" or `Value` like ?");
            searchSQL.append(" or `ScreenshotFileName` like ?");
            searchSQL.append(" or `UsrCreated` like ?");
            searchSQL.append(" or `DateCreated` like ?");
            searchSQL.append(" or `UsrModif` like ?");
            searchSQL.append(" or `DateModif` like ?)");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and ");
                searchSQL.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individalColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }

        query.append(searchSQL);
        query.append(" order by `").append(columnName).append("` asc");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());
                Statement stm = connection.createStatement();) {

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

            try (ResultSet resultSet = preStat.executeQuery();
                    ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()");) {
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
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

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

    private TestCaseExecutionQueueDep loadFromResultSet(ResultSet rs) throws SQLException {
        Integer ID = ParameterParserUtil.parseIntegerParam(rs.getString("ID"), -1);
        String application = ParameterParserUtil.parseStringParam(rs.getString("Application"), "");
        String object = ParameterParserUtil.parseStringParam(rs.getString("Object"), "");
        String value = ParameterParserUtil.parseStringParam(rs.getString("Value"), "");
        String screenshotfilename = ParameterParserUtil.parseStringParam(rs.getString("ScreenshotFileName"), "");
        String usrcreated = ParameterParserUtil.parseStringParam(rs.getString("UsrCreated"), "");
        String datecreated = ParameterParserUtil.parseStringParam(rs.getString("DateCreated"), "");
        String usrmodif = ParameterParserUtil.parseStringParam(rs.getString("UsrModif"), "");
        String datemodif = ParameterParserUtil.parseStringParam(rs.getString("DateModif"), "");

//        return factoryApplicationObject.create(ID, application, object, value, screenshotfilename, usrcreated, datecreated, usrmodif, datemodif);
        return null;
    }
}
