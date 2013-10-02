package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.ITestCaseStepActionDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.TestCaseStepAction;
import com.redcats.tst.factory.IFactoryTestCaseStepAction;
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
                        list.add(factoryTestCaseStepAction.create(test, testcase, step, sequence, action, object, property));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionDAO.class.getName(), Level.ERROR, exception.toString());
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
