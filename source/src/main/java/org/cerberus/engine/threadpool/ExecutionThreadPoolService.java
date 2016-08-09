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
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.crud.entity.Parameter;
import org.cerberus.crud.entity.TestCaseExecutionInQueue;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestCaseExecutionInQueueService;
import org.cerberus.engine.entity.ExecutionThreadPool;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.servlet.zzpublic.RunTestCase;
import org.cerberus.util.ParamRequestMaker;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author bcivel
 */
@Service
public class ExecutionThreadPoolService implements IParameterService.ParameterAware {

    private static final String THREAD_POOL_SIZE_CONFIGURATION_KEY = "cerberus_execution_threadpool_size";

    private static final Logger LOG = Logger.getLogger(ExecutionThreadPoolService.class);

    @Autowired
    ITestCaseExecutionInQueueService tceiqService;
    @Autowired
    ExecutionThreadPool threadPool;
    @Autowired
    IParameterService parameterService;

    public void putExecutionInQueue(String url, String tag) throws CerberusException {
        ExecutionWorkerThread task = new ExecutionWorkerThread();
        task.setExecutionUrl(url);
        task.setTag(tag);
        try {
            threadPool.submit(task);
        } catch (Exception e) {
            String message = "Unable to submit new task " + task;
            LOG.warn(message, e);
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.GENERIC_ERROR).resolveDescription("REASON", message));
        }
    }

    public void searchExecutionInQueueTableAndTriggerExecution() throws CerberusException, UnsupportedEncodingException, InterruptedException {

        try {
            /**
             * Find all testCase in Queue not Procedeed
             */
            List<TestCaseExecutionInQueue> tceiqList = tceiqService.findAllNotProcedeed();
            if (null != tceiqList && !tceiqList.isEmpty()) {
                /**
                 * Generate the URL for the execution
                 */
                String host = parameterService.findParameterByKey("cerberus_url", "").getValue();
                host += "/RunTestCase?";
                for (TestCaseExecutionInQueue tceiq : tceiqList) {
                    ParamRequestMaker paramRequestMaker = makeParamRequestfromLastInQueue(tceiq);
                    String uri = paramRequestMaker.mkString().replace(" ", "+");
                    String query = host + uri;
                    this.putExecutionInQueue(query, tceiq.getTag());
                }
            }

        } catch (CerberusException ex) {
            LOG.info(ex.toString());
        }

    }

    public static ParamRequestMaker makeParamRequestfromLastInQueue(TestCaseExecutionInQueue lastInQueue) {
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
        paramRequestMaker.addParam(RunTestCase.PARAMETER_TIMEOUT, Long.toString(lastInQueue.getTimeout()));
        paramRequestMaker.addParam(RunTestCase.PARAMETER_SYNCHRONEOUS, lastInQueue.isSynchroneous() ? ParameterParserUtil.DEFAULT_BOOLEAN_TRUE_VALUE
                : ParameterParserUtil.DEFAULT_BOOLEAN_FALSE_VALUE);
        paramRequestMaker.addParam(RunTestCase.PARAMETER_PAGE_SOURCE, Integer.toString(lastInQueue.getPageSource()));
        paramRequestMaker.addParam(RunTestCase.PARAMETER_SELENIUM_LOG, Integer.toString(lastInQueue.getSeleniumLog()));
        paramRequestMaker.addParam(RunTestCase.PARAMETER_EXECUTION_QUEUE_ID, Long.toString(lastInQueue.getId()));
        paramRequestMaker.addParam(RunTestCase.PARAMETER_NUMBER_OF_RETRIES, Long.toString(lastInQueue.getRetries()));
        return paramRequestMaker;
    }

    @Override
    public void parameterChanged(Parameter parameter) {
        try {
            threadPool.setSize(Integer.valueOf(parameter.getValue()));
        } catch (Exception e) {
            LOG.warn("Unable to set size from property change event", e);
        }
    }

    @PostConstruct
    private void init() {
        try {
            threadPool.setSize(Integer.valueOf(parameterService.findParameterByKey(THREAD_POOL_SIZE_CONFIGURATION_KEY, "").getValue()));
            parameterService.register(THREAD_POOL_SIZE_CONFIGURATION_KEY, this);
        } catch (CerberusException e) {
            LOG.warn("Unable to initialize thread pool", e);
        }
    }

    @PreDestroy
    private void destroy() {
        parameterService.unregister(THREAD_POOL_SIZE_CONFIGURATION_KEY, this);
    }

}
