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
import org.cerberus.core.crud.dao.ICampaignLabelDAO;
import org.cerberus.core.crud.entity.CampaignLabel;
import org.cerberus.core.crud.entity.Label;
import org.cerberus.core.crud.factory.IFactoryCampaignLabel;
import org.cerberus.core.crud.factory.IFactoryLabel;
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
public class CampaignLabelDAO implements ICampaignLabelDAO {

    private final DatabaseSpring databaseSpring;
    private final IFactoryCampaignLabel factoryCampaignLabel;
    private final IFactoryLabel factoryLabel;

    private static final Logger LOG = LogManager.getLogger(CampaignLabelDAO.class);
    private static final String OBJECT_NAME = "Campaign Label";
    private static final String SQL_DUPLICATED_CODE = "23000";
    private static final int MAX_ROW_SELECTED = 100000;

    @Override
    public AnswerItem<CampaignLabel> readByKeyTech(Integer campaignLabelID) {
        AnswerItem<CampaignLabel> ans = new AnswerItem<>();
        CampaignLabel result;
        final String query = "SELECT * FROM `campaignlabel` cpl JOIN label lab ON lab.id = cpl.labelid WHERE `campaignlabelid` = ? ";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.campaignlabelid : {}", campaignLabelID);
        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            preStat.setInt(1, campaignLabelID);
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
    public AnswerItem<CampaignLabel> readByKey(String campaign, Integer labelId) {
        AnswerItem<CampaignLabel> ans = new AnswerItem<>();
        CampaignLabel result;
        final String query = "SELECT * FROM `campaignlabel` cpl JOIN label lab ON lab.id = cpl.labelid WHERE `campaign` = ? and `labelid` = ? ";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.campaign : {}", campaign);
        LOG.debug("SQL.param.labelid : {}", labelId);
        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

            preStat.setString(1, campaign);
            preStat.setInt(2, labelId);
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
    public AnswerList<CampaignLabel> readByVariousByCriteria(String campaign, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        AnswerList<CampaignLabel> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<CampaignLabel> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder()
                .append("SELECT SQL_CALC_FOUND_ROWS * FROM campaignlabel cpl ")
                .append("JOIN label lab ON lab.id = cpl.labelid");

        searchSQL.append(" where 1=1 ");

        if (StringUtil.isNotEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (cpl.`campaignlabelid` like ?");
            searchSQL.append(" or cpl.`campaign` like ?");
            searchSQL.append(" or cpl.`labelid` like ?");
            searchSQL.append(" or cpl.`usrCreated` like ?");
            searchSQL.append(" or cpl.`usrModif` like ?");
            searchSQL.append(" or cpl.`dateCreated` like ?");
            searchSQL.append(" or cpl.`dateModif` like ?)");
        }
        if (MapUtils.isNotEmpty(individualSearch)) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and ");
                searchSQL.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individalColumnSearchValues.addAll(entry.getValue());
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
            }
            for (String individualColumnSearchValue : individalColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }
            if (StringUtil.isNotEmptyOrNull(campaign)) {
                preStat.setString(i, campaign);
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
    public Answer create(CampaignLabel object) {
        MessageEvent msg;
        StringBuilder query = new StringBuilder()
                .append("INSERT INTO campaignlabel (`campaign`, `labelid`, `usrcreated`) ")
                .append("VALUES (?,?,?)");

        LOG.debug("SQL : {}", query);
        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString())) {

            int i = 1;
            preStat.setString(i++, object.getCampaign());
            preStat.setInt(i++, object.getLabelId());
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
    public Answer delete(CampaignLabel object) {
        MessageEvent msg;
        final String query = "DELETE FROM campaignlabel WHERE `campaignlabelid` = ? ";

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.campaignlabelid : {}", object.getCampaignLabelID());

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query)) {

            preStat.setInt(1, object.getCampaignLabelID());
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
    public Answer update(CampaignLabel object) {
        MessageEvent msg;
        final String query = "UPDATE campaignlabel SET campaign = ?, labelid = ? ,"
                + "dateModif = NOW(), usrModif= ?  WHERE `campaignlabelid` = ? ";

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.service : {}", object.getCampaignLabelID());
        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query)) {

            int i = 1;
            preStat.setString(i++, object.getCampaign());
            preStat.setInt(i++, object.getLabelId());
            preStat.setString(i++, object.getUsrModif());
            preStat.setInt(i, object.getCampaignLabelID());
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

        StringBuilder query = new StringBuilder()
                .append("SELECT distinct ")
                .append(columnName)
                .append(" as distinctValues FROM campaignlabel ");

        searchSQL.append("WHERE 1=1");
        if (StringUtil.isNotEmptyOrNull(campaign)) {
            searchSQL.append(" and (`campaign` = ? )");
        }

        if (StringUtil.isNotEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (cpl.`campaignlabelid` like ?");
            searchSQL.append(" or cpl.`campaign` like ?");
            searchSQL.append(" or cpl.`labelid` like ?");
            searchSQL.append(" or cpl.`usrCreated` like ?");
            searchSQL.append(" or cpl.`usrModif` like ?");
            searchSQL.append(" or cpl.`dateCreated` like ?");
            searchSQL.append(" or cpl.`dateModif` like ?)");
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
            if (StringUtil.isNotEmptyOrNull(campaign)) {
                preStat.setString(i++, campaign);
            }
            if (StringUtil.isNotEmptyOrNull(searchTerm)) {
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
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }

        answer.setResultMessage(msg);
        answer.setDataList(distinctValues);
        return answer;
    }

    @Override
    public CampaignLabel loadFromResultSet(ResultSet rs) throws SQLException {
        int campaignLabelId = ParameterParserUtil.parseIntegerParam(rs.getString("cpl.campaignlabelid"), 0);
        String campaign = ParameterParserUtil.parseStringParam(rs.getString("cpl.campaign"), "");
        int labelId = ParameterParserUtil.parseIntegerParam(rs.getString("cpl.labelid"), 0);
        String usrModif = ParameterParserUtil.parseStringParam(rs.getString("cpl.UsrModif"), "");
        String usrCreated = ParameterParserUtil.parseStringParam(rs.getString("cpl.UsrCreated"), "");
        Timestamp dateModif = rs.getTimestamp("cpl.DateModif");
        Timestamp dateCreated = rs.getTimestamp("cpl.DateCreated");

        int id = ParameterParserUtil.parseIntegerParam(rs.getString("lab.id"), 0);
        String system = ParameterParserUtil.parseStringParam(rs.getString("lab.system"), "");
        String label = ParameterParserUtil.parseStringParam(rs.getString("lab.label"), "");
        String type = ParameterParserUtil.parseStringParam(rs.getString("lab.type"), "");
        String color = ParameterParserUtil.parseStringParam(rs.getString("lab.color"), "");
        String reqType = ParameterParserUtil.parseStringParam(rs.getString("lab.RequirementType"), "");
        String reqStatus = ParameterParserUtil.parseStringParam(rs.getString("lab.RequirementStatus"), "");
        String reqCriticity = ParameterParserUtil.parseStringParam(rs.getString("lab.RequirementCriticity"), "");
        int parentLabel = Integer.parseInt(ParameterParserUtil.parseStringParam(rs.getString("lab.parentLabelid"), "0"));
        String description = ParameterParserUtil.parseStringParam(rs.getString("lab.description"), "");
        String longDesc = ParameterParserUtil.parseStringParam(rs.getString("lab.LongDescription"), "");
        String usrCreated1 = ParameterParserUtil.parseStringParam(rs.getString("lab.usrCreated"), "");
        Timestamp dateCreated1 = rs.getTimestamp("lab.dateCreated");
        String usrModif1 = ParameterParserUtil.parseStringParam(rs.getString("lab.usrModif"), "");
        Timestamp dateModif1 = rs.getTimestamp("lab.dateModif");

        Label labelObj = factoryLabel.create(id, system, label, type, color, parentLabel, reqType, reqStatus, reqCriticity, description, longDesc, usrCreated1, dateCreated1, usrModif1, dateModif1);
        CampaignLabel res = factoryCampaignLabel.create(campaignLabelId, campaign, labelId, usrCreated, dateCreated, usrModif, dateModif);
        res.setLabel(labelObj);
        return res;
    }
}
