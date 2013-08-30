package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.ITestCaseStepActionControlDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.TestCaseStepActionControl;
import com.redcats.tst.factory.IFactoryTestCaseStepActionControl;
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
    public List<TestCaseStepActionControl> findControlByTestTestCaseStepSequence(String test, String testcase, int stepNumber, int sequence) {
        List<TestCaseStepActionControl> list = null;
        final String query = "SELECT * FROM testcasestepactioncontrol WHERE test = ? AND testcase = ? AND step = ? AND sequence = ? ORDER BY control";

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setString(1, test);
            preStat.setString(2, testcase);
            preStat.setInt(3, stepNumber);
            preStat.setInt(4, sequence);
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestCaseStepActionControl>();
                try {
                    while (resultSet.next()) {
                        int step = resultSet.getInt("Step");
                        int control = resultSet.getInt("Control");
                        String type = resultSet.getString("Type");
                        String object = resultSet.getString("ControlValue");
                        String property = resultSet.getString("ControlProperty");
                        String fatal = resultSet.getString("Fatal");
                        list.add(factoryTestCaseStepActionControl.create(test, testcase, step, sequence, control, type, object, property, fatal));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionControlDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            this.databaseSpring.disconnect();
        }
        return list;
    }

}
