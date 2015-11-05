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
import org.cerberus.crud.entity.TCase;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author bcivel
 * @author tbernardes
 * @author FNogueira
 */
public interface ITestCaseService {

    /**
     *
     * @param test
     * @param testCase
     * @return
     * @throws org.cerberus.exception.CerberusException
     */
    TCase findTestCaseByKey(String test, String testCase) throws CerberusException;

    TCase findTestCaseByKeyWithDependency(String test, String testCase) throws CerberusException;

    List<TCase> findTestCaseByTest(String test);

    List<TCase> findTestCaseByTestSystem(String test, String system);

    List<TCase> findTestCaseActiveByCriteria(String test, String application, String country);

    boolean updateTestCaseInformation(TestCase testCase);

    boolean updateTestCaseInformationCountries(TestCase tc);

    boolean createTestCase(TCase testCase) throws CerberusException;

    /**
     * @since 0.9.1
     */
    List<TCase> findTestCaseByAllCriteria(TCase tCase, String text, String system);

    /**
     * @since 0.9.1
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
    List<TCase> findTestCaseActiveAutomatedBySystem(String test, String system);

    /**
     *
     * @param testCase
     * @return true if delete is OK
     */
    boolean deleteTestCase(TCase testCase);

    /**
     *
     * @param name Key of the table
     * @param columnName Name of the column
     * @param value New value of the columnName
     */
    void updateTestCaseField(TCase tc, String columnName, String value);

    /**
     *
     * @param tCase
     * @param system
     * @return
     * @since 1.0.2
     */
    List<TCase> findTestCaseByGroupInCriteria(TCase tCase, String system);

    /**
     *
     * @param campaign the campaign name
     * @return the list of TCase used in the campaign
     * @since 1.0.2
     */
    List<TCase> findTestCaseByCampaignName(String campaign);

    /**
     *
     * @param campaign the campaign name
     * @param countries arrays of country
     * @return the list of TCase used in the campaign and activated for the
     * countries
     * @since 1.0.2
     */
    List<TCase> findTestCaseByCampaignNameAndCountries(String campaign, String[] countries);

    public void updateTestCase(TCase tc) throws CerberusException;

    /**
     *
     * @param test
     * @return
     * @since 1.0.2
     */
    String getMaxNumberTestCase(String test);

    List<TCase> findUseTestCaseList(String test, String testCase) throws CerberusException;

    List<TCase> findByCriteria(String[] test, String[] project, String[] app, String[] active, String[] priority, String[] status, String[] group, String[] targetBuild, String[] targetRev, String[] creator, String[] implementer, String[] function, String[] campaign, String[] battery);

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

    public AnswerList readByTestByCriteria(String test, int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    public AnswerItem readByKey(String test, String testCase);

    public AnswerList readByVariousCriteria(String[] test, String[] idProject, String[] app, String[] creator, String[] implementer, String[] system,
                                            String[] testBattery, String[] campaign, String[] priority, String[] group, String[] status);

    public Answer update(TCase testCase);

    public Answer create(TCase testCase);

    public Answer delete(TCase testCase);
}
