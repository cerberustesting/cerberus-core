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

import com.google.gson.Gson;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.dto.TestCaseWithExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ICampaignService;
import org.cerberus.crud.service.ITestCaseExecutionInQueueService;
import org.cerberus.crud.service.ITestCaseExecutionService;
import org.cerberus.dto.SummaryStatisticsDTO;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.AnswerList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
        ITestCaseExecutionService testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
        ITestCaseExecutionInQueueService testCaseExecutionInQueueService = appContext.getBean(ITestCaseExecutionInQueueService.class);

        JSONObject jsonResult = new JSONObject();
        String tag = request.getParameter("Tag");
        boolean split = ParameterParserUtil.parseBooleanParam(request.getParameter("split"), false);

        /**
         * Get list of execution by tag, env, country, browser
         */
        List<TestCaseWithExecution> testCaseWithExecutions = campaignService.getCampaignTestCaseExecutionForEnvCountriesBrowserTag(tag);

        /**
         * Get list of Execution in Queue by Tag
         */
        List<TestCaseWithExecution> testCaseWithExecutionsInQueue = testCaseExecutionInQueueService.findTestCaseWithExecutionInQueuebyTag(tag);

        /**
         * Feed hash map with execution from the two list (to get only one by
         * test,testcase,country,env,browser)
         */
        testCaseWithExecutions = hashExecution(testCaseWithExecutions, testCaseWithExecutionsInQueue);

        if (!split) {

            Map<String, JSONObject> axisMap = new HashMap<String, JSONObject>();

            for (TestCaseWithExecution testCaseWithExecution : testCaseWithExecutions) {
                String key;
                String controlStatus;
                JSONObject control = new JSONObject();
                JSONObject function = new JSONObject();

                if (testCaseWithExecution.getFunction() != null && !"".equals(testCaseWithExecution.getFunction())) {
                    key = testCaseWithExecution.getFunction();
                } else {
                    key = testCaseWithExecution.getTest();
                }

                controlStatus = testCaseWithExecution.getControlStatus();

                control.put("value", 1);
                control.put("color", getColor(controlStatus));
                control.put("label", controlStatus);
                function.put("name", key);

                if (axisMap.containsKey(key)) {
                    function = axisMap.get(key);
                    if (function.has(controlStatus)) {
                        int prec = function.getJSONObject(controlStatus).getInt("value");
                        control.put("value", prec + 1);
                    }
                }
                function.put(controlStatus, control);
                axisMap.put(key, function);
            }

            jsonResult.put("axis", axisMap.values());
            jsonResult.put("tag", tag);
        } else if (split) {
            boolean env = ParameterParserUtil.parseBooleanParam(request.getParameter("env"), false);
            boolean country = ParameterParserUtil.parseBooleanParam(request.getParameter("country"), false);
            boolean browser = ParameterParserUtil.parseBooleanParam(request.getParameter("browser"), false);
            boolean app = ParameterParserUtil.parseBooleanParam(request.getParameter("app"), false);

            AnswerList columnExec = testCaseExecutionService.readDistinctColumnByTag(tag, env, country, browser, app);
            List<TestCaseWithExecution> columnTcExec = columnExec.getDataList();

            AnswerList columnQueue = testCaseExecutionInQueueService.readDistinctColumnByTag(tag, env, country, browser, app);
            List<TestCaseWithExecution> columnInQueue = columnQueue.getDataList();

            Map<String, TestCaseWithExecution> testCaseWithExecutionsList = new LinkedHashMap();

            for (TestCaseWithExecution column : columnTcExec) {
                String key = column.getBrowser()
                        + column.getCountry()
                        + column.getEnvironment()
                        + column.getApplication();
                testCaseWithExecutionsList.put(key, column);
            }

            for (TestCaseWithExecution column : columnInQueue) {
                String key = column.getBrowser()
                        + column.getCountry()
                        + column.getEnvironment()
                        + column.getApplication();
                testCaseWithExecutionsList.put(key, column);
            }

            List<TestCaseWithExecution> res = new ArrayList<TestCaseWithExecution>(testCaseWithExecutionsList.values());

            HashMap<String, SummaryStatisticsDTO> statMap = new HashMap<String, SummaryStatisticsDTO>();
            for (TestCaseWithExecution column : res) {
                SummaryStatisticsDTO stat = new SummaryStatisticsDTO();
                stat.setEnvironment(column.getEnvironment());
                stat.setCountry(column.getCountry());
                stat.setBrowser(column.getBrowser());
                stat.setApplication(column.getApplication());

                statMap.put(column.getEnvironment() + "_" + column.getCountry() + "_" + column.getBrowser() + "_" + column.getApplication(),
                        stat);
            }

            jsonResult.put("contentTable", getStatByEnvCountryBrowser(testCaseWithExecutions, statMap, env, country, browser, app));

        }

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

    private List<TestCaseWithExecution> hashExecution(List<TestCaseWithExecution> testCaseWithExecutions, List<TestCaseWithExecution> testCaseWithExecutionsInQueue) throws ParseException {
        Map<String, TestCaseWithExecution> testCaseWithExecutionsList = new LinkedHashMap();
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
        List<TestCaseWithExecution> result = new ArrayList<TestCaseWithExecution>(testCaseWithExecutionsList.values());

        return result;
    }

    private JSONObject getStatByEnvCountryBrowser(List<TestCaseWithExecution> testCaseWithExecutions, HashMap<String, SummaryStatisticsDTO> statMap, boolean env, boolean country, boolean browser, boolean app) throws JSONException {
        SummaryStatisticsDTO total = new SummaryStatisticsDTO();
        total.setEnvironment("Total");
        
        for (TestCaseWithExecution testCaseWithExecution : testCaseWithExecutions) {
            StringBuilder key = new StringBuilder();

            key.append((env) ? testCaseWithExecution.getEnvironment() : "");
            key.append("_");
            key.append((country) ? testCaseWithExecution.getCountry() : "");
            key.append("_");
            key.append((browser) ? testCaseWithExecution.getBrowser() : "");
            key.append("_");
            key.append((app) ? testCaseWithExecution.getApplication() : "");

            if (statMap.containsKey(key.toString())) {
                statMap.get(key.toString()).updateStatisticByStatus(testCaseWithExecution.getControlStatus());
            }
            total.updateStatisticByStatus(testCaseWithExecution.getControlStatus());
        }
        
        return extractSummaryData(statMap, total);
    }

    private JSONObject extractSummaryData(HashMap<String, SummaryStatisticsDTO> summaryMap, SummaryStatisticsDTO total) throws JSONException {
        JSONObject extract = new JSONObject();
        JSONArray dataArray = new JSONArray();
        Gson gson = new Gson();
        //sort keys
        TreeMap<String, SummaryStatisticsDTO> sortedKeys = new TreeMap<String, SummaryStatisticsDTO>(summaryMap);
        for (String key : sortedKeys.keySet()) {
            SummaryStatisticsDTO sumStats = summaryMap.get(key);
            //percentage values
            sumStats.updatePercentageStatistics();
            dataArray.put(new JSONObject(gson.toJson(sumStats)));
        }
        total.updatePercentageStatistics();

        extract.put("split", dataArray);
        extract.put("total", new JSONObject(gson.toJson(total)));
        return extract;
    }

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
