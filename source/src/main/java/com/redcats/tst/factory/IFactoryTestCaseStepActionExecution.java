/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.MessageEvent;
import com.redcats.tst.entity.TestCaseStepAction;
import com.redcats.tst.entity.TestCaseStepActionExecution;
import com.redcats.tst.entity.TestCaseStepExecution;

/**
 * @author bcivel
 */
public interface IFactoryTestCaseStepActionExecution {

    TestCaseStepActionExecution create(long id, String test, String testCase, int step,
                                       int sequence, String returnCode, String returnMessage, String action, String object,
                                       String property, long start, long end, long startLong, long endLong, String screenshotFilename,
                                       MessageEvent resultMessage, TestCaseStepAction testCaseStepAction, TestCaseStepExecution testCaseStepExecution);
}
