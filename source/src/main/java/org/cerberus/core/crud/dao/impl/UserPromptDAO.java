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
import org.cerberus.core.crud.entity.UserPrompt;
import org.cerberus.core.crud.entity.UserPromptMessage;
import org.cerberus.core.database.DatabaseSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for UserPrompt entity.
 */
@Repository
public class UserPromptDAO implements IUserPromptDAO {

    @Autowired
    private DatabaseSpring databaseSpring;

    private static final Logger LOG = LogManager.getLogger(UserPromptDAO.class);

    private static final String TABLE = "userprompt";

    @Override
    public boolean insertUserPrompt(UserPrompt userPrompt) {
        boolean result = false;
        final String sql = "INSERT INTO " + TABLE + " (login, sessionID, iaModel, iaMaxTokens, title, usrCreated) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        LOG.debug("SQL: {}", sql);

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                     ResultSet.CONCUR_READ_ONLY)) {

            int i = 1;
            preStat.setString(i++, userPrompt.getLogin());
            preStat.setString(i++, userPrompt.getSessionID());
            preStat.setString(i++, userPrompt.getIaModel());
            preStat.setInt(i++, userPrompt.getIaMaxTokens());
            preStat.setString(i++, userPrompt.getTitle());
            preStat.setString(i++, userPrompt.getUsrCreated());

            preStat.executeUpdate();

            ResultSet rs = preStat.getGeneratedKeys();
            if (rs.first()) {
                userPrompt.setId(rs.getInt(1));
                result = true;
            }

        } catch (SQLException e) {
            LOG.error("Unable to insert UserPrompt: {}", e.getMessage(), e);
        }

        return result;
    }

    @Override
    public UserPrompt findUserPromptById(int id) {
        UserPrompt prompt = null;
        final String sql = "SELECT * FROM " + TABLE + " WHERE `id` = ?";

        LOG.debug("SQL: {}", sql);

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                     ResultSet.CONCUR_READ_ONLY)) {

            preStat.setInt(1, id);
            ResultSet rs = preStat.executeQuery();

            if (rs.first()) {
                prompt = loadFromResultSet(rs);
            }

        } catch (SQLException e) {
            LOG.error("Unable to find UserPrompt by ID: {}", e.getMessage(), e);
        }

        return prompt;
    }

    @Override
    public UserPrompt findUserPromptByUserSessionID(String login, String sessionID){
        UserPrompt prompt = null;
        final String sql = "SELECT * FROM " + TABLE + " WHERE login = ? AND sessionID = ?";

        LOG.debug("SQL: {}", sql);

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                     ResultSet.CONCUR_READ_ONLY)) {

            preStat.setString(1, login);
            preStat.setString(2, sessionID);
            ResultSet rs = preStat.executeQuery();

            if (rs.first()) {
                prompt = loadFromResultSet(rs);
            }

        } catch (SQLException e) {
            LOG.error("Unable to find UserPrompt by sessionID: {}", e.getMessage(), e);
        }

        return prompt;
    };

    @Override
    public boolean deleteUserPrompt(int id) {
        boolean result = false;
        final String sql = "DELETE FROM " + TABLE + " WHERE `id` = ?";

        LOG.debug("SQL: {}", sql);

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(sql)) {

            preStat.setInt(1, id);
            result = preStat.executeUpdate() > 0;

        } catch (SQLException e) {
            LOG.error("Unable to delete UserPrompt: {}", e.getMessage(), e);
        }

        return result;
    }

    @Override
    public boolean updateUserPrompt(UserPrompt userPrompt) {
        boolean result = false;
        final String sql = "UPDATE " + TABLE + " SET " +
                "`login` = ?, `sessionID` = ?, `iaModel` = ?, `iaMaxTokens` = ?, `title` = ?, `usrModif` = ?, `dateModif` = ? " +
                "WHERE `id` = ?";

        LOG.debug("SQL: {}", sql);

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(sql)) {

            int i = 1;
            preStat.setString(i++, userPrompt.getLogin());
            preStat.setString(i++, userPrompt.getSessionID());
            preStat.setString(i++, userPrompt.getIaModel());
            preStat.setInt(i++, userPrompt.getIaMaxTokens());
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
    public List<UserPrompt> findAllUserPrompts() {
        List<UserPrompt> prompts = new ArrayList<>();
        final String sql = "SELECT * FROM " + TABLE + " ORDER BY dateCreated DESC";

        LOG.debug("SQL: {}", sql);

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(sql);
             ResultSet rs = preStat.executeQuery()) {

            while (rs.next()) {
                prompts.add(loadFromResultSet(rs));
            }

        } catch (SQLException e) {
            LOG.error("Unable to load all UserPrompts: {}", e.getMessage(), e);
        }

        return prompts;
    }

    @Override
    public List<UserPrompt> findUserPromptsByLogin(String login) {
        List<UserPrompt> prompts = new ArrayList<>();
        final String sql = "SELECT * FROM " + TABLE + " WHERE login = ? ORDER BY dateCreated DESC";

        LOG.debug("SQL: {}", sql);

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(sql)) {

            preStat.setString(1, login);
            ResultSet rs = preStat.executeQuery();

            while (rs.next()) {
                prompts.add(loadFromResultSet(rs));
            }

        } catch (SQLException e) {
            LOG.error("Unable to find UserPrompts by login: {}", e.getMessage(), e);
        }

        return prompts;
    }

    private UserPrompt loadFromResultSet(ResultSet rs) throws SQLException {
        return UserPrompt.builder()
                .id(rs.getInt("id"))
                .login(rs.getString("login"))
                .sessionID(rs.getString("sessionID"))
                .iaModel(rs.getString("iaModel"))
                .iaMaxTokens(rs.getInt("iaMaxTokens"))
                .title(rs.getString("title"))
                .usrCreated(rs.getString("usrCreated"))
                .dateCreated(rs.getTimestamp("dateCreated"))
                .usrModif(rs.getString("usrModif"))
                .dateModif(rs.getTimestamp("dateModif"))
                .build();
    }
}