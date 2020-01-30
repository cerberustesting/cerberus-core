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
import org.cerberus.crud.dao.IUserGroupDAO;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.UserGroup;
import org.cerberus.crud.entity.User;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.cerberus.crud.factory.IFactoryUserGroup;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 09/08/2013
 * @since 2.0.0
 */
@Repository
public class UserGroupDAO implements IUserGroupDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryUserGroup factoryGroup;

    /**
     * Declare SQL queries used by this {@link UserGroup}
     *
     * @author Aurelien Bourdon
     */
    private static interface Query {

        /**
         * Get list of {@link UserGroup} associated with the given
         * {@link User}'s name
         */
        String READ_BY_USER = "SELECT * FROM usergroup usg WHERE usg.`login` = ? ";

        /**
         * Create a new {@link UserGroup}
         */
        String CREATE = "INSERT INTO `usergroup` (`login`, `groupName`) VALUES (?, ?)";

        /**
         * Remove an existing {@link UserGroup}
         */
        String DELETE = "DELETE FROM `usergroup` WHERE `login` = ? AND `groupName` = ?";

    }

    /**
     * The associated {@link Logger} to this class
     */
    private static final Logger LOG = LogManager.getLogger(UserSystemDAO.class);

    /**
     * The associated entity name to this DAO
     */
    private static final String OBJECT_NAME = UserGroup.class.getSimpleName();

    @Override
    public boolean addGroupToUser(UserGroup group, User user) {
        boolean bool = false;
        final String query = "INSERT INTO usergroup (Login, GroupName) VALUES (?, ?)";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, user.getLogin());
                preStat.setString(2, group.getGroup());

                int res = preStat.executeUpdate();
                bool = res > 0;
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        return bool;
    }

    @Override
    public boolean removeGroupFromUser(UserGroup group, User user) {
        boolean bool = false;
        final String query = "DELETE FROM usergroup WHERE login = ? AND groupname = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, user.getLogin());
                preStat.setString(2, group.getGroup());

                int res = preStat.executeUpdate();
                bool = res > 0;
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        return bool;
    }

    @Override
    public List<UserGroup> findGroupByKey(String login) {
        List<UserGroup> list = null;
        final String query = "SELECT groupname FROM usergroup WHERE login = ? ORDER BY groupname";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, login);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<UserGroup>();
                    while (resultSet.next()) {
                        UserGroup group = factoryGroup.create(resultSet.getString("groupname"));
                        list.add(group);
                    }
                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : "+exception.toString());
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
    public AnswerList<UserGroup> readByUser(String login) {
        AnswerList<UserGroup> ans = new AnswerList<>();
        MessageEvent msg = null;

        try (Connection connection = databaseSpring.connect();
            PreparedStatement preStat = connection.prepareStatement(Query.READ_BY_USER)) {
            // Prepare and execute query
            preStat.setString(1, login);
            try(ResultSet resultSet = preStat.executeQuery();){
            	List<UserGroup> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(loadUserGroupFromResultSet(resultSet));
                }
                ans.setDataList(result);

                // Set the final message
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                        .resolveDescription("OPERATION", "CREATE");
            }catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } 
        } catch (Exception e) {
            LOG.warn("Unable to read UserGroup: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }
        return ans;
    }

    @Override
    public Answer create(UserGroup group) {
        Answer ans = new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        MessageEvent msg = null;

        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(Query.CREATE)) {
            // Prepare and execute query
            preStat.setString(1, group.getLogin());
            preStat.setString(2, group.getGroup());
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "CREATE");
        } catch (Exception e) {
            LOG.warn("Unable to create userGroup: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public Answer remove(UserGroup group) {
        Answer ans = new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        MessageEvent msg = null;

        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(Query.DELETE)) {
            // Prepare and execute query
            preStat.setString(1, group.getLogin());
            preStat.setString(2, group.getGroup());
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "DELETE");
        } catch (Exception e) {
            LOG.warn("Unable to delete userGroup: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public Answer removeGroupByUser(UserGroup group, User user) {
        return null;
    }

    private UserGroup loadUserGroupFromResultSet(ResultSet rs) throws SQLException {
        String login = ParameterParserUtil.parseStringParam(rs.getString("login"), "");
        String groupName = ParameterParserUtil.parseStringParam(rs.getString("groupName"), "");
        return factoryGroup.create(login, groupName);
    }
}
