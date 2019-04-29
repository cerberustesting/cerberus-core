/*Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
import java.sql.Timestamp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.Scheduler;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.AnswerItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.cerberus.crud.dao.ISchedulerDAO;
import org.cerberus.crud.factory.IFactoryScheduler;
import org.cerberus.crud.factory.impl.FactoryScheduler;

/**
 *
 * @author cdelage
 */
public class SchedulerDAO implements ISchedulerDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    IFactoryScheduler factoryscheduler = new FactoryScheduler();
    private static final Logger LOG = LogManager.getLogger(SchedulerDAO.class);
    private final String OBJECT_NAME = "Scheduler";

    @Override
    public AnswerItem<Scheduler> readByKey(String name) {
        AnswerItem ans = new AnswerItem<>();
        Scheduler result = null;
        final String query = "SELECT * FROM `campaignScheduler` campaignScheduler WHERE `campaign` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.CampaignScheduler : " + name);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, name);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = loadFromResultSet(resultSet);
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        ans.setItem(result);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString(), exception);
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString(), exception);
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString(), exception);
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

        //sets the message
        ans.setResultMessage(msg);
        return ans;
    }

    private Scheduler loadFromResultSet(ResultSet rs) throws SQLException {
        int schedulerId = ParameterParserUtil.parseIntegerParam(rs.getString("scheduler.ID"), 0);
        String type = ParameterParserUtil.parseStringParam(rs.getString("scheduler.type"), "");
        String name = ParameterParserUtil.parseStringParam(rs.getString("scheduler.name"), "");
        String cronDefinition = ParameterParserUtil.parseStringParam(rs.getString("scheduler.cronDefinition"), "");
        String lastExecution = ParameterParserUtil.parseStringParam(rs.getString("scheduler.lastExecution"), "");
        String active = ParameterParserUtil.parseStringParam(rs.getString("scheduler.active"), "");
        String usrCreated = ParameterParserUtil.parseStringParam(rs.getString("scheduler.UsrCreated"), "");
        String dateCreated = ParameterParserUtil.parseStringParam(rs.getString("scheduler.DateCreated"), "");
        String usrModif = ParameterParserUtil.parseStringParam(rs.getString("scheduler.UsrModif"), "");
        String dateModif = ParameterParserUtil.parseStringParam(rs.getString("scheduler.DateModif"), "");

        Scheduler newScheduler = factoryscheduler.create(schedulerId, type, name, cronDefinition, lastExecution, active, usrCreated, dateCreated, usrModif, dateModif);
        return newScheduler;
    }

    @Override
    public boolean createScheduleEntry(Scheduler scheduler) {
        final StringBuilder query = new StringBuilder("INSERT INTO `scheduleentry` (`type`, `name`,`cronDefinition`,`lastExecution`,`active`,`UsrCreated`,`DateCreated`,`UsrModif`,`DateModif`) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?);");

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());) {
            preStat.setString(1, scheduler.getType());
            preStat.setString(2, scheduler.getName());
            preStat.setString(3, scheduler.getCronDefinition());
            preStat.setString(4, scheduler.getLastExecution());
            preStat.setString(5, scheduler.getActive());
            preStat.setString(6, scheduler.getUsrCreated());
            preStat.setString(7, scheduler.getDateCreated());
            preStat.setString(8, scheduler.getUsrModif());
            preStat.setString(7, scheduler.getDateModif());
            return (preStat.executeUpdate() == 1);
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        }
        return false;
    }
}
