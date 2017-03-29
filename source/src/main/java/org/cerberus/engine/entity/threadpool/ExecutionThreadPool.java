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
package org.cerberus.engine.entity.threadpool;

import org.cerberus.util.threadpool.jobdiscoverer.JobDiscovererManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A parameterized and size-settable execution thread pool.
 *
 * @param <T> the type of submitted tasks
 * @author bcivel
 * @author abourdon
 */
public class ExecutionThreadPool<T extends Runnable> {

    /**
     * The associated name of this {@link ExecutionThreadPool}
     */
    private String name;

    /**
     * The inner {@link ManageableThreadPoolExecutor} that control Test Cases executions.
     * <p>
     * When instanciated, this {@link ManageableThreadPoolExecutor} act the same as a {@link java.util.concurrent.Executors#newFixedThreadPool(int)},
     * but with the ability to tune its core pool size and be pausable/resumable
     */
    private ManageableThreadPoolExecutor executor;

    /**
     * The associated {@link Lock} to handle critical section when dealing with executor's pool size
     */
    private Lock executorPoolSizeLock = new ReentrantLock();

    /**
     * Create a new {@link ExecutionThreadPool} based on the given name and initial pool size
     *
     * @param name        the {@link ExecutionThreadPool} name
     * @param initialSize the initial pool size of this {@link ExecutionThreadPool}
     */
    public ExecutionThreadPool(String name, int initialSize) {
        setName(name);
        initExecutor(initialSize);
    }

    /**
     * Get the name of this {@link ExecutionThreadPool}.
     *
     * @return the name of this {@link ExecutionThreadPool}
     */
    public String getName() {
        return name;
    }

    /**
     * Set a name for this {@link ExecutionThreadPool}.
     *
     * @param name the name of this {@link ExecutionThreadPool}
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set the number of maximum simultaneous active threads this {@link ExecutionThreadPool} can have
     *
     * @param size the number of maximum simultaneous active threads to set
     */
    public void setSize(Integer size) {
        executorPoolSizeLock.lock();
        try {
            int currentSize = getPoolSize();
            if (size < currentSize) {
                executor.setCorePoolSize(size);
                executor.setMaximumPoolSize(size);
            } else if (size > currentSize) {
                executor.setMaximumPoolSize(size);
                executor.setCorePoolSize(size);
            }
        } finally {
            executorPoolSizeLock.unlock();
        }
    }

    /**
     * Get the number of {@link Runnable} tasks this {@link ExecutionThreadPool} can execute in the same time
     *
     * @return the number of {@link Runnable} tasks this {@link ExecutionThreadPool} can execute in the same time
     */
    public int getPoolSize() {
        executorPoolSizeLock.lock();
        try {
            // Should be equal to executor.getMaximumPoolSize()
            return executor.getCorePoolSize();
        } finally {
            executorPoolSizeLock.unlock();
        }
    }

    /**
     * Get the number of currently active threads
     *
     * @return the number of currently active threads
     */
    public int getInExecution() {
        return executor.getActiveCount();
    }

    /**
     * Get the approximate (not atomic) number of active and pending executions
     *
     * @return the approximate number of active and pending executions
     */
    public long getInQueue() {
        return executor.getTaskCount() - executor.getCompletedTaskCount();
    }

    /**
     * Submit a new task to execute from this {@link ExecutionThreadPool}.
     * <p>
     * If the maximum of simultaneous active threads is reached, then this task is kept in queue until a thread is released
     *
     * @param task the task to submit to this {@link ExecutionThreadPool}
     * @see #getPoolSize()
     */
    public void submit(T task) {
        executorPoolSizeLock.lock();
        try {
            executor.submit(task);
        } finally {
            executorPoolSizeLock.unlock();
        }
    }

    /**
     * Pause this {@link ExecutionThreadPool}
     */
    public void pause() {
        executor.pause();
    }

    /**
     * Resume this {@link ExecutionThreadPool}
     */
    public void resume() {
        executor.resume();
    }

    /**
     * Check if this {@link ExecutionThreadPool} is currently paused
     *
     * @return <code>true</code> if this {@link ExecutionThreadPool} is currently paused, <code>false</code> otherwise
     */
    public boolean isPaused() {
        return executor.isPaused();
    }

    /**
     * Stop this {@link ExecutionThreadPool} stopping its inner thread pool
     *
     * @return the list of remaining tasks of this {@link ExecutionThreadPool}, or <code>null</code> if this {@link ExecutionThreadPool} cannot be stopped
     */
    public List<T> stop() {
        return stopExecutor();
    }

    /**
     * Check if this {@link ExecutionThreadPool} is currently stopped, i.e., if its internal {@link ThreadPoolExecutor} is (or being to be) shutdown
     *
     * @return <code>true</code> if this {@link ExecutionThreadPool} is currently stopped, <code>false</code> otherwise
     */
    public boolean isStopped() {
        return executor.isShutdown();
    }

    /**
     * Get the currently queued and executing tasks registered to this {@link ExecutionThreadPool}
     *
     * @return the currently queued and executing tasks registered to this {@link ExecutionThreadPool}
     */
    public Map<ManageableThreadPoolExecutor.TaskState, List<T>> getTasks() {
        final Map<ManageableThreadPoolExecutor.TaskState, List<? super Object>> rawTasks = executor.getTasks();
        final Map<ManageableThreadPoolExecutor.TaskState, List<T>> formatedTasks = new HashMap<ManageableThreadPoolExecutor.TaskState, List<T>>() {
            {
                put(ManageableThreadPoolExecutor.TaskState.QUEUED, new ArrayList<T>());
                put(ManageableThreadPoolExecutor.TaskState.EXECUTING, new ArrayList<T>());
            }
        };
        for (final Map.Entry<ManageableThreadPoolExecutor.TaskState, List<? super Object>> rawTaskGroup : rawTasks.entrySet()) {
            for (final Object rawTask : rawTaskGroup.getValue()) {
                formatedTasks.get(rawTaskGroup.getKey()).add((T) JobDiscovererManager.getInstance().findRealTask(rawTask));
            }
        }
        return formatedTasks;
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Initialize the inner thread pool executor
     *
     * @param poolSize the thread pool size to set to the inner thread pool executor
     */
    private void initExecutor(int poolSize) {
        // The same as Executors#newFixedThreadPool(int) but with:
        // - access to the ThreadPoolExecutor API and so more controls than the ExecutorService one provided by Executors#newFixedThreadPool(int)
        // - access to the ManageableThreadPoolExecutor API and so be able to pause/resume executions
        executor = new ManageableThreadPoolExecutor(
                poolSize,
                poolSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>()
        );
    }

    /**
     * Shutdown the inner thread pool by sopping any of its pending tasks
     *
     * @return the list of pending taks, or <code>null</code> if the inner thread pool cannot be stopped
     */
    private List<T> stopExecutor() {
        if (!executor.isShutdown()) {
            // First, shutdown the inner thread pool
            final List<Runnable> remainingTasks = executor.shutdownNow();

            // Then, release waiting threads to this now shutdown ExecutionThreadPool
            resume();

            // Finally retrieve the original tasks submitted to #submit(Runnable) from the remaining
            final List<T> originalRemainingTasks = new ArrayList<>(remainingTasks.size());
            for (Runnable task : remainingTasks) {
                originalRemainingTasks.add((T) JobDiscovererManager.getInstance().findRealTask(task));
            }
            return originalRemainingTasks;
        }
        return null;
    }

}
