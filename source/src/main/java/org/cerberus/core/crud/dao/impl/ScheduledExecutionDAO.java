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
import java.sql.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.IScheduledExecutionDAO;
import org.cerberus.core.crud.entity.ScheduledExecution;
import org.cerberus.core.crud.factory.IFactoryScheduledExecution;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author cdelage
 */
@Repository
public class ScheduledExecutionDAO implements IScheduledExecutionDAO {

    @Autowired
    private IFactoryScheduledExecution factoryScheduledExecution;
    @Autowired
    private DatabaseSpring databaseSpring;

    private final String OBJECT_NAME = "scheduledexecution";
    private final int SQL_DUPLICATED_CODE = 23000;
    private final int MAX_ROW_SELECTED = 100000;

    private static final Logger LOG = LogManager.getLogger(ScheduledExecutionDAO.class);

    @Override
    public long create(ScheduledExecution object) throws CerberusException {
        boolean throwEx = false;
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO scheduledexecution (`schedulerID`, `scheduleName`, `scheduledDate`"
                + ", `scheduleFireTime`, `status`, `comment`, `UsrCreated`"
                + ")");
        query.append("VALUES (?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.id : " + object.getSchedulerId());
            LOG.debug("SQL.date : " + object.getScheduledDate());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);
            try {
                int i = 1;
                preStat.setLong(i++, object.getSchedulerId());
                preStat.setString(i++, object.getScheduleName());
                preStat.setTimestamp(i++, object.getScheduledDate());
                preStat.setTimestamp(i++, object.getScheduleFireTime());
                preStat.setString(i++, object.getStatus());
                preStat.setString(i++, object.getComment());
                preStat.setString(i++, object.getUsrCreated());

                preStat.executeUpdate();
                ResultSet resultSet = preStat.getGeneratedKeys();

                try {
                    if (resultSet.first()) {
                        return resultSet.getLong(1);
                    } else {
                        return 0;
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString(), exception);
                    throwEx = true;
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                // LOG is only in debug as it could happen on normal situation where The same campaign is triggered more than once at the exact same time from different scheduler entry or JVM instance.
                LOG.debug("Unable to execute query : " + exception.toString(), exception);
                throwEx = true;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString(), exception);
            throwEx = true;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
        }
        return 0;
    }

    @Override
    public long createWhenNotExist(ScheduledExecution object) throws CerberusException {
        boolean throwEx = false;
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();

        query.append("INSERT INTO scheduledexecution (`schedulerID`, `scheduleName`, `scheduledDate`, `scheduleFireTime`, `status`, `comment`, `UsrCreated`) "
                + "SELECT ?,?,?,?,?,?,? FROM scheduledexecution "
                + "WHERE NOT EXISTS (SELECT ID FROM scheduledexecution WHERE `schedulerID`=? and scheduledDate = ?) LIMIT 1;");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.id : " + object.getSchedulerId());
            LOG.debug("SQL.date : " + object.getScheduledDate());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);
            try {
                int i = 1;
                preStat.setLong(i++, object.getSchedulerId());
                preStat.setString(i++, object.getScheduleName());
                preStat.setTimestamp(i++, object.getScheduledDate());
                preStat.setTimestamp(i++, object.getScheduleFireTime());
                preStat.setString(i++, object.getStatus());
                preStat.setString(i++, object.getComment());
                preStat.setString(i++, object.getUsrCreated());
                preStat.setLong(i++, object.getSchedulerId());
                preStat.setTimestamp(i++, object.getScheduledDate());

                preStat.executeUpdate();
                ResultSet resultSet = preStat.getGeneratedKeys();

                try {
                    if (resultSet.first()) {
                        return resultSet.getLong(1);
                    } else {
                        return 0;
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString(), exception);
                    throwEx = true;
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString(), exception);
                throwEx = true;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString(), exception);
            throwEx = true;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
        }
        return 0;
    }

    @Override
    public Answer update(ScheduledExecution scheduledExecutionObject) {
        MessageEvent msg = null;

        String query = "UPDATE scheduledexecution SET status = ?, comment = ?, dateModif = NOW() WHERE ID = ?";
        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.id : " + scheduledExecutionObject.getID());
            LOG.debug("SQL.param.status : " + scheduledExecutionObject.getStatus());
            LOG.debug("SQL.param.comment : " + scheduledExecutionObject.getComment());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setString(i++, scheduledExecutionObject.getStatus());
                preStat.setString(i++, StringUtil.getLeftStringPretty(scheduledExecutionObject.getComment().replace("'", ""), 250));
                preStat.setLong(i++, scheduledExecutionObject.getID());
                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }
        return new Answer(msg);
    }

}
