/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.ITestCaseStepDAO;
import com.redcats.tst.entity.TestCaseStep;
import com.redcats.tst.service.ITestCaseStepService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseStepService implements ITestCaseStepService {

    @Autowired
    private ITestCaseStepDAO testCaseStepDAO;
    
    @Override
    public List<TestCaseStep> getListOfSteps(String test, String testcase) {
        return testCaseStepDAO.findTestCaseStepByTestCase(test, testcase);
        }

    @Override
    public List<String> getLoginStepFromTestCase(String countryCode, String application) {
        return testCaseStepDAO.getLoginStepFromTestCase(countryCode, application);
    }
    
}
