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
package org.cerberus.servlet.reporting;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.dto.TestCaseWithExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ICampaignService;
import org.cerberus.service.ITestCaseExecutionInQueueService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author cerberus
 */
@WebServlet(name = "GetReportData", urlPatterns = {"/GetReportData"})
public class GetReportData extends HttpServlet {

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
            throws ServletException, IOException, CerberusException, ParseException, JSONException {
        response.setContentType("text/html;charset=UTF-8");
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ICampaignService campaignService = appContext.getBean(ICampaignService.class);
        ITestCaseExecutionInQueueService testCaseExecutionInQueueService = appContext.getBean(ITestCaseExecutionInQueueService.class);

        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        String campaignName = policy.sanitize(request
                .getParameter("CampaignName"));
        String tag = request.getParameter("Tag");
        String[] env = request.getParameterValues("Environment");
        String[] country = request.getParameterValues("Country");
        String[] browser = request.getParameterValues("Browser");

        /**
         * Get list of execution by tag, env, country, browser
         */
        List<TestCaseWithExecution> testCaseWithExecutions = campaignService.getCampaignTestCaseExecutionForEnvCountriesBrowserTag(campaignName, tag, env, country, browser);

        /**
         * Get list of Execution in Queue by Tag
         */
        List<TestCaseWithExecution> testCaseWithExecutionsInQueue = testCaseExecutionInQueueService.findTestCaseWithExecutionInQueuebyTag(tag);

        /**
         * Feed hash map with execution from the two list (to get only one by
         * test,testcase,country,env,browser)
         */
        HashMap<String, TestCaseWithExecution> testCaseWithExecutionsList = new HashMap();
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        for (TestCaseWithExecution testCaseWithExecution : testCaseWithExecutions) {
            String key = testCaseWithExecution.getBrowser() + "_"
                    + testCaseWithExecution.getCountry() + "_"
                    + testCaseWithExecution.getEnvironment() + "_"
                    + testCaseWithExecution.getTest() + "_"
                    + testCaseWithExecution.getTestCase();
            testCaseWithExecutionsList.put(key, testCaseWithExecution);
        }
        for (TestCaseWithExecution testCaseWithExecutionInQueue : testCaseWithExecutionsInQueue) {
            String key = testCaseWithExecutionInQueue.getBrowser() + "_"
                    + testCaseWithExecutionInQueue.getCountry() + "_"
                    + testCaseWithExecutionInQueue.getEnvironment() + "_"
                    + testCaseWithExecutionInQueue.getTest() + "_"
                    + testCaseWithExecutionInQueue.getTestCase();
            if ((testCaseWithExecutionsList.containsKey(key)
                    && formater.parse(testCaseWithExecutionsList.get(key).getStart()).before(formater.parse(testCaseWithExecutionInQueue.getStart())))
                    || !testCaseWithExecutionsList.containsKey(key)) {
                testCaseWithExecutionsList.put(key, testCaseWithExecutionInQueue);
            }
        }
        testCaseWithExecutions = new ArrayList<TestCaseWithExecution>(testCaseWithExecutionsList.values());

        JSONArray axis = new JSONArray();
        for (TestCaseWithExecution testCaseWithExecution : testCaseWithExecutions) {
            String key;
            String controlStatus;
            JSONObject control = new JSONObject();
            if (testCaseWithExecution.getFunction() != null && !"".equals(testCaseWithExecution.getFunction())) {
                key = testCaseWithExecution.getFunction();
            } else {
                key = testCaseWithExecution.getTest();
            }

            controlStatus = testCaseWithExecution.getControlStatus();

            control.put("value", 1);
            control.put("color", getColor(controlStatus));
            control.put("label", controlStatus);
            JSONObject tmp = new JSONObject();
            tmp.put("name", key);
            boolean isKey = false;
            for (int i = 0; i < axis.length(); i++) {
                tmp = axis.getJSONObject(i);
                if (tmp.has("name") && tmp.getString("name").equals(key)) {
                    isKey = true;
                    if (tmp.has(controlStatus)) {
                        int prec = tmp.getJSONObject(controlStatus).getInt("value");
                        control.put("value", prec + 1);
                    }
                    axis.getJSONObject(i).put(controlStatus, control);
                }
            }
            if (isKey == false) {
                tmp = new JSONObject();
                tmp.put("name", key);
                tmp.put(controlStatus, control);
                axis.put(tmp);
            }
        }
        JSONObject jsonResult = new JSONObject();

        jsonResult.put("axis", axis);

        response.setContentType("application/json");
        response.getWriter().print(jsonResult);
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
        } catch (CerberusException ex) {
            Logger.getLogger(GetReportData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(GetReportData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(GetReportData.class.getName()).log(Level.SEVERE, null, ex);
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
        } catch (CerberusException ex) {
            Logger.getLogger(GetReportData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(GetReportData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(GetReportData.class.getName()).log(Level.SEVERE, null, ex);
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

    private String getColor(String controlStatus) {
        String color = null;

        if ("OK".equals(controlStatus)) {
            color = "#5CB85C";
        } else if ("KO".equals(controlStatus)) {
            color = "#D9534F";
        } else if ("FA".equals(controlStatus) || "CA".equals(controlStatus)) {
            color = "#F0AD4E";
        } else if ("NA".equals(controlStatus)) {
            color = "#F1C40F";
        } else if ("NE".equals(controlStatus)) {
            color = "#34495E";
        } else if ("PE".equals(controlStatus)) {
            color = "#3498DB";
        } else {
            color = "#000000";
        }
        return color;
    }

}
