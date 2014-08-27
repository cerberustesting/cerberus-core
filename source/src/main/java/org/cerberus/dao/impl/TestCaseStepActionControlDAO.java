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
import org.cerberus.dao.ITestCaseStepActionControlDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.TestCaseStepActionControl;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTestCaseStepActionControl;
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
public class TestCaseStepActionControlDAO implements ITestCaseStepActionControlDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseStepActionControl factoryTestCaseStepActionControl;

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
    public TestCaseStepActionControl findTestCaseStepActionControlByKey(String test, String testcase, int stepNumber, int sequence, int control) {
        TestCaseStepActionControl actionControl = null;
        final String query = "SELECT * FROM testcasestepactioncontrol WHERE test = ? AND testcase = ? AND step = ? AND sequence = ? AND control = ? ORDER BY control";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                preStat.setInt(3, stepNumber);
                preStat.setInt(4, sequence);
                preStat.setInt(5, control);

                ResultSet resultSet = preStat.executeQuery();
                try {

                    if (resultSet.first()) {
                        String type = resultSet.getString("Type");
                        String object = resultSet.getString("ControlValue");
                        String property = resultSet.getString("ControlProperty");
                        String fatal = resultSet.getString("Fatal");
                        String description = resultSet.getString("ControlDescription");
                        actionControl = factoryTestCaseStepActionControl.create(test, testcase, stepNumber, sequence, control, type, object, property, fatal, description);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return actionControl;
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
    public List<TestCaseStepActionControl> findControlByTestTestCaseStepSequence(String test, String testcase, int stepNumber, int sequence) {
        List<TestCaseStepActionControl> list = null;
        final String query = "SELECT * FROM testcasestepactioncontrol WHERE test = ? AND testcase = ? AND step = ? AND sequence = ? ORDER BY control";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                preStat.setInt(3, stepNumber);
                preStat.setInt(4, sequence);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<TestCaseStepActionControl>();

                    while (resultSet.next()) {
                        int step = resultSet.getInt("Step");
                        int control = resultSet.getInt("Control");
                        String type = resultSet.getString("Type");
                        String object = resultSet.getString("ControlValue");
                        String property = resultSet.getString("ControlProperty");
                        String fatal = resultSet.getString("Fatal");
                        String description = resultSet.getString("ControlDescription");
                        list.add(factoryTestCaseStepActionControl.create(test, testcase, step, sequence, control, type, object, property, fatal, description));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
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
    public void insertTestCaseStepActionControl(TestCaseStepActionControl testCaseStepActionControl) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcasestepactioncontrol (`test`, `testCase`, `step`, `sequence`, `control`, `type`, `controlvalue`, `controlproperty`, `fatal`, `ControlDescription`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?)");
        
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testCaseStepActionControl.getTest());
                preStat.setString(2, testCaseStepActionControl.getTestCase());
                preStat.setInt(3, testCaseStepActionControl.getStep());
                preStat.setInt(4, testCaseStepActionControl.getSequence());
                preStat.setInt(5, testCaseStepActionControl.getControl());
                preStat.setString(6, testCaseStepActionControl.getType());
                preStat.setString(7, testCaseStepActionControl.getControlValue());
                preStat.setString(8, testCaseStepActionControl.getControlProperty());
                preStat.setString(9, testCaseStepActionControl.getFatal());
                preStat.setString(10, testCaseStepActionControl.getDescription());

                preStat.executeUpdate();
                throwExcep = false;
                
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }}

    @Override
    public List<TestCaseStepActionControl> findControlByTestTestCaseStep(String test, String testcase, int step) {
        List<TestCaseStepActionControl> list = null;
        final String query = "SELECT * FROM testcasestepactioncontrol WHERE test = ? AND testcase = ? AND step = ? ORDER BY control";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                preStat.setInt(3, step);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<TestCaseStepActionControl>();

                    while (resultSet.next()) {
                        int sequence = resultSet.getInt("Sequence");
                        int control = resultSet.getInt("Control");
                        String type = resultSet.getString("Type");
                        String object = resultSet.getString("ControlValue");
                        String property = resultSet.getString("ControlProperty");
                        String fatal = resultSet.getString("Fatal");
                        String description = resultSet.getString("ControlDescription");
                        list.add(factoryTestCaseStepActionControl.create(test, testcase, step, sequence, control, type, object, property, fatal, description));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
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
    public void updateTestCaseStepActionControl(TestCaseStepActionControl testCaseStepActionControl) throws CerberusException {
        boolean throwExcep = false;
        final String query = new StringBuilder("UPDATE `testcasestepactioncontrol` SET ")
                .append("`Test` = ?, ")
                .append("`TestCase` = ?, ")
                .append("`Step` = ?, ")
                .append("`Sequence` = ?, ")
                .append("`Control` = ?, ")
                .append("`Type` = ?, ")
                .append("`ControlValue` = ?, ")
                .append("`ControlProperty` = ?, ")
                .append("`ControlDescription` = ?>, ")
                .append("`Fatal` = ? ")
                .append("WHERE `Test` = ? AND `TestCase` = ? AND `Step` = ? AND `Sequence` = ? AND `Control` = ? ")
                .toString();

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testCaseStepActionControl.getTest());
                preStat.setString(2, testCaseStepActionControl.getTestCase());
                preStat.setInt(3, testCaseStepActionControl.getStep());
                preStat.setInt(4, testCaseStepActionControl.getSequence());
                preStat.setInt(5, testCaseStepActionControl.getControl());
                preStat.setString(6, testCaseStepActionControl.getType());
                preStat.setString(7, testCaseStepActionControl.getControlValue());
                preStat.setString(8, testCaseStepActionControl.getControlProperty());
                preStat.setString(9, testCaseStepActionControl.getFatal());
                preStat.setString(10, testCaseStepActionControl.getDescription());

                preStat.setString(11, testCaseStepActionControl.getTest());
                preStat.setString(12, testCaseStepActionControl.getTestCase());
                preStat.setInt(13, testCaseStepActionControl.getStep());
                preStat.setInt(14, testCaseStepActionControl.getSequence());
                preStat.setInt(15, testCaseStepActionControl.getControl());

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void deleteTestCaseStepActionControl(TestCaseStepActionControl tcsac) throws CerberusException {
        boolean throwExcep = false;
        final String query = "DELETE FROM testcasestepactioncontrol WHERE test = ? and testcase = ? and step = ? and `sequence` = ? and `control` = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, tcsac.getTest());
                preStat.setString(2, tcsac.getTestCase());
                preStat.setInt(3, tcsac.getStep());
                preStat.setInt(4, tcsac.getSequence());
                preStat.setInt(5, tcsac.getControl());

                throwExcep = preStat.executeUpdate() == 0;
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public List<TestCaseStepActionControl> findControlByTestTestCase(String test, String testCase) throws CerberusException {
        List<TestCaseStepActionControl> list = null;
        final String query = "SELECT * FROM testcasestepactioncontrol WHERE test = ? AND testcase = ? ORDER BY step, sequence, control";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<TestCaseStepActionControl>();

                    while (resultSet.next()) {
                        int step = resultSet.getInt("Step");
                        int sequence = resultSet.getInt("Sequence");
                        int control = resultSet.getInt("Control");
                        String type = resultSet.getString("Type");
                        String object = resultSet.getString("ControlValue");
                        String property = resultSet.getString("ControlProperty");
                        String fatal = resultSet.getString("Fatal");
                        String description = resultSet.getString("ControlDescription");
                        list.add(factoryTestCaseStepActionControl.create(test, testCase, step, sequence, control, type, object, property, fatal, description));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
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

}
