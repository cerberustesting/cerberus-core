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
package org.cerberus.servlet.zzpublic;

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
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.cerberus.crud.entity.ExecutionSOAPResponse;
import org.cerberus.crud.entity.ExecutionUUID;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.crud.entity.Parameter;
import org.cerberus.crud.entity.Robot;
import org.cerberus.crud.entity.Session;
import org.cerberus.crud.entity.SessionCapabilities;
import org.cerberus.crud.entity.TCase;
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.factory.IFactoryTCase;
import org.cerberus.crud.factory.IFactoryTestCaseExecution;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.IRobotService;
import org.cerberus.crud.service.ITestCaseCountryService;
import org.cerberus.crud.service.ITestCaseExecutionInQueueService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.engine.IRunTestCaseService;
import org.cerberus.service.engine.impl.RunTestCaseService;
import org.cerberus.util.DateUtil;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.version.Infos;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 25/01/2013
 * @since 0.9.0
 */
@WebServlet(name = "RunTestCase", urlPatterns = {"/RunTestCase"})
public class RunTestCase extends HttpServlet {

    public static final String SERVLET_URL = "/RunTestCase";

    public static final String PARAMETER_TEST = "Test";
    public static final String PARAMETER_TEST_CASE = "TestCase";
    public static final String PARAMETER_COUNTRY = "Country";
    public static final String PARAMETER_ENVIRONMENT = "Environment";
    public static final String PARAMETER_ROBOT = "robot";
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
    public static final String SCREEN_SIZE = "screenSize";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        /**
         * Adding Log entry.
         */
        ILogEventService logEventService = appContext.getBean(LogEventService.class);
        logEventService.createPublicCalls("/RunTestCase", "CALL", "RunTestCaseV0 called : " + request.getRequestURL(), request);

        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        //Tool
        String ss_ip = ""; // Selenium IP
        String ss_p = ""; // Selenium Port
        String browser = "";
        String version = "";
        String platform = "";
        String robot = "";
        String active = "";
        String timeout = "";
        String userAgent = "";
        String screenSize = "";
        boolean synchroneous = true;
        int getPageSource = 0;
        int getSeleniumLog = 0;
        String manualExecution = "";

        //Test
        String test = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("Test")), "");
        String testCase = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("TestCase")), "");
        String country = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("Country")), "");
        String environment = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("Environment")), "");

        //Test Dev Environment
        boolean manualURL = ParameterParserUtil.parseBooleanParam(policy.sanitize(request.getParameter("manualURL")), false);
        String myHost = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("myhost")), "");
        String myContextRoot = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("mycontextroot")), "");
        String myLoginRelativeURL = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("myloginrelativeurl")), "");
        //TODO find another solution
        myLoginRelativeURL = myLoginRelativeURL.replace("&#61;", "=");
        String myEnvData = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("myenvdata")), "");

        //Execution
        String tag = ParameterParserUtil.parseStringParam(request.getParameter("Tag"), "");
        String outputFormat = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("outputformat")), "compact");
        int screenshot = ParameterParserUtil.parseIntegerParam(policy.sanitize(request.getParameter("screenshot")), 1);
        int verbose = ParameterParserUtil.parseIntegerParam(policy.sanitize(request.getParameter("verbose")), 0);
        timeout = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("timeout")), "");
        synchroneous = ParameterParserUtil.parseBooleanParam(policy.sanitize(request.getParameter("synchroneous")), true);
        getPageSource = ParameterParserUtil.parseIntegerParam(policy.sanitize(request.getParameter("pageSource")), 1);
        getSeleniumLog = ParameterParserUtil.parseIntegerParam(policy.sanitize(request.getParameter("seleniumLog")), 1);
        manualExecution = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("manualExecution")), "N");
        long idFromQueue = ParameterParserUtil.parseIntegerParam(policy.sanitize(request.getParameter("IdFromQueue")), 0);
        int numberOfRetries = ParameterParserUtil.parseIntegerParam(policy.sanitize(request.getParameter("retries")), 0);
        screenSize = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("screenSize")), "");

        robot = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("robot")), "");
        ss_ip = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("ss_ip")), "");
        ss_p = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("ss_p")), "");
        if (request.getParameter("Browser") != null && !"".equals(request.getParameter("Browser"))) {
            browser = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("Browser")), "firefox");
        } else {
            browser = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("browser")), "firefox");
        }
        version = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("version")), "");
        platform = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("platform")), "");

        String helpMessage = "\nThis servlet is used to start the execution of a test case.\n"
                + "Parameter list :\n"
                + "- Test [mandatory] : Test to execute. [" + test + "]\n"
                + "- TestCase [mandatory] : Test Case reference to execute. [" + testCase + "]\n"
                + "- Country [mandatory] : Country where the test case will execute. [" + country + "]\n"
                + "- Environment : Environment where the test case will execute. This parameter is mandatory only if manualURL is not set to Y. [" + environment + "]\n"
                + "- robot : robot name on which the test will be executed. [" + robot + "]\n"
                + "- ss_ip : Host of the Robot where the test will be executed. (Can be overwriten if robot is defined) [" + ss_ip + "]\n"
                + "- ss_p : Port of the Robot. (Can be overwriten if robot is defined) [" + ss_p + "]\n"
                + "- browser : Browser to use for the execution. (Can be overwriten if robot is defined) [" + browser + "]\n"
                + "- version : Version to use for the execution. (Can be overwriten if robot is defined) [" + version + "]\n"
                + "- platform : Platform to use for the execution. (Can be overwriten if robot is defined) [" + platform + "]\n"
                + "- screenSize : Size of the screen to set for the execution. [" + screenSize + "]\n"
                + "- manualURL : Activate or not the Manual URL of the application to execute. If activated the 4 parameters after (myhost, mycontextroot, myloginrelativeurl, myenvdata) are necessary. [" + manualURL + "]\n"
                + "- myhost : Host of the application to test (only used when manualURL is feed). [" + myHost + "]\n"
                + "- mycontextroot : Context root of the application to test (only used when manualURL is feed). [" + myContextRoot + "]\n"
                + "- myloginrelativeurl : Relative login URL of the application (only used when manualURL is feed). [" + myLoginRelativeURL + "]\n"
                + "- myenvdata : Environment where to get the test data when a manualURL is defined. (only used when manualURL is feed) [" + myEnvData + "]\n"
                + "- Tag : Tag that will be stored on the execution. [" + StringEscapeUtils.escapeHtml4(tag) + "]\n"
                + "- outputformat : Format of the output of the execution. [" + outputFormat + "]\n"
                + "- screenshot : Activate or not the screenshots. [" + screenshot + "]\n"
                + "- verbose : Verbose level of the execution. [" + verbose + "]\n"
                + "- timeout : Timeout used for the action. If empty, the default value will be the one configured in parameter table. [" + timeout + "]\n"
                + "- synchroneous : Synchroneous define if the servlet wait for the end of the execution to report its execution. [" + synchroneous + "\n"
                + "- pageSource : Record Page Source during the execution. [" + getPageSource + "]\n"
                + "- seleniumLog : Get the SeleniumLog at the end of the execution. [" + getSeleniumLog + "]\n"
                + "- manualExecution : Execute testcase in manual mode. [" + manualExecution + "]\n"
                + "- retries : Number of tries if the result is not OK. [" + numberOfRetries + "]\n";

        boolean error = false;

        // -- Checking the parameter validity. --
        // test, testcase and country parameters are mandatory
        if (StringUtils.isBlank(test)) {
            out.println("Error - Parameter test is mandatory.");
            error = true;
        }
        if (StringUtils.isBlank(testCase)) {
            out.println("Error - Parameter testCase is mandatory.");
            error = true;
        }
        if (StringUtils.isBlank(country)) {
            out.println("Error - Parameter country is mandatory.");
            error = true;
        }
        // environment is mandatory when manualURL is not activated.
        if (StringUtils.isBlank(environment) && !manualURL) {
            out.println("Error - Parameter environment is mandatory (or use the manualURL parameter).");
            error = true;
        }
        //verify the format of the ScreenSize. It must be 2 integer separated by a *. For example : 1024*768
        if (!"".equals(screenSize)) {
            if (!screenSize.contains("*")) {
                out.println("Error - ScreenSize format is not Correct. It must be 2 Integer separated by a *.");
                error = true;
            } else {
                try {
                    String screenWidth = screenSize.split("\\*")[0];
                    String screenLength = screenSize.split("\\*")[1];
                    Integer.parseInt(screenWidth);
                    Integer.parseInt(screenLength);
                } catch (Exception e) {
                    out.println("Error - ScreenSize format is not Correct. It must be 2 Integer separated by a *.");
                    error = true;
                }
            }
        }
        if (!"".equals(robot)) {
            IRobotService robotService = appContext.getBean(IRobotService.class);
            try {
                Robot robObj = robotService.convert(robotService.readByKey(robot));
                // If Robot parameter is defined and we can find the robot, we overwrite the corresponding parameters.
                ss_ip = ParameterParserUtil.parseStringParam(robObj.getHost(), ss_ip);
                ss_p = ParameterParserUtil.parseStringParam(String.valueOf(robObj.getPort()), ss_p);
                browser = ParameterParserUtil.parseStringParam(robObj.getBrowser(), browser);
                version = ParameterParserUtil.parseStringParam(robObj.getVersion(), version);
                platform = ParameterParserUtil.parseStringParam(robObj.getPlatform(), platform);
                active = robObj.getActive();
                userAgent = robObj.getUserAgent();
            } catch (CerberusException ex) {
                out.println("Error - Robot [" + robot + "] does not exist.");
                error = true;
            }
        }
        // We cannot execute a testcase on a desactivated Robot.
        if (active.equals("N")) {
            out.println("Error - Robot is not Active.");
            error = true;
        }

        if (!error) {
            //check if the test case is to be executed in the specific parameters            
            boolean exists = false;
            try {
                ITestCaseCountryService tccService = appContext.getBean(ITestCaseCountryService.class);

                TestCaseCountry tcc = tccService.findTestCaseCountryByKey(test, testCase, country);
                if (tcc != null) {
                    exists = true;
                }
            } catch (CerberusException ex) {
                response.setContentType("text/plain");
                DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_DISPLAY);
                String errorMessage = df.format(new Date()) + " - " + 0
                        + " [" + test
                        + "|" + testCase
                        + "|" + country
                        + "|" + environment
                        + "] : Test Case is not selected for country!' ";
                out.println(errorMessage);
            }
            //there is no need to lauch the execution if the test case does not exist for the country
            if (exists) {
                //TODO:FN debug messages to be removed
                org.apache.log4j.Logger.getLogger(RunTestCase.class.getName()).log(org.apache.log4j.Level.DEBUG, "[DEBUG] STARTED: Test " + test + "-" + testCase);

                IRunTestCaseService runTestCaseService = appContext.getBean(RunTestCaseService.class);
                IFactoryTCase factoryTCase = appContext.getBean(IFactoryTCase.class);
                IFactoryTestCaseExecution factoryTCExecution = appContext.getBean(IFactoryTestCaseExecution.class);

                TCase tCase = factoryTCase.create(test, testCase);

                TestCaseExecution tCExecution = factoryTCExecution.create(0, test, testCase, null, null, environment, country, browser, version, platform, "",
                        0, 0, "", "", null, ss_ip, null, ss_p, tag, "N", verbose, screenshot, getPageSource, getSeleniumLog, synchroneous, timeout, outputFormat, null,
                        Infos.getInstance().getProjectNameAndVersion(), tCase, null, null, manualURL, myHost, myContextRoot, myLoginRelativeURL, myEnvData, ss_ip, ss_p,
                        null, new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTSTARTED), "Selenium", numberOfRetries, screenSize);

                /**
                 * Set UserAgent
                 */
                tCExecution.setUserAgent(userAgent);
                /**
                 * Set UUID
                 */
                ExecutionUUID executionUUIDObject = appContext.getBean(ExecutionUUID.class);
                UUID executionUUID = UUID.randomUUID();
                executionUUIDObject.setExecutionUUID(executionUUID.toString(), tCExecution);
                tCExecution.setExecutionUUID(executionUUID.toString());
                org.apache.log4j.Logger.getLogger(RunTestCase.class.getName()).log(org.apache.log4j.Level.INFO, "Execution Requested : UUID=" + executionUUID);
                //MyLogger.log(RunTestCase.class.getName(), Level.INFO, "Execution Requested : UUID=" + executionUUID);

                ExecutionSOAPResponse eSResponse = appContext.getBean(ExecutionSOAPResponse.class);
                eSResponse.setExecutionSOAPResponse(executionUUID.toString(), "init");

                /**
                 * Set Session
                 */
                IParameterService parameterService = appContext.getBean(IParameterService.class);
                long defaultWait;
                try {
                    Parameter param = parameterService.findParameterByKey("selenium_defaultWait", "");
                    String to = tCExecution.getTimeout().equals("") ? param.getValue() : tCExecution.getTimeout();
                    defaultWait = Long.parseLong(to);
                } catch (CerberusException ex) {
                    //MyLogger.log(RunTestCase.class.getName(), Level.WARN, "Parameter (selenium_defaultWait) not in Parameter table, default wait set to 90 seconds");
                    org.apache.log4j.Logger.getLogger(RunTestCase.class.getName()).log(org.apache.log4j.Level.WARN, "Parameter (selenium_defaultWait) not in Parameter table, default wait set to 90 seconds", ex);
                    defaultWait = 90;
                }

                /**
                 * Set IdFromQueue
                 */
                tCExecution.setIdFromQueue(idFromQueue);

                List<SessionCapabilities> capabilities = new ArrayList();
                SessionCapabilities sc = new SessionCapabilities();
                sc.create("browser", browser);
                capabilities.add(sc);
                sc = new SessionCapabilities();
                sc.create("platform", platform);
                capabilities.add(sc);
                sc = new SessionCapabilities();
                sc.create("version", version);
                capabilities.add(sc);

                Session session = new Session();
                session.setDefaultWait(defaultWait);
                session.setHost(tCExecution.getSeleniumIP());
                session.setPort(tCExecution.getPort());
                session.setCapabilities(capabilities);

                tCExecution.setSession(session);

                while (tCExecution.getNumberOfRetries() >= 0 && !tCExecution.getResultMessage().getCodeString().equals("OK")) {
                    try {
                        org.apache.log4j.Logger.getLogger(RunTestCase.class.getName()).log(org.apache.log4j.Level.DEBUG, "Start execution " + tCExecution.getId());
                        tCExecution = runTestCaseService.runTestCase(tCExecution);
                        tCExecution.decreaseNumberOfRetries();
                    } catch (Exception ex) {
                        org.apache.log4j.Logger.getLogger(RunTestCase.class.getName()).log(org.apache.log4j.Level.ERROR, "Error while executing RunTestCase ", ex);
                        break;
                    }
                }
                /**
                 * If execution from queue, remove it from the queue or update
                 * information in Queue
                 *
                 * IdFromQueue must be different of 0 ReturnCode of Testcase
                 * should be
                 */
                try {
                    if (tCExecution.getIdFromQueue() != 0) {
                        ITestCaseExecutionInQueueService testCaseExecutionInQueueService = appContext.getBean(ITestCaseExecutionInQueueService.class);
                        if (tCExecution.getResultMessage().getCode() == 63) {
                            testCaseExecutionInQueueService.updateComment(tCExecution.getIdFromQueue(), tCExecution.getResultMessage().getDescription());
                        } else {
                            testCaseExecutionInQueueService.remove(tCExecution.getIdFromQueue());
                        }
                    }
                } catch (CerberusException ex) {
                    org.apache.log4j.Logger.getLogger(RunTestCase.class.getName()).log(org.apache.log4j.Level.ERROR, "Error while performin testcase in queue ", ex);
                }

                /**
             * Clean memory in case testcase has not been launched(Remove all
             * object put in memory)
                 */
                try {
                    if (tCExecution.getId() == 0) {
                        executionUUIDObject.removeExecutionUUID(tCExecution.getExecutionUUID());
                        org.apache.log4j.Logger.getLogger(RunTestCase.class.getName()).log(org.apache.log4j.Level.DEBUG, "Clean ExecutionUUID");

                        if (eSResponse.getExecutionSOAPResponse(tCExecution.getExecutionUUID()) != null) {
                            eSResponse.removeExecutionSOAPResponse(tCExecution.getExecutionUUID());
                            org.apache.log4j.Logger.getLogger(RunTestCase.class.getName()).log(org.apache.log4j.Level.DEBUG, "ExecutionSOAPResponse ExecutionUUID");                        
                        }
                    }
                } catch (Exception ex) {
                    org.apache.log4j.Logger.getLogger(RunTestCase.class.getName()).log(org.apache.log4j.Level.ERROR, "Exception cleaning Memory: ", ex);
                }

                long runID = tCExecution.getId();
                if (outputFormat.equalsIgnoreCase("gui")) { // HTML GUI output. either the detailed execution page or an error page when the execution is not created.
                    if (runID > 0) { // Execution has been created.
                        response.sendRedirect("./ExecutionDetail.jsp?id_tc=" + runID);
                    } else { // Execution was not even created.
                        response.setContentType("text/html");
                        out.println("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><title>Test Execution Result</title></head>");
                        out.println("<body>");
                        out.println("<table>");
                        out.println("<tr><td>RunID</td><td><span id='RunID'>" + runID + "</span></td></tr>");
                        out.println("<tr><td>IdFromQueue</td><td><b><span id='IdFromQueue'>" + tCExecution.getIdFromQueue() + "</span></b></td></tr>");
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
                        out.println("<tr><td>SeleniumLog</td><td><span id='SeleniumLog'>" + getSeleniumLog + "</span></td></tr>");
                        out.println("<tr><td>Robot</td><td><span id='Robot'>" + robot + "</span></td></tr>");
                        out.println("<tr><td>Browser</td><td><span id='Browser'>" + browser + "</span></td></tr>");
                        out.println("<tr><td>Version</td><td><span id='Version'>" + version + "</span></td></tr>");
                        out.println("<tr><td>Platform</td><td><span id='Platform'>" + platform + "</span></td></tr>");
                        out.println("<tr><td>ManualURL</td><td><span id='ManualURL'>" + tCExecution.isManualURL() + "</span></td></tr>");
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
                                + "<td><input id=\"ButtonOpenTC\" type=\"button\" value=\"Open Test Case\" onClick=\"window.open('TestCase.jsp?Test=" + test + "&TestCase=" + testCase + "&Load=Load')\"></td>"
                                + "</tr>");
                        out.println("</table>");
                        out.println("</body>");
                        out.println("</html>");
                    }
                } else if (outputFormat.equalsIgnoreCase("verbose-txt")) { // Text verbose output.
                    response.setContentType("text/plain");
                    String separator = " = ";
                    out.println("RunID" + separator + runID);
                    out.println("Test" + separator + test);
                    out.println("TestCase" + separator + testCase);
                    out.println("Country" + separator + country);
                    out.println("Environment" + separator + environment);
                    out.println("Time Start" + separator + new Timestamp(tCExecution.getStart()));
                    out.println("Time End" + separator + new Timestamp(tCExecution.getEnd()));
                    out.println("OutputFormat" + separator + outputFormat);
                    out.println("Verbose" + separator + verbose);
                    out.println("Screenshot" + separator + screenshot);
                    out.println("PageSource" + separator + getPageSource);
                    out.println("SeleniumLog" + separator + getSeleniumLog);
                    out.println("Robot" + separator + robot);
                    out.println("Browser" + separator + browser);
                    out.println("Version" + separator + version);
                    out.println("Platform" + separator + platform);
                    out.println("ManualURL" + separator + tCExecution.isManualURL());
                    out.println("MyHost" + separator + tCExecution.getMyHost());
                    out.println("MyContextRoot" + separator + tCExecution.getMyContextRoot());
                    out.println("MyLoginRelativeURL" + separator + tCExecution.getMyLoginRelativeURL());
                    out.println("myEnvironmentData" + separator + tCExecution.getEnvironmentData());
                    out.println("ReturnCode" + separator + tCExecution.getResultMessage().getCode());
                    out.println("ReturnCodeDescription" + separator + tCExecution.getResultMessage().getDescription());
                    out.println("ControlStatus" + separator + tCExecution.getResultMessage().getCodeString());
                } else if (outputFormat.equalsIgnoreCase("redirectToReport")) { // Redirect to the reporting page by tag.
                    response.sendRedirect("./ReportingExecutionByTag.jsp?Tag=" + StringUtil.encodeAsJavaScriptURIComponent(tag));
                } else { // Default behaviour when not outputformat is defined : compact mode.
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
            }

        } else {
            // In case of errors, we display the help message.
            out.println(helpMessage);
        }

    }
}
