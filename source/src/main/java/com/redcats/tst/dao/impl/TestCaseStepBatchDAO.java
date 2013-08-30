package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.ITestCaseStepBatchDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.TestCaseStepBatch;
import com.redcats.tst.factory.IFactoryTestCaseStepBatch;
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
 * @version 1.0, 29/12/2012
 * @since 2.0.0
 */
@Repository
public class TestCaseStepBatchDAO implements ITestCaseStepBatchDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseStepBatch factoryTestCaseStepBatch;

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
    public List<TestCaseStepBatch> findTestCaseStepBatchByTestCaseStep(String test, String testcase, int stepNumber) {
        List<TestCaseStepBatch> list = null;
        final String query = "SELECT * FROM testcasestepbatch WHERE test = ? AND testcase = ? AND step = ?";

        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setString(1, test);
            preStat.setString(2, testcase);
            preStat.setInt(3, stepNumber);
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestCaseStepBatch>();
                try {
                    while (resultSet.next()) {
                        String batch = resultSet.getString("Batch");
                        list.add(factoryTestCaseStepBatch.create(test, testcase, stepNumber, batch));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseStepBatchDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepBatchDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepBatchDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            this.databaseSpring.disconnect();
        }
        return list;
    }
}
