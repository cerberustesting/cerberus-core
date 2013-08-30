/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.ITestCaseStepActionControlDAO;
import com.redcats.tst.entity.TestCaseStepActionControl;
import com.redcats.tst.entity.TestCaseStepActionControlExecution;
import com.redcats.tst.service.ITestCaseStepActionControlService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseStepActionControlService implements ITestCaseStepActionControlService{

    @Autowired
    private ITestCaseStepActionControlDAO testCaseStepActionControlDao;
    
    @Override
    public List<TestCaseStepActionControl> findControlByTestTestCaseStepSequence(String test, String testcase, int stepNumber, int sequence) {
        return testCaseStepActionControlDao.findControlByTestTestCaseStepSequence(test, testcase, stepNumber, sequence);
    }
    
    
    
    
}
