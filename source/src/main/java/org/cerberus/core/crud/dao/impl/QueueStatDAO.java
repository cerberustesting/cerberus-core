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
import org.cerberus.core.crud.dao.IQueueStatDAO;
import org.cerberus.core.crud.entity.QueueStat;
import org.cerberus.core.crud.factory.IFactoryQueueStat;
import org.cerberus.core.crud.service.IParameterService;
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
import java.util.Date;
import java.util.List;

/**
 * Implements methods defined on IApplicationDAO
 *
 * @author tbernardes
 * @version 1.0, 15/10/13
 * @since 0.9.0
 */
@Repository
public class QueueStatDAO implements IQueueStatDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryQueueStat factoryQueueStat;
    @Autowired
    private IParameterService parameterService;

    private static final Logger LOG = LogManager.getLogger(QueueStatDAO.class);

    private final String OBJECT_NAME = "QueueStat";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 50000;

    @Override
    public AnswerList<QueueStat> readByCriteria(Date from, Date to, int modulo) {
        AnswerList<QueueStat> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<QueueStat> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();
        Timestamp t1;

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM queuestat ");

        searchSQL.append(" where 1=1 ");

        searchSQL.append(" and DateCreated > ? and DateCreated < ? ");

        query.append(searchSQL);

        query.append(" order by ID desc");

        query.append(" limit ").append(0).append(" , ").append(MAX_ROW_SELECTED);

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param.modulo : " + modulo);
        }

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString());
             Statement stm = connection.createStatement();) {

            int i = 1;
            t1 = new Timestamp(from.getTime());
            preStat.setTimestamp(i++, t1);
            t1 = new Timestamp(to.getTime());
            preStat.setTimestamp(i++, t1);

            try (ResultSet resultSet = preStat.executeQuery();
                 ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()");) {
                //gets the data
                int c = 0;
                while (resultSet.next()) {
                    if (modulo != 0) {
                        int t = c++ % modulo;
                        if (t == 0) {
                            objectList.add(this.loadFromResultSet_light(resultSet));
                        }
                    } else {
                        objectList.add(this.loadFromResultSet_light(resultSet));
                    }
                }

                //get the total number of rows
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

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        response.setResultMessage(msg);
        response.setDataList(objectList);
        return response;
    }

    @Override
    public AnswerItem<Integer> readNbRowsByCriteria(Date from, Date to) {
        AnswerItem<Integer> response = new AnswerItem<>();
        Integer result = 0;
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        StringBuilder searchSQL = new StringBuilder();
        Timestamp t1;

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that
        //were applied -- used for pagination p
        query.append("SELECT count(*) FROM queuestat ");

        searchSQL.append(" where 1=1 ");

        searchSQL.append(" and DateCreated > ? and DateCreated < ? ");

        query.append(searchSQL);

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             Statement stm = connection.createStatement();) {

            int i = 1;
            t1 = new Timestamp(from.getTime());
            preStat.setTimestamp(i++, t1);
            t1 = new Timestamp(to.getTime());
            preStat.setTimestamp(i++, t1);

            try (ResultSet resultSet = preStat.executeQuery()) {
                resultSet.first();
                //gets the data
                result = resultSet.getInt(1);

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        response.setResultMessage(msg);
        response.setItem(result);
        return response;
    }

    @Override
    public Answer create(QueueStat object) {
        Answer ans = new Answer();
        MessageEvent msg = null;

        final String query = "INSERT INTO `queuestat` (`globalConstrain`,`currentlyRunning`,`queueSize`,`usrcreated`) VALUES (?, ?, ?, ?)";
        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query)) {
            // Prepare and execute query
            preStat.setInt(1, object.getGlobalConstrain());
            preStat.setInt(2, object.getCurrentlyRunning());
            preStat.setInt(3, object.getQueueSize());
            preStat.setString(4, object.getUsrCreated());
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "CREATE");
        } catch (Exception e) {
            LOG.error("Unable to create QueueStat : " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    private QueueStat loadFromResultSet(ResultSet rs) throws SQLException {
        long id = ParameterParserUtil.parseLongParam(rs.getString("id"), -1);
        Integer globalConstrain = ParameterParserUtil.parseIntegerParam(rs.getString("globalConstrain"), -1);
        Integer currentlyRunning = ParameterParserUtil.parseIntegerParam(rs.getString("currentlyRunning"), -1);
        Integer queueSize = ParameterParserUtil.parseIntegerParam(rs.getString("queueSize"), -1);
        String usrcreated = ParameterParserUtil.parseStringParam(rs.getString("UsrCreated"), "");
        Timestamp datecreated = rs.getTimestamp("DateCreated");
        String usrmodif = ParameterParserUtil.parseStringParam(rs.getString("UsrModif"), "");
        Timestamp datemodif = rs.getTimestamp("DateModif");

        return factoryQueueStat.create(id, globalConstrain, currentlyRunning, queueSize, usrcreated, datecreated, usrmodif, datemodif);
    }

    private QueueStat loadFromResultSet_light(ResultSet rs) throws SQLException {
        long id = -1;
        Integer globalConstrain = ParameterParserUtil.parseIntegerParam(rs.getString("globalConstrain"), -1);
        Integer currentlyRunning = ParameterParserUtil.parseIntegerParam(rs.getString("currentlyRunning"), -1);
        Integer queueSize = ParameterParserUtil.parseIntegerParam(rs.getString("queueSize"), -1);
        String usrcreated = null;
        Timestamp datecreated = rs.getTimestamp("DateCreated");
        String usrmodif = null;
        Timestamp datemodif = null;

        return factoryQueueStat.create(id, globalConstrain, currentlyRunning, queueSize, usrcreated, datecreated, usrmodif, datemodif);
    }
}
