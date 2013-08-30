/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.ITestCaseDAO;
import com.redcats.tst.entity.TCase;
import com.redcats.tst.entity.TestCase;
import com.redcats.tst.entity.TestCaseCountry;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.service.IInvariantService;
import com.redcats.tst.service.ITestCaseCountryService;
import com.redcats.tst.service.ITestCaseService;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author bcivel
 */
@Service
public class TestCaseService implements ITestCaseService {
    
    @Autowired
    private ITestCaseDAO testCaseDao;
    @Autowired
    private IInvariantService invariantService;
    @Autowired
    private ITestCaseCountryService testCaseCountryService;

    @Override
    public TCase findTestCaseByKey(String test, String testCase) throws CerberusException {
        return testCaseDao.findTestCaseByKey(test, testCase);
    }

    @Override
    public TCase findTestCaseByKeyWithDependency(String test, String testCase) throws CerberusException {
        TCase newTcase;
        newTcase = testCaseDao.findTestCaseByKey(test, testCase);
        List<TestCaseCountry> testCaseCountry = testCaseCountryService.findTestCaseCountryByTestTestCase(test, testCase);
        newTcase.setTestCaseCountry(testCaseCountry);
        return newTcase;
    }
    
    
    @Override
    public List<TCase> findTestCaseByTest(String test) {
        return testCaseDao.findTestCaseByTest(test);
    }

    
    
    @Override
    public boolean updateTestCaseInformation(TestCase testCase) {
        return testCaseDao.updateTestCaseInformation(testCase);
    }

    @Override
    public boolean updateTestCaseInformationCountries(TestCase tc) {
        return testCaseDao.updateTestCaseInformationCountries(tc);
    }

    @Override
    public boolean createTestCase(TestCase testCase) {
        return testCaseDao.createTestCase(testCase);
    }

    @Override
    public List<TCase> findTestCaseActiveByCriteria(String test, String application, String country) {
        return testCaseDao.findTestCaseByCriteria ( test,  application,  country, "Y");
    }
    
}
