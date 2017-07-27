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
package org.cerberus.engine.threadpool;

import org.cerberus.crud.entity.CountryEnvironmentParameters;
import org.cerberus.engine.entity.threadpool.ExecutionThreadPool;
import org.cerberus.engine.entity.threadpool.ExecutionThreadPoolStats;
import org.cerberus.engine.entity.threadpool.ExecutionWorkerThread;
import org.cerberus.engine.entity.threadpool.ManageableThreadPoolExecutor;
import org.cerberus.exception.CerberusException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Manage a set of {@link ExecutionThreadPool} to support multiple Cerberus test
 * case executions
 *
 * @author abourdon
 */
public interface IExecutionThreadPoolService {

    /**
     * Search any {@link org.cerberus.crud.entity.TestCaseExecutionInQueue}
     * which are currently waiting for execution and trigger their executions
     *
     * @throws CerberusException if an error occurred during search or trigger
     * process
     */
    void executeNextInQueue() throws CerberusException;

    /**
     * Search at most #limit
     * {@link org.cerberus.crud.entity.TestCaseExecutionInQueue} which are
     * currently waiting for execution and trigger their executions
     *
     * @param limit the limit size of
     * {@link org.cerberus.crud.entity.TestCaseExecutionInQueue} in waiting
     * state to execute
     * @throws CerberusException if an error occurred during search or trigger
     * process
     */
    void executeNextInQueue(int limit) throws CerberusException;

    /**
     * Start execution of the given list of executions in queue id
     *
     * @param ids the list of executions in queue to start
     * @throws CerberusException if an error occurred
     */
    void executeNextInQueue(List<Long> ids) throws CerberusException;

    /**
     * Start execution of the given list of executions in queue id
     *
     * @param id executions in queue to start
     * @throws CerberusException if an error occurred
     */
    void executeNextInQueue(Long id) throws CerberusException;

    /**
     * Get an quasi-accurate (not atomic) statistics of the current execution
     * pools
     *
     * @return a collection of {@link ExecutionThreadPoolStats}
     */
    Collection<ExecutionThreadPoolStats> getStats();

    /**
     * Get the currently queued and executing task registered to the
     * {@link ExecutionThreadPool} identified by the given
     * {@link org.cerberus.crud.entity.CountryEnvironmentParameters.Key}
     *
     * @param key the
     * {@link org.cerberus.crud.entity.CountryEnvironmentParameters.Key} to
     * identify the {@link ExecutionThreadPool} from which getting tasks
     * @return the currently queued and executing task registered to the
     * {@link ExecutionThreadPool} identified by the given
     * {@link org.cerberus.crud.entity.CountryEnvironmentParameters.Key}
     */
    Map<ManageableThreadPoolExecutor.TaskState, List<ExecutionWorkerThread>> getTasks(CountryEnvironmentParameters.Key key);

    /**
     * Pause the {@link ExecutionThreadPool} identified by the given
     * {@link org.cerberus.crud.entity.CountryEnvironmentParameters.Key}
     *
     * @param key the
     * {@link org.cerberus.crud.entity.CountryEnvironmentParameters.Key} to
     * identify the {@link ExecutionThreadPool} to pause
     */
    void pauseExecutionThreadPool(CountryEnvironmentParameters.Key key);

    /**
     * Resume the {@link ExecutionThreadPool} identified by the given
     * {@link org.cerberus.crud.entity.CountryEnvironmentParameters.Key}
     *
     * @param key the
     * {@link org.cerberus.crud.entity.CountryEnvironmentParameters.Key} to
     * identify the {@link ExecutionThreadPool} to resume
     */
    void resumeExecutionThreadPool(CountryEnvironmentParameters.Key key);

    /**
     * Remove the {@link ExecutionThreadPool} identified by the given
     * {@link org.cerberus.crud.entity.CountryEnvironmentParameters.Key}
     *
     * @param key the
     * {@link org.cerberus.crud.entity.CountryEnvironmentParameters.Key} to
     * identify the {@link ExecutionThreadPool} to remove
     */
    void removeExecutionThreadPool(CountryEnvironmentParameters.Key key);

}
