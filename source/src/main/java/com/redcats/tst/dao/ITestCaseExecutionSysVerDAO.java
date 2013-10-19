package com.redcats.tst.dao;

import com.redcats.tst.entity.TestCaseExecutionSysVer;
import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 03/01/2013
 * @since 2.0.0
 */
public interface ITestCaseExecutionSysVerDAO {

    /**
     *
     * @param testCaseStepExecution
     */
    void insertTestCaseExecutionSysVer(TestCaseExecutionSysVer testCaseExecutionSysVer);

    /**
     *
     * @param id
     * @return
     */
    List<TestCaseExecutionSysVer> findTestCaseExecutionSysVerById(long id);
}
