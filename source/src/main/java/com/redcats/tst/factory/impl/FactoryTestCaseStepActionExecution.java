/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.MessageEvent;
import com.redcats.tst.entity.TestCaseStepAction;
import com.redcats.tst.entity.TestCaseStepActionExecution;
import com.redcats.tst.entity.TestCaseStepExecution;
import com.redcats.tst.factory.IFactoryTestCaseStepActionExecution;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseStepActionExecution implements IFactoryTestCaseStepActionExecution {

    @Override
    public TestCaseStepActionExecution create(long id, String test, String testCase, int step, int sequence, String returnCode, String returnMessage, String action, String object, String property, long start, long end, long startLong, long endLong, String screenshotFilename, MessageEvent resultMessage, TestCaseStepAction testCaseStepAction, TestCaseStepExecution testCaseStepExecution) {
        TestCaseStepActionExecution testCaseStepActionExecution = new TestCaseStepActionExecution();
        testCaseStepActionExecution.setAction(action);
        testCaseStepActionExecution.setEnd(end);
        testCaseStepActionExecution.setEndLong(endLong);
        testCaseStepActionExecution.setId(id);
        testCaseStepActionExecution.setObject(object);
        testCaseStepActionExecution.setProperty(property);
        testCaseStepActionExecution.setReturnCode(returnCode);
        testCaseStepActionExecution.setReturnMessage(returnMessage);
        testCaseStepActionExecution.setScreenshotFilename(screenshotFilename);
        testCaseStepActionExecution.setSequence(sequence);
        testCaseStepActionExecution.setStart(start);
        testCaseStepActionExecution.setStartLong(startLong);
        testCaseStepActionExecution.setStep(step);
        testCaseStepActionExecution.setTest(test);
        testCaseStepActionExecution.setTestCase(testCase);
        testCaseStepActionExecution.setActionResultMessage(resultMessage);
        testCaseStepActionExecution.setTestCaseStepAction(testCaseStepAction);
        testCaseStepActionExecution.setTestCaseStepExecution(testCaseStepExecution);
        return testCaseStepActionExecution;
    }

}
