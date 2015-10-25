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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.AsyncContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.ParameterParser;
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
import org.cerberus.crud.service.ITestCaseExecutionInQueueService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.impl.TestCaseCountryService;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.engine.impl.RunTestCaseService;
import org.cerberus.servlet.zzpublic.RunTestCase;
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
        String msg = "Async Supported? " + asyncContext.getRequest().isAsyncSupported();
        parseMandatoryURLParameters(asyncContext.getRequest());
        boolean isValid = validateMandatoryParameters(urlParameters);
        if(isValid){
            org.apache.log4j.Logger.getLogger(RunTestCaseCommandLine.class.getName()).log(org.apache.log4j.Level.WARN, "RunTestCaseCommandLine - Start::Name="
                            + Thread.currentThread().getName() + "::ID="
                            + Thread.currentThread().getId() + " TEST = " + urlParameters.getTest() + "TESTCASE " + urlParameters.getTestCase());
            System.out.println(msg);
            org.apache.log4j.Logger.getLogger(AsyncRequestProcessor.class.getName()).log(org.apache.log4j.Level.WARN, msg);
            
            //obtain the remaining parameters
            parseExecutionParameters(asyncContext.getRequest());
            processing();
        }else{
            result = "KO: invalid parameters. Please check if  test, testcase, environment and country are defined. " ;
            org.apache.log4j.Logger.getLogger(AsyncRequestProcessor.class.getName()).log(org.apache.log4j.Level.FATAL, 
                    "KO: invalid parameters. Please check if  test, testcase, environment and country are defined. ");
        }
        //complete the processing
        asyncContext.complete();
        org.apache.log4j.Logger.getLogger(AsyncRequestProcessor.class.getName()).log(org.apache.log4j.Level.WARN, "Process complete");
        
        
    }
    public AsyncRequestProcessor() {
        
    }

    public AsyncRequestProcessor(AsyncContext asyncCtx) {
        this.asyncContext = asyncCtx;
    }
    
    // TODO:FN remove test debug
    private void processing() {
        
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(asyncContext.getRequest().getServletContext());
        ITestCaseService service = appContext.getBean(ITestCaseService.class);
        ITestCaseCountryService tccService = appContext.getBean(TestCaseCountryService.class);
        try {
            TCase tc = service.findTestCaseByKey(urlParameters.getTest(), urlParameters.getTestCase());
            //check if the test case is to be executed in the specific parameters            
            boolean exists = false;
            try{
                TestCaseCountry tcc = tccService.findTestCaseCountryByKey(urlParameters.getTest(), urlParameters.getTestCase(), urlParameters.getCountry());
                if(tcc != null){
                    exists = true;                    
                }
            }catch(CerberusException ex){
                result = "OK  " + tc.getTest() + " - " + tc.getTestCase() + " - is not defined for the selected country!";
            }
            
            
            if(exists){
                //if the test exists for the country then we can execute it
                
                RunTestCaseService runTestCaseService = appContext.getBean(RunTestCaseService.class);
                IFactoryTCase factoryTCase = appContext.getBean(IFactoryTCase.class);
                IFactoryTestCaseExecution factoryTCExecution = appContext.getBean(IFactoryTestCaseExecution.class);

                TCase tCase = factoryTCase.create(urlParameters.getTest(), urlParameters.getTestCase());

                TestCaseExecution tCExecution = factoryTCExecution.create(0, urlParameters.getTest(), urlParameters.getTestCase(), null, null, urlParameters.getEnvironment(),
                        urlParameters.getCountry(), urlParameters.getBrowser(), urlParameters.getVersion(), urlParameters.getPlatform(), "", 0, 0, "", "", null, 
                        urlParameters.getSeleniumIp(), null, urlParameters.getSeleniumPort(), urlParameters.getTag(), "N", urlParameters.getVerbose(), 
                        urlParameters.getScreenshot(), urlParameters.getPageSource(), urlParameters.getSeleniumLog(), false, "", urlParameters.getOutputFormat(), null,
                        Infos.getInstance().getProjectNameAndVersion(), tCase, null, null, false, "", "", "", "", "", "",
                        null, new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTSTARTED), "Selenium", urlParameters.getNrRetries(), urlParameters.getScreenSize());
            
                /*********************EXECUTION***********************/ 
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

                org.apache.log4j.Logger.getLogger(RunTestCaseCommandLine.class.getName()).log(org.apache.log4j.Level.INFO, "Execution Requested : UUID=" + executionUUID);
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

            //while (tCExecution.getNumberOfRetries() >= 0 && !tCExecution.getResultMessage().getCodeString().equals("OK")) {
                try {
            //        //TODO:FN remove log messaegs
                    org.apache.log4j.Logger.getLogger(RunTestCaseCommandLine.class.getName()).log(org.apache.log4j.Level.WARN, "START!! " + tCExecution.getId()); 
                    tCExecution = runTestCaseService.runTestCase(tCExecution);
           //         tCExecution.decreaseNumberOfRetries();
                } catch (Exception ex) {
                    org.apache.log4j.Logger.getLogger(RunTestCaseCommandLine.class.getName()).log(org.apache.log4j.Level.ERROR, "Error while executing RunTestCase ", ex);
                    //MyLogger.log(RunTestCase.class.getName(), Level.FATAL, "Exception on testcase: " + tCExecution.getId() + "\nDetail: " + ex.getMessage() + "\n\n" + ex.toString());
                    
                }
            //}
            //TODO:FN debug purposes
            /*if(session.getDriver() != null){                
                if(session.getDriver().getWindowHandles() !=  null && session.getDriver().getWindowHandles().size() > 0){
                    org.apache.log4j.Logger.getLogger(RunTestCase.class.getName()).log(org.apache.log4j.Level.WARN, "WINDOW HANDLES PENDING: " + session.getDriver().getWindowHandles().size());
                }
                session.getDriver().quit();
            }*/
            /**
             * If execution from queue, remove it from the queue or update
             * information in Queue
             *
             * IdFromQueue must be different of 0 ReturnCode of Testcase should
             * be
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
                //MyLogger.log(RunTestCase.class.getName(), Level.WARN, ex.getMessageError().getDescription());
            }

            /**
             * Clean memory in case testcase has not been launched(Remove all
             * object put in memory)
             */
            try {
                if (tCExecution.getId() == 0) {
                    executionUUIDObject.removeExecutionUUID(tCExecution.getExecutionUUID());
                    org.apache.log4j.Logger.getLogger(RunTestCase.class.getName()).log(org.apache.log4j.Level.DEBUG, "Clean ExecutionUUID");
                    //MyLogger.log(RunTestCase.class.getName(), Level.DEBUG, "Clean ExecutionUUID");

                    if (eSResponse.getExecutionSOAPResponse(tCExecution.getExecutionUUID()) != null) {
                        eSResponse.removeExecutionSOAPResponse(tCExecution.getExecutionUUID());
                        org.apache.log4j.Logger.getLogger(RunTestCase.class.getName()).log(org.apache.log4j.Level.DEBUG, "ExecutionSOAPResponse ExecutionUUID");
                        //MyLogger.log(RunTestCase.class.getName(), Level.DEBUG, "Clean ExecutionSOAPResponse");
                    }
                }
            } catch (Exception ex) {
                org.apache.log4j.Logger.getLogger(RunTestCase.class.getName()).log(org.apache.log4j.Level.ERROR, "Exception cleaning Memory: ", ex);
                //MyLogger.log(RunTestCase.class.getName(), Level.FATAL, "Exception cleaning Memory: " + ex.toString());
            }

                
                
                
                
                result = "KO " + tc.getTest() + " - " + tc.getTestCase() + " - " + tc.getDescription();
            }
            /*else{
                result = "OK  " + tc.getTest() + " - " + tc.getTestCase() + " - is not defined for the selected country!";
            }*/
            
        } catch (CerberusException ex) {
            Logger.getLogger(AsyncRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
            result = "KO " + ex.toString();
        }
        org.apache.log4j.Logger.getLogger(AsyncRequestProcessor.class.getName()).log(org.apache.log4j.Level.WARN, "Processing.. " + result);
        
    }
    private void parseMandatoryURLParameters(ServletRequest request) {
        String test  = ParameterParserUtil.parseStringParam(request.getParameter("test"), "");
        String testcase  = ParameterParserUtil.parseStringParam(request.getParameter("testcase"), "");
        String country  = ParameterParserUtil.parseStringParam(request.getParameter("country"), "");
        String environment  = ParameterParserUtil.parseStringParam(request.getParameter("environment"), "");        
        org.apache.log4j.Logger.getLogger(AsyncRequestProcessor.class.getName()).log(org.apache.log4j.Level.WARN, "DATA: Test: " + test + " Testcase: " + testcase + " country: " + country + "ENV: " + environment);
        urlParameters = new AsyncRequestParameters(test, testcase, country,environment);        
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
        if(StringUtil.isNullOrEmpty(urlParameters.getTest()) 
                || StringUtil.isNullOrEmpty(urlParameters.getTestCase()) 
                || StringUtil.isNullOrEmpty(urlParameters.getCountry()) 
                ||StringUtil.isNullOrEmpty(urlParameters.getEnvironment()) ){
            org.apache.log4j.Logger.getLogger(AsyncRequestParameters.class.getName()).log(org.apache.log4j.Level.FATAL, "Mandatory parameters: test or testcase or environment or country is null ");
            return false;
        }
        return true;
    }
}
