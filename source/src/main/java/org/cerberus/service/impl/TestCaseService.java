/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.service.impl;

import org.cerberus.dao.ITestCaseDAO;
import org.cerberus.entity.TCase;
import org.cerberus.entity.TestCase;
import org.cerberus.entity.TestCaseCountry;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ITestCaseCountryService;
import org.cerberus.service.ITestCaseService;
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
