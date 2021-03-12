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
package org.cerberus.servlet.crud.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.factory.IFactoryTestCase;
import org.cerberus.crud.factory.IFactoryTestCaseStep;
import org.cerberus.crud.factory.IFactoryTestCaseStepAction;
import org.cerberus.crud.factory.IFactoryTestCaseStepActionControl;
import org.cerberus.crud.factory.IFactoryTestCaseCountryProperties;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.ITestCaseStepActionControlService;
import org.cerberus.crud.service.ITestCaseStepActionService;
import org.cerberus.crud.service.ITestCaseStepService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "UpdateTestCaseWithDependencies", urlPatterns = {"/UpdateTestCaseWithDependencies"})
public class UpdateTestCaseWithDependencies extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(UpdateTestCaseWithDependencies.class);
    private ITestCaseService testCaseService;
    private ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
    private ITestCaseStepService tcsService;
    private ITestCaseStepActionService tcsaService;
    private ITestCaseStepActionControlService tcsacService;
    private IFactoryTestCaseCountryProperties testCaseCountryPropertiesFactory;

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

        /**
         * Parsing and securing all required parameters.
         */
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

        boolean duplicate = false;

        /**
         * Checking all constrains before calling the services.
         */
        if (StringUtil.isNullOrEmpty(testId) || StringUtil.isNullOrEmpty(testCaseId)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Case")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "mandatory fields are missing."));
            ans.setResultMessage(msg);
        } else {
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            testCaseService = appContext.getBean(ITestCaseService.class);
            testCaseCountryPropertiesService = appContext.getBean(ITestCaseCountryPropertiesService.class);
            tcsService = appContext.getBean(ITestCaseStepService.class);
            tcsaService = appContext.getBean(ITestCaseStepActionService.class);
            tcsacService = appContext.getBean(ITestCaseStepActionControlService.class);
            testCaseCountryPropertiesFactory = appContext.getBean(IFactoryTestCaseCountryProperties.class);

            AnswerItem<TestCase> testcaseAnswerItem = testCaseService.readByKey(testId, testCaseId);
            TestCase testcase = testcaseAnswerItem.getItem();
            if (!(testcaseAnswerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && testcaseAnswerItem.getItem() != null)) {
                /**
                 * Object could not be found. We stop here and report the error.
                 */
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCase")
                        .replace("%OPERATION%", "Update")
                        .replace("%REASON%", "TestCase does not exist."));
                ans.setResultMessage(msg);

            } else if (!testCaseService.hasPermissionsUpdate(testcase, request)) { // We cannot update the testcase if the user is not at least in Test role.
                /**
                 * The service was able to perform the query and confirm the
                 * object exist, then we can update it.
                 */
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCase")
                        .replace("%OPERATION%", "Update")
                        .replace("%REASON%", "Not enought privilege to update the testcase. You mut belong to Test Privilege or even TestAdmin in case the test is in WORKING status."));
                ans.setResultMessage(msg);

            } else { // Test Case exist and we can update it so Global update start here

                /**
                 * TestcaseCountryProperties Update.
                 */
                List<TestCaseCountryProperties> testcaseCountryPropertiesFromPage = getTestCaseCountryPropertiesFromParameter(testcase, properties);
                testCaseCountryPropertiesService.compareListAndUpdateInsertDeleteElements(initialTest, initialTestCase, testcaseCountryPropertiesFromPage);

                /*
                 * Get steps, actions and controls from page by:
                 * - generating a new stepId, action or controlId number,
                 * - setting the correct related stepId and action for action or controlId
                 */
                List<TestCaseStep> testcaseStepsFromPage = getTestCaseStepsFromParameter(request, appContext, testId, testCaseId, duplicate, steps);
                List<TestCaseStepAction> testcaseStepActionsFromPage = new ArrayList<>();
                List<TestCaseStepActionControl> testcaseStepActionControlsFromPage = new ArrayList<>();

                int nextStepNumber = getMaxStepNumber(testcaseStepsFromPage);
                for (TestCaseStep testcaseStepFromPage : testcaseStepsFromPage) {
                    if (testcaseStepFromPage.getStepId() == -1) {
                        testcaseStepFromPage.setStepId(++nextStepNumber);
                    }

                    if (testcaseStepFromPage.getActions() != null) {
                        int nextSequenceNumber = getMaxSequenceNumber(testcaseStepFromPage.getActions());
                        for (TestCaseStepAction tcsa : testcaseStepFromPage.getActions()) {
                            if (tcsa.getActionId() == -1) {
                                tcsa.setActionId(++nextSequenceNumber);
                            }
                            tcsa.setStepId(testcaseStepFromPage.getStepId());

                            if (tcsa.getControls() != null) {
                                int nextControlNumber = getMaxControlNumber(tcsa.getControls());
                                for (TestCaseStepActionControl tscac : tcsa.getControls()) {
                                    if (tscac.getControlId() == -1) {
                                        tscac.setControlId(++nextControlNumber);
                                    }
                                    tscac.setStepId(testcaseStepFromPage.getStepId());
                                    tscac.setActionId(tcsa.getActionId());
                                }
                                testcaseStepActionControlsFromPage.addAll(tcsa.getControls());
                            }
                        }
                        testcaseStepActionsFromPage.addAll(testcaseStepFromPage.getActions());
                    }
                }

                /*
                 * Create, update or delete stepId, action and controlId according to the needs
                 */
                List<TestCaseStep> tcsFromDtb = new ArrayList<>(tcsService.getListOfSteps(initialTest, initialTestCase));
                tcsService.compareListAndUpdateInsertDeleteElements(testcaseStepsFromPage, tcsFromDtb, duplicate);

                List<TestCaseStepAction> tcsaFromDtb = new ArrayList<>(tcsaService.findTestCaseStepActionbyTestTestCase(initialTest, initialTestCase));
                tcsaService.compareListAndUpdateInsertDeleteElements(testcaseStepActionsFromPage, tcsaFromDtb, duplicate);

                List<TestCaseStepActionControl> tcsacFromDtb = new ArrayList<>(tcsacService.findControlByTestTestCase(initialTest, initialTestCase));
                tcsacService.compareListAndUpdateInsertDeleteElements(testcaseStepActionControlsFromPage, tcsacFromDtb, duplicate);

                testcase.setUsrModif(request.getUserPrincipal().getName());
                testcase.setVersion(testcase.getVersion() + 1);

                testCaseService.update(testcase.getTest(), testcase.getTestcase(), testcase);

                /**
                 * Adding Log entry.
                 */
                if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    /**
                     * Update was successful. Adding Log entry.
                     */
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    logEventService.createForPrivateCalls("/UpdateTestCaseWithDependencies", "UPDATE", "Update TestCase Script : ['" + testcase.getTest() + "'|'" + testcase.getTestcase() + "'] version : " + testcase.getVersion(), request);
                }

            }
        }

        /**
         * Formating and returning the json result.
         */
        jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", ans.getResultMessage().getDescription());

        response.getWriter().print(jsonResponse);
        response.getWriter().flush();

    }

    /**
     * Get the highest stepId number from the given steps
     *
     * @param steps a collection of steps from which get the highest stepId
     * number
     * @return the highest stepId number from the given steps
     */
    private int getMaxStepNumber(Collection<TestCaseStep> steps) {
        int nextStepNumber = 0;
        if (steps != null) {
            for (TestCaseStep step : steps) {
                if (nextStepNumber < step.getStepId()) {
                    nextStepNumber = step.getStepId();
                }
            }
        }
        return nextStepNumber;
    }

    /**
     * Get the highest action sequence from the given actions
     *
     * @param actions a collection of actions from which get the highest action
     * sequence
     * @return the highest action sequence from the given actions
     */
    private int getMaxSequenceNumber(Collection<TestCaseStepAction> actions) {
        int nextSequenceNumber = 0;
        if (actions != null) {
            for (TestCaseStepAction action : actions) {
                if (nextSequenceNumber < action.getActionId()) {
                    nextSequenceNumber = action.getActionId();
                }
            }
        }
        return nextSequenceNumber;
    }

    /**
     * Get the highest controlId number from the given controls
     *
     * @param controls a collection of controls from which get the highest
 controlId number
     * @return the highest controlId number from the given controls
     */
    private int getMaxControlNumber(Collection<TestCaseStepActionControl> controls) {
        int nextControlNumber = 0;
        if (controls != null) {
            for (TestCaseStepActionControl control : controls) {
                if (nextControlNumber < control.getControlId()) {
                    nextControlNumber = control.getControlId();
                }
            }
        }
        return nextControlNumber;
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
            String length = propJson.getString("length");
            int rowLimit = propJson.getInt("rowLimit");
            int retryNb = propJson.optInt("retryNb");
            int retryPeriod = propJson.optInt("retryPeriod");
            int rank = propJson.optInt("rank");
            String nature = propJson.getString("nature");
            String database = propJson.getString("database");
            JSONArray countries = propJson.getJSONArray("countries");
            if (!delete && !property.equals("")) {
                for (int j = 0; j < countries.length(); j++) {
                    String country = countries.getJSONObject(j).getString("value");
                    testCaseCountryProp.add(testCaseCountryPropertiesFactory.create(testcase.getTest(), testcase.getTestcase(), country, property, description, type, database, value, value2, length, rowLimit, nature,
                            retryNb, retryPeriod, cacheExpire, rank, null, null, null, null));
                }
            }
        }
        return testCaseCountryProp;
    }

    private List<TestCaseStep> getTestCaseStepsFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, boolean duplicate, JSONArray stepArray) throws JSONException {
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
            String description = step.getString("description");
            boolean isUsingLibraryStep = step.getBoolean("isUsingLibraryStep");
            String libraryStepTest = step.getString("libraryStepTest");
            String libraryStepTestCase = step.getString("libraryStepTestCase");
            int libraryStepStepId = step.getInt("libraryStepStepId");
            boolean isLibraryStep = step.getBoolean("isLibraryStep");
            boolean isExecutionForced = step.getBoolean("isExecutionForced");
            JSONArray stepActions = step.getJSONArray("actions");

            if (!delete) {
                TestCaseStep tcStep = testCaseStepFactory.create(test, testCase, stepId, sort, loop, conditionOperator, conditionValue1, conditionValue2, conditionValue3, description, isUsingLibraryStep, libraryStepTest,
                        libraryStepTestCase, libraryStepStepId, isLibraryStep, isExecutionForced, null, null, request.getUserPrincipal().getName(), null);

                if (!isUsingLibraryStep) {
                    tcStep.setActions(getTestCaseStepActionsFromParameter(request, appContext, test, testCase, stepActions));
                } else {
                    TestCaseStep tcs = null;
                    if (libraryStepStepId != -1 && !libraryStepTest.equals("") && !libraryStepTestCase.equals("")) {
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
            String action = tcsaJson.getString("action");
            String value1 = tcsaJson.getString("object");
            String value2 = tcsaJson.getString("property");
            String value3 = tcsaJson.getString("value3");
            boolean isFatal = tcsaJson.getBoolean("isFatal");
            String description = tcsaJson.getString("description");
            String screenshot = tcsaJson.getString("screenshotFileName");
            JSONArray controlArray = tcsaJson.getJSONArray("controls");

            if (!delete) {
                TestCaseStepAction tcsa = testCaseStepActionFactory.create(test, testcase, stepId, actionId, sort, conditionOperator, conditionValue1, conditionValue2, conditionValue3, action, value1, value2, value3, isFatal, description, screenshot);
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
            //String type = controlJson.getString("objType");
            String controlValue = controlJson.getString("control");
            String value1 = controlJson.getString("value1");
            String value2 = controlJson.getString("value2");
            String value3 = controlJson.isNull("value3") ? "" : controlJson.getString("value3");
            boolean isFatal = controlJson.getBoolean("isFatal");
            String description = controlJson.getString("description");
            String screenshot = controlJson.getString("screenshotFileName");
            if (!delete) {
                testCaseStepActionControl.add(testCaseStepActionControlFactory.create(test, testCase, stepId, actionId, controlId, sort, conditionOperator, conditionValue1, conditionValue2, conditionValue3, controlValue, value1, value2, value3, isFatal, description, screenshot));
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
            LOG.warn(ex);
        } catch (JSONException ex) {
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
        } catch (JSONException ex) {
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

}
