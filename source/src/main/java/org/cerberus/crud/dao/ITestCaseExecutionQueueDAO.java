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

/**
 * {@link TestCaseExecutionQueue} DAO
 *
 * @author abourdon
 */
public interface ITestCaseExecutionQueueDAO {

    int UNLIMITED_FETCH_SIZE = -1;

    /**
     * Inserts the given {@link TestCaseExecutionQueue} to the execution queue
     *
     * @param inQueue the {@link TestCaseExecutionQueue} to insert to the
     *                execution queue
     * @throws CerberusException if an exception occurs
     */
    void insert(TestCaseExecutionQueue inQueue) throws CerberusException;

    /**
     * Removes a {@link TestCaseExecutionQueue} record from the database.
     *
     * @param id the {@link TestCaseExecutionQueue#getId()} to remove
     * @throws CerberusException if an exception occurs
     */
    void remove(long id) throws CerberusException;

    /**
     * Find a list of {@link TestCaseExecutionQueue}
     *
     * @param tag
     * @return list of object TestCaseExecutionInQueue
     * @throws CerberusException
     */
    List<TestCaseExecutionQueue> findTestCaseExecutionInQueuebyTag(String tag) throws CerberusException;

    /**
     * Fing a {@link TestCaseExecutionQueue} record from the database knowing
     * the key
     *
     * @param id
     * @return
     * @throws CerberusException
     */
    TestCaseExecutionQueue findByKey(long id) throws CerberusException;

    TestCaseExecutionQueue findByKeyWithDependencies(long id) throws CerberusException;

    public List<TestCaseExecutionQueue> findAll() throws CerberusException;

    List<TestCaseExecutionQueue> toQueued(int maxFetchSize) throws CerberusException;

    List<TestCaseExecutionQueue> toQueued(List<Long> ids) throws CerberusException;

    void toExecuting(long id) throws CerberusException;

    void toWaiting(long id) throws CerberusException;

    List<Long> toWaiting(List<Long> ids) throws CerberusException;

    void toError(long id, String comment) throws CerberusException;

    void toDone(long id, String comment, long exeId) throws CerberusException;
    
    void toCancelled(long id) throws CerberusException;

    List<Long> toCancelled(List<Long> ids) throws CerberusException;

    public AnswerList readByTagByCriteria(String tag, int start, int amount, String sort, String searchTerm, Map<String, List<String>> individualSearch) throws CerberusException;
    
    /**
     * Read TestCaseExecutionInQueue By Tag
     * @param tag Tag used to filter execution
     * @return AnswerList that contains a list of TestCaseExecutionInQueue object enriched with TestCase and Application objects
     * @throws CerberusException
     */
    public AnswerList readByTag(String tag) throws CerberusException;

    public AnswerList readByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch);

    public AnswerList readDistinctEnvCoutnryBrowserByTag(String tag);

    public AnswerList readDistinctColumnByTag(String tag, boolean env, boolean country, boolean browser, boolean app);

    public AnswerList findTagList(int tagnumber);

    public AnswerList readBySystemByVarious(String system, List<String> testList, List<String> applicationList, List<String> projectList, List<String> tcstatusList, List<String> groupList, List<String> tcactiveList, List<String> priorityList, List<String> targetsprintList, List<String> targetrevisionList, List<String> creatorList, List<String> implementerList, List<String> buildList, List<String> revisionList, List<String> environmentList, List<String> countryList, List<String> browserList, List<String> tcestatusList, String ip, String port, String tag, String browserversion, String comment, String bugid, String ticket);

    public AnswerList readDistinctValuesByCriteria(String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch, String column);

    /**
     * Uses data of ResultSet to create object {@link TestCaseExecutionQueue}
     *
     * @param resultSet ResultSet relative to select from table TestCaseExecutionInQueue
     * @return object {@link TestCaseExecutionQueue}
     * @throws SQLException when trying to get value from
     *                      {@link java.sql.ResultSet#getString(String)}
     * @see TestCaseExecutionQueue
     */
    TestCaseExecutionQueue loadFromResultSet(ResultSet resultSet) throws SQLException, FactoryCreationException;
}
