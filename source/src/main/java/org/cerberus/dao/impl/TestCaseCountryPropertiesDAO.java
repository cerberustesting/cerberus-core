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
import org.cerberus.dao.ITestCaseCountryPropertiesDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.TestCaseCountryProperties;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTestCaseCountryProperties;
import org.cerberus.log.MyLogger;
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
        query.append(" group by `property`, `type`, `database`, `value1`, `value2`, `length`, `rowlimit`, `nature`");

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
        query.append(" AND `property` = ? AND `type` = ? AND `database` = ? AND `value1` = ? AND `value2` = ? AND `length` = ?");
        query.append(" AND `rowlimit` = ? AND `nature` = ?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testCaseCountryProperties.getTest());
                preStat.setString(2, testCaseCountryProperties.getTestCase());
                preStat.setString(3, testCaseCountryProperties.getProperty());
                preStat.setString(4, testCaseCountryProperties.getType());
                preStat.setString(5, testCaseCountryProperties.getDatabase());
                preStat.setString(6, testCaseCountryProperties.getValue1());
                preStat.setString(7, testCaseCountryProperties.getValue2());
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
        final String query = "SELECT * FROM testcasecountryproperties WHERE test = ? AND testcase = ? AND country = ? AND property = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                preStat.setString(3, country);
                preStat.setString(4, property);

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
        query.append(" WHERE Test = ? AND TestCase = ? AND Country = ? AND Property = ? ");

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
                preStat.setString(11, testCaseCountryProperties.getProperty());
                

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

        final String query = "SELECT country FROM testcasecountryproperties WHERE test = ? AND testcase = ? AND property = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                preStat.setString(3, property);

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
        final String query = "DELETE FROM testcasecountryproperties WHERE test = ? and testcase = ? and country = ? and property = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, tccp.getTest());
                preStat.setString(2, tccp.getTestCase());
                preStat.setString(3, tccp.getCountry());
                preStat.setString(4, tccp.getProperty());

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
}
