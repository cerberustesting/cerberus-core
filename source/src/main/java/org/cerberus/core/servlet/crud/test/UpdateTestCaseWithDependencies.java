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
package org.cerberus.core.servlet.crud.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.cerberus.core.crud.factory.IFactoryTestCaseStep;
import org.cerberus.core.crud.factory.IFactoryTestCaseStepAction;
import org.cerberus.core.crud.factory.IFactoryTestCaseStepActionControl;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.crud.service.ITestCaseStepActionControlService;
import org.cerberus.core.crud.service.ITestCaseStepActionService;
import org.cerberus.core.crud.service.ITestCaseStepService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.TestCaseHisto;
import org.cerberus.core.crud.service.ITestCaseHistoService;

/**
 * @author bcivel
 */
@WebServlet(name = "UpdateTestCaseWithDependencies", urlPatterns = {"/UpdateTestCaseWithDependencies"})
public class UpdateTestCaseWithDependencies extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(UpdateTestCaseWithDependencies.class);
    private ITestCaseService testCaseService;
    private ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
    private ITestCaseStepService stepService;
    private ITestCaseStepActionService actionService;
    private ITestCaseStepActionControlService controlService;
    private ITestCaseHistoService testCaseHistoService;

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
            throws ServletException, IOException, CerberusException, JSONException {

        JSONObject jsonResponse = new JSONObject();
        Answer ans = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        response.setContentType("application/json");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        //Parsing and securing all required parameters.
        StringBuilder sb = new StringBuilder();
        BufferedReader br = request.getReader();
        String str;
        while ((str = br.readLine()) != null) {
            sb.append(str);
        }
        JSONObject jObj = new JSONObject(sb.toString());
        String initialTest = jObj.getString("informationInitialTest");
        String initialTestCase = jObj.getString("informationInitialTestCase");
        String testId = jObj.getString("informationTest");
        String testCaseId = jObj.getString("informationTestCase");
        JSONArray properties = jObj.getJSONArray("properties");
        JSONArray steps = jObj.getJSONArray("steps");

        //Checking all constrains before calling the services.
        if (StringUtil.isEmptyOrNull(testId) || StringUtil.isEmptyOrNull(testCaseId)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Case")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "mandatory fields are missing."));
            ans.setResultMessage(msg);
        } else {
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            testCaseService = appContext.getBean(ITestCaseService.class);
            testCaseCountryPropertiesService = appContext.getBean(ITestCaseCountryPropertiesService.class);
            stepService = appContext.getBean(ITestCaseStepService.class);
            actionService = appContext.getBean(ITestCaseStepActionService.class);
            controlService = appContext.getBean(ITestCaseStepActionControlService.class);
            testCaseHistoService = appContext.getBean(ITestCaseHistoService.class);

            AnswerItem<TestCase> testcaseAnswerItem = testCaseService.readByKey(testId, testCaseId);
            TestCase testcase = testcaseAnswerItem.getItem();
            if (!(testcaseAnswerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && testcaseAnswerItem.getItem() != null)) {
                //Object could not be found. We stop here and report the error.
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCase")
                        .replace("%OPERATION%", "Update")
                        .replace("%REASON%", "TestCase does not exist."));
                ans.setResultMessage(msg);

            } else if (!testCaseService.hasPermissionsUpdate(testcase, request)) { // We cannot update the testcase if the user is not at least in Test role.
                //The service was able to perform the query and confirm the object exist, then we can update it.
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCase")
                        .replace("%OPERATION%", "Update")
                        .replace("%REASON%", "Not enought privilege to update the testcase. You mut belong to Test Privilege or even TestAdmin in case the test is in " + TestCase.TESTCASE_STATUS_WORKING + " status."));
                ans.setResultMessage(msg);

            } else { // Test Case exist and we can update it so Global update start here

                // Save histo entry
                this.testCaseHistoService.create(TestCaseHisto.builder()
                        .test(testcase.getTest())
                        .testCase(testcase.getTestcase())
                        .version(testcase.getVersion())
                        .usrCreated(testcase.getUsrModif())
//                        .testCaseContent(testcase.toJsonV001("", null))
                        .testCaseContent(new JSONObject())
                        .description("")
                        .build());

                // TestcaseCountryProperties Update
                List<TestCaseCountryProperties> testcaseCountryPropertiesFromPage = getTestCaseCountryPropertiesFromParameter(testcase, properties);
                testCaseCountryPropertiesService.compareListAndUpdateInsertDeleteElements(initialTest, initialTestCase, testcaseCountryPropertiesFromPage);

                /*
                 * Get steps, actions and controls from page by:
                 * - generating a new stepId, action or controlId number,
                 * - setting the correct related stepId and action for action or controlId
                 */
                List<TestCaseStep> stepsFromRequest = getTestCaseStepsFromParameter(request, appContext, testId, testCaseId, steps);
                List<TestCaseStepAction> actionsFromRequest = new ArrayList<>();
                List<TestCaseStepActionControl> controlsFromRequest = new ArrayList<>();

                int maxStepId = stepService.getMaxStepId(stepsFromRequest);
                for (TestCaseStep step : stepsFromRequest) {
                    if (step.getStepId() == -1) {
                        step.setStepId(++maxStepId);
                    }

                    if (step.getActions() != null) {
                        int maxActionId = actionService.getMaxActionId(step.getActions());
                        for (TestCaseStepAction action : step.getActions()) {
                            if (action.getActionId() == -1) {
                                action.setActionId(++maxActionId);
                            }
                            action.setStepId(step.getStepId());

                            if (action.getControls() != null) {
                                int maxControlId = controlService.getMaxControlId(action.getControls());
                                for (TestCaseStepActionControl control : action.getControls()) {
                                    if (control.getControlId() == -1) {
                                        control.setControlId(++maxControlId);
                                    }
                                    control.setStepId(step.getStepId());
                                    control.setActionId(action.getActionId());
                                }
                                controlsFromRequest.addAll(action.getControls());
                            }
                        }
                        actionsFromRequest.addAll(step.getActions());
                    }
                }

                /*
                 * Create, update or delete stepId, action and controlId according to the needs
                 */
                List<TestCaseStep> stepsFromDatabase = stepService.getListOfSteps(initialTest, initialTestCase);
                stepService.compareListAndUpdateInsertDeleteElements(stepsFromRequest, stepsFromDatabase, false);

                List<TestCaseStepAction> actionsFromDatabase = actionService.readByTestTestCase(initialTest, initialTestCase).getDataList();
                actionService.compareListAndUpdateInsertDeleteElements(actionsFromRequest, actionsFromDatabase, false);

                List<TestCaseStepActionControl> controlsFromDatabase = new ArrayList<>(controlService.findControlByTestTestCase(initialTest, initialTestCase));
                controlService.compareListAndUpdateInsertDeleteElements(controlsFromRequest, controlsFromDatabase, false);

                testcase.setUsrModif(request.getUserPrincipal().getName());
                testcase.setVersion(testcase.getVersion() + 1);

                testCaseService.update(testcase.getTest(), testcase.getTestcase(), testcase);

                //Adding Log entry.
                if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    //Update was successful. Adding Log entry.
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    logEventService.createForPrivateCalls("/UpdateTestCaseWithDependencies", "UPDATE", LogEvent.STATUS_INFO, "Update TestCase Script : ['" + testcase.getTest() + "'|'" + testcase.getTestcase() + "'] version : " + testcase.getVersion(), request);
                }

            }
        }

        //Formating and returning the json result.
        jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", ans.getResultMessage().getDescription());

        response.getWriter().print(jsonResponse);
        response.getWriter().flush();

    }

    private List<TestCaseCountryProperties> getTestCaseCountryPropertiesFromParameter(TestCase testcase, JSONArray properties) throws JSONException {
        List<TestCaseCountryProperties> testCaseCountryProp = new ArrayList<>();

        for (int i = 0; i < properties.length(); i++) {
            JSONObject propJson = properties.getJSONObject(i);
            boolean delete = propJson.getBoolean("toDelete");
            String property = propJson.getString("property");
            String description = propJson.getString("description");
            int cacheExpire = propJson.getInt("cacheExpire");
            String type = propJson.getString("type");
            String value = propJson.getString("value1");
            String value2 = propJson.getString("value2");
            String value3 = "";
            if (propJson.has("value3")) {
                value3 = propJson.getString("value3");
            }
            String length = propJson.getString("length");
            int rowLimit = propJson.getInt("rowLimit");
            int retryNb = propJson.optInt("retryNb");
            int retryPeriod = propJson.optInt("retryPeriod");
            int rank = propJson.optInt("rank");
            String nature = propJson.getString("nature");
            String database = propJson.getString("database");
            JSONArray countries = propJson.getJSONArray("countries");
            if (!delete && !property.isEmpty()) {
                for (int j = 0; j < countries.length(); j++) {
                    String country = countries.getJSONObject(j).getString("value");
                    testCaseCountryProp.add(TestCaseCountryProperties.builder()
                            .test(testcase.getTest())
                            .testcase(testcase.getTestcase())
                            .country(country)
                            .property(property)
                            .description(description)
                            .type(type)
                            .database(database)
                            .value1(value)
                            .value2(value2)
                            .value3(value3)
                            .length(length)
                            .rowLimit(rowLimit)
                            .cacheExpire(cacheExpire)
                            .nature(nature)
                            .retryNb(retryNb)
                            .retryPeriod(retryPeriod)
                            .rank(rank)
                            .build());
                }
            }
        }
        return testCaseCountryProp;
    }

    private List<TestCaseStep> getTestCaseStepsFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, JSONArray stepArray) throws JSONException {
        List<TestCaseStep> testCaseStep = new ArrayList<>();
        ITestCaseStepService tcsService = appContext.getBean(ITestCaseStepService.class);
        IFactoryTestCaseStep testCaseStepFactory = appContext.getBean(IFactoryTestCaseStep.class);

        for (int i = 0; i < stepArray.length(); i++) {
            JSONObject step = stepArray.getJSONObject(i);

            boolean delete = step.getBoolean("toDelete");
            int stepId = step.isNull("stepId") ? -1 : step.getInt("stepId");
            int sort = step.isNull("sort") ? -1 : step.getInt("sort");
            String loop = step.getString("loop");
            String conditionOperator = step.getString("conditionOperator");
            String conditionValue1 = step.getString("conditionValue1");
            String conditionValue2 = step.getString("conditionValue2");
            String conditionValue3 = step.getString("conditionValue3");
            JSONArray conditionOptions = step.getJSONArray("conditionOptions");
            String description = step.getString("description");
            boolean isUsingLibraryStep = step.getBoolean("isUsingLibraryStep");
            String libraryStepTest = step.getString("libraryStepTest");
            String libraryStepTestCase = step.getString("libraryStepTestCase");
            int libraryStepStepId = step.getInt("libraryStepStepId");
            boolean isLibraryStep = step.getBoolean("isLibraryStep");
            boolean isExecutionForced = step.getBoolean("isExecutionForced");
            JSONArray stepActions = step.getJSONArray("actions");

            if (!delete) {
                TestCaseStep tcStep = testCaseStepFactory.create(test, testCase, stepId, sort, loop, conditionOperator, conditionValue1, conditionValue2, conditionValue3, conditionOptions, description, isUsingLibraryStep, libraryStepTest,
                        libraryStepTestCase, libraryStepStepId, isLibraryStep, isExecutionForced, null, null, request.getUserPrincipal().getName(), null);

                if (!isUsingLibraryStep) {
                    tcStep.setActions(getTestCaseStepActionsFromParameter(request, appContext, test, testCase, stepActions));
                } else {
                    TestCaseStep tcs = null;
                    if (libraryStepStepId != -1 && !libraryStepTest.isEmpty() && !libraryStepTestCase.isEmpty()) {
                        tcs = tcsService.findTestCaseStep(libraryStepTest, libraryStepTestCase, libraryStepStepId);
                        if (tcs != null) {
                            tcStep.setLibraryStepTest(tcs.getTest());
                            tcStep.setLibraryStepTestcase(tcs.getTestcase());
                            tcStep.setLibraryStepStepId(tcs.getStepId());
                        }
                    }
                }
                testCaseStep.add(tcStep);
            }
        }
        return testCaseStep;
    }

    private List<TestCaseStepAction> getTestCaseStepActionsFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testcase, JSONArray testCaseStepActionJson) throws JSONException {
        List<TestCaseStepAction> testCaseStepAction = new ArrayList<>();
        IFactoryTestCaseStepAction testCaseStepActionFactory = appContext.getBean(IFactoryTestCaseStepAction.class);

        for (int i = 0; i < testCaseStepActionJson.length(); i++) {
            JSONObject tcsaJson = testCaseStepActionJson.getJSONObject(i);

            boolean delete = tcsaJson.getBoolean("toDelete");
            int stepId = tcsaJson.isNull("stepId") ? -1 : tcsaJson.getInt("stepId");
            int actionId = tcsaJson.isNull("actionId") ? -1 : tcsaJson.getInt("actionId");
            int sort = tcsaJson.isNull("sort") ? -1 : tcsaJson.getInt("sort");
            String conditionOperator = tcsaJson.getString("conditionOperator");
            String conditionValue1 = tcsaJson.getString("conditionValue1");
            String conditionValue2 = tcsaJson.getString("conditionValue2");
            String conditionValue3 = tcsaJson.getString("conditionValue3");
            JSONArray condOptionsArray = tcsaJson.getJSONArray("conditionOptions");
            String action = tcsaJson.getString("action");
            String value1 = tcsaJson.getString("object");
            String value2 = tcsaJson.getString("property");
            String value3 = tcsaJson.getString("value3");
            JSONArray optionsArray = tcsaJson.getJSONArray("options");
            boolean isFatal = tcsaJson.getBoolean("isFatal");
            boolean doScreenshotBefore = tcsaJson.getBoolean("doScreenshotBefore");
            boolean doScreenshotAfter = tcsaJson.getBoolean("doScreenshotAfter");
            int waitBefore = tcsaJson.getInt("waitBefore");
            int waitAfter = tcsaJson.getInt("waitAfter");
            String description = tcsaJson.getString("description");
            String screenshot = tcsaJson.getString("screenshotFileName");
            JSONArray controlArray = tcsaJson.getJSONArray("controls");

            if (!delete) {
                TestCaseStepAction tcsa = testCaseStepActionFactory.create(test, testcase, stepId, actionId, sort, conditionOperator, conditionValue1, conditionValue2, conditionValue3, condOptionsArray,
                        action, value1, value2, value3, optionsArray, isFatal, description, screenshot,
                        doScreenshotBefore, doScreenshotAfter, waitBefore, waitAfter);
                tcsa.setControls(getTestCaseStepActionControlsFromParameter(request, appContext, test, testcase, controlArray));
                testCaseStepAction.add(tcsa);
            }
        }
        return testCaseStepAction;
    }

    private List<TestCaseStepActionControl> getTestCaseStepActionControlsFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, JSONArray controlArray) throws JSONException {
        List<TestCaseStepActionControl> testCaseStepActionControl = new ArrayList<>();
        IFactoryTestCaseStepActionControl testCaseStepActionControlFactory = appContext.getBean(IFactoryTestCaseStepActionControl.class);

        for (int i = 0; i < controlArray.length(); i++) {
            JSONObject controlJson = controlArray.getJSONObject(i);

            boolean delete = controlJson.getBoolean("toDelete");
            int stepId = controlJson.isNull("stepId") ? -1 : controlJson.getInt("stepId");
            int actionId = controlJson.isNull("actionId") ? -1 : controlJson.getInt("actionId");
            int controlId = controlJson.isNull("controlId") ? -1 : controlJson.getInt("controlId");
            int sort = controlJson.isNull("sort") ? -1 : controlJson.getInt("sort");
            String conditionOperator = controlJson.isNull("conditionOperator") ? "always" : controlJson.getString("conditionOperator");
            String conditionValue1 = controlJson.isNull("conditionValue1") ? "" : controlJson.getString("conditionValue1");
            String conditionValue2 = controlJson.isNull("conditionValue2") ? "" : controlJson.getString("conditionValue2");
            String conditionValue3 = controlJson.isNull("conditionValue3") ? "" : controlJson.getString("conditionValue3");
            JSONArray conditionOptions = controlJson.getJSONArray("conditionOptions");
            //String type = controlJson.getString("objType");
            String controlValue = controlJson.getString("control");
            String value1 = controlJson.getString("value1");
            String value2 = controlJson.getString("value2");
            String value3 = controlJson.isNull("value3") ? "" : controlJson.getString("value3");
            JSONArray options = controlJson.getJSONArray("options");
            boolean isFatal = controlJson.getBoolean("isFatal");
            boolean doScreenshotBefore = controlJson.getBoolean("doScreenshotBefore");
            boolean doScreenshotAfter = controlJson.getBoolean("doScreenshotAfter");
            int waitBefore = controlJson.getInt("waitBefore");
            int waitAfter = controlJson.getInt("waitAfter");
            String description = controlJson.getString("description");
            String screenshot = controlJson.getString("screenshotFileName");
            if (!delete) {
                testCaseStepActionControl.add(testCaseStepActionControlFactory.create(test, testCase, stepId, actionId, controlId, sort,
                        conditionOperator, conditionValue1, conditionValue2, conditionValue3, conditionOptions,
                        controlValue, value1, value2, value3, options, isFatal, description, screenshot, doScreenshotBefore, doScreenshotAfter, waitBefore, waitAfter));
            }
        }
        return testCaseStepActionControl;
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
            LOG.warn(ex, ex);
        } catch (JSONException ex) {
            LOG.warn(ex, ex);
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
            LOG.warn(ex, ex);
        } catch (JSONException ex) {
            LOG.warn(ex, ex);
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

}
