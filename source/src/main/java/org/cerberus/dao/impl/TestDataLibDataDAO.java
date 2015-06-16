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
package org.cerberus.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List; 
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Level;
import org.cerberus.dao.ITestDataLibDataDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.TestDataLibData;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTestDataLibData;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.Answer; 
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author bcivel
 */
@Repository
public class TestDataLibDataDAO implements ITestDataLibDataDAO{

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestDataLibData factoryTestDataLibData;

    @Override
    public void createTestDataLibData(TestDataLibData testDataLibData) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testdatalibdata (`TestDataLibID`, `subData`, `value`, `column`, `parsinganswer`, `description`) ");
        query.append("VALUES (?,?,?,?,?,?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setInt(1, testDataLibData.getTestDataLibID());
                preStat.setString(2, ParameterParserUtil.returnEmptyStringIfNull(testDataLibData.getSubData()));
                preStat.setString(3, ParameterParserUtil.returnEmptyStringIfNull(testDataLibData.getValue()));
                preStat.setString(4, ParameterParserUtil.returnEmptyStringIfNull(testDataLibData.getColumn()));
                preStat.setString(5, ParameterParserUtil.returnEmptyStringIfNull(testDataLibData.getParsingAnswer()));
                preStat.setString(6, ParameterParserUtil.returnEmptyStringIfNull(testDataLibData.getDescription()));

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void updateTestDataLibData(TestDataLibData testDataLibData) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("update testdatalibdata set `value`= ?, `column`= ? , `parsinganswer`= ? , `description`= ? where `testdatalibID`= ? and `subdata`=? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testDataLibData.getValue());
                preStat.setString(2, testDataLibData.getColumn());
                preStat.setString(3, testDataLibData.getParsingAnswer());
                preStat.setString(4, testDataLibData.getDescription());
                preStat.setInt(5, testDataLibData.getTestDataLibID());
                preStat.setString(6, testDataLibData.getSubData());

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void deleteTestDataLibData(TestDataLibData testDataLibData) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("delete from testdatalibdata where `testdatalibID`=? and `subdata`=? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setInt(1, testDataLibData.getTestDataLibID());
                preStat.setString(2, ParameterParserUtil.returnEmptyStringIfNull(testDataLibData.getSubData()));

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public Answer deleteByTestDataLibID(int testDataLibID) throws CerberusException {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        String query = "delete from testdatalibdata where `testdatalibID`= ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setInt(1, testDataLibID);
                preStat.executeUpdate(); //as the testdatalib may not contain subdata entries, it is possible that this statement returs 0
                
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_EXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Lib Data").
                        replace("%OPERATION%", "DELETE").replace("%REASON%", ""));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to execute Delete"));
        } finally {
            this.databaseSpring.closeConnection();
        }
        return new Answer(msg);
    }

    @Override
    public TestDataLibData findTestDataLibDataByKey(Integer testDataLibID, String subData) throws CerberusException {
        TestDataLibData result = null;
        final String query = new StringBuilder("SELECT * FROM testdatalibdata where `testdatalibID`=?")
                .append(" and `subData` like ? ").toString();

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setInt(1, testDataLibID);
            preStat.setString(2, subData);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadTestDataLibDataFromResultSet(resultSet);
                    } else {
                        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }

    @Override
    public List<TestDataLibData> findAllTestDataLibData() {
        List<TestDataLibData> list = null;
        final String query = "SELECT * FROM testdatalibdata";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestDataLibData>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadTestDataLibDataFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public AnswerList findTestDataLibDataListByTestDataLib(Integer testDataLibID) {
        List<TestDataLibData> testDataLibListData = new ArrayList<TestDataLibData>();
        AnswerList answer = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testdatalibdata where `testDataLibID` = ? ;");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setInt(1, testDataLibID);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {

                    while (resultSet.next()) {
                        testDataLibListData.add(this.loadTestDataLibDataFromResultSet(resultSet));
                    }

                    answer = new AnswerList(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
                    answer.setDataList(testDataLibListData);
                    answer.setTotalRows(testDataLibListData.size());
                    //TODO:FN complete messages
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    //TODO:FN error message
                } finally {
                    resultSet.close();
                }
                
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                //TODO:FN error message
            } finally {
                preStat.close();
            }

        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            //TODO:FN error message
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, e.toString());
            }
        }
        return answer;        
    }

    @Override
    public List<TestDataLibData> findTestDataLibDataByCriteria(Integer testDataLibID, String subData, String value, String column, String parsingAnswer, String description) throws CerberusException {
        boolean throwEx = false;
        final StringBuffer query = new StringBuffer("SELECT * FROM testdatalibdata c WHERE 1=1 ");

        if (testDataLibID != null) {
            query.append(" AND c.testDataLibID = ?");
        }
        if (subData != null && !"".equals(subData.trim())) {
            query.append(" AND c.subData LIKE ?");
        }
        if (value != null && !"".equals(value.trim())) {
            query.append(" AND c.value LIKE ?");
        }
        if (column != null && !"".equals(column.trim())) {
            query.append(" AND c.column LIKE ?");
        }
        if (parsingAnswer != null && !"".equals(parsingAnswer.trim())) {
            query.append(" AND c.parsingAnswer LIKE ?");
        }
        if (description != null && !"".equals(description.trim())) {
            query.append(" AND c.description LIKE ?");
        }

        // " c.campaignID = ? AND c.campaign LIKE ? AND c.description LIKE ?";
        List<TestDataLibData> testDataLibData = new ArrayList<TestDataLibData>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            int index = 1;
            if (testDataLibID != null) {
                preStat.setInt(index, testDataLibID);
                index++;
            }
            if (subData != null && !"".equals(subData.trim())) {
                preStat.setString(index, "%" + subData.trim() + "%");
                index++;
            }
            if (value != null && !"".equals(value.trim())) {
                preStat.setString(index, "%" + value.trim() + "%");
                index++;
            }
            if (column != null && !"".equals(column.trim())) {
                preStat.setString(index, "%" + column.trim() + "%");
                index++;
            }
            if (parsingAnswer != null && !"".equals(parsingAnswer.trim())) {
                preStat.setString(index, "%" + parsingAnswer.trim() + "%");
                index++;
            }
            if (description != null && !"".equals(description.trim())) {
                preStat.setString(index, "%" + description.trim() + "%");
                index++;
            }

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testDataLibData.add(this.loadTestDataLibDataFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    testDataLibData = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                testDataLibData = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            testDataLibData = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return testDataLibData;
    }

    private TestDataLibData loadTestDataLibDataFromResultSet(ResultSet resultSet) throws SQLException {
        Integer testDataLibID = resultSet.getInt("TestDataLibID");
        String subData = resultSet.getString("SubData");
        String value = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("Value"));
        String column = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("Column"));
        String parsingAnswer = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("ParsingAnswer"));
        String description = resultSet.getString("Description");

        return factoryTestDataLibData.create(testDataLibID, subData, value, column, parsingAnswer, description);
    }

    @Override
    public Answer createTestDataLibDataBatch(List<TestDataLibData> subdataSet) throws CerberusException {
        //TODO:FN adicionar as mensagens
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testdatalibdata (`TestDataLibID`, `subData`, `value`, `column`, `parsinganswer`, `description`) ");
        query.append("VALUES (?,?,?,?,?,?)");
        MessageEvent rs = null;
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
                    preStat.setString(6, ParameterParserUtil.returnEmptyStringIfNull(subdata.getDescription()));
                    preStat.addBatch();
                }
                
                preStat.executeBatch();
                boolean someFailed = false;
                //TODO:FN check if the number of ids resultant match the total number of entries to insert
                
                if(someFailed == false){
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    //DATA_OPERATION_OK(001, "success", "%ITEM% was %OPERATION% with success!", false, false ,false , MessageGeneralEnum.DATA_OPERATION_SUCCESS);
                    rs.setDescription(rs.getDescription().replace("%ITEM%", "Test data lib").replace("%OPERATION%", "INSERT"));
                }else{
                    //TODO:FN ver quando devolvo outroe stado
                }
            } catch (SQLException exception) {
                rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "It was not possible to update table."));
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "It was not possible to update table."));
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            this.databaseSpring.closeConnection();
        }
        return new Answer(rs);
    }

    

    @Override
    public Answer updateTestDataLibDataBatch(ArrayList<TestDataLibData> entriesToUpdate) {
        StringBuilder query = new StringBuilder();
        query.append("update testdatalibdata set `value`= ?, `column`= ? , `parsinganswer`= ? , "
                + "`description`= ? where `testdatalibID`= ? and `subdata` LIKE ?  ");
        MessageEvent rs = null;
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                for (TestDataLibData subdata : entriesToUpdate) {
                    
                    preStat.setString(1, ParameterParserUtil.returnEmptyStringIfNull(subdata.getValue()));
                    preStat.setString(2, ParameterParserUtil.returnEmptyStringIfNull(subdata.getColumn()));
                    preStat.setString(3, ParameterParserUtil.returnEmptyStringIfNull(subdata.getParsingAnswer()));
                    preStat.setString(4, ParameterParserUtil.returnEmptyStringIfNull(subdata.getDescription()));
                    preStat.setInt(5, subdata.getTestDataLibID());
                    preStat.setString(6, ParameterParserUtil.returnEmptyStringIfNull(subdata.getSubData()));
                    preStat.addBatch();
                }
                
                preStat.executeBatch();
                boolean someFailed = false;
                //TODO:FN check if the number of ids resultant match the total number of entries to insert
                
                if(someFailed == false){
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    //DATA_OPERATION_OK(001, "success", "%ITEM% was %OPERATION% with success!", false, false ,false , MessageGeneralEnum.DATA_OPERATION_SUCCESS);
                    rs.setDescription(rs.getDescription().replace("%ITEM%", "Test data lib").replace("%OPERATION%", "UPDATE"));
                }else{
                    //TODO:FN ver quando devolvo outroe stado
                }
            } catch (SQLException exception) {
                rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "It was not possible to update table."));
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "It was not possible to update table."));
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            this.databaseSpring.closeConnection();
        }
        return new Answer(rs);
    }

    @Override
    public Answer deleteTestDataLibDataBatch(int testDataLibIdForData, ArrayList<String> entriesToRemove) {
        StringBuilder query = new StringBuilder();
        
        //query.append("delete from testdatalibdata where `testdatalibID`=? and `subdata` LIKE ? ");
        //don't delete properties that are currently being used 
        //by active test cases (working and full implemented)
        query.append("delete from testdatalibdata where testdataLibID = ? and ");
        query.append("`subdata` LIKE ? and `subdata` ");
        query.append("not in (select value2 from testcasecountryproperties tccp ");
        query.append("inner join testcase tc ");
        query.append("on tccp.Test = tc.Test and  ");
        query.append("tccp.TestCase = tc.TestCase  ");
        query.append("inner join testdatalib tdl ");
        query.append("on tdl.`name` = tccp.value1 and tdl.testdataLibID = ? and ");
        query.append("tccp.`type` like 'getFromDataLib' and  ");
        query.append("tc.TcActive = 'Y' and ");
        query.append("(tc.status like 'WORKING'  OR tc.status like 'FULLY_IMPLEMENTED' )) ");

        
        MessageEvent rs = null;
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                for (String subdata : entriesToRemove) {
                    preStat.setInt(1, testDataLibIdForData);
                    preStat.setString(2, ParameterParserUtil.returnEmptyStringIfNull(subdata));
                    preStat.setInt(3, testDataLibIdForData);
                    preStat.addBatch();
                }
                
                
                int result[] = preStat.executeBatch(); //TODO:FN verificar o que foi devolvido.
                
                //verify if some of the statements failed
                boolean someFailed = contains(result, 0) || contains(result, Statement.EXECUTE_FAILED);

                //TODO:FN check if the number of ids resultant match the total number of entries to insert
                
                if(someFailed == false){
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    //DATA_OPERATION_OK(001, "success", "%ITEM% was %OPERATION% with success!", false, false ,false , MessageGeneralEnum.DATA_OPERATION_SUCCESS);
                    rs.setDescription(rs.getDescription().replace("%ITEM%", "Test data lib").replace("%OPERATION%", "UPDATE"));
                }else{
                    //TODO:FN ver quando devolvo outroe stado
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_EXPECTED_ERROR);
                    
                    //DATA_OPERATION_EXPECTED_ERROR(901, "danger", "%ITEM% - %OPERATION% failed to complete. %REASON%", false, false ,false , MessageGeneralEnum.DATA_OPERATION_ERROR),
                    rs.setDescription(rs.getDescription().replace("%ITEM%", "Test data lib").replace("%OPERATION%", "DELETE").replace("%REASON%", "Some active test cases are using"
                            + " the subdata entries that you are trying to remove"));
                    
                }
            } catch (SQLException exception) {
                rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "It was not possible to update table."));
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "It was not possible to update table."));
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            this.databaseSpring.closeConnection();
        }
        return new Answer(rs);
    }
    //TODO:FN colocar isto num ficheiro de utilitario
    public boolean contains(final int[] array, final int key) {     
        return ArrayUtils.contains(array, key);
    }
}
