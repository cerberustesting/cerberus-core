package com.redcats.tst.dao;

import com.redcats.tst.entity.TestCaseStep;

import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 29/12/2012
 * @since 2.0.0
 */
public interface ITestCaseStepDAO {
    List<String> getLoginStepFromTestCase(String countryCode, String application);

    List<TestCaseStep> findTestCaseStepByTestCase(String test, String testcase);
}
