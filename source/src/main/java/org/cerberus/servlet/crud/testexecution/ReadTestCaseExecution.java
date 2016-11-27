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

import com.google.common.base.Strings;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringEscapeUtils;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionInQueue;
import org.cerberus.crud.entity.TestCaseLabel;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.ITestCaseExecutionInQueueService;
import org.cerberus.crud.service.ITestCaseExecutionService;
import org.cerberus.crud.service.ITestCaseLabelService;
import org.cerberus.crud.service.impl.InvariantService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.AnswerUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.JavaScriptUtils;

/**
 *
 * @author cerberus
 */
@WebServlet(name = "ReadTestCaseExecution", urlPatterns = {"/ReadTestCaseExecution"})
public class ReadTestCaseExecution extends HttpServlet {

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
     * @throws org.cerberus.exception.CerberusException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, CerberusException, ParseException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");

        testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);

        try {
            JSONObject jsonResponse = new JSONObject();
            AnswerItem answer = new AnswerItem(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
            // Data/Filter Parameters.
            String Tag = ParameterParserUtil.parseStringParam(request.getParameter("Tag"), "");
            String test = ParameterParserUtil.parseStringParam(request.getParameter("test"), "");
            String testCase = ParameterParserUtil.parseStringParam(request.getParameter("testCase"), "");
            String system = ParameterParserUtil.parseStringParam(request.getParameter("system"), "");
            long executionId = ParameterParserUtil.parseLongParam(request.getParameter("executionId"), 0);
            // Switch Parameters.
            boolean executionWithDependency = ParameterParserUtil.parseBooleanParam("executionWithDependency", false);
            String columnName = ParameterParserUtil.parseStringParam(request.getParameter("columnName"), "");
            boolean byColumns = ParameterParserUtil.parseBooleanParam(request.getParameter("byColumns"), false);

            if (!Strings.isNullOrEmpty(columnName)) {
                //If columnName is present, then return the distinct value of this column.
                //In this specific case, do nothing as distinct will be done client side
            } else if (!Tag.equals("") && byColumns) {
                //Return the columns to display in the execution table
                answer = findExecutionColumns(appContext, request, Tag);
                jsonResponse = (JSONObject) answer.getItem();
            } else if (!Tag.equals("") && !byColumns) {
                //Return the list of execution for the execution table
                answer = findExecutionList(appContext, request, Tag);
                jsonResponse = (JSONObject) answer.getItem();
            } else if (!system.isEmpty()) {
                //find execution by system, the remaining parameters are parsed after avoiding the extra processing
                answer = findExecutionListBySystem(system, appContext, request);
                jsonResponse = (JSONObject) answer.getItem();
            } else if (!test.equals("") && !testCase.equals("")) {
                TestCaseExecution lastExec = testCaseExecutionService.findLastTestCaseExecutionNotPE(test, testCase);
                JSONObject result = new JSONObject();
                result.put("id", lastExec.getId());
                result.put("controlStatus", lastExec.getControlStatus());
                result.put("env", lastExec.getEnvironment());
                result.put("country", lastExec.getCountry());
                result.put("end", new Date(lastExec.getEnd())).toString();
                jsonResponse.put("contentTable", result);
            } else if (executionId != 0 && !executionWithDependency) {
                answer = testCaseExecutionService.readByKeyWithDependency(executionId);
                TestCaseExecution tce = (TestCaseExecution) answer.getItem();
                jsonResponse.put("testCaseExecution", tce.toJson());
            } else if (executionId != 0 && executionWithDependency) {

            }

            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());

            response.getWriter().print(jsonResponse.toString());
        } catch (JSONException ex) {
            org.apache.log4j.Logger.getLogger(ReadTestCaseExecution.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
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
        try {
            processRequest(request, response);
        } catch (CerberusException ex) {
            Logger.getLogger(ReadTestCaseExecution.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(ReadTestCaseExecution.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(ReadTestCaseExecution.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(ReadTestCaseExecution.class.getName()).log(Level.SEVERE, null, ex);
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

    private AnswerItem findExecutionColumns(ApplicationContext appContext, HttpServletRequest request, String Tag) throws CerberusException, ParseException, JSONException {
        AnswerItem answer = new AnswerItem(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        JSONObject jsonResponse = new JSONObject();

        AnswerList testCaseExecutionList = new AnswerList();
        AnswerList testCaseExecutionListInQueue = new AnswerList();

        testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
        testCaseExecutionInQueueService = appContext.getBean(ITestCaseExecutionInQueueService.class);

        /**
         * Get list of execution by tag, env, country, browser
         */
        testCaseExecutionList = testCaseExecutionService.readDistinctEnvCoutnryBrowserByTag(Tag);
        List<TestCaseExecution> testCaseExecutions = testCaseExecutionList.getDataList();

        /**
         * Get list of Execution in Queue by Tag
         */
        testCaseExecutionListInQueue = testCaseExecutionInQueueService.readDistinctEnvCoutnryBrowserByTag(Tag);
        List<TestCaseExecutionInQueue> testCaseExecutionsInQueue = testCaseExecutionListInQueue.getDataList();

        /**
         * Feed hash map with execution from the two list (to get only one by
         * test,testcase,country,env,browser)
         */
        LinkedHashMap<String, TestCaseExecution> testCaseExecutionsList = new LinkedHashMap();

        for (TestCaseExecution testCaseWithExecution : testCaseExecutions) {
            String key = testCaseWithExecution.getBrowser() + "_"
                    + testCaseWithExecution.getCountry() + "_"
                    + testCaseWithExecution.getEnvironment() + " "
                    + testCaseWithExecution.getControlStatus();
            testCaseExecutionsList.put(key, testCaseWithExecution);
        }
        for (TestCaseExecutionInQueue testCaseWithExecutionInQueue : testCaseExecutionsInQueue) {
            TestCaseExecution testCaseExecution = testCaseExecutionInQueueService.convertToTestCaseExecution(testCaseWithExecutionInQueue);
            String key = testCaseExecution.getBrowser() + "_"
                    + testCaseExecution.getCountry() + "_"
                    + testCaseExecution.getEnvironment() + "_"
                    + testCaseExecution.getControlStatus();
            testCaseExecutionsList.put(key, testCaseExecution);
        }

        testCaseExecutions = new ArrayList<TestCaseExecution>(testCaseExecutionsList.values());

        JSONObject statusFilter = getStatusList(request);
        JSONObject countryFilter = getCountryList(request, appContext);
        LinkedHashMap<String, JSONObject> columnMap = new LinkedHashMap<String, JSONObject>();

        for (TestCaseExecution testCaseWithExecution : testCaseExecutions) {
            String controlStatus = testCaseWithExecution.getControlStatus();
            if (statusFilter.get(controlStatus).equals("on") && countryFilter.get(testCaseWithExecution.getCountry()).equals("on")) {
                JSONObject column = new JSONObject();
                column.put("country", testCaseWithExecution.getCountry());
                column.put("environment", testCaseWithExecution.getEnvironment());
                column.put("browser", testCaseWithExecution.getBrowser());
                columnMap.put(testCaseWithExecution.getBrowser() + "_" + testCaseWithExecution.getCountry() + "_" + testCaseWithExecution.getEnvironment(), column);
            }
        }

        jsonResponse.put("Columns", columnMap.values());
        answer.setItem(jsonResponse);
        answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        return answer;
    }

    private AnswerItem findExecutionList(ApplicationContext appContext, HttpServletRequest request, String Tag)
            throws CerberusException, ParseException, JSONException {
        AnswerItem answer = new AnswerItem(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        testCaseLabelService = appContext.getBean(ITestCaseLabelService.class);

        int startPosition = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayStart"), "0"));
        int length = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayLength"), "0"));

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "test,testCase,application,priority,status,description,bugId,function");
        String columnToSort[] = sColumns.split(",");
        
        //Get Sorting information
        int numberOfColumnToSort = Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortingCols"), "1"));
        int columnToSortParameter = 0;
        String sort = "asc";
        StringBuilder sortInformation = new StringBuilder();
        for (int c = 0; c < numberOfColumnToSort; c++) {
            columnToSortParameter = Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortCol_" + c), "0"));
            sort = ParameterParserUtil.parseStringParam(request.getParameter("sSortDir_" + c), "asc");
            String columnName = columnToSort[columnToSortParameter];
            sortInformation.append(columnName).append(" ").append(sort);

            if (c != numberOfColumnToSort - 1) {
                sortInformation.append(" , ");
            }
        }

        Map<String, List<String>> individualSearch = new HashMap<String, List<String>>();
        for (int a = 0; a < columnToSort.length; a++) {
            if (null != request.getParameter("sSearch_" + a) && !request.getParameter("sSearch_" + a).isEmpty()) {
                List<String> search = new ArrayList(Arrays.asList(request.getParameter("sSearch_" + a).split(",")));
                individualSearch.put(columnToSort[a], search);
            }
        }

        List<TestCaseExecution> testCaseExecutions = readExecutionByTagList(appContext, Tag, startPosition, length, sortInformation.toString(), searchParameter, individualSearch);

        JSONArray executionList = new JSONArray();
        JSONObject statusFilter = getStatusList(request);
        JSONObject countryFilter = getCountryList(request, appContext);
        LinkedHashMap<String, JSONObject> ttc = new LinkedHashMap<String, JSONObject>();

        String globalStart = "";
        String globalEnd = "";
        String globalStatus = "Finished";

        /**
         * Find the list of labels
         */
        AnswerList testCaseLabelList = testCaseLabelService.readByTestTestCase(null, null);

        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            try {
                if (testCaseExecution.getStart() != 0) {
                    if ((globalStart.isEmpty()) || (globalStart.compareTo(String.valueOf(testCaseExecution.getStart())) > 0)) {
                        globalStart = String.valueOf(testCaseExecution.getStart());
                    }
                }
                if (testCaseExecution.getEnd() != 0) {
                    if ((globalEnd.isEmpty()) || (globalEnd.compareTo(String.valueOf(testCaseExecution.getEnd())) < 0)) {
                        globalEnd = String.valueOf(testCaseExecution.getEnd());
                    }
                }
                if (testCaseExecution.getControlStatus().equalsIgnoreCase("PE")) {
                    globalStatus = "Pending...";
                }
                String controlStatus = testCaseExecution.getControlStatus();
                if (statusFilter.get(controlStatus).equals("on") && countryFilter.get(testCaseExecution.getCountry()).equals("on")) {
                    JSONObject execution = testCaseExecutionToJSONObject(testCaseExecution);
                    String execKey = testCaseExecution.getEnvironment() + " " + testCaseExecution.getCountry() + " " + testCaseExecution.getBrowser();
                    String testCaseKey = testCaseExecution.getTest() + "_" + testCaseExecution.getTestCase();
                    JSONObject execTab = new JSONObject();

                    executionList.put(testCaseExecutionToJSONObject(testCaseExecution));
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
                }
            } catch (JSONException ex) {
                Logger.getLogger(ReadTestCaseExecution.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        JSONObject jsonResponse = new JSONObject();

        jsonResponse.put("globalEnd", globalEnd.toString());
        jsonResponse.put("globalStart", globalStart.toString());
        jsonResponse.put("globalStatus", globalStatus);

        jsonResponse.put("testList", ttc.values());
        jsonResponse.put("iTotalRecords", ttc.size());
        jsonResponse.put("iTotalDisplayRecords", ttc.size());

        answer.setItem(jsonResponse);
        answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        return answer;
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

    private List<TestCaseExecution> hashExecution(List<TestCaseExecution> testCaseExecutions, List<TestCaseExecutionInQueue> testCaseExecutionsInQueue) throws ParseException {
        LinkedHashMap<String, TestCaseExecution> testCaseExecutionsList = new LinkedHashMap();
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            String key = testCaseExecution.getBrowser() + "_"
                    + testCaseExecution.getCountry() + "_"
                    + testCaseExecution.getEnvironment() + "_"
                    + testCaseExecution.getTest() + "_"
                    + testCaseExecution.getTestCase();
            testCaseExecutionsList.put(key, testCaseExecution);
        }
        for (TestCaseExecutionInQueue testCaseExecutionInQueue : testCaseExecutionsInQueue) {
            TestCaseExecution testCaseExecution = testCaseExecutionInQueueService.convertToTestCaseExecution(testCaseExecutionInQueue);
            String key = testCaseExecution.getBrowser() + "_"
                    + testCaseExecution.getCountry() + "_"
                    + testCaseExecution.getEnvironment() + "_"
                    + testCaseExecution.getTest() + "_"
                    + testCaseExecution.getTestCase();
            if ((testCaseExecutionsList.containsKey(key)
                    && testCaseExecutionsList.get(key).getStart() < testCaseExecutionInQueue.getRequestDate().getTime())
                    || !testCaseExecutionsList.containsKey(key)) {
                testCaseExecutionsList.put(key, testCaseExecution);
            }
        }
        List<TestCaseExecution> result = new ArrayList<TestCaseExecution>(testCaseExecutionsList.values());

        return result;
    }

    private JSONObject testCaseExecutionToJSONObject(
            TestCaseExecution testCaseExecution) throws JSONException {
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

    private List<TestCaseExecution> readExecutionByTagList(ApplicationContext appContext, String Tag, int startPosition, int length, String sortInformation, String searchParameter, Map<String, List<String>> individualSearch) throws ParseException, CerberusException {
        AnswerList<TestCaseExecution> testCaseExecution;
        AnswerList<TestCaseExecutionInQueue> testCaseExecutionInQueue;

        ITestCaseExecutionService testCaseExecService = appContext.getBean(ITestCaseExecutionService.class);

        ITestCaseExecutionInQueueService testCaseExecutionInQueueService = appContext.getBean(ITestCaseExecutionInQueueService.class);
        /**
         * Get list of execution by tag, env, country, browser
         */
        testCaseExecution = testCaseExecService.readByTagByCriteria(Tag, startPosition, length, sortInformation, searchParameter, individualSearch);
        List<TestCaseExecution> testCaseExecutions = testCaseExecution.getDataList();
        /**
         * Get list of Execution in Queue by Tag
         */
        testCaseExecutionInQueue = testCaseExecutionInQueueService.readByTagByCriteria(Tag, startPosition, length, sortInformation, searchParameter, individualSearch);
        List<TestCaseExecutionInQueue> testCaseExecutionsInQueue = testCaseExecutionInQueue.getDataList();
        /**
         * Feed hash map with execution from the two list (to get only one by
         * test,testcase,country,env,browser)
         */
        testCaseExecutions = hashExecution(testCaseExecutions, testCaseExecutionsInQueue);
        return testCaseExecutions;
    }

    private AnswerItem findExecutionListBySystem(String system, ApplicationContext appContext, HttpServletRequest request)
            throws ParseException, JSONException {
        AnswerItem answer = new AnswerItem(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));

        /**
         * Parse all parameters used in the search.
         */
        String charset = request.getCharacterEncoding();
        /**
         * Parse parameters - list of values
         */

        List<String> testList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("test"), null, charset);
        List<String> applicationList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("application"), null, charset);
        List<String> projectList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("project"), null, charset);

        List<String> tcstatusList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("tcstatus"), null, charset);
        List<String> groupList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("group"), null, charset);
        List<String> tcactiveList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("tcactive"), null, charset);
        List<String> priorityList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("priority"), null, charset);

        List<String> targetsprintList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("targetsprint"), null, charset);
        List<String> targetrevisionList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("targetrevision"), null, charset);
        List<String> creatorList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("creator"), null, charset);
        List<String> implementerList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("implementer"), null, charset);

        List<String> environmentList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("environment"), null, charset);
        List<String> buildList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("build"), null, charset);
        List<String> revisionList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("revision"), null, charset);

        List<String> countryList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("country"), null, charset);
        List<String> browserList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("browser"), null, charset);
        List<String> tcestatusList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("tcestatus"), null, charset);

        //Sorts the lists 
        if (countryList != null) {
            Collections.sort(countryList);
        }
        if (browserList != null) {
            Collections.sort(browserList);
        }

        /**
         * Parse parameters - free text
         */
        String bugid = StringEscapeUtils.escapeHtml4(request.getParameter("bugid"));
        String ticket = StringEscapeUtils.escapeHtml4(request.getParameter("ticket"));
        String ip = StringEscapeUtils.escapeHtml4(request.getParameter("ip"));
        String port = StringEscapeUtils.escapeHtml4(request.getParameter("port"));
        String tag = StringEscapeUtils.escapeHtml4(request.getParameter("tag"));
        String browserversion = StringEscapeUtils.escapeHtml4(request.getParameter("browserversion"));
        String comment = StringEscapeUtils.escapeHtml4(request.getParameter("comment"));

        /**
         * Gets regular executions (not in queue)
         */
        AnswerList answerExecutions = testCaseExecutionService.readBySystemByVarious(system, testList, applicationList, projectList, tcstatusList, groupList, tcactiveList, priorityList,
                targetsprintList, targetrevisionList, creatorList, implementerList, buildList, revisionList,
                environmentList, countryList, browserList, tcestatusList, ip, port, tag, browserversion, comment, bugid, ticket);

        List<TestCaseExecution> testCaseExecutions = (List<TestCaseExecution>) answerExecutions.getDataList();

        /**
         * Get list of Execution in Queue by Tag
         */
        ITestCaseExecutionInQueueService testCaseExecutionInQueueService = appContext.getBean(ITestCaseExecutionInQueueService.class);
        AnswerList answerExecutionsInQueue = testCaseExecutionInQueueService.readBySystemByVarious(system, testList, applicationList, projectList, tcstatusList, groupList, tcactiveList, priorityList,
                targetsprintList, targetrevisionList, creatorList, implementerList, buildList, revisionList,
                environmentList, countryList, browserList, tcestatusList, ip, port, tag, browserversion, comment, bugid, ticket);
        List<TestCaseExecutionInQueue> testCaseExecutionsInQueue = (List<TestCaseExecutionInQueue>) answerExecutionsInQueue.getDataList();

        /**
         * Merge Test Case Executions
         */
        List<TestCaseExecution> allTestCaseExecutions = hashExecution(testCaseExecutions, testCaseExecutionsInQueue);

        JSONArray executionList = new JSONArray();
        LinkedHashMap<String, JSONObject> ttc = new LinkedHashMap<String, JSONObject>();

        for (TestCaseExecution testCaseExecution : allTestCaseExecutions) {
            try {
                JSONObject execution = testCaseExecutionToJSONObject(testCaseExecution);
                String execKey = testCaseExecution.getCountry() + " " + testCaseExecution.getBrowser(); //the key is country and browser
                String testCaseKey = testCaseExecution.getTest() + "_" + testCaseExecution.getTestCase();
                JSONObject execTab = new JSONObject();

                executionList.put(testCaseExecutionToJSONObject(testCaseExecution));
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
                    ttcObject.put("status", testCaseExecution.getTestCaseObj().getStatus());
                    ttcObject.put("application", testCaseExecution.getApplication());
                    ttcObject.put("bugId", testCaseExecution.getTestCaseObj().getBugID());
                    ttcObject.put("ticket", testCaseExecution.getTestCaseObj().getTicket());
                    ttcObject.put("comment", testCaseExecution.getTestCaseObj().getComment());
                    ttcObject.put("priority", testCaseExecution.getTestCaseObj().getPriority());
                    ttcObject.put("status", testCaseExecution.getStatus());
                    ttcObject.put("group", testCaseExecution.getTestCaseObj().getGroup());
                    execTab.put(execKey, execution);
                    ttcObject.put("execTab", execTab);
                }
                ttc.put(testCaseExecution.getTest() + "_" + testCaseExecution.getTestCase(), ttcObject);
            } catch (JSONException ex) {
                Logger.getLogger(ReadTestCaseExecution.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("contentTable", ttc.values());
        jsonResponse.put("iTotalRecords", ttc.size());
        jsonResponse.put("iTotalDisplayRecords", ttc.size());

        answer.setItem(jsonResponse);
        answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        return answer;
    }

}
