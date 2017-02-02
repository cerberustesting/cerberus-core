package org.cerberus.engine.threadpool;

import org.cerberus.crud.entity.CountryEnvironmentParameters;
import org.cerberus.engine.entity.threadpool.ExecutionThreadPool;
import org.cerberus.engine.entity.threadpool.ExecutionThreadPoolStats;
import org.cerberus.engine.entity.threadpool.ExecutionWorkerThread;
import org.cerberus.engine.entity.threadpool.ManageableThreadPoolExecutor;
import org.cerberus.exception.CerberusException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Manage a set of {@link ExecutionThreadPool} to support multiple Cerberus test case executions
 *
 * @author abourdon
 */
public interface IExecutionThreadPoolService {

    /**
     * Search any {@link org.cerberus.crud.entity.TestCaseExecutionInQueue} which are currently waiting for execution and trigger their executions
     *
     * @throws CerberusException if an error occurred during search or trigger process
     */
    void executeNextInQueue() throws CerberusException;

    /**
     * Search at most #limit {@link org.cerberus.crud.entity.TestCaseExecutionInQueue} which are currently waiting for execution and trigger their executions
     *
     * @param limit the limit size of {@link org.cerberus.crud.entity.TestCaseExecutionInQueue} in waiting state to execute
     * @throws CerberusException if an error occurred during search or trigger process
     */
    void executeNextInQueue(int limit) throws CerberusException;

    /**
     * Get an quasi-accurate (not atomic) statistics of the current execution pools
     *
     * @return a collection of {@link ExecutionThreadPoolStats}
     */
    Collection<ExecutionThreadPoolStats> getStats();

    /**
     * Get the currently queued and executing task registered to the {@link ExecutionThreadPool} identified by the given {@link org.cerberus.crud.entity.CountryEnvironmentParameters.Key}
     *
     * @param key the {@link org.cerberus.crud.entity.CountryEnvironmentParameters.Key} to identify the {@link ExecutionThreadPool} from which getting tasks
     * @return the currently queued and executing task registered to the {@link ExecutionThreadPool} identified by the given {@link org.cerberus.crud.entity.CountryEnvironmentParameters.Key}
     */
    Map<ManageableThreadPoolExecutor.TaskState, List<ExecutionWorkerThread>> getTasks(CountryEnvironmentParameters.Key key);

    /**
     * Pause the {@link ExecutionThreadPool} identified by the given {@link org.cerberus.crud.entity.CountryEnvironmentParameters.Key}
     *
     * @param key the {@link org.cerberus.crud.entity.CountryEnvironmentParameters.Key} to identify the {@link ExecutionThreadPool} to pause
     */
    void pauseExecutionThreadPool(CountryEnvironmentParameters.Key key);

    /**
     * Resume the {@link ExecutionThreadPool} identified by the given {@link org.cerberus.crud.entity.CountryEnvironmentParameters.Key}
     *
     * @param key the {@link org.cerberus.crud.entity.CountryEnvironmentParameters.Key} to identify the {@link ExecutionThreadPool} to resume
     */
    void resumeExecutionThreadPool(CountryEnvironmentParameters.Key key);

    /**
     * Remove the {@link ExecutionThreadPool} identified by the given {@link org.cerberus.crud.entity.CountryEnvironmentParameters.Key}
     *
     * @param key the {@link org.cerberus.crud.entity.CountryEnvironmentParameters.Key} to identify the {@link ExecutionThreadPool} to remove
     */
    void removeExecutionThreadPool(CountryEnvironmentParameters.Key key);

}
