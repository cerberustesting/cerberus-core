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
package org.cerberus.servlet.crud.test.testcase;

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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.*;
import org.cerberus.crud.service.*;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
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
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author cerberus
 */
@WebServlet(name = "ReadTestCase", urlPatterns = {"/ReadTestCase"})
public class ReadTestCase extends AbstractCrudTestCase {

    @Autowired
    private ITestCaseService testCaseService;
    @Autowired
    private ITestCaseCountryService testCaseCountryService;
    @Autowired
    private ITestCaseDepService testCaseDepService;
    @Autowired
    private ITestCaseStepService testCaseStepService;
    @Autowired
    private ITestCaseStepActionService testCaseStepActionService;
    @Autowired
    private ITestCaseStepActionControlService testCaseStepActionControlService;
    @Autowired
    private ITestCaseLabelService testCaseLabelService;
    @Autowired
    private ICampaignParameterService campaignParameterService;
    @Autowired
    private ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
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
        int sEcho = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("sEcho"), "0"));
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
        List<String> system = ParameterParserUtil.parseListParamAndDecodeAndDeleteEmptyValue(request.getParameterValues("system"), Arrays.asList("DEFAULT"), "UTF-8");
        String testCase = ParameterParserUtil.parseStringParam(request.getParameter("testCase"), null);
        String campaign = ParameterParserUtil.parseStringParam(request.getParameter("campaign"), "");
        boolean getMaxTC = ParameterParserUtil.parseBooleanParam(request.getParameter("getMaxTC"), false);
        boolean filter = ParameterParserUtil.parseBooleanParam(request.getParameter("filter"), false);
        boolean withSteps = ParameterParserUtil.parseBooleanParam(request.getParameter("withSteps"), false);
        String columnName = ParameterParserUtil.parseStringParam(request.getParameter("columnName"), "");

        // Global boolean on the servlet that define if the user has permition to edit and delete object.
        boolean userHasPermissions = request.isUserInRole("TestAdmin");

        // Init Answer with potencial error from Parsing parameter.
        AnswerItem answer = new AnswerItem<>(msg);
        JSONObject jsonResponse = new JSONObject();

        try {
            if (!Strings.isNullOrEmpty(test) && testCase != null) {
                answer = findTestCaseByTestTestCase(test, testCase, request, withSteps);
                jsonResponse = (JSONObject) answer.getItem();
            } else if (!Strings.isNullOrEmpty(test) && getMaxTC) {
                String max = testCaseService.getMaxNumberTestCase(test);
                if (max == null) {
                    max = "0";
                }
                jsonResponse.put("maxTestCase", Integer.valueOf(max));
                answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
            } else if (filter) {
                answer = findTestCaseByVarious(request);
                jsonResponse = (JSONObject) answer.getItem();
            } else if (!Strings.isNullOrEmpty(campaign)) {
                answer = findTestCaseByCampaign(campaign);
                jsonResponse = (JSONObject) answer.getItem();
            } else if (!Strings.isNullOrEmpty(columnName)) {
                //If columnName is present, then return the distinct value of this column.
                answer = findDistinctValuesOfColumn(system, test, request, columnName);
                jsonResponse = (JSONObject) answer.getItem();
            } else { // Page TestCaseList
                answer = findTestCaseByTest(system, test, request);
                jsonResponse = (JSONObject) answer.getItem();
            }

            if (jsonResponse == null) {
                jsonResponse = new JSONObject();
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

    private AnswerItem<JSONObject> findTestCaseByTest(List<String> system, String test, HttpServletRequest request) throws JSONException, CerberusException {
        AnswerItem<JSONObject> answer = new AnswerItem<>();
        JSONObject object = new JSONObject();

        int startPosition = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayStart"), "0"));
        int length = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayLength"), "0"));

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "tec.test,tec.testcase,tec.application,project,ticket,description,detailedDescription,readonly,bugtrackernewurl,deploytype,mavengroupid");
        String columnToSort[] = sColumns.split(",");
        List<String> individualLike = new ArrayList<>(Arrays.asList(ParameterParserUtil.parseStringParam(request.getParameter("sLike"), "").split(",")));

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
                List<String> search = new ArrayList<>(Arrays.asList(request.getParameter("sSearch_" + a).split(",")));
                if (individualLike.contains(columnToSort[a])) {
                    individualSearch.put(columnToSort[a] + ":like", search);
                } else {
                    individualSearch.put(columnToSort[a], search);
                }
            }
        }
        AnswerList<TestCase> testCaseList = testCaseService.readByTestByCriteria(system, test, startPosition, length, sortInformation.toString(), searchParameter, individualSearch);
        JSONArray jsonArray = new JSONArray();
        if (testCaseList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values

            if (testCaseList.getDataList().size() > 0) {

                /**
                 * Find the list of countries
                 */
                AnswerList<TestCaseCountry> testCaseCountries = testCaseCountryService.readByTestTestCase(system, test, null, testCaseList.getDataList());
                /**
                 * Iterate on the country retrieved and generate HashMap based
                 * on the key Test_TestCase
                 */
                LinkedHashMap<String, JSONArray> testCaseWithCountry = new LinkedHashMap<>();
                for (TestCaseCountry country : (List<TestCaseCountry>) testCaseCountries.getDataList()) {
                    String key = country.getTest() + "_" + country.getTestCase();

                    if (testCaseWithCountry.containsKey(key)) {
                        testCaseWithCountry.get(key).put(convertToJSONObject(country));
                    } else {
                        testCaseWithCountry.put(key, new JSONArray().put(convertToJSONObject(country)));
                    }
                }

                /**
                 * find the list of dependencies
                 */
                List<TestCaseDep> testCaseDependencies = testCaseDepService.readByTestAndTestCase(testCaseList.getDataList());
                LinkedHashMap<String, JSONArray> testCaseWithDependencies = new LinkedHashMap<>();
                for (TestCaseDep testCaseDependency : testCaseDependencies) {
                    String key = testCaseDependency.getTest() + "_" + testCaseDependency.getTestCase();

                    JSONObject jo = convertToJSONObject(testCaseDependency);

                    if (testCaseWithDependencies.containsKey(key)) {
                        testCaseWithDependencies.get(key).put(jo);
                    } else {
                        testCaseWithDependencies.put(key, new JSONArray().put(jo));
                    }
                }

                /**
                 * Find the list of labels
                 */
                AnswerList<TestCaseLabel> testCaseLabelList = testCaseLabelService.readByTestTestCase(test, null, testCaseList.getDataList());
                /**
                 * Iterate on the label retrieved and generate HashMap based on
                 * the key Test_TestCase
                 */
                LinkedHashMap<String, JSONArray> testCaseWithLabel = new LinkedHashMap<>();
                LinkedHashMap<String, JSONArray> testCaseWithLabelSticker = new LinkedHashMap<>();
                LinkedHashMap<String, JSONArray> testCaseWithLabelRequirement = new LinkedHashMap<>();
                LinkedHashMap<String, JSONArray> testCaseWithLabelBattery = new LinkedHashMap<>();
                for (TestCaseLabel label : (List<TestCaseLabel>) testCaseLabelList.getDataList()) {
                    String key = label.getTest() + "_" + label.getTestcase();

                    JSONObject jo = new JSONObject().put("name", label.getLabel().getLabel()).put("color", label.getLabel().getColor()).put("description", label.getLabel().getDescription());
                    switch (label.getLabel().getType()) {
                        case Label.TYPE_STICKER:
                            if (testCaseWithLabelSticker.containsKey(key)) {
                                testCaseWithLabelSticker.get(key).put(jo);
                            } else {
                                testCaseWithLabelSticker.put(key, new JSONArray().put(jo));
                            }
                            break;
                        case Label.TYPE_REQUIREMENT:
                            if (testCaseWithLabelRequirement.containsKey(key)) {
                                testCaseWithLabelRequirement.get(key).put(jo);
                            } else {
                                testCaseWithLabelRequirement.put(key, new JSONArray().put(jo));
                            }
                            break;
                        case Label.TYPE_BATTERY:
                            if (testCaseWithLabelBattery.containsKey(key)) {
                                testCaseWithLabelBattery.get(key).put(jo);
                            } else {
                                testCaseWithLabelBattery.put(key, new JSONArray().put(jo));
                            }
                            break;
                        default:
                    }
                    if (testCaseWithLabel.containsKey(key)) {
                        testCaseWithLabel.get(key).put(jo);
                    } else {
                        testCaseWithLabel.put(key, new JSONArray().put(jo));
                    }
                }

                for (TestCase testCase : (List<TestCase>) testCaseList.getDataList()) {
                    String key = testCase.getTest() + "_" + testCase.getTestCase();
                    JSONObject value = convertToJSONObject(testCase);
                    value.put("bugs", testCase.getBugs());
                    value.put("hasPermissionsDelete", testCaseService.hasPermissionsDelete(testCase, request));
                    value.put("hasPermissionsUpdate", testCaseService.hasPermissionsUpdate(testCase, request));
                    value.put("hasPermissionsCreate", testCaseService.hasPermissionsCreate(testCase, request));
                    value.put("countries", testCaseWithCountry.get(key));
                    value.put("labels", testCaseWithLabel.get(key));
                    value.put("labelsSTICKER", testCaseWithLabelSticker.get(key));
                    value.put("labelsREQUIREMENT", testCaseWithLabelRequirement.get(key));
                    value.put("labelsBATTERY", testCaseWithLabelBattery.get(key));
                    value.put("dependencies", testCaseWithDependencies.get(key));
                    jsonArray.put(value);
                }
            }
        }

//        object.put("hasPermissions", testCaseService.hasPermissions(request));
        object.put("hasPermissionsCreate", testCaseService.hasPermissionsCreate(null, request));

        object.put("contentTable", jsonArray);
        object.put("iTotalRecords", testCaseList.getTotalRows());
        object.put("iTotalDisplayRecords", testCaseList.getTotalRows());

        answer.setItem(object);
        answer.setResultMessage(testCaseList.getResultMessage());
        return answer;
    }

    private AnswerItem<JSONObject> findTestCaseByTestTestCase(String test, String testCase, HttpServletRequest request, boolean withSteps) throws JSONException, CerberusException {
        AnswerItem<JSONObject> answerItem = new AnswerItem<>();
        JSONObject jsonResponse = new JSONObject();

        AnswerItem answerTestCase;
        answerTestCase = testCaseService.findTestCaseByKeyWithDependencies(test, testCase, withSteps);
        if (answerTestCase.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && answerTestCase.getItem() != null) {
            TestCase tc = (TestCase) answerTestCase.getItem();
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
            for (TestCase tc : (List<TestCase>) answer.getDataList()) {
                JSONObject value = convertToJSONObject(tc);
                value.put("bugs", tc.getBugs());
                dataArray.put(value);
            }
        }

        object.put("contentTable", dataArray);
        item.setItem(object);
        item.setResultMessage(answer.getResultMessage());
        return item;
    }

    private AnswerItem findTestCaseByCampaign(String campaign) throws JSONException {
        AnswerItem<JSONObject> answer = new AnswerItem<>();
        JSONObject jsonResponse = new JSONObject();
        JSONArray dataArray = new JSONArray();

        final AnswerItem<Map<String, List<String>>> parsedCampaignParameters = campaignParameterService.parseParametersByCampaign(campaign);

        List<String> countries = parsedCampaignParameters.getItem().get(CampaignParameter.COUNTRY_PARAMETER);

        AnswerList<TestCase> resp = null;

        if (countries != null && !countries.isEmpty()) {
            resp = testCaseService.findTestCaseByCampaignNameAndCountries(campaign, countries.toArray(new String[countries.size()]));
        } else {
            resp = testCaseService.findTestCaseByCampaignNameAndCountries(campaign, null);
        }

        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            for (Object c : resp.getDataList()) {
                TestCase cc = (TestCase) c;
                JSONObject value = convertToJSONObject(cc);
                value.put("bugs", cc.getBugs());
                dataArray.put(value);
            }
        }

        jsonResponse.put("contentTable", dataArray);
        answer.setItem(jsonResponse);
        answer.setResultMessage(resp.getResultMessage());
        return answer;
    }

    private AnswerItem<JSONObject> findTestCaseWithStep(HttpServletRequest request, String test, String testCase) throws JSONException, CerberusException {
        AnswerItem<JSONObject> item = new AnswerItem<>();
        JSONObject object = new JSONObject();
        HashMap<String, JSONObject> hashProp = new HashMap<>();
        JSONObject jsonResponse = new JSONObject();

        //finds the testcase
        AnswerItem answer = testCaseService.readByKey(test, testCase);

        if (answer.getItem() == null) {
            item.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_NOT_FOUND_OR_NOT_AUTHORIZE));
            return item;
        }

        AnswerList<TestCaseCountry> testCaseCountries = testCaseCountryService.readByTestTestCase(null, test, testCase, null);
        AnswerList<TestCaseStep> testCaseSteps = testCaseStepService.readByTestTestCase(test, testCase);
        AnswerList<TestCaseStepAction> testCaseStepActions = testCaseStepActionService.readByTestTestCase(test, testCase);
        AnswerList<TestCaseStepActionControl> testCaseStepActionControls = testCaseStepActionControlService.readByTestTestCase(test, testCase);

        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item and convert it to JSONformat
            TestCase tc = (TestCase) answer.getItem();
            object = convertToJSONObject(tc);
            object.put("bugs", tc.getBugs());

            jsonResponse.put("hasPermissionsDelete", testCaseService.hasPermissionsDelete(tc, request));
            jsonResponse.put("hasPermissionsUpdate", testCaseService.hasPermissionsUpdate(tc, request));
            jsonResponse.put("hasPermissionsStepLibrary", (request.isUserInRole("TestStepLibrary")));
        }

        JSONArray countryLst = new JSONArray();
        for (TestCaseCountry country : (List<TestCaseCountry>) testCaseCountries.getDataList()) {
            countryLst.put(convertToJSONObject(country));
        }
        object.put("countries", countryLst);

        JSONArray steps = new JSONArray();
        Gson gson = new Gson();
        for (TestCaseStep step : (List<TestCaseStep>) testCaseSteps.getDataList()) {
            step = testCaseStepService.modifyTestCaseStepDataFromUsedStep(step);
            JSONObject jsonStep = new JSONObject(gson.toJson(step));

            //Fill JSON with step info
            jsonStep.put("objType", "step");
            //Add a JSON array for Action List from this step
            jsonStep.put("actions", new JSONArray());
            //Fill action List

            if (step.getUseStep().equals("Y")) {
                //If this step is imported from library, we call the service to retrieve actions
                TestCaseStep usedStep = testCaseStepService.findTestCaseStep(step.getUseStepTest(), step.getUseStepTestCase(), step.getUseStepStep());
                List<TestCaseStepAction> actions = testCaseStepActionService.getListOfAction(step.getUseStepTest(), step.getUseStepTestCase(), step.getUseStepStep());
                List<TestCaseStepActionControl> controls = testCaseStepActionControlService.findControlByTestTestCaseStep(step.getUseStepTest(), step.getUseStepTestCase(), step.getUseStepStep());
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
                    propertyFound.put("rank", prop.getRank());
                    List<String> countriesSelected = testCaseCountryPropertiesService.findCountryByProperty(prop);
                    JSONArray countries = new JSONArray();
                    for (String country : countriesSelected) {
                        countries.put(country);
                    }
                    propertyFound.put("country", countries);

                    hashProp.put(prop.getTest() + "_" + prop.getTestCase() + "_" + prop.getProperty(), propertyFound);
                }

                for (TestCaseStepAction action : actions) {

                    if (action.getStep() == step.getUseStepStep()) {
                        JSONObject jsonAction = new JSONObject(gson.toJson(action));

                        jsonAction.put("objType", "action");

                        jsonAction.put("controls", new JSONArray());
                        //We fill the action with the corresponding controls
                        for (TestCaseStepActionControl control : controls) {

                            if (control.getStep() == step.getUseStepStep()
                                    && control.getSequence() == action.getSequence()) {
                                JSONObject jsonControl = new JSONObject(gson.toJson(control));

                                jsonControl.put("objType", "control");

                                jsonAction.getJSONArray("controls").put(jsonControl);
                            }
                        }
                        //we put the action in the actions for the corresponding step
                        jsonStep.getJSONArray("actions").put(jsonAction);
                    }
                }
            } else {
                //else, we fill the actions with the action from this step
                for (TestCaseStepAction action : (List<TestCaseStepAction>) testCaseStepActions.getDataList()) {

                    if (action.getStep() == step.getStep()) {
                        JSONObject jsonAction = new JSONObject(gson.toJson(action));

                        jsonAction.put("objType", "action");
                        jsonAction.put("controls", new JSONArray());
                        //We fill the action with the corresponding controls
                        for (TestCaseStepActionControl control : (List<TestCaseStepActionControl>) testCaseStepActionControls.getDataList()) {

                            if (control.getStep() == step.getStep()
                                    && control.getSequence() == action.getSequence()) {
                                JSONObject jsonControl = new JSONObject(gson.toJson(control));
                                jsonControl.put("objType", "control");

                                jsonAction.getJSONArray("controls").put(jsonControl);
                            }
                        }
                        //we put the action in the actions for the corresponding step
                        jsonStep.getJSONArray("actions").put(jsonAction);
                    }
                }
            }
            steps.put(jsonStep);
        }

        jsonResponse.put("info", object);
        jsonResponse.put("steps", steps);
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

    private JSONObject convertToJSONObject(TestCaseDep testCaseDep) throws JSONException {
        return new JSONObject()
                .put("id", testCaseDep.getId())
                .put("test", testCaseDep.getTest())
                .put("testCase", testCaseDep.getTestCase())
                .put("depTest", testCaseDep.getDepTest())
                .put("depTestCase", testCaseDep.getDepTestCase())
                .put("type", testCaseDep.getType())
                .put("active", "Y".equals(testCaseDep.getActive()))
                .put("description", testCaseDep.getDescription())
                .put("depDescription", testCaseDep.getDepDescription())
                .put("depEvent", testCaseDep.getDepEvent());
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

    private AnswerItem<JSONObject> findDistinctValuesOfColumn(List<String> system, String test, HttpServletRequest request, String columnName) throws JSONException {
        AnswerItem<JSONObject> answer = new AnswerItem<>();
        JSONObject object = new JSONObject();

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "tec.test,tec.testcase,application,project,ticket,description,detailedDescription,readonly,bugtrackernewurl,deploytype,mavengroupid");
        String columnToSort[] = sColumns.split(",");

        List<String> individualLike = new ArrayList<>(Arrays.asList(ParameterParserUtil.parseStringParam(request.getParameter("sLike"), "").split(",")));

        Map<String, List<String>> individualSearch = new HashMap<>();
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

        AnswerList testCaseList = testCaseService.readDistinctValuesByCriteria(system, test, searchParameter, individualSearch, columnName);

        object.put("distinctValues", testCaseList.getDataList());

        answer.setItem(object);
        answer.setResultMessage(testCaseList.getResultMessage());
        return answer;
    }

}
