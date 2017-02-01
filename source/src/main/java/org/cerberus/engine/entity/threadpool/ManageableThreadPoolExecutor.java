package org.cerberus.engine.entity.threadpool;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link ThreadPoolExecutor} with additional capabilities as:
 * <ul>
 * <li>Getting the current queued and executing <em>{@link Runnable}</em> tasks (not {@link Future})</li>
 * <li>Pause and resume execution processing</li>
 * </ul>
 *
 * @author abourdon
 */
public class ManageableThreadPoolExecutor extends ThreadPoolExecutor {

    /**
     * Tasks state
     */
    public enum TaskState {
        QUEUED,
        EXECUTING
    }

    /**
     * The associated {@link Logger} to this class
     */
    private static final Logger LOGGER = Logger.getLogger(ManageableThreadPoolExecutor.class);

    /**
     * If this {@link ManageableThreadPoolExecutor} is currently paused or not
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

    /**
     * Set of currently queued and executing tasks
     */
    private Map<TaskState, List<? super Object>> tasks = new HashMap<TaskState, List<? super Object>>() {
        {
            put(TaskState.QUEUED, new ArrayList<>());
            put(TaskState.EXECUTING, new ArrayList<>());
        }
    };

    public ManageableThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override
    protected void beforeExecute(final Thread t, final Runnable r) {
        super.beforeExecute(t, r);
        pauseLock.lock();
        try {
            while (isPaused) {
                resumed.await();
            }
            synchronized (tasks) {
                tasks.get(TaskState.QUEUED).remove(r);
                tasks.get(TaskState.EXECUTING).add(r);
            }
        } catch (InterruptedException e) {
            LOGGER.warn("Execution interrupted", e);
            t.interrupt();
        } finally {
            pauseLock.unlock();
        }
    }

    @Override
    public Future<?> submit(final Runnable task) {
        final Future<?> futureTask = super.submit(task);
        synchronized (tasks) {
            tasks.get(TaskState.QUEUED).add(futureTask);
        }
        return futureTask;
    }

    @Override
    public <T> Future<T> submit(final Runnable task, final T result) {
        final Future<T> futureTask = super.submit(task, result);
        synchronized (tasks) {
            tasks.get(TaskState.QUEUED).add(task);
        }
        return futureTask;
    }

    @Override
    public <T> Future<T> submit(final Callable<T> task) {
        final Future<T> futureTask = super.submit(task);
        synchronized (tasks) {
            tasks.get(TaskState.QUEUED).add(futureTask);
        }
        return futureTask;
    }

    @Override
    protected void afterExecute(final Runnable r, final Throwable t) {
        super.afterExecute(r, t);
        synchronized (tasks) {
            tasks.get(TaskState.EXECUTING).remove(r);
        }
    }

    /**
     * Check if this {@link ManageableThreadPoolExecutor} is currently paused
     *
     * @return <code>true</code> if this {@link ManageableThreadPoolExecutor} is currently paused, <code>false</code> otherwise
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * Pause this {@link ManageableThreadPoolExecutor}
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
     * Resume this {@link ManageableThreadPoolExecutor}
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

    /**
     * Get the current tasks registered to this {@link ManageableThreadPoolExecutor}, if this {@link ManageableThreadPoolExecutor} is currently active (not shutdown}
     *
     * @return the current tasks registered to this {@link ManageableThreadPoolExecutor}
     */
    public Map<TaskState, List<? super Object>> getTasks() {
        // TODO make a copy to avoid direct reference manipulation (with weak references?)
        return tasks;
    }

    @Override
    public void shutdown() {
        tasks.clear();
        super.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        tasks.clear();
        return super.shutdownNow();
    }

}