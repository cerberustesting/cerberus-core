/* Cerberus Copyright (C) 2013 - 2017 cerberustesting
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.

This file is part of Cerberus.

Cerberus is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Cerberus is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.crud.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.dao.IScheduledExecutionDAO;
import org.cerberus.crud.entity.ScheduleEntry;
import org.cerberus.crud.entity.ScheduledExecution;
import org.cerberus.crud.entity.Tag;
import org.cerberus.crud.factory.IFactoryScheduledExecution;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
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
    public AnswerItem<Integer> create(ScheduledExecution object) {
        AnswerItem<Integer> ans = new AnswerItem<>();
        LOG.debug("working to insert : " + object.getScheduleName() + " scheduledexecution in database");
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO scheduledexecution (`schedulerID`, `scheduleName`, `scheduledDate`"
                + ", `scheduleFireTime`, `status`, `comment`, `UsrCreated`"
                + ")");
        query.append("VALUES (?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);
            try {
                int i = 1;
                preStat.setInt(i++, object.getSchedulerId());
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
                        LOG.debug("ID of job triggered " + resultSet.getInt(1));
                        ans.setItem(resultSet.getInt(1));
                    }
                } catch (Exception e) {
                    LOG.debug("Exception catch :", e);
                } finally {
                    resultSet.close();
                }

                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString(), exception);

                if (exception.getSQLState().equals(SQL_DUPLICATED_CODE)) { //23000 is the sql state for duplicate entries
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_DUPLICATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT").replace("%REASON%", exception.toString()));
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                }
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } catch (Exception exception) {
            LOG.error("Unable to execute query : " + exception.toString(), exception);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to close connection : " + exception.toString());
            }
        }
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public Answer update(ScheduledExecution scheduledExecutionObject) {
        MessageEvent msg = null;

        String query = "UPDATE scheduledexecution SET status = ?, comment = ?, dateModif = NOW() WHERE ID = ?";
        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.ExecutionScheduled : " + scheduledExecutionObject.getScheduleName());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {

                int i = 1;
                preStat.setString(i++, scheduledExecutionObject.getStatus());
                preStat.setString(i++, scheduledExecutionObject.getComment());
                preStat.setInt(i++, scheduledExecutionObject.getID());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));
                LOG.debug(msg.getDescription());
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : ", exception.toString());
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
