package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.ILogEventDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.LogEvent;
import com.redcats.tst.entity.MessageGeneral;
import com.redcats.tst.entity.MessageGeneralEnum;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryLogEvent;
import com.redcats.tst.log.MyLogger;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
    public List<LogEvent> findAllLogEvent(int start, int amount, String colName, String dir) throws CerberusException {
        List<LogEvent> list = null;
        boolean throwExe = true;
        String query = "SELECT * FROM logevent order by " + colName + " " + dir + " limit " + start + " , " + amount;
        MyLogger.log(LogEventDAO.class.getName(), Level.DEBUG, query);

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
    public Integer getNumberOfLogEvent() throws CerberusException {
        boolean throwExe = true;
        final String query = "SELECT count(*) c FROM logevent ; ";

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
}
