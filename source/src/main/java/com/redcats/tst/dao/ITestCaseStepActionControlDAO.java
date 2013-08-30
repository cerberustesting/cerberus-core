package com.redcats.tst.dao;

import com.redcats.tst.entity.TestCaseStepActionControl;

import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 31/12/2012
 * @since 2.0.0
 */
public interface ITestCaseStepActionControlDAO {

    List<TestCaseStepActionControl> findControlByTestTestCaseStepSequence(String test, String testcase, int stepNumber, int sequence);
}
