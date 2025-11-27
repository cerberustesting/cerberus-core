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
import org.cerberus.core.crud.dao.IUserPromptMessageDAO;
import org.cerberus.core.crud.entity.UserPromptMessage;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserPromptMessageDAO implements IUserPromptMessageDAO {

    @Autowired
    private DatabaseSpring databaseSpring;

    private static final Logger LOG = LogManager.getLogger(UserPromptMessageDAO.class);

    private static final String TABLE = "userpromptmessage";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public Answer create(UserPromptMessage userPromptMessage){
        MessageEvent msg = null;
        final String sql = "INSERT INTO " + TABLE + " (sessionID, role, message, tokens, cost, usrCreated) VALUES (?, ?, ?, ?, ?, ?)";

        LOG.debug("SQL: {}", sql);

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                     ResultSet.CONCUR_READ_ONLY)) {

            int i = 1;
            preStat.setString(i++, userPromptMessage.getSessionID());
            preStat.setString(i++, userPromptMessage.getRole());
            preStat.setString(i++, userPromptMessage.getMessage());
            preStat.setInt(i++, userPromptMessage.getTokens());
            preStat.setDouble(i++, userPromptMessage.getCost());
            preStat.setString(i++, userPromptMessage.getUsrCreated());

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
    public AnswerItem<UserPromptMessage> readByKey(Integer id){
        AnswerItem<UserPromptMessage> ans = new AnswerItem<>();
        UserPromptMessage userPromptMessage = null;
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        final String sql = "SELECT * FROM " + TABLE + " WHERE `id` = ?";

        LOG.debug("SQL: {}", sql);

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                     ResultSet.CONCUR_READ_ONLY)) {

            preStat.setInt(1, id);
            ResultSet rs = preStat.executeQuery();

            if (rs.first()) {
                userPromptMessage = loadFromResultSet(rs);
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", TABLE).replace("%OPERATION%", "SELECT"));
                ans.setItem(userPromptMessage);
            }

        } catch (SQLException e) {
            LOG.error("Unable to find UserPromptMessage by ID: {}", e.getMessage(), e);
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", e.toString()));
        }

        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public AnswerList<UserPromptMessage> readBySessionID(String sessionID) {
        AnswerList<UserPromptMessage> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        List<UserPromptMessage> userPromptList = new ArrayList<>();
        final String sql = "SELECT * FROM " + TABLE + " WHERE sessionID = ? ORDER BY dateCreated ASC";

        LOG.debug("SQL: {}", sql);

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(sql)) {

            preStat.setString(1, sessionID);
            ResultSet rs = preStat.executeQuery();

            while (rs.next()) {
                userPromptList.add(loadFromResultSet(rs));
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
                response = new AnswerList<>(userPromptList, nrTotalRows);
            } else if (userPromptList.size() <= 0) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                response = new AnswerList<>(userPromptList, nrTotalRows);
            } else {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", TABLE).replace("%OPERATION%", "SELECT"));
                response = new AnswerList<>(userPromptList, nrTotalRows);
            }

        } catch (SQLException e) {
            LOG.error("Unable to find messages by sessionID: {}", e.getMessage(), e);
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", e.toString()));
        }

        response.setResultMessage(msg);
        response.setDataList(userPromptList);
        return response;
    }

    @Override
    public boolean update(UserPromptMessage userPromptMessage) {
        boolean result = false;
        final String sql = "UPDATE " + TABLE + " SET sessionID = ?, role = ?, message = ?, tokens = ?, cost = ?, dateCreated = ? WHERE `ìd` = ?";

        LOG.debug("SQL: {}", sql);

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(sql)) {

            int i = 1;
            preStat.setString(i++, userPromptMessage.getSessionID());
            preStat.setString(i++, userPromptMessage.getRole());
            preStat.setString(i++, userPromptMessage.getMessage());
            preStat.setInt(i++, userPromptMessage.getTokens());
            preStat.setDouble(i++, userPromptMessage.getCost());
            preStat.setTimestamp(i++, userPromptMessage.getDateCreated());
            preStat.setInt(i++, userPromptMessage.getId());

            result = preStat.executeUpdate() > 0;

        } catch (SQLException e) {
            LOG.error("Unable to update UserPromptMessage: {}", e.getMessage(), e);
        }

        return result;
    }

    @Override
    public boolean delete(Integer id) {
        boolean result = false;
        final String sql = "DELETE FROM " + TABLE + " WHERE `id` = ?";

        LOG.debug("SQL: {}", sql);

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(sql)) {

            preStat.setInt(1, id);
            result = preStat.executeUpdate() > 0;

        } catch (SQLException e) {
            LOG.error("Unable to delete UserPromptMessage: {}", e.getMessage(), e);
        }

        return result;
    }

    private UserPromptMessage loadFromResultSet(ResultSet rs) throws SQLException {
        return UserPromptMessage.builder()
                .id(rs.getInt("id"))
                .sessionID(rs.getString("sessionID"))
                .role(rs.getString("role"))
                .message(rs.getString("message"))
                .tokens(rs.getInt("tokens"))
                .cost(rs.getDouble("cost"))
                .usrCreated(rs.getString("usrCreated"))
                .dateCreated(rs.getTimestamp("dateCreated"))
                .usrModif(rs.getString("usrModif"))
                .dateModif(rs.getTimestamp("dateModif"))
                .build();
    }
}
