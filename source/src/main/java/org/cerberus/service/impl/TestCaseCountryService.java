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

import java.util.ArrayList;
import java.util.List;

import org.cerberus.dao.impl.TestCaseCountryDAO;
import org.cerberus.entity.TestCaseCountry;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ITestCaseCountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
