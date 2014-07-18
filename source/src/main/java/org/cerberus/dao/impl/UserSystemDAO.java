/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.cerberus.dao.IUserSystemDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.UserSystem;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryUserSystem;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author bcivel
 */
@Repository
public class UserSystemDAO implements IUserSystemDAO{
    
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryUserSystem factoryUserSystem;

    @Override
    public UserSystem findUserSystemByKey(String login, String system) throws CerberusException {
        UserSystem result = null;
        final String query = "SELECT * FROM userSystem u WHERE u.`login` = ? and u.`system`";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, login);
                preStat.setString(2, system);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadUserSystemFromResultSet(resultSet);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(UserSystemDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(UserSystemDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserSystemDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UserSystemDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }

    @Override
    public List<UserSystem> findallUser() throws CerberusException {
        List<UserSystem> list = null;
        final String query = "SELECT * FROM userSystem ORDER BY `login`";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<UserSystem>();
                    while (resultSet.next()) {
                        UserSystem user = this.loadUserSystemFromResultSet(resultSet);
                        list.add(user);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(UserSystemDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(UserSystemDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserSystemDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UserSystemDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public List<UserSystem> findUserSystemByUser(String login) throws CerberusException {
        List<UserSystem> list = null;
        final String query = "SELECT * FROM userSystem u WHERE u.`login` = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, login);
                
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<UserSystem>();
                    while (resultSet.next()) {
                        UserSystem user = this.loadUserSystemFromResultSet(resultSet);
                        list.add(user);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(UserSystemDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(UserSystemDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserSystemDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UserSystemDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public List<UserSystem> findUserSystemBySystem(String system) throws CerberusException {
        List<UserSystem> list = null;
        final String query = "SELECT * FROM userSystem u WHERE u.`system` = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, system);
                
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<UserSystem>();
                    while (resultSet.next()) {
                        UserSystem user = this.loadUserSystemFromResultSet(resultSet);
                        list.add(user);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(UserSystemDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(UserSystemDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserSystemDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UserSystemDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public void insertUserSystem(UserSystem userSystem) throws CerberusException {
        final String query = "INSERT INTO userSystem (`login`, `system`) VALUES (?, ?)";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, userSystem.getLogin());
                preStat.setString(2, userSystem.getSystem());
                
                preStat.execute();
            } catch (SQLException exception) {
                MyLogger.log(UserSystemDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserSystemDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UserSystemDAO.class.getName(), Level.WARN, e.toString());
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
            }
        }
    }

    @Override
    public void deleteUserSystem(UserSystem userSystem) throws CerberusException {
        final String query = "DELETE FROM userSystem WHERE `login` = ? and `system` = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, userSystem.getLogin());
                preStat.setString(2, userSystem.getSystem());

                preStat.execute();
            } catch (SQLException exception) {
                MyLogger.log(UserSystemDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserSystemDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UserSystemDAO.class.getName(), Level.WARN, e.toString());
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
            }
        }
    }

    @Override
    public void updateUserSystem(UserSystem userSystem) throws CerberusException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private UserSystem loadUserSystemFromResultSet(ResultSet rs) throws SQLException {
        String login = ParameterParserUtil.parseStringParam(rs.getString("login"), "");
        String system = ParameterParserUtil.parseStringParam(rs.getString("system"), "");
        return factoryUserSystem.create(login, system);
    }
    
}
