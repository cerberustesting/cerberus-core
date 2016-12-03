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
package org.cerberus.crud.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cerberus.crud.dao.ITestCaseDAO;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.crud.service.ITestCaseCountryService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.ITestCaseStepActionControlService;
import org.cerberus.crud.service.ITestCaseStepActionService;
import org.cerberus.crud.service.ITestCaseStepService;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.crud.factory.IFactoryTestCase;

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
    @Autowired
    private ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
    @Autowired
    private ITestCaseStepService testCaseStepService;
    @Autowired
    private ITestCaseStepActionService testCaseStepActionService;
    @Autowired
    private ITestCaseStepActionControlService testCaseStepActionControlService;
    @Autowired
    private IFactoryTestCase factoryTCase;

    @Override
    public TestCase findTestCaseByKey(String test, String testCase) throws CerberusException {
        return testCaseDao.findTestCaseByKey(test, testCase);
    }

    @Override
    public TestCase findTestCaseByKeyWithDependency(String test, String testCase) throws CerberusException {
        TestCase newTcase;
        newTcase = findTestCaseByKey(test, testCase);
        if (newTcase == null) {
            //TODO:FN temporary debug messages
            org.apache.log4j.Logger.getLogger(TestCaseService.class.getName()).log(org.apache.log4j.Level.ERROR, "test case is null - test: " + test + " testcase: " + testCase);
        } else {
            List<TestCaseCountry> testCaseCountry = testCaseCountryService.findTestCaseCountryByTestTestCase(test, testCase);
            List<TestCaseCountry> testCaseCountryToAdd = new ArrayList();
            for (TestCaseCountry tcc : testCaseCountry) {
                List<TestCaseCountryProperties> properties = testCaseCountryPropertiesService.findListOfPropertyPerTestTestCaseCountry(test, testCase, tcc.getCountry());
                tcc.setTestCaseCountryProperty(properties);
                testCaseCountryToAdd.add(tcc);
            }
            newTcase.setTestCaseCountry(testCaseCountryToAdd);

            String initialTest = test;
            String initialTc = testCase;
            List<TestCaseStep> tcs = testCaseStepService.getListOfSteps(test, testCase);
            List<TestCaseStep> tcsToAdd = new ArrayList();
            for (TestCaseStep step : tcs) {
                int stepNumber = step.getStep();
                int initialStep = step.getStep();
                if (step.getUseStep().equals("Y")) {
                    test = step.getUseStepTest();
                    testCase = step.getUseStepTestCase();
                    stepNumber = step.getUseStepStep();
                }
                List<TestCaseStepAction> tcsa = testCaseStepActionService.getListOfAction(test, testCase, stepNumber);
                List<TestCaseStepAction> tcsaToAdd = new ArrayList();
                for (TestCaseStepAction action : tcsa) {
                    List<TestCaseStepActionControl> tcsac = testCaseStepActionControlService.findControlByTestTestCaseStepSequence(test, testCase, stepNumber, action.getSequence());
                    List<TestCaseStepActionControl> tcsacToAdd = new ArrayList();
                    for (TestCaseStepActionControl control : tcsac) {
                        control.setTest(initialTest);
                        control.setTestCase(initialTc);
                        control.setStep(initialStep);
                        tcsacToAdd.add(control);
                    }
                    action.setTestCaseStepActionControl(tcsacToAdd);
                    action.setTest(initialTest);
                    action.setTestCase(initialTc);
                    action.setStep(initialStep);
                    tcsaToAdd.add(action);
                }
                step.setTestCaseStepAction(tcsaToAdd);
                tcsToAdd.add(step);
            }
            newTcase.setTestCaseStep(tcsToAdd);
        }
        return newTcase;
    }

    @Override
    public List<TestCase> findTestCaseByTest(String test) {
        return testCaseDao.findTestCaseByTest(test);
    }

    @Override
    public List<TestCase> findTestCaseByTestSystem(String test, String system) {
        return testCaseDao.findTestCaseByTestSystem(test, system);
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
    public boolean createTestCase(TestCase testCase) throws CerberusException {
        return testCaseDao.createTestCase(testCase);
    }

    @Override
    public List<TestCase> findTestCaseActiveByCriteria(String test, String application, String country) {
        return testCaseDao.findTestCaseByCriteria(test, application, country, "Y");
    }

    /**
     * @since 0.9.1
     */
    @Override
    public List<TestCase> findTestCaseByAllCriteria(TestCase tCase, String text, String system) {
        return this.testCaseDao.findTestCaseByCriteria(tCase, text, system);
    }

    @Override
    public AnswerList readByVariousCriteria(String[] test, String[] idProject, String[] app, String[] creator, String[] implementer, String[] system,
            String[] testBattery, String[] campaign, String[] priority, String[] group, String[] status, int length) {
        return testCaseDao.readByVariousCriteria(test, idProject, app, creator, implementer, system, testBattery, campaign, priority, group, status, length);
    }

    /**
     * @param column
     * @return
     * @since 0.9.1
     */
    @Override
    public List<String> findUniqueDataOfColumn(String column) {
        return this.testCaseDao.findUniqueDataOfColumn(column);
    }

    @Override
    public List<String> findTestWithTestCaseActiveAutomatedBySystem(String system) {
        TestCase tCase = factoryTCase.create(null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, -1, null, null, null, null, null, "Y", null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        List<String> result = new ArrayList();
        List<TestCase> testCases = findTestCaseByAllCriteria(tCase, null, system);
        for (TestCase testCase : testCases) {
            if (!testCase.getGroup().equals("PRIVATE")) {
                result.add(testCase.getTest());
            }
        }
        Set<String> uniqueResult = new HashSet<String>(result);
        result = new ArrayList();
        result.addAll(uniqueResult);
        Collections.sort(result);
        return result;
    }

    @Override
    public List<TestCase> findTestCaseActiveAutomatedBySystem(String test, String system) {
        TestCase tCase = factoryTCase.create(test, null, null, null, null, null, null, null, null, null,
                null, null, null, null, -1, null, null, null, null, null, "Y", null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        List<TestCase> result = new ArrayList();
        List<TestCase> testCases = findTestCaseByAllCriteria(tCase, null, system);
        for (TestCase testCase : testCases) {
            if (!testCase.getGroup().equals("PRIVATE")) {
                result.add(testCase);
            }
        }
        return result;
    }

    @Override
    public boolean deleteTestCase(TestCase testCase) {
        return testCaseDao.deleteTestCase(testCase);
    }

    @Override
    public void updateTestCaseField(TestCase tc, String columnName, String value) {
        testCaseDao.updateTestCaseField(tc, columnName, value);
    }

    /**
     * @since 1.0.2
     */
    @Override
    public List<TestCase> findTestCaseByGroupInCriteria(TestCase tCase, String system) {
        return this.testCaseDao.findTestCaseByGroupInCriteria(tCase, system);
    }

    @Override
    public void updateTestCase(TestCase tc) throws CerberusException {
        testCaseDao.updateTestCase(tc);
    }

    @Override
    public List<TestCase> findTestCaseByCampaignName(String campaign) {
        return testCaseDao.findTestCaseByCampaignName(campaign);
    }

    @Override
    public String getMaxNumberTestCase(String test) {
        return this.testCaseDao.getMaxNumberTestCase(test);
    }

    @Override
    public List<TestCase> findTestCaseByCampaignNameAndCountries(String campaign, String[] countries) {
        return this.testCaseDao.findTestCaseByCampaignNameAndCountries(campaign, countries);
    }

    @Override
    public List<TestCase> findUseTestCaseList(String test, String testCase) throws CerberusException {
        List<TestCase> result = new ArrayList();
        List<TestCaseStep> tcsList = testCaseStepService.getListOfSteps(test, testCase);
        for (TestCaseStep tcs : tcsList) {
            if (("Y").equals(tcs.getUseStep())) {
                result.add(this.findTestCaseByKey(tcs.getUseStepTest(), tcs.getUseStepTestCase()));
            }
        }
        return result;
    }

    @Override
    public List<TestCase> findByCriteria(String[] test, String[] project, String[] app, String[] active, String[] priority, String[] status, String[] group, String[] targetBuild, String[] targetRev, String[] creator, String[] implementer, String[] function, String[] campaign, String[] battery) {
        return testCaseDao.findTestCaseByCriteria(test, project, app, active, priority, status, group, targetBuild, targetRev, creator, implementer, function, campaign, battery);
    }

    @Override
    public String findSystemOfTestCase(String test, String testcase) throws CerberusException {
        return testCaseDao.findSystemOfTestCase(test, testcase);
    }

    @Override
    public AnswerList findTestCasesThatUseTestDataLib(int testDataLibId, String name, String country) {
        return testCaseCountryPropertiesService.findTestCaseCountryPropertiesByValue1(testDataLibId, name, country, TestCaseCountryProperties.TYPE_GETFROMDATALIB);
    }

    @Override
    public AnswerList readTestCaseByStepsInLibrary(String test) {
        return testCaseDao.readTestCaseByStepsInLibrary(test);
    }

    @Override
    public AnswerList readByTestByCriteria(String system, String test, int start, int amount, String sortInformation, String searchTerm, Map<String, List<String>> individualSearch) {
        return testCaseDao.readByTestByCriteria(system, test, start, amount, sortInformation, searchTerm, individualSearch);
    }

    @Override
    public AnswerItem readByKey(String test, String testCase) {
        return testCaseDao.readByKey(test, testCase);
    }

    @Override
    public AnswerList<List<String>> readDistinctValuesByCriteria(String system, String test, String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return testCaseDao.readDistinctValuesByCriteria(system, test, searchParameter, individualSearch, columnName);
    }

    @Override
    public Answer update(TestCase testCase) {
        return testCaseDao.update(testCase);
    }

    @Override
    public Answer create(TestCase testCase) {
        return testCaseDao.create(testCase);
    }

    @Override
    public Answer delete(TestCase testCase) {
        return testCaseDao.delete(testCase);
    }
}
