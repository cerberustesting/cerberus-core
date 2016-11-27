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
package org.cerberus.servlet.manualtestcase;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.cerberus.crud.entity.*;
import org.cerberus.crud.factory.IFactoryTestCaseStepActionControlExecution;
import org.cerberus.crud.factory.IFactoryTestCaseStepActionExecution;
import org.cerberus.crud.factory.IFactoryTestCaseStepExecution;
import org.cerberus.crud.service.*;
import org.cerberus.engine.execution.IRecorderService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.util.answer.AnswerItem;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

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
        String controlStatus = getParameterIfExists(req, "executionStatus");
        String controlMessage = req.getParameter("controlMessage");
        long executionId = Long.valueOf(req.getParameter("executionId"));
        String cancelExecution = req.getParameter("isCancelExecution") == null ? "" : req.getParameter("isCancelExecution");

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IParameterService parameterService = appContext.getBean(IParameterService.class);
        ITestCaseExecutionService testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
        ITestCaseStepExecutionService testCaseStepExecutionService = appContext.getBean(ITestCaseStepExecutionService.class);
        ITestCaseStepActionExecutionService testCaseStepActionExecutionService = appContext.getBean(ITestCaseStepActionExecutionService.class);
        ITestCaseStepActionControlExecutionService testCaseStepActionControlExecutionService = appContext.getBean(ITestCaseStepActionControlExecutionService.class);

        try {
            TestCaseExecution execution = testCaseExecutionService.findTCExecutionByKey(executionId);
            execution.setControlMessage(controlMessage);
            String test = execution.getTest();
            String testCase = execution.getTestCase();
            List<String> status = new ArrayList();
            List<String> stepStatus = new ArrayList();

            /**
             * If cancel execution, Set execution to cancel
             */
            if (cancelExecution.equals("Y")) {
                execution.setControlStatus("CA");
                testCaseExecutionService.updateTCExecution(execution);
            } else {

                /**
                 * Get Step Execution and insert them into Database
                 */
                List<TestCaseStepExecution> tcseList = getTestCaseStepExecution(req, appContext, test, testCase, executionId);

                //If no step, set execution to controlStatus kept from the page 
                if (tcseList.isEmpty()) {
                    execution.setControlStatus(controlStatus == null ? "OK" : controlStatus);
                    testCaseExecutionService.updateTCExecution(execution);
                } else {
                    for (TestCaseStepExecution tcse : tcseList) {
                        testCaseStepExecutionService.insertTestCaseStepExecution(tcse);
                        /**
                         * Get Step Action Execution and insert them into
                         * Database
                         */
                        List tcae = new ArrayList();
                        for (TestCaseStepActionExecution tcsae : getTestCaseStepActionExecution(req, appContext, test, testCase, executionId, tcse.getSort())) {
                            testCaseStepActionExecutionService.insertTestCaseStepActionExecution(tcsae);
                            status.add(tcsae.getReturnCode());

                            /**
                             * Get Step Action Control Execution and insert them
                             * into Database
                             */
                            for (TestCaseStepActionControlExecution tcsace : getTestCaseStepActionControlExecution(req, appContext, test, testCase, executionId, tcse.getSort(), tcsae.getSort())) {
                                testCaseStepActionControlExecutionService.insertTestCaseStepActionControlExecution(tcsace);
                                status.add(tcsace.getReturnCode());
                            }
                        }

                        /**
                         * Update stepexecution with status of action/control
                         */
                        if (status.contains("KO")) {
                            tcse.setReturnCode("KO");
                            stepStatus.add("KO");
                        } else if (status.contains("NA")) {
                            tcse.setReturnCode("NA");
                            stepStatus.add("NA");
                        } else {
                            tcse.setReturnCode(tcse.getReturnCode() != null ? tcse.getReturnCode() : "OK");
                            stepStatus.add(tcse.getReturnCode() != null ? tcse.getReturnCode() : "OK");
                        }
                        testCaseStepExecutionService.updateTestCaseStepExecution(tcse);

                    }

                    /**
                     * Update execution with status of action/control
                     */
                    if (stepStatus.contains("KO")) {
                        execution.setControlStatus("KO");
                    } else if (stepStatus.contains("NA")) {
                        execution.setControlStatus("NA");
                    } else {
                        execution.setControlStatus("OK");
                    }

                    testCaseExecutionService.updateTCExecution(execution);

                }

                //Notify it's finnished
//        WebsocketTest wst = new WebsocketTest();
//        try {
//            wst.handleMessage(execution.getTag());
//        } catch (IOException ex) {
//            MyLogger.log(SaveManualExecution.class.getName(), Level.FATAL, "" + ex);
//        }
                /**
                 * Get Step Execution and insert them into Database
                 */
//            for (TestCaseExecutionData tced : getTestCaseExecutionData(req, appContext, test, testCase, executionId)){
//            testCaseStepExecutionService.insertTestCaseStepExecution(null);
//            }
            }

            AnswerItem a = parameterService.readByKey("", "cerberus_executiondetail_use");
            if (a.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && a.getItem() != null) {
                Parameter p = (Parameter) a.getItem();
                if (!p.getValue().equals("N")) {
                    resp.sendRedirect("ExecutionDetail2.jsp?executionId=" + executionId);
                } else {
                    resp.sendRedirect("ExecutionDetail.jsp?id_tc=" + executionId);
                }
            } else {
                resp.sendRedirect("ExecutionDetail.jsp?id_tc=" + executionId);
            }

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

    private List<TestCaseStepExecution> getTestCaseStepExecution(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, long executionId) {
        List<TestCaseStepExecution> result = new ArrayList();
        long now = new Date().getTime();
        IFactoryTestCaseStepExecution testCaseStepExecutionFactory = appContext.getBean(IFactoryTestCaseStepExecution.class);

        String[] testcase_step_increment = getParameterValuesIfExists(request, "step_increment");
        if (testcase_step_increment != null) {
            for (String inc : testcase_step_increment) {
                int step = Integer.valueOf(getParameterIfExists(request, "step_technical_number_" + inc) == null ? "0" : getParameterIfExists(request, "step_technical_number_" + inc));
                int sort = Integer.valueOf(getParameterIfExists(request, "step_number_" + inc) == null ? "0" : getParameterIfExists(request, "step_number_" + inc));
                String stepResultMessage = getParameterIfExists(request, "stepResultMessage_" + inc);
                String stepReturnCode = getParameterIfExists(request, "stepStatus_" + inc);

                result.add(testCaseStepExecutionFactory.create(executionId, test, testCase, step, sort, null, now, now, now, now,
                        new BigDecimal("0"), stepReturnCode, stepResultMessage, ""));
            }
        }
        return result;
    }

    private List<TestCaseStepActionExecution> getTestCaseStepActionExecution(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, long executionId, int stepSort) {
        List<TestCaseStepActionExecution> result = new ArrayList();
        IRecorderService recorderService = appContext.getBean(IRecorderService.class);
        long now = new Date().getTime();
        IFactoryTestCaseStepActionExecution testCaseStepActionExecutionFactory = appContext.getBean(IFactoryTestCaseStepActionExecution.class);
        //IRecorderService recorderService = appContext.getBean(IRecorderService.class);

        String[] stepAction_increment = getParameterValuesIfExists(request, "action_increment_" + stepSort);
        if (stepAction_increment != null) {
            for (String inc : stepAction_increment) {
                int step = Integer.valueOf(getParameterIfExists(request, "action_technical_step_" + stepSort + "_" + inc) == null
                        ? "0" : getParameterIfExists(request, "action_technical_step_" + stepSort + "_" + inc));
                int sequence = Integer.valueOf(getParameterIfExists(request, "action_technical_sequence_" + stepSort + "_" + inc) == null
                        ? "0" : getParameterIfExists(request, "action_technical_sequence_" + stepSort + "_" + inc));
                int sort = Integer.valueOf(getParameterIfExists(request, "action_sequence_" + stepSort + "_" + inc) == null
                        ? "0" : getParameterIfExists(request, "action_sequence_" + stepSort + "_" + inc));
                String actionReturnCode = getParameterIfExists(request, "actionStatus_" + stepSort + "_" + inc);
                String actionReturnMessage = getParameterIfExists(request, "actionResultMessage_" + stepSort + "_" + inc);

                result.add(testCaseStepActionExecutionFactory.create(executionId, test, testCase, step, sequence, sort, actionReturnCode,
                        actionReturnMessage, "", "", "Manual Action", "", "", "", "", "", now, now, now, now,
                        null, "", null, null));
            }
        }
        return result;
    }

    private List<TestCaseStepActionControlExecution> getTestCaseStepActionControlExecution(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, long executionId, int stepSort, int actionSort) {
        List<TestCaseStepActionControlExecution> result = new ArrayList();
        IRecorderService recorderService = appContext.getBean(IRecorderService.class);
        long now = new Date().getTime();
        IFactoryTestCaseStepActionControlExecution testCaseStepActionExecutionFactory = appContext.getBean(IFactoryTestCaseStepActionControlExecution.class);
        //IRecorderService recorderService = appContext.getBean(IRecorderService.class);

        String[] stepActionControl_increment = getParameterValuesIfExists(request, "control_increment_" + stepSort + "_" + actionSort);
        if (stepActionControl_increment != null) {
            for (String inc : stepActionControl_increment) {
                int step = Integer.valueOf(getParameterIfExists(request, "control_technical_step_" + stepSort + "_" + actionSort + "_" + inc) == null
                        ? "0" : getParameterIfExists(request, "control_technical_step_" + stepSort + "_" + actionSort + "_" + inc));
                int sequence = Integer.valueOf(getParameterIfExists(request, "control_technical_sequence_" + stepSort + "_" + actionSort + "_" + inc) == null
                        ? "0" : getParameterIfExists(request, "control_technical_sequence_" + stepSort + "_" + actionSort + "_" + inc));
                int control = Integer.valueOf(getParameterIfExists(request, "control_technical_control_" + stepSort + "_" + actionSort + "_" + inc) == null
                        ? "0" : getParameterIfExists(request, "control_technical_control_" + stepSort + "_" + actionSort + "_" + inc));
                int sort = Integer.valueOf(getParameterIfExists(request, "control_control_" + stepSort + "_" + actionSort + "_" + inc) == null
                        ? "0" : getParameterIfExists(request, "control_control_" + stepSort + "_" + actionSort + "_" + inc));
                String controlReturnCode = getParameterIfExists(request, "controlStatus_" + stepSort + "_" + actionSort + "_" + inc);
                String controlReturnMessage = getParameterIfExists(request, "controlResultMessage_" + stepSort + "_" + actionSort + "_" + inc);

                result.add(testCaseStepActionExecutionFactory.create(executionId, test, testCase, step, sequence, control, sort,
                        controlReturnCode, controlReturnMessage,"always", "", "Manual Control", null, null, null, null, null, now, now,
                        now, now, "", null, null));
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

    private List<TestCaseExecutionData> getTestCaseExecutionData(HttpServletRequest req, ApplicationContext appContext, String test, String testCase, long executionId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
