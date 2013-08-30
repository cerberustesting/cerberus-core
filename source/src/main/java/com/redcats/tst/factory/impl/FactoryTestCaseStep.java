/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.TestCaseStep;
import com.redcats.tst.factory.IFactoryTestCaseStep;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseStep implements IFactoryTestCaseStep {

    @Override
    public TestCaseStep create(String test, String testCase, int step, String description) {
        TestCaseStep testCaseStep = new TestCaseStep();
        testCaseStep.setDescription(description);
        testCaseStep.setStep(step);
        testCaseStep.setTest(test);
        testCaseStep.setTestCase(testCase);
        return testCaseStep;
    }
}
