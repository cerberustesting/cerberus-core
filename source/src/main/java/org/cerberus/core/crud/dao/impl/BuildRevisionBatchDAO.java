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
import org.cerberus.core.crud.dao.IBuildRevisionBatchDAO;
import org.cerberus.core.crud.entity.BuildRevisionBatch;
import org.cerberus.core.crud.factory.IFactoryBuildRevisionBatch;
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
 * @author vertigo17
 */
@AllArgsConstructor
@Repository
public class BuildRevisionBatchDAO implements IBuildRevisionBatchDAO {

    private final DatabaseSpring databaseSpring;
    private final IFactoryBuildRevisionBatch factoryBuildRevisionBatch;

    private static final Logger LOG = LogManager.getLogger(BuildRevisionBatchDAO.class);
    private static final String OBJECT_NAME = "BuildRevisionBatchDAO";
    private static final String SQL_DUPLICATED_CODE = "23000";
    private static final int MAX_ROW_SELECTED = 100000;

    @Override
    public AnswerItem<BuildRevisionBatch> readByKey(long id) {
        AnswerItem<BuildRevisionBatch> ans = new AnswerItem<>();
        BuildRevisionBatch result;
        final String query = "SELECT * FROM buildrevisionbatch WHERE id = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        LOG.debug("SQL : {}", query);

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            preStat.setLong(1, id);

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
    public AnswerList<BuildRevisionBatch> readByVariousByCriteria(String system, String country, String environment, int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        AnswerList<BuildRevisionBatch> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<BuildRevisionBatch> resultList = new ArrayList<>();

        StringBuilder searchSQL = new StringBuilder();
        StringBuilder query = new StringBuilder();
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM buildrevisionbatch ");
        searchSQL.append(" where 1=1 ");

        if (StringUtil.isNotEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (`id` like ?");
            searchSQL.append(" or `system` like ?");
            searchSQL.append(" or `Country` like ?");
            searchSQL.append(" or `Environment` like ?");
            searchSQL.append(" or `Build` like ?");
            searchSQL.append(" or `Revision` like ?");
            searchSQL.append(" or `Batch` like ?");
            searchSQL.append(" or `DateBatch` like ?)");
        }
        if (StringUtil.isNotEmptyOrNull(individualSearch)) {
            searchSQL.append(" and ( ? )");
        }
        if (StringUtil.isNotEmptyOrNull(system)) {
            searchSQL.append(" and (`system` = ?)");
        }
        if (StringUtil.isNotEmptyOrNull(country)) {
            searchSQL.append(" and (`country` = ?)");
        }
        if (StringUtil.isNotEmptyOrNull(environment)) {
            searchSQL.append(" and (`environment` = ?)");
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
            }
            if (StringUtil.isNotEmptyOrNull(individualSearch)) {
                preStat.setString(i++, individualSearch);
            }
            if (StringUtil.isNotEmptyOrNull(system)) {
                preStat.setString(i++, system);
            }
            if (StringUtil.isNotEmptyOrNull(country)) {
                preStat.setString(i++, country);
            }
            if (StringUtil.isNotEmptyOrNull(environment)) {
                preStat.setString(i, environment);
            }
            try (ResultSet resultSet = preStat.executeQuery();
                 ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {
                while (resultSet.next()) {
                    resultList.add(this.loadFromResultSet(resultSet));
                }

                int nrTotalRows = 0;
                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }
                if (resultList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                } else if (resultList.isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                }
                response = new AnswerList<>(resultList, nrTotalRows);
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        }

        response.setResultMessage(msg);
        response.setDataList(resultList);
        return response;
    }

    @Override
    public Answer create(BuildRevisionBatch buildRevisionBatch) {
        MessageEvent msg;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO buildrevisionbatch (`system`, `Country`, `Environment`, `Build`, `Revision`, `Batch` ) ");
        query.append("VALUES (?,?,?,?,?,?)");

        LOG.debug("SQL : {}", query);

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString())) {

            preStat.setString(1, buildRevisionBatch.getSystem());
            preStat.setString(2, buildRevisionBatch.getCountry());
            preStat.setString(3, buildRevisionBatch.getEnvironment());
            preStat.setString(4, buildRevisionBatch.getBuild());
            preStat.setString(5, buildRevisionBatch.getRevision());
            preStat.setString(6, buildRevisionBatch.getBatch());

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
    public Answer delete(BuildRevisionBatch buildRevisionBatch) {
        MessageEvent msg;
        final String query = "DELETE FROM buildrevisionbatch WHERE id = ? ";

        LOG.debug("SQL : {}", query);

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query)) {

            preStat.setLong(1, buildRevisionBatch.getId());
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
    public Answer update(BuildRevisionBatch buildRevisionBatch) {
        MessageEvent msg;
        final String query = "UPDATE buildrevisionbatch SET `system` = ?, Country = ?, Environment = ?, Build = ?, Revision = ?, "
                + "Batch = ?  WHERE id = ? ";

        LOG.debug("SQL : {}", query);
        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query)) {

            preStat.setString(1, buildRevisionBatch.getSystem());
            preStat.setString(2, buildRevisionBatch.getCountry());
            preStat.setString(3, buildRevisionBatch.getEnvironment());
            preStat.setString(4, buildRevisionBatch.getBuild());
            preStat.setString(5, buildRevisionBatch.getRevision());
            preStat.setString(6, buildRevisionBatch.getBatch());
            preStat.setLong(7, buildRevisionBatch.getId());

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
    public BuildRevisionBatch loadFromResultSet(ResultSet resultSet) throws SQLException {

        long id = ParameterParserUtil.parseLongParam(resultSet.getString("id"), 0);
        String system = ParameterParserUtil.parseStringParam(resultSet.getString("system"), "");
        String country = ParameterParserUtil.parseStringParam(resultSet.getString("country"), "");
        String environment = ParameterParserUtil.parseStringParam(resultSet.getString("environment"), "");
        String build = ParameterParserUtil.parseStringParam(resultSet.getString("build"), "");
        String revision = ParameterParserUtil.parseStringParam(resultSet.getString("revision"), "");
        String batch = ParameterParserUtil.parseStringParam(resultSet.getString("batch"), "");
        Timestamp dateBatch = resultSet.getTimestamp("DateBatch");
        return factoryBuildRevisionBatch.create(id, system, country, environment, build, revision, batch, dateBatch);
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String system, String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        AnswerList<String> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individualColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder()
                .append("SELECT distinct ")
                .append(columnName)
                .append(" as distinctValues FROM buildrevisionbatch ");

        searchSQL.append("WHERE 1=1");
        if (StringUtil.isNotEmptyOrNull(system)) {
            searchSQL.append(" and (`System` = ? )");
        }

        if (StringUtil.isNotEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (`id` like ?");
            searchSQL.append(" or `system` like ?");
            searchSQL.append(" or `Country` like ?");
            searchSQL.append(" or `Environment` like ?");
            searchSQL.append(" or `Build` like ?");
            searchSQL.append(" or `Revision` like ?");
            searchSQL.append(" or `Batch` like ?");
            searchSQL.append(" or `DateBatch` like ?)");
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
                } else if (distinctValues.isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                }
                answer = new AnswerList<>(distinctValues, nrTotalRows);
            }
        } catch (Exception e) {
            LOG.error("Unable to execute query : {}", e.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        }

        answer.setResultMessage(msg);
        answer.setDataList(distinctValues);
        return answer;
    }
}
