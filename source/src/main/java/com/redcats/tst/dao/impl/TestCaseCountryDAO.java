package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.ITestCaseCountryDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.MessageGeneral;
import com.redcats.tst.entity.MessageGeneralEnum;
import com.redcats.tst.entity.TestCaseCountry;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryTestCaseCountry;
import com.redcats.tst.log.MyLogger;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
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
public class TestCaseCountryDAO implements ITestCaseCountryDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseCountry factoryTestCaseCountry;

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
    public TestCaseCountry findTestCaseCountryByKey(String test, String testCase, String country) throws CerberusException {
        final String query = "SELECT * FROM testcasecountry WHERE test = ? AND testcase = ? AND country = ? ";
        TestCaseCountry result = null;
        boolean throwException = false;

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);
                preStat.setString(3, country);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = factoryTestCaseCountry.create(test, testCase, country);
                    } else {
                        throwException = true;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseCountryDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseCountryDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwException) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

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
    public List<TestCaseCountry> findTestCaseCountryByTestTestCase(String test, String testCase) {
        List<TestCaseCountry> result = null;
        final String query = "SELECT * FROM testcasecountry WHERE test = ? AND testcase = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    result = new ArrayList<TestCaseCountry>();

                    while (resultSet.next()) {
                        String country = resultSet.getString("Country");
                        result.add(factoryTestCaseCountry.create(test, testCase, country));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseCountryDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseCountryDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }
}
