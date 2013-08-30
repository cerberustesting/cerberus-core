/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.TestCaseStep;
import java.util.List;

/**
 *
 * @author bcivel
 */
public interface ITestCaseStepService {
    
    List<TestCaseStep> getListOfSteps(String test, String testcase);
    
    List<String> getLoginStepFromTestCase(String countryCode, String application);
   
}
