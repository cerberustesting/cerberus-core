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
import org.cerberus.core.crud.dao.IBuildRevisionParametersDAO;
import org.cerberus.core.crud.entity.BuildRevisionParameters;
import org.cerberus.core.crud.factory.IFactoryBuildRevisionParameters;
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

@AllArgsConstructor
@Repository
public class BuildRevisionParametersDAO implements IBuildRevisionParametersDAO {

    private final DatabaseSpring databaseSpring;
    private final IFactoryBuildRevisionParameters factoryBuildRevisionParameters;

    private static final Logger LOG = LogManager.getLogger(BuildRevisionParametersDAO.class);
    private static final String OBJECT_NAME = "BuildRevisionParameters";
    private static final String SQL_DUPLICATED_CODE = "23000";
    private static final int MAX_ROW_SELECTED = 100000;

    @Override
    public AnswerItem<BuildRevisionParameters> readByKeyTech(int id) {
        AnswerItem<BuildRevisionParameters> ans = new AnswerItem<>();
        BuildRevisionParameters result;
        final String query = "SELECT * FROM `buildrevisionparameters` WHERE `id` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        LOG.debug("SQL : {}", query);
        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

            preStat.setInt(1, id);
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
    public AnswerItem<BuildRevisionParameters> readLastBySystem(String system) {
        AnswerItem<BuildRevisionParameters> ans = new AnswerItem<>();
        BuildRevisionParameters result;
        StringBuilder query = new StringBuilder()
                .append("SELECT * from buildrevisionparameters brp ")
                .append("   left outer join buildrevisioninvariant bri1 on brp.build = bri1.versionname and bri1.level=1 and bri1.`System` = ? ")
                .append("   left outer join buildrevisioninvariant bri2 on brp.revision = bri2.versionname and bri2.level=2 and bri2.`System` = ? ")
                .append("WHERE 1=1 ")
                .append("AND application in (SELECT application FROM application WHERE `System` = ? ) ")
                .append("ORDER BY bri1.seq desc, bri2.seq desc, brp.datecre desc limit 1; ");

        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        LOG.debug("SQL : {}", query);
        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            int i = 1;
            preStat.setString(i++, system);
            preStat.setString(i++, system);
            preStat.setString(i, system);
            try (ResultSet resultSet = preStat.executeQuery()) {
                if (resultSet.first()) {
                    result = loadFromResultSet(resultSet);
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT LAST"));
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
    public AnswerItem<BuildRevisionParameters> readByVarious2(String build, String revision, String release, String application) {
        AnswerItem<BuildRevisionParameters> ans = new AnswerItem<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        BuildRevisionParameters result;
        StringBuilder query = new StringBuilder()
                .append("Select * FROM  buildrevisionparameters ")
                .append(" WHERE build= ? and revision= ? and `release` = ?  and application = ? ");

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            preStat.setString(1, build);
            preStat.setString(2, revision);
            preStat.setString(3, release);
            preStat.setString(4, application);
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
    public AnswerList<BuildRevisionParameters> readByVarious1ByCriteria(String system, String application, String build, String revision, int start, int amount,
                                                                        String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        AnswerList<BuildRevisionParameters> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<BuildRevisionParameters> brpList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individualColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM buildrevisionparameters ");
        searchSQL.append(" where 1=1 ");

        if (StringUtil.isNotEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (`id` like ?");
            searchSQL.append(" or `Build` like ?");
            searchSQL.append(" or `Revision` like ?");
            searchSQL.append(" or `Release` like ?");
            searchSQL.append(" or `Application` like ?");
            searchSQL.append(" or `Project` like ?");
            searchSQL.append(" or `TicketIDFixed` like ?");
            searchSQL.append(" or `BugIDFixed` like ?");
            searchSQL.append(" or `Link` like ?");
            searchSQL.append(" or `ReleaseOwner` like ?");
            searchSQL.append(" or `datecre` like ?");
            searchSQL.append(" or `jenkinsbuildid` like ?");
            searchSQL.append(" or `mavengroupid` like ?");
            searchSQL.append(" or `mavenartifactid` like ?");
            searchSQL.append(" or `repositoryurl` like ?");
            searchSQL.append(" or `mavenversion` like ? )");
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
        if (StringUtil.isNotEmptyOrNull(system)) {
            searchSQL.append(" and application in (SELECT application FROM application WHERE `System` = ? )");
        }
        if (StringUtil.isNotEmptyOrNull(application)) {
            searchSQL.append(" and (`Application`= ? )");
        }
        if (StringUtil.isNotEmptyOrNull(build)) {
            searchSQL.append(" and (`Build`= ? )");
        }
        if (StringUtil.isNotEmptyOrNull(revision)) {
            searchSQL.append(" and (`Revision`= ? )");
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
            if (StringUtil.isNotEmptyOrNull(system)) {
                preStat.setString(i++, system);
            }
            if (StringUtil.isNotEmptyOrNull(application)) {
                preStat.setString(i++, application);
            }
            if (StringUtil.isNotEmptyOrNull(build)) {
                preStat.setString(i++, build);
            }
            if (StringUtil.isNotEmptyOrNull(revision)) {
                preStat.setString(i, revision);
            }

            try (ResultSet resultSet = preStat.executeQuery();
                 ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {
                while (resultSet.next()) {
                    brpList.add(this.loadFromResultSet(resultSet));
                }

                int nrTotalRows = 0;
                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }

                if (brpList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                } else if (brpList.isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                }
                response = new AnswerList<>(brpList, nrTotalRows);
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }

        response.setResultMessage(msg);
        response.setDataList(brpList);
        return response;
    }

    @Override
    public AnswerList<BuildRevisionParameters> readMaxSVNReleasePerApplication(String system, String build, String revision, String lastBuild, String lastRevision) {
        AnswerList<BuildRevisionParameters> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<BuildRevisionParameters> brpList = new ArrayList<>();

        StringBuilder query = new StringBuilder()
                .append("SELECT * from ( ")
                .append("SELECT Application, max(`rel1`) rel FROM (")
                .append("SELECT brp.Application, CAST(`Release` AS UNSIGNED) rel1 ")
                .append(" FROM buildrevisionparameters brp ")
                .append("JOIN application a on a.application = brp.application ")
                .append("JOIN buildrevisioninvariant bri on bri.versionname = brp.revision and bri.`system` = ? and bri.`level` = 2 ")
                .append(" WHERE 1=1 ")
                .append(" and a.`system` = ? ")
                .append(" and brp.build = ? ");
        if (lastBuild.equalsIgnoreCase(build)) { // If last version is on the same build.
            if (StringUtil.isEmptyOrNull(lastRevision)) { // Same build and revision some we filter only the current content.
                query.append(" and bri.seq = (select seq from buildrevisioninvariant where `system` = ? and `level` = 2 and `versionname` = ? ) "); // revision
            } else { // 2 different revisions inside the same build, we take the content between the 2.
                query.append(" and bri.seq > (select seq from buildrevisioninvariant where `system` = ? and `level` = 2 and `versionname` = ? ) "); // lastRevision
            }
        }
        query.append(" and bri.seq <= (select seq from buildrevisioninvariant where `system` = ? and `level` = 2 and `versionname` = ? )") // revision
                .append(" and `release` REGEXP '^-?[0-9]+$' ") // Release needs to be a svn number
                .append(" and jenkinsbuildid is not null and jenkinsbuildid != '' ") // We need to have a jenkinsBuildID
                .append("   ORDER BY brp.Application ) as al1 ")
                .append("   GROUP BY Application  ORDER BY Application) as al ")
                .append("JOIN buildrevisionparameters brp ")
                .append("  ON brp.application=al.application and brp.release=al.rel and brp.build = ? ")
                .append(" JOIN application app ")
                .append(" ON app.application=al.application ")
                .append("WHERE app.`system` = ? ;");

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.system : {}", system);
        LOG.debug("SQL.param.build : {}", build);
        LOG.debug("SQL.param.revision : {}", revision);
        LOG.debug("SQL.param.lastBuild : {}", lastBuild);
        LOG.debug("SQL.param.lastRevision {}: ", lastRevision);

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString());
             Statement stm = connection.createStatement()) {

            int i = 1;
            preStat.setString(i++, system);
            preStat.setString(i++, system);
            preStat.setString(i++, build);
            if (lastBuild.equalsIgnoreCase(build)) {
                if (StringUtil.isEmptyOrNull(lastRevision)) { // if lastRevision is not defined, we filter only the current content.
                    preStat.setString(i++, system);
                    preStat.setString(i++, revision);
                } else { // 2 different revisions inside the same build, we take the content between the 2.
                    preStat.setString(i++, system);
                    preStat.setString(i++, lastRevision);
                }
            }
            preStat.setString(i++, system);
            preStat.setString(i++, revision);
            preStat.setString(i++, build);
            preStat.setString(i, system);

            try (ResultSet resultSet = preStat.executeQuery();
                 ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {
                while (resultSet.next()) {
                    BuildRevisionParameters brpItem = this.loadFromResultSet(resultSet);
                    brpItem.setAppDeployType(resultSet.getString("app.deploytype"));
                    brpList.add(brpItem);
                }

                int nrTotalRows = 0;
                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }

                if (brpList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                } else if (brpList.isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                }
                response = new AnswerList<>(brpList, nrTotalRows);
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }

        response.setResultMessage(msg);
        response.setDataList(brpList);
        return response;
    }

    @Override
    public AnswerList<BuildRevisionParameters> readNonSVNRelease(String system, String build, String revision, String lastBuild, String lastRevision) {
        AnswerList<BuildRevisionParameters> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<BuildRevisionParameters> brpList = new ArrayList<>();

        StringBuilder query = new StringBuilder()
                .append("SELECT * from ( ")
                .append("SELECT distinct brp.Application, `Release` rel, link, max(id) maxid ")
                .append(" FROM buildrevisionparameters brp ")
                .append("JOIN application a on a.application = brp.application ")
                .append("JOIN buildrevisioninvariant bri ON bri.versionname = brp.revision and bri.`system` = ? and bri.`level` = 2 ")
                .append(" WHERE 1=1 ")
                .append(" and a.`system` = ? ")
                .append(" and brp.build = ? ");
        if (lastBuild.equalsIgnoreCase(build)) {
            if (StringUtil.isEmptyOrNull(lastRevision)) { // if lastRevision is not defined, we filter only the current content.
                query.append(" and bri.seq = (select seq from buildrevisioninvariant where `system` = ? and `level` = 2 and `versionname` = ? ) "); // revision
            } else { // 2 different revisions inside the same build, we take the content between the 2.
                query.append(" and bri.seq > (select seq from buildrevisioninvariant where `system` = ? and `level` = 2 and `versionname` = ? ) "); // lastRevision
            }
        }
        query.append(" and bri.seq <= (select seq from buildrevisioninvariant where `system` = ? and `level` = 2 and `versionname` = ? )") // revision
                .append("  and link is not null and length(trim(link))>0 ") // Release Link for instal instructions needs to exist.
                .append("   GROUP BY brp.Application, `Release`, link  ORDER BY Application, `Release`, link ")
                .append(") as toto ")
                .append(" JOIN buildrevisionparameters brp   ON brp.id = toto.maxid ")
                .append(" JOIN buildrevisioninvariant bri1 on bri1.versionname = brp.build  and bri1.`system` = ?  and bri1.`level` = 1 ")
                .append(" JOIN buildrevisioninvariant bri2 on bri2.versionname = brp.revision  and bri2.`system` = ?  and bri2.`level` = 2 ")
                .append(" JOIN application app ")
                .append(" ON app.application=toto.application ")
                .append(" ORDER BY bri1.seq asc , bri2.seq asc , brp.Application asc;");

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.system : {}", system);
        LOG.debug("SQL.param.build : {}", build);
        LOG.debug("SQL.param.revision : {}", revision);
        LOG.debug("SQL.param.lastBuild : {}", lastBuild);
        LOG.debug("SQL.param.lastRevision : {}", lastRevision);

        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString());
             Statement stm = connection.createStatement()) {

            int i = 1;
            preStat.setString(i++, system);
            preStat.setString(i++, system);
            preStat.setString(i++, build);
            if (lastBuild.equalsIgnoreCase(build)) {
                if (StringUtil.isEmptyOrNull(lastRevision)) { // if lastRevision is not defined, we filter only the current content.
                    preStat.setString(i++, system);
                    preStat.setString(i++, revision);
                } else { // 2 different revisions inside the same build, we take the content between the 2.
                    preStat.setString(i++, system);
                    preStat.setString(i++, lastRevision);
                }
            }
            preStat.setString(i++, system);
            preStat.setString(i++, revision);
            preStat.setString(i++, system);
            preStat.setString(i, system);

            try (ResultSet resultSet = preStat.executeQuery();
                 ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {
                while (resultSet.next()) {
                    BuildRevisionParameters newBRP;
                    newBRP = factoryBuildRevisionParameters.create(ParameterParserUtil.parseIntegerParam(resultSet.getString("maxid"), 0)
                            , ParameterParserUtil.parseStringParam(resultSet.getString("build"), ""), ParameterParserUtil.parseStringParam(resultSet.getString("revision"), "")
                            , ParameterParserUtil.parseStringParam(resultSet.getString("rel"), ""), ParameterParserUtil.parseStringParam(resultSet.getString("application"), "")
                            , "", "", "", ParameterParserUtil.parseStringParam(resultSet.getString("link"), ""), "", "", null, null, null, null, null, null);
                    newBRP.setAppDeployType(ParameterParserUtil.parseStringParam(resultSet.getString("app.deploytype"), ""));
                    brpList.add(newBRP);
                }

                int nrTotalRows = 0;
                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }

                if (brpList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                } else if (brpList.isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                }
                response = new AnswerList<>(brpList, nrTotalRows);
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }

        response.setResultMessage(msg);
        response.setDataList(brpList);
        return response;
    }

    @Override
    public Answer create(BuildRevisionParameters brp) {
        MessageEvent msg;
        StringBuilder query = new StringBuilder()
                .append("INSERT INTO buildrevisionparameters (`Build`,`Revision`,`Release`,`Link` , `Application`, `releaseOwner`, `Project`")
                .append(" , `BugIDFixed`, `TicketIDFixed` , `Subject`, `jenkinsbuildid`, `mavengroupid`, `mavenartifactid`, `mavenversion`, `repositoryurl`) ")
                .append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        LOG.debug("SQL : {}", query);
        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString())) {
            int i = 1;
            preStat.setString(i++, brp.getBuild());
            preStat.setString(i++, brp.getRevision());
            preStat.setString(i++, brp.getRelease());
            preStat.setString(i++, brp.getLink());
            preStat.setString(i++, brp.getApplication());
            preStat.setString(i++, brp.getReleaseOwner());
            preStat.setString(i++, brp.getProject());
            preStat.setString(i++, brp.getBugIdFixed());
            preStat.setString(i++, brp.getTicketIdFixed());
            preStat.setString(i++, brp.getSubject());
            preStat.setString(i++, brp.getJenkinsBuildId());
            preStat.setString(i++, brp.getMavenGroupId());
            preStat.setString(i++, brp.getMavenArtifactId());
            preStat.setString(i++, brp.getMavenVersion());
            preStat.setString(i, brp.getRepositoryUrl());
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
    public Answer delete(BuildRevisionParameters brp) {
        MessageEvent msg;
        final String query = "DELETE FROM buildrevisionparameters WHERE id = ?";

        LOG.debug("SQL : {}", query);
        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query)) {

            preStat.setInt(1, brp.getId());
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
    public Answer update(BuildRevisionParameters brp) {
        MessageEvent msg;
        final String query = "UPDATE buildrevisionparameters SET `build` = ?, revision = ?, application = ?,"
                + "`release` = ?, project = ?, ticketidfixed = ?, bugidfixed = ?, `subject` = ?, releaseowner = ?,"
                + " link = ?, jenkinsbuildid = ?, mavengroupid = ?, mavenartifactid = ?, mavenversion = ?, repositoryurl = ? "
                + "  WHERE id = ?";

        LOG.debug("SQL : {}", query);
        try (Connection connection = this.databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query)) {
            int i = 1;
            preStat.setString(i++, brp.getBuild());
            preStat.setString(i++, brp.getRevision());
            preStat.setString(i++, brp.getApplication());
            preStat.setString(i++, brp.getRelease());
            preStat.setString(i++, brp.getProject());
            preStat.setString(i++, brp.getTicketIdFixed());
            preStat.setString(i++, brp.getBugIdFixed());
            preStat.setString(i++, brp.getSubject());
            preStat.setString(i++, brp.getReleaseOwner());
            preStat.setString(i++, brp.getLink());
            preStat.setString(i++, brp.getJenkinsBuildId());
            preStat.setString(i++, brp.getMavenGroupId());
            preStat.setString(i++, brp.getMavenArtifactId());
            preStat.setString(i++, brp.getMavenVersion());
            preStat.setString(i++, brp.getRepositoryUrl());
            preStat.setInt(i, brp.getId());
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

        StringBuilder query = new StringBuilder()
                .append("SELECT distinct ")
                .append(columnName)
                .append(" as distinctValues FROM buildrevisionparameters ");

        searchSQL.append("WHERE 1=1");
        if (StringUtil.isNotEmptyOrNull(system)) {
            searchSQL.append(" and application in (SELECT application FROM application WHERE `System` = ? )");
        }

        if (StringUtil.isNotEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (`id` like ?");
            searchSQL.append(" or `Build` like ?");
            searchSQL.append(" or `Revision` like ?");
            searchSQL.append(" or `Release` like ?");
            searchSQL.append(" or `Application` like ?");
            searchSQL.append(" or `Project` like ?");
            searchSQL.append(" or `TicketIDFixed` like ?");
            searchSQL.append(" or `BugIDFixed` like ?");
            searchSQL.append(" or `Link` like ?");
            searchSQL.append(" or `ReleaseOwner` like ?");
            searchSQL.append(" or `datecre` like ?");
            searchSQL.append(" or `jenkinsbuildid` like ?");
            searchSQL.append(" or `mavengroupid` like ?");
            searchSQL.append(" or `mavenartifactid` like ?");
            searchSQL.append(" or `repositoryurl` like ?");
            searchSQL.append(" or `mavenversion` like ? )");
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
            LOG.warn("Unable to execute query : {}", e.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        }

        answer.setResultMessage(msg);
        answer.setDataList(distinctValues);
        return answer;
    }

    @Override
    public BuildRevisionParameters loadFromResultSet(ResultSet rs) throws SQLException {
        int iD = rs.getInt("ID");
        String build = ParameterParserUtil.parseStringParam(rs.getString("build"), "");
        String revision = ParameterParserUtil.parseStringParam(rs.getString("revision"), "");
        String release = ParameterParserUtil.parseStringParam(rs.getString("release"), "");
        String application = ParameterParserUtil.parseStringParam(rs.getString("application"), "");
        String project = ParameterParserUtil.parseStringParam(rs.getString("project"), "");
        String ticketIdFixed = ParameterParserUtil.parseStringParam(rs.getString("ticketidfixed"), "");
        String budIdFixed = ParameterParserUtil.parseStringParam(rs.getString("bugidfixed"), "");
        String link = ParameterParserUtil.parseStringParam(rs.getString("link"), "");
        String releaseOwner = ParameterParserUtil.parseStringParam(rs.getString("releaseowner"), "");
        String subject = ParameterParserUtil.parseStringParam(rs.getString("subject"), "");
        Timestamp dateCreation = rs.getTimestamp("datecre");
        String jenkinsBuildId = ParameterParserUtil.parseStringParam(rs.getString("jenkinsbuildid"), "");
        String mavenGroupId = ParameterParserUtil.parseStringParam(rs.getString("mavengroupid"), "");
        String mavenArtifactId = ParameterParserUtil.parseStringParam(rs.getString("mavenartifactid"), "");
        String mavenVersion = ParameterParserUtil.parseStringParam(rs.getString("mavenversion"), "");
        String repositoryUrl = ParameterParserUtil.parseStringParam(rs.getString("repositoryurl"), "");
        return factoryBuildRevisionParameters.create(iD, build, revision, release, application, project, ticketIdFixed, budIdFixed, link,
                releaseOwner, subject, dateCreation, jenkinsBuildId, mavenGroupId, mavenArtifactId, mavenVersion, repositoryUrl);
    }
}
