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
package org.cerberus.engine.entity;

import org.apache.log4j.Logger;
import org.cerberus.crud.entity.Parameter;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.util.observe.ObservableEngine;
import org.cerberus.util.observe.Observer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The execution thread pool to control Test Cases executions
 *
 * @author bcivel
 * @author abourdon
 */
@Component
public class ExecutionThreadPool implements Observer<String, Parameter> {

    /**
     * The associated {@link Logger} to this class
     */
    private static final Logger LOGGER = Logger.getLogger(ExecutionThreadPool.class);

    /**
     * The configuration key to get configured thread pool size
     */
    private static final String THREAD_POOL_SIZE_CONFIGURATION_KEY = "cerberus_execution_threadpool_size";

    /**
     * The default thread pool size
     */
    public static final int DEFAULT_THREAD_POOL_SIZE = 3;

    /**
     * The inner {@link ThreadPoolExecutor} that control Test Cases executions.
     * <p>
     * When instanciated, this {@link ThreadPoolExecutor} act the same as a {@link java.util.concurrent.Executors#newFixedThreadPool(int)},
     * but with the ability to tune its core pool size
     */
    private ThreadPoolExecutor executor;

    /**
     * The associated {@link IParameterService}
     */
    @Autowired
    private IParameterService parameterService;

    /**
     * Set the number of maximum simultaneous active threads this {@link ExecutionThreadPool} can have
     *
     * @param size the number of maximum simultaneous active threads to set
     */
    public synchronized void setSize(Integer size) {
        int currentSize = getPoolSize();
        if (size < currentSize) {
            executor.setCorePoolSize(size);
            executor.setMaximumPoolSize(size);
        } else if (size > currentSize) {
            executor.setMaximumPoolSize(size);
            executor.setCorePoolSize(size);
        }
    }

    /**
     * Get the number of {@link Runnable} tasks this {@link ExecutionThreadPool} can execute in the same time
     *
     * @return the number of {@link Runnable} tasks this {@link ExecutionThreadPool} can execute in the same time
     */
    public synchronized int getPoolSize() {
        // Should be equal to executor.getMaximumPoolSize()
        return executor.getCorePoolSize();
    }

    /**
     * Get the number of currently active threads
     *
     * @return the number of currently active threads
     */
    public synchronized int getInExecution() {
        return executor.getActiveCount();
    }

    /**
     * Get the approximate number of active and pending executions
     *
     * @return the approximate number of active and pending executions
     */
    public synchronized long getInQueue() {
        return executor.getTaskCount() - executor.getCompletedTaskCount();
    }

    /**
     * Submit a new {@link Runnable} to a new thread from this {@link ExecutionThreadPool}.
     * <p>
     * If the maximum of simultaneous active threads is reached, then this task is kept in queue until a thread is released
     *
     * @param task the {@link Runnable} to submit to this {@link ExecutionThreadPool}
     * @see #getPoolSize()
     */
    public synchronized void submit(Runnable task) {
        executor.submit(task);
    }

    /**
     * Reset this {@link ExecutionThreadPool} by trying to stop any submitted tasks and make the {@link #getInExecution()} equals to 0
     *
     * @see #getInExecution()
     */
    public synchronized void reset() {
        int currentSIze = getPoolSize();
        stopExecutor();
        initExecutor(currentSIze);
    }

    @Override
    public void observeCreate(String topic, Parameter parameter) {
        sizeChanged(parameter);
    }

    @Override
    public void observeUpdate(String topic, Parameter parameter) {
        sizeChanged(parameter);
    }

    @Override
    public void observeDelete(String topic, Parameter parameter) {
        // Nothing to do
    }

    /**
     * Initialize this {@link ExecutionThreadPool} by creating thread pool executor and register to {@link Parameter} changes
     */
    @PostConstruct
    private void init() {
        initExecutor(getInitialSize());
        initRegistration();
    }

    /**
     * Get the configured thread pool size from database.
     * <p>
     * If an error occured, then get the {@link #DEFAULT_THREAD_POOL_SIZE} value
     *
     * @return the configured thread pool size from database, or {@link #DEFAULT_THREAD_POOL_SIZE} if an error occurred
     */
    private int getInitialSize() {
        int applicableSize = DEFAULT_THREAD_POOL_SIZE;
        try {
            applicableSize = Integer.valueOf(parameterService.findParameterByKey(THREAD_POOL_SIZE_CONFIGURATION_KEY, "").getValue());
        } catch (Exception e) {
            LOGGER.warn("Unable to set configured thread pool size", e);
        }
        return applicableSize;
    }

    /**
     * Initialize the inner thread pool executor
     *
     * @param poolSize the thread pool size to set to the inner thread pool executor
     */
    private void initExecutor(int poolSize) {
        // The same as Executors#newFixedThreadPool(int) but with access to the
        // ThreadPoolExecutor API and so more controls than the ExecutorService one provided by
        // Executors#newFixedThreadPool(int)
        executor = new ThreadPoolExecutor(
                poolSize,
                poolSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>()
        );
    }

    /**
     * Initialize the registration to {@link Parameter} changes
     */
    private void initRegistration() {
        parameterService.register(THREAD_POOL_SIZE_CONFIGURATION_KEY, this);
    }

    /**
     * Stop this {@link ExecutionThreadPool} by stopping registration to {@link Parameter} changes and stopping the inner thread pool
     */
    @PreDestroy
    private void stop() {
        stopRegistration();
        stopExecutor();
    }

    /**
     * Unregister this {@link ExecutionThreadPool} from {@link Parameter} changes
     */
    private void stopRegistration() {
        parameterService.unregister(THREAD_POOL_SIZE_CONFIGURATION_KEY, this);
    }

    /**
     * Shutdown the inner thread pool by trying to stop any of its active or pending tasks
     */
    private void stopExecutor() {
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    /**
     * React to {@link Parameter} changes, especially the {@link #THREAD_POOL_SIZE_CONFIGURATION_KEY} to update the pool size
     *
     * @param parameter the changing {@link Parameter}
     * @see #setSize(Integer)
     */
    private void sizeChanged(Parameter parameter) {
        try {
            setSize(Integer.valueOf(parameter.getValue()));
        } catch (Exception e) {
            LOGGER.warn("Unable to set size from property change event", e);
        }
    }

}
