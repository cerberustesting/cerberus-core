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
package org.cerberus.serviceEngine.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.log.MyLogger;
import org.cerberus.serviceEngine.IConnectionPoolDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 15/01/2013
 * @since 2.0.0
 */
@Repository
public class ConnectionPoolDAO implements IConnectionPoolDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;

    /**
     * Short one line description.
     * <p/>
     * Longer description. If there were any, it would be here. <p> And even
     * more explanations to follow in consecutive paragraphs separated by HTML
     * paragraph breaks.
     *
     * @param variable Description text text text.
     * @return Description text text text.
     */
    @Override
    public List<String> queryDatabase(String connectionName, String sql, int limit) throws CerberusEventException {
        List<String> list = null;
        boolean throwEx = false;
        int maxSecurityFetch = 100;
        int nbFetch = 0;
        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_GENERIC);
        msg.setDescription(msg.getDescription().replaceAll("%JDBC%", "jdbc/" + connectionName));

        Connection connection = this.databaseSpring.connect(connectionName);
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            if(limit > 0 && limit < maxSecurityFetch) {
                preStat.setMaxRows(limit);
            } else {
                preStat.setMaxRows(maxSecurityFetch);
            }
            //TODO add limit of select
            /*
             ORACLE      => * WHERE ROWNUM <= limit *
             DB2         => * FETCH FIRST limit ROWS ONLY
             MYSQL       => * LIMIT 0, limit
             SQL SERVER  => SELECT TOP limit *
             SYBASE      => SET ROWCOUNT limit *
             if (limit > 0) {
             sql.concat(Util.DbLimit(databaseType, limit));
             }
             */
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<String>();
                try {
                    while ((resultSet.next()) && (nbFetch < maxSecurityFetch)) {
                        list.add(resultSet.getString(1));
                        nbFetch++;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ConnectionPoolDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());

                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ConnectionPoolDAO.class.getName(), Level.WARN, exception.toString());
                msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_ERROR);
                msg.setDescription(msg.getDescription().replaceAll("%SQL%", sql));
                msg.setDescription(msg.getDescription().replaceAll("%EX%", exception.toString()));
                throwEx = true;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ConnectionPoolDAO.class.getName(), Level.FATAL, exception.toString());
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_CANNOTACCESSJDBC);
            msg.setDescription(msg.getDescription().replaceAll("%JDBC%", "jdbc/" + connectionName));
            msg.setDescription(msg.getDescription().replaceAll("%EX%", exception.toString()));
            throwEx = true;
        } catch (NullPointerException exception) {
            //TODO check where exception occur
            MyLogger.log(ConnectionPoolDAO.class.getName(), Level.FATAL, exception.toString());
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_CANNOTACCESSJDBC);
            msg.setDescription(msg.getDescription().replaceAll("%JDBC%", "jdbc/" + connectionName));
            msg.setDescription(msg.getDescription().replaceAll("%EX%", exception.toString()));
            throwEx = true;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ConnectionPoolDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusEventException(msg);
        }
        return list;
    }
}
