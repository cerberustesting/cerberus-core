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

import java.util.List;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionQueueDep;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author vertigo
 */
public interface ITestCaseExecutionQueueDepService {

    /**
     * Insert execution dependencies attached to queueid, env, country and tag
     * from test/testcase
     *
     * @param queueId
     * @param env
     * @param country
     * @param tag
     * @param test
     * @param testcase
     * @return
     */
    AnswerItem<Integer> insertFromTCDep(long queueId, String env, String country, String tag, String test, String testcase);

    /**
     * Insert execution dependencies attached to queueid, env, country and tag
     * from test/testcase
     *
     * @param queueId
     * @param fromQueueId
     * @return
     */
    AnswerItem<Integer> insertFromQueueExeDep(long queueId, long fromQueueId);

    /**
     *
     * Set comment and exeId and RELEASE the dep lines linked to criterias.
     *
     * @param env
     * @param Country
     * @param tag
     * @param type
     * @param test
     * @param testCase
     * @param comment
     * @param exeId
     * @return
     */
    AnswerItem<Integer> updateStatusToRelease(String env, String Country, String tag, String type, String test, String testCase, String comment, long exeId, long queueId);

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
    AnswerList<TestCaseExecutionQueueDep> readByQueueId(long exeQueueId);

    /**
     *
     * @param exeQueueId
     * @return
     */
    AnswerItem<Integer> readNbWaitingByExeQueue(long exeQueueId);

    /**
     * load test case dependency Queue on object each TestCaseExecution
     *
     * @param testCaseExecutions
     * @throws org.cerberus.exception.CerberusException
     */
    void loadDependenciesOnTestCaseExecution(List<TestCaseExecution> testCaseExecutions) throws CerberusException;

    /**
     *
     * @param exeQueueId
     * @return
     */
    AnswerItem<Integer> readNbReleasedWithNOKByExeQueue(long exeQueueId);

    /**
     *
     * That method manage the dependency after the end of an execution.
     *
     * @param tCExecution
     */
    void manageDependenciesEndOfExecution(TestCaseExecution tCExecution);

    /**
     *
     * That method manage the dependency after the end of a queue entry.
     *
     * @param idQueue
     */
    void manageDependenciesEndOfQueueExecution(long idQueue);

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

}
