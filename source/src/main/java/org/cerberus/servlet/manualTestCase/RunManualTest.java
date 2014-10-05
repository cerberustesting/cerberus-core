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
package org.cerberus.servlet.manualTestCase;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.cerberus.entity.Application;
import org.cerberus.entity.CountryEnvParam;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.TCase;
import org.cerberus.entity.TestCaseExecution;
import org.cerberus.entity.TestCaseExecutionSysVer;
import org.cerberus.entity.TestCaseStep;
import org.cerberus.entity.TestCaseStepAction;
import org.cerberus.entity.TestCaseStepActionControlExecution;
import org.cerberus.entity.TestCaseStepActionExecution;
import org.cerberus.entity.TestCaseStepExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTestCaseExecution;
import org.cerberus.factory.IFactoryTestCaseExecutionSysVer;
import org.cerberus.factory.IFactoryTestCaseStep;
import org.cerberus.factory.IFactoryTestCaseStepAction;
import org.cerberus.factory.IFactoryTestCaseStepActionControl;
import org.cerberus.factory.IFactoryTestCaseStepActionControlExecution;
import org.cerberus.factory.IFactoryTestCaseStepActionExecution;
import org.cerberus.factory.IFactoryTestCaseStepExecution;
import org.cerberus.log.MyLogger;
import org.cerberus.service.IApplicationService;
import org.cerberus.service.ICountryEnvParamService;
import org.cerberus.service.ITestCaseExecutionService;
import org.cerberus.service.ITestCaseExecutionSysVerService;
import org.cerberus.service.ITestCaseService;
import org.cerberus.service.ITestCaseStepActionControlExecutionService;
import org.cerberus.service.ITestCaseStepActionExecutionService;
import org.cerberus.service.ITestCaseStepExecutionService;
import org.cerberus.service.ITestCaseStepService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.version.Version;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.HtmlUtils;

/**
 *
 * @author bcivel
 */
public class RunManualTest extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param req servlet request
     * @param resp servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String test = req.getParameter("test");
        String testCase = req.getParameter("testCase");
        String env = req.getParameter("env");
        String country = req.getParameter("country");
        String controlStatus = req.getParameter("controlStatus");
        String controlMessage = req.getParameter("controlMessage");
        String tag = req.getParameter("tag");
        String browser = req.getParameter("browser");
        String browserVersion = req.getParameter("browserVersion");

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseService testService = appContext.getBean(ITestCaseService.class);
        IApplicationService applicationService = appContext.getBean(IApplicationService.class);
        ITestCaseExecutionService testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
        ITestCaseExecutionSysVerService testCaseExecutionSysVerService = appContext.getBean(ITestCaseExecutionSysVerService.class);
        ICountryEnvParamService countryEnvParamService = appContext.getBean(ICountryEnvParamService.class);

        IFactoryTestCaseExecution factoryTCExecution = appContext.getBean(IFactoryTestCaseExecution.class);
        IFactoryTestCaseExecutionSysVer factoryTestCaseExecutionSysVer = appContext.getBean(IFactoryTestCaseExecutionSysVer.class);

        try {
            Application application = null;
            TCase tCase = testService.findTestCaseByKey(test, testCase);
            if (tCase != null) {
                application = applicationService.findApplicationByKey(tCase.getApplication());
            } else {
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
            }
            CountryEnvParam countryEnvParam;
            try {
                countryEnvParam = countryEnvParamService.findCountryEnvParamByKey(application.getSystem(), country, env);
            } catch (CerberusException e) {
                CerberusException ex = new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                ex.getMessageError().setDescription("Combination Environment: '" + env + "' and Country: '" + country
                        + "' not defined for System/Application: " + application.getSystem() + "/" + application.getApplication());
                throw ex;
            }
            String build = countryEnvParam.getBuild();
            String revision = countryEnvParam.getRevision();
            long now = new Date().getTime();
            String version = "Cerberus-" + Version.VERSION;

            String myUser = "";
            if (!(req.getUserPrincipal() == null)) {
                myUser = ParameterParserUtil.parseStringParam(req.getUserPrincipal().getName(), "");
            }

            if (myUser == null || myUser.length() <= 0) {
                myUser = "Manual";
            }

            TestCaseExecution execution = factoryTCExecution.create(0, test, testCase, build, revision, env, country, browser, "", "", browserVersion, now, now,
                    controlStatus, controlMessage, application, "", "", "", tag, "Y", 0, 0, 0, 0, true, "", "", tCase.getStatus(), version,
                    null, null, null, false, "", "", "", "", "", "", null, null, myUser);

            execution.setId(testCaseExecutionService.insertTCExecution(execution));

            TestCaseExecutionSysVer testCaseExecutionSysVer = factoryTestCaseExecutionSysVer.create(execution.getId(), application.getSystem(), build, revision);
            testCaseExecutionSysVerService.insertTestCaseExecutionSysVer(testCaseExecutionSysVer);

            resp.getWriter().print(execution.getId());

        } catch (CerberusException e) {
            MyLogger.log(SaveManualExecution.class.getName(), Level.FATAL, "" + e.getMessageError().getDescription());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("text/html");
            resp.getWriter().print(e.getMessageError().getDescription());
        }
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

    private List<TestCaseStepExecution> createTestCaseStepExecution(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, long executionId) {
        List<TestCaseStepExecution> result = new ArrayList();
        long now = new Date().getTime();
        ITestCaseStepExecutionService tcseService = appContext.getBean(ITestCaseStepExecutionService.class);
        IFactoryTestCaseStepExecution testCaseStepExecutionFactory = appContext.getBean(IFactoryTestCaseStepExecution.class);

        String[] testcase_step_increment = getParameterValuesIfExists(request, "step_increment");
        if (testcase_step_increment != null) {
            for (String inc : testcase_step_increment) {
                int step = Integer.valueOf(getParameterIfExists(request, "step_number_" + inc) == null ? "0" : getParameterIfExists(request, "step_number_" + inc));
                String stepReturnCode = null;

                result.add(testCaseStepExecutionFactory.create(executionId, test, testCase, step, null, now, now, now, now,
                        0, stepReturnCode, null, null, null, null, null, null, 0));
            }
        }
        return result;
    }

    private List<TestCaseStepActionExecution> createTestCaseStepActionExecution(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, long executionId) {
        List<TestCaseStepActionExecution> result = new ArrayList();
        long now = new Date().getTime();
        ITestCaseStepActionExecutionService tcsaeService = appContext.getBean(ITestCaseStepActionExecutionService.class);
        IFactoryTestCaseStepActionExecution testCaseStepActionExecutionFactory = appContext.getBean(IFactoryTestCaseStepActionExecution.class);

        String[] stepAction_increment = getParameterValuesIfExists(request, "action_increment");
        if (stepAction_increment != null) {
            for (String inc : stepAction_increment) {
                int step = Integer.valueOf(getParameterIfExists(request, "action_step_"+ inc) == null 
                        ? "0" : getParameterIfExists(request, "action_step_" + inc));
                int sequence = Integer.valueOf(getParameterIfExists(request, "action_sequence_"+ inc) == null 
                        ? "0" : getParameterIfExists(request, "action_sequence_" + inc));
                String actionReturnCode = null;
                String actionReturnMessage = null;
                String actionScreenshotFileName = null;
                
                testCaseStepActionExecutionFactory.create(executionId, test, testCase, step, sequence, actionReturnCode,
                        actionReturnMessage, null, null, null,  now, now, now, now, 
                        actionScreenshotFileName, null, null, null, null);
            }
        }
        return result;
    }

    private List<TestCaseStepActionControlExecution> createTestCaseStepActionControlExecution(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, long executionId) {
        List<TestCaseStepActionControlExecution> result = new ArrayList();
        long now = new Date().getTime();
        ITestCaseStepActionControlExecutionService tcsaeService = appContext.getBean(ITestCaseStepActionControlExecutionService.class);
        IFactoryTestCaseStepActionControlExecution testCaseStepActionExecutionFactory = appContext.getBean(IFactoryTestCaseStepActionControlExecution.class);

        String[] stepActionControl_increment = getParameterValuesIfExists(request, "control_increment");
        if (stepActionControl_increment != null) {
            for (String inc : stepActionControl_increment) {
                int step = Integer.valueOf(getParameterIfExists(request, "control_step_" + inc) == null 
                        ? "0" : getParameterIfExists(request, "control_step_"+ inc));
                int sequence = Integer.valueOf(getParameterIfExists(request, "control_sequence_"+ inc) == null 
                        ? "0" : getParameterIfExists(request, "control_sequence_" + inc));
                int control = Integer.valueOf(getParameterIfExists(request, "control_control_" + inc) == null 
                        ? "0" : getParameterIfExists(request, "control_control_" + inc));
                String controlReturnCode = null;
                String controlReturnMessage = null;
                String controlScreenshot = null;
                
                testCaseStepActionExecutionFactory.create(executionId, test, testCase, step, sequence, control,
                        controlReturnCode, controlReturnMessage, null, null, null, null, now, now, 
                        now, now, controlScreenshot, null, null, null);
            }
        }
        return result;
    }

    private String getParameterIfExists(HttpServletRequest request, String parameter) {
        String result = null;
        if (request.getParameter(parameter) != null) {
            result = request.getParameter(parameter);
        }
        return result;
    }

    private String[] getParameterValuesIfExists(HttpServletRequest request, String parameter) {
        String[] result = null;
        if (request.getParameterValues(parameter) != null) {
            result = request.getParameterValues(parameter);
        }
        return result;
    }

}
