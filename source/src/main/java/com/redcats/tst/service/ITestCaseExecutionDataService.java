/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.TestCaseExecutionData;
import com.redcats.tst.exception.CerberusException;
import java.util.List;

/**
 *
 * @author bcivel
 */
public interface ITestCaseExecutionDataService {

    TestCaseExecutionData findTestCaseExecutionDataByKey(long id, String property);

    void insertTestCaseExecutionData(TestCaseExecutionData testCaseExecutionData) throws CerberusException;

    void updateTestCaseExecutionData(TestCaseExecutionData testCaseExecutionData) throws CerberusException;

    /**
     *
     * @param id
     * @return List of testCaseExecutionData that correspond to the Id.
     */
    List<TestCaseExecutionData> findTestCaseExecutionDataById(long id);
    
    /**
     *
     * @param id
     * @return List of TestCaseExecutionData that match the Id.
     */
    List<TestCaseExecutionData> findTestCaseExecutionDataByCriteria1(String property, String value, String controlStatus, Integer nbMinutes);
}
