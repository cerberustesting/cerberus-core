/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.MessageEvent;
import com.redcats.tst.entity.TestCaseStepActionControlExecution;
import com.redcats.tst.entity.TestCaseStepActionExecution;

/**
 * @author bcivel
 */
public interface IFactoryTestCaseStepActionControlExecution {

    TestCaseStepActionControlExecution create(long id, String test, String testCase, int step,
                                              int sequence, int control, String returnCode, String returnMessage, String controlType,
                                              String controlProperty, String controlValue, String fatal, long start, long end,
                                              long startLong, long endLong, String screenshotFilename, TestCaseStepActionExecution testCaseStepActionExecution, MessageEvent resultMessage);
}
