/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.MessageEvent;
import com.redcats.tst.entity.TCExecution;
import com.redcats.tst.entity.TestCaseStep;
import com.redcats.tst.entity.TestCaseStepExecution;
import com.redcats.tst.factory.IFactoryTestCaseStepExecution;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseStepExecution implements IFactoryTestCaseStepExecution {

    @Override
    public TestCaseStepExecution create(long id, String test, String testCase, int step, String batNumExe, long start, long end, long fullStart, long fullEnd, long timeElapsed, String returnCode, MessageEvent stepResultMessage, TestCaseStep testCaseStep, TCExecution tCExecution) {
        TestCaseStepExecution testCaseStepExecution = new TestCaseStepExecution();
        testCaseStepExecution.setBatNumExe(batNumExe);
        testCaseStepExecution.setEnd(end);
        testCaseStepExecution.setFullEnd(fullEnd);
        testCaseStepExecution.setFullStart(fullStart);
        testCaseStepExecution.setId(id);
        testCaseStepExecution.setReturnCode(returnCode);
        testCaseStepExecution.setStart(start);
        testCaseStepExecution.setStep(step);
        testCaseStepExecution.setTest(test);
        testCaseStepExecution.setTestCase(testCase);
        testCaseStepExecution.setTimeElapsed(timeElapsed);
        testCaseStepExecution.setStepResultMessage(stepResultMessage);
        testCaseStepExecution.setTestCaseStep(testCaseStep);
        testCaseStepExecution.settCExecution(tCExecution);
        return testCaseStepExecution;
    }

}
