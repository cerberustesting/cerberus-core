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

import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.dto.TestListDTO;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;

import java.util.HashMap;
import java.util.List;

/**
 * @author bcivel
 */
public interface ITestCaseCountryPropertiesService {

    /**
     * @param test
     * @param testCase
     * @param country
     * @return
     */
    List<TestCaseCountryProperties> findListOfPropertyPerTestTestCaseCountry(String test, String testCase, String country);

    /**
     * @param test
     * @param testcase
     * @return
     * @throws CerberusException
     */
    List<TestCaseCountryProperties> findListOfPropertyPerTestTestCase(String test, String testcase) throws CerberusException;

    /**
     * @param test
     * @param testcase
     * @param oneproperty
     * @return
     */
    List<TestCaseCountryProperties> findListOfPropertyPerTestTestCaseProperty(String test, String testcase, String oneproperty);

    /**
     * @param testCaseList
     * @param countryInvariants
     * @return
     * @throws CerberusException
     */
    public List<TestCaseCountryProperties> findDistinctPropertiesOfTestCaseFromTestcaseList(List<TestCase> testCaseList, HashMap<String, Invariant> countryInvariants) throws CerberusException;

    /**
     * @param testCase
     * @param countryInvariants
     * @return
     * @throws CerberusException
     */
    public List<TestCaseCountryProperties> findDistinctInheritedPropertiesOfTestCase(TestCase testCase, HashMap<String, Invariant> countryInvariants) throws CerberusException;

    /**
     * @param testCaseCountryProperties
     * @return
     */
    List<String> findCountryByProperty(TestCaseCountryProperties testCaseCountryProperties);

    /**
     * @param test
     * @param testCase
     * @param country
     * @param property
     * @return
     * @throws CerberusException
     */
    TestCaseCountryProperties findTestCaseCountryPropertiesByKey(String test, String testCase, String country, String property) throws CerberusException;

    /**
     * @param testCaseCountryProperties
     * @throws CerberusException
     */
    void insertTestCaseCountryProperties(TestCaseCountryProperties testCaseCountryProperties) throws CerberusException;

    /**
     * @param testCaseCountryProperties
     * @throws CerberusException
     */
    void updateTestCaseCountryProperties(TestCaseCountryProperties testCaseCountryProperties) throws CerberusException;

    /**
     *
     * @param application
     * @param oldObject
     * @param newObject
     */
    public void updateApplicationObject(String application, String oldObject, String newObject);

    /**
     * @param testCaseCountryPropertiesList
     * @return
     */
    boolean insertListTestCaseCountryProperties(List<TestCaseCountryProperties> testCaseCountryPropertiesList);

    /**
     * @param test
     * @param testcase
     * @param property
     * @return
     */
    List<String> findCountryByPropertyNameAndTestCase(String test, String testcase, String property);

    /**
     * @param tccpToDelete
     * @throws CerberusException
     */
    void deleteListTestCaseCountryProperties(List<TestCaseCountryProperties> tccpToDelete) throws CerberusException;

    /**
     * @param tccp
     * @throws CerberusException
     */
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
     * @param name          testdatalib name
     * @param country       country where
     * @param propertyType
     * @return an answer with the test cases and a message indicating the status
     * of the operation
     */
    AnswerList<TestListDTO> findTestCaseCountryPropertiesByValue1(int testDataLibID, String name, String country, String propertyType);

    /**
     * @param listOfPropertiesToInsert
     * @return
     */
    Answer createListTestCaseCountryPropertiesBatch(List<TestCaseCountryProperties> listOfPropertiesToInsert);

    /**
     * @param object
     * @return
     */
    Answer create(TestCaseCountryProperties object);

    /**
     * @param object
     * @return
     */
    Answer delete(TestCaseCountryProperties object);

    /**
     * @param object
     * @return
     */
    Answer update(TestCaseCountryProperties object);

    /**
     * @param objectList
     * @return
     */
    Answer createList(List<TestCaseCountryProperties> objectList);

    /**
     * @param objectList
     * @return
     */
    Answer deleteList(List<TestCaseCountryProperties> objectList);

    /**
     * @param test
     * @param testCase
     * @param newList
     * @return
     * @throws CerberusException
     */
    Answer compareListAndUpdateInsertDeleteElements(String test, String testCase, List<TestCaseCountryProperties> newList) throws CerberusException;

    /**
     * @param objectList
     * @param targetTest
     * @param targetTestCase
     * @return
     */
    Answer duplicateList(List<TestCaseCountryProperties> objectList, String targetTest, String targetTestCase);

    /**
     * Used when you receive a list of properties where the properties have been grouped by value and countries
     *
     * @param testCaseCountryProperties
     * @return a flatten list of TestCaseCountryProperties with its own country
     */
    List<TestCaseCountryProperties> getFlatListOfTestCaseCountryPropertiesFromAggregate(List<TestCaseCountryProperties> testCaseCountryProperties);
}
