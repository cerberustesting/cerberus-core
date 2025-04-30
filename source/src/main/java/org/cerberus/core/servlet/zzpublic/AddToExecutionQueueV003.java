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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.Campaign;
import org.cerberus.core.crud.entity.CampaignParameter;
import org.cerberus.core.crud.entity.CountryEnvParam;
import org.cerberus.core.crud.entity.Robot;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseCountry;
import org.cerberus.core.crud.entity.TestCaseExecutionQueue;
import org.cerberus.core.crud.factory.IFactoryRobot;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecutionQueue;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.ICampaignParameterService;
import org.cerberus.core.crud.service.ICampaignService;
import org.cerberus.core.crud.service.ICountryEnvParamService;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.IRobotService;
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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.cerberus.core.crud.entity.CountryEnvironmentParameters;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.service.ICountryEnvironmentParametersService;

/**
 * Add a test case to the execution queue (so to be executed later).
 *
 * @author abourdon
 */
@WebServlet(name = "AddToExecutionQueueV003", description = "Add a test case to the execution queue.", urlPatterns = {"/AddToExecutionQueueV003"})
public class AddToExecutionQueueV003 extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(AddToExecutionQueueV003.class);

    private static final String PARAMETER_CAMPAIGN = "campaign";
    private static final String PARAMETER_TEST = "test";
    private static final String PARAMETER_TESTCASE = "testcase";
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
    private static final String PARAMETER_VIDEO = "video";
    private static final String PARAMETER_VERBOSE = "verbose";
    private static final String PARAMETER_TIMEOUT = "timeout";
    private static final String PARAMETER_PAGE_SOURCE = "pagesource";
    private static final String PARAMETER_ROBOT_LOG = "seleniumlog";
    private static final String PARAMETER_CONSOLE_LOG = "consolelog";
    private static final String PARAMETER_RETRIES = "retries";
    private static final String PARAMETER_MANUAL_EXECUTION = "manualexecution";
    private static final String PARAMETER_EXEPRIORITY = "priority";
    private static final String PARAMETER_OUTPUTFORMAT = "outputformat";
    private static final String PARAMETER_EXECUTOR = "executor";

    private static final String DEFAULT_VALUE_TAG = "";
    private static final int DEFAULT_VALUE_SCREENSHOT = 1;
    private static final int DEFAULT_VALUE_VIDEO = 0;
    private static final int DEFAULT_VALUE_MANUAL_URL = 0;
    private static final int DEFAULT_VALUE_VERBOSE = 1;
    private static final String DEFAULT_VALUE_TIMEOUT = "30000";
    private static final int DEFAULT_VALUE_PAGE_SOURCE = 1;
    private static final int DEFAULT_VALUE_ROBOT_LOG = 1;
    private static final int DEFAULT_VALUE_CONSOLE_LOG = 1;
    private static final int DEFAULT_VALUE_RETRIES = 0;
    private static final String DEFAULT_VALUE_MANUAL_EXECUTION = "N";
    private static final int DEFAULT_VALUE_PRIORITY = 1000;
    private static final String DEFAULT_VALUE_OUTPUTFORMAT = "compact";
    private static final String LOCAL_SEPARATOR = "_-_";

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
    private ICampaignService campaignService;
    private ICountryEnvParamService countryEnvParamService;
    private ICountryEnvironmentParametersService countryEnvironmentParametersService;
    private IRobotService robotService;
    private IFactoryRobot robotFactory;
    private IParameterService parameterService;
    private IAPIKeyService apiKeyService;
    private ITagService tagService;

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
        campaignService = appContext.getBean(ICampaignService.class);
        countryEnvParamService = appContext.getBean(ICountryEnvParamService.class);
        countryEnvironmentParametersService = appContext.getBean(ICountryEnvironmentParametersService.class);
        robotService = appContext.getBean(IRobotService.class);
        robotFactory = appContext.getBean(IFactoryRobot.class);
        parameterService = appContext.getBean(IParameterService.class);
        apiKeyService = appContext.getBean(IAPIKeyService.class);
        tagService = appContext.getBean(ITagService.class);

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
        logEventService.createForPublicCalls("/AddToExecutionQueueV003", "CALL", LogEvent.STATUS_INFO, "AddToExecutionQueueV003 called : " + request.getRequestURL(), request);

        if (apiKeyService.authenticate(request, response)) {
            // Parsing all parameters.
            // Execution scope parameters : Campaign, TestCases, Countries, Environment, Browser.
            String campaign = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter(PARAMETER_CAMPAIGN), null, charset);
            List<String> selectTest = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues(PARAMETER_TEST), null, charset);
            List<String> selectTestCase = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues(PARAMETER_TESTCASE), null, charset);
            List<String> countries = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues(PARAMETER_COUNTRY), null, charset);
            List<String> environments = ParameterParserUtil.parseListParamAndDecodeAndDeleteEmptyValue(request.getParameterValues(PARAMETER_ENVIRONMENT), null, charset);
            List<String> robots = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues(PARAMETER_ROBOT), new ArrayList<>(), charset);
            String tag = ParameterParserUtil.parseStringParam(request.getParameter(PARAMETER_TAG), DEFAULT_VALUE_TAG); // Execution parameters.
            String robotIP = ParameterParserUtil.parseStringParamAndDecode(request.getParameter(PARAMETER_ROBOT_IP), null, charset);
            String robotPort = ParameterParserUtil.parseStringParamAndDecode(request.getParameter(PARAMETER_ROBOT_PORT), null, charset);
            String browser = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter(PARAMETER_BROWSER), null, charset);
            String browserVersion = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter(PARAMETER_BROWSER_VERSION), null, charset);
            String platform = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter(PARAMETER_PLATFORM), null, charset);
            String screenSize = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter(PARAMETER_SCREENSIZE), null, charset);
            int manualURL = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter(PARAMETER_MANUAL_URL), DEFAULT_VALUE_MANUAL_URL, charset);
            String manualHost = ParameterParserUtil.parseStringParamAndDecode(request.getParameter(PARAMETER_MANUAL_HOST), null, charset);
            String manualContextRoot = ParameterParserUtil.parseStringParamAndDecode(request.getParameter(PARAMETER_MANUAL_CONTEXT_ROOT), null, charset);
            String manualLoginRelativeURL = ParameterParserUtil.parseStringParamAndDecode(request.getParameter(PARAMETER_MANUAL_LOGIN_RELATIVE_URL), null, charset);
            String manualEnvData = ParameterParserUtil.parseStringParamAndDecode(request.getParameter(PARAMETER_MANUAL_ENV_DATA), null, charset);
            String outputFormat = ParameterParserUtil.parseStringParamAndDecode(request.getParameter(PARAMETER_OUTPUTFORMAT), DEFAULT_VALUE_OUTPUTFORMAT, charset);
            String executor = ParameterParserUtil.parseStringParamAndDecode(request.getParameter(PARAMETER_EXECUTOR), null, charset);

            JSONArray countryJSONArray = new JSONArray(countries);
            JSONArray envJSONArray = new JSONArray(environments);

            try {
                tag = URLDecoder.decode(tag, "UTF-8");
            } catch (Exception ex) {
                // In case exception is raized, we keep the original string.
                LOG.debug("Failed to decode :'" + tag + "' - " + ex, ex);
            }

            int screenshot = DEFAULT_VALUE_SCREENSHOT;
            int video = DEFAULT_VALUE_VIDEO;
            int verbose = DEFAULT_VALUE_VERBOSE;
            String timeout = DEFAULT_VALUE_TIMEOUT;
            int pageSource = DEFAULT_VALUE_PAGE_SOURCE;
            int robotLog = DEFAULT_VALUE_ROBOT_LOG;
            int consoleLog = DEFAULT_VALUE_CONSOLE_LOG;
            int retries = DEFAULT_VALUE_RETRIES;
            String manualExecution = DEFAULT_VALUE_MANUAL_EXECUTION;
            int priority = DEFAULT_VALUE_PRIORITY;

            // The rest of the parameter depend on the campaign values.
            Campaign mCampaign = null;
            if (!StringUtil.isEmptyOrNull(campaign)) {
                mCampaign = campaignService.readByKey(campaign).getItem();
            }
            if (mCampaign == null) {
                // Campaign not defined or does not exist so we parse parameter from servlet query string or defaut values
                screenshot = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter(PARAMETER_SCREENSHOT), DEFAULT_VALUE_SCREENSHOT, charset);
                // Manage retrocompatibility in case the screenshot parameter is called with values 3 or 4 (should not be possible ).
                int defaultVideo = DEFAULT_VALUE_VIDEO;
                if (screenshot == 3) {
                    defaultVideo = 1;
                    screenshot = 1;
                }
                if (screenshot == 4) {
                    defaultVideo = 2;
                    screenshot = 2;
                }
                video = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter(PARAMETER_VIDEO), defaultVideo, charset);
                verbose = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter(PARAMETER_VERBOSE), DEFAULT_VALUE_VERBOSE, charset);
                pageSource = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter(PARAMETER_PAGE_SOURCE), DEFAULT_VALUE_PAGE_SOURCE, charset);
                robotLog = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter(PARAMETER_ROBOT_LOG), DEFAULT_VALUE_ROBOT_LOG, charset);
                consoleLog = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter(PARAMETER_CONSOLE_LOG), DEFAULT_VALUE_CONSOLE_LOG, charset);
                timeout = ParameterParserUtil.parseStringParamAndDecode(request.getParameter(PARAMETER_TIMEOUT), DEFAULT_VALUE_TIMEOUT, charset);
                retries = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter(PARAMETER_RETRIES), DEFAULT_VALUE_RETRIES, charset);
                manualExecution = ParameterParserUtil.parseStringParamAndDecode(request.getParameter(PARAMETER_MANUAL_EXECUTION), DEFAULT_VALUE_MANUAL_EXECUTION, charset);
                priority = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter(PARAMETER_EXEPRIORITY), DEFAULT_VALUE_PRIORITY, charset);
            } else {
                // Campaign defined and exist so we parse parameter from 1/ servlet 2/ campaign definition 3/ Servlet default values.
                screenshot = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter(PARAMETER_SCREENSHOT),
                        ParameterParserUtil.parseIntegerParamAndDecode(mCampaign.getScreenshot(), DEFAULT_VALUE_SCREENSHOT, charset), charset);
                video = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter(PARAMETER_VIDEO),
                        ParameterParserUtil.parseIntegerParamAndDecode(mCampaign.getVideo(), DEFAULT_VALUE_VIDEO, charset), charset);
                verbose = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter(PARAMETER_VERBOSE),
                        ParameterParserUtil.parseIntegerParamAndDecode(mCampaign.getVerbose(), DEFAULT_VALUE_VERBOSE, charset), charset);
                pageSource = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter(PARAMETER_PAGE_SOURCE),
                        ParameterParserUtil.parseIntegerParamAndDecode(mCampaign.getPageSource(), DEFAULT_VALUE_PAGE_SOURCE, charset), charset);
                robotLog = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter(PARAMETER_ROBOT_LOG),
                        ParameterParserUtil.parseIntegerParamAndDecode(mCampaign.getRobotLog(), DEFAULT_VALUE_ROBOT_LOG, charset), charset);
                consoleLog = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter(PARAMETER_CONSOLE_LOG),
                        ParameterParserUtil.parseIntegerParamAndDecode(mCampaign.getConsoleLog(), DEFAULT_VALUE_CONSOLE_LOG, charset), charset);
                timeout = ParameterParserUtil.parseStringParamAndDecode(request.getParameter(PARAMETER_TIMEOUT),
                        ParameterParserUtil.parseStringParamAndDecode(mCampaign.getTimeout(), DEFAULT_VALUE_TIMEOUT, charset), charset);
                retries = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter(PARAMETER_RETRIES),
                        ParameterParserUtil.parseIntegerParamAndDecode(mCampaign.getRetries(), DEFAULT_VALUE_RETRIES, charset), charset);
                manualExecution = ParameterParserUtil.parseStringParamAndDecode(request.getParameter(PARAMETER_MANUAL_EXECUTION),
                        ParameterParserUtil.parseStringParamAndDecode(mCampaign.getManualExecution(), DEFAULT_VALUE_MANUAL_EXECUTION, charset), charset);
                priority = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter(PARAMETER_EXEPRIORITY),
                        ParameterParserUtil.parseIntegerParamAndDecode(mCampaign.getPriority(), DEFAULT_VALUE_PRIORITY, charset), charset);
            }

            // Defining help message.
            String helpMessage = "This servlet is used to add to Cerberus execution queue a list of execution. Execution list will be calculated from cartesian product of "
                    + "testcase, country, environment and browser list. Those list can be defined from the associated servlet parameter but can also be defined from campaign directy inside Cerberus.\n"
                    + "List defined from servlet overwrite the list defined from the campaign. All other execution parameters will be taken to each execution.\n"
                    + "Available parameters:\n"
                    + "- " + PARAMETER_CAMPAIGN + " : Campaign name from which testcase, countries, environment and browser can be defined from Cerberus. [" + campaign + "]\n"
                    + "- " + PARAMETER_TEST + " : List of test to trigger. That list overwrite the list coming from the Campaign (if defined).. [" + selectTest + "]\n"
                    + "- " + PARAMETER_TESTCASE + " : List of testCase to trigger. That list overwrite the list coming from the Campaign (if defined).. [" + selectTestCase + "]\n"
                    + "- " + PARAMETER_COUNTRY + " : List of countries to trigger. That list overwrite the list coming from the Campaign (if defined).. [" + countries + "]\n"
                    + "- " + PARAMETER_ENVIRONMENT + " : List of environment to trigger. That list overwrite the list coming from the Campaign (if defined).. [" + environments + "]\n"
                    + "- " + PARAMETER_ROBOT + " : List of robot to trigger. That list overwrite the list coming from the Campaign (if defined).. [" + robots + "]\n"
                    + "- " + PARAMETER_ROBOT_IP + " : Robot IP that will be used for every execution triggered. [" + robotIP + "]\n"
                    + "- " + PARAMETER_ROBOT_PORT + " : Robot Port that will be used for every execution triggered. [" + robotPort + "]\n"
                    + "- " + PARAMETER_BROWSER + " : Browser that will be used for every execution triggered. [" + browser + "]\n"
                    + "- " + PARAMETER_BROWSER_VERSION + " : Browser Version that will be used for every execution triggered. [" + browserVersion + "]\n"
                    + "- " + PARAMETER_PLATFORM + " : Platform that will be used for every execution triggered. [" + platform + "]\n"
                    + "- " + PARAMETER_SCREENSIZE + " : Size of the screen that will be used for every execution triggered. [" + screenSize + "]\n"
                    + "- " + PARAMETER_MANUAL_URL + " : Activate (1) or not (0) or Override (2) the Manual URL of the application to execute. If Activated (1) the 4 parameters after are necessary. If Override (2) at least 1 parameters after are necessary (other parameters will use cerberus values) [" + manualURL + "]\n"
                    + "- " + PARAMETER_MANUAL_HOST + " : Host of the application to test (only used when " + PARAMETER_MANUAL_URL + " is activated or override). [" + manualHost + "].   Manual host can be  `applicationname1:manualhost1;applicationname2:manualhost2;...` or just 'manualHost1'\n"
                    + "- " + PARAMETER_MANUAL_CONTEXT_ROOT + " : Context root of the application to test (only used when " + PARAMETER_MANUAL_URL + " is activated or override). [" + manualContextRoot + "]\n"
                    + "- " + PARAMETER_MANUAL_LOGIN_RELATIVE_URL + " : Relative login URL of the application (only used when " + PARAMETER_MANUAL_URL + " is activated or override). [" + manualLoginRelativeURL + "]\n"
                    + "- " + PARAMETER_MANUAL_ENV_DATA + " : Environment where to get the test data when a " + PARAMETER_MANUAL_URL + " is defined. (only used when manualURL is active or override). [" + manualEnvData + "]\n"
                    + "- " + PARAMETER_TAG + " : Tag that will be used for every execution triggered. [" + tag + "]\n"
                    + "- " + PARAMETER_SCREENSHOT + " : Activate or not the screenshots for every execution triggered. [" + screenshot + "]\n"
                    + "- " + PARAMETER_VIDEO + " : Activate or not the video for every execution triggered. [" + video + "]\n"
                    + "- " + PARAMETER_VERBOSE + " : Verbose level for every execution triggered. [" + verbose + "]\n"
                    + "- " + PARAMETER_TIMEOUT + " : Timeout used for the action that will be used for every execution triggered. [" + timeout + "]\n"
                    + "- " + PARAMETER_PAGE_SOURCE + " : Record Page Source during for every execution triggered. [" + pageSource + "]\n"
                    + "- " + PARAMETER_ROBOT_LOG + " : Get the Robot Logs at the end of the execution for every execution triggered. [" + robotLog + "]\n"
                    + "- " + PARAMETER_CONSOLE_LOG + " : Get the Console Log at the end of the execution for every execution triggered. [" + consoleLog + "]\n"
                    + "- " + PARAMETER_MANUAL_EXECUTION + " : Execute testcase in manual mode for every execution triggered. [" + manualExecution + "]\n"
                    + "- " + PARAMETER_RETRIES + " : Number of tries if the result is not OK for every execution triggered. [" + retries + "]\n"
                    + "- " + PARAMETER_EXEPRIORITY + " : Priority that will be used in the queue for every execution triggered. [" + priority + "]\n"
                    + "- " + PARAMETER_OUTPUTFORMAT + " : Format of the servlet output. can be compact, json [" + outputFormat + "]\n"
                    + "- " + PARAMETER_EXECUTOR + " : Name of the user who trigger the execution. Value only used if servlet call is not authenticated [" + executor + "]\n";

            // Checking the parameter validity.
            StringBuilder errorMessage = new StringBuilder();
            boolean error = false;

            if ((tag == null || tag.isEmpty()) && mCampaign != null && !StringUtil.isEmptyOrNull(mCampaign.getTag())) {
                tag = mCampaign.getTag();
            } else if (tag == null || tag.isEmpty()) {
                if (request.getRemoteUser() != null) {
                    tag = request.getRemoteUser() + ".";
                }
                if (campaign != null) {
                    tag += campaign + ".";
                }
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                String mytimestamp = sdf.format(timestamp);
                tag += mytimestamp;

            } else if (tag.length() > 255) {

                errorMessage.append("Error - Parameter ").append(PARAMETER_TAG).append(" is too big. Maximum size if 255. Current size is : ").append(tag.length()).append("\n");
                error = true;
            }

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String mytimestamp = sdf.format(timestamp);
            String myuser = ServletUtil.getUser(request);
            LOG.debug("User : " + myuser);
            LOG.debug("Executor : " + executor);
            String reqEnvironments = StringUtil.convertToString(environments, parameterService.getParameterStringByKey("cerberus_tagvariable_separator", "", "-"));
            String reqCountries = StringUtil.convertToString(countries, parameterService.getParameterStringByKey("cerberus_tagvariable_separator", "", "-"));

            if (campaign != null && !campaign.isEmpty()) {
                final AnswerItem<Map<String, List<String>>> parsedCampaignParameters = campaignParameterService.parseParametersByCampaign(campaign);
                if (parsedCampaignParameters.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    // Parameters from campaign could be retreived. we can now replace the parameters comming from the calls in case they are still not defined.
                    // If parameters are already defined from request, we ignore the campaign values.
                    if (countries == null || countries.isEmpty()) {
                        countries = parsedCampaignParameters.getItem().get(CampaignParameter.COUNTRY_PARAMETER);
                        countryJSONArray = new JSONArray(countries);
                        reqCountries = StringUtil.convertToString(countries, parameterService.getParameterStringByKey("cerberus_tagvariable_separator", "", "-"));
                    }
                    if (environments == null || environments.isEmpty()) {
                        environments = parsedCampaignParameters.getItem().get(CampaignParameter.ENVIRONMENT_PARAMETER);
                        envJSONArray = new JSONArray(environments);
                        reqEnvironments = StringUtil.convertToString(environments, parameterService.getParameterStringByKey("cerberus_tagvariable_separator", "", "-"));
                    }
                    if (robots == null || robots.isEmpty()) {
                        robots = parsedCampaignParameters.getItem().get(CampaignParameter.ROBOT_PARAMETER);
                    }
                }
                if ((countries != null) && (selectTest == null || selectTest.isEmpty())) {
                    // If no countries are found, there is no need to get the testcase list. None will be returned. We will report an error later on.
                    selectTest = new ArrayList<>();
                    selectTestCase = new ArrayList<>();
                    testcases = testCaseService.findTestCaseByCampaignNameAndCountries(campaign, countries.toArray(new String[countries.size()]));

                    if (testcases != null) {
                        if (testcases.getDataList() != null) {
                            for (TestCase campaignTestCase : testcases.getDataList()) {
                                selectTest.add(campaignTestCase.getTest());
                                selectTestCase.add(campaignTestCase.getTestcase());
                            }
                        } else {
                            errorMessage.append("Error - ").append(testcases.getMessageDescription()).append("\n");
                            error = true;
                        }
                    }
                }
            }
            tag = tag
                    .replace("%TIMESTAMP%", mytimestamp)
                    .replace("%USER%", myuser)
                    .replace("%REQCOUNTRYLIST%", reqCountries)
                    .replace("%REQENVIRONMENTLIST%", reqEnvironments);

            if (countries == null || countries.isEmpty()) {
                errorMessage.append("Error - No Country defined. You can either feed it with parameter '" + PARAMETER_COUNTRY + "' or add it into the campaign definition.\n");
                error = true;
            }
            if (selectTest == null || selectTest.isEmpty() || selectTestCase == null || selectTestCase.isEmpty()) {
                errorMessage.append("Error - No TestCases defined. You can either feed it with parameters '" + PARAMETER_TEST + "' and '" + PARAMETER_TESTCASE + "' or add it into the campaign definition.\n");
                error = true;
            }
            if ((selectTest != null) && (selectTestCase != null) && (selectTest.size() != selectTestCase.size())) {
                errorMessage.append("Error - Test list size (").append(selectTest.size()).append(") is not the same as testcase list size (").append(selectTestCase.size()).append("). Please check that both list are consistent.\n");
                error = true;
            }
            if (manualURL == 1) {
                if (manualHost == null || manualEnvData == null) {
                    errorMessage.append("Error - ManualURL has been activated but no ManualHost or Manual Environment defined.\n");
                    error = true;
                }
            } else if (environments == null || environments.isEmpty()) {
                errorMessage.append("Error - No Environment defined (and " + PARAMETER_MANUAL_URL + " not activated). You can either feed it with parameter '" + PARAMETER_ENVIRONMENT + "' or add it into the campaign definition.\n");
                error = true;
            }

            int nbExe = 0;
            JSONArray jsonArray = new JSONArray();
            String user = myuser;
            user = StringUtil.isEmptyOrNull(user) && !StringUtil.isEmptyOrNull(executor) ? executor : user;

            int nbtestcasenotactive = 0;
            int nbtestcasenotexist = 0;
            int nbtestcaseenvgroupnotallowed = 0;
            int nbenvnotexist = 0;
            int nbrobotmissing = 0;
            boolean tagAlreadyAdded = false;

            Map<String, String> myHostMap;
            myHostMap = getManualHostMap(manualHost);

            int nbrobot = 0;
            Map<String, Robot> robotsMap = new HashMap<>();
            if (StringUtil.isEmptyOrNull(robotIP)) {
                if (robots == null || robots.isEmpty()) {
                    // RobotIP is not defined and no robot are provided so the content is probably testcases that does not require robot definition.
                    if (manualExecution.equalsIgnoreCase("Y") || manualExecution.equalsIgnoreCase("A")) {
                        robotIP = "manual";
                        robotsMap.put("", robotFactory.create(0, "", platform, browser, "", true, "", "", "", screenSize, "", 0, "", true, browser, ""));
                    }
                    nbrobot = 1;
                } else {
                    // Not RobotIP defined but at least 1 robot has been found from servlet call or campaign definition.
                    nbrobot = robots.size();
                    try {
                        // Load the map of robot from input.
                        robotsMap = robotService.readToHashMapByRobotList(robots); // load Robots available for the campaign
                        nbrobot = robotsMap.size();
                    } catch (CerberusException ex) {
                        LOG.warn(ex.toString(), ex);
                    }
                }
            } else {
                // When RobotIP is feeded, we do not consider the robot definition.
                LOG.debug("Adding fake Robot.");
                nbrobot = 1;
                robots = new ArrayList<>();
                robots.add("");
                robotsMap.put("", robotFactory.create(0, "", platform, browser, "", true, "", "", "", screenSize, "", 0, "", true, browser, ""));
            }

            HashMap<String, Application> appMap = new HashMap<>();
            List<String> errorMessages = new ArrayList<>();

            // Starting the request only if previous parameters exist.
            if (!error) {
                if (environments == null) {
                    environments = new ArrayList<>();
                    environments.add("MANUAL");
                }
                int nbenv = environments.size();
                int nbcountries = countries.size();

                // Part 0: Load to memory Environments and robots.
                Map<String, String> invariantEnvMap = invariantService.readToHashMapGp1StringByIdname("ENVIRONMENT", "");
                invariantEnvMap.put("MANUAL", "");

                // Part 1: Getting all possible Execution from test cases + countries + environments + browsers which have been sent to this servlet.
                List<TestCaseExecutionQueue> toInserts = new ArrayList<>();
                try {

                    HashMap<String, CountryEnvParam> envMap = new HashMap<>();
                    LOG.debug("Loading all environments.");
                    for (CountryEnvParam envParam : countryEnvParamService.convert(countryEnvParamService.readActiveBySystem(null))) {
                        envMap.put(envParam.getSystem() + LOCAL_SEPARATOR + envParam.getCountry() + LOCAL_SEPARATOR + envParam.getEnvironment(), envParam);
                    }

                    HashMap<String, CountryEnvironmentParameters> envAppMap = new HashMap<>();
                    LOG.debug("Loading all environments.");
                    for (CountryEnvironmentParameters envAppParam : countryEnvironmentParametersService.convert(countryEnvironmentParametersService.readByVarious(null, null, null, null))) {
                        if (envAppParam.isActive()) {
                            envAppMap.put(envAppParam.getSystem() + LOCAL_SEPARATOR + envAppParam.getCountry() + LOCAL_SEPARATOR + envAppParam.getEnvironment() + LOCAL_SEPARATOR + envAppParam.getApplication(), envAppParam);
                        }
                    }

                    if (!StringUtil.isEmptyOrNull(campaign)) {
                        LOG.info("Campaign [" + campaign + "] asked to triggered.");
                        LOG.info(" [" + campaign + "] tests : " + selectTest);
                        LOG.info(" [" + campaign + "] testcases : " + selectTestCase);
                        LOG.info(" [" + campaign + "] environments : " + environments);
                        LOG.info(" [" + campaign + "] countries : " + countries);
                        LOG.info(" [" + campaign + "] robots : " + robotsMap);
                    }
                    LOG.debug("Nb of TestCase : " + selectTest.size());
                    for (int i = 0; i < selectTest.size(); i++) {

                        String test = selectTest.get(i);
                        String testCase = selectTestCase.get(i);
                        TestCase tc = testCaseService.convert(testCaseService.readByKey(test, testCase));
                        // TestCases that are not active are not inserted into queue.
                        if (tc != null) {

                            if (tc.isActive()) {
                                // We only insert testcase that exist for the given country.
                                for (TestCaseCountry country : testCaseCountryService.convert(testCaseCountryService.readByTestTestCase(null, test, testCase, null))) {
                                    if (countries.contains(country.getCountry())) {
                                        // for each environment we test that correspondng gp1 is compatible with testcase environment flag activation.
                                        for (String environment : environments) {
                                            String envGp1 = invariantEnvMap.get(environment);
                                            if (((envGp1.equals("PROD")) && tc.isActivePROD())
                                                    || ((envGp1.equals("UAT")) && tc.isActiveUAT())
                                                    || ((envGp1.equals("QA")) && tc.isActiveQA())
                                                    || (envGp1.equals("DEV"))
                                                    || (envGp1.isEmpty())) {
                                                // Getting Application in order to check application type against browser.
                                                appMap = updateMapWithApplication(tc.getApplication(), appMap);
                                                Application app = appMap.get(tc.getApplication());
                                                if ((envMap.containsKey(app.getSystem() + LOCAL_SEPARATOR + country.getCountry() + LOCAL_SEPARATOR + environment))
                                                        || (environment.equals("MANUAL"))) {

                                                    if ((envAppMap.containsKey(app.getSystem() + LOCAL_SEPARATOR + country.getCountry() + LOCAL_SEPARATOR + environment + LOCAL_SEPARATOR + app.getApplication()))
                                                            || (environment.equals("MANUAL"))) {

                                                        // Create Tag only if not already done and defined.
                                                        if (!StringUtil.isEmptyOrNull(tag) && !tagAlreadyAdded) {
                                                            // We create or update it.
                                                            tagService.createAuto(tag, campaign, user, envJSONArray, countryJSONArray);
                                                            tagAlreadyAdded = true;
                                                        }

                                                        // manage manual host for this execution
                                                        String manualHostforThisApplication = getManualHostForThisApplication(myHostMap, app.getApplication());

                                                        if ((app != null)
                                                                && (app.getType() != null)
                                                                && (app.getType().equalsIgnoreCase(Application.TYPE_GUI) || app.getType().equalsIgnoreCase(Application.TYPE_APK)
                                                                || app.getType().equalsIgnoreCase(Application.TYPE_IPA) || app.getType().equalsIgnoreCase(Application.TYPE_FAT))) {

                                                            for (Map.Entry<String, Robot> entry : robotsMap.entrySet()) {
                                                                String key = entry.getKey();
                                                                Robot robot = entry.getValue();

                                                                try {
                                                                    if ("".equals(robot.getType()) || app.getType().equals(robot.getType())) {
                                                                        // Robot type is not feeded (not attached to any techno) or robot type match the one of the application.
                                                                        LOG.debug("Insert Queue Entry.");
                                                                        // We get here the corresponding robotDecli value from robot.
                                                                        String robotDecli = robot.getRobotDecli();
                                                                        if (StringUtil.isEmptyOrNull(robotDecli)) {
                                                                            robotDecli = robot.getRobot();
                                                                        }
                                                                        if ("".equals(robot.getRobot()) && StringUtil.isEmptyOrNull(robotIP)) {
                                                                            // We don't insert the execution for robot application that have no robot and robotIP defined.
                                                                            nbrobotmissing++;
                                                                        } else {
                                                                            toInserts.add(inQueueFactoryService.create(app.getSystem(),
                                                                                    test, testCase, country.getCountry(), environment,
                                                                                    robot.getRobot(), robotDecli, robotIP, robotPort, browser,
                                                                                    browserVersion, platform, screenSize, manualURL,
                                                                                    manualHostforThisApplication, manualContextRoot,
                                                                                    manualLoginRelativeURL, manualEnvData, tag,
                                                                                    screenshot, video, verbose, timeout, pageSource,
                                                                                    robotLog, consoleLog, 0, retries, manualExecution, priority,
                                                                                    user, null, null, null));
                                                                        }
                                                                    } else {
                                                                        LOG.debug("Not inserted because app type '" + app.getType() + "' does not match robot type '" + robot.getType() + "'.");
                                                                    }
                                                                } catch (FactoryCreationException e) {
                                                                    LOG.error("Unable to insert record due to: " + e, e);
                                                                    LOG.error("test: " + test + "-" + testCase + "-" + country.getCountry() + "-" + environment + "-" + robots);
                                                                }
                                                            }

                                                        } else {
                                                            // Application does not support robot so we force an empty value.
                                                            LOG.debug("Forcing Robot to empty value. Application type=" + app.getType());
                                                            try {
                                                                LOG.debug("Insert Queue Entry.");
                                                                toInserts.add(inQueueFactoryService.create(app.getSystem(), test,
                                                                        testCase, country.getCountry(), environment, "", "", "", "",
                                                                        "", "", "", "", manualURL, manualHostforThisApplication, manualContextRoot,
                                                                        manualLoginRelativeURL, manualEnvData, tag, screenshot, video,
                                                                        verbose, timeout, pageSource, robotLog, consoleLog, 0, retries,
                                                                        manualExecution, priority, user, null, null, null));
                                                            } catch (FactoryCreationException e) {
                                                                LOG.error("Unable to insert record due to: " + e, e);
                                                                LOG.error("test: " + test + "-" + testCase + "-" + country.getCountry() + "-" + environment + "-" + robots);
                                                            }
                                                        }
                                                    } else {
                                                        LOG.debug("Env Application does not exist or is not active.");
//                                                messages.add(String.format("Environment '%s' and Country %s doesn't exist for application '%s'", environment, country.getCountry(), tc.getApplication()));
                                                        nbenvnotexist += nbrobot;
                                                    }
                                                } else {
                                                    LOG.debug("Env does not exist or is not active.");
                                                    nbenvnotexist += nbrobot;
                                                }

                                            } else {
                                                LOG.debug("Env group not active for testcase : " + environment);
                                                nbtestcaseenvgroupnotallowed += nbrobot;
                                            }
                                        }
                                    } else {
                                        LOG.debug("Country does not match. " + countries + " " + country.getCountry());
                                    }
                                }
                            } else {
                                LOG.debug("TestCase not Active.");
                                nbtestcasenotactive += (nbcountries * nbenv * nbrobot);
                            }
                        } else {
                            LOG.debug("TestCase not exist.");
                            nbtestcasenotexist += (nbcountries * nbenv * nbrobot);
                            String errorMessageTmp = "Testcase " + test + "-" + testCase + " does not exist !!";
                            LOG.warn(errorMessageTmp);
                            errorMessages.add(errorMessageTmp);
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

                // Part 2a: Try to insert all these test cases to the execution queue.
                List<Long> queueInsertedIds = new ArrayList<>();
                for (TestCaseExecutionQueue toInsert : toInserts) {
                    try {
                        inQueueService.convert(inQueueService.create(toInsert, true, 0, TestCaseExecutionQueue.State.QUTEMP, testCasesInserted));
                        nbExe++;
                        JSONObject value = new JSONObject();
                        value.put("queueId", toInsert.getId());
                        value.put("test", toInsert.getTest());
                        value.put("testcase", toInsert.getTestCase());
                        value.put("country", toInsert.getCountry());
                        value.put("environment", toInsert.getEnvironment());
                        queueInsertedIds.add(toInsert.getId());

                        jsonArray.put(value);
                    } catch (CerberusException e) {
                        String errorMessageTmp = "Unable to insert " + toInsert.toString() + " due to " + e.getMessage();
                        LOG.warn(errorMessageTmp);
                        errorMessages.add(errorMessageTmp);
                    } catch (JSONException ex) {
                        LOG.error(ex, ex);
                    }
                }

                // Part 2b: move all the execution queue from tag to QUEUE state.
                inQueueService.updateAllTagToQueuedFromQuTemp(tag, queueInsertedIds);

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
                errorMessage.append(" execution(s) successfully inserted to queue. ");

                if (testcases != null && testcases.getResultMessage().getSource() == MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT) {
                    errorMessage.append(testcases.getResultMessage().getDescription());
                }

                // Message that everything went fine.
                msg = new MessageEvent(MessageEventEnum.GENERIC_OK);

            } else {
                LOG.info("No execution triggered from AddToExecutionQueue : " + errorMessage.toString());
            }

            // Init Answer with potential error from Parsing parameter.
            AnswerItem answer = new AnswerItem<>(msg);

            switch (outputFormat) {
                case "json":
                    try {
                    JSONObject jsonResponse = new JSONObject();
                    jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
                    jsonResponse.put("message", errorMessage.toString());
                    if (error) {
                        // Only display help message if error.
                        jsonResponse.put("helpMessage", helpMessage);
                    }
                    jsonResponse.put("tag", tag);
                    jsonResponse.put("nbExe", nbExe);
                    jsonResponse.put("nbErrorTCNotActive", nbtestcasenotactive);
                    jsonResponse.put("nbErrorTCNotExist", nbtestcasenotexist);
                    jsonResponse.put("nbErrorTCNotAllowedOnEnv", nbtestcaseenvgroupnotallowed);
                    jsonResponse.put("nbErrorEnvNotExistOrNotActive", nbenvnotexist);
                    jsonResponse.put("nbErrorRobotMissing", nbrobotmissing);
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
            Date date1 = new Date();
            LOG.debug("TOTAL Duration : " + (date1.getTime() - requestDate.getTime()));
        }
    }

    private HashMap<String, Application> updateMapWithApplication(String application, HashMap<String, Application> appMap) throws CerberusException {
        if (!appMap.containsKey(application)) {
            Application app = applicationService.convert(applicationService.readByKey(application));
            appMap.put(application, app);
        }
        return appMap;
    }

    /**
     * @param manualHost
     * @param application
     * @return
     */
    private String getManualHostForThisApplication(Map<String, String> manualHost, String application) {

        if (manualHost.containsKey("")) {
            return manualHost.get("");
        }
        if (manualHost.containsKey(application)) {
            return manualHost.get(application);
        }
        return "";
    }

    /**
     * Convert manualhost parameter to MAP. manual host can be just
     * 'manualHost1' (case 1) or
     * `applicationname1:manualhost1;applicationname2:manualhost2;...` (cases 2)
     * or a json string in format : { "applicationname1" : "manualhost1",
     * "applicationname2" : "manualhost2" } (case 3)
     *
     * @param manualHost
     * @return a Map of application : url
     */
    private Map<String, String> getManualHostMap(String manualHost) {
        Map<String, String> myHostMap = new HashMap<>();
        if (StringUtil.isEmptyOrNull(manualHost)) {
            LOG.debug("Converting from empty.");
            myHostMap.put("", "");
            return myHostMap;
        }
        try {
            JSONObject myJSONObj = new JSONObject(manualHost);
            Iterator<?> nameItr = myJSONObj.keys();
            LOG.debug("Converting from JSON.");
            while (nameItr.hasNext()) {
                String name = (String) nameItr.next();
                myHostMap.put(name, myJSONObj.getString(name));
            }
            return myHostMap;
        } catch (JSONException ex) {
            // parameter could not be converted to JSON Array so we try with the : and ; separators.
            String newManualHost = "";
            // Remove the http:// and https:// in order to avoid conflict with : split that will be done
            if (!StringUtil.isEmptyOrNull(manualHost)) {
                newManualHost = manualHost.replace("http://", "|ZZZHTTPZZZ|");
                newManualHost = newManualHost.replace("https://", "|ZZZHTTPSZZZ|");
            }
            if (!StringUtil.isEmptyOrNull(manualHost) && !newManualHost.contains(":")) {
                LOG.debug("Converting from string.");
                myHostMap.put("", manualHost);
                return myHostMap; // if no :, just return manual host (case 1)
            }
            // (case 2)
            if (!StringUtil.isEmptyOrNull(manualHost)) {
                LOG.debug("Converting from separator.");
                String[] manualHostByApp = newManualHost.split(";");
                for (String appManualHost : manualHostByApp) {
                    String[] appAndHost = appManualHost.split(":");
                    myHostMap.put(appAndHost[0], appAndHost[1].replace("|ZZZHTTPZZZ|", "http://").replace("|ZZZHTTPSZZZ|", "https://"));
                }
                return myHostMap;
            }
        }
        return myHostMap;
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
