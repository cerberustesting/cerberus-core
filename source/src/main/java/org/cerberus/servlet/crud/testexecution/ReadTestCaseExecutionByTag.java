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

import com.google.gson.Gson;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionInQueue;
import org.cerberus.crud.entity.TestCaseLabel;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.ITestCaseExecutionInQueueService;
import org.cerberus.crud.service.ITestCaseExecutionService;
import org.cerberus.crud.service.ITestCaseLabelService;
import org.cerberus.crud.service.impl.InvariantService;
import org.cerberus.dto.SummaryStatisticsDTO;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.JavaScriptUtils;

/**
 *
 * @author bcivel
 */
public class ReadTestCaseExecutionByTag extends HttpServlet {

    private ITestCaseExecutionService testCaseExecutionService;
    private ITestCaseExecutionInQueueService testCaseExecutionInQueueService;
    private ITestCaseLabelService testCaseLabelService;

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

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");

        AnswerItem answer = new AnswerItem(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));

        testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
        testCaseExecutionInQueueService = appContext.getBean(ITestCaseExecutionInQueueService.class);

        try {
            // Data/Filter Parameters.
            String Tag = ParameterParserUtil.parseStringParam(request.getParameter("Tag"), "");
            List<String> outputReport = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("outputReport"), new ArrayList(), "UTF-8");

            JSONObject jsonResponse = new JSONObject();
            JSONObject statusFilter = getStatusList(request);
            JSONObject countryFilter = getCountryList(request, appContext);

            //Get Data from database
            List<TestCaseExecution> testCaseExecutions = testCaseExecutionService.readLastExecutionAndExecutionInQueueByTag(Tag);

            if (outputReport.isEmpty() || outputReport.contains("table")) {
                jsonResponse.put("table", generateTestCaseExecutionTable(appContext, testCaseExecutions, statusFilter, countryFilter));
            }
            if (outputReport.isEmpty() || outputReport.contains("functionChart")) {
                jsonResponse.put("functionChart", generateFunctionChart(testCaseExecutions, Tag, statusFilter, countryFilter));
            }
            if (outputReport.isEmpty() || outputReport.contains("statsChart")) {
                jsonResponse.put("statsChart", generateStats(request, testCaseExecutions, statusFilter, countryFilter));
            }

            answer.setItem(jsonResponse);
            answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));

            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());

            response.getWriter().print(jsonResponse.toString());

        } catch (ParseException ex) {
            Logger.getLogger(ReadTestCaseExecutionByTag.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CerberusException ex) {
            Logger.getLogger(ReadTestCaseExecutionByTag.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(ReadTestCaseExecutionByTag.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private JSONObject testCaseExecutionToJSONObject(TestCaseExecution testCaseExecution) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("ID", String.valueOf(testCaseExecution.getId()));
        result.put("Test", JavaScriptUtils.javaScriptEscape(testCaseExecution.getTest()));
        result.put("TestCase", JavaScriptUtils.javaScriptEscape(testCaseExecution.getTestCase()));
        result.put("Environment", JavaScriptUtils.javaScriptEscape(testCaseExecution.getEnvironment()));
        result.put("Start", testCaseExecution.getStart());
        result.put("End", testCaseExecution.getEnd());
        result.put("Country", JavaScriptUtils.javaScriptEscape(testCaseExecution.getCountry()));
        result.put("Browser", JavaScriptUtils.javaScriptEscape(testCaseExecution.getBrowser()));
        result.put("ControlStatus", JavaScriptUtils.javaScriptEscape(testCaseExecution.getControlStatus()));
        result.put("ControlMessage", JavaScriptUtils.javaScriptEscape(testCaseExecution.getControlMessage()));
        result.put("Status", JavaScriptUtils.javaScriptEscape(testCaseExecution.getStatus()));

        String bugId;
        if (testCaseExecution.getApplicationObj() != null && testCaseExecution.getApplicationObj().getBugTrackerUrl() != null
                && !"".equals(testCaseExecution.getApplicationObj().getBugTrackerUrl()) && testCaseExecution.getTestCaseObj().getBugID() != null) {
            bugId = testCaseExecution.getApplicationObj().getBugTrackerUrl().replace("%BUGID%", testCaseExecution.getTestCaseObj().getBugID());
            bugId = new StringBuffer("<a href='")
                    .append(bugId)
                    .append("' target='reportBugID'>")
                    .append(testCaseExecution.getTestCaseObj().getBugID())
                    .append("</a>")
                    .toString();
        } else {
            bugId = testCaseExecution.getTestCaseObj().getBugID();
        }
        result.put("BugID", bugId);

        result.put("Comment", JavaScriptUtils.javaScriptEscape(testCaseExecution.getTestCaseObj().getComment()));
        result.put("Priority", JavaScriptUtils.javaScriptEscape(String.valueOf(testCaseExecution.getTestCaseObj().getPriority())));
        result.put("Function", JavaScriptUtils.javaScriptEscape(testCaseExecution.getTestCaseObj().getFunction()));
        result.put("Application", JavaScriptUtils.javaScriptEscape(testCaseExecution.getApplication()));
        result.put("ShortDescription", testCaseExecution.getTestCaseObj().getDescription());

        return result;
    }

    private JSONObject getStatusList(HttpServletRequest request) {
        JSONObject statusList = new JSONObject();

        try {
            statusList.put("OK", ParameterParserUtil.parseStringParam(request.getParameter("OK"), "off"));
            statusList.put("KO", ParameterParserUtil.parseStringParam(request.getParameter("KO"), "off"));
            statusList.put("NA", ParameterParserUtil.parseStringParam(request.getParameter("NA"), "off"));
            statusList.put("NE", ParameterParserUtil.parseStringParam(request.getParameter("NE"), "off"));
            statusList.put("PE", ParameterParserUtil.parseStringParam(request.getParameter("PE"), "off"));
            statusList.put("FA", ParameterParserUtil.parseStringParam(request.getParameter("FA"), "off"));
            statusList.put("CA", ParameterParserUtil.parseStringParam(request.getParameter("CA"), "off"));
        } catch (JSONException ex) {
            Logger.getLogger(ReadTestCaseExecution.class.getName()).log(Level.SEVERE, null, ex);
        }

        return statusList;
    }

    private JSONObject getCountryList(HttpServletRequest request, ApplicationContext appContext) {
        JSONObject countryList = new JSONObject();
        try {
            IInvariantService invariantService = appContext.getBean(InvariantService.class);
            AnswerList answer = invariantService.readByIdname("COUNTRY"); //TODO: handle if the response does not turn ok
            for (Invariant country : (List<Invariant>) answer.getDataList()) {
                countryList.put(country.getValue(), ParameterParserUtil.parseStringParam(request.getParameter(country.getValue()), "off"));
            }
        } catch (JSONException ex) {
            Logger.getLogger(ReadTestCaseExecution.class.getName()).log(Level.SEVERE, null, ex);
        }

        return countryList;
    }

    private JSONObject generateTestCaseExecutionTable(ApplicationContext appContext, List<TestCaseExecution> testCaseExecutions, JSONObject statusFilter, JSONObject countryFilter) {
        JSONObject testCaseExecutionTable = new JSONObject();
        LinkedHashMap<String, JSONObject> ttc = new LinkedHashMap<String, JSONObject>();
        LinkedHashMap<String, JSONObject> columnMap = new LinkedHashMap<String, JSONObject>();
        testCaseLabelService = appContext.getBean(ITestCaseLabelService.class);
        AnswerList testCaseLabelList = testCaseLabelService.readByTestTestCase(null, null);

        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            try {
                String controlStatus = testCaseExecution.getControlStatus();
                if (statusFilter.get(controlStatus).equals("on") && countryFilter.get(testCaseExecution.getCountry()).equals("on")) {

                    JSONObject execution = testCaseExecutionToJSONObject(testCaseExecution);
                    String execKey = testCaseExecution.getEnvironment() + " " + testCaseExecution.getCountry() + " " + testCaseExecution.getBrowser();
                    String testCaseKey = testCaseExecution.getTest() + "_" + testCaseExecution.getTestCase();
                    JSONObject execTab = new JSONObject();
                    JSONObject ttcObject = new JSONObject();

                    if (ttc.containsKey(testCaseKey)) {
                        ttcObject = ttc.get(testCaseKey);
                        execTab = ttcObject.getJSONObject("execTab");
                        execTab.put(execKey, execution);
                        ttcObject.put("execTab", execTab);
                    } else {
                        ttcObject.put("test", testCaseExecution.getTest());
                        ttcObject.put("testCase", testCaseExecution.getTestCase());
                        ttcObject.put("function", testCaseExecution.getTestCaseObj().getFunction());
                        ttcObject.put("shortDesc", testCaseExecution.getTestCaseObj().getDescription());
                        ttcObject.put("status", testCaseExecution.getStatus());
                        ttcObject.put("application", testCaseExecution.getApplication());
                        ttcObject.put("priority", testCaseExecution.getTestCaseObj().getPriority());
                        ttcObject.put("bugId", new JSONObject("{\"bugId\":\"" + testCaseExecution.getTestCaseObj().getBugID() + "\",\"bugTrackerUrl\":\"" + testCaseExecution.getApplicationObj().getBugTrackerUrl().replace("%BUGID%", testCaseExecution.getTestCaseObj().getBugID()) + "\"}"));
                        ttcObject.put("comment", testCaseExecution.getTestCaseObj().getComment());
                        execTab.put(execKey, execution);
                        ttcObject.put("execTab", execTab);

                        /**
                         * Iterate on the label retrieved and generate HashMap
                         * based on the key Test_TestCase
                         */
                        LinkedHashMap<String, JSONArray> testCaseWithLabel = new LinkedHashMap();
                        for (TestCaseLabel label : (List<TestCaseLabel>) testCaseLabelList.getDataList()) {
                            String key = label.getTest() + "_" + label.getTestcase();

                            if (testCaseWithLabel.containsKey(key)) {
                                JSONObject jo = new JSONObject().put("name", label.getLabel().getLabel()).put("color", label.getLabel().getColor()).put("description", label.getLabel().getDescription());
                                testCaseWithLabel.get(key).put(jo);
                            } else {
                                JSONObject jo = new JSONObject().put("name", label.getLabel().getLabel()).put("color", label.getLabel().getColor()).put("description", label.getLabel().getDescription());
                                testCaseWithLabel.put(key, new JSONArray().put(jo));
                            }
                        }
                        ttcObject.put("labels", testCaseWithLabel.get(testCaseExecution.getTest() + "_" + testCaseExecution.getTestCase()));
                    }
                    ttc.put(testCaseExecution.getTest() + "_" + testCaseExecution.getTestCase(), ttcObject);

                    JSONObject column = new JSONObject();
                    column.put("country", testCaseExecution.getCountry());
                    column.put("environment", testCaseExecution.getEnvironment());
                    column.put("browser", testCaseExecution.getBrowser());
                    columnMap.put(testCaseExecution.getBrowser() + "_" + testCaseExecution.getCountry() + "_" + testCaseExecution.getEnvironment(), column);

                }
                Map<String, JSONObject> treeMap = new TreeMap<String, JSONObject>(columnMap);
                testCaseExecutionTable.put("tableContent", ttc.values());
                testCaseExecutionTable.put("iTotalRecords", ttc.size());
                testCaseExecutionTable.put("iTotalDisplayRecords", ttc.size());
                testCaseExecutionTable.put("tableColumns", treeMap.values());
            } catch (JSONException ex) {
                Logger.getLogger(ReadTestCaseExecution.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return testCaseExecutionTable;
    }

    private JSONObject generateFunctionChart(List<TestCaseExecution> testCaseExecutions, String tag, JSONObject statusFilter, JSONObject countryFilter) throws JSONException {
        JSONObject jsonResult = new JSONObject();
        Map<String, JSONObject> axisMap = new HashMap<String, JSONObject>();
        String globalStart = "";
        String globalEnd = "";
        String globalStatus = "Finished";

        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            String key;
            JSONObject control = new JSONObject();
            JSONObject function = new JSONObject();

            String controlStatus = testCaseExecution.getControlStatus();
            if (statusFilter.get(controlStatus).equals("on") && countryFilter.get(testCaseExecution.getCountry()).equals("on")) {
                if (testCaseExecution.getTestCaseObj().getFunction() != null && !"".equals(testCaseExecution.getTestCaseObj().getFunction())) {
                    key = testCaseExecution.getTestCaseObj().getFunction();
                } else {
                    key = testCaseExecution.getTest();
                }

                controlStatus = testCaseExecution.getControlStatus();

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
            if (testCaseExecution.getStart() != 0) {
                if ((globalStart.isEmpty()) || (globalStart.compareTo(String.valueOf(testCaseExecution.getStart())) > 0)) {
                    globalStart = String.valueOf(new Date(testCaseExecution.getStart()));
                }
            }
            if (!testCaseExecution.getControlStatus().equalsIgnoreCase("PE") && testCaseExecution.getEnd() != 0) {
                if ((globalEnd.isEmpty()) || (globalEnd.compareTo(String.valueOf(testCaseExecution.getEnd())) < 0)) {
                    globalEnd = String.valueOf(new Date(testCaseExecution.getEnd()));
                }
            }
            if (testCaseExecution.getControlStatus().equalsIgnoreCase("PE")) {
                globalStatus = "Pending...";
            }
        }

        jsonResult.put("axis", axisMap.values());
        jsonResult.put("tag", tag);
        jsonResult.put("globalEnd", globalEnd.toString());
        jsonResult.put("globalStart", globalStart.toString());
        jsonResult.put("globalStatus", globalStatus);
        return jsonResult;
    }

    private JSONObject generateStats(HttpServletRequest request, List<TestCaseExecution> testCaseExecutions, JSONObject statusFilter, JSONObject countryFilter) throws JSONException {

        JSONObject jsonResult = new JSONObject();
        boolean env = request.getParameter("env") != null;
        boolean country = request.getParameter("country") != null;
        boolean browser = request.getParameter("browser") != null;
        boolean app = request.getParameter("app") != null;

        HashMap<String, SummaryStatisticsDTO> statMap = new HashMap<String, SummaryStatisticsDTO>();
        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            String controlStatus = testCaseExecution.getControlStatus();
            if (statusFilter.get(controlStatus).equals("on") && countryFilter.get(testCaseExecution.getCountry()).equals("on")) {

                StringBuilder key = new StringBuilder();

                key.append((env) ? testCaseExecution.getEnvironment() : "");
                key.append("_");
                key.append((country) ? testCaseExecution.getCountry() : "");
                key.append("_");
                key.append((browser) ? testCaseExecution.getBrowser() : "");
                key.append("_");
                key.append((app) ? testCaseExecution.getApplication() : "");

                SummaryStatisticsDTO stat = new SummaryStatisticsDTO();
                stat.setEnvironment(testCaseExecution.getEnvironment());
                stat.setCountry(testCaseExecution.getCountry());
                stat.setBrowser(testCaseExecution.getBrowser());
                stat.setApplication(testCaseExecution.getApplication());

                statMap.put(key.toString(), stat);
            }
        }

        jsonResult.put("contentTable", getStatByEnvCountryBrowser(testCaseExecutions, statMap, env, country, browser, app, statusFilter, countryFilter));

        return jsonResult;
    }

    private JSONObject getStatByEnvCountryBrowser(List<TestCaseExecution> testCaseExecutions, HashMap<String, SummaryStatisticsDTO> statMap, boolean env, boolean country, boolean browser, boolean app, JSONObject statusFilter, JSONObject countryFilter) throws JSONException {
        SummaryStatisticsDTO total = new SummaryStatisticsDTO();
        total.setEnvironment("Total");

        for (TestCaseExecution testCaseExecution : testCaseExecutions) {

            String controlStatus = testCaseExecution.getControlStatus();
            if (statusFilter.get(controlStatus).equals("on") && countryFilter.get(testCaseExecution.getCountry()).equals("on")) {
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
