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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.crud.dao.IInvariantDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryInvariant;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.log.MyLogger;
import org.cerberus.statistics.EnvironmentStatisticsDAOImpl;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerList;
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

    private static final Logger LOG = Logger.getLogger(InvariantDAO.class);

    private final String OBJECT_NAME = "Invariant";
    private final int MAX_ROW_SELECTED = 1000;

    @Override
    public Invariant readByKey(String idName, String value) throws CerberusException {
        boolean throwException = true;
        Invariant result = null;
        final String query = "SELECT * FROM invariant i  WHERE i.idname = ? AND i.value = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, idName);
                preStat.setString(2, value);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        throwException = false;
                        result = this.loadFromResultSet(resultSet);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } catch (NullPointerException ex) {
                    MyLogger.log(InvariantDAO.class.getName(), Level.FATAL, "InvariantDAO - NullPointerException Resultset");
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } catch (NullPointerException ex) {
                MyLogger.log(InvariantDAO.class.getName(), Level.FATAL, "InvariantDAO - NullPointerException Statement");
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } catch (NullPointerException ex) {
            MyLogger.log(InvariantDAO.class.getName(), Level.FATAL, "InvariantDAO - NullPointerException Connection");
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(InvariantDAO.class.getName(), Level.WARN, "Connection already closed!");
            }
        }
        if (throwException) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    @Override
    public Invariant readByIdnameBySort(String idName, Integer sort) throws CerberusException {
        boolean throwException = true;
        Invariant result = null;
        final String query = "SELECT * FROM invariant i  WHERE i.idname = ? AND i.sort = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, idName);
                preStat.setInt(2, sort);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        throwException = false;
                        result = this.loadFromResultSet(resultSet);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } catch (NullPointerException ex) {
                    MyLogger.log(InvariantDAO.class.getName(), Level.FATAL, "InvariantDAO - NullPointerException Resultset");
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } catch (NullPointerException ex) {
                MyLogger.log(InvariantDAO.class.getName(), Level.FATAL, "InvariantDAO - NullPointerException Statement");
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } catch (NullPointerException ex) {
            MyLogger.log(InvariantDAO.class.getName(), Level.FATAL, "InvariantDAO - NullPointerException Connection");
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(InvariantDAO.class.getName(), Level.WARN, "Connection already closed!");
            }
        }
        if (throwException) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    @Override
    public AnswerList readByIdname(String idName) {
        AnswerList answer = new AnswerList();
        MessageEvent msg;
        List<Invariant> result = new ArrayList<Invariant>();;

        final String query = "SELECT * FROM invariant i  WHERE i.idname = ? ORDER BY sort";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, idName);

                ResultSet resultSet = preStat.executeQuery();
                try {

                    while (resultSet.next()) {
                        result.add(this.loadFromResultSet(resultSet));
                    }
                    if (result.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    }

                } catch (SQLException exception) {
                    MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                    result.clear();
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(InvariantDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        answer.setTotalRows(result.size());
        answer.setDataList(result);
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerList readByIdnameByGp1(String idName, String gp) {
        AnswerList ansList = new AnswerList();
        MessageEvent msg;

        List<Invariant> invariantList = new ArrayList<Invariant>();
        final String query = "SELECT * FROM invariant i  WHERE i.idname = ? AND i.gp1 = ? ORDER BY sort";

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
                    MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to execute query : " + exception.toString()));
                    invariantList.clear();
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to execute query : " + exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to execute query : " + exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(InvariantDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        ansList.setTotalRows(invariantList.size());
        ansList.setDataList(invariantList);
        ansList.setResultMessage(msg);
        return ansList;
    }

    @Override
    public AnswerList readByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch, String PublicPrivateFilter) {
        List<Invariant> invariantList = new ArrayList<Invariant>();
        AnswerList answer = new AnswerList();
        MessageEvent msg;

        StringBuilder searchSQL = new StringBuilder();
        searchSQL.append(" where 1=1 ");

        StringBuilder query = new StringBuilder();
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM invariant ");

        if (!searchTerm.equals("") && !individualSearch.equals("")) {
            searchSQL.append(" and ");
            searchSQL.append(getSearchString(searchTerm));
            searchSQL.append(" and ");
            searchSQL.append(individualSearch);
        } else if (!individualSearch.equals("")) {
            searchSQL.append(" and `");
            searchSQL.append(individualSearch);
            searchSQL.append("`");
        } else if (!searchTerm.equals("")) {
            searchSQL.append(" and ");
            searchSQL.append(getSearchString(searchTerm));
        }
        if (!(PublicPrivateFilter.equalsIgnoreCase(""))) {
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
                    MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                    invariantList.clear();

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, e.toString());
            }
        }
        answer.setResultMessage(msg);
        answer.setTotalRows(nrTotalRows);
        answer.setDataList(invariantList);
        return answer;
    }

    @Override
    public AnswerList readByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch, String PublicPrivateFilter) {
        List<Invariant> invariantList = new ArrayList<Invariant>();
        AnswerList answer = new AnswerList();
        List<String> individalColumnSearchValues = new ArrayList<String>();
        MessageEvent msg;

        StringBuilder searchSQL = new StringBuilder();
        searchSQL.append(" where 1=1 ");

        StringBuilder query = new StringBuilder();
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM invariant ");

        if (!Strings.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and ?");
        }
        if (!Strings.isNullOrEmpty(PublicPrivateFilter)) {
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

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());

            int i = 1;
            if (!StringUtil.isNullOrEmpty(searchTerm)) {
                preStat.setString(i++, getSearchString(searchTerm));
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
                    MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                    invariantList.clear();

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, e.toString());
            }
        }
        answer.setResultMessage(msg);
        answer.setTotalRows(nrTotalRows);
        answer.setDataList(invariantList);
        return answer;
    }

    @Override
    public AnswerList readDistinctValuesByCriteria(String column, String dir, String searchTerm, Map<String, List<String>> individualSearch, String PublicPrivateFilter, String columnName) {
        List invariantList = new ArrayList();
        AnswerList answer = new AnswerList();
        List<String> individalColumnSearchValues = new ArrayList<String>();
        MessageEvent msg;

        StringBuilder searchSQL = new StringBuilder();
        searchSQL.append(" where 1=1 ");

        StringBuilder query = new StringBuilder();
        query.append("SELECT distinct ");
        query.append(columnName);
        query.append(" as distinctValues FROM invariant");

        if (!Strings.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and ?");
        }
        if (!Strings.isNullOrEmpty(PublicPrivateFilter)) {
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
            if (!StringUtil.isNullOrEmpty(searchTerm)) {
                preStat.setString(i++, getSearchString(searchTerm));
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
                    MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                    invariantList.clear();

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, e.toString());
            }
        }
        answer.setResultMessage(msg);
        answer.setTotalRows(nrTotalRows);
        answer.setDataList(invariantList);
        return answer;
    }

    @Override
    public AnswerList readInvariantCountryListEnvironmentLastChanges(String system, Integer nbdays) {
        AnswerList response = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<Invariant> objectList = new ArrayList<Invariant>();
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
                        response = new AnswerList(objectList, nrTotalRows);
                    } else if (objectList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList(objectList, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList(objectList, nrTotalRows);
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
    public Integer getNumberOfInvariant(String searchTerm, String PublicPrivateFilter) throws CerberusException {
        boolean throwException = true;
        Integer result = 0;

        StringBuilder searchSQL = new StringBuilder();
        if (!(PublicPrivateFilter.equalsIgnoreCase(""))) {
            searchSQL.append(" and ");
            searchSQL.append(PublicPrivateFilter);
        }
        if (!(searchTerm.equalsIgnoreCase(""))) {
            searchSQL.append(" and ");
            searchSQL.append(getSearchString(searchTerm));
        }

        String query = "SELECT count(*) FROM invariant i  WHERE 1=1 " + searchSQL.toString();

        MyLogger.log(InvariantDAO.class.getName(), Level.DEBUG, query);

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        throwException = false;
                        result = resultSet.getInt(1);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } catch (NullPointerException ex) {
                    MyLogger.log(InvariantDAO.class.getName(), Level.FATAL, "InvariantDAO - NullPointerException Resultset");
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } catch (NullPointerException ex) {
                MyLogger.log(InvariantDAO.class.getName(), Level.FATAL, "InvariantDAO - NullPointerException Statement");
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } catch (NullPointerException ex) {
            MyLogger.log(InvariantDAO.class.getName(), Level.FATAL, "InvariantDAO - NullPointerException Connection");
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(InvariantDAO.class.getName(), Level.WARN, "Connection already closed!");
            }
        }
        if (throwException) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    @Override
    public void create(Invariant invariant) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO invariant (`idname`, `value`, `sort`, `description`, `VeryShortDesc`, `gp1`, `gp2`, `gp3`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, invariant.getIdName());
                preStat.setString(2, invariant.getValue());
                preStat.setInt(3, invariant.getSort());
                preStat.setString(4, invariant.getDescription());
                preStat.setString(5, invariant.getVeryShortDesc());
                preStat.setString(6, invariant.getGp1());
                preStat.setString(7, invariant.getGp2());
                preStat.setString(8, invariant.getGp3());

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(InvariantDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void delete(Invariant invariant) throws CerberusException {
        boolean throwExcep = false;
        final String query = "DELETE FROM invariant WHERE idname = ? and `value` = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, invariant.getIdName());
                preStat.setString(2, invariant.getValue());

                throwExcep = preStat.executeUpdate() == 0;
            } catch (SQLException exception) {
                MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(InvariantDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void update(Invariant invariant) throws CerberusException {
        boolean throwExcep = false;
        final String query = "UPDATE invariant SET sort = ?, Description = ?, VeryShortDesc = ?, gp1 = ?, gp2 = ?, gp3 = ?  WHERE idname = ? and `value` = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setInt(1, invariant.getSort());
                preStat.setString(2, invariant.getDescription());
                preStat.setString(3, invariant.getVeryShortDesc());
                preStat.setString(4, invariant.getGp1());
                preStat.setString(5, invariant.getGp2());
                preStat.setString(6, invariant.getGp3());
                preStat.setString(7, invariant.getIdName());
                preStat.setString(8, invariant.getValue());

                throwExcep = preStat.executeUpdate() == 0;
            } catch (SQLException exception) {
                MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(InvariantDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(InvariantDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    public Invariant loadFromResultSet(ResultSet resultSet) throws SQLException {
        String idName = (resultSet.getString("idName") != null)?resultSet.getString("idName"):"";
        int sort = resultSet.getInt("sort");
        String description = (resultSet.getString("description") != null)?resultSet.getString("description"):"";
        String veryShortDesc = (resultSet.getString("VeryShortDesc") != null)?resultSet.getString("VeryShortDesc"):"";
        String gp1 = (resultSet.getString("gp1") != null)?resultSet.getString("gp1"):"";
        String gp2 = (resultSet.getString("gp2") != null)?resultSet.getString("gp2"):"";
        String gp3 = (resultSet.getString("gp3") != null)?resultSet.getString("gp3"):"";
        String value = (resultSet.getString("value") != null)?resultSet.getString("value"):"";
        return factoryInvariant.create(idName, value, sort, description, veryShortDesc, gp1, gp2, gp3);
    }

    private String getSearchString(String searchTerm) {
        if (StringUtil.isNullOrEmpty(searchTerm)) {
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
