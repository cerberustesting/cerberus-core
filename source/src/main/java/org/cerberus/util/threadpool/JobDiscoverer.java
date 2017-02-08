package org.cerberus.util.threadpool;

/**
 * Created by aurel on 31/01/2017.
 */

import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Helper to extract the real task which was submitted to a {@link java.util.concurrent.ThreadPoolExecutor}
 * <p>
 * From the H. M. Kabutz's javaspecialists issue: http://www.javaspecialists.eu/archive/Issue228.html
 * <p>
 * Edit:
 * <ul>
 * <li>change {@link #findRealTask} signature to accept {@link Object} instead of {@link Runnable}</li>
 * <li>be compatible with old {@link FutureTask} implementations before change due to http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7132378</li>
 * <li>reinforce exception catching during class initialization</li>
 * <li>add logger to read exception message easier</li>
 * </ul>
 *
 * @author Heinz M. Kabutz
 * @author abourdon
 */
public class JobDiscoverer {
    private final static Field callableInFutureTask;
    private static final Class<? extends Callable> adapterClass;
    private static final Field runnableInAdapter;

    private static final Logger LOGGER = Logger.getLogger(JobDiscoverer.class);

    static {
        try {
            Field callableInFutureTaskCandidate;
            try {
                callableInFutureTaskCandidate =
                        FutureTask.class.getDeclaredField("callable");
            } catch (NoSuchFieldException e) {
                callableInFutureTaskCandidate = FutureTask.class.getDeclaredField("sync").getType().getDeclaredField("callable");
            }
            callableInFutureTask = callableInFutureTaskCandidate;
            callableInFutureTask.setAccessible(true);
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

    public static Object findRealTask(Object task) {
        if (task instanceof FutureTask) {
            try {
                Object callable = callableInFutureTask.get(task);
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
