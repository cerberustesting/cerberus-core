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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.ITagStatisticDAO;
import org.cerberus.core.crud.entity.TagStatistic;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.SqlUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Repository
public class TagStatisticDAO implements ITagStatisticDAO {

    private static final int MAX_ROW_SELECTED = 100000;

    private final DatabaseSpring databaseSpring;

    private static final String OBJECT_NAME = "TagStatistic";

    private static final Logger LOG = LogManager.getLogger(TagStatisticDAO.class);

    /**
     * Insert a unique line of TagStatistic in database
     *
     * @param object
     * @return
     */
    @Override
    public Answer create(TagStatistic object) {
        throw new UnsupportedOperationException();
    }

    /**
     * Insert a list of tagStatistics in only one INSERT statement
     *
     * @param tagStatistics
     * @return
     */
    @Override
    public Answer createWithMap(Map<String, TagStatistic> tagStatistics) {
        MessageEvent msg;
        StringBuilder placeholders = new StringBuilder();
        String baseQuery = "INSERT INTO tagstatistic (`Tag`, `Country`, `Environment`, `Campaign`, `CampaignGroup1`, `SystemList`, `ApplicationList`, `DateStartExe`, `DateEndExe`, `NbExe`, `NbExeUsefull`, `NbOK`, `nbKO`, `nbFA`, `nbNA`, `nbNE`, `nbWE`, `nbPE`, `nbQU`, `nbQE`, `nbCA`, `UsrCreated`) VALUES ";
        placeholders.append("(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        if (tagStatistics.size() > 1) {
            for (int i = 1; i < tagStatistics.size(); i++) {
                placeholders.append(", (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            }
        }
        String query = baseQuery + placeholders.toString();

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query)) {
            int parameterIndex = 1;
            for (Map.Entry<String, TagStatistic> tagStatisticEntry : tagStatistics.entrySet()) {
                preStat.setString(parameterIndex++, tagStatisticEntry.getValue().getTag());
                preStat.setString(parameterIndex++, tagStatisticEntry.getValue().getCountry());
                preStat.setString(parameterIndex++, tagStatisticEntry.getValue().getEnvironment());
                preStat.setString(parameterIndex++, tagStatisticEntry.getValue().getCampaign());
                preStat.setString(parameterIndex++, tagStatisticEntry.getValue().getCampaignGroup1());
                preStat.setString(parameterIndex++, tagStatisticEntry.getValue().getSystemList());
                preStat.setString(parameterIndex++, tagStatisticEntry.getValue().getApplicationList());
                preStat.setTimestamp(parameterIndex++, tagStatisticEntry.getValue().getDateStartExe());
                preStat.setTimestamp(parameterIndex++, tagStatisticEntry.getValue().getDateEndExe());
                preStat.setInt(parameterIndex++, tagStatisticEntry.getValue().getNbExe());
                preStat.setInt(parameterIndex++, tagStatisticEntry.getValue().getNbExeUseful());
                preStat.setInt(parameterIndex++, tagStatisticEntry.getValue().getNbOK());
                preStat.setInt(parameterIndex++, tagStatisticEntry.getValue().getNbKO());
                preStat.setInt(parameterIndex++, tagStatisticEntry.getValue().getNbFA());
                preStat.setInt(parameterIndex++, tagStatisticEntry.getValue().getNbNA());
                preStat.setInt(parameterIndex++, tagStatisticEntry.getValue().getNbNE());
                preStat.setInt(parameterIndex++, tagStatisticEntry.getValue().getNbWE());
                preStat.setInt(parameterIndex++, tagStatisticEntry.getValue().getNbPE());
                preStat.setInt(parameterIndex++, tagStatisticEntry.getValue().getNbQU());
                preStat.setInt(parameterIndex++, tagStatisticEntry.getValue().getNbQE());
                preStat.setInt(parameterIndex++, tagStatisticEntry.getValue().getNbCA());
                preStat.setString(parameterIndex++, tagStatisticEntry.getValue().getUsrCreated());
            }

            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_DUPLICATE);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT").replace("%REASON%", exception.toString()));
        }
        return new Answer(msg);
    }

    /**
     * Get a TagStatistic object from database
     *
     * @param object
     * @return
     */
    @Override
    public Answer read(TagStatistic object) {
        return null;
    }

    @Override
    public AnswerList<TagStatistic> readByCriteria(List<String> systems, List<String> applications, List<String> group1List, String minDate, String maxDate) {
        AnswerList<TagStatistic> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TagStatistic> tagStatistics = new ArrayList<>();

        StringBuilder query = new StringBuilder();

        query.append("SELECT `Id`, `Tag`, `Country`, `Environment`, `Campaign`, `CampaignGroup1`, `SystemList`, `ApplicationList`, `DateStartExe`, `DateEndExe`, `NbExe`, `NbExeUsefull`, `NbOK`, `NbKO`, `NbFA`, `NbNA`, `NbNE`, `NbPE`, `NbWE`, `NbPE`, `NbQU`, `NbQE`, `NbCA` from tagstatistic WHERE `Campaign` IN (SELECT DISTINCT `Campaign` FROM tagstatistic");

        String systemRegex = "";
        String applicationRegex = "";

        if (systems!=null && !systems.isEmpty()) {
            systemRegex = systems.stream()
                    .map(sys -> "\"" + sys + "\"")
                    .collect(Collectors.joining("|"));
            query.append(" WHERE `SystemList` REGEXP ?");
        }

        if (applications != null && !applications.isEmpty()) {
            if (systems.isEmpty()) {
                query.append(" WHERE `ApplicationList` REGEXP ?");
            } else {
                query.append(" AND `ApplicationList` REGEXP ?");
            }
            applicationRegex = applications.stream()
                    .map(app -> "\"" + app + "\"")
                    .collect(Collectors.joining("|"));
        }

        if (group1List != null && !group1List.isEmpty()) {
            query.append(" AND ").append(SqlUtil.generateInClause("CampaignGroup1", group1List));
        }

        query.append(") AND `DateStartExe` >= ? AND `DateEndExe` <= ?");

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); Statement stm = connection.createStatement()) {

            int i = 1;

            if (systems!=null && !systems.isEmpty()) {
                preStat.setString(i++, systemRegex);
            }
            if (applications != null && !applications.isEmpty()) {
                preStat.setString(i++, applicationRegex);
            }

            if (group1List != null && !group1List.isEmpty()) {
                for (String group1 : group1List) {
                    preStat.setString(i++, group1.replaceAll("%20", " ")); //Replace %20 (encoded space) by decoded space in case of group 1 contains spaces
                }
            }

            preStat.setString(i++, minDate);
            preStat.setString(i++, maxDate);

            try (ResultSet resultSet = preStat.executeQuery(); ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {

                LOG.info("Execute SQL Statement: {} ", preStat);

                while (resultSet.next()) {
                    tagStatistics.add(this.loadFromResultSet(resultSet));
                }

                int nrTotalRows = 0;
                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }

                if (tagStatistics.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                } else if (tagStatistics.isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                }
                response = new AnswerList<>(tagStatistics, nrTotalRows);
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        response.setResultMessage(msg);
        response.setDataList(tagStatistics);
        return response;
    }

    @Override
    public AnswerList<TagStatistic> readByCriteria(String campaign, List<String> countries, List<String> environments, String minDate, String maxDate) {
        AnswerList<TagStatistic> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TagStatistic> tagStatistics = new ArrayList<>();

        StringBuilder query = new StringBuilder();

        query.append("SELECT `Id`, `Tag`, `Country`, `Environment`, `Campaign`, `CampaignGroup1`, `SystemList`, `ApplicationList`, `DateStartExe`, `DateEndExe`, `NbExe`, `NbExeUsefull`, `NbOK`, `NbKO`, `NbFA`, `NbNA`, `NbNE`, `NbPE`, `NbWE`, `NbPE`, `NbQU`, `NbQE`, `NbCA` from tagstatistic WHERE `Campaign` = ? AND `DateStartExe` >= ? AND `DateEndExe` <= ?");

        if (!countries.isEmpty()) {
            query.append(" AND ").append(SqlUtil.generateInClause("`Country`", countries));
        }

        if (!environments.isEmpty()) {
            query.append(" AND ").append(SqlUtil.generateInClause("`Environment`", environments));
        }

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); Statement stm = connection.createStatement()) {

            int i = 1;

            preStat.setString(i++, campaign);
            preStat.setString(i++, minDate);
            preStat.setString(i++, maxDate);

            if (!countries.isEmpty()) {
                for (String country : countries) {
                    preStat.setString(i++, country);
                }
            }

            if (!environments.isEmpty()) {
                for (String environment : environments) {
                    preStat.setString(i++, environment);
                }
            }

            try (ResultSet resultSet = preStat.executeQuery(); ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {

                LOG.info("Execute SQL Statement: {} ", preStat);

                while (resultSet.next()) {
                    tagStatistics.add(this.loadFromResultSet(resultSet));
                }

                int nrTotalRows = 0;
                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }

                if (tagStatistics.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                } else if (tagStatistics.isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                }
                response = new AnswerList<>(tagStatistics, nrTotalRows);
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        response.setResultMessage(msg);
        response.setDataList(tagStatistics);
        return response;
    }

    /**
     * Get a TagStatistics list by tag from database
     *
     * @param tag
     * @return
     */
    @Override
    public AnswerList<TagStatistic> readByTag(String tag) {
        AnswerList<TagStatistic> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TagStatistic> tagStatistics = new ArrayList<>();

        String query = "SELECT * FROM `tagstatistic` WHERE `tag` = ?";
        LOG.debug("SQL : {}", query);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query); Statement stm = connection.createStatement()) {
            preStat.setString(1, tag);

            try (ResultSet resultSet = preStat.executeQuery(); ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {

                while (resultSet.next()) {
                    tagStatistics.add(this.loadFromResultSet(resultSet));
                }

                int nrTotalRows = 0;
                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }

                if (tagStatistics.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                } else if (tagStatistics.isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                }
                response = new AnswerList<>(tagStatistics, nrTotalRows);
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }
        response.setResultMessage(msg);
        response.setDataList(tagStatistics);
        return response;
    }

    /**
     * Update a TagStatistic
     *
     * @param object
     * @return
     */
    @Override
    public Answer update(TagStatistic object) {
        throw new UnsupportedOperationException();
    }

    /**
     * Delete a TagStatistic object in database
     *
     * @param tag
     * @param object
     * @return
     */
    @Override
    public Answer delete(String tag, TagStatistic object) {
        throw new UnsupportedOperationException();
    }

    /**
     * Convert a database result set into entity
     *
     * @param resultSet
     * @return
     * @throws SQLException
     */
    private TagStatistic loadFromResultSet(ResultSet resultSet) throws SQLException {
        long id = ParameterParserUtil.parseLongParam(resultSet.getString("Id"), 0);
        String tag = ParameterParserUtil.parseStringParam(resultSet.getString("Tag"), "");
        String country = ParameterParserUtil.parseStringParam(resultSet.getString("Country"), "");
        String environment = ParameterParserUtil.parseStringParam(resultSet.getString("Environment"), "");
        String campaign = ParameterParserUtil.parseStringParam(resultSet.getString("Campaign"), "");
        String campaignGroup1 = ParameterParserUtil.parseStringParam(resultSet.getString("CampaignGroup1"), "");
        String systemList = ParameterParserUtil.parseStringParam(resultSet.getString("SystemList"), "");
        String applicationList = ParameterParserUtil.parseStringParam(resultSet.getString("ApplicationList"), "");
        Timestamp dateStartExe = resultSet.getTimestamp("DateStartExe");
        Timestamp dateEndExe = resultSet.getTimestamp("DateEndExe");
        int nbExe = resultSet.getInt("nbExe");
        int nbExeUseful = resultSet.getInt("nbExeUsefull");
        int nbOK = resultSet.getInt("nbOK");
        int nbKO = resultSet.getInt("nbKO");
        int nbFA = resultSet.getInt("nbFA");
        int nbNA = resultSet.getInt("nbNA");
        int nbNE = resultSet.getInt("nbNE");
        int nbWE = resultSet.getInt("nbWE");
        int nbPE = resultSet.getInt("nbPE");
        int nbQU = resultSet.getInt("nbQU");
        int nbQE = resultSet.getInt("nbQE");
        int nbCA = resultSet.getInt("nbCA");

        return TagStatistic.builder()
                .id(id)
                .tag(tag)
                .country(country)
                .environment(environment)
                .campaign(campaign)
                .campaignGroup1(campaignGroup1)
                .systemList(systemList)
                .applicationList(applicationList)
                .dateStartExe(dateStartExe)
                .dateEndExe(dateEndExe)
                .nbExe(nbExe)
                .nbExeUseful(nbExeUseful)
                .nbOK(nbOK)
                .nbKO(nbKO)
                .nbFA(nbFA)
                .nbNA(nbNA)
                .nbNE(nbNE)
                .nbWE(nbWE)
                .nbPE(nbPE)
                .nbQU(nbQU)
                .nbQE(nbQE)
                .nbCA(nbCA)
                .build();
    }
}
