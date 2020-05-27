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
package org.cerberus.crud.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.factory.impl.FactoryTestCase;
import org.cerberus.dto.TestListDTO;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 18/Dez/2012
 * @since 0.9.0
 */
public interface ITestCaseDAO {

    List<TestCase> findTestCaseByTest(String test);

    TestCase findTestCaseByKey(String test, String testCase) throws CerberusException;

    boolean updateTestCaseInformation(TestCase testCase);

    boolean updateTestCaseInformationCountries(TestCase tc);

    boolean createTestCase(TestCase testCase);

    List<TestCase> findTestCaseByApplication(String application);

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
     *
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
     * @param maxReturn nd max of records to return. (Prevent from returning too
     * large list)
     * @return the list of TCase used in the campaign
     * @since 1.0.2
     */
    AnswerList<TestCase> findTestCaseByCampaignNameAndCountries(String campaign, String[] countries, List<Integer> labelIdList, String[] status, String[] system, String[] application, String[] priority, String[] type, Integer maxReturn);

    public void updateTestCase(TestCase tc) throws CerberusException;

    String getMaxNumberTestCase(String test);

    public List<TestCase> findTestCaseByTestSystem(String test, String system);

    /**
     *
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

    public String findSystemOfTestCase(String test, String testcase) throws CerberusException;

    AnswerList readTestCaseByStepsInLibrary(String test);

    /**
     *
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
     * @param test
     * @param testCase
     * @return
     */
    public AnswerItem<TestCase> readByKey(String test, String testCase);

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
     * @param testCase target object value.
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
     *
     * @param service
     * @return
     */
    public AnswerList<TestListDTO> findTestCaseByService(String service);

    /**
     *
     * @param service
     * @return
     */
    public AnswerList<TestListDTO> findTestCaseByServiceByDataLib(String service);

}
