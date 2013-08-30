package com.redcats.tst.dao;

import com.redcats.tst.entity.TestCaseExecutionData;
import com.redcats.tst.exception.CerberusException;
import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 02/01/2013
 * @since 2.0.0
 */
public interface ITestCaseExecutionDataDAO {

    TestCaseExecutionData findTestCaseExecutionDataByKey(long id, String property);
    
    void insertTestCaseExecutionData (TestCaseExecutionData testCaseExecutionData) throws CerberusException;
    
    void updateTestCaseExecutionData (TestCaseExecutionData testCaseExecutionData) throws CerberusException;
    
    List<String> getPastValuesOfProperty(String propName, String test, String testCase, String build, String environment, String country);

    /**
     *
     * @param id
     * @return List of TestCaseExecutionData that match the Id.
     */
    List<TestCaseExecutionData> findTestCaseExecutionDataById(long id);

}
