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
import org.cerberus.core.crud.dao.ITagDAO;
import org.cerberus.core.crud.entity.Tag;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;

/**
 * Implements methods defined on IApplicationDAO
 *
 * @author tbernardes
 * @version 1.0, 15/10/13
 * @since 0.9.0
 */
@AllArgsConstructor
@Repository
public class TagDAO implements ITagDAO {

    private final DatabaseSpring databaseSpring;
    private static final Logger LOG = LogManager.getLogger(TagDAO.class);
    private static final String OBJECT_NAME = "Tag";
    private static final String SQL_DUPLICATED_CODE = "23000";
    private static final int MAX_ROW_SELECTED = 100000;

    @Override
    public AnswerItem<Tag> readByKey(String tag) {
        AnswerItem<Tag> ans = new AnswerItem<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        final String query = "SELECT * FROM `tag` WHERE `tag` = ?";

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.tag : {}", tag);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            preStat.setString(1, tag);
            try (ResultSet resultSet = preStat.executeQuery()) {
                if (resultSet.first()) {
                    Tag result = loadFromResultSet(resultSet);
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
    public AnswerItem<Tag> readByKeyTech(long id) {
        AnswerItem<Tag> ans = new AnswerItem<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        final String query = "SELECT * FROM `tag` WHERE `id` = ?";

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.id : {}", id);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

            preStat.setLong(1, id);
            try (ResultSet resultSet = preStat.executeQuery()) {
                if (resultSet.first()) {
                    Tag result = loadFromResultSet(resultSet);
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
    public AnswerList<Tag> readByVariousByCriteria(String campaign, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch, List<String> systems) {
        AnswerList<Tag> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<Tag> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individualColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();

        if (CollectionUtils.isNotEmpty(systems)) {
            query.append("SELECT SQL_CALC_FOUND_ROWS distinct tag.* FROM tag tag JOIN tagsystem tas ON tas.tag=tag.tag WHERE ");
            searchSQL.append(SqlUtil.generateInClause("tas.system", systems));
        } else {
            query.append("SELECT SQL_CALC_FOUND_ROWS * FROM tag tag ");
            searchSQL.append(" where 1=1 ");
        }

        if (StringUtil.isNotEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (tag.`id` like ?");
            searchSQL.append(" or tag.`tag` like ?");
            searchSQL.append(" or tag.`description` like ?");
            searchSQL.append(" or tag.`campaign` like ?)");
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

        if (StringUtil.isNotEmptyOrNull(campaign)) {
            searchSQL.append(" and (`campaign` = ? )");
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

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); Statement stm = connection.createStatement()) {

            int i = 1;
            if (systems != null && !systems.isEmpty()) {
                for (String system : systems) {
                    preStat.setString(i++, system);
                }
            }

            if (!StringUtil.isEmptyOrNull(searchTerm)) {
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
            }
            for (String individualColumnSearchValue : individualColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }
            if (!StringUtil.isEmptyOrNull(campaign)) {
                preStat.setString(i, campaign);
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
    public AnswerList<Tag> readByVarious(List<String> campaigns, List<String> systems, Date from, Date to) {
        AnswerList<Tag> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<Tag> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        Timestamp t1;

        StringBuilder query = new StringBuilder();
        if (CollectionUtils.isNotEmpty(systems)) {
            query.append("SELECT SQL_CALC_FOUND_ROWS tag.* FROM tag tag JOIN tagsystem tas ON tas.tag=tag.tag WHERE ");
            searchSQL.append(SqlUtil.generateInClause("tas.system", systems));
        } else {
            query.append("SELECT SQL_CALC_FOUND_ROWS * FROM tag tag ");
            searchSQL.append(" where 1=1 ");
        }

        if (CollectionUtils.isNotEmpty(campaigns)) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("tag.campaign", campaigns));
        }

        searchSQL.append(" and tag.`DateCreated` > ? and tag.`DateCreated` < ? ");
        query.append(searchSQL);

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.from : {}", from);
        LOG.debug("SQL.to : {}", to);
        LOG.debug("SQL.system {}: ", systems);
        LOG.debug("SQL.campaign {}: ", campaigns);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); Statement stm = connection.createStatement()) {

            int i = 1;
            if (CollectionUtils.isNotEmpty(systems)) {
                for (String system : systems) {
                    preStat.setString(i++, system);
                }
            }
            if (CollectionUtils.isNotEmpty(campaigns)) {
                for (String campaign : campaigns) {
                    preStat.setString(i++, campaign);
                }
            }
            t1 = new Timestamp(from.getTime());
            preStat.setTimestamp(i++, t1);
            t1 = new Timestamp(to.getTime());
            preStat.setTimestamp(i++, t1);

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
    public AnswerList<Tag> readByVarious(List<String> campaigns, List<String> group1s, List<String> group2s, List<String> group3s, List<String> environments, List<String> countries, List<String> robotDeclis, Date from, Date to) {
        AnswerList<Tag> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<Tag> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        Timestamp t1;

        StringBuilder query = new StringBuilder();
        query.append("SELECT SQL_CALC_FOUND_ROWS tag.* FROM tag tag JOIN campaign cap ON cap.campaign=tag.campaign ");
        searchSQL.append(" where nbExe>0 ");
        if (CollectionUtils.isNotEmpty(campaigns)) {
            searchSQL.append(SqlUtil.generateInClause("and tag.campaign", campaigns));
        }
        if (CollectionUtils.isNotEmpty(group1s)) {
            searchSQL.append(SqlUtil.generateInClause("and cap.group1", group1s));
        }
        if (CollectionUtils.isNotEmpty(group2s)) {
            searchSQL.append(SqlUtil.generateInClause("and cap.group2", group2s));
        }
        if (CollectionUtils.isNotEmpty(group3s)) {
            searchSQL.append(SqlUtil.generateInClause("and cap.group3", group3s));
        }
        if (CollectionUtils.isNotEmpty(environments)) {
            searchSQL.append(SqlUtil.generateInClause("and cap.environmentlist", environments));
        }
        if (CollectionUtils.isNotEmpty(countries)) {
            searchSQL.append(SqlUtil.generateInClause("and cap.countrylist", countries));
        }
        if (CollectionUtils.isNotEmpty(robotDeclis)) {
            searchSQL.append(SqlUtil.generateInClause("and cap.robotdeclilist", robotDeclis));
        }

        searchSQL.append(" and tag.`DateCreated` > ? and tag.`DateCreated` < ? ");
        searchSQL.append(" order by tag.id asc ");

        query.append(searchSQL);

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.from : {}", from);
        LOG.debug("SQL.to : {}", to);
        LOG.debug("SQL.campaigns : {}", campaigns);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); Statement stm = connection.createStatement()) {

            int i = 1;
            if (CollectionUtils.isNotEmpty(campaigns)) {
                for (String campaign : campaigns) {
                    preStat.setString(i++, campaign);
                }
            }
            if (CollectionUtils.isNotEmpty(group1s)) {
                for (String group : group1s) {
                    preStat.setString(i++, group);
                }
            }
            if (CollectionUtils.isNotEmpty(group2s)) {
                for (String group : group2s) {
                    preStat.setString(i++, group);
                }
            }
            if (CollectionUtils.isNotEmpty(group3s)) {
                for (String group : group3s) {
                    preStat.setString(i++, group);
                }
            }
            if (CollectionUtils.isNotEmpty(environments)) {
                for (String environment : environments) {
                    preStat.setString(i++, environment);
                }
            }
            if (CollectionUtils.isNotEmpty(countries)) {
                for (String country : countries) {
                    preStat.setString(i++, country);
                }
            }
            if (CollectionUtils.isNotEmpty(robotDeclis)) {
                for (String robotDecli : robotDeclis) {
                    preStat.setString(i++, robotDecli);
                }
            }
            t1 = new Timestamp(from.getTime());
            preStat.setTimestamp(i++, t1);
            t1 = new Timestamp(to.getTime());
            preStat.setTimestamp(i, t1);

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
    public Answer create(Tag object) {
        MessageEvent msg;
        StringBuilder query = new StringBuilder();
        StringBuilder valuesQuery = new StringBuilder();
        query.append("INSERT INTO tag (`tag`, `description`");
        valuesQuery.append("VALUES (?,?");
        if (StringUtil.isNotEmptyOrNull(object.getCampaign())) {
            query.append(", `campaign`");
            valuesQuery.append(",?");
        }
        if (StringUtil.isNotEmptyOrNull(object.getUsrCreated())) {
            query.append(", `usrcreated`");
            valuesQuery.append(",?");
        }
        if (StringUtil.isNotEmptyOrNull(object.getReqCountryList())) {
            query.append(", `ReqCountryList`");
            valuesQuery.append(",?");
        }
        if (StringUtil.isNotEmptyOrNull(object.getReqEnvironmentList())) {
            query.append(", `ReqEnvironmentList`");
            valuesQuery.append(",?");
        }
        query.append(") ");
        valuesQuery.append(");");
        query.append(valuesQuery);

        LOG.debug("SQL : {}", query);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString())) {

            int i = 1;
            preStat.setString(i++, object.getTag());
            preStat.setString(i++, object.getDescription());
            if (StringUtil.isNotEmptyOrNull(object.getCampaign())) {
                preStat.setString(i++, object.getCampaign());
            }
            if (StringUtil.isNotEmptyOrNull(object.getUsrCreated())) {
                preStat.setString(i++, object.getUsrCreated());
            }
            if (StringUtil.isNotEmptyOrNull(object.getReqCountryList())) {
                preStat.setString(i++, object.getReqCountryList());
            }
            if (StringUtil.isNotEmptyOrNull(object.getReqEnvironmentList())) {
                preStat.setString(i, object.getReqEnvironmentList());
            }

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
    public Answer delete(Tag object) {
        MessageEvent msg;
        final String query = "DELETE FROM tag WHERE tag = ? ";

        LOG.debug("SQL : {}", query);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {
            preStat.setString(1, object.getTag());
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
    public Answer update(String tag, Tag object) {
        MessageEvent msg;
        StringBuilder query = new StringBuilder("UPDATE tag SET tag = ?, description = ?, dateModif = NOW(), usrModif= ?");
        if (StringUtil.isNotEmptyOrNull(object.getCampaign())) {
            query.append(", campaign = ?");
        }
        query.append("  WHERE Tag = ?");

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.tag : {}", object.getTag());

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString())) {

            int i = 1;
            preStat.setString(i++, object.getTag());
            preStat.setString(i++, object.getDescription());
            if (StringUtil.isNotEmptyOrNull(object.getCampaign())) {
                preStat.setString(i++, object.getCampaign());
            }
            preStat.setString(i++, object.getUsrModif());
            preStat.setString(i, tag);
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
    public Answer updateBrowserStackBuild(String tag, Tag object) {
        MessageEvent msg;
        StringBuilder query = new StringBuilder("UPDATE tag SET browserstackBuildHash = ?, browserstackAppBuildHash = ?, dateModif = NOW(), usrModif= ?");
        query.append("  WHERE Tag = ?");

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.tag : {}", object.getTag());

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString())) {
            int i = 1;
            preStat.setString(i++, object.getBrowserstackBuildHash());
            preStat.setString(i++, object.getBrowserstackAppBuildHash());
            preStat.setString(i++, object.getUsrModif());
            preStat.setString(i, tag);
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
    public Answer updateLambdatestBuild(String tag, Tag object) {
        MessageEvent msg;
        StringBuilder query = new StringBuilder("UPDATE tag SET LambdaTestBuild = ?, dateModif = NOW(), usrModif= ?");
        query.append("  WHERE Tag = ?");

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.tag : {}", object.getTag());

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString())) {

            int i = 1;
            preStat.setString(i++, object.getLambdaTestBuild());
            preStat.setString(i++, object.getUsrModif());
            preStat.setString(i, tag);
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
    public Answer updateDescription(String tag, Tag object) {
        MessageEvent msg;
        StringBuilder query = new StringBuilder("UPDATE tag SET description = ?, dateModif = NOW(), usrModif= ?");
        query.append("  WHERE Tag = ?");

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.tag : {}", object.getTag());

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString())) {
            int i = 1;
            preStat.setString(i++, object.getDescription());
            preStat.setString(i++, object.getUsrModif());
            preStat.setString(i, tag);
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
    public Answer updateComment(String tag, Tag object) {
        MessageEvent msg;
        StringBuilder query = new StringBuilder("UPDATE tag SET comment = ?, dateModif = NOW(), usrModif= ?");
        query.append("  WHERE Tag = ?");

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.tag : {}", tag);
        LOG.debug("SQL.param.comment : {}", object.getComment());

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString())) {

            int i = 1;
            preStat.setString(i++, object.getComment());
            preStat.setString(i++, object.getUsrModif());
            preStat.setString(i, tag);
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
    public Answer appendComment(String tag, Tag object) {
        MessageEvent msg;
        StringBuilder query = new StringBuilder("UPDATE tag SET comment = trim(concat (comment, ?)), dateModif = NOW(), usrModif= ?");
        query.append("  WHERE Tag = ?");

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.tag : {}", tag);
        LOG.debug("SQL.param.comment : {}", object.getComment());

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString())) {

            int i = 1;
            preStat.setString(i++, object.getComment());
            preStat.setString(i++, object.getUsrModif());
            preStat.setString(i, tag);
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
    public Answer updateXRayTestExecution(String tag, Tag object) {
        MessageEvent msg;
        StringBuilder query = new StringBuilder("UPDATE tag SET XRayTestExecution = ?, XRayURL = ?, XRayMEssage = ?, dateModif = NOW(), usrModif= ?");
        query.append("  WHERE Tag = ?");

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.tag : {}", object.getTag());

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString())) {

            int i = 1;
            preStat.setString(i++, object.getXRayTestExecution());
            preStat.setString(i++, object.getXRayURL());
            preStat.setString(i++, object.getXRayMessage());
            preStat.setString(i++, "updateXRayTestExecution");
            preStat.setString(i, tag);
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
    public void updateFalseNegative(String tag, boolean falseNegative, String usrModif) throws CerberusException {
        final String query = "UPDATE tag SET FalseNegative = ?, dateModif = NOW(), usrModif= ? WHERE tag = ?";
        LOG.debug("SQL : {}", query);
        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {
            preStat.setBoolean(1, falseNegative);
            preStat.setString(2, usrModif);
            preStat.setString(3, tag);
            preStat.executeUpdate();

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public int lockXRayTestExecution(String tag, Tag object) {
        MessageEvent msg;
        StringBuilder query = new StringBuilder("UPDATE tag SET XRayTestExecution = 'PENDING', dateModif = NOW(), usrModif= ?");
        query.append(" WHERE Tag = ? and XRayTestExecution != 'PENDING'");

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.tag : {}", object.getTag());

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString())) {
            int i = 1;
            preStat.setString(i++, "lockXRayTestExecution");
            preStat.setString(i, tag);
            return preStat.executeUpdate();
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        return 0;
    }

    @Override
    public Answer updateDateEndQueue(Tag tag) {
        MessageEvent msg;
        StringBuilder query = new StringBuilder();
        query.append("UPDATE tag SET DateEndQueue = ?, DurationMs = ?, nbExe = ?, nbExeUsefull = ?, nbOK = ?, nbKO = ?, ");
        query.append("  nbFA = ?, nbNA = ?, nbNE = ?, nbWE = ?, nbPE = ?, nbQU = ?, nbQE = ?, nbCA = ?, nbFlaky = ?, nbMuted = ?, ");
        query.append("  CIScore = ?, CIScoreThreshold = ?, CIScoreMax = ?, CIResult = ?, EnvironmentList = ?, CountryList = ?, ");
        query.append("  RobotDecliList = ?, SystemList = ?, ApplicationList = ?  ");
        query.append("WHERE Tag = ?");

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.tag : {}", tag.getTag());

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString())) {

            int i = 1;
            preStat.setTimestamp(i++, tag.getDateEndQueue());
            preStat.setLong(i++, tag.getDurationMs());
            preStat.setInt(i++, tag.getNbExe());
            preStat.setInt(i++, tag.getNbExeUsefull());
            preStat.setInt(i++, tag.getNbOK());
            preStat.setInt(i++, tag.getNbKO());
            preStat.setInt(i++, tag.getNbFA());
            preStat.setInt(i++, tag.getNbNA());
            preStat.setInt(i++, tag.getNbNE());
            preStat.setInt(i++, tag.getNbWE());
            preStat.setInt(i++, tag.getNbPE());
            preStat.setInt(i++, tag.getNbQU());
            preStat.setInt(i++, tag.getNbQE());
            preStat.setInt(i++, tag.getNbCA());
            preStat.setInt(i++, tag.getNbFlaky());
            preStat.setInt(i++, tag.getNbMuted());
            preStat.setInt(i++, tag.getCiScore());
            preStat.setInt(i++, tag.getCiScoreThreshold());
            preStat.setInt(i++, tag.getCiScoreMax());
            preStat.setString(i++, tag.getCiResult());
            preStat.setString(i++, tag.getEnvironmentList());
            preStat.setString(i++, tag.getCountryList());
            preStat.setString(i++, tag.getRobotDecliList());
            preStat.setString(i++, tag.getSystemList());
            preStat.setString(i++, tag.getApplicationList());
            preStat.setString(i, tag.getTag());

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
    public Answer updateDateStartExe(Tag tag) {
        MessageEvent msg;
        StringBuilder query = new StringBuilder();
        query.append("UPDATE tag SET DateStartExe = ? ");
        query.append("WHERE Tag = ?");

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.tag : {}", tag.getTag());

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString())) {

            int i = 1;
            preStat.setTimestamp(i++, tag.getDateStartExe());
            preStat.setString(i, tag.getTag());

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
    public AnswerList<String> readDistinctValuesByCriteria(String campaign, String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        AnswerList<String> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individualColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append("SELECT distinct ");
        query.append(columnName);
        query.append(" as distinctValues FROM tag ");
        searchSQL.append("WHERE 1=1");
        if (StringUtil.isNotEmptyOrNull(campaign)) {
            searchSQL.append(" and (`campaign` = ? )");
        }
        if (StringUtil.isNotEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (`tag` like ?");
            searchSQL.append(" or `id` like ?");
            searchSQL.append(" or `description` like ?");
            searchSQL.append(" or `campaign` like ?)");
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

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); Statement stm = connection.createStatement()) {

            int i = 1;
            if (StringUtil.isNotEmptyOrNull(campaign)) {
                preStat.setString(i++, campaign);
            }
            if (StringUtil.isNotEmptyOrNull(searchTerm)) {
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
                if (distinctValues.size() >= MAX_ROW_SELECTED) {
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
        } catch (Exception exception) {
            LOG.warn("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    exception.toString());
        }
        answer.setResultMessage(msg);
        answer.setDataList(distinctValues);
        return answer;
    }

    @Override
    public Tag loadFromResultSet(ResultSet rs) throws SQLException {
        long id = ParameterParserUtil.parseLongParam(rs.getString("tag.id"), 0);
        String tag = ParameterParserUtil.parseStringParam(rs.getString("tag.tag"), "");
        String description = ParameterParserUtil.parseStringParam(rs.getString("tag.description"), "");
        String comment = ParameterParserUtil.parseStringParam(rs.getString("tag.comment"), "");
        String campaign = ParameterParserUtil.parseStringParam(rs.getString("tag.campaign"), "");
        Timestamp dateEndQueue = rs.getTimestamp("tag.DateEndQueue");
        Timestamp dateStartExe = rs.getTimestamp("tag.DateStartExe");
        String usrModif = ParameterParserUtil.parseStringParam(rs.getString("tag.UsrModif"), "");
        String usrCreated = ParameterParserUtil.parseStringParam(rs.getString("tag.UsrCreated"), "");
        Timestamp dateModif = rs.getTimestamp("tag.DateModif");
        Timestamp dateCreated = rs.getTimestamp("tag.DateCreated");

        int nbExe = rs.getInt("tag.nbExe");
        int nbExeUsefull = rs.getInt("tag.nbExeUsefull");
        int nbOK = rs.getInt("tag.nbOK");
        int nbKO = rs.getInt("tag.nbKO");
        int nbFA = rs.getInt("tag.nbFA");
        int nbNA = rs.getInt("tag.nbNA");
        int nbNE = rs.getInt("tag.nbNE");
        int nbWE = rs.getInt("tag.nbWE");
        int nbPE = rs.getInt("tag.nbPE");
        int nbQU = rs.getInt("tag.nbQU");
        int nbQE = rs.getInt("tag.nbQE");
        int nbCA = rs.getInt("tag.nbCA");
        int ciScore = rs.getInt("tag.ciScore");
        int ciScoreThreshold = rs.getInt("tag.ciScoreThreshold");
        int ciScoreMax = rs.getInt("tag.ciScoreMax");
        int nbFlaky = rs.getInt("tag.nbFlaky");
        int nbMuted = rs.getInt("tag.nbMuted");
        int duration = rs.getInt("tag.DurationMs");
        String ciResult = rs.getString("tag.ciResult");
        boolean falseNegative = rs.getBoolean("tag.FalseNegative");
        String environmentList = rs.getString("tag.EnvironmentList");
        String countryList = rs.getString("tag.CountryList");
        String robotDecliList = rs.getString("tag.RobotDecliList");
        String systemList = rs.getString("tag.SystemList");
        String applicationList = rs.getString("tag.ApplicationList");
        String reqEnvironmentList = rs.getString("tag.ReqEnvironmentList");
        String reqCountryList = rs.getString("tag.ReqCountryList");
        String browserstackBuildHash = rs.getString("tag.BrowserstackBuildHash");
        String browserstackAppBuildHash = rs.getString("tag.BrowserstackAppBuildHash");
        String lambdaTestBuild = rs.getString("tag.LambdaTestBuild");
        String xRayTestExecution = rs.getString("tag.XRayTestExecution");
        String xRayURL = rs.getString("tag.XRayURL");
        String xRayMEssage = rs.getString("tag.XRayMessage");

        return Tag.builder()
                .id(id).tag(tag).description(description).comment(comment)
                .campaign(campaign).dateEndQueue(dateEndQueue).dateStartExe(dateStartExe).nbExe(nbExe)
                .nbExeUsefull(nbExeUsefull).nbOK(nbOK).nbKO(nbKO).nbFA(nbFA)
                .nbNA(nbNA).nbNE(nbNE).nbWE(nbWE).nbPE(nbPE).nbQU(nbQU).nbQE(nbQE)
                .nbCA(nbCA).ciScore(ciScore).ciScoreThreshold(ciScoreThreshold).ciScoreMax(ciScoreMax)
                .ciResult(ciResult).falseNegative(falseNegative).environmentList(environmentList)
                .countryList(countryList).robotDecliList(robotDecliList)
                .systemList(systemList).applicationList(applicationList)
                .reqEnvironmentList(reqEnvironmentList).reqCountryList(reqCountryList)
                .browserstackBuildHash(browserstackBuildHash).browserstackAppBuildHash(browserstackAppBuildHash)
                .lambdaTestBuild(lambdaTestBuild)
                .xRayURL(xRayURL).xRayTestExecution(xRayTestExecution).xRayURL(xRayURL).xRayMessage(xRayMEssage)
                .nbFlaky(nbFlaky).nbMuted(nbMuted).durationMs(duration)
                .usrCreated(usrCreated).dateCreated(dateCreated).usrModif(usrModif)
                .dateModif(dateModif).build();
    }
}
