/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.TestCaseStepAction;
import com.redcats.tst.factory.IFactoryTestCaseStepAction;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseStepAction implements IFactoryTestCaseStepAction {

    @Override
    public TestCaseStepAction create(String test, String testCase, int step, int sequence, String action, String object, String property) {
        TestCaseStepAction testCaseStepAction = new TestCaseStepAction();
        testCaseStepAction.setAction(action);
        testCaseStepAction.setObject(object);
        testCaseStepAction.setProperty(property);
        testCaseStepAction.setSequence(sequence);
        testCaseStepAction.setStep(step);
        testCaseStepAction.setTest(test);
        testCaseStepAction.setTestCase(testCase);
        return testCaseStepAction;
    }

}
