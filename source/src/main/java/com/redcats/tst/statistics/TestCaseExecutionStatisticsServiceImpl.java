/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.statistics;


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
    public BuildRevisionStatistics getStatisticsOfExecution(String MySystem, String build, String revision, List<String> environment) {
        return testCaseExecutionStatisticsDAO.getStatisticsOfExecution(MySystem, build, revision, environment);
    }

    @Override
    public BuildRevisionStatistics getStatisticsOfExternalExecution(String MySystem, String build, String revision, List<String> environment) {
        return testCaseExecutionStatisticsDAO.getStatisticsOfExternalExecution(MySystem, build, revision, environment);
    }

    @Override
    public BuildRevisionStatistics getStatisticsOfExecution(String MySystem, String build, String revision, String environment) {
        List<String> env = new ArrayList<String>();
        env.add(environment);
        return this.getStatisticsOfExecution(MySystem, build, revision, env);
    }

    @Override
    public BuildRevisionStatistics getStatisticsOfExternalExecution(String MySystem, String build, String revision, String environment) {
        List<String> env = new ArrayList<String>();
        env.add(environment);
        return this.getStatisticsOfExternalExecution(MySystem, build, revision, env);
    }

    @Override
    public List<BuildRevisionStatistics> getListOfXLastBuildAndRevExecuted(String system, int listSize) {
        return testCaseExecutionStatisticsDAO.getListOfXLastBuildAndRevExecuted(system, listSize);
    }

    @Override
    public List<BuildRevisionStatistics> getListOfXLastBuildAndRev(String system, int listSize) {
        return testCaseExecutionStatisticsDAO.getListOfXLastBuildAndRev(system, listSize);
    }
    
}
