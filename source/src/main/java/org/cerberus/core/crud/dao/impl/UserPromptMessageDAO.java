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
import org.cerberus.core.crud.entity.UserPrompt;
import org.cerberus.core.crud.entity.UserPromptMessage;
import org.cerberus.core.database.DatabaseSpring;
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

    @Override
    public boolean insertUserPromptMessage(UserPromptMessage msg) {
        boolean result = false;
        final String sql = "INSERT INTO " + TABLE + " (sessionID, role, message, usrCreated) VALUES (?, ?, ?, ?)";

        LOG.debug("SQL: {}", sql);

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                     ResultSet.CONCUR_READ_ONLY)) {

            int i = 1;
            preStat.setString(i++, msg.getSessionID());
            preStat.setString(i++, msg.getRole());
            preStat.setString(i++, msg.getMessage());
            preStat.setString(i++, msg.getUsrCreated());

            preStat.executeUpdate();

            ResultSet rs = preStat.getGeneratedKeys();
            if (rs.first()) {
                msg.setId(rs.getInt(1));
                result = true;
            }

        } catch (SQLException e) {
            LOG.error("Unable to insert UserPromptMessage: {}", e.getMessage(), e);
        }

        return result;
    }

    @Override
    public UserPromptMessage findById(int id) {
        UserPromptMessage msg = null;
        final String sql = "SELECT * FROM " + TABLE + " WHERE `id = ?";

        LOG.debug("SQL: {}", sql);

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                     ResultSet.CONCUR_READ_ONLY)) {

            preStat.setInt(1, id);
            ResultSet rs = preStat.executeQuery();

            if (rs.first()) {
                msg = loadFromResultSet(rs);
            }

        } catch (SQLException e) {
            LOG.error("Unable to find UserPromptMessage by ID: {}", e.getMessage(), e);
        }

        return msg;
    }

    @Override
    public boolean updateUserPromptMessage(UserPromptMessage msg) {
        boolean result = false;
        final String sql = "UPDATE " + TABLE + " SET sessionID = ?, role = ?, message = ?, dateCreated = ? WHERE `ìd` = ?";

        LOG.debug("SQL: {}", sql);

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(sql)) {

            int i = 1;
            preStat.setString(i++, msg.getSessionID());
            preStat.setString(i++, msg.getRole());
            preStat.setString(i++, msg.getMessage());
            preStat.setTimestamp(i++, msg.getDateCreated());
            preStat.setInt(i++, msg.getId());

            result = preStat.executeUpdate() > 0;

        } catch (SQLException e) {
            LOG.error("Unable to update UserPromptMessage: {}", e.getMessage(), e);
        }

        return result;
    }

    @Override
    public boolean deleteUserPromptMessage(int id) {
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

    @Override
    public List<UserPromptMessage> findBySessionID(String sessionID) {
        List<UserPromptMessage> messages = new ArrayList<>();
        final String sql = "SELECT * FROM " + TABLE + " WHERE sessionID = ? ORDER BY dateCreated ASC";

        LOG.debug("SQL: {}", sql);

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(sql)) {

            preStat.setString(1, sessionID);
            ResultSet rs = preStat.executeQuery();

            while (rs.next()) {
                messages.add(loadFromResultSet(rs));
            }

        } catch (SQLException e) {
            LOG.error("Unable to find messages by sessionID: {}", e.getMessage(), e);
        }

        return messages;
    }

    @Override
    public UserPromptMessage loadFromResultSet(ResultSet rs) throws SQLException {
        return UserPromptMessage.builder()
                .id(rs.getInt("id"))
                .sessionID(rs.getString("sessionID"))
                .role(rs.getString("role"))
                .message(rs.getString("message"))
                .usrCreated(rs.getString("usrCreated"))
                .dateCreated(rs.getTimestamp("dateCreated"))
                .usrModif(rs.getString("usrModif"))
                .dateModif(rs.getTimestamp("dateModif"))
                .build();
    }
}
