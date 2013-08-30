/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.MessageEvent;
import com.redcats.tst.entity.TCExecution;
import com.redcats.tst.entity.TestCaseStep;
import com.redcats.tst.entity.TestCaseStepExecution;

/**
 * @author bcivel
 */
public interface IFactoryTestCaseStepExecution {

    TestCaseStepExecution create(long id, String test, String testCase, int step, String batNumExe,
                                 long start, long end, long fullStart, long fullEnd, long timeElapsed, String returnCode,
                                 MessageEvent stepResultMessage, TestCaseStep testCaseStep, TCExecution tCExecution);
}
