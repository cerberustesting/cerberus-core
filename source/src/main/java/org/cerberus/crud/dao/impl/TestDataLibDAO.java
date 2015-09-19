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
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryTestDataLib;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil; 
import org.cerberus.util.answer.Answer; 
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author bcivel
 */
@Repository
public class TestDataLibDAO implements ITestDataLibDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestDataLib factoryTestDataLib;

    private final String SQL_DUPLICATED_CODE = "23000";
    @Override
    public Answer createTestDataLib(TestDataLib testDataLib) {
        MessageEvent msg = null;
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
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Lib").replace("%OPERATION%", "INSERT"));
                }catch (SQLException exception) {
                    MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to execute query - INSERT Test data lib"));
                }
                finally {
                    if(keys != null){
                        keys.close();
                    }
                }

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                
                if(exception.getSQLState().equals(SQL_DUPLICATED_CODE)){ //23000 is the sql state for duplicate entries
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_DUPLICATE_ERROR);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "Test data lib ").replace("%OPERATION%", "INSERT"));                
                }else{
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR); 
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to update data!"));
                }
                
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to execute query - INSERT Test data lib"));
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
        return new Answer(msg);
    }

    @Override
    public Answer updateTestDataLib(TestDataLib testDataLib){
        
        MessageEvent rs = null; 
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
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_EXPECTED_ERROR);
                    rs.setDescription(rs.getDescription().replace("%REASON%", " 0 Records updated."));
                }else{
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);                    
                }
                
                rs.setDescription(rs.getDescription().replace("%ITEM%", "Test data lib entry with name: " + testDataLib.getName()).replace("%OPERATION%", "UPDATED"));
 
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                if(exception.getSQLState().equals(SQL_DUPLICATED_CODE)){ //23000 is the sql state for duplicate entries
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_DUPLICATE_ERROR);
                    rs.setDescription(rs.getDescription().replace("%ITEM%", "Test data lib ").replace("%OPERATION%", "UPDATE"));                
                }else{
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR); 
                    rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "Unable to update data!"));
                }
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR); 
            rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "Unable to update data!"));
            
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
        
        
        return new Answer(rs);
    }

    @Override
    public void deleteTestDataLib(TestDataLib testDataLib) throws CerberusException {
        boolean throwExcep = false;
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
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
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
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }
    
    @Override 
    public Answer deleteUnusedTestDataLib(int testDataLibID){
        
        MessageEvent rs = null; 

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
                rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_EXPECTED_ERROR);
                rs.setDescription(rs.getDescription().replace("%ITEM%", "Test Data Lib").replace("%OPERATION%", "DELETE")
                        .replace("%REASON%", "0 rows deleted. Please check if the test data lib is not being used."));
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
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "Test data lib can't be deleted."));           

                }else{                
                    //everything went well
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    rs.setDescription(rs.getDescription().replace("%ITEM%", "Test data lib").replace("%OPERATION%", "DELETE"));
                }
            }
                    
        } catch (SQLException exception) {
            rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "Test data lib can't be deleted."));            
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
        return new Answer(rs);
    }

    @Override
    public List<TestDataLib> findAllTestDataLib() {
        List<TestDataLib> list = null;
        final String query = "SELECT * FROM testdatalib";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestDataLib>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadTestDataLibFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    if(resultSet != null){
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
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
        return list;
    }

    @Override
    public AnswerList findTestDataLibListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        
        AnswerList response = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
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
        gSearch.append("%')");

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
        query.append(" limit ");
        query.append(start);
        query.append(" , ");
        query.append(amount);

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        testDataLibList.add(this.loadTestDataLibFromResultSet(resultSet));
                    }
                    
                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;
                    
                    if(resultSet != null && resultSet.next()){
                        nrTotalRows = resultSet.getInt(1);
                    }
                    
                    response = new AnswerList(testDataLibList, nrTotalRows);

                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
                     
                } finally {
                    if(resultSet != null){
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
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
        response.setResultMessage(msg);
        response.setDataList(testDataLibList);
        return response;
    }

    private TestDataLib loadTestDataLibFromResultSet(ResultSet resultSet) throws SQLException {
        Integer testDataLibID = resultSet.getInt("testDataLibID");
        String name = resultSet.getString("name");
        String system = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("system"));
        String environment = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("environment"));
        String country = ParameterParserUtil.returnEmptyStringIfNull(resultSet.getString("country"));
        String group = resultSet.getString("group");
        String type = resultSet.getString("type");
        String database = resultSet.getString("database");
        String script = resultSet.getString("script");
        String servicePath = resultSet.getString("servicePath");
        String method = resultSet.getString("method");
        String envelope = resultSet.getString("envelope");
        String description = resultSet.getString("description");

        return factoryTestDataLib.create(testDataLibID, name, system, environment, country, group, type, database, script, servicePath, method, envelope, description);
    }

    @Override
    public AnswerItem findTestDataLibByKey(String name, String system, String environment, String country) throws CerberusException {
        AnswerItem answer = new AnswerItem();
        TestDataLib result = null;
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        
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
                        result = this.loadTestDataLibFromResultSet(resultSet);                                               
                        
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
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "It was not possible to retrieve the data."));
                    
                } finally {
                    if(resultSet != null){
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "It was not possible to retrieve the data."));
                    
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "It was not possible to retrieve the data."));
                    
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
    public AnswerItem findTestDataLibByKey(int testDataLibID) {
        TestDataLib result = null;
        final String query = "SELECT * FROM testdatalib where `TestDataLibID` = ?";
        AnswerItem item = new AnswerItem();
        Connection connection = this.databaseSpring.connect();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setInt(1, testDataLibID);
            
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadTestDataLibFromResultSet(resultSet); 
                        msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Lib").replace("%DESCRIPTION%", "SELECT"));
                        //sets the object
                        item.setItem(result);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_EXPECTED_ERROR);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Lib ").replace("%OPERATION%", "SELECT").
                                replace("%REASONS%", "Check if the selected entry exists!"));                                                
                    }
                    
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to execute query"));
                    
                } finally {
                    if(resultSet != null){
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to execute query"));
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
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
        item.setResultMessage(msg);
        return item;
    }

    
    @Override
    public Integer getNumberOfTestDataLibPerCriteria(String searchTerm, String inds) {
        Integer result = 0;
        StringBuilder query = new StringBuilder();
        StringBuilder gSearch = new StringBuilder();
        String searchSQL = "";

        query.append("SELECT count(*) FROM testdatalib");

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
        gSearch.append("%')");

        if (!searchTerm.equals("") && !inds.equals("")) {
            searchSQL = gSearch.toString() + " and " + inds;
        } else if (!inds.equals("")) {
            searchSQL = " where " + inds;
        } else if (!searchTerm.equals("")) {
            searchSQL = gSearch.toString();
        }

        query.append(searchSQL);

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {

                    if (resultSet.first()) {
                        result = resultSet.getInt(1);
                    }

                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }

        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
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
        return result;

    }
    
    @Override
    public AnswerList<String> getListOfGroupsPerType(String type){
        AnswerList<String> answerList = new AnswerList<String>();
        ArrayList<String> listOfGroups = new ArrayList<String>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        
        Connection connection = this.databaseSpring.connect();
        
        String query = "SELECT distinct(`Group`) FROM testdatalib  where `Type` like ? and `Group` <> '' order by `Group`";
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setString(1, type);
            
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while(resultSet.next()) {
                        listOfGroups.add(resultSet.getString(1));
                    }                    
                    answerList.setTotalRows(listOfGroups.size());
                    msg.setDescription(msg.getDescription().replace("GROUPS", "SELECT"));
                    
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to execute query"));
                } finally {
                    if(resultSet != null){
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to execute query"));
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
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
        answerList.setDataList(listOfGroups);
        answerList.setResultMessage(msg);
        return answerList;
    }

    @Override
    public Answer createTestDataLibBatch(List<TestDataLib> testDataLibEntries){
        MessageEvent rs = null;
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
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    rs.setDescription(rs.getDescription().replace("%ITEM%", "Test Data Lib (" + testDataLibEntries.size() + " entries) ").replace("%OPERATION%", "INSERT "));
                }else{
                    //some of the statements failed therefore we need to send a specific exception 
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_EXPECTED_ERROR);
                    rs.setDescription(rs.getDescription().replace("%ITEM%", "Test data lib ").replace("%OPERATION%", "INSERT").
                            replace("%REASON%", "Some problem occurred while inserting the test data lib entries!"));
                }
                
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                //23000 duplicated error code - http://dev.mysql.com/doc/refman/5.0/en/error-messages-server.html#error_er_dup_key                              
                if(exception.getSQLState().equals(SQL_DUPLICATED_CODE)){
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_DUPLICATE_ERROR);
                    rs.setDescription(rs.getDescription().replace("%ITEM%", "Test data lib ").replace("%OPERATION%", "INSERT"));
                }else{
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "Unable to execute query to insert testdatalib"));
                }
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "Unable to execute query to insert testdatalib"));
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
        return new Answer(rs); 
    }
   
    /**
     * Searches for the testdatalib names that match (totally or partially) the name provided. Used by the autocomplete feature.
     * @param testDataLibName 
     * @param limit
     * @return 
     */
    @Override
    public AnswerList findTestDataLibNameList(String testDataLibName, int limit) {
        
        List<String> namesList = new ArrayList<String>();
        AnswerList answer = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        
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

                    msg.setDescription(msg.getDescription().replace("%ITEM%", "Entries List").replace("%OPERATION%", "SELECT"));
                    answer.setDataList(namesList);
                    answer.setTotalRows(namesList.size());                     
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "It was not possible to retrieve the data."));  
                } finally {
                    if(resultSet != null){
                        resultSet.close();
                    }
                }
                
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "It was not possible to retrieve the data."));              
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "It was not possible to retrieve the data."));            
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
}
