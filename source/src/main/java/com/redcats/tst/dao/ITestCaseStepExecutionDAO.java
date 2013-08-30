package com.redcats.tst.dao;

import com.redcats.tst.entity.TestCaseStepExecution;
import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 03/01/2013
 * @since 2.0.0
 */
public interface ITestCaseStepExecutionDAO {

    /**
     *
     * @param testCaseStepExecution
     */
    void insertTestCaseStepExecution(TestCaseStepExecution testCaseStepExecution);

    /**
     *
     * @param testCaseStepExecution
     */
    void updateTestCaseStepExecution(TestCaseStepExecution testCaseStepExecution);
    
    /**
     *
     * @param id
     * @return
     */
    List<TestCaseStepExecution> findTestCaseStepExecutionById(long id);
}
