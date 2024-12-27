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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.IAppServiceDAO;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.factory.IFactoryAppService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.SqlUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.security.UserSecurity;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

/**
 * {Insert class description here}
 *
 * @author cte
 */
@AllArgsConstructor
@Repository
public class AppServiceDAO implements IAppServiceDAO {

    private final DatabaseSpring databaseSpring;
    private final IFactoryAppService factoryAppService;
    private final IParameterService parameterService;

    private static final Logger LOG = LogManager.getLogger(AppServiceDAO.class);
    private static final String OBJECT_NAME = "AppService";
    private static final int MAX_ROW_SELECTED = 100000;
    private static final String SQL_DUPLICATED_CODE = "23000";
    private static final String SQL_ERROR_UNABLETOEXECUTEQUERY = "Unable to execute query : {}";
    private static final String SQL_MESSAGE = "SQL : {}";

    @Override
    public AppService findAppServiceByKey(String service) throws CerberusException {
        AppService result = null;
        final String query = "SELECT * FROM appservice srv WHERE `service` = ?";

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {
            preStat.setString(1, service);

            try (ResultSet resultSet = preStat.executeQuery()) {
                if (resultSet.next()) {
                    result = this.loadFromResultSet(resultSet);
                }
            }
        } catch (SQLException exception) {
            LOG.warn(SQL_ERROR_UNABLETOEXECUTEQUERY, exception.toString());
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }

        return result;
    }

    @Override
    public AnswerList<AppService> findAppServiceByLikeName(String service, int limit) {
        AnswerList<AppService> response = new AnswerList<>();
        final String query = "SELECT * FROM appservice srv WHERE `service` LIKE ? limit ?";
        List<AppService> objectList = new ArrayList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        LOG.debug(SQL_MESSAGE, query);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query); Statement stm = connection.createStatement()) {

            preStat.setString(1, "%" + service + "%");
            preStat.setInt(2, limit);

            try (ResultSet resultSet = preStat.executeQuery(); ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {
                while (resultSet.next()) {
                    objectList.add(this.loadFromResultSet(resultSet));
                }
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));

                int nrTotalRows = 0;
                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }
                response = new AnswerList<>(objectList, nrTotalRows);
            }
        } catch (SQLException exception) {
            LOG.warn(SQL_ERROR_UNABLETOEXECUTEQUERY, exception.toString());
        }

        response.setResultMessage(msg);
        response.setDataList(objectList);
        return response;
    }

    @Override
    public AnswerList<AppService> readByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch, List<String> systems) {

        AnswerList<AppService> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<AppService> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individualColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder()
                .append("SELECT SQL_CALC_FOUND_ROWS * FROM appservice srv ")
                .append("left outer JOIN application app ON srv.application = app.application");

        query.append(" WHERE 1=1");

        if (StringUtil.isNotEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (srv.Service like ?");
            searchSQL.append(" or srv.Application like ?");
            searchSQL.append(" or srv.Type like ?");
            searchSQL.append(" or srv.ServicePath like ?");
            searchSQL.append(" or srv.Method like ?");
            searchSQL.append(" or srv.Operation like ?");
            searchSQL.append(" or srv.ServiceRequest like ?");
            searchSQL.append(" or srv.KafkaTopic like ?");
            searchSQL.append(" or srv.KafkaKey like ?");
            searchSQL.append(" or srv.KafkaFilterPath like ?");
            searchSQL.append(" or srv.KafkaFilterValue like ?");
            searchSQL.append(" or srv.KafkaFilterHeaderPath like ?");
            searchSQL.append(" or srv.KafkaFilterHeaderValue like ?");
            searchSQL.append(" or srv.SchemaRegistryUrl like ?");
            searchSQL.append(" or srv.AttachementURL like ?");
            searchSQL.append(" or srv.Collection like ?");
            searchSQL.append(" or srv.Description like ?");
            searchSQL.append(" or srv.UsrCreated like ?");
            searchSQL.append(" or srv.DateCreated like ?");
            searchSQL.append(" or srv.UsrModif like ?");
            searchSQL.append(" or srv.DateModif like ?)");
        }
        if (MapUtils.isNotEmpty(individualSearch)) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and ");
                String q = SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue());
                if (q == null || "".equals(q)) {
                    q = "(" + entry.getKey() + " IS NULL OR " + entry.getKey() + " = '')";
                }
                searchSQL.append(q);
                individualColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }

        query.append(searchSQL);

        if (CollectionUtils.isNotEmpty(systems)) {
            systems.add(""); // authorize transversal object
            query.append(" and ( app.Application is null or ");
            query.append(SqlUtil.generateInClause("app.system", systems));
            query.append(" ) ");
        }

        query.append(" AND ( app.Application is null or ");
        query.append(UserSecurity.getSystemAllowForSQL("app.system"));
        query.append(" ) ");

        if (StringUtil.isNotEmptyOrNull(column)) {
            query.append(" order by ").append(column).append(" ").append(dir);
        }

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        LOG.debug(SQL_MESSAGE, query);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); Statement stm = connection.createStatement()) {

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
            for (String individualColumnSearchValue : individualColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }

            if (CollectionUtils.isNotEmpty(systems)) {
                for (String system : systems) {
                    preStat.setString(i++, system);
                }
            }

            try (ResultSet resultSet = preStat.executeQuery(); ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {
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
                } else if (objectList.isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                }
                response = new AnswerList<>(objectList, nrTotalRows);
            }
        } catch (SQLException exception) {
            LOG.error(SQL_ERROR_UNABLETOEXECUTEQUERY, exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }

        response.setResultMessage(msg);
        response.setDataList(objectList);
        return response;
    }

    @Override
    public Integer getNbServices(List<String> systems) {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        StringBuilder query = new StringBuilder();
        query.append("SELECT SQL_CALC_FOUND_ROWS count(*) FROM appservice srv ");
        query.append(" LEFT OUTER JOIN application app ON app.application = srv.application ");
        query.append(" where 1=1 ");

        if (CollectionUtils.isNotEmpty(systems)) {
            systems.add(""); // authorize transversal object
            query.append(" and ( app.Application is null or ");
            query.append(SqlUtil.generateInClause("app.system", systems));
            query.append(" ) ");
        }

        LOG.debug("SQL : {}", query);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); Statement stm = connection.createStatement()) {
            int i = 1;
            if (CollectionUtils.isNotEmpty(systems)) {
                for (String system : systems) {
                    preStat.setString(i++, system);
                }
            }

            try (ResultSet resultSet = preStat.executeQuery(); ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {

                while (resultSet.next()) {
                    return resultSet.getInt(1);
                }

                int nrTotalRows = 0;
                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }

            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }

        return 0;
    }

    @Override
    public AnswerItem<AppService> readByKey(String key) {
        AnswerItem<AppService> ans = new AnswerItem<>();
        AppService result;
        final String query = "SELECT * FROM `appservice` srv WHERE `service` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        LOG.debug(SQL_MESSAGE, query);
        LOG.debug("SQL.param.service : {}", key);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

            preStat.setString(1, key);
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
            LOG.error(SQL_ERROR_UNABLETOEXECUTEQUERY, exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }

        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        AnswerList<String> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individualColumnSearchValues = new ArrayList<>();
        StringBuilder query = new StringBuilder();

        query.append("SELECT distinct ");
        query.append(columnName);
        query.append(" as distinctValues FROM appservice srv");
        query.append(" where 1=1");

        if (StringUtil.isNotEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (srv.Service like ?");
            searchSQL.append(" or srv.Collection like ?");
            searchSQL.append(" or srv.ServicePath like ?");
            searchSQL.append(" or srv.Operation like ?");
            searchSQL.append(" or srv.KafkaTopic like ?");
            searchSQL.append(" or srv.KafkaKey like ?");
            searchSQL.append(" or srv.KafkaFilterPath like ?");
            searchSQL.append(" or srv.KafkaFilterValue like ?");
            searchSQL.append(" or srv.AttachementURL like ?");
            searchSQL.append(" or srv.Description like ?");
            searchSQL.append(" or srv.ServiceRequest like ?)");
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
        query.append(" group by ").append(columnName);
        query.append(" order by ").append(columnName).append(" asc");

        LOG.debug(SQL_MESSAGE, query);

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); Statement stm = connection.createStatement()) {

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
            }
            for (String individualColumnSearchValue : individualColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }

            try (ResultSet resultSet = preStat.executeQuery(); ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {

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
            LOG.error(SQL_ERROR_UNABLETOEXECUTEQUERY, e.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED)
                    .resolveDescription("DESCRIPTION", e.toString());
        }

        answer.setResultMessage(msg);
        answer.setDataList(distinctValues);
        return answer;
    }

    @Override
    public Answer create(AppService object) {
        MessageEvent msg;
        StringBuilder query = new StringBuilder()
                .append("INSERT INTO appservice (`Service`, `Collection`, `Application`, `Type`, `Method`, `ServicePath`, `isFollowRedir`, `Operation`, `BodyType`, `ServiceRequest`, ")
                .append("   `isAvroEnable`, `SchemaRegistryUrl`,  `isAvroEnableKey`, `AvroSchemaKey`,  `isAvroEnableValue`, `AvroSchemaValue`, `ParentContentService`, `KafkaTopic`, `KafkaKey`, ")
                .append("   `KafkaFilterPath`, `KafkaFilterValue`, `KafkaFilterHeaderPath`, `KafkaFilterHeaderValue`, `AttachementURL`, `Description`, `FileName`, `SimulationParameters`, ")
                .append("   `AuthType`, `AuthUser`, `AuthPassword`, `AuthAddTo`, `UsrCreated`) ")
                .append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        LOG.debug(SQL_MESSAGE, query);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString())) {

            int i = 1;
            preStat.setString(i++, object.getService());
            preStat.setString(i++, object.getCollection());
            if (StringUtil.isNotEmptyOrNull(object.getApplication())) {
                preStat.setString(i++, object.getApplication());
            } else {
                preStat.setString(i++, null);
            }
            preStat.setString(i++, object.getType());
            preStat.setString(i++, object.getMethod());
            preStat.setString(i++, object.getServicePath());
            preStat.setBoolean(i++, object.isFollowRedir());
            preStat.setString(i++, object.getOperation());
            preStat.setString(i++, object.getBodyType());
            preStat.setString(i++, object.getServiceRequest());
            preStat.setBoolean(i++, object.isAvroEnable());
            preStat.setString(i++, object.getSchemaRegistryURL());
            preStat.setBoolean(i++, object.isAvroEnableKey());
            preStat.setString(i++, object.getAvroSchemaKey());
            preStat.setBoolean(i++, object.isAvroEnableValue());
            preStat.setString(i++, object.getAvroSchemaValue());
            if (StringUtil.isNotEmptyOrNull(object.getParentContentService())) {
                preStat.setString(i++, object.getParentContentService());
            } else {
                preStat.setString(i++, null);
            }
            preStat.setString(i++, object.getKafkaTopic());
            preStat.setString(i++, object.getKafkaKey());
            preStat.setString(i++, object.getKafkaFilterPath());
            preStat.setString(i++, object.getKafkaFilterValue());
            preStat.setString(i++, object.getKafkaFilterHeaderPath());
            preStat.setString(i++, object.getKafkaFilterHeaderValue());
            preStat.setString(i++, object.getAttachementURL());
            preStat.setString(i++, object.getDescription());
            preStat.setString(i++, object.getFileName());
            preStat.setString(i++, object.getSimulationParameters().toString());
            preStat.setString(i++, object.getAuthType());
            preStat.setString(i++, object.getAuthUser());
            preStat.setString(i++, object.getAuthPassword());
            preStat.setString(i++, object.getAuthAddTo());
            preStat.setString(i, object.getUsrCreated());

            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));
        } catch (SQLException exception) {
            LOG.error(SQL_ERROR_UNABLETOEXECUTEQUERY, exception.toString());
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
    public Answer update(String service, AppService object) {
        MessageEvent msg;
        StringBuilder query = new StringBuilder()
                .append("UPDATE appservice srv SET `Service` = ?, `Collection` = ?, `ServicePath` = ?, `isFollowRedir` = ?, `Operation` = ?, BodyType = ?, ServiceRequest = ?, ")
                .append("`isAvroEnable` = ?, `SchemaRegistryUrl` = ?, `isAvroEnableKey` = ?, `AvroSchemaKey` = ?, `isAvroEnableValue` = ?, `AvroSchemaValue` = ?, ParentContentService = ?, KafkaTopic = ?, KafkaKey = ?, ")
                .append("KafkaFilterPath = ?, KafkaFilterValue = ?, KafkaFilterHeaderPath = ?, KafkaFilterHeaderValue = ?, AttachementURL = ?, SimulationParameters = ?, ")
                .append("`Description` = ?, `Type` = ?, Method = ?, AuthType = ?, AuthUser = ?, AuthPassword = ?, AuthAddTo = ?, `UsrModif`= ?, `DateModif` = NOW(), `FileName` = ?");
        if ((object.getApplication() != null) && (!object.getApplication().isEmpty())) {
            query.append(" ,Application = ?");
        } else {
            query.append(" ,Application = null");
        }
        query.append(" WHERE `Service` = ?");

        LOG.debug(SQL_MESSAGE, query);
        LOG.debug("SQL.param.service (new) : {}", object.getService());
        LOG.debug("SQL.param.application : {}", object.getApplication());
        LOG.debug("SQL.param.service : {}", service);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString())) {

            int i = 1;
            preStat.setString(i++, object.getService());
            preStat.setString(i++, object.getCollection());
            preStat.setString(i++, object.getServicePath());
            preStat.setBoolean(i++, object.isFollowRedir());
            preStat.setString(i++, object.getOperation());
            preStat.setString(i++, object.getBodyType());
            preStat.setString(i++, object.getServiceRequest());
            preStat.setBoolean(i++, object.isAvroEnable());
            preStat.setString(i++, object.getSchemaRegistryURL());
            preStat.setBoolean(i++, object.isAvroEnableKey());
            preStat.setString(i++, object.getAvroSchemaKey());
            preStat.setBoolean(i++, object.isAvroEnableValue());
            preStat.setString(i++, object.getAvroSchemaValue());
            if (StringUtil.isEmptyOrNull(object.getParentContentService())) {
                preStat.setString(i++, null);
            } else {
                preStat.setString(i++, object.getParentContentService());
            }
            preStat.setString(i++, object.getKafkaTopic());
            preStat.setString(i++, object.getKafkaKey());
            preStat.setString(i++, object.getKafkaFilterPath());
            preStat.setString(i++, object.getKafkaFilterValue());
            preStat.setString(i++, object.getKafkaFilterHeaderPath());
            preStat.setString(i++, object.getKafkaFilterHeaderValue());
            preStat.setString(i++, object.getAttachementURL());
            preStat.setString(i++, object.getSimulationParameters().toString());
            preStat.setString(i++, object.getDescription());
            preStat.setString(i++, object.getType());
            preStat.setString(i++, object.getMethod());
            preStat.setString(i++, object.getAuthType());
            preStat.setString(i++, object.getAuthUser());
            preStat.setString(i++, object.getAuthPassword());
            preStat.setString(i++, object.getAuthAddTo());
            preStat.setString(i++, object.getUsrModif());
            preStat.setString(i++, object.getFileName());
            if (StringUtil.isNotEmptyOrNull(object.getApplication())) {
                preStat.setString(i++, object.getApplication());
            }
            preStat.setString(i, service);

            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));
        } catch (SQLException exception) {
            LOG.error(SQL_ERROR_UNABLETOEXECUTEQUERY, exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        return new Answer(msg);
    }

    @Override
    public Answer delete(AppService object) {
        MessageEvent msg;
        final String query = "DELETE FROM appservice WHERE `Service` = ? ";

        LOG.debug(SQL_MESSAGE, query);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {

            preStat.setString(1, object.getService());
            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "DELETE"));
        } catch (SQLException exception) {
            LOG.error(SQL_ERROR_UNABLETOEXECUTEQUERY, exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }

        return new Answer(msg);
    }

    @Override
    public Answer uploadFile(String service, FileItem file) {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                "cerberus_ftpfile_path Parameter not found");
        AnswerItem<Parameter> a = parameterService.readByKey("", "cerberus_ftpfile_path");
        if (a.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            Parameter p = a.getItem();
            String uploadPath = p.getValue();
            File appDir = new File(uploadPath + File.separator + service);
            if (!appDir.exists()) {
                try {
                    appDir.mkdirs();
                } catch (SecurityException se) {
                    LOG.warn("Unable to create ftp local file dir: {}", se.getMessage());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                            se.toString());
                    a.setResultMessage(msg);
                }
            }
            if (a.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                deleteFolder(appDir, false);
                File picture = new File(uploadPath + File.separator + service + File.separator + file.getName());
                try {
                    file.write(picture);
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("DESCRIPTION",
                            "ftp local file uploaded");
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "FTP Local File").replace("%OPERATION%", "Upload"));
                } catch (Exception e) {
                    LOG.warn("Unable to upload ftp local file: {}", e.getMessage());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                            e.toString());
                }
            }
        } else {
            LOG.warn("cerberus_ftpfile_path Parameter not found");
        }
        a.setResultMessage(msg);
        return a;
    }

    @Override
    public AppService loadFromResultSet(ResultSet rs) throws SQLException {
        String service = ParameterParserUtil.parseStringParam(rs.getString("srv.Service"), "");
        String collection = ParameterParserUtil.parseStringParam(rs.getString("srv.Collection"), "");
        String servicePath = ParameterParserUtil.parseStringParam(rs.getString("srv.ServicePath"), "");
        String operation = ParameterParserUtil.parseStringParam(rs.getString("srv.Operation"), "");
        String bodyType = ParameterParserUtil.parseStringParam(rs.getString("srv.BodyType"), "");
        String serviceRequest = ParameterParserUtil.parseStringParam(rs.getString("srv.ServiceRequest"), "");
        String attachementURL = ParameterParserUtil.parseStringParam(rs.getString("srv.AttachementURL"), "");
        String description = ParameterParserUtil.parseStringParam(rs.getString("srv.Description"), "");
        String type = ParameterParserUtil.parseStringParam(rs.getString("srv.Type"), "");
        String method = ParameterParserUtil.parseStringParam(rs.getString("srv.Method"), "");
        String application = ParameterParserUtil.parseStringParam(rs.getString("srv.Application"), "");
        String usrModif = rs.getString("srv.UsrModif");
        String usrCreated = rs.getString("srv.UsrCreated");
        Timestamp dateCreated = rs.getTimestamp("srv.DateCreated");
        Timestamp dateModif = rs.getTimestamp("srv.DateModif");
        String fileName = ParameterParserUtil.parseStringParam(rs.getString("srv.FileName"), "");
        String kafkaTopic = ParameterParserUtil.parseStringParam(rs.getString("srv.kafkaTopic"), "");
        String kafkaKey = ParameterParserUtil.parseStringParam(rs.getString("srv.kafkaKey"), "");
        String kafkaFilterPath = ParameterParserUtil.parseStringParam(rs.getString("srv.kafkaFilterPath"), "");
        String kafkaFilterValue = ParameterParserUtil.parseStringParam(rs.getString("srv.kafkaFilterValue"), "");
        String kafkaFilterHeaderPath = ParameterParserUtil.parseStringParam(rs.getString("srv.kafkaFilterHeaderPath"), "");
        String kafkaFilterHeaderValue = ParameterParserUtil.parseStringParam(rs.getString("srv.kafkaFilterHeaderValue"), "");
        boolean isFollowRedir = rs.getBoolean("srv.isFollowRedir");
        boolean isAvroEnable = rs.getBoolean("srv.isAvroEnable");
        String schemaRegistryURL = ParameterParserUtil.parseStringParam(rs.getString("srv.SchemaRegistryUrl"), "");
        boolean isAvroEnableKey = rs.getBoolean("srv.isAvroEnableKey");
        String avroSchemaKey = ParameterParserUtil.parseStringParam(rs.getString("srv.AvroSchemaKey"), "");
        boolean isAvroEnableValue = rs.getBoolean("srv.isAvroEnableValue");
        String avroSchemaValue = ParameterParserUtil.parseStringParam(rs.getString("srv.AvroSchemaValue"), "");
        String parentContentService = ParameterParserUtil.parseStringParam(rs.getString("srv.ParentContentService"), "");
        JSONObject simulationParameters = SqlUtil.getJSONObjectFromColumn(rs, "srv.SimulationParameters");
        String authType = ParameterParserUtil.parseStringParam(rs.getString("srv.AuthType"), "");
        String authUser = ParameterParserUtil.parseStringParam(rs.getString("srv.AuthUser"), "");
        String authPassword = ParameterParserUtil.parseStringParam(rs.getString("srv.AuthPassword"), "");
        String authAddTo = ParameterParserUtil.parseStringParam(rs.getString("srv.AuthAddTo"), "");

        AppService result = factoryAppService.create(service, type, method, application, collection, bodyType, serviceRequest, kafkaTopic, kafkaKey, kafkaFilterPath, kafkaFilterValue, kafkaFilterHeaderPath, kafkaFilterHeaderValue,
                description, servicePath, isFollowRedir, attachementURL, operation, isAvroEnable, schemaRegistryURL, isAvroEnableKey, avroSchemaKey, isAvroEnableValue, avroSchemaValue, parentContentService, usrCreated, dateCreated, usrModif, dateModif, fileName);
        result.setSimulationParameters(simulationParameters);
        result.setAuthType(authType);
        result.setAuthUser(authUser);
        result.setAuthPassword(authPassword);
        result.setAuthAddTo(authAddTo);
        return result;
    }

    private static void deleteFolder(File folder, boolean deleteit) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f, true);
                } else {
                    f.delete();
                }
            }
        }
        if (deleteit) {
            folder.delete();
        }
    }

}
