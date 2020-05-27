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

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.crud.dao.ITestCaseCountryPropertiesDAO;
import org.cerberus.crud.utils.RequestDbUtils;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.dto.PropertyListDTO;
import org.cerberus.dto.TestCaseListDTO;
import org.cerberus.dto.TestListDTO;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryTestCaseCountryProperties;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseCountryProperties factoryTestCaseCountryProperties;

    private static final Logger LOG = LogManager.getLogger(TestCaseCountryPropertiesDAO.class);

    private final String OBJECT_NAME = "TestCaseCountryProperties";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public List<TestCaseCountryProperties> findListOfPropertyPerTestTestCase(String test, String testcase) throws CerberusException {
        final String query = "SELECT * FROM testcasecountryproperties tcp WHERE test = ? AND testcase = ? ";
        // TestCase dependency should only be used on testcasedataexecution.
        // In other words, when a test case is linked to another testcase, it should have access to its data at execution level but should not inherit from testcase property definition.
        // As a consequece this method should not return properties from dependencies.
//                "OR exists (select 1 from  testcasedep  where DepTest = tcp.Test AND DepTestCase = tcp.TestCase AND Test = ? AND TestCase = ?)"; // Manage tc dependencies

        return RequestDbUtils.executeQueryList(databaseSpring, query,
                ps -> {
                    ps.setString(1, test);
                    ps.setString(2, testcase);
                    //ps.setString(3, test);
                    //ps.setString(4, testcase);
                },
                resultSet -> {
                    String country = resultSet.getString("country");
                    String property = resultSet.getString("property");
                    String description = resultSet.getString("description");
                    String type = resultSet.getString("type");
                    String database = resultSet.getString("database");
                    String value1 = resultSet.getString("value1");
                    String value2 = resultSet.getString("value2");
                    String length = resultSet.getString("length");
                    int rowLimit = resultSet.getInt("rowLimit");
                    String nature = resultSet.getString("nature");
                    int retryNb = resultSet.getInt("RetryNb");
                    int retryPeriod = resultSet.getInt("RetryPeriod");
                    int cacheExpire = resultSet.getInt("CacheExpire");
                    int rank = resultSet.getInt("Rank");
                    return factoryTestCaseCountryProperties.create(test, testcase, country, property, description, type, database, value1, value2, length, rowLimit, nature, retryNb, retryPeriod, cacheExpire, rank);
                }
        );

    }

    @Override
    public List<TestCaseCountryProperties> findOnePropertyPerTestTestCase(String test, String testcase, String oneproperty) {
        List<TestCaseCountryProperties> list = null;
        final String query = "SELECT * FROM testcasecountryproperties WHERE test = ? AND testcase = ? AND property = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                preStat.setString(3, oneproperty);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<TestCaseCountryProperties>();

                    while (resultSet.next()) {
                        String country = resultSet.getString("country");
                        String property = resultSet.getString("property");
                        String description = resultSet.getString("description");
                        String type = resultSet.getString("type");
                        String database = resultSet.getString("database");
                        String value1 = resultSet.getString("value1");
                        String value2 = resultSet.getString("value2");
                        String length = resultSet.getString("length");
                        int rowLimit = resultSet.getInt("rowLimit");
                        String nature = resultSet.getString("nature");
                        int retryNb = resultSet.getInt("RetryNb");
                        int retryPeriod = resultSet.getInt("RetryPeriod");
                        int cacheExpire = resultSet.getInt("CacheExpire");
                        int rank = resultSet.getInt("Rank");
                        list.add(factoryTestCaseCountryProperties.create(test, testcase, country, property, description, type, database, value1, value2, length, rowLimit, nature, retryNb, retryPeriod, cacheExpire, rank));

                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn(exception.toString());
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
                        String description = resultSet.getString("description");
                        String type = resultSet.getString("type");
                        String database = resultSet.getString("database");
                        String value1 = resultSet.getString("value1");
                        String value2 = resultSet.getString("value2");
                        String length = resultSet.getString("length");
                        int rowLimit = resultSet.getInt("rowLimit");
                        String nature = resultSet.getString("nature");
                        int retryNb = resultSet.getInt("RetryNb");
                        int retryPeriod = resultSet.getInt("RetryPeriod");
                        int cacheExpire = resultSet.getInt("CacheExpire");
                        int rank = resultSet.getInt("Rank");
                        listProperties.add(factoryTestCaseCountryProperties.create(test, testcase, country, property, description, type, database, value1, value2, length, rowLimit, nature, retryNb, retryPeriod, cacheExpire, rank));

                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
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

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testCaseCountryProperties.getTest());
                preStat.setString(2, testCaseCountryProperties.getTestCase());
                preStat.setBytes(3, testCaseCountryProperties.getProperty().getBytes("UTF-8"));
                preStat.setString(4, testCaseCountryProperties.getType());
                preStat.setString(5, testCaseCountryProperties.getDatabase());
                preStat.setBytes(6, testCaseCountryProperties.getValue1().getBytes("UTF-8"));
                preStat.setBytes(7, testCaseCountryProperties.getValue2().getBytes("UTF-8"));
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
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } catch (UnsupportedEncodingException ex) {
                LOG.error(ex.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        return list;
    }

    @Override
    public List<TestCaseCountryProperties> findListOfPropertyPerTestTestCaseCountry(String test, String testcase, String country) {
        List<TestCaseCountryProperties> list = null;
        final String query = "SELECT * FROM testcasecountryproperties WHERE test = ? AND testcase = ? AND country = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.test : " + test);
            LOG.debug("SQL.param.testcase : " + testcase);
            LOG.debug("SQL.param.country : " + country);
        }

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
                        String description = resultSet.getString("description");
                        String type = resultSet.getString("type");
                        String database = resultSet.getString("database");
                        String value1 = resultSet.getString("value1");
                        String value2 = resultSet.getString("value2");
                        String length = resultSet.getString("length");
                        int rowLimit = resultSet.getInt("rowLimit");
                        String nature = resultSet.getString("nature");
                        int retryNb = resultSet.getInt("RetryNb");
                        int retryPeriod = resultSet.getInt("RetryPeriod");
                        int cacheExpire = resultSet.getInt("CacheExpire");
                        int rank = resultSet.getInt("Rank");
                        list.add(factoryTestCaseCountryProperties.create(test, testcase, country, property, description, type, database, value1, value2, length, rowLimit, nature, retryNb, retryPeriod, cacheExpire, rank));
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
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
                preStat.setBytes(4, property.getBytes("UTF-8"));

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        String description = resultSet.getString("description");
                        String type = resultSet.getString("type");
                        String database = resultSet.getString("database");
                        String value1 = resultSet.getString("value1");
                        String value2 = resultSet.getString("value2");
                        String length = resultSet.getString("length");
                        int rowLimit = resultSet.getInt("rowLimit");
                        String nature = resultSet.getString("nature");
                        int retryNb = resultSet.getInt("RetryNb");
                        int retryPeriod = resultSet.getInt("RetryPeriod");
                        int cacheExpire = resultSet.getInt("CacheExpire");
                        int rank = resultSet.getInt("Rank");
                        result = factoryTestCaseCountryProperties.create(test, testcase, country, property, description, type, database, value1, value2, length, rowLimit, nature, retryNb, retryPeriod, cacheExpire, rank);
                    } else {
                        throwException = true;
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } catch (UnsupportedEncodingException ex) {
                LOG.error(ex.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
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
        query.append("INSERT INTO testcasecountryproperties (`Test`,`TestCase`,`Country`,`Property` ,`Description`,`Type`");
        query.append(",`Database`,`Value1`,`Value2`,`Length`,`RowLimit`,`Nature`,`RetryNb`,`RetryPeriod`,`Rank`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testCaseCountryProperties.getTest());
                preStat.setString(2, testCaseCountryProperties.getTestCase());
                preStat.setString(3, testCaseCountryProperties.getCountry());
                preStat.setBytes(4, testCaseCountryProperties.getProperty().getBytes("UTF-8"));
                preStat.setBytes(5, testCaseCountryProperties.getDescription().getBytes("UTF-8"));
                preStat.setString(6, testCaseCountryProperties.getType());
                preStat.setString(7, testCaseCountryProperties.getDatabase());
                preStat.setBytes(8, testCaseCountryProperties.getValue1().getBytes("UTF-8"));
                preStat.setBytes(9, testCaseCountryProperties.getValue2().getBytes("UTF-8"));
                preStat.setString(10, testCaseCountryProperties.getLength());
                preStat.setInt(11, testCaseCountryProperties.getRowLimit());
                preStat.setString(12, testCaseCountryProperties.getNature());
                preStat.setInt(13, testCaseCountryProperties.getRetryNb());
                preStat.setInt(14, testCaseCountryProperties.getRetryPeriod());
                preStat.setInt(15, testCaseCountryProperties.getRank());

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } catch (UnsupportedEncodingException ex) {
                LOG.error(ex.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
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
        query.append(" `Description` = ?, `Type` = ? ,`Database` = ? ,Value1 = ?,Value2 = ?,`Length` = ?,  RowLimit = ?,  `Nature` = ? ,  `RetryNb` = ? ,  `RetryPeriod` = ? , `Rank` = ? ");
        query.append(" WHERE Test = ? AND TestCase = ? AND Country = ? AND hex(`Property`) like hex(?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setBytes(1, testCaseCountryProperties.getDescription().getBytes("UTF-8"));
                preStat.setString(2, testCaseCountryProperties.getType());
                preStat.setString(3, testCaseCountryProperties.getDatabase());
                preStat.setBytes(4, testCaseCountryProperties.getValue1().getBytes("UTF-8"));
                preStat.setBytes(5, testCaseCountryProperties.getValue2().getBytes("UTF-8"));
                preStat.setString(6, testCaseCountryProperties.getLength());
                preStat.setInt(7, testCaseCountryProperties.getRowLimit());
                preStat.setString(8, testCaseCountryProperties.getNature());
                preStat.setInt(9, testCaseCountryProperties.getRetryNb());
                preStat.setInt(10, testCaseCountryProperties.getRetryPeriod());
                preStat.setString(11, testCaseCountryProperties.getTest());
                preStat.setString(12, testCaseCountryProperties.getTestCase());
                preStat.setString(13, testCaseCountryProperties.getCountry());
                preStat.setBytes(14, testCaseCountryProperties.getProperty().getBytes("UTF-8"));
                preStat.setInt(15, testCaseCountryProperties.getRank());

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } catch (UnsupportedEncodingException ex) {
                LOG.error(ex.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public Answer bulkRenameProperties(String oldName, String newName) {
        Answer answer = new Answer();
        MessageEvent msg;

        String query = "UPDATE testcasecountryproperties SET `Value1`=? ";
        query += "WHERE `Type` = 'getFromDataLib' AND `Value1`=?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }

        try (Connection connection = this.databaseSpring.connect()) {
            try (PreparedStatement preStat = connection.prepareStatement(query)) {
                int i = 1;
                preStat.setString(i++, newName);
                preStat.setString(i++, oldName);
                int rowsUpdated = preStat.executeUpdate();

                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                // Message to customize : X properties updated using the rowsUpdated variable
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
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString(), exception);
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

        }
        answer.setResultMessage(msg);
        return answer;
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
                preStat.setBytes(3, property.getBytes("UTF-8"));

                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        String country = resultSet.getString("country");
                        if (country != null && !"".equals(country)) {
                            result.add(country);
                        }
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } catch (UnsupportedEncodingException ex) {
                LOG.error(ex.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
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
                preStat.setBytes(4, tccp.getProperty().getBytes("UTF-8"));

                throwExcep = preStat.executeUpdate() == 0;
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
            } catch (UnsupportedEncodingException ex) {
                LOG.error(ex.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public AnswerList<TestListDTO> findTestCaseCountryPropertiesByValue1(int testDataLib, String name, String country, String propertyType) {
        AnswerList<TestListDTO> ansList = new AnswerList<>();
        MessageEvent rs;
        List<TestListDTO> listOfTests = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query.append("select count(*) as total, tccp.property, t.Test, tc.TestCase, t.Description as testDescription, tc.Description as testCaseDescription, tc.Application, ");
        query.append("tc.isActive, tc.`Group`, tc.UsrCreated, tc.`Status` ");
        query.append("from testcasecountryproperties tccp    ");
        query.append("inner join test t on t.test = tccp.test ");
        query.append("inner join testcase tc  on t.test = tccp.test  and t.test = tc.test ");
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
                try {
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

                            testCaseDTO.setGroup(resultSet.getString("Group"));
                            testCaseDTO.setIsActive(resultSet.getString("isActive"));
                            testList.getTestCaseList().add(testCaseDTO);
                            map.put(test, testList);
                            propertiesList = new ArrayList<PropertyListDTO>();
                        } else {
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
                    for (TestListDTO list : listOfTests) {
                        for (TestCaseListDTO cases : list.getTestCaseList()) {
                            cases.setPropertiesList(auxiliaryMap.get(list.getTest() + ":" + cases.getTestCaseNumber()));
                        }
                    }
                    if (listOfTests.isEmpty()) {
                        rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        rs.setDescription(rs.getDescription().replace("%ITEM%", "List of Test Cases").replace("%OPERATION%", "Select"));
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "Unable to get the list of test cases."));
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "Unable to get the list of test cases."));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "Unable to get the list of test cases."));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        ansList.setResultMessage(rs);
        ansList.setDataList(listOfTests);

        return ansList;
    }

    @Override
    public Answer createTestCaseCountryPropertiesBatch(List<TestCaseCountryProperties> listOfPropertiesToInsert) {
        Answer answer = new Answer();
        MessageEvent rs = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcasecountryproperties (`Test`,`TestCase`,`Country`,`Property` , `Description`, `Type`");
        query.append(",`Database`,`Value1`,`Value2`,`Length`,`RowLimit`,`Nature`,`RetryNb`,`RetryPeriod`, `Rank`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                for (TestCaseCountryProperties prop : listOfPropertiesToInsert) {
                    preStat.setString(1, prop.getTest());
                    preStat.setString(2, prop.getTestCase());
                    preStat.setString(3, prop.getCountry());
                    preStat.setString(4, prop.getProperty());
                    preStat.setString(5, prop.getDescription());
                    preStat.setString(6, prop.getType());
                    preStat.setString(7, prop.getDatabase());
                    preStat.setString(8, prop.getValue1());
                    preStat.setString(9, prop.getValue2());
                    preStat.setString(10, prop.getLength());
                    preStat.setInt(11, prop.getRowLimit());
                    preStat.setString(12, prop.getNature());
                    preStat.setInt(13, prop.getRetryNb());
                    preStat.setInt(14, prop.getRetryPeriod());
                    preStat.setInt(15, prop.getRank());

                    preStat.addBatch();
                }

                //executes the batch
                preStat.executeBatch();

                int affectedRows[] = preStat.executeBatch();

                //verify if some of the statements failed
                boolean someFailed = ArrayUtils.contains(affectedRows, 0) || ArrayUtils.contains(affectedRows, Statement.EXECUTE_FAILED);

                if (someFailed == false) {
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    rs.setDescription(rs.getDescription().replace("%ITEM%", "Property").replace("%OPERATION%", "CREATE"));
                } else {
                    rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                    rs.setDescription(rs.getDescription().replace("%ITEM%", "Property").replace("%OPERATION%", "CREATE").
                            replace("%REASON%", "Some problem occurred while creating the new property! "));
                }

            } catch (SQLException exception) {
                rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "It was not possible to update table."));
                LOG.error("Unable to execute query : " + exception.toString());
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", "It was not possible to update table."));
            LOG.error("Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }

        answer.setResultMessage(rs);
        return answer;
    }

    @Override
    public Answer create(TestCaseCountryProperties object) {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcasecountryproperties (`Test`,`TestCase`,`Country`,`Property`,`Description`,`Type`");
        query.append(",`Database`,`Value1`,`Value2`,`Length`,`RowLimit`,`Nature`,`RetryNb`,`RetryPeriod`,`CacheExpire`,`Rank`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, object.getTest());
                preStat.setString(2, object.getTestCase());
                preStat.setString(3, object.getCountry());
                preStat.setString(4, object.getProperty());
                preStat.setString(5, object.getDescription());
                preStat.setString(6, object.getType());
                preStat.setString(7, object.getDatabase());
                preStat.setString(8, object.getValue1());
                preStat.setString(9, object.getValue2());
                preStat.setString(10, object.getLength());
                preStat.setInt(11, object.getRowLimit());
                preStat.setString(12, object.getNature());
                preStat.setInt(13, object.getRetryNb());
                preStat.setInt(14, object.getRetryPeriod());
                preStat.setInt(15, object.getCacheExpire());
                preStat.setInt(16, object.getRank());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString(), exception);

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
            LOG.error("Unable to execute query : " + exception.toString(), exception);
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to close connection : " + exception.toString(), exception);
            }
        }
        return new Answer(msg);
    }

    @Override
    public Answer delete(TestCaseCountryProperties object) {
        MessageEvent msg = null;
        final String query = "DELETE FROM `testcasecountryproperties` WHERE `Test`=? and `TestCase`=? and `Country`=? and `Property`=?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, object.getTest());
                preStat.setString(2, object.getTestCase());
                preStat.setString(3, object.getCountry());
                preStat.setString(4, object.getProperty());

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

    @Override
    public Answer update(TestCaseCountryProperties object) {
        MessageEvent msg = null;
        final String query = "UPDATE testcasecountryproperties SET `Description` = ?, `Type` = ? ,`Database` = ? ,Value1 = ?, Value2 = ?,`Length` = ?,  RowLimit = ?,  `Nature` = ?,  `RetryNb` = ?,  `RetryPeriod` = ? , `CacheExpire` = ?, `Rank` = ? WHERE Test = ? AND TestCase = ? AND Country = ? AND hex(`Property`) like hex(?)";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, object.getDescription());
                preStat.setString(2, object.getType());
                preStat.setString(3, object.getDatabase());
                preStat.setString(4, object.getValue1());
                preStat.setString(5, object.getValue2());
                preStat.setString(6, object.getLength());
                preStat.setInt(7, object.getRowLimit());
                preStat.setString(8, object.getNature());
                preStat.setInt(9, object.getRetryNb());
                preStat.setInt(10, object.getRetryPeriod());
                preStat.setInt(11, object.getCacheExpire());
                preStat.setInt(12, object.getRank());
                preStat.setString(13, object.getTest());
                preStat.setString(14, object.getTestCase());
                preStat.setString(15, object.getCountry());
                preStat.setString(16, object.getProperty());

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

}
