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
import java.util.Map;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionQueue;
import org.cerberus.engine.queuemanagement.entity.TestCaseExecutionQueueToTreat;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 * Service layer to handle {@link TestCaseExecutionQueue} instances
 *
 * @author abourdon
 */
public interface ITestCaseExecutionQueueService {

    /**
     *
     * @param queueId
     * @param withDep
     * @return
     */
    AnswerItem<TestCaseExecutionQueue> readByKey(long queueId, boolean withDep);

    /**
     * Find a list of TestCaseWithExecution object from testcaseexecutionqueue
     * table
     *
     * @param tag
     * @return
     * @throws CerberusException
     */
    List<TestCaseExecutionQueue> findTestCaseExecutionInQueuebyTag(String tag) throws CerberusException;

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
    AnswerList<TestCaseExecutionQueue> readByTagByCriteria(String tag, int start, int amount, String sort, String searchTerm, Map<String, List<String>> individualSearch) throws CerberusException;

    /**
     *
     * @param tag tag to filter.
     * @param stateList List of State to filter.
     * @param withDependencies
     * @return
     * @throws CerberusException
     */
    AnswerList<TestCaseExecutionQueue> readByVarious1(String tag, List<String> stateList, boolean withDependencies) throws CerberusException;

    /**
     * All entries that are taken by the queue engine for processing.
     *
     * @return @throws CerberusException
     */
    AnswerList<TestCaseExecutionQueueToTreat> readQueueToTreat() throws CerberusException;

    /**
     * All entries that are considered as running and consuming ressource for
     * constrain management.
     *
     * @return @throws CerberusException
     */
    AnswerList<TestCaseExecutionQueueToTreat> readQueueRunning() throws CerberusException;

    /**
     * Entries that are either executing or still to execute from the queue.
     *
     * @return @throws CerberusException
     */
    AnswerList<TestCaseExecutionQueueToTreat> readQueueToTreatOrRunning() throws CerberusException;

    /**
     * Tag is considered as still running until there still entries on those
     * status.
     *
     * @param tag
     * @return
     * @throws org.cerberus.exception.CerberusException
     */
    AnswerList<TestCaseExecutionQueue> readQueueOpen(String tag) throws CerberusException;

    /**
     *
     * @param start
     * @param amount
     * @param column
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @return
     */
    AnswerList<TestCaseExecutionQueue> readByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch);

    /**
     *
     * @param id
     * @param prio
     * @return
     */
    int getNbEntryToGo(long id, int prio);

    /**
     *
     * @param tag
     * @return
     */
    AnswerList<TestCaseExecutionQueue> readDistinctEnvCountryBrowserByTag(String tag);

    /**
     *
     * @param tag
     * @param env
     * @param country
     * @param browser
     * @param app
     * @return
     */
    public AnswerList<TestCaseExecutionQueue> readDistinctColumnByTag(String tag, boolean env, boolean country, boolean browser, boolean app);

    /**
     *
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param individualSearch
     * @param column
     * @return
     */
    public AnswerList<String> readDistinctValuesByCriteria(String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch, String column);

    /**
     *
     * @param tagnumber
     * @return
     */
    public AnswerList findTagList(int tagnumber);

    /**
     *
     * @param system
     * @param testList
     * @param applicationList
     * @param projectList
     * @param tcstatusList
     * @param groupList
     * @param isActiveList
     * @param priorityList
     * @param targetMajorList
     * @param targetMinorList
     * @param creatorList
     * @param implementerList
     * @param buildList
     * @param revisionList
     * @param environmentList
     * @param countryList
     * @param browserList
     * @param tcestatusList
     * @param ip
     * @param port
     * @param tag
     * @param browserversion
     * @param comment
     * @param bugs
     * @param ticket
     * @return
     */
    AnswerList readBySystemByVarious(String system, List<String> testList, List<String> applicationList, List<String> tcstatusList, List<String> groupList,
            List<String> isActiveList, List<String> priorityList, List<String> targetMajorList, List<String> targetMinorList, List<String> creatorList,
            List<String> implementerList, List<String> buildList, List<String> revisionList, List<String> environmentList, List<String> countryList, List<String> browserList, List<String> tcestatusList, String ip, String port, String tag, String browserversion,
            String comment, String bugs, String ticket);

    /**
     * Create a new Queue entry on database from existing object. if withNewDep
     * true, we create new not RELEASED dependencies. if false, we duplicate
     * existing dependencies from queue entry exeQueue.
     *
     * @param object the {@link queue entry} to Create
     * @param withNewDep
     * @param exeQueue original queue entry id from which the duplication is
     * done.
     * @param targetState
     * @return {@link AnswerItem}
     */
    AnswerItem<TestCaseExecutionQueue> create(TestCaseExecutionQueue object, boolean withNewDep, long exeQueue, TestCaseExecutionQueue.State targetState);

    /**
     *
     * @param exeQueueId
     * @param tag
     */
    void checkAndReleaseQueuedEntry(long exeQueueId, String tag);

    /**
     * @param object the {@link AppService} to Update
     * @return {@link AnswerItem}
     */
    Answer update(TestCaseExecutionQueue object);

    /**
     *
     * @param id
     * @param priority
     * @return
     */
    Answer updatePriority(long id, int priority);

    /**
     *
     * @param id
     * @param comment
     * @return
     */
    Answer updateComment(long id, String comment);

    /**
     *
     * @param id
     * @param comment
     * @param targetState
     * @return
     */
    Answer updateToState(long id, String comment, TestCaseExecutionQueue.State targetState);

    /**
     *
     * @param id
     * @param comment
     * @return
     */
    Answer updateToQueued(long id, String comment);

    /**
     *
     * @param id
     * @param comment
     * @return
     */
    Answer updateToQueuedFromQuWithDep(long id, String comment);

    /**
     *
     * @param tag
     * @param queueIds
     * @return
     */
    Answer updateAllTagToQueuedFromQuTemp(String tag, List<Long> queueIds);

    /**
     *
     * @param id
     * @return
     * @throws CerberusException
     */
    boolean updateToWaiting(final Long id) throws CerberusException;

    /**
     *
     * @param id
     * @param comment
     * @param exeId
     * @throws CerberusException
     */
    void updateToExecuting(long id, String comment, long exeId) throws CerberusException;

    /**
     *
     * @param id
     * @param selectedRobot
     * @param selectedRobotExt
     * @throws CerberusException
     */
    void updateToStarting(long id, String selectedRobot, String selectedRobotExt) throws CerberusException;

    /**
     *
     * @param id
     * @param comment
     * @throws CerberusException
     */
    void updateToError(long id, String comment) throws CerberusException;

    /**
     *
     * @param id
     * @param comment
     * @throws CerberusException
     */
    void updateToErrorFromQuWithDep(long id, String comment) throws CerberusException;

    /**
     *
     * @param id
     * @param comment
     * @param exeId
     * @throws CerberusException
     */
    void updateToDone(long id, String comment, long exeId) throws CerberusException;

    /**
     *
     * @param id
     * @param comment
     * @return
     */
    Answer updateToCancelled(long id, String comment);

    /**
     *
     * @param id
     * @param comment
     * @return
     */
    Answer updateToCancelledForce(long id, String comment);

    /**
     *
     * @param id
     * @param comment
     * @return
     */
    Answer updateToErrorForce(long id, String comment);

    /**
     * @param object the {@link AppService} to Delete
     * @return {@link AnswerItem}
     */
    Answer delete(TestCaseExecutionQueue object);

    /**
     * @param id the {@link AppService} to Delete
     * @return {@link AnswerItem}
     */
    Answer delete(Long id);

    /**
     *
     */
    void cancelRunningOldQueueEntries();

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    TestCaseExecutionQueue convert(AnswerItem<TestCaseExecutionQueue> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<TestCaseExecutionQueue> convert(AnswerList<TestCaseExecutionQueue> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

    /**
     * Convert a testCaseExecutionInQueue object into TestCaseExecution
     *
     * @param testCaseExecutionInQueue
     * @return TestCaseExecution Object
     */
    public TestCaseExecution convertToTestCaseExecution(TestCaseExecutionQueue testCaseExecutionInQueue);
}
