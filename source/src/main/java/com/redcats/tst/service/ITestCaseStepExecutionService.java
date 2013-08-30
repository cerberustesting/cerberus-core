/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.TestCaseStepExecution;
import java.util.List;

/**
 *
 * @author bcivel
 */
public interface ITestCaseStepExecutionService {

    void insertTestCaseStepExecution(TestCaseStepExecution testCaseStepExecution);

    void updateTestCaseStepExecution(TestCaseStepExecution testCaseStepExecution);

    /**
     *
     * @param id
     * @return List of testCaseStepExecution that correspond to the Id.
     */
    List<TestCaseStepExecution> findTestCaseStepExecutionById(long id);
}
