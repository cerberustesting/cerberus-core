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
public class ExecutionThreadPool {

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

    public synchronized void setSize(Integer size) {
        int currentSize = getSize();
        if (size < currentSize) {
            executor.setCorePoolSize(size);
            executor.setMaximumPoolSize(size);
        } else if (size > currentSize) {
            executor.setMaximumPoolSize(size);
            executor.setCorePoolSize(size);
        }
    }

    public synchronized Integer getSize() {
        // Should be equal to executor.getMaximumPoolSize()
        return executor.getCorePoolSize();
    }

    public synchronized void submit(Runnable task) {
        executor.submit(task);
    }

    public synchronized void reset() {
        int currentSIze = getSize();
        stop();
        init(currentSIze);
    }

    public synchronized Integer getInExecution() {
        return executor.getActiveCount();
    }

    @PostConstruct
    private void init() {
        init(DEFAULT_THREAD_POOL_SIZE);
    }

    private void init(int poolSize) {
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

    @PreDestroy
    private void stop() {
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

}
