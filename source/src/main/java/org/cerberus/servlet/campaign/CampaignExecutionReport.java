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
package org.cerberus.servlet.campaign;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.entity.CampaignContent;

import org.cerberus.entity.CampaignParameter;
import org.cerberus.entity.TCase;
import org.cerberus.entity.TestBatteryContent;
import org.cerberus.entity.TestCaseExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ICampaignService;
import org.cerberus.service.ITestBatteryService;
import org.cerberus.service.ITestCaseExecutionService;
import org.cerberus.service.ITestCaseService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.JavaScriptUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class CampaignExecutionReport.
 *
 * @author memiks
 */
public class CampaignExecutionReport extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            ApplicationContext appContext = WebApplicationContextUtils
                    .getWebApplicationContext(this.getServletContext());

            ICampaignService campaignService = appContext
                    .getBean(ICampaignService.class);

            ITestCaseExecutionService testCaseExecutionService = appContext
                    .getBean(ITestCaseExecutionService.class);

            ITestCaseService testCaseService = appContext
                    .getBean(ITestCaseService.class);

            ITestBatteryService testBatteryService = appContext
                    .getBean(ITestBatteryService.class);

            PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

            String campaignName = policy.sanitize(request
                    .getParameter("campaignName"));
            String tag = policy.sanitize(request.getParameter("tag"));
            String[] env = request.getParameterValues("Environment");
            String[] country = request.getParameterValues("Country");
            String[] browser = request.getParameterValues("Browser");

            JSONArray jSONResult = new JSONArray();

            List<CampaignParameter> campaignParameters = campaignService
                    .findCampaignParametersByCampaignName(campaignName);

            List<String> environments = new ArrayList<String>();
            List<String> countries = new ArrayList<String>();
            List<String> browsers = new ArrayList<String>();

            if (env != null && env.length > 0) {
                environments.addAll(Arrays.asList(env));
            }
            if (country != null && country.length > 0) {
                countries.addAll(Arrays.asList(country));
            }
            if (browser != null && browser.length > 0) {
                browsers.addAll(Arrays.asList(browser));
            }

            for (CampaignParameter parameter : campaignParameters) {
                if ("BROWSER".equals(parameter.getParameter())) {
                    browsers.add(parameter.getValue());
                }
                if ("ENVIRONMENT".equals(parameter.getParameter())) {
                    environments.add(parameter.getValue());
                }
                if ("COUNTRY".equals(parameter.getParameter())) {
                    countries.add(parameter.getValue());
                }
            }

            List<TestCaseExecution> findAllCampaignTagExecutions = testCaseExecutionService.findExecutionsByCampaignNameAndTag(campaignName, tag);

            HashMap<String, List<TestCaseExecution>> hmTestCaseExecutionByTestCase = new HashMap<String, List<TestCaseExecution>>();

            String key;
            List<TestCaseExecution> testCaseExecutionsByTestCase;
            for (TestCaseExecution testCaseExecution : findAllCampaignTagExecutions) {
                if (environments.contains(testCaseExecution.getEnvironment())
                        && countries.contains(testCaseExecution.getCountry())
                        && browsers.contains(testCaseExecution.getBrowser())) {
                    key = testCaseExecution.getTest() + testCaseExecution.getTestCase();
                    if (hmTestCaseExecutionByTestCase.containsKey(key)) {
                        testCaseExecutionsByTestCase = hmTestCaseExecutionByTestCase.get(key);
                    } else {
                        testCaseExecutionsByTestCase = new ArrayList<TestCaseExecution>();
                    }
                    testCaseExecutionsByTestCase.add(testCaseExecution);
                    hmTestCaseExecutionByTestCase.put(key, testCaseExecutionsByTestCase);
                }
            }

            for (CampaignContent campaignContent : campaignService.findCampaignContentsByCampaignName(campaignName)) {
                for (TestBatteryContent batteryContent : testBatteryService.findTestBatteryContentsByTestBatteryName(campaignContent.getTestbattery())) {
                    key = batteryContent.getTest() + batteryContent.getTestCase();
                    if (hmTestCaseExecutionByTestCase.containsKey(key)) {
                        for (TestCaseExecution testCaseExecution : hmTestCaseExecutionByTestCase.get(key)) {
                            try {
                                TCase tCase = testCaseService.findTestCaseByKey(testCaseExecution.getTest(), testCaseExecution.getTestCase());
                                jSONResult.put(testCaseExecutionToJSONObject(testCaseExecution, tCase));
                            } catch (JSONException ex) {
                                Logger.getLogger(CampaignExecutionReport.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            }
            response.setContentType("application/json");
            response.getWriter().print(jSONResult);
        } catch (CerberusException ex) {
            Logger.getLogger(CampaignExecutionReport.class.getName()).log(
                    Level.WARNING, null, ex);

        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed"
    // desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
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
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#getServletInfo()
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    /**
     * Test case execution to json object.
     *
     * @param testCaseExecutions the test case executions
     * @return the JSON object
     * @throws JSONException the JSON exception
     */
    private JSONObject testCaseExecutionToJSONObject(
            TestCaseExecution testCaseExecutions, TCase testCase) throws JSONException {
        JSONObject result = new JSONObject();

        result.put("ID", String.valueOf(testCaseExecutions.getId()));
        result.put("Test", JavaScriptUtils.javaScriptEscape(testCaseExecutions.getTest()));
        result.put("TestCase", JavaScriptUtils.javaScriptEscape(testCaseExecutions.getTestCase()));
        result.put("Environment", JavaScriptUtils.javaScriptEscape(testCaseExecutions.getEnvironment()));
        result.put("Start", String.valueOf(testCaseExecutions.getStart()));
        result.put("End", String.valueOf(testCaseExecutions.getEnd()));
        result.put("Country", JavaScriptUtils.javaScriptEscape(testCaseExecutions.getCountry()));
        result.put("Browser", JavaScriptUtils.javaScriptEscape(testCaseExecutions.getBrowser()));
        result.put("ControlStatus", JavaScriptUtils.javaScriptEscape(testCaseExecutions.getControlStatus()));
        result.put("ControlMessage", JavaScriptUtils.javaScriptEscape(testCaseExecutions.getControlMessage()));
        result.put("Status", JavaScriptUtils.javaScriptEscape(testCaseExecutions.getStatus()));

        String bugId;
        if (testCaseExecutions.getApplication().getBugTrackerUrl() != null
                && !"".equals(testCaseExecutions.getApplication().getBugTrackerUrl())) {
            bugId = testCaseExecutions.getApplication().getBugTrackerUrl().replaceAll("%BUGID%", testCase.getBugID());
            bugId = new StringBuffer("<a href='")
                    .append(bugId)
                    .append("' target='reportBugID'>")
                    .append(testCase.getBugID())
                    .append("</a>")
                    .toString();
        } else {
            bugId = testCase.getBugID();
        }
        result.put("BugID", bugId);

        result.put("Comment", JavaScriptUtils.javaScriptEscape(testCase.getComment()));
        result.put("Function", JavaScriptUtils.javaScriptEscape(testCase.getFunction()));
        result.put("Application", JavaScriptUtils.javaScriptEscape(testCase.getApplication()));

        return result;
    }
}
