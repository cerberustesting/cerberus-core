/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.impl.TestCaseCountryDAO;
import com.redcats.tst.entity.TestCaseCountry;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.service.ITestCaseCountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author bcivel
 */
@Service
public class TestCaseCountryService implements ITestCaseCountryService {

    @Autowired
    TestCaseCountryDAO tccDao;

    @Override
    public List<TestCaseCountry> findTestCaseCountryByTestTestCase(String test, String testCase) {
        return tccDao.findTestCaseCountryByTestTestCase(test, testCase);
    }

    @Override
    public List<String> findListOfCountryByTestTestCase(String test, String testcase) {
        List<String> result = new ArrayList<String>();
        for (TestCaseCountry tcc : this.tccDao.findTestCaseCountryByTestTestCase(test, testcase)) {
            result.add(tcc.getCountry());
        }
        return result;
    }

    @Override
    public TestCaseCountry findTestCaseCountryByKey(String test, String testCase, String country) throws CerberusException {
        return this.tccDao.findTestCaseCountryByKey(test, testCase, country);
    }
}
