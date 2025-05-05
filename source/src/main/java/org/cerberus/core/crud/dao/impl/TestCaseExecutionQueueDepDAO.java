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

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.core.crud.dao.ITestCaseExecutionQueueDepDAO;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionQueueDep;
import org.cerberus.core.crud.utils.RequestDbUtils;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.SqlUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

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

    private static final Logger LOG = LogManager.getLogger(TestCaseExecutionQueueDepDAO.class);

    private final String OBJECT_NAME = "TestCaseExecutionQueueDep";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public AnswerItem<TestCaseExecutionQueueDep> readByKey(long id) {
        AnswerItem<TestCaseExecutionQueueDep> ans = new AnswerItem<>();
        MessageEvent msg = null;

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement("SELECT * FROM `testcaseexecutionqueuedep` WHERE `ID` = ?")) {
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
        AnswerList<TestCaseExecutionQueueDep> ans = new AnswerList<>();
        MessageEvent msg = null;

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement("SELECT * FROM `testcaseexecutionqueuedep` WHERE `ExeID` = ?")) {
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
    public AnswerItem<Integer> readNbWaitingByExeQueueId(long exeQueueId) {
        AnswerItem<Integer> ans = new AnswerItem<>();
        MessageEvent msg = null;

        final String query = "SELECT ID FROM testcaseexecutionqueuedep WHERE `ExeQueueID` = ? and Status = 'WAITING';";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.exeQueueId : " + exeQueueId);
        }

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {
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
    public AnswerItem<Integer> readNbReleasedWithNOKByExeQueueId(long exeQueueId) {
        AnswerItem<Integer> ans = new AnswerItem<>();
        MessageEvent msg = null;

        final String query = "SELECT tce.controlstatus FROM testcaseexecutionqueuedep tcd "
                + "LEFT OUTER JOIN testcaseexecution tce ON tcd.exeid=tce.id "
                + "WHERE tcd.`ExeQueueID` = ? and tcd.Status = 'RELEASED' and tcd.Type = 'TCEXEENDOK'  and (tce.controlstatus is null or tce.controlstatus != 'OK');";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.exeQueueId : " + exeQueueId);
        }

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {
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
    public AnswerList<TestCaseExecutionQueueDep> getWaitingDepReadytoRelease() {
        AnswerList<TestCaseExecutionQueueDep> ans = new AnswerList<>();
        MessageEvent msg = null;
        final String query = "SELECT * from testcaseexecutionqueuedep where Type = 'TIMING' and Status ='WAITING' and DepDate is not NULL and NOW()>=DepDate ;";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {
            // Prepare and execute query
            ResultSet rs = preStat.executeQuery();
            try {
                List<TestCaseExecutionQueueDep> al = new ArrayList<>();
                while (rs.next()) {
                    al.add(loadFromResultSet(rs));
                }
                ans.setDataList(al);
                ans.setTotalRows(al.size());
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
    public AnswerList<Long> readExeQueueIdByExeId(long exeId) {
        AnswerList<Long> ans = new AnswerList<>();
        MessageEvent msg = null;

        final String query = "SELECT DISTINCT ExeQueueID FROM testcaseexecutionqueuedep WHERE `ExeID` = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.exeId : " + exeId);
        }

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {
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
    public AnswerList<Long> readExeQueueIdByQueueId(long queueId) {
        AnswerList<Long> ans = new AnswerList<>();
        MessageEvent msg = null;

        final String query = "SELECT DISTINCT ExeQueueID FROM testcaseexecutionqueuedep WHERE `QueueID` = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.queueId : " + queueId);
        }

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {
            // Prepare and execute query
            preStat.setLong(1, queueId);
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
    public AnswerList<TestCaseExecutionQueueDep> readByExeQueueId(long exeQueueId) {
        AnswerList<TestCaseExecutionQueueDep> ans = new AnswerList<>();
        MessageEvent msg = null;

        final String query = "SELECT * FROM testcaseexecutionqueuedep WHERE `ExeQueueID` = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.queueId : " + exeQueueId);
        }

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {
            // Prepare and execute query
            preStat.setLong(1, exeQueueId);
            ResultSet rs = preStat.executeQuery();
            try {
                List<TestCaseExecutionQueueDep> al = new ArrayList<>();
                while (rs.next()) {
                    al.add(loadFromResultSet(rs));
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
        AnswerList<TestCaseExecutionQueueDep> response = new AnswerList<>();
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

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
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

        if (!StringUtil.isEmptyOrNull(column)) {
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

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); Statement stm = connection.createStatement();) {

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
    public AnswerItem<Integer> insertFromTestCaseDep(long queueId, String env, String country, String tag, String test, String testcase) {
        AnswerItem<Integer> ans = new AnswerItem<>();
        MessageEvent msg = null;
        final String query = "INSERT INTO testcaseexecutionqueuedep(ExeQueueID, Environment, Country, Tag, Type, DepTest, DepTestCase, DepTCDelay, DepEvent, Status) "
                + "SELECT ?, ?, ?, ?, Type, DependencyTest DepTest, DependencyTestcase DepTestCase, DependencyTCDelay DepTCDelay, DependencyEvent DepEvent, 'WAITING' FROM testcasedep "
                + "WHERE Test=? and TestCase=? and IsActive=1;";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.test : " + test);
            LOG.debug("SQL.param.testcase : " + testcase);
        }

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {
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
    public AnswerItem<Long> create(TestCaseExecutionQueueDep object) {
        MessageEvent msg;
        AnswerItem<Long> ans = new AnswerItem<>();
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcaseexecutionqueuedep(ExeQueueID, Environment, Country, Tag, Type, DepTest, DepTestCase, DepTCDelay, DepEvent, Status, Comment, ExeId, QueueId, UsrCreated)");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.exeid : {}", object.getQueueId());
        LOG.debug("SQL.param.queueid : {}", object.getExeId());

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            preStat.setLong(i++, object.getExeQueueId());
            preStat.setString(i++, object.getEnvironment());
            preStat.setString(i++, object.getCountry());
            preStat.setString(i++, object.getTag());
            preStat.setString(i++, object.getType());
            preStat.setString(i++, object.getDepTest());
            preStat.setString(i++, object.getDepTestCase());
            preStat.setInt(i++, object.getDepTCDelay());
            preStat.setString(i++, object.getDepEvent());
            preStat.setString(i++, object.getStatus());
            preStat.setString(i++, object.getComment());
            preStat.setLong(i++, object.getExeId());
            preStat.setLong(i++, object.getQueueId());
            preStat.setString(i++, object.getUsrCreated());

            preStat.executeUpdate();
            try (ResultSet resultSet = preStat.getGeneratedKeys()) {
                if (resultSet.first()) {
                    LOG.debug("Inserted Queue Dep id : " + resultSet.getLong(1));
                    ans.setItem(resultSet.getLong(1));
                }
            }
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());

            if (exception.getSQLState().equals(SQL_DUPLICATED_CODE)) { //23000 is the sql state for duplicate entries
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_DUPLICATE);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT").replace("%REASON%", exception.toString()));
            } else {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            }
        }
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public AnswerItem<Integer> insertFromExeQueueIdDep(long queueId, long fromExeQueueId) {
        AnswerItem<Integer> ans = new AnswerItem<>();
        MessageEvent msg = null;
        final String query = "INSERT INTO testcaseexecutionqueuedep (ExeQueueID, Environment, Country, Tag, Type, DepTest, DepTestCase, DepEvent, Status, ReleaseDate, Comment, ExeId, QueueId) "
                + "SELECT ?, Environment, Country, Tag, Type, DepTest, DepTestCase, DepEvent, Status, ReleaseDate, Comment, ExeId, QueueId FROM testcaseexecutionqueuedep "
                + "WHERE ExeQueueID=?;";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.queueId : " + queueId);
            LOG.debug("SQL.param.fromQueueId : " + fromExeQueueId);
        }

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {
            TestCaseExecutionQueueDep ao = null;
            // Prepare and execute query
            int i = 1;
            preStat.setLong(i++, queueId);
            preStat.setLong(i++, fromExeQueueId);
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
    public AnswerItem<Integer> insertNewTimingDep(String env, String Country, String tag, String test, String testCase, long exeID) {
        AnswerItem<Integer> ans = new AnswerItem<>();
        MessageEvent msg = null;

        String query = "INSERT INTO testcaseexecutionqueuedep (ExeQueueId, Environment, Country, Tag, `Type` , DepDate, Status, Comment) "
                + " SELECT ExeQueueId, Environment, Country , Tag , 'TIMING', DATE_ADD(NOW(), INTERVAL DepTCDelay MINUTE) , 'WAITING', concat('Extra ', DepTCDelay, ' min delay following end of execution ', ?, ' of ', DepTest, '-', DepTestCase) "
                + "  FROM testcaseexecutionqueuedep "
                + "  WHERE `Status` = 'WAITING' and `Type` in ('TCEXEEND','TCEXEENDOK') and `DepTest` = ? and `DepTestCase` = ? "
                + "   and `Tag` = ? and `Environment` = ? and `Country` = ? and DepTCDelay > 0;";

        // Debug message on SQL.
        LOG.debug("SQL : " + query);
        LOG.debug("SQL.param.id : " + String.valueOf(exeID));

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {
            // Prepare and execute query
            int i = 1;
            preStat.setLong(i++, exeID);
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
    public AnswerItem<Integer> updateStatusToRelease(String env, String Country, String tag, String test, String testCase, String comment, long exeId, long queueId) {
        AnswerItem<Integer> ans = new AnswerItem<>();
        MessageEvent msg = null;
        String query = "UPDATE `testcaseexecutionqueuedep` SET `Status` = 'RELEASED', `Comment` = ? , `ExeId` = ?, `QueueId` = ?, ReleaseDate = NOW(), DateModif = NOW() "
                + " WHERE `Status` = 'WAITING' and `Type` in ('TCEXEENDOK','TCEXEEND') and `DepTest` = ? and `DepTestCase` = ? and `Tag` = ? and `Environment` = ? and `Country` = ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {
            // Prepare and execute query
            int i = 1;
            preStat.setString(i++, comment);
            preStat.setLong(i++, exeId);
            preStat.setLong(i++, queueId);
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
    public AnswerItem<Integer> updateStatusToRelease(long id, String comment, long exeId, long queueId) {
        AnswerItem<Integer> ans = new AnswerItem<>();
        MessageEvent msg = null;
        String query = "UPDATE `testcaseexecutionqueuedep` SET `Status` = 'RELEASED', ExeId = ?, QueueId = ?, `Comment` = ? , ReleaseDate = NOW(), DateModif = NOW() "
                + " WHERE `Status` = 'WAITING' and id = ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {
            // Prepare and execute query
            int i = 1;
            preStat.setLong(i++, exeId);
            preStat.setLong(i++, queueId);
            preStat.setString(i++, comment);
            preStat.setLong(i++, id);
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
    public AnswerItem<Integer> updateStatusToRelease(long id) {
        AnswerItem<Integer> ans = new AnswerItem<>();
        MessageEvent msg = null;
        String query = "UPDATE `testcaseexecutionqueuedep` SET `Status` = 'RELEASED', `Comment` = ? , ReleaseDate = NOW(), DateModif = NOW() "
                + " WHERE `Status` = 'WAITING' and id = ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {
            // Prepare and execute query
            int i = 1;
            preStat.setString(i++, "Timing was reached.");
            preStat.setLong(i++, id);
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
        AnswerList<String> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();

        query.append("SELECT distinct `");
        query.append(columnName);
        query.append("` as distinctValues FROM testcaseexecutionqueuedep ");

        searchSQL.append("WHERE 1=1 ");

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
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

    @Override
    public HashMap<TestCaseExecution, List<TestCaseExecutionQueueDep>> readDependenciesByTestCaseExecution(List<TestCaseExecution> testCaseExecutions) throws CerberusException {

        HashMap<TestCaseExecution, List<TestCaseExecutionQueueDep>> hashMap = new HashMap<>();
        if (CollectionUtils.isEmpty(testCaseExecutions)) {
            return hashMap;
        }

        StringBuilder query = new StringBuilder(
                "SELECT * FROM testcaseexecutionqueuedep "
                + "where exeQueueID in (");
        testCaseExecutions.forEach(tc -> query.append("?,"));
        query.setLength(query.length() - 1);
        query.append(")");

        List<TestCaseExecutionQueueDep> lst = RequestDbUtils.executeQueryList(databaseSpring, query.toString(),
                ps -> {
                    int idx = 1;
                    for (TestCaseExecution tc : testCaseExecutions) {
                        ps.setLong(idx++, tc.getQueueID());
                    }
                },
                rs -> loadFromResultSet(rs));

        Map<Long, TestCaseExecution> hashMapTC = testCaseExecutions.stream().collect(Collectors.toMap(tce -> tce.getQueueID(), tce -> tce));

        for (TestCaseExecutionQueueDep tce : lst) {
            hashMap
                    .computeIfAbsent(
                            hashMapTC.get(tce.getExeQueueId()),
                            k -> new ArrayList<>())
                    .add(tce);
        }

        return hashMap;
    }

    private TestCaseExecutionQueueDep loadFromResultSet(ResultSet rs) throws SQLException {
        Long id = ParameterParserUtil.parseLongParam(rs.getString("id"), -1);
        Long exeQueueID = ParameterParserUtil.parseLongParam(rs.getString("ExeQueueID"), -1);
        String environment = ParameterParserUtil.parseStringParam(rs.getString("Environment"), "");
        String country = ParameterParserUtil.parseStringParam(rs.getString("Country"), "");
        String tag = ParameterParserUtil.parseStringParam(rs.getString("Tag"), "");
        String type = ParameterParserUtil.parseStringParam(rs.getString("Type"), "");
        String depTest = ParameterParserUtil.parseStringParam(rs.getString("DepTest"), "");
        String depEvent = ParameterParserUtil.parseStringParam(rs.getString("DepEvent"), "");
        String depTestCase = ParameterParserUtil.parseStringParam(rs.getString("DepTestCase"), "");
        Integer depTCDelay = rs.getInt("DepTCDelay");
        Timestamp depDate = rs.getTimestamp("DepDate");
        String status = ParameterParserUtil.parseStringParam(rs.getString("Status"), "");
        Timestamp releaseDate = rs.getTimestamp("ReleaseDate");
        String comment = ParameterParserUtil.parseStringParam(rs.getString("Comment"), "");
        Long exeID = ParameterParserUtil.parseLongParam(rs.getString("ExeID"), -1);
        Long queueID = ParameterParserUtil.parseLongParam(rs.getString("QueueID"), -1);
        String usrCreated = ParameterParserUtil.parseStringParam(rs.getString("UsrCreated"), "");
        Timestamp dateCreated = rs.getTimestamp("DateCreated");
        String usrModif = ParameterParserUtil.parseStringParam(rs.getString("UsrModif"), "");
        Timestamp dateModif = rs.getTimestamp("DateModif");

        return TestCaseExecutionQueueDep.builder()
                .id(id).exeQueueId(exeQueueID).environment(environment).country(country).tag(tag)
                .type(type).depTest(depTest).depEvent(depEvent).depTestCase(depTestCase).depTCDelay(depTCDelay).depDate(depDate)
                .status(status).releaseDate(releaseDate).comment(comment).exeId(exeID).queueId(queueID)
                .usrCreated(usrCreated).dateCreated(dateCreated).usrModif(usrModif).dateModif(dateModif).build();

    }
}
