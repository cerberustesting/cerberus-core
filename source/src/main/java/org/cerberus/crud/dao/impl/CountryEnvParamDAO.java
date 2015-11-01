/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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

import com.google.common.base.Strings;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.crud.dao.ICountryEnvParamDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.CountryEnvParam;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryCountryEnvParam;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerList;
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
    private static final Logger LOG = Logger.getLogger(CountryEnvParamDAO.class);
    private final int MAX_ROW_SELECTED = 10000;
    private final String OBJECT_NAME = "CountryEnvParam";

    @Override
    public CountryEnvParam findCountryEnvParamByKey(String system, String country, String environment) throws CerberusException {
        CountryEnvParam result = null;
        boolean throwex = false;
        StringBuilder query = new StringBuilder();
        query.append("SELECT `system`, country, environment, Description, Build, Revision,chain, distriblist, eMailBodyRevision, type,eMailBodyChain, eMailBodyDisableEnvironment,  active, maintenanceact, ");
        query.append("maintenancestr, maintenanceend FROM countryenvparam WHERE `system` = ? AND country = ? AND environment = ?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, system);
                preStat.setString(2, country);
                preStat.setString(3, environment);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.next()) {
                        result = this.loadCountryEnvParamFromResultSet(resultSet);
                    } else {
                        throwex = true;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(CountryEnvironmentParametersDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CountryEnvironmentParametersDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CountryEnvironmentParametersDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CountryEnvironmentParametersDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwex) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    private CountryEnvParam loadCountryEnvParamFromResultSet(ResultSet resultSet) throws SQLException {
        String system = resultSet.getString("System");
        String count = resultSet.getString("Country");
        String env = resultSet.getString("Environment");
        String description = resultSet.getString("Description");
        String build = resultSet.getString("Build");
        String revision = resultSet.getString("Revision");
        String chain = resultSet.getString("chain");
        String distribList = resultSet.getString("distribList");
        String eMailBodyRevision = resultSet.getString("eMailBodyRevision");
        String type = resultSet.getString("type");
        String eMailBodyChain = resultSet.getString("eMailBodyChain");
        String eMailBodyDisableEnvironment = resultSet.getString("eMailBodyDisableEnvironment");
        boolean active = StringUtil.parseBoolean(resultSet.getString("active"));
        boolean maintenanceAct = StringUtil.parseBoolean(resultSet.getString("maintenanceact"));
        String maintenanceStr = resultSet.getString("maintenancestr");
        String maintenanceEnd = resultSet.getString("maintenanceend");
        return factoryCountryEnvParam.create(system, count, env, description, build, revision, chain, distribList, eMailBodyRevision,
                type, eMailBodyChain, eMailBodyDisableEnvironment, active, maintenanceAct, maintenanceStr, maintenanceEnd);
    }

    @Override
    public AnswerList readActiveBySystem(String system) {
        AnswerList response = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<CountryEnvParam> countryEnvParamList = new ArrayList<CountryEnvParam>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT * FROM countryenvparam WHERE system = ? AND active = 'Y'");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, system);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        countryEnvParamList.add(this.loadCountryEnvParamFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (countryEnvParamList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList(countryEnvParamList, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList(countryEnvParamList, nrTotalRows);
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
                LOG.error("Unable to close connection : " + exception.toString());
            }
        }
        response.setResultMessage(msg);
        response.setDataList(countryEnvParamList);
        return response;
    }

    @Override
    public List<CountryEnvParam> findCountryEnvParamByCriteria(CountryEnvParam countryEnvParam) throws CerberusException {
        List<CountryEnvParam> result = new ArrayList();
        boolean throwex = false;
        StringBuilder query = new StringBuilder();
        query.append("SELECT `system`, `country`, `environment`, `Build`, `Revision`,`chain`, `distriblist`, `eMailBodyRevision`, `type`,`eMailBodyChain`, `eMailBodyDisableEnvironment`,  `active`, `maintenanceact`, ");
        query.append(" `maintenancestr`, `maintenanceend` FROM countryenvparam WHERE `system` LIKE ? AND `country` LIKE ? ");
        query.append("AND `environment` LIKE ? AND `build` LIKE ? AND `Revision` LIKE ? AND `chain` LIKE ? ");
        query.append("AND `distriblist` LIKE ? AND `eMailBodyRevision` LIKE ? AND `type` LIKE ? AND `eMailBodyChain` LIKE ? ");
        query.append("AND `eMailBodyDisableEnvironment` LIKE ? AND `active` LIKE ? AND `maintenanceact` LIKE ? AND `maintenancestr` LIKE ? ");
        query.append("AND `maintenanceend` LIKE ? ");

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
                        result.add(this.loadCountryEnvParamFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(CountryEnvironmentParametersDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CountryEnvironmentParametersDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CountryEnvironmentParametersDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CountryEnvironmentParametersDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwex) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    @Override
    public List<CountryEnvParam> findAll(String system) throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM countryenvparam c where `system`=?";

        List<CountryEnvParam> result = new ArrayList<CountryEnvParam>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        result.add(this.loadCountryEnvParamFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    throwEx = true;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                throwEx = true;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            throwEx = true;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception closing the connection :" + e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    @Override
    public void update(CountryEnvParam cep) throws CerberusException {
        final StringBuffer query = new StringBuffer("UPDATE `countryenvparam` SET `build`=?, ");
        query.append("`revision`=?,`chain`=?, `distriblist`=?, `eMailBodyRevision`=?, `type`=?,`eMailBodyChain`=?,");
        query.append("`eMailBodyDisableEnvironment`=?,  `active`=?, `maintenanceact`=?, `maintenancestr`=?, `maintenanceend`=? ");
        query.append(" where `system`=? and `country`=? and `environment`=? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, cep.getBuild());
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
            preStat.setString(13, cep.getSystem());
            preStat.setString(14, cep.getCountry());
            preStat.setString(15, cep.getEnvironment());

            try {
                preStat.executeUpdate();
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception closing the connection :" + e.toString());
            }
        }
    }

    @Override
    public void delete(CountryEnvParam cep) throws CerberusException {
        final StringBuffer query = new StringBuffer("DELETE FROM `countryenvparam` WHERE `system`=? and `country`=? and `environment`=?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, cep.getSystem());
            preStat.setString(2, cep.getCountry());
            preStat.setString(3, cep.getEnvironment());

            try {
                preStat.executeUpdate();
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception closing the connection :" + e.toString());
            }
        }
    }

    @Override
    public void create(CountryEnvParam cep) throws CerberusException {
        final StringBuffer query = new StringBuffer("INSERT INTO `countryenvparam` ");
        query.append("(`system`, `country`, `environment`, `build`, `revision`,`chain`, `distriblist`, `eMailBodyRevision`, `type`,`eMailBodyChain`,");
        query.append("`eMailBodyDisableEnvironment`,  `active`, `maintenanceact`, `maintenancestr`, `maintenanceend`) VALUES ");
        query.append("(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
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

            try {
                preStat.executeUpdate();
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception closing the connection :" + e.toString());
            }
        }
    }

    @Override
    public List<CountryEnvParam> findListByCriteria(String system, int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        List<CountryEnvParam> result = new ArrayList<CountryEnvParam>();
        StringBuilder gSearch = new StringBuilder();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM countryenvparam where `system` = ? ");

        gSearch.append(" and (`country` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `build` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `revision` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `chain` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `distriblist` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `eMailBodyRevision` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `type` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `eMailBodyChain` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `eMailBodyDisableEnvironment` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `active` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `maintenanceact` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `maintenancestr` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `maintenanceend` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%')");

        if (!searchTerm.equals("") && !individualSearch.equals("")) {
            searchSQL.append(gSearch.toString());
            searchSQL.append(" and ");
            searchSQL.append(individualSearch);
        } else if (!individualSearch.equals("")) {
            searchSQL.append(" and `");
            searchSQL.append(individualSearch);
            searchSQL.append("`");
        } else if (!searchTerm.equals("")) {
            searchSQL.append(gSearch.toString());
        }

        query.append(searchSQL);
        query.append("order by `");
        query.append(column);
        query.append("` ");
        query.append(dir);
        query.append(" limit ");
        query.append(start);
        query.append(" , ");
        query.append(amount);

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, system);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {

                    while (resultSet.next()) {
                        result.add(this.loadCountryEnvParamFromResultSet(resultSet));
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.error(e.toString());
            }
        }
        return result;
    }

    @Override
    public Integer count(String searchTerm, String inds) {
        Integer result = 0;
        StringBuilder query = new StringBuilder();
        StringBuilder gSearch = new StringBuilder();
        String searchSQL = "";

        query.append("SELECT count(*) FROM countryenvparam");

        gSearch.append(" where (`system` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `country` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `build` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `revision` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `chain` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `distriblist` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `eMailBodyRevision` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `type` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `eMailBodyChain` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `eMailBodyDisableEnvironment` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `active` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `maintenanceact` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `maintenancestr` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `maintenanceend` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%')");

        if (!searchTerm.equals("") && !inds.equals("")) {
            searchSQL = gSearch.toString() + " and " + inds;
        } else if (!inds.equals("")) {
            searchSQL = " where " + inds;
        } else if (!searchTerm.equals("")) {
            searchSQL = gSearch.toString();
        }

        query.append(searchSQL);

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {

                    if (resultSet.first()) {
                        result = resultSet.getInt(1);
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.error(e.toString());
            }
        }
        return result;
    }

    @Override
    public List<CountryEnvParam> findListByCriteria(String system) {
        List<CountryEnvParam> result = new ArrayList<CountryEnvParam>();

        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM countryenvparam where `system` = ? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, system);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {

                    while (resultSet.next()) {
                        result.add(this.loadCountryEnvParamFromResultSet(resultSet));
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.error(e.toString());
            }
        }
        return result;
    }
}
