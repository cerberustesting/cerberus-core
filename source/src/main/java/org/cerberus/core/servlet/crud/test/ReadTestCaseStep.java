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
package org.cerberus.core.servlet.crud.test;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.service.ITestCaseStepService;
import org.cerberus.core.crud.service.impl.TestCaseStepService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author FNogueira
 */
@WebServlet(name = "ReadTestCaseStep", urlPatterns = {"/ReadTestCaseStep"})
public class ReadTestCaseStep extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ReadTestCaseStep.class);
    
    ITestCaseStepService testCaseStepService;

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
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");    
        testCaseStepService = appContext.getBean(TestCaseStepService.class);
        
        LOG.debug("READTESTCASESTEP ===========================");

        try {
            JSONObject jsonResponse = new JSONObject();
            AnswerItem answer = new AnswerItem<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
            String test = request.getParameter("test");
            String testcase = request.getParameter("testcase");
            int stepId = Integer.parseInt(request.getParameter("stepId"));
            boolean isUsingLibraryStep = ParameterParserUtil.parseBooleanParam(request.getParameter("getUses"), false);

            LOG.debug(test + " + " + testcase + " + " + stepId + " + " + isUsingLibraryStep);
            
            if (isUsingLibraryStep) {
                jsonResponse = getStepUsesByKey(test, testcase, stepId, appContext);
            } else {
                jsonResponse = getStepByKey(test, testcase, stepId, appContext);
            }

            jsonResponse.put("messageType", "OK");
            jsonResponse.put("message", answer.getResultMessage().getDescription());

            response.getWriter().print(jsonResponse.toString());

        } catch (JSONException e) {
            LOG.warn(e.getMessage(), e);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        }
    }

    private JSONObject getStepUsesByKey(String test, String testcase, int stepId, ApplicationContext appContext) throws JSONException {
        JSONObject jsonResponse = new JSONObject();
        AnswerList<TestCaseStep> steps = testCaseStepService.readByLibraryUsed(test, testcase, stepId);
        JSONArray res = new JSONArray();

        for (TestCaseStep testcaseStep : steps.getDataList()) {
            res.put(testcaseStep.toJson());
        }

        jsonResponse.put("step", res);

        return jsonResponse;
    }

    private JSONObject getStepByKey(String test, String testcase, int stepId, ApplicationContext appContext) throws JSONException {
        JSONObject jsonResponse = new JSONObject();

        TestCaseStep testcaseStep = testCaseStepService.readTestcaseStepWithDependencies(test, testcase, stepId);

        return jsonResponse.put("step", testcaseStep.toJson());
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
