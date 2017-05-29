/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.crud.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.cerberus.crud.dao.ITestDataLibDataDAO;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.crud.factory.IFactoryTestDataLibData;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author bcivel
 * @author FNogueira
 */
@Repository
public class TestDataLibDataDAO implements ITestDataLibDataDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestDataLibData factoryTestDataLibData;

    private static final Logger LOG = Logger.getLogger(TestDataLibDataDAO.class);

    private final String OBJECT_NAME = "Test Data Library - Sub data";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 10000000;

    @Override
    public AnswerItem readByKey(Integer testDataLibID, String subData) {
        AnswerItem answer = new AnswerItem();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);

        TestDataLibData result = null;
        final String query = "SELECT * FROM testdatalibdata where `testdatalibID`=? and `subData` like ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.testDataLibID : " + testDataLibID);
            LOG.debug("SQL.param.subData : " + subData);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setInt(1, testDataLibID);
            preStat.setString(2, subData);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadFromResultSet(resultSet);
                    } else {
                        //specific message for gefromdatalib
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
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
            } catch (SQLException ex) {
                LOG.warn("Unable to close connection : " + ex.toString());
            }
        }
        answer.setResultMessage(msg);
        answer.setItem(result);
        return answer;
    }

    @Override
    public AnswerItem readByKeyTech(Integer testDataLibDataID) {
        AnswerItem answer = new AnswerItem();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);

        TestDataLibData result = null;
        final String query = "SELECT * FROM testdatalibdata where `testdatalibdataid`=? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.testDataLibDataID : " + testDataLibDataID);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setInt(1, testDataLibDataID);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadFromResultSet(resultSet);
                    } else {
                        //specific message for gefromdatalib
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
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
            } catch (SQLException ex) {
                LOG.warn("Unable to close connection : " + ex.toString());
            }
        }
        answer.setResultMessage(msg);
        answer.setItem(result);
        return answer;
    }

    @Override
    public AnswerList<TestDataLibData> readByVarious(Integer testDataLibID, String columnEmpty, String parsingAnswerEmpty, String columnPositionEmpty) {
        List<TestDataLibData> testDataLibListData = new ArrayList<TestDataLibData>();
        AnswerList answer = new AnswerList();
        MessageEvent msg;
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testdatalibdata where `testDataLibID` = ? ");

        if ("Y".equalsIgnoreCase(columnEmpty)) {
            query.append(" and `Column`='' ");
        } else if ("N".equalsIgnoreCase(columnEmpty)) {
            query.append(" and `Column`!='' ");
        }
        if ("Y".equalsIgnoreCase(parsingAnswerEmpty)) {
            query.append(" and `ParsingAnswer`='' ");
        } else if ("N".equalsIgnoreCase(parsingAnswerEmpty)) {
            query.append(" and `ParsingAnswer`!='' ");
        }
        if ("Y".equalsIgnoreCase(columnPositionEmpty)) {
            query.append(" and `columnPosition`='' ");
        } else if ("N".equalsIgnoreCase(columnPositionEmpty)) {
            query.append(" and `columnPosition`!='' ");
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.testDataLibID : " + testDataLibID);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setInt(1, testDataLibID);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {

                    while (resultSet.next()) {
                        testDataLibListData.add(this.loadFromResultSet(resultSet));
                    }

                    if (testDataLibListData.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    }

                } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                    testDataLibListData.clear();

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                testDataLibListData.clear();
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            testDataLibListData.clear();
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                LOG.warn("Unable to close connection : " + ex.toString());
            }
        }

        answer.setDataList(testDataLibListData);
        answer.setTotalRows(testDataLibListData.size());
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerList<TestDataLibData> readAll() {
        AnswerList answerList = new AnswerList();
        List<TestDataLibData> list = new ArrayList<TestDataLibData>();
        MessageEvent msg;
        final String query = "SELECT * FROM testdatalibdata";

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
                        list.add(this.loadFromResultSet(resultSet));
                    }
                    if (list.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    }

                } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                    list.clear();
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                list.clear();
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            list.clear();
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                LOG.warn("Unable to close connection : " + ex.toString());
            }
        }
        answerList.setDataList(list);
        answerList.setTotalRows(list.size());
        answerList.setResultMessage(msg);
        return answerList;
    }

    @Override
    public AnswerList readByCriteria(int start, int amount, String colName, String dir, String searchTerm, String individualSearch) {
        AnswerList answer = new AnswerList();
        MessageEvent msg;
        int nrTotalRows = 0;
        List<TestDataLibData> testDataLibListData = new ArrayList<TestDataLibData>();

        StringBuilder gSearch = new StringBuilder();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM testdatalibdata ");

        gSearch.append(" where (`subdata` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `value` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `column` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `parsinganswer` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `columnPosition` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `description` like '%");
        gSearch.append("%') ");

        if (!searchTerm.equals("") && !individualSearch.equals("")) {
            searchSQL.append(gSearch.toString());
            searchSQL.append(" and ");
            searchSQL.append(individualSearch);
        } else if (!individualSearch.equals("")) {
            searchSQL.append(" where `");
            searchSQL.append(individualSearch);
            searchSQL.append("`");
        } else if (!searchTerm.equals("")) {
            searchSQL.append(gSearch.toString());
        }

        query.append(searchSQL);
        query.append("order by `");
        query.append(colName);
        query.append("` ");
        query.append(dir);

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount).append(" ");
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
                    //gets the data
                    while (resultSet.next()) {
                        testDataLibListData.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }
                    if (testDataLibListData.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.warn("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                    } else if (testDataLibListData.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    }

                } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                    testDataLibListData.clear();
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
            } catch (SQLException ex) {
                LOG.warn("Unable to close connection : " + ex.toString());
            }
        }
        answer.setTotalRows(nrTotalRows);
        answer.setResultMessage(msg);
        answer.setDataList(testDataLibListData);
        return answer;
    }

    @Override
    public AnswerList readByName(String testDataLibName) {
        AnswerList answer = new AnswerList();
        MessageEvent msg;
        List<TestDataLibData> testDataLibListData = new ArrayList<TestDataLibData>();

        StringBuilder query = new StringBuilder();
        query.append("SELECT tdld.*, tdl.`name`, tdl.type, tdl.system, tdl.country, tdl.environment FROM testdatalibdata tdld ");
        query.append("inner join testdatalib tdl ");
        query.append("on tdld.testDataLibID = tdl.testDataLibID ");
        query.append("and tdl.`name` LIKE ? ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.testDataLibName : " + testDataLibName);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, testDataLibName);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testDataLibListData.add(this.loadFromResultSet(resultSet));
                    }
                    if (testDataLibListData.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
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
            } catch (SQLException ex) {
                LOG.warn("Unable to close connection : " + ex.toString());
            }
        }

        answer.setResultMessage(msg);
        answer.setDataList(testDataLibListData);
        answer.setTotalRows(testDataLibListData.size()); //all lines are retrieved 
        return answer;
    }

    @Override
    public Answer create(TestDataLibData object) {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testdatalibdata (`TestDataLibID`, `subData`, `value`, `column`, `parsinganswer`, `columnPosition`,`description`) ");
        query.append("VALUES (?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param.TestDataLibID : " + object.getTestDataLibID());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setInt(1, object.getTestDataLibID());
                preStat.setString(2, ParameterParserUtil.returnEmptyStringIfNull(object.getSubData()));
                preStat.setString(3, ParameterParserUtil.returnEmptyStringIfNull(object.getValue()));
                preStat.setString(4, ParameterParserUtil.returnEmptyStringIfNull(object.getColumn()));
                preStat.setString(5, ParameterParserUtil.returnEmptyStringIfNull(object.getParsingAnswer()));
                preStat.setString(6, ParameterParserUtil.returnEmptyStringIfNull(object.getColumnPosition()));
                preStat.setString(7, ParameterParserUtil.returnEmptyStringIfNull(object.getDescription()));

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
    public Answer update(TestDataLibData object) {
        MessageEvent msg = null;
        final String query = "UPDATE testdatalibdata SET `value`= ? , `column`= ? , `parsinganswer`= ? , `columnPosition`= ? , `description`= ? WHERE `testdatalibdataid`= ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, object.getValue());
                preStat.setString(2, object.getColumn());
                preStat.setString(3, object.getParsingAnswer());
                preStat.setString(4, object.getColumnPosition());
                preStat.setString(5, object.getDescription());
                preStat.setInt(6, object.getTestDataLibDataID());

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
    public Answer delete(TestDataLibData object) {
        MessageEvent msg = null;
        final String query = "DELETE FROM testdatalibdata WHERE `testdatalibdataid`= ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
            LOG.debug("SQL.param.TestDataLibDataID : " + object.getTestDataLibDataID());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setInt(1, object.getTestDataLibDataID());

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

    private TestDataLibData loadFromResultSet(ResultSet resultSet) throws SQLException {
        Integer testDataLibDataID = resultSet.getInt("TestDataLibDataID");
        Integer testDataLibID = resultSet.getInt("TestDataLibID");
        String subData = resultSet.getString("SubData");
        String value = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("Value"));
        String column = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("Column"));
        String parsingAnswer = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("ParsingAnswer"));
        String columnPosition = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("columnPosition"));
        String description = resultSet.getString("Description");

        return factoryTestDataLibData.create(testDataLibDataID, testDataLibID, subData, value, column, parsingAnswer, columnPosition, description);
    }
}
