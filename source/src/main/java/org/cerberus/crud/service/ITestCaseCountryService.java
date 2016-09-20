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
package org.cerberus.crud.service;

import java.util.List;

import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author bcivel
 */
public interface ITestCaseCountryService {

    /**
     *
     * @param test
     * @param testCase
     * @param Country
     * @return
     */
    TestCaseCountry findTestCaseCountryByKey(String test, String testCase, String Country) throws CerberusException;

    List<TestCaseCountry> findTestCaseCountryByTestTestCase(String test, String testCase);

    List<String> findListOfCountryByTestTestCase(String test, String testcase);

    void insertTestCaseCountry(TestCaseCountry testCaseCountry) throws CerberusException;

    boolean insertListTestCaseCountry(List<TestCaseCountry> testCaseCountryList);

    //void updateTestCaseCountry(TestCaseCountry tccLeft) throws CerberusException;
    void deleteTestCaseCountry(TestCaseCountry tcc) throws CerberusException;

    void deleteListTestCaseCountry(List<TestCaseCountry> tccToDelete) throws CerberusException;

    /**
     *
     * @param system
     * @param test
     * @param testCase
     * @return
     */
    public AnswerList readByTestTestCase(String system, String test, String testCase);

    /**
     *
     * @param test
     * @param testCase
     * @param country
     * @return
     */
    public AnswerItem readByKey(String test, String testCase, String country);

    public Answer create(TestCaseCountry testDataLibData);

    public Answer update(TestCaseCountry testDataLibData);

    public Answer delete(TestCaseCountry testDataLibData);

    public Answer createList(List<TestCaseCountry> objectList);

    public Answer deleteList(List<TestCaseCountry> objectList);

    public Answer compareListAndUpdateInsertDeleteElements(String test, String testCase, List<TestCaseCountry> newList);

    public TestCaseCountry convert(AnswerItem answerItem) throws CerberusException;

    public List<TestCaseCountry> convert(AnswerList answerList) throws CerberusException;

    public void convert(Answer answer) throws CerberusException;
    
    public Answer duplicateList(List<TestCaseCountry> objectList, String targetTest, String targetTestCase);
}
