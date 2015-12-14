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
import org.cerberus.crud.dao.ITestCaseStepActionDAO;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryTestCaseStepAction;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.log.MyLogger;
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
public class TestCaseStepActionDAO implements ITestCaseStepActionDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseStepAction factoryTestCaseStepAction;

    private static final Logger LOG = Logger.getLogger(TestCaseStepActionDAO.class);

    private final String OBJECT_NAME = "TestCaseStepAction";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public TestCaseStepAction readByKey(String test, String testCase, int step, int sequence) {
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
                        String screenshotFilename = resultSet.getString("screenshotFilename");
                        testCaseStepAction = factoryTestCaseStepAction.create(test, testCase, step, sequence, action, object, property, description, screenshotFilename);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return testCaseStepAction;
    }

    /**
     * Short one line description.
     * <p/>
     * Longer description. If there were any, it would be here.
     * <p>
     * And even more explanations to follow in consecutive paragraphs separated
     * by HTML paragraph breaks.
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
                        String screenshotFilename = resultSet.getString("screenshotFilename");
                        list.add(factoryTestCaseStepAction.create(test, testcase, step, sequence, action, object, property, description, screenshotFilename));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public void create(TestCaseStepAction testCaseStepAction) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcasestepaction (`test`, `testCase`, `step`, `sequence`, `action`, `object`, `property`, `description`, `screenshotfilename`) ");
        query.append("VALUES (?,?,?,?,?,?,?,?,?)");

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
                preStat.setString(9, testCaseStepAction.getScreenshotFilename());

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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

    private TestCaseStepAction loadFromResultSet(ResultSet resultSet) throws SQLException {
        String test = resultSet.getString("Test");
        String testCase = resultSet.getString("TestCase");
        Integer step = resultSet.getInt("Step");
        Integer sequence = resultSet.getInt("Sequence");
        String action = resultSet.getString("Action");
        String object = resultSet.getString("Object");
        String property = resultSet.getString("Property");
        String description = resultSet.getString("description");
        String screenshotFilename = resultSet.getString("screenshotFilename");

        return factoryTestCaseStepAction.create(test, testCase, step, sequence, action, object, property, description, screenshotFilename);
    }

    /**
     * Short one line description.
     * <p/>
     * Longer description. If there were any, it would be here.
     * <p>
     * And even more explanations to follow in consecutive paragraphs separated
     * by HTML paragraph breaks.
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
                MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return false;
    }

    @Override
    public void update(TestCaseStepAction testCaseStepAction) throws CerberusException {
        boolean throwExcep = false;
        final String query = new StringBuilder("UPDATE `testcasestepaction` ")
                .append("SET ")
                .append("`Test` = ?, ")
                .append("`TestCase` = ?, ")
                .append("`Step` = ?, ")
                .append("`Sequence` = ?, ")
                .append("`Action` = ?, ")
                .append("`Object` = ?, ")
                .append("`Property` = ?, ")
                .append("`Description` = ?, ")
                .append("`ScreenshotFilename` = ? ")
                .append("WHERE `Test` = ? AND `TestCase` = ? AND `Step` = ? AND `Sequence` = ? ")
                .toString();

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, testCaseStepAction.getTest());
                preStat.setString(2, testCaseStepAction.getTestCase());
                preStat.setInt(3, testCaseStepAction.getStep());
                preStat.setInt(4, testCaseStepAction.getSequence());
                preStat.setString(5, testCaseStepAction.getAction());
                preStat.setString(6, testCaseStepAction.getObject());
                preStat.setString(7, testCaseStepAction.getProperty());
                preStat.setString(8, testCaseStepAction.getDescription());
                preStat.setString(9, testCaseStepAction.getScreenshotFilename());

                preStat.setString(10, testCaseStepAction.getTest());
                preStat.setString(11, testCaseStepAction.getTestCase());
                preStat.setInt(12, testCaseStepAction.getStep());
                preStat.setInt(13, testCaseStepAction.getSequence());

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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

    @Override
    public void delete(TestCaseStepAction tcsa) throws CerberusException {
        boolean throwExcep = false;
        final String query = "DELETE FROM testcasestepaction WHERE test = ? and testcase = ? and step = ? and `sequence` = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, tcsa.getTest());
                preStat.setString(2, tcsa.getTestCase());
                preStat.setInt(3, tcsa.getStep());
                preStat.setInt(4, tcsa.getSequence());

                throwExcep = preStat.executeUpdate() == 0;
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
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

    @Override
    public List<TestCaseStepAction> findTestCaseStepActionbyTestTestCase(String test, String testCase) throws CerberusException {
        List<TestCaseStepAction> list = null;
        final String query = "SELECT * FROM testcasestepaction WHERE test = ? AND testcase = ? ORDER BY step, sequence";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);

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
                        String screenshotFilename = resultSet.getString("screenshotFilename");
                        list.add(factoryTestCaseStepAction.create(test, testCase, step, sequence, action, object, property, description, screenshotFilename));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public AnswerList readByTestTestCase(String test, String testcase) {
        AnswerList response = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCaseStepAction> actionList = new ArrayList<TestCaseStepAction>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcasestepaction WHERE test = ? AND testcase = ? ORDER BY sequence");

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
                        actionList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (actionList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList(actionList, actionList.size());
                    } else if (actionList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList(actionList, actionList.size());
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList(actionList, actionList.size());
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

}
