package com.redcats.tst.dao;

import com.redcats.tst.entity.TestCaseStepActionExecution;
import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 02/01/2013
 * @since 2.0.0
 */
public interface ITestCaseStepActionExecutionDAO {

    void updateTestCaseStepActionExecution(TestCaseStepActionExecution testCaseStepActionExecution);

    void insertTestCaseStepActionExecution(TestCaseStepActionExecution testCaseStepActionExecution);

    List<List<String>> getListOfSequenceDuration(String idList);

    List<TestCaseStepActionExecution> findTestCaseStepActionExecutionByCriteria(long id, String test, String testCase, int step);
}
