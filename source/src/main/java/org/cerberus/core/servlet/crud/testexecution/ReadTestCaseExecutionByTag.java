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
package org.cerberus.core.servlet.crud.testexecution;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.entity.Label;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionQueueDep;
import org.cerberus.core.crud.entity.TestCaseLabel;
import org.cerberus.core.crud.factory.IFactoryTestCase;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.ILabelService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.crud.service.ITestCaseLabelService;
import org.cerberus.core.crud.service.impl.InvariantService;
import org.cerberus.core.crud.service.impl.LabelService;
import org.cerberus.core.dto.SummaryStatisticsBugTrackerDTO;
import org.cerberus.core.dto.SummaryStatisticsDTO;
import org.cerberus.core.dto.TreeNode;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.JavaScriptUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.util.Arrays.asList;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.service.IParameterService;

/**
 * @author bcivel
 */
@WebServlet(name = "ReadTestCaseExecutionByTag", urlPatterns = {"/ReadTestCaseExecutionByTag"})
public class ReadTestCaseExecutionByTag extends HttpServlet {

    private ITestCaseExecutionService testCaseExecutionService;
    private ITagService tagService;
    private ITestCaseExecutionQueueService testCaseExecutionInQueueService;
    private ITestCaseLabelService testCaseLabelService;
    private ILabelService labelService;
    private IFactoryTestCase factoryTestCase;
    private IParameterService parameterService;

    private static final Logger LOG = LogManager.getLogger("ReadTestCaseExecutionByTag");

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
        String echo = request.getParameter("sEcho");

        AnswerItem<JSONObject> answer = new AnswerItem<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));

        testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
        tagService = appContext.getBean(ITagService.class);
        factoryTestCase = appContext.getBean(IFactoryTestCase.class);
        testCaseExecutionInQueueService = appContext.getBean(ITestCaseExecutionQueueService.class);
        parameterService = appContext.getBean(IParameterService.class);

        try {
            // Data/Filter Parameters.
            String Tag = ParameterParserUtil.parseStringParam(request.getParameter("Tag"), "");
            List<String> outputReport = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("outputReport"), new ArrayList<>(), "UTF-8");
            boolean fullList = ParameterParserUtil.parseBooleanParam(request.getParameter("fullList"), false);
            boolean fullListDefined = request.getQueryString().contains("fullList=");

            JSONObject jsonResponse = new JSONObject();
            JSONObject statusFilter = getStatusList(request);
            JSONObject countryFilter = getCountryList(request, appContext);

            //Get Data from database
            List<TestCaseExecution> testCaseExecutions = testCaseExecutionService.readLastExecutionAndExecutionInQueueByTag(Tag);

            List<TestCaseLabel> testCaseLabelScopeList = null;
            if (outputReport.isEmpty() || outputReport.contains("labelStat") || outputReport.contains("table")) {
                String testCaseKey = "";
                HashMap<String, TestCase> ttc = new HashMap<>();
                List<TestCase> tcList = new ArrayList<>();
                for (TestCaseExecution testCaseExecution : testCaseExecutions) {
                    testCaseKey = testCaseExecution.getTest() + "__" + testCaseExecution.getTestCase();
                    if (ttc.get(testCaseKey) == null) {
                        ttc.put(testCaseKey, factoryTestCase.create(testCaseExecution.getTest(), testCaseExecution.getTestCase()));
                        tcList.add(factoryTestCase.create(testCaseExecution.getTest(), testCaseExecution.getTestCase()));
                    }

                }

                testCaseLabelService = appContext.getBean(ITestCaseLabelService.class);
                AnswerList<TestCaseLabel> testCaseLabelList = testCaseLabelService.readByTestTestCase(null, null, tcList);
                testCaseLabelScopeList = testCaseLabelList.getDataList();
            }

            // Table that contain the list of testcases and corresponding executions
            if (outputReport.isEmpty() || outputReport.contains("table")) {
                jsonResponse.put("table", generateTestCaseExecutionTable(appContext, testCaseExecutions, statusFilter, countryFilter, testCaseLabelScopeList, fullList, fullListDefined));
            }
            // Table that contain the list of testcases and corresponding executions
            if (outputReport.isEmpty() || outputReport.contains("table")) {
                jsonResponse.put("manualExecutionList", generateManualExecutionTable(appContext, testCaseExecutions, statusFilter, countryFilter));
            }
            // Executions per Function (or Test).
            if (outputReport.isEmpty() || outputReport.contains("testFolderChart")) {
                jsonResponse.put("testFolderChart", generateTestFolderChart(testCaseExecutions, Tag, statusFilter, countryFilter));
            }
            // Global executions stats per Status
            if (outputReport.isEmpty() || outputReport.contains("statsChart")) {
                jsonResponse.put("statsChart", generateStats(request, testCaseExecutions, statusFilter, countryFilter, true));
            }
            // BugTracker Recap
            if (outputReport.isEmpty() || outputReport.contains("bugTrackerStat")) {
                jsonResponse.put("bugTrackerStat", generateBugStats(request, testCaseExecutions, statusFilter, countryFilter));
            }
            // Labels Stats
            if (outputReport.isEmpty() || outputReport.contains("labelStat")) {
                jsonResponse.put("labelStat", generateLabelStats(appContext, request, testCaseExecutions, statusFilter, countryFilter, testCaseLabelScopeList));
            }
            if (!outputReport.isEmpty()) {
                //currently used to optimize the homePage
                if (outputReport.contains("totalStatsCharts") && !outputReport.contains("statsChart")) {
                    jsonResponse.put("statsChart", generateStats(request, testCaseExecutions, statusFilter, countryFilter, false));
                }
                //currently used to optimize the homePage
                if (outputReport.contains("resendTag")) {
                    jsonResponse.put("tag", Tag);
                }
            }
            Tag mytag = tagService.convert(tagService.readByKey(Tag));
            if (mytag != null) {
                JSONObject tagJSON = convertTagToJSONObject(mytag);
                jsonResponse.put("tagObject", tagJSON);
                jsonResponse.put("tagDuration", (mytag.getDateEndQueue().getTime() - mytag.getDateStartExe().getTime()) / 60000);
            }

            answer.setItem(jsonResponse);
            answer.setResultMessage(answer.getResultMessage().resolveDescription("ITEM", "Tag Statistics").resolveDescription("OPERATION", "Read"));

            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());
            jsonResponse.put("sEcho", echo);

            response.getWriter().print(jsonResponse.toString());

        } catch (ParseException ex) {
            LOG.error("Error on main call : " + ex, ex);
        } catch (CerberusException ex) {
            LOG.error("Error on main call : " + ex, ex);
        } catch (JSONException ex) {
            LOG.error("Error on main call : " + ex, ex);
        } catch (Exception ex) {
            LOG.error("Error on main call : " + ex, ex);
        }
    }

    private JSONObject testCaseExecutionToJSONObject(TestCaseExecution testCaseExecution) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("ID", String.valueOf(testCaseExecution.getId()));
        result.put("QueueID", String.valueOf(testCaseExecution.getQueueID()));
        result.put("Test", JavaScriptUtils.javaScriptEscape(testCaseExecution.getTest()));
        result.put("TestCase", JavaScriptUtils.javaScriptEscape(testCaseExecution.getTestCase()));
        result.put("Environment", JavaScriptUtils.javaScriptEscape(testCaseExecution.getEnvironment()));
        result.put("Start", testCaseExecution.getStart());
        result.put("End", testCaseExecution.getEnd());
        result.put("DurationMs", testCaseExecution.getDurationMs());
        result.put("Country", JavaScriptUtils.javaScriptEscape(testCaseExecution.getCountry()));
        result.put("RobotDecli", JavaScriptUtils.javaScriptEscape(testCaseExecution.getRobotDecli()));
        result.put("ManualExecution", JavaScriptUtils.javaScriptEscape(testCaseExecution.getManualExecution()));
        if (testCaseExecution.getExecutor() != null) {
            result.put("Executor", JavaScriptUtils.javaScriptEscape(testCaseExecution.getExecutor()));
        }
        result.put("ControlStatus", JavaScriptUtils.javaScriptEscape(testCaseExecution.getControlStatus()));
        result.put("ControlMessage", JavaScriptUtils.javaScriptEscape(testCaseExecution.getControlMessage()));
        result.put("Status", JavaScriptUtils.javaScriptEscape(testCaseExecution.getStatus()));
        result.put("NbExecutions", String.valueOf(testCaseExecution.getNbExecutions()));
        result.put("previousExeId", testCaseExecution.getPreviousExeId());
        result.put("firstExeStart", testCaseExecution.getFirstExeStart());
        result.put("lastExeStart", testCaseExecution.getLastExeStart());
        result.put("lastExeEnd", testCaseExecution.getLastExeEnd());
        result.put("isMuted", testCaseExecution.isTestCaseIsMuted());
        result.put("isFlaky", testCaseExecution.isFlaky());
        result.put("isFalseNegative", testCaseExecution.isFalseNegative());
        result.put("isFalseNegativeRootcause", testCaseExecution.isFalseNegative());
        result.put("priority", testCaseExecution.getTestCasePriority());
        if (testCaseExecution.getPreviousExeStatus() != null) {
            result.put("previousExeControlStatus", JavaScriptUtils.javaScriptEscape(testCaseExecution.getPreviousExeStatus()));
        }
        if (testCaseExecution.getQueueState() != null) {
            result.put("QueueState", JavaScriptUtils.javaScriptEscape(testCaseExecution.getQueueState()));
        }

        List<JSONObject> testCaseDep = new ArrayList<>();

        if (testCaseExecution.getTestCaseExecutionQueueDepList() != null) {
            for (TestCaseExecutionQueueDep tce : testCaseExecution.getTestCaseExecutionQueueDepList()) {
                JSONObject obj = new JSONObject();
                obj.put("test", tce.getDepTest());
                obj.put("testcase", tce.getDepTestCase());
                obj.put("date", tce.getDepDate());
                obj.put("delayM", tce.getDepTCDelay());
                obj.put("event", tce.getDepEvent());
                obj.put("status", tce.getStatus());
                obj.put("type", tce.getType());
                testCaseDep.add(obj);
            }
        }
        result.put("TestCaseDep", testCaseDep);

        return result;
    }

    private JSONObject getStatusList(HttpServletRequest request) {
        JSONObject statusList = new JSONObject();

        try {
            statusList.put("OK", ParameterParserUtil.parseStringParam(request.getParameter("OK"), "off"));
            statusList.put("KO", ParameterParserUtil.parseStringParam(request.getParameter("KO"), "off"));
            statusList.put("NA", ParameterParserUtil.parseStringParam(request.getParameter("NA"), "off"));
            statusList.put("NE", ParameterParserUtil.parseStringParam(request.getParameter("NE"), "off"));
            statusList.put("WE", ParameterParserUtil.parseStringParam(request.getParameter("WE"), "off"));
            statusList.put("PE", ParameterParserUtil.parseStringParam(request.getParameter("PE"), "off"));
            statusList.put("FA", ParameterParserUtil.parseStringParam(request.getParameter("FA"), "off"));
            statusList.put("CA", ParameterParserUtil.parseStringParam(request.getParameter("CA"), "off"));
            statusList.put("QU", ParameterParserUtil.parseStringParam(request.getParameter("QU"), "off"));
            statusList.put("PA", ParameterParserUtil.parseStringParam(request.getParameter("PA"), "off"));
            statusList.put("QE", ParameterParserUtil.parseStringParam(request.getParameter("QE"), "off"));
        } catch (JSONException ex) {
            LOG.error("Error on getStatusList : " + ex, ex);
        }

        return statusList;
    }

    private JSONObject getCountryList(HttpServletRequest request, ApplicationContext appContext) {
        JSONObject countryList = new JSONObject();
        try {
            IInvariantService invariantService = appContext.getBean(InvariantService.class);
            for (Invariant country : invariantService.readByIdName("COUNTRY")) {
                countryList.put(country.getValue(), ParameterParserUtil.parseStringParam(request.getParameter(country.getValue()), "off"));
            }
        } catch (JSONException | CerberusException ex) {
            LOG.error("Error on getCountryList : " + ex, ex);
        }

        return countryList;
    }

    private JSONObject generateTestCaseExecutionTable(ApplicationContext appContext, List<TestCaseExecution> testCaseExecutions, JSONObject statusFilter, JSONObject countryFilter, List<TestCaseLabel> testCaseLabelList, boolean fullList, boolean fullListDefined) {
        JSONObject testCaseExecutionTable = new JSONObject();
        LinkedHashMap<String, JSONObject> ttc = new LinkedHashMap<>();
        LinkedHashMap<String, JSONObject> columnMap = new LinkedHashMap<>();

        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            try {
                String controlStatus = testCaseExecution.getControlStatus();
                String previousControlStatus = testCaseExecution.getPreviousExeStatus();

                // We check is Country and status is inside the fitered values.
                if (statusFilter.get(controlStatus).equals("on") && countryFilter.get(testCaseExecution.getCountry()).equals("on")) {

                    JSONObject executionJSON = testCaseExecutionToJSONObject(testCaseExecution);
                    String execKey = testCaseExecution.getEnvironment() + " " + testCaseExecution.getCountry() + " " + testCaseExecution.getRobotDecli();
                    String testCaseKey = testCaseExecution.getTest() + "_" + testCaseExecution.getTestCase();
                    JSONObject execTab = new JSONObject();
                    JSONObject ttcObject = new JSONObject();

                    if (ttc.containsKey(testCaseKey)) {
                        // We add an execution entry into the testcase line.
                        ttcObject = ttc.get(testCaseKey);
                        execTab = ttcObject.getJSONObject("execTab");
                        execTab.put(execKey, executionJSON);
                        ttcObject.put("execTab", execTab);
                        // Nb Total Executions
                        Integer nbExeTot = (Integer) ttcObject.get("NbExe");
                        nbExeTot += testCaseExecution.getNbExecutions();
                        ttcObject.put("NbExe", nbExeTot);
                        // Nb Total Executions
                        Integer nbRetryTot = (Integer) ttcObject.get("NbRetry");
                        nbRetryTot += testCaseExecution.getNbExecutions() - 1;
                        ttcObject.put("NbRetry", nbRetryTot);
                        // Max Duration
                        long durationMax = ttcObject.getLong("DurationMsMax");
                        if (testCaseExecution.getDurationMs() > durationMax) {
                            durationMax = testCaseExecution.getDurationMs();
                            ttcObject.put("DurationMsMax", durationMax);
                        }
                        ttcObject.put("DurationMsMax", testCaseExecution.getDurationMs());
                        // Nb Total Usefull Executions
                        Integer nbExeUsefullTot = (Integer) ttcObject.get("NbExeUsefull");
                        nbExeUsefullTot++;
                        ttcObject.put("NbExeUsefull", nbExeUsefullTot);
                        // Nb Total Usefull Executions in QU or OK status
                        Integer nbExeTmp;
                        if (isToHide(controlStatus, previousControlStatus)) {
                            nbExeTmp = (Integer) ttcObject.get("NbExeUsefullToHide");
                            ttcObject.put("NbExeUsefullToHide", ++nbExeTmp);
                        }
                        if (isNotBug(controlStatus)) {
                            nbExeTmp = (Integer) ttcObject.get("NbExeUsefullOK");
                            ttcObject.put("NbExeUsefullOK", ++nbExeTmp);
                        }
                        if (isBug(controlStatus)) {
                            nbExeTmp = (Integer) ttcObject.get("NbExeUsefullHasBug");
                            ttcObject.put("NbExeUsefullHasBug", ++nbExeTmp);
                        }
                        if (isPending(controlStatus)) {
                            nbExeTmp = (Integer) ttcObject.get("NbExeUsefullIsPending");
                            ttcObject.put("NbExeUsefullIsPending", ++nbExeTmp);
                        }

                        // first Exe Start
                        ttcObject.put("firstExeStart", testCaseExecution.getFirstExeStart() < ttcObject.getLong("firstExeStart") && testCaseExecution.getFirstExeStart() > 0 ? testCaseExecution.getFirstExeStart() : ttcObject.getLong("firstExeStart"));
                        // last Exe Start
                        ttcObject.put("lastExeStart", testCaseExecution.getLastExeEnd() > ttcObject.getLong("lastExeEnd") ? testCaseExecution.getLastExeStart() : ttcObject.getLong("lastExeStart"));
                        // last Exe End
                        ttcObject.put("lastExeEnd", testCaseExecution.getLastExeEnd() > ttcObject.getLong("lastExeEnd") ? testCaseExecution.getLastExeEnd() : ttcObject.getLong("lastExeEnd"));

                    } else {
                        // We add a new testcase entry (with The current execution).
                        ttcObject.put("test", testCaseExecution.getTest());
                        ttcObject.put("testCase", testCaseExecution.getTestCase());
                        ttcObject.put("shortDesc", testCaseExecution.getDescription());
                        ttcObject.put("status", testCaseExecution.getStatus());
                        ttcObject.put("application", testCaseExecution.getApplication());
                        ttcObject.put("isMuted", testCaseExecution.isTestCaseIsMuted());
                        if (testCaseExecution.getApplicationObj() != null && testCaseExecution.getApplicationObj().getBugTrackerUrl() != null
                                && !"".equals(testCaseExecution.getApplicationObj().getBugTrackerUrl()) && testCaseExecution.getTestCaseObj().getBugs() != null) {
                            ttcObject.put("AppBugURL", testCaseExecution.getApplicationObj().getBugTrackerUrl());
                        }
                        boolean testExist = ((testCaseExecution.getTestCaseObj() != null) && (testCaseExecution.getTestCaseObj().getTest() != null));
                        if (testExist) {
                            ttcObject.put("priority", testCaseExecution.getTestCaseObj().getPriority());
                            ttcObject.put("comment", testCaseExecution.getTestCaseObj().getComment());
                            ttcObject.put("bugs", testCaseExecution.getTestCaseObj().getBugsActive());

                        } else {

                            ttcObject.put("function", "");
                            ttcObject.put("priority", 0);
                            ttcObject.put("comment", "");
                            ttcObject.put("bugs", new JSONArray());

                        }

                        // first Exe Start
                        ttcObject.put("firstExeStart", testCaseExecution.getFirstExeStart());
                        // last Exe Start
                        ttcObject.put("lastExeStart", testCaseExecution.getLastExeStart());
                        // last Exe End
                        ttcObject.put("lastExeEnd", testCaseExecution.getLastExeEnd());

                        // Flag that report if test case still exist.
                        ttcObject.put("testExist", testExist);

                        // Adding nb of execution on retry.
                        ttcObject.put("NbRetry", (testCaseExecution.getNbExecutions() - 1));

                        // Adding Duration Max.
                        ttcObject.put("DurationMsMax", testCaseExecution.getDurationMs());

                        // Adding nb of execution on retry.
                        ttcObject.put("NbExe", (testCaseExecution.getNbExecutions()));

                        // Nb Total Usefull Executions
                        ttcObject.put("NbExeUsefull", 1);

                        // Nb Total Usefull Executions in QU or OK status
                        if (isToHide(controlStatus, previousControlStatus)) {
                            ttcObject.put("NbExeUsefullToHide", 1);
                        } else {
                            ttcObject.put("NbExeUsefullToHide", 0);
                        }
                        // Nb Total Usefull Executions with no bug to report
                        if (isNotBug(controlStatus)) {
                            ttcObject.put("NbExeUsefullOK", 1);
                        } else {
                            ttcObject.put("NbExeUsefullOK", 0);
                        }
                        // Nb Total Usefull Executions with bug to report
                        if (isBug(controlStatus)) {
                            ttcObject.put("NbExeUsefullHasBug", 1);
                        } else {
                            ttcObject.put("NbExeUsefullHasBug", 0);
                        }
                        // Nb Total Usefull Executions still to run
                        if (isPending(controlStatus)) {
                            ttcObject.put("NbExeUsefullIsPending", 1);
                        } else {
                            ttcObject.put("NbExeUsefullIsPending", 0);
                        }

                        execTab.put(execKey, executionJSON);
                        ttcObject.put("execTab", execTab);

                        /**
                         * Iterate on the label retrieved and generate HashMap
                         * based on the key Test_TestCase
                         */
                        LinkedHashMap<String, JSONArray> testCaseWithLabel = new LinkedHashMap<>();
                        for (TestCaseLabel label : testCaseLabelList) {
                            if (Label.TYPE_STICKER.equals(label.getLabel().getType())) { // We only display STICKER Type Label in Reporting By Tag Page..
                                String key = label.getTest() + "_" + label.getTestcase();

                                JSONObject jo = new JSONObject().put("name", label.getLabel().getLabel()).put("color", label.getLabel().getColor()).put("description", label.getLabel().getDescription());
                                if (testCaseWithLabel.containsKey(key)) {
                                    testCaseWithLabel.get(key).put(jo);
                                } else {
                                    testCaseWithLabel.put(key, new JSONArray().put(jo));
                                }
                            }
                        }
                        ttcObject.put("labels", testCaseWithLabel.get(testCaseExecution.getTest() + "_" + testCaseExecution.getTestCase()));
                    }
                    ttc.put(testCaseExecution.getTest() + "_" + testCaseExecution.getTestCase(), ttcObject);

                    JSONObject column = new JSONObject();
                    column.put("country", testCaseExecution.getCountry());
                    column.put("environment", testCaseExecution.getEnvironment());
                    column.put("robotDecli", testCaseExecution.getRobotDecli());
                    columnMap.put(testCaseExecution.getRobotDecli() + "_" + testCaseExecution.getCountry() + "_" + testCaseExecution.getEnvironment(), column);

                }

                TreeMap<String, JSONObject> bugMap = new TreeMap<>();
                HashMap<String, Boolean> bugMapUniq = new HashMap<>();
                int nbTOCLEAN = 0;
                int nbPENDING = 0;
                int nbTOREPORT = 0;
                long durationMax = 0;
                // building Bug Status & agregated values.
                for (Map.Entry<String, JSONObject> entry : ttc.entrySet()) {
                    JSONObject val = entry.getValue();

                    // Max duration
                    if (val.getLong("DurationMsMax") > durationMax) {
                        durationMax = val.getLong("DurationMsMax");
                    }

                    // Bug
                    JSONArray bugA = val.getJSONArray("bugs");
                    int nbBug = bugA.length();
                    if (nbBug > 0) {
                        for (int i = 0; i < nbBug; i++) {
                            bugMapUniq.put(bugA.getJSONObject(i).getString("id"), true);
                            String key = bugA.getJSONObject(i).getString("id") + "#" + val.getString("test") + "#" + val.getString("testCase");

                            if (bugMap.containsKey(key)) {
                                JSONObject bugO = bugMap.get(key);
                            } else {
                                JSONObject bugO = new JSONObject();
                                bugO.put("test", val.getString("test"));
                                bugO.put("testCase", val.getString("testCase"));
                                bugO.put("bug", bugA.getJSONObject(i).getString("id"));
                                bugO.put("NbExeUsefullHasBug", val.getInt("NbExeUsefullHasBug"));
                                bugO.put("testExist", val.getBoolean("testExist"));
                                bugO.put("NbExeUsefull", val.getInt("NbExeUsefull"));
                                bugO.put("NbExeUsefullIsPending", val.getInt("NbExeUsefullIsPending"));
                                if (val.getInt("NbExeUsefullIsPending") > 0) {
                                    bugO.put("status", "STILL RUNNING...");
                                    nbPENDING++;
                                } else {
                                    if (val.getInt("NbExeUsefull") == val.getInt("NbExeUsefullOK")) {
                                        bugO.put("status", "TO CLEAN");
                                        nbTOCLEAN++;
                                    }

                                }
                                bugMap.put(key, bugO);
                            }
                        }
                    } else {
                        if (val.getInt("NbExeUsefullHasBug") > 0) {
                            String key = val.getString("test") + "#" + val.getString("testCase");

                            JSONObject bugO = new JSONObject();
                            bugO.put("test", val.getString("test"));
                            bugO.put("testCase", val.getString("testCase"));
                            bugO.put("bug", "");
                            bugO.put("NbExeUsefullHasBug", val.getInt("NbExeUsefullHasBug"));
                            bugO.put("testExist", val.getBoolean("testExist"));
                            bugO.put("NbExeUsefull", val.getInt("NbExeUsefull"));
                            bugO.put("NbExeUsefullIsPending", val.getInt("NbExeUsefullIsPending"));
                            bugO.put("status", "TO REPORT...");
                            nbTOREPORT++;
                            bugMap.put(key, bugO);
                        }
                    }
                }
                JSONObject bugRes = new JSONObject();
                bugRes.put("bugSummary", bugMap.values());
                bugRes.put("nbTOREPORT", nbTOREPORT);
                bugRes.put("nbPENDING", nbPENDING);
                bugRes.put("nbTOCLEAN", nbTOCLEAN);
                bugRes.put("nbBugs", bugMapUniq.size());
                testCaseExecutionTable.put("bugContent", bugRes);

                testCaseExecutionTable.put("durationMax", durationMax);

            } catch (JSONException ex) {
                LOG.error("Error on generateTestCaseExecutionTable : " + ex, ex);
            } catch (Exception ex) {
                LOG.error("Error on generateTestCaseExecutionTable : " + ex, ex);
            }
        }

        // Sort and Feed colomn list
        Map<String, JSONObject> treeMap = new TreeMap<>(columnMap);
        List<JSONObject> treeMapList = new ArrayList<>();
        for (Map.Entry<String, JSONObject> entry : treeMap.entrySet()) {
            JSONObject val = entry.getValue();
            if (!treeMapList.contains(val)) {
                treeMapList.add(val);
            }
        }
        Collections.sort(treeMapList, new SortColumns());
        for (JSONObject jSONObject : treeMapList) {
            testCaseExecutionTable.append("tableColumns", jSONObject);
        }

        try {

            // Now loading only necessary records to final structure (filtering testcase that have all usefull executions OK of QU).
            if (!fullListDefined) { // If nb of exe is low, we force them to be displayed
                if (ttc.size() < parameterService.getParameterIntegerByKey(Parameter.VALUE_cerberus_reportbytag_nblinestotriggerautohide_int, "", 50)) {
                    LOG.debug("Not defined and size lower than target. " + ttc.size() + " param : " + parameterService.getParameterIntegerByKey(Parameter.VALUE_cerberus_reportbytag_nblinestotriggerautohide_int, "", 50));
                    fullList = true;
                }
            }

            if (fullList) {
                testCaseExecutionTable.put("tableContent", ttc.values());
                testCaseExecutionTable.put("iTotalRecords", ttc.size());
                testCaseExecutionTable.put("iTotalDisplayRecords", ttc.size());
                testCaseExecutionTable.put("fullList", fullList);
            } else {
                LinkedHashMap<String, JSONObject> newttc = new LinkedHashMap<>();
                for (Map.Entry<String, JSONObject> entry : ttc.entrySet()) {
                    String key = entry.getKey();
                    JSONObject val = entry.getValue();
                    if ((val.getInt("NbExeUsefullToHide") != val.getInt("NbExeUsefull")) // One of the execution of the test case has a status <> QU and OK
                            || (val.getJSONArray("bugs").length() > 0) // At least 1 bug has been assigned to the testcase.
                            || ((val.getInt("NbExeUsefullHasBug") == 0) && (val.getInt("NbRetry") > 0)) // No bug to report but retry was needed --> Flaky test.
                            ) {
                        newttc.put(key, val);
                    }
                }
                testCaseExecutionTable.put("tableContent", newttc.values());
                testCaseExecutionTable.put("iTotalRecords", newttc.size());
                testCaseExecutionTable.put("iTotalDisplayRecords", newttc.size());
                testCaseExecutionTable.put("fullList", fullList);
            }

        } catch (JSONException ex) {
            LOG.error("Error on generateTestCaseExecutionTable : " + ex, ex);
        } catch (Exception ex) {
            LOG.error("Error on generateTestCaseExecutionTable : " + ex, ex);
        }

        return testCaseExecutionTable;
    }

    class SortColumns implements Comparator<JSONObject> {
        // Used for sorting in ascending order of
        // name value.

        @Override
        public int compare(JSONObject a, JSONObject b) {
            if (a != null && b != null) {
                try {
                    String aS = a.getString("environment") + "-" + a.getString("country") + "-" + a.getString("robotDecli");
                    String bS = b.getString("environment") + "-" + b.getString("country") + "-" + b.getString("robotDecli");

                    return aS.compareToIgnoreCase(bS);
                } catch (JSONException ex) {
                    LOG.error("JSON Error Exception", ex);
                    return 1;
                }
            } else {
                return 1;
            }
        }
    }

    // We hide is status is QU of OK and there were no previous execution.
    private boolean isToHide(String controlStatus, String previousControlStatus) {
        return (controlStatus.equals(TestCaseExecution.CONTROLSTATUS_QU) && (StringUtil.isEmptyOrNull(previousControlStatus))
                || controlStatus.equals(TestCaseExecution.CONTROLSTATUS_OK));
    }

    private boolean isPending(String controlStatus) {
        return (controlStatus.equals(TestCaseExecution.CONTROLSTATUS_QU) || controlStatus.equals(TestCaseExecution.CONTROLSTATUS_WE) || controlStatus.equals(TestCaseExecution.CONTROLSTATUS_PE));
    }

    private boolean isBug(String controlStatus) {
        return (controlStatus.equals(TestCaseExecution.CONTROLSTATUS_FA) || controlStatus.equals(TestCaseExecution.CONTROLSTATUS_KO));
    }

    private boolean isNotBug(String controlStatus) {
        return (controlStatus.equals(TestCaseExecution.CONTROLSTATUS_OK) || controlStatus.equals(TestCaseExecution.CONTROLSTATUS_QE));
    }

    private JSONObject generateManualExecutionTable(ApplicationContext appContext, List<TestCaseExecution> testCaseExecutions, JSONObject statusFilter, JSONObject countryFilter) {
        JSONObject manualExecutionTable = new JSONObject();
        HashMap<String, JSONObject> manualExecutions = new HashMap<>();
        int totalManualExecution = 0;
        int totalManualWEExecution = 0;

        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            try {
                String controlStatus = testCaseExecution.getControlStatus();
                boolean isManual = StringUtil.parseBoolean(testCaseExecution.getManualExecution());

                // We check is Country and status is inside the fitered values.
                if ((countryFilter.has(testCaseExecution.getCountry())) && (countryFilter.get(testCaseExecution.getCountry()).equals("on"))) {

                    if (isManual) {
                        totalManualExecution++;

                        String executor = "NoExecutorDefined";
                        if (!StringUtil.isEmptyOrNull(testCaseExecution.getExecutor())) {
                            executor = testCaseExecution.getExecutor();
                        }

                        if (manualExecutions.containsKey(executor)) {
                            JSONObject executorObj = manualExecutions.get(executor);
                            JSONArray array = (JSONArray) executorObj.get("executionList");
                            array.put(testCaseExecution.getId());
                            JSONArray arrayWE = (JSONArray) executorObj.get("executionWEList");
                            if (controlStatus.equals(TestCaseExecution.CONTROLSTATUS_WE)) {
                                arrayWE.put(testCaseExecution.getId());
                            }
                            executorObj.put("executionList", array);
                            executorObj.put("executionWEList", arrayWE);
                            executorObj.put("executor", executor);
                            manualExecutions.put(executor, executorObj);
                        } else {
                            JSONObject executorObj = new JSONObject();
                            JSONArray array = new JSONArray();
                            array.put(testCaseExecution.getId());
                            JSONArray arrayWE = new JSONArray();
                            if (controlStatus.equals(TestCaseExecution.CONTROLSTATUS_WE)) {
                                arrayWE.put(testCaseExecution.getId());
                            }
                            executorObj.put("executionList", array);
                            executorObj.put("executionWEList", arrayWE);
                            executorObj.put("executor", executor);
                            manualExecutions.put(executor, executorObj);
                        }

                        if (controlStatus.equals(TestCaseExecution.CONTROLSTATUS_WE)) {
                            totalManualWEExecution++;
                        }
                    }

                }

                JSONArray array = new JSONArray();
                for (Map.Entry<String, JSONObject> entry : manualExecutions.entrySet()) {
                    Object key = entry.getKey();
                    JSONObject val = entry.getValue();
                    array.put(val);
                }
                manualExecutionTable.put("perExecutor", array);
                manualExecutionTable.put("totalExecution", totalManualExecution);
                manualExecutionTable.put("totalWEExecution", totalManualWEExecution);

            } catch (JSONException ex) {
                LOG.error("Error on generateManualExecutionTable : " + ex, ex);
            } catch (Exception ex) {
                LOG.error("Error on generateManualExecutionTable : " + ex, ex);
            }
        }
        return manualExecutionTable;
    }

    private JSONObject generateTestFolderChart(List<TestCaseExecution> testCaseExecutions, String tag, JSONObject statusFilter, JSONObject countryFilter) throws JSONException {
        JSONObject jsonResult = new JSONObject();
        Map<String, JSONObject> axisMap = new HashMap<>();
        String globalStart = "";
        String globalEnd = "";
        long globalStartL = 0;
        long globalEndL = 0;
        String globalStatus = "Finished";

        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            String key;
            JSONObject control = new JSONObject();
            JSONObject function = new JSONObject();

            String controlStatus = testCaseExecution.getControlStatus();
            if (statusFilter.get(controlStatus).equals("on") && countryFilter.has(testCaseExecution.getCountry()) && countryFilter.get(testCaseExecution.getCountry()).equals("on")) {

                key = testCaseExecution.getTest();

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
                if ((globalStartL == 0) || (globalStartL > testCaseExecution.getStart())) {
                    globalStartL = testCaseExecution.getStart();
                    globalStart = String.valueOf(new Date(testCaseExecution.getStart()));
                }
            }
            if (!testCaseExecution.getControlStatus().equalsIgnoreCase("PE") && testCaseExecution.getEnd() != 0) {
                if ((globalEndL == 0) || (globalEndL < testCaseExecution.getEnd())) {
                    globalEndL = testCaseExecution.getEnd();
                    globalEnd = String.valueOf(new Date(testCaseExecution.getEnd()));
                }
            }
            if (testCaseExecution.getControlStatus().equalsIgnoreCase("PE")) {
                globalStatus = "Pending...";
            }
        }

        Gson gson = new Gson();
        List<JSONObject> axisList = new ArrayList<>();

        for (Map.Entry<String, JSONObject> entry : axisMap.entrySet()) {
            String key = entry.getKey();
            JSONObject value = entry.getValue();
            axisList.add(value);
        }
        Collections.sort(axisList, new SortExecution());
        jsonResult.put("axis", axisList);
        jsonResult.put("tag", tag);
        jsonResult.put("globalEnd", gson.toJson(new Timestamp(globalEndL)).replace("\"", ""));
        jsonResult.put("globalStart", globalStart);
        jsonResult.put("globalStatus", globalStatus);

        return jsonResult;
    }

    class SortExecution implements Comparator<JSONObject> {
        // Used for sorting in ascending order of
        // name value.

        @Override
        public int compare(JSONObject a, JSONObject b) {
            if (a != null && b != null) {
                try {
                    String aS = (String) a.get("name");
                    String bS = (String) b.get("name");
                    return aS.compareToIgnoreCase(bS);
                } catch (JSONException ex) {
                    LOG.error("JSON Error Exception", ex);
                    return 1;
                }
            } else {
                return 1;
            }
        }
    }

    private JSONObject generateStats(HttpServletRequest request, List<TestCaseExecution> testCaseExecutions, JSONObject statusFilter, JSONObject countryFilter, boolean splitStats) throws JSONException {

        JSONObject jsonResult = new JSONObject();
        boolean env = request.getParameter("env") != null || !splitStats;
        boolean country = request.getParameter("country") != null || !splitStats;
        boolean robotDecli = request.getParameter("robotDecli") != null || !splitStats;
        boolean app = request.getParameter("app") != null || !splitStats;

        HashMap<String, SummaryStatisticsDTO> statMap = new HashMap<>();
        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            String controlStatus = testCaseExecution.getControlStatus();
            if (statusFilter.get(controlStatus).equals("on")
                    && countryFilter.has(testCaseExecution.getCountry())
                    && countryFilter.get(testCaseExecution.getCountry()).equals("on")) {

                StringBuilder key = new StringBuilder();

                key.append((env) ? testCaseExecution.getEnvironment() : "");
                key.append("_");
                key.append((country) ? testCaseExecution.getCountry() : "");
                key.append("_");
                key.append((robotDecli) ? testCaseExecution.getRobotDecli() : "");
                key.append("_");
                key.append((app) ? testCaseExecution.getApplication() : "");

                SummaryStatisticsDTO stat = new SummaryStatisticsDTO();
                stat.setEnvironment(testCaseExecution.getEnvironment());
                stat.setCountry(testCaseExecution.getCountry());
                stat.setRobotDecli(testCaseExecution.getRobotDecli());
                stat.setApplication(testCaseExecution.getApplication());

                statMap.put(key.toString(), stat);
            }
        }

        jsonResult.put("contentTable", getStatByEnvCountryRobotDecli(testCaseExecutions, statMap, env, country, robotDecli, app, statusFilter, countryFilter, splitStats));

        return jsonResult;
    }

    private JSONObject generateBugStats(HttpServletRequest request, List<TestCaseExecution> testCaseExecutions, JSONObject statusFilter, JSONObject countryFilter) throws JSONException {

        JSONObject jsonResult = new JSONObject();
        SummaryStatisticsBugTrackerDTO stat = new SummaryStatisticsBugTrackerDTO();
        String bugsToReport = "KO,FA";
        stat.setNbExe(1);
        int totalBugReported = 0;
        int totalBugToReport = 0;
        int totalBugToReportReported = 0;
        int totalBugToClean = 0;
        HashMap<String, SummaryStatisticsBugTrackerDTO> statMap = new HashMap<>();
        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            String controlStatus = testCaseExecution.getControlStatus();
            if (statusFilter.get(controlStatus).equals("on")
                    && countryFilter.has(testCaseExecution.getCountry())
                    && countryFilter.get(testCaseExecution.getCountry()).equals("on")) {

                String key = "";

                if (bugsToReport.contains(testCaseExecution.getControlStatus())) {
                    totalBugToReport++;
                }
                if ((testCaseExecution.getTestCaseObj() != null) && (testCaseExecution.getTestCaseObj().getBugs().length() > 0)) {
                    JSONArray arr = testCaseExecution.getTestCaseObj().getBugs();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject bug = (JSONObject) arr.get(i);
                        key = bug.getString("id");

                        stat = statMap.get(key);
                        totalBugReported++;
                        if (stat == null) {
                            stat = new SummaryStatisticsBugTrackerDTO();
                            stat.setNbExe(1);
                            stat.setBugId(key);
                            stat.setBugIdURL(testCaseExecution.getApplicationObj().getBugTrackerUrl().replace("%BUGID%", key));
                            stat.setExeIdLastStatus(testCaseExecution.getControlStatus());
                            stat.setExeIdFirst(testCaseExecution.getId());
                            stat.setExeIdLast(testCaseExecution.getId());
                            stat.setTestFirst(testCaseExecution.getTest());
                            stat.setTestLast(testCaseExecution.getTest());
                            stat.setTestCaseFirst(testCaseExecution.getTestCase());
                            stat.setTestCaseLast(testCaseExecution.getTestCase());
                        } else {
                            stat.setNbExe(stat.getNbExe() + 1);
                            stat.setExeIdLastStatus(testCaseExecution.getControlStatus());
                            stat.setExeIdLast(testCaseExecution.getId());
                            stat.setTestLast(testCaseExecution.getTest());
                            stat.setTestCaseLast(testCaseExecution.getTestCase());
                        }
                        if (!(bugsToReport.contains(testCaseExecution.getControlStatus()))) {
                            totalBugToClean++;
                            stat.setToClean(true);
                        } else {
                            totalBugToReportReported++;
                        }
                        statMap.put(key, stat);
                    }
                }

            }
        }

        Gson gson = new Gson();
        JSONArray dataArray = new JSONArray();
        for (String key : statMap.keySet()) {
            SummaryStatisticsBugTrackerDTO sumStats = statMap.get(key);
            dataArray.put(new JSONObject(gson.toJson(sumStats)));
        }

        jsonResult.put("BugTrackerStat", dataArray);
        jsonResult.put("totalBugToReport", totalBugToReport);
        jsonResult.put("totalBugToReportReported", totalBugToReportReported);
        jsonResult.put("totalBugReported", totalBugReported);
        jsonResult.put("totalBugToClean", totalBugToClean);

        return jsonResult;
    }

    private JSONObject getStatByEnvCountryRobotDecli(List<TestCaseExecution> testCaseExecutions, HashMap<String, SummaryStatisticsDTO> statMap, boolean env, boolean country, boolean robotDecli, boolean app, JSONObject statusFilter, JSONObject countryFilter, boolean splitStats) throws JSONException {
        SummaryStatisticsDTO total = new SummaryStatisticsDTO();
        total.setEnvironment("Total");

        for (TestCaseExecution testCaseExecution : testCaseExecutions) {

            String controlStatus = testCaseExecution.getControlStatus();
            if ((statusFilter.get(controlStatus).equals("on") && countryFilter.has(testCaseExecution.getCountry()) && countryFilter.get(testCaseExecution.getCountry()).equals("on"))
                    || !splitStats) {
                StringBuilder key = new StringBuilder();

                key.append((env) ? testCaseExecution.getEnvironment() : "");
                key.append("_");
                key.append((country) ? testCaseExecution.getCountry() : "");
                key.append("_");
                key.append((robotDecli) ? testCaseExecution.getRobotDecli() : "");
                key.append("_");
                key.append((app) ? testCaseExecution.getApplication() : "");

                if (statMap.containsKey(key.toString())) {
                    statMap.get(key.toString()).updateStatisticByStatus(testCaseExecution.getControlStatus());
                }
                total.updateStatisticByStatus(testCaseExecution.getControlStatus());
            }
        }
        return extractSummaryData(statMap, total, splitStats);
    }

    private JSONObject extractSummaryData(HashMap<String, SummaryStatisticsDTO> summaryMap, SummaryStatisticsDTO total, boolean splitStats) throws JSONException {
        JSONObject extract = new JSONObject();
        Gson gson = new Gson();
        if (splitStats) {
            JSONArray dataArray = new JSONArray();
            //sort keys
            TreeMap<String, SummaryStatisticsDTO> sortedKeys = new TreeMap<>(summaryMap);
            for (String key : sortedKeys.keySet()) {
                SummaryStatisticsDTO sumStats = summaryMap.get(key);
                //percentage values
                sumStats.updatePercentageStatistics();
                dataArray.put(new JSONObject(gson.toJson(sumStats)));
            }
            extract.put("split", dataArray);
        }
        total.updatePercentageStatistics();
        extract.put("total", new JSONObject(gson.toJson(total)));
        return extract;
    }

    private String getColor(String controlStatus) {
        String color = null;

        if ("OK".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_OK_COL;
        } else if ("KO".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_KO_COL;
        } else if ("FA".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_FA_COL;
        } else if ("CA".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_CA_COL;
        } else if ("NA".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_NA_COL;
        } else if ("NE".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_NE_COL;
        } else if ("WE".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_WE_COL;
        } else if ("PE".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_PE_COL;
        } else if ("QU".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_QU_COL;
        } else if ("QE".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_QE_COL;
        } else if ("PA".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_PA_COL;
        } else {
            color = "#000000";
        }
        return color;
    }

    private JSONObject convertTagToJSONObject(Tag tag) throws JSONException {

        return tag.toJson();
    }

    private JSONObject generateLabelStats(ApplicationContext appContext, HttpServletRequest request, List<TestCaseExecution> testCaseExecutions, JSONObject statusFilter, JSONObject countryFilter, List<TestCaseLabel> testCaseLabelList) throws JSONException {

        JSONObject jsonResult = new JSONObject();

        labelService = appContext.getBean(LabelService.class);
        TreeNode node;
        JSONArray jsonArraySTICKER = new JSONArray();
        JSONArray jsonArrayREQUIREMENT = new JSONArray();

        AnswerList<Label> resp = labelService.readByVarious(new ArrayList<>(), new ArrayList<>(asList(Label.TYPE_STICKER, Label.TYPE_REQUIREMENT)));

        // Building Label inputlist with target layout
        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {

            HashMap<Integer, TreeNode> inputList = new HashMap<>();

            for (Label label : resp.getDataList()) {

                String text = "";

                text += "<span class='label label-primary' style='background-color:" + label.getColor() + "' data-toggle='tooltip' data-labelid='" + label.getId() + "' title='' data-original-title=''>" + label.getLabel() + "</span>";
                text += "<span style='margin-left: 5px; margin-right: 5px;' class=''>" + label.getDescription() + "</span>";

                text += "%STATUSBAR%";
                text += "%COUNTER1TEXT%";
                text += "%COUNTER1WITHCHILDTEXT%";
                text += "%NBNODESWITHCHILDTEXT%";

                // Specific pills
                //text += "<span class='badge badge-pill badge-secondary'>666</span>";
                // Standard pills
                List<String> attributList = new ArrayList<>();
                if (Label.TYPE_REQUIREMENT.equals(label.getType())) {
                    if (!StringUtil.isEmptyOrNull(label.getRequirementType()) && !"unknown".equalsIgnoreCase(label.getRequirementType())) {
                        attributList.add("<span class='badge badge-pill badge-secondary'>" + label.getRequirementType() + "</span>");
                    }
                    if (!StringUtil.isEmptyOrNull(label.getRequirementStatus()) && !"unknown".equalsIgnoreCase(label.getRequirementStatus())) {
                        attributList.add("<span class='badge badge-pill badge-secondary'>" + label.getRequirementStatus() + "</span>");
                    }
                    if (!StringUtil.isEmptyOrNull(label.getRequirementCriticity()) && !"unknown".equalsIgnoreCase(label.getRequirementCriticity())) {
                        attributList.add("<span class='badge badge-pill badge-secondary'>" + label.getRequirementCriticity() + "</span>");
                    }
                }

                // Create Node.
                node = new TreeNode(label.getId() + "-" + label.getSystem() + "-" + label.getLabel(), label.getSystem(), label.getLabel(), label.getId(), label.getParentLabelID(), text, null, null, false);
                node.setCounter1(0);
                node.setCounter1WithChild(0);
                node.setTags(attributList);
                node.setType(label.getType());
                node.setCounter1Text("<span style='background-color:#000000' class='cnt1 badge badge-pill badge-secondary'>%COUNTER1%</span>");
                node.setCounter1WithChildText("<span class='cnt1WC badge badge-pill badge-secondary'>%COUNTER1WITHCHILD%</span>");
                node.setNbNodesText("<span style='background-color:#337ab7' class='nbNodes badge badge-pill badge-primary'>%NBNODESWITHCHILD%</span>");
                node.setLabelObj(label);
                inputList.put(node.getId(), node);
//                    LOG.debug("Label : " + node.getId() + " T : " + node);
            }

            HashMap<String, List<Integer>> testCaseWithLabel1 = new HashMap<>();
            for (TestCaseLabel label : testCaseLabelList) {
//                LOG.debug("TCLabel : " + label.getLabel() + " T : " + label.getTest() + " C : " + label.getTestcase() + " Type : " + label.getLabel().getType());
                if ((Label.TYPE_STICKER.equals(label.getLabel().getType()))
                        || (Label.TYPE_REQUIREMENT.equals(label.getLabel().getType()))) {
                    String key = label.getTest() + "_" + label.getTestcase();
                    List<Integer> curLabelIdList = new ArrayList<>();
                    if (testCaseWithLabel1.get(key) != null) {
                        curLabelIdList = testCaseWithLabel1.get(key);
                        curLabelIdList.add(label.getLabelId());
                        testCaseWithLabel1.put(key, curLabelIdList);
//                        LOG.debug("  ADDED");
                    } else {
                        curLabelIdList.add(label.getLabelId());
                        testCaseWithLabel1.put(key, curLabelIdList);
//                        LOG.debug("  ADDED");
                    }
                }
            }

            /**
             * For All execution, get all labels from the test case and add if
             * those labels were in the list add the stats of executions into
             * the counters.
             */
            for (TestCaseExecution testCaseExecution : testCaseExecutions) {
//                    LOG.debug("Exe : " + testCaseExecution.getId() + " T : " + testCaseExecution.getTest() + " C : " + testCaseExecution.getTestCase());
                String controlStatus = testCaseExecution.getControlStatus();
                if (statusFilter.get(controlStatus).equals("on")
                        && countryFilter.has(testCaseExecution.getCountry())
                        && countryFilter.get(testCaseExecution.getCountry()).equals("on")) {

                    //Get label for current test_testcase
                    List<Integer> labelsForTestCase = testCaseWithLabel1.get(testCaseExecution.getTest() + "_" + testCaseExecution.getTestCase());
                    if (labelsForTestCase != null) {
                        for (Integer integer : labelsForTestCase) {
//                                LOG.debug(" T : " + testCaseExecution.getTest() + " C : " + testCaseExecution.getTestCase() + " T : " + integer);
                            TreeNode curTreenode = inputList.get(integer);
                            if (curTreenode != null) {
//                                    LOG.debug(" K : " + titi.getKey() + " C : " + titi.getCounter1());
                                curTreenode.setCounter1(curTreenode.getCounter1() + 1);
                                curTreenode.setCounter1WithChild(curTreenode.getCounter1WithChild() + 1);
                                switch (testCaseExecution.getControlStatus()) {
                                    case TestCaseExecution.CONTROLSTATUS_OK:
                                        curTreenode.setNbOK(curTreenode.getNbOK() + 1);
                                        break;
                                    case TestCaseExecution.CONTROLSTATUS_KO:
                                        curTreenode.setNbKO(curTreenode.getNbKO() + 1);
                                        break;
                                    case TestCaseExecution.CONTROLSTATUS_FA:
                                        curTreenode.setNbFA(curTreenode.getNbFA() + 1);
                                        break;
                                    case TestCaseExecution.CONTROLSTATUS_NA:
                                        curTreenode.setNbNA(curTreenode.getNbNA() + 1);
                                        break;
                                    case TestCaseExecution.CONTROLSTATUS_NE:
                                        curTreenode.setNbNE(curTreenode.getNbNE() + 1);
                                        break;
                                    case TestCaseExecution.CONTROLSTATUS_WE:
                                        curTreenode.setNbWE(curTreenode.getNbWE() + 1);
                                        break;
                                    case TestCaseExecution.CONTROLSTATUS_PE:
                                        curTreenode.setNbPE(curTreenode.getNbPE() + 1);
                                        break;
                                    case TestCaseExecution.CONTROLSTATUS_QE:
                                        curTreenode.setNbQE(curTreenode.getNbQE() + 1);
                                        break;
                                    case TestCaseExecution.CONTROLSTATUS_QU:
                                        curTreenode.setNbQU(curTreenode.getNbQU() + 1);
                                        break;
                                    case TestCaseExecution.CONTROLSTATUS_PA:
                                        curTreenode.setNbPA(curTreenode.getNbPA() + 1);
                                        break;
                                    case TestCaseExecution.CONTROLSTATUS_CA:
                                        curTreenode.setNbCA(curTreenode.getNbCA() + 1);
                                        break;
                                }
                                inputList.put(curTreenode.getId(), curTreenode);
                            }
                        }
                    }
                }
            }

            // Build Tres.
            List<TreeNode> finalList;
            jsonArraySTICKER = new JSONArray();
            jsonArrayREQUIREMENT = new JSONArray();
            finalList = labelService.hierarchyConstructor(inputList);

            for (TreeNode treeNode : finalList) {
                if (treeNode.getCounter1WithChild() > 0) {
                    if (Label.TYPE_STICKER.equals(treeNode.getType())) {
                        jsonArraySTICKER.put(treeNode.toJson());
                    } else {
                        jsonArrayREQUIREMENT.put(treeNode.toJson());
                    }
                }
            }

        }

        if ((jsonArraySTICKER.length() <= 0) && (jsonArrayREQUIREMENT.length() <= 0)) {
            return null;
        }
        jsonResult.put("labelTreeSTICKER", jsonArraySTICKER);
        jsonResult.put("labelTreeREQUIREMENT", jsonArrayREQUIREMENT);

        return jsonResult;
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
