/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.statistics;

import java.util.List;

/**
 *
 * @author bcivel
 */
public interface ITestCaseExecutionStatisticsDAO {
    
    BuildRevisionStatistics getStatisticsOfExecution(String MySystem, String build, String revision, List<String> environment);
    
    BuildRevisionStatistics getStatisticsOfExternalExecution(String MySystem, String build, String revision, List<String> environment);
    
    List<BuildRevisionStatistics> getListOfXLastBuildAndRevExecuted(String system, int listSize);
    
    List<BuildRevisionStatistics> getListOfXLastBuildAndRev(String system, int listSize);
    
}
