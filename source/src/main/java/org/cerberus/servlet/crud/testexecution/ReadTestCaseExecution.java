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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.dto.TestCaseWithExecution;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ITestCaseExecutionInQueueService;
import org.cerberus.crud.service.ITestCaseExecutionService;
import org.cerberus.crud.service.impl.InvariantService;
import org.cerberus.servlet.crud.testcampaign.CampaignExecutionReport;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
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

        testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);

        try {
            JSONObject jsonResponse = new JSONObject();
            AnswerItem answer = new AnswerItem(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
            int sEcho = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("sEcho"), "0"));
            String Tag = ParameterParserUtil.parseStringParam(request.getParameter("Tag"), "");
            String test = ParameterParserUtil.parseStringParam(request.getParameter("test"), "");
            String testCase = ParameterParserUtil.parseStringParam(request.getParameter("testCase"), "");

            if (sEcho == 0 && !Tag.equals("")) {
                answer = findExecutionColumns(appContext, request, Tag);
                jsonResponse = (JSONObject) answer.getItem();
            } else if (sEcho != 0 && !Tag.equals("")) {
                answer = findExecutionList(appContext, request, Tag);
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
            }

            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());
            response.setContentType("application/json");
            response.getWriter().print(jsonResponse.toString());
        } catch (JSONException ex) {
            org.apache.log4j.Logger.getLogger(ReadTestCaseExecution.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.setContentType("application/json");
            MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
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

    private AnswerItem findExecutionColumns(ApplicationContext appContext, HttpServletRequest request, String Tag) throws CerberusException, ParseException, JSONException {
        AnswerItem answer = new AnswerItem(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        JSONObject jsonResponse = new JSONObject();

        AnswerList testCaseExecution = new AnswerList();
        AnswerList testCaseExecutionInQueue = new AnswerList();

        ITestCaseExecutionService testCaseExecService = appContext
                .getBean(ITestCaseExecutionService.class);

        ITestCaseExecutionInQueueService testCaseExecutionInQueueService = appContext
                .getBean(ITestCaseExecutionInQueueService.class);

        /**
         * Get list of execution by tag, env, country, browser
         */
        testCaseExecution = testCaseExecService.readDistinctEnvCoutnryBrowserByTag(Tag);
        List<TestCaseWithExecution> testCaseWithExecutions = testCaseExecution.getDataList();

        /**
         * Get list of Execution in Queue by Tag
         */
        testCaseExecutionInQueue = testCaseExecutionInQueueService.readDistinctEnvCoutnryBrowserByTag(Tag);
        List<TestCaseWithExecution> testCaseWithExecutionsInQueue = testCaseExecutionInQueue.getDataList();

        /**
         * Feed hash map with execution from the two list (to get only one by
         * test,testcase,country,env,browser)
         */
        LinkedHashMap<String, TestCaseWithExecution> testCaseWithExecutionsList = new LinkedHashMap();

        for (TestCaseWithExecution testCaseWithExecution : testCaseWithExecutions) {
            String key = testCaseWithExecution.getBrowser() + "_"
                    + testCaseWithExecution.getCountry() + "_"
                    + testCaseWithExecution.getEnvironment() + " "
                    + testCaseWithExecution.getControlStatus();
            testCaseWithExecutionsList.put(key, testCaseWithExecution);
        }
        for (TestCaseWithExecution testCaseWithExecutionInQueue : testCaseWithExecutionsInQueue) {
            String key = testCaseWithExecutionInQueue.getBrowser() + "_"
                    + testCaseWithExecutionInQueue.getCountry() + "_"
                    + testCaseWithExecutionInQueue.getEnvironment() + "_"
                    + testCaseWithExecutionInQueue.getControlStatus();
            testCaseWithExecutionsList.put(key, testCaseWithExecutionInQueue);
        }

        testCaseWithExecutions = new ArrayList<TestCaseWithExecution>(testCaseWithExecutionsList.values());

        JSONObject statusFilter = getStatusList(request);
        JSONObject countryFilter = getCountryList(request, appContext);
        LinkedHashMap<String, JSONObject> columnMap = new LinkedHashMap<String, JSONObject>();

        for (TestCaseWithExecution testCaseWithExecution : testCaseWithExecutions) {
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
        AnswerList testCaseExecution = new AnswerList();
        AnswerList testCaseExecutionInQueue = new AnswerList();

        ITestCaseExecutionService testCaseExecService = appContext
                .getBean(ITestCaseExecutionService.class);

        ITestCaseExecutionInQueueService testCaseExecutionInQueueService = appContext
                .getBean(ITestCaseExecutionInQueueService.class);

        int startPosition = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayStart"), "0"));
        int length = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayLength"), "0"));

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        int columnToSortParameter = Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortCol_0"), "0"));
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "test,testCase,application,status,description,bugId,function");
        String columnToSort[] = sColumns.split(",");
        String columnName = columnToSort[columnToSortParameter];
        String sort = ParameterParserUtil.parseStringParam(request.getParameter("sSortDir_0"), "asc");

        /**
         * Get list of execution by tag, env, country, browser
         */
        testCaseExecution = testCaseExecService.readByTagByCriteria(Tag, startPosition, length, columnName, sort, searchParameter, "");
        List<TestCaseWithExecution> testCaseWithExecutions = testCaseExecution.getDataList();

        /**
         * Get list of Execution in Queue by Tag
         */
        testCaseExecutionInQueue = testCaseExecutionInQueueService.readByTagByCriteria(Tag, startPosition, length, columnName, sort, searchParameter, "");
        List<TestCaseWithExecution> testCaseWithExecutionsInQueue = testCaseExecutionInQueue.getDataList();

        /**
         * Feed hash map with execution from the two list (to get only one by
         * test,testcase,country,env,browser)
         */
        testCaseWithExecutions = hashExecution(testCaseWithExecutions, testCaseWithExecutionsInQueue);

        JSONArray executionList = new JSONArray();
        JSONObject statusFilter = getStatusList(request);
        JSONObject countryFilter = getCountryList(request, appContext);
        LinkedHashMap<String, JSONObject> ttc = new LinkedHashMap<String, JSONObject>();

        for (TestCaseWithExecution testCaseWithExecution : testCaseWithExecutions) {
            try {
                String controlStatus = testCaseWithExecution.getControlStatus();
                if (statusFilter.get(controlStatus).equals("on") && countryFilter.get(testCaseWithExecution.getCountry()).equals("on")) {
                    JSONObject execution = testCaseExecutionToJSONObject(testCaseWithExecution);
                    String execKey = testCaseWithExecution.getEnvironment() + " " + testCaseWithExecution.getCountry() + " " + testCaseWithExecution.getBrowser();
                    String testCaseKey = testCaseWithExecution.getTest() + "_" + testCaseWithExecution.getTestCase();
                    JSONObject execTab = new JSONObject();

                    executionList.put(testCaseExecutionToJSONObject(testCaseWithExecution));
                    JSONObject ttcObject = new JSONObject();

                    if (ttc.containsKey(testCaseKey)) {
                        ttcObject = ttc.get(testCaseKey);
                        execTab = ttcObject.getJSONObject("execTab");
                        execTab.put(execKey, execution);
                        ttcObject.put("execTab", execTab);
                    } else {
                        ttcObject.put("test", testCaseWithExecution.getTest());
                        ttcObject.put("testCase", testCaseWithExecution.getTestCase());
                        ttcObject.put("function", testCaseWithExecution.getFunction());
                        ttcObject.put("shortDesc", testCaseWithExecution.getShortDescription());
                        ttcObject.put("status", testCaseWithExecution.getStatus());
                        ttcObject.put("application", testCaseWithExecution.getApplication());
                        ttcObject.put("bugId", testCaseWithExecution.getBugID());
                        ttcObject.put("comment", testCaseWithExecution.getComment());
                        execTab.put(execKey, execution);
                        ttcObject.put("execTab", execTab);
                    }
                    ttc.put(testCaseWithExecution.getTest() + "_" + testCaseWithExecution.getTestCase(), ttcObject);
                }
            } catch (JSONException ex) {
                Logger.getLogger(CampaignExecutionReport.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        JSONObject jsonResponse = new JSONObject();
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

        IInvariantService invariantService = appContext.getBean(InvariantService.class);

        try {
            for (Invariant country : invariantService.findListOfInvariantById("COUNTRY")) {
                countryList.put(country.getValue(), ParameterParserUtil.parseStringParam(request.getParameter(country.getValue()), "off"));
            }
        } catch (JSONException ex) {
            Logger.getLogger(ReadTestCaseExecution.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CerberusException ex) {
            Logger.getLogger(ReadTestCaseExecution.class.getName()).log(Level.SEVERE, null, ex);
        }

        return countryList;
    }

    private List<TestCaseWithExecution> hashExecution(List<TestCaseWithExecution> testCaseWithExecutions, List<TestCaseWithExecution> testCaseWithExecutionsInQueue) throws ParseException {
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
        List<TestCaseWithExecution> result = new ArrayList<TestCaseWithExecution>(testCaseWithExecutionsList.values());

        return result;
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
