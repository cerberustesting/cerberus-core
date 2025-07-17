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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionQueue;
import org.cerberus.core.crud.entity.TestCaseLabel;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.IBuildRevisionInvariantService;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.crud.service.ITestCaseLabelService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.crud.service.impl.ApplicationService;
import org.cerberus.core.crud.service.impl.BuildRevisionInvariantService;
import org.cerberus.core.crud.service.impl.InvariantService;
import org.cerberus.core.crud.service.impl.TestCaseExecutionService;
import org.cerberus.core.crud.service.impl.TestCaseService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.cerberus.core.util.StringUtil;

/**
 * @author cerberus
 */
@WebServlet(name = "ReadTestCaseExecution", urlPatterns = {"/ReadTestCaseExecution"})
public class ReadTestCaseExecution extends HttpServlet {

    private ITestCaseExecutionService testCaseExecutionService;
    private ITestCaseExecutionQueueService testCaseExecutionInQueueService;
    private ITestCaseLabelService testCaseLabelService;
    private ITestCaseService testCaseService;
    private IInvariantService invariantService;
    private IBuildRevisionInvariantService buildRevisionInvariantService;
    private IApplicationService applicationService;

    private static final Logger LOG = LogManager.getLogger(ReadTestCaseExecution.class);
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.S'Z'";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws org.cerberus.core.exception.CerberusException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, CerberusException, ParseException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");

        testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
        testCaseService = appContext.getBean(ITestCaseService.class);

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);
        JSONObject jsonResponse = new JSONObject();
        try {
            try {

                AnswerItem answer = new AnswerItem<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
                // Data/Filter Parameters.
                String Tag = ParameterParserUtil.parseStringParam(request.getParameter("Tag"), "");
                String value = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
                String test = ParameterParserUtil.parseStringParam(request.getParameter("test"), "");
                String testCase = ParameterParserUtil.parseStringParam(request.getParameter("testCase"), "");
                List<String> system = ParameterParserUtil.parseListParamAndDeleteEmptyValue(request.getParameterValues("system"), Arrays.asList("DEFAULT"), "UTF-8");
                long executionId = ParameterParserUtil.parseLongParam(request.getParameter("executionId"), 0);
                boolean likeColumn = ParameterParserUtil.parseBooleanParam(request.getParameter("likeColumn"), false);
                // Switch Parameters.
                boolean executionWithDependency = ParameterParserUtil.parseBooleanParam("executionWithDependency", false);
                String columnName = ParameterParserUtil.parseStringParam(request.getParameter("columnName"), "");
                boolean byColumns = ParameterParserUtil.parseBooleanParam(request.getParameter("byColumns"), false);

                if (!StringUtil.isEmptyOrNull(columnName)) {
                    //If columnName is present, then return the distinct value of this column.
                    answer = findValuesForColumnFilter(system, test, appContext, request, columnName);
                    jsonResponse = (JSONObject) answer.getItem();
                } else if (!Tag.isEmpty() && byColumns) {
                    //Return the columns to display in the execution table
                    answer = findExecutionColumns(appContext, request, Tag);
                    jsonResponse = (JSONObject) answer.getItem();
//                } else if (!Tag.isEmpty() && !byColumns) {
//                    //Return the list of execution for the execution table
//                    LOG.debug("toto");
//                    answer = findExecutionListByTag(appContext, request, Tag);
//                    jsonResponse = (JSONObject) answer.getItem();
                } else if (!test.isEmpty() && !testCase.isEmpty()) {
                    TestCaseExecution lastExec = testCaseExecutionService.findLastTestCaseExecutionNotPE(test, testCase);
                    JSONObject result = new JSONObject();
                    if (lastExec != null) {
                        result.put("id", lastExec.getId());
                        result.put("queueId", lastExec.getQueueID());
                        result.put("controlStatus", lastExec.getControlStatus());
                        result.put("tag", lastExec.getTag());

                        result.put("env", lastExec.getEnvironment());
                        result.put("country", lastExec.getCountry());
                        Date d = new Date(lastExec.getEnd());
                        TimeZone tz = TimeZone.getTimeZone("UTC");
                        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
                        df.setTimeZone(tz);
                        result.put("end", df.format(d)).toString();
                    }
                    jsonResponse.put("contentTable", result);
                } else if (executionId != 0 && !executionWithDependency) {
                    answer = testCaseExecutionService.readByKeyWithDependency(executionId);
                    TestCaseExecution tce = (TestCaseExecution) answer.getItem();
                    if (tce.getTestCaseObj() != null) {
                        tce.getTestCaseObj().setRefOrigineUrl(testCaseService.getRefOriginUrl(tce.getTestCaseObj().getOrigine(), tce.getTestCaseObj().getRefOrigine(), tce.getTestCaseObj().getSystem()));
                    }
                    jsonResponse.put("testCaseExecution", tce.toJson(true));
                } else if (executionId != 0 && executionWithDependency) {

                } else {
                    answer = findTestCaseExecutionList(appContext, true, request);
                    jsonResponse = (JSONObject) answer.getItem();
                }

                jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
                jsonResponse.put("message", answer.getResultMessage().getDescription());

                response.getWriter().print(jsonResponse.toString());
            } catch (CerberusException ce) {
                LOG.warn(ce, ce);
                AnswerItem answer = AnswerUtil.convertToAnswerItem(() -> {
                    throw ce;
                });

                jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
                jsonResponse.put("message", answer.getResultMessage().getDescription());

                response.getWriter().print(jsonResponse.toString());
            } catch (Exception ce) {
                LOG.error(ce, ce);
            }

        } catch (JSONException ex) {
            LOG.warn(ex, ex);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        } catch (Exception ce) {
            LOG.error(ce, ce);
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
            LOG.warn(ex);
        } catch (ParseException ex) {
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

    private AnswerItem<JSONObject> findExecutionColumns(ApplicationContext appContext, HttpServletRequest request, String Tag) throws CerberusException, ParseException, JSONException {
        AnswerItem<JSONObject> answer = new AnswerItem<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        JSONObject jsonResponse = new JSONObject();

        AnswerList<TestCaseExecution> testCaseExecutionList = new AnswerList<>();
        AnswerList<TestCaseExecutionQueue> testCaseExecutionListInQueue = new AnswerList<>();

        testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
        testCaseExecutionInQueueService = appContext.getBean(ITestCaseExecutionQueueService.class);

        /**
         * Get list of execution by tag, env, country, browser
         */
        testCaseExecutionList = testCaseExecutionService.readDistinctEnvCountryBrowserByTag(Tag);
        List<TestCaseExecution> testCaseExecutions = testCaseExecutionList.getDataList();

        /**
         * Get list of Execution in Queue by Tag
         */
        testCaseExecutionListInQueue = testCaseExecutionInQueueService.readDistinctEnvCountryBrowserByTag(Tag);
        List<TestCaseExecutionQueue> testCaseExecutionsInQueue = testCaseExecutionListInQueue.getDataList();

        /**
         * Feed hash map with execution from the two list (to get only one by
         * test,testcase,country,env,browser)
         */
        LinkedHashMap<String, TestCaseExecution> testCaseExecutionsList = new LinkedHashMap<>();

        for (TestCaseExecution testCaseWithExecution : testCaseExecutions) {
            String key = testCaseWithExecution.getBrowser() + "_"
                    + testCaseWithExecution.getCountry() + "_"
                    + testCaseWithExecution.getEnvironment() + " "
                    + testCaseWithExecution.getControlStatus();
            testCaseExecutionsList.put(key, testCaseWithExecution);
        }
        for (TestCaseExecutionQueue testCaseWithExecutionInQueue : testCaseExecutionsInQueue) {
            TestCaseExecution testCaseExecution = testCaseExecutionInQueueService.convertToTestCaseExecution(testCaseWithExecutionInQueue);
            String key = testCaseExecution.getBrowser() + "_"
                    + testCaseExecution.getCountry() + "_"
                    + testCaseExecution.getEnvironment() + "_"
                    + testCaseExecution.getControlStatus();
            testCaseExecutionsList.put(key, testCaseExecution);
        }

        testCaseExecutions = new ArrayList<>(testCaseExecutionsList.values());

        JSONObject statusFilter = getStatusList(request);
        JSONObject countryFilter = getCountryList(request, appContext);
        LinkedHashMap<String, JSONObject> columnMap = new LinkedHashMap<>();

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
//
//    private AnswerItem findExecutionListByTag(ApplicationContext appContext, HttpServletRequest request, String Tag) throws CerberusException, ParseException, JSONException {
//        AnswerItem<JSONObject> answer = new AnswerItem<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
//        testCaseLabelService = appContext.getBean(ITestCaseLabelService.class);
//
//        int startPosition = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayStart"), "0"));
//        int length = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayLength"), "0"));
//
//        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
//        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "test,testCase,application,priority,status,description,bugs,function");
//        String columnToSort[] = sColumns.split(",");
//
//        //Get Sorting information
//        int numberOfColumnToSort = Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortingCols"), "1"));
//        int columnToSortParameter = 0;
//        String sort = "asc";
//        StringBuilder sortInformation = new StringBuilder();
//        for (int c = 0; c < numberOfColumnToSort; c++) {
//            columnToSortParameter = Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortCol_" + c), "0"));
//            sort = ParameterParserUtil.parseStringParam(request.getParameter("sSortDir_" + c), "asc");
//            String columnName = columnToSort[columnToSortParameter];
//            sortInformation.append(columnName).append(" ").append(sort);
//
//            if (c != numberOfColumnToSort - 1) {
//                sortInformation.append(" , ");
//            }
//        }
//
//        Map<String, List<String>> individualSearch = new HashMap<>();
//        for (int a = 0; a < columnToSort.length; a++) {
//            if (null != request.getParameter("sSearch_" + a) && !request.getParameter("sSearch_" + a).isEmpty()) {
//                List<String> search = new ArrayList<>(Arrays.asList(request.getParameter("sSearch_" + a).split(",")));
//                individualSearch.put(columnToSort[a], search);
//            }
//        }
//                    LOG.debug("tata");
//
//        List<TestCaseExecution> testCaseExecutions = readExecutionByTagList(appContext, Tag, startPosition, length, sortInformation.toString(), searchParameter, individualSearch);
//
//        JSONArray executionList = new JSONArray();
//        JSONObject statusFilter = getStatusList(request);
//        JSONObject countryFilter = getCountryList(request, appContext);
//        LinkedHashMap<String, JSONObject> ttc = new LinkedHashMap<>();
//
//        String globalStart = "";
//        String globalEnd = "";
//        String globalStatus = "Finished";
//
//        /**
//         * Find the list of labels
//         */
//        AnswerList<TestCaseLabel> testCaseLabelList = testCaseLabelService.readByTestTestCase(null, null, null);
//
//        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
//            try {
//                if (testCaseExecution.getStart() != 0) {
//                    if ((globalStart.isEmpty()) || (globalStart.compareTo(String.valueOf(testCaseExecution.getStart())) > 0)) {
//                        globalStart = String.valueOf(testCaseExecution.getStart());
//                    }
//                }
//                if (testCaseExecution.getEnd() != 0) {
//                    if ((globalEnd.isEmpty()) || (globalEnd.compareTo(String.valueOf(testCaseExecution.getEnd())) < 0)) {
//                        globalEnd = String.valueOf(testCaseExecution.getEnd());
//                    }
//                }
//                if (testCaseExecution.getControlStatus().equalsIgnoreCase("PE")) {
//                    globalStatus = "Pending...";
//                }
//                String controlStatus = testCaseExecution.getControlStatus();
//                if (statusFilter.get(controlStatus).equals("on") && countryFilter.get(testCaseExecution.getCountry()).equals("on")) {
//                    JSONObject execution = testCaseExecutionToJSONObject(testCaseExecution);
//                    String execKey = testCaseExecution.getEnvironment() + " " + testCaseExecution.getCountry() + " " + testCaseExecution.getBrowser();
//                    String testCaseKey = testCaseExecution.getTest() + "_" + testCaseExecution.getTestCase();
//                    JSONObject execTab = new JSONObject();
//
//                    executionList.put(testCaseExecutionToJSONObject(testCaseExecution));
//                    JSONObject ttcObject = new JSONObject();
//
//                    if (ttc.containsKey(testCaseKey)) {
//                        ttcObject = ttc.get(testCaseKey);
//                        execTab = ttcObject.getJSONObject("execTab");
//                        execTab.put(execKey, execution);
//                        ttcObject.put("execTab", execTab);
//                    } else {
//                        ttcObject.put("test", testCaseExecution.getTest());
//                        ttcObject.put("testCase", testCaseExecution.getTestCase());
//                        ttcObject.put("shortDesc", testCaseExecution.getTestCaseObj().getDescription());
//                        ttcObject.put("status", testCaseExecution.getStatus());
//                        ttcObject.put("application", testCaseExecution.getApplication());
//                        ttcObject.put("priority", testCaseExecution.getTestCaseObj().getPriority());
//                        ttcObject.put("comment", testCaseExecution.getTestCaseObj().getComment());
//                        execTab.put(execKey, execution);
//                        ttcObject.put("execTab", execTab);
//
//                        /**
//                         * Iterate on the label retrieved and generate HashMap
//                         * based on the key Test_TestCase
//                         */
//                        LinkedHashMap<String, JSONArray> testCaseWithLabel = new LinkedHashMap<>();
//                        for (TestCaseLabel label : testCaseLabelList.getDataList()) {
//                            String key = label.getTest() + "_" + label.getTestcase();
//
//                            if (testCaseWithLabel.containsKey(key)) {
//                                JSONObject jo = new JSONObject().put("name", label.getLabel().getLabel()).put("color", label.getLabel().getColor()).put("description", label.getLabel().getDescription());
//                                testCaseWithLabel.get(key).put(jo);
//                            } else {
//                                JSONObject jo = new JSONObject().put("name", label.getLabel().getLabel()).put("color", label.getLabel().getColor()).put("description", label.getLabel().getDescription());
//                                testCaseWithLabel.put(key, new JSONArray().put(jo));
//                            }
//                        }
//                        ttcObject.put("labels", testCaseWithLabel.get(testCaseExecution.getTest() + "_" + testCaseExecution.getTestCase()));
//                    }
//                    ttc.put(testCaseExecution.getTest() + "_" + testCaseExecution.getTestCase(), ttcObject);
//                }
//            } catch (JSONException ex) {
//                LOG.warn(ex);
//            }
//        }
//
//        JSONObject jsonResponse = new JSONObject();
//
//        jsonResponse.put("globalEnd", globalEnd);
//        jsonResponse.put("globalStart", globalStart);
//        jsonResponse.put("globalStatus", globalStatus);
//
//        jsonResponse.put("testList", ttc.values());
//        jsonResponse.put("iTotalRecords", ttc.size());
//        jsonResponse.put("iTotalDisplayRecords", ttc.size());
//
//        answer.setItem(jsonResponse);
//        answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
//        return answer;
//    }

    private AnswerItem<JSONObject> findTestCaseExecutionList(ApplicationContext appContext, boolean userHasPermissions, HttpServletRequest request) throws JSONException, CerberusException {
        AnswerItem<JSONObject> answer = new AnswerItem<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED));
        List<TestCaseExecution> testCaseExecutionList;
        JSONObject object = new JSONObject();

        testCaseExecutionService = appContext.getBean(TestCaseExecutionService.class);

        int startPosition = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayStart"), "0"));
        int length = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayLength"), "0"));

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        int columnToSortParameter = Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortCol_0"), "0"));
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "test,description,active,automated,tdatecrea");
        String columnToSort[] = sColumns.split(",");
        String columnName = columnToSort[columnToSortParameter];
        String sort = ParameterParserUtil.parseStringParam(request.getParameter("sSortDir_0"), "asc");
        List<String> system = ParameterParserUtil.parseListParamAndDeleteEmptyValue(request.getParameterValues("system"), Arrays.asList("DEFAULT"), "UTF-8");

        Map<String, List<String>> individualSearch = new HashMap<>();
        List<String> individualLike = new ArrayList<>(Arrays.asList(ParameterParserUtil.parseStringParam(request.getParameter("sLike"), "").split(",")));

        for (int a = 0; a < columnToSort.length; a++) {
            if (null != request.getParameter("sSearch_" + a) && !request.getParameter("sSearch_" + a).isEmpty()) {
                List<String> search = new ArrayList<>(Arrays.asList(request.getParameter("sSearch_" + a).split(",")));
                if (individualLike.contains(columnToSort[a])) {
                    individualSearch.put(columnToSort[a] + ":like", search);
                } else {
                    individualSearch.put(columnToSort[a], search);
                }
            }
        }

        AnswerList<TestCaseExecution> resp = testCaseExecutionService.readByCriteria(startPosition, length, columnName.concat(" ").concat(sort), searchParameter, individualSearch, individualLike, system);

        JSONArray jsonArray = new JSONArray();
        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            for (TestCaseExecution testCaseExecution : resp.getDataList()) {
                jsonArray.put(testCaseExecution.toJson(true).put("hasPermissions", userHasPermissions));
            }
        }

        object.put("contentTable", jsonArray);
        object.put("hasPermissions", userHasPermissions);
        object.put("iTotalRecords", resp.getTotalRows());
        object.put("iTotalDisplayRecords", resp.getTotalRows());

        answer.setItem(object);
        answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK_GENERIC));
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
            LOG.warn(ex);
        }

        return statusList;
    }

    private JSONObject getCountryList(HttpServletRequest request, ApplicationContext appContext) {
        JSONObject countryList = new JSONObject();
        try {
            invariantService = appContext.getBean(InvariantService.class);
            for (Invariant country : invariantService.readByIdName("COUNTRY")) {
                countryList.put(country.getValue(), ParameterParserUtil.parseStringParam(request.getParameter(country.getValue()), "off"));
            }
        } catch (JSONException ex) {
            LOG.warn("JSON exception when getting Country List.", ex);
        } catch (CerberusException ex) {
            LOG.error("JSON exception when getting Country List.", ex);
        }

        return countryList;
    }

//    private List<TestCaseExecution> hashExecution(List<TestCaseExecution> testCaseExecutions, List<TestCaseExecutionQueue> testCaseExecutionsInQueue) throws ParseException {
//        LinkedHashMap<String, TestCaseExecution> testCaseExecutionsList = new LinkedHashMap<>();
//        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//
//        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
//            String key = testCaseExecution.getBrowser() + "_"
//                    + testCaseExecution.getCountry() + "_"
//                    + testCaseExecution.getEnvironment() + "_"
//                    + testCaseExecution.getTest() + "_"
//                    + testCaseExecution.getTestCase();
//            testCaseExecutionsList.put(key, testCaseExecution);
//        }
//        for (TestCaseExecutionQueue testCaseExecutionInQueue : testCaseExecutionsInQueue) {
//            TestCaseExecution testCaseExecution = testCaseExecutionInQueueService.convertToTestCaseExecution(testCaseExecutionInQueue);
//            String key = testCaseExecution.getBrowser() + "_"
//                    + testCaseExecution.getCountry() + "_"
//                    + testCaseExecution.getEnvironment() + "_"
//                    + testCaseExecution.getTest() + "_"
//                    + testCaseExecution.getTestCase();
//            if ((testCaseExecutionsList.containsKey(key)
//                    && testCaseExecutionsList.get(key).getStart() < testCaseExecutionInQueue.getRequestDate().getTime())
//                    || !testCaseExecutionsList.containsKey(key)) {
//                testCaseExecutionsList.put(key, testCaseExecution);
//            }
//        }
//        List<TestCaseExecution> result = new ArrayList<>(testCaseExecutionsList.values());
//
//        return result;
//    }
//    private JSONObject testCaseExecutionToJSONObject(
//            TestCaseExecution testCaseExecution) throws JSONException {
//        JSONObject result = new JSONObject();
//        result.put("ID", String.valueOf(testCaseExecution.getId()));
//        result.put("Test", JavaScriptUtils.javaScriptEscape(testCaseExecution.getTest()));
//        result.put("TestCase", JavaScriptUtils.javaScriptEscape(testCaseExecution.getTestCase()));
//        result.put("Environment", JavaScriptUtils.javaScriptEscape(testCaseExecution.getEnvironment()));
//        result.put("Start", testCaseExecution.getStart());
//        result.put("End", testCaseExecution.getEnd());
//        result.put("Country", JavaScriptUtils.javaScriptEscape(testCaseExecution.getCountry()));
//        result.put("Browser", JavaScriptUtils.javaScriptEscape(testCaseExecution.getBrowser()));
//        result.put("ControlStatus", JavaScriptUtils.javaScriptEscape(testCaseExecution.getControlStatus()));
//        result.put("ControlMessage", JavaScriptUtils.javaScriptEscape(testCaseExecution.getControlMessage()));
//        result.put("Status", JavaScriptUtils.javaScriptEscape(testCaseExecution.getStatus()));
//
//        JSONArray bugs = new JSONArray();
//        if (testCaseExecution.getApplicationObj() != null && testCaseExecution.getApplicationObj().getBugTrackerUrl() != null
//                && !"".equals(testCaseExecution.getApplicationObj().getBugTrackerUrl()) && testCaseExecution.getTestCaseObj().getBugs() != null) {
//        } else {
//            bugs = testCaseExecution.getTestCaseObj().getBugs();
//        }
//        result.put("bugs", bugs);
//
//        result.put("Comment", JavaScriptUtils.javaScriptEscape(testCaseExecution.getTestCaseObj().getComment()));
//        result.put("Priority", JavaScriptUtils.javaScriptEscape(String.valueOf(testCaseExecution.getTestCaseObj().getPriority())));
//        result.put("Application", JavaScriptUtils.javaScriptEscape(testCaseExecution.getApplication()));
//        result.put("ShortDescription", testCaseExecution.getTestCaseObj().getDescription());
//
//        return result;
//    }
//    private List<TestCaseExecution> readExecutionByTagList(ApplicationContext appContext, String Tag, int startPosition, int length, String sortInformation, String searchParameter, Map<String, List<String>> individualSearch) throws ParseException, CerberusException {
//        AnswerList<TestCaseExecution> testCaseExecution;
//        AnswerList<TestCaseExecutionQueue> testCaseExecutionInQueue;
//
//        ITestCaseExecutionService testCaseExecService = appContext.getBean(ITestCaseExecutionService.class);
//
//        testCaseExecutionInQueueService = appContext.getBean(ITestCaseExecutionQueueService.class);
//        /**
//         * Get list of execution by tag, env, country, browser
//         */
//        testCaseExecution = testCaseExecService.readByTagByCriteria(Tag, startPosition, length, sortInformation, searchParameter, individualSearch);
//        List<TestCaseExecution> testCaseExecutions = testCaseExecution.getDataList();
//        /**
//         * Get list of Execution in Queue by Tag
//         */
//        testCaseExecutionInQueue = testCaseExecutionInQueueService.readByTagByCriteria(Tag, startPosition, length, sortInformation, searchParameter, individualSearch);
//        List<TestCaseExecutionQueue> testCaseExecutionsInQueue = testCaseExecutionInQueue.getDataList();
//        /**
//         * Feed hash map with execution from the two list (to get only one by
//         * test,testcase,country,env,browser)
//         */
//        testCaseExecutions = hashExecution(testCaseExecutions, testCaseExecutionsInQueue);
//        return testCaseExecutions;
//    }
    /**
     * Find Values to display for Column Filter
     *
     * @param system
     * @param test
     * @param appContext
     * @param request
     * @param columnName
     * @return
     * @throws JSONException
     */
    private AnswerItem findValuesForColumnFilter(List<String> system, String test, ApplicationContext appContext, HttpServletRequest request, String columnName) throws JSONException {
        AnswerItem<JSONObject> answer = new AnswerItem<>();
        JSONObject object = new JSONObject();
        AnswerList<String> values = new AnswerList<>();
        Map<String, List<String>> individualSearch = new HashMap<>();

        testCaseService = appContext.getBean(TestCaseService.class);
        invariantService = appContext.getBean(InvariantService.class);
        buildRevisionInvariantService = appContext.getBean(BuildRevisionInvariantService.class);
        applicationService = appContext.getBean(ApplicationService.class);

        LOG.debug(columnName);
        switch (columnName) {
            /**
             * Columns from Status
             */
            case "exe.controlStatus":
                List<String> dataList = new ArrayList<>();
                dataList.add(TestCaseExecution.CONTROLSTATUS_CA);
                dataList.add(TestCaseExecution.CONTROLSTATUS_FA);
                dataList.add(TestCaseExecution.CONTROLSTATUS_KO);
                dataList.add(TestCaseExecution.CONTROLSTATUS_NA);
                dataList.add(TestCaseExecution.CONTROLSTATUS_NE);
                dataList.add(TestCaseExecution.CONTROLSTATUS_WE);
                dataList.add(TestCaseExecution.CONTROLSTATUS_OK);
                dataList.add(TestCaseExecution.CONTROLSTATUS_PE);
                values.setDataList(dataList);
                MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "execution").replace("%OPERATION%", "SELECT"));
                values.setResultMessage(msg);
                break;
            /**
             * For columns test and testcase, get distinct values from test
             * table
             */
            case "exe.test":
            case "exe.testcase":
            case "exe.status":
                values = testCaseService.readDistinctValuesByCriteria(system, test, "", null, columnName.replace("exe.", "tec."));
                break;
            /**
             * For columns country, environment get values from invariant
             */
            case "exe.country":
            case "exe.environment":
                try {
                /**
                 *
                 */
                List<Invariant> invariantList = invariantService.readByIdName(columnName.replace("exe.", ""));
                List<String> stringResult = new ArrayList<>();
                for (Invariant inv : invariantList) {
                    stringResult.add(inv.getValue());
                }
                values.setDataList(stringResult);
                values.setTotalRows(invariantList.size());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK_GENERIC);
                values.setResultMessage(msg);

            } catch (CerberusException ex) {
                LOG.error(ex);
            }
            break;
            /**
             * For columns build, revision get values from
             * buildrevisioninvariant
             */
            case "exe.build":
            case "exe.revision":
                individualSearch = new HashMap<>();
                individualSearch.put("level", new ArrayList<>(Arrays.asList(columnName.equals("exe.build") ? "1" : "2")));
                values = buildRevisionInvariantService.readDistinctValuesByCriteria(system, "", individualSearch, "versionName");
                break;
            /**
             * For columns application get values from application
             */
            case "exe.application":
                values = applicationService.readDistinctValuesByCriteria(system, "", null, columnName.replace("exe.", ""));
                break;
            /**
             * For all other columns, get distinct values from testcaseexecution
             */
            default:
                String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
                String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "tec.test,tec.testcase,application,project,ticket,description,detailedDescription,readonly,bugtrackernewurl,deploytype,mavengroupid");
                String columnToSort[] = sColumns.split(",");

                List<String> individualLike = new ArrayList<>(Arrays.asList(ParameterParserUtil.parseStringParam(request.getParameter("sLike"), "").split(",")));

                individualSearch = new HashMap<>();
                for (int a = 0; a < columnToSort.length; a++) {
                    if (null != request.getParameter("sSearch_" + a) && !request.getParameter("sSearch_" + a).isEmpty()) {
                        List<String> search = new ArrayList<>(Arrays.asList(request.getParameter("sSearch_" + a).split(",")));
                        if (individualLike.contains(columnToSort[a])) {
                            individualSearch.put(columnToSort[a] + ":like", search);
                        } else {
                            individualSearch.put(columnToSort[a], search);
                        }
                    }
                }
                values = testCaseExecutionService.readDistinctValuesByCriteria(system, test, searchParameter, individualSearch, columnName);
        }

        object.put("distinctValues", values.getDataList());

        answer.setItem(object);
        answer.setResultMessage(values.getResultMessage());
        return answer;
    }

}
