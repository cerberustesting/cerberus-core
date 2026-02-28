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
import org.cerberus.core.crud.dao.IUserPromptDAO;
import org.cerberus.core.crud.entity.*;
import org.cerberus.core.crud.entity.stats.UserPromptStats;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.SqlUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DAO for UserPrompt entity.
 */
@Repository
public class UserPromptDAO implements IUserPromptDAO {

    @Autowired
    private DatabaseSpring databaseSpring;

    private static final Logger LOG = LogManager.getLogger(UserPromptDAO.class);

    private static final String TABLE = "userprompt";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public Answer create(UserPrompt userPrompt) {
        MessageEvent msg = null;
        final String sql = "INSERT INTO " + TABLE + " (login, sessionID, iaModel, iaMaxTokens, type, title, totalCalls, totalInputTokens, totalOutputTokens, totalCost, usrCreated) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LOG.debug("SQL: {}", sql);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY)) {

            int i = 1;
            preStat.setString(i++, userPrompt.getLogin());
            preStat.setString(i++, userPrompt.getSessionID());
            preStat.setString(i++, userPrompt.getIaModel());
            preStat.setInt(i++, userPrompt.getIaMaxTokens());
            preStat.setString(i++, userPrompt.getType());
            preStat.setString(i++, userPrompt.getTitle());
            preStat.setInt(i++, userPrompt.getTotalCalls());
            preStat.setInt(i++, userPrompt.getTotalInputTokens());
            preStat.setInt(i++, userPrompt.getTotalOutputTokens());
            preStat.setDouble(i++, userPrompt.getTotalCost());
            preStat.setString(i++, userPrompt.getUsrCreated());

            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", TABLE).replace("%OPERATION%", "INSERT"));

        } catch (SQLException e) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_DUPLICATE);
            msg.setDescription(msg.getDescription().replace("%ITEM%", TABLE).replace("%OPERATION%", "INSERT").replace("%REASON%", e.toString()));
        }

        return new Answer(msg);
    }

    @Override
    public AnswerItem<UserPrompt> readByKey(Integer id) {
        AnswerItem<UserPrompt> ans = new AnswerItem<>();
        UserPrompt prompt = null;
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        final String sql = "SELECT * FROM " + TABLE + " WHERE `id` = ?";

        LOG.debug("SQL: {}", sql);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY)) {

            preStat.setInt(1, id);
            ResultSet rs = preStat.executeQuery();

            if (rs.first()) {
                prompt = loadFromResultSet(rs);
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", TABLE).replace("%OPERATION%", "SELECT"));
                ans.setItem(prompt);
            } else {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
            }

        } catch (SQLException e) {
            LOG.error("Unable to find UserPrompt by ID: {}", e.getMessage(), e);
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", e.toString()));

        }

        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public AnswerList<UserPrompt> readByUser(String login) {
        AnswerList<UserPrompt> ans = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        List<UserPrompt> userPromptList = new ArrayList<>();

        final String sql = "SELECT * FROM " + TABLE + " WHERE login = ? ORDER BY dateCreated DESC";

        LOG.debug("SQL: {}", sql);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY)) {

            preStat.setString(1, login);
            ResultSet rs = preStat.executeQuery();

            while (rs.next()) {
                userPromptList.add(this.loadFromResultSet(rs));
            }

            //get the total number of rows
            rs = preStat.executeQuery("SELECT FOUND_ROWS()");
            int nrTotalRows = 0;

            if (rs != null && rs.next()) {
                nrTotalRows = rs.getInt(1);
            }

            if (userPromptList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                LOG.error("Partial Result in the query.");
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                ans = new AnswerList<>(userPromptList, nrTotalRows);
            } else if (userPromptList.size() <= 0) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                ans = new AnswerList<>(userPromptList, nrTotalRows);
            } else {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", TABLE).replace("%OPERATION%", "SELECT"));
                ans = new AnswerList<>(userPromptList, nrTotalRows);
            }

        } catch (SQLException e) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", e.toString()));
        }

        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public AnswerItem<UserPrompt> readByUserSessionID(String login, String sessionID) {
        AnswerItem<UserPrompt> ans = new AnswerItem<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        UserPrompt prompt = null;

        final String sql = "SELECT * FROM " + TABLE + " WHERE login = ? AND sessionID = ? ORDER BY dateCreated DESC";

        LOG.debug("SQL: {}", sql);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY)) {

            preStat.setString(1, login);
            preStat.setString(2, sessionID);
            ResultSet rs = preStat.executeQuery();

            if (rs.first()) {
                prompt = loadFromResultSet(rs);
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", TABLE).replace("%OPERATION%", "SELECT"));
                ans.setItem(prompt);
            } else {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
            }

        } catch (SQLException e) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", e.toString()));
        }

        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public AnswerList<UserPrompt> readByCriteria(int start, int amount, String colName, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        AnswerList<UserPrompt> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        List<UserPrompt> userPromptList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        final StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS up.*, COALESCE(SUM(upm.cost), 0) AS totalCost FROM userprompt up ");
        query.append("LEFT JOIN userpromptmessage upm ON up.sessionID = upm.sessionID");

        searchSQL.append(" where 1=1 ");

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (`login` like ?");
            searchSQL.append(" or `SessionID` like ?");
            searchSQL.append(" or `iaModel` like ?");
            searchSQL.append(" or `iaMaxTokens` like ?");
            searchSQL.append(" or `type` like ?");
            searchSQL.append(" or `title` like ?");
            searchSQL.append(" or `totalCalls` like ?");
            searchSQL.append(" or `totalInputTokens` like ?");
            searchSQL.append(" or `totalOutputTokens` like ?");
            searchSQL.append(" or `totalCost` like ?");
            searchSQL.append(" or `UsrCreated` like ? ");
            searchSQL.append(" or `DateCreated` like ? )");
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

        query.append(" GROUP BY up.id ");

        if (!StringUtil.isEmptyOrNull(colName)) {
            query.append("order by `").append(colName).append("` ").append(dir);
        } else {
            query.append("order by `ID` desc");
        }
        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        // Debug message on SQL.
        LOG.debug("SQL : " + query.toString());

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
                        userPromptList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (userPromptList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList<>(userPromptList, nrTotalRows);
                    } else if (userPromptList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList<>(userPromptList, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", TABLE).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList<>(userPromptList, nrTotalRows);
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
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
        response.setDataList(userPromptList);
        return response;
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        AnswerList<String> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();

        query.append("SELECT distinct ");
        query.append(columnName);
        query.append(" as distinctValues FROM userprompt ");

        searchSQL.append("WHERE 1=1");

        if (!StringUtil.isEmptyOrNull(searchParameter)) {
            searchSQL.append(" and (`login` like ?");
            searchSQL.append(" or `SessionID` like ?");
            searchSQL.append(" or `iaModel` like ?");
            searchSQL.append(" or `iaMaxTokens` like ?");
            searchSQL.append(" or `type` like ?");
            searchSQL.append(" or `title` like ?");
            searchSQL.append(" or `totalCalls` like ?");
            searchSQL.append(" or `totalInputTokens` like ?");
            searchSQL.append(" or `totalOutputTokens` like ?");
            searchSQL.append(" or `totalCost` like ?");
            searchSQL.append(" or `UsrCreated` like ? ");
            searchSQL.append(" or `DateCreated` like ? )");
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
        query.append(" order by ").append(columnName).append(" asc");

        // Debug message on SQL.
        LOG.debug("SQL : " + query.toString());

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); Statement stm = connection.createStatement();) {

            int i = 1;
            if (!StringUtil.isEmptyOrNull(searchParameter)) {
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
                preStat.setString(i++, "%" + searchParameter + "%");
            }
            for (String individualColumnSearchValue : individalColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }

            try (ResultSet resultSet = preStat.executeQuery(); ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()");) {

                //gets the data
                while (resultSet.next()) {
                    distinctValues.add(resultSet.getString("distinctValues") == null ? "" : resultSet.getString("distinctValues"));
                }

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
                    msg.setDescription(msg.getDescription().replace("%ITEM%", TABLE).replace("%OPERATION%", "SELECT"));
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
    public boolean delete(Integer id) {
        boolean result = false;
        final String sql = "DELETE FROM " + TABLE + " WHERE `id` = ?";

        LOG.debug("SQL: {}", sql);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(sql)) {

            preStat.setInt(1, id);
            result = preStat.executeUpdate() > 0;

        } catch (SQLException e) {
            LOG.error("Unable to delete UserPrompt: {}", e.getMessage(), e);
        }

        return result;
    }

    @Override
    public boolean update(UserPrompt userPrompt) {
        boolean result = false;
        final String sql = "UPDATE " + TABLE + " SET "
                + "`login` = ?, `sessionID` = ?, `iaModel` = ?, `iaMaxTokens` = ?, `type` = ?, `title` = ?, `usrModif` = ?, `dateModif` = ? "
                + "WHERE `id` = ?";

        LOG.debug("SQL: {}", sql);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(sql)) {

            int i = 1;
            preStat.setString(i++, userPrompt.getLogin());
            preStat.setString(i++, userPrompt.getSessionID());
            preStat.setString(i++, userPrompt.getIaModel());
            preStat.setInt(i++, userPrompt.getIaMaxTokens());
            preStat.setString(i++, userPrompt.getType());
            preStat.setString(i++, userPrompt.getTitle());
            preStat.setString(i++, userPrompt.getUsrModif());
            preStat.setTimestamp(i++, userPrompt.getDateModif());
            preStat.setInt(i++, userPrompt.getId());

            result = preStat.executeUpdate() > 0;

        } catch (SQLException e) {
            LOG.error("Unable to update UserPrompt: {}", e.getMessage(), e);
        }

        return result;
    }

    @Override
    public boolean incrementUsage(String user, String aiSessionID, Integer inputTokens, Integer outputTokens, Double cost) {
        boolean result = false;
        final String sql = "UPDATE " + TABLE + " SET "
                + "`totalCalls` = `totalCalls` + 1, `totalInputTokens` = `totalInputTokens` + ?, `totalOutputTokens` = `totalOutputTokens` + ?, `totalCost` = `totalCost` + ?, `usrModif` = ?, `dateModif` = ? "
                + "WHERE `sessionID` = ?";

        LOG.debug("SQL: {}", sql);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(sql)) {

            int i = 1;
            preStat.setInt(i++, inputTokens != null ? inputTokens : 0);
            preStat.setInt(i++, outputTokens != null ? outputTokens : 0);
            preStat.setDouble(i++, cost != null ? cost : 0.0);
            preStat.setString(i++, user);
            preStat.setTimestamp(i++, new Timestamp(System.currentTimeMillis()));
            preStat.setString(i++, aiSessionID);

            result = preStat.executeUpdate() > 0;

        } catch (SQLException e) {
            LOG.error("Unable to update UserPrompt: {}", e.getMessage(), e);
        }

        return result;
    }

    @Override
    public AnswerItem<UserPromptStats> readSumByPeriod(Timestamp startDate, Timestamp endDate, String user) {
        AnswerItem<UserPromptStats> ans = new AnswerItem<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);

        String query
                = "SELECT "
                + "SUM(totalInputTokens) AS totalInput, "
                + "SUM(totalOutputTokens) AS totalOutput, "
                + "SUM(totalCost) AS totalCost "
                + "FROM " + TABLE + " "
                + "WHERE DateCreated BETWEEN ? AND ?";

        if (!user.equals("ALL")) {
            query += " AND UsrCreated = ?";
        }

        LOG.debug("SQL : {}", query);

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {

            preStat.setTimestamp(1, startDate);
            preStat.setTimestamp(2, endDate);
            if (!user.equals("ALL")) {
                preStat.setString(3, user);
            }

            try (ResultSet rs = preStat.executeQuery()) {
                if (rs.next()) {
                    UserPromptStats stats = new UserPromptStats(
                            rs.getInt("totalInput"),
                            rs.getInt("totalOutput"),
                            rs.getDouble("totalCost")
                    );
                    ans.setItem(stats);
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK)
                            .resolveDescription("ITEM", TABLE)
                            .resolveDescription("OPERATION", "SELECT");
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                }
            }
        } catch (SQLException e) {
            LOG.error("Unable to execute query : " + e.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED)
                    .resolveDescription("DESCRIPTION", e.toString());
        }

        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public AnswerList<UserPromptStats> readUsageByDay(Timestamp startDate, Timestamp endDate, String user) {
        AnswerList<UserPromptStats> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);

        StringBuilder query = new StringBuilder();
        query.append("SELECT DATE(DateCreated) AS day, ")
                .append("SUM(totalInputTokens) AS totalInput, ")
                .append("SUM(totalOutputTokens) AS totalOutput, ")
                .append("SUM(totalCost) AS totalCost FROM ")
                .append(TABLE)
                .append(" WHERE DateCreated BETWEEN ? AND ? ");

        if (!"ALL".equals(user)) {
            query.append("AND UsrCreated = ? ");
        }

        query.append("GROUP BY DATE(DateCreated) ")
                .append("ORDER BY DATE(DateCreated) ASC");

        LOG.debug("SQL : {}", query);

        try (Connection connection = databaseSpring.connect(); PreparedStatement ps = connection.prepareStatement(query.toString())) {

            ps.setTimestamp(1, startDate);
            ps.setTimestamp(2, endDate);
            if (!"ALL".equals(user)) {
                ps.setString(3, user);
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<UserPromptStats> list = new ArrayList<>();
                while (rs.next()) {
                    String day = rs.getDate("day").toString();
                    list.add(new UserPromptStats(
                            rs.getInt("totalInput"),
                            rs.getInt("totalOutput"),
                            rs.getDouble("totalCost"),
                            day,
                            day
                    ));
                }
                answer.setDataList(list);
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK)
                        .resolveDescription("ITEM", TABLE)
                        .resolveDescription("OPERATION", "SELECT");
            }

        } catch (SQLException e) {
            LOG.error("Unable to execute query : " + e.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED)
                    .resolveDescription("DESCRIPTION", e.toString());
        }

        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerItem<UserPromptStats> readStats(String fromDate, String toDate, String user) {
        AnswerItem<UserPromptStats> ans = new AnswerItem<>();
        MessageEvent msg;

        StringBuilder sql = new StringBuilder(
                "SELECT "
                + "  COALESCE(SUM(up.totalInputTokens), 0) AS totalInputTokens, "
                + "  COALESCE(SUM(up.totalOutputTokens), 0) AS totalOutputTokens, "
                + "  COUNT(DISTINCT up.login) AS totalUsers, "
                + "  COUNT(DISTINCT up.sessionID) AS totalSessions, "
                + "  COALESCE(SUM(upm.cost), 0) AS totalCost "
                + "FROM userprompt up "
                + "LEFT JOIN userpromptmessage upm ON up.sessionID = upm.sessionID "
                + "WHERE up.DateCreated > ? AND up.DateCreated <= ?"
        );

        boolean hasUserFilter = (user != null && !user.trim().isEmpty());
        if (hasUserFilter) {
            sql.append(" AND up.login = ?");
        }
        LOG.debug("SQL: {}", sql);

        UserPromptStats stats = null;

        LOG.debug("SQL : {}", sql);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement ps = connection.prepareStatement(sql.toString())) {

            ps.setString(1, fromDate);
            ps.setString(2, toDate);

            if (hasUserFilter) {
                ps.setString(3, user);
            }

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                stats = UserPromptStats.builder()
                        .totalInputTokens(rs.getInt("totalInputTokens"))
                        .totalOutputTokens(rs.getInt("totalOutputTokens"))
                        .totalUsers(rs.getInt("totalUsers"))
                        .totalSessions(rs.getInt("totalSessions"))
                        .totalCost(rs.getDouble("totalCost"))
                        .fromDate(fromDate)
                        .toDate(toDate)
                        .build();

                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Stats").replace("%OPERATION%", "SELECT"));
            } else {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
            }

        } catch (SQLException e) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", e.toString()));
        }

        ans.setItem(stats);
        ans.setResultMessage(msg);
        return ans;
    }

    private UserPrompt loadFromResultSet(ResultSet rs) throws SQLException {
        return UserPrompt.builder()
                .id(rs.getInt("id"))
                .login(rs.getString("login"))
                .sessionID(rs.getString("sessionID"))
                .iaModel(rs.getString("iaModel"))
                .iaMaxTokens(rs.getInt("iaMaxTokens"))
                .type(rs.getString("type"))
                .title(rs.getString("title"))
                .totalCalls(rs.getInt("totalCalls"))
                .totalInputTokens(rs.getInt("totalInputTokens"))
                .totalOutputTokens(rs.getInt("totalOutputTokens"))
                .totalCost(rs.getDouble("totalCost"))
                .usrCreated(rs.getString("usrCreated"))
                .dateCreated(rs.getTimestamp("dateCreated"))
                .usrModif(rs.getString("usrModif"))
                .dateModif(rs.getTimestamp("dateModif"))
                .build();
    }
}
