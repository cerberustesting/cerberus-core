package org.cerberus.util.threadpool.jobdiscoverer;

/**
 * Extract the real task which was submitted to a {@link java.util.concurrent.ThreadPoolExecutor}
 * <p>
 * <p>
 * Initially inspired by the H. M. Kabutz's javaspecialists issue: http://www.javaspecialists.eu/archive/Issue228.html
 * <p>
 *
 * @author abourdon
 */
public interface JobDiscoverer {

    /**
     * Find the real task which was submitted to a {@link java.util.concurrent.ThreadPoolExecutor}
     *
     * @param task the task received from the {@link java.util.concurrent.ThreadPoolExecutor}
     * @return the original task orginally submitted to the {@link java.util.concurrent.ThreadPoolExecutor}
     */
    Object findRealTask(Object task);

}
