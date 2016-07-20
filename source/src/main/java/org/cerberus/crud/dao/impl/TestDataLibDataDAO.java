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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Level;
import org.cerberus.crud.dao.ITestDataLibDataDAO;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.crud.factory.IFactoryTestDataLibData;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
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
public class TestDataLibDataDAO implements ITestDataLibDataDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestDataLibData factoryTestDataLibData;
    private final String OBJECT_NAME = "Test Data Library - Sub data";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 10000000;

    @Override
    public AnswerItem readByKey(Integer testDataLibID, String subData) {
        AnswerItem answer = new AnswerItem();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);

        TestDataLibData result = null;
        final String query = "SELECT * FROM testdatalibdata where `testdatalibID`=? and `subData` like ? ";

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
                    MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();

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
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
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
                    MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();

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
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }
        answer.setResultMessage(msg);
        answer.setItem(result);
        return answer;
    }

    @Override
    public AnswerList<TestDataLibData> readByVarious(Integer testDataLibID, String columnEmpty, String parsingAnswerEmpty) {
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
                        msg = MessageEventUtil.createSelectSuccessMessageDAO(OBJECT_NAME);
                    }

                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
                    testDataLibListData.clear();

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
                testDataLibListData.clear();
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
            testDataLibListData.clear();
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
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
                        msg = MessageEventUtil.createSelectSuccessMessageDAO(OBJECT_NAME);
                    }

                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
                    list.clear();
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
                list.clear();
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
            list.clear();
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
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
                        MyLogger.log(TestDataLibDAO.class.getName(), Level.INFO, "Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                    } else if (testDataLibListData.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg = MessageEventUtil.createSelectSuccessMessageDAO(OBJECT_NAME);
                    }

                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
                    testDataLibListData.clear();
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
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
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
                        msg = MessageEventUtil.createSelectSuccessMessageDAO(OBJECT_NAME);
                    }

                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();

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
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }

        answer.setResultMessage(msg);
        answer.setDataList(testDataLibListData);
        answer.setTotalRows(testDataLibListData.size()); //all lines are retrieved 
        return answer;
    }

    @Override
    public Answer create(TestDataLibData testDataLibData) {
        Answer ans = new Answer();
        MessageEvent msg;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testdatalibdata (`TestDataLibID`, `subData`, `value`, `column`, `parsinganswer`, `columnPosition`,`description`) ");
        query.append("VALUES (?,?,?,?,?,?,?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setInt(1, testDataLibData.getTestDataLibID());
                preStat.setString(2, ParameterParserUtil.returnEmptyStringIfNull(testDataLibData.getSubData()));
                preStat.setString(3, ParameterParserUtil.returnEmptyStringIfNull(testDataLibData.getValue()));
                preStat.setString(4, ParameterParserUtil.returnEmptyStringIfNull(testDataLibData.getColumn()));
                preStat.setString(5, ParameterParserUtil.returnEmptyStringIfNull(testDataLibData.getParsingAnswer()));
                preStat.setString(6, ParameterParserUtil.returnEmptyStringIfNull(testDataLibData.getColumnPosition()));
                preStat.setString(7, ParameterParserUtil.returnEmptyStringIfNull(testDataLibData.getDescription()));

                preStat.executeUpdate();
                msg = MessageEventUtil.createInsertSuccessMessageDAO(OBJECT_NAME);

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = MessageEventUtil.createInsertUnexpectedErrorMessageDAO();
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = MessageEventUtil.createInsertUnexpectedErrorMessageDAO();
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }

        }
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public Answer create(List<TestDataLibData> subdataSet) {
        Answer answer = new Answer();
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testdatalibdata (`TestDataLibID`, `subData`, `value`, `column`, `parsinganswer`,`columnPosition`, `description`) ");
        query.append("VALUES (?,?,?,?,?,?,?)");
        MessageEvent msg;

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                for (TestDataLibData subdata : subdataSet) {
                    preStat.setInt(1, subdata.getTestDataLibID());
                    preStat.setString(2, ParameterParserUtil.returnEmptyStringIfNull(subdata.getSubData()));
                    preStat.setString(3, ParameterParserUtil.returnEmptyStringIfNull(subdata.getValue()));
                    preStat.setString(4, ParameterParserUtil.returnEmptyStringIfNull(subdata.getColumn()));
                    preStat.setString(5, ParameterParserUtil.returnEmptyStringIfNull(subdata.getParsingAnswer()));
                    preStat.setString(6, ParameterParserUtil.returnEmptyStringIfNull(subdata.getColumnPosition()));
                    preStat.setString(7, ParameterParserUtil.returnEmptyStringIfNull(subdata.getDescription()));
                    preStat.addBatch();
                }

                int affectedRows[] = preStat.executeBatch();
                //verify if some of the statements failed
                boolean someFailed = ArrayUtils.contains(affectedRows, 0) || ArrayUtils.contains(affectedRows, Statement.EXECUTE_FAILED);

                if (someFailed == false) {
                    msg = MessageEventUtil.createInsertSuccessMessageDAO(OBJECT_NAME);
                } else {
                    //some of the statements failed therefore we need to send a specific exception 
                    msg = MessageEventUtil.createInsertExpectedErrorMessageDAO(OBJECT_NAME,
                            "Some problem occurred while inserting the subdata entries - some failed to be inserted!");
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                if (exception.getSQLState().equals(SQL_DUPLICATED_CODE)) { //23000 is the sql state for duplicate entries
                    //specific messagem for duplicated issue
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_DUPLICATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "Insert "));
                } else {
                    msg = MessageEventUtil.createInsertUnexpectedErrorMessageDAO();

                }
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            msg = MessageEventUtil.createInsertUnexpectedErrorMessageDAO();
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            if (!this.databaseSpring.isOnTransaction()) {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException ex) {
                    MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
                }
            }
        }

        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public Answer update(TestDataLibData testDataLibData) {

        Answer answer = new Answer();
        MessageEvent msg;

        StringBuilder query = new StringBuilder();
        query.append("update testdatalibdata set `value`= ?, `column`= ? , `parsinganswer`= ? , `columnPosition`= ?, `description`= ? where "
                + "`testdatalibdataid`= ? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testDataLibData.getValue());
                preStat.setString(2, testDataLibData.getColumn());
                preStat.setString(3, testDataLibData.getParsingAnswer());
                preStat.setString(4, testDataLibData.getColumnPosition());
                preStat.setString(5, testDataLibData.getDescription());
                preStat.setInt(6, testDataLibData.getTestDataLibDataID());

                int totalRows = preStat.executeUpdate();

                if (totalRows == 0) {
                    msg = MessageEventUtil.createUpdateExpectedErrorMessageDAO(OBJECT_NAME, " Sub-data can't be updated");
                } else {
                    msg = MessageEventUtil.createUpdateSuccessMessageDAO(OBJECT_NAME);
                }

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = MessageEventUtil.createUpdateUnexpectedErrorMessageDAO();
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = MessageEventUtil.createUpdateUnexpectedErrorMessageDAO();
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public Answer update(ArrayList<TestDataLibData> entriesToUpdate) {
        Answer answer = new Answer();
        StringBuilder query = new StringBuilder();
        query.append("update testdatalibdata set `subdata` = ?, `value`= ?, `column`= ? , `parsinganswer`= ? , `columnPosition` = ? ,"
                + "`description`= ? where `testdatalibdataid`= ? ");
        //TODO:FN for now it is not being verified if the testdatalib is used by tests
        MessageEvent msg = null;

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                for (TestDataLibData subdata : entriesToUpdate) {
                    preStat.setString(1, ParameterParserUtil.returnEmptyStringIfNull(subdata.getSubData()));
                    preStat.setString(2, ParameterParserUtil.returnEmptyStringIfNull(subdata.getValue()));
                    preStat.setString(3, ParameterParserUtil.returnEmptyStringIfNull(subdata.getColumn()));
                    preStat.setString(4, ParameterParserUtil.returnEmptyStringIfNull(subdata.getParsingAnswer()));
                    preStat.setString(5, ParameterParserUtil.returnEmptyStringIfNull(subdata.getColumnPosition()));
                    preStat.setString(6, ParameterParserUtil.returnEmptyStringIfNull(subdata.getDescription()));
                    preStat.setInt(7, subdata.getTestDataLibDataID());

                    preStat.addBatch();
                }

                int affectedRows[] = preStat.executeBatch();
                boolean someFailed = ArrayUtils.contains(affectedRows, 0) || ArrayUtils.contains(affectedRows, Statement.EXECUTE_FAILED);

                if (someFailed == false) {
                    msg = MessageEventUtil.createUpdateSuccessMessageDAO(OBJECT_NAME);
                } else {
                    msg = MessageEventUtil.createUpdateExpectedErrorMessageDAO(OBJECT_NAME,
                            "Some problem occurred while updating the sub-data entries!");
                }
            } catch (SQLException exception) {
                msg = MessageEventUtil.createUpdateUnexpectedErrorMessageDAO();
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            msg = MessageEventUtil.createUpdateUnexpectedErrorMessageDAO();
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }

        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public Answer delete(TestDataLibData testDataLibData) {
        Answer ans = new Answer();
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();

        query.append("delete from testdatalibdata where `testdatalibdataid`=? ");
        //TODO:FN this delete should be analysed in order to avaoid delete sub-data entries that are being used by test cases

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setInt(1, testDataLibData.getTestDataLibDataID());

                int totalRows = preStat.executeUpdate();

                if (totalRows == 0) {
                    msg = MessageEventUtil.createDeleteExpectedErrorMessageDAO(OBJECT_NAME, " Sub-data can't be deleted.");
                } else {
                    msg = MessageEventUtil.createDeleteSuccessMessageDAO(OBJECT_NAME);
                }

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = MessageEventUtil.createDeleteUnexpectedErrorMessageDAO();
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = MessageEventUtil.createDeleteUnexpectedErrorMessageDAO();
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }

        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public Answer delete(TestDataLib testDataLib) {
        MessageEvent msg = null;
        String query = "delete from testdatalibdata where `testdatalibID`= ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setInt(1, testDataLib.getTestDataLibID());
                preStat.executeUpdate(); //as the testdatalib may not contain subdata entries, it is possible that this statement returs 0
                msg = MessageEventUtil.createDeleteSuccessMessageDAO(OBJECT_NAME);

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = MessageEventUtil.createDeleteUnexpectedErrorMessageDAO();
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = MessageEventUtil.createDeleteUnexpectedErrorMessageDAO();
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }
        return new Answer(msg);
    }

    @Override
    public Answer delete(List<TestDataLibData> subdataSet) {
        Answer answer = new Answer();
        MessageEvent msg;
        StringBuilder query = new StringBuilder();

        query.append("delete from testdatalibdata where testDataLibDataID = ? "); //TODO:FN for now it is not being verified if the testdatalib is used by tests

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                for (TestDataLibData subdata : subdataSet) {
                    preStat.setInt(1, subdata.getTestDataLibDataID());
                    preStat.addBatch();
                }
                //executes the query                
                int affectedRows[] = preStat.executeBatch();

                //verify if some of the statements failed
                boolean someFailed = ArrayUtils.contains(affectedRows, 0) || ArrayUtils.contains(affectedRows, Statement.EXECUTE_FAILED);

                if (someFailed == false) {
                    msg = MessageEventUtil.createDeleteSuccessMessageDAO(OBJECT_NAME);
                } else {
                    msg = MessageEventUtil.createDeleteExpectedErrorMessageDAO(OBJECT_NAME, "Some problem occurred while deleting the subdata entries! "
                            + "Please check if there are active test cases that are using the subdata entries that you are trying to delete!");
                }
            } catch (SQLException exception) {
                msg = MessageEventUtil.createDeleteUnexpectedErrorMessageDAO();
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            msg = MessageEventUtil.createDeleteUnexpectedErrorMessageDAO();
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }

        answer.setResultMessage(msg);
        return answer;
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
