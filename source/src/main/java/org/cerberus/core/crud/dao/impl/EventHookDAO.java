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
import org.cerberus.core.crud.dao.IEventHookDAO;
import org.cerberus.core.crud.entity.EventHook;
import org.cerberus.core.crud.factory.IFactoryEventHook;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.SqlUtil;
import org.cerberus.core.util.StringUtil;
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
import java.util.Map;

/**
 * @author vertigo17
 */
@Repository
public class EventHookDAO implements IEventHookDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryEventHook factoryEventHook;

    private static final Logger LOG = LogManager.getLogger(EventHookDAO.class);

    private final String OBJECT_NAME = "Event Hook";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public AnswerItem<EventHook> readByKey(Integer id) {
        AnswerItem<EventHook> ans = new AnswerItem<>();
        EventHook result = null;
        final String query = "SELECT * FROM `eventhook` evh WHERE `id` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.id : " + id);
        }
        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            //prepare and execute query
            preStat.setInt(1, id);
            try (ResultSet resultSet = preStat.executeQuery();) {
                //parse query
                if (resultSet.first()) {
                    result = loadFromResultSet(resultSet);
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    ans.setItem(result);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

            }
        } catch (Exception e) {
            LOG.warn("Unable to readByKey Label: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public AnswerList<EventHook> readByEventReferenceByCriteria(List<String> eventReference, List<String> objectKey1, boolean activeOnly, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        AnswerList<EventHook> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<EventHook> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS evh.* FROM `eventhook` evh ");

        searchSQL.append(" where 1=1 ");

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (evh.`id` like ?");
            searchSQL.append(" or evh.`ObjectKey1` like ?");
            searchSQL.append(" or evh.`ObjectKey2` like ?");
            searchSQL.append(" or evh.`EventReference` like ?");
            searchSQL.append(" or evh.`HookConnector` like ?");
            searchSQL.append(" or evh.`HookRecipient` like ?");
            searchSQL.append(" or evh.`HookChannel` like ?");
            searchSQL.append(" or evh.`description` like ?");
            searchSQL.append(" or evh.`usrCreated` like ?");
            searchSQL.append(" or evh.`dateCreated` like ?");
            searchSQL.append(" or evh.`usrModif` like ?");
            searchSQL.append(" or evh.`dateModif` like ?)");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and ");
                searchSQL.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individalColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }

        if (eventReference != null) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("evh.`EventReference`", eventReference));
        }

        if (objectKey1 != null) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("evh.`ObjectKey1`", objectKey1));
        }

        query.append(searchSQL);

        if (!StringUtil.isEmptyOrNull(column)) {
            query.append(" order by ").append(column).append(" ").append(dir);
        }

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param.eventReference : " + eventReference);
            LOG.debug("SQL.param.objectKey1 : " + objectKey1);
        }
        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString());
             Statement stm = connection.createStatement();) {

            int i = 1;
            if (!StringUtil.isEmptyOrNull(searchTerm)) {
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
            }

            for (String individualColumnSearchValue : individalColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }

            if (eventReference != null) {
                for (String evtRef : eventReference) {
                    preStat.setString(i++, evtRef);
                }
            }

            if (objectKey1 != null) {
                for (String objKey1 : objectKey1) {
                    preStat.setString(i++, objKey1);
                }
            }

            try (ResultSet resultSet = preStat.executeQuery();
                 ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()");) {

                //gets the data
                while (resultSet.next()) {
                    objectList.add(this.loadFromResultSet(resultSet));
                }

                int nrTotalRows = 0;

                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }

                if (objectList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                    response = new AnswerList<>(objectList, nrTotalRows);
                } else if (objectList.size() <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    response = new AnswerList<>(objectList, nrTotalRows);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    response = new AnswerList<>(objectList, nrTotalRows);
                }
                response.setDataList(objectList);

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

            }
        } catch (Exception e) {
            LOG.warn("Unable to readBySystemCriteria Label: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            response.setResultMessage(msg);
        }
        return response;
    }

    @Override
    public Answer create(EventHook eventHook) {
        Answer response = new Answer();
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO eventhook (`EventReference`, `ObjectKey1`, `ObjectKey2`, `IsActive`, `HookConnector`, `HookRecipient`, `HookChannel`, `Description`, `usrCreated` ) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("eventreference : " + eventHook.toString());
        }
        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString())) {

            int i = 1;
            preStat.setString(i++, eventHook.getEventReference());
            preStat.setString(i++, eventHook.getObjectKey1());
            preStat.setString(i++, eventHook.getObjectKey2());
            preStat.setBoolean(i++, eventHook.isActive());
            preStat.setString(i++, eventHook.getHookConnector());
            preStat.setString(i++, eventHook.getHookRecipient());
            preStat.setString(i++, eventHook.getHookChannel());
            preStat.setString(i++, eventHook.getDescription());
            preStat.setString(i++, eventHook.getUsrCreated());

            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));

        } catch (Exception e) {
            LOG.warn("Unable to create EventHook: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            response.setResultMessage(msg);
        }

        return response;
    }

    @Override
    public Answer delete(EventHook object) {
        Answer response = new Answer();
        MessageEvent msg = null;
        final String query = "DELETE FROM eventhook WHERE id = ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query)) {
            preStat.setInt(1, object.getId());

            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "DELETE"));
        } catch (Exception e) {
            LOG.warn("Unable to delete Event Hook: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            response.setResultMessage(msg);
        }

        return response;
    }

    @Override
    public Answer update(EventHook object) {
        Answer response = new Answer();
        MessageEvent msg = null;
        final String query = "UPDATE eventhook SET `EventReference` = ?, `ObjectKey1` = ?, `ObjectKey2` = ?, `IsActive` = ?, `HookConnector` = ?, `HookRecipient` = ?, `HookChannel` = ?, `description` = ?"
                + ", `usrModif` = ?, `dateModif` = now()  WHERE id = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query)) {
            int i = 1;
            preStat.setString(i++, object.getEventReference());
            preStat.setString(i++, object.getObjectKey1());
            preStat.setString(i++, object.getObjectKey2());
            preStat.setBoolean(i++, object.isActive());
            preStat.setString(i++, object.getHookConnector());
            preStat.setString(i++, object.getHookRecipient());
            preStat.setString(i++, object.getHookChannel());
            preStat.setString(i++, object.getDescription());
            preStat.setString(i++, object.getUsrModif());
            preStat.setInt(i++, object.getId());

            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));
        } catch (Exception e) {
            LOG.warn("Unable to update Event Hook: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            response.setResultMessage(msg);
        }

        return response;
    }

    @Override
    public EventHook loadFromResultSet(ResultSet rs) throws SQLException {
        Integer id = ParameterParserUtil.parseIntegerParam(rs.getString("evh.id"), 0);
        String eventReference = ParameterParserUtil.parseStringParam(rs.getString("evh.EventReference"), "");
        String objectKey1 = ParameterParserUtil.parseStringParam(rs.getString("evh.ObjectKey1"), "");
        String objectKey2 = ParameterParserUtil.parseStringParam(rs.getString("evh.ObjectKey2"), "");
        boolean isActive = ParameterParserUtil.parseBooleanParam(rs.getString("evh.isActive"), true);
        String hookConnector = ParameterParserUtil.parseStringParam(rs.getString("evh.hookConnector"), "");
        String hookRecipient = ParameterParserUtil.parseStringParam(rs.getString("evh.hookRecipient"), "");
        String hookChannel = ParameterParserUtil.parseStringParam(rs.getString("evh.hookChannel"), "");
        String description = ParameterParserUtil.parseStringParam(rs.getString("evh.description"), "");
        String usrCreated = ParameterParserUtil.parseStringParam(rs.getString("evh.usrCreated"), "");
        Timestamp dateCreated = rs.getTimestamp("evh.dateCreated");
        String usrModif = ParameterParserUtil.parseStringParam(rs.getString("evh.usrModif"), "");
        Timestamp dateModif = rs.getTimestamp("evh.dateModif");
        EventHook evtHookObj = factoryEventHook.create(id, eventReference, objectKey1, objectKey2, isActive, hookConnector, hookRecipient, hookChannel, description, usrCreated, dateCreated, usrModif, dateModif);
        return evtHookObj;
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String system, String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        AnswerList<String> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();

        query.append("SELECT distinct ");
        query.append(columnName);
        query.append(" as distinctValues FROM eventhook evh ");

        searchSQL.append("WHERE 1=1");

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (evh.`id` like ?");
            searchSQL.append(" or evh.`ObjectKey1` like ?");
            searchSQL.append(" or evh.`ObjectKey2` like ?");
            searchSQL.append(" or evh.`EventReference` like ?");
            searchSQL.append(" or evh.`HookConnector` like ?");
            searchSQL.append(" or evh.`HookRecipient` like ?");
            searchSQL.append(" or evh.`HookChannel` like ?");
            searchSQL.append(" or evh.`description` like ?");
            searchSQL.append(" or evh.`usrCreated` like ?");
            searchSQL.append(" or evh.`dateCreated` like ?");
            searchSQL.append(" or evh.`usrModif` like ?");
            searchSQL.append(" or evh.`dateModif` like ?)");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and ");
                searchSQL.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individalColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }

        query.append(searchSQL);
        query.append(" order by ").append(columnName).append(" asc");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString());
             Statement stm = connection.createStatement();) {

            int i = 1;

            if (!StringUtil.isEmptyOrNull(searchTerm)) {
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
            }
            for (String individualColumnSearchValue : individalColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }

            try (ResultSet resultSet = preStat.executeQuery();
                 ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()");) {

                //gets the data
                while (resultSet.next()) {
                    distinctValues.add(resultSet.getString("distinctValues") == null ? "" : resultSet.getString("distinctValues"));
                }

                int nrTotalRows = 0;

                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }

                if (distinctValues.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                } else if (distinctValues.size() <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            }
        } catch (Exception e) {
            LOG.warn("Unable to execute query : " + e.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            // We always set the result message
            answer.setResultMessage(msg);
        }

        answer.setResultMessage(msg);
        answer.setDataList(distinctValues);
        return answer;
    }

}
