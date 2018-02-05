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

    List<TestCase> findTestCaseByCriteria(String test, String application, String country, String active);

    /**
     * @param testCase
     * @param text
     * @param system
     * @return
     * @since 0.9.1
     */
    List<TestCase> findTestCaseByCriteria(TestCase testCase, String text, String system);

    List<String> findUniqueDataOfColumn(String column);

    /**
     * @param testCase
     * @return true if delete is OK
     */
    boolean deleteTestCase(TestCase testCase);

    /**
     * @param campaign the campaign name
     * @param countries arrays of country
     * @param withLabelOrBattery
     * @param status status of test case
     * @param system of test case
     * @param application of test case
     * @param priority of test case
     * @param maxReturn
     * @return the list of TCase used in the campaign
     * @since 1.0.2
     */
    AnswerItem<List<TestCase>> findTestCaseByCampaignNameAndCountries(String campaign, String[] countries, boolean withLabelOrBattery, String[] status, String[] system, String[] application, String[] priority, Integer maxReturn);

    public void updateTestCase(TestCase tc) throws CerberusException;

    String getMaxNumberTestCase(String test);

    public List<TestCase> findTestCaseByTestSystem(String test, String system);

    List<TestCase> findTestCaseByCriteria(String[] test, String[] project, String[] app, String[] active, String[] priority, String[] status, String[] group, String[] targetBuild, String[] targetRev, String[] creator, String[] implementer, String[] function, String[] campaign, String[] battery);

    public String findSystemOfTestCase(String test, String testcase) throws CerberusException;

    AnswerList readTestCaseByStepsInLibrary(String test);

    public AnswerList readByTestByCriteria(String system, String test, int start, int amount, String sortInformation, String searchTerm, Map<String, List<String>> individualSearch);

    /**
     *
     * @param test
     * @param idProject
     * @param app
     * @param creator
     * @param implementer
     * @param system
     * @param testBattery
     * @param campaign
     * @param labelid
     * @param priority
     * @param group
     * @param status
     * @param length
     * @return
     */
    public AnswerList<List<TestCase>> readByVarious(String[] test, String[] idProject, String[] app, String[] creator, String[] implementer, String[] system,
            String[] testBattery, String[] campaign, String[] labelid, String[] priority, String[] group, String[] status, int length);

    public AnswerItem readByKey(String test, String testCase);

    public AnswerList<List<String>> readDistinctValuesByCriteria(String system, String test, String searchParameter, Map<String, List<String>> individualSearch, String columnName);

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
    public AnswerList findTestCaseByService(String service);
}
