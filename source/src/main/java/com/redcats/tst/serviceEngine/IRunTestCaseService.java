package com.redcats.tst.serviceEngine;

import com.redcats.tst.entity.TCExecution;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 23/01/2013
 * @since 2.0.0
 */
public interface IRunTestCaseService {

    /**
     * Execute the Test Case
     * @param tCExecution
     * @return
     */
    TCExecution runTestCase(TCExecution tCExecution);
}
