/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.TestCaseStepActionExecution;
import java.util.List;

/**
 *
 * @author bcivel
 */
public interface ITestCaseStepActionExecutionService {

    /**
     *
     * @param testCaseStepActionExecution
     */
    void insertTestCaseStepActionExecution(TestCaseStepActionExecution testCaseStepActionExecution);

    /**
     *
     * @param testCaseStepActionExecution
     */
    void updateTestCaseStepActionExecution(TestCaseStepActionExecution testCaseStepActionExecution);

    /**
     *
     * @param id
     * @return List of testCaseStepExecution that correspond to the Id.
     */
    List<TestCaseStepActionExecution> findTestCaseStepActionExecutionByCriteria(long id, String test, String testCase, int step);
}
