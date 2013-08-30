/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.ITestCaseExecutionDataDAO;
import com.redcats.tst.entity.TestCaseExecutionData;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.service.ITestCaseExecutionDataService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseExecutionDataService implements ITestCaseExecutionDataService {

    @Autowired
    ITestCaseExecutionDataDAO testCaseExecutionDataDao;

    @Override
    public TestCaseExecutionData findTestCaseExecutionDataByKey(long id, String property) {
        return testCaseExecutionDataDao.findTestCaseExecutionDataByKey(id, property);
    }

    @Override
    public void insertTestCaseExecutionData(TestCaseExecutionData testCaseExecutionData) throws CerberusException {
        testCaseExecutionDataDao.insertTestCaseExecutionData(testCaseExecutionData);
    }

    @Override
    public void updateTestCaseExecutionData(TestCaseExecutionData testCaseExecutionData) throws CerberusException {
        testCaseExecutionDataDao.updateTestCaseExecutionData(testCaseExecutionData);
    }

    @Override
    public List<TestCaseExecutionData> findTestCaseExecutionDataById(long id) {
        return testCaseExecutionDataDao.findTestCaseExecutionDataById(id);
    }

    @Override
    public List<TestCaseExecutionData> findTestCaseExecutionDataByCriteria1(String property, String value, String controlStatus, Integer nbMinutes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
}
