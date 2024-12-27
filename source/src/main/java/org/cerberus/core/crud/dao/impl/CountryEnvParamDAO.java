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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.core.crud.dao.ICountryEnvParamDAO;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.crud.entity.CountryEnvParam;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.factory.IFactoryCountryEnvParam;
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

/**
 * @author bcivel
 */
@Repository
public class CountryEnvParamDAO implements ICountryEnvParamDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryCountryEnvParam factoryCountryEnvParam;

    private static final Logger LOG = LogManager.getLogger(CountryEnvParamDAO.class);

    private final String OBJECT_NAME = "CountryEnvParam";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 10000;

    @Override
    public List<CountryEnvParam> findCountryEnvParamByCriteria(CountryEnvParam countryEnvParam) throws CerberusException {
        List<CountryEnvParam> result = new ArrayList<>();
        boolean throwex = false;
        StringBuilder query = new StringBuilder();
        query.append("SELECT * ");
        query.append(" FROM countryenvparam cev ");
        query.append(" LEFT OUTER JOIN invariant inv on inv.idname='ENVIRONMENT' and inv.value=cev.environment ");
        query.append(" WHERE cev.`system` LIKE ? AND cev.`country` LIKE ? ");
        query.append("AND cev.`environment` LIKE ? AND cev.`build` LIKE ? AND cev.`Revision` LIKE ? AND cev.`chain` LIKE ? ");
        query.append("AND cev.`distriblist` LIKE ? AND cev.`eMailBodyRevision` LIKE ? AND cev.`type` LIKE ? AND cev.`eMailBodyChain` LIKE ? ");
        query.append("AND cev.`eMailBodyDisableEnvironment` LIKE ? AND cev.`active` LIKE ? AND cev.`maintenanceact` LIKE ? AND cev.`maintenancestr` LIKE ? ");
        query.append("AND cev.`maintenanceend` LIKE ? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, ParameterParserUtil.wildcardIfEmpty(countryEnvParam.getSystem()));
                preStat.setString(2, ParameterParserUtil.wildcardIfEmpty(countryEnvParam.getCountry()));
                preStat.setString(3, ParameterParserUtil.wildcardIfEmpty(countryEnvParam.getEnvironment()));
                preStat.setString(4, ParameterParserUtil.wildcardIfEmpty(countryEnvParam.getBuild()));
                preStat.setString(5, ParameterParserUtil.wildcardIfEmpty(countryEnvParam.getRevision()));
                preStat.setString(6, ParameterParserUtil.wildcardIfEmpty(countryEnvParam.getChain()));
                preStat.setString(7, ParameterParserUtil.wildcardIfEmpty(countryEnvParam.getDistribList()));
                preStat.setString(8, ParameterParserUtil.wildcardIfEmpty(countryEnvParam.geteMailBodyRevision()));
                preStat.setString(9, ParameterParserUtil.wildcardIfEmpty(countryEnvParam.getType()));
                preStat.setString(10, ParameterParserUtil.wildcardIfEmpty(countryEnvParam.geteMailBodyChain()));
                preStat.setString(11, ParameterParserUtil.wildcardIfEmpty(countryEnvParam.geteMailBodyDisableEnvironment()));
                if (countryEnvParam.isActive()) {
                    preStat.setString(12, "Y");
                } else {
                    preStat.setString(12, "%");
                }
                if (countryEnvParam.isMaintenanceAct()) {
                    preStat.setString(13, "Y");
                } else {
                    preStat.setString(13, "%");
                }
                preStat.setString(14, ParameterParserUtil.wildcardIfEmpty(countryEnvParam.getMaintenanceStr()));
                preStat.setString(15, ParameterParserUtil.wildcardIfEmpty(countryEnvParam.getMaintenanceEnd()));

                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        result.add(this.loadFromResultSet(resultSet));
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
                LOG.warn(e.toString());
            }
        }
        if (throwex) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    @Override
    public AnswerItem<CountryEnvParam> readByKey(String system, String country, String environment) {
        AnswerItem<CountryEnvParam> ans = new AnswerItem<>();
        CountryEnvParam result = null;
        final String query = "SELECT * FROM `countryenvparam` cev "
                + " LEFT OUTER JOIN invariant inv on inv.idname='ENVIRONMENT' and inv.value=cev.environment "
                + "WHERE cev.`system` = ? and cev.`country` = ? and cev.`environment` = ?";

        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.system : " + system);
            LOG.debug("SQL.param.country : " + country);
            LOG.debug("SQL.param.environment : " + environment);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            try {
                preStat.setString(1, system);
                preStat.setString(2, country);
                preStat.setString(3, environment);
                ResultSet resultSet = preStat.executeQuery();
                try {
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
                } finally {
                    resultSet.close();
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
    public AnswerList<CountryEnvParam> readActiveBySystem(String system) {
        AnswerList<CountryEnvParam> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<CountryEnvParam> countryEnvParamList = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT * FROM countryenvparam cev ");
        query.append("LEFT OUTER JOIN invariant inv on inv.idname='ENVIRONMENT' and inv.value=cev.environment ");
        query.append(" WHERE 1=1 ");

        if (system != null) {
            query.append("AND cev.system = ? ");
        }
        query.append("AND cev.active = 'Y'");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());
                Statement stm = connection.createStatement()) {
            if (system != null) {
                preStat.setString(1, system);
            }

            try (ResultSet resultSet = preStat.executeQuery();
                    ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()");) {
                //gets the data
                while (resultSet.next()) {
                    countryEnvParamList.add(this.loadFromResultSet(resultSet));
                }

                //get the total number of rows
                int nrTotalRows = 0;

                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }

                if (countryEnvParamList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                    response = new AnswerList<>(countryEnvParamList, nrTotalRows);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    response = new AnswerList<>(countryEnvParamList, nrTotalRows);
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        }
        response.setResultMessage(msg);
        response.setDataList(countryEnvParamList);
        return response;
    }

    @Override
    public AnswerList<CountryEnvParam> readByCriteria(int start, int amount, String colName, String dir, String searchTerm, String individualSearch) {
        AnswerList<CountryEnvParam> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<CountryEnvParam> cepList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM countryenvparam cev ");
        query.append(" LEFT OUTER JOIN invariant inv on inv.idname='ENVIRONMENT' and inv.value=cev.environment ");

        searchSQL.append(" where 1=1 ");

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (cev.`system` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or cev.`country` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or cev.`environment` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or cev.`description` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or cev.`build` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or cev.`revision` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or cev.`chain` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or cev.`distriblist` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or cev.`emailbodyrevision` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or cev.`type` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or cev.`emailbodychain` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or cev.`emailbodydisableenvironment` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or cev.`active` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or cev.`maintenanceact` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or cev.`maintenancestr` like '%").append(searchTerm).append("%'");
            searchSQL.append(" or cev.`maintenanceend` like '%").append(searchTerm).append("%')");
        }
        if (!StringUtil.isEmptyOrNull(individualSearch)) {
            searchSQL.append(" and (`").append(individualSearch).append("`)");
        }
        query.append(searchSQL);

        if (!StringUtil.isEmptyOrNull(colName)) {
            query.append("order by `").append(colName).append("` ").append(dir);
        }
        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        cepList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (cepList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList<>(cepList, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", "CountryEnvParam").replace("%OPERATION%", "SELECT"));
                        response = new AnswerList<>(cepList, nrTotalRows);
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
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
        response.setDataList(cepList);
        return response;
    }

    @Override
    public AnswerList<CountryEnvParam> readByVariousByCriteria(List<String> systems, String country, String environment, String build, String revision, String active, String envGp, int start, int amount, String colName, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        AnswerList<CountryEnvParam> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<CountryEnvParam> cepList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS cev.*, inv.* FROM countryenvparam cev");

        searchSQL.append(" LEFT OUTER JOIN invariant inv on inv.idname='ENVIRONMENT' and inv.value=cev.environment ");

        searchSQL.append(" where 1=1 ");

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (cev.`system` like ?");
            searchSQL.append(" or cev.`country` like ?");
            searchSQL.append(" or cev.`environment` like ?");
            searchSQL.append(" or cev.`description` like ?");
            searchSQL.append(" or cev.`build` like ?");
            searchSQL.append(" or cev.`revision` like ?");
            searchSQL.append(" or cev.`chain` like ?");
            searchSQL.append(" or cev.`distriblist` like ?");
            searchSQL.append(" or cev.`emailbodyrevision` like ?");
            searchSQL.append(" or cev.`type` like ?");
            searchSQL.append(" or cev.`emailbodychain` like ?");
            searchSQL.append(" or cev.`emailbodydisableenvironment` like ?");
            searchSQL.append(" or cev.`active` like ?");
            searchSQL.append(" or cev.`maintenanceact` like ?");
            searchSQL.append(" or cev.`maintenancestr` like ?");
            searchSQL.append(" or cev.`maintenanceend` like ?)");
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

        if ((systems != null) && (!systems.isEmpty())) {
            searchSQL.append(" and (").append(SqlUtil.generateInClause("cev.`System`", systems)).append(") ");
        }

        searchSQL.append(" AND ").append(UserSecurity.getSystemAllowForSQL("cev.`System`")).append(" ");

        if (!StringUtil.isEmptyOrNull(active)) {
            searchSQL.append(" and (cev.`active` = ? )");
        }
        if (!StringUtil.isEmptyOrNull(country)) {
            searchSQL.append(" and (cev.`country` = ? )");
        }
        if (!StringUtil.isEmptyOrNull(environment)) {
            searchSQL.append(" and (cev.`environment` = ? )");
        }
        if (!StringUtil.isEmptyOrNull(build)) {
            searchSQL.append(" and (cev.`build` = ? )");
        }
        if (!StringUtil.isEmptyOrNull(revision)) {
            searchSQL.append(" and (cev.`revision` = ? )");
        }
        if (!StringUtil.isEmptyOrNull(envGp)) {
            searchSQL.append(" and (inv.`gp1` = ? )");
        }
        query.append(searchSQL);

        if (!StringUtil.isEmptyOrNull(colName)) {
            query.append(" order by `").append(colName).append("` ").append(dir);
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
                    preStat.setString(i++, "%" + searchTerm + "%");
                }
                for (String individualColumnSearchValue : individalColumnSearchValues) {
                    preStat.setString(i++, individualColumnSearchValue);
                }
                if ((systems != null) && (!systems.isEmpty())) {
                    for (String mysystem : systems) {
                        preStat.setString(i++, mysystem);
                    }
                }
                if (!StringUtil.isEmptyOrNull(active)) {
                    preStat.setString(i++, active);
                }
                if (!StringUtil.isEmptyOrNull(country)) {
                    preStat.setString(i++, country);
                }
                if (!StringUtil.isEmptyOrNull(environment)) {
                    preStat.setString(i++, environment);
                }
                if (!StringUtil.isEmptyOrNull(build)) {
                    preStat.setString(i++, build);
                }
                if (!StringUtil.isEmptyOrNull(revision)) {
                    preStat.setString(i++, revision);
                }
                if (!StringUtil.isEmptyOrNull(envGp)) {
                    preStat.setString(i++, envGp);
                }
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        cepList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (cepList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList<>(cepList, nrTotalRows);
                    } else if (cepList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList<>(cepList, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", "CountryEnvParam").replace("%OPERATION%", "SELECT"));
                        response = new AnswerList<>(cepList, nrTotalRows);
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString(), exception);
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString(), exception);
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString(), exception);
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
        response.setDataList(cepList);
        return response;
    }

    @Override
    public AnswerList<CountryEnvParam> readDistinctEnvironmentByVariousByCriteria(List<String> systems, String country, String environment, String build, String revision, String active, String envGp, int start, int amount, String colName, String dir, String searchTerm, String individualSearch) {
        AnswerList<CountryEnvParam> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<CountryEnvParam> cepList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS ");

        query.append(" max(cev.`system`) `system`, max(cev.country) country, Environment, '' Description,  ");
        query.append(" max(cev.build) build, max(cev.revision) revision, max(cev.chain) chain, '' DistribList, ");
        query.append(" '' EMailBodyRevision, max(cev.Type) type, '' EMailBodyChain, '' EMailBodyDisableEnvironment,  ");
        query.append(" max(cev.Active) active, max(cev.maintenanceact) maintenanceact, max(cev.maintenancestr) maintenancestr, max(cev.maintenanceend) maintenanceend, ");
        query.append("inv.gp1");
        query.append(" FROM countryenvparam cev ");

        query.append(" LEFT OUTER JOIN invariant inv on inv.idname='ENVIRONMENT' and inv.value=cev.environment ");

        searchSQL.append(" where 1=1 ");

        if ((systems != null) && (!systems.isEmpty())) {
            searchSQL.append(" and (").append(SqlUtil.generateInClause("`System`", systems)).append(") ");
        }
//        if (!StringUtil.isNullOrEmpty(system)) {
//            searchSQL.append(" and (`System` = ? )");
//        }
        if (!StringUtil.isEmptyOrNull(active)) {
            searchSQL.append(" and (`active` = ? )");
        }
        if (!StringUtil.isEmptyOrNull(country)) {
            searchSQL.append(" and (`country` = ? )");
        }
        if (!StringUtil.isEmptyOrNull(environment)) {
            searchSQL.append(" and (`environment` = ? )");
        }
        if (!StringUtil.isEmptyOrNull(build)) {
            searchSQL.append(" and (`build` = ? )");
        }
        if (!StringUtil.isEmptyOrNull(revision)) {
            searchSQL.append(" and (`revision` = ? )");
        }
        if (!StringUtil.isEmptyOrNull(envGp)) {
            searchSQL.append(" and (inv.`gp1` = ? )");
        }
        query.append(searchSQL);

        query.append(" group by `environment` ");
        query.append(" order by `environment` ");

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.system : " + systems);
            LOG.debug("SQL.active : " + active);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                if ((systems != null) && (!systems.isEmpty())) {
                    for (String mysystem : systems) {
                        preStat.setString(i++, mysystem);
                    }
                }
//                if (!StringUtil.isNullOrEmpty(system)) {
//                    preStat.setString(i++, system);
//                }
                if (!StringUtil.isEmptyOrNull(active)) {
                    preStat.setString(i++, active);
                }
                if (!StringUtil.isEmptyOrNull(country)) {
                    preStat.setString(i++, country);
                }
                if (!StringUtil.isEmptyOrNull(environment)) {
                    preStat.setString(i++, environment);
                }
                if (!StringUtil.isEmptyOrNull(build)) {
                    preStat.setString(i++, build);
                }
                if (!StringUtil.isEmptyOrNull(revision)) {
                    preStat.setString(i++, revision);
                }
                if (!StringUtil.isEmptyOrNull(envGp)) {
                    preStat.setString(i++, envGp);
                }
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {

                        String system1 = resultSet.getString("System");
                        String count = resultSet.getString("Country");
                        String env = resultSet.getString("Environment");
                        String description = resultSet.getString("Description");
                        String build1 = resultSet.getString("Build");
                        String revision1 = resultSet.getString("Revision");
                        String chain = resultSet.getString("chain");
                        String distribList = resultSet.getString("distribList");
                        String eMailBodyRevision = resultSet.getString("eMailBodyRevision");
                        String type = resultSet.getString("type");
                        String eMailBodyChain = resultSet.getString("eMailBodyChain");
                        String eMailBodyDisableEnvironment = resultSet.getString("eMailBodyDisableEnvironment");
                        boolean active1 = StringUtil.parseBoolean(resultSet.getString("active"));
                        boolean maintenanceAct = StringUtil.parseBoolean(resultSet.getString("maintenanceact"));
                        String maintenanceStr = resultSet.getString("maintenancestr");
                        String maintenanceEnd = resultSet.getString("maintenanceend");
                        String envGp1 = resultSet.getString("gp1");
                        cepList.add(factoryCountryEnvParam.create(system1, count, env, description, build1, revision1, chain, distribList, eMailBodyRevision,
                                type, eMailBodyChain, eMailBodyDisableEnvironment, active1, maintenanceAct, maintenanceStr, maintenanceEnd, envGp1));

                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (cepList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList<>(cepList, nrTotalRows);
                    } else if (cepList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList<>(cepList, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", "CountryEnvParam").replace("%OPERATION%", "SELECT"));
                        response = new AnswerList<>(cepList, nrTotalRows);
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
        response.setDataList(cepList);
        return response;
    }

    @Override
    public AnswerList<CountryEnvParam> readDistinctCountryByVariousByCriteria(List<String> systems, String country, String environment, String build, String revision, String active, String envGp, int start, int amount, String colName, String dir, String searchTerm, String individualSearch) {
        AnswerList<CountryEnvParam> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<CountryEnvParam> cepList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS ");

        query.append(" max(cev.`system`) `system`, max(cev.Environment) Environment, Country, '' Description,  ");
        query.append(" max(cev.build) build, max(cev.revision) revision, max(cev.chain) chain, '' DistribList, ");
        query.append(" '' EMailBodyRevision, max(cev.Type) type, '' EMailBodyChain, '' EMailBodyDisableEnvironment,  ");
        query.append(" max(cev.Active) active, max(cev.maintenanceact) maintenanceact, max(cev.maintenancestr) maintenancestr, max(cev.maintenanceend) maintenanceend, ");
        query.append("inv.gp1");
        query.append(" FROM countryenvparam cev ");

        query.append(" LEFT OUTER JOIN invariant inv on inv.idname='COUNTRY' and inv.value=cev.country ");

        searchSQL.append(" where 1=1 ");

        if ((systems != null) && (!systems.isEmpty())) {
            searchSQL.append(" and (").append(SqlUtil.generateInClause("`System`", systems)).append(") ");
        }
//        if (!StringUtil.isNullOrEmpty(system)) {
//            searchSQL.append(" and (`System` = ? )");
//        }
        if (!StringUtil.isEmptyOrNull(active)) {
            searchSQL.append(" and (`active` = ? )");
        }
        if (!StringUtil.isEmptyOrNull(country)) {
            searchSQL.append(" and (`country` = ? )");
        }
        if (!StringUtil.isEmptyOrNull(environment)) {
            searchSQL.append(" and (`environment` = ? )");
        }
        if (!StringUtil.isEmptyOrNull(build)) {
            searchSQL.append(" and (`build` = ? )");
        }
        if (!StringUtil.isEmptyOrNull(revision)) {
            searchSQL.append(" and (`revision` = ? )");
        }
        if (!StringUtil.isEmptyOrNull(envGp)) {
            searchSQL.append(" and (inv.`gp1` = ? )");
        }
        query.append(searchSQL);

        query.append(" group by `country` ");
        query.append(" order by `country` ");

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.system : " + systems);
            LOG.debug("SQL.active : " + active);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                if ((systems != null) && (!systems.isEmpty())) {
                    for (String mysystem : systems) {
                        preStat.setString(i++, mysystem);
                    }
                }
//                if (!StringUtil.isNullOrEmpty(system)) {
//                    preStat.setString(i++, system);
//                }
                if (!StringUtil.isEmptyOrNull(active)) {
                    preStat.setString(i++, active);
                }
                if (!StringUtil.isEmptyOrNull(country)) {
                    preStat.setString(i++, country);
                }
                if (!StringUtil.isEmptyOrNull(environment)) {
                    preStat.setString(i++, environment);
                }
                if (!StringUtil.isEmptyOrNull(build)) {
                    preStat.setString(i++, build);
                }
                if (!StringUtil.isEmptyOrNull(revision)) {
                    preStat.setString(i++, revision);
                }
                if (!StringUtil.isEmptyOrNull(envGp)) {
                    preStat.setString(i++, envGp);
                }
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {

                        String system1 = resultSet.getString("System");
                        String count = resultSet.getString("Country");
                        String env = resultSet.getString("Environment");
                        String description = resultSet.getString("Description");
                        String build1 = resultSet.getString("Build");
                        String revision1 = resultSet.getString("Revision");
                        String chain = resultSet.getString("chain");
                        String distribList = resultSet.getString("distribList");
                        String eMailBodyRevision = resultSet.getString("eMailBodyRevision");
                        String type = resultSet.getString("type");
                        String eMailBodyChain = resultSet.getString("eMailBodyChain");
                        String eMailBodyDisableEnvironment = resultSet.getString("eMailBodyDisableEnvironment");
                        boolean active1 = StringUtil.parseBoolean(resultSet.getString("active"));
                        boolean maintenanceAct = StringUtil.parseBoolean(resultSet.getString("maintenanceact"));
                        String maintenanceStr = resultSet.getString("maintenancestr");
                        String maintenanceEnd = resultSet.getString("maintenanceend");
                        String envGp1 = resultSet.getString("gp1");
                        cepList.add(factoryCountryEnvParam.create(system1, count, env, description, build1, revision1, chain, distribList, eMailBodyRevision,
                                type, eMailBodyChain, eMailBodyDisableEnvironment, active1, maintenanceAct, maintenanceStr, maintenanceEnd, envGp1));

                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (cepList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList<>(cepList, nrTotalRows);
                    } else if (cepList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList<>(cepList, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", "CountryEnvParam").replace("%OPERATION%", "SELECT"));
                        response = new AnswerList<>(cepList, nrTotalRows);
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
        response.setDataList(cepList);
        return response;
    }

    @Override
    public Answer create(CountryEnvParam cep) {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO `countryenvparam`  (`system`, `country`, `environment`, `build`, `revision`,`chain`, `distriblist`, `eMailBodyRevision`, `type`,`eMailBodyChain`, ");
        query.append("`eMailBodyDisableEnvironment`,  `active`, `maintenanceact`, `maintenancestr`, `maintenanceend`, `description`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, cep.getSystem());
                preStat.setString(2, cep.getCountry());
                preStat.setString(3, cep.getEnvironment());
                preStat.setString(4, cep.getBuild());
                preStat.setString(5, cep.getRevision());
                preStat.setString(6, cep.getChain());
                preStat.setString(7, cep.getDistribList());
                preStat.setString(8, cep.geteMailBodyRevision());
                preStat.setString(9, cep.getType());
                preStat.setString(10, cep.geteMailBodyChain());
                preStat.setString(11, cep.geteMailBodyDisableEnvironment());
                if (cep.isActive()) {
                    preStat.setString(12, "Y");
                } else {
                    preStat.setString(12, "N");
                }
                if (cep.isMaintenanceAct()) {
                    preStat.setString(13, "Y");
                } else {
                    preStat.setString(13, "N");
                }
                preStat.setString(14, cep.getMaintenanceStr());
                preStat.setString(15, cep.getMaintenanceEnd());
                preStat.setString(16, cep.getDescription());

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
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }
        return new Answer(msg);
    }

    @Override
    public Answer delete(CountryEnvParam cep) {
        MessageEvent msg = null;
        final String query = "DELETE FROM `countryenvparam` WHERE `system`=? and `country`=? and `environment`=?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, cep.getSystem());
                preStat.setString(2, cep.getCountry());
                preStat.setString(3, cep.getEnvironment());

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
    public Answer update(CountryEnvParam cep) {
        MessageEvent msg = null;

        final StringBuilder query = new StringBuilder("UPDATE `countryenvparam` SET `build`=?, ");
        query.append("`revision`=?,`chain`=?, `distriblist`=?, `eMailBodyRevision`=?, `type`=?,`eMailBodyChain`=?,");
        query.append("`eMailBodyDisableEnvironment`=?,  `active`=?, `maintenanceact`=?, `maintenancestr`=?, `maintenanceend`=?, `description`=? ");
        query.append(" where `system`=? and `country`=? and `environment`=? ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param.maintenancestr : " + cep.getMaintenanceStr());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, ParameterParserUtil.parseStringParam(cep.getBuild(), ""));
                preStat.setString(2, cep.getRevision());
                preStat.setString(3, cep.getChain());
                preStat.setString(4, cep.getDistribList());
                preStat.setString(5, cep.geteMailBodyRevision());
                preStat.setString(6, cep.getType());
                preStat.setString(7, cep.geteMailBodyChain());
                preStat.setString(8, cep.geteMailBodyDisableEnvironment());
                if (cep.isActive()) {
                    preStat.setString(9, "Y");
                } else {
                    preStat.setString(9, "N");
                }
                if (cep.isMaintenanceAct()) {
                    preStat.setString(10, "Y");
                } else {
                    preStat.setString(10, "N");
                }
                preStat.setString(11, cep.getMaintenanceStr());
                preStat.setString(12, cep.getMaintenanceEnd());
                preStat.setString(13, cep.getDescription());
                preStat.setString(14, cep.getSystem());
                preStat.setString(15, cep.getCountry());
                preStat.setString(16, cep.getEnvironment());

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
    public AnswerList<String> readDistinctValuesByCriteria(List<String> system, String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        AnswerList<String> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();

        if (columnName.equals("envGp")) {
            columnName = "inv.gp1";
        }
        query.append("SELECT distinct `");
        query.append(columnName);
        query.append("` as distinctValues FROM countryenvparam cev ");
        query.append(" LEFT OUTER JOIN invariant inv on inv.idname='ENVIRONMENT' and inv.value=cev.environment ");

        searchSQL.append(" where 1=1 ");

        if (system != null && !system.isEmpty()) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`System`", system));
        }

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (`system` like ?");
            searchSQL.append(" or `country` like ?");
            searchSQL.append(" or `environment` like ?");
            searchSQL.append(" or `description` like ?");
            searchSQL.append(" or `build` like ?");
            searchSQL.append(" or `revision` like ?");
            searchSQL.append(" or `chain` like ?");
            searchSQL.append(" or `distriblist` like ?");
            searchSQL.append(" or `emailbodyrevision` like ?");
            searchSQL.append(" or `type` like ?");
            searchSQL.append(" or `emailbodychain` like ?");
            searchSQL.append(" or `emailbodydisableenvironment` like ?");
            searchSQL.append(" or `active` like ?");
            searchSQL.append(" or `maintenanceact` like ?");
            searchSQL.append(" or `maintenancestr` like ?");
            searchSQL.append(" or `maintenanceend` like ?)");
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
            if (system != null && !system.isEmpty()) {
                for (String sys : system) {
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

    private CountryEnvParam loadFromResultSet(ResultSet resultSet) throws SQLException {
        String system = resultSet.getString("cev.System");
        String count = resultSet.getString("cev.Country");
        String env = resultSet.getString("cev.Environment");
        String description = resultSet.getString("cev.Description");
        String build = resultSet.getString("cev.Build");
        String revision = resultSet.getString("cev.Revision");
        String chain = resultSet.getString("cev.chain");
        String distribList = resultSet.getString("cev.distribList");
        String eMailBodyRevision = resultSet.getString("cev.eMailBodyRevision");
        String type = resultSet.getString("cev.type");
        String eMailBodyChain = resultSet.getString("cev.eMailBodyChain");
        String eMailBodyDisableEnvironment = resultSet.getString("cev.eMailBodyDisableEnvironment");
        boolean active = StringUtil.parseBoolean(resultSet.getString("cev.active"));
        boolean maintenanceAct = StringUtil.parseBoolean(resultSet.getString("cev.maintenanceact"));
        String maintenanceStr = resultSet.getString("cev.maintenancestr");
        String maintenanceEnd = resultSet.getString("cev.maintenanceend");
        String envGp = resultSet.getString("inv.gp1");
        return factoryCountryEnvParam.create(system, count, env, description, build, revision, chain, distribList, eMailBodyRevision,
                type, eMailBodyChain, eMailBodyDisableEnvironment, active, maintenanceAct, maintenanceStr, maintenanceEnd, envGp);
    }

}
