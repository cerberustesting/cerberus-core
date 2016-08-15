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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.crud.dao.ITestDataLibDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.crud.factory.IFactoryTestDataLib;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.MessageEventUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author bcivel
 * @author FNogueira
 */
@Repository
public class TestDataLibDAO implements ITestDataLibDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestDataLib factoryTestDataLib;

    private static final Logger LOG = Logger.getLogger(TestDataLibDAO.class);

    private final String OBJECT_NAME = "Test Data Library";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 10000000;

    @Override
    public AnswerItem readByNameBySystemByEnvironmentByCountry(String name, String system, String environment, String country, String type) {
        AnswerItem answer = new AnswerItem();
        TestDataLib result = null;
        MessageEvent msg;

        final String query = new StringBuilder("SELECT * FROM testdatalib tdl")
                .append(" LEFT OUTER JOIN testdatalibdata tdd ON tdl.TestDataLibID = tdd.TestDataLibID and tdd.Subdata='' ")
                .append(" WHERE `name` LIKE ?")
                .append(" and (`system` = ? or `system` = '')")
                .append(" and (`environment` = ? or `environment` = '')")
                .append(" and (`country` = ? or `country` = '')")
                .append(" ORDER BY `name` DESC, system DESC, environment DESC, country DESC, tdl.TestDataLibID ASC")
                .append(" LIMIT 1").toString();

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL name : " + name);
            LOG.debug("SQL system : " + system);
            LOG.debug("SQL environment : " + environment);
            LOG.debug("SQL country : " + country);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setString(1, name);
            preStat.setString(2, ParameterParserUtil.returnEmptyStringIfNull(system));
            preStat.setString(3, ParameterParserUtil.returnEmptyStringIfNull(environment));
            preStat.setString(4, ParameterParserUtil.returnEmptyStringIfNull(country));
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadFromResultSet(resultSet);
                        msg = MessageEventUtil.createSelectSuccessMessageDAO(OBJECT_NAME);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);

                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();

            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();

        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }
        answer.setItem(result);
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerItem readByKey(String name, String system, String environment, String country) {
        AnswerItem answer = new AnswerItem();
        TestDataLib result = null;
        MessageEvent msg;

        final String query = new StringBuilder("SELECT * FROM testdatalib tdl ")
                .append(" LEFT OUTER JOIN testdatalibdata tdd ON tdl.TestDataLibID = tdd.TestDataLibID and tdd.Subdata='' ")
                .append("WHERE tdl.`name` LIKE ? ")
                .append(" and tdl.`system` = ? ")
                .append(" and tdl.`environment` = ? ")
                .append(" and tdl.`country` = ? ")
                .append(" order by tdl.`name` DESC, tdl.system DESC, tdl.environment DESC, tdl.country DESC, tdl.TestDataLibID ASC")
                .append(" limit 1").toString();

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL name : " + name);
            LOG.debug("SQL system : " + system);
            LOG.debug("SQL environment : " + environment);
            LOG.debug("SQL country : " + country);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setString(1, name);
            preStat.setString(2, ParameterParserUtil.returnEmptyStringIfNull(system));
            preStat.setString(3, ParameterParserUtil.returnEmptyStringIfNull(environment));
            preStat.setString(4, ParameterParserUtil.returnEmptyStringIfNull(country));
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadFromResultSet(resultSet);
                        msg = MessageEventUtil.createSelectSuccessMessageDAO(OBJECT_NAME);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);

                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();

            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();

        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }
        answer.setItem(result);
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerItem<TestDataLib> readByKey(int testDataLibID) {
        AnswerItem answer = new AnswerItem();
        MessageEvent msg;
        TestDataLib result;
        final String query = "SELECT * FROM testdatalib tdl "
                + " LEFT OUTER JOIN testdatalibdata tdd ON tdl.TestDataLibID = tdd.TestDataLibID and tdd.Subdata='' "
                + " WHERE tdl.`TestDataLibID` = ? ;";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        Connection connection = this.databaseSpring.connect();

        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setInt(1, testDataLibID);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadFromResultSet(resultSet);
                        msg = MessageEventUtil.createSelectSuccessMessageDAO(OBJECT_NAME);
                        //sets the object
                        answer.setItem(result);
                    } else {
                        msg = MessageEventUtil.createSelectExpectedErrorMessageDAO(OBJECT_NAME, "Check if the selected entry exists!");
                    }

                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }
        //sets the message
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerList readNameListByName(String testDataLibName, int limit) {
        AnswerList answer = new AnswerList();
        MessageEvent msg;
        List<String> namesList = new ArrayList<String>();

        StringBuilder query = new StringBuilder();
        query.append("SELECT distinct(`name`) ")
                .append("FROM testdatalib tdl ")
                .append(" WHERE `name` like ? ")
                .append(" order by `name`  ")
                .append(" limit ? ");

        if ((limit <= 0) || (limit >= MAX_ROW_SELECTED)) {
            limit = MAX_ROW_SELECTED;
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, "%" + testDataLibName + "%");
            preStat.setInt(2, limit);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {

                    while (resultSet.next()) {
                        String name = resultSet.getString("tdl.Name");
                        namesList.add(name);
                    }

                    if (namesList.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg = MessageEventUtil.createSelectSuccessMessageDAO(OBJECT_NAME);
                    }

                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
                    namesList.clear();
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }

        answer.setDataList(namesList);
        answer.setTotalRows(namesList.size());
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerList readAll() {
        AnswerList answer = new AnswerList();
        MessageEvent msg;

        List<TestDataLib> list = new ArrayList<TestDataLib>();
        final String query = "SELECT * FROM testdatalib tdl"
                + " LEFT OUTER JOIN testdatalibdata tdd ON tdl.TestDataLibID = tdd.TestDataLibID and tdd.Subdata=''; ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestDataLib>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadFromResultSet(resultSet));
                    }
                    if (list.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg = MessageEventUtil.createSelectSuccessMessageDAO(OBJECT_NAME);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
                    list.clear();

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }
        answer.setResultMessage(msg);
        answer.setDataList(list);
        answer.setTotalRows(list.size());
        return answer;
    }

    @Override
    public AnswerList readByVariousByCriteria(String name, String system, String environment, String country, String type, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {

        AnswerList answer = new AnswerList();
        MessageEvent msg;
        int nrTotalRows = 0;
        List<TestDataLib> objectList = new ArrayList<>();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM testdatalib tdl ");

        query.append("LEFT OUTER JOIN testdatalibdata tdd ON tdl.TestDataLibID=tdd.TestDataLibID and tdd.SubData='' ");

        searchSQL.append(" WHERE 1=1 ");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (tdl.`name` like ?");
            searchSQL.append(" or tdl.`group` like ?");
            searchSQL.append(" or tdl.`type` like ?");
            searchSQL.append(" or tdl.`database` like ?");
            searchSQL.append(" or tdl.`databaseUrl` like ?");
            searchSQL.append(" or tdl.`script` like ?");
            searchSQL.append(" or tdl.`servicepath` like ?");
            searchSQL.append(" or tdl.`method` like ?");
            searchSQL.append(" or tdl.`envelope` like ?");
            searchSQL.append(" or tdl.`databaseCsv` like ?");
            searchSQL.append(" or tdl.`csvUrl` like ?");
            searchSQL.append(" or tdl.`separator` like ?");
            searchSQL.append(" or tdl.`description` like ?");
            searchSQL.append(" or tdl.`system` like ?");
            searchSQL.append(" or tdl.`environment` like ?");
            searchSQL.append(" or tdl.`country` like ?) ");
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
        if (name != null) {
            searchSQL.append(" and tdl.`name` = ? ");
        }
        if (system != null) {
            searchSQL.append(" and tdl.`system` = ? ");
        }
        if (environment != null) {
            searchSQL.append(" and tdl.`environment` = ? ");
        }
        if (country != null) {
            searchSQL.append(" and tdl.`country` = ? ");
        }
        if (!StringUtil.isNullOrEmpty(type)) {
            searchSQL.append(" and tdl.`type` = ? ");
        }
        query.append(searchSQL);

        if (!StringUtil.isNullOrEmpty(column)) {
            query.append(" order by ").append(column).append(" ").append(dir);
        }

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount).append(" ");
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.name : " + name);
            LOG.debug("SQL.system : " + system);
            LOG.debug("SQL.environment : " + environment);
            LOG.debug("SQL.country : " + country);
            LOG.debug("SQL.type : " + type);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                if (!StringUtil.isNullOrEmpty(searchTerm)) {
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
                if (name != null) {
                    preStat.setString(i++, name);
                }
                if (system != null) {
                    preStat.setString(i++, system);
                }
                if (environment != null) {
                    preStat.setString(i++, environment);
                }
                if (country != null) {
                    preStat.setString(i++, country);
                }
                if (!StringUtil.isNullOrEmpty(type)) {
                    preStat.setString(i++, type);
                }
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        objectList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }
                    if (objectList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        MyLogger.log(TestDataLibDAO.class.getName(), Level.INFO, "Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                    } else if (objectList.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg = MessageEventUtil.createSelectSuccessMessageDAO(OBJECT_NAME);
                    }

                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
                    objectList.clear();
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }
        answer.setTotalRows(nrTotalRows);
        answer.setResultMessage(msg);
        answer.setDataList(objectList);
        return answer;
    }

    @Override
    public AnswerList readDistinctGroups() {
        AnswerList answerList = new AnswerList();
        ArrayList<String> listOfGroups = new ArrayList<String>();
        MessageEvent msg;
        String query = "SELECT distinct(`Group`) FROM testdatalib  WHERE `Group` <> '' ORDER BY `Group`";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        Connection connection = this.databaseSpring.connect();

        try {
            PreparedStatement preStat = connection.prepareStatement(query);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        listOfGroups.add(resultSet.getString(1));
                    }
                    if (listOfGroups.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg = MessageEventUtil.createSelectSuccessMessageDAO("GROUPS");
                    }

                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
                    listOfGroups.clear();
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }
        answerList.setTotalRows(listOfGroups.size());
        answerList.setDataList(listOfGroups);
        answerList.setResultMessage(msg);
        return answerList;
    }

    @Override
    public Answer create(TestDataLib testDataLib) {
        MessageEvent msg;
        Answer answer = new Answer();
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testdatalib (`name`, `system`, `environment`, `country`, `group`, `type`, `database`, "
                + "`script`, `databaseUrl`, `servicePath`, `method`, `envelope`, `databaseCsv`, `csvUrl`,`separator`, `description`, `creator`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString(), PreparedStatement.RETURN_GENERATED_KEYS);
            try {
                preStat.setString(1, testDataLib.getName());
                preStat.setString(2, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getSystem()));
                preStat.setString(3, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getEnvironment()));
                preStat.setString(4, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getCountry()));
                preStat.setString(5, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getGroup()));
                preStat.setString(6, testDataLib.getType());
                preStat.setString(7, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getDatabase()));
                preStat.setString(8, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getScript()));
                preStat.setString(9, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getDatabaseUrl()));
                preStat.setString(10, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getServicePath()));
                preStat.setString(11, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getMethod()));
                preStat.setString(12, testDataLib.getEnvelope()); //is the one that allows null values
                preStat.setString(13, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getDatabaseCsv()));
                preStat.setString(14, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getCsvUrl()));
                preStat.setString(15, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getSeparator()));
                preStat.setString(16, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getDescription()));
                preStat.setString(17, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getCreator()));

                preStat.executeUpdate();

                ResultSet keys = preStat.getGeneratedKeys();
                try {
                    if (keys != null && keys.next()) {
                        testDataLib.setTestDataLibID(keys.getInt(1)); //saves the returned key which will be used to save the subdata entries
                    }
                    msg = MessageEventUtil.createInsertSuccessMessageDAO(OBJECT_NAME);
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = MessageEventUtil.createInsertUnexpectedErrorMessageDAO();
                } finally {
                    if (keys != null) {
                        keys.close();
                    }
                }

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());

                if (exception.getSQLState().equals(SQL_DUPLICATED_CODE)) { //23000 is the sql state for duplicate entries
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_DUPLICATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "Test data lib ").replace("%OPERATION%", "INSERT").replace("%REASON%", exception.toString()));
                } else {
                    msg = MessageEventUtil.createInsertUnexpectedErrorMessageDAO();
                }

            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = MessageEventUtil.createInsertUnexpectedErrorMessageDAO();
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public Answer delete(TestDataLib testDataLib) {
        Answer ans = new Answer();
        MessageEvent msg;
        StringBuilder query = new StringBuilder();
        query.append("DELETE FROM testdatalib WHERE testdatalibid = ?");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setInt(1, testDataLib.getTestDataLibID());

                int rowsDeleted = preStat.executeUpdate();

                if (rowsDeleted == 0) {
                    //the delete statement didn't removed anything
                    msg = MessageEventUtil.createDeleteExpectedErrorMessageDAO(OBJECT_NAME, "Test data library wasn't deleted.");
                } else {
                    //everything went well
                    msg = MessageEventUtil.createDeleteSuccessMessageDAO(OBJECT_NAME);
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = MessageEventUtil.createDeleteUnexpectedErrorMessageDAO();

            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = MessageEventUtil.createDeleteUnexpectedErrorMessageDAO();
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public Answer update(TestDataLib testDataLib) {
        Answer answer = new Answer();
        MessageEvent msg;
        String query = "UPDATE testdatalib SET `type`=?, `group`= ?, `system`=?, `environment`=?, `country`=?, `database`= ? , `script`= ? , "
                + "`databaseUrl`= ? , `servicepath`= ? , `method`= ? , `envelope`= ? , `DatabaseCsv` = ? , `csvUrl` = ? ,`separator`= ?,  `description`= ? , `LastModifier`= ?, `LastModified` = NOW() WHERE "
                + "`TestDataLibID`= ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                //name is not editable
                preStat.setString(1, testDataLib.getType());
                preStat.setString(2, testDataLib.getGroup());
                preStat.setString(3, testDataLib.getSystem());
                preStat.setString(4, testDataLib.getEnvironment());
                preStat.setString(5, testDataLib.getCountry());
                preStat.setString(6, testDataLib.getDatabase());
                preStat.setString(7, testDataLib.getScript());
                preStat.setString(8, testDataLib.getDatabaseUrl());
                preStat.setString(9, testDataLib.getServicePath());
                preStat.setString(10, testDataLib.getMethod());
                preStat.setString(11, testDataLib.getEnvelope());
                preStat.setString(12, testDataLib.getDatabaseCsv());
                preStat.setString(13, testDataLib.getCsvUrl());
                preStat.setString(14, testDataLib.getSeparator());
                preStat.setString(15, testDataLib.getDescription());
                preStat.setString(16, testDataLib.getLastModifier());
                preStat.setInt(17, testDataLib.getTestDataLibID());

                int rowsUpdated = preStat.executeUpdate();

                if (rowsUpdated == 0) {
                    msg = MessageEventUtil.createUpdateExpectedErrorMessageDAO(OBJECT_NAME, " 0 Records updated.");
                } else {
                    msg = MessageEventUtil.createUpdateSuccessMessageDAO("Test data lib entry with name: " + testDataLib.getName());
                }

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                if (exception.getSQLState().equals(SQL_DUPLICATED_CODE)) { //23000 is the sql state for duplicate entries
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_DUPLICATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "Test data lib ").replace("%OPERATION%", "UPDATE").replace("%REASON%", exception.toString()));
                } else {
                    msg = MessageEventUtil.createUpdateUnexpectedErrorMessageDAO();
                }
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = MessageEventUtil.createUpdateUnexpectedErrorMessageDAO();

        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }

        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public TestDataLib loadFromResultSet(ResultSet resultSet) throws SQLException {
        Integer testDataLibID = resultSet.getInt("tdl.testDataLibID");
        String name = resultSet.getString("tdl.name");
        String system = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.system"));
        String environment = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.environment"));
        String country = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.country"));
        String group = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.group"));
        String type = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.type"));
        String database = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.database"));
        String script = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.script"));
        String databaseUrl = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.databaseUrl"));
        String servicePath = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.servicePath"));
        String method = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.method"));
        String envelope = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.envelope"));
        String databaseCsv = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.databaseCsv"));
        String csvUrl = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.csvUrl"));
        String separator = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.separator"));
        String description = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.description"));
        String creator = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.Creator"));
        Timestamp created = resultSet.getTimestamp("tdl.Created");
        String lastModifier = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdl.LastModifier"));
        Timestamp lastModified = resultSet.getTimestamp("tdl.LastModified");
        String subDataValue = null;
        String subDataColumn = null;
        String subDataParsingAnswer = null;
        String subDataColumnPosition = null;
        try {
            subDataValue = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdd.Value"));
            subDataColumn = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdd.Column"));
            subDataParsingAnswer = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdd.parsingAnswer"));
            subDataColumnPosition = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("tdd.columnPosition"));
        } catch (Exception ex) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.WARN, ex.toString());
        }

        return factoryTestDataLib.create(testDataLibID, name, system, environment, country, group, type, database, script, databaseUrl, servicePath,
                method, envelope, databaseCsv, csvUrl, separator,  description, creator, created, lastModifier, lastModified, subDataValue, subDataColumn, subDataParsingAnswer, subDataColumnPosition);
    }

    @Override
    public AnswerList<List<String>> readDistinctValuesByCriteria(String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        AnswerList answer = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<String>();

        StringBuilder query = new StringBuilder();

        query.append("SELECT distinct ");
        query.append(columnName);
        query.append(" as distinctValues FROM testdatalib tdl  ");
        query.append("LEFT OUTER JOIN testdatalibdata tdd ON tdl.TestDataLibID=tdd.TestDataLibID and tdd.SubData='' ");

        searchSQL.append("WHERE 1=1");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (tdl.`name` like ?");
            searchSQL.append(" or tdl.`group` like ?");
            searchSQL.append(" or tdl.`type` like ?");
            searchSQL.append(" or tdl.`database` like ?");
            searchSQL.append(" or tdl.`databaseUrl` like ?");
            searchSQL.append(" or tdl.`script` like ?");
            searchSQL.append(" or tdl.`servicepath` like ?");
            searchSQL.append(" or tdl.`method` like ?");
            searchSQL.append(" or tdl.`envelope` like ?");
            searchSQL.append(" or tdl.`csvUrl` like ?");
            searchSQL.append(" or tdl.`separator` like ?");
            searchSQL.append(" or tdl.`description` like ?");
            searchSQL.append(" or tdl.`system` like ?");
            searchSQL.append(" or tdl.`environment` like ?");
            searchSQL.append(" or tdl.`country` like ?) ");
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
        query.append(" order by ").append(columnName).append(" asc");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString())) {

            int i = 1;
            if (!Strings.isNullOrEmpty(searchTerm)) {
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

            ResultSet resultSet = preStat.executeQuery();

            //gets the data
            while (resultSet.next()) {
                distinctValues.add(resultSet.getString("distinctValues") == null ? "" : resultSet.getString("distinctValues"));
            }

            //get the total number of rows
            resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
            int nrTotalRows = 0;

            if (resultSet != null && resultSet.next()) {
                nrTotalRows = resultSet.getInt(1);
            }

            if (distinctValues.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                LOG.error("Partial Result in the query.");
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                answer = new AnswerList(distinctValues, nrTotalRows);
            } else if (distinctValues.size() <= 0) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                answer = new AnswerList(distinctValues, nrTotalRows);
            } else {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                answer = new AnswerList(distinctValues, nrTotalRows);
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
