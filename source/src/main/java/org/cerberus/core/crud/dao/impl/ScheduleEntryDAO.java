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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.IScheduleEntryDAO;
import org.cerberus.core.crud.entity.ScheduleEntry;
import org.cerberus.core.crud.factory.IFactoryScheduleEntry;
import org.cerberus.core.crud.factory.impl.FactoryScheduleEntry;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
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
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public AnswerItem<ScheduleEntry> readByKey(long id) {
        LOG.debug(id);
        AnswerItem<ScheduleEntry> ans = new AnswerItem<>();
        ScheduleEntry result = null;
        final String query = "SELECT * FROM `scheduleentry` WHERE `id` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.SchedulerDAO : " + id);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            try {
                int i = 1;
                preStat.setLong(i++, id);
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
    public AnswerList<ScheduleEntry> readByName(String name) {
        //LOG.debug("readAllActive is running");
        AnswerList<ScheduleEntry> ans = new AnswerList<>();
        List<ScheduleEntry> objectList = new ArrayList<>();
        final String query = "SELECT * FROM `scheduleentry` WHERE `name` = ?";
        MessageEvent msg = null;
        // Debug message on SQL.        
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.name : " + name);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setString(i++, name);
                try {
                    ResultSet resultSet = preStat.executeQuery();
                    try {
                        objectList = new ArrayList<>();
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
            } catch (Exception exception) {
                LOG.debug("Failed to construct request for read schduler");
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
        ans.setDataList(objectList);
        //sets the message
        return ans;
    }

    @Override
    public AnswerList<ScheduleEntry> readAllActive() {
        //LOG.debug("readAllActive is running");
        AnswerList<ScheduleEntry> ans = new AnswerList<>();
        List<ScheduleEntry> objectList = new ArrayList<>();
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
                    objectList = new ArrayList<>();
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
        ans.setDataList(objectList);
        //sets the message
        return ans;
    }

    private ScheduleEntry loadFromResultSet(ResultSet rs) throws SQLException {
        //LOG.debug("loadFromResultSet scheduleentry");
        long schedulerId = ParameterParserUtil.parseLongParam(rs.getString("scheduleentry.ID"), 0);
        String type = ParameterParserUtil.parseStringParam(rs.getString("scheduleentry.type"), "");
        String name = ParameterParserUtil.parseStringParam(rs.getString("scheduleentry.name"), "");
        String cronDefinition = ParameterParserUtil.parseStringParam(rs.getString("scheduleentry.cronDefinition"), "");
        String active = ParameterParserUtil.parseStringParam(rs.getString("scheduleentry.active"), "");
        String desc = ParameterParserUtil.parseStringParam(rs.getString("scheduleentry.description"), "");
        String usrCreated = ParameterParserUtil.parseStringParam(rs.getString("scheduleentry.UsrCreated"), "");
        String usrModif = ParameterParserUtil.parseStringParam(rs.getString("scheduleentry.UsrModif"), "");
        Timestamp lastExecution = rs.getTimestamp("scheduleentry.lastExecution");
        Timestamp dateModif = rs.getTimestamp("scheduleentry.DateModif");
        Timestamp dateCreated = rs.getTimestamp("scheduleentry.DateCreated");
        ScheduleEntry newScheduleEntry = factoryscheduleentry.create(schedulerId, type, name, cronDefinition, lastExecution, active, desc, usrCreated, dateCreated, usrModif, dateModif);
        return newScheduleEntry;

    }

    @Override
    public AnswerItem<Integer> create(ScheduleEntry scheduler) {
        MessageEvent msg = null;
        AnswerItem<Integer> ans = new AnswerItem<>();
        final StringBuilder query = new StringBuilder("INSERT INTO `scheduleentry` (`type`, `name`,`cronDefinition`,`active`,`description`,`UsrCreated`) VALUES ( ?, ?, ?, ?, ?, ?);");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.name : " + scheduler.getName());
            LOG.debug("SQL.param.crondefinition : " + scheduler.getCronDefinition());
            LOG.debug("SQL.param.usrCreated : " + scheduler.getUsrCreated());
        }
        Connection connection = this.databaseSpring.connect();
        try {

            PreparedStatement preStat = connection.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);

            try {
                int i = 1;
                preStat.setString(i++, scheduler.getType());
                preStat.setString(i++, scheduler.getName());
                preStat.setString(i++, scheduler.getCronDefinition());
                preStat.setString(i++, scheduler.getActive());
                preStat.setString(i++, scheduler.getDescription());
                preStat.setString(i++, scheduler.getUsrCreated());
                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));
                ans.setItem(MAX_ROW_SELECTED);
                ResultSet resultSet = preStat.getGeneratedKeys();

                try {
                    if (resultSet.first()) {
                        LOG.debug("ID of new job " + resultSet.getInt(1));
                        ans.setItem(resultSet.getInt(1));
                    }
                } catch (Exception e) {
                    LOG.debug("Exception catch :", e);
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());

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
    public Answer update(ScheduleEntry scheduleEntryObject) {
        MessageEvent msg = null;
        String query = "UPDATE scheduleentry SET type = ? , name = ?, cronDefinition = ?,lastExecution = ?, active = ?, description = ?, DateModif = CURRENT_TIMESTAMP, UsrModif = ? WHERE ID = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.name : " + scheduleEntryObject.getName());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {

                int i = 1;
                preStat.setString(i++, scheduleEntryObject.getType());
                preStat.setString(i++, scheduleEntryObject.getName());
                preStat.setString(i++, scheduleEntryObject.getCronDefinition());
                preStat.setTimestamp(i++, scheduleEntryObject.getLastExecution());
                preStat.setString(i++, scheduleEntryObject.getActive());
                preStat.setString(i++, scheduleEntryObject.getDescription());
                preStat.setString(i++, scheduleEntryObject.getUsrModif());
                preStat.setLong(i++, scheduleEntryObject.getID());

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

    @Override
    public Answer updateLastExecution(long schedulerId, Timestamp lastExecution) {
        MessageEvent msg = null;
        String query = "UPDATE scheduleentry SET lastExecution = ? WHERE ID = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {

                int i = 1;
                preStat.setTimestamp(i++, lastExecution);
                preStat.setLong(i++, schedulerId);

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

    @Override
    public Answer delete(ScheduleEntry object) {
        MessageEvent msg = null;
        final String query = "DELETE FROM scheduleentry WHERE id = ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.parm.id : " + object.getID());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setLong(1, object.getID());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "DELETE"));
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
