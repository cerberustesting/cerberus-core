/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.TestCaseStepActionControlExecution;
import java.util.List;

/**
 *
 * @author bcivel
 */
public interface ITestCaseStepActionControlExecutionService {

    /**
     *
     * @param testCaseStepActionControlExecution
     */
    void insertTestCaseStepActionControlExecution(TestCaseStepActionControlExecution testCaseStepActionControlExecution);

    /**
     *
     * @param testCaseStepActionControlExecution
     */
    void updateTestCaseStepActionControlExecution(TestCaseStepActionControlExecution testCaseStepActionControlExecution);
    
    List<TestCaseStepActionControlExecution> findTestCaseStepActionControlExecutionByCriteria(long id, String test, String testCase, int step, int sequence);
}
