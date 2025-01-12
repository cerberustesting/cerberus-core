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

import java.util.List;
import java.util.Map;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionQueue;
import org.cerberus.core.crud.entity.TestCaseExecutionQueueDep;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

/**
 *
 * @author vertigo
 */
public interface ITestCaseExecutionQueueDepService {

    /**
     * Insert execution dependencies to queueid from test case definition on the
     * env, country and tag
     *
     *
     * @param queueId
     * @param env
     * @param country
     * @param tag
     * @param test
     * @param testcase
     * @param queueToInsert Optionally that hashmap include all Execution of the
     * context. This is used in order to avoid inserting a dependency on a
     * testcase that is not included inside the campaign.
     * @param withDep
     *
     * @return
     */
    AnswerItem<Integer> insertFromTestCaseDep(long queueId, String env, String country, String tag, String test, String testcase, Map<String, TestCaseExecutionQueue> queueToInsert, boolean withDep);

    /**
     * Insert execution dependencies to queueid from existing ExeQueueId
     *
     * @param queueId
     * @param fromExeQueueId
     * @return
     */
    AnswerItem<Integer> insertFromExeQueueIdDep(long queueId, long fromExeQueueId);

    /**
     *
     * Set comment and exeId and RELEASE the dep lines linked to criterias.
     *
     * @param env
     * @param Country
     * @param tag
     * @param test
     * @param testCase
     * @param comment
     * @param exeId
     * @param queueId
     * @return
     */
    AnswerItem<Integer> updateStatusToRelease(String env, String Country, String tag, String test, String testCase, String comment, long exeId, long queueId);

    /**
     *
     * @param id
     * @param comment
     * @param exeId
     * @param queueId
     * @return
     */
    AnswerItem<Integer> updateStatusToRelease(long id, String comment, long exeId, long queueId);

    /**
     *
     * @param id
     * @return
     */
    AnswerItem<Integer> updateStatusToRelease(long id);

    /**
     *
     * @param exeId
     * @return
     */
    AnswerList<Long> readExeQueueIdByExeId(long exeId);

    /**
     *
     * @param exeId
     * @return
     */
    AnswerList<Long> readExeQueueIdByQueueId(long exeId);

    /**
     *
     * @param exeQueueId
     * @return
     */
    AnswerList<TestCaseExecutionQueueDep> readByExeQueueId(long exeQueueId);

    /**
     *
     * @param exeQueueId
     * @return
     */
    AnswerItem<Integer> readNbWaitingByExeQueueId(long exeQueueId);

    /**
     * load test case dependency Queue on object each TestCaseExecution
     *
     * @param testCaseExecutions
     * @throws org.cerberus.core.exception.CerberusException
     */
    void loadDependenciesOnTestCaseExecution(List<TestCaseExecution> testCaseExecutions) throws CerberusException;

    /**
     *
     * @param exeQueueId
     * @return
     */
    AnswerItem<Integer> readNbReleasedWithNOKByExeQueueId(long exeQueueId);

    /**
     *
     * @return
     */
    AnswerList<TestCaseExecutionQueueDep> getWaitingDepReadytoRelease();

    /**
     *
     * @param env
     * @param Country
     * @param tag
     * @param test
     * @param testCase
     * @return
     */
    AnswerItem<Integer> insertNewTimingDep(String env, String Country, String tag, String test, String testCase, long exeID);

    /**
     *
     * That method manage the dependency after the end of an execution. It will
     * release all associated dependencies and check the associated queue
     * execution if they can also be released to QUEUED status
     *
     * @param tCExecution
     */
    void manageDependenciesEndOfExecution(TestCaseExecution tCExecution);

    /**
     *
     * That method manage the dependency after a queue dep has been inserted. It
     * will release all associated dependencies and check the associated queue
     * execution if they can also be released to QUEUED status
     *
     * @param executionQueueDep
     */
    void manageDependenciesNewOfQueueDep(TestCaseExecutionQueueDep executionQueueDep);

    /**
     *
     * That method manage the dependency after the end of a queue entry.
     *
     * @param idQueue
     */
    void manageDependenciesEndOfQueueExecution(long idQueue);

    /**
     * That method manage the dependency that are based on TIMINGS. It will
     * release all associated dependencies and check the associated queue
     * execution if they can also be released to QUEUED status. It should be
     * triggered periodically
     *
     * @return the number of queue execution that has been released and ready to
     * be triggered.
     */
    int manageDependenciesCheckTimingWaiting();

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    TestCaseExecutionQueueDep convert(AnswerItem<TestCaseExecutionQueueDep> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<TestCaseExecutionQueueDep> convert(AnswerList<TestCaseExecutionQueueDep> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

    /**
     * Will enrish the list with all dependent queue Entries. result list is at
     * least as big as initial one.
     *
     * @param queueIdList
     * @return
     */
    public List<Long> enrichWithDependencies(List<Long> queueIdList);
}
