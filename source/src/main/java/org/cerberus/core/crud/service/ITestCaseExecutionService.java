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

import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;

/**
 * @author bcivel
 */
public interface ITestCaseExecutionService {

    /**
     * @param tCExecution
     * @return
     * @throws CerberusException
     */
    long insertTCExecution(TestCaseExecution tCExecution) throws CerberusException;

    /**
     * @param tCExecution
     * @throws CerberusException
     */
    void updateTCExecution(TestCaseExecution tCExecution) throws CerberusException;

    /**
     * @param application
     * @return
     */
    AnswerItem<TestCaseExecution> readLastByCriteria(String application);

    /**
     * @param test
     * @param testCase
     * @param environment
     * @param country
     * @param build
     * @param revision
     * @return
     * @throws CerberusException
     */
    TestCaseExecution findLastTCExecutionByCriteria(String test, String testCase, String environment, String country,
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
     * @return a list of the testcaseExecution done after dateLimitFrom
     * parameter and that match the other criteria.
     * @throws CerberusException when no Execution match the criteria.
     */
    List<TestCaseExecution> findTCExecutionByCriteria1(String dateLimitFrom, String test, String testCase, String application, String country, String environment, String controlStatus, String status) throws CerberusException;

    /**
     * @param system
     * @param from
     * @param countries
     * @param robotDecli
     * @param testcases
     * @param environments
     * @param to
     * @return
     * @throws CerberusException
     */
    List<TestCaseExecution> readByCriteria(List<String> system, List<String> countries, List<String> environments, List<String> robotDecli, List<TestCase> testcases, Date from, Date to) throws CerberusException;

    /**
     * @param system
     * @param tags
     * @param from
     * @param to
     * @return
     * @throws CerberusException
     */
    List<TestCaseExecution> readByCriteria(List<String> system, List<String> tags, Date from, Date to) throws CerberusException;

    /**
     *
     * @param system
     * @return
     * @throws CerberusException
     */
    public Integer getNbExecutions(List<String> system) throws CerberusException;

    /**
     * @param tCExecution
     * @return
     * @throws CerberusException
     */
    long registerRunID(TestCaseExecution tCExecution) throws CerberusException;

    /**
     * @param id of the test case execution
     * @return the test case execution object
     * @throws CerberusException
     */
    TestCaseExecution findTCExecutionByKey(long id) throws CerberusException;

    /**
     * @param test
     * @param testCase
     * @return TestCaseExecution Object
     * @throws CerberusException
     */
    TestCaseExecution findLastTestCaseExecutionNotPE(String test, String testCase) throws CerberusException;

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
     * @param withUUIDTag determine of we must retreive UUID tag or not
     * @return a list of String tag
     * @throws CerberusException when no tags can be found.
     */
    List<String> findDistinctTag(boolean withUUIDTag) throws CerberusException;

    /**
     * Set Tag to an Execution
     *
     * @param id : ID of the execution to tag
     * @param tag : Tag to apply to the execution
     * @throws CerberusException when exception occur during the update.
     */
    void setTagToExecution(long id, String tag) throws CerberusException;

    /**
     * Set Tag to an Execution
     *
     * @param id : ID of the execution to update
     * @param falseNegative : value of falseNegative.
     * @param usrModif
     * @throws CerberusException when exception occur during the update.
     */
    void updateFalseNegative(long id, boolean falseNegative, String usrModif) throws CerberusException;

    /**
     * Set Tag to an Execution
     *
     * @param id : ID of the execution to update
     * @param isLast : value of Last Execution from retry.
     * @param isFlaky : value of Flaky Flag.
     * @param usrModif
     * @throws CerberusException when exception occur during the update.
     */
    void updateLastAndFlaky(long id, boolean isLast, boolean isFlaky, String usrModif) throws CerberusException;

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
    AnswerList<TestCaseExecution> readByTagByCriteria(String tag, int start, int amount, String sort, String searchTerm, Map<String, List<String>> individualSearch) throws CerberusException;

    /**
     * @param start
     * @param amount
     * @param sort
     * @param searchTerm
     * @param individualSearch
     * @param individualLike
     * @param system
     * @return
     * @throws CerberusException
     */
    AnswerList<TestCaseExecution> readByCriteria(int start, int amount, String sort, String searchTerm, Map<String, List<String>> individualSearch, List<String> individualLike, List<String> system) throws CerberusException;

    /**
     * @param tag
     * @return
     * @throws CerberusException
     */
    AnswerList<TestCaseExecution> readByTag(String tag) throws CerberusException;

    /**
     * @param tag
     * @return
     * @throws CerberusException
     */
    int readNbByTag(String tag) throws CerberusException;

    /**
     * @param tag
     * @return
     */
    AnswerList<TestCaseExecution> readDistinctEnvCountryBrowserByTag(String tag);

    /**
     * @param testCaseList
     * @param envList
     * @param country
     * @return
     */
    List<TestCaseExecution> createAllTestCaseExecution(List<TestCase> testCaseList, List<String> envList, List<String> country);

    /**
     * Read TestCaseExecution knowing the Key
     *
     * @param executionId : ID of the execution
     * @return AnswerItem with returncode and testcaseexecution object as item.
     */
    AnswerItem<TestCaseExecution> readByKey(long executionId);

    /**
     *
     * @param test
     * @param testCase
     * @param country
     * @param environment
     * @param tag
     * @return
     */
    AnswerItem<TestCaseExecution> readLastByCriteria(String test, String testCase, String country, String environment, String tag);

    /**
     * Read TestCaseExecution knowing the Key
     *
     * @param executionId : ID of the execution
     * @return AnswerItem with returncode and testcaseexecution object as item.
     */
    AnswerItem readByKeyWithDependency(long executionId);

    /**
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    TestCaseExecution convert(AnswerItem<TestCaseExecution> answerItem) throws CerberusException;

    /**
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<TestCaseExecution> convert(AnswerList<TestCaseExecution> answerList) throws CerberusException;

    /**
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

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
     * @param tag
     * @return
     * @throws ParseException
     * @throws CerberusException
     */
    public List<TestCaseExecution> readLastExecutionAndExecutionInQueueByTag(String tag) throws ParseException, CerberusException;

    /**
     *
     * @param test
     * @param testCase
     * @param tag
     * @param numberOfExecution
     * @return
     * @throws CerberusException
     */
    public JSONArray getLastByCriteria(String test, String testCase, String tag, Integer numberOfExecution) throws CerberusException;
}
