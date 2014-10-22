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
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.cerberus.dto.TestCaseWithExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ICampaignService;
import org.cerberus.service.ITestCaseExecutionInQueueService;
import org.cerberus.service.ITestCaseService;
import org.cerberus.util.GraphicHelper;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class CampaignExecutionReport.
 *
 * @author memiks
 */
public class CampaignExecutionStatusBarGraphByFunction extends HttpServlet {

    private final static Logger LOGGER = Logger.getLogger(CampaignExecutionStatusBarGraphByFunction.class);
    
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

            ITestCaseService testCaseService = appContext
                    .getBean(ITestCaseService.class);
            
            ITestCaseExecutionInQueueService testCaseExecutionInQueueService = appContext
                    .getBean(ITestCaseExecutionInQueueService.class);

            PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

            String campaignName = policy.sanitize(request
                    .getParameter("CampaignName"));
            String tag = policy.sanitize(request.getParameter("Tag"));
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
             * Feed hash map with execution from the two list (to get only one by test,testcase,country,env,browser)
             */
            HashMap<String, TestCaseWithExecution> testCaseWithExecutionsList = new HashMap();
            
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
                testCaseWithExecutionsList.put(key, testCaseWithExecutionInQueue);
            }

            testCaseWithExecutions = new ArrayList<TestCaseWithExecution>(testCaseWithExecutionsList.values());
            
            HashMap<String, List<String>> datas = generateMultiBarAxisFromStatus(testCaseWithExecutions);

            List<JSONObject> axis = new ArrayList<JSONObject>();
            
            axis.add(GraphicHelper.generateAxisForMultiBar("OK", 
                    datas.get("OK").toArray(new String[1]),"#00EE00","#33DD33","#33FF55"));

            axis.add(GraphicHelper.generateAxisForMultiBar("KO", 
                    datas.get("KO").toArray(new String[1]),"#F7464A","#FF5A5E","#FF7A7E"));

            axis.add(GraphicHelper.generateAxisForMultiBar("NA", 
                    datas.get("NA").toArray(new String[1]),"#EEEE00","#EEEE55","#EEFE65"));

            axis.add(GraphicHelper.generateAxisForMultiBar("NE", 
                    datas.get("NE").toArray(new String[1]),"#000","#000","#000"));

            axis.add(GraphicHelper.generateAxisForMultiBar("PE", 
                    datas.get("PE").toArray(new String[1]),"#5555EE","#2222FF","#5555EE"));

            axis.add(GraphicHelper.generateAxisForMultiBar("FA", 
                    datas.get("FA").toArray(new String[1]),"#FDB45C","#FFC870","#FFE890"));

            JSONObject jSONResult = GraphicHelper.generateChart(
                    GraphicHelper.ChartType.MultiBar, 
                    axis.toArray(new JSONObject[1]), 
                    datas.get("labels").toArray(new String[1]));

            response.setContentType("application/json");
            response.getWriter().print(jSONResult);
        } catch (CerberusException ex) {
            LOGGER.error(ex);

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

    private HashMap<String, List<String>> generateMultiBarAxisFromStatus(List<TestCaseWithExecution> testCaseWithExecutions) {
        HashMap<String, List<String>> results = new HashMap<String, List<String>>();

        results.put("total", new ArrayList<String>());
        results.put("labels", new ArrayList<String>());
        results.put("OK", new ArrayList<String>());
        results.put("KO", new ArrayList<String>());
        results.put("NA", new ArrayList<String>());
        results.put("NE", new ArrayList<String>());
        results.put("PE", new ArrayList<String>());
        results.put("FA", new ArrayList<String>());
        
        int index;
        String key;
        for (TestCaseWithExecution testCaseWithExecution : testCaseWithExecutions) {
            if(testCaseWithExecution.getFunction() != null && !"".equals(testCaseWithExecution.getFunction())) {
                key = testCaseWithExecution.getFunction();
            } else {
                key = testCaseWithExecution.getTest();
            }

            if(!results.get("labels").contains(key)) {
                results.get("labels").add(key);

                results.get("total").add("0");
                results.get("OK").add("0");
                results.get("KO").add("0");
                results.get("NA").add("0");
                results.get("NE").add("0");
                results.get("PE").add("0");
                results.get("FA").add("0");

                results.get("total").add("0");
            }
            
            index = results.get("labels").indexOf(key);
            
            results.get(testCaseWithExecution.getControlStatus()).set(index,String.valueOf(Integer.valueOf(results.get(testCaseWithExecution.getControlStatus()).get(index))+1));
            results.get("total").set(index, String.valueOf(Integer.parseInt(results.get("total").get(index)) + 1));
        }

        return results;
    }

}
