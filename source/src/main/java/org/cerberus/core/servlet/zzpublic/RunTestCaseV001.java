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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionQueue;
import org.cerberus.core.crud.factory.IFactoryTestCase;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecution;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecutionQueue;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.engine.entity.ExecutionUUID;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.engine.execution.IRunTestCaseService;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.FactoryCreationException;
import org.cerberus.core.service.authentification.IAPIKeyService;
import org.cerberus.core.util.DateUtil;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerUtil;
import org.cerberus.core.util.servlet.ServletUtil;
import org.cerberus.core.version.Infos;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 25/01/2013
 * @since 0.9.0
 */
@WebServlet(name = "RunTestCaseV001", urlPatterns = {"/RunTestCaseV001"})
public class RunTestCaseV001 extends HttpServlet {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(RunTestCaseV001.class);

    public static final String SERVLET_URL = "/RunTestCaseV001";

    public static final String PARAMETER_TEST = "Test";
    public static final String PARAMETER_TEST_CASE = "TestCase";
    public static final String PARAMETER_COUNTRY = "Country";
    public static final String PARAMETER_ENVIRONMENT = "Environment";
    public static final String PARAMETER_ROBOT = "robot";
    public static final String PARAMETER_ROBOTEXECUTOR = "robotexecutor";
    public static final String PARAMETER_ROBOT_IP = "ss_ip";
    public static final String PARAMETER_ROBOT_PORT = "ss_p";
    public static final String PARAMETER_BROWSER = "browser";
    public static final String PARAMETER_BROWSER_VERSION = "version";
    public static final String PARAMETER_PLATFORM = "platform";
    public static final String PARAMETER_MANUAL_URL = "manualURL";
    public static final String PARAMETER_MANUAL_HOST = "myhost";
    public static final String PARAMETER_MANUAL_CONTEXT_ROOT = "mycontextroot";
    public static final String PARAMETER_MANUAL_LOGIN_RELATIVE_URL = "myloginrelativeurl";
    public static final String PARAMETER_MANUAL_ENV_DATA = "myenvdata";
    public static final String PARAMETER_TAG = "Tag";
    public static final String PARAMETER_OUTPUT_FORMAT = "outputformat";
    public static final String PARAMETER_SCREENSHOT = "screenshot";
    public static final String PARAMETER_VERBOSE = "verbose";
    public static final String PARAMETER_TIMEOUT = "timeout";
    public static final String PARAMETER_SYNCHRONEOUS = "synchroneous";
    public static final String PARAMETER_PAGE_SOURCE = "pageSource";
    public static final String PARAMETER_SELENIUM_LOG = "seleniumLog";
    public static final String PARAMETER_MANUAL_EXECUTION = "manualExecution";
    public static final String PARAMETER_EXECUTION_QUEUE_ID = "IdFromQueue";
    public static final String PARAMETER_SYSTEM = "MySystem";
    public static final String PARAMETER_NUMBER_OF_RETRIES = "retries";
    public static final String AUTOMATIC_RUN = "autoRun";
    public static final String PARAMETER_SCREEN_SIZE = "screenSize";
    public static final String PARAMETER_EXECUTOR = "executor";

    private IAPIKeyService apiKeyService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            PrintWriter out = response.getWriter();

            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            apiKeyService = appContext.getBean(IAPIKeyService.class);

            // Calling Servlet Transversal Util.
            ServletUtil.servletStart(request);

            /**
             * Adding Log entry.
             */
            ILogEventService logEventService = appContext.getBean(ILogEventService.class);
            logEventService.createForPublicCalls("/RunTestCaseV001", "CALL", LogEvent.STATUS_INFO, "RunTestCaseV001 called : " + request.getRequestURL(), request);

            if (apiKeyService.authenticate(request, response)) {

                //Tool
                String robot = "";
                String robotExecutor = "";
                String robotHost = ""; // Selenium IP
                String robotPort = ""; // Selenium Port
                String browser = "";
                String version = "";
                String platform = "";
                String active = "";
                String timeout = "";
                String screenSize = "";
                boolean synchroneous = true;
                int getPageSource = 0;
                int getRobotLog = 0;
                String manualExecution = "N";

                //Test
                String test = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter(PARAMETER_TEST), "");
                String testCase = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter(PARAMETER_TEST_CASE), "");
                String country = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter(PARAMETER_COUNTRY), "");
                String environment = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter(PARAMETER_ENVIRONMENT), "");

                //Test Dev Environment
                boolean manualURL = ParameterParserUtil.parseBooleanParam(request.getParameter(PARAMETER_MANUAL_URL), false);
                String myHost = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter(PARAMETER_MANUAL_HOST), "");
                String myContextRoot = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter(PARAMETER_MANUAL_CONTEXT_ROOT), "");
                String myLoginRelativeURL = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter(PARAMETER_MANUAL_LOGIN_RELATIVE_URL), "");
                //TODO find another solution
                myLoginRelativeURL = myLoginRelativeURL.replace("&#61;", "=");
                String myEnvData = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter(PARAMETER_MANUAL_ENV_DATA), "");

                //Execution
                String tag = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter(PARAMETER_TAG), "");
                String outputFormat = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter(PARAMETER_OUTPUT_FORMAT), "compact");
                int screenshot = ParameterParserUtil.parseIntegerParam(request.getParameter(PARAMETER_SCREENSHOT), 1);
                int verbose = ParameterParserUtil.parseIntegerParam(request.getParameter(PARAMETER_VERBOSE), 1);
                timeout = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter(PARAMETER_TIMEOUT), "");
                synchroneous = ParameterParserUtil.parseBooleanParam(request.getParameter(PARAMETER_SYNCHRONEOUS), false);
                getPageSource = ParameterParserUtil.parseIntegerParam(request.getParameter(PARAMETER_PAGE_SOURCE), 1);
                getRobotLog = ParameterParserUtil.parseIntegerParam(request.getParameter(PARAMETER_SELENIUM_LOG), 1);
                manualExecution = ParameterParserUtil.parseStringParam(request.getParameter(PARAMETER_MANUAL_EXECUTION), "N");
                int numberOfRetries = ParameterParserUtil.parseIntegerParam(request.getParameter(PARAMETER_NUMBER_OF_RETRIES), 0);
                screenSize = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter(PARAMETER_SCREEN_SIZE), "");

                robot = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter(PARAMETER_ROBOT), "");
                robotHost = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter(PARAMETER_ROBOT_IP), "");
                robotPort = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter(PARAMETER_ROBOT_PORT), "");
                robotExecutor = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter(PARAMETER_ROBOTEXECUTOR), "");
                browser = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("Browser"), ParameterParserUtil.parseStringParamAndSanitize(request.getParameter(PARAMETER_BROWSER), ""));
                version = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter(PARAMETER_BROWSER_VERSION), "");
                platform = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter(PARAMETER_PLATFORM), "");

                // hidden parameters.
                long idFromQueue = ParameterParserUtil.parseIntegerParam(request.getParameter("IdFromQueue"), 0);
                String executor = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter(PARAMETER_EXECUTOR), ParameterParserUtil.parseStringParamAndSanitize(request.getRemoteUser(), ""));

                String helpMessage = "\nThis servlet is used to start the execution of a test case.\n"
                        + "Parameter list :\n"
                        + "- " + PARAMETER_TEST + " [mandatory] : Test to execute. [" + test + "]\n"
                        + "- " + PARAMETER_TEST_CASE + " [mandatory] : Test Case reference to execute. [" + testCase + "]\n"
                        + "- " + PARAMETER_COUNTRY + " [mandatory] : Country where the test case will execute. [" + country + "]\n"
                        + "- " + PARAMETER_ENVIRONMENT + " : Environment where the test case will execute. This parameter is mandatory only if manualURL is not set to Y. [" + environment + "]\n"
                        + "- " + PARAMETER_ROBOT + " : robot name on which the test will be executed. [" + robot + "]\n"
                        + "- " + PARAMETER_ROBOT_IP + " : Host of the Robot where the test will be executed. (Can be overwriten if robot is defined) [" + robotHost + "]\n"
                        + "- " + PARAMETER_ROBOT_PORT + " : Port of the Robot. (Can be overwriten if robot is defined) [" + robotPort + "]\n"
                        + "- " + PARAMETER_BROWSER + " : Browser to use for the execution. (Can be overwriten if robot is defined) [" + browser + "]\n"
                        + "- " + PARAMETER_BROWSER_VERSION + " : Version to use for the execution. (Can be overwriten if robot is defined) [" + version + "]\n"
                        + "- " + PARAMETER_PLATFORM + " : Platform to use for the execution. (Can be overwriten if robot is defined) [" + platform + "]\n"
                        + "- " + PARAMETER_SCREEN_SIZE + " : Size of the screen to set for the execution. [" + screenSize + "]\n"
                        + "- " + PARAMETER_MANUAL_URL + " : Activate or not the Manual URL of the application to execute. If activated the 4 parameters after (myhost, mycontextroot, myloginrelativeurl, myenvdata) are necessary. [" + manualURL + "]\n"
                        + "- " + PARAMETER_MANUAL_HOST + " : Host of the application to test (only used when manualURL is feed). [" + myHost + "]\n"
                        + "- " + PARAMETER_MANUAL_CONTEXT_ROOT + " : Context root of the application to test (only used when manualURL is feed). [" + myContextRoot + "]\n"
                        + "- " + PARAMETER_MANUAL_LOGIN_RELATIVE_URL + " : Relative login URL of the application (only used when manualURL is feed). [" + myLoginRelativeURL + "]\n"
                        + "- " + PARAMETER_MANUAL_ENV_DATA + " : Environment where to get the test data when a manualURL is defined. (only used when manualURL is feed) [" + myEnvData + "]\n"
                        + "- " + PARAMETER_TAG + " : Tag that will be stored on the execution. [" + tag + "]\n"
                        + "- " + PARAMETER_OUTPUT_FORMAT + " : Format of the output of the execution. [" + outputFormat + "]\n"
                        + "- " + PARAMETER_SCREENSHOT + " : Activate or not the screenshots. [" + screenshot + "]\n"
                        + "- " + PARAMETER_VERBOSE + " : Verbose level of the execution. [" + verbose + "]\n"
                        + "- " + PARAMETER_TIMEOUT + " : Timeout used for the action. If empty, the default value will be the one configured in parameter table. [" + timeout + "]\n"
                        + "- " + PARAMETER_SYNCHRONEOUS + " : Synchroneous define if the servlet wait for the end of the execution to report its execution. [" + synchroneous + "\n"
                        + "- " + PARAMETER_PAGE_SOURCE + " : Record Page Source during the execution. [" + getPageSource + "]\n"
                        + "- " + PARAMETER_SELENIUM_LOG + " : Get the SeleniumLog at the end of the execution. [" + getRobotLog + "]\n"
                        + "- " + PARAMETER_MANUAL_EXECUTION + " : Execute testcase in manual mode. [" + manualExecution + "]\n"
                        + "- " + PARAMETER_NUMBER_OF_RETRIES + " : Number of tries if the result is not OK. [" + numberOfRetries + "]\n";

                boolean error = false;
                String errorMessage = "";

                // -- Checking the parameter validity. --
                // test, testcase and country parameters are mandatory
                if (StringUtil.isEmptyOrNull(test)) {
                    errorMessage += "Error - Parameter Test is mandatory. ";
                    error = true;
                }
                if (StringUtil.isEmptyOrNull(testCase)) {
                    errorMessage += "Error - Parameter TestCase is mandatory. ";
                    error = true;
                }
                if (!StringUtil.isEmptyOrNull(tag) && tag.length() > 255) {
                    errorMessage += "Error - Parameter Tag value is too big. Tag cannot be larger than 255 Characters. Currently has : " + tag.length();
                    error = true;
                }
                if (StringUtil.isEmptyOrNull(country)) {
                    errorMessage += "Error - Parameter Country is mandatory. ";
                    error = true;
                }
                // environment is mandatory when manualURL is not activated.
                if (StringUtil.isEmptyOrNull(environment) && !manualURL) {
                    errorMessage += "Error - Parameter Environment is mandatory (or activate the manualURL parameter). ";
                    error = true;
                }
                // myenv is mandatory when manualURL is activated.
                if (StringUtil.isEmptyOrNull(myEnvData) && manualURL) {
                    if (StringUtil.isEmptyOrNull(environment)) {
                        errorMessage += "Error - Parameter myenvdata is mandatory (when manualURL parameter is activated). ";
                        error = true;
                    } else {
                        myEnvData = environment;
                    }
                }

                // We check that execution is not desactivated by cerberus_automaticexecution_enable parameter.
                IParameterService parameterService = appContext.getBean(IParameterService.class);
                if (!(parameterService.getParameterBooleanByKey("cerberus_automaticexecution_enable", "", true))) {
                    errorMessage += "Error - Execution disable by configuration (cerberus_automaticexecution_enable <> Y). ";
                    error = true;
                    LOG.info("Execution request ignored by cerberus_automaticexecution_enable parameter. " + test + " / " + testCase);
                }

                //verify the format of the ScreenSize. It must be 2 integer separated by a *. For example : 1024*768
                if (!"".equals(screenSize)) {
                    if (!screenSize.contains("*")) {
                        errorMessage += "Error - ScreenSize format is not Correct. It must be 2 Integer separated by a *. ";
                        error = true;
                    } else {
                        try {
                            String screenWidth = screenSize.split("\\*")[0];
                            String screenLength = screenSize.split("\\*")[1];
                            Integer.parseInt(screenWidth);
                            Integer.parseInt(screenLength);
                        } catch (Exception e) {
                            errorMessage += "Error - ScreenSize format is not Correct. It must be 2 Integer separated by a *. ";
                            error = true;
                        }
                    }
                }

                // Create Tag when exist.
                if (!StringUtil.isEmptyOrNull(tag)) {
                    // We create or update it.
                    ITagService tagService = appContext.getBean(ITagService.class);
                    List<String> envList = new ArrayList<>();
                    envList.add(environment);
                    List<String> countryList = new ArrayList<>();
                    countryList.add(country);
                    tagService.createAuto(tag, "", executor, new JSONArray(envList), new JSONArray(countryList));
                }

                if (!error) {
                    //TODO:FN debug messages to be removed
                    LOG.debug("STARTED: Test " + test + "-" + testCase);

                    IRunTestCaseService runTestCaseService = appContext.getBean(IRunTestCaseService.class);
                    IFactoryTestCase factoryTCase = appContext.getBean(IFactoryTestCase.class);
                    IFactoryTestCaseExecution factoryTCExecution = appContext.getBean(IFactoryTestCaseExecution.class);
                    IFactoryTestCaseExecutionQueue factoryTCExecutionQueue = appContext.getBean(IFactoryTestCaseExecutionQueue.class);
                    ITestCaseExecutionService tces = appContext.getBean(ITestCaseExecutionService.class);
                    ITestCaseService tcs = appContext.getBean(ITestCaseService.class);
                    TestCase tCase = factoryTCase.create(test, testCase);

                    // Building Execution Object.
                    TestCaseExecution tCExecution = factoryTCExecution.create(0, test, testCase, null, null, null, environment, country, robot, robotExecutor, robotHost, robotPort, "", browser, version, platform,
                            0, 0, "", "", "", null, null, tag, verbose, screenshot, 0, getPageSource, getRobotLog, 0, synchroneous, timeout, outputFormat, null,
                            Infos.getInstance().getProjectNameAndVersion(), tCase, null, null, manualURL ? 1 : 2, myHost, myContextRoot, myLoginRelativeURL, myEnvData, robotHost, robotPort,
                            null, new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTSTARTED), executor, numberOfRetries, screenSize, null, "", "",
                            "", "", "", "", "", "", "", manualExecution, "", 0, 0, "", executor, null, executor, null);

                    /**
                     * Set IdFromQueue
                     */
                    try {
                        tCExecution.setQueueID(idFromQueue);

                        TestCaseExecutionQueue queueExecution = factoryTCExecutionQueue.create(idFromQueue, "", test, testCase, country, environment, robot, "", robotHost, robotPort, browser, version,
                                platform, screenSize, 0, myHost, myContextRoot, myLoginRelativeURL, myEnvData, tag, screenshot, 0, verbose, timeout, getPageSource, getRobotLog, 0, 0, numberOfRetries,
                                manualExecution, executor, null, null, null);
                        tCExecution.setTestCaseExecutionQueue(queueExecution);
                    } catch (FactoryCreationException ex) {
                        LOG.error(ex, ex);
                    }

                    /**
                     * Set UUID
                     */
                    ExecutionUUID executionUUIDObject = appContext.getBean(ExecutionUUID.class);
                    UUID executionUUID = UUID.randomUUID();
                    executionUUIDObject.setExecutionUUID(executionUUID.toString(), tCExecution);
                    tCExecution.setExecutionUUID(executionUUID.toString());
                    LOG.info("Execution Requested : UUID=" + executionUUID);

                    /**
                     * Execution of the testcase.
                     */
                    LOG.debug("Start execution " + tCExecution.getId());
                    tCExecution = runTestCaseService.runTestCase(tCExecution);

                    /**
                     * Clean memory in case testcase has not been
                     * launched(Remove all object put in memory)
                     */
                    try {
                        if (tCExecution.getId() == 0) {
                            executionUUIDObject.removeExecutionUUID(tCExecution.getExecutionUUID());
                            LOG.debug("Clean ExecutionUUID");

                        }
                    } catch (Exception ex) {
                        LOG.error("Exception cleaning Memory: ", ex);
                    }

                    /**
                     * Execution is finished we report the result.
                     */
                    long runID = tCExecution.getId();

                    switch (outputFormat) {

                        case "gui":
                            if (runID > 0) { // Execution has been created.
                                response.sendRedirect("TestCaseExecution.jsp?executionId=" + runID);
                            } else { // Execution was not even created.
                                response.setContentType("text/html");
                                out.println("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><title>Test Execution Result</title></head>");
                                out.println("<body>");
                                out.println("<table>");
                                out.println("<tr><td>RunID</td><td><span id='RunID'>" + runID + "</span></td></tr>");
                                out.println("<tr><td>IdFromQueue</td><td><b><span id='IdFromQueue'>" + tCExecution.getQueueID() + "</span></b></td></tr>");
                                out.println("<tr><td>Test</td><td><span id='Test'>" + test + "</span></td></tr>");
                                out.println("<tr><td>TestCase</td><td><span id='TestCase'>" + testCase + "</span></td></tr>");
                                out.println("<tr><td>Country</td><td><span id='Country'>" + country + "</span></td></tr>");
                                out.println("<tr><td>Environment</td><td><span id='Environment'>" + environment + "</span></td></tr>");
                                out.println("<tr><td>TimestampStart</td><td><span id='TimestampStart'>" + new Timestamp(tCExecution.getStart()) + "</span></td></tr>");
                                out.println("<tr><td>TimestampEnd</td><td><span id='TimestampEnd'>" + new Timestamp(tCExecution.getEnd()) + "</span></td></tr>");
                                out.println("<tr><td>OutputFormat</td><td><span id='OutputFormat'>" + outputFormat + "</span></td></tr>");
                                out.println("<tr><td>Verbose</td><td><span id='Verbose'>" + verbose + "</span></td></tr>");
                                out.println("<tr><td>Screenshot</td><td><span id='Screenshot'>" + screenshot + "</span></td></tr>");
                                out.println("<tr><td>PageSource</td><td><span id='PageSource'>" + getPageSource + "</span></td></tr>");
                                out.println("<tr><td>SeleniumLog</td><td><span id='SeleniumLog'>" + getRobotLog + "</span></td></tr>");
                                out.println("<tr><td>Robot</td><td><span id='Robot'>" + robot + "</span></td></tr>");
                                out.println("<tr><td>Robot Server IP</td><td><span id='SeleniumIP'>" + robotHost + "</span></td></tr>");
                                out.println("<tr><td>Robot Server Port</td><td><span id='SeleniumPort'>" + robotPort + "</span></td></tr>");
                                out.println("<tr><td>Timeout</td><td><span id='Timeout'>" + timeout + "</span></td></tr>");
                                out.println("<tr><td>Synchroneous</td><td><span id='Synchroneous'>" + synchroneous + "</span></td></tr>");
                                out.println("<tr><td>Browser</td><td><span id='Browser'>" + browser + "</span></td></tr>");
                                out.println("<tr><td>Version</td><td><span id='Version'>" + version + "</span></td></tr>");
                                out.println("<tr><td>Platform</td><td><span id='Platform'>" + platform + "</span></td></tr>");
                                out.println("<tr><td>Screen Size</td><td><span id='screenSize'>" + screenSize + "</span></td></tr>");
                                out.println("<tr><td>Number of Retry</td><td><span id='nbretry'>" + numberOfRetries + "</span></td></tr>");
                                out.println("<tr><td>ManualURL</td><td><span id='ManualURL'>" + tCExecution.getManualURL() + "</span></td></tr>");
                                out.println("<tr><td>MyHost</td><td><span id='MyHost'>" + tCExecution.getMyHost() + "</span></td></tr>");
                                out.println("<tr><td>MyContextRoot</td><td><span id='MyContextRoot'>" + tCExecution.getMyContextRoot() + "</span></td></tr>");
                                out.println("<tr><td>MyLoginRelativeURL</td><td><span id='MyLoginRelativeURL'>" + tCExecution.getMyLoginRelativeURL() + "</span></td></tr>");
                                out.println("<tr><td>myEnvironmentData</td><td><span id='myEnvironmentData'>" + tCExecution.getEnvironmentData() + "</span></td></tr>");
                                out.println("<tr><td>ReturnCode</td><td><b><span id='ReturnCodeDescription'>" + tCExecution.getResultMessage().getCode() + "</span></b></td></tr>");
                                out.println("<tr><td>ReturnCodeDescription</td><td><b><span id='ReturnCodeDescription'>" + tCExecution.getResultMessage().getDescription() + "</span></b></td></tr>");
                                out.println("<tr><td>ControlStatus</td><td><b><span id='ReturnCodeMessage'>" + tCExecution.getResultMessage().getCodeString() + "</span></b></td></tr>");
                                out.println("<tr><td></td><td></td></tr>");
                                out.println("</table><br><br>");
                                out.println("<table border>");
                                out.println("<tr>"
                                        + "<td><input id=\"ButtonRetry\" type=\"button\" value=\"Retry\" onClick=\"window.location.reload()\"></td>"
                                        + "<td><input id=\"ButtonBack\" type=\"button\" value=\"Go Back\" onClick=\"window.history.back()\"></td>"
                                        + "<td><input id=\"ButtonOpenTC\" type=\"button\" value=\"Open Test Case\" onClick=\"window.open('TestCaseScript.jsp?test=" + test + "&testcase=" + testCase + "')\"></td>"
                                        + "</tr>");
                                out.println("</table>");
                                out.println("</body>");
                                out.println("</html>");
                            }
                            break;

                        case "verbose-txt":
                            response.setContentType("text/plain");
                            String separator = " = ";
                            out.println("id" + separator + runID);
                            out.println("queueID" + separator + idFromQueue);
                            out.println("test" + separator + test);
                            out.println("testcase" + separator + testCase);
                            out.println("country" + separator + country);
                            out.println("environment" + separator + environment);
                            out.println("Time Start" + separator + new Timestamp(tCExecution.getStart()));
                            out.println("Time End" + separator + new Timestamp(tCExecution.getEnd()));
                            out.println("OutputFormat" + separator + outputFormat);
                            out.println("Verbose" + separator + verbose);
                            out.println("Screenshot" + separator + screenshot);
                            out.println("PageSource" + separator + getPageSource);
                            out.println("SeleniumLog" + separator + getRobotLog);
                            out.println("Robot" + separator + robot);
                            out.println("Robot Server IP" + separator + robotHost);
                            out.println("Robot Server Port" + separator + robotPort);
                            out.println("Timeout" + separator + timeout);
                            out.println("Synchroneous" + separator + synchroneous);
                            out.println("Browser" + separator + browser);
                            out.println("Version" + separator + version);
                            out.println("Platform" + separator + platform);
                            out.println("ScreenSize" + separator + screenSize);
                            out.println("Nb Of Retry" + separator + numberOfRetries);
                            out.println("ManualURL" + separator + tCExecution.getManualURL());
                            out.println("MyHost" + separator + tCExecution.getMyHost());
                            out.println("MyContextRoot" + separator + tCExecution.getMyContextRoot());
                            out.println("MyLoginRelativeURL" + separator + tCExecution.getMyLoginRelativeURL());
                            out.println("myEnvironmentData" + separator + tCExecution.getEnvironmentData());
                            out.println("ReturnCode" + separator + tCExecution.getResultMessage().getCode());
                            out.println("controlMessage" + separator + tCExecution.getResultMessage().getDescription());
                            out.println("controlStatus" + separator + tCExecution.getResultMessage().getCodeString());
                            break;

                        case "verbose-json":
                        case "json":
                        try {
                            JSONObject jsonResponse = new JSONObject();
                            response.setContentType("application/json");
                            response.setCharacterEncoding("utf8");

                            jsonResponse.put("id", runID);
                            jsonResponse.put("queueID", idFromQueue);
                            jsonResponse.put("test", test);
                            jsonResponse.put("testcase", testCase);
                            jsonResponse.put("country", country);
                            jsonResponse.put("environment", environment);
                            jsonResponse.put("Time Start", new Timestamp(tCExecution.getStart()));
                            jsonResponse.put("Time End", new Timestamp(tCExecution.getEnd()));
                            jsonResponse.put("OutputFormat", outputFormat);
                            jsonResponse.put("Verbose", verbose);
                            jsonResponse.put("Screenshot", screenshot);
                            jsonResponse.put("PageSource", getPageSource);
                            jsonResponse.put("SeleniumLog", getRobotLog);
                            jsonResponse.put("Robot", robot);
                            jsonResponse.put("Robot Server IP", robotHost);
                            jsonResponse.put("Robot Server Port", robotPort);
                            jsonResponse.put("Timeout", timeout);
                            jsonResponse.put("Synchroneous", synchroneous);
                            jsonResponse.put("Browser", browser);
                            jsonResponse.put("Version", version);
                            jsonResponse.put("Platform", platform);
                            jsonResponse.put("ScreenSize", screenSize);
                            jsonResponse.put("Nb Of Retry", numberOfRetries);
                            jsonResponse.put("ManualURL", manualURL);
                            jsonResponse.put("MyHost", myHost);
                            jsonResponse.put("MyContextRoot", myContextRoot);
                            jsonResponse.put("MyLoginRelativeURL", myLoginRelativeURL);
                            jsonResponse.put("myEnvironmentData", myEnvData);
                            jsonResponse.put("ReturnCode", tCExecution.getResultMessage().getCode());
                            jsonResponse.put("controlStatus", tCExecution.getResultMessage().getCodeString());
                            jsonResponse.put("controlMessage", tCExecution.getResultMessage().getDescription());
                            if (runID > 0) { // Execution has been created.
                                // Detail of the execution is returned from memory.
                                jsonResponse.put("executionDetail", tCExecution.toJson(true));
//                            TestCaseExecution t = (TestCaseExecution) tces.readByKeyWithDependency(runID).getItem();
//                            jsonResponse.put("executionDetail", t.toJson(true));
                            }
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
                            response.setContentType("text/plain");
                            DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_DISPLAY);
                            out.println(df.format(tCExecution.getStart()) + " - " + runID
                                    + " [" + test
                                    + "|" + testCase
                                    + "|" + country
                                    + "|" + environment
                                    + "] : '" + tCExecution.getResultMessage().getCodeString() + "' - "
                                    + tCExecution.getResultMessage().getCode()
                                    + " " + tCExecution.getResultMessage().getDescription());
                    }

                } else {
                    // An error occured when parsing the parameters.

                    switch (outputFormat) {

                        case "verbose-txt":
                            response.setContentType("text/plain");
                            String separator = " = ";
                            out.println("id" + separator + 0);
                            out.println("queueID" + separator + idFromQueue);
                            out.println("test" + separator + test);
                            out.println("testcase" + separator + testCase);
                            out.println("country" + separator + country);
                            out.println("environment" + separator + environment);
                            out.println("OutputFormat" + separator + outputFormat);
                            out.println("Verbose" + separator + verbose);
                            out.println("Screenshot" + separator + screenshot);
                            out.println("PageSource" + separator + getPageSource);
                            out.println("SeleniumLog" + separator + getRobotLog);
                            out.println("Robot" + separator + robot);
                            out.println("Robot Server IP" + separator + robotHost);
                            out.println("Robot Server Port" + separator + robotPort);
                            out.println("Timeout" + separator + timeout);
                            out.println("Synchroneous" + separator + synchroneous);
                            out.println("Browser" + separator + browser);
                            out.println("Version" + separator + version);
                            out.println("Platform" + separator + platform);
                            out.println("ScreenSize" + separator + screenSize);
                            out.println("Nb Of Retry" + separator + numberOfRetries);
                            out.println("ManualURL" + separator + manualURL);
                            out.println("MyHost" + separator + myHost);
                            out.println("MyContextRoot" + separator + myContextRoot);
                            out.println("MyLoginRelativeURL" + separator + myLoginRelativeURL);
                            out.println("myEnvironmentData" + separator + myEnvData);
                            out.println("ReturnCode" + separator + MessageGeneralEnum.EXECUTION_FA_SERVLETVALIDATONS.getCode());
                            out.println("controlMessage" + separator + MessageGeneralEnum.EXECUTION_FA_SERVLETVALIDATONS.getDescription() + " " + errorMessage);
                            out.println("controlStatus" + separator + MessageGeneralEnum.EXECUTION_FA_SERVLETVALIDATONS.getCodeString());
                            break;

                        case "json":
                        case "verbose-json":
                        try {
                            JSONObject jsonResponse = new JSONObject();
                            jsonResponse.put("OutputFormat", outputFormat);
                            jsonResponse.put("Verbose", verbose);
                            jsonResponse.put("Screenshot", screenshot);
                            jsonResponse.put("PageSource", getPageSource);
                            jsonResponse.put("SeleniumLog", getRobotLog);
                            jsonResponse.put("Robot", robot);
                            jsonResponse.put("Robot Server IP", robotHost);
                            jsonResponse.put("Robot Server Port", robotPort);
                            jsonResponse.put("Timeout", timeout);
                            jsonResponse.put("Synchroneous", synchroneous);
                            jsonResponse.put("Browser", browser);
                            jsonResponse.put("Version", version);
                            jsonResponse.put("Platform", platform);
                            jsonResponse.put("ScreenSize", screenSize);
                            jsonResponse.put("Nb Of Retry", numberOfRetries);
                            jsonResponse.put("ManualURL", manualURL);
                            jsonResponse.put("MyHost", myHost);
                            jsonResponse.put("MyContextRoot", myContextRoot);
                            jsonResponse.put("MyLoginRelativeURL", myLoginRelativeURL);
                            jsonResponse.put("myEnvironmentData", myEnvData);
                            jsonResponse.put("ReturnCode", MessageGeneralEnum.EXECUTION_FA_SERVLETVALIDATONS.getCode());
                            jsonResponse.put("helpMessage", helpMessage);

                            // Correct format. Previous entries will have to be removed in an earlier version.
                            jsonResponse.put("id", 0);
                            jsonResponse.put("queueID", idFromQueue);
                            jsonResponse.put("test", test);
                            jsonResponse.put("testcase", testCase);
                            jsonResponse.put("country", country);
                            jsonResponse.put("environment", environment);
                            jsonResponse.put("controlStatus", MessageGeneralEnum.EXECUTION_FA_SERVLETVALIDATONS.getCodeString());
                            jsonResponse.put("controlMessage", MessageGeneralEnum.EXECUTION_FA_SERVLETVALIDATONS.getDescription() + " " + errorMessage);

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
                            // In case of errors, we display the help message.
                            response.setContentType("text/plain");
                            DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_DISPLAY);
                            String errorMessageFinal = df.format(new Date()) + " - " + 0
                                    + " [" + test
                                    + "|" + testCase
                                    + "|" + country
                                    + "|" + environment
                                    + "] : '" + MessageGeneralEnum.EXECUTION_FA_SERVLETVALIDATONS.getCodeString() + "' - "
                                    + MessageGeneralEnum.EXECUTION_FA_SERVLETVALIDATONS.getCode()
                                    + " " + MessageGeneralEnum.EXECUTION_FA_SERVLETVALIDATONS.getDescription() + " " + errorMessage;
                            out.println(errorMessageFinal);
                            if (request.getParameter("help") != null) {
                                out.println(helpMessage);
                            }
                    }

                }
            }
        } catch (Exception e) {
            LOG.error("Execution calling the RunTestCaseV001 Servlet.", e);
        }

    }
}
