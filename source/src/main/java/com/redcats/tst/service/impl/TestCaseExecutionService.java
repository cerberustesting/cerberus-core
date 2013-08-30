/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.ITestCaseExecutionDAO;
import com.redcats.tst.entity.TCExecution;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.service.ITestCaseExecutionService;
import com.redcats.tst.util.ParameterParserUtil;

import java.util.List;

import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class TestCaseExecutionService implements ITestCaseExecutionService {

    @Autowired
    ITestCaseExecutionDAO testCaseExecutionDao;

    @Override
    public void insertTCExecution(TCExecution tCExecution) throws CerberusException {
        testCaseExecutionDao.insertTCExecution(tCExecution);
    }

    @Override
    public void updateTCExecution(TCExecution tCExecution) throws CerberusException {
        testCaseExecutionDao.updateTCExecution(tCExecution);
    }

//    @Override
//    public TCExecution findLastTCExecutionByCriteria(String test, String testCase, String environment, String country) throws CerberusException {
//        return testCaseExecutionDao.findLastTCExecutionByCriteria(test, testCase, environment, country);
//    }

    @Override
    public List<TCExecution> findTCExecutionbyCriteria1(String dateLimitFrom, String test, String testCase, String application, String country, String environment, String controlStatus, String status) throws CerberusException {
        // Transform empty parameter in % in order to remove from SQL filter (thanks to the like operator).
        test = ParameterParserUtil.wildcardIfEmpty(test);
        testCase = ParameterParserUtil.wildcardIfEmpty(testCase);
        application = ParameterParserUtil.wildcardIfEmpty(application);
        country = ParameterParserUtil.wildcardIfEmpty(country);
        environment = ParameterParserUtil.wildcardIfEmpty(environment);
        controlStatus = ParameterParserUtil.wildcardIfEmpty(controlStatus);
        status = ParameterParserUtil.wildcardIfEmpty(status);
        return testCaseExecutionDao.findExecutionbyCriteria1(dateLimitFrom, test, testCase, application, country, environment, controlStatus, status);
    }

    @Override
    public long registerRunID(TCExecution tCExecution) {

        /**
         * Insert TestCaseExecution
         */
        try {
            this.insertTCExecution(tCExecution);
        } catch (CerberusException ex) {
            MyLogger.log(TestCaseExecutionService.class.getName(), Level.FATAL, ex.toString());
        }
        return tCExecution.getId();
    }
}
