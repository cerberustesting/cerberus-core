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
package org.cerberus.core.crud.dao;

import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.dto.TestListDTO;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 28/Dez/2012
 * @since 2.0.0
 */
public interface ITestCaseCountryPropertiesDAO {

    /**
     * @param test
     * @param testcase
     * @return
     * @throws CerberusException
     */
    List<TestCaseCountryProperties> findListOfPropertyPerTestTestCase(String test, String testcase) throws CerberusException;

    /**
     * @param testcases
     * @return
     * @throws CerberusException
     */
    List<TestCaseCountryProperties> findListOfPropertyPerTestTestCaseList(List<TestCase> testcases) throws CerberusException;

    /**
     * @param test
     * @param testcase
     * @param oneproperty
     * @return
     */
    List<TestCaseCountryProperties> findListOfPropertyPerTestTestCaseProperty(String test, String testcase, String oneproperty);

    /**
     * @param test
     * @param testcase
     * @param country
     * @return
     */
    List<TestCaseCountryProperties> findListOfPropertyPerTestTestCaseCountry(String test, String testcase, String country);

    /**
     * @param testCaseCountryProperties
     * @return
     */
    List<String> findCountryByProperty(TestCaseCountryProperties testCaseCountryProperties);

    /**
     * @param test
     * @param testcase
     * @param property
     * @return
     */
    List<String> findCountryByPropertyNameAndTestCase(String test, String testcase, String property);

    /**
     * @param test
     * @param testcase
     * @param country
     * @param property
     * @return
     * @throws CerberusException
     */
    TestCaseCountryProperties findTestCaseCountryPropertiesByKey(String test, String testcase, String country, String property) throws CerberusException;

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
     * @param tccp
     * @throws CerberusException
     */
    public void deleteTestCaseCountryProperties(TestCaseCountryProperties tccp) throws CerberusException;

    /**
     * @param testDataLib
     * @param name
     * @param country
     * @param propertyType
     * @return
     */
    AnswerList<TestListDTO> findTestCaseCountryPropertiesByValue1(int testDataLib, String name, String country, String propertyType);

    /**
     * @param listOfPropertiesToInsert
     * @return
     */
    Answer createTestCaseCountryPropertiesBatch(List<TestCaseCountryProperties> listOfPropertiesToInsert);

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
     * Update @field on database replacing %object.oldObject% to
     * %object.newObject% on all lines that belong to @application
     *
     * @param field
     * @param application
     * @param oldObject
     * @param newObject
     * @throws CerberusException
     */
    void updateApplicationObject(String field, String application, String oldObject, String newObject) throws CerberusException;

    /**
     * @param oldName
     * @param newName
     * @return
     */
    Answer bulkRenameProperties(String oldName, String newName);

    /**
     * @param resultSet
     * @return
     * @throws SQLException
     */
    TestCaseCountryProperties loadFromResultSet(ResultSet resultSet) throws SQLException;

}
