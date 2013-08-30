/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.MessageEvent;
import com.redcats.tst.entity.TestCaseStepActionControlExecution;
import com.redcats.tst.entity.TestCaseStepActionExecution;
import com.redcats.tst.factory.IFactoryTestCaseStepActionControlExecution;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseStepActionControlExecution implements IFactoryTestCaseStepActionControlExecution {

    @Override
    public TestCaseStepActionControlExecution create(long id, String test, String testCase, int step, int sequence,
                                                     int control, String returnCode, String returnMessage, String controlType, String controlProperty,
                                                     String controlValue, String fatal, long start, long end, long startLong, long endLong,
                                                     String screenshotFilename, TestCaseStepActionExecution testCaseStepActionExecution, MessageEvent resultMessage) {
        TestCaseStepActionControlExecution testCaseStepActionControlExecution = new TestCaseStepActionControlExecution();
        testCaseStepActionControlExecution.setId(id);
        testCaseStepActionControlExecution.setTest(test);
        testCaseStepActionControlExecution.setTestCase(testCase);
        testCaseStepActionControlExecution.setStep(step);
        testCaseStepActionControlExecution.setSequence(sequence);
        testCaseStepActionControlExecution.setControl(control);
        testCaseStepActionControlExecution.setReturnCode(returnCode);
        testCaseStepActionControlExecution.setReturnMessage(returnMessage);
        testCaseStepActionControlExecution.setControlType(controlType);
        testCaseStepActionControlExecution.setControlProperty(controlProperty);
        testCaseStepActionControlExecution.setControlValue(controlValue);
        testCaseStepActionControlExecution.setFatal(fatal);
        testCaseStepActionControlExecution.setStart(start);
        testCaseStepActionControlExecution.setEnd(end);
        testCaseStepActionControlExecution.setStartLong(startLong);
        testCaseStepActionControlExecution.setEndLong(endLong);
        testCaseStepActionControlExecution.setScreenshotFilename(screenshotFilename);
        testCaseStepActionControlExecution.setTestCaseStepActionExecution(testCaseStepActionExecution);
        testCaseStepActionControlExecution.setControlResultMessage(resultMessage);

        return testCaseStepActionControlExecution;
    }

}
