package org.cerberus.engine.entity.threadpool;

import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link ThreadPoolExecutor} with pause/resume capability
 * <p>
 *
 * @author abourdon
 */
public class PausableThreadPoolExecutor extends ThreadPoolExecutor {

    /**
     * The associated {@link Logger} to this class
     */
    private static final Logger LOGGER = Logger.getLogger(PausableThreadPoolExecutor.class);

    /**
     * If this {@link PausableThreadPoolExecutor} is currently paused or not
     */
    private boolean isPaused = false;

    /**
     * The associated {@link ReentrantLock} to the pause/resume process
     */
    private ReentrantLock pauseLock = new ReentrantLock();

    /**
     * The associated {@link Condition} to the {@link #pauseLock}
     */
    private Condition resumed = pauseLock.newCondition();

    public PausableThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public PausableThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public PausableThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public PausableThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final ThreadFactory threadFactory, final RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    protected void beforeExecute(final Thread t, final Runnable r) {
        super.beforeExecute(t, r);
        pauseLock.lock();
        try {
            while (isPaused) {
                resumed.await();
            }
        } catch (InterruptedException e) {
            LOGGER.warn("Execution interrupted", e);
            t.interrupt();
        } finally {
            pauseLock.unlock();
        }
    }

    /**
     * Check if this {@link PausableThreadPoolExecutor} is currently paused
     *
     * @return <code>true</code> if this {@link PausableThreadPoolExecutor} is currently paused, <code>false</code> otherwise
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * Pause this {@link PausableThreadPoolExecutor}
     */
    public void pause() {
        pauseLock.lock();
        try {
            isPaused = true;
        } finally {
            pauseLock.unlock();
        }
    }

    /**
     * Resume this {@link PausableThreadPoolExecutor}
     */
    public void resume() {
        pauseLock.lock();
        try {
            isPaused = false;
            resumed.signalAll();
        } finally {
            pauseLock.unlock();
        }
    }

}
