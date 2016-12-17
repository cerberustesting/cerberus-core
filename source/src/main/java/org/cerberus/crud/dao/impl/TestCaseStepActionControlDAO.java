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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.crud.dao.ITestCaseStepActionControlDAO;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryTestCaseStepActionControl;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.log.MyLogger;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;
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

    private static final Logger LOG = Logger.getLogger(TestCaseStepActionControlDAO.class);

    private final String OBJECT_NAME = "TestCaseStepActionControl";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public TestCaseStepActionControl findTestCaseStepActionControlByKey(String test, String testcase, int stepNumber, int sequence, int controlSequence) {
        TestCaseStepActionControl actionControl = null;
        final String query = "SELECT * FROM testcasestepactioncontrol WHERE test = ? AND testcase = ? AND step = ? AND sequence = ? AND control = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                preStat.setInt(3, stepNumber);
                preStat.setInt(4, sequence);
                preStat.setInt(5, controlSequence);

                ResultSet resultSet = preStat.executeQuery();
                try {

                    if (resultSet.first()) {
                        int sort = resultSet.getInt("Sort");
                        String conditionOper = resultSet.getString("conditionOper");
                        String conditionVal1 = resultSet.getString("conditionVal1");
                        String conditionVal2 = resultSet.getString("conditionVal2");
                        String control = resultSet.getString("Control");
                        String value1 = resultSet.getString("Value1");
                        String value2 = resultSet.getString("Value2");
                        String fatal = resultSet.getString("Fatal");
                        String description = resultSet.getString("Description");
                        String screenshotFilename = resultSet.getString("screenshotFilename");
                        actionControl = factoryTestCaseStepActionControl.create(test, testcase, stepNumber, sequence, controlSequence, sort, conditionOper, conditionVal1, conditionVal2, control,
                                value1, value2, fatal, description, screenshotFilename);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
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
        return actionControl;
    }

    @Override
    public List<TestCaseStepActionControl> findControlByTestTestCaseStepSequence(String test, String testcase, int stepNumber, int sequence) {
        List<TestCaseStepActionControl> list = null;
        final String query = "SELECT * FROM testcasestepactioncontrol WHERE test = ? AND testcase = ? AND step = ? AND sequence = ? ORDER BY sort";

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
                        int controlSequence = resultSet.getInt("ControlSequence");
                        int sort = resultSet.getInt("Sort");
                        String conditionOper = resultSet.getString("conditionOper");
                        String conditionVal1 = resultSet.getString("conditionVal1");
                        String conditionVal2 = resultSet.getString("conditionVal2");
                        String control = resultSet.getString("Control");
                        String value1 = resultSet.getString("Value1");
                        String value2 = resultSet.getString("Value2");
                        String fatal = resultSet.getString("Fatal");
                        String description = resultSet.getString("Description");
                        String screenshotFilename = resultSet.getString("screenshotFilename");
                        list.add(factoryTestCaseStepActionControl.create(test, testcase, step, sequence, controlSequence, sort, conditionOper, conditionVal1, conditionVal2, control, value1, value2, fatal, description, screenshotFilename));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
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
        return list;
    }

    @Override
    public void insertTestCaseStepActionControl(TestCaseStepActionControl testCaseStepActionControl) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcasestepactioncontrol (`test`, `testCase`, `step`, `sequence`, `controlSequence`, `sort`, `conditionOper`, `conditionVal1`, `conditionVal2`, `control`, `value1`, `value2`, `fatal`, `Description`, `screenshotfilename`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testCaseStepActionControl.getTest());
                preStat.setString(2, testCaseStepActionControl.getTestCase());
                preStat.setInt(3, testCaseStepActionControl.getStep());
                preStat.setInt(4, testCaseStepActionControl.getSequence());
                preStat.setInt(5, testCaseStepActionControl.getControlSequence());
                preStat.setInt(6, testCaseStepActionControl.getSort());
                preStat.setString(7, testCaseStepActionControl.getConditionOper());
                preStat.setString(8, testCaseStepActionControl.getConditionVal1());
                preStat.setString(9, testCaseStepActionControl.getConditionVal2());
                preStat.setString(10, testCaseStepActionControl.getControl());
                preStat.setString(11, testCaseStepActionControl.getValue1());
                preStat.setString(12, testCaseStepActionControl.getValue2());
                preStat.setString(13, testCaseStepActionControl.getFatal());
                preStat.setString(14, testCaseStepActionControl.getDescription());
                preStat.setString(15, testCaseStepActionControl.getScreenshotFilename());

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
    public List<TestCaseStepActionControl> findControlByTestTestCaseStep(String test, String testcase, int step) {
        List<TestCaseStepActionControl> list = null;
        final String query = "SELECT * FROM testcasestepactioncontrol WHERE test = ? AND testcase = ? AND step = ?";

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
                        int controlSequence = resultSet.getInt("ControlSequence");
                        int sort = resultSet.getInt("Sort");
                        String conditionOper = resultSet.getString("conditionOper");
                        String conditionVal1 = resultSet.getString("conditionVal1");
                        String conditionVal2 = resultSet.getString("conditionVal2");
                        String control = resultSet.getString("Control");
                        String value1 = resultSet.getString("Value1");
                        String value2 = resultSet.getString("Value2");
                        String fatal = resultSet.getString("Fatal");
                        String description = resultSet.getString("Description");
                        String screenshotFilename = resultSet.getString("screenshotFilename");
                        list.add(factoryTestCaseStepActionControl.create(test, testcase, step, sequence, controlSequence, sort, conditionOper, conditionVal1, conditionVal2, control, value1, value2, fatal, description, screenshotFilename));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
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
                .append("`ControlSequence` = ?, ")
                .append("`Sort` = ?, ")
                .append("`ConditionOper` = ?, ")
                .append("`ConditionVal1` = ?, ")
                .append("`ConditionVal2` = ?, ")
                .append("`Control` = ?, ")
                .append("`Value1` = ?, ")
                .append("`Value2` = ?, ")
                .append("`Description` = ?, ")
                .append("`Fatal` = ?, ")
                .append("`screenshotFilename` = ? ")
                .append("WHERE `Test` = ? AND `TestCase` = ? AND `Step` = ? AND `Sequence` = ? AND `ControlSequence` = ? ")
                .toString();

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                int i=1;
                preStat.setString(i++, testCaseStepActionControl.getTest());
                preStat.setString(i++, testCaseStepActionControl.getTestCase());
                preStat.setInt(i++, testCaseStepActionControl.getStep());
                preStat.setInt(i++, testCaseStepActionControl.getSequence());
                preStat.setInt(i++, testCaseStepActionControl.getControlSequence());
                preStat.setInt(i++, testCaseStepActionControl.getSort());
                preStat.setString(i++, testCaseStepActionControl.getConditionOper());
                preStat.setString(i++, testCaseStepActionControl.getConditionVal1());
                preStat.setString(i++, testCaseStepActionControl.getConditionVal2());
                preStat.setString(i++, testCaseStepActionControl.getControl());
                preStat.setString(i++, testCaseStepActionControl.getValue1());
                preStat.setString(i++, testCaseStepActionControl.getValue2());
                preStat.setString(i++, testCaseStepActionControl.getDescription());
                preStat.setString(i++, testCaseStepActionControl.getFatal());
                preStat.setString(i++, testCaseStepActionControl.getScreenshotFilename());

                preStat.setString(i++, testCaseStepActionControl.getTest());
                preStat.setString(i++, testCaseStepActionControl.getTestCase());
                preStat.setInt(i++, testCaseStepActionControl.getStep());
                preStat.setInt(i++, testCaseStepActionControl.getSequence());
                preStat.setInt(i++, testCaseStepActionControl.getControlSequence());

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
        final String query = "DELETE FROM testcasestepactioncontrol WHERE test = ? and testcase = ? and step = ? and `sequence` = ? and `controlSequence` = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, tcsac.getTest());
                preStat.setString(2, tcsac.getTestCase());
                preStat.setInt(3, tcsac.getStep());
                preStat.setInt(4, tcsac.getSequence());
                preStat.setInt(5, tcsac.getControlSequence());

                throwExcep = preStat.executeUpdate() == 0;
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
    public List<TestCaseStepActionControl> findControlByTestTestCase(String test, String testCase) throws CerberusException {
        List<TestCaseStepActionControl> list = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT tcsac.* ");
        query.append("FROM testcasestepactioncontrol AS tcsac ");
        query.append("RIGHT JOIN testcasestepaction AS tcsa ON tcsac.Test = tcsa.Test AND tcsac.TestCase = tcsa.TestCase AND tcsac.Step = tcsa.Step AND tcsac.Sequence = tcsa.Sequence ");
        query.append("RIGHT JOIN testcasestep AS tcs ON tcsac.Test = tcs.Test AND tcsac.TestCase = tcs.TestCase AND tcsac.Step = tcs.Step ");
        query.append("WHERE tcsac.Test = ? AND tcsac.TestCase = ? ");
        query.append("GROUP BY tcsac.Test, tcsac.TestCase, tcsac.Step, tcsac.Sequence, tcsac.ControlSequence ");
        query.append("ORDER BY tcs.Sort, tcsa.Sort, tcsac.Sort ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<TestCaseStepActionControl>();

                    while (resultSet.next()) {
                        int step = resultSet.getInt("Step");
                        int sequence = resultSet.getInt("Sequence");
                        int controlSequence = resultSet.getInt("ControlSequence");
                        int sort = resultSet.getInt("Sort");
                        String conditionOper = resultSet.getString("conditionOper");
                        String conditionVal1 = resultSet.getString("conditionVal1");
                        String conditionVal2 = resultSet.getString("conditionVal2");
                        String control = resultSet.getString("Control");
                        String value1 = resultSet.getString("Value1");
                        String value2 = resultSet.getString("Value2");
                        String fatal = resultSet.getString("Fatal");
                        String description = resultSet.getString("Description");
                        String screenshotFilename = resultSet.getString("screenshotFilename");
                        list.add(factoryTestCaseStepActionControl.create(test, testCase, step, sequence, controlSequence, sort, conditionOper, conditionVal1, conditionVal2, control, value1, value2, fatal, description, screenshotFilename));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
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
        return list;
    }

    @Override
    public AnswerList readByTestTestCase(String test, String testcase) {
        AnswerList response = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCaseStepActionControl> controlList = new ArrayList<TestCaseStepActionControl>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcasestepactioncontrol WHERE test = ? AND testcase = ?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        controlList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (controlList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList(controlList, controlList.size());
                    } else if (controlList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList(controlList, controlList.size());
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList(controlList, controlList.size());
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        response.setResultMessage(msg);
        return response;
    }

    @Override
    public AnswerList readByVarious1(String test, String testcase, int step, int sequence) {
        AnswerList response = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCaseStepActionControl> controlList = new ArrayList<TestCaseStepActionControl>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcasestepactioncontrol WHERE test = ? AND testcase = ? AND step = ? AND sequence = ?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                preStat.setInt(3, step);
                preStat.setInt(4, sequence);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        controlList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (controlList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList(controlList, controlList.size());
                    } else if (controlList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList(controlList, controlList.size());
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList(controlList, controlList.size());
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        response.setResultMessage(msg);
        return response;
    }

    @Override
    public Answer create(TestCaseStepActionControl testCaseStepActionControl) {
        Answer ans = new Answer();
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcasestepactioncontrol (`test`, `testCase`, `step`, `sequence`, `controlSequence`, `sort`, `conditionOper`, `conditionVal1`, `conditionVal2`, `control`, `value1`, `value2`, `fatal`, `Description`, `screenshotfilename`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString())) {
            // Prepare and execute query
            preStat.setString(1, testCaseStepActionControl.getTest());
            preStat.setString(2, testCaseStepActionControl.getTestCase());
            preStat.setInt(3, testCaseStepActionControl.getStep());
            preStat.setInt(4, testCaseStepActionControl.getSequence());
            preStat.setInt(5, testCaseStepActionControl.getControlSequence());
            preStat.setInt(6, testCaseStepActionControl.getSort());
            preStat.setString(7, testCaseStepActionControl.getConditionOper());
            preStat.setString(8, testCaseStepActionControl.getConditionVal1());
            preStat.setString(9, testCaseStepActionControl.getConditionVal2());
            preStat.setString(10, testCaseStepActionControl.getControl());
            preStat.setString(11, testCaseStepActionControl.getValue1());
            preStat.setString(12, testCaseStepActionControl.getValue2());
            preStat.setString(13, testCaseStepActionControl.getFatal());
            preStat.setString(14, testCaseStepActionControl.getDescription());
            preStat.setString(15, testCaseStepActionControl.getScreenshotFilename());
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "CREATE");
        } catch (Exception e) {
            LOG.warn("Unable to TestCaseStepActionControl: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    private TestCaseStepActionControl loadFromResultSet(ResultSet resultSet) throws SQLException {
        String test = resultSet.getString("Test");
        String testCase = resultSet.getString("TestCase");
        Integer step = resultSet.getInt("Step");
        Integer sequence = resultSet.getInt("Sequence");
        Integer controlSequence = resultSet.getInt("controlSequence");
        Integer sort = resultSet.getInt("Sort");
        String conditionOper = resultSet.getString("conditionOper");
        String conditionVal1 = resultSet.getString("conditionVal1");
        String conditionVal2 = resultSet.getString("conditionVal2");
        String control = resultSet.getString("Control");
        String fatal = resultSet.getString("fatal");
        String value1 = resultSet.getString("Value1");
        String value2 = resultSet.getString("Value2");
        String description = resultSet.getString("Description");
        String screenshotFilename = resultSet.getString("screenshotFilename");

        return factoryTestCaseStepActionControl.create(test, testCase, step, sequence, controlSequence, sort, conditionOper, conditionVal1, conditionVal2, control, value1, value2, fatal, description, screenshotFilename);
    }
}
