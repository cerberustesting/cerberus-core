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

import org.cerberus.crud.entity.CountryEnvironmentParameters;

/**
 * Statistics about a {@link ExecutionThreadPool}
 * <p>
 * @author abourdon
 */
public class ExecutionThreadPoolStats {

    private CountryEnvironmentParameters.Key id;

    private long poolSize;

    private long inExecution;

    private long inQueue;

    private long remaining;

    private boolean stopped;

    private boolean paused;

    public ExecutionThreadPoolStats() {

    }

    public CountryEnvironmentParameters.Key getId() {
        return id;
    }

    public ExecutionThreadPoolStats setId(CountryEnvironmentParameters.Key id) {
        this.id = id;
        return this;
    }

    public long getPoolSize() {
        return poolSize;
    }

    public ExecutionThreadPoolStats setPoolSize(long poolSize) {
        this.poolSize = poolSize;
        return this;
    }

    public long getInExecution() {
        return inExecution;
    }

    public ExecutionThreadPoolStats setInExecution(long inExecution) {
        this.inExecution = inExecution;
        computeRemaining();
        return this;
    }

    public long getInQueue() {
        return inQueue;
    }

    public ExecutionThreadPoolStats setInQueue(long inQueue) {
        this.inQueue = inQueue;
        computeRemaining();
        return this;
    }

    public long getRemaining() {
        return remaining;
    }

    private void setRemaining(long remaining) {
        this.remaining = remaining;
    }

    private void computeRemaining() {
        setRemaining(getInQueue() - getInExecution());
    }

    public boolean isStopped() {
        return stopped;
    }

    public ExecutionThreadPoolStats setStopped(final boolean stopped) {
        this.stopped = stopped;
        return this;
    }

    public boolean isPaused() {
        return paused;
    }

    public ExecutionThreadPoolStats setPaused(final boolean paused) {
        this.paused = paused;
        return this;
    }
}

