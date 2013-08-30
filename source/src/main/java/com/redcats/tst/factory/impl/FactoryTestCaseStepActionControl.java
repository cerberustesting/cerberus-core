/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.TestCaseStepActionControl;
import com.redcats.tst.factory.IFactoryTestCaseStepActionControl;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseStepActionControl implements IFactoryTestCaseStepActionControl {

    @Override
    public TestCaseStepActionControl create(String test, String testCase, int step, int sequence,
                                            int control, String type, String controlValue, String controlProperty, String fatal) {
        TestCaseStepActionControl testCaseStepActionControl = new TestCaseStepActionControl();
        testCaseStepActionControl.setTest(test);
        testCaseStepActionControl.setTestCase(testCase);
        testCaseStepActionControl.setType(type);
        testCaseStepActionControl.setStep(step);
        testCaseStepActionControl.setSequence(sequence);
        testCaseStepActionControl.setControl(control);
        testCaseStepActionControl.setControlValue(controlValue);
        testCaseStepActionControl.setControlProperty(controlProperty);
        testCaseStepActionControl.setFatal(fatal);
        return testCaseStepActionControl;
    }

}
