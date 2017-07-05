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
package org.cerberus.engine.entity.threadpool;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;
import org.cerberus.crud.entity.CountryEnvironmentParameters;
import org.cerberus.crud.entity.TestCaseExecutionInQueue;
import org.cerberus.crud.service.ITestCaseExecutionInQueueService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.servlet.zzpublic.RunTestCase;
import org.cerberus.util.ParamRequestMaker;
import org.cerberus.util.ParameterParserUtil;

/**
 * Execute a {@link TestCaseExecutionInQueue}
 *
 * @author bcivel
 * @author abourdon
 */
public class ExecutionWorkerThread implements Runnable, Comparable {

    /**
     * The fixed value for {@link RunTestCase#PARAMETER_OUTPUT_FORMAT}
     */
    public static String PARAMETER_OUTPUT_FORMAT_VALUE = "verbose-txt";

    /**
     * The fixed value for {@link RunTestCase#PARAMETER_SYNCHRONEOUS}.
     * <p>
     * Always {@link ParameterParserUtil#DEFAULT_BOOLEAN_TRUE_VALUE} to respect
     * maximum thread pool size
     */
    public static String PARAMETER_SYNCHRONEOUS_VALUE = ParameterParserUtil.DEFAULT_BOOLEAN_TRUE_VALUE;

    /**
     * The default timeout for an execution
     */
    public static int DEFAULT_TIMEOUT = 600000;

    /**
     * The associated {@link Logger} to this class
     */
    private static final Logger LOG = Logger.getLogger(ExecutionWorkerThread.class);

    /**
     * The associated {@link Pattern} to the {@link RunTestCase} servlet answer
     * output
     */
    private static final Pattern RETURN_CODE_FROM_ANSWER_PATTERN = Pattern.compile("^ReturnCode = (\\d+)$", Pattern.MULTILINE);

    /**
     * The associated {@link Pattern} to the {@link RunTestCase} servlet answer
     * output
     */
    private static final Pattern RETURN_CODE_DESCRIPTION_FROM_ANSWER_PATTERN = Pattern.compile("^ReturnCodeDescription = (.*)$", Pattern.MULTILINE);

    /**
     * The set of return codes to be considered as technical errors
     */
    private static Set<Integer> RETURN_CODES_IN_ERROR = new HashSet<Integer>() {
        {
            // Pre execution checks (from ExecutionStartService#startExecution())
            add(MessageGeneralEnum.VALIDATION_FAILED_OUTPUTFORMAT_INVALID.getCode());
            add(MessageGeneralEnum.VALIDATION_FAILED_VERBOSE_INVALID.getCode());
            add(MessageGeneralEnum.VALIDATION_FAILED_SCREENSHOT_INVALID.getCode());
            add(MessageGeneralEnum.NO_DATA_FOUND.getCode());
            add(MessageGeneralEnum.VALIDATION_FAILED_TESTCASE_NOT_FOUND.getCode());
            add(MessageGeneralEnum.VALIDATION_FAILED_TEST_NOT_FOUND.getCode());
            add(MessageGeneralEnum.VALIDATION_FAILED_APPLICATION_NOT_FOUND.getCode());
            add(MessageGeneralEnum.VALIDATION_FAILED_COUNTRY_NOT_FOUND.getCode());
            add(MessageGeneralEnum.VALIDATION_FAILED_MANUALURL_INVALID.getCode());
            add(MessageGeneralEnum.VALIDATION_FAILED_COUNTRYENVAPP_NOT_FOUND.getCode());
            add(MessageGeneralEnum.VALIDATION_FAILED_ENVIRONMENT_DOESNOTEXIST_MAN.getCode());
            add(MessageGeneralEnum.VALIDATION_FAILED_ENVIRONMENT_DOESNOTEXIST.getCode());
            add(MessageGeneralEnum.VALIDATION_FAILED_COUNTRYENV_NOT_FOUND.getCode());
            add(MessageGeneralEnum.VALIDATION_FAILED_BROWSER_NOT_SUPPORTED.getCode());
            add(MessageGeneralEnum.VALIDATION_FAILED_SELENIUM_EMPTYORBADIP.getCode());
            add(MessageGeneralEnum.VALIDATION_FAILED_SELENIUM_EMPTYORBADPORT.getCode());
            add(MessageGeneralEnum.VALIDATION_FAILED_COULDNOTCREATE_RUNID.getCode());

            // Pre-execution checks (from SeleniumServerService#startServer())
            add(MessageGeneralEnum.VALIDATION_FAILED_URL_MALFORMED.getCode());
            add(MessageGeneralEnum.VALIDATION_FAILED_SELENIUM_COULDNOTCONNECT.getCode());
            add(MessageGeneralEnum.EXECUTION_FA_SELENIUM.getCode());

            // Pre-execution checks (from SikuliService)
            add(MessageEventEnum.ACTION_FAILED_SIKULI_SERVER_NOT_REACHABLE.getCode());
        }
    };

    /**
     * Associated builder to the {@link ExecutionWorkerThread} class
     */
    public static class Builder {

        public static final String THREAD_NAME_FORMAT = "pool(%s-%s-%s-%s), queued(%d)";

        private ExecutionWorkerThread executionWorkerThread;

        private CountryEnvironmentParameters.Key toExecuteKey;

        private String cerberusUrl;

        public Builder() {
            executionWorkerThread = new ExecutionWorkerThread();
        }

        public Builder toExecute(TestCaseExecutionInQueue toExecute) {
            executionWorkerThread.setToExecute(toExecute);
            return this;
        }

        public Builder toExecuteKey(CountryEnvironmentParameters.Key toExecuteKey) {
            this.toExecuteKey = toExecuteKey;
            return this;
        }

        public Builder cerberusUrl(String cerberusUrl) {
            this.cerberusUrl = cerberusUrl;
            return this;
        }

        public Builder inQueueService(ITestCaseExecutionInQueueService inQueueService) {
            executionWorkerThread.setInQueueService(inQueueService);
            return this;
        }

        public Builder toExecuteTimeout(int toExecuteTimeout) {
            executionWorkerThread.setToExecuteTimeout(toExecuteTimeout);
            return this;
        }

        public ExecutionWorkerThread build() {
            if (executionWorkerThread.getToExecute() == null) {
                throw new IllegalStateException("Unable to create a new ExecutionWorkerThread without the TestCaseExecutionInQueue to execute");
            }
            if (toExecuteKey == null) {
                throw new IllegalStateException("Unable to create a new ExecutionWorkerThread without the CountryEnvironmentParameters.Key associated to the TestCaseExecutionInQueue to execute");
            }
            if (cerberusUrl == null) {
                throw new IllegalStateException("Unable to create a new ExecutionWorkerThread without the Cerberus base URL");
            }
            if (executionWorkerThread.getInQueueService() == null) {
                throw new IllegalStateException("Unable to create a new ExecutionWorkerThread without the TestCaseExecutionInQueueService service");
            }
            if (executionWorkerThread.getToExecuteTimeout() == 0) {
                executionWorkerThread.setToExecuteTimeout(DEFAULT_TIMEOUT);
            }
            executionWorkerThread.setName(getName());
            executionWorkerThread.setToExecuteUrl(getExecutionUrl());
            return executionWorkerThread;
        }

        private String getName() {
            return String.format(THREAD_NAME_FORMAT,
                    toExecuteKey.getSystem(),
                    toExecuteKey.getApplication(),
                    toExecuteKey.getCountry(),
                    toExecuteKey.getEnvironment(),
                    executionWorkerThread.getToExecute().getId()
            );
        }

        private String getExecutionUrl() {
            StringBuilder url = new StringBuilder();
            url.append(cerberusUrl);
            url.append(RunTestCase.SERVLET_URL);
            url.append("?");
            url.append(makeParamRequest().mkString().replace(" ", "+"));
            return url.toString();
        }

        private ParamRequestMaker makeParamRequest() {
            ParamRequestMaker paramRequestMaker = new ParamRequestMaker();
            try {
                paramRequestMaker.addParam(RunTestCase.PARAMETER_TEST, URLEncoder.encode(executionWorkerThread.getToExecute().getTest(), "UTF-8"));
                paramRequestMaker.addParam(RunTestCase.PARAMETER_TEST_CASE, URLEncoder.encode(executionWorkerThread.getToExecute().getTestCase(), "UTF-8"));
                paramRequestMaker.addParam(RunTestCase.PARAMETER_COUNTRY, executionWorkerThread.getToExecute().getCountry());
                paramRequestMaker.addParam(RunTestCase.PARAMETER_ENVIRONMENT, executionWorkerThread.getToExecute().getEnvironment());
                paramRequestMaker.addParam(RunTestCase.PARAMETER_ROBOT, executionWorkerThread.getToExecute().getRobot());
                paramRequestMaker.addParam(RunTestCase.PARAMETER_ROBOT_IP, executionWorkerThread.getToExecute().getRobotIP());
                paramRequestMaker.addParam(RunTestCase.PARAMETER_ROBOT_PORT, executionWorkerThread.getToExecute().getRobotPort());
                paramRequestMaker.addParam(RunTestCase.PARAMETER_BROWSER, executionWorkerThread.getToExecute().getBrowser());
                paramRequestMaker.addParam(RunTestCase.PARAMETER_BROWSER_VERSION, executionWorkerThread.getToExecute().getBrowserVersion());
                paramRequestMaker.addParam(RunTestCase.PARAMETER_PLATFORM, executionWorkerThread.getToExecute().getPlatform());
                paramRequestMaker.addParam(RunTestCase.PARAMETER_SCREEN_SIZE, executionWorkerThread.getToExecute().getScreenSize());
                if (executionWorkerThread.getToExecute().isManualURL()) {
                    paramRequestMaker.addParam(RunTestCase.PARAMETER_MANUAL_URL, ParameterParserUtil.DEFAULT_BOOLEAN_TRUE_VALUE);
                    paramRequestMaker.addParam(RunTestCase.PARAMETER_MANUAL_HOST, URLEncoder.encode(executionWorkerThread.getToExecute().getManualHost(), "UTF-8"));
                    paramRequestMaker.addParam(RunTestCase.PARAMETER_MANUAL_CONTEXT_ROOT, URLEncoder.encode(executionWorkerThread.getToExecute().getManualContextRoot(), "UTF-8"));
                    paramRequestMaker.addParam(RunTestCase.PARAMETER_MANUAL_LOGIN_RELATIVE_URL, URLEncoder.encode(executionWorkerThread.getToExecute().getManualLoginRelativeURL(), "UTF-8"));
                    paramRequestMaker.addParam(RunTestCase.PARAMETER_MANUAL_ENV_DATA, executionWorkerThread.getToExecute().getManualEnvData());
                }
                paramRequestMaker.addParam(RunTestCase.PARAMETER_TAG, URLEncoder.encode(executionWorkerThread.getToExecute().getTag(), "UTF-8"));
                paramRequestMaker.addParam(RunTestCase.PARAMETER_OUTPUT_FORMAT, PARAMETER_OUTPUT_FORMAT_VALUE);
                paramRequestMaker.addParam(RunTestCase.PARAMETER_SCREENSHOT, Integer.toString(executionWorkerThread.getToExecute().getScreenshot()));
                paramRequestMaker.addParam(RunTestCase.PARAMETER_VERBOSE, Integer.toString(executionWorkerThread.getToExecute().getVerbose()));
                paramRequestMaker.addParam(RunTestCase.PARAMETER_TIMEOUT, executionWorkerThread.getToExecute().getTimeout());
                paramRequestMaker.addParam(RunTestCase.PARAMETER_SYNCHRONEOUS, PARAMETER_SYNCHRONEOUS_VALUE);
                paramRequestMaker.addParam(RunTestCase.PARAMETER_PAGE_SOURCE, Integer.toString(executionWorkerThread.getToExecute().getPageSource()));
                paramRequestMaker.addParam(RunTestCase.PARAMETER_SELENIUM_LOG, Integer.toString(executionWorkerThread.getToExecute().getSeleniumLog()));
                paramRequestMaker.addParam(RunTestCase.PARAMETER_EXECUTION_QUEUE_ID, Long.toString(executionWorkerThread.getToExecute().getId()));
                paramRequestMaker.addParam(RunTestCase.PARAMETER_NUMBER_OF_RETRIES, Long.toString(executionWorkerThread.getToExecute().getRetries()));
            } catch (UnsupportedEncodingException ex) {
                LOG.error("Error when encoding string in URL : ", ex);
            }
            return paramRequestMaker;
        }

    }

    /**
     * The associated {@link RuntimeException} for any errors during the run
     * process
     */
    public static class RunProcessException extends RuntimeException {

        public RunProcessException(String message) {
            super(message);
        }

        public RunProcessException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    private String name;

    private TestCaseExecutionInQueue toExecute;

    @JsonIgnore
    private String toExecuteUrl;

    @JsonIgnore
    private int toExecuteTimeout;

    @JsonIgnore
    private ITestCaseExecutionInQueueService inQueueService;

    /**
     * Private constructor, use the {@link Builder} instead
     */
    private ExecutionWorkerThread() {
    }

    public String getName() {
        return name;
    }

    private void setName(final String name) {
        this.name = name;
    }

    public TestCaseExecutionInQueue getToExecute() {
        return toExecute;
    }

    private void setToExecute(TestCaseExecutionInQueue toExecute) {
        this.toExecute = toExecute;
    }

    public String getToExecuteUrl() {
        return toExecuteUrl;
    }

    private void setToExecuteUrl(String toExecuteUrl) {
        this.toExecuteUrl = toExecuteUrl;
    }

    public int getToExecuteTimeout() {
        return toExecuteTimeout;
    }

    private void setToExecuteTimeout(int toExecuteTimeout) {
        this.toExecuteTimeout = toExecuteTimeout;
    }

    public ITestCaseExecutionInQueueService getInQueueService() {
        return inQueueService;
    }

    private void setInQueueService(ITestCaseExecutionInQueueService inQueueService) {
        this.inQueueService = inQueueService;
    }

    /**
     * Process to the test case in queue execution by:
     * <ol>
     * <li>Moving its state from its current
     * ({@link org.cerberus.crud.entity.TestCaseExecutionInQueue.State#QUEUED})
     * to
     * {@link org.cerberus.crud.entity.TestCaseExecutionInQueue.State#EXECUTING}</li>
     * <li>Following state moving result then process execution by requesting
     * the associated {@link RunTestCase} servlet</li>
     * <li>Following execution result then removing it from the execution in
     * queue table or moving its state to ERROR in case of technical error</li>
     * </ol>
     *
     * @see #runFromQueuedToExecuting()
     * @see #runExecution()
     * @see #runParseAnswer(String)
     * @see #runRemoveExecutionInQueue()
     */
    @Override
    public void run() {
        Thread.currentThread().setName(getName());
        try {
            if (!runFromQueuedToExecuting()) {
                LOG.warn("Execution in queue " + toExecute.getId() + " has finished with error");
                return;
            }
            runParseAnswer(runExecution());
            runRemoveExecutionInQueue();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Execution in queue " + toExecute.getId() + " has been successfully executed");
            }
        } catch (RunProcessException e) {
            LOG.warn("Execution in queue " + toExecute.getId() + " has finished with error", e);
            try {
                inQueueService.toError(toExecute.getId(), e.getMessage());
            } catch (CerberusException again) {
                LOG.warn("Unable to mark execution in queue " + toExecute.getId() + " as in error", again);
            }
        }
    }

    /**
     * Move the inner {@link TestCaseExecutionInQueue} to the
     * {@link org.cerberus.crud.entity.TestCaseExecutionInQueue.State#EXECUTING}
     * state
     *
     * @see #run()
     */
    private boolean runFromQueuedToExecuting() {
        try {
            inQueueService.toExecuting(toExecute.getId());
            return true;
        } catch (CerberusException e) {
            LOG.warn("Unable to mark execution in queue " + toExecute.getId() + " as executing. Is execution in queue currently marked as queued?", e);
            return false;
        }
    }

    /**
     * Request execution of the inner {@link TestCaseExecutionInQueue} to the
     * {@link RunTestCase} servlet
     *
     * @return the execution answer from the {@link RunTestCase} servlet
     * @throws RunProcessException if an error occurred during request execution
     * @see #run()
     */
    private String runExecution() {
        try {
            return Request
                    .Get(toExecuteUrl)
                    .connectTimeout(toExecuteTimeout)
                    .socketTimeout(toExecuteTimeout)
                    .execute()
                    .returnContent()
                    .asString();
        } catch (Exception e) {
            final StringBuilder errorMessage = new StringBuilder("An unexpected error occurred during test case execution: ");
            if (e instanceof HttpResponseException) {
                errorMessage.append(String.format("%d (%s)", ((HttpResponseException) e).getStatusCode(), e.getMessage()));
            } else {
                errorMessage.append(e.getMessage());
                errorMessage.append(". Check server logs");
            }
            throw new RunProcessException(errorMessage.toString(), e);
        }
    }

    /**
     * Parse the answer given by the {@link RunTestCase}
     * <p>
     * Assume answer has been written following the
     * {@link #PARAMETER_OUTPUT_FORMAT_VALUE}
     *
     * @param answer the {@link RunTestCase}'s answer
     * @throws RunProcessException if an error occurred if execution was on
     * failure or if answer cannot be parsed
     * @see #run()
     */
    private void runParseAnswer(String answer) {
        // Check answer format
        Matcher matcher = RETURN_CODE_FROM_ANSWER_PATTERN.matcher(answer);
        if (!matcher.find()) {
            LOG.warn("Bad answer format: " + answer);
            throw new RunProcessException("Bad answer format. Expected " + PARAMETER_OUTPUT_FORMAT_VALUE + ". Probably due to bad cerberus_url value. Check server logs");
        }

        // Extract the return code
        int returnCode;
        try {
            returnCode = Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            throw new RunProcessException("Bad return code format: " + matcher.group(1));
        }

        // Check if return code is in error
        if (RETURN_CODES_IN_ERROR.contains(returnCode)) {
            Matcher descriptionMatcher = RETURN_CODE_DESCRIPTION_FROM_ANSWER_PATTERN.matcher(answer);
            if (!descriptionMatcher.find()) {
                LOG.warn("Bad return code description format: " + answer);
                throw new RunProcessException("Bad answer format. Expected " + PARAMETER_OUTPUT_FORMAT_VALUE + ". Check server logs");
            }
            throw new RunProcessException(descriptionMatcher.group(1));
        }
    }

    /**
     * Remove the inner execution in queue from the execution in queue table
     *
     * @throws RunProcessException if an error occurred during execution in
     * queue removal
     * @see #run()
     */
    private void runRemoveExecutionInQueue() {
        try {
            inQueueService.remove(toExecute.getId());
        } catch (CerberusException e) {
            throw new RunProcessException("Unable to remove execution in queue " + toExecute.getId() + " due to " + e.getMessageError(), e);
        }
    }

    @Override
    public String toString() {
        return "ExecutionWorkerThread{"
                + "toExecute=" + toExecute
                + ", name=" + name
                + ", toExecuteUrl='" + toExecuteUrl + '\''
                + ", toExecuteTimeout=" + toExecuteTimeout
                + '}';
    }

    @Override
    public int compareTo(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
