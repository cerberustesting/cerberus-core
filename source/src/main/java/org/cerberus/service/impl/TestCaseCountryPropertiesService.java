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

import java.util.List;

import org.cerberus.dao.ITestCaseCountryPropertiesDAO;
import org.cerberus.entity.TestCaseCountryProperties;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ITestCaseCountryPropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseCountryPropertiesService implements ITestCaseCountryPropertiesService {

    @Autowired
    ITestCaseCountryPropertiesDAO testCaseCountryPropertiesDAO;
            
    @Override
    public List<TestCaseCountryProperties> findListOfPropertyPerTestTestCaseCountry(String test, String testCase, String country) {
        return testCaseCountryPropertiesDAO.findListOfPropertyPerTestTestCaseCountry(test, testCase, country); 
    }

    @Override
    public List<TestCaseCountryProperties> findListOfPropertyPerTestTestCase(String test, String testcase) {
        return testCaseCountryPropertiesDAO.findListOfPropertyPerTestTestCase(test, testcase);
    }

    @Override
    public List<TestCaseCountryProperties> findDistinctPropertiesOfTestCase(String test, String testcase) {
        return testCaseCountryPropertiesDAO.findDistinctPropertiesOfTestCase(test, testcase);
    }

    @Override
    public List<String> findCountryByProperty(TestCaseCountryProperties testCaseCountryProperties) {
        return testCaseCountryPropertiesDAO.findCountryByProperty(testCaseCountryProperties);
    }

    @Override
    public TestCaseCountryProperties findTestCaseCountryPropertiesByKey(String test, String testCase, String country, String property) throws CerberusException {
        return testCaseCountryPropertiesDAO.findTestCaseCountryPropertiesByKey(test, testCase, country, property);
    }
        
       
}
