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
import org.cerberus.dto.TestDataLibDataDTO;
import org.cerberus.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.entity.TestDataLibData;
import org.cerberus.entity.TestDataLibDataUpdate;
import org.cerberus.enums.TestDataLibTypeEnum;
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
 * @author FNogueira
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
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {            
            try {
                if(!this.databaseSpring.isOnTransaction()){
                    if(connection != null){
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
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
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if(!this.databaseSpring.isOnTransaction()){
                    if(connection != null){
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
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
        //TODO:FN ver este delete
            
        
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setInt(1, testDataLibData.getTestDataLibID());
                preStat.setString(2, ParameterParserUtil.returnEmptyStringIfNull(testDataLibData.getSubData()));
                preStat.setInt(3, testDataLibData.getTestDataLibID());
                
                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if(!this.databaseSpring.isOnTransaction()){
                    if(connection != null){
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public Answer deleteByTestDataLibID(int testDataLibID) {
        MessageEvent msg = null;
        String query = "delete from testdatalibdata where `testdatalibID`= ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setInt(1, testDataLibID);
                preStat.executeUpdate(); //as the testdatalib may not contain subdata entries, it is possible that this statement returs 0
                
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Lib ").
                        replace("%OPERATION%", "DELETE").replace("%OPERATION%", "Delete"));
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_EXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Lib Data").
                        replace("%OPERATION%", "DELETE").replace("%REASON%", ""));
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to execute Delete"));
        } finally {
            try {
                if(!this.databaseSpring.isOnTransaction()){
                    if(connection != null){
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
                    if(resultSet != null){
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
           try {
                if(!this.databaseSpring.isOnTransaction()){
                    if(connection != null){
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
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
                    if(resultSet != null){
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if(!this.databaseSpring.isOnTransaction()){
                    if(connection != null){
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }
        return list;
    }

    @Override
    public AnswerList findTestDataLibDataListByID(Integer testDataLibID) {
        List<TestDataLibData> testDataLibListData = new ArrayList<TestDataLibData>();
        AnswerList answer = new AnswerList();
        MessageEvent rs = null;
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

                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    rs.setDescription(rs.getDescription().replace("%ITEM%", "Subdata entries").replace("%OPERATION%", "SELECT by ID"));

                    answer.setDataList(testDataLibListData);
                    answer.setTotalRows(testDataLibListData.size());
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());                    
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "It was not possible to select table."));
                    
                } finally {
                    if(resultSet != null){
                        resultSet.close();
                    }
                }
                
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "It was not possible to select table."));
                    
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "It was not possible to select table."));
        } finally {
            try {
                if(!this.databaseSpring.isOnTransaction()){
                    if(connection != null){
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }
        
        answer.setResultMessage(rs);
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
                    if(resultSet != null){
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                testDataLibData = null;
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            testDataLibData = null;
        } finally {
            try {
                if(!this.databaseSpring.isOnTransaction()){
                    if(connection != null){
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
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
    public Answer createTestDataLibDataBatch(List<TestDataLibData> subdataSet) {
        
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testdatalibdata (`TestDataLibID`, `subData`, `value`, `column`, `parsinganswer`, `description`) ");
        query.append("VALUES (?,?,?,?,?,?)");
        MessageEvent rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        
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
                
                int affectedRows[] = preStat.executeBatch();
                //verify if some of the statements failed
                boolean someFailed = ArrayUtils.contains(affectedRows, 0) || ArrayUtils.contains(affectedRows, Statement.EXECUTE_FAILED);                
                                
                if(someFailed == false){
                    rs.setDescription(rs.getDescription().replace("%ITEM%", "Subdata entries ").replace("%OPERATION%", "INSERT"));
                }else{
                    //some of the statements failed therefore we need to send a specific exception 
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_EXPECTED_ERROR);
                    rs.setDescription(rs.getDescription().replace("%ITEM%", "Subdata entries ").replace("%OPERATION%", "INSERT").
                            replace("%REASON%", "Some problem occurred while inserting the subdata entries - some failed to be inserted!"));
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                if(exception.getSQLState().equals("23000")){ //23000 is the sql state for duplicate entries
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_DUPLICATE_ERROR);
                    rs.setDescription(rs.getDescription().replace("%ITEM%", "Test data lib data ").replace("%OPERATION%", "INSERT"));                
                }else{
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "It was not possible to update table."));
                }
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "It was not possible to update table."));
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            if(!this.databaseSpring.isOnTransaction()){
                try {
                    if(connection != null){
                        connection.close();
                    }
                } catch (SQLException ex) {
                    MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
                }
            }
        }
        return new Answer(rs);
    }

    

    @Override
    public Answer updateTestDataLibDataBatch(ArrayList<TestDataLibDataUpdate> entriesToUpdate) {
        StringBuilder query = new StringBuilder();
        query.append("update testdatalibdata set `subdata` = ?, `value`= ?, `column`= ? , `parsinganswer`= ? , "
                + "`description`= ? where `testdatalibID`= ? and `subdata` LIKE ?  ");
        //TODO:FN for now it is not being verified if the testdatalib is used by tests
        MessageEvent rs = null;
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                for (TestDataLibDataUpdate subdataUpdate : entriesToUpdate) { 
                    TestDataLibData subdata = subdataUpdate.getModifiedObject();
                    preStat.setString(1, ParameterParserUtil.returnEmptyStringIfNull(subdata.getSubData()));
                    preStat.setString(2, ParameterParserUtil.returnEmptyStringIfNull(subdata.getValue()));
                    preStat.setString(3, ParameterParserUtil.returnEmptyStringIfNull(subdata.getColumn()));
                    preStat.setString(4, ParameterParserUtil.returnEmptyStringIfNull(subdata.getParsingAnswer()));
                    preStat.setString(5, ParameterParserUtil.returnEmptyStringIfNull(subdata.getDescription()));
                    preStat.setInt(6, subdata.getTestDataLibID());
                    preStat.setString(7, ParameterParserUtil.returnEmptyStringIfNull(subdataUpdate.getSubDataOriginalKey()));
                    preStat.addBatch();
                }
                
                int affectedRows[] = preStat.executeBatch();
                boolean someFailed = ArrayUtils.contains(affectedRows, 0) || ArrayUtils.contains(affectedRows, Statement.EXECUTE_FAILED);   
                
                if(someFailed == false){
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    rs.setDescription(rs.getDescription().replace("%ITEM%", "Sub-data entries ").replace("%OPERATION%", "UPDATE"));
                }else{
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_EXPECTED_ERROR);
                    rs.setDescription(rs.getDescription().replace("%ITEM%", "Sub-data entries ").replace("%OPERATION%", "UPDATE").
                            replace("%REASON%", "Some problem occurred while updating the subdata entries!"));
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
            try {
                if(!this.databaseSpring.isOnTransaction()){
                    if(connection != null){
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }
        return new Answer(rs);
    }

    @Override
    public Answer deleteTestDataLibDataBatch(int testDataLibIdForData, ArrayList<String> entriesToRemove) {
        StringBuilder query = new StringBuilder();
        
        query.append("delete from testdatalibdata where testdataLibID = ? and ");
        query.append("`subdata` LIKE ? "); //TODO:FN for now it is not being verified if the testdatalib is used by tests
        
        MessageEvent rs = null;
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                for (String subdata : entriesToRemove) {
                    preStat.setInt(1, testDataLibIdForData);
                    preStat.setString(2, ParameterParserUtil.returnEmptyStringIfNull(subdata));
                    preStat.addBatch();
                }
                //executes the query                
                int affectedRows[] = preStat.executeBatch(); 
                
                //verify if some of the statements failed
                boolean someFailed = ArrayUtils.contains(affectedRows, 0) || ArrayUtils.contains(affectedRows, Statement.EXECUTE_FAILED);

                if(someFailed == false){
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    rs.setDescription(rs.getDescription().replace("%ITEM%", "Test data library").replace("%OPERATION%", "UPDATE"));
                }else{
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_EXPECTED_ERROR);
                    rs.setDescription(rs.getDescription().replace("%ITEM%", "Test data library").replace("%OPERATION%", "DELETE").
                            replace("%REASON%", "Some problem occurred while deleting the subdata entries! Please check if there are active test cases that are using"
                            + " the subdata entries that you are trying to delete!"));                    
                }
            } catch (SQLException exception) {
                rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "It was not possible to update table."));
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "It was not possible to update table."));
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if(!this.databaseSpring.isOnTransaction()){
                    if(connection != null){
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }
        return new Answer(rs);
    }
   

    @Override
    public AnswerList findTestDataLibDataByName(String testDataLibName) {
        List<TestDataLibDataDTO> dtoList = new ArrayList<TestDataLibDataDTO>();
        MessageEvent  rs = null;
        AnswerList answer = new AnswerList();
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
                        TestDataLibDataDTO dto = new TestDataLibDataDTO();
                        //data from testdatalib table
                        dto.setTestDataLibId(resultSet.getInt("TestDataLibID"));
                        dto.setName(resultSet.getString("Name"));
                        dto.setType(resultSet.getString("type"));
                        //system + environment + country
                        dto.setSystem(ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("system")));
                        dto.setEnvironment(ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("environment")));
                        dto.setCountry(ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("country")));
                        
                        //specific data
                        dto.setSubdata(resultSet.getString("SubData"));
                        if(dto.getType().equalsIgnoreCase(TestDataLibTypeEnum.STATIC.toString())){
                            dto.setData(ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("Value")));                            
                        }if(dto.getType().equalsIgnoreCase(TestDataLibTypeEnum.SQL.toString())){
                            dto.setData(ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("Column")));                            
                        }else if(dto.getType().equalsIgnoreCase(TestDataLibTypeEnum.SOAP.toString())){ 
                            dto.setData(ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("ParsingAnswer")));  
                        }
                        dto.setDescription(ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("Description")));

                        dtoList.add(dto);
                    }

                   
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    rs.setDescription(rs.getDescription().replace("%ITEM%", "Subdata list ").replace("%OPERATION%", "SELECT"));
                    
                    answer.setDataList(dtoList);
                    answer.setTotalRows(dtoList.size()); //all lines are retrieved 
                    
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "It was not possible to retrieve the data."));
                    
                } finally {
                    if(resultSet != null){
                        resultSet.close();
                    }
                }
                
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "It was not possible to retrieve the data."));
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "It was not possible to retrieve the data."));
        } finally {
            try {
                if(!this.databaseSpring.isOnTransaction()){
                    if(connection != null){
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }
        
        answer.setResultMessage(rs);
        return answer;     
    }

    @Override
    public AnswerList findTestDataLibSubData(String testDataLib, String nameToSearch, int limit) {
        List<String> subDataList = new ArrayList<String>();
        AnswerList answer = new AnswerList();
        MessageEvent  rs = null;
        StringBuilder query = new StringBuilder();
        query.append("select SQL_CALC_FOUND_ROWS distinct(`subdata`) ");
        query.append("from testdatalibdata tdld ");
        query.append(" inner join testdatalib tdl on ");
        query.append(" tdld.TestDataLibID = tdl.TestDataLibID ");
        query.append(" where tdld.`subdata`  like ? and ");
        query.append(" tdl.`name` like ? ");
        query.append(" limit ? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, "%" + nameToSearch +"%");
            preStat.setString(2, testDataLib);
            preStat.setInt(3, limit);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {

                    while (resultSet.next()) {
                        String name = resultSet.getString("Subdata");                        
                        subDataList.add(name);
                    }
                   
                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;
                    
                    if(resultSet != null && resultSet.next()){
                        nrTotalRows = resultSet.getInt(1);
                    }
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    rs.setDescription(rs.getDescription().replace("%ITEM%", "Subdata list ").replace("%OPERATION%", "SELECT"));
                    
                    answer.setResultMessage(rs);
                    answer.setDataList(subDataList);
                    answer.setTotalRows(nrTotalRows); 
                    
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "It was not possible to retrieve the data."));

                } finally {
                    if(resultSet != null){
                        resultSet.close();
                    }
                }
                
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "It was not possible to retrieve the data."));            
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "It was not possible to retrieve the data."));
            
        } finally {
            try {
                if(!this.databaseSpring.isOnTransaction()){
                    if(connection != null){
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }
        return answer;     
    }
    
    
    
}
