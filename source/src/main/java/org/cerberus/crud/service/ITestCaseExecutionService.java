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

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import org.cerberus.crud.entity.TestCase;

import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 * @author bcivel
 */
public interface ITestCaseExecutionService {

    long insertTCExecution(TestCaseExecution tCExecution) throws CerberusException;

    void updateTCExecution(TestCaseExecution tCExecution) throws CerberusException;

    AnswerItem readLastByCriteria(String application);

    TestCaseExecution findLastTCExecutionByCriteria(String test, String testCase, String environment, String country,
            String build, String revision) throws CerberusException;

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
    List<TestCaseExecution> findTCExecutionbyCriteria1(String dateLimitFrom, String test, String testCase, String application, String country, String environment, String controlStatus, String status) throws CerberusException;

    long registerRunID(TestCaseExecution tCExecution) throws CerberusException;

    /**
     *
     * @param id of the test case execution
     * @return the test case execution object
     * @throws CerberusException
     */
    TestCaseExecution findTCExecutionByKey(long id) throws CerberusException;

    /**
     * @param campaign the campaign name for the execution list
     * @param tag the tag name of the execution list
     * @return a list of testCaseExecution done on the campaign name and tag
     * with tag name.
     * @throws CerberusException when no executions can be found.
     */
    List<TestCaseExecution> findExecutionsByCampaignNameAndTag(String campaign, String tag) throws CerberusException;

    /**
     *
     * @param test
     * @param testCase
     * @return TestCaseExecution Object
     * @throws CerberusException
     */
    TestCaseExecution findLastTestCaseExecutionNotPE(String test, String testCase) throws CerberusException;

    /**
     *
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

    AnswerList findTagList(int tagnumber) throws CerberusException;

    /**
     *
     * @param tag
     * @param start
     * @param amount
     * @param sort
     * @param searchTerm
     * @param individualSearch
     * @return
     * @throws CerberusException
     */
    AnswerList readByTagByCriteria(String tag, int start, int amount, String sort, String searchTerm, Map<String, List<String>> individualSearch) throws CerberusException;

    /**
     *
     * @param start
     * @param amount
     * @param sort
     * @param searchTerm
     * @param individualSearch
     * @return
     * @throws CerberusException
     */
    AnswerList readByCriteria( int start, int amount, String sort, String searchTerm, Map<String, List<String>> individualSearch) throws CerberusException;

    /**
     *
     * @param tag
     * @return
     * @throws CerberusException
     */
    AnswerList readByTag(String tag) throws CerberusException;

    /**
     *
     * @param tag
     * @return
     */
    AnswerList readDistinctEnvCoutnryBrowserByTag(String tag);

    /**
     *
     * @param tag
     * @param env
     * @param country
     * @param browser
     * @param app
     * @return
     */
    AnswerList readDistinctColumnByTag(String tag, boolean env, boolean country, boolean browser, boolean app);

    List<TestCaseExecution> createAllTestCaseExecution(List<TestCase> testCaseList, List<String> envList, List<String> country);

    AnswerList readBySystemByVarious(String system, List<String> testList, List<String> applicationList, List<String> projectList, List<String> tcstatusList, List<String> groupList,
            List<String> tcactiveList, List<String> priorityList, List<String> targetsprintList, List<String> targetrevisionList, List<String> creatorList,
            List<String> implementerList, List<String> buildList, List<String> revisionList, List<String> environmentList, List<String> countryList, List<String> browserList, List<String> tcestatusList, String ip, String port, String tag, String browserversion,
            String comment, String bugid, String ticket);

    /**
     * Read TestCaseExecution knowing the Key
     *
     * @param executionId : ID of the execution
     * @return AnswerItem with returncode and testcaseexecution object as item.
     */
    AnswerItem readByKey(long executionId);

    /**
     * Read TestCaseExecution knowing the Key
     *
     * @param executionId : ID of the execution
     * @return AnswerItem with returncode and testcaseexecution object as item.
     */
    AnswerItem readByKeyWithDependency(long executionId);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    TestCaseExecution convert(AnswerItem answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<TestCaseExecution> convert(AnswerList answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

    public AnswerList readDistinctValuesByCriteria(String system, String test, String searchParameter, Map<String, List<String>> individualSearch, String columnName);

    /**
     *
     * @param tag
     * @return
     * @throws ParseException
     * @throws CerberusException
     */
    public List<TestCaseExecution> readLastExecutionAndExecutionInQueueByTag(String tag) throws ParseException, CerberusException;
   
}
