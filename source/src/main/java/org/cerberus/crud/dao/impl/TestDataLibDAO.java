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
import org.cerberus.crud.dao.ITestDataLibDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum; 
import org.cerberus.crud.entity.TestDataLib; 
import org.cerberus.crud.factory.IFactoryTestDataLib;
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
public class TestDataLibDAO implements ITestDataLibDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestDataLib factoryTestDataLib;
    private final String OBJECT_NAME = "Test Data Library";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 10000000;
     
    @Override
    public Answer create(TestDataLib testDataLib) {
        MessageEvent msg;
        Answer answer = new Answer();
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testdatalib (`name`, `system`, `environment`, `country`, `group`, `type`, `database`, "
                + "`script`, `servicePath`, `method`, `envelope`, `description`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");

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
                preStat.setString(9, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getServicePath()));
                preStat.setString(10, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getMethod()));
                preStat.setString(11, testDataLib.getEnvelope()); //is the one that allows null values
                preStat.setString(12, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getDescription()));

                preStat.executeUpdate();
                
                ResultSet keys = preStat.getGeneratedKeys();
                try{
                    if(keys != null && keys.next()){
                        testDataLib.setTestDataLibID(keys.getInt(1)); //saves the returned key which will be used to save the subdata entries
                    }
                    msg = MessageEventUtil.createInsertSuccessMessageDAO(OBJECT_NAME);
                }catch (SQLException exception) {
                    MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                    msg = MessageEventUtil.createInsertUnexpectedErrorMessageDAO();
                }
                finally {
                    if(keys != null){
                        keys.close();
                    }
                }

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                
                if(exception.getSQLState().equals(SQL_DUPLICATED_CODE)){ //23000 is the sql state for duplicate entries
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_DUPLICATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "Test data lib ").replace("%OPERATION%", "INSERT"));                
                }else{
                    msg = MessageEventUtil.createInsertUnexpectedErrorMessageDAO();
                }
                
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            msg = MessageEventUtil.createInsertUnexpectedErrorMessageDAO();
        } finally {
            try {
                if(!this.databaseSpring.isOnTransaction()){
                    if(connection != null){
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
    public Answer update(TestDataLib testDataLib){
        Answer answer = new Answer();
        MessageEvent msg; 
        String query = "update testdatalib set `type`=?, `group`= ?, `system`=?, `environment`=?, `country`=?, `database`= ? , `script`= ? , "
                + "`servicepath`= ? , `method`= ? , `envelope`= ? , `description`= ? where "
                + "`TestDataLibID`= ?"; 
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
                preStat.setString(8, testDataLib.getServicePath());
                preStat.setString(9, testDataLib.getMethod());
                preStat.setString(10, testDataLib.getEnvelope());
                preStat.setString(11, testDataLib.getDescription());
                preStat.setInt(12, testDataLib.getTestDataLibID());

                int rowsUpdated= preStat.executeUpdate();
                
                if(rowsUpdated == 0){                                                            
                    msg = MessageEventUtil.createUpdateExpectedErrorMessageDAO(OBJECT_NAME,  " 0 Records updated.");
                }else{
                    msg = MessageEventUtil.createUpdateSuccessMessageDAO("Test data lib entry with name: " + testDataLib.getName());
                }
 
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                if(exception.getSQLState().equals(SQL_DUPLICATED_CODE)){ //23000 is the sql state for duplicate entries
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_DUPLICATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "Test data lib ").replace("%OPERATION%", "UPDATE"));                
                }else{
                    msg = MessageEventUtil.createUpdateUnexpectedErrorMessageDAO();
                }
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            msg = MessageEventUtil.createUpdateUnexpectedErrorMessageDAO();
            
        } finally {
            try {
                if(!this.databaseSpring.isOnTransaction()){
                    if(connection != null){
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
    public Answer delete(TestDataLib testDataLib){
        Answer ans = new Answer();
        MessageEvent msg;
        StringBuilder query = new StringBuilder();
        query.append("delete from testdatalib where `name`=? and `system`=? and `environment`=? and `country`=? ");
        
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testDataLib.getName());
                preStat.setString(2, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getSystem()));
                preStat.setString(3, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getEnvironment()));
                preStat.setString(4, ParameterParserUtil.returnEmptyStringIfNull(testDataLib.getCountry()));

                preStat.executeUpdate();
                
                int rowsDeleted = preStat.executeUpdate();                
                           
                if(rowsDeleted == 0){      
                    //the delete statement didn't removed anything
                    msg = MessageEventUtil.createDeleteExpectedErrorMessageDAO(OBJECT_NAME, "Test data library can't be deleted");
                }else{
                    //everything went well
                    msg = MessageEventUtil.createDeleteSuccessMessageDAO(OBJECT_NAME);
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                msg = MessageEventUtil.createDeleteUnexpectedErrorMessageDAO();
            
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            msg = MessageEventUtil.createDeleteUnexpectedErrorMessageDAO();
        } finally {
            try {
                if(!this.databaseSpring.isOnTransaction()){
                    if(connection != null){
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
    public Answer deleteUnused(int testDataLibID){
        Answer answer = new Answer();
        MessageEvent msg = null; 

        Connection connection = this.databaseSpring.connect();
        //removes the test datalib and and all the subdata
        PreparedStatement preStat = null;
        int testDataLibRows = 0 ;
        try {
             
            StringBuilder query = new StringBuilder();
            
            query.append("SELECT  "); 
            query.append("CASE tdl.country  "); 
            query.append("WHEN '' THEN   "); 
            query.append(" case (select count(*) from testcasecountryproperties tccp where tccp.value1 like tdl.`Name` and tccp.`type` LIKE 'getFromDataLib')  "); 
            query.append(" when 0 then (select count(*) from testcasecountryproperties tccp where tccp.country not in (select distinct(country)  "); 
            query.append(" from testdatalib where `name` like tdl.`Name`) and tccp.value1 = tdl.`Name` and tccp.`type` LIKE 'getFromDataLib') "); 
            query.append(" else (select count(distinct(country)) from testcasecountryproperties tccp where tccp.value1 like tdl.`Name` and tccp.`type` LIKE 'getFromDataLib')  "); 
            query.append("    end "); 
            query.append(" ELSE  "); 
            query.append(" case (select count(*) from testdatalib tdl2 where tdl2.`Name` = tdl.`Name` and tdl2.country like '')  "); 
            query.append(" when 0 then (select count(*) from testcasecountryproperties tccp where tccp.value1 like tdl.`Name` and tccp.country = tdl.country"
                    + " and tccp.`type` LIKE 'getFromDataLib') "); 
            query.append(" else 0 "); 
            query.append(" end  "); 
            query.append(" END as canDelete  "); 
            query.append(" FROM TestDataLib tdl "); 
            query.append(" where tdl.TestDataLibID=?"); 

            preStat = connection.prepareStatement(query.toString());
            //deletes the testdatalib
            preStat.setInt(1, testDataLibID); 
            //testDataLibRows = preStat.executeUpdate();
            ResultSet resultSet = preStat.executeQuery();
            int canDelete = -1;
            if(resultSet != null & resultSet.next()){
                canDelete = resultSet.getInt("canDelete");
            }
            //if canDelete is different from 0 then means that it can't be deleted
            if(canDelete != 0){
                msg = MessageEventUtil.createDeleteExpectedErrorMessageDAO(OBJECT_NAME, "0 rows deleted. Please check if the test data library is not being used.");                
            }else{
                
                preStat.clearParameters();
                query = new StringBuilder("Delete from testdatalib where testdatalibid = ?");
                preStat = connection.prepareStatement(query.toString());
                preStat.setInt(1, testDataLibID);
                testDataLibRows = preStat.executeUpdate();
                
                           
                if(testDataLibRows == 0){            
                    //as we don't want to proceed then we will close the transaction
                    //connection.rollback();
                    //if the the testdatalib can be removed then we don't try to remove 
                    msg = MessageEventUtil.createDeleteExpectedErrorMessageDAO(OBJECT_NAME, "The test data library can't be deleted.");                

                }else{                
                    //everything went well
                    msg = MessageEventUtil.createDeleteSuccessMessageDAO(OBJECT_NAME);
                }
            }
                    
        } catch (SQLException exception) {
            msg = MessageEventUtil.createDeleteUnexpectedErrorMessageDAO();
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if(preStat != null){
                    preStat.close();
                }
            
                if(!this.databaseSpring.isOnTransaction()){
                    if(connection != null){
                        connection.close();
                    }
                }
            
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerList<TestDataLib> readAll() {
        AnswerList answer = new AnswerList();
        MessageEvent msg;
                
        List<TestDataLib> list = new ArrayList<TestDataLib>();
        final String query = "SELECT * FROM testdatalib";

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
               
                    msg = MessageEventUtil.createSelectSuccessMessageDAO(OBJECT_NAME);
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                    msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
                    list.clear();
                    
                } finally {
                    if(resultSet != null){
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
        } finally {
            try {
                if(!this.databaseSpring.isOnTransaction()){
                    if(connection != null){
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
    public AnswerList<TestDataLib> readByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        
        AnswerList answer = new AnswerList();
        MessageEvent msg;
        int nrTotalRows = 0;
        List<TestDataLib> testDataLibList = new ArrayList<TestDataLib>();
        
        StringBuilder gSearch = new StringBuilder();
        StringBuilder searchSQL = new StringBuilder();
        
        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM testdatalib ");

        gSearch.append(" where (`name` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `group` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `type` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `database` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `script` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `servicepath` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `method` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `envelope` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `description` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `system` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `environment` like '%");
        gSearch.append(searchTerm);
        gSearch.append("%'");
        gSearch.append(" or `country` like '%");
        gSearch.append(searchTerm);
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
        query.append(column);
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
                        testDataLibList.add(this.loadFromResultSet(resultSet));
                    }
                    
                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                                        
                    if(resultSet != null && resultSet.next()){
                        nrTotalRows = resultSet.getInt(1);
                    }
                    if (testDataLibList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        MyLogger.log(TestDataLibDAO.class.getName(), Level.INFO, "Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                    } else {
                        msg = MessageEventUtil.createSelectSuccessMessageDAO(OBJECT_NAME);                    
                    }

                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                    msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
                    testDataLibList.clear();
                } finally {
                    if(resultSet != null){
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
        } finally {
            try {
                if(!this.databaseSpring.isOnTransaction()){
                    if(connection != null){
                        connection.close();
                    }
                }
            } catch (SQLException ex) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : " + ex.toString());
            }
        }
        answer.setTotalRows(nrTotalRows);
        answer.setResultMessage(msg);
        answer.setDataList(testDataLibList);
        return answer;
    }

    private TestDataLib loadFromResultSet(ResultSet resultSet) throws SQLException {
        Integer testDataLibID = resultSet.getInt("testDataLibID");
        String name = resultSet.getString("name");
        String system = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("system"));
        String environment = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("environment"));
        String country = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("country"));
        String group = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("group"));
        String type = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("type"));
        String database = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("database"));
        String script = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("script"));
        String servicePath = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("servicePath"));
        String method = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("method"));
        String envelope = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("envelope"));
        String description = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("description"));

        return factoryTestDataLib.create(testDataLibID, name, system, environment, country, group, type, database, script, servicePath, method, envelope, description);
    }

    @Override
    public AnswerItem<TestDataLib> readByKey(String name, String system, String environment, String country){
        AnswerItem answer = new AnswerItem();
        TestDataLib result = null;
        MessageEvent msg;
        
        final String query = new StringBuilder("SELECT * FROM testdatalib where `name` LIKE ?")
                .append(" and (`system` = ? or `system` = '')")
                .append(" and (`environment` = ? or `environment` = '')")
                .append(" and (`country` = ? or `country` = '')")
                .append(" order by `name` DESC, system DESC, environment DESC, country DESC")
                .append(" limit 1").toString();

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

                        //check if property is defined for other countries
                        String countQuery = "SELECT count(*) FROM testdatalib where `name` LIKE ?"; 
                        preStat = connection.prepareStatement(countQuery);
                        preStat.setString(1, name);
                        resultSet = preStat.executeQuery();
                        int nrTotalRows = 0;
                        if(resultSet != null && resultSet.next()){
                            nrTotalRows = resultSet.getInt(1);
                        }
                        
                        //specific messages
                        if(nrTotalRows > 0){
                            msg = new MessageEvent(MessageEventEnum.TESTDATALIB_NOT_FOUND_ERROR);
                            msg.setDescription(msg.getDescription().replace("%ITEM%", name).replace("%COUNTRY%", country).
                                    replace("%ENVIRONMENT%", environment).replace("%SYSTEM%", system));
                        }else{                         
                            //the specified library is not valid
                            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB);    
                            msg.setDescription(msg.getDescription().replace("%VALUE1%", name));
                        }  
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                    msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
                    
                } finally {
                    if(resultSet != null){
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
                    
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
                    
        } finally {
            try {
                if(!this.databaseSpring.isOnTransaction()){
                    if(connection != null){
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
        TestDataLib result = null;
        final String query = "SELECT * FROM testdatalib where `TestDataLibID` = ?";
        
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
                    MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to execute query"));
                    
                } finally {
                    if(resultSet != null){
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to execute query"));
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to execute query"));
        } finally {
            try {
                if(!this.databaseSpring.isOnTransaction()){
                    if(connection != null){
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
    public AnswerList<String> readDistinctGroups(){
        AnswerList<String> answerList = new AnswerList<String>();
        ArrayList<String> listOfGroups = new ArrayList<String>();
        MessageEvent msg;
        
        Connection connection = this.databaseSpring.connect();
        
        String query = "SELECT distinct(`Group`) FROM testdatalib  where `Group` <> '' order by `Group`";
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while(resultSet.next()) {
                        listOfGroups.add(resultSet.getString(1));
                    }                    
                    msg = MessageEventUtil.createSelectSuccessMessageDAO("GROUPS");                    
                    
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                    msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
                    listOfGroups.clear();
                } finally {
                    if(resultSet != null){
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
        } finally {
            try {
                if(!this.databaseSpring.isOnTransaction()){
                    if(connection != null){
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
    public Answer createBatch(List<TestDataLib> testDataLibEntries){
        Answer answer = new Answer();
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testdatalib (`name`, `system`, `environment`, `country`, `group`, `type`, `database`, "
                + "`script`, `servicePath`, `method`, `envelope`, `description`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString(), PreparedStatement.RETURN_GENERATED_KEYS);
            try {
                for(TestDataLib subdata: testDataLibEntries){
                    preStat.setString(1, subdata.getName());
                    preStat.setString(2, ParameterParserUtil.returnEmptyStringIfNull(subdata.getSystem()));
                    preStat.setString(3, ParameterParserUtil.returnEmptyStringIfNull(subdata.getEnvironment()));
                    preStat.setString(4, ParameterParserUtil.returnEmptyStringIfNull(subdata.getCountry()));
                    preStat.setString(5, ParameterParserUtil.returnEmptyStringIfNull(subdata.getGroup()));
                    preStat.setString(6, subdata.getType());
                    preStat.setString(7, ParameterParserUtil.returnEmptyStringIfNull(subdata.getDatabase()));
                    preStat.setString(8, ParameterParserUtil.returnEmptyStringIfNull(subdata.getScript()));
                    preStat.setString(9, ParameterParserUtil.returnEmptyStringIfNull(subdata.getServicePath()));
                    preStat.setString(10, ParameterParserUtil.returnEmptyStringIfNull(subdata.getMethod()));
                    preStat.setString(11, subdata.getEnvelope()); //is the one that allows null values
                    preStat.setString(12, ParameterParserUtil.returnEmptyStringIfNull(subdata.getDescription()));
                    preStat.addBatch();                        
                }                
                
                int affectedRows[] = preStat.executeBatch();
                
                boolean someFailed = ArrayUtils.contains(affectedRows, 0) || ArrayUtils.contains(affectedRows, Statement.EXECUTE_FAILED);                
                                
                if(someFailed == false){
                    ResultSet keys = preStat.getGeneratedKeys();
                    int i = 0;

                    if(keys != null){
                        while(keys.next()){
                            //gets the keys and associates them with the corresponding entries
                            testDataLibEntries.get(i).setTestDataLibID(keys.getInt(1)); //saves the returned key which will be used to save the subdata entries
                            i++;
                        }
                    } 
                    msg = MessageEventUtil.createInsertSuccessMessageDAO(OBJECT_NAME + " (" + testDataLibEntries.size() + " entries) ");                   
                }else{
                    //some of the statements failed therefore we need to send a specific exception 
                    msg = MessageEventUtil.createInsertExpectedErrorMessageDAO(OBJECT_NAME, "Some problem occurred while inserting the test data lib entries!");
                }
                
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                //23000 duplicated error code - http://dev.mysql.com/doc/refman/5.0/en/error-messages-server.html#error_er_dup_key                              
                if(exception.getSQLState().equals(SQL_DUPLICATED_CODE)){
                    //specific message for duplicated records
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_DUPLICATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));
                }else{
                    msg = MessageEventUtil.createInsertUnexpectedErrorMessageDAO();
                }
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            msg = MessageEventUtil.createInsertUnexpectedErrorMessageDAO();
        } finally {
            try {
                if(!this.databaseSpring.isOnTransaction()){
                    if(connection != null){
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
   
    /**
     * Searches for the testdatalib names that match (totally or partially) the name provided. Used by the autocomplete feature.
     * @param testDataLibName 
     * @param limit
     * @return 
     */
    @Override
    public AnswerList<TestDataLib> readByName(String testDataLibName, int limit) {
        AnswerList answer = new AnswerList();
        MessageEvent msg;
        List<String> namesList = new ArrayList<String>();
                
        StringBuilder query = new StringBuilder();
        query.append("select distinct(`name`) ");
        query.append("from testdatalib where `name` like ? ");
        query.append(" order by `name`  ");
        query.append(" limit ? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, "%" + testDataLibName +"%");
            preStat.setInt(2, limit);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {

                    while (resultSet.next()) {
                        String name = resultSet.getString("Name");                        
                        namesList.add(name);
                    }
                    msg = MessageEventUtil.createSelectSuccessMessageDAO(OBJECT_NAME);
                    
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
                    namesList.clear();
                } finally {
                    if(resultSet != null){
                        resultSet.close();
                    }
                }
                
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());            
            msg = MessageEventUtil.createSelectUnexpectedErrorMessageDAO();
        } finally {
           try {
                if(!this.databaseSpring.isOnTransaction()){
                    if(connection != null){
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
}
