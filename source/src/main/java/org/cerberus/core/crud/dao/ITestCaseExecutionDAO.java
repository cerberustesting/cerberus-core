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
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 02/01/2013
 * @since 2.0.0
 */
public interface ITestCaseExecutionDAO {

    /**
     * @param tCExecution TestCaseExecution Object to insert in
     * TestcaseExecution table
     * @return execution id (long)
     * @throws org.cerberus.core.exception.CerberusException
     */
    long insertTCExecution(TestCaseExecution tCExecution) throws CerberusException;

    /**
     * @param tCExecution
     * @throws CerberusException
     */
    void updateTCExecution(TestCaseExecution tCExecution) throws CerberusException;

    /**
     * @param test
     * @param testcase
     * @param country
     * @return
     */
    List<String> getIDListOfLastExecutions(String test, String testcase, String country);

    /**
     * Gets the last execution from the database following the defined criteria.
     *
     * @param application
     * @return
     */
    AnswerItem<TestCaseExecution> readLastByCriteria(String application);

    /**
     * @param test Test Criteria
     * @param testcase TestCase Criteria
     * @param environment Environment Criteria
     * @param country Country Criteria
     * @param build
     * @param revision
     * @return TestCaseExecution Object created only with attributes from
     * database
     * @throws org.cerberus.core.exception.CerberusException
     */
    TestCaseExecution findLastTCExecutionByCriteria(String test, String testcase, String environment, String country,
            String build, String revision) throws CerberusException;

    /**
     * @param test
     * @param testCase
     * @param environment
     * @param country
     * @param build
     * @param revision
     * @param browser
     * @param browserVersion
     * @param ip
     * @param port
     * @param tag
     * @return
     */
    TestCaseExecution findLastTCExecutionByCriteria(String test, String testCase, String environment, String country,
            String build, String revision, String browser, String browserVersion,
            String ip, String port, String tag);

    /**
     * @param dateLimitFrom The limit start date of the executions from which
     * the selection is done. Mandatory parameter.
     * @param test filter on the test
     * @param testCase filter on the testCase
     * @param application filter on the application.
     * @param country filter on the country
     * @param environment filter on the environment
     * @param controlStatus filter on the control status (RC of the execution)
     * @param status filter on the status (Status of the testCase when execution
     * was made)
     * @return a list of testCaseExecution done after the dateLimitFrom
     * parameter and following the other criteria.
     * @throws CerberusException when no executions can be found.
     */
    List<TestCaseExecution> findExecutionByCriteria1(String dateLimitFrom, String test, String testCase,
            String application, String country, String environment, String controlStatus, String status);

    /**
     * @param id of the test case execution
     * @return the test case execution object
     * @throws CerberusException
     */
    TestCaseExecution findTCExecutionByKey(long id);

    /**
     * @param withUUIDTag determine of we must retreive UUID tag or not
     * @return a list of String tag
     * @throws CerberusException when no tags can be found.
     */
    List<String> findDistinctTag(boolean withUUIDTag);

    /**
     * @param test
     * @param testCase
     * @return
     * @throws CerberusException
     */
    TestCaseExecution findLastTestCaseExecutionNotPE(String test, String testCase);

    /**
     * @param test
     * @param testCase
     * @param environment
     * @param country
     * @param build
     * @param revision
     * @param browser
     * @param browserVersion
     * @param ip
     * @param port
     * @param tag
     * @return
     */
    public TestCaseExecution findLastTCExecutionInGroup(String test, String testCase, String environment, String country,
            String build, String revision, String browser, String browserVersion,
            String ip, String port, String tag);

    /**
     * Set Tag to an execution
     *
     * @param id : ID of the execution
     * @param tag : Tag to set to the execution
     * @throws org.cerberus.core.exception.CerberusException
     */
    public void setTagToExecution(long id, String tag) throws CerberusException;

    /**
     * Set Tag to an execution
     *
     * @param id : ID of the execution
     * @param falseNegative : falseNegative value
     * @param usrModif
     * @throws org.cerberus.core.exception.CerberusException
     */
    public void updateFalseNegative(long id, boolean falseNegative, String usrModif) throws CerberusException;

    /**
     * Set Tag to an execution
     *
     * @param id : ID of the execution
     * @param last
     * @param flaky
     * @param usrModif
     * @throws org.cerberus.core.exception.CerberusException
     */
    public void updateLastAndFlaky(long id, boolean last, boolean flaky, String usrModif) throws CerberusException;

    /**
     * @param tag
     * @param start
     * @param amount
     * @param sort
     * @param searchTerm
     * @param individualSearch
     * @return
     * @throws CerberusException
     */
    public List<TestCaseExecution> readByTagByCriteria(String tag, int start, int amount, String sort, String searchTerm, Map<String, List<String>> individualSearch) throws CerberusException;

    /**
     * @param system
     * @param countries
     * @param environments
     * @param from
     * @param robotDecli
     * @param testcases
     * @param to
     * @return
     */
    public AnswerList<TestCaseExecution> readByCriteria(List<String> system, List<String> countries, List<String> environments, List<String> robotDecli, List<TestCase> testcases, Date from, Date to);

    /**
     * @param system
     * @param tags
     * @param from
     * @param to
     * @return
     */
    public AnswerList<TestCaseExecution> readByCriteria(List<String> system, List<String> tags, Date from, Date to);

    /**
     * @param systems
     * @return
     */
    public Integer getNbExecutions(List<String> systems);

    /**
     * Read TestCaseExecution By Tag
     *
     * @param tag Tag used to filter execution
     * @return AnswerList that contains a list of TestCaseExecution object
     * enriched with TestCase and Application objects
     * @throws CerberusException
     */
    public AnswerList<TestCaseExecution> readByTag(String tag);

    /**
     * Read TestCaseExecution By Tag
     *
     * @param tag Tag used to filter execution
     * @return AnswerList that contains a list of TestCaseExecution object
     * enriched with TestCase and Application objects
     * @throws CerberusException
     */
    public Integer readNbByTag(String tag) throws CerberusException;

    /**
     * @param start
     * @param amount
     * @param sort
     * @param searchTerm
     * @param individualSearch
     * @param individualLike
     * @param systems
     * @return
     * @throws CerberusException
     */
    AnswerList<TestCaseExecution> readByCriteria(int start, int amount, String sort, String searchTerm, Map<String, List<String>> individualSearch, List<String> individualLike, List<String> systems) throws CerberusException;

    /**
     * @param tag
     * @return
     */
    public AnswerList<TestCaseExecution> readDistinctEnvCountryBrowserByTag(String tag);

    /**
     * @param executionId
     * @return
     */
    public AnswerItem<TestCaseExecution> readByKey(long executionId);

    /**
     * @param test
     * @param testCase
     * @param country
     * @param environment
     * @param tag
     * @return
     */
    public AnswerItem<TestCaseExecution> readLastByCriteria(String test, String testCase, String country, String environment, String tag);

    /**
     * Uses data of ResultSet to create object {@link TestCaseExecution}
     *
     * @param resultSet ResultSet relative to select from table
     * TestCaseExecution
     * @return object {@link TestCaseExecution}
     * @throws SQLException when trying to get value from
     * {@link java.sql.ResultSet#getString(String)}
     * @see org.cerberus.core.crud.factory.impl.FactoryTestCaseExecution
     */
    public TestCaseExecution loadFromResultSet(ResultSet resultSet) throws SQLException;

    /**
     * Get the distinct value of the specified colum
     *
     * @param system
     * @param test
     * @param searchParameter
     * @param individualSearch
     * @param columnName Name of the column
     * @return object {@link TestCaseExecution}
     */
    public AnswerList<String> readDistinctValuesByCriteria(List<String> system, String test, String searchParameter, Map<String, List<String>> individualSearch, String columnName);

}
