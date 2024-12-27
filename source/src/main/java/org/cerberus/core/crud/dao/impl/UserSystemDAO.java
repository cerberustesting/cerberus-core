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
import org.cerberus.core.crud.dao.IUserSystemDAO;
import org.cerberus.core.crud.entity.UserSystem;
import org.cerberus.core.crud.factory.IFactoryUserSystem;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.SqlUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author bcivel
 */
@Repository
public class UserSystemDAO implements IUserSystemDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryUserSystem factoryUserSystem;

    /**
     * Declare SQL queries used by this {@link UserSystem}
     *
     * @author Aurelien Bourdon
     */
    private static interface Query {

        /**
         * Create a new {@link UserSystem}
         */
        String CREATE = "INSERT INTO `usersystem` (`login`, `system`) VALUES (?, ?)";

        /**
         * Remove an existing {@link UserSystem}
         */
        String DELETE = "DELETE FROM `usersystem` WHERE `login` = ? AND `system` = ?";

    }

    /**
     * The associated {@link Logger} to this class
     */
    private static final Logger LOG = LogManager.getLogger(UserSystemDAO.class);

    /**
     * The associated entity name to this DAO
     */
    private static final String OBJECT_NAME = UserSystem.class.getSimpleName();

    @Override
    public UserSystem findUserSystemByKey(String login, String system) throws CerberusException {
        UserSystem result = null;
        final String query = "SELECT uss.* FROM usersystem u WHERE u.`login` = ? and u.`system` = ?";

        // Debug message on SQL.
        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.login : {}", login);
        LOG.debug("SQL.param.system : {}", system);

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            try {
                preStat.setString(1, login);
                preStat.setString(2, system);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadUserSystemFromResultSet(resultSet);
                    }
                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        return result;
    }

    @Override
    public List<UserSystem> findallUser() throws CerberusException {
        List<UserSystem> list = null;
        final String query = "SELECT uss.* FROM usersystem uss ORDER BY `login`";

        // Debug message on SQL.
        LOG.debug("SQL : {}", query);

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<>();
                    while (resultSet.next()) {
                        UserSystem user = this.loadUserSystemFromResultSet(resultSet);
                        list.add(user);
                    }
                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
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
    public List<UserSystem> findUserSystemByUser(String login) throws CerberusException {
        List<UserSystem> list = null;
        final String query = "SELECT uss.* FROM usersystem uss JOIN invariant inv ON inv.value=uss.system and inv.idname='SYSTEM' WHERE  uss.`login` = ? order by inv.sort;";

        // Debug message on SQL.
        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.login : {}", login);

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, login);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<>();
                    while (resultSet.next()) {
                        UserSystem user = this.loadUserSystemFromResultSet(resultSet);
                        list.add(user);
                    }
                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
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
    public List<UserSystem> findUserSystemBySystem(String system) throws CerberusException {
        List<UserSystem> list = null;
        final String query = "SELECT uss.* FROM usersystem uss WHERE uss.`system` = ? ";

        // Debug message on SQL.
        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.system : {}", system);

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, system);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<>();
                    while (resultSet.next()) {
                        UserSystem user = this.loadUserSystemFromResultSet(resultSet);
                        list.add(user);
                    }
                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
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
    public void insertUserSystem(UserSystem userSystem) throws CerberusException {
        final String query = "INSERT INTO usersystem (`login`, `system`) VALUES (?, ?)";

        // Debug message on SQL.
        LOG.debug("SQL : {}", query);

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query);) {
            try {
                preStat.setString(1, userSystem.getLogin());
                preStat.setString(2, userSystem.getSystem());
                preStat.execute();
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void deleteUserSystem(UserSystem userSystem) throws CerberusException {
        final String query = "DELETE FROM usersystem WHERE `login` = ? and `system` = ?";

        // Debug message on SQL.
        LOG.debug("SQL : {}", query);

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query);) {
            try {
                preStat.setString(1, userSystem.getLogin());
                preStat.setString(2, userSystem.getSystem());
                preStat.execute();
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void updateUserSystem(UserSystem userSystem) throws CerberusException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AnswerList<UserSystem> readByUser(String login) {
        AnswerList<UserSystem> ans = new AnswerList<>();
        MessageEvent msg = null;
        String query = "SELECT uss.* FROM usersystem uss JOIN invariant inv ON inv.value=uss.system and inv.idname='SYSTEM' WHERE  uss.`login` = ? order by inv.sort;";

        // Debug message on SQL.
        LOG.debug("SQL : {}", query);

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query)) {
            // Prepare and execute query
            preStat.setString(1, login);

            try (ResultSet resultSet = preStat.executeQuery();) {
                // Parse query
                List<UserSystem> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(loadUserSystemFromResultSet(resultSet));
                }
                ans.setDataList(result);

                // Set the final message
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                        .resolveDescription("OPERATION", "GET");
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            }
        } catch (Exception e) {
            LOG.warn("Unable to read userSystem: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public Answer create(UserSystem sys) {
        Answer ans = new Answer();
        MessageEvent msg = null;

        String query = Query.CREATE;

        // Debug message on SQL.
        LOG.debug("SQL : {}", query);

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query)) {
            // Prepare and execute query
            preStat.setString(1, sys.getLogin());
            preStat.setString(2, sys.getSystem());
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "CREATE");
        } catch (Exception e) {
            LOG.warn("Unable to create UserSystem: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public Answer createSystemList(String user, String[] systemList) {
        Answer ans = new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        MessageEvent msg = null;

        String query = "INSERT INTO `usersystem` (`login`, `system`) SELECT ? , value FROM invariant where idname='SYSTEM' and " + SqlUtil.generateInClause("value", Arrays.asList(systemList)) + ";";

        // Debug message on SQL.
        LOG.debug("SQL : {}", query);

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query)) {
            // Prepare and execute query
            int i = 1;
            preStat.setString(i++, user);
            for (String system : systemList) {
                preStat.setString(i++, system);

            }
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "CREATE");
        } catch (Exception e) {
            LOG.warn("Unable to create userSystem: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public Answer createAllSystemList(String user) {
        Answer ans = new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        MessageEvent msg = null;

        String query = "INSERT INTO `usersystem` (`login`, `system`) SELECT ? , value FROM invariant where idname='SYSTEM' and value not like 'US-%';";

        // Debug message on SQL.
        LOG.debug("SQL : {}", query);

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query)) {
            // Prepare and execute query
            preStat.setString(1, user);
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "CREATE");
        } catch (Exception e) {
            LOG.warn("Unable to create userSystem: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public Answer remove(UserSystem sys) {
        Answer ans = new Answer();
        MessageEvent msg = null;

        // Debug message on SQL.
        LOG.debug("SQL : {}", Query.DELETE);

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(Query.DELETE)) {
            // Prepare and execute query
            preStat.setString(1, sys.getLogin());
            preStat.setString(2, sys.getSystem());
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "DELTE");
        } catch (Exception e) {
            LOG.warn("Unable to delete UserSystem: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    private UserSystem loadUserSystemFromResultSet(ResultSet rs) throws SQLException {
        String login = ParameterParserUtil.parseStringParam(rs.getString("uss.login"), "");
        String system = ParameterParserUtil.parseStringParam(rs.getString("uss.system"), "");
        return factoryUserSystem.create(login, system);
    }

}
