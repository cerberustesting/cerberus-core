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
package org.cerberus.engine.threadpool.impl;

import org.apache.log4j.Logger;
import org.cerberus.crud.entity.CountryEnvironmentParameters;
import org.cerberus.crud.entity.TestCaseExecutionInQueue;
import org.cerberus.crud.service.ICountryEnvironmentParametersService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestCaseExecutionInQueueService;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.engine.entity.threadpool.ExecutionThreadPool;
import org.cerberus.engine.entity.threadpool.ExecutionThreadPoolStats;
import org.cerberus.engine.entity.threadpool.ExecutionWorkerThread;
import org.cerberus.engine.entity.threadpool.ManageableThreadPoolExecutor;
import org.cerberus.engine.threadpool.IExecutionThreadPoolService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.observe.Observer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link IExecutionThreadPoolService} default implementation
 *
 * @author bcivel
 * @author abourdon
 */
@Service
public class ExecutionThreadPoolService implements IExecutionThreadPoolService, Observer<CountryEnvironmentParameters.Key, CountryEnvironmentParameters> {

    private static final Logger LOG = Logger.getLogger(ExecutionThreadPoolService.class);

    private static final int UNLIMITED_FETCH_SIZE = -1;

    /**
     * The string format when displaying generated name.
     * <p>
     * Values are:
     * <ol>
     * <li>{@link CountryEnvironmentParameters.Key#getSystem()}</li>
     * <li>{@link CountryEnvironmentParameters.Key#getApplication()}</li>
     * <li>{@link CountryEnvironmentParameters.Key#getCountry()}</li>
     * <li>{@link CountryEnvironmentParameters.Key#getEnvironment()}</li>
     * </ol>
     *
     * @see #generateName(CountryEnvironmentParameters.Key)
     */
    private static final String EXECUTION_POOL_NAME_FORMAT = "%s-%s-%s";

    private static final String PARAMETER_CERBERUS_URL = "cerberus_url";

    @Autowired
    private ITestCaseExecutionInQueueService tceiqService;

    @Autowired
    private IParameterService parameterService;

    @Autowired
    private ICountryEnvironmentParametersService countryEnvironmentParametersService;

    private Map<CountryEnvironmentParameters.Key, ExecutionThreadPool<ExecutionWorkerThread>> executionPools;

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeNextInQueue() throws CerberusException {
        executeNextInQueue(UNLIMITED_FETCH_SIZE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeNextInQueue(final int limit) throws CerberusException {
        if (limit < 0 && limit != UNLIMITED_FETCH_SIZE) {
            LOG.warn("Unable to fetch " + limit + " waiting execution in queue");
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
        }
        final List<TestCaseExecutionInQueue> executionsInQueue = limit == UNLIMITED_FETCH_SIZE ? tceiqService.toQueued() : tceiqService.toQueued(limit);
        for (final TestCaseExecutionInQueue executionInQueue : executionsInQueue) {
            try {
                execute(executionInQueue);
            } catch (CerberusException e) {
                LOG.warn("Unable to execute " + executionInQueue.getId() + " due to " + e.getMessageError().getDescription(), e);
                tceiqService.toError(executionInQueue.getId(), e.getMessageError().getDescription());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeNextInQueue(final List<Long> ids) throws CerberusException {
        for (final TestCaseExecutionInQueue queued : tceiqService.toQueued(ids)) {
            try {
                execute(queued);
            } catch (CerberusException e) {
                LOG.warn("Unable to execute " + queued.getId() + " due to " + e.getMessageError().getDescription(), e);
                try {
                    tceiqService.toError(queued.getId(), e.getMessageError().getDescription());
                } catch (CerberusException again) {
                    LOG.warn("Unable to put in error " + queued.getId() + " due to " + e.getMessageError().getDescription(), e);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ExecutionThreadPoolStats> getStats() {
        final Collection<ExecutionThreadPoolStats> stats = new ArrayList<>();
        for (Map.Entry<CountryEnvironmentParameters.Key, ExecutionThreadPool<ExecutionWorkerThread>> pool : executionPools.entrySet()) {
            // Quasi-accurate (not atomic) statistics
            stats.add(new ExecutionThreadPoolStats()
                    .setId(pool.getKey())
                    .setPoolSize(pool.getValue().getPoolSize())
                    .setInQueue(pool.getValue().getInQueue())
                    .setInExecution(pool.getValue().getInExecution())
                    .setPaused(pool.getValue().isPaused())
                    .setStopped(pool.getValue().isStopped())
            );
        }
        return stats;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<ManageableThreadPoolExecutor.TaskState, List<ExecutionWorkerThread>> getTasks(CountryEnvironmentParameters.Key key) {
        ExecutionThreadPool<ExecutionWorkerThread> associatedPool = getExecutionPool(key);
        if (associatedPool == null) {
            return null;
        }
        return associatedPool.getTasks();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pauseExecutionThreadPool(CountryEnvironmentParameters.Key key) {
        ExecutionThreadPool<ExecutionWorkerThread> associatedPool = getExecutionPool(key);
        if (associatedPool == null) {
            return;
        }
        associatedPool.pause();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resumeExecutionThreadPool(CountryEnvironmentParameters.Key key) {
        ExecutionThreadPool<ExecutionWorkerThread> associatedPool = getExecutionPool(key);
        if (associatedPool == null) {
            return;
        }
        associatedPool.resume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeExecutionThreadPool(CountryEnvironmentParameters.Key key) {
        // Stop the execution thread pool and remove it from our associated map
        ExecutionThreadPool<ExecutionWorkerThread> associatedPool;
        List<ExecutionWorkerThread> remainingExecutions = null;
        synchronized (executionPools) {
            associatedPool = executionPools.remove(key);
            if (associatedPool != null) {
                remainingExecutions = associatedPool.stop();
            }
        }

        // Try to remove executions from database
        if (remainingExecutions != null) {
            for (ExecutionWorkerThread remainingExecution : remainingExecutions) {
                try {
                    tceiqService.remove(remainingExecution.getToExecute().getId());
                } catch (Exception e) {
                    LOG.warn("Unable to clean execution in queue " + remainingExecution.getToExecute().getId() + " during stopping process of the execution thread pool " + associatedPool.getName(), e);
                }
            }
        }
    }

    @Override
    public void observeCreate(CountryEnvironmentParameters.Key key, CountryEnvironmentParameters countryEnvironmentParameters) {
        // Nothing to do
    }

    @Override
    public void observeUpdate(CountryEnvironmentParameters.Key key, CountryEnvironmentParameters countryEnvironmentParameters) {
        ExecutionThreadPool<ExecutionWorkerThread> associatedExecutionPool = getExecutionPool(key);
        if (associatedExecutionPool != null) {
            associatedExecutionPool.setSize(countryEnvironmentParameters.getPoolSize());
        }
    }

    @Override
    public void observeDelete(CountryEnvironmentParameters.Key key, CountryEnvironmentParameters countryEnvironmentParameters) {
        removeExecutionThreadPool(key);
    }

    @PostConstruct
    private void init() {
        initExecutionPools();
    }

    @PreDestroy
    private void stop() {
        stopRegistration();
        stopExecutionPools();
    }

    private void initExecutionPools() {
        executionPools = new HashMap<>();
    }

    private void stopExecutionPools() {
        if (executionPools == null) {
            return;
        }
        for (ExecutionThreadPool<ExecutionWorkerThread> executionPool : executionPools.values()) {
            LOG.info("Stopping execution pool " + executionPool.getName());
            executionPool.stop();
        }
    }

    private void stopRegistration() {
        countryEnvironmentParametersService.unregister(this);
    }

    private String generateName(CountryEnvironmentParameters.Key key) {
        return String.format(EXECUTION_POOL_NAME_FORMAT, key.getSystem(), key.getApplication(), key.getCountry(), key.getEnvironment());
    }

    private void execute(TestCaseExecutionInQueue toExecute) throws CerberusException {
        try {
            CountryEnvironmentParameters.Key toExecuteKey = getKey(toExecute);
            ExecutionThreadPool<ExecutionWorkerThread> executionPool = getOrCreateExecutionPool(toExecuteKey);
            ExecutionWorkerThread execution = new ExecutionWorkerThread.Builder()
                    .toExecute(toExecute)
                    .toExecuteKey(toExecuteKey)
                    .cerberusUrl(parameterService.findParameterByKey(PARAMETER_CERBERUS_URL, "").getValue())
                    .inQueueService(tceiqService)
                    .build();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Request to execute " + execution + " from execution pool " + executionPool);
            }
            executionPool.submit(execution);
        } catch (Exception e) {
            String message = "Unable to execute " + toExecute + " due to " + e.getMessage();
            LOG.warn(message, e);
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.GENERIC_ERROR).resolveDescription("REASON", message));
        }
    }

    private ExecutionThreadPool getOrCreateExecutionPool(CountryEnvironmentParameters.Key key) {
        ExecutionThreadPool<ExecutionWorkerThread> executionPool = executionPools.get(key);
        if (executionPool == null) {
            synchronized (executionPools) {
                executionPool = executionPools.get(key);
                if (executionPool == null) {
                    executionPool = new ExecutionThreadPool<>(generateName(key), getPoolSize(key));
                    executionPools.put(key, executionPool);
                    registerTo(key);
                }
            }
        }
        return executionPool;
    }

    private ExecutionThreadPool<ExecutionWorkerThread> getExecutionPool(CountryEnvironmentParameters.Key key) {
        synchronized (executionPools) {
            return executionPools.get(key);
        }
    }

    private CountryEnvironmentParameters.Key getKey(TestCaseExecutionInQueue inQueue) throws CerberusException {
        return new CountryEnvironmentParameters.Key(
                inQueue.getApplicationObj().getSystem(),
                inQueue.getApplicationObj().getApplication(),
                inQueue.getCountry(),
                inQueue.getEnvironment()
        );
    }

    private int getPoolSize(CountryEnvironmentParameters.Key key) {
        AnswerItem<Integer> poolSize = countryEnvironmentParametersService.readPoolSizeByKey(key.getSystem(), key.getCountry(), key.getEnvironment(), key.getApplication());
        if (MessageEventEnum.DATA_OPERATION_OK.equals(poolSize.getResultMessage().getSource())) {
            return poolSize.getItem();
        } else {
            LOG.warn("Unable to get pool size from " + key + ". Get default");
            return countryEnvironmentParametersService.defaultPoolSize();
        }
    }

    private void registerTo(CountryEnvironmentParameters.Key key) {
        countryEnvironmentParametersService.register(key, this);
    }

}
