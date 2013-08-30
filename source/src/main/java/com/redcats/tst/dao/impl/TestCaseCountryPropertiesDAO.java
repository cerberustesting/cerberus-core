package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.ITestCaseCountryPropertiesDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.MessageEventEnum;
import com.redcats.tst.entity.MessageGeneral;
import com.redcats.tst.entity.MessageGeneralEnum;
import com.redcats.tst.entity.TestCaseCountryProperties;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryTestCaseCountryProperties;
import com.redcats.tst.log.MyLogger;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestCaseCountryProperties>();
                try {
                    while (resultSet.next()) {
                        String country = resultSet.getString("country");
                        String property = resultSet.getString("property");
                        String type = resultSet.getString("type");
                        String database = resultSet.getString("database");
                        String value = resultSet.getString("value");
                        int length = resultSet.getInt("length");
                        int rowLimit = resultSet.getInt("rowLimit");
                        String nature = resultSet.getString("nature");
                        list.add(factoryTestCaseCountryProperties.create(test, testcase, country, property, type, database, value, length, rowLimit, nature));

                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            this.databaseSpring.disconnect();
        }
        return list;
    }

    @Override
    public List<TestCaseCountryProperties> findDistinctPropertiesOfTestCase(String test, String testcase) {
        List<TestCaseCountryProperties> listProperties = null;
        final StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcasecountryproperties WHERE test = ? AND testcase = ?");
        query.append(" group by `property`, `type`, `database`, `value`, `length`, `rowlimit`, `nature`");

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query.toString());
            preStat.setString(1, test);
            preStat.setString(2, testcase);
            try {
                ResultSet resultSet = preStat.executeQuery();
                listProperties = new ArrayList<TestCaseCountryProperties>();
                try {
                    while (resultSet.next()) {
                        String country = resultSet.getString("country");
                        String property = resultSet.getString("property");
                        String type = resultSet.getString("type");
                        String database = resultSet.getString("database");
                        String value = resultSet.getString("value");
                        int length = resultSet.getInt("length");
                        int rowLimit = resultSet.getInt("rowLimit");
                        String nature = resultSet.getString("nature");
                        listProperties.add(factoryTestCaseCountryProperties.create(test, testcase, country, property, type, database, value, length, rowLimit, nature));

                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            this.databaseSpring.disconnect();
        }
        return listProperties;
    }

    @Override
    public List<String> findCountryByProperty(TestCaseCountryProperties testCaseCountryProperties) {
        List<String> list = null;
        final StringBuilder query = new StringBuilder();
        query.append("SELECT country FROM testcasecountryproperties WHERE test = ? AND testcase = ?");
        query.append(" AND `property` = ? AND `type` = ? AND `database` = ? AND `value` = ? AND `length` = ?");
        query.append(" AND `rowlimit` = ? AND `nature` = ?");

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query.toString());
            preStat.setString(1, testCaseCountryProperties.getTest());
            preStat.setString(2, testCaseCountryProperties.getTestCase());
            preStat.setString(3, testCaseCountryProperties.getProperty());
            preStat.setString(4, testCaseCountryProperties.getType());
            preStat.setString(5, testCaseCountryProperties.getDatabase());
            preStat.setString(6, testCaseCountryProperties.getValue());
            preStat.setString(7, String.valueOf(testCaseCountryProperties.getLength()));
            preStat.setString(8, String.valueOf(testCaseCountryProperties.getRowLimit()));
            preStat.setString(9, testCaseCountryProperties.getNature());
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<String>();
                String valueToAdd;
                try {
                    while (resultSet.next()) {
                        valueToAdd = resultSet.getString("Country") == null ? "" : resultSet.getString("Country");
                        list.add(valueToAdd);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            this.databaseSpring.disconnect();
        }
        return list;
    }

    @Override
    public List<TestCaseCountryProperties> findListOfPropertyPerTestTestCaseCountry(String test, String testcase, String country) {
        List<TestCaseCountryProperties> list = null;
        final String query = "SELECT * FROM testcasecountryproperties WHERE test = ? AND testcase = ? AND country = ?";

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setString(1, test);
            preStat.setString(2, testcase);
            preStat.setString(3, country);
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestCaseCountryProperties>();
                try {
                    while (resultSet.next()) {
                        String property = resultSet.getString("property");
                        String type = resultSet.getString("type");
                        String database = resultSet.getString("database");
                        String value = resultSet.getString("value");
                        int length = resultSet.getInt("length");
                        int rowLimit = resultSet.getInt("rowLimit");
                        String nature = resultSet.getString("nature");
                        MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.DEBUG, "Found Test Case Country property : " + test + "-" + testcase + "-" + country + "-" + property);
                        list.add(factoryTestCaseCountryProperties.create(test, testcase, country, property, type, database, value, length, rowLimit, nature));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            this.databaseSpring.disconnect();
        }
        return list;
    }

    @Override
    public TestCaseCountryProperties findTestCaseCountryPropertiesByKey(String test, String testcase, String country, String property) throws CerberusException {
        TestCaseCountryProperties result = null;
        boolean throwException = false;
        final String query = "SELECT * FROM testcasecountryproperties WHERE test = ? AND testcase = ? AND country = ? AND property = ?";

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setString(1, test);
            preStat.setString(2, testcase);
            preStat.setString(3, country);
            preStat.setString(4, property);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        String type = resultSet.getString("type");
                        String database = resultSet.getString("database");
                        String value = resultSet.getString("value");
                        int length = resultSet.getInt("length");
                        int rowLimit = resultSet.getInt("rowLimit");
                        String nature = resultSet.getString("nature");
                        result = factoryTestCaseCountryProperties.create(test, testcase, country, property, type, database, value, length, rowLimit, nature);
                    } else {
                        throwException = true;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryPropertiesDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            this.databaseSpring.disconnect();
        }
        if (throwException) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }
}
