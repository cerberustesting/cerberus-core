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
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Level;
import org.cerberus.dao.ITestCaseCountryPropertiesDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.dto.PropertyListDTO;
import org.cerberus.dto.TestCaseListDTO;
import org.cerberus.dto.TestListDTO;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.TestCaseCountryProperties;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTestCaseCountryProperties;
import org.cerberus.log.MyLogger;
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
public class TestCaseCountryPropertiesDAO implements ITestCaseCountryPropertiesDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseCountryProperties factoryTestCaseCountryProperties;

    /**
     * Short one line description.
     * <p/>
     * Longer description. If there were any, it would be here. <p> And even
     * more explanations to follow in consecutive paragraphs separated by HTML
     * paragraph breaks.
     *
     * @param variable Description text text text.
     * @return Description text text text.
     */
    @Override
    public List<TestCaseCountryProperties> findListOfPropertyPerTestTestCase(String test, String testcase) {
        List<TestCaseCountryProperties> list = null;
        final String query = "SELECT * FROM testcasecountryproperties WHERE test = ? AND testcase = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<TestCaseCountryProperties>();

                    while (resultSet.next()) {
                        String country = resultSet.getString("country");
                        String property = resultSet.getString("property");
                        String type = resultSet.getString("type");
                        String database = resultSet.getString("database");
                        String value1 = resultSet.getString("value1");
                        String value2 = resultSet.getString("value2");
                        int length = resultSet.getInt("length");
                        int rowLimit = resultSet.getInt("rowLimit");
                        String nature = resultSet.getString("nature");
                        list.add(factoryTestCaseCountryProperties.create(test, testcase, country, property, type, database, value1,value2, length, rowLimit, nature));

                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public List<TestCaseCountryProperties> findDistinctPropertiesOfTestCase(String test, String testcase) {
        List<TestCaseCountryProperties> listProperties = null;
        final StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcasecountryproperties WHERE test = ? AND testcase = ?");
        query.append(" group by HEX(`property`), `type`, `database`, HEX(`value1`) ,  HEX(`value2`) , `length`, `rowlimit`, `nature`");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    listProperties = new ArrayList<TestCaseCountryProperties>();

                    while (resultSet.next()) {
                        String country = resultSet.getString("country");
                        String property = resultSet.getString("property");
                        String type = resultSet.getString("type");
                        String database = resultSet.getString("database");
                        String value1 = resultSet.getString("value1");
                        String value2 = resultSet.getString("value2");
                        int length = resultSet.getInt("length");
                        int rowLimit = resultSet.getInt("rowLimit");
                        String nature = resultSet.getString("nature");
                        listProperties.add(factoryTestCaseCountryProperties.create(test, testcase, country, property, type, database, value1, value2, length, rowLimit, nature));

                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return listProperties;
    }

    @Override
    public List<String> findCountryByProperty(TestCaseCountryProperties testCaseCountryProperties) {
        List<String> list = null;
        final StringBuilder query = new StringBuilder();
        query.append("SELECT country FROM testcasecountryproperties WHERE test = ? AND testcase = ?");
        query.append(" AND HEX(`property`) = hex(?) AND `type` =? AND `database` =? AND hex(`value1`) like hex( ? ) AND hex(`value2`) like hex( ? ) AND `length` = ? ");
        query.append(" AND `rowlimit` = ? AND `nature` = ?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testCaseCountryProperties.getTest());
                preStat.setString(2, testCaseCountryProperties.getTestCase());
                preStat.setNString(3, testCaseCountryProperties.getProperty());
                preStat.setString(4, testCaseCountryProperties.getType());
                preStat.setString(5, testCaseCountryProperties.getDatabase());
                preStat.setNString(6, testCaseCountryProperties.getValue1());
                preStat.setNString(7, testCaseCountryProperties.getValue2());
                preStat.setString(8, String.valueOf(testCaseCountryProperties.getLength()));
                preStat.setString(9, String.valueOf(testCaseCountryProperties.getRowLimit()));
                preStat.setString(10, testCaseCountryProperties.getNature());

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<String>();
                    String valueToAdd;

                    while (resultSet.next()) {
                        valueToAdd = resultSet.getString("Country") == null ? "" : resultSet.getString("Country");
                        list.add(valueToAdd);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public List<TestCaseCountryProperties> findListOfPropertyPerTestTestCaseCountry(String test, String testcase, String country) {
        List<TestCaseCountryProperties> list = null;
        final String query = "SELECT * FROM testcasecountryproperties WHERE test = ? AND testcase = ? AND country = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                preStat.setString(3, country);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<TestCaseCountryProperties>();

                    while (resultSet.next()) {
                        String property = resultSet.getString("property");
                        String type = resultSet.getString("type");
                        String database = resultSet.getString("database");
                        String value1 = resultSet.getString("value1");
                        String value2 = resultSet.getString("value2");
                        int length = resultSet.getInt("length");
                        int rowLimit = resultSet.getInt("rowLimit");
                        String nature = resultSet.getString("nature");
                        MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.DEBUG, "Found Test Case Country property : " + test + "-" + testcase + "-" + country + "-" + property);
                        list.add(factoryTestCaseCountryProperties.create(test, testcase, country, property, type, database, value1,value2, length, rowLimit, nature));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public TestCaseCountryProperties findTestCaseCountryPropertiesByKey(String test, String testcase, String country, String property) throws CerberusException {
        TestCaseCountryProperties result = null;
        boolean throwException = false;
        final String query = "SELECT * FROM testcasecountryproperties WHERE test = ? AND testcase = ? AND country = ? AND hex(`property`) = hex(?)";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                preStat.setString(3, country);
                preStat.setNString(4, property);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        String type = resultSet.getString("type");
                        String database = resultSet.getString("database");
                        String value1 = resultSet.getString("value1");
                        String value2 = resultSet.getString("value2");
                        int length = resultSet.getInt("length");
                        int rowLimit = resultSet.getInt("rowLimit");
                        String nature = resultSet.getString("nature");
                        result = factoryTestCaseCountryProperties.create(test, testcase, country, property, type, database, value1, value2, length, rowLimit, nature);
                    } else {
                        throwException = true;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwException) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    @Override
    public void insertTestCaseCountryProperties(TestCaseCountryProperties testCaseCountryProperties) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcasecountryproperties (`Test`,`TestCase`,`Country`,`Property` ,`Type`");
        query.append(",`Database`,`Value1`,`Value2`,`Length`,`RowLimit`,`Nature`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?)");
        
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testCaseCountryProperties.getTest());
                preStat.setString(2, testCaseCountryProperties.getTestCase());
                preStat.setString(3, testCaseCountryProperties.getCountry());
                preStat.setString(4, testCaseCountryProperties.getProperty());
                preStat.setString(5, testCaseCountryProperties.getType());
                preStat.setString(6, testCaseCountryProperties.getDatabase());
                preStat.setString(7, testCaseCountryProperties.getValue1());
                preStat.setString(8, testCaseCountryProperties.getValue2());
                preStat.setInt(9, testCaseCountryProperties.getLength());
                preStat.setInt(10, testCaseCountryProperties.getRowLimit());
                preStat.setString(11, testCaseCountryProperties.getNature());
                

                preStat.executeUpdate();
                throwExcep = false;
                
                
            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }
    
    @Override
    public void updateTestCaseCountryProperties(TestCaseCountryProperties testCaseCountryProperties) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("UPDATE testcasecountryproperties SET ");
        query.append(" `Type` = ? ,`Database` = ? ,Value1 = ?,Value2 = ?,`Length` = ?,  RowLimit = ?,  `Nature` = ? ");
        query.append(" WHERE Test = ? AND TestCase = ? AND Country = ? AND hex(`Property`) like hex(?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testCaseCountryProperties.getType());
                preStat.setString(2, testCaseCountryProperties.getDatabase());
                preStat.setString(3, testCaseCountryProperties.getValue1());
                preStat.setString(4, testCaseCountryProperties.getValue2());
                preStat.setInt(5, testCaseCountryProperties.getLength());
                preStat.setInt(6, testCaseCountryProperties.getRowLimit());
                preStat.setString(7, testCaseCountryProperties.getNature());
                preStat.setString(8, testCaseCountryProperties.getTest());
                preStat.setString(9, testCaseCountryProperties.getTestCase());
                preStat.setString(10, testCaseCountryProperties.getCountry());
                preStat.setNString(11, testCaseCountryProperties.getProperty());
                

                preStat.executeUpdate();
                throwExcep = false;
                
                
            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public List<String> findCountryByPropertyNameAndTestCase(String test, String testcase, String property) {
        List<String> result = new ArrayList<String>();

        final String query = "SELECT country FROM testcasecountryproperties WHERE test = ? AND testcase = ? AND hex(`property`) like hex(?)";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                preStat.setNString(3, property);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        String country = resultSet.getString("country");
                        if (country != null && !"".equals(country)) {
                            result.add(country);
                        }
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        if (result.size() == 0) {
            return null;
        }

        return result;
    }

    @Override
    public void deleteTestCaseCountryProperties(TestCaseCountryProperties tccp) throws CerberusException {
        boolean throwExcep = false;
        final String query = "DELETE FROM testcasecountryproperties WHERE test = ? and testcase = ? and country = ? and hex(`property`) like hex(?)";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, tccp.getTest());
                preStat.setString(2, tccp.getTestCase());
                preStat.setString(3, tccp.getCountry());
                preStat.setNString(4, tccp.getProperty());

                throwExcep = preStat.executeUpdate() == 0;
            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public AnswerList findTestCaseCountryPropertiesByValue1(int testDataLib, String name, String country, String propertyType) {
        AnswerList ansList = new AnswerList();
        MessageEvent rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        List<TestListDTO> listOfTests = new ArrayList<TestListDTO>();
        StringBuilder query = new StringBuilder();
//        query.append("select count(*) as total, tccp.*, t.Description as testDescription, tc.Description as testCaseDescription, tc.Application, tc.TcActive as Active, tc.`Group`, ");
//        query.append("tc.Creator, tc.`Status` ");
//        query.append("from testcasecountryproperties tccp, test t, testcase tc, testdatalib tdl ");
//        query.append("where t.test = tccp.test and ");
//        query.append("tccp.TestCase = tc.TestCase and ");
//        query.append("tc.Test = t.Test and ");
//        query.append("(tccp.Country = tdl.Country or tdl.country='') and ");
//        query.append("tccp.Value1 = tdl.`Name` and  ");
//        query.append("tdl.TestDataLibID = ? and ");
//        query.append("tdl.`Name` LIKE ? and ");
//        query.append("(tdl.Country = ? or tdl.country='') and ");
//        query.append("tccp.`Type` LIKE ? ");
//        query.append("group by tccp.test, tccp.testcase, tccp.property ");
        query.append("select count(*) as total, tccp.property, t.Test, tc.TestCase, t.Description as testDescription, tc.Description as testCaseDescription, tc.Application, ");
        query.append("tc.TcActive as Active, tc.`Group`, tc.Creator, tc.`Status` ");
        query.append("from testcasecountryproperties tccp    ");
        query.append("inner join  test t on t.test = tccp.test ");
        query.append("inner join  testcase tc  on t.test = tccp.test  and t.test = tc.test ");
        query.append("inner join testdatalib tdl on tdl.`name` = tccp.value1  and ");
        query.append("(tccp.Country = tdl.Country or tdl.country='') and tccp.test = t.test and tccp.testcase = tc.testcase ");
        query.append("where tccp.`Type` LIKE ? and tdl.TestDataLibID = ? "); 
        query.append("and tdl.`Name` LIKE ? and (tdl.Country = ? or tdl.country='') ");
        query.append("group by tccp.test, tccp.testcase, tccp.property ");

        
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, propertyType);
                preStat.setInt(2, testDataLib);
                preStat.setString(3, name);
                preStat.setString(4, country);
                
                 
                HashMap<String, TestListDTO> map = new HashMap<String, TestListDTO>();
               
                
                HashMap<String, List<PropertyListDTO>> auxiliaryMap = new HashMap<String, List<PropertyListDTO>>();//the key is the test + ":" +testcasenumber
                
                String key, test, testCase;
                ResultSet resultSet = preStat.executeQuery();
                try{
                    while(resultSet.next()){
                        TestListDTO testList;
                        TestCaseListDTO testCaseDTO; 
                        List<PropertyListDTO> propertiesList;
                        
                        test = resultSet.getString("Test");
                        testCase = resultSet.getString("TestCase");
                        
                        //TEST
                        //gets the info from test cases that match the desired information
                        if(map.containsKey(test)){
                            testList = map.get(test);
                        }else{
                            testList = new TestListDTO();
                            
                            testList.setDescription(resultSet.getString("testDescription"));
                            testList.setTest(test);
                        }
                        
                        //TESTCASE
                        key = test + ":" + testCase;
                        if(!auxiliaryMap.containsKey(key)){
                            //means that we must associate a new test case with a test
                            testCaseDTO = new TestCaseListDTO();
                            testCaseDTO.setTestCaseDescription(resultSet.getString("testCaseDescription"));
                            testCaseDTO.setTestCaseNumber(testCase);
                            testCaseDTO.setApplication(resultSet.getString("Application"));
                            testCaseDTO.setCreator(resultSet.getString("Creator"));
                            testCaseDTO.setStatus(resultSet.getString("Status"));

                            testCaseDTO.setGroup(resultSet.getString("Group"));
                            testCaseDTO.setIsActive(resultSet.getString("Active"));
                            testList.getTestCaseList().add(testCaseDTO);
                            map.put(test, testList);
                            propertiesList = new ArrayList<PropertyListDTO>();
                        }else{
                            propertiesList = auxiliaryMap.get(key);
                        }
                        
                        PropertyListDTO prop = new PropertyListDTO();
                        
                        prop.setNrCountries(resultSet.getInt("total"));
                        prop.setPropertyName(resultSet.getString("property"));
                        propertiesList.add(prop);
                        //stores the information about the properties
                        auxiliaryMap.put(key, propertiesList);
                        
                    }
                   
                    
                    //assigns the list of tests retrieved by the query to the list
                    listOfTests = new ArrayList<TestListDTO>(map.values());
                    
                    
                     //assigns the list of properties to the correct testcaselist
                    for(TestListDTO list : listOfTests){
                        for(TestCaseListDTO cases : list.getTestCaseList()){
                            cases.setPropertiesList(auxiliaryMap.get(list.getTest() + ":" + cases.getTestCaseNumber()));
                        }
                    }
                    
                    rs.setDescription(rs.getDescription().replace("%ITEM%", "Test Cases that use property").replace("%OPERATION%", "SELECT"));
                    
                }catch (SQLException exception) {
                    MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                    rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "Unable to get the list of test cases."));
                } finally{
                    if(resultSet != null){
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "Unable to get the list of test cases."));
            } finally {
                if(preStat != null){
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "Unable to get the list of test cases."));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        ansList.setResultMessage(rs);
        ansList.setDataList(listOfTests); 
        
        return ansList;
    }
}
