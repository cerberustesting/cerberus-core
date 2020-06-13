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

import org.cerberus.crud.entity.TestCaseExecutionQueue;
import org.cerberus.exception.CerberusException;
import org.cerberus.exception.FactoryCreationException;
import org.cerberus.util.answer.AnswerList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.cerberus.engine.queuemanagement.entity.TestCaseExecutionQueueToTreat;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;

/**
 * {@link TestCaseExecutionQueue} DAO
 *
 * @author abourdon
 */
public interface ITestCaseExecutionQueueDAO {

    int UNLIMITED_FETCH_SIZE = -1;

    /**
     *
     * @param queueId
     * @return
     */
    AnswerItem<TestCaseExecutionQueue> readByKey(long queueId);

    /**
     * Find a list of {@link TestCaseExecutionQueue}
     *
     * @param tag
     * @return list of object TestCaseExecutionInQueue
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
    public AnswerList<TestCaseExecutionQueue> readByTagByCriteria(String tag, int start, int amount, String sort, String searchTerm, Map<String, List<String>> individualSearch) throws CerberusException;

    /**
     * Read TestCaseExecutionInQueue By Tag
     *
     * @param tag Tag used to filter execution.
     * @param stateList List of State to filter.
     * @param withDependencies
     * @return AnswerList that contains a list of TestCaseExecutionInQueue
     * object enriched with TestCase and Application objects
     * @throws CerberusException
     */
    public AnswerList<TestCaseExecutionQueue> readByVarious1(String tag, List<String> stateList, boolean withDependencies) throws CerberusException;

    /**
     *
     * @param stateList list of state to filter.
     * @return @throws CerberusException
     */
    public AnswerList<TestCaseExecutionQueueToTreat> readByVarious2(List<String> stateList) throws CerberusException;

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
    public AnswerList<TestCaseExecutionQueue> readByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch);

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
    public AnswerList<TestCaseExecutionQueue> readDistinctEnvCountryBrowserByTag(String tag);

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
    public AnswerList readBySystemByVarious(String system, List<String> testList, List<String> applicationList, List<String> tcstatusList, List<String> groupList, List<String> isActiveList, List<String> priorityList, List<String> targetMajorList, List<String> targetMinorList, List<String> creatorList, List<String> implementerList, List<String> buildList, List<String> revisionList, List<String> environmentList, List<String> countryList, List<String> browserList, List<String> tcestatusList, String ip, String port, String tag, String browserversion, String comment, String bugs, String ticket);

    TestCaseExecutionQueue findByKeyWithDependencies(long id) throws CerberusException;

    /**
     * @param object the {@link AppService} to Create
     * @return {@link AnswerItem}
     */
    AnswerItem<TestCaseExecutionQueue> create(TestCaseExecutionQueue object);

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
     * @param tag
     * @param queueIds
     * @return
     */
    Answer updateAllTagToQueuedFromQuTemp(String tag, List<Long> queueIds);

    /**
     *
     * @param id
     * @param comment
     * @return
     */
    Answer updateToQueuedFromQuWithDep(long id, String comment);

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
     * @param selectedRobot
     * @param selectedRobotExt
     * @throws CerberusException
     */
    void updateToStarting(long id, String selectedRobot, String selectedRobotExt) throws CerberusException;

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
     * @param timeOutInS
     * @param comment
     * @return
     */
    AnswerItem<Integer> updateToCancelledOldRecord(Integer timeOutInS, String comment);

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
     * Uses data of ResultSet to create object {@link TestCaseExecutionQueue}
     *
     * @param resultSet ResultSet relative to select from table
     * TestCaseExecutionInQueue
     * @return object {@link TestCaseExecutionQueue}
     * @throws SQLException when trying to get value from
     * {@link java.sql.ResultSet#getString(String)}
     * @see TestCaseExecutionQueue
     */
    TestCaseExecutionQueue loadFromResultSet(ResultSet resultSet) throws SQLException, FactoryCreationException;
}
