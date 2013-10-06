/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.refactor;


import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseExecutionStatisticsServiceImpl implements ITestCaseExecutionStatisticsService {

    @Autowired
    ITestCaseExecutionStatisticsDAO testCaseExecutionStatisticsDAO;

    @Override
    public TestCaseExecutionStatistics getStatisticsOfExecution(String MySystem, String build, String revision, List<String> environment) {
        return testCaseExecutionStatisticsDAO.getStatisticsOfExecution(MySystem, build, revision, environment);
    }

    @Override
    public TestCaseExecutionStatistics getStatisticsOfExecution(String MySystem, String build, String revision, String environment) {
        List<String> env = new ArrayList<String>();
        env.add(environment);
        return this.getStatisticsOfExecution(MySystem, build, revision, env);
    }

    @Override
    public List<TestCaseExecutionStatistics> getListOfXLastBuildAndRevExecuted(String system, int listSize) {
        return testCaseExecutionStatisticsDAO.getListOfXLastBuildAndRevExecuted(system, listSize);
    }
}
