/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.servlet.zzpublic;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.core.crud.entity.CampaignParameter;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecutionQueue;
import org.cerberus.core.crud.service.ICampaignParameterService;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.exception.FactoryCreationException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.servlet.ServletUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecutionQueue;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.core.service.authentification.IAPIKeyService;

/**
 * Add a test case to the execution queue (so to be executed later).
 *
 * @author abourdon
 */
@WebServlet(name = "AddToExecutionQueue", description = "Add a test case to the execution queue.", urlPatterns = {"/AddToExecutionQueue"})
public class AddToExecutionQueue extends HttpServlet {

    /**
     * Exception thrown when the parameter scanning process goes wrong.
     *
     * @author abourdon
     */
    private static class ParameterException extends Exception {

        private static final long serialVersionUID = 1L;

        public ParameterException(String message) {
            super(message);
        }

        public ParameterException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LogManager.getLogger(AddToExecutionQueue.class);

    private static final String PARAMETER_SELECTED_TEST = "SelectedTest";
    private static final String PARAMETER_SELECTED_TEST_TEST = "Test";
    private static final String PARAMETER_SELECTED_TEST_TEST_CASE = "TestCase";
    private static final String PARAMETER_COUNTRY = "Country";
    private static final String PARAMETER_ENVIRONMENT = "Environment";
    private static final String PARAMETER_ROBOT = "Robot";
    private static final String PARAMETER_ROBOT_IP = "ss_ip";
    private static final String PARAMETER_ROBOT_PORT = "ss_p";
    private static final String PARAMETER_BROWSER = "Browser";
    private static final String PARAMETER_BROWSER_VERSION = "BrowserVersion";
    private static final String PARAMETER_PLATFORM = "Platform";
    private static final String PARAMETER_MANUAL_URL = "ManualURL";
    private static final String PARAMETER_MANUAL_HOST = "ManualHost";
    private static final String PARAMETER_MANUAL_CONTEXT_ROOT = "ManualContextRoot";
    private static final String PARAMETER_MANUAL_LOGIN_RELATIVE_URL = "ManualLoginRelativeURL";
    private static final String PARAMETER_MANUAL_ENV_DATA = "ManualEnvData";
    private static final String PARAMETER_TAG = "Tag";
    private static final String PARAMETER_OUTPUT_FORMAT = "OutputFormat";
    private static final String PARAMETER_SCREENSHOT = "Screenshot";
    private static final String PARAMETER_VERBOSE = "Verbose";
    private static final String PARAMETER_TIMEOUT = "timeout";
    private static final String PARAMETER_SYNCHRONEOUS = "Synchroneous";
    private static final String PARAMETER_PAGE_SOURCE = "PageSource";
    private static final String PARAMETER_SELENIUM_LOG = "SeleniumLog";
    private static final String PARAMETER_CAMPAIGN = "SelectedCampaign";
    private static final String PARAMETER_RETRIES = "retries";
    private static final String PARAMETER_MANUAL_EXECUTION = "manualExecution";

    private static final int DEFAULT_VALUE_SCREENSHOT = 0;
    private static final int DEFAULT_VALUE_MANUAL_URL = 0;
    private static final int DEFAULT_VALUE_VERBOSE = 0;
    private static final long DEFAULT_VALUE_TIMEOUT = 300;
    private static final int DEFAULT_VALUE_PAGE_SOURCE = 1;
    private static final int DEFAULT_VALUE_SELENIUM_LOG = 1;
    private static final int DEFAULT_VALUE_RETRIES = 0;
    private static final String DEFAULT_VALUE_MANUAL_EXECUTION = "N";

    private static final String LINE_SEPARATOR = "\n";

    private ITestCaseExecutionQueueService inQueueService;
    private IFactoryTestCaseExecutionQueue inQueueFactoryService;
    private IExecutionThreadPoolService executionThreadService;
    private ITestCaseService testCaseService;
    private ICampaignParameterService campaignParameterService;
    private IAPIKeyService apiKeyService;

    @Override
    public void init() throws ServletException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        inQueueService = appContext.getBean(ITestCaseExecutionQueueService.class);
        inQueueFactoryService = appContext.getBean(IFactoryTestCaseExecutionQueue.class);
        executionThreadService = appContext.getBean(IExecutionThreadPoolService.class);
        testCaseService = appContext.getBean(ITestCaseService.class);
        campaignParameterService = appContext.getBean(ICampaignParameterService.class);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    /**
     * Process request for both GET and POST method.
     *
     * <p>
     * Request processing is divided in two parts:
     * <ol>
     * <li>Getting all test cases which have been sent to this servlet;</li>
     * <li>Try to insert all these test cases to the execution queue.</li>
     * </ol>
     * </p>
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        // Loading Services.
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        apiKeyService = appContext.getBean(IAPIKeyService.class);

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        /**
         * Adding Log entry.
         */
        ILogEventService logEventService = appContext.getBean(ILogEventService.class);
        logEventService.createForPublicCalls("/AddToExecutionQueue", "CALL", LogEvent.STATUS_INFO, "AddToExecutionQueue called : " + request.getRequestURL(), request);

        // Parsing all parameters.
        String tag = ParameterParserUtil.parseStringParam(request.getParameter(PARAMETER_TAG), "");
        // TO BE IMPLEMENTED...

        // Defining help message.
        String helpMessage = "\nThis servlet is used to add to Cerberus execution queue a list of execution specified by various parameters:\n"
                + "- " + PARAMETER_TAG + " [mandatory] : Tag that will be used for every execution triggered. [" + tag + "]\n";
        // TO BE IMPLEMENTED...

        if (apiKeyService.authenticate(request, response)) {

            // Checking the parameter validity.
            boolean error = false;
            if (tag == null || tag.isEmpty()) {
                out.println("Error - Parameter " + PARAMETER_TAG + " is mandatory.");
                error = true;
            } else if (tag.length() > 255) {
                out.println("Error - Parameter " + PARAMETER_TAG + " is too big. Maximum size is 255. Current size is : " + tag.length());
                error = true;
            }

            // Starting the request only if previous parameters exist.
            if (!error) {

                // Create Tag when exist.
                if (!StringUtil.isEmptyOrNull(tag)) {
                    // We create or update it.
                    ITagService tagService = appContext.getBean(ITagService.class);
                    tagService.createAuto(tag, "", "", null, null);
                }

                // Part 1: Getting all test cases which have been sent to this servlet.
                List<TestCaseExecutionQueue> toInserts = null;
                try {
                    toInserts = getTestCasesToInsert(request);
                } catch (ParameterException pe) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, pe.getMessage());
                    return;
                } catch (CerberusException ex) {
                    LOG.warn(ex);
                }

                Map<String, TestCaseExecutionQueue> testCasesInserted = new HashMap<>();
                for (TestCaseExecutionQueue toInsert : toInserts) {
                    if (!testCasesInserted.containsKey(inQueueService.getUniqKey(toInsert.getTest(), toInsert.getTestCase(), toInsert.getCountry(), toInsert.getEnvironment()))) {
                        testCasesInserted.put(inQueueService.getUniqKey(toInsert.getTest(), toInsert.getTestCase(), toInsert.getCountry(), toInsert.getEnvironment()), toInsert);
                    }
                }

                // Part 2: Try to insert all these test cases to the execution queue.
                List<String> errorMessages = new ArrayList<>();
                for (TestCaseExecutionQueue toInsert : toInserts) {
                    try {
                        inQueueService.convert(inQueueService.create(toInsert, true, 0, TestCaseExecutionQueue.State.QUEUED, testCasesInserted));
                    } catch (CerberusException e) {
                        String errorMessage = "Unable to insert " + toInsert.toString() + " due to " + e.getMessage();
                        LOG.warn(errorMessage);
                        errorMessages.add(errorMessage);
                        continue;
                    }
                }

                // Part 3 : Put these tests in the queue in memory
                try {
                    executionThreadService.executeNextInQueueAsynchroneously(false);
                } catch (CerberusException ex) {
                    String errorMessage = "Unable to feed the execution queue due to " + ex.getMessage();
                    LOG.warn(errorMessage);
                    errorMessages.add(errorMessage);
                }

                if (!errorMessages.isEmpty()) {
                    StringBuilder errorMessage = new StringBuilder();
                    for (String item : errorMessages) {
                        errorMessage.append(item);
                        errorMessage.append(LINE_SEPARATOR);
                    }
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, errorMessage.toString());
                }

                response.sendRedirect("ReportingExecutionByTag.jsp?enc=1&Tag=" + StringUtil.encodeAsJavaScriptURIComponent(request.getParameter(PARAMETER_TAG)));

            } else {
                // In case of errors, we displayu the help message.
                out.println(helpMessage);

            }

        }

    }

    /**
     * Gets all test cases requested to be inserted into the execution queue
     *
     * @param request
     * @return a {@link List} of {@link TestCaseExecutionQueue} which have been
     * defined into the request.
     * @throws ParameterException
     */
    private List<TestCaseExecutionQueue> getTestCasesToInsert(HttpServletRequest request) throws ParameterException, CerberusException {

        final String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();

        // Select test cases and associated parameters to run
        List<Map<String, String>> selectedTests;
        List<String> countries;
        List<String> environments;
        List<String> browsers;
        final String campaign = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter(PARAMETER_CAMPAIGN), null, charset);
        if (campaign == null || campaign.isEmpty()) {
            selectedTests = ParameterParserUtil.parseListMapParamAndDecode(request.getParameterValues(PARAMETER_SELECTED_TEST), null, charset);
            if (selectedTests == null || selectedTests.isEmpty()) {
                throw new ParameterException("Selected tests are not defined");
            }
            countries = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues(PARAMETER_COUNTRY), null, charset);
            if (countries == null || countries.isEmpty()) {
                throw new ParameterException("Countries are not defined");
            }
            environments = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues(PARAMETER_ENVIRONMENT), null, charset);
            if (environments == null || environments.isEmpty()) {
                throw new ParameterException("Environment are not defined");
            }
            browsers = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues(PARAMETER_BROWSER), null, charset);
            if (browsers == null || browsers.isEmpty()) {
                throw new ParameterException("Browsers are not defined");
            }
        } else {
            final AnswerItem<Map<String, List<String>>> parsedCampaignParameters = campaignParameterService.parseParametersByCampaign(campaign);
            if (!parsedCampaignParameters.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                throw new ParameterException("Unable to get selected campaign or associated parameters");
            }
            countries = parsedCampaignParameters.getItem().get(CampaignParameter.COUNTRY_PARAMETER);
            if (countries == null || countries.isEmpty()) {
                throw new ParameterException("Selected campaign does not defined any country");
            }
            environments = parsedCampaignParameters.getItem().get(CampaignParameter.ENVIRONMENT_PARAMETER);
            if (environments == null || environments.isEmpty()) {
                throw new ParameterException("Selected campaign does not defined any environment");
            }
            browsers = parsedCampaignParameters.getItem().get(CampaignParameter.BROWSER_PARAMETER);
            if (browsers == null || browsers.isEmpty()) {
                throw new ParameterException("Selected campaign does not defined any browser");
            }
            selectedTests = new ArrayList<>();
            for (final TestCase testCase : testCaseService.findTestCaseByCampaignNameAndCountries(campaign, countries.toArray(new String[countries.size()])).getDataList()) {
                selectedTests.add(new HashMap<String, String>() {
                    {
                        put(PARAMETER_SELECTED_TEST_TEST, testCase.getTest());
                        put(PARAMETER_SELECTED_TEST_TEST_CASE, testCase.getTestcase());
                    }
                });
            }
            if (selectedTests.isEmpty()) {
                throw new ParameterException("Selected campaign does not defined any test case");
            }
        }

        final String tag = ParameterParserUtil.parseStringParam(request.getParameter(PARAMETER_TAG), "");
        if (tag == null || tag.isEmpty()) {
            throw new ParameterException("Tag is not defined");
        }

        Date requestDate = new Date();

        String robot = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter(PARAMETER_ROBOT), null, charset);
        String robotDecli = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter(PARAMETER_ROBOT), null, charset);
        String robotIP = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter(PARAMETER_ROBOT_IP), null, charset);
        String robotPort = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter(PARAMETER_ROBOT_PORT), null, charset);
        String browserVersion = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter(PARAMETER_BROWSER_VERSION), null, charset);
        String platform = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter(PARAMETER_PLATFORM), null, charset);
        int manualURL = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter(PARAMETER_MANUAL_URL), DEFAULT_VALUE_MANUAL_URL, charset);
        String manualHost = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter(PARAMETER_MANUAL_HOST), null, charset);
        String manualContextRoot = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter(PARAMETER_MANUAL_CONTEXT_ROOT), null, charset);
        String manualLoginRelativeURL = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter(PARAMETER_MANUAL_LOGIN_RELATIVE_URL), null, charset);
        String manualEnvData = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter(PARAMETER_MANUAL_ENV_DATA), null, charset);
        int screenshot = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter(PARAMETER_SCREENSHOT), DEFAULT_VALUE_SCREENSHOT, charset);
        int verbose = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter(PARAMETER_VERBOSE), DEFAULT_VALUE_VERBOSE, charset);
        String timeout = request.getParameter(PARAMETER_TIMEOUT);
        int pageSource = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter(PARAMETER_PAGE_SOURCE), DEFAULT_VALUE_PAGE_SOURCE, charset);
        int seleniumLog = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter(PARAMETER_SELENIUM_LOG), DEFAULT_VALUE_SELENIUM_LOG, charset);
        int retries = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter(PARAMETER_RETRIES), DEFAULT_VALUE_RETRIES, charset);
        String manualExecution = ParameterParserUtil.parseStringParamAndDecode(request.getParameter(PARAMETER_MANUAL_EXECUTION), DEFAULT_VALUE_MANUAL_EXECUTION, charset);

        List<TestCaseExecutionQueue> inQueues = new ArrayList<>();
        for (Map<String, String> selectedTest : selectedTests) {
            String test = selectedTest.get(PARAMETER_SELECTED_TEST_TEST);
            String testCase = selectedTest.get(PARAMETER_SELECTED_TEST_TEST_CASE);
            for (String country : countries) {
                for (String environment : environments) {
                    for (String browser : browsers) {
                        try {
                            String user = request.getRemoteUser() == null ? "" : request.getRemoteUser();
                            inQueues.add(inQueueFactoryService.create("", test, testCase, country, environment, robot, robotDecli, robotIP, robotPort, browser, browserVersion,
                                    platform, "", manualURL, manualHost, manualContextRoot, manualLoginRelativeURL, manualEnvData, tag, screenshot, 0, verbose, timeout, pageSource,
                                    seleniumLog, 0, 0, retries, manualExecution, 1000, user, null, null, null));
                        } catch (FactoryCreationException e) {
                            throw new ParameterException("Unable to insert record due to: " + e.getMessage(), e);
                        }
                    }
                }
            }
        }
        return inQueues;
    }
}
