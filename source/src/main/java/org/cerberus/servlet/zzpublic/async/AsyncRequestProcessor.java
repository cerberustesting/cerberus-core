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
package org.cerberus.servlet.zzpublic.async;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.AsyncContext;
import javax.servlet.ServletRequest;
import org.cerberus.crud.entity.ExecutionSOAPResponse;
import org.cerberus.crud.entity.ExecutionUUID;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.crud.entity.Parameter;
import org.cerberus.crud.entity.Session;
import org.cerberus.crud.entity.SessionCapabilities;
import org.cerberus.crud.entity.TCase;
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.factory.IFactoryTCase;
import org.cerberus.crud.factory.IFactoryTestCaseExecution;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestCaseCountryService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.impl.TestCaseCountryService;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.engine.impl.RunTestCaseService;
import org.cerberus.util.DateUtil;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.version.Infos;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * {Insert class description here}
 *
 * @author FNogueira
 * @version 1.0, 24/10/2015
 * @since 1.1.3
 */
public class AsyncRequestProcessor implements Runnable {

    private AsyncContext asyncContext;
    private AsyncRequestParameters urlParameters;
    private String result;

    public String getResult() {
        return result;
    }

    @Override
    public void run() {
        DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_DISPLAY);
        String msg = "Async Supported? " + asyncContext.getRequest().isAsyncSupported();
        org.apache.log4j.Logger.getLogger(AsyncRequestProcessor.class.getName()).log(org.apache.log4j.Level.INFO, msg);
        parseMandatoryURLParameters(asyncContext.getRequest());
        boolean isValid = validateMandatoryParameters(urlParameters);
        
        if (isValid) {
            org.apache.log4j.Logger.getLogger(AsyncRequestProcessor.class.getName()).log(org.apache.log4j.Level.INFO, "Thread run() - Start::Name="
                    + Thread.currentThread().getName() + "::ID="
                    + Thread.currentThread().getId() + " TEST = " + urlParameters.getTest() + "TESTCASE = " + urlParameters.getTestCase());
            //obtain the remaining parameters
            parseExecutionParameters(asyncContext.getRequest());
            processing(df);
        } else {
            result = df.format(new Date()) + " - " + 0
                    + " [" + urlParameters.getTest()
                    + "|" + urlParameters.getTestCase()
                    + "|" + urlParameters.getCountry()
                    + "|" + urlParameters.getEnvironment()
                    + "] : invalid parameters. Please check if  test, testcase, environment and country are defined. !'";
            org.apache.log4j.Logger.getLogger(AsyncRequestProcessor.class.getName()).log(org.apache.log4j.Level.FATAL, result);
        }

        PrintWriter outResponse;
        try {
            outResponse = asyncContext.getResponse().getWriter();
            asyncContext.getResponse().setContentType("text/plain");
            outResponse.print(result);
        } catch (IOException ex) {
            Logger.getLogger(AsyncRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }

        //complete the processing
        asyncContext.complete();
        org.apache.log4j.Logger.getLogger(AsyncRequestProcessor.class.getName()).log(org.apache.log4j.Level.INFO, "Process complete");

    }

    public AsyncRequestProcessor() {

    }

    public AsyncRequestProcessor(AsyncContext asyncCtx) {
        this.asyncContext = asyncCtx;
    }

    // TODO:FN remove test debug
    private void processing(DateFormat df) {

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(asyncContext.getRequest().getServletContext());
        ITestCaseCountryService tccService = appContext.getBean(TestCaseCountryService.class);
        

        //check if the test case is to be executed in the specific parameters            
        boolean exists = false;
        try {
            TestCaseCountry tcc = tccService.findTestCaseCountryByKey(urlParameters.getTest(), urlParameters.getTestCase(), urlParameters.getCountry());
            if (tcc != null) {
                exists = true;
            }
        } catch (CerberusException ex) {
            result = df.format(new Date()) + " - " + 0
                    + " [" + urlParameters.getTest()
                    + "|" + urlParameters.getTestCase()
                    + "|" + urlParameters.getCountry()
                    + "|" + urlParameters.getEnvironment()
                    + "] : Test Case is not selected for country!'";
        }

        if (exists) {
            //if the test exists for the country then we can execute it

            RunTestCaseService runTestCaseService = appContext.getBean(RunTestCaseService.class);
            IFactoryTCase factoryTCase = appContext.getBean(IFactoryTCase.class);
            IFactoryTestCaseExecution factoryTCExecution = appContext.getBean(IFactoryTestCaseExecution.class);

            TCase tCase = factoryTCase.create(urlParameters.getTest(), urlParameters.getTestCase());

            TestCaseExecution tCExecution = factoryTCExecution.create(0, urlParameters.getTest(), urlParameters.getTestCase(), null, null, urlParameters.getEnvironment(),
                    urlParameters.getCountry(), urlParameters.getBrowser(), urlParameters.getVersion(), urlParameters.getPlatform(), "", 0, 0, "", "", null,
                    urlParameters.getSeleniumIp(), null, urlParameters.getSeleniumPort(), urlParameters.getTag(), "N", urlParameters.getVerbose(),
                    urlParameters.getScreenshot(), urlParameters.getPageSource(), urlParameters.getSeleniumLog(), false, "", urlParameters.getOutputFormat(), null,
                    Infos.getInstance().getProjectNameAndVersion(), tCase, null, null, false, "", "", "", "", urlParameters.getSeleniumIp(), urlParameters.getSeleniumPort(),
                    null, new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTSTARTED), "Selenium", urlParameters.getNrRetries(), urlParameters.getScreenSize());

            /**
             * *******************EXECUTION**********************
             */
            /**
             * Set UserAgent
             */
            tCExecution.setUserAgent(urlParameters.getUserAgent());
            /**
             * Set UUID
             */
            ExecutionUUID executionUUIDObject = appContext.getBean(ExecutionUUID.class);
            UUID executionUUID = UUID.randomUUID();
            executionUUIDObject.setExecutionUUID(executionUUID.toString(), tCExecution);
            tCExecution.setExecutionUUID(executionUUID.toString());

            org.apache.log4j.Logger.getLogger(AsyncRequestProcessor.class.getName()).log(org.apache.log4j.Level.INFO, "Execution Requested : UUID=" + executionUUID);

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
                org.apache.log4j.Logger.getLogger(AsyncRequestProcessor.class.getName()).log(org.apache.log4j.Level.WARN, "Parameter (selenium_defaultWait) not in Parameter table, default wait set to 90 seconds", ex);
                defaultWait = 45;
            }

            /**
             * Set IdFromQueue
             */
            tCExecution.setIdFromQueue(urlParameters.getIdFromQueue());

            List<SessionCapabilities> capabilities = new ArrayList();
            SessionCapabilities sc = new SessionCapabilities();
            sc.create("browser", urlParameters.getBrowser());
            capabilities.add(sc);
            sc = new SessionCapabilities();
            sc.create("platform", urlParameters.getPlatform());
            capabilities.add(sc);
            sc = new SessionCapabilities();
            sc.create("version", urlParameters.getVersion());
            capabilities.add(sc);

            Session session = new Session();
            session.setDefaultWait(defaultWait);
            session.setHost(tCExecution.getSeleniumIP());
            session.setPort(tCExecution.getPort());
            session.setCapabilities(capabilities);

            tCExecution.setSession(session);

            try {
                org.apache.log4j.Logger.getLogger(AsyncRequestProcessor.class.getName()).log(org.apache.log4j.Level.INFO, "START!! " + tCExecution.getId());
                tCExecution = runTestCaseService.runTestCase(tCExecution);
                //returns a succes message
                result = df.format(tCExecution.getStart()) + " - " + tCExecution.getId()
                        + " [" + tCExecution.getTest()
                        + "|" + tCExecution.getTestCase()
                        + "|" + tCExecution.getCountry()
                        + "|" + tCExecution.getEnvironment()
                        + "] : '" + tCExecution.getResultMessage().getCodeString() + "' - "
                        + tCExecution.getResultMessage().getCode()
                        + " " + tCExecution.getResultMessage().getDescription();

            } catch (Exception ex) {
                org.apache.log4j.Logger.getLogger(AsyncRequestProcessor.class.getName()).log(org.apache.log4j.Level.ERROR, "Error while executing RunTestCase ", ex);
                result = df.format(new Date()) + " - " + 0
                        + " [" + urlParameters.getTest()
                        + "|" + urlParameters.getTestCase()
                        + "|" + urlParameters.getCountry()
                        + "|" + urlParameters.getEnvironment()
                        + "] : Error while executing RunTestCase'" + ex.getMessage();
            }

            /**
             * Clean memory in case testcase has not been launched(Remove all
             * object put in memory)
             */
            try {
                if (tCExecution.getId() == 0) {
                    executionUUIDObject.removeExecutionUUID(tCExecution.getExecutionUUID());
                    org.apache.log4j.Logger.getLogger(AsyncRequestProcessor.class.getName()).log(org.apache.log4j.Level.DEBUG, "Clean ExecutionUUID");

                    if (eSResponse.getExecutionSOAPResponse(tCExecution.getExecutionUUID()) != null) {
                        eSResponse.removeExecutionSOAPResponse(tCExecution.getExecutionUUID());
                        org.apache.log4j.Logger.getLogger(AsyncRequestProcessor.class.getName()).log(org.apache.log4j.Level.DEBUG, "ExecutionSOAPResponse ExecutionUUID");
                        //MyLogger.log(RunTestCase.class.getName(), Level.DEBUG, "Clean ExecutionSOAPResponse");
                    }
                }
            } catch (Exception ex) {
                org.apache.log4j.Logger.getLogger(AsyncRequestProcessor.class.getName()).log(org.apache.log4j.Level.ERROR, "Exception cleaning Memory: ", ex);
            }
        }

        org.apache.log4j.Logger.getLogger(AsyncRequestProcessor.class.getName()).log(org.apache.log4j.Level.INFO, "Processing.. " + result);

    }

    private void parseMandatoryURLParameters(ServletRequest request) {
        String test = ParameterParserUtil.parseStringParam(request.getParameter("Test"), "");
        String testcase = ParameterParserUtil.parseStringParam(request.getParameter("TestCase"), "");
        String country = ParameterParserUtil.parseStringParam(request.getParameter("Country"), "");
        String environment = ParameterParserUtil.parseStringParam(request.getParameter("Environment"), "");
        org.apache.log4j.Logger.getLogger(AsyncRequestProcessor.class.getName()).log(org.apache.log4j.Level.INFO, "Mandatory PARAMS: Test: " + test + " Testcase: " + testcase + " country: " + country + "ENV: " + environment);
        urlParameters = new AsyncRequestParameters(test, testcase, country, environment);
    }

    private void parseExecutionParameters(ServletRequest request) {
        urlParameters.setSeleniumPort(ParameterParserUtil.parseStringParam(request.getParameter("ss_p"), ""));
        urlParameters.setSeleniumIp(ParameterParserUtil.parseStringParam(request.getParameter("ss_ip"), ""));

        String browser = "";
        if (request.getParameter("Browser") != null && !"".equals(request.getParameter("Browser"))) {
            browser = ParameterParserUtil.parseStringParam(request.getParameter("Browser"), "firefox");
        } else {
            browser = ParameterParserUtil.parseStringParam(request.getParameter("browser"), "firefox");
        }
        urlParameters.setBrowser(browser);
        urlParameters.setTag(ParameterParserUtil.parseStringParam(request.getParameter("Tag"), ""));
        //verbose default is 0 
        //outputformat default is "compact" 
        //screenshot, pageSource and seleniumLog default is 1

        urlParameters.setScreenSize(ParameterParserUtil.parseStringParam(request.getParameter("screenSize"), ""));
        //robot = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("robot")), "");
        urlParameters.setVersion(ParameterParserUtil.parseStringParam(request.getParameter("version"), ""));
        urlParameters.setPlatform(ParameterParserUtil.parseStringParam(request.getParameter("platform"), ""));
        /*
         ss_ip=192.168.135.148&
         ss_p=5555&
         browser=firefox&
         test=Omniture%20MOB%20Transversal%20Variable&
         testcase=0003M&
         Country=UK&
         Environment=UAT&
         Tag=TESTE&
         verbose=0&
         outputformat=compact&
         screenshot=1&
         pageSource=1&
         seleniumLog=1 
         */

        //output format compact is default
        //urlParameters.setOutp//(ParameterParserUtil.parseStringParam(request.getParameter("Tag"), ""));
        //screenshot request.getParameter("screenshot"); //default 1 - error
        //screenshot request.getParameter("verbose"); //default 0 
        //timeout = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("timeout")), "");
        //synchroneous = ParameterParserUtil.parseBooleanParam(policy.sanitize(request.getParameter("synchroneous")), true);
        //urlParameters.setPageSource(ParameterParserUtil.parseIntegerParam(request.getParameter("pageSource"), 0));
        //urlParameters.setSeleniumLog(ParameterParserUtil.parseIntegerParam(request.getParameter("seleniumLog"), 0));
        //manualExecution = ParameterParserUtil.parseStringParam(policy.sanitize(request.getParameter("manualExecution")), "N");
        //long idFromQueue = ParameterParserUtil.parseIntegerParam(policy.sanitize(request.getParameter("IdFromQueue")), 0);
        //int numberOfRetries = ParameterParserUtil.parseIntegerParam(policy.sanitize(request.getParameter("retries")), 0);
        //verify the format of the ScreenSize. It must be 2 integer separated by a *. For example : 1024*768
        /*if (!"".equals(screenSize)) {
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
         }*/
    }

    private boolean validateMandatoryParameters(AsyncRequestParameters urlParameters) {
        if (StringUtil.isNullOrEmpty(urlParameters.getTest())
                || StringUtil.isNullOrEmpty(urlParameters.getTestCase())
                || StringUtil.isNullOrEmpty(urlParameters.getCountry())
                || StringUtil.isNullOrEmpty(urlParameters.getEnvironment())) {
            org.apache.log4j.Logger.getLogger(AsyncRequestParameters.class.getName()).log(org.apache.log4j.Level.ERROR, "Mandatory parameters: test or testcase or environment or country is null ");
            return false;
        }
        return true;
    }
}
