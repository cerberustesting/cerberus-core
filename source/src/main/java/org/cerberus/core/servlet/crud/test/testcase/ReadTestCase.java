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
package org.cerberus.core.servlet.crud.test.testcase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.service.ILabelService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author cerberus
 */
@WebServlet(name = "ReadTestCase", urlPatterns = {"/ReadTestCase"})
public class ReadTestCase extends AbstractCrudTestCase {

    @Autowired
    private ITestCaseService testCaseService;
    @Autowired
    private ILabelService labelService;

    private static final Logger LOG = LogManager.getLogger(ReadTestCase.class);

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        // Default message to unexpected error.
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        /**
         * Parsing and securing all required parameters.
         */
        int sEcho = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("sEcho"), "0"));
        String test = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("test"), "");
        List<String> system = ParameterParserUtil.parseListParamAndDeleteEmptyValue(request.getParameterValues("system"), Arrays.asList("DEFAULT"), "UTF-8");
        String testCase = ParameterParserUtil.parseStringParam(request.getParameter("testCase"), null);
        String campaign = ParameterParserUtil.parseStringParam(request.getParameter("campaign"), "");
        boolean getMaxTC = ParameterParserUtil.parseBooleanParam(request.getParameter("getMaxTC"), false);
        boolean filter = ParameterParserUtil.parseBooleanParam(request.getParameter("filter"), false);
        boolean withSteps = ParameterParserUtil.parseBooleanParam(request.getParameter("withSteps"), false);
        String columnName = ParameterParserUtil.parseStringParam(request.getParameter("columnName"), "");

        // Init Answer with potencial error from Parsing parameter.
        AnswerItem<JSONObject> answer = new AnswerItem<>(msg);
        JSONObject jsonResponse = new JSONObject();

        try {
            if (!StringUtil.isEmptyOrNull(test) && testCase != null) {
                answer = findTestCaseByTestTestCase(test, testCase, request, withSteps);
            } else if (!StringUtil.isEmptyOrNull(test) && getMaxTC) {
                String max = testCaseService.getNextAvailableTestcaseId(test);
                jsonResponse.put("nextAvailableTestcaseId", max);
                answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
            } else if (filter) {
                answer = findTestCaseByVarious(request);
            } else if (!StringUtil.isEmptyOrNull(campaign)) {
                answer = findTestCaseByCampaign(campaign);
            } else if (!StringUtil.isEmptyOrNull(columnName)) {
                //If columnName is present, then return the distinct value of this column.
                answer = findDistinctValuesOfColumn(system, test, request, columnName);
            } else {
                // Page TestCaseList
                answer = findTestCaseByTest(system, test, request);
            }

            if (!getMaxTC) {
                jsonResponse = (answer.getItem() == null) ? new JSONObject() : answer.getItem();
            }

            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());
            jsonResponse.put("sEcho", sEcho);

            response.getWriter().print(jsonResponse.toString());
        } catch (JSONException e) {
            LOG.warn(e, e);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        } catch (CerberusException ex) {
            LOG.error(ex, ex);
            // TODO return to the gui
        } catch (Exception ex) {
            LOG.error(ex, ex);
            // TODO return to the gui
        }
    }

    private AnswerItem<JSONObject> findTestCaseByTest(List<String> system, String test, HttpServletRequest request) throws JSONException, CerberusException {
        AnswerItem<JSONObject> answer = new AnswerItem<>();
        JSONObject object = new JSONObject();

        boolean isCalledFromListPage = (request.getParameter("sColumns") != null);
        int startPosition = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayStart"), "0"));
        int length = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayLength"), "0"));

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "tec.test,tec.testcase,tec.application,project,ticket,description,detailedDescription,readonly,bugtrackernewurl,deploytype,mavengroupid");
        String columnToSort[] = sColumns.split(",");
        List<String> individualLike = new ArrayList<>(Arrays.asList(ParameterParserUtil.parseStringParam(request.getParameter("sLike"), "").split(",")));

        StringBuilder sortInformation = getSortingInformation(columnToSort, request);
        Map<String, List<String>> individualSearch = getIndivualSearch(request, columnToSort, individualLike);
        AnswerList<TestCase> testCases = testCaseService.findTestCasesByTestByCriteriaWithDependencies(system, test, startPosition, length, sortInformation.toString(), searchParameter, individualSearch, isCalledFromListPage);

        JSONArray jsonArray = new JSONArray();

        for (TestCase testCase : testCases.getDataList()) {
            JSONObject jsonTestCase = testCase.toJson();
            jsonTestCase.put("hasPermissionsUpdate", testCaseService.hasPermissionsUpdate(testCase, request));
            jsonTestCase.put("hasPermissionsDelete", testCaseService.hasPermissionsDelete(testCase, request));
            jsonArray.put(jsonTestCase);
        }

        object.put("hasPermissionsCreate", testCaseService.hasPermissionsCreate(null, request));
        object.put("contentTable", jsonArray);
        object.put("iTotalRecords", testCases.getTotalRows());
        object.put("iTotalDisplayRecords", testCases.getTotalRows());

        answer.setItem(object);
        answer.setResultMessage(testCases.getResultMessage());
        return answer;
    }

    private AnswerItem<JSONObject> findTestCaseByTestTestCase(String test, String testCase, HttpServletRequest request, boolean withSteps) throws JSONException, CerberusException {
        AnswerItem<JSONObject> answerItem = new AnswerItem<>();
        JSONObject jsonResponse = new JSONObject();

        AnswerItem<TestCase> answerTestCase;
        answerTestCase = testCaseService.findTestCaseByKeyWithDependencies(test, testCase, withSteps);
        if (answerTestCase.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && answerTestCase.getItem() != null) {
            TestCase tc = answerTestCase.getItem();
            if (withSteps) {
                jsonResponse.put("hasPermissionsStepLibrary", (request.isUserInRole("TestStepLibrary")));
            }
            jsonResponse.put("hasPermissionsUpdate", testCaseService.hasPermissionsUpdate(tc, request));
            jsonResponse.put("hasPermissionsDelete", testCaseService.hasPermissionsDelete(tc, request));
            jsonResponse.put("contentTable", new JSONArray().put(tc.toJson()));
        } else {
            answerItem.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_NOT_FOUND_OR_NOT_AUTHORIZE));
            return answerItem;
        }

        answerItem.setItem(jsonResponse);
        answerItem.setResultMessage(answerTestCase.getResultMessage());

        LOG.debug(answerItem.getItem());
        return answerItem;
    }

    private AnswerItem<JSONObject> findTestCaseByVarious(HttpServletRequest request) throws JSONException {
        AnswerItem<JSONObject> item = new AnswerItem<>();
        JSONObject object = new JSONObject();
        JSONArray dataArray = new JSONArray();

        String[] test = request.getParameterValues("test");
        String[] idProject = request.getParameterValues("project");
        String[] app = request.getParameterValues("application");
        String[] creator = request.getParameterValues("creator");
        String[] implementer = request.getParameterValues("implementer");
        String[] system = request.getParameterValues("system");
        String[] campaign = request.getParameterValues("campaign");
        String[] priority = request.getParameterValues("priority");
        String[] type = request.getParameterValues("type");
        String[] status = request.getParameterValues("status");
        String[] labelid = request.getParameterValues("labelid");
        List<Integer> labels = new ArrayList<>();
        if (labelid != null) {
            for (int i = 0; i < labelid.length; i++) {
                String string = labelid[i];
                labels.add(Integer.valueOf(string));
            }
            labels = labelService.enrichWithChild(labels);
        }
        int length = ParameterParserUtil.parseIntegerParam(request.getParameter("length"), -1);

        AnswerList<TestCase> answer = testCaseService.readByVarious(test, app, creator, implementer, system, campaign, labels, priority, type, status, length);

        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            for (TestCase tc : answer.getDataList()) {
                JSONObject value = tc.toJson();
                dataArray.put(value);
            }
        }

        object.put("contentTable", dataArray);
        item.setItem(object);
        item.setResultMessage(answer.getResultMessage());
        return item;
    }

    private AnswerItem<JSONObject> findTestCaseByCampaign(String campaign) throws JSONException {
        AnswerItem<JSONObject> answerItem = new AnswerItem<>();
        JSONObject jsonResponse = new JSONObject();
        JSONArray dataArray = new JSONArray();

        AnswerList<TestCase> testCases = testCaseService.findTestCaseByCampaign(campaign);

        if (testCases.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            for (TestCase testCase : testCases.getDataList()) {
                JSONObject jsonTestCase = testCase.toJson();
                dataArray.put(jsonTestCase);
            }
        }

        jsonResponse.put("contentTable", dataArray);
        answerItem.setItem(jsonResponse);
        answerItem.setResultMessage(testCases.getResultMessage());
        return answerItem;
    }

    private AnswerItem<JSONObject> findDistinctValuesOfColumn(List<String> system, String test, HttpServletRequest request, String columnName) throws JSONException {
        AnswerItem<JSONObject> answerItem = new AnswerItem<>();
        JSONObject jsonResponse = new JSONObject();

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "tec.test,tec.testcase,application,project,ticket,description,detailedDescription,readonly,bugtrackernewurl,deploytype,mavengroupid");
        String columnToSort[] = sColumns.split(",");

        List<String> individualLike = new ArrayList<>(Arrays.asList(ParameterParserUtil.parseStringParam(request.getParameter("sLike"), "").split(",")));
        Map<String, List<String>> individualSearch = getIndivualSearch(request, columnToSort, individualLike);
        AnswerList testCaseList = testCaseService.readDistinctValuesByCriteria(system, test, searchParameter, individualSearch, columnName);

        jsonResponse.put("distinctValues", testCaseList.getDataList());
        answerItem.setItem(jsonResponse);
        answerItem.setResultMessage(testCaseList.getResultMessage());
        return answerItem;
    }

    private StringBuilder getSortingInformation(String columnToSort[], HttpServletRequest request) {
        int numberOfColumnToSort = Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortingCols"), "1"));
        int columnToSortParameter = 0;
        String sort = "asc";
        StringBuilder sortInformation = new StringBuilder();

        for (int i = 0; i < numberOfColumnToSort; i++) {
            columnToSortParameter = Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortCol_" + i), "0"));
            sort = ParameterParserUtil.parseStringParam(request.getParameter("sSortDir_" + i), "asc");
            String columnName = columnToSort[columnToSortParameter];
            sortInformation.append(columnName).append(" ").append(sort);

            if (i != numberOfColumnToSort - 1) {
                sortInformation.append(" , ");
            }
        }
        return sortInformation;
    }

    private Map<String, List<String>> getIndivualSearch(HttpServletRequest request, String columnToSort[], List<String> individualLike) {
        Map<String, List<String>> individualSearch = new HashMap<>();

        for (int i = 0; i < columnToSort.length; i++) {
            if (null != request.getParameter("sSearch_" + i) && !request.getParameter("sSearch_" + i).isEmpty()) {
                List<String> search = new ArrayList<>(Arrays.asList(request.getParameter("sSearch_" + i).split(",")));
                if (individualLike.contains(columnToSort[i])) {
                    individualSearch.put(columnToSort[i] + ":like", search);
                } else {
                    individualSearch.put(columnToSort[i], search);
                }
            }
        }
        return individualSearch;
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
