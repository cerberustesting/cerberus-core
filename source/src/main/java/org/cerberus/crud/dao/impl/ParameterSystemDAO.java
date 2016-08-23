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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.crud.dao.IParameterSystemDAO;
import org.cerberus.crud.entity.*;
import org.cerberus.crud.factory.impl.FactoryApplication;
import org.cerberus.crud.factory.impl.FactoryParameterSystem;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryParameterSystem;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerList;
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
public class ParameterSystemDAO implements IParameterSystemDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryParameterSystem factoryParameterSystem;

    private static final Logger LOG = Logger.getLogger(ParameterSystemDAO.class);

    private final int MAX_ROW_SELECTED = 100000;
    private final String OBJECT_NAME = "Parameter";

    @Override
    public List<ParameterSystem> findAllParameterSystem(String mySystem) throws CerberusException {
        boolean throwExep = true;
        List<ParameterSystem> result = null;
        ParameterSystem paramet;
        StringBuilder mySQL = new StringBuilder();
        mySQL.append("SELECT Cerberus.param param, Cerberus.`value` valC, System.`value` valS, Cerberus.description FROM parameter Cerberus ");
        mySQL.append("LEFT OUTER JOIN ( SELECT * from parameter System WHERE System.system= ? ) as System ON System.param = Cerberus.param ");
        mySQL.append(" WHERE Cerberus.system= ''; ");
        final String query = mySQL.toString();

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setString(1, mySystem);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    result = new ArrayList<ParameterSystem>();
                    while (resultSet.next()) {
                        String param = resultSet.getString("param");
                        String valueC = resultSet.getString("valC");
                        String valueS = resultSet.getString("valS");
                        String desc = resultSet.getString("description");
                        paramet = factoryParameterSystem.create(param, valueC, valueS, desc);
                        result.add(paramet);
                        throwExep = false;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ParameterSystemDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ParameterSystemDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ParameterSystemDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ParameterSystemDAO.class.getName(), Level.WARN, e.toString());
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
    public AnswerList readBySystemByCriteria(String system, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch){

        AnswerList response = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<ParameterSystem> objectList = new ArrayList<ParameterSystem>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<String>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS Cerberus.param para, Cerberus.`value` valC, System.`value` valS, Cerberus.description descr FROM parameter Cerberus ");

        query.append(" LEFT OUTER JOIN ( SELECT * from parameter System WHERE System.system = ? ) as System ON System.`param` = Cerberus.`param` ");
        query.append(" WHERE Cerberus.system = ''");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (Cerberus.param like ?");
            searchSQL.append(" or Cerberus.`value` like ?");
            searchSQL.append(" or System.`value` like ?");
            searchSQL.append(" or Cerberus.description like ?)");
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

        if (!StringUtil.isNullOrEmpty(column)) {
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
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;

                preStat.setString(i++, system);

                if (!StringUtil.isNullOrEmpty(searchTerm)) {
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
    public ParameterSystem loadFromResultSet(ResultSet rs) throws SQLException {
        String param = ParameterParserUtil.parseStringParam(rs.getString("para"), "");
        String valc = ParameterParserUtil.parseStringParam(rs.getString("valC"), "");
        String vals = ParameterParserUtil.parseStringParam(rs.getString("valS"), "");
        String description = ParameterParserUtil.parseStringParam(rs.getString("descr"), "");

        //TODO remove when working in test with mockito and autowired
        factoryParameterSystem = new FactoryParameterSystem();
        return factoryParameterSystem.create(param,valc,vals,description);
    }
}
