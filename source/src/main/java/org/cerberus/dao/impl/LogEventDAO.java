/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.cerberus.dao.ILogEventDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.LogEvent;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.log.MyLogger;
import org.cerberus.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author vertigo
 */
@Repository
public class LogEventDAO implements ILogEventDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryLogEvent factoryLogEvent;

    @Override
    public List<LogEvent> findAllLogEvent() throws CerberusException {
        List<LogEvent> list = null;
        boolean throwExe = true;
        final String query = "SELECT * FROM logevent ORDER BY logeventid ; ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<LogEvent>();
                    while (resultSet.next()) {
                        throwExe = false;
                        long logEventId = resultSet.getLong("logeventid");
                        long userID = resultSet.getLong("userID");
                        String login = resultSet.getString("login");
                        Timestamp time = resultSet.getTimestamp("time");
                        String page = resultSet.getString("page");
                        String action = resultSet.getString("action");
                        String log = resultSet.getString("log");
                        String remoteIP = resultSet.getString("remoteIP");
                        String localIP = resultSet.getString("localIP");
                        LogEvent myLogEvent = factoryLogEvent.create(logEventId, userID, login, time, page, action, log, remoteIP, localIP);
                        list.add(myLogEvent);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UserDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExe) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return list;
    }

    @Override
    public List<LogEvent> findAllLogEvent(int start, int amount, String colName, String dir, String searchTerm) throws CerberusException {
        List<LogEvent> list = null;
        boolean throwExe = true;

        StringBuilder gSearch = new StringBuilder();
        if (!(searchTerm.equalsIgnoreCase(""))) {
            gSearch.append(" WHERE ");
            gSearch.append(getSearchString(searchTerm));
        }

        StringBuilder gOrder = new StringBuilder();
        if (!(colName.equalsIgnoreCase(""))){
            gOrder.append(" ORDER BY ");
            gOrder.append(colName);
            gOrder.append(" ");
            gOrder.append(dir);
            gOrder.append(" ");
        }
        String query = "SELECT * FROM logevent " + gSearch.toString().replace("?", "") + gOrder.toString() + " LIMIT ? , ? ";

        MyLogger.log(LogEventDAO.class.getName(), Level.DEBUG, query);

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setInt(1, start);
            preStat.setInt(2, amount);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<LogEvent>();
                    while (resultSet.next()) {
                        throwExe = false;
                        long logEventId = resultSet.getLong("logeventid");
                        long userID = resultSet.getLong("userID");
                        String login = resultSet.getString("login");
                        Timestamp time = resultSet.getTimestamp("time");
                        String page = resultSet.getString("page");
                        String action = resultSet.getString("action");
                        String log = resultSet.getString("log");
                        String remoteIP = resultSet.getString("remoteIP");
                        String localIP = resultSet.getString("localIP");
                        LogEvent myLogEvent = factoryLogEvent.create(logEventId, userID, login, time, page, action, log, remoteIP, localIP);
                        list.add(myLogEvent);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UserDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExe) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return list;
    }

    @Override
    public Integer getNumberOfLogEvent(String searchTerm) throws CerberusException {
        boolean throwExe = true;
        
        StringBuilder gSearch = new StringBuilder();
        if (!(searchTerm.equalsIgnoreCase(""))) {
            gSearch.append(" WHERE ");
            gSearch.append(getSearchString(searchTerm));
        }

        final String query = "SELECT count(*) c FROM logevent " + gSearch.toString();

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        throwExe = false;
                        return resultSet.getInt("c");
                    }
                } catch (SQLException exception) {
                    MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UserDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExe) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return 0;
    }

    @Override
    public boolean insertLogEvent(LogEvent logevent) throws CerberusException {
        boolean bool = false;
        final String query = "INSERT INTO logevent (userID, Login, Page, Action, Log, remoteIP, localIP) VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            try {
                preStat.setLong(1, logevent.getUserID());
                preStat.setString(2, logevent.getLogin());
                preStat.setString(3, logevent.getPage());
                preStat.setString(4, logevent.getAction());
                preStat.setString(5, logevent.getLog());
                preStat.setString(6, logevent.getremoteIP());
                preStat.setString(7, logevent.getLocalIP());

                preStat.executeUpdate();
                ResultSet resultSet = preStat.getGeneratedKeys();
                try {
                    if (resultSet.first()) {
                        bool = true;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UserDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return bool;
    }
    
    
    private String getSearchString(String searchTerm) {
        if (StringUtil.isNullOrEmpty(searchTerm))  {
            return "";
        } else {
            StringBuilder gSearch = new StringBuilder();
            gSearch.append(" (`login` like '%");
            gSearch.append(searchTerm.replace("'", "\\'"));
            gSearch.append("%'");
            gSearch.append(" or `page` like '%");
            gSearch.append(searchTerm.replace("'", "\\'"));
            gSearch.append("%'");
            gSearch.append(" or `action` like '%");
            gSearch.append(searchTerm.replace("'", "\\'"));
            gSearch.append("%'");
            gSearch.append(" or `log` like '%");
            gSearch.append(searchTerm.replace("'", "\\'"));
            gSearch.append("%') ");
            return gSearch.toString();
        }
    }
    
}
