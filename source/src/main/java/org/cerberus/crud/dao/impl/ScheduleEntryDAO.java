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
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.ScheduleEntry;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.AnswerItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.cerberus.crud.entity.Campaign;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.factory.impl.FactoryScheduleEntry;
import org.cerberus.crud.factory.IFactoryScheduleEntry;
import org.cerberus.util.answer.AnswerList;
import org.springframework.stereotype.Repository;
import org.cerberus.crud.dao.IScheduleEntryDAO;

/**
 *
 * @author cdelage
 */
@Repository
public class ScheduleEntryDAO implements IScheduleEntryDAO {

    @Autowired
    private DatabaseSpring databaseSpring;

    @Autowired
    IFactoryScheduleEntry factoryscheduleentry = new FactoryScheduleEntry();
    
    private static final Logger LOG = LogManager.getLogger(ScheduleEntryDAO.class);
    private final String OBJECT_NAME = "Scheduler";

    @Override
    public AnswerItem<ScheduleEntry> readByKey(String name) {
        AnswerItem<ScheduleEntry> ans = new AnswerItem<>();
        ScheduleEntry result = null;
        final String query = "SELECT * FROM `scheduleentry` AS sce WHERE `name` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.SchedulerDAO : " + name);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                //LOG.debug(name);
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
        return ans;
    }
    
    @Override
    public AnswerItem<List> readAllActive() {
        //LOG.debug("readAllActive is running");
        AnswerItem<List> ans = new AnswerItem();
        List<ScheduleEntry> objectList = new ArrayList<ScheduleEntry>();
        final String query = "SELECT * FROM `scheduleentry` WHERE `active` = 'Y'";
        MessageEvent msg; 
        // Debug message on SQL.        
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    objectList = new ArrayList<ScheduleEntry>();
                    while (resultSet.next()) {
                        objectList.add(this.loadFromResultSet(resultSet));
                    }
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                  
                } finally {
                    resultSet.close();
                }
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
                LOG.warn(exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            }
        }
        ans.setResultMessage(msg);
        ans.setItem(objectList);
        //sets the message
        return ans;
    }

    private ScheduleEntry loadFromResultSet(ResultSet rs) throws SQLException {
        //LOG.debug("loadFromResultSet scheduleentry");
        int schedulerId = ParameterParserUtil.parseIntegerParam(rs.getString("scheduleentry.ID"), 0);
        String type = ParameterParserUtil.parseStringParam(rs.getString("scheduleentry.type"), "");
        String name = ParameterParserUtil.parseStringParam(rs.getString("scheduleentry.name"), "");
        String cronDefinition = ParameterParserUtil.parseStringParam(rs.getString("scheduleentry.cronDefinition"), "");
        String active = ParameterParserUtil.parseStringParam(rs.getString("scheduleentry.active"), "");
        String usrCreated = ParameterParserUtil.parseStringParam(rs.getString("scheduleentry.UsrCreated"), "");
        String usrModif = ParameterParserUtil.parseStringParam(rs.getString("scheduleentry.UsrModif"), "");
        Timestamp lastExecution = rs.getTimestamp("scheduleentry.lastExecution");
        Timestamp dateModif = rs.getTimestamp("scheduleentry.DateModif");
        Timestamp dateCreated = rs.getTimestamp("scheduleentry.DateCreated"); 
        ScheduleEntry newScheduleEntry = factoryscheduleentry.create(schedulerId, type, name, cronDefinition, lastExecution, active, usrCreated, dateCreated, usrModif, dateModif);
        //LOG.debug("id             : " + newScheduleEntry.getID());
        //LOG.debug("type           : " + newScheduleEntry.getType());
        //LOG.debug("name           : " + newScheduleEntry.getName());
        //LOG.debug("active         : " + newScheduleEntry.getActive());
        //LOG.debug("usrCreated     : " + newScheduleEntry.getUsrCreated());
        //LOG.debug("dateCreated    : " + newScheduleEntry.getDateCreated());
        //LOG.debug("usrModif       : " + newScheduleEntry.getUsrModif());
        //LOG.debug("dateModif      : " + newScheduleEntry.getDateModif());
        //LOG.debug("lastExecution  : " + newScheduleEntry.getLastExecution());
        //LOG.debug("CronDefinition : " + newScheduleEntry.getCronDefinition());
        return newScheduleEntry;

    }

    @Override
    public boolean create(ScheduleEntry scheduler) {
        final StringBuilder query = new StringBuilder("INSERT INTO `scheduleentry` (`type`, `name`,`cronDefinition`,`lastExecution`,`active`,`UsrCreated`,`DateCreated`,`UsrModif`,`DateModif`) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?);");

        try (Connection connection = this.databaseSpring.connect();
            PreparedStatement preStat = connection.prepareStatement(query.toString());) {
            preStat.setString(1, scheduler.getType());
            preStat.setString(2, scheduler.getName());
            preStat.setString(3, scheduler.getCronDefinition());
            preStat.setString(4, scheduler.getLastExecution().toString());
            preStat.setString(5, scheduler.getActive());
            preStat.setString(6, scheduler.getUsrCreated());
            preStat.setString(7, scheduler.getDateCreated().toString());
            preStat.setString(8, scheduler.getUsrModif());
            preStat.setString(7, scheduler.getDateModif().toString());
            return (preStat.executeUpdate() == 1);
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        }
        return false;
    }
}
