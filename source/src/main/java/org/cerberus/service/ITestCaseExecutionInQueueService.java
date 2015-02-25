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
package org.cerberus.service;

import java.util.List;
import org.cerberus.dto.TestCaseWithExecution;

import org.cerberus.entity.TestCaseExecutionInQueue;
import org.cerberus.exception.CerberusException;

/**
 * Service layer to handle {@link TestCaseExecutionInQueue} instances
 *
 * @author abourdon
 */
public interface ITestCaseExecutionInQueueService {

    /**
     * Checks if the given {@link TestCaseExecutionInQueue} can be inserted into
     * the execution queue
     *
     * @param inQueue the {@link TestCaseExecutionInQueue} to check
     * @return <code>true</code> if the given {@link TestCaseExecutionInQueue}
     * can be inserted into the execution queue, <code>false</code> otherwise
     * @throws CerberusException if an exception occurs
     */
    boolean canInsert(TestCaseExecutionInQueue inQueue) throws CerberusException;

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
     * Find a list of TestCaseWithExecution object from testcaseexecutionqueue
     * table
     *
     * @param tag
     * @return
     * @throws CerberusException
     */
    List<TestCaseWithExecution> findTestCaseWithExecutionInQueuebyTag(String tag) throws CerberusException;

    /**
     * Find a {@link TestCaseExecutionInQueue} from database
     *
     * @param id
     * @return
     * @throws CerberusException
     */
    TestCaseExecutionInQueue findByKey(long id) throws CerberusException;
    
    
    /**
     * Find the list of TestCaseWithExecution object with Procedeed = 0 from testcaseexecutionqueue
     * @return
     * @throws CerberusException 
     */
    List<TestCaseExecutionInQueue> findAllNotProcedeed() throws CerberusException;
}
