/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
import java.util.Map;

import org.cerberus.crud.entity.TestCaseExecution;

import org.cerberus.crud.entity.TestCaseExecutionInQueue;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 * Service layer to handle {@link TestCaseExecutionInQueue} instances
 *
 * @author abourdon
 */
public interface ITestCaseExecutionInQueueService {

    /**
     * Inserts the given {@link TestCaseExecutionInQueue} to the execution queue
     *
     * @param inQueue the {@link TestCaseExecutionInQueue} to insert to the
     *                execution queue
     * @throws CerberusException if an exception occurs
     */
    void insert(TestCaseExecutionInQueue inQueue) throws CerberusException;

    /**
     * Removes a {@link TestCaseExecutionInQueue} record from the database.
     *
     * @param id the {@link TestCaseExecutionInQueue#getId()} to remove
     * @throws CerberusException if an exception occurs
     */
    void remove(long id) throws CerberusException;

    /**
     * Find a list of TestCaseWithExecution object from testcaseexecutionqueue
     * table
     *
     * @param tag
     * @return
     * @throws CerberusException
     */
    List<TestCaseExecutionInQueue> findTestCaseExecutionInQueuebyTag(String tag) throws CerberusException;

    /**
     * Find a {@link TestCaseExecutionInQueue} from database
     *
     * @param id
     * @return
     * @throws CerberusException
     */
    TestCaseExecutionInQueue findByKey(long id) throws CerberusException;

    TestCaseExecutionInQueue findByKeyWithDependencies(long key) throws CerberusException;

    void toWaiting(long id) throws CerberusException;

    List<TestCaseExecutionInQueue> toQueued() throws CerberusException;

    List<TestCaseExecutionInQueue> toQueued(int maxFetchSize) throws CerberusException;

    void toExecuting(long id) throws CerberusException;

    void toError(long id, String comment) throws CerberusException;

    void toCancelled(long id) throws CerberusException;

    /**
     * Find the list of TestCaseWithExecution object from testcaseexecutionqueue
     *
     * @return
     * @throws CerberusException
     */
    List<TestCaseExecutionInQueue> findAll() throws CerberusException;

    public AnswerList readByTagByCriteria(String tag, int start, int amount, String sort, String searchTerm, Map<String, List<String>> individualSearch) throws CerberusException;

    public AnswerList readByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch);

    public AnswerList readDistinctEnvCoutnryBrowserByTag(String tag);

    public AnswerList readDistinctColumnByTag(String tag, boolean env, boolean country, boolean browser, boolean app);

    public AnswerList readDistinctValuesByCriteria(String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch, String column);

    public AnswerList findTagList(int tagnumber);

    AnswerList readBySystemByVarious(String system, List<String> testList, List<String> applicationList, List<String> projectList, List<String> tcstatusList, List<String> groupList,
                                     List<String> tcactiveList, List<String> priorityList, List<String> targetsprintList, List<String> targetrevisionList, List<String> creatorList,
                                     List<String> implementerList, List<String> buildList, List<String> revisionList, List<String> environmentList, List<String> countryList, List<String> browserList, List<String> tcestatusList, String ip, String port, String tag, String browserversion,
                                     String comment, String bugid, String ticket);

    public Answer create(TestCaseExecutionInQueue test);

    public Answer update(TestCaseExecutionInQueue test);

    public Answer delete(TestCaseExecutionInQueue test);

    /**
     * Convert a testCaseExecutionInQueue object into TestCaseExecution
     *
     * @param testCaseExecutionInQueue
     * @return TestCaseExecution Object
     */
    public TestCaseExecution convertToTestCaseExecution(TestCaseExecutionInQueue testCaseExecutionInQueue);
}
