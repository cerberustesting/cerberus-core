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
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.log.MyLogger;
import org.cerberus.crud.service.ILoadTestCaseService;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.impl.LogEventService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author memiks
 */
@WebServlet(name = "RunTestCaseManually", urlPatterns = {"/RunTestCaseManually"})
public class RunTestCaseManually extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

            /**
             * Adding Log entry.
             */
            ILogEventService logEventService = appContext.getBean(LogEventService.class);
            logEventService.createPublicCalls("/RunTestCaseManually", "CALL", "RunTestCaseManuallyV0 called : " + request.getRequestURL(), request);

            ILoadTestCaseService loadTestCaseService = appContext.getBean(ILoadTestCaseService.class);

            TestCase testCase = new TestCase();

            PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

            testCase.setTest(policy.sanitize(request.getParameter("Test")));
            testCase.setTestCase(policy.sanitize(request.getParameter("TestCase")));

            /**
             * Load Main TestCase with Step dependencies (Actions/Control)
             */
            MyLogger.log(RunTestCaseManually.class.getName(), Level.DEBUG, " - Loading all Steps information of main testcase.");
            List<TestCaseStep> testCaseStepList;
            testCaseStepList = loadTestCaseService.loadTestCaseStep(testCase);

            try {
                response.setContentType("application/json");
                response.getWriter().print(convertTestCaseToJSON(testCaseStepList));
            } catch (JSONException ex) {
                MyLogger.log(RunTestCaseManually.class.getName(), Level.ERROR, " Unable to retrieve TestCase " + ex.getMessage());
            }

            MyLogger.log(RunTestCaseManually.class.getName(), Level.DEBUG, " - Loaded all Steps information of main testcase. " + testCaseStepList.size() + " Step(s) found.");

        } finally {
            out.close();
        }
    }

    private JSONObject convertTestCaseToJSON(List<TestCaseStep> testCaseStepList) throws JSONException {
        JSONObject testCase = new JSONObject();

        JSONArray steps = new JSONArray();
        for (TestCaseStep testCaseStep : testCaseStepList) {
            steps.put(convertStepToJSON(testCaseStep));
        }

        testCase.put("Steps", steps);

        return testCase;
    }

    private JSONObject convertStepToJSON(TestCaseStep testCaseStep) throws JSONException {
        JSONObject step = new JSONObject();

        step.append("Description", testCaseStep.getDescription());
        step.append("Step", testCaseStep.getStep());

        JSONArray actions = new JSONArray();
        for (TestCaseStepAction testCaseStepAction : testCaseStep.getTestCaseStepAction()) {
            actions.put(convertActionToJSON(testCaseStepAction));
        }
        step.append("Actions", actions);

        return step;
    }

    private JSONObject convertActionToJSON(TestCaseStepAction testCaseStepAction) throws JSONException {
        JSONObject action = new JSONObject();

        action.append("Description", testCaseStepAction.getDescription());
        action.append("Action", testCaseStepAction.getAction());
        action.append("Object", testCaseStepAction.getValue1());
        action.append("Property", testCaseStepAction.getValue2());
        action.append("Sequence", testCaseStepAction.getSequence());

        JSONArray controls = new JSONArray();
        for (TestCaseStepActionControl testCaseStepActionControl : testCaseStepAction.getTestCaseStepActionControl()) {
            controls.put(convertControlToJSON(testCaseStepActionControl));
        }
        action.append("Controls", controls);

        return action;
    }

    private JSONObject convertControlToJSON(TestCaseStepActionControl testCaseStepActionControl) throws JSONException {
        JSONObject control = new JSONObject();

        control.append("Description", testCaseStepActionControl.getDescription());
        control.append("Control", testCaseStepActionControl.getControlSequence());
        control.append("Property", testCaseStepActionControl.getValue2());
        control.append("Value", testCaseStepActionControl.getValue1());
        control.append("Fatal", testCaseStepActionControl.getFatal());
        control.append("Sequence", testCaseStepActionControl.getSequence());
        control.append("Type", testCaseStepActionControl.getControl());

        return control;
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
