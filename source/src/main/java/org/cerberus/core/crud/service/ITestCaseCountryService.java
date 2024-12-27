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
package org.cerberus.core.crud.service;

import java.util.HashMap;
import java.util.List;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseCountry;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

/**
 *
 * @author bcivel
 */
public interface ITestCaseCountryService {


    List<TestCaseCountry> findTestCaseCountryByTestTestCase(String test, String testCase);

    List<String> findListOfCountryByTestTestCase(String test, String testcase);

    HashMap<String, TestCaseCountry> readByTestTestCaseToHashCountryAsKey(String test, String testCase);
    
    public HashMap<String, HashMap<String, TestCaseCountry>> convertListToHashMapTestTestCaseAsKey(List<TestCaseCountry> testCaseCountryList);

    boolean insertListTestCaseCountry(List<TestCaseCountry> testCaseCountryList);

    //void updateTestCaseCountry(TestCaseCountry tccLeft) throws CerberusException;

    void deleteListTestCaseCountry(List<TestCaseCountry> tccToDelete) throws CerberusException;

    /**
     *
     * @param test
     * @param testCase
     * @param country
     * @return
     */
    AnswerItem<TestCaseCountry> readByKey(String test, String testCase, String country);

    /**
     *
     * @param system
     * @param test
     * @param testCase
     * @param testCaseList
     * @return
     */
    AnswerList<TestCaseCountry> readByTestTestCase(List<String> system, String test, String testCase, List<TestCase> testCaseList);

    /**
     *
     * @param test
     * @param testcase
     * @param country
     * @return
     */
    boolean exist(String test, String testcase, String country);

    /**
     *
     * @param testDataLibData
     * @return
     */
    Answer create(TestCaseCountry testDataLibData);

    /**
     *
     * @param testDataLibData
     * @return
     */
    Answer update(TestCaseCountry testDataLibData);

    /**
     *
     * @param testDataLibData
     * @return
     */
    Answer delete(TestCaseCountry testDataLibData);

    /**
     *
     * @param objectList
     * @return
     */
    Answer createList(List<TestCaseCountry> objectList);

    /**
     *
     * @param objectList
     * @return
     */
    Answer deleteList(List<TestCaseCountry> objectList);

    /**
     *
     * @param test
     * @param testCase
     * @param newList
     * @return
     */
    Answer compareListAndUpdateInsertDeleteElements(String test, String testCase, List<TestCaseCountry> newList);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    TestCaseCountry convert(AnswerItem<TestCaseCountry> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<TestCaseCountry> convert(AnswerList<TestCaseCountry> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

    /**
     *
     * @param objectList
     * @param targetTest
     * @param targetTestCase
     * @return
     */
    Answer duplicateList(List<TestCaseCountry> objectList, String targetTest, String targetTestCase);
}
