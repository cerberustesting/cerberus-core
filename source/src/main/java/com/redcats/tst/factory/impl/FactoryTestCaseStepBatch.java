/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.TestCaseStepBatch;
import com.redcats.tst.factory.IFactoryTestCaseStepBatch;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseStepBatch implements IFactoryTestCaseStepBatch {

    @Override
    public TestCaseStepBatch create(String test, String testCase, int step, String batch) {
        TestCaseStepBatch testCaseStepBatch = new TestCaseStepBatch();
        testCaseStepBatch.setBatch(batch);
        testCaseStepBatch.setStep(step);
        testCaseStepBatch.setTest(test);
        testCaseStepBatch.setTestCase(testCase);
        return testCaseStepBatch;
    }

}
