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

    TestCase findTestCaseByKeyWithDependency(String test, String testCase) throws CerberusException;

    List<TestCase> findTestCaseByTest(String test);

    List<TestCase> findTestCaseByTestSystem(String test, String system);

    List<TestCase> findTestCaseByApplication(String application);

    List<TestCase> findTestCaseActiveByCriteria(String test, String application, String country);

    boolean updateTestCaseInformation(TestCase testCase);

    boolean updateTestCaseInformationCountries(TestCase tc);

    boolean createTestCase(TestCase testCase) throws CerberusException;

    List<TestCase> findTestCaseByAllCriteria(TestCase tCase, String text, String system);

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
    AnswerItem<List<TestCase>> findTestCaseByCampaignNameAndCountries(String campaign, String[] countries);

    public void updateTestCase(TestCase tc) throws CerberusException;

    /**
     * @param test
     * @return
     * @since 1.0.2
     */
    String getMaxNumberTestCase(String test);

    List<TestCase> findUseTestCaseList(String test, String testCase) throws CerberusException;

    List<TestCase> findByCriteria(String[] test, String[] project, String[] app, String[] active, String[] priority, String[] status, String[] group, String[] targetBuild, String[] targetRev, String[] creator, String[] implementer, String[] function, String[] campaign, String[] battery);

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
    AnswerList findTestCasesThatUseTestDataLib(int testDataLibId, String name, String country);

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
    public AnswerList readByTestByCriteria(String system, String test, int start, int amount, String sortInformation, String searchTerm, Map<String, List<String>> individualSearch);

    /**
     * @param test
     * @param testCase
     * @return
     */
    public AnswerItem readByKey(String test, String testCase);

    /**
     * @param test
     * @param testCase
     * @return
     */
    public AnswerItem readByKeyWithDependency(String test, String testCase);

    /**
     *
     * @param test
     * @param idProject
     * @param app
     * @param creator
     * @param implementer
     * @param system
     * @param campaign
     * @param labelid
     * @param priority
     * @param group
     * @param status
     * @param length
     * @return
     */
    public AnswerList<List<TestCase>> readByVarious(String[] test, String[] idProject, String[] app, String[] creator, String[] implementer, String[] system,
            String[] campaign, String[] labelid, String[] priority, String[] group, String[] status, int length);

    public AnswerList<List<String>> readDistinctValuesByCriteria(String system, String test, String searchParameter, Map<String, List<String>> individualSearch, String columnName);

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
    TestCase convert(AnswerItem answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<TestCase> convert(AnswerList answerList) throws CerberusException;

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
    public AnswerList findTestCasesThatUseService(String service);

}
