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
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.log.MyLogger;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;
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

    private final String SQL_DUPLICATED_CODE = "23000";

    @Override
    public AnswerList readByCriteria(int start, int amount, String colName, String dir, String searchTerm, String individualSearch) {
        AnswerList response = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        List<LogEvent> logEventList = new ArrayList<LogEvent>();
        StringBuilder gSearch = new StringBuilder();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM logevent ");

        gSearch.append(" where (`time` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `login` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `page` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `action` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `log` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%')");

        if (!searchTerm.equals("") && !individualSearch.equals("")) {
            searchSQL.append(gSearch.toString());
            searchSQL.append(" and ");
            searchSQL.append(individualSearch);
        } else if (!individualSearch.equals("")) {
            searchSQL.append(" where `");
            searchSQL.append(individualSearch);
            searchSQL.append("`");
        } else if (!searchTerm.equals("")) {
            searchSQL.append(gSearch.toString());
        }

        query.append(searchSQL);
        query.append("order by `");
        query.append(colName);
        query.append("` ");
        query.append(dir);
        query.append(" limit ");
        query.append(start);
        query.append(" , ");
        query.append(amount);

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        logEventList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    msg.setDescription(msg.getDescription().replace("%ITEM%", "LogEvent").replace("%OPERATION%", "SELECT"));
                    response = new AnswerList(logEventList, nrTotalRows);

                } catch (SQLException exception) {
                    MyLogger.log(LogEventDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                MyLogger.log(LogEventDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(LogEventDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(LogEventDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        response.setResultMessage(msg);
        response.setDataList(logEventList);
        return response;
    }

    @Override
    public Answer create(LogEvent logevent) {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO logevent (userID, Login, Page, Action, Log, remoteIP, localIP) ");
        query.append("VALUES (?, ?, ?, ?, ?, ?, ?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setLong(1, logevent.getUserID());
                preStat.setString(2, logevent.getLogin());
                preStat.setString(3, logevent.getPage());
                preStat.setString(4, logevent.getAction());
                preStat.setString(5, logevent.getLog());
                preStat.setString(6, logevent.getremoteIP());
                preStat.setString(7, logevent.getLocalIP());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "LogEvent").replace("%OPERATION%", "INSERT"));

            } catch (SQLException exception) {
                MyLogger.log(LogEventDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());

                if (exception.getSQLState().equals(SQL_DUPLICATED_CODE)) { //23000 is the sql state for duplicate entries
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_DUPLICATE_ERROR);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "LogEvent").replace("%OPERATION%", "INSERT"));
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                }
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(LogEventDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(LogEventDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return new Answer(msg);
    }

    @Override
    public LogEvent loadFromResultSet(ResultSet resultSet) throws SQLException {
        long logEventID = resultSet.getLong("logEventID") == 0 ? 0 : resultSet.getLong("logEventID");
        long userID = resultSet.getLong("userID") == 0 ? 0 : resultSet.getLong("userID");
        String login = resultSet.getString("login") == null ? "" : resultSet.getString("login");
        Timestamp time = resultSet.getTimestamp("time");
        String page = resultSet.getString("page") == null ? "" : resultSet.getString("page");
        String action = resultSet.getString("action") == null ? "" : resultSet.getString("action");
        String log = resultSet.getString("log") == null ? "" : resultSet.getString("log");
        String remoteIP = resultSet.getString("remoteIP") == null ? "" : resultSet.getString("remoteIP");
        String localIP = resultSet.getString("localIP") == null ? "" : resultSet.getString("localIP");
        return factoryLogEvent.create(logEventID, userID, login, time, page, action, log, remoteIP, localIP);
    }

}
