package com.redcats.tst.dao;

import com.redcats.tst.entity.TestCaseStepAction;

import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 31/Dez/2012
 * @since 2.0.0
 */
public interface ITestCaseStepActionDAO {
    List<TestCaseStepAction> findActionByTestTestCaseStep(String test, String testcase, int stepNumber);
}
