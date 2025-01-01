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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.CampaignParameter;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseCountry;
import org.cerberus.core.crud.entity.TestCaseExecutionQueue;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecutionQueue;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.ICampaignParameterService;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.crud.service.ITestCaseCountryService;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.exception.FactoryCreationException;
import org.cerberus.core.service.authentification.IAPIKeyService;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Add a test case to the execution queue (so to be executed later).
 *
 * @author abourdon
 */
@WebServlet(name = "AddToExecutionQueueV001", description = "Add a test case to the execution queue.", urlPatterns = {"/AddToExecutionQueueV001"})
public class AddToExecutionQueueV001 extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(AddToExecutionQueueV001.class);

    private static final String PARAMETER_CAMPAIGN = "campaign";
    private static final String PARAMETER_SELECTED_TEST = "testlist";
    private static final String PARAMETER_SELECTED_TEST_KEY_TEST = "test";
    private static final String PARAMETER_SELECTED_TEST_KEY_TESTCASE = "testcase";
    private static final String PARAMETER_COUNTRY = "country";
    private static final String PARAMETER_ENVIRONMENT = "environment";
    private static final String PARAMETER_BROWSER = "browser";
    private static final String PARAMETER_ROBOT = "robot";
    private static final String PARAMETER_ROBOT_IP = "ss_ip";
    private static final String PARAMETER_ROBOT_PORT = "ss_p";
    private static final String PARAMETER_BROWSER_VERSION = "version";
    private static final String PARAMETER_PLATFORM = "platform";
    private static final String PARAMETER_SCREENSIZE = "screensize";
    private static final String PARAMETER_MANUAL_URL = "manualurl";
    private static final String PARAMETER_MANUAL_HOST = "myhost";
    private static final String PARAMETER_MANUAL_CONTEXT_ROOT = "mycontextroot";
    private static final String PARAMETER_MANUAL_LOGIN_RELATIVE_URL = "myloginrelativeurl";
    private static final String PARAMETER_MANUAL_ENV_DATA = "myenvdata";
    private static final String PARAMETER_TAG = "tag";
    private static final String PARAMETER_SCREENSHOT = "screenshot";
    private static final String PARAMETER_VERBOSE = "verbose";
    private static final String PARAMETER_TIMEOUT = "timeout";
    private static final String PARAMETER_PAGE_SOURCE = "pagesource";
    private static final String PARAMETER_SELENIUM_LOG = "seleniumlog";
    private static final String PARAMETER_RETRIES = "retries";
    private static final String PARAMETER_MANUAL_EXECUTION = "manualexecution";
    private static final String PARAMETER_EXEPRIORITY = "priority";
    private static final String PARAMETER_OUTPUTFORMAT = "outputformat";

    private static final int DEFAULT_VALUE_SCREENSHOT = 0;
    private static final int DEFAULT_VALUE_MANUAL_URL = 0;
    private static final int DEFAULT_VALUE_VERBOSE = 0;
    private static final long DEFAULT_VALUE_TIMEOUT = 300;
    private static final int DEFAULT_VALUE_PAGE_SOURCE = 1;
    private static final int DEFAULT_VALUE_SELENIUM_LOG = 1;
    private static final int DEFAULT_VALUE_RETRIES = 0;
    private static final String DEFAULT_VALUE_MANUAL_EXECUTION = "N";
    private static final int DEFAULT_VALUE_PRIORITY = 1000;
    private static final String DEFAULT_VALUE_OUTPUTFORMAT = "compact";

    private static final String LINE_SEPARATOR = "\n";

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");

    private ITestCaseExecutionQueueService inQueueService;
    private IFactoryTestCaseExecutionQueue inQueueFactoryService;
    private IExecutionThreadPoolService executionThreadService;
    private IInvariantService invariantService;
    private IApplicationService applicationService;
    private ITestCaseService testCaseService;
    private ITestCaseCountryService testCaseCountryService;
    private ICampaignParameterService campaignParameterService;
    private IAPIKeyService apiKeyService;

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
        final String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();
        Date requestDate = new Date();

        // Loading Services.
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        inQueueService = appContext.getBean(ITestCaseExecutionQueueService.class);
        inQueueFactoryService = appContext.getBean(IFactoryTestCaseExecutionQueue.class);
        executionThreadService = appContext.getBean(IExecutionThreadPoolService.class);
        testCaseService = appContext.getBean(ITestCaseService.class);
        invariantService = appContext.getBean(IInvariantService.class);
        applicationService = appContext.getBean(IApplicationService.class);
        testCaseCountryService = appContext.getBean(ITestCaseCountryService.class);
        campaignParameterService = appContext.getBean(ICampaignParameterService.class);
        apiKeyService = appContext.getBean(IAPIKeyService.class);

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        // Default message to unexpected error.
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        AnswerList<TestCase> testcases = null;

        /**
         * Adding Log entry.
         */
        ILogEventService logEventService = appContext.getBean(ILogEventService.class);
        logEventService.createForPublicCalls("/AddToExecutionQueueV001", "CALL", LogEvent.STATUS_INFO, "AddToExecutionQueueV001 called : " + request.getRequestURL(), request);

        if (apiKeyService.authenticate(request, response)) {

            // Parsing all parameters.
            // Execution scope parameters : Campaign, TestCases, Countries, Environment, Browser.
            String campaign = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter(PARAMETER_CAMPAIGN), null, charset);
            List<Map<String, String>> selectedTests;
            selectedTests = ParameterParserUtil.parseListMapParamAndDecode(request.getParameterValues(PARAMETER_SELECTED_TEST), null, charset);
            List<String> countries;
            countries = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues(PARAMETER_COUNTRY), null, charset);
            List<String> environments;
            environments = ParameterParserUtil.parseListParamAndDecodeAndDeleteEmptyValue(request.getParameterValues(PARAMETER_ENVIRONMENT), null, charset);

            JSONArray countryJSONArray = new JSONArray(countries);
            JSONArray envJSONArray = new JSONArray(environments);

            List<String> browsers;
            browsers = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues(PARAMETER_BROWSER), null, charset);
            // Execution parameters.
            String tag = ParameterParserUtil.parseStringParam(request.getParameter(PARAMETER_TAG), "");
            String robot = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter(PARAMETER_ROBOT), null, charset);
            String robotIP = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter(PARAMETER_ROBOT_IP), null, charset);
            String robotPort = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter(PARAMETER_ROBOT_PORT), null, charset);
            String browserVersion = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter(PARAMETER_BROWSER_VERSION), null, charset);
            String platform = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter(PARAMETER_PLATFORM), null, charset);
            String screenSize = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter(PARAMETER_SCREENSIZE), null, charset);
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
            int priority = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter(PARAMETER_EXEPRIORITY), DEFAULT_VALUE_PRIORITY, charset);
            if (manualExecution.isEmpty()) {
                manualExecution = DEFAULT_VALUE_MANUAL_EXECUTION;
            }
            String outputFormat = ParameterParserUtil.parseStringParamAndDecode(request.getParameter(PARAMETER_OUTPUTFORMAT), DEFAULT_VALUE_OUTPUTFORMAT, charset);

            // Defining help message.
            String helpMessage = "This servlet is used to add to Cerberus execution queue a list of execution. Execution list will be calculated from cartesian product of "
                    + "testcase, country, environment and browser list. Those list can be defined from the associated servlet parameter but can also be defined from campaign directy inside Cerberus.\n"
                    + "List defined from servlet overwrite the list defined from the campaign. All other execution parameters will be taken to each execution.\n"
                    + "Available parameters:\n"
                    + "- " + PARAMETER_CAMPAIGN + " : Campaign name from which testcase, countries, environment and browser can be defined from Cerberus. [" + campaign + "]\n"
                    + "- " + PARAMETER_SELECTED_TEST + " : List of testCase to trigger. That list overwrite the list coming from the Campaign (if defined). Ex : " + PARAMETER_SELECTED_TEST + "=" + PARAMETER_SELECTED_TEST_KEY_TEST + "=Cerberus%26" + PARAMETER_SELECTED_TEST_KEY_TESTCASE + "=9644A. [" + selectedTests + "]\n"
                    + "- " + PARAMETER_COUNTRY + " : List of countries to trigger. That list overwrite the list coming from the Campaign (if defined).. [" + countries + "]\n"
                    + "- " + PARAMETER_ENVIRONMENT + " : List of environment to trigger. That list overwrite the list coming from the Campaign (if defined).. [" + environments + "]\n"
                    + "- " + PARAMETER_BROWSER + " : List of browser to trigger. That list overwrite the list coming from the Campaign (if defined).. [" + browsers + "]\n"
                    + "- " + PARAMETER_ROBOT + " : Robot Name that will be used for every execution triggered. [" + robot + "]\n"
                    + "- " + PARAMETER_ROBOT_IP + " : Robot IP that will be used for every execution triggered. [" + robotIP + "]\n"
                    + "- " + PARAMETER_ROBOT_PORT + " : Robot Port that will be used for every execution triggered. [" + robotPort + "]\n"
                    + "- " + PARAMETER_BROWSER_VERSION + " : Browser Version that will be used for every execution triggered. [" + browserVersion + "]\n"
                    + "- " + PARAMETER_PLATFORM + " : Platform that will be used for every execution triggered. [" + platform + "]\n"
                    + "- " + PARAMETER_SCREENSIZE + " : Size of the screen that will be used for every execution triggered. [" + screenSize + "]\n"
                    + "- " + PARAMETER_MANUAL_URL + " : Activate (1) or not (0) the Manual URL of the application to execute. If activated the 4 parameters after are necessary. [" + manualURL + "]\n"
                    + "- " + PARAMETER_MANUAL_HOST + " : Host of the application to test (only used when " + PARAMETER_MANUAL_URL + " is activated). [" + manualHost + "]\n"
                    + "- " + PARAMETER_MANUAL_CONTEXT_ROOT + " : Context root of the application to test (only used when " + PARAMETER_MANUAL_URL + " is activated). [" + manualContextRoot + "]\n"
                    + "- " + PARAMETER_MANUAL_LOGIN_RELATIVE_URL + " : Relative login URL of the application (only used when " + PARAMETER_MANUAL_URL + " is activated). [" + manualLoginRelativeURL + "]\n"
                    + "- " + PARAMETER_MANUAL_ENV_DATA + " : Environment where to get the test data when a " + PARAMETER_MANUAL_URL + " is defined. (only used when manualURL is active). [" + manualEnvData + "]\n"
                    + "- " + PARAMETER_TAG + " : Tag that will be used for every execution triggered. [" + tag + "]\n"
                    + "- " + PARAMETER_SCREENSHOT + " : Activate or not the screenshots for every execution triggered. [" + screenshot + "]\n"
                    + "- " + PARAMETER_VERBOSE + " : Verbose level for every execution triggered. [" + verbose + "]\n"
                    + "- " + PARAMETER_TIMEOUT + " : Timeout used for the action that will be used for every execution triggered. [" + timeout + "]\n"
                    + "- " + PARAMETER_PAGE_SOURCE + " : Record Page Source during for every execution triggered. [" + pageSource + "]\n"
                    + "- " + PARAMETER_SELENIUM_LOG + " : Get the SeleniumLog at the end of the execution for every execution triggered. [" + seleniumLog + "]\n"
                    + "- " + PARAMETER_MANUAL_EXECUTION + " : Execute testcase in manual mode for every execution triggered. [" + manualExecution + "]\n"
                    + "- " + PARAMETER_RETRIES + " : Number of tries if the result is not OK for every execution triggered. [" + retries + "]\n"
                    + "- " + PARAMETER_EXEPRIORITY + " : Priority that will be used in the queue for every execution triggered. [" + priority + "]\n";

//        try {
            // Checking the parameter validity.
            StringBuilder errorMessage = new StringBuilder();
            boolean error = false;
            if (tag == null || tag.isEmpty()) {
                if (request.getRemoteUser() != null) {
                    tag = request.getRemoteUser();
                }
                if (tag.length() > 0) {
                    tag += ".";
                }
                if (campaign != null) {
                    tag += campaign;
                }
                if (tag.length() > 0) {
                    tag += ".";
                }
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                String mytimestamp = sdf.format(timestamp);
                tag += mytimestamp;

            } else if (tag.length() > 255) {

                errorMessage.append("Error - Parameter " + PARAMETER_TAG + " is too big. Maximum size if 255. Current size is : ").append(tag.length());
                error = true;
            }

            if (campaign != null && !campaign.isEmpty()) {
                final AnswerItem<Map<String, List<String>>> parsedCampaignParameters = campaignParameterService.parseParametersByCampaign(campaign);
                if (parsedCampaignParameters.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    // Parameters from campaign could be retreived. we can now replace the parameters comming from the calls in case they are still not defined.
                    // If parameters are already defined from request, we ignore the campaign values.
                    if (countries == null || countries.isEmpty()) {
                        countries = parsedCampaignParameters.getItem().get(CampaignParameter.COUNTRY_PARAMETER);
                    }
                    if (environments == null || environments.isEmpty()) {
                        environments = parsedCampaignParameters.getItem().get(CampaignParameter.ENVIRONMENT_PARAMETER);
                    }
                    if (browsers == null || browsers.isEmpty()) {
                        browsers = parsedCampaignParameters.getItem().get(CampaignParameter.BROWSER_PARAMETER);
                    }
                }
                if ((countries != null) && (selectedTests == null || selectedTests.isEmpty())) {
                    // If no countries are found, there is no need to get the testcase list. None will be returned.
                    selectedTests = new ArrayList<>();
                    testcases = testCaseService.findTestCaseByCampaignNameAndCountries(campaign, countries.toArray(new String[countries.size()]));

                    ListIterator<TestCase> it = testcases.getDataList().listIterator();
                    while (it.hasNext()) {
                        TestCase str = it.next();
                        selectedTests.add(new HashMap<String, String>() {
                            {
                                put(PARAMETER_SELECTED_TEST_KEY_TEST, str.getTest());
                                put(PARAMETER_SELECTED_TEST_KEY_TESTCASE, str.getTestcase());
                            }
                        });

                    }
                }
            }
            if (countries == null || countries.isEmpty()) {
                errorMessage.append("Error - No Country defined. You can either feed it with parameter '" + PARAMETER_COUNTRY + "' or add it into the campaign definition.");
                error = true;
            }
            if (browsers == null || browsers.isEmpty()) {
                errorMessage.append("Error - No Browser defined. You can either feed it with parameter '" + PARAMETER_BROWSER + "' or add it into the campaign definition.");
                error = true;
            }
            if (selectedTests == null || selectedTests.isEmpty()) {
                errorMessage.append("Error - No TestCases defined. You can either feed it with parameter '" + PARAMETER_SELECTED_TEST + "' or add it into the campaign definition.");
                error = true;
            }
            if (manualURL >= 1) {
                if (manualHost == null || manualEnvData == null) {
                    errorMessage.append("Error - ManualURL has been activated but no ManualHost or Manual Environment defined.");
                    error = true;
                }
            } else if (environments == null || environments.isEmpty()) {
                errorMessage.append("Error - No Environment defined (and " + PARAMETER_MANUAL_URL + " not activated). You can either feed it with parameter '" + PARAMETER_ENVIRONMENT + "' or add it into the campaign definition.");
                error = true;
            }

            int nbExe = 0;
            JSONArray jsonArray = new JSONArray();
            String user = request.getRemoteUser() == null ? "" : request.getRemoteUser();
            // Starting the request only if previous parameters exist.
            if (!error) {

                // Create Tag when exist.
                if (!StringUtil.isEmptyOrNull(tag)) {
                    // We create or update it.
                    ITagService tagService = appContext.getBean(ITagService.class);
                    tagService.createAuto(tag, campaign, user, envJSONArray, countryJSONArray);
                }

                // Part 1: Getting all possible xecution from test cases + countries + environments + browsers which have been sent to this servlet.
                Map<String, String> invariantEnv = invariantService.readToHashMapGp1StringByIdname("ENVIRONMENT", "");
                List<TestCaseExecutionQueue> toInserts = new ArrayList<>();
                try {
                    LOG.debug("Nb of TestCase : " + selectedTests.size());
                    for (Map<String, String> selectedTest : selectedTests) {
                        String test = selectedTest.get(PARAMETER_SELECTED_TEST_KEY_TEST);
                        String testCase = selectedTest.get(PARAMETER_SELECTED_TEST_KEY_TESTCASE);
                        TestCase tc = testCaseService.convert(testCaseService.readByKey(test, testCase));
                        // TestCases that are not active are not inserted into queue.
                        if (tc.isActive()) {
                            // We only insert testcase that exist for the given country.
                            for (TestCaseCountry country : testCaseCountryService.convert(testCaseCountryService.readByTestTestCase(null, test, testCase, null))) {
                                if (countries.contains(country.getCountry())) {
                                    // for each environment we test that correspondng gp1 is compatible with testcase environment flag activation.
                                    for (String environment : environments) {
                                        String envGp1 = invariantEnv.get(environment);
                                        if (((envGp1.equals("PROD")) && tc.isActivePROD())
                                                || ((envGp1.equals("UAT")) && tc.isActiveUAT())
                                                || ((envGp1.equals("QA")) && tc.isActiveQA())
                                                || (envGp1.equals("DEV"))) {
                                            // Getting Application in order to check application type against browser.
                                            Application app = applicationService.convert(applicationService.readByKey(tc.getApplication()));
                                            if ((app != null) && (app.getType() != null) && app.getType().equalsIgnoreCase(Application.TYPE_GUI)) {
                                                for (String browser : browsers) {
                                                    try {
                                                        toInserts.add(inQueueFactoryService.create("", test, testCase, country.getCountry(), environment, robot, robot, robotIP, robotPort, browser, browserVersion,
                                                                platform, screenSize, manualURL, manualHost, manualContextRoot, manualLoginRelativeURL, manualEnvData, tag, screenshot, 0, verbose,
                                                                timeout, pageSource, seleniumLog, 0, 0, retries, manualExecution, priority, user, null, null, null));
                                                    } catch (FactoryCreationException e) {
                                                        LOG.error("Unable to insert record due to: " + e, e);
                                                        LOG.error("test: " + test + "-" + testCase + "-" + country.getCountry() + "-" + environment + "-" + robot);
                                                    }
                                                }
                                            } else {
                                                // Application does not support browser so we force an empty value.
                                                LOG.debug("Forcing Browser to empty value. Application type=" + app.getType());
                                                try {
                                                    toInserts.add(inQueueFactoryService.create("", test, testCase, country.getCountry(), environment, robot, robot, robotIP, robotPort, "", browserVersion,
                                                            platform, screenSize, manualURL, manualHost, manualContextRoot, manualLoginRelativeURL, manualEnvData, tag, screenshot, 0, verbose,
                                                            timeout, pageSource, seleniumLog, 0, 0, retries, manualExecution, priority, user, null, null, null));
                                                } catch (FactoryCreationException e) {
                                                    LOG.error("Unable to insert record due to: " + e, e);
                                                    LOG.error("test: " + test + "-" + testCase + "-" + country.getCountry() + "-" + environment + "-" + robot);
                                                }
                                            }
                                        } else {
                                            LOG.debug("Env group not active for testcase : " + environment);
                                        }
                                    }
                                } else {
                                    LOG.debug("Country does not match. " + countries + " " + country.getCountry());
                                }
                            }
                        } else {
                            LOG.debug("TestCase not Active.");
                        }
                    }
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
                        nbExe++;
                        JSONObject value = new JSONObject();
                        value.put("queueId", toInsert.getId());
                        value.put("test", toInsert.getTest());
                        value.put("testcase", toInsert.getTestCase());
                        value.put("country", toInsert.getCountry());
                        value.put("environment", toInsert.getEnvironment());

                        jsonArray.put(value);
                    } catch (CerberusException e) {
                        String errorMessageTmp = "Unable to insert " + toInsert.toString() + " due to " + e.getMessage();
                        LOG.warn(errorMessageTmp);
                        errorMessages.add(errorMessageTmp);
                        continue;
                    } catch (JSONException ex) {
                        LOG.error(ex, ex);
                    }
                }

                // Part 3 : Trigger JobQueue
                try {
                    executionThreadService.executeNextInQueueAsynchroneously(false);
                } catch (CerberusException ex) {
                    String errorMessageTmp = "Unable to feed the execution queue due to " + ex.getMessage();
                    LOG.warn(errorMessageTmp);
                    errorMessages.add(errorMessageTmp);
                }

                if (!errorMessages.isEmpty()) {
                    StringBuilder errorMessageTmp = new StringBuilder();
                    for (String item : errorMessages) {
                        errorMessageTmp.append(item);
                        errorMessageTmp.append(LINE_SEPARATOR);
                    }
                    errorMessage.append(errorMessageTmp.toString());
                }

                errorMessage.append(nbExe);
                errorMessage.append(" execution(s) succesfully inserted to queue. ");

                if (testcases.getResultMessage().getSource() == MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT) {
                    errorMessage.append(testcases.getResultMessage().getDescription());
                }

                // Message that everything went fine.
                msg = new MessageEvent(MessageEventEnum.GENERIC_OK);

            } else {
                // In case of errors, we display the help message.
//            errorMessage.append(helpMessage);
            }

            // Init Answer with potencial error from Parsing parameter.
            AnswerItem answer = new AnswerItem<>(msg);

            switch (outputFormat) {
                case "json":
                try {
                    JSONObject jsonResponse = new JSONObject();
                    jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
                    jsonResponse.put("message", errorMessage.toString());
                    jsonResponse.put("helpMessage", helpMessage);
                    jsonResponse.put("tag", tag);
                    jsonResponse.put("nbExe", nbExe);
                    jsonResponse.put("queueList", jsonArray);

                    response.setContentType("application/json");
                    response.setCharacterEncoding("utf8");
                    response.getWriter().print(jsonResponse.toString());
                } catch (JSONException e) {
                    LOG.warn(e);
                    //returns a default error message with the json format that is able to be parsed by the client-side
                    response.setContentType("application/json");
                    response.setCharacterEncoding("utf8");
                    response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
                }
                break;
                default:
                    response.setContentType("text");
                    response.setCharacterEncoding("utf8");
                    if (error) {
                        errorMessage.append("\n");
                        errorMessage.append(helpMessage);
                    }
                    response.getWriter().print(errorMessage.toString());
            }

        }

//        } catch (Exception e) {
//            LOG.error(e);
//            out.println(helpMessage);
//            out.println(e.toString());
//        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
