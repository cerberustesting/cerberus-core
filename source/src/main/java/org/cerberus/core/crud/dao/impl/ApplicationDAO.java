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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.IApplicationDAO;
import org.cerberus.core.crud.entity.Application;
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
import java.util.HashMap;
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
public class ApplicationDAO implements IApplicationDAO {

    private final DatabaseSpring databaseSpring;
    private static final Logger LOG = LogManager.getLogger(ApplicationDAO.class);
    private static final String OBJECT_NAME = "Application";
    private static final String SQL_DUPLICATED_CODE = "23000";
    private static final int MAX_ROW_SELECTED = 100000;

    @Override
    public AnswerItem<Application> readByKey(String application) {
        AnswerItem<Application> ans = new AnswerItem<>();
        Application result;
        final String query = "SELECT * FROM `application` app WHERE `application` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.application : {}", application);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            preStat.setString(1, application);
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
    public AnswerList<Application> readBySystemByCriteria(List<String> systems, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        AnswerList<Application> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<Application> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individualColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM application app ");
        searchSQL.append(" where 1=1 ");

        if (StringUtil.isNotEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (app.`application` like ?");
            searchSQL.append(" or app.`description` like ?");
            searchSQL.append(" or app.`sort` like ?");
            searchSQL.append(" or app.`type` like ?");
            searchSQL.append(" or app.`System` like ?");
            searchSQL.append(" or app.`Subsystem` like ?");
            searchSQL.append(" or app.`repoURL` like ?");
            searchSQL.append(" or app.`bugtrackerurl` like ?");
            searchSQL.append(" or app.`bugtrackernewurl` like ?");
            searchSQL.append(" or app.`deploytype` like ?");
            searchSQL.append(" or app.`mavengroupid` like ?)");
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

        if (CollectionUtils.isNotEmpty(systems)) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`System`", systems));
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
        LOG.debug("SQL.param.system : {}", systems);

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
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }

        response.setResultMessage(msg);
        response.setDataList(objectList);
        return response;
    }

    @Override
    public Integer getNbApplications(List<String> systems) {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        StringBuilder query = new StringBuilder();
        query.append("SELECT SQL_CALC_FOUND_ROWS count(*) FROM application app ");
        query.append(" where 1=1 ");

        if (CollectionUtils.isNotEmpty(systems)) {
            query.append(" and ");
            query.append(SqlUtil.generateInClause("`System`", systems));
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
    public AnswerItem<HashMap<String, HashMap<String, Integer>>> readTestCaseCountersBySystemByStatus(List<String> system) {
        AnswerItem<HashMap<String, HashMap<String, Integer>>> response = new AnswerItem<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        StringBuilder query = new StringBuilder()
                .append("SELECT a.`system` as SystemName, a.application as ApplicationName, inv.`value` as `Status`, count(inv.`value`) as CountStatus ")
                .append("FROM application a ")
                .append("JOIN invariant isys ON isys.value=a.`system` and isys.idname='SYSTEM' ")
                .append("inner join testcase tc on a.application = tc.application ")
                .append("inner join invariant inv on tc.`Status` = inv.`value` ")
                .append("where ")
                .append(SqlUtil.generateInClause("a.`system`", system))
                .append(" and inv.idname='TCSTATUS' and inv.gp1<>'N' ")
                .append("group by isys.sort , a.`system`, a.application, inv.`value` ")
                .append("order by isys.sort asc, a.`system`, a.application ");

        HashMap<String, HashMap<String, Integer>> result = new HashMap<>();

        LOG.debug("SQL : {}", query);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString())) {

            int i = 1;
            for (String string : system) {
                preStat.setString(i++, string);
            }

            try (ResultSet resultSet = preStat.executeQuery()) {
                //gets the data
                boolean hasData = false;
                while (resultSet.next()) {
                    hasData = true;
                    String appName = resultSet.getString("ApplicationName");
                    String tcStatus = resultSet.getString("Status");
                    int countStatus = resultSet.getInt("CountStatus");
                    HashMap<String, Integer> totalsMap = result.get(appName);
                    if (totalsMap == null) {
                        totalsMap = new HashMap<>();
                    }
                    totalsMap.put(tcStatus, countStatus);
                    result.put(appName, totalsMap);
                }

                if (hasData) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "APPLICATION").replace("%OPERATION%", "SELECT TOTAL"));
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                }
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }

        response.setResultMessage(msg);
        response.setItem(result);
        return response;
    }

    @Override
    public Answer create(Application object) {
        MessageEvent msg;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO application (`application`, `description`, `sort`, `type`, `system`, `SubSystem`, `repourl`, `poolSize`, `BugTrackerConnector`, `BugTrackerParam1`, `BugTrackerParam2`, `BugTrackerParam3`, `BugTrackerUrl`, `BugTrackerNewUrl`, `deploytype`");
        query.append(", `mavengroupid`, `usrcreated` ) ");
        if (StringUtil.isEmptyOrNull(object.getDeploytype())) {
            query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,null,?,?)");
        } else {
            query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        }

        LOG.debug("SQL : {}", query);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString())) {

            int i = 1;
            preStat.setString(i++, object.getApplication());
            preStat.setString(i++, object.getDescription());
            preStat.setInt(i++, object.getSort());
            preStat.setString(i++, object.getType());
            preStat.setString(i++, object.getSystem());
            preStat.setString(i++, object.getSubsystem());
            preStat.setString(i++, object.getRepoUrl());
            preStat.setInt(i++, object.getPoolSize());
            preStat.setString(i++, object.getBugTrackerConnector());
            preStat.setString(i++, object.getBugTrackerParam1());
            preStat.setString(i++, object.getBugTrackerParam2());
            preStat.setString(i++, object.getBugTrackerParam3());
            preStat.setString(i++, object.getBugTrackerUrl());
            preStat.setString(i++, object.getBugTrackerNewUrl());
            if (!StringUtil.isEmptyOrNull(object.getDeploytype())) {
                preStat.setString(i++, object.getDeploytype());
            }
            preStat.setString(i++, object.getMavengroupid());
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
    public Answer delete(Application object) {
        MessageEvent msg;
        final String query = "DELETE FROM application WHERE application = ? ";

        LOG.debug("SQL : {}", query);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {

            preStat.setString(1, object.getApplication());
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
    public Answer update(String application, Application object) {
        MessageEvent msg;
        if (StringUtil.isEmptyOrNull(object.getDeploytype())) {
            object.setDeploytype(null);
        }
        final String query = "UPDATE application SET Application = ?, description = ?, sort = ?, `type` = ?, `system` = ?, SubSystem = ?, repourl = ?, poolSize = ?, BugTrackerConnector = ?, BugTrackerParam1 = ?, BugTrackerParam2 = ?, BugTrackerParam3 = ?, BugTrackerUrl = ?, BugTrackerNewUrl = ?, "
                + "deploytype = ?, mavengroupid = ?, dateModif = NOW(), usrModif= ?  WHERE Application = ?";

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.application : {}", object.getApplication());

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {

            int i = 1;
            preStat.setString(i++, object.getApplication());
            preStat.setString(i++, object.getDescription());
            preStat.setInt(i++, object.getSort());
            preStat.setString(i++, object.getType());
            preStat.setString(i++, object.getSystem());
            preStat.setString(i++, object.getSubsystem());
            preStat.setString(i++, object.getRepoUrl());
            preStat.setInt(i++, object.getPoolSize());
            preStat.setString(i++, object.getBugTrackerConnector());
            preStat.setString(i++, object.getBugTrackerParam1());
            preStat.setString(i++, object.getBugTrackerParam2());
            preStat.setString(i++, object.getBugTrackerParam3());
            preStat.setString(i++, object.getBugTrackerUrl());
            preStat.setString(i++, object.getBugTrackerNewUrl());
            preStat.setString(i++, object.getDeploytype());
            preStat.setString(i++, object.getMavengroupid());
            preStat.setString(i++, object.getUsrModif());
            preStat.setString(i, application);

            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString(), exception);
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        return new Answer(msg);
    }

    @Override
    public AnswerList<String> readDistinctSystem() {
        MessageEvent msg;
        AnswerList<String> answer = new AnswerList<>();
        List<String> result = new ArrayList<>();
        final String query = "SELECT DISTINCT a.system FROM application a ORDER BY a.system";

        LOG.debug("SQL : {}", query);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query); ResultSet resultSet = preStat.executeQuery()) {

            while (resultSet.next()) {
                result.add(resultSet.getString("system"));
            }
            if (result.isEmpty()) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
            } else {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            result.clear();
        }

        answer.setTotalRows(result.size());
        answer.setDataList(result);
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(List<String> systems, String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        AnswerList<String> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individualColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder()
                .append("SELECT distinct `")
                .append(columnName)
                .append("` as distinctValues FROM application ");

        searchSQL.append("WHERE 1=1");
        if (CollectionUtils.isNotEmpty(systems)) {
            searchSQL.append(" AND ");
            searchSQL.append(SqlUtil.generateInClause("`System`", systems));
        }

        if (StringUtil.isNotEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (`application` like ?");
            searchSQL.append(" or `description` like ?");
            searchSQL.append(" or `sort` like ?");
            searchSQL.append(" or `type` like ?");
            searchSQL.append(" or `System` like ?");
            searchSQL.append(" or `Subsystem` like ?");
            searchSQL.append(" or `repoURL` like ?");
            searchSQL.append(" or `bugtrackerurl` like ?");
            searchSQL.append(" or `bugtrackernewurl` like ?");
            searchSQL.append(" or `deploytype` like ?");
            searchSQL.append(" or `mavengroupid` like ?)");
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
        query.append(" order by `").append(columnName).append("` asc");

        LOG.debug("SQL : {}", query);

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); Statement stm = connection.createStatement()) {

            int i = 1;
            if (CollectionUtils.isNotEmpty(systems)) {
                for (String system : systems) {
                    preStat.setString(i++, system);
                }
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
            LOG.error("Unable to execute query : {}", e.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        }

        answer.setResultMessage(msg);
        answer.setDataList(distinctValues);
        return answer;
    }

    @Override
    public Application loadFromResultSet(ResultSet rs) throws SQLException {
        String app = ParameterParserUtil.parseStringParam(rs.getString("app.application"), "");
        String description = ParameterParserUtil.parseStringParam(rs.getString("app.description"), "");
        int sort = ParameterParserUtil.parseIntegerParam(rs.getString("app.sort"), 0);
        String type = ParameterParserUtil.parseStringParam(rs.getString("app.type"), "");
        String system = ParameterParserUtil.parseStringParam(rs.getString("app.system"), "");
        String subsystem = ParameterParserUtil.parseStringParam(rs.getString("app.subsystem"), "");
        String repoUrl = ParameterParserUtil.parseStringParam(rs.getString("app.repourl"), "");
        int poolSize = ParameterParserUtil.parseIntegerParam(rs.getString("app.poolSize"), 0);
        String deployType = ParameterParserUtil.parseStringParam(rs.getString("app.deploytype"), "");
        String mavenGroupId = ParameterParserUtil.parseStringParam(rs.getString("app.mavengroupid"), "");
        String bugTrackerConnector = ParameterParserUtil.parseStringParam(rs.getString("app.bugtrackerconnector"), "");
        String bugTrackerParam1 = ParameterParserUtil.parseStringParam(rs.getString("app.bugtrackerparam1"), "");
        String bugTrackerParam2 = ParameterParserUtil.parseStringParam(rs.getString("app.bugtrackerparam2"), "");
        String bugTrackerParam3 = ParameterParserUtil.parseStringParam(rs.getString("app.bugtrackerparam3"), "");
        String bugTrackerUrl = ParameterParserUtil.parseStringParam(rs.getString("app.bugtrackerurl"), "");
        String bugTrackerNewUrl = ParameterParserUtil.parseStringParam(rs.getString("app.bugtrackernewurl"), "");
        String usrModif = ParameterParserUtil.parseStringParam(rs.getString("app.UsrModif"), "");
        String usrCreated = ParameterParserUtil.parseStringParam(rs.getString("app.UsrCreated"), "");
        Timestamp dateModif = rs.getTimestamp("app.DateModif");
        Timestamp dateCreated = rs.getTimestamp("app.DateCreated");

        return Application.builder()
                .application(app)
                .sort(sort)
                .type(type)
                .system(system)
                .subsystem(subsystem)
                .repoUrl(repoUrl)
                .bugTrackerConnector(bugTrackerConnector)
                .bugTrackerParam1(bugTrackerParam1)
                .bugTrackerParam2(bugTrackerParam2)
                .bugTrackerParam3(bugTrackerParam3)
                .bugTrackerUrl(bugTrackerUrl)
                .bugTrackerNewUrl(bugTrackerNewUrl)
                .poolSize(poolSize)
                .deploytype(deployType)
                .mavengroupid(mavenGroupId)
                .description(description)
                .usrCreated(usrCreated)
                .dateCreated(dateCreated)
                .usrModif(usrModif)
                .dateModif(dateModif)
                .build();
    }
}
