/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.ITestCaseStepActionControlExecutionDAO;
import com.redcats.tst.entity.TestCaseStepActionControlExecution;
import com.redcats.tst.service.ITestCaseStepActionControlExecutionService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseStepActionControlExecutionService implements ITestCaseStepActionControlExecutionService{

    @Autowired
    private ITestCaseStepActionControlExecutionDAO testCaseStepActionControlExecutionDao;
    
    @Override
    public void insertTestCaseStepActionControlExecution(TestCaseStepActionControlExecution testCaseStepActionControlExecution) {
        testCaseStepActionControlExecutionDao.insertTestCaseStepActionControlExecution(testCaseStepActionControlExecution);
    }
    
    @Override
    public void updateTestCaseStepActionControlExecution(TestCaseStepActionControlExecution testCaseStepActionControlExecution) {
        testCaseStepActionControlExecutionDao.updateTestCaseStepActionControlExecution(testCaseStepActionControlExecution);
    }
    
    @Override
    public List<TestCaseStepActionControlExecution> findTestCaseStepActionControlExecutionByCriteria(long id, String test, String testCase, int step, int sequence) {
        return testCaseStepActionControlExecutionDao.findTestCaseStepActionControlExecutionByCriteria( id,  test,  testCase,  step,  sequence);
    }
}
