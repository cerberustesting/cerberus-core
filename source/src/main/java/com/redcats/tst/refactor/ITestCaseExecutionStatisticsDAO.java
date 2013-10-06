/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.refactor;

import java.util.List;

/**
 *
 * @author bcivel
 */
public interface ITestCaseExecutionStatisticsDAO {
    
    TestCaseExecutionStatistics getStatisticsOfExecution(String MySystem, String build, String revision, List<String> environment);
    
    List<TestCaseExecutionStatistics> getListOfXLastBuildAndRevExecuted(String system, int listSize);
    
}
