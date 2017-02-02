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
package org.cerberus.servlet.crud.testexecution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionInQueue;
import org.cerberus.crud.factory.IFactoryTestCaseExecutionInQueue;
import org.cerberus.crud.service.IApplicationService;
import org.cerberus.crud.service.ICountryEnvParamService;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestCaseExecutionInQueueService;
import org.cerberus.crud.service.ITestCaseExecutionService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.ITestService;
import org.cerberus.dto.ExecutionValidator;
import org.cerberus.engine.threadpool.IExecutionThreadPoolService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.exception.FactoryCreationException;
import org.cerberus.engine.execution.IExecutionCheckService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author cerberus
 */
@WebServlet(name = "GetExecutionQueue", urlPatterns = {"/GetExecutionQueue"})
public class GetExecutionQueue extends HttpServlet {

    private static final String PARAMETER_ROBOT = "robot";
    private static final String PARAMETER_ROBOT_IP = "ss_ip";
    private static final String PARAMETER_ROBOT_PORT = "ss_p";
    private static final String PARAMETER_BROWSER = "Browser";
    private static final String PARAMETER_BROWSER_VERSION = "BrowserVersion";
    private static final String PARAMETER_PLATFORM = "Platform";

    private static final String PARAMETER_TAG = "Tag";
    private static final String PARAMETER_OUTPUT_FORMAT = "OutputFormat";
    private static final String PARAMETER_SCREENSHOT = "Screenshot";
    private static final String PARAMETER_VERBOSE = "Verbose";
    private static final String PARAMETER_TIMEOUT = "timeout";
    private static final String PARAMETER_SYNCHRONEOUS = "Synchroneous";
    private static final String PARAMETER_PAGE_SOURCE = "PageSource";
    private static final String PARAMETER_SELENIUM_LOG = "SeleniumLog";
    private static final String PARAMETER_RETRIES = "retries";
    private static final String PARAMETER_MANUAL_EXECUTION = "manualExecution";

    private static final String DEFAULT_VALUE_OUTPUT_FORMAT = "compact";
    private static final int DEFAULT_VALUE_SCREENSHOT = 0;
    private static final boolean DEFAULT_VALUE_MANUAL_URL = false;
    private static final int DEFAULT_VALUE_VERBOSE = 0;
    private static final long DEFAULT_VALUE_TIMEOUT = 300;
    private static final boolean DEFAULT_VALUE_SYNCHRONEOUS = true;
    private static final int DEFAULT_VALUE_PAGE_SOURCE = 1;
    private static final int DEFAULT_VALUE_SELENIUM_LOG = 1;
    private static final int DEFAULT_VALUE_RETRIES = 0;
    private static final boolean DEFAULT_VALUE_MANUAL_EXECUTION = false;

    private static final String PARAMETER_MANUAL_HOST = "ManualHost";
    private static final String PARAMETER_MANUAL_CONTEXT_ROOT = "ManualContextRoot";
    private static final String PARAMETER_MANUAL_LOGIN_RELATIVE_URL = "ManualLoginRelativeURL";
    private static final String PARAMETER_MANUAL_ENV_DATA = "ManualEnvData";

    private ITestCaseExecutionService testCaseExecutionService;

    private static final Logger LOG = Logger.getLogger(GetExecutionQueue.class);

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws org.json.JSONException
     * @throws org.cerberus.exception.CerberusException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, JSONException, CerberusException {
        AnswerItem answer = new AnswerItem(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        JSONObject jsonResponse = new JSONObject();
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        boolean check = ParameterParserUtil.parseBooleanParam(request.getParameter("check"), false);
        boolean push = ParameterParserUtil.parseBooleanParam(request.getParameter("push"), false);

        if (check) {
            IApplicationService applicationService = appContext.getBean(IApplicationService.class);
            IInvariantService invariantService = appContext.getBean(IInvariantService.class);
            ITestService testService = appContext.getBean(ITestService.class);
            ITestCaseService testCaseService = appContext.getBean(ITestCaseService.class);
            ICountryEnvParamService cepService = appContext.getBean(ICountryEnvParamService.class);
            IParameterService parameterService = appContext.getBean(IParameterService.class);
            testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
            List<ExecutionValidator> inQueue = new ArrayList<ExecutionValidator>();

            JSONArray testCaseList = new JSONArray(request.getParameter("testcase"));
            JSONArray environmentList = new JSONArray(request.getParameter("environment"));
            JSONArray countryList = new JSONArray(request.getParameter("countries"));

            /**
             * Creating all the list from the JSON to call the services
             */
            List<TestCase> TCList = new ArrayList<TestCase>();
            List<String> envList = new ArrayList<String>();
            List<String> countries = new ArrayList<String>();

            for (int index = 0; index < testCaseList.length(); index++) {
                JSONObject testCaseJson = testCaseList.getJSONObject(index);
                TestCase tc = new TestCase();

                tc.setTest(testCaseJson.getString("test"));
                tc.setTestCase(testCaseJson.getString("testcase"));
                TCList.add(tc);
            }

            for (int index = 0; index < environmentList.length(); index++) {
                String environment = environmentList.getString(index);

                envList.add(environment);
            }

            for (int index = 0; index < countryList.length(); index++) {
                String country = countryList.getString(index);
                countries.add(country);
            }

            List<TestCaseExecution> tceList = testCaseExecutionService.createAllTestCaseExecution(TCList, envList, countries);

            IExecutionCheckService execCheckService = appContext.getBean(IExecutionCheckService.class);

            for (TestCaseExecution execution : tceList) {
                boolean exception = false;
                ExecutionValidator validator = new ExecutionValidator();

                try {
                    execution.setTestObj(testService.convert(testService.readByKey(execution.getTest())));
                } catch (CerberusException ex) {
                    MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_TEST_NOT_FOUND);
                    mes.setDescription(mes.getDescription().replace("%TEST%", execution.getTest()));
                    validator.setValid(false);
                    validator.setMessage(mes.getDescription());
                    exception = true;
                }

                try {
                    execution.setTestCaseObj(testCaseService.findTestCaseByKey(execution.getTest(), execution.getTestCase()));
                } catch (CerberusException ex) {
                    MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_TESTCASE_NOT_FOUND);
                    mes.setDescription(mes.getDescription().replace("%TEST%", execution.getTest()));
                    mes.setDescription(mes.getDescription().replace("%TESTCASE%", execution.getTestCase()));
                    validator.setValid(false);
                    validator.setMessage(mes.getDescription());
                    exception = true;
                }

                try {
                    execution.setApplicationObj(applicationService.convert(applicationService.readByKey(execution.getTestCaseObj().getApplication())));
                } catch (CerberusException ex) {
                    MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_APPLICATION_NOT_FOUND);
                    mes.setDescription(mes.getDescription().replace("%APPLI%", execution.getTestCaseObj().getApplication()));
                    validator.setValid(false);
                    validator.setMessage(mes.getDescription());
                    exception = true;
                }

                execution.setEnvironmentData(execution.getEnvironment());

                try {
                    execution.setCountryEnvParam(cepService.convert(cepService.readByKey(execution.getApplicationObj().getSystem(), execution.getCountry(), execution.getEnvironment())));
                } catch (CerberusException ex) {
                    MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COUNTRYENV_NOT_FOUND);
                    mes.setDescription(mes.getDescription().replace("%SYSTEM%", execution.getApplicationObj().getSystem()));
                    mes.setDescription(mes.getDescription().replace("%COUNTRY%", execution.getCountry()));
                    mes.setDescription(mes.getDescription().replace("%ENV%", execution.getEnvironmentData()));
                    validator.setValid(false);
                    validator.setMessage(mes.getDescription());
                    exception = true;
                }

                try {
                    execution.setEnvironmentDataObj(invariantService.findInvariantByIdValue("ENVIRONMENT", execution.getEnvironmentData()));
                } catch (CerberusException ex) {
                    MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_ENVIRONMENT_DOESNOTEXIST);
                    mes.setDescription(mes.getDescription().replace("%ENV%", execution.getEnvironmentData()));
                    validator.setValid(false);
                    validator.setMessage(mes.getDescription());
                    exception = true;
                }

                execution.setBrowser("firefox");

                if (exception == false) {
                    /**
                     * Checking the execution as it would be checked in the
                     * engine
                     */
                    MessageGeneral message = execCheckService.checkTestCaseExecution(execution);
                    if (!(message.equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_CHECKINGPARAMETERS)))) {
                        validator.setValid(false);
                        validator.setMessage(message.getDescription());
                    } else {
                        validator.setValid(true);
                        validator.setMessage(message.getDescription());
                    }
                }
                validator.setExecution(execution);
                inQueue.add(validator);
            }

            JSONArray dataArray = new JSONArray();
            for (ExecutionValidator tce : inQueue) {
                JSONObject exec = new JSONObject();

                exec.put("test", tce.getExecution().getTest());
                exec.put("testcase", tce.getExecution().getTestCase());
                exec.put("env", tce.getExecution().getEnvironment());
                exec.put("country", tce.getExecution().getCountry());
                exec.put("isValid", tce.isValid());
                exec.put("message", tce.getMessage());
                dataArray.put(exec);
            }
            jsonResponse.put("contentTable", dataArray);
        }

        if (push) {
            IExecutionThreadPoolService executionThreadService = appContext.getBean(IExecutionThreadPoolService.class);
            IParameterService parameterService = appContext.getBean(IParameterService.class);
            IFactoryTestCaseExecutionInQueue inQueueFactoryService = appContext.getBean(IFactoryTestCaseExecutionInQueue.class);
            ITestCaseExecutionInQueueService inQueueService = appContext.getBean(ITestCaseExecutionInQueueService.class);
            int addedToQueue = 0;
            JSONArray toAddList = new JSONArray(request.getParameter("toAddList"));
            JSONArray browsers = new JSONArray(request.getParameter("browsers"));
            Date requestDate = new Date();

            /**
             * RETRIEVING ROBOT SETTINGS *
             */
            String robot = ParameterParserUtil.parseStringParam(request.getParameter(PARAMETER_ROBOT), null);
            String robotIP = ParameterParserUtil.parseStringParam(request.getParameter(PARAMETER_ROBOT_IP), null);
            String robotPort = ParameterParserUtil.parseStringParam(request.getParameter(PARAMETER_ROBOT_PORT), null);
            String browserVersion = ParameterParserUtil.parseStringParam(request.getParameter(PARAMETER_BROWSER_VERSION), null);
            String platform = ParameterParserUtil.parseStringParam(request.getParameter(PARAMETER_PLATFORM), null);
            String outputFormat = ParameterParserUtil.parseStringParam(request.getParameter(PARAMETER_OUTPUT_FORMAT), DEFAULT_VALUE_OUTPUT_FORMAT);
            /**
             * RETRIEVING EXECUTION SETTINGS *
             */
            String tag = ParameterParserUtil.parseStringParam(request.getParameter(PARAMETER_TAG), "");
            int screenshot = ParameterParserUtil.parseIntegerParam(request.getParameter(PARAMETER_SCREENSHOT), DEFAULT_VALUE_SCREENSHOT);
            int verbose = ParameterParserUtil.parseIntegerParam(request.getParameter(PARAMETER_VERBOSE), DEFAULT_VALUE_VERBOSE);
            String timeout = request.getParameter(PARAMETER_TIMEOUT);
            boolean synchroneous = ParameterParserUtil.parseBooleanParam(request.getParameter(PARAMETER_SYNCHRONEOUS), DEFAULT_VALUE_SYNCHRONEOUS);
            int pageSource = ParameterParserUtil.parseIntegerParam(request.getParameter(PARAMETER_PAGE_SOURCE), DEFAULT_VALUE_PAGE_SOURCE);
            int seleniumLog = ParameterParserUtil.parseIntegerParam(request.getParameter(PARAMETER_SELENIUM_LOG), DEFAULT_VALUE_SELENIUM_LOG);
            int retries = ParameterParserUtil.parseIntegerParam(request.getParameter(PARAMETER_RETRIES), DEFAULT_VALUE_RETRIES);
            boolean manualExecution = ParameterParserUtil.parseBooleanParam(request.getParameter(PARAMETER_MANUAL_EXECUTION), DEFAULT_VALUE_MANUAL_EXECUTION);
            /**
             * RETRIEVING MANUAL ENVIRONMENT SETTINGS *
             */
            String manualHost = ParameterParserUtil.parseStringParam(request.getParameter(PARAMETER_MANUAL_HOST), null);
            String manualContextRoot = ParameterParserUtil.parseStringParam(request.getParameter(PARAMETER_MANUAL_CONTEXT_ROOT), null);
            String manualLoginRelativeURL = ParameterParserUtil.parseStringParam(request.getParameter(PARAMETER_MANUAL_LOGIN_RELATIVE_URL), null);
            String manualEnvData = ParameterParserUtil.parseStringParam(request.getParameter(PARAMETER_MANUAL_ENV_DATA), null);

            for (int index = 0; index < toAddList.length(); index++) {
                JSONObject toAdd = toAddList.getJSONObject(index);
                boolean manualURL = false;
                if (toAdd.getString("env").equals("MANUAL")) {
                    manualURL = true;
                }
                /**
                 * Creating executions for each browser
                 */
                for (int iterBrowser = 0; iterBrowser < browsers.length(); iterBrowser++) {
                    String browser = browsers.getString(iterBrowser);

                    try {
                        TestCaseExecutionInQueue tceiq = inQueueFactoryService.create(toAdd.getString("test"),
                                toAdd.getString("testcase"),
                                toAdd.getString("country"),
                                toAdd.getString("env"),
                                robot,
                                robotIP,
                                robotPort,
                                browser,
                                browserVersion,
                                platform,
                                manualURL,
                                manualHost,
                                manualContextRoot,
                                manualLoginRelativeURL,
                                manualEnvData,
                                tag,
                                outputFormat,
                                screenshot,
                                verbose,
                                timeout,
                                synchroneous,
                                pageSource,
                                seleniumLog,
                                requestDate,
                                retries,
                                manualExecution);
                        /**
                         * Insert the Execution in the Queue
                         */
                        inQueueService.insert(tceiq);
                        addedToQueue++;
                    } catch (FactoryCreationException ex) {
                        String errorMessage = "Unable to feed the execution queue due to " + ex.getMessage();
                        LOG.error("Unable to create TestCaseExecutionInQueue : " + ex.toString());
                        answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED));
                        answer.getResultMessage().setDescription(errorMessage);
                    }
                }
            }

            /**
             * Trigger the execution
             */
            try {
                executionThreadService.executeNextInQueue();
            } catch (CerberusException ex) {
                String errorMessage = "Unable to feed the execution queue due to " + ex.getMessage();
                LOG.warn(errorMessage);
                answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED));
                answer.getResultMessage().setDescription(errorMessage);
            }
            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());
            jsonResponse.put("addedToQueue", addedToQueue);
            jsonResponse.put("redirect", "ReportingExecutionByTag.jsp?Tag=" + StringUtil.encodeAsJavaScriptURIComponent(tag));
        }

        response.setContentType("application/json");
        response.getWriter().print(jsonResponse.toString());
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
        try {
            processRequest(request, response);
        } catch (JSONException ex) {
            java.util.logging.Logger.getLogger(GetExecutionQueue.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CerberusException ex) {
            java.util.logging.Logger.getLogger(GetExecutionQueue.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        try {
            processRequest(request, response);
        } catch (JSONException ex) {
            java.util.logging.Logger.getLogger(GetExecutionQueue.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CerberusException ex) {
            java.util.logging.Logger.getLogger(GetExecutionQueue.class.getName()).log(Level.SEVERE, null, ex);
        }
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
