/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.ITestCaseStepExecutionDAO;
import com.redcats.tst.entity.TestCaseStepExecution;
import com.redcats.tst.service.ITestCaseStepExecutionService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseStepExecutionService implements ITestCaseStepExecutionService {

    @Autowired
    ITestCaseStepExecutionDAO testCaseStepExecutionDao;

    @Override
    public void insertTestCaseStepExecution(TestCaseStepExecution testCaseStepExecution) {
        this.testCaseStepExecutionDao.insertTestCaseStepExecution(testCaseStepExecution);
    }

    @Override
    public void updateTestCaseStepExecution(TestCaseStepExecution testCaseStepExecution) {
        this.testCaseStepExecutionDao.updateTestCaseStepExecution(testCaseStepExecution);
    }

    @Override
    public List<TestCaseStepExecution> findTestCaseStepExecutionById(long id) {
        return testCaseStepExecutionDao.findTestCaseStepExecutionById(id);
    }
}
