package com.redcats.tst.serviceEngine;

import com.redcats.tst.entity.TestCaseStepActionControlExecution;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 24/01/2013
 * @since 2.0.0
 */
public interface IControlService {

    TestCaseStepActionControlExecution doControl(TestCaseStepActionControlExecution testCaseStepActionControlExecution);
}
