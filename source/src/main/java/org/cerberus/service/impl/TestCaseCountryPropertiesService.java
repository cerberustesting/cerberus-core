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
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Level;
import org.cerberus.dao.ITestCaseCountryPropertiesDAO;
import org.cerberus.dao.ITestCaseStepActionDAO;
import org.cerberus.entity.TCase;
import org.cerberus.entity.TestCaseCountryProperties;
import org.cerberus.entity.TestCaseStep;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.ITestCaseCountryPropertiesService;
import org.cerberus.service.ITestCaseService;
import org.cerberus.service.ITestCaseStepService;
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
    @Autowired
    ITestCaseStepActionDAO testCaseStepActionDAO;
    @Autowired
    ITestCaseService testCaseService;
            
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
      
    @Override
    public void insertTestCaseCountryProperties(TestCaseCountryProperties testCaseCountryProperties) throws CerberusException {
        testCaseCountryPropertiesDAO.insertTestCaseCountryProperties(testCaseCountryProperties);
    }
    
    @Override
    public boolean insertListTestCaseCountryProperties(List<TestCaseCountryProperties> testCaseCountryPropertiesList) {
        for (TestCaseCountryProperties tccp : testCaseCountryPropertiesList){
            try {
                insertTestCaseCountryProperties(tccp);
            } catch (CerberusException ex) {
                MyLogger.log(TestCaseStepService.class.getName(), Level.FATAL, ex.toString());
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void updateTestCaseCountryProperties(TestCaseCountryProperties testCaseCountryProperties) throws CerberusException {
        testCaseCountryPropertiesDAO.updateTestCaseCountryProperties(testCaseCountryProperties);
    }

    @Override
    public List<String> findCountryByPropertyNameAndTestCase(String test, String testcase, String property) {
        return testCaseCountryPropertiesDAO.findCountryByPropertyNameAndTestCase(test, testcase, property);
    }

    @Override
    public void deleteListTestCaseCountryProperties(List<TestCaseCountryProperties> tccpToDelete) throws CerberusException {
        for (TestCaseCountryProperties tccp : tccpToDelete){
            deleteTestCaseCountryProperties(tccp);
        }
    }

    @Override
    public void deleteTestCaseCountryProperties(TestCaseCountryProperties tccp) throws CerberusException {
        testCaseCountryPropertiesDAO.deleteTestCaseCountryProperties(tccp);
    }
    
    @Override
    public List<TestCaseCountryProperties> findAllWithDependencies(String test, String testcase, String country) throws CerberusException {
        List<TestCaseCountryProperties> tccpList = new ArrayList();
        List<TestCaseCountryProperties> tccpListPerCountry = new ArrayList();
        TCase mainTC = testCaseService.findTestCaseByKey(test, testcase);
        
        //find all properties of preTests
        List<TCase> tcptList = testCaseService.findTestCaseActiveByCriteria("Pre Testing", mainTC.getApplication(), country);
        for (TCase tcase : tcptList){
            tccpList.addAll(testCaseCountryPropertiesDAO.findListOfPropertyPerTestTestCase(tcase.getTest(), tcase.getTestCase()));
            tccpListPerCountry.addAll(testCaseCountryPropertiesDAO.findListOfPropertyPerTestTestCaseCountry(tcase.getTest(), tcase.getTestCase(), country));
        }
        
        //find all properties of the used step
        List<TCase> tcList = testCaseService.findUseTestCaseList(test, testcase);
        for (TCase tcase : tcList){
            tccpList.addAll(testCaseCountryPropertiesDAO.findListOfPropertyPerTestTestCase(tcase.getTest(), tcase.getTestCase()));            
            tccpListPerCountry.addAll(testCaseCountryPropertiesDAO.findListOfPropertyPerTestTestCaseCountry(tcase.getTest(), tcase.getTestCase(), country));
        }
        
        //find all properties of the testcase
        tccpList.addAll(testCaseCountryPropertiesDAO.findListOfPropertyPerTestTestCase(test, testcase));
        tccpListPerCountry.addAll(testCaseCountryPropertiesDAO.findListOfPropertyPerTestTestCaseCountry(test, testcase, country));
        
        //Keep only one property by name
        //all properties that are defined for the country are included
        HashMap tccpMap = new HashMap();
        for (TestCaseCountryProperties tccp : tccpListPerCountry){
            tccpMap.put(tccp.getProperty(), tccp);
        }
        //These if/else instructions are done because of the way how the propertyService verifies if
        //the properties exist for the country. 
        for (TestCaseCountryProperties tccp : tccpList){
            TestCaseCountryProperties p = (TestCaseCountryProperties)tccpMap.get(tccp.getProperty());
            if(p == null){
                tccpMap.put(tccp.getProperty(), tccp);
            }else{
                if(p.getCountry().compareTo(country) != 0 && tccp.getCountry().compareTo(country) == 0){
                    tccpMap.put(tccp.getProperty(), tccp);
                }
            }
        }
        
        List<TestCaseCountryProperties> result = new ArrayList<TestCaseCountryProperties>(tccpMap.values());
        return result;
    }

}
