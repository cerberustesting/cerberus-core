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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.ILabelDAO;
import org.cerberus.core.crud.entity.Label;
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
import org.cerberus.core.util.security.UserSecurity;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Implements methods defined on ILabelDAO
 */
@Repository
public class LabelDAO implements ILabelDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryLabel factoryLabel;

    private static final Logger LOG = LogManager.getLogger(LabelDAO.class);

    private final String OBJECT_NAME = "Label";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public AnswerItem<Label> readByKey(Integer id) {
        AnswerItem<Label> ans = new AnswerItem<>();
        Label result = null;
        final String query = "SELECT * FROM `label` lab WHERE `id` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.label : " + id);
        }
        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            //prepare and execute query
            preStat.setInt(1, id);
            try (ResultSet resultSet = preStat.executeQuery();) {
                //parse query
                if (resultSet.first()) {
                    result = loadFromResultSet(resultSet);
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    ans.setItem(result);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

            }
        } catch (Exception e) {
            LOG.warn("Unable to readByKey Label: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public AnswerList<Label> readBySystemByCriteria(List<String> system, boolean strictSystemFilter, List<String> type, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        AnswerList<Label> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<Label> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS lab.*,  count(tcl.id) cnt FROM `label` lab ");
        query.append("left outer join testcaselabel tcl ON lab.id = tcl.labelid ");

        searchSQL.append(" where 1=1 ");

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (lab.`id` like ?");
            searchSQL.append(" or lab.`system` like ?");
            searchSQL.append(" or lab.`label` like ?");
            searchSQL.append(" or lab.`type` like ?");
            searchSQL.append(" or lab.`color` like ?");
            searchSQL.append(" or lab.`parentLabelid` like ?");
            searchSQL.append(" or lab.`RequirementType` like ?");
            searchSQL.append(" or lab.`RequirementStatus` like ?");
            searchSQL.append(" or lab.`RequirementCriticity` like ?");
            searchSQL.append(" or lab.`description` like ?");
            searchSQL.append(" or lab.`LongDescription` like ?");
            searchSQL.append(" or lab.`usrCreated` like ?");
            searchSQL.append(" or lab.`dateCreated` like ?");
            searchSQL.append(" or lab.`usrModif` like ?");
            searchSQL.append(" or lab.`dateModif` like ?)");
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

        if ((system != null) && (!system.isEmpty())) {
            if (!strictSystemFilter) {
                system.add("");
            }
            searchSQL.append(" and (").append(SqlUtil.generateInClause("lab.`System`", system)).append(")");
        }

        query.append(" AND ").append(UserSecurity.getSystemAllowForSQL("lab.`System`")).append(" ");

        if ((type != null) && (!type.isEmpty())) {
            searchSQL.append(" and (").append(SqlUtil.generateInClause("lab.`Type`", type)).append(")");
        }
        query.append(searchSQL);

        query.append(" group by lab.id ");

        if (!StringUtil.isEmptyOrNull(column)) {
            query.append(" order by `").append(column).append("` ").append(dir);
        }

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param.system : " + system);
            LOG.debug("SQL.param.type : " + type);
        }
        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString());
             Statement stm = connection.createStatement();) {

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
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
            }
            for (String individualColumnSearchValue : individalColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }
            if ((system != null) && (!system.isEmpty())) {
                for (String mysystem : system) {
                    preStat.setString(i++, mysystem);
                }
            }
            if ((type != null) && (!type.isEmpty())) {
                for (String mytype : type) {
                    preStat.setString(i++, mytype);
                }
            }

            try (ResultSet resultSet = preStat.executeQuery();
                 ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()");) {

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
                    response = new AnswerList<>(objectList, nrTotalRows);
                } else if (objectList.size() <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    response = new AnswerList<>(objectList, nrTotalRows);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    response = new AnswerList<>(objectList, nrTotalRows);
                }
                response.setDataList(objectList);

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

            }
        } catch (Exception e) {
            LOG.warn("Unable to readBySystemCriteria Label: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            response.setResultMessage(msg);
        }
        return response;
    }

    @Override
    public AnswerList<Label> readAllLinks() {
        AnswerList<Label> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<Label> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS id, parentlabelid from label lab where lab.ParentLabelID <> 0 ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString());
             Statement stm = connection.createStatement();) {

            try (ResultSet resultSet = preStat.executeQuery();
                 ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()");) {

                //gets the data
                while (resultSet.next()) {
                    objectList.add(this.loadLinkFromResultSet(resultSet));
                }

                int nrTotalRows = 0;

                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
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
                response.setDataList(objectList);

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

            }
        } catch (Exception e) {
            LOG.warn("Unable to readBySystemCriteria Label: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            response.setResultMessage(msg);
        }
        return response;
    }

    @Override
    public Answer create(Label label) {
        Answer response = new Answer();
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO label (`system`, `label`, `type`, `color`, `parentLabelid`, `RequirementType`, `RequirementStatus`, `RequirementCriticity`, `description`, `LongDescription`, `usrCreated`, `dateCreated`, `usrModif`, `dateModif` ) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("Label : " + label.toString());
        }
        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString())) {

            int i = 1;
            preStat.setString(i++, label.getSystem());
            preStat.setString(i++, label.getLabel());
            preStat.setString(i++, label.getType());
            preStat.setString(i++, label.getColor());
            preStat.setInt(i++, label.getParentLabelID());
            preStat.setString(i++, label.getRequirementType());
            preStat.setString(i++, label.getRequirementStatus());
            preStat.setString(i++, label.getRequirementCriticity());
            preStat.setString(i++, label.getDescription());
            preStat.setString(i++, label.getLongDescription());
            preStat.setString(i++, label.getUsrCreated());
            preStat.setTimestamp(i++, label.getDateCreated());
            preStat.setString(i++, label.getUsrModif());
            preStat.setTimestamp(i++, label.getDateModif());

            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));

        } catch (Exception e) {
            LOG.warn("Unable to create label: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            response.setResultMessage(msg);
        }

        return response;
    }

    @Override
    public Answer delete(Label object) {
        Answer response = new Answer();
        MessageEvent msg = null;
        final String query = "DELETE FROM label WHERE id = ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query)) {
            preStat.setInt(1, object.getId());

            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "DELETE"));
        } catch (Exception e) {
            LOG.warn("Unable to delete label: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            response.setResultMessage(msg);
        }

        return response;
    }

    @Override
    public Answer update(Label object) {
        Answer response = new Answer();
        MessageEvent msg = null;
        final String query = "UPDATE label SET `system` = ?, `label` = ?, `type` = ?, `color` = ?, `parentLabelid` = ?, `usrModif` = ?, `dateModif` = ?, `description` = ?"
                + ", `LongDescription` = ?, `RequirementType` = ?, `RequirementStatus` = ?, `RequirementCriticity` = ?  WHERE id = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query)) {
            int i = 1;
            preStat.setString(i++, object.getSystem());
            preStat.setString(i++, object.getLabel());
            preStat.setString(i++, object.getType());
            preStat.setString(i++, object.getColor());
            preStat.setInt(i++, object.getParentLabelID());
            preStat.setString(i++, object.getUsrModif());
            preStat.setTimestamp(i++, object.getDateModif());
            preStat.setString(i++, object.getDescription());
            preStat.setString(i++, object.getLongDescription());
            preStat.setString(i++, object.getRequirementType());
            preStat.setString(i++, object.getRequirementStatus());
            preStat.setString(i++, object.getRequirementCriticity());
            preStat.setInt(i++, object.getId());

            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));
        } catch (Exception e) {
            LOG.warn("Unable to update label: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            response.setResultMessage(msg);
        }

        return response;
    }

    @Override
    public Label loadFromResultSet(ResultSet rs) throws SQLException {
        Integer id = ParameterParserUtil.parseIntegerParam(rs.getString("lab.id"), 0);
        String system = ParameterParserUtil.parseStringParam(rs.getString("lab.system"), "");
        String label = ParameterParserUtil.parseStringParam(rs.getString("lab.label"), "");
        String type = ParameterParserUtil.parseStringParam(rs.getString("lab.type"), "");
        String color = ParameterParserUtil.parseStringParam(rs.getString("lab.color"), "");
        Integer parentLabelid = ParameterParserUtil.parseIntegerParam(rs.getString("lab.parentLabelid"), 0);
        String requirementType = ParameterParserUtil.parseStringParam(rs.getString("lab.RequirementType"), "");
        String requirementStatus = ParameterParserUtil.parseStringParam(rs.getString("lab.RequirementStatus"), "");
        String requirementCriticity = ParameterParserUtil.parseStringParam(rs.getString("lab.RequirementCriticity"), "");
        String description = ParameterParserUtil.parseStringParam(rs.getString("lab.description"), "");
        String longDescription = ParameterParserUtil.parseStringParam(rs.getString("lab.longDescription"), "");
        String usrCreated = ParameterParserUtil.parseStringParam(rs.getString("lab.usrCreated"), "");
        Timestamp dateCreated = rs.getTimestamp("lab.dateCreated");
        String usrModif = ParameterParserUtil.parseStringParam(rs.getString("lab.usrModif"), "");
        Timestamp dateModif = rs.getTimestamp("lab.dateModif");
        Integer counter = 0;
        try {
            counter = ParameterParserUtil.parseIntegerParam(rs.getString("cnt"), 0);
        } catch (Exception e) {
        }
        Label labelObj = factoryLabel.create(id, system, label, type, color, parentLabelid, requirementType, requirementStatus, requirementCriticity, description, longDescription, usrCreated, dateCreated, usrModif, dateModif);
        labelObj.setCounter1(counter);
        return labelObj;
    }

    private Label loadLinkFromResultSet(ResultSet rs) throws SQLException {
        Integer id = ParameterParserUtil.parseIntegerParam(rs.getString("lab.id"), 0);
        Integer parentLabelid = ParameterParserUtil.parseIntegerParam(rs.getString("lab.parentLabelid"), 0);
        Label labelObj = factoryLabel.create(id, null, null, null, null, parentLabelid, null, null, null, null, null, null, null, null, null);
        return labelObj;
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(List<String> systems, String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        AnswerList<String> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();

        query.append("SELECT distinct ");
        query.append("`").append(columnName).append("`");
        query.append(" as distinctValues FROM label ");

        searchSQL.append("WHERE 1=1");
        if (systems != null && !systems.isEmpty()) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`System`", systems));
        }

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (`id` like ?");
            searchSQL.append(" or `system` like ?");
            searchSQL.append(" or `label` like ?");
            searchSQL.append(" or `type` like ?");
            searchSQL.append(" or `color` like ?");
            searchSQL.append(" or `parentLabelid` like ?");
            searchSQL.append(" or `RequirementType` like ?");
            searchSQL.append(" or `RequirementStatus` like ?");
            searchSQL.append(" or `RequirementCriticity` like ?");
            searchSQL.append(" or `description` like ?");
            searchSQL.append(" or `LongDescription` like ?");
            searchSQL.append(" or `usrCreated` like ?");
            searchSQL.append(" or `dateCreated` like ?");
            searchSQL.append(" or `usrModif` like ?");
            searchSQL.append(" or `dateModif` like ?)");
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
        query.append(" order by `").append(columnName).append("` asc");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        try (Connection connection = databaseSpring.connect();
             PreparedStatement preStat = connection.prepareStatement(query.toString());
             Statement stm = connection.createStatement();) {

            int i = 1;
            if (systems != null && !systems.isEmpty()) {
                for (String sys : systems) {
                    preStat.setString(i++, sys);
                }
            }

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

}
