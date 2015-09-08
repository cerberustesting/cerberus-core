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
package org.cerberus.servlet.testCaseExecution;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.dto.TestCaseWithExecution;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.ITestCaseExecutionInQueueService;
import org.cerberus.service.ITestCaseExecutionService;
import org.cerberus.service.impl.LogEventService;
import org.cerberus.servlet.campaign.CampaignExecutionReport;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.JavaScriptUtils;

/**
 *
 * @author cerberus
 */
@WebServlet(name = "ReadTestCaseExecution", urlPatterns = {"/ReadTestCaseExecution"})
public class ReadTestCaseExecution extends HttpServlet {

    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
    private ITestCaseExecutionService testCaseExecutionService;

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

        /**
         * Adding Log entry.
         */
        ILogEventService logEventService = appContext.getBean(LogEventService.class);
        logEventService.createPublicCalls("/GetCampaignExecutionsCommand", "CALL", "GetCampaignExecutionsCommandV0 called : " + request.getRequestURL(), request);

        testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);

        try {
            JSONObject jsonResponse = new JSONObject();
            AnswerItem answer = new AnswerItem(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
            int sEcho = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("sEcho"), "0"));
            String Tag = ParameterParserUtil.parseStringParam(request.getParameter("Tag"), "");
            if (sEcho == 0 && !Tag.equals("")) {
                answer = findExecutionColumns(appContext, request, Tag);
                jsonResponse = (JSONObject) answer.getItem();
            } else if (sEcho != 0 && !Tag.equals("")) {
                answer = findExecutionList(appContext, request, Tag);
                jsonResponse = (JSONObject) answer.getItem();
            } else if (sEcho == 0) {
                answer = findTagList(appContext, request, response);
                jsonResponse = (JSONObject) answer.getItem();
            }

            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());
            response.setContentType("application/json");
            response.getWriter().print(jsonResponse.toString());
        } catch (JSONException ex) {
            org.apache.log4j.Logger.getLogger(ReadTestCaseExecution.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.setContentType("application/json");
            MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("{'messageType':'").append(msg.getCode()).append("', ");
            errorMessage.append(" 'message': '");
            errorMessage.append(msg.getDescription().replace("%DESCRIPTION%", "Unable to check the status of your request! Try later or - Open a bug or ask for any new feature \n"
                    + "<a href=\"https://github.com/vertigo17/Cerberus/issues/\" target=\"_blank\">here</a>"));
            errorMessage.append("'}");
            response.getWriter().print(errorMessage.toString());

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

    private AnswerItem findTagList(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response)
            throws CerberusException, JSONException {
        AnswerItem answer = new AnswerItem();
        JSONObject jsonResponse = new JSONObject();

        testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);

        AnswerList resp;

        resp = testCaseExecutionService.findTagList();

        jsonResponse.put("tags", resp.getDataList());

        answer.setItem(jsonResponse);
        answer.setResultMessage(resp.getResultMessage());
        return answer;
    }

    private AnswerItem findExecutionColumns(ApplicationContext appContext, HttpServletRequest request, String Tag) throws CerberusException, ParseException, JSONException {
        AnswerItem answer = new AnswerItem(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        JSONObject jsonResponse = new JSONObject();

        AnswerList testCaseExecution = new AnswerList();
        AnswerList testCaseExecutionInQueue = new AnswerList();

        ITestCaseExecutionService testCaseExecService = appContext
                .getBean(ITestCaseExecutionService.class);

        ITestCaseExecutionInQueueService testCaseExecutionInQueueService = appContext
                .getBean(ITestCaseExecutionInQueueService.class);

        int startPosition = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayStart"), "0"));
        int length = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayLength"), "100000"));

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        int columnToSortParameter = Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortCol_0"), "0"));
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "test,testCase,application,status,description,bugId,function");
        String columnToSort[] = sColumns.split(",");
        String columnName = columnToSort[columnToSortParameter];
        String sort = ParameterParserUtil.parseStringParam(request.getParameter("sSortDir_0"), "asc");
        List<String> statusList = getStatusList(request);
        /**
         * Get list of execution by tag, env, country, browser
         */
        testCaseExecution = testCaseExecService.getTestCaseExecution(startPosition, length, columnName, sort, searchParameter, "", Tag, statusList);
        List<TestCaseWithExecution> testCaseWithExecutions = testCaseExecution.getDataList();

        /**
         * Get list of Execution in Queue by Tag
         */
        testCaseExecutionInQueue = testCaseExecutionInQueueService.findTestCaseExecutionInQueuebyTag(startPosition, length, columnName, sort, searchParameter, "", Tag);
        List<TestCaseWithExecution> testCaseWithExecutionsInQueue = testCaseExecutionInQueue.getDataList();

        /**
         * Feed hash map with execution from the two list (to get only one by
         * test,testcase,country,env,browser)
         */
        LinkedHashMap<String, TestCaseWithExecution> testCaseWithExecutionsList = new LinkedHashMap();
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

        LinkedHashMap<String, JSONObject> ceb = new LinkedHashMap<String, JSONObject>();
        for (TestCaseWithExecution testCaseWithExecution : testCaseWithExecutions) {
            JSONObject cebObject = new JSONObject();
            cebObject.put("country", testCaseWithExecution.getCountry());
            cebObject.put("environment", testCaseWithExecution.getEnvironment());
            cebObject.put("browser", testCaseWithExecution.getBrowser());
            ceb.put(testCaseWithExecution.getBrowser() + "_" + testCaseWithExecution.getCountry() + "_" + testCaseWithExecution.getEnvironment(), cebObject);
        }

        jsonResponse.put("Columns", ceb.values());
        answer.setItem(jsonResponse);
        answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        return answer;
    }

    private AnswerItem findExecutionList(ApplicationContext appContext, HttpServletRequest request, String Tag)
            throws CerberusException, ParseException, JSONException {
        AnswerItem answer = new AnswerItem(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        AnswerList testCaseExecution = new AnswerList();
        AnswerList testCaseExecutionInQueue = new AnswerList();

        ITestCaseExecutionService testCaseExecService = appContext
                .getBean(ITestCaseExecutionService.class);

        ITestCaseExecutionInQueueService testCaseExecutionInQueueService = appContext
                .getBean(ITestCaseExecutionInQueueService.class);

        JSONArray executionList = new JSONArray();

        int startPosition = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayStart"), "0"));
        int length = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayLength"), "100000"));
        /*int sEcho  = Integer.valueOf(request.getParameter("sEcho"));*/

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        int columnToSortParameter = Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortCol_0"), "0"));
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "test,testCase,application,status,description,bugId,function");
        String columnToSort[] = sColumns.split(",");
        String columnName = columnToSort[columnToSortParameter];
        String sort = ParameterParserUtil.parseStringParam(request.getParameter("sSortDir_0"), "asc");
        List<String> statusList = getStatusList(request);

        /**
         * Get list of execution by tag, env, country, browser
         */
        testCaseExecution = testCaseExecService.getTestCaseExecution(startPosition, length, columnName, sort, searchParameter, "", Tag, statusList);
        List<TestCaseWithExecution> testCaseWithExecutions = testCaseExecution.getDataList();

        /**
         * Get list of Execution in Queue by Tag
         */
        testCaseExecutionInQueue = testCaseExecutionInQueueService.findTestCaseExecutionInQueuebyTag(startPosition, length, columnName, sort, searchParameter, "", Tag);
        List<TestCaseWithExecution> testCaseWithExecutionsInQueue = testCaseExecutionInQueue.getDataList();

        /**
         * Feed hash map with execution from the two list (to get only one by
         * test,testcase,country,env,browser)
         */
        LinkedHashMap<String, TestCaseWithExecution> testCaseWithExecutionsList = new LinkedHashMap();
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

        LinkedHashMap<String, JSONObject> ttc = new LinkedHashMap<String, JSONObject>();
        for (TestCaseWithExecution testCaseWithExecution : testCaseWithExecutions) {
            try {
                executionList.put(testCaseExecutionToJSONObject(testCaseWithExecution));
                JSONObject ttcObject = new JSONObject();
                ttcObject.put("test", testCaseWithExecution.getTest());
                ttcObject.put("testCase", testCaseWithExecution.getTestCase());
                ttcObject.put("function", testCaseWithExecution.getFunction());
                ttcObject.put("shortDesc", testCaseWithExecution.getShortDescription());
                ttcObject.put("status", testCaseWithExecution.getStatus());
                ttcObject.put("application", testCaseWithExecution.getApplication());
                ttcObject.put("bugId", testCaseWithExecution.getBugID());
                ttcObject.put("comment", testCaseWithExecution.getComment());
                ttc.put(testCaseWithExecution.getTest() + "_" + testCaseWithExecution.getTestCase(), ttcObject);
            } catch (JSONException ex) {
                Logger.getLogger(CampaignExecutionReport.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        JSONArray lines = new JSONArray(ttc.values());

        for (int indexValues = 0; indexValues < executionList.length(); indexValues++) {
            JSONObject testExec = executionList.getJSONObject(indexValues);

            for (int indexLines = 0; indexLines < lines.length(); indexLines++) {

                if (testExec.getString("Test").equals(lines.getJSONObject(indexLines).getString("test"))
                        && testExec.getString("TestCase").equals(lines.getJSONObject(indexLines).getString("testCase"))
                        && testExec.getString("Application").equals(lines.getJSONObject(indexLines).getString("application"))) {
                    StringBuilder key = new StringBuilder();
                    key.append(testExec.getString("Environment")).append(" ")
                            .append(testExec.getString("Country")).append(" ")
                            .append(testExec.getString("Browser"));
                    if (!lines.getJSONObject(indexLines).has("execTab")) {
                        lines.getJSONObject(indexLines).put("execTab", new JSONObject());
                    }

                    lines.getJSONObject(indexLines).getJSONObject("execTab").put(key.toString(), testExec);
                }
            }
        }

        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("testList", lines);
        jsonResponse.put("iTotalRecords", lines.length());
        jsonResponse.put("iTotalDisplayRecords", lines.length());

        answer.setItem(jsonResponse);
        answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        return answer;
    }

    private List<String> getStatusList(HttpServletRequest request) {
        List<String> statusList = new ArrayList<String>();

        if (ParameterParserUtil.parseStringParam(request.getParameter("OK"), "off").equals("on")) {
            statusList.add("OK");
        }
        if (ParameterParserUtil.parseStringParam(request.getParameter("KO"), "off").equals("on")) {
            statusList.add("KO");
        }
        if (ParameterParserUtil.parseStringParam(request.getParameter("NA"), "off").equals("on")) {
            statusList.add("NA");
        }
        if (ParameterParserUtil.parseStringParam(request.getParameter("NE"), "off").equals("on")) {
            statusList.add("NE");
        }
        if (ParameterParserUtil.parseStringParam(request.getParameter("PE"), "off").equals("on")) {
            statusList.add("PE");
        }
        if (ParameterParserUtil.parseStringParam(request.getParameter("FA"), "off").equals("on")) {
            statusList.add("FA");
        }
        if (ParameterParserUtil.parseStringParam(request.getParameter("CA"), "off").equals("on")) {
            statusList.add("CA");
        }

        return statusList;
    }

    private JSONObject testCaseExecutionToJSONObject(
            TestCaseWithExecution testCaseWithExecution) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("ID", String.valueOf(testCaseWithExecution.getStatusExecutionID()));
        result.put("Test", JavaScriptUtils.javaScriptEscape(testCaseWithExecution.getTest()));
        result.put("TestCase", JavaScriptUtils.javaScriptEscape(testCaseWithExecution.getTestCase()));
        result.put("Environment", JavaScriptUtils.javaScriptEscape(testCaseWithExecution.getEnvironment()));
        result.put("Start", testCaseWithExecution.getStart());
        result.put("End", testCaseWithExecution.getEnd());
        result.put("Country", JavaScriptUtils.javaScriptEscape(testCaseWithExecution.getCountry()));
        result.put("Browser", JavaScriptUtils.javaScriptEscape(testCaseWithExecution.getBrowser()));
        result.put("ControlStatus", JavaScriptUtils.javaScriptEscape(testCaseWithExecution.getControlStatus()));
        result.put("ControlMessage", JavaScriptUtils.javaScriptEscape(testCaseWithExecution.getControlMessage()));
        result.put("Status", JavaScriptUtils.javaScriptEscape(testCaseWithExecution.getStatus()));

        String bugId;
        if (testCaseWithExecution.getApplicationObject() != null && testCaseWithExecution.getApplicationObject().getBugTrackerUrl() != null
                && !"".equals(testCaseWithExecution.getApplicationObject().getBugTrackerUrl()) && testCaseWithExecution.getBugID() != null) {
            bugId = testCaseWithExecution.getApplicationObject().getBugTrackerUrl().replaceAll("%BUGID%", testCaseWithExecution.getBugID());
            bugId = new StringBuffer("<a href='")
                    .append(bugId)
                    .append("' target='reportBugID'>")
                    .append(testCaseWithExecution.getBugID())
                    .append("</a>")
                    .toString();
        } else {
            bugId = testCaseWithExecution.getBugID();
        }
        result.put("BugID", bugId);

        result.put("Comment", JavaScriptUtils.javaScriptEscape(testCaseWithExecution.getComment()));
        result.put("Function", JavaScriptUtils.javaScriptEscape(testCaseWithExecution.getFunction()));
        result.put("Application", JavaScriptUtils.javaScriptEscape(testCaseWithExecution.getApplication()));
        result.put("ShortDescription", testCaseWithExecution.getShortDescription());

        return result;
    }
}
