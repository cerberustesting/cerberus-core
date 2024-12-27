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
import org.cerberus.core.crud.dao.IParameterDAO;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.crud.factory.impl.FactoryParameter;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.factory.IFactoryParameter;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.SqlUtil;
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
 * @version 1.0, 22/03/2013
 * @since 2.0.0
 */
@Repository
public class ParameterDAO implements IParameterDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryParameter factoryParameter;

    private static final Logger LOG = LogManager.getLogger(ParameterDAO.class);

    private final int MAX_ROW_SELECTED = 100000;
    private final String OBJECT_NAME = "Parameter";

    /**
     * Declare SQL queries used by this {@link RobotCapabilityDAO}
     *
     * @author Aurelien Bourdon
     */
    private static interface Query {

        /**
         * Get {@link Parameter} with the given key
         */
        String READ_BY_KEY = "SELECT * FROM `parameter` WHERE `system` = ? AND `param` = ? ";

        /**
         * Create a new {@link Parameter}
         */
        String CREATE = "INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES (?, ?, ?, ?)";

        /**
         * Update an existing {@link Parameter}
         */
        String UPDATE = "UPDATE `parameter` SET `value` = ? WHERE `system` = ? AND `param` = ?";

        /**
         * Remove an existing {@link Parameter}
         */
        String DELETE = "DELETE FROM `parameter` WHERE `system` = ? AND `param` = ?";
    }

    @Override
    public Parameter findParameterByKey(String system, String key) throws CerberusException {
        boolean throwExep = false;
        Parameter result = null;
        final String query = "SELECT * FROM parameter p WHERE p.`system` = ? and p.param = ? ";

        Connection connection = this.databaseSpring.connect();
        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        try {
            PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            try {
                preStat.setString(1, system);
                preStat.setString(2, key);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        String value = resultSet.getString("value");
                        String desc = resultSet.getString("description");
                        result = factoryParameter.create(system, key, value, desc);
                    } else {
                        throwExep = true;
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
        if (throwExep) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND);
            mes.setDescription(mes.getDescription() + " Parameter not defined : " + key);
            throw new CerberusException(mes);
        }
        return result;
    }

    @Override
    public List<Parameter> findAllParameter() throws CerberusException {
        boolean throwExep = true;
        List<Parameter> result = null;
        Parameter paramet;
        final String query = "SELECT * FROM parameter p ";

        Connection connection = this.databaseSpring.connect();
        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        try {
            PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    result = new ArrayList<>();
                    while (resultSet.next()) {
                        String system = resultSet.getString("system");
                        String param = resultSet.getString("param");
                        String value = resultSet.getString("value");
                        String desc = resultSet.getString("description");
                        paramet = factoryParameter.create(system, param, value, desc);
                        result.add(paramet);
                        throwExep = false;
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
        if (throwExep) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND);
            mes.setDescription(mes.getDescription() + " Parameter table empty.");
            throw new CerberusException(mes);
        }
        return result;
    }

    @Override
    public List<Parameter> findAllParameterWithSystem1(String mySystem, String mySystem1) throws CerberusException {
        boolean throwExep = true;
        List<Parameter> result = null;
        Parameter paramet;
        StringBuilder mySQL = new StringBuilder();
        mySQL.append("SELECT par.param param, par.`value` valC, par2.`value` valS, par2.description FROM parameter par ");
        mySQL.append("LEFT OUTER JOIN ( SELECT * from parameter par2 WHERE par2.system= ? ) as par2 ON par.param = par.param ");
        mySQL.append(" WHERE par.system= ?; ");
        final String query = mySQL.toString();

        Connection connection = this.databaseSpring.connect();

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try {
            PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            preStat.setString(1, mySystem1);
            preStat.setString(1, mySystem);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    result = new ArrayList<>();
                    while (resultSet.next()) {
                        String param = resultSet.getString("param");
                        String valueC = resultSet.getString("valC");
                        String valueS = resultSet.getString("valS");
                        String desc = resultSet.getString("description");
                        paramet = factoryParameter.create(param, "", valueC, desc, mySystem, valueS);
                        result.add(paramet);
                        throwExep = false;
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
        if (throwExep) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND);
            mes.setDescription(mes.getDescription() + " Parameter table empty.");
            throw new CerberusException(mes);
        }
        return result;
    }

    @Override
    public AnswerList<Parameter> readWithSystem1BySystemByCriteria(String system, String system1, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {

        AnswerList<Parameter> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<Parameter> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS par.param, par.`value`, par.description, ? system1, par1.`value` system1Value FROM parameter par ");
        query.append(" LEFT OUTER JOIN ( SELECT * from parameter WHERE `system` = ? ) as par1 ON par1.`param` = par.`param` ");
        query.append(" WHERE par.`system` = ?");

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (par.param like ?");
            searchSQL.append(" or par.`value` like ?");
            searchSQL.append(" or par1.`value` like ?");
            searchSQL.append(" or par.description like ?)");
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
        }
        Connection connection = this.databaseSpring.connect();

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            try {
                int i = 1;

                preStat.setString(i++, system1);
                preStat.setString(i++, system1);
                preStat.setString(i++, system);

                if (!StringUtil.isEmptyOrNull(searchTerm)) {
                    preStat.setString(i++, "%" + searchTerm + "%");
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
                        objectList.add(this.loadFromResultSetWithSystem1(resultSet));
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
    public AnswerItem<Parameter> readWithSystem1ByKey(String system, String key, String system1) {
        AnswerItem<Parameter> a = new AnswerItem<>();
        StringBuilder query = new StringBuilder();
        Parameter p = new Parameter();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        query.append("SELECT par.param, par.`value`, par.description, ? system1, par1.`value` system1value FROM parameter par "
                + "LEFT OUTER JOIN (SELECT * FROM parameter WHERE `system` = ? and param = ?) as par1 ON par.param = par1.param WHERE par.`system` = ? AND par.param = ?");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.system1 : " + system1);
            LOG.debug("SQL.param.system : " + system);
            LOG.debug("SQL.param.key : " + key);
        }

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {

            preStat.setString(1, system1);
            preStat.setString(2, system1);
            preStat.setString(3, key);
            preStat.setString(4, system);
            preStat.setString(5, key);

            try (ResultSet resultSet = preStat.executeQuery();) {
                //gets the data
                while (resultSet.next()) {
                    p = this.loadFromResultSetWithSystem1(resultSet);
                }
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

            }
        } catch (SQLException e) {
            LOG.error("Unable to execute query : " + e.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", e.toString()));
        }
        a.setResultMessage(msg);
        a.setItem(p);
        return a;
    }

    @Override
    public Parameter loadFromResultSetWithSystem1(ResultSet rs) throws SQLException {
        String param = ParameterParserUtil.parseStringParam(rs.getString("par.param"), "");
        String value = ParameterParserUtil.parseStringParam(rs.getString("par.value"), "");
        String description = ParameterParserUtil.parseStringParam(rs.getString("par.description"), "");
        String system1 = ParameterParserUtil.parseStringParam(rs.getString("system1"), "");
        String system1Value = ParameterParserUtil.parseStringParam(rs.getString("system1Value"), "");

        //TODO remove when working in test with mockito and autowired
        factoryParameter = new FactoryParameter();
        return factoryParameter.create("", param, value, description, system1, system1Value);
    }

    @Override
    public Parameter loadFromResultSet(ResultSet rs) throws SQLException {
        String system = ParameterParserUtil.parseStringParam(rs.getString("system"), "");
        String param = ParameterParserUtil.parseStringParam(rs.getString("param"), "");
        String value = ParameterParserUtil.parseStringParam(rs.getString("value"), "");
        String description = ParameterParserUtil.parseStringParam(rs.getString("description"), "");

        //TODO remove when working in test with mockito and autowired
        factoryParameter = new FactoryParameter();
        return factoryParameter.create(system, param, value, description);
    }

    @Override
    public AnswerList<String> readDistinctValuesWithSystem1ByCriteria(String system, String system1, String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        AnswerList<String> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();

        query.append("SELECT distinct ");
        query.append(columnName);
        query.append(" as distinctValues FROM parameter par");

        query.append(" LEFT OUTER JOIN ( SELECT * from parameter par1 WHERE par1.system = ? ) as par1 ON par1.`param` = par.`param` ");
        query.append(" WHERE par.system = ?");

        // Not allowing distinct on forbiden data.
        if (columnName.contains("par.value") || columnName.contains("par1.value")) {
            searchSQL.append(" and par.param not in " + Parameter.SECUREDPARAMINSQLCLAUSE);
        }

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (par.param like ?");
            searchSQL.append(" or par.`value` like ?");
            searchSQL.append(" or par1.`value` like ?");
            searchSQL.append(" or par.description like ?)");
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
//        query.append(" group by ifnull(").append(columnName).append(",'')");
        query.append(" order by ").append(columnName).append(" asc");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); Statement stm = connection.createStatement();) {

            int i = 1;
            if (!StringUtil.isEmptyOrNull(system1)) {
                preStat.setString(i++, system1);
            }
            preStat.setString(i++, system);
            if (!StringUtil.isEmptyOrNull(searchTerm)) {
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
            }
            for (String individualColumnSearchValue : individalColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }

            try (ResultSet resultSet = preStat.executeQuery(); ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()");) {
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
    public AnswerItem<Parameter> readByKey(String system, String param) {
        AnswerItem<Parameter> ans = new AnswerItem<>();
        MessageEvent msg = null;

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(Query.READ_BY_KEY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

            // Debug message on SQL.
            if (LOG.isDebugEnabled()) {
                LOG.debug("SQL : " + Query.READ_BY_KEY);
                LOG.debug("SQL.param.system : " + system);
                LOG.debug("SQL.param.param : " + param);
            }

            // Prepare and execute query
            preStat.setString(1, system);
            preStat.setString(2, param);

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
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public Answer update(Parameter object) {
        Answer ans = new Answer();
        MessageEvent msg = null;

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(Query.UPDATE, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {

            // Debug message on SQL.
            if (LOG.isDebugEnabled()) {
                LOG.debug("SQL : " + Query.UPDATE);
                LOG.debug("SQL.param.system : " + object.getSystem());
                LOG.debug("SQL.param.param : " + object.getParam());
            }
            // Prepare and execute query
            preStat.setString(1, object.getValue());
            preStat.setString(2, object.getSystem());
            preStat.setString(3, object.getParam());
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "UPDATE");
        } catch (Exception e) {
            LOG.warn("Unable to update parameter: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public Answer setParameter(String parameterKey, String system, String value) {
        Answer ans = new Answer();
        MessageEvent msg = null;

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(Query.UPDATE, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {

            // Debug message on SQL.
            if (LOG.isDebugEnabled()) {
                LOG.debug("SQL : " + Query.UPDATE);
                LOG.debug("SQL.param.system : " + system);
                LOG.debug("SQL.param.param : " + parameterKey);
            }
            // Prepare and execute query
            preStat.setString(1, value);
            preStat.setString(2, system);
            preStat.setString(3, parameterKey);
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "UPDATE");
        } catch (Exception e) {
            LOG.warn("Unable to update parameter: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public Answer create(Parameter object) {
        Answer ans = new Answer();
        MessageEvent msg = null;

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(Query.CREATE, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {

            // Debug message on SQL.
            if (LOG.isDebugEnabled()) {
                LOG.debug("SQL : " + Query.CREATE);
            }
            // Prepare and execute query
            preStat.setString(1, object.getSystem());
            preStat.setString(2, object.getParam());
            preStat.setString(3, object.getValue());
            preStat.setString(4, object.getDescription());
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "CREATE");
        } catch (Exception e) {
            LOG.warn("Unable to create parameter: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

}
