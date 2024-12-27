/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.crud.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.ITestCaseCountryDAO;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.crud.entity.TestCaseCountry;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.service.ITestCaseCountryService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class TestCaseCountryService implements ITestCaseCountryService {

    @Autowired
    ITestCaseCountryDAO testcaseCountryDAO;

    private final String OBJECT_NAME = "TestCaseCountry";

    private static final Logger LOG = LogManager.getLogger(TestCaseCountryService.class);

    @Override
    public List<TestCaseCountry> findTestCaseCountryByTestTestCase(String test, String testcase) {
        return testcaseCountryDAO.findTestCaseCountryByTestTestCase(test, testcase);
    }

    @Override
    public List<String> findListOfCountryByTestTestCase(String test, String testcase) {
        List<String> countries = new ArrayList<>();
        this.testcaseCountryDAO.findTestCaseCountryByTestTestCase(test, testcase).forEach(testcaseCountry -> {
            countries.add(testcaseCountry.getCountry());
        });
        return countries;
    }

    @Override
    public boolean insertListTestCaseCountry(List<TestCaseCountry> testCaseCountryList) {
        for (TestCaseCountry tcc : testCaseCountryList) {
            try {
                this.convert(create(tcc));
            } catch (CerberusException ex) {
                LOG.warn(ex.toString());
                return false;
            }
        }
        return true;
    }

//    @Override
//    public void updateTestCaseCountry(TestCaseCountry testcaseCountry) throws CerberusException {
//        testcaseCountryDAO.updateTestCaseCountry(testcaseCountry);
//    }
    @Override
    public void deleteListTestCaseCountry(List<TestCaseCountry> tccToDelete) throws CerberusException {
        for (TestCaseCountry tcc : tccToDelete) {
            this.convert(this.delete(tcc));
        }
    }

    @Override
    public AnswerItem<TestCaseCountry> readByKey(String test, String testCase, String country) {
        return testcaseCountryDAO.readByKey(test, testCase, country);
    }

    @Override
    public AnswerList<TestCaseCountry> readByTestTestCase(List<String> system, String test, String testCase, List<TestCase> testCaseList) {
        return testcaseCountryDAO.readByVarious1(system, test, testCase, testCaseList);
    }

    @Override
    public HashMap<String, TestCaseCountry> readByTestTestCaseToHashCountryAsKey(String test, String testCase) {

        HashMap<String, TestCaseCountry> testcaseCountries = new HashMap<>();
        this.readByTestTestCase(null, test, testCase, null).getDataList().forEach(testcaseCountry -> {
            testcaseCountries.put(testcaseCountry.getCountry(), testcaseCountry);
        });
        return testcaseCountries;
    }

    @Override
    public HashMap<String, HashMap<String, TestCaseCountry>> convertListToHashMapTestTestCaseAsKey(List<TestCaseCountry> testCaseCountryList) {

        HashMap<String, HashMap<String, TestCaseCountry>> testCaseCountries = new HashMap<>();
        for (TestCaseCountry testCaseCountry : testCaseCountryList) {
            String key = testCaseCountry.getTest() + "##" + testCaseCountry.getTestcase();
            if (testCaseCountries.containsKey(key)) {
                testCaseCountries.get(key).put(testCaseCountry.getCountry(), testCaseCountry);
            } else {
                testCaseCountries.put(key, new HashMap<>());
                testCaseCountries.get(key).put(testCaseCountry.getCountry(), testCaseCountry);
            }
        }
        return testCaseCountries;
    }

    @Override
    public boolean exist(String test, String testcase, String country) {
            AnswerItem objectAnswer = readByKey(test, testcase, country);
            return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public Answer create(TestCaseCountry testDataLibData) {
        return testcaseCountryDAO.create(testDataLibData);
    }

    @Override
    public Answer update(TestCaseCountry testDataLibData) {
        return testcaseCountryDAO.update(testDataLibData);
    }

    @Override
    public Answer delete(TestCaseCountry testDataLibData) {
        return testcaseCountryDAO.delete(testDataLibData);
    }

    @Override
    public Answer createList(List<TestCaseCountry> objectList) {
        Answer ans = new Answer(null);
        for (TestCaseCountry objectToCreate : objectList) {
            ans = testcaseCountryDAO.create(objectToCreate);
        }
        return ans;
    }

    @Override
    public Answer deleteList(List<TestCaseCountry> objectList) {
        Answer ans = new Answer(null);
        for (TestCaseCountry objectToCreate : objectList) {
            ans = testcaseCountryDAO.delete(objectToCreate);
        }
        return ans;
    }

    @Override
    public Answer compareListAndUpdateInsertDeleteElements(String test, String testCase, List<TestCaseCountry> newList) {
        Answer ans = new Answer(null);

        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        List<TestCaseCountry> oldList = new ArrayList<>();
        try {
            oldList = this.convert(this.readByTestTestCase(null, test, testCase, null));
        } catch (CerberusException ex) {
            LOG.error(ex, ex);
        }

        /**
         * Update and Create all objects database Objects from newList
         */
        List<TestCaseCountry> listToUpdateOrInsert = new ArrayList<>(newList);
        listToUpdateOrInsert.removeAll(oldList);
        List<TestCaseCountry> listToUpdateOrInsertToIterate = new ArrayList<>(listToUpdateOrInsert);

        for (TestCaseCountry objectDifference : listToUpdateOrInsertToIterate) {
            for (TestCaseCountry objectInDatabase : oldList) {
                if (objectDifference.hasSameKey(objectInDatabase)) {
                    ans = this.update(objectDifference);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                    listToUpdateOrInsert.remove(objectDifference);
                }
            }
        }

        /**
         * Delete all objects database Objects that do not exist from newList
         */
        List<TestCaseCountry> listToDelete = new ArrayList<>(oldList);
        listToDelete.removeAll(newList);
        List<TestCaseCountry> listToDeleteToIterate = new ArrayList<>(listToDelete);

        for (TestCaseCountry tcsDifference : listToDeleteToIterate) {
            for (TestCaseCountry tcsInPage : newList) {
                if (tcsDifference.hasSameKey(tcsInPage)) {
                    listToDelete.remove(tcsDifference);
                }
            }
        }
        if (!listToDelete.isEmpty()) {
            ans = this.deleteList(listToDelete);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
        }

        // We insert only at the end (after deletion of all potencial enreg - linked with #1281)
        if (!listToUpdateOrInsert.isEmpty()) {
            ans = this.createList(listToUpdateOrInsert);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
        }
        return finalAnswer;
    }

    @Override
    public TestCaseCountry convert(AnswerItem<TestCaseCountry> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<TestCaseCountry> convert(AnswerList<TestCaseCountry> answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerList.getDataList();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public void convert(Answer answer) throws CerberusException {
        if (!answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
        }
    }

    @Override
    public Answer duplicateList(List<TestCaseCountry> objectList, String targetTest, String targetTestCase) {
        Answer ans = new Answer(null);
        List<TestCaseCountry> listToCreate = new ArrayList<>();
        for (TestCaseCountry objectToDuplicate : objectList) {
            objectToDuplicate.setTest(targetTest);
            objectToDuplicate.setTestcase(targetTestCase);
            listToCreate.add(objectToDuplicate);
        }
        ans = createList(listToCreate);
        return ans;
    }

}
