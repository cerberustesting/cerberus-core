/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionQueue;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ITestCaseExecutionService;
import org.cerberus.dto.SummaryStatisticsDTO;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.AnswerList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.cerberus.crud.service.ITestCaseExecutionQueueService;

/**
 *
 * @author cerberus
 */
@WebServlet(name = "GetReportData", urlPatterns = {"/GetReportData"})
public class GetReportData extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(GetReportData.class);

    ITestCaseExecutionService testCaseExecutionService;
    ITestCaseExecutionQueueService testCaseExecutionInQueueService;

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
        testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
        testCaseExecutionInQueueService = appContext.getBean(ITestCaseExecutionQueueService.class);

        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");

        JSONObject jsonResult = new JSONObject();
        String tag = request.getParameter("Tag");
        boolean split = ParameterParserUtil.parseBooleanParam(request.getParameter("split"), false);

        /**
         * Get list of execution by tag, env, country, browser
         */
        AnswerList<TestCaseExecution> listOfExecution = testCaseExecutionService.readByTagByCriteria(tag, 0, 0, null, null, null);
        List<TestCaseExecution> testCaseExecutions = listOfExecution.getDataList();

        /**
         * Get list of Execution in Queue by Tag
         */
        List<TestCaseExecutionQueue> testCaseExecutionsInQueue = testCaseExecutionInQueueService.findTestCaseExecutionInQueuebyTag(tag);

        /**
         * Feed hash map with execution from the two list (to get only one by
         * test,testcase,country,env,browser)
         */
        testCaseExecutions = hashExecution(testCaseExecutions, testCaseExecutionsInQueue);

        /**
         * Geting the global start and end of the execution tag.
         */
        long startMin = 0;
        long endMax = 0;
        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            if ((startMin == 0) || (testCaseExecution.getStart() < startMin)) {
                startMin = testCaseExecution.getStart();
            }
            if ((endMax == 0) || (testCaseExecution.getEnd() > endMax)) {
                endMax = testCaseExecution.getEnd();
            }
        }

        if (!split) {

            Map<String, JSONObject> axisMap = new HashMap<String, JSONObject>();

            for (TestCaseExecution testCaseWithExecution : testCaseExecutions) {
                String key;
                String controlStatus;
                JSONObject control = new JSONObject();
                JSONObject function = new JSONObject();

                key = testCaseWithExecution.getTest();

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
            jsonResult.put("start", new Date(startMin));
            jsonResult.put("end", new Date(endMax));

        } else if (split) {

            boolean env = ParameterParserUtil.parseBooleanParam(request.getParameter("env"), false);
            boolean country = ParameterParserUtil.parseBooleanParam(request.getParameter("country"), false);
            boolean browser = ParameterParserUtil.parseBooleanParam(request.getParameter("browser"), false);
            boolean app = ParameterParserUtil.parseBooleanParam(request.getParameter("app"), false);

            AnswerList<TestCaseExecution> columnExec = testCaseExecutionService.readDistinctColumnByTag(tag, env, country, browser, app);
            List<TestCaseExecution> columnTcExec = columnExec.getDataList();

            AnswerList<TestCaseExecutionQueue> columnQueue = testCaseExecutionInQueueService.readDistinctColumnByTag(tag, env, country, browser, app);
            List<TestCaseExecutionQueue> columnInQueue = columnQueue.getDataList();

            Map<String, TestCaseExecution> testCaseExecutionsList = new LinkedHashMap<>();

            for (TestCaseExecution column : columnTcExec) {
                String key = column.getBrowser()
                        + column.getCountry()
                        + column.getEnvironment()
                        + column.getApplication();
                testCaseExecutionsList.put(key, column);
            }

            for (TestCaseExecutionQueue column : columnInQueue) {
                TestCaseExecution testCaseExecution = testCaseExecutionInQueueService.convertToTestCaseExecution(column);
                String key = testCaseExecution.getBrowser()
                        + testCaseExecution.getCountry()
                        + testCaseExecution.getEnvironment()
                        + testCaseExecution.getApplicationObj().getApplication();
                testCaseExecutionsList.put(key, testCaseExecution);
            }

            List<TestCaseExecution> res = new ArrayList<>(testCaseExecutionsList.values());

            HashMap<String, SummaryStatisticsDTO> statMap = new HashMap<String, SummaryStatisticsDTO>();
            for (TestCaseExecution column : res) {
                SummaryStatisticsDTO stat = new SummaryStatisticsDTO();
                stat.setEnvironment(column.getEnvironment());
                stat.setCountry(column.getCountry());
                stat.setRobotDecli(column.getBrowser());
                stat.setApplication(column.getApplication());

                statMap.put(column.getEnvironment() + "_" + column.getCountry() + "_" + column.getBrowser() + "_" + column.getApplication(),
                        stat);
            }

            jsonResult.put("contentTable", getStatByEnvCountryBrowser(testCaseExecutions, statMap, env, country, browser, app));

        }

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
            LOG.warn(ex);
        } catch (ParseException ex) {
            LOG.warn(ex);
        } catch (JSONException ex) {
            LOG.warn(ex);
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
            LOG.warn(ex);
        } catch (ParseException ex) {
            LOG.warn(ex);
        } catch (JSONException ex) {
            LOG.warn(ex);
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

    private List<TestCaseExecution> hashExecution(List<TestCaseExecution> testCaseExecutions, List<TestCaseExecutionQueue> testCaseExecutionsInQueue) throws ParseException {
        Map<String, TestCaseExecution> testCaseExecutionsList = new LinkedHashMap<>();
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            String key = testCaseExecution.getBrowser() + "_"
                    + testCaseExecution.getCountry() + "_"
                    + testCaseExecution.getEnvironment() + "_"
                    + testCaseExecution.getTest() + "_"
                    + testCaseExecution.getTestCase();
            testCaseExecutionsList.put(key, testCaseExecution);
        }
        for (TestCaseExecutionQueue testCaseExecutionInQueue : testCaseExecutionsInQueue) {
            TestCaseExecution testCaseExecution = testCaseExecutionInQueueService.convertToTestCaseExecution(testCaseExecutionInQueue);
            String key = testCaseExecution.getBrowser() + "_"
                    + testCaseExecution.getCountry() + "_"
                    + testCaseExecution.getEnvironment() + "_"
                    + testCaseExecution.getTest() + "_"
                    + testCaseExecution.getTestCase();
            if ((testCaseExecutionsList.containsKey(key)
                    && testCaseExecutionsList.get(key).getStart() < testCaseExecution.getStart())
                    || !testCaseExecutionsList.containsKey(key)) {
                testCaseExecutionsList.put(key, testCaseExecution);
            }
        }
        List<TestCaseExecution> result = new ArrayList<TestCaseExecution>(testCaseExecutionsList.values());

        return result;
    }

    private JSONObject getStatByEnvCountryBrowser(List<TestCaseExecution> testCaseExecutions, HashMap<String, SummaryStatisticsDTO> statMap, boolean env, boolean country, boolean browser, boolean app) throws JSONException {
        SummaryStatisticsDTO total = new SummaryStatisticsDTO();
        total.setEnvironment("Total");

        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            StringBuilder key = new StringBuilder();

            key.append((env) ? testCaseExecution.getEnvironment() : "");
            key.append("_");
            key.append((country) ? testCaseExecution.getCountry() : "");
            key.append("_");
            key.append((browser) ? testCaseExecution.getBrowser() : "");
            key.append("_");
            key.append((app) ? testCaseExecution.getApplication() : "");

            if (statMap.containsKey(key.toString())) {
                statMap.get(key.toString()).updateStatisticByStatus(testCaseExecution.getControlStatus());
            }
            total.updateStatisticByStatus(testCaseExecution.getControlStatus());
        }

        return extractSummaryData(statMap, total);
    }

    private JSONObject extractSummaryData(HashMap<String, SummaryStatisticsDTO> summaryMap, SummaryStatisticsDTO total) throws JSONException {
        JSONObject extract = new JSONObject();
        JSONArray dataArray = new JSONArray();
        Gson gson = new Gson();
        //sort keys
        TreeMap<String, SummaryStatisticsDTO> sortedKeys = new TreeMap<>(summaryMap);
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
