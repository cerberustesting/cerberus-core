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
package org.cerberus.engine.threadpool;

import org.apache.log4j.Logger;
import org.cerberus.crud.entity.CountryEnvironmentParameters;
import org.cerberus.crud.service.ICountryEnvironmentParametersService;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.crud.entity.TestCaseExecutionInQueue;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestCaseExecutionInQueueService;
import org.cerberus.engine.entity.ExecutionThreadPool;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.servlet.zzpublic.RunTestCase;
import org.cerberus.util.ParamRequestMaker;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.observe.Observer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bcivel
 */
@Service
public class ExecutionThreadPoolService implements Observer<CountryEnvironmentParameters.Key, CountryEnvironmentParameters> {

    public static class ExecutionThreadPoolStats {

        private String name;

        private long poolSize;

        private long inExecution;

        private long inQueue;

        private long remaining;

        public String getName() {
            return name;
        }

        /* default */ ExecutionThreadPoolStats setName(String name) {
            this.name = name;
            return this;
        }

        public long getPoolSize() {
            return poolSize;
        }

        /* default */ ExecutionThreadPoolStats setPoolSize(long poolSize) {
            this.poolSize = poolSize;
            return this;
        }

        public long getInExecution() {
            return inExecution;
        }

        /* default */ ExecutionThreadPoolStats setInExecution(long inExecution) {
            this.inExecution = inExecution;
            computeRemaining();
            return this;
        }

        public long getInQueue() {
            return inQueue;
        }

        /* default */ ExecutionThreadPoolStats setInQueue(long inQueue) {
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

    }

    private static final Logger LOG = Logger.getLogger(ExecutionThreadPoolService.class);

    private static ParamRequestMaker makeParamRequest(TestCaseExecutionInQueue lastInQueue) {
        ParamRequestMaker paramRequestMaker = new ParamRequestMaker();
        paramRequestMaker.addParam(RunTestCase.PARAMETER_TEST, lastInQueue.getTest());
        paramRequestMaker.addParam(RunTestCase.PARAMETER_TEST_CASE, lastInQueue.getTestCase());
        paramRequestMaker.addParam(RunTestCase.PARAMETER_COUNTRY, lastInQueue.getCountry());
        paramRequestMaker.addParam(RunTestCase.PARAMETER_ENVIRONMENT, lastInQueue.getEnvironment());
        paramRequestMaker.addParam(RunTestCase.PARAMETER_ROBOT, lastInQueue.getRobot());
        paramRequestMaker.addParam(RunTestCase.PARAMETER_ROBOT_IP, lastInQueue.getRobotIP());
        paramRequestMaker.addParam(RunTestCase.PARAMETER_ROBOT_PORT, lastInQueue.getRobotPort());
        paramRequestMaker.addParam(RunTestCase.PARAMETER_BROWSER, lastInQueue.getBrowser());
        paramRequestMaker.addParam(RunTestCase.PARAMETER_BROWSER_VERSION, lastInQueue.getBrowserVersion());
        paramRequestMaker.addParam(RunTestCase.PARAMETER_PLATFORM, lastInQueue.getPlatform());
        paramRequestMaker.addParam(RunTestCase.PARAMETER_MANUAL_URL, lastInQueue.isManualURL() ? ParameterParserUtil.DEFAULT_BOOLEAN_TRUE_VALUE : null);
        if (lastInQueue.isManualURL()) {
            paramRequestMaker.addParam(RunTestCase.PARAMETER_MANUAL_HOST, lastInQueue.getManualHost());
            paramRequestMaker.addParam(RunTestCase.PARAMETER_MANUAL_CONTEXT_ROOT, lastInQueue.getManualContextRoot());
            paramRequestMaker.addParam(RunTestCase.PARAMETER_MANUAL_LOGIN_RELATIVE_URL, lastInQueue.getManualLoginRelativeURL());
            paramRequestMaker.addParam(RunTestCase.PARAMETER_MANUAL_ENV_DATA, lastInQueue.getManualEnvData());
        }
        paramRequestMaker.addParam(RunTestCase.PARAMETER_TAG, lastInQueue.getTag());
        paramRequestMaker.addParam(RunTestCase.PARAMETER_OUTPUT_FORMAT, lastInQueue.getOutputFormat());
        paramRequestMaker.addParam(RunTestCase.PARAMETER_SCREENSHOT, Integer.toString(lastInQueue.getScreenshot()));
        paramRequestMaker.addParam(RunTestCase.PARAMETER_VERBOSE, Integer.toString(lastInQueue.getVerbose()));
        paramRequestMaker.addParam(RunTestCase.PARAMETER_TIMEOUT, lastInQueue.getTimeout());
        // Always synchronous in order to respect maximum thread pool size
        paramRequestMaker.addParam(RunTestCase.PARAMETER_SYNCHRONEOUS, ParameterParserUtil.DEFAULT_BOOLEAN_TRUE_VALUE);
        paramRequestMaker.addParam(RunTestCase.PARAMETER_PAGE_SOURCE, Integer.toString(lastInQueue.getPageSource()));
        paramRequestMaker.addParam(RunTestCase.PARAMETER_SELENIUM_LOG, Integer.toString(lastInQueue.getSeleniumLog()));
        paramRequestMaker.addParam(RunTestCase.PARAMETER_EXECUTION_QUEUE_ID, Long.toString(lastInQueue.getId()));
        paramRequestMaker.addParam(RunTestCase.PARAMETER_NUMBER_OF_RETRIES, Long.toString(lastInQueue.getRetries()));
        return paramRequestMaker;
    }

    @Autowired
    private ITestCaseExecutionInQueueService tceiqService;

    @Autowired
    private IParameterService parameterService;

    @Autowired
    private ICountryEnvironmentParametersService countryEnvironmentParametersService;

    private Map<CountryEnvironmentParameters.Key, ExecutionThreadPool> executionPools;

    public void searchExecutionInQueueTableAndTriggerExecution() throws CerberusException, UnsupportedEncodingException, InterruptedException {
        List<TestCaseExecutionInQueue> executionsInQueue = tceiqService.toQueued();
        for (TestCaseExecutionInQueue executionInQueue : executionsInQueue) {
            try {
                execute(executionInQueue);
            } catch (CerberusException e) {
                LOG.warn("Unable to execute " + executionInQueue + " due to " + e.getMessageError().getDescription(), e);
                tceiqService.toError(executionInQueue.getId(), e.getMessageError().getDescription());
            }
        }
    }

    /**
     * Get an quasi-accurate statistics of the current execution pools
     *
     * @return a collection of {@link ExecutionThreadPoolStats}
     */
    public Collection<ExecutionThreadPoolStats> getStats() {
        final Collection<ExecutionThreadPoolStats> stats = new ArrayList<>();
        for (ExecutionThreadPool pool : executionPools.values()) {
            // Quasi-accurate statistics
            stats.add(new ExecutionThreadPoolStats()
                    .setName(pool.getName())
                    .setPoolSize(pool.getPoolSize())
                    .setInQueue(pool.getInQueue())
                    .setInExecution(pool.getInExecution())
            );
        }
        return stats;
    }

    @Override
    public void observeCreate(CountryEnvironmentParameters.Key key, CountryEnvironmentParameters countryEnvironmentParameters) {
        // Nothing to do
    }

    @Override
    public void observeUpdate(CountryEnvironmentParameters.Key key, CountryEnvironmentParameters countryEnvironmentParameters) {
        ExecutionThreadPool associatedExecutionPool = getExecutionPool(key);
        if (associatedExecutionPool != null) {
            associatedExecutionPool.setSize(countryEnvironmentParameters.getPoolSize());
        }
    }

    @Override
    public void observeDelete(CountryEnvironmentParameters.Key key, CountryEnvironmentParameters countryEnvironmentParameters) {
        removeExecutionPool(key);
    }

    @PostConstruct
    private void init() {
        initExecutionPools();
    }

    @PreDestroy
    private void stop() {
        stopRegistration();
        stopExecutionPools();
    }


    private void initExecutionPools() {
        executionPools = new HashMap<>();
    }

    private void stopExecutionPools() {
        if (executionPools == null) {
            return;
        }
        for (ExecutionThreadPool executionPool : executionPools.values()) {
            LOG.info("Stopping execution pool " + executionPool.getName());
            executionPool.stop();
        }
    }

    private void stopRegistration() {
        countryEnvironmentParametersService.unregister(this);
    }

    private void execute(TestCaseExecutionInQueue toExecute) throws CerberusException {
        try {
            ExecutionThreadPool executionPool = getOrCreateExecutionPool(getKey(toExecute));
            ExecutionWorkerThread execution = new ExecutionWorkerThread(getExecutionUrl(toExecute), toExecute.getTag());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Request to execute " + execution + " from execution pool " + executionPool);
            }
            executionPool.submit(execution);
        } catch (Exception e) {
            String message = "Unable to execute " + toExecute + " due to " + e.getMessage();
            LOG.warn(message, e);
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.GENERIC_ERROR).resolveDescription("REASON", message));
        }
    }

    private String getExecutionUrl(TestCaseExecutionInQueue toExecute) throws CerberusException{
        ParamRequestMaker paramRequestMaker = makeParamRequest(toExecute);
        String executionParameters = paramRequestMaker.mkString().replace(" ", "+");
        return parameterService.findParameterByKey("cerberus_url", "").getValue() + "/RunTestCase?" + executionParameters;
    }

    private ExecutionThreadPool getOrCreateExecutionPool(CountryEnvironmentParameters.Key key) {
        ExecutionThreadPool executionPool = executionPools.get(key);
        if (executionPool == null) {
            synchronized (executionPools) {
                executionPool = executionPools.get(key);
                if (executionPool == null) {
                    executionPool = new ExecutionThreadPool(key, getPoolSize(key));
                    executionPools.put(key, executionPool);
                    registerTo(key);
                }
            }
        }
        return executionPool;
    }

    private ExecutionThreadPool getExecutionPool(CountryEnvironmentParameters.Key key) {
        synchronized (executionPools) {
            return executionPools.get(key);
        }
    }

    private void removeExecutionPool(CountryEnvironmentParameters.Key key) {
        synchronized (executionPools) {
            ExecutionThreadPool pool = executionPools.get(key);
            if (pool != null) {
                pool.stop();
                executionPools.remove(key);
            }
        }
    }

    private CountryEnvironmentParameters.Key getKey(TestCaseExecutionInQueue inQueue) throws CerberusException {
        TestCaseExecutionInQueue completeInQueue = tceiqService.findByKeyWithDependencies(inQueue.getId());
        return new CountryEnvironmentParameters.Key(
                completeInQueue.getApplicationObj().getSystem(),
                completeInQueue.getApplicationObj().getApplication(),
                completeInQueue.getCountry(),
                completeInQueue.getEnvironment()
        );
    }

    private int getPoolSize(CountryEnvironmentParameters.Key key) {
        AnswerItem<Integer> poolSize = countryEnvironmentParametersService.readPoolSizeByKey(key.getSystem(), key.getCountry(), key.getEnvironment(), key.getApplication());
        if (MessageEventEnum.DATA_OPERATION_OK.equals(poolSize.getResultMessage().getSource())) {
            return poolSize.getItem();
        } else {
            LOG.warn("Unable to get pool size from " + key + ". Get default");
            return countryEnvironmentParametersService.defaultPoolSize();
        }
    }

    private void registerTo(CountryEnvironmentParameters.Key key) {
        countryEnvironmentParametersService.register(key, this);
    }


}
