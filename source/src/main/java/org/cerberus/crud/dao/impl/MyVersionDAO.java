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
import java.util.logging.Logger;

import org.apache.log4j.Level;
import org.cerberus.crud.dao.IMyVersionDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.MyVersion;
import org.cerberus.crud.factory.IFactoryMyversion;
import org.cerberus.crud.factory.impl.FactoryMyversion;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Benoit Dumont
 * @version 1.0, 09/06/2013
 * @since 2.0.0
 */
@Repository
public class MyVersionDAO implements IMyVersionDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryMyversion factoryMyversion;

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AppServiceDAO.class);

    @Override
    public MyVersion findMyVersionByKey(String key) {
        MyVersion result = new MyVersion();
        final String query = "SELECT * FROM myversion mv WHERE mv.`key` = ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.key : " + key);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, key);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.next()) {
                        factoryMyversion = new FactoryMyversion();
                        result = loadFromResultSet(resultSet);
                    }
                } catch (SQLException exception) {
                    result = null;
                    MyLogger.log(MyVersionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                result = null;
                MyLogger.log(MyVersionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(MyVersionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(MyVersionDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }

    @Override
    public boolean updateMyVersion(MyVersion myVersion) {
        boolean result = false;
        final String query = "UPDATE myversion SET value = ? WHERE `key` = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setInt(1, myVersion.getValue());
                preStat.setString(2, myVersion.getKey());

                result = preStat.execute();
            } catch (SQLException exception) {
                MyLogger.log(MyVersionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(MyVersionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(MyVersionDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }

    @Override
    public boolean updateMyVersionString(MyVersion myVersion) {
        boolean result = false;
        final String query = "UPDATE myversion SET valueString = ? WHERE `key` = ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.key : " + myVersion.getKey());
            LOG.debug("SQL.param.valueString : " + myVersion.getValueString());
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, myVersion.getValueString());
                preStat.setString(2, myVersion.getKey());

                result = preStat.execute();
            } catch (SQLException exception) {
                MyLogger.log(MyVersionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(MyVersionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(MyVersionDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }

    private MyVersion loadFromResultSet(ResultSet resultSet) {
        String key = "";
        int value = 0;
        try {
            key = ParameterParserUtil.parseStringParam(resultSet.getString("Key"), "");
            value = ParameterParserUtil.parseIntegerParam(resultSet.getString("Value"), 0);
        } catch (SQLException ex) {
            Logger.getLogger(MyVersionDAO.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        String valueString = "";
        try {
            valueString = ParameterParserUtil.parseStringParam(resultSet.getString("ValueString"), "");
        } catch (SQLException ex) {
            Logger.getLogger(MyVersionDAO.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        return factoryMyversion.create(key, value, valueString);
    }

}
