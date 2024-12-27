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

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.IAppServiceHeaderDAO;
import org.cerberus.core.crud.entity.AppServiceHeader;
import org.cerberus.core.crud.factory.IFactoryAppServiceHeader;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.SqlUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
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
 * Implements methods defined on IApplicationDAO
 *
 * @author tbernardes
 * @version 1.0, 15/10/13
 * @since 0.9.0
 */
@AllArgsConstructor
@Repository
public class AppServiceHeaderDAO implements IAppServiceHeaderDAO {

    private final DatabaseSpring databaseSpring;
    private final IFactoryAppServiceHeader factoryAppServiceHeader;
    private static final Logger LOG = LogManager.getLogger(AppServiceHeaderDAO.class);
    private static final String OBJECT_NAME = "Service Http Header";
    private static final String SQL_DUPLICATED_CODE = "23000";
    private static final int MAX_ROW_SELECTED = 100000;

    @Override
    public AnswerItem<AppServiceHeader> readByKey(String service, String key) {
        AnswerItem<AppServiceHeader> ans = new AnswerItem<>();
        AppServiceHeader result;
        final String query = "SELECT * FROM `appserviceheader` srh WHERE `service` = ? and `key` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.service : {}", service);
        LOG.debug("SQL.param.key : {}", key);


        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

            preStat.setString(1, service);
            preStat.setString(2, key);

            try (ResultSet resultSet = preStat.executeQuery()) {
                if (resultSet.first()) {
                    result = loadFromResultSet(resultSet);
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    ans.setItem(result);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                }
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }

        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public AnswerList<AppServiceHeader> readByVariousByCriteria(String service, boolean withActiveCriteria, boolean isActive, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        AnswerList<AppServiceHeader> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<AppServiceHeader> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individualColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM appserviceheader srh ");
        searchSQL.append(" where 1=1 ");

        if (StringUtil.isNotEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (srh.`service` like ?");
            searchSQL.append(" or srh.`key` like ?");
            searchSQL.append(" or srh.`value` like ?");
            searchSQL.append(" or srh.`sort` like ?");
            searchSQL.append(" or srh.`isActive` like ?");
            searchSQL.append(" or srh.`usrCreated` like ?");
            searchSQL.append(" or srh.`usrModif` like ?");
            searchSQL.append(" or srh.`dateCreated` like ?");
            searchSQL.append(" or srh.`dateModif` like ?");
            searchSQL.append(" or srh.`description` like ?)");
        }
        if (MapUtils.isNotEmpty(individualSearch)) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and ");
                searchSQL.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individualColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }

        if (StringUtil.isNotEmptyOrNull(service)) {
            searchSQL.append(" and (srh.`service` = ? )");
        }

        if (withActiveCriteria) {
            searchSQL.append(" and (srh.`isActive` = ? )");
        }
        query.append(searchSQL);

        if (StringUtil.isNotEmptyOrNull(column)) {
            query.append(" order by `").append(column).append("` ").append(dir);
        }

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        LOG.debug("SQL : {}", query);

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString());
             Statement stm = connection.createStatement()) {

            int i = 1;
            if (StringUtil.isNotEmptyOrNull(searchTerm)) {
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
            for (String individualColumnSearchValue : individualColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }
            if (StringUtil.isNotEmptyOrNull(service)) {
                preStat.setString(i++, service);
            }
            if (withActiveCriteria) {
                preStat.setBoolean(i, isActive);
            }

            try (ResultSet resultSet = preStat.executeQuery();
                 ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {
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
                } else if (objectList.isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    response = new AnswerList<>(objectList, nrTotalRows);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    response = new AnswerList<>(objectList, nrTotalRows);
                }
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }

        response.setResultMessage(msg);
        response.setDataList(objectList);
        return response;
    }

    @Override
    public Answer create(AppServiceHeader object) {
        MessageEvent msg;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO appserviceheader (`service`, `key`, `value`, `sort`, `isActive`, `description`, `usrcreated`) ");
        query.append("VALUES (?,?,?,?,?,?,?)");

        LOG.debug("SQL : {}", query);

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString())) {

            int i = 1;
            preStat.setString(i++, object.getService());
            preStat.setString(i++, object.getKey());
            preStat.setString(i++, object.getValue());
            preStat.setInt(i++, object.getSort());
            preStat.setBoolean(i++, object.isActive());
            preStat.setString(i++, object.getDescription());
            preStat.setString(i, object.getUsrCreated());

            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());

            if (exception.getSQLState().equals(SQL_DUPLICATED_CODE)) { //23000 is the sql state for duplicate entries
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_DUPLICATE);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT").replace("%REASON%", exception.toString()));
            } else {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            }
        }

        return new Answer(msg);
    }

    @Override
    public Answer delete(AppServiceHeader object) {
        MessageEvent msg;
        final String query = "DELETE FROM appserviceheader WHERE `service` = ? and `key` = ? ";

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.service : {}", object.getService());
        LOG.debug("SQL.param.key : {}", object.getKey());


        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query)) {

            int i = 1;
            preStat.setString(i++, object.getService());
            preStat.setString(i, object.getKey());

            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "DELETE"));
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        return new Answer(msg);
    }

    @Override
    public Answer update(String service, String key, AppServiceHeader object) {
        MessageEvent msg;
        final String query = "UPDATE appserviceheader SET `Service` = ?, `Key` = ?, description = ?, sort = ?, `IsActive` = ?, `value` = ?, "
                + "dateModif = NOW(), usrModif= ?  WHERE `Service` = ? and `Key` = ?";

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.service : {}", object.getService());
        LOG.debug("SQL.param.key : {}", object.getKey());


        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query)
        ) {

            int i = 1;
            preStat.setString(i++, object.getService());
            preStat.setString(i++, object.getKey());
            preStat.setString(i++, object.getDescription());
            preStat.setInt(i++, object.getSort());
            preStat.setBoolean(i++, object.isActive());
            preStat.setString(i++, object.getValue());
            preStat.setString(i++, object.getUsrModif());
            preStat.setString(i++, service);
            preStat.setString(i, key);

            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        return new Answer(msg);
    }
    
    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String system, String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        AnswerList<String> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individualColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();

        query.append("SELECT distinct ");
        query.append(columnName);
        query.append(" as distinctValues FROM appserviceheader ");

        searchSQL.append("WHERE 1=1");
        if (StringUtil.isNotEmptyOrNull(system)) {
            searchSQL.append(" and (`System` = ? )");
        }

        if (StringUtil.isNotEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (srh.`service` like ?");
            searchSQL.append(" or srh.`key` like ?");
            searchSQL.append(" or srh.`value` like ?");
            searchSQL.append(" or srh.`sort` like ?");
            searchSQL.append(" or srh.`isActive` like ?");
            searchSQL.append(" or srh.`usrCreated` like ?");
            searchSQL.append(" or srh.`usrModif` like ?");
            searchSQL.append(" or srh.`dateCreated` like ?");
            searchSQL.append(" or srh.`dateModif` like ?");
            searchSQL.append(" or srh.`description` like ?)");
        }
        if (MapUtils.isNotEmpty(individualSearch)) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and ");
                searchSQL.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individualColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }
        query.append(searchSQL);
        query.append(" order by ").append(columnName).append(" asc");

        LOG.debug("SQL : {}", query);

        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString());
             Statement stm = connection.createStatement()) {

            int i = 1;
            if (StringUtil.isNotEmptyOrNull(system)) {
                preStat.setString(i++, system);
            }
            if (StringUtil.isNotEmptyOrNull(searchTerm)) {
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
            for (String individualColumnSearchValue : individualColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }

            try (ResultSet resultSet = preStat.executeQuery();
                 ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {
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
                } else if (distinctValues.isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                }
            }
        } catch (Exception e) {
            LOG.warn("Unable to execute query : {}", e.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        }
        answer.setResultMessage(msg);
        answer.setDataList(distinctValues);
        return answer;
    }

    @Override
    public AppServiceHeader loadFromResultSet(ResultSet rs) throws SQLException {
        String service = ParameterParserUtil.parseStringParam(rs.getString("srh.service"), "");
        String key = ParameterParserUtil.parseStringParam(rs.getString("srh.key"), "");
        String value = ParameterParserUtil.parseStringParam(rs.getString("srh.value"), "");
        int sort = ParameterParserUtil.parseIntegerParam(rs.getString("srh.sort"), 0);
        boolean isActive = rs.getBoolean("srh.isActive");
        String description = ParameterParserUtil.parseStringParam(rs.getString("srh.description"), "");
        String usrModif = ParameterParserUtil.parseStringParam(rs.getString("srh.UsrModif"), "");
        String usrCreated = ParameterParserUtil.parseStringParam(rs.getString("srh.UsrCreated"), "");
        Timestamp dateModif = rs.getTimestamp("srh.DateModif");
        Timestamp dateCreated = rs.getTimestamp("srh.DateCreated");

        return factoryAppServiceHeader.create(service, key, value, isActive, sort, description,
                usrCreated, dateCreated, usrModif, dateModif);
    }
}
