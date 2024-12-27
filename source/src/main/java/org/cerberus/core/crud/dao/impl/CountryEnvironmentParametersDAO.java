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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.ICountryEnvironmentParametersDAO;
import org.cerberus.core.crud.entity.CountryEnvironmentParameters;
import org.cerberus.core.crud.factory.IFactoryCountryEnvironmentParameters;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 31/12/2012
 * @since 2.0.0
 */
@Repository
public class CountryEnvironmentParametersDAO implements ICountryEnvironmentParametersDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryCountryEnvironmentParameters factoryCountryEnvironmentParameters;

    @Autowired
    private IParameterService parameterService;

    private final String OBJECT_NAME = "CountryEnvironmentParameters";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    private static final Logger LOG = LogManager.getLogger(CountryEnvironmentParametersDAO.class);

    @Override
    public AnswerItem<CountryEnvironmentParameters> readByKey(String system, String country, String environment, String application) {
        AnswerItem<CountryEnvironmentParameters> ans = new AnswerItem<>();
        CountryEnvironmentParameters result = null;
        final String query = "SELECT * FROM countryenvironmentparameters cea WHERE cea.`system` = ? AND cea.country = ? AND cea.environment = ? AND cea.application = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.system : " + system);
            LOG.debug("SQL.param.country : " + country);
            LOG.debug("SQL.param.environment : " + environment);
            LOG.debug("SQL.param.application : " + application);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            try {
                preStat.setString(1, system);
                preStat.setString(2, country);
                preStat.setString(3, environment);
                preStat.setString(4, application);
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
    public List<CountryEnvironmentParameters> readByKeyByApplication(String application) throws CerberusException {
        List<CountryEnvironmentParameters> result = new ArrayList<>();
        boolean throwex = false;
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM countryenvironmentparameters cea ");
        query.append(" WHERE cea.Application = ? ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.application : " + application);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, application);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        result.add(loadFromResultSet(resultSet));
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
    public List<String[]> getEnvironmentAvailable(String country, String application) {
        List<String[]> list = null;
        final String query = "SELECT cev.Environment Environment, cev.Build Build, cev.Revision Revision "
                + "FROM countryenvironmentparameters cea, countryenvparam cev, invariant inv "
                + "WHERE cev.system = cea.system AND cev.country = cea.country AND cev.environment = cea.environment "
                + "AND cea.Application = ? AND cea.country= ? "
                + "AND cev.active='Y' AND inv.idname = 'ENVIRONMENT' AND inv.Value = cev.Environment "
                + "ORDER BY inv.sort";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.application : " + application);
            LOG.debug("SQL.param.country : " + country);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, application);
                preStat.setString(2, country);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<>();

                    while (resultSet.next()) {
                        String[] array = new String[3];
                        array[0] = resultSet.getString("Environment");
                        array[1] = resultSet.getString("Build");
                        array[2] = resultSet.getString("Revision");

                        list.add(array);
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
        return list;
    }

    @Override
    public List<CountryEnvironmentParameters> findCountryEnvironmentParametersByCriteria(CountryEnvironmentParameters countryEnvironmentParameter) throws CerberusException {
        List<CountryEnvironmentParameters> result = new ArrayList<>();
        boolean throwex = false;
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM countryenvironmentparameters cea ");
        query.append(" WHERE cea.`system` LIKE ? AND cea.country LIKE ? AND cea.environment LIKE ? AND cea.Application LIKE ? ");
        query.append("AND cea.IP LIKE ? AND cea.URL LIKE ? AND cea.URLLOGIN LIKE ? AND cea.domain like ? ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.system : " + ParameterParserUtil.wildcardIfEmpty(countryEnvironmentParameter.getSystem()));
            LOG.debug("SQL.param.country : " + ParameterParserUtil.wildcardIfEmpty(countryEnvironmentParameter.getCountry()));
            LOG.debug("SQL.param.environment : " + ParameterParserUtil.wildcardIfEmpty(countryEnvironmentParameter.getEnvironment()));
            LOG.debug("SQL.param.application : " + ParameterParserUtil.wildcardIfEmpty(countryEnvironmentParameter.getApplication()));
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, ParameterParserUtil.wildcardIfEmpty(countryEnvironmentParameter.getSystem()));
                preStat.setString(2, ParameterParserUtil.wildcardIfEmpty(countryEnvironmentParameter.getCountry()));
                preStat.setString(3, ParameterParserUtil.wildcardIfEmpty(countryEnvironmentParameter.getEnvironment()));
                preStat.setString(4, ParameterParserUtil.wildcardIfEmpty(countryEnvironmentParameter.getApplication()));
                preStat.setString(5, ParameterParserUtil.wildcardIfEmpty(countryEnvironmentParameter.getIp()));
                preStat.setString(6, ParameterParserUtil.wildcardIfEmpty(countryEnvironmentParameter.getUrl()));
                preStat.setString(7, ParameterParserUtil.wildcardIfEmpty(countryEnvironmentParameter.getUrlLogin()));
                preStat.setString(8, ParameterParserUtil.wildcardIfEmpty(countryEnvironmentParameter.getDomain()));

                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        result.add(loadFromResultSet(resultSet));
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
    public AnswerList<CountryEnvironmentParameters> readByVariousByCriteria(String system, String country, String environment, String application, int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        AnswerList<CountryEnvironmentParameters> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<CountryEnvironmentParameters> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS cea.* FROM countryenvironmentparameters cea");
        query.append(" JOIN application app on app.Application = cea.Application and app.`System` = cea.`System` ");

        searchSQL.append(" where 1=1 ");

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (cea.`system` like ?");
            searchSQL.append(" or cea.`country` like ?");
            searchSQL.append(" or cea.`environment` like ?");
            searchSQL.append(" or cea.`application` like ?");
            searchSQL.append(" or cea.`ip` like ?");
            searchSQL.append(" or cea.`domain` like ?");
            searchSQL.append(" or cea.`URL` like ?");
            searchSQL.append(" or cea.`URLLOGIN` like ?)");
        }
        if (!StringUtil.isEmptyOrNull(individualSearch)) {
            searchSQL.append(" and (`?`)");
        }
        if (!StringUtil.isEmptyOrNull(system)) {
            searchSQL.append(" and (cea.`System` = ? )");
        }
        if (!StringUtil.isEmptyOrNull(country)) {
            searchSQL.append(" and (cea.`country` = ? )");
        }
        if (!StringUtil.isEmptyOrNull(environment)) {
            searchSQL.append(" and (cea.`environment` = ? )");
        }
        if (!StringUtil.isEmptyOrNull(application)) {
            searchSQL.append(" and (cea.`application` = ? )");
        }
        query.append(searchSQL);

        if (!StringUtil.isEmptyOrNull(column)) {
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
            LOG.debug("SQL.param.system : " + system);
            LOG.debug("SQL.param.country : " + country);
            LOG.debug("SQL.param.environment : " + environment);
            LOG.debug("SQL.param.application : " + application);
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
                }
                if (!StringUtil.isEmptyOrNull(individualSearch)) {
                    preStat.setString(i++, individualSearch);
                }
                if (!StringUtil.isEmptyOrNull(system)) {
                    preStat.setString(i++, system);
                }
                if (!StringUtil.isEmptyOrNull(country)) {
                    preStat.setString(i++, country);
                }
                if (!StringUtil.isEmptyOrNull(environment)) {
                    preStat.setString(i++, environment);
                }
                if (!StringUtil.isEmptyOrNull(application)) {
                    preStat.setString(i++, application);
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
    public AnswerList<CountryEnvironmentParameters> readDependenciesByVarious(String system, String country, String environment) {
        AnswerList<CountryEnvironmentParameters> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<CountryEnvironmentParameters> objectList = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS cea.* FROM countryenvironmentparameters cea");
        query.append(" JOIN application app on app.Application = cea.Application and app.`System` = cea.`System` ");
        query.append(" JOIN (SELECT systemLink , CountryLink , EnvironmentLink  from countryenvlink where `system` = ? and Country = ? and Environment = ? ) as lnk ");
        query.append("   where cea.`system` = lnk.systemLink and cea.`Country` = lnk.CountryLink and cea.`Environment` = lnk.EnvironmentLink ;  ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param.system : " + system);
            LOG.debug("SQL.param.country : " + country);
            LOG.debug("SQL.param.environment : " + environment);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                if (!StringUtil.isEmptyOrNull(system)) {
                    preStat.setString(i++, system);
                }
                if (!StringUtil.isEmptyOrNull(country)) {
                    preStat.setString(i++, country);
                }
                if (!StringUtil.isEmptyOrNull(environment)) {
                    preStat.setString(i++, environment);
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
    public Answer create(CountryEnvironmentParameters object) {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO `countryenvironmentparameters` (`system`, `country`, `environment`, `application`, `isActive`, `ip`, `domain`, `url`, `urllogin`, `Var1`, `Var2`, `Var3`, `Var4`, `Secret1`, `Secret2`, `poolSize`, `mobileActivity`, `mobilePackage`, `UsrCreated`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param.usrCreated : " + object.getUsrCreated());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i=1;
                preStat.setString(i++, object.getSystem());
                preStat.setString(i++, object.getCountry());
                preStat.setString(i++, object.getEnvironment());
                preStat.setString(i++, object.getApplication());
                preStat.setBoolean(i++, object.isActive());
                preStat.setString(i++, object.getIp());
                preStat.setString(i++, object.getDomain());
                preStat.setString(i++, object.getUrl());
                preStat.setString(i++, object.getUrlLogin());
                preStat.setString(i++, object.getVar1());
                preStat.setString(i++, object.getVar2());
                preStat.setString(i++, object.getVar3());
                preStat.setString(i++, object.getVar4());
                preStat.setString(i++, object.getSecret1());
                preStat.setString(i++, object.getSecret2());
                preStat.setInt(i++, object.getPoolSize());
                preStat.setString(i++, object.getMobileActivity());
                preStat.setString(i++, object.getMobilePackage());
                preStat.setString(i++, object.getUsrCreated());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString(), exception);

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
    public Answer delete(CountryEnvironmentParameters object) {
        MessageEvent msg = null;
        final String query = "DELETE FROM `countryenvironmentparameters` WHERE `system`=? and `country`=? and `environment`=? and `application`=? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, object.getSystem());
                preStat.setString(2, object.getCountry());
                preStat.setString(3, object.getEnvironment());
                preStat.setString(4, object.getApplication());

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
    public Answer update(CountryEnvironmentParameters object) {
        MessageEvent msg = null;
        final String query = "UPDATE `countryenvironmentparameters` SET `IsActive`=?, `IP`=?, `URL`=?, `URLLOGIN`=?, `domain`=?, Var1=?, Var2=?, Var3=?, Var4=?, Secret1=?, Secret2=?, poolSize=?, mobileActivity=?, mobilePackage=?, `UsrModif`= ?, `DateModif` = NOW()"
                + " where `system`=? and `country`=? and `environment`=? and `application`=? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setBoolean(i++, object.isActive());
                preStat.setString(i++, object.getIp());
                preStat.setString(i++, object.getUrl());
                preStat.setString(i++, object.getUrlLogin());
                preStat.setString(i++, object.getDomain());
                preStat.setString(i++, object.getVar1());
                preStat.setString(i++, object.getVar2());
                preStat.setString(i++, object.getVar3());
                preStat.setString(i++, object.getVar4());
                preStat.setString(i++, object.getSecret1());
                preStat.setString(i++, object.getSecret2());
                preStat.setInt(i++, object.getPoolSize());
                preStat.setString(i++, object.getMobileActivity());
                preStat.setString(i++, object.getMobilePackage());
                preStat.setString(i++, object.getUsrModif());
                preStat.setString(i++, object.getSystem());
                preStat.setString(i++, object.getCountry());
                preStat.setString(i++, object.getEnvironment());
                preStat.setString(i++, object.getApplication());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to execute query : " + exception.toString()));
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

    private CountryEnvironmentParameters loadFromResultSet(ResultSet resultSet) throws SQLException {
        String system = resultSet.getString("cea.System");
        String count = resultSet.getString("cea.Country");
        String env = resultSet.getString("cea.Environment");
        String application = resultSet.getString("cea.application");
        boolean isActive = resultSet.getBoolean("cea.isActive");
        String ip = resultSet.getString("cea.ip");
        String domain = resultSet.getString("cea.domain");
        String url = resultSet.getString("cea.url");
        String urllogin = resultSet.getString("cea.urllogin");
        String var1 = resultSet.getString("cea.Var1");
        String var2 = resultSet.getString("cea.Var2");
        String var3 = resultSet.getString("cea.Var3");
        String var4 = resultSet.getString("cea.Var4");
        String secret1 = resultSet.getString("cea.Secret1");
        String secret2 = resultSet.getString("cea.Secret2");
        String mobileActivity = resultSet.getString("cea.mobileActivity");
        if (mobileActivity == null) {
            mobileActivity = "";
        }
        String mobilePackage = resultSet.getString("cea.mobilePackage");
        if (mobilePackage == null) {
            mobilePackage = "";
        }
        int poolSize = resultSet.getInt("cea.poolSize");
        String usrModif = resultSet.getString("cea.UsrModif");
        String usrCreated = resultSet.getString("cea.UsrCreated");
        Timestamp dateCreated = resultSet.getTimestamp("cea.DateCreated");
        Timestamp dateModif = resultSet.getTimestamp("cea.DateModif");

        return factoryCountryEnvironmentParameters.create(system, count, env, application, isActive, ip, domain, url, urllogin, var1, var2, var3, var4, secret1, secret2,
                poolSize, mobileActivity, mobilePackage, usrCreated, dateCreated, usrModif, dateModif);
    }

}
