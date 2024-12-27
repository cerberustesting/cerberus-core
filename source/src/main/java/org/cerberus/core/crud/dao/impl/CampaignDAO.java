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
import org.cerberus.core.crud.dao.ICampaignDAO;
import org.cerberus.core.crud.entity.Campaign;
import org.cerberus.core.crud.factory.IFactoryCampaign;
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
 * @author memiks
 */
@AllArgsConstructor
@Repository
public class CampaignDAO implements ICampaignDAO {

    private final DatabaseSpring databaseSpring;
    private final IFactoryCampaign factoryCampaign;

    private static final Logger LOG = LogManager.getLogger(CampaignDAO.class);
    private static final String OBJECT_NAME = "Campaign";
    private static final int MAX_ROW_SELECTED = 100000;
    private static final int SQL_DUPLICATED_CODE = 23000;

    @Override
    public AnswerList<Campaign> readByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        AnswerList<Campaign> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<Campaign> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individualColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder()
                .append("SELECT SQL_CALC_FOUND_ROWS * FROM campaign cpg ")
                .append(" WHERE 1=1");

        if (StringUtil.isNotEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (cpg.campaign like ?");
            searchSQL.append(" or cpg.description like ?)");
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

        if (StringUtil.isNotEmptyOrNull(column)) {
            query.append(" order by ").append(column).append(" ").append(dir);
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
            }
            for (String individualColumnSearchValue : individualColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
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
    public AnswerItem<Campaign> readByKey(String key) {
        AnswerItem<Campaign> ans = new AnswerItem<>();
        MessageEvent msg;
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM campaign cpg WHERE campaign = ?");

        LOG.debug("SQL : {}", query);
        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString())) {
            preStat.setString(1, key);
            try (ResultSet resultSet = preStat.executeQuery()) {
                while (resultSet.next()) {
                    ans.setItem(loadFromResultSet(resultSet));
                }
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                        .resolveDescription("OPERATION", "SELECT");
            }
        } catch (Exception e) {
            LOG.warn("Unable to execute query : {}", e.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        }
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public AnswerItem<Campaign> readByKeyTech(int key) {
        AnswerItem<Campaign> ans = new AnswerItem<>();
        MessageEvent msg;
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM campaign cpg WHERE campaignid = ?");

        LOG.debug("SQL : {}", query);
        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString())) {
            preStat.setInt(1, key);
            try (ResultSet resultSet = preStat.executeQuery()) {
                while (resultSet.next()) {
                    ans.setItem(loadFromResultSet(resultSet));
                }
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                        .resolveDescription("OPERATION", "SELECT");
            }
        } catch (Exception e) {
            LOG.error("Unable to execute query : {}", e.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
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

        StringBuilder query = new StringBuilder()
                .append("SELECT distinct ")
                .append(columnName)
                .append(" as distinctValues FROM campaign cpg")
                .append(" WHERE 1=1");

        if (StringUtil.isNotEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (campaign like ?");
            searchSQL.append(" or description like ?)");
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

        LOG.debug("SQL : {}", query);
        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString());
             Statement stm = connection.createStatement()) {

            int i = 1;
            if (StringUtil.isNotEmptyOrNull(searchTerm)) {
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
            LOG.warn("Unable to execute query : {}", e.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        }

        answer.setResultMessage(msg);
        answer.setDataList(distinctValues);
        return answer;
    }

    @Override
    public Answer create(Campaign object) {
        MessageEvent msg;
        StringBuilder query = new StringBuilder()
                .append("INSERT INTO campaign (`campaign`")
                .append(", CIScoreThreshold, Tag, Verbose, Screenshot, Video, PageSource, RobotLog, ConsoleLog, Timeout, Retries, Priority, ManualExecution")
                .append(", `Description`, LongDescription, Group1, Group2, Group3, UsrCreated) ")
                .append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        LOG.debug("SQL : {}", query);
        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString())) {

            int i = 1;
            preStat.setString(i++, object.getCampaign());
            preStat.setString(i++, object.getCIScoreThreshold());
            preStat.setString(i++, object.getTag());
            preStat.setString(i++, object.getVerbose());
            preStat.setString(i++, object.getScreenshot());
            preStat.setString(i++, object.getVideo());
            preStat.setString(i++, object.getPageSource());
            preStat.setString(i++, object.getRobotLog());
            preStat.setString(i++, object.getConsoleLog());
            preStat.setString(i++, object.getTimeout());
            preStat.setString(i++, object.getRetries());
            preStat.setString(i++, object.getPriority());
            preStat.setString(i++, object.getManualExecution());
            preStat.setString(i++, object.getDescription());
            preStat.setString(i++, object.getLongDescription());
            preStat.setString(i++, object.getGroup1());
            preStat.setString(i++, object.getGroup2());
            preStat.setString(i++, object.getGroup3());
            preStat.setString(i, object.getUsrCreated());

            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        return new Answer(msg);
    }

    @Override
    public Answer update(String originalCampaign, Campaign object) {
        MessageEvent msg;
        final String query = "UPDATE campaign cpg SET campaign = ?"
                + ", CIScoreThreshold = ?, Tag = ?, Verbose = ?, Screenshot = ?, Video = ?, PageSource = ?, RobotLog = ?, ConsoleLog = ?, Timeout = ?, Retries = ?, Priority = ?, ManualExecution = ?"
                + ", Description = ?, LongDescription = ?, Group1 = ?, Group2 = ?, Group3 = ?, UsrModif = ?, DateModif =  NOW() WHERE campaignID = ?";

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.campaign : {}", object.getCampaign());
        LOG.debug("SQL.param.campaignid : {}", object.getCampaignID());

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query)) {

            int i = 1;
            preStat.setString(i++, object.getCampaign());
            preStat.setString(i++, object.getCIScoreThreshold());
            preStat.setString(i++, object.getTag());
            preStat.setString(i++, object.getVerbose());
            preStat.setString(i++, object.getScreenshot());
            preStat.setString(i++, object.getVideo());
            preStat.setString(i++, object.getPageSource());
            preStat.setString(i++, object.getRobotLog());
            preStat.setString(i++, object.getConsoleLog());
            preStat.setString(i++, object.getTimeout());
            preStat.setString(i++, object.getRetries());
            preStat.setString(i++, object.getPriority());
            preStat.setString(i++, object.getManualExecution());
            preStat.setString(i++, object.getDescription());
            preStat.setString(i++, object.getLongDescription());
            preStat.setString(i++, object.getGroup1());
            preStat.setString(i++, object.getGroup2());
            preStat.setString(i++, object.getGroup3());
            preStat.setString(i++, object.getUsrModif());
            preStat.setInt(i, object.getCampaignID());

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
    public Answer delete(Campaign object) {
        MessageEvent msg;
        final String query = "DELETE FROM campaign WHERE campaignID = ? ";

        LOG.debug("SQL : {}", query);
        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query)) {
            preStat.setInt(1, object.getCampaignID());

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
    public Campaign loadFromResultSet(ResultSet rs) throws SQLException {
        int campID = ParameterParserUtil.parseIntegerParam(rs.getString("cpg.campaignID"), 0);
        String camp = ParameterParserUtil.parseStringParam(rs.getString("cpg.campaign"), "");
        String cIScoreThreshold = ParameterParserUtil.parseStringParam(rs.getString("cpg.CIScoreThreshold"), "");
        String tag = ParameterParserUtil.parseStringParam(rs.getString("cpg.Tag"), "");
        String verbose = ParameterParserUtil.parseStringParam(rs.getString("cpg.Verbose"), "");
        String screenshot = ParameterParserUtil.parseStringParam(rs.getString("cpg.Screenshot"), "");
        String video = ParameterParserUtil.parseStringParam(rs.getString("cpg.Video"), "");
        String pageSource = ParameterParserUtil.parseStringParam(rs.getString("cpg.PageSource"), "");
        String robotLog = ParameterParserUtil.parseStringParam(rs.getString("cpg.RobotLog"), "");
        String consoleLog = ParameterParserUtil.parseStringParam(rs.getString("cpg.ConsoleLog"), "");
        String timeout = ParameterParserUtil.parseStringParam(rs.getString("cpg.Timeout"), "");
        String retries = ParameterParserUtil.parseStringParam(rs.getString("cpg.Retries"), "");
        String priority = ParameterParserUtil.parseStringParam(rs.getString("cpg.Priority"), "");
        String manualExecution = ParameterParserUtil.parseStringParam(rs.getString("cpg.ManualExecution"), "");
        String desc = ParameterParserUtil.parseStringParam(rs.getString("cpg.description"), "");
        String longDesc = ParameterParserUtil.parseStringParam(rs.getString("cpg.LongDescription"), "");
        String group1 = ParameterParserUtil.parseStringParam(rs.getString("cpg.Group1"), "");
        String group2 = ParameterParserUtil.parseStringParam(rs.getString("cpg.Group2"), "");
        String group3 = ParameterParserUtil.parseStringParam(rs.getString("cpg.Group3"), "");
        String usrModif = ParameterParserUtil.parseStringParam(rs.getString("cpg.UsrModif"), "");
        String usrCreated = ParameterParserUtil.parseStringParam(rs.getString("cpg.UsrCreated"), "");
        Timestamp dateModif = rs.getTimestamp("cpg.DateModif");
        Timestamp dateCreated = rs.getTimestamp("cpg.DateCreated");

        return factoryCampaign.create(campID, camp,
                cIScoreThreshold,
                tag, verbose, screenshot, video, pageSource, robotLog, consoleLog, timeout, retries, priority, manualExecution,
                desc, longDesc, group1, group2, group3,
                usrCreated, dateCreated, usrModif, dateModif);
    }
}
