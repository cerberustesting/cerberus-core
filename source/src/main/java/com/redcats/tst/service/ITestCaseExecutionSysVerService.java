/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.TestCaseExecutionSysVer;
import java.util.List;

/**
 *
 * @author bcivel
 */
public interface ITestCaseExecutionSysVerService {

    void insertTestCaseExecutionSysVer(TestCaseExecutionSysVer testCaseExecutionSysVer);

    /**
     *
     * @param id
     * @return List of testCaseStepExecution that correspond to the Id.
     */
    List<TestCaseExecutionSysVer> findTestCaseExecutionSysVerById(long id);
}
