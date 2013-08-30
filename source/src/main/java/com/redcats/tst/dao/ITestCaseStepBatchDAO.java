package com.redcats.tst.dao;

import com.redcats.tst.entity.TestCaseStepBatch;
import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 29/Dez/2012
 * @since 2.0.0
 */
public interface ITestCaseStepBatchDAO {
    List<TestCaseStepBatch> findTestCaseStepBatchByTestCaseStep(String test, String testcase, int stepNumber);
}
