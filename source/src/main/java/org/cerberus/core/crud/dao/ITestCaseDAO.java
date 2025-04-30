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
import org.cerberus.core.crud.factory.impl.FactoryTestCase;
import org.cerberus.core.dto.TestListDTO;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 18/Dez/2012
 * @since 0.9.0
 */
public interface ITestCaseDAO {

    /**
     * @param test
     * @return
     */
    List<TestCase> findTestCaseByTest(String test);

    /**
     * @param systems
     * @return
     */
    Integer getnbtc(List<String> systems);

    /**
     * @param test
     * @param testCase
     * @return
     * @throws CerberusException
     */
    TestCase findTestCaseByKey(String test, String testCase) throws CerberusException;

    /**
     * @param testCase
     * @return
     */
    boolean updateTestCaseInformation(TestCase testCase);

    /**
     * @param tc
     * @return
     */
    boolean updateTestCaseInformationCountries(TestCase tc);

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
     *
     * @param test
     * @param testcase
     * @param lastExecuted
     * @throws CerberusException
     */
    void updateLastExecuted(String test, String testcase, Timestamp lastExecuted) throws CerberusException;

    /**
     *
     * @param test
     * @param testcase
     * @param newBugList
     * @throws CerberusException
     */
    void updateBugList(String test, String testcase, String newBugList) throws CerberusException;

    /**
     * @param testCase
     * @return
     */
    boolean createTestCase(TestCase testCase);

    /**
     * @param application
     * @return
     */
    List<TestCase> findTestCaseByApplication(String application);

    /**
     * @param test
     * @param application
     * @param country
     * @param isActive
     * @return
     */
    List<TestCase> findTestCaseByCriteria(String test, String application, String country, String isActive);

    /**
     * @param testCase
     * @param text
     * @param system
     * @return
     * @since 0.9.1
     */
    List<TestCase> findTestCaseByCriteria(TestCase testCase, String text, String system);

    /**
     * @param column
     * @return
     */
    List<String> findUniqueDataOfColumn(String column);

    /**
     * @param testCase
     * @return true if delete is OK
     */
    boolean deleteTestCase(TestCase testCase);

    /**
     * @param campaign the campaign name
     * @param countries arrays of country to filter
     * @param labelIdList
     * @param status arrays of status to filter
     * @param system arrays of system to filter
     * @param application arrays of application to filter
     * @param priority arrays of priority to filter
     * @param type arrays of type to filter
     * @param testFolder
     * @param maxReturn nd max of records to return. (Prevent from returning too
     * large list)
     * @return the list of TCase used in the campaign
     * @since 1.0.2
     */
    AnswerList<TestCase> findTestCaseByCampaignNameAndCountries(String campaign, String[] countries, List<Integer> labelIdList, String[] status, String[] system, String[] application, String[] priority, String[] type, String[] testFolder, Integer maxReturn);

    /**
     * @param tc
     * @throws CerberusException
     */
    public void updateTestCase(TestCase tc) throws CerberusException;

    /**
     * @param test
     * @return
     */
    String getMaxTestcaseIdByTestFolder(String test);

    /**
     * @param test
     * @param system
     * @return
     */
    public List<TestCase> findTestCaseByTestSystem(String test, String system);

    /**
     * @param test
     * @param app
     * @param isActive
     * @param priority
     * @param status
     * @param type
     * @param targetMajor
     * @param targetMinor
     * @param creator
     * @param implementer
     * @param campaign
     * @return
     */
    List<TestCase> findTestCaseByCriteria(String[] test, String[] app, String[] isActive, String[] priority, String[] status, String[] type, String[] targetMajor, String[] targetMinor, String[] creator, String[] implementer, String[] campaign);

    /**
     * @param test
     * @param testcase
     * @return
     * @throws CerberusException
     */
    public String findSystemOfTestCase(String test, String testcase) throws CerberusException;

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
     * @param test
     * @param testCase
     * @return
     */
    public AnswerItem<TestCase> readByKey(String test, String testCase);

    /**
     * @param system
     * @param test
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return
     */
    public AnswerList<String> readDistinctValuesByCriteria(List<String> system, String test, String searchParameter, Map<String, List<String>> individualSearch, String columnName);

    /**
     * @param systems
     * @param to
     * @return
     */
    public AnswerList<TestCase> readStatsBySystem(List<String> systems, Date to);

    /**
     * @param keyTest
     * @param keyTestCase
     * @param testCase target object value.
     * @return
     */
    public Answer update(String keyTest, String keyTestCase, TestCase testCase);

    /**
     * @param testCase
     * @return
     */
    public Answer create(TestCase testCase);

    /**
     * @param testCase
     * @return
     */
    public Answer delete(TestCase testCase);

    /**
     * Uses data of ResultSet to create object {@link TestCase}
     *
     * @param resultSet ResultSet relative to select from table TestCase
     * @return object {@link TestCase}
     * @throws SQLException when trying to get value from
     * {@link java.sql.ResultSet#getString(String)}
     * @see FactoryTestCase
     */
    public TestCase loadFromResultSet(ResultSet resultSet) throws SQLException;

    /**
     * @param service
     * @return
     */
    public AnswerList<TestListDTO> findTestCaseByService(String service);

    /**
     * @param service
     * @return
     */
    public AnswerList<TestListDTO> findTestCaseByServiceByDataLib(String service);

}
