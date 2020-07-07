/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.cerberus.crud.entity.TestCase;
import org.cerberus.dto.TestListDTO;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 * @author bcivel
 * @author tbernardes
 * @author FNogueira
 */
public interface ITestCaseService {

    /**
     * @param test
     * @param testCase
     * @return
     * @throws org.cerberus.exception.CerberusException
     */
    TestCase findTestCaseByKey(String test, String testCase) throws CerberusException;

    /**
     *
     * @param test
     * @param testCase
     * @return
     * @throws CerberusException
     */
    TestCase findTestCaseByKeyWithDependency(String test, String testCase) throws CerberusException;

    /**
     *
     * @param test
     * @param testCase
     * @param withSteps
     * @return
     * @throws CerberusException
     */
    AnswerItem<TestCase> findTestCaseByKeyWithDependencies(String test, String testCase, boolean withSteps) throws CerberusException;

    /**
     *
     * @param testCases
     * @param withSteps
     * @return
     * @throws CerberusException
     */
    AnswerItem<List<TestCase>> findTestCasesByTestWithDependencies(List<TestCase> testCases, boolean withSteps) throws CerberusException;

    /**
     *
     * @param test
     * @return
     */
    List<TestCase> findTestCaseByTest(String test);

    /**
     *
     * @param test
     * @param system
     * @return
     */
    List<TestCase> findTestCaseByTestSystem(String test, String system);

    /**
     *
     * @param application
     * @return
     */
    List<TestCase> findTestCaseByApplication(String application);

    /**
     * Retreive all testcase that match testFilter value, are active, exist for
     * country, match applicationFilter and inside build/revision range.
     *
     * @param testFilter
     * @param applicationFilter
     * @param country
     * @param system system of the main test case.
     * @param build current build
     * @param revision current revision
     * @return
     */
    List<TestCase> getTestCaseForPrePostTesting(String testFilter, String applicationFilter, String country, String system, String build, String revision);

    /**
     *
     * @param testCase
     * @return
     */
    boolean updateTestCaseInformation(TestCase testCase);

    /**
     *
     * @param tc
     * @return
     */
    boolean updateTestCaseInformationCountries(TestCase tc);

    /**
     *
     * @param testCase
     * @return
     * @throws CerberusException
     */
    boolean createTestCase(TestCase testCase) throws CerberusException;

    /**
     *
     * @param tCase
     * @param text
     * @param system
     * @return
     */
    List<TestCase> findTestCaseByAllCriteria(TestCase tCase, String text, String system);

    /**
     *
     * @param column
     * @return
     */
    List<String> findUniqueDataOfColumn(String column);

    /**
     * @param system
     * @return List of String formated like this >> Test
     * @since 0.9.2
     */
    List<String> findTestWithTestCaseActiveAutomatedBySystem(String system);

    /**
     * @param test
     * @param system
     * @return List of TCase object
     * @since 0.9.2
     */
    List<TestCase> findTestCaseActiveAutomatedBySystem(String test, String system);

    /**
     * @param testCase
     * @return true if delete is OK
     */
    boolean deleteTestCase(TestCase testCase);

    /**
     * @param campaign the campaign name
     * @param countries arrays of country
     * @return the list of TCase used in the campaign and activated for the
     * countries
     * @since 1.0.2
     */
    AnswerList<TestCase> findTestCaseByCampaignNameAndCountries(String campaign, String[] countries);

    /**
     * @param test
     * @return
     * @since 1.0.2
     */
    String getMaxNumberTestCase(String test);

    /**
     *
     * @param test
     * @param testCase
     * @return
     * @throws CerberusException
     */
    List<TestCase> findUseTestCaseList(String test, String testCase) throws CerberusException;

    /**
     *
     * @param test
     * @param project
     * @param app
     * @param active
     * @param priority
     * @param status
     * @param type
     * @param targetMajor
     * @param targetMinor
     * @param creator
     * @param implementer
     * @param campaign
     * @param battery
     * @return
     */
    List<TestCase> findByCriteria(String[] test, String[] app, String[] active, String[] priority, String[] status, String[] type, String[] targetMajor, String[] targetMinor, String[] creator, String[] implementer, String[] campaign, String[] battery);

    /**
     *
     * @param test
     * @param testcase
     * @return
     * @throws CerberusException
     */
    String findSystemOfTestCase(String test, String testcase) throws CerberusException;

    /**
     * Method that get all the testcases that use a determined testdatalib entry
     *
     * @param testDataLibId testdatalib unique identifier
     * @param name testdatalib name
     * @param country country for which testdatalib is defined
     * @return an answer with the test cases and a message indicating the status
     * of the operation
     */
    AnswerList<TestListDTO> findTestCasesThatUseTestDataLib(int testDataLibId, String name, String country);

    /**
     *
     * @param test
     * @return
     */
    AnswerList readTestCaseByStepsInLibrary(String test);

    /**
     * @param system
     * @param test
     * @param start
     * @param amount
     * @param sortInformation
     * @param searchTerm
     * @param individualSearch
     * @return
     */
    public AnswerList<TestCase> readByTestByCriteria(List<String> system, String test, int start, int amount, String sortInformation, String searchTerm, Map<String, List<String>> individualSearch);

    /**
     * @param test
     * @param testCase
     * @return
     */
    public AnswerItem<TestCase> readByKey(String test, String testCase);

    /**
     * @param test
     * @param testCase
     * @return
     */
    public AnswerItem<TestCase> readByKeyWithDependency(String test, String testCase);

    /**
     *
     * @param test
     * @param app
     * @param creator
     * @param implementer
     * @param system
     * @param campaign
     * @param labelid
     * @param priority
     * @param type
     * @param status
     * @param length
     * @return
     */
    public AnswerList<TestCase> readByVarious(String[] test, String[] app, String[] creator, String[] implementer, String[] system,
            String[] campaign, List<Integer> labelid, String[] priority, String[] type, String[] status, int length);

    /**
     *
     * @param system
     * @param test
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return
     */
    public AnswerList<String> readDistinctValuesByCriteria(List<String> system, String test, String searchParameter, Map<String, List<String>> individualSearch, String columnName);

    /**
     *
     * @param keyTest
     * @param keyTestCase
     * @param testCase
     * @return
     */
    public Answer update(String keyTest, String keyTestCase, TestCase testCase);

    /**
     *
     * @param testCase
     * @return
     */
    public Answer create(TestCase testCase);

    /**
     *
     * @param testCase
     * @return
     */
    public Answer delete(TestCase testCase);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    TestCase convert(AnswerItem<TestCase> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<TestCase> convert(AnswerList<TestCase> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

    /**
     * This method returns a boolean that define if the object can be access by
     * user authenticated by request. in case object is null it return if the
     * user defined by request can globally access any object
     *
     * @param testCase
     * @param request
     * @return
     */
    boolean hasPermissionsRead(TestCase testCase, HttpServletRequest request);

    /**
     * This method returns a boolean that define if the object can be updated by
     * user authenticated by request. in case object is null it return if the
     * user defined by request can globally update any object
     *
     * @param testCase
     * @param request
     * @return
     */
    boolean hasPermissionsUpdate(TestCase testCase, HttpServletRequest request);

    /**
     * This method returns a boolean that define if the object can be deleted by
     * user authenticated by request. in case object is null it return if the
     * user defined by request can globally delete any object
     *
     * @param testCase
     * @param request
     * @return
     */
    boolean hasPermissionsDelete(TestCase testCase, HttpServletRequest request);

    /**
     * This method returns a boolean that define if the object can be created by
     * user authenticated by request. in case object is null it return if the
     * user defined by request can globally create any object
     *
     * @param testCase
     * @param request
     * @return
     */
    public boolean hasPermissionsCreate(TestCase testCase, HttpServletRequest request);

    /**
     *
     * @param service
     * @return
     */
    public AnswerList<TestListDTO> findTestCasesThatUseService(String service);

    /**
     *
     * @param testCase
     * @throws org.cerberus.exception.CerberusException
     */
    public void importWithDependency(TestCase testCase) throws CerberusException;

}
