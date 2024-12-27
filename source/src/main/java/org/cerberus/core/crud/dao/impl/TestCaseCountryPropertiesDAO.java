/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.crud.dao.impl;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.ITestCaseCountryPropertiesDAO;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.crud.utils.RequestDbUtils;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.dto.PropertyListDTO;
import org.cerberus.core.dto.TestCaseListDTO;
import org.cerberus.core.dto.TestListDTO;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @author FNogueira
 * @version 1.0, 28/Dez/2012
 * @since 2.0.0
 */
@Repository
public class TestCaseCountryPropertiesDAO implements ITestCaseCountryPropertiesDAO {

    private final DatabaseSpring databaseSpring;

    private static final Logger LOG = LogManager.getLogger(TestCaseCountryPropertiesDAO.class);

    private final String OBJECT_NAME = "TestCaseCountryProperties";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Autowired
    public TestCaseCountryPropertiesDAO(DatabaseSpring databaseSpring) {
        this.databaseSpring = databaseSpring;
    }

    @Override
    public List<TestCaseCountryProperties> findListOfPropertyPerTestTestCase(String test, String testcase) throws CerberusException {
        final String query = "SELECT * FROM testcasecountryproperties tcp WHERE test = ? AND testcase = ? ";
        // TestCase dependency should only be used on testcasedataexecution.
        // In other words, when a test case is linked to another testcase, it should have access to its data at execution level but should not inherit from testcase property definition.
        // As a consequece this method should not return testCaseCountryPropertiesList from dependencies.
        //"OR exists (select 1 from  testcasedep  where DepTest = tcp.Test AND DepTestCase = tcp.TestCase AND Test = ? AND TestCase = ?)";
        // Manage tc dependencies

        return RequestDbUtils.executeQueryList(databaseSpring, query,
                ps -> {
                    ps.setString(1, test);
                    ps.setString(2, testcase);
                },
                this::loadFromResultSet
        );

    }

    @Override
    public List<TestCaseCountryProperties> findListOfPropertyPerTestTestCaseList(List<TestCase> testcases) throws CerberusException {
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcasecountryproperties tcp WHERE 1=1");
        // TestCase dependency should only be used on testcasedataexecution.
        // In other words, when a test case is linked to another testcase, it should have access to its data at execution level but should not inherit from testcase property definition.
        // As a consequece this method should not return testCaseCountryPropertiesList from dependencies.
        //"OR exists (select 1 from  testcasedep  where DepTest = tcp.Test AND DepTestCase = tcp.TestCase AND Test = ? AND TestCase = ?)";
        // Manage tc dependencies

        if ((testcases != null) && !testcases.isEmpty() && testcases.size() < 5000) {
            query.append(" AND (");
            int j = 0;
            for (TestCase testCase1 : testcases) {
                if (j != 0) {
                    query.append(" OR");
                }
                query.append(" (tcp.`test` = ? and tcp.testcase = ?) ");
                j++;
            }
            query.append(" )");
        } else {
            query.append(" and 1=0 ");
        }

        return RequestDbUtils.executeQueryList(databaseSpring, query.toString(),
                ps -> {
                    int i = 1;
                    if ((testcases != null) && !testcases.isEmpty() && testcases.size() < 5000) {
                        for (TestCase testCaseObj : testcases) {
                            ps.setString(i++, testCaseObj.getTest());
                            ps.setString(i++, testCaseObj.getTestcase());
                        }
                    }
                },
                this::loadFromResultSet
        );
    }

    @Override
    public List<TestCaseCountryProperties> findListOfPropertyPerTestTestCaseProperty(String test, String testcase, String oneproperty) {
        List<TestCaseCountryProperties> testCaseCountryPropertiesList = null;
        final String query = "SELECT * FROM testcasecountryproperties WHERE test = ? AND testcase = ? AND property = ?";

        loggingQuery(query);

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query);) {

            int i = 1;
            preStat.setString(i++, test);
            preStat.setString(i++, testcase);
            preStat.setString(i++, oneproperty);

            try (ResultSet resultSet = preStat.executeQuery();) {
                testCaseCountryPropertiesList = new ArrayList<>();
                while (resultSet.next()) {
                    testCaseCountryPropertiesList.add(loadFromResultSet(resultSet));
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        }
        return testCaseCountryPropertiesList;
    }

    @Override
    public List<String> findCountryByProperty(TestCaseCountryProperties testCaseCountryProperties) {
        List<String> countries = null;
        final StringBuilder query = new StringBuilder();
        query.append("SELECT country FROM testcasecountryproperties WHERE test = ? AND testcase = ?");
        query.append(" AND HEX(`property`) = hex(?) AND `type` =? AND `database` =? AND hex(`value1`) like hex( ? ) AND hex(`value2`) like hex( ? ) AND hex(`value3`) like hex( ? ) AND `length` = ? ");
        query.append(" AND `rowlimit` = ? AND `nature` = ?");

        loggingQuery(query.toString());

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());) {

            int i = 1;
            preStat.setString(i++, testCaseCountryProperties.getTest());
            preStat.setString(i++, testCaseCountryProperties.getTestcase());
            preStat.setBytes(i++, testCaseCountryProperties.getProperty().getBytes("UTF-8"));
            preStat.setString(i++, testCaseCountryProperties.getType());
            preStat.setString(i++, testCaseCountryProperties.getDatabase());
            preStat.setBytes(i++, testCaseCountryProperties.getValue1().getBytes("UTF-8"));
            preStat.setBytes(i++, testCaseCountryProperties.getValue2().getBytes("UTF-8"));
            preStat.setBytes(i++, testCaseCountryProperties.getValue3().getBytes("UTF-8"));
            preStat.setString(i++, String.valueOf(testCaseCountryProperties.getLength()));
            preStat.setString(i++, String.valueOf(testCaseCountryProperties.getRowLimit()));
            preStat.setString(i++, testCaseCountryProperties.getNature());

            try (ResultSet resultSet = preStat.executeQuery();) {
                countries = new ArrayList<>();
                String countryToAdd;

                while (resultSet.next()) {
                    countryToAdd = resultSet.getString("Country") == null ? "" : resultSet.getString("Country");
                    countries.add(countryToAdd);
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } catch (UnsupportedEncodingException ex) {
            LOG.error(ex.toString());
        }
        return countries;
    }

    @Override
    public List<TestCaseCountryProperties> findListOfPropertyPerTestTestCaseCountry(String test, String testcase, String country) {
        List<TestCaseCountryProperties> testCaseCountryPropertiesList = null;
        final String query = "SELECT * FROM testcasecountryproperties WHERE test = ? AND testcase = ? AND country = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.test : " + test);
            LOG.debug("SQL.param.testcase : " + testcase);
            LOG.debug("SQL.param.country : " + country);
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query);) {

            int i = 1;
            preStat.setString(i++, test);
            preStat.setString(i++, testcase);
            preStat.setString(i++, country);

            try (ResultSet resultSet = preStat.executeQuery();) {
                testCaseCountryPropertiesList = new ArrayList<>();

                while (resultSet.next()) {
                    testCaseCountryPropertiesList.add(loadFromResultSet(resultSet));
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        }
        return testCaseCountryPropertiesList;
    }

    @Override
    public TestCaseCountryProperties findTestCaseCountryPropertiesByKey(String test, String testcase, String country, String property) throws CerberusException {
        TestCaseCountryProperties testCaseCountryProperties = null;
        final String query = "SELECT * FROM testcasecountryproperties WHERE test = ? AND testcase = ? AND country = ? AND hex(`property`) = hex(?)";

        loggingQuery(query);

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {

            int i = 1;
            preStat.setString(i++, test);
            preStat.setString(i++, testcase);
            preStat.setString(i++, country);
            preStat.setBytes(i++, property.getBytes("UTF-8"));

            ResultSet resultSet = preStat.executeQuery();
            try {
                if (resultSet.first()) {
                    testCaseCountryProperties = loadFromResultSet(resultSet);
                } else {
                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } catch (UnsupportedEncodingException ex) {
            LOG.error(ex.toString());
        }

        return testCaseCountryProperties;
    }

    @Override
    public void insertTestCaseCountryProperties(TestCaseCountryProperties testCaseCountryProperties) throws CerberusException {
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcasecountryproperties (`Test`,`TestCase`,`Country`,`Property` ,`Description`,`Type`");
        query.append(",`Database`,`Value1`,`Value2`,`Value3`,`Length`,`RowLimit`,`Nature`,`RetryNb`,`RetryPeriod`,`Rank`,");
        query.append("`UsrCreated`)");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        loggingQuery(query.toString());

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());) {

            int i = 1;
            preStat.setString(i++, testCaseCountryProperties.getTest());
            preStat.setString(i++, testCaseCountryProperties.getTestcase());
            preStat.setString(i++, testCaseCountryProperties.getCountry());
            preStat.setBytes(i++, testCaseCountryProperties.getProperty().getBytes("UTF-8"));
            preStat.setBytes(i++, testCaseCountryProperties.getDescription().getBytes("UTF-8"));
            preStat.setString(i++, testCaseCountryProperties.getType());
            preStat.setString(i++, testCaseCountryProperties.getDatabase());
            preStat.setBytes(i++, testCaseCountryProperties.getValue1().getBytes("UTF-8"));
            preStat.setBytes(i++, testCaseCountryProperties.getValue2().getBytes("UTF-8"));
            preStat.setBytes(i++, testCaseCountryProperties.getValue3().getBytes("UTF-8"));
            preStat.setString(i++, testCaseCountryProperties.getLength());
            preStat.setInt(i++, testCaseCountryProperties.getRowLimit());
            preStat.setString(i++, testCaseCountryProperties.getNature());
            preStat.setInt(i++, testCaseCountryProperties.getRetryNb());
            preStat.setInt(i++, testCaseCountryProperties.getRetryPeriod());
            preStat.setInt(i++, testCaseCountryProperties.getRank());
            preStat.setString(i++, testCaseCountryProperties.getUsrCreated() == null ? "" : testCaseCountryProperties.getUsrCreated());

            preStat.executeUpdate();

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } catch (UnsupportedEncodingException ex) {
            LOG.error(ex.toString());
        }
    }

    @Override
    public void updateTestCaseCountryProperties(TestCaseCountryProperties testCaseCountryProperties) throws CerberusException {
        StringBuilder query = new StringBuilder();
        query.append("UPDATE testcasecountryproperties SET ");
        query.append("`Description` = ?, ");
        query.append("`Type` = ?, ");
        query.append("`Database` = ?, ");
        query.append("`Value1` = ?, ");
        query.append("`Value2` = ?, ");
        query.append("`Value3` = ?, ");
        query.append("`Length` = ?, ");
        query.append("`RowLimit` = ?, ");
        query.append("`Nature` = ?, ");
        query.append("`RetryNb` = ?, ");
        query.append("`RetryPeriod` = ?, ");
        query.append("`Rank` = ?, ");
        query.append("`UsrModif` = ?, ");
        query.append("`DateModif` = CURRENT_TIMESTAMP ");
        query.append(" WHERE Test = ? AND TestCase = ? AND Country = ? AND hex(`Property`) like hex(?)");

        loggingQuery(query.toString());

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());) {

            int i = 1;
            preStat.setBytes(i++, testCaseCountryProperties.getDescription().getBytes("UTF-8"));
            preStat.setString(i++, testCaseCountryProperties.getType());
            preStat.setString(i++, testCaseCountryProperties.getDatabase());
            preStat.setBytes(i++, testCaseCountryProperties.getValue1().getBytes("UTF-8"));
            preStat.setBytes(i++, testCaseCountryProperties.getValue2().getBytes("UTF-8"));
            preStat.setBytes(i++, testCaseCountryProperties.getValue3().getBytes("UTF-8"));
            preStat.setString(i++, testCaseCountryProperties.getLength());
            preStat.setInt(i++, testCaseCountryProperties.getRowLimit());
            preStat.setString(i++, testCaseCountryProperties.getNature());
            preStat.setInt(i++, testCaseCountryProperties.getRetryNb());
            preStat.setInt(i++, testCaseCountryProperties.getRetryPeriod());
            preStat.setInt(i++, testCaseCountryProperties.getRank());
            preStat.setString(i++, testCaseCountryProperties.getUsrModif() == null ? "" : testCaseCountryProperties.getUsrModif());
            preStat.setString(i++, testCaseCountryProperties.getTest());
            preStat.setString(i++, testCaseCountryProperties.getTestcase());
            preStat.setString(i++, testCaseCountryProperties.getCountry());
            preStat.setBytes(i++, testCaseCountryProperties.getProperty().getBytes("UTF-8"));

            preStat.executeUpdate();
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } catch (UnsupportedEncodingException ex) {
            LOG.error(ex.toString());
        }
    }

    @Override
    public Answer bulkRenameProperties(String oldName, String newName) {
        Answer answer = new Answer();
        MessageEvent msg;
        StringBuilder query = new StringBuilder();
        query.append("UPDATE testcasecountryproperties SET ");
        query.append("`Value1`= ?, ");
        query.append("`DateModif` = CURRENT_TIMESTAMP ");
        query.append("WHERE `Type` = 'getFromDataLib' AND `Value1`= ?");

        loggingQuery(query.toString());

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());) {

            int i = 1;
            preStat.setString(i++, newName);
            preStat.setString(i++, oldName);
            int rowsUpdated = preStat.executeUpdate();

            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            // Message to customize : X testCaseCountryPropertiesList updated using the rowsUpdated variable
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE").replace("success!", "success! - Row(s) updated : " + String.valueOf(rowsUpdated)));

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString(), exception);
            if (exception.getSQLState().equals(SQL_DUPLICATED_CODE)) { //23000 is the sql state for duplicate entries
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_DUPLICATE);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Test data lib ").replace("%OPERATION%", "UPDATE").replace("%REASON%", exception.toString()));
            } else {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            }
        }
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public List<String> findCountryByPropertyNameAndTestCase(String test, String testcase, String property) {
        List<String> countries = new ArrayList<>();

        final String query = "SELECT country FROM testcasecountryproperties WHERE test = ? AND testcase = ? AND hex(`property`) like hex(?)";

        loggingQuery(query);

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query);) {

            int i = 1;
            preStat.setString(i++, test);
            preStat.setString(i++, testcase);
            preStat.setBytes(i++, property.getBytes("UTF-8"));

            try (ResultSet resultSet = preStat.executeQuery();) {
                while (resultSet.next()) {
                    String country = resultSet.getString("country");
                    if (country != null && !"".equals(country)) {
                        countries.add(country);
                    }
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } catch (UnsupportedEncodingException ex) {
            LOG.error(ex.toString());
        }

        return (countries.isEmpty()) ? null : countries;
    }

    @Override
    public void deleteTestCaseCountryProperties(TestCaseCountryProperties tccp) throws CerberusException {

        final String query = "DELETE FROM testcasecountryproperties WHERE test = ? and testcase = ? and country = ? and hex(`property`) like hex(?)";

        loggingQuery(query);

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query);) {

            int i = 1;
            preStat.setString(i++, tccp.getTest());
            preStat.setString(i++, tccp.getTestcase());
            preStat.setString(i++, tccp.getCountry());
            preStat.setBytes(i++, tccp.getProperty().getBytes("UTF-8"));

            if (preStat.executeUpdate() == 0) {
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } catch (UnsupportedEncodingException ex) {
            LOG.error(ex.toString());
        }
    }

    @Override
    public AnswerList<TestListDTO> findTestCaseCountryPropertiesByValue1(int testDataLib, String name, String country, String propertyType) {
        AnswerList<TestListDTO> ansList = new AnswerList<>();
        MessageEvent msg;
        List<TestListDTO> listOfTests = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query.append("select count(*) as total, tccp.property, t.Test, tc.TestCase, t.Description as testDescription, tc.Description as testCaseDescription, tc.Application, ");
        query.append("tc.isActive, tc.`Type`, tc.UsrCreated, tc.`Status` ");
        query.append("from testcasecountryproperties tccp    ");
        query.append("inner join test t on t.test = tccp.test ");
        query.append("inner join testcase tc  on t.test = tccp.test  and t.test = tc.test ");
        query.append("inner join testdatalib tdl on tdl.`name` = tccp.value1  and ");
        query.append("(tccp.Country = tdl.Country or tdl.country='') and tccp.test = t.test and tccp.testcase = tc.testcase ");
        query.append("where tccp.`Type` LIKE ? and tdl.TestDataLibID = ? ");
        query.append("and tdl.`Name` LIKE ? and (tdl.Country = ? or tdl.country='') ");
        query.append("group by tccp.test, tccp.testcase, tccp.property ");

        loggingQuery(query.toString());

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());) {

            int i = 1;
            preStat.setString(i++, propertyType);
            preStat.setInt(i++, testDataLib);
            preStat.setString(i++, name);
            preStat.setString(i++, country);

            HashMap<String, TestListDTO> map = new HashMap<>();

            HashMap<String, List<PropertyListDTO>> auxiliaryMap = new HashMap<>();//the key is the test + ":" +testcasenumber

            String key, test, testCase;

            try (ResultSet resultSet = preStat.executeQuery();) {
                while (resultSet.next()) {
                    TestListDTO testList;
                    TestCaseListDTO testCaseDTO;
                    List<PropertyListDTO> propertiesList;

                    test = resultSet.getString("Test");
                    testCase = resultSet.getString("TestCase");

                    //TEST
                    //gets the info from test cases that match the desired information
                    if (map.containsKey(test)) {
                        testList = map.get(test);
                    } else {
                        testList = new TestListDTO();

                        testList.setDescription(resultSet.getString("testDescription"));
                        testList.setTest(test);
                    }

                    //TESTCASE
                    key = test + ":" + testCase;
                    if (!auxiliaryMap.containsKey(key)) {
                        //means that we must associate a new test case with a test
                        testCaseDTO = new TestCaseListDTO();
                        testCaseDTO.setTestCaseDescription(resultSet.getString("testCaseDescription"));
                        testCaseDTO.setTestCaseNumber(testCase);
                        testCaseDTO.setApplication(resultSet.getString("Application"));
                        testCaseDTO.setCreator(resultSet.getString("tc.UsrCreated"));
                        testCaseDTO.setStatus(resultSet.getString("Status"));

                        testCaseDTO.setGroup(resultSet.getString("Type"));
                        testCaseDTO.setIsActive(resultSet.getString("isActive"));
                        testList.getTestCaseList().add(testCaseDTO);
                        map.put(test, testList);
                        propertiesList = new ArrayList<>();
                    } else {
                        propertiesList = auxiliaryMap.get(key);
                    }

                    PropertyListDTO prop = new PropertyListDTO();

                    prop.setNrCountries(resultSet.getInt("total"));
                    prop.setPropertyName(resultSet.getString("property"));
                    propertiesList.add(prop);
                    //stores the information about the testCaseCountryPropertiesList
                    auxiliaryMap.put(key, propertiesList);

                }

                //assigns the testCaseCountryPropertiesList of tests retrieved by the query to the testCaseCountryPropertiesList
                listOfTests = new ArrayList<>(map.values());

                //assigns the testCaseCountryPropertiesList of testCaseCountryPropertiesList to the correct testcaselist
                for (TestListDTO list : listOfTests) {
                    for (TestCaseListDTO cases : list.getTestCaseList()) {
                        cases.setPropertiesList(auxiliaryMap.get(list.getTest() + ":" + cases.getTestCaseNumber()));
                    }
                }
                if (listOfTests.isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                } else {
                    msg = successExecuteQuery("List of Test Cases", "SELECT");
                }

            } catch (SQLException exception) {
                msg = unexpectedError(exception, "Unable to get the list of test cases.");
            }
        } catch (SQLException exception) {
            msg = unexpectedError(exception, "Unable to get the list of test cases.");
        }
        ansList.setResultMessage(msg);
        ansList.setDataList(listOfTests);

        return ansList;
    }

    @Override
    public Answer createTestCaseCountryPropertiesBatch(List<TestCaseCountryProperties> testCaseCountryPropertiesList) {
        Answer answer = new Answer();
        MessageEvent msg;

        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcasecountryproperties (`Test`,`TestCase`,`Country`,`Property` , `Description`, `Type`");
        query.append(",`Database`,`Value1`,`Value2`,`Value3`,`Length`,`RowLimit`,`Nature`,`RetryNb`,`RetryPeriod`, `Rank`, `UsrCreated`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        loggingQuery(query.toString());

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());) {

            for (TestCaseCountryProperties testCaseCountryProperties : testCaseCountryPropertiesList) {

                int i = 1;
                preStat.setString(i++, testCaseCountryProperties.getTest());
                preStat.setString(i++, testCaseCountryProperties.getTestcase());
                preStat.setString(i++, testCaseCountryProperties.getCountry());
                preStat.setString(i++, testCaseCountryProperties.getProperty());
                preStat.setString(i++, testCaseCountryProperties.getDescription());
                preStat.setString(i++, testCaseCountryProperties.getType());
                preStat.setString(i++, testCaseCountryProperties.getDatabase());
                preStat.setString(i++, testCaseCountryProperties.getValue1());
                preStat.setString(i++, testCaseCountryProperties.getValue2());
                preStat.setString(i++, testCaseCountryProperties.getValue3());
                preStat.setString(i++, testCaseCountryProperties.getLength());
                preStat.setInt(i++, testCaseCountryProperties.getRowLimit());
                preStat.setString(i++, testCaseCountryProperties.getNature());
                preStat.setInt(i++, testCaseCountryProperties.getRetryNb());
                preStat.setInt(i++, testCaseCountryProperties.getRetryPeriod());
                preStat.setInt(i++, testCaseCountryProperties.getRank());
                preStat.setString(i++, testCaseCountryProperties.getUsrCreated() == null ? "" : testCaseCountryProperties.getUsrCreated());

                preStat.addBatch();
            }

            preStat.executeBatch();

            int affectedRows[] = preStat.executeBatch();

            //verify if some of the statements failed
            boolean someFailed = ArrayUtils.contains(affectedRows, 0) || ArrayUtils.contains(affectedRows, Statement.EXECUTE_FAILED);

            if (someFailed) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Property").replace("%OPERATION%", "CREATE").
                        replace("%REASON%", "Some problem occurred while creating the new property! "));
            } else {
                msg = successExecuteQuery(OBJECT_NAME, "CREATE");
            }

        } catch (SQLException exception) {
            msg = unexpectedError(exception, "It was not possible to update table.");
        }
        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public Answer create(TestCaseCountryProperties testCaseCountryProperties) {
        MessageEvent msg = null;

        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcasecountryproperties (`Test`,`TestCase`,`Country`,`Property`,`Description`,`Type`");
        query.append(",`Database`,`Value1`,`Value2`,`Value3`,`Length`,`RowLimit`,`Nature`,`RetryNb`,`RetryPeriod`,`CacheExpire`,`Rank`, `UsrCreated`)");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        loggingQuery(query.toString());

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());) {

            int i = 1;
            preStat.setString(i++, testCaseCountryProperties.getTest());
            preStat.setString(i++, testCaseCountryProperties.getTestcase());
            preStat.setString(i++, testCaseCountryProperties.getCountry());
            preStat.setString(i++, testCaseCountryProperties.getProperty());
            preStat.setString(i++, testCaseCountryProperties.getDescription());
            preStat.setString(i++, testCaseCountryProperties.getType());
            preStat.setString(i++, testCaseCountryProperties.getDatabase());
            preStat.setString(i++, testCaseCountryProperties.getValue1());
            preStat.setString(i++, testCaseCountryProperties.getValue2());
            preStat.setString(i++, testCaseCountryProperties.getValue3());
            preStat.setString(i++, testCaseCountryProperties.getLength());
            preStat.setInt(i++, testCaseCountryProperties.getRowLimit());
            preStat.setString(i++, testCaseCountryProperties.getNature());
            preStat.setInt(i++, testCaseCountryProperties.getRetryNb());
            preStat.setInt(i++, testCaseCountryProperties.getRetryPeriod());
            preStat.setInt(i++, testCaseCountryProperties.getCacheExpire());
            preStat.setInt(i++, testCaseCountryProperties.getRank());
            preStat.setString(i++, testCaseCountryProperties.getUsrCreated() == null ? "" : testCaseCountryProperties.getUsrCreated());

            preStat.executeUpdate();

            msg = successExecuteQuery(OBJECT_NAME, "INSERT");

        } catch (SQLException exception) {

            if (exception.getSQLState().equals(SQL_DUPLICATED_CODE)) { //23000 is the sql state for duplicate entries
                LOG.error("Unable to execute query : " + exception.toString(), exception);
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_DUPLICATE);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT").replace("%REASON%", exception.toString()));
            } else {
                msg = unexpectedError(exception);
            }

        }
        return new Answer(msg);
    }

    @Override
    public Answer delete(TestCaseCountryProperties object) {
        MessageEvent msg;
        final String query = "DELETE FROM `testcasecountryproperties` WHERE `Test`=? and `TestCase`=? and `Country`=? and `Property`=?";

        loggingQuery(query);

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query);) {

            int i = 1;
            preStat.setString(i++, object.getTest());
            preStat.setString(i++, object.getTestcase());
            preStat.setString(i++, object.getCountry());
            preStat.setString(i++, object.getProperty());

            preStat.executeUpdate();

            msg = successExecuteQuery(OBJECT_NAME, "DELETE");

        } catch (SQLException exception) {
            msg = unexpectedError(exception);
        }
        return new Answer(msg);
    }

    @Override
    public Answer update(TestCaseCountryProperties testCaseCountryProperties) {
        MessageEvent msg;
        StringBuilder query = new StringBuilder();
        query.append("UPDATE testcasecountryproperties SET ");
        query.append("`Description` = ?, ");
        query.append("`Type` = ?, ");
        query.append("`Database` = ?, ");
        query.append("`Value1` = ?, ");
        query.append("`Value2` = ?, ");
        query.append("`Value3` = ?, ");
        query.append("`Length` = ?, ");
        query.append("`RowLimit` = ?, ");
        query.append("`Nature` = ?, ");
        query.append("`RetryNb` = ?, ");
        query.append("`RetryPeriod` = ?, ");
        query.append("`CacheExpire` = ?, ");
        query.append("`Rank` = ?, ");
        query.append("`UsrModif` = ?, ");
        query.append("`DateModif` = CURRENT_TIMESTAMP ");
        query.append("WHERE `Test` = ? AND `TestCase` = ? AND `Country` = ? AND `Property` = ?");

        loggingQuery(query.toString());

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());) {

            int i = 1;
            preStat.setString(i++, testCaseCountryProperties.getDescription());
            preStat.setString(i++, testCaseCountryProperties.getType());
            preStat.setString(i++, testCaseCountryProperties.getDatabase());
            preStat.setString(i++, testCaseCountryProperties.getValue1());
            preStat.setString(i++, testCaseCountryProperties.getValue2());
            preStat.setString(i++, testCaseCountryProperties.getValue3());
            preStat.setString(i++, testCaseCountryProperties.getLength());
            preStat.setInt(i++, testCaseCountryProperties.getRowLimit());
            preStat.setString(i++, testCaseCountryProperties.getNature());
            preStat.setInt(i++, testCaseCountryProperties.getRetryNb());
            preStat.setInt(i++, testCaseCountryProperties.getRetryPeriod());
            preStat.setInt(i++, testCaseCountryProperties.getCacheExpire());
            preStat.setInt(i++, testCaseCountryProperties.getRank());
            preStat.setString(i++, testCaseCountryProperties.getUsrModif() == null ? "" : testCaseCountryProperties.getUsrModif());
            preStat.setString(i++, testCaseCountryProperties.getTest());
            preStat.setString(i++, testCaseCountryProperties.getTestcase());
            preStat.setString(i++, testCaseCountryProperties.getCountry());
            preStat.setString(i++, testCaseCountryProperties.getProperty());

            preStat.executeUpdate();

            msg = successExecuteQuery(OBJECT_NAME, "UPDATE");

        } catch (SQLException exception) {
            msg = unexpectedError(exception);
        }

        return new Answer(msg);
    }

    @Override
    public void updateApplicationObject(String field, String application, String oldObject, String newObject) throws CerberusException {
        final String query = new StringBuilder("UPDATE testcasecountryproperties tcp ")
                .append("INNER JOIN testcase tc ON tc.test = tcp.test AND tc.testcase = tcp.testcase ")
                .append("SET tcp.").append(field).append(" = replace(tcp." + field + ", '%object." + oldObject + ".', '%object." + newObject + ".'), tcp.`dateModif` = CURRENT_TIMESTAMP ")
                .append("where tc.application = ? and tcp.").append(field).append(" like ? ;")
                .toString();

        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL " + query);
            LOG.debug("SQL.param.service " + field);
            LOG.debug("SQL.param.service " + application);
            LOG.debug("SQL.param.service " + "%\\%object." + oldObject + ".%");
        }

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query);) {

            int i = 1;
            preStat.setString(i++, application);
            preStat.setString(i++, "%\\%object." + oldObject + ".%");

            preStat.executeUpdate();
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString());
        }
    }


    @Override
    public TestCaseCountryProperties loadFromResultSet(ResultSet resultSet) throws SQLException {
        return TestCaseCountryProperties.builder()
                .test(resultSet.getString(TestCaseCountryProperties.DB_TEST))
                .testcase(resultSet.getString(TestCaseCountryProperties.DB_TESTCASE))
                .country(resultSet.getString(TestCaseCountryProperties.DB_COUNTRY))
                .property(resultSet.getString(TestCaseCountryProperties.DB_PROPERTY))
                .description(resultSet.getString(TestCaseCountryProperties.DB_DESCRIPTION))
                .type(resultSet.getString(TestCaseCountryProperties.DB_TYPE))
                .database(resultSet.getString(TestCaseCountryProperties.DB_DATABASE))
                .value1(resultSet.getString(TestCaseCountryProperties.DB_VALUE1))
                .value2(resultSet.getString(TestCaseCountryProperties.DB_VALUE2))
                .value3(resultSet.getString(TestCaseCountryProperties.DB_VALUE3))
                .length(resultSet.getString(TestCaseCountryProperties.DB_LENGTH))
                .rowLimit(resultSet.getInt(TestCaseCountryProperties.DB_ROWLIMIT))
                .nature(resultSet.getString(TestCaseCountryProperties.DB_NATURE))
                .retryNb(resultSet.getInt(TestCaseCountryProperties.DB_RETRYNB))
                .retryPeriod(resultSet.getInt(TestCaseCountryProperties.DB_RETRYPERIOD))
                .cacheExpire(resultSet.getInt(TestCaseCountryProperties.DB_CACHEEXPIRE))
                .rank(resultSet.getInt(TestCaseCountryProperties.DB_RANK))
                .dateCreated(resultSet.getTimestamp(TestCaseCountryProperties.DB_DATECREATED))
                .usrCreated(resultSet.getString(TestCaseCountryProperties.DB_USRCREATED))
                .dateModif(resultSet.getTimestamp(TestCaseCountryProperties.DB_DATEMODIF))
                .usrModif(resultSet.getString(TestCaseCountryProperties.DB_USRMODIF))
                .build();
    }

    private MessageEvent unexpectedError(Exception exception) {
        LOG.error("Unable to execute query : " + exception.toString());
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

        return msg;
    }

    private MessageEvent unexpectedError(Exception exception, String description) {
        LOG.error("Unable to execute query : " + exception.toString());
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", description));

        return msg;
    }

    private MessageEvent successExecuteQuery(String item, String operation) {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        msg.setDescription(msg.getDescription().replace("%ITEM%", item).replace("%OPERATION%", operation));

        return msg;
    }

    private void loggingQuery(String query) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
    }

}
