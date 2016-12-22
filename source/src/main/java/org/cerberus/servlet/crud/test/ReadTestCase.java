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
package org.cerberus.servlet.crud.test;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.cerberus.crud.entity.CampaignContent;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseLabel;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.crud.service.ICampaignContentService;
import org.cerberus.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.crud.service.ITestCaseCountryService;
import org.cerberus.crud.service.ITestCaseLabelService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.ITestCaseStepActionControlService;
import org.cerberus.crud.service.ITestCaseStepActionService;
import org.cerberus.crud.service.ITestCaseStepService;
import org.cerberus.crud.service.impl.TestCaseCountryService;
import org.cerberus.crud.service.impl.TestCaseLabelService;
import org.cerberus.crud.service.impl.TestCaseService;
import org.cerberus.crud.service.impl.TestCaseStepActionControlService;
import org.cerberus.crud.service.impl.TestCaseStepActionService;
import org.cerberus.crud.service.impl.TestCaseStepService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.AnswerUtil;
import org.cerberus.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author cerberus
 */
@WebServlet(name = "ReadTestCase", urlPatterns = {"/ReadTestCase"})
public class ReadTestCase extends HttpServlet {

    private ITestCaseService testCaseService;
    private ITestCaseCountryService testCaseCountryService;
    private ITestCaseStepService testCaseStepService;
    private ITestCaseStepActionService testCaseStepActionService;
    private ITestCaseStepActionControlService testCaseStepActionControlService;
    private ITestCaseLabelService testCaseLabelService;

    private static final Logger LOG = Logger.getLogger(ReadTestCase.class);

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int sEcho = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("sEcho"), "0"));
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

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
        String test = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("test"), "");
        String system = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("system"), "");
        String testCase = ParameterParserUtil.parseStringParam(request.getParameter("testCase"), null);
        String campaign = ParameterParserUtil.parseStringParam(request.getParameter("campaign"), "");
        boolean getMaxTC = ParameterParserUtil.parseBooleanParam(request.getParameter("getMaxTC"), false);
        boolean filter = ParameterParserUtil.parseBooleanParam(request.getParameter("filter"), false);
        boolean withStep = ParameterParserUtil.parseBooleanParam(request.getParameter("withStep"), false);
        String columnName = ParameterParserUtil.parseStringParam(request.getParameter("columnName"), "");

        // Global boolean on the servlet that define if the user has permition to edit and delete object.
        boolean userHasPermissions = request.isUserInRole("TestAdmin");

        // Init Answer with potencial error from Parsing parameter.
        AnswerItem answer = new AnswerItem(msg);

        try {
            JSONObject jsonResponse = new JSONObject();
            if (!Strings.isNullOrEmpty(test) && testCase != null && !withStep) {
                answer = findTestCaseByTestTestCase(test, testCase, appContext, request);
                jsonResponse = (JSONObject) answer.getItem();
            } else if (!Strings.isNullOrEmpty(test) && testCase != null && withStep) {
                answer = findTestCaseWithStep(appContext, test, testCase);
                jsonResponse = (JSONObject) answer.getItem();
            } else if (!Strings.isNullOrEmpty(test) && getMaxTC) {
                testCaseService = appContext.getBean(TestCaseService.class);
                String max = testCaseService.getMaxNumberTestCase(test);
                if (max == null) {
                    max = "0";
                }
                jsonResponse.put("maxTestCase", Integer.valueOf(max));
                answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
            } else if (filter) {
                answer = findTestCaseByVariousCriteria(appContext, request);
                jsonResponse = (JSONObject) answer.getItem();
            } else if (!Strings.isNullOrEmpty(campaign)) {
                answer = findTestCaseByCampaign(appContext, campaign);
                jsonResponse = (JSONObject) answer.getItem();
            } else if (!Strings.isNullOrEmpty(columnName)) {
                //If columnName is present, then return the distinct value of this column.
                answer = findDistinctValuesOfColumn(system, test, appContext, request, columnName);
                jsonResponse = (JSONObject) answer.getItem();
            } else {
                answer = findTestCaseByTest(system, test, appContext, request);
                jsonResponse = (JSONObject) answer.getItem();
            }

            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());
            jsonResponse.put("sEcho", sEcho);

            response.getWriter().print(jsonResponse.toString());
        } catch (JSONException e) {
            org.apache.log4j.Logger.getLogger(ReadTestCase.class.getName()).log(org.apache.log4j.Level.ERROR, null, e);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
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

    private AnswerItem findTestCaseByTest(String system, String test, ApplicationContext appContext, HttpServletRequest request) throws JSONException {
        AnswerItem answer = new AnswerItem();
        JSONObject object = new JSONObject();

        testCaseService = appContext.getBean(TestCaseService.class);
        testCaseCountryService = appContext.getBean(TestCaseCountryService.class);
        testCaseLabelService = appContext.getBean(ITestCaseLabelService.class);

        int startPosition = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayStart"), "0"));
        int length = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayLength"), "0"));

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "tec.test,tec.testcase,tec.application,project,ticket,description,behaviororvalueexpected,readonly,bugtrackernewurl,deploytype,mavengroupid");
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

        AnswerList testCaseList = testCaseService.readByTestByCriteria(system, test, startPosition, length, sortInformation.toString(), searchParameter, individualSearch);

        AnswerList testCaseCountryList = testCaseCountryService.readByTestTestCase(system, test, null);
        /**
         * Find the list of labels
         */
        AnswerList testCaseLabelList = testCaseLabelService.readByTestTestCase(test, null);

        LinkedHashMap<String, JSONObject> testCaseWithCountry = new LinkedHashMap();
        for (TestCaseCountry country : (List<TestCaseCountry>) testCaseCountryList.getDataList()) {
            String key = country.getTest() + "_" + country.getTestCase();

            if (testCaseWithCountry.containsKey(key)) {
                testCaseWithCountry.get(key).put(country.getCountry(), country.getCountry());
            } else {
                testCaseWithCountry.put(key, new JSONObject().put(country.getCountry(), country.getCountry()));
            }
        }

        /**
         * Iterate on the label retrieved and generate HashMap based on the key
         * Test_TestCase
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

        JSONArray jsonArray = new JSONArray();
        boolean isTest = request.isUserInRole("Test");
        boolean isTestAdmin = request.isUserInRole("TestAdmin");
        if (testCaseList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            for (TestCase testCase : (List<TestCase>) testCaseList.getDataList()) {
                String key = testCase.getTest() + "_" + testCase.getTestCase();
                JSONObject value = convertToJSONObject(testCase);
                value.put("hasPermissionsDelete", isTestAdmin);
                if (testCase.getStatus().equalsIgnoreCase("WORKING")) { // If testcase is WORKING only TestAdmin can update it
                    value.put("hasPermissionsUpdate", isTestAdmin);
                } else {
                    value.put("hasPermissionsUpdate", isTest);
                }
                value.put("countryList", testCaseWithCountry.get(key));
                value.put("labels", testCaseWithLabel.get(key));

                jsonArray.put(value);
            }
        }

        object.put("hasPermissions", isTest);
        object.put("hasPermissionsCreate", isTest);
        object.put("hasPermissionsDelete", isTestAdmin);
        object.put("contentTable", jsonArray);
        object.put("iTotalRecords", testCaseList.getTotalRows());
        object.put("iTotalDisplayRecords", testCaseList.getTotalRows());

        answer.setItem(object);
        answer.setResultMessage(testCaseList.getResultMessage());
        return answer;
    }

    private AnswerItem findTestCaseByTestTestCase(String test, String testCase, ApplicationContext appContext, HttpServletRequest request) throws JSONException {
        AnswerItem item = new AnswerItem();
        JSONObject object = new JSONObject();
        boolean hasPermissionsUpdate = false;
        boolean isTest = request.isUserInRole("Test");
        boolean isTestAdmin = request.isUserInRole("TestAdmin");

        testCaseService = appContext.getBean(TestCaseService.class);
        testCaseCountryService = appContext.getBean(TestCaseCountryService.class);
        testCaseLabelService = appContext.getBean(TestCaseLabelService.class);

        //finds the project
        AnswerItem answerTestCase = testCaseService.readByKey(test, testCase);

        if (answerTestCase.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && answerTestCase.getItem() != null) {
            //if the service returns an OK message then we can get the item and convert it to JSONformat
            TestCase tc = (TestCase) answerTestCase.getItem();
            JSONObject response = convertToJSONObject(tc);

            // Country List feed.
            JSONArray countryArray = new JSONArray();
            AnswerList answerTestCaseCountryList = testCaseCountryService.readByTestTestCase(null, test, testCase);
            for (TestCaseCountry country : (List<TestCaseCountry>) answerTestCaseCountryList.getDataList()) {
                countryArray.put(convertToJSONObject(country));
            }
            response.put("countryList", countryArray);

            // Label List feed.
            JSONArray labelArray = new JSONArray();
            AnswerList answerTestCaseLabelList = testCaseLabelService.readByTestTestCase(test, testCase);
            for (TestCaseLabel label : (List<TestCaseLabel>) answerTestCaseLabelList.getDataList()) {
                labelArray.put(convertToJSONObject(label));
            }
            response.put("labelList", labelArray);

            object.put("contentTable", response);

            // Access right calculation.
            if (tc.getStatus().equalsIgnoreCase("WORKING")) { // If testcase is WORKING only TestAdmin can update it
                hasPermissionsUpdate = isTestAdmin;
            } else {
                hasPermissionsUpdate = isTest;
            }
            object.put("hasPermissionsUpdate", hasPermissionsUpdate);

        }

        item.setItem(object);
        item.setResultMessage(answerTestCase.getResultMessage());

        return item;
    }

    private AnswerItem findTestCaseByVariousCriteria(ApplicationContext appContext, HttpServletRequest request) throws JSONException {
        AnswerItem item = new AnswerItem();
        JSONObject object = new JSONObject();
        JSONArray dataArray = new JSONArray();

        String[] test = request.getParameterValues("test");
        String[] idProject = request.getParameterValues("project");
        String[] app = request.getParameterValues("application");
        String[] creator = request.getParameterValues("creator");
        String[] implementer = request.getParameterValues("implementer");
        String[] system = request.getParameterValues("system");
        String[] testBattery = request.getParameterValues("testBattery");
        String[] campaign = request.getParameterValues("campaign");
        String[] priority = request.getParameterValues("priority");
        String[] group = request.getParameterValues("group");
        String[] status = request.getParameterValues("status");
        int length = ParameterParserUtil.parseIntegerParam(request.getParameter("length"), -1);

        testCaseService = appContext.getBean(TestCaseService.class);
        AnswerList answer = testCaseService.readByVariousCriteria(test, idProject, app, creator, implementer, system, testBattery, campaign, priority, group, status, length);

        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            for (TestCase tc : (List<TestCase>) answer.getDataList()) {
                dataArray.put(convertToJSONObject(tc));
            }
        }

        object.put("contentTable", dataArray);
        item.setItem(object);
        item.setResultMessage(answer.getResultMessage());
        return item;
    }

    private AnswerItem findTestCaseByCampaign(ApplicationContext appContext, String campaign) throws JSONException {
        AnswerItem answer = new AnswerItem();
        JSONObject jsonResponse = new JSONObject();
        JSONArray dataArray = new JSONArray();

        ICampaignContentService campaignContentService = appContext.getBean(ICampaignContentService.class);
        testCaseService = appContext.getBean(TestCaseService.class);

        AnswerList testBatteryAnswer = campaignContentService.readByCampaignByCriteria(campaign, 0, 0, "testbattery", "asc", "", "");
        List<CampaignContent> testBatteryList = testBatteryAnswer.getDataList();
        String[] testBattery = new String[testBatteryList.size()];

        for (int index = 0; index < testBatteryList.size(); index++) {
            testBattery[index] = testBatteryList.get(index).getTestbattery();
        }

        AnswerList resp = testCaseService.readByVariousCriteria(null, null, null, null, null, null, testBattery, null, null, null, null, -1);

        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            for (TestCase tc : (List<TestCase>) resp.getDataList()) {
                dataArray.put(convertToJSONObject(tc));
            }
        }

        jsonResponse.put("contentTable", dataArray);
        answer.setItem(jsonResponse);
        answer.setResultMessage(resp.getResultMessage());
        return answer;
    }

    private AnswerItem findTestCaseWithStep(ApplicationContext appContext, String test, String testCase) throws JSONException {
        AnswerItem item = new AnswerItem();
        JSONObject object = new JSONObject();
        HashMap<String, JSONObject> hashProp = new HashMap<String, JSONObject>();
        JSONObject jsonResponse = new JSONObject();

        testCaseService = appContext.getBean(TestCaseService.class);
        testCaseCountryService = appContext.getBean(TestCaseCountryService.class);
        testCaseStepService = appContext.getBean(TestCaseStepService.class);
        testCaseStepActionService = appContext.getBean(TestCaseStepActionService.class);
        testCaseStepActionControlService = appContext.getBean(TestCaseStepActionControlService.class);
        ITestCaseCountryPropertiesService testCaseCountryPropertiesService = appContext.getBean(ITestCaseCountryPropertiesService.class);

        //finds the testcase     
        AnswerItem answer = testCaseService.readByKey(test, testCase);

        AnswerList testCaseCountryList = testCaseCountryService.readByTestTestCase(null, test, testCase);
        AnswerList testCaseStepList = testCaseStepService.readByTestTestCase(test, testCase);
        AnswerList testCaseStepActionList = testCaseStepActionService.readByTestTestCase(test, testCase);
        AnswerList testCaseStepActionControlList = testCaseStepActionControlService.readByTestTestCase(test, testCase);

        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item and convert it to JSONformat
            TestCase tc = (TestCase) answer.getItem();
            object = convertToJSONObject(tc);
            object.put("countryList", new JSONObject());
        }

        for (TestCaseCountry country : (List<TestCaseCountry>) testCaseCountryList.getDataList()) {
            object.getJSONObject("countryList").put(country.getCountry(), country.getCountry());
        }

        JSONArray stepList = new JSONArray();
        Gson gson = new Gson();
        for (TestCaseStep step : (List<TestCaseStep>) testCaseStepList.getDataList()) {
            JSONObject jsonStep = new JSONObject(gson.toJson(step));

            //Fill JSON with step info
            jsonStep.put("objType", "step");
            //Add a JSON array for Action List from this step
            jsonStep.put("actionList", new JSONArray());
            //Fill action List

            if (step.getUseStep().equals("Y")) {
                //If this step is imported from library, we call the service to retrieve actions
                TestCaseStep usedStep = testCaseStepService.findTestCaseStep(step.getUseStepTest(), step.getUseStepTestCase(), step.getUseStepStep());
                List<TestCaseStepAction> actionList = testCaseStepActionService.getListOfAction(step.getUseStepTest(), step.getUseStepTestCase(), step.getUseStepStep());
                List<TestCaseStepActionControl> controlList = testCaseStepActionControlService.findControlByTestTestCaseStep(step.getUseStepTest(), step.getUseStepTestCase(), step.getUseStepStep());
                List<TestCaseCountryProperties> properties = testCaseCountryPropertiesService.findDistinctPropertiesOfTestCase(step.getUseStepTest(), step.getUseStepTestCase());

                // Get the used step sort
                jsonStep.put("useStepStepSort", usedStep.getSort());

                //retrieve the inherited properties
                for (TestCaseCountryProperties prop : properties) {
                    JSONObject propertyFound = new JSONObject();

                    propertyFound.put("fromTest", prop.getTest());
                    propertyFound.put("fromTestCase", prop.getTestCase());
                    propertyFound.put("property", prop.getProperty());
                    propertyFound.put("description", prop.getDescription());
                    propertyFound.put("type", prop.getType());
                    propertyFound.put("database", prop.getDatabase());
                    propertyFound.put("value1", prop.getValue1());
                    propertyFound.put("value2", prop.getValue2());
                    propertyFound.put("length", prop.getLength());
                    propertyFound.put("rowLimit", prop.getRowLimit());
                    propertyFound.put("nature", prop.getNature());
                    List<String> countriesSelected = testCaseCountryPropertiesService.findCountryByProperty(prop);
                    JSONArray countries = new JSONArray();
                    for (String country : countriesSelected) {
                        countries.put(country);
                    }
                    propertyFound.put("country", countries);

                    hashProp.put(prop.getTest() + "_" + prop.getTestCase() + "_" + prop.getProperty(), propertyFound);
                }

                for (TestCaseStepAction action : actionList) {

                    if (action.getStep() == step.getUseStepStep()) {
                        JSONObject jsonAction = new JSONObject(gson.toJson(action));

                        jsonAction.put("objType", "action");

                        jsonAction.put("controlList", new JSONArray());
                        //We fill the action with the corresponding controls
                        for (TestCaseStepActionControl control : controlList) {

                            if (control.getStep() == step.getUseStepStep()
                                    && control.getSequence() == action.getSequence()) {
                                JSONObject jsonControl = new JSONObject(gson.toJson(control));

                                jsonControl.put("objType", "control");

                                jsonAction.getJSONArray("controlList").put(jsonControl);
                            }
                        }
                        //we put the action in the actionList for the corresponding step
                        jsonStep.getJSONArray("actionList").put(jsonAction);
                    }
                }
            } else {
                //else, we fill the actionList with the action from this step
                for (TestCaseStepAction action : (List<TestCaseStepAction>) testCaseStepActionList.getDataList()) {

                    if (action.getStep() == step.getStep()) {
                        JSONObject jsonAction = new JSONObject(gson.toJson(action));

                        jsonAction.put("objType", "action");
                        jsonAction.put("controlList", new JSONArray());
                        //We fill the action with the corresponding controls
                        for (TestCaseStepActionControl control : (List<TestCaseStepActionControl>) testCaseStepActionControlList.getDataList()) {

                            if (control.getStep() == step.getStep()
                                    && control.getSequence() == action.getSequence()) {
                                JSONObject jsonControl = new JSONObject(gson.toJson(control));
                                jsonControl.put("objType", "control");

                                jsonAction.getJSONArray("controlList").put(jsonControl);
                            }
                        }
                        //we put the action in the actionList for the corresponding step
                        jsonStep.getJSONArray("actionList").put(jsonAction);
                    }
                }
            }
            stepList.put(jsonStep);
        }

        jsonResponse.put("info", object);
        jsonResponse.put("stepList", stepList);
        jsonResponse.put("inheritedProp", hashProp.values());

        item.setItem(jsonResponse);
        item.setResultMessage(answer.getResultMessage());

        return item;
    }

    private JSONObject convertToJSONObject(TestCase object) throws JSONException {
        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(object));
        return result;
    }

    private JSONObject convertToJSONObject(TestCaseCountry object) throws JSONException {
        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(object));
        return result;
    }

    private JSONObject convertToJSONObject(TestCaseLabel object) throws JSONException {
        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(object));
        return result;
    }

    private AnswerItem findDistinctValuesOfColumn(String system, String test, ApplicationContext appContext, HttpServletRequest request, String columnName) throws JSONException {
        AnswerItem answer = new AnswerItem();
        JSONObject object = new JSONObject();

        testCaseService = appContext.getBean(TestCaseService.class);
        testCaseCountryService = appContext.getBean(TestCaseCountryService.class);

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "tec.test,tec.testcase,application,project,ticket,description,behaviororvalueexpected,readonly,bugtrackernewurl,deploytype,mavengroupid");
        String columnToSort[] = sColumns.split(",");

        Map<String, List<String>> individualSearch = new HashMap<>();
        for (int a = 0; a < columnToSort.length; a++) {
            if (null != request.getParameter("sSearch_" + a) && !request.getParameter("sSearch_" + a).isEmpty()) {
                List<String> search = new ArrayList(Arrays.asList(request.getParameter("sSearch_" + a).split(",")));
                individualSearch.put(columnToSort[a], search);
            }
        }

        AnswerList testCaseList = testCaseService.readDistinctValuesByCriteria(system, test, searchParameter, individualSearch, columnName);

        object.put("distinctValues", testCaseList.getDataList());

        answer.setItem(object);
        answer.setResultMessage(testCaseList.getResultMessage());
        return answer;
    }

}
