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

import java.util.HashMap;
import java.util.List;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.dto.TestListDTO;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;

/**
 * @author bcivel
 */
public interface ITestCaseCountryPropertiesService {

    List<TestCaseCountryProperties> findListOfPropertyPerTestTestCaseCountry(String test, String testCase, String country);

    List<TestCaseCountryProperties> findListOfPropertyPerTestTestCase(String test, String testcase) throws CerberusException;

    List<TestCaseCountryProperties> findOnePropertyPerTestTestCase(String test, String testcase, String oneproperty);

    public List<TestCaseCountryProperties> findDistinctPropertiesOfTestCase(String test, String testcase) throws CerberusException;

    public List<TestCaseCountryProperties> findDistinctPropertiesOfTestCase(String test, String testcase, HashMap<String, Invariant> countryInvariants) throws CerberusException;

    public List<TestCaseCountryProperties> findDistinctInheritedPropertiesOfTestCase(TestCase testCase, HashMap<String, Invariant> countryInvariants) throws CerberusException;

    List<String> findCountryByProperty(TestCaseCountryProperties testCaseCountryProperties);

    TestCaseCountryProperties findTestCaseCountryPropertiesByKey(String test, String testCase, String country, String property) throws CerberusException;

    void insertTestCaseCountryProperties(TestCaseCountryProperties testCaseCountryProperties) throws CerberusException;

    void updateTestCaseCountryProperties(TestCaseCountryProperties testCaseCountryProperties) throws CerberusException;

    boolean insertListTestCaseCountryProperties(List<TestCaseCountryProperties> testCaseCountryPropertiesList);

    List<String> findCountryByPropertyNameAndTestCase(String test, String testcase, String property);

    void deleteListTestCaseCountryProperties(List<TestCaseCountryProperties> tccpToDelete) throws CerberusException;

    void deleteTestCaseCountryProperties(TestCaseCountryProperties tccp) throws CerberusException;

    /**
     * Find all the properties of a testcase including those of the preTests,
     * postTests and the use steps
     *
     * @param test
     * @param testcase
     * @param country
     * @param system
     * @param build
     * @param revision
     * @return List of unique testcasecountryproperties (from tc first, use step
     * if not found in tc and then, in pretest if not found)
     * @throws CerberusException
     */
    public List<TestCaseCountryProperties> findAllWithDependencies(String test, String testcase, String country, String system, String build, String revision) throws CerberusException;

    /**
     * Method that check if a determined property is used in the value1 of a
     * property
     *
     * @param testDataLibID testdatalib unique identifier
     * @param name testdatalib name
     * @param country country where
     * @param propertyType
     * @return an answer with the test cases and a message indicating the status
     * of the operation
     */
    AnswerList<TestListDTO> findTestCaseCountryPropertiesByValue1(int testDataLibID, String name, String country, String propertyType);

    /**
     *
     * @param listOfPropertiesToInsert
     * @return
     */
    Answer createListTestCaseCountryPropertiesBatch(List<TestCaseCountryProperties> listOfPropertiesToInsert);

    /**
     *
     * @param object
     * @return
     */
    Answer create(TestCaseCountryProperties object);

    /**
     *
     * @param object
     * @return
     */
    Answer delete(TestCaseCountryProperties object);

    /**
     *
     * @param object
     * @return
     */
    Answer update(TestCaseCountryProperties object);

    /**
     *
     * @param objectList
     * @return
     */
    Answer createList(List<TestCaseCountryProperties> objectList);

    /**
     *
     * @param objectList
     * @return
     */
    Answer deleteList(List<TestCaseCountryProperties> objectList);

    /**
     *
     * @param test
     * @param testCase
     * @param newList
     * @return
     * @throws CerberusException
     */
    Answer compareListAndUpdateInsertDeleteElements(String test, String testCase, List<TestCaseCountryProperties> newList) throws CerberusException;

    /**
     *
     * @param objectList
     * @param targetTest
     * @param targetTestCase
     * @return
     */
    Answer duplicateList(List<TestCaseCountryProperties> objectList, String targetTest, String targetTestCase);
}
