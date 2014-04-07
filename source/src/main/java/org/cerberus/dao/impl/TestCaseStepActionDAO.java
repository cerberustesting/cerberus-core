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
import org.cerberus.dao.ITestCaseStepActionDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.TestCaseStepAction;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTestCaseStepAction;
import org.cerberus.log.MyLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 31/12/2012
 * @since 2.0.0
 */
@Repository
public class TestCaseStepActionDAO implements ITestCaseStepActionDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseStepAction factoryTestCaseStepAction;

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
    public TestCaseStepAction findTestCaseStepActionbyKey(String test, String testCase, int step, int sequence) {
        TestCaseStepAction testCaseStepAction = null;
        final String query = "SELECT * FROM testcasestepaction WHERE test = ? AND testcase = ? AND step = ? AND sequence = ? ORDER BY step, sequence";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);
                preStat.setInt(3, step);
                preStat.setInt(4, sequence);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        String action = resultSet.getString("Action");
                        String object = resultSet.getString("Object");
                        String property = resultSet.getString("Property");
                        String description = resultSet.getString("Description");
                        testCaseStepAction = factoryTestCaseStepAction.create(test, testCase, step, sequence, action, object, property, description);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return testCaseStepAction;
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
    public List<TestCaseStepAction> findActionByTestTestCaseStep(String test, String testcase, int stepNumber) {
        List<TestCaseStepAction> list = null;
        final String query = "SELECT * FROM testcasestepaction WHERE test = ? AND testcase = ? AND step = ? ORDER BY step, sequence";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                preStat.setInt(3, stepNumber);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<TestCaseStepAction>();
                    while (resultSet.next()) {
                        int step = resultSet.getInt("Step");
                        int sequence = resultSet.getInt("Sequence");
                        String action = resultSet.getString("Action");
                        String object = resultSet.getString("Object");
                        String property = resultSet.getString("Property");
                        String description = resultSet.getString("Description");
                        list.add(factoryTestCaseStepAction.create(test, testcase, step, sequence, action, object, property, description));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public void insertTestCaseStepAction(TestCaseStepAction testCaseStepAction) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcasestepaction (`test`, `testCase`, `step`, `sequence`, `action`, `object`, `property`, `description`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?)");
        
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testCaseStepAction.getTest());
                preStat.setString(2, testCaseStepAction.getTestCase());
                preStat.setInt(3, testCaseStepAction.getStep());
                preStat.setInt(4, testCaseStepAction.getSequence());
                preStat.setString(5, testCaseStepAction.getAction());
                preStat.setString(6, testCaseStepAction.getObject());
                preStat.setString(7, testCaseStepAction.getProperty());
                preStat.setString(8, testCaseStepAction.getDescription());

                preStat.executeUpdate();
                throwExcep = false;
                
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    private TestCaseStepAction loadTestCaseStepActionFromResultSet(ResultSet resultSet) throws SQLException {
        String test = resultSet.getString("Test");
        String testCase = resultSet.getString("TestCase");
        Integer step = resultSet.getInt("Step");
        Integer sequence = resultSet.getInt("Sequence");
        String action = resultSet.getString("Action");
        String object = resultSet.getString("Object");
        String property = resultSet.getString("Property");
        String description = resultSet.getString("description");
        
        return factoryTestCaseStepAction.create(test, testCase, step, sequence, action, object, property, description);
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
    public boolean changeTestCaseStepActionSequence(String test, String testCase, int step, int oldSequence, int newSequence) {
        TestCaseStepAction testCaseStepAction = null;
        final String query = "update testcasestepaction set sequence = ? WHERE test = ? AND testcase = ? AND step = ? AND sequence = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setInt(1, newSequence);
                preStat.setString(2, test);
                preStat.setString(3, testCase);
                preStat.setInt(4, step);
                preStat.setInt(5, oldSequence);

                int lines = preStat.executeUpdate();
                return (lines > 0);
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return false;
    }

}
