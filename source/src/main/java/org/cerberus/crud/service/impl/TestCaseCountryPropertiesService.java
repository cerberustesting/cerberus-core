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
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Level;
import org.cerberus.crud.dao.ITestCaseCountryPropertiesDAO;
import org.cerberus.crud.dao.ITestCaseStepActionDAO;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.AnswerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 * @author FNogueira
 */
@Service
public class TestCaseCountryPropertiesService implements ITestCaseCountryPropertiesService {

    @Autowired
    ITestCaseCountryPropertiesDAO testCaseCountryPropertiesDAO;
    @Autowired
    ITestCaseStepActionDAO testCaseStepActionDAO;
    @Autowired
    ITestCaseService testCaseService;
    @Autowired
    private DatabaseSpring dbmanager;

    private final String OBJECT_NAME = "TestCaseCountryProperties";

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CountryEnvironmentDatabaseService.class);

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
        for (TestCaseCountryProperties tccp : testCaseCountryPropertiesList) {
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
        for (TestCaseCountryProperties tccp : tccpToDelete) {
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
        TestCase mainTC = testCaseService.findTestCaseByKey(test, testcase);

        /**
         * We load here all the properties countries from all related testcases
         * linked with test/testcase The order the load is done is important as
         * it will define the priority of each property. properties coming from
         * Pre Testing is the lower prio then, the property coming from the
         * useStep and then, top priority is the property on the test +
         * testcase.
         */
        //find all properties of preTests
        List<TestCase> tcptList = testCaseService.findTestCaseActiveByCriteria("Pre Testing", mainTC.getApplication(), country);
        for (TestCase tcase : tcptList) {
            tccpList.addAll(testCaseCountryPropertiesDAO.findListOfPropertyPerTestTestCase(tcase.getTest(), tcase.getTestCase()));
        }
        //find all properties of the used step
        List<TestCase> tcList = testCaseService.findUseTestCaseList(test, testcase);
        for (TestCase tcase : tcList) {
            tccpList.addAll(testCaseCountryPropertiesDAO.findListOfPropertyPerTestTestCase(tcase.getTest(), tcase.getTestCase()));
        }
        //find all properties of the testcase
        tccpList.addAll(testCaseCountryPropertiesDAO.findListOfPropertyPerTestTestCase(test, testcase));

        /**
         * We loop here the previous list, keeping by property, the last value
         * (top priority). That will define the level to consider property on
         * the test. testcase.
         */
        HashMap<String, TestCaseCountryProperties> tccpMap1 = new HashMap<String, TestCaseCountryProperties>();
        LOG.debug("Init");
        for (TestCaseCountryProperties tccp : tccpList) {
            LOG.debug(tccp.getProperty() + " - " + tccp.getCountry() + " - " + tccp.getTest() + " - " + tccp.getTestCase());
            tccpMap1.put(tccp.getProperty(), tccp);
        }

        /**
         * We then loop again in order to keep the selected properties for the
         * given country and level found on the previous step by property.
         */
        List<TestCaseCountryProperties> result = new ArrayList<TestCaseCountryProperties>();
        for (TestCaseCountryProperties tccp : tccpList) {
            if (tccp.getCountry().equals(country)) {
                TestCaseCountryProperties toto = (TestCaseCountryProperties) tccpMap1.get(tccp.getProperty());
                if ((toto != null)
                        && (((tccp.getTest().equals("Pre Testing")) && (toto.getTest().equals("Pre Testing")))
                        || ((tccp.getTest().equals(test)) && (tccp.getTestCase().equals(testcase)) && (toto.getTest().equals(test)) && (toto.getTestCase().equals(testcase)))
                        || ((tccp.getTest().equals(toto.getTest())) && (tccp.getTestCase().equals(toto.getTestCase()))))) {
                    result.add(tccp);
                    LOG.debug(tccp.getProperty() + " - " + tccp.getCountry() + " - " + tccp.getTest() + " - " + tccp.getTestCase());
                }
            }
        }

        return result;
    }

    @Override
    public AnswerList findTestCaseCountryPropertiesByValue1(int testDataLibID, String name, String country, String propertyType) {
        return testCaseCountryPropertiesDAO.findTestCaseCountryPropertiesByValue1(testDataLibID, name, country, propertyType);
    }

    @Override
    public Answer createListTestCaseCountryPropertiesBatch(List<TestCaseCountryProperties> objectList) {

        dbmanager.beginTransaction();
        Answer answer = testCaseCountryPropertiesDAO.createTestCaseCountryPropertiesBatch(objectList);

        if (!answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            dbmanager.abortTransaction();
        } else {
            dbmanager.commitTransaction();
        }
        return answer;
    }

    @Override
    public Answer create(TestCaseCountryProperties object) {
        return testCaseCountryPropertiesDAO.create(object);
    }

    @Override
    public Answer delete(TestCaseCountryProperties object) {
        return testCaseCountryPropertiesDAO.delete(object);
    }

    @Override
    public Answer update(TestCaseCountryProperties object) {
        return testCaseCountryPropertiesDAO.update(object);
    }

    @Override
    public Answer createList(List<TestCaseCountryProperties> objectList) {
        Answer ans = new Answer(null);
        for (TestCaseCountryProperties objectToCreate : objectList) {
            ans = testCaseCountryPropertiesDAO.create(objectToCreate);
        }
        return ans;
    }

    @Override
    public Answer deleteList(List<TestCaseCountryProperties> objectList) {
        Answer ans = new Answer(null);
        for (TestCaseCountryProperties objectToDelete : objectList) {
            ans = testCaseCountryPropertiesDAO.delete(objectToDelete);
        }
        return ans;
    }

    @Override
    public Answer compareListAndUpdateInsertDeleteElements(String test, String testCase, List<TestCaseCountryProperties> newList) {
        Answer ans = new Answer(null);

        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        List<TestCaseCountryProperties> oldList = new ArrayList();
//        try {
        oldList = this.findListOfPropertyPerTestTestCase(test, testCase);
//        } catch (CerberusException ex) {
//            LOG.error(ex);
//        }

        /**
         * Iterate on (Object From Page - Object From Database) If Object in
         * Database has same key : Update and remove from the list. If Object in
         * database does ot exist : Insert it.
         */
        List<TestCaseCountryProperties> listToUpdateOrInsert = new ArrayList(newList);
        listToUpdateOrInsert.removeAll(oldList);
        List<TestCaseCountryProperties> listToUpdateOrInsertToIterate = new ArrayList(listToUpdateOrInsert);

        for (TestCaseCountryProperties objectDifference : listToUpdateOrInsertToIterate) {
            for (TestCaseCountryProperties objectInDatabase : oldList) {
                if (objectDifference.hasSameKey(objectInDatabase)) {
                    ans = this.update(objectDifference);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
                    listToUpdateOrInsert.remove(objectDifference);
                }
            }
        }
        if (!listToUpdateOrInsert.isEmpty()) {
            ans = this.createList(listToUpdateOrInsert);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
        }

        /**
         * Iterate on (Object From Database - Object From Page). If Object in
         * Page has same key : remove from the list. Then delete the list of
         * Object
         */
        List<TestCaseCountryProperties> listToDelete = new ArrayList(oldList);
        listToDelete.removeAll(newList);
        List<TestCaseCountryProperties> listToDeleteToIterate = new ArrayList(listToDelete);

        for (TestCaseCountryProperties objectDifference : listToDeleteToIterate) {
            for (TestCaseCountryProperties objectInPage : newList) {
                if (objectDifference.hasSameKey(objectInPage)) {
                    listToDelete.remove(objectDifference);
                }
            }
        }
        if (!listToDelete.isEmpty()) {
            ans = this.deleteList(listToDelete);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
        }
        return finalAnswer;
    }

    @Override
    public Answer duplicateList(List<TestCaseCountryProperties> objectList, String targetTest, String targetTestCase) {
        Answer ans = new Answer(null);
        List<TestCaseCountryProperties> listToCreate = new ArrayList();
        for (TestCaseCountryProperties objectToDuplicate : objectList) {
            objectToDuplicate.setTest(targetTest);
            objectToDuplicate.setTestCase(targetTestCase);
            listToCreate.add(objectToDuplicate);
        }
        ans = createList(listToCreate);
        return ans;
    }

}
