/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.crud.dao.impl;

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
import org.apache.logging.log4j.Logger;
import org.apache.commons.fileupload.FileItem;
import org.apache.logging.log4j.LogManager;
import org.cerberus.crud.dao.IAppServiceDAO;
import org.cerberus.crud.entity.AppService;
import org.cerberus.crud.entity.Parameter;
import org.cerberus.crud.factory.IFactoryAppService;
import org.cerberus.crud.factory.impl.FactoryAppService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.security.UserSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author cte
 */
@Repository
public class AppServiceDAO implements IAppServiceDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryAppService factoryAppService;
    @Autowired
    private IParameterService parameterService;

    private static final Logger LOG = LogManager.getLogger(AppServiceDAO.class);

    private final String OBJECT_NAME = "AppService";
    private final int MAX_ROW_SELECTED = 100000;
    private final String SQL_DUPLICATED_CODE = "23000";

    @Override
    public AppService findAppServiceByKey(String service) throws CerberusException {
        boolean throwEx = false;
        AppService result = null;
        final String query = "SELECT * FROM appservice srv WHERE `service` = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, service);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        String group = resultSet.getString("Group");
                        String serviceRequest = resultSet.getString("ServiceRequest");
                        String description = resultSet.getString("Description");
                        String servicePath = resultSet.getString("servicePath");
                        String attachementURL = resultSet.getString("AttachementURL");
                        String operation = resultSet.getString("Operation");
                        String application = resultSet.getString("Application");
                        String type = resultSet.getString("Type");
                        String method = resultSet.getString("Method");
                        String usrModif = resultSet.getString("UsrModif");
                        String usrCreated = resultSet.getString("UsrCreated");
                        Timestamp dateCreated = resultSet.getTimestamp("DateCreated");
                        Timestamp dateModif = resultSet.getTimestamp("DateModif");
                        String fileName = resultSet.getString("FileName");
                        String kafkaTopic = resultSet.getString("KafkaTopic");
                        String kafkaKey = resultSet.getString("KafkaKey");
                        String kafkaFilterPath = resultSet.getString("KafkaFilterPath");
                        String kafkaFilterValue = resultSet.getString("KafkaFilterValue");

                        result = this.factoryAppService.create(service, type, method, application, group, serviceRequest, kafkaTopic, kafkaKey, kafkaFilterPath, kafkaFilterValue, description, servicePath, attachementURL, operation, usrCreated, dateCreated, usrModif, dateModif, fileName);
                    } else {
                        throwEx = true;
                    }
                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception closing connection : " + e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    @Override
    public AnswerList<AppService> findAppServiceByLikeName(String service, int limit) {
        AnswerList<AppService> response = new AnswerList<>();
        boolean throwEx = false;
        AppService result = null;
        final String query = "SELECT * FROM appservice srv WHERE `service` LIKE ? limit ?";
        List<AppService> objectList = new ArrayList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);

            try {
                preStat.setString(1, "%" + service + "%");
                preStat.setInt(2, limit);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        objectList.add(this.loadFromResultSet(resultSet));
                    }

                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));

                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }
                    response = new AnswerList<>(objectList, nrTotalRows);
                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception closing connection : " + e.toString());
            }
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
        List<AppService> objectList = new ArrayList<AppService>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<String>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM appservice srv ");
        query.append("left outer JOIN application app ON srv.application = app.application");

        query.append(" WHERE 1=1");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
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
            searchSQL.append(" or srv.AttachementURL like ?");
            searchSQL.append(" or srv.Group like ?");
            searchSQL.append(" or srv.Description like ?");
            searchSQL.append(" or srv.UsrCreated like ?");
            searchSQL.append(" or srv.DateCreated like ?");
            searchSQL.append(" or srv.UsrModif like ?");
            searchSQL.append(" or srv.DateModif like ?)");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and ");
                String q = SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue());
                if (q == null || q == "") {
                    q = "(" + entry.getKey() + " IS NULL OR " + entry.getKey() + " = '')";
                }
                searchSQL.append(q);
                individalColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }

        query.append(searchSQL);

        if (systems != null && !systems.isEmpty()) {
            systems.add(""); // authorize tranversal object
            query.append(" and ( app.Application is null or ");
            query.append(SqlUtil.generateInClause("app.system", systems));
            query.append(" ) ");
        }

        query.append(" AND ( app.Application is null or ");
        query.append(UserSecurity.getSystemAllowForSQL("app.system"));
        query.append(" ) ");

        if (!StringUtil.isNullOrEmpty(column)) {
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
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;

                if (!StringUtil.isNullOrEmpty(searchTerm)) {
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
                for (String individualColumnSearchValue : individalColumnSearchValues) {
                    preStat.setString(i++, individualColumnSearchValue);
                }

                if (systems != null && !systems.isEmpty()) {
                    for (String sys : systems) {
                        preStat.setString(i++, sys);
                    }
                }

                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        objectList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
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

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        response.setResultMessage(msg);
        response.setDataList(objectList);
        return response;
    }

    @Override
    public AnswerItem<AppService> readByKey(String key) {
        AnswerItem<AppService> ans = new AnswerItem<>();
        AppService result = null;
        final String query = "SELECT * FROM `appservice` srv WHERE `service` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.service : " + key);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, key);
                try (ResultSet resultSet = preStat.executeQuery();) {
                    if (resultSet.first()) {
                        result = loadFromResultSet(resultSet);
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        ans.setItem(result);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    }
                } catch (SQLException exception) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                } finally {
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
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        //sets the message
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
        List<String> individalColumnSearchValues = new ArrayList<>();
        StringBuilder query = new StringBuilder();

        query.append("SELECT distinct ");
        query.append(columnName);
        query.append(" as distinctValues FROM appservice srv");
        query.append(" where 1=1");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (srv.Service like ?");
            searchSQL.append(" or srv.Group like ?");
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
        query.append(" group by ifnull(").append(columnName).append(",'')");
        query.append(" order by ").append(columnName).append(" asc");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());
                Statement stm = connection.createStatement();) {

            int i = 1;
            if (!StringUtil.isNullOrEmpty(searchTerm)) {
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

                //get the total number of rows
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

            } catch (SQLException e) {
                LOG.warn(e.toString());
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

    @Override
    public Answer create(AppService object) {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO appservice (`Service`, `Group`, `Application`, `Type`, `Method`, `ServicePath`, `Operation`, `ServiceRequest`, `KafkaTopic`, `KafkaKey`, `KafkaFilterPath`, `KafkaFilterValue`, `AttachementURL`, `Description`, `FileName`) ");
        if ((object.getApplication() != null) && (!object.getApplication().equals(""))) {
            query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        } else {
            query.append("VALUES (?,?,null,?,?,?,?,?,?,?,?,?,?,?,?)");
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                preStat.setString(i++, object.getService());
                preStat.setString(i++, object.getGroup());
                if ((object.getApplication() != null) && (!object.getApplication().equals(""))) {
                    preStat.setString(i++, object.getApplication());
                }
                preStat.setString(i++, object.getType());
                preStat.setString(i++, object.getMethod());
                preStat.setString(i++, object.getServicePath());
                preStat.setString(i++, object.getOperation());
                preStat.setString(i++, object.getServiceRequest());
                preStat.setString(i++, object.getKafkaTopic());
                preStat.setString(i++, object.getKafkaKey());
                preStat.setString(i++, object.getKafkaFilterPath());
                preStat.setString(i++, object.getKafkaFilterValue());
                preStat.setString(i++, object.getAttachementURL());
                preStat.setString(i++, object.getDescription());
                preStat.setString(i++, object.getFileName());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));

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
        return new Answer(msg);
    }

    @Override
    public Answer update(String service, AppService object) {
        MessageEvent msg = null;
        String query = "UPDATE appservice srv SET `Service` = ?, `Group` = ?, `ServicePath` = ?, `Operation` = ?, ServiceRequest = ?, KafkaTopic = ?, KafkaKey = ?, KafkaFilterPath = ?, KafkaFilterValue = ?, AttachementURL = ?, "
                + "Description = ?, `Type` = ?, Method = ?, `UsrModif`= ?, `DateModif` = NOW(), `FileName` = ?";
        if ((object.getApplication() != null) && (!object.getApplication().equals(""))) {
            query += " ,Application = ?";
        } else {
            query += " ,Application = null";
        }
        query += " WHERE `Service` = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.application : " + object.getApplication());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setString(i++, object.getService());
                preStat.setString(i++, object.getGroup());
                preStat.setString(i++, object.getServicePath());
                preStat.setString(i++, object.getOperation());
                preStat.setString(i++, object.getServiceRequest());
                preStat.setString(i++, object.getKafkaTopic());
                preStat.setString(i++, object.getKafkaKey());
                preStat.setString(i++, object.getKafkaFilterPath());
                preStat.setString(i++, object.getKafkaFilterValue());
                preStat.setString(i++, object.getAttachementURL());
                preStat.setString(i++, object.getDescription());
                preStat.setString(i++, object.getType());
                preStat.setString(i++, object.getMethod());
                preStat.setString(i++, object.getUsrModif());
                preStat.setString(i++, object.getFileName());
                if ((object.getApplication() != null) && (!object.getApplication().equals(""))) {
                    preStat.setString(i++, object.getApplication());
                }
                preStat.setString(i++, service);

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

    @Override
    public Answer delete(AppService object) {
        MessageEvent msg = null;
        final String query = "DELETE FROM appservice WHERE `Service` = ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, object.getService());

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

    @Override
    public Answer uploadFile(String service, FileItem file) {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                "cerberus_ftpfile_path Parameter not found");
        AnswerItem a = parameterService.readByKey("", "cerberus_ftpfile_path");
        if (a.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            Parameter p = (Parameter) a.getItem();
            String uploadPath = p.getValue();
            File appDir = new File(uploadPath + File.separator + service);
            if (!appDir.exists()) {
                try {
                    appDir.mkdirs();
                } catch (SecurityException se) {
                    LOG.warn("Unable to create ftp local file dir: " + se.getMessage());
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
                    LOG.warn("Unable to upload ftp local file: " + e.getMessage());
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
        String group = ParameterParserUtil.parseStringParam(rs.getString("srv.Group"), "");
        String servicePath = ParameterParserUtil.parseStringParam(rs.getString("srv.ServicePath"), "");
        String operation = ParameterParserUtil.parseStringParam(rs.getString("srv.Operation"), "");
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
        factoryAppService = new FactoryAppService();
        return factoryAppService.create(service, type, method, application, group, serviceRequest, kafkaTopic, kafkaKey, kafkaFilterPath, kafkaFilterValue, description, servicePath, attachementURL, operation, usrCreated, dateCreated, usrModif, dateModif, fileName);
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
