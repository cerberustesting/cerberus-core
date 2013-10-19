/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.ITestCaseExecutionSysVerDAO;
import com.redcats.tst.entity.TestCaseExecutionSysVer;
import com.redcats.tst.service.ITestCaseExecutionSysVerService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseExecutionSysVerService implements ITestCaseExecutionSysVerService {

    @Autowired
    ITestCaseExecutionSysVerDAO testCaseExecutionSysVerDao;

    @Override
    public void insertTestCaseExecutionSysVer(TestCaseExecutionSysVer testCaseExecutionSysVer) {
        this.testCaseExecutionSysVerDao.insertTestCaseExecutionSysVer(testCaseExecutionSysVer);
    }

    @Override
    public List<TestCaseExecutionSysVer> findTestCaseExecutionSysVerById(long id) {
        return testCaseExecutionSysVerDao.findTestCaseExecutionSysVerById(id);
    }
}
