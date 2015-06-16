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
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.cerberus.dao.ITestDataLibDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.TestDataLib;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTestDataLib;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
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

    @Override
    public void createTestDataLib(TestDataLib testDataLib) throws CerberusException {
        boolean throwExcep = false;
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
                if(keys != null && keys.next()){
                    testDataLib.setTestDataLibID(keys.getInt(1)); //saves the returned key which will be used to save the subdata entries
                }
                throwExcep = false;
                

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public Answer updateTestDataLib(TestDataLib testDataLib){
        
        MessageEvent rs = null; 
        String query = "update testdatalib set `group`= ?, `type`= ? , `database`= ? , `script`= ? , "
                + "`servicepath`= ? , `method`= ? , `envelope`= ? , `description`= ? where "
                + "`TestDataLibID`= ?"; 
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                //TODO:FN podemos mudar system+environment+country
                preStat.setString(1, testDataLib.getGroup());
                preStat.setString(2, testDataLib.getType());//TODO:FN podemos mudar o type?
                preStat.setString(3, testDataLib.getDatabase());
                preStat.setString(4, testDataLib.getScript());
                preStat.setString(5, testDataLib.getServicePath());
                preStat.setString(6, testDataLib.getMethod());
                preStat.setString(7, testDataLib.getEnvelope());
                preStat.setString(8, testDataLib.getDescription());
                preStat.setInt(9, testDataLib.getTestDataLibID());

                int rowsUpdated= preStat.executeUpdate();
                
                if(rowsUpdated == 0){                                        
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_EXPECTED_ERROR);
                    rs.setDescription(rs.getDescription().replace("%REASON%", " 0 Records updated."));
                }else{
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);                    
                }
                
                rs.setDescription(rs.getDescription().replace("%ITEM%", "Test data lib entry").replace("%OPERATION%", "UPDATED"));
 
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR); 
                rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "Unable to update data!"));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR); 
            rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "Unable to update data!"));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.WARN, e.toString());
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
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }
    
    @Override 
    public Answer deleteTestDataLib(int testDataLibID) throws CerberusException {
        
        MessageEvent rs = null;
        boolean stepOneOk = false;

        Connection connection = this.databaseSpring.connect();
        //removes the test datalib and and all the subdata
        PreparedStatement preStat = null;
        int testDataLibRows = 0 ;
        try {
            //auto commit is false because we want to rollback if it is not possible to delete the rows
            connection.setAutoCommit(false);
            StringBuilder query = new StringBuilder();
            query.append("delete from testdatalib where `testDataLibID`=? and ");
            query.append("`name` not in (select value1 from testcasecountryproperties tccp ");
            query.append("inner join testcase tc ");
            query.append("on tccp.Test = tc.Test and ");
            query.append("tccp.TestCase = tc.TestCase  and ");
            query.append("tccp.`type` like 'getFromDataLib' ");
            query.append("and tc.TcActive = 'Y' and ");
            query.append("(tc.status like 'WORKING'  OR tc.status like 'FULLY_IMPLEMENTED' ))");

 
            preStat = connection.prepareStatement(query.toString());
            //deletes the testdatalib
            preStat.setInt(1, testDataLibID);
            testDataLibRows = preStat.executeUpdate();
            stepOneOk = true;            
            if(testDataLibRows == 0){            
                //as we don't want to proceed then we will close the transaction
                //connection.rollback();
                //if the the testdatalib can be removed then we don't try to remove 
                rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_EXPECTED_ERROR);
                rs.setDescription(rs.getDescription().replace("%ITEM%", "Test Data Lib").replace("%OPERATION%", "DELETE")
                        .replace("%REASON%", "0 rows deleted. Please check if the test data lib is not being used by active test cases, which have the "
                                + " WORKING or FULL_IMPLEMENTED status"));
            }else{
                
                //everything went well
                rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                rs.setDescription(rs.getDescription().replace("%ITEM%", "Test data lib").replace("%OPERATION%", "DELETE"));
            }
            
                    
        } catch (SQLException exception) {
            rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "Test data lib can't be deleted."));            
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                //if one of the steps have failed then we need to rollback our changes
                if(!stepOneOk){
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "Test data lib can't be deleted."));                                
                }
                
                if(preStat != null){
                    preStat.close();
                }
                this.databaseSpring.closeConnection();
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
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public AnswerList findTestDataLibListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        
        AnswerList response = null;
        
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
                    response = new AnswerList(null, 0);
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
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, e.toString());
            }
        }

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
    public TestDataLib findTestDataLibByKey(String name, String system, String environment, String country) throws CerberusException {
        TestDataLib result = null;
        final String query = new StringBuilder("SELECT * FROM testdatalib where `name`=?")
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
                        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
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
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }
    
    @Override
    public AnswerItem findTestDataLibByKey(int testDataLibID) throws CerberusException {
        TestDataLib result = null;
        final String query = "SELECT * FROM testdatalib where `TestDataLibID`=?";
        AnswerItem item = new AnswerItem();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setInt(1, testDataLibID);
            
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadTestDataLibFromResultSet(resultSet);
                        item.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK)); //TODO:FN check this message;
                        item.setItem(result);
                    } else {
                        //TODO:FN check this message
                        item.setResultMessage(new MessageEvent(MessageEventEnum.NO_DATA_FOUND));                          
                    }
                    
                } catch (SQLException exception) {
                    MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                    //TODO:FN message
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                //TODO:FN message
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            //TODO:FN message
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.WARN, e.toString());
            }
        }
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
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.ERROR, e.toString());
            }
        }
        return result;

    }
    
    @Override
    public List<String> getListOfGroupsPerType(String type){
        ArrayList<String> listOfGroups = new ArrayList<String>();
        Connection connection = this.databaseSpring.connect();
        
        String query = "SELECT distinct(`Group`) FROM cerberus_partial.testdatalib  where `Type` like ? and `Group` <> '' order by `Group`";
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setString(1, type);
            
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while(resultSet.next()) {
                        listOfGroups.add(resultSet.getString(1));
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
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestDataLibDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return listOfGroups;
    }

    @Override
    public void createTestDataLibBatch(List<TestDataLib> testDataLibEntries) throws CerberusException{
        boolean throwExcep = false;
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
                preStat.executeBatch();
                
                ResultSet keys = preStat.getGeneratedKeys();
                int i = 0;
                
                if(keys != null){
                    while(keys.next()){
                        //gets the keys and associates them with the corresponding entries
                        testDataLibEntries.get(i).setTestDataLibID(keys.getInt(1)); //saves the returned key which will be used to save the subdata entries
                        i++;
                    }
                }
                
                
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            this.databaseSpring.closeConnection();
//            try {
//                if (connection != null) {
//                    connection.close();
//                }
//            } catch (SQLException e) {
//                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.WARN, e.toString());
//            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public boolean exists(String name, String[] system, String[] environment, String[] country) throws CerberusException{
        boolean throwExcep = false;
        boolean found = false;
        //ResponseItem answer = new ResponseItem();
        MessageEvent ms = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                
        StringBuilder query = new StringBuilder();
        query.append("select `name`, system,  environment, country, count(*) from testdatalib ");
        
        StringBuilder exppressions = new StringBuilder();
        
        Connection connection = this.databaseSpring.connect();
        int totalElements = system.length * environment.length * country.length;
        try {
            if(totalElements > 0){
                exppressions.append(" where ");
                for(String sys : system){
                    for(String env: environment){
                        for(String co: country){
                            exppressions.append("(`name` like ? AND system LIKE ? AND environment like ? AND country like ?) OR ");
                        }
                    }
                    
                }
                if(exppressions.toString().endsWith(" OR ")){
                    query.append(exppressions.toString().substring(0, exppressions.toString().length() -3 ));
                    //exppressions.(totalElements, totalElements, "");
                }else {
                    query.append(exppressions.toString());
                }
            }
            
            
            query.append(" group by `name`, `system`, `environment`, `country`");
            
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            
            int i = 0;
            for(String sys : system){
                for(String env: environment){
                    for(String co: country){
                        preStat.setString(++i, name);
                        preStat.setString(++i, sys);                        
                        preStat.setString(++i, env);
                        preStat.setString(++i, co);
                    }
                }

            }
            
            try {
                    
                ResultSet rs =preStat.executeQuery();
                StringBuilder except = new StringBuilder();
                except.append("Property with name: " ).append(name).append("already exists for:");
                
                if(rs != null){
                    while( rs.next()){
                    int total = rs.getInt(5);
                        if(total > 0){  //if total > 0  then are records that match the provided criteria
                            except.append("System: ").append(rs.getString(2)).append("  Environment: ").append(rs.getString(3)).
                                    append(" Country: ").append(rs.getString(4));
                            found = true;
                        }
                    }
                    // there are entries with the same properties
                    if(found){
                        ms = new MessageEvent(MessageEventEnum.DATA_OPERATION_DUPLICATE_ERROR);
                        ms.setDescription(except.toString());
                    }
                }
                
                throwExcep = false;
            } catch (SQLException exception) {
                MyLogger.log(TestDataLibDataDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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
        
        //return item;
        return found;
    }
    
}
