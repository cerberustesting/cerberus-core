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
import org.apache.logging.log4j.LogManager;
import org.cerberus.crud.dao.ICampaignDAO;
import org.cerberus.crud.entity.Campaign;
import org.cerberus.crud.factory.IFactoryCampaign;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author memiks
 */
@Repository
public class CampaignDAO implements ICampaignDAO {

    private static final Logger LOG = LogManager.getLogger(CampaignDAO.class);

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryCampaign factoryCampaign;

    private final String OBJECT_NAME = "Campaign";
    private final int MAX_ROW_SELECTED = 100000;
    private final int SQL_DUPLICATED_CODE = 23000;

    @Override
    public AnswerList<Campaign> readByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        AnswerList<Campaign> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<Campaign> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM campaign cpg ");
        query.append(" WHERE 1=1");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (cpg.campaign like ?");
            searchSQL.append(" or cpg.distriblist like ?");
            searchSQL.append(" or cpg.description like ?)");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and ");
                String q = SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue());
                if (q == null || "".equals(q)) {
                    q = "(" + entry.getKey() + " IS NULL OR " + entry.getKey() + " = '')";
                }
                searchSQL.append(q);
                individalColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }

        query.append(searchSQL);

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
                }
                for (String individualColumnSearchValue : individalColumnSearchValues) {
                    preStat.setString(i++, individualColumnSearchValue);
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
    public AnswerItem<Campaign> readByKey(String key) {
        AnswerItem<Campaign> ans = new AnswerItem<>();
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM campaign cpg WHERE campaign = ?");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString())) {
            // Prepare and execute query
            preStat.setString(1, key);

            try (ResultSet resultSet = preStat.executeQuery();) {
                while (resultSet.next()) {
                    ans.setItem(loadFromResultSet(resultSet));
                }
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                        .resolveDescription("OPERATION", "SELECT");
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
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public AnswerItem<Campaign> readByKeyTech(int key) {
        AnswerItem<Campaign> ans = new AnswerItem<>();
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM campaign cpg WHERE campaignid = ?");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString())) {
            // Prepare and execute query
            preStat.setInt(1, key);
            try (ResultSet resultSet = preStat.executeQuery()) {
                while (resultSet.next()) {
                    ans.setItem(loadFromResultSet(resultSet));
                }
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                        .resolveDescription("OPERATION", "SELECT");
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
            ans.setResultMessage(msg);
        }
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
        query.append(" as distinctValues FROM campaign cpg");
        query.append(" WHERE 1=1");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (campaign like ?");
            searchSQL.append(" or distriblist like ?");
            searchSQL.append(" or description like ?)");
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

    @Override
    public Answer create(Campaign object) {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO campaign (`campaign`, `DistribList`, `NotifyStartTagExecution`, `NotifyEndTagExecution`"
                + ", SlackNotifyStartTagExecution, SlackNotifyEndTagExecution, SlackWebhook, SlackChannel"
                + ", CIScoreThreshold, Tag, Verbose, Screenshot, PageSource, RobotLog, Timeout, Retries, Priority, ManualExecution"
                + ", `Description`, LongDescription, Group1, Group2, Group3, UsrCreated) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                preStat.setString(i++, object.getCampaign());
                preStat.setString(i++, object.getDistribList());
                preStat.setString(i++, object.getNotifyStartTagExecution());
                preStat.setString(i++, object.getNotifyEndTagExecution());
                preStat.setString(i++, object.getSlackNotifyStartTagExecution());
                preStat.setString(i++, object.getSlackNotifyEndTagExecution());
                preStat.setString(i++, object.getSlackWebhook());
                preStat.setString(i++, object.getSlackChannel());
                preStat.setString(i++, object.getCIScoreThreshold());
                preStat.setString(i++, object.getTag());
                preStat.setString(i++, object.getVerbose());
                preStat.setString(i++, object.getScreenshot());
                preStat.setString(i++, object.getPageSource());
                preStat.setString(i++, object.getRobotLog());
                preStat.setString(i++, object.getTimeout());
                preStat.setString(i++, object.getRetries());
                preStat.setString(i++, object.getPriority());
                preStat.setString(i++, object.getManualExecution());
                preStat.setString(i++, object.getDescription());
                preStat.setString(i++, object.getLongDescription());
                preStat.setString(i++, object.getGroup1());
                preStat.setString(i++, object.getGroup2());
                preStat.setString(i++, object.getGroup3());
                preStat.setString(i++, object.getUsrCreated());

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
    public Answer update(Campaign object) {
        MessageEvent msg = null;
        final String query = "UPDATE campaign cpg SET campaign = ?, DistribList = ?, NotifyStartTagExecution = ?, NotifyEndTagExecution = ?"
                + ", SlackNotifyStartTagExecution = ?,  SlackNotifyEndTagExecution = ?, SlackWebhook = ?, SlackChannel = ?"
                + ", CIScoreThreshold = ?, Tag = ?, Verbose = ?, Screenshot = ?, PageSource = ?, RobotLog = ?, Timeout = ?, Retries = ?, Priority = ?, ManualExecution = ?"
                + ", Description = ?, LongDescription = ?, Group1 = ?, Group2 = ?, Group3 = ?, UsrModif = ?, DateModif =  NOW() WHERE campaignID = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setString(i++, object.getCampaign());
                preStat.setString(i++, object.getDistribList());
                preStat.setString(i++, object.getNotifyStartTagExecution());
                preStat.setString(i++, object.getNotifyEndTagExecution());
                preStat.setString(i++, object.getSlackNotifyStartTagExecution());
                preStat.setString(i++, object.getSlackNotifyEndTagExecution());
                preStat.setString(i++, object.getSlackWebhook());
                preStat.setString(i++, object.getSlackChannel());
                preStat.setString(i++, object.getCIScoreThreshold());
                preStat.setString(i++, object.getTag());
                preStat.setString(i++, object.getVerbose());
                preStat.setString(i++, object.getScreenshot());
                preStat.setString(i++, object.getPageSource());
                preStat.setString(i++, object.getRobotLog());
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
                preStat.setInt(i++, object.getCampaignID());

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
    public Answer delete(Campaign object) {
        MessageEvent msg = null;
        final String query = "DELETE FROM campaign WHERE campaignID = ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setInt(1, object.getCampaignID());

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
    public Campaign loadFromResultSet(ResultSet rs) throws SQLException {
        int campID = ParameterParserUtil.parseIntegerParam(rs.getString("cpg.campaignID"), 0);
        String camp = ParameterParserUtil.parseStringParam(rs.getString("cpg.campaign"), "");
        String distribList = ParameterParserUtil.parseStringParam(rs.getString("cpg.DistribList"), "");
        String notifyStartTagExecution = ParameterParserUtil.parseStringParam(rs.getString("cpg.NotifyStartTagExecution"), "N");
        String notifyEndTagExecution = ParameterParserUtil.parseStringParam(rs.getString("cpg.NotifyEndTagExecution"), "N");

        String slackNotifyStartTagExecution = ParameterParserUtil.parseStringParam(rs.getString("cpg.SlackNotifyStartTagExecution"), "N");
        String slackNotifyEndTagExecution = ParameterParserUtil.parseStringParam(rs.getString("cpg.SlackNotifyEndTagExecution"), "N");
        String slackWebhook = ParameterParserUtil.parseStringParam(rs.getString("cpg.SlackWebhook"), "");
        String slackChannel = ParameterParserUtil.parseStringParam(rs.getString("cpg.SlackChannel"), "");

        String cIScoreThreshold = ParameterParserUtil.parseStringParam(rs.getString("cpg.CIScoreThreshold"), "");
        String tag = ParameterParserUtil.parseStringParam(rs.getString("cpg.Tag"), "");
        String verbose = ParameterParserUtil.parseStringParam(rs.getString("cpg.Verbose"), "");
        String screenshot = ParameterParserUtil.parseStringParam(rs.getString("cpg.Screenshot"), "");
        String pageSource = ParameterParserUtil.parseStringParam(rs.getString("cpg.PageSource"), "");
        String robotLog = ParameterParserUtil.parseStringParam(rs.getString("cpg.RobotLog"), "");
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

        return factoryCampaign.create(campID, camp, distribList, notifyStartTagExecution, notifyEndTagExecution,
                slackNotifyStartTagExecution, slackNotifyEndTagExecution, slackWebhook, slackChannel,
                cIScoreThreshold,
                tag, verbose, screenshot, pageSource, robotLog, timeout, retries, priority, manualExecution,
                desc, longDesc, group1, group2, group3,
                usrCreated, dateCreated, usrModif, dateModif);
    }

}
