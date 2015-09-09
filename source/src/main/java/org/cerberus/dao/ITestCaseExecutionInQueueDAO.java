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
package org.cerberus.dao;

import java.util.List;
import org.cerberus.dto.TestCaseWithExecution;

import org.cerberus.entity.TestCaseExecutionInQueue;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.AnswerList;

/**
 * {@link TestCaseExecutionInQueue} DAO
 *
 * @author abourdon
 */
public interface ITestCaseExecutionInQueueDAO {

    /**
     * Inserts the given {@link TestCaseExecutionInQueue} to the execution queue
     *
     * @param inQueue the {@link TestCaseExecutionInQueue} to insert to the
     * execution queue
     * @throws CerberusException if an exception occurs
     */
    void insert(TestCaseExecutionInQueue inQueue) throws CerberusException;

    /**
     * Gets the next {@link TestCaseExecutionInQueue} to be executed and proceed
     * it.
     *
     * <p>
     * A {@link TestCaseExecutionInQueue} is proceeded when its database
     * Proceeded field is set to <code>true</code>
     * </p>
     *
     * @return the next {@link TestCaseExecutionInQueue} to be executed and
     * which has just been proceeded
     *
     * @throws CerberusException if an exception occurs
     */
    TestCaseExecutionInQueue getNextAndProceed() throws CerberusException;

    /**
     * Gets the list of {@link TestCaseExecutionInQueue} which have been
     * proceeded (so which have its proceeded field marked as <code>true</code>)
     * and which have been marked with the given tag.
     *
     * @param tag the tag to find proceeded {@link TestCaseExecutionInQueue}. If
     * <code>null</code> then every proceeded {@link TestCaseExecutionInQueue}
     * will be returned
     * @return a list of {@link TestCaseExecutionInQueue}
     * @throws CerberusException if an exception occurs
     */
    List<TestCaseExecutionInQueue> getProceededByTag(String tag) throws CerberusException;

    /**
     * Removes a {@link TestCaseExecutionInQueue} record from the database.
     *
     * @param id the {@link TestCaseExecutionInQueue#getId()} to remove
     * @throws CerberusException if an exception occurs
     */
    void remove(long id) throws CerberusException;

    /**
     * Find a list of {@link TestCaseWithExecution}
     *
     * @param tag
     * @return list of object TestCaseWithExecution
     * @throws CerberusException
     */
    List<TestCaseWithExecution> findTestCaseWithExecutionInQueuebyTag(String tag) throws CerberusException;

    /**
     * Fing a {@link TestCaseExecutionInQueue} record from the database knowing
     * the key
     *
     * @param id
     * @return
     * @throws CerberusException
     */
    TestCaseExecutionInQueue findByKey(long id) throws CerberusException;

    List<TestCaseExecutionInQueue> getNotProceededAndProceed() throws CerberusException;

    public List<TestCaseExecutionInQueue> findAll() throws CerberusException;

    public void setProcessedTo(Long l, String changeTo) throws CerberusException;

    public void updateComment(Long queueId, String comment) throws CerberusException;

    public AnswerList readByStatusByCriteria(List<String> statusList, int start, int amount, String column, String dir, String searchTerm, String individualSearch, String tag) throws CerberusException;

}
