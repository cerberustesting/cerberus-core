package org.cerberus.util.threadpool.jobdiscoverer.impl;

import org.apache.log4j.Logger;
import org.cerberus.util.threadpool.jobdiscoverer.JobDiscoverer;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Extract the real task which was submitted to a {@link java.util.concurrent.ThreadPoolExecutor} when a JDK version prior of this issue fixing: http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7132378
 * <p>
 * Initially inspired by the H. M. Kabutz's javaspecialists issue: http://www.javaspecialists.eu/archive/Issue228.html
 * <p>
 *
 * @author abourdon
 */
public class AbstractQueuedSynchronizerFutureTaskImplementationJobDiscoverer implements JobDiscoverer {
    private static final Field syncInFutureTask;
    private static final Field callableInFutureTaskSync;
    private static final Class<? extends Callable> adapterClass;
    private static final Field runnableInAdapter;

    private static final Logger LOGGER = Logger.getLogger(AbstractQueuedSynchronizerFutureTaskImplementationJobDiscoverer.class);

    static {
        try {
            syncInFutureTask = FutureTask.class.getDeclaredField("sync");
            syncInFutureTask.setAccessible(true);
            callableInFutureTaskSync = syncInFutureTask.getType().getDeclaredField("callable");
            callableInFutureTaskSync.setAccessible(true);
            adapterClass = Executors.callable(new Runnable() {
                public void run() {
                }
            }).getClass();
            runnableInAdapter =
                    adapterClass.getDeclaredField("task");
            runnableInAdapter.setAccessible(true);
        } catch (Exception e) {
            LOGGER.error("Unable to initialize JobDiscover due to " + e.getMessage(), e);
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public Object findRealTask(Object task) {
        if (task instanceof FutureTask) {
            try {
                Object sync = syncInFutureTask.get(task);
                Object callable = callableInFutureTaskSync.get(sync);
                if (adapterClass.isInstance(callable)) {
                    return runnableInAdapter.get(callable);
                } else {
                    return callable;
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        throw new ClassCastException("Not a FutureTask");
    }
}
