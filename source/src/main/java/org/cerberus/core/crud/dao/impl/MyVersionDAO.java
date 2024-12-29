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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.cerberus.core.crud.dao.IMyVersionDAO;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.crud.entity.MyVersion;
import org.cerberus.core.crud.factory.IFactoryMyversion;
import org.cerberus.core.crud.factory.impl.FactoryMyversion;
import org.cerberus.core.util.ParameterParserUtil;
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

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(MyVersionDAO.class);

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
                    LOG.warn("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                result = null;
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
    public boolean update(MyVersion myVersion) {
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
    public boolean updateAndLockVersionEntryDuringMs(String version, long value, long lockDurationMs) {
        boolean result = false;
        final String query = "UPDATE myversion SET value = ? WHERE `key` = ? and value < ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.value : " + value);
            LOG.debug("SQL.param.value : " + (value - lockDurationMs));
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setLong(1, value);
                preStat.setString(2, version);
                preStat.setLong(3, value - lockDurationMs);

                if (preStat.executeUpdate() >= 1) {
                    result = true;
                } else {
                    result = false;
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
    public boolean flagMyVersionString(String key) {
        boolean result = false;
        final String query = "UPDATE myversion SET valueString = 'Y' WHERE `key` = ? and valueString = 'N'";

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

                if (preStat.executeUpdate() >= 1) {
                    result = true;
                } else {
                    result = false;
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

    private MyVersion loadFromResultSet(ResultSet resultSet) {
        String key = "";
        int value = 0;
        try {
            key = ParameterParserUtil.parseStringParam(resultSet.getString("Key"), "");
            value = ParameterParserUtil.parseIntegerParam(resultSet.getString("Value"), 0);
        } catch (SQLException ex) {
            LOG.warn(ex);
        }
        String valueString = "";
        try {
            valueString = ParameterParserUtil.parseStringParam(resultSet.getString("ValueString"), "");
        } catch (SQLException ex) {
            LOG.warn(ex);
        }

        return factoryMyversion.create(key, value, valueString);
    }

}
