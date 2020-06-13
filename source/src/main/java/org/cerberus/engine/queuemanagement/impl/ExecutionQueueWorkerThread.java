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
package org.cerberus.engine.queuemanagement.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.cerberus.crud.entity.TestCaseExecutionQueue;
import org.cerberus.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.crud.service.ITestCaseExecutionQueueDepService;
import org.cerberus.engine.execution.IRetriesService;
import org.cerberus.exception.CerberusException;
import org.cerberus.servlet.zzpublic.RunTestCaseV001;
import org.cerberus.util.ParamRequestMaker;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;

/**
 *
 * @author bcivel
 */
public class ExecutionQueueWorkerThread implements Runnable {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(ExecutionQueueWorkerThread.class);

    private ITestCaseExecutionQueueService queueService;
    private IRetriesService retriesService;
    private ITestCaseExecutionQueueDepService queueDepService;

    private ExecutionQueueThreadPool execThreadPool;

    private long queueId;
    private String robotExecutor;
    private String selectedRobotHost;
    private String selectedRobotExtHost;
    private TestCaseExecutionQueue toExecute;

    private String cerberusExecutionUrl;
    private int toExecuteTimeout;

    private Future<?> future;
    private static final Pattern EXECUTION_ID_FROM_ANSWER_PATTERN = Pattern.compile("^id = (\\d+)$", Pattern.MULTILINE);
    private static final Pattern RETURN_CODE_DESCRIPTION_FROM_ANSWER_PATTERN = Pattern.compile("^controlMessage = (.*)$", Pattern.MULTILINE);

    public static String PARAMETER_OUTPUT_FORMAT_VALUE = "verbose-txt";

    private ParamRequestMaker makeParamRequest() {
        ParamRequestMaker paramRequestMaker = new ParamRequestMaker();
        try {
            paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_TEST, URLEncoder.encode(getToExecute().getTest(), "UTF-8"));
            paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_TEST_CASE, URLEncoder.encode(getToExecute().getTestCase(), "UTF-8"));
            paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_COUNTRY, getToExecute().getCountry());
            paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_ENVIRONMENT, getToExecute().getEnvironment());
            paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_ROBOT, getToExecute().getRobot());
            paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_ROBOTEXECUTOR, getRobotExecutor());
            paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_ROBOT_IP, getToExecute().getRobotIP());
            paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_ROBOT_PORT, getToExecute().getRobotPort());
            paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_BROWSER, getToExecute().getBrowser());
            paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_BROWSER_VERSION, getToExecute().getBrowserVersion());
            paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_PLATFORM, getToExecute().getPlatform());
            paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_SCREEN_SIZE, getToExecute().getScreenSize());

            if (getToExecute().getManualURL() >= 1) { // 1 (Activate) or 2 (Override)
                if (getToExecute().getManualURL() == 1) { // set manual url only if 1. if 2, manual url == false and, we ovveride host, contextroot, login and env data if attributs available
                    paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_MANUAL_URL, ParameterParserUtil.DEFAULT_BOOLEAN_TRUE_VALUE);
                }

                addIfNotNullOrEmpty(paramRequestMaker, RunTestCaseV001.PARAMETER_MANUAL_HOST, getToExecute().getManualHost(), true);
                addIfNotNullOrEmpty(paramRequestMaker, RunTestCaseV001.PARAMETER_MANUAL_CONTEXT_ROOT, getToExecute().getManualContextRoot(), true);
                addIfNotNullOrEmpty(paramRequestMaker, RunTestCaseV001.PARAMETER_MANUAL_LOGIN_RELATIVE_URL, getToExecute().getManualLoginRelativeURL(), true);
                addIfNotNullOrEmpty(paramRequestMaker, RunTestCaseV001.PARAMETER_MANUAL_ENV_DATA, getToExecute().getManualEnvData(), false);
            }

            paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_TAG, URLEncoder.encode(getToExecute().getTag(), "UTF-8"));
            paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_SCREENSHOT, Integer.toString(getToExecute().getScreenshot()));
            paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_VERBOSE, Integer.toString(getToExecute().getVerbose()));
            paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_TIMEOUT, getToExecute().getTimeout());
            paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_PAGE_SOURCE, Integer.toString(getToExecute().getPageSource()));
            paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_SELENIUM_LOG, Integer.toString(getToExecute().getSeleniumLog()));
            paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_EXECUTION_QUEUE_ID, Long.toString(getToExecute().getId()));
            paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_NUMBER_OF_RETRIES, Long.toString(getToExecute().getRetries()));
            paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_EXECUTOR, getToExecute().getUsrCreated());
            paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_MANUAL_EXECUTION, getToExecute().getManualExecution());
            paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_OUTPUT_FORMAT, PARAMETER_OUTPUT_FORMAT_VALUE);
            paramRequestMaker.addParam(RunTestCaseV001.PARAMETER_SYNCHRONEOUS, ParameterParserUtil.DEFAULT_BOOLEAN_TRUE_VALUE);

        } catch (UnsupportedEncodingException ex) {
            LOG.error("Error when encoding string in URL : ", ex);
        }
        return paramRequestMaker;
    }

    /**
     * The associated {@link RuntimeException} for any errors during the run
     * process
     */
    public static class RunQueueProcessException extends RuntimeException {

        public RunQueueProcessException(String message) {
            super(message);
        }

        public RunQueueProcessException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    public ITestCaseExecutionQueueDepService getQueueDepService() {
        return queueDepService;
    }

    public void setQueueDepService(ITestCaseExecutionQueueDepService queueDepService) {
        this.queueDepService = queueDepService;
    }

    public String getRobotExecutor() {
        return robotExecutor;
    }

    public void setRobotExecutor(String robotExecutor) {
        this.robotExecutor = robotExecutor;
    }

    public String getSelectedRobotHost() {
        return selectedRobotHost;
    }

    public void setSelectedRobotHost(String selectedRobotHost) {
        this.selectedRobotHost = selectedRobotHost;
    }

    public String getSelectedRobotExtHost() {
        return selectedRobotExtHost;
    }

    public void setSelectedRobotExtHost(String selectedRobotExtHost) {
        this.selectedRobotExtHost = selectedRobotExtHost;
    }

    public TestCaseExecutionQueue getToExecute() {
        return toExecute;
    }

    private void setToExecute(TestCaseExecutionQueue toExecute) {
        this.toExecute = toExecute;
    }

    public ITestCaseExecutionQueueService getQueueService() {
        return queueService;
    }

    public void setQueueService(ITestCaseExecutionQueueService queueService) {
        this.queueService = queueService;
    }

    public void setRetriesService(IRetriesService retriesService) {
        this.retriesService = retriesService;
    }

    public void setCerberusExecutionUrl(String url) {
        this.cerberusExecutionUrl = url;
    }

    public void setQueueId(long queueId) {
        this.queueId = queueId;
    }

    public void setExecThreadPool(ExecutionQueueThreadPool etp) {
        this.execThreadPool = etp;
    }

    public void setFuture(Future<?> future) {
        this.future = future;
    }

    public int getToExecuteTimeout() {
        return toExecuteTimeout;
    }

    public void setToExecuteTimeout(int toExecuteTimeout) {
        this.toExecuteTimeout = toExecuteTimeout;
    }

    @Override
    public void run() {
        try {
            LOG.debug("Start to execute : " + queueId + " with RobotHost : " + selectedRobotHost+ " with RobotExtensionHost : " + selectedRobotExtHost);

            // Flag the queue entry to STARTING
            queueService.updateToStarting(queueId, selectedRobotHost, selectedRobotExtHost);

            LOG.debug("Get queue exe to execute : " + queueId);
            // Getting the queue full object.
            setToExecute(queueService.convert(queueService.readByKey(queueId, false)));

            StringBuilder url = new StringBuilder();
            url.append(cerberusExecutionUrl);
            url.append(RunTestCaseV001.SERVLET_URL);
            url.append("?");
            url.append(makeParamRequest().mkString().replace(" ", "+"));

            LOG.debug("Make http call : " + queueId);
            // Make the http call and parse the output.
            runParseAnswer(runExecution(url), cerberusExecutionUrl + RunTestCaseV001.SERVLET_URL, url.toString());

        } catch (Exception e) {
            LOG.warn("Execution in queue " + queueId + " has finished with error");
            try {

                queueService.updateToError(queueId, e.getMessage());
                queueDepService.manageDependenciesEndOfQueueExecution(queueId);

            } catch (CerberusException again) {
                LOG.error("Unable to mark execution in queue " + queueId + " as in error", again);
            }

        }
    }

    /**
     * Request execution of the inner {@link TestCaseExecutionQueue} to the
     * {@link RunTestCase} servlet
     *
     * @return the execution answer from the {@link RunTestCase} servlet
     * @throws RunQueueProcessException if an error occurred during request
     * execution
     * @see #run()
     */
    private String runExecution(StringBuilder url) {
        try {

            LOG.debug("Trigger Execution to URL : " + url.toString());
            LOG.debug("Trigger Execution with TimeOut : " + toExecuteTimeout);

            CloseableHttpClient httpclient = HttpClientBuilder.create().disableAutomaticRetries().build();
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(toExecuteTimeout)
                    .setConnectionRequestTimeout(toExecuteTimeout)
                    .setSocketTimeout(toExecuteTimeout)
                    .build();

            HttpGet httpGet = new HttpGet(url.toString());
            httpGet.setConfig(requestConfig);
            CloseableHttpResponse response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String responseContent = EntityUtils.toString(entity);
            return responseContent;

        } catch (Exception e) {
            final StringBuilder errorMessage = new StringBuilder("An unexpected error occurred during test case execution: ");
            if (e instanceof HttpResponseException) {
                errorMessage.append(String.format("%d (%s)", ((HttpResponseException) e).getStatusCode(), e.getMessage()));
            } else {
                errorMessage.append(e.getMessage());
                errorMessage.append(". Check server logs");
            }
            LOG.error(errorMessage.toString(), e);
            throw new RunQueueProcessException(errorMessage.toString(), e);
        }
    }

    /**
     * Parse the answer given by the {@link RunTestCase}
     * <p>
     * @param answer the {@link RunTestCase}'s answer
     * @throws RunQueueProcessException if an error occurred if execution was on
     * failure or if answer cannot be parsed
     * @see #run()
     */
    private void runParseAnswer(String answer, String cerberusUrl, String cerberusFullUrl) {
        // Check answer format
        Matcher matcher = EXECUTION_ID_FROM_ANSWER_PATTERN.matcher(answer);
        if (!matcher.find()) {
            LOG.error("Bad answer format (could not find 'RunID = '). URL Called: " + cerberusFullUrl);
            LOG.error("Bad answer format (could not find 'RunID = '). Answer: " + answer);
            throw new RunQueueProcessException("Error occured when calling the service to run the testcase. Service answer did not have the expected format (missing 'RunID = '). Probably due to bad cerberus_url value. URL Called: '" + cerberusUrl + "'.");
        }

        // Extract the return code
        Long executionID;
        try {
            executionID = Long.parseLong(matcher.group(1));
        } catch (NumberFormatException e) {
            LOG.error("Bad answer format (executionId is not numeric). Answer: " + answer);
            throw new RunQueueProcessException("Bad return code format: " + matcher.group(1));
        }

        // Check if return code is in error
        if (executionID == 0) {
            Matcher descriptionMatcher = RETURN_CODE_DESCRIPTION_FROM_ANSWER_PATTERN.matcher(answer);
            if (!descriptionMatcher.find()) {
                LOG.error("Bad answer format (could not find 'ReturnCodeDescription = '). URL Called: " + cerberusFullUrl);
                LOG.error("Bad answer format (could not find 'ReturnCodeDescription = '). Answer: " + answer);
                throw new RunQueueProcessException("Error occured when calling the service to run the testcase. Service answer did not have the expected format (missing 'ReturnCodeDescription = '). Probably due to bad cerberus_url value. URL Called: '" + cerberusUrl + "'.");
            }
            throw new RunQueueProcessException(descriptionMatcher.group(1));
        }
    }

    @Override
    public String toString() {
        return this.cerberusExecutionUrl;
    }

    private void addIfNotNullOrEmpty(ParamRequestMaker paramRequestMaker, String key, String value, boolean encode) throws UnsupportedEncodingException {
        if (!StringUtil.isNullOrEmpty(value)) {
            paramRequestMaker.addParam(key, encode ? URLEncoder.encode(value, "UTF-8") : value);
        }
    }
}
