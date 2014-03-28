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
package org.cerberus.servlet.publi;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.TestCaseExecution;
import org.cerberus.entity.TCase;
import org.cerberus.factory.IFactoryTestCaseExecution;
import org.cerberus.factory.IFactoryTCase;
import org.cerberus.log.MyLogger;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.impl.LogEventService;
import org.cerberus.serviceEngine.IRunTestCaseService;
import org.cerberus.serviceEngine.impl.RunTestCaseService;
import org.cerberus.util.DateUtil;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.version.Version;
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        /**
         * Adding Log entry.
         */
        ILogEventService logEventService = appContext.getBean(LogEventService.class);
        logEventService.insertLogEventPublicCalls("/RunTestCase", "CALL", "RunTestCase called : " + request.getRequestURI(), request);

        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        //Tool
        String seleniumIP = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("ss_ip")), "");
        String seleniumPort = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("ss_p")), "");
        String browser = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("browser")), "firefox");

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
        String tag = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("Tag")), "");
        String outputFormat = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("outputformat")), "compact");
        int screenshot = ParameterParserUtil.parseIntegerParam(policy.sanitize(request.getParameter("screenshot")), 1);
        int verbose = ParameterParserUtil.parseIntegerParam(policy.sanitize(request.getParameter("verbose")), 0);

        IRunTestCaseService runTestCaseService = appContext.getBean(RunTestCaseService.class);
        IFactoryTCase factoryTCase = appContext.getBean(IFactoryTCase.class);
        IFactoryTestCaseExecution factoryTCExecution = appContext.getBean(IFactoryTestCaseExecution.class);

        TCase tCase = factoryTCase.create(test, testCase);

        TestCaseExecution tCExecution = factoryTCExecution.create(0, test, testCase, null, null, environment, country, browser, "",
                0, 0, "", "", null, seleniumIP, null, seleniumPort, tag, "N", verbose, screenshot, outputFormat, null,
                Version.PROJECT_NAME_VERSION, tCase, null, null, manualURL, myHost, myContextRoot, myLoginRelativeURL, myEnvData, seleniumIP, seleniumPort, null, new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTSTARTED));

        try {
            tCExecution = runTestCaseService.runTestCase(tCExecution);
        } catch (Exception ex) {
            MyLogger.log(RunTestCase.class.getName(), Level.FATAL, "Exception on testcase: " + tCExecution.getId() + "\nDetail: " + ex.getMessage() + "\n\n" + ex.toString());
        }

        long runID = tCExecution.getId();
        if (outputFormat.equalsIgnoreCase("gui")) {
            if (runID > 0) {
                response.sendRedirect("./ExecutionDetail.jsp?id_tc=" + runID);
            } else {
                out.println("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><title>Test Execution Result</title></head>");
                out.println("<body>");
                out.println("<table>");
                out.println("<tr><td>RunID</td><td><span id='RunID'>" + runID + "</span></td></tr>");
                out.println("<tr><td>Test</td><td><span id='Test'>" + test + "</span></td></tr>");
                out.println("<tr><td>TestCase</td><td><span id='TestCase'>" + testCase + "</span></td></tr>");
                out.println("<tr><td>Country</td><td><span id='Country'>" + country + "</span></td></tr>");
                out.println("<tr><td>Environment</td><td><span id='Environment'>" + environment + "</span></td></tr>");
                out.println("<tr><td>TimestampStart</td><td><span id='TimestampStart'>" + new Timestamp(tCExecution.getStart()) + "</span></td></tr>");
                out.println("<tr><td>TimestampEnd</td><td><span id='TimestampEnd'>" + new Timestamp(tCExecution.getEnd()) + "</span></td></tr>");
                out.println("<tr><td>OutputFormat</td><td><span id='OutputFormat'>" + outputFormat + "</span></td></tr>");
                out.println("<tr><td>Verbose</td><td><span id='Verbose'>" + verbose + "</span></td></tr>");
                out.println("<tr><td>Screenshot</td><td><span id='Screenshot'>" + screenshot + "</span></td></tr>");
                out.println("<tr><td>Browser</td><td><span id='Browser'>" + browser + "</span></td></tr>");
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
        } else if (outputFormat.equalsIgnoreCase("verbose-txt")) {
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
            out.println("Browser" + separator + browser);
            out.println("ManualURL" + separator + tCExecution.isManualURL());
            out.println("MyHost" + separator + tCExecution.getMyHost());
            out.println("MyContextRoot" + separator + tCExecution.getMyContextRoot());
            out.println("MyLoginRelativeURL" + separator + tCExecution.getMyLoginRelativeURL());
            out.println("myEnvironmentData" + separator + tCExecution.getEnvironmentData());
            out.println("ReturnCode" + separator + tCExecution.getResultMessage().getCode());
            out.println("ReturnCodeDescription" + separator + tCExecution.getResultMessage().getDescription());
            out.println("ControlStatus" + separator + tCExecution.getResultMessage().getCodeString());
        } else {
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
}
