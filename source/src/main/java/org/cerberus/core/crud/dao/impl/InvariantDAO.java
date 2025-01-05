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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.core.crud.dao.IInvariantDAO;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.factory.IFactoryInvariant;
import org.cerberus.core.crud.utils.RequestDbUtils;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.SqlUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.security.UserSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 28/Dez/2012
 * @since 2.0.0
 */
@Repository
public class InvariantDAO implements IInvariantDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryInvariant factoryInvariant;

    private static final Logger LOG = LogManager.getLogger(InvariantDAO.class);

    private final String OBJECT_NAME = "Invariant";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 1000;

    @Override
    public Invariant readByKey(String id, String value) throws CerberusException {
        final String query = "SELECT * FROM `invariant` WHERE `idname` = ? AND `value` = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL.param.id : " + id);
            LOG.debug("SQL.param.value : " + value);
        }

        return RequestDbUtils.executeQuery(databaseSpring, query,
                ps -> {
                    ps.setString(1, id);
                    ps.setString(2, value);
                },
                resultSet -> {
                    return loadFromResultSet(resultSet);
                }
            );
    }

    @Override
    public Invariant readFirstByIdName(String id) throws CerberusException {
        final String query = "SELECT * FROM `invariant` WHERE `idname` = ? order by sort LIMIT 1;";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL.param.id : " + id);
        }

        return RequestDbUtils.executeQuery(databaseSpring, query,
                ps -> {
                    ps.setString(1, id);
                },
                resultSet -> {
                    return loadFromResultSet(resultSet);
                }
            );
    }

    @Override
    public List<Invariant> readByIdname(String idName) throws CerberusException {

        // secure invariant with allow System user
        String systemClause = "";

        if ( "SYSTEM".equals(idName) ) {
            systemClause = " AND " + UserSecurity.getSystemAllowForSQL("value");
        }

        final String query = "SELECT * FROM invariant i  WHERE i.idname = ? " + systemClause + " ORDER BY sort";

        return RequestDbUtils.executeQueryList(databaseSpring, query,
               ps -> {
                   ps.setString(1, idName);
               },
               resultSet -> {
                   return this.loadFromResultSet(resultSet);
               }
        );
    }

    @Override
    public AnswerList<Invariant> readByIdnameByGp1(String idName, String gp) {
        AnswerList<Invariant> ansList = new AnswerList<>();
        MessageEvent msg;

        List<Invariant> invariantList = new ArrayList<>();
        final String query = "SELECT * FROM invariant i  WHERE i.idname = ? AND i.gp1 = ? ORDER BY sort";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.idName : " + idName);
            LOG.debug("SQL.param.gp : " + gp);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, idName);
                preStat.setString(2, gp);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        invariantList.add(this.loadFromResultSet(resultSet));
                    }
                    if (invariantList.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    }
                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to execute query : " + exception.toString()));
                    invariantList.clear();
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to execute query : " + exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to execute query : " + exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        ansList.setTotalRows(invariantList.size());
        ansList.setDataList(invariantList);
        ansList.setResultMessage(msg);
        return ansList;
    }

    @Override
    public AnswerList<Invariant> readByIdnameByNotGp1(String idName, String gp) {
        AnswerList<Invariant> ansList = new AnswerList<>();
        MessageEvent msg;

        List<Invariant> invariantList = new ArrayList<>();
        final String query = "SELECT * FROM invariant i  WHERE i.idname = ? AND i.gp1 <> ? ORDER BY sort";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.idName : " + idName);
            LOG.debug("SQL.param.gp : " + gp);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, idName);
                preStat.setString(2, gp);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        invariantList.add(this.loadFromResultSet(resultSet));
                    }
                    if (invariantList.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    }
                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to execute query : " + exception.toString()));
                    invariantList.clear();
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to execute query : " + exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to execute query : " + exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        ansList.setTotalRows(invariantList.size());
        ansList.setDataList(invariantList);
        ansList.setResultMessage(msg);
        return ansList;
    }

    @Override
    public AnswerList<Invariant> readByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch, String PublicPrivateFilter) {
        List<Invariant> invariantList = new ArrayList<>();
        AnswerList<Invariant> answer = new AnswerList<>();
        MessageEvent msg;

        StringBuilder searchSQL = new StringBuilder();
        searchSQL.append(" where 1=1 ");

        StringBuilder query = new StringBuilder();
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM invariant ");

        if (!searchTerm.isEmpty() && !individualSearch.isEmpty()) {
            searchSQL.append(" and ");
            searchSQL.append(getSearchString(searchTerm));
            searchSQL.append(" and ");
            searchSQL.append(individualSearch);
        } else if (!individualSearch.isEmpty()) {
            searchSQL.append(" and `");
            searchSQL.append(individualSearch);
            searchSQL.append("`");
        } else if (!searchTerm.isEmpty()) {
            searchSQL.append(" and ");
            searchSQL.append(getSearchString(searchTerm));
        }
        if (!(PublicPrivateFilter.isEmpty())) {
            searchSQL.append(" and ");
            searchSQL.append(PublicPrivateFilter);
        }

        query.append(searchSQL);
        query.append(" order by `");
        query.append(column);
        query.append("` ");
        query.append(dir);

        int nrTotalRows = 0;

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {

                    while (resultSet.next()) {
                        invariantList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (invariantList.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    }

                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                    invariantList.clear();

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        answer.setResultMessage(msg);
        answer.setTotalRows(nrTotalRows);
        answer.setDataList(invariantList);
        return answer;
    }

    @Override
    public AnswerList<Invariant> readByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch, String PublicPrivateFilter) {
        List<Invariant> invariantList = new ArrayList<>();
        AnswerList<Invariant> answer = new AnswerList<>();
        List<String> individalColumnSearchValues = new ArrayList<>();
        MessageEvent msg;

        StringBuilder searchSQL = new StringBuilder();
        searchSQL.append(" where 1=1 ");

        StringBuilder query = new StringBuilder();
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM invariant ");

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (idname like ? or value like ? or sort like ? or description like ? or VeryShortDesc like ? or gp1 like ? or gp2 like ? or gp3 like ?)");
        }
        if (!StringUtil.isEmptyOrNull(PublicPrivateFilter)) {
            searchSQL.append(" and ");
            searchSQL.append(PublicPrivateFilter);
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
        query.append(" order by `");
        query.append(column);
        query.append("` ");
        query.append(dir);

        int nrTotalRows = 0;

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());

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
            for (String individualColumnSearchValue : individalColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {

                    while (resultSet.next()) {
                        invariantList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (invariantList.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    }

                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                    invariantList.clear();

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        answer.setResultMessage(msg);
        answer.setTotalRows(nrTotalRows);
        answer.setDataList(invariantList);
        return answer;
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String column, String dir, String searchTerm, Map<String, List<String>> individualSearch, String PublicPrivateFilter, String columnName) {
        List<String> invariantList = new ArrayList<>();
        AnswerList<String> answer = new AnswerList<>();
        List<String> individalColumnSearchValues = new ArrayList<>();
        MessageEvent msg;

        StringBuilder searchSQL = new StringBuilder();
        searchSQL.append(" where 1=1 ");

        StringBuilder query = new StringBuilder();
        query.append("SELECT distinct ");
        query.append(columnName);
        query.append(" as distinctValues FROM invariant");

        if (!StringUtil.isEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (idname like ? or value like ? or sort like ? or description like ? or VeryShortDesc like ? or gp1 like ? or gp2 like ? or gp3 like ?)");
        }
        if (!StringUtil.isEmptyOrNull(PublicPrivateFilter)) {
            searchSQL.append(" and ");
            searchSQL.append(PublicPrivateFilter);
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
        query.append(" order by `");
        query.append(column);
        query.append("` ");
        query.append(dir);

        int nrTotalRows = 0;

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());

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
            for (String individualColumnSearchValue : individalColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {

                    while (resultSet.next()) {
                        invariantList.add(resultSet.getString("distinctValues"));
                    }

                    if (invariantList.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    }

                } catch (SQLException exception) {
                    LOG.warn("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                    invariantList.clear();

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        answer.setResultMessage(msg);
        answer.setTotalRows(nrTotalRows);
        answer.setDataList(invariantList);
        return answer;
    }

    @Override
    public AnswerList<Invariant> readCountryListEnvironmentLastChanges(String system, Integer nbdays) {
        AnswerList<Invariant> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<Invariant> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p

        query.append("SELECT distinct i.* FROM countryenvparam_log cl ");
        query.append(" JOIN invariant i on i.value=cl.country and i.idname='COUNTRY' ");
        query.append(" WHERE TO_DAYS(NOW()) - TO_DAYS(cl.datecre) <= ? and build != '' and `System` = ? order by i.sort;");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param : " + nbdays);
            LOG.debug("SQL.param : " + system);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                preStat.setInt(i++, nbdays);
                preStat.setString(i++, system);
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
    public Answer create(Invariant object) {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO invariant (`idname`, `value`, `sort`, `description`, `VeryShortDesc`, `gp1`, `gp2`, `gp3`, `gp4`, `gp5`, `gp6`, `gp7`, `gp8`, `gp9`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                preStat.setString(i++, object.getIdName());
                preStat.setString(i++, object.getValue());
                preStat.setInt(i++, object.getSort());
                preStat.setString(i++, object.getDescription());
                preStat.setString(i++, object.getVeryShortDesc());
                preStat.setString(i++, object.getGp1());
                preStat.setString(i++, object.getGp2());
                preStat.setString(i++, object.getGp3());
                preStat.setString(i++, object.getGp4());
                preStat.setString(i++, object.getGp5());
                preStat.setString(i++, object.getGp6());
                preStat.setString(i++, object.getGp7());
                preStat.setString(i++, object.getGp8());
                preStat.setString(i++, object.getGp9());

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
    public Answer delete(Invariant object) {
        MessageEvent msg = null;
        final String query = "DELETE FROM invariant WHERE idname = ? and `value` = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {

                preStat.setString(1, object.getIdName());
                preStat.setString(2, object.getValue());

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
    public Answer update(String idname, String value, Invariant object) {
        MessageEvent msg = null;
        final String query = "UPDATE invariant SET idname = ?, `value` = ?, sort = ?, Description = ?, VeryShortDesc = ?, gp1 = ?, gp2 = ?, gp3 = ?, gp4 = ?, gp5 = ?, gp6 = ?, gp7 = ?, gp8 = ?, gp9 = ?  WHERE idname = ? and `value` = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.idname : " + idname);
            LOG.debug("SQL.param.value : " + value);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i = 1;
                preStat.setString(i++, object.getIdName());
                preStat.setString(i++, object.getValue());
                preStat.setInt(i++, object.getSort());
                preStat.setString(i++, object.getDescription());
                preStat.setString(i++, object.getVeryShortDesc());
                preStat.setString(i++, object.getGp1());
                preStat.setString(i++, object.getGp2());
                preStat.setString(i++, object.getGp3());
                preStat.setString(i++, object.getGp4());
                preStat.setString(i++, object.getGp5());
                preStat.setString(i++, object.getGp6());
                preStat.setString(i++, object.getGp7());
                preStat.setString(i++, object.getGp8());
                preStat.setString(i++, object.getGp9());
                preStat.setString(i++, idname);
                preStat.setString(i++, value);

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

    public Invariant loadFromResultSet(ResultSet resultSet) throws SQLException {
        String idName = (resultSet.getString("idName") != null) ? resultSet.getString("idName") : "";
        int sort = resultSet.getInt("sort");
        String description = (resultSet.getString("description") != null) ? resultSet.getString("description") : "";
        String veryShortDesc = (resultSet.getString("VeryShortDesc") != null) ? resultSet.getString("VeryShortDesc") : "";
        String gp1 = (resultSet.getString("gp1") != null) ? resultSet.getString("gp1") : "";
        String gp2 = (resultSet.getString("gp2") != null) ? resultSet.getString("gp2") : "";
        String gp3 = (resultSet.getString("gp3") != null) ? resultSet.getString("gp3") : "";
        String gp4 = (resultSet.getString("gp4") != null) ? resultSet.getString("gp4") : "";
        String gp5 = (resultSet.getString("gp5") != null) ? resultSet.getString("gp5") : "";
        String gp6 = (resultSet.getString("gp6") != null) ? resultSet.getString("gp6") : "";
        String gp7 = (resultSet.getString("gp7") != null) ? resultSet.getString("gp7") : "";
        String gp8 = (resultSet.getString("gp8") != null) ? resultSet.getString("gp8") : "";
        String gp9 = (resultSet.getString("gp9") != null) ? resultSet.getString("gp9") : "";
        String value = (resultSet.getString("value") != null) ? resultSet.getString("value") : "";
        return factoryInvariant.create(idName, value, sort, description, veryShortDesc, gp1, gp2, gp3, gp4, gp5, gp6, gp7, gp8, gp9);
    }

    private String getSearchString(String searchTerm) {
        if (StringUtil.isEmptyOrNull(searchTerm)) {
            return "";
        } else {
            StringBuilder gSearch = new StringBuilder();
            gSearch.append(" (`idname` like '%");
            gSearch.append(searchTerm);
            gSearch.append("%'");
            gSearch.append(" or `value` like '%");
            gSearch.append(searchTerm);
            gSearch.append("%'");
            gSearch.append(" or `sort` like '%");
            gSearch.append(searchTerm);
            gSearch.append("%'");
            gSearch.append(" or `description` like '%");
            gSearch.append(searchTerm);
            gSearch.append("%'");
            gSearch.append(" or `veryshortdesc` like '%");
            gSearch.append(searchTerm);
            gSearch.append("%'");
            gSearch.append(" or `gp1` like '%");
            gSearch.append(searchTerm);
            gSearch.append("%'");
            gSearch.append(" or `gp2` like '%");
            gSearch.append(searchTerm);
            gSearch.append("%'");
            gSearch.append(" or `gp3` like '%");
            gSearch.append(searchTerm);
            gSearch.append("%') ");
            return gSearch.toString();
        }
    }
}
