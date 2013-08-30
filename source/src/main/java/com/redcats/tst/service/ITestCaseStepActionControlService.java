/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.TestCaseStepActionControl;
import java.util.List;

/**
 *
 * @author bcivel
 */
public interface ITestCaseStepActionControlService {
    
    List<TestCaseStepActionControl> findControlByTestTestCaseStepSequence(String test, String testcase, int stepNumber, int sequence);
}
