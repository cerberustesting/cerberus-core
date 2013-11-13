/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.ITestCaseDAO;
import com.redcats.tst.entity.TCase;
import com.redcats.tst.entity.TestCase;
import com.redcats.tst.entity.TestCaseCountry;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.service.ITestCaseCountryService;
import com.redcats.tst.service.ITestCaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author bcivel
 * @author tbernardes
 */
@Service
public class TestCaseService implements ITestCaseService {

    @Autowired
    private ITestCaseDAO testCaseDao;
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
        return testCaseDao.findTestCaseByCriteria(test, application, country, "Y");
    }

    /**
     * @since 0.9.1
     */
    @Override
    public List<TCase> findTestCaseByAllCriteria(TCase tCase, String text, String system) {
        return this.testCaseDao.findTestCaseByCriteria(tCase, text, system);
    }

    /**
     * @since 0.9.1
     */
    @Override
    public List<String> findUniqueDataOfColumn(String column) {
        return this.testCaseDao.findUniqueDataOfColumn(column);
    }
}
