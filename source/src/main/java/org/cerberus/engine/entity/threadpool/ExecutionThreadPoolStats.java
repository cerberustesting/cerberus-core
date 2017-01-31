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

