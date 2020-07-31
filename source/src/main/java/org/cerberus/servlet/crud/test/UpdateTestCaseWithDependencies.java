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
import org.cerberus.crud.factory.IFactoryTestCaseStep;
import org.cerberus.crud.factory.IFactoryTestCaseStepAction;
import org.cerberus.crud.factory.IFactoryTestCaseStepActionControl;
import org.cerberus.crud.factory.IFactoryTestCaseCountryProperties;
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
        String test = jObj.getString("informationTest");
        String testCase = jObj.getString("informationTestCase");
        JSONArray properties = jObj.getJSONArray("propArr");
        JSONArray stepArray = jObj.getJSONArray("stepArray");

        boolean duplicate = false;

        /**
         * Checking all constrains before calling the services.
         */
        if (StringUtil.isNullOrEmpty(test) || StringUtil.isNullOrEmpty(testCase)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Case")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "mandatory fields are missing."));
            ans.setResultMessage(msg);
        } else {
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            ITestCaseService testCaseService = appContext.getBean(ITestCaseService.class);
            ITestCaseCountryPropertiesService tccpService = appContext.getBean(ITestCaseCountryPropertiesService.class);
            ITestCaseStepService tcsService = appContext.getBean(ITestCaseStepService.class);
            ITestCaseStepActionService tcsaService = appContext.getBean(ITestCaseStepActionService.class);
            ITestCaseStepActionControlService tcsacService = appContext.getBean(ITestCaseStepActionControlService.class);

            AnswerItem resp = testCaseService.readByKey(test, testCase);
            TestCase tc = (TestCase) resp.getItem();
            if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
                /**
                 * Object could not be found. We stop here and report the error.
                 */
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCase")
                        .replace("%OPERATION%", "Update")
                        .replace("%REASON%", "TestCase does not exist."));
                ans.setResultMessage(msg);

            } else /**
             * The service was able to perform the query and confirm the object
             * exist, then we can update it.
             */
            if (!testCaseService.hasPermissionsUpdate(tc, request)) { // We cannot update the testcase if the user is not at least in Test role.
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCase")
                        .replace("%OPERATION%", "Update")
                        .replace("%REASON%", "Not enought privilege to update the testcase. You mut belong to Test Privilege or even TestAdmin in case the test is in WORKING status."));
                ans.setResultMessage(msg);

            } else {

                // Test Case exist and we can update it so Global update start here //
                /**
                 * TestcaseCountryProperties Update.
                 */
                List<TestCaseCountryProperties> tccpFromPage = getTestCaseCountryPropertiesFromParameter(request, appContext, test, testCase, properties);
                tccpService.compareListAndUpdateInsertDeleteElements(initialTest, initialTestCase, tccpFromPage);

                /*
                 * Get steps, actions and controls from page by:
                 * - generating a new step, action or control number,
                 * - setting the correct related step and action for action or control
                 */
                List<TestCaseStep> tcsFromPage = getTestCaseStepFromParameter(request, appContext, test, testCase, duplicate, stepArray);
                List<TestCaseStepAction> tcsaFromPage = new ArrayList<>();
                List<TestCaseStepActionControl> tcsacFromPage = new ArrayList<>();

                int nextStepNumber = getMaxStepNumber(tcsFromPage);
                for (TestCaseStep tcs : tcsFromPage) {
                    if (tcs.getStep() == -1) {
                        tcs.setStep(++nextStepNumber);
                    }

                    if (tcs.getActions() != null) {
                        int nextSequenceNumber = getMaxSequenceNumber(tcs.getActions());
                        for (TestCaseStepAction tcsa : tcs.getActions()) {
                            if (tcsa.getSequence() == -1) {
                                tcsa.setSequence(++nextSequenceNumber);
                            }
                            tcsa.setStep(tcs.getStep());

                            if (tcsa.getControls() != null) {
                                int nextControlNumber = getMaxControlNumber(tcsa.getControls());
                                for (TestCaseStepActionControl tscac : tcsa.getControls()) {
                                    if (tscac.getControlSequence() == -1) {
                                        tscac.setControlSequence(++nextControlNumber);
                                    }
                                    tscac.setStep(tcs.getStep());
                                    tscac.setSequence(tcsa.getSequence());
                                }
                                tcsacFromPage.addAll(tcsa.getControls());
                            }
                        }
                        tcsaFromPage.addAll(tcs.getActions());
                    }
                }

                /*
                 * Create, update or delete step, action and control according to the needs
                 */
                List<TestCaseStep> tcsFromDtb = new ArrayList<>(tcsService.getListOfSteps(initialTest, initialTestCase));
                tcsService.compareListAndUpdateInsertDeleteElements(tcsFromPage, tcsFromDtb, duplicate);

                List<TestCaseStepAction> tcsaFromDtb = new ArrayList<>(tcsaService.findTestCaseStepActionbyTestTestCase(initialTest, initialTestCase));
                tcsaService.compareListAndUpdateInsertDeleteElements(tcsaFromPage, tcsaFromDtb, duplicate);

                List<TestCaseStepActionControl> tcsacFromDtb = new ArrayList<>(tcsacService.findControlByTestTestCase(initialTest, initialTestCase));
                tcsacService.compareListAndUpdateInsertDeleteElements(tcsacFromPage, tcsacFromDtb, duplicate);

                tc.setUsrModif(request.getUserPrincipal().getName());
                tc.setVersion(tc.getVersion() + 1);

                testCaseService.update(tc.getTest(), tc.getTestCase(), tc);

                /**
                 * Adding Log entry.
                 */
                if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    /**
                     * Update was successful. Adding Log entry.
                     */
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    logEventService.createForPrivateCalls("/UpdateTestCaseWithDependencies", "UPDATE", "Update TestCase Script : ['" + tc.getTest() + "'|'" + tc.getTestCase() + "'] version : " + tc.getVersion(), request);
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
     * Get the highest step number from the given steps
     *
     * @param steps a collection of steps from which get the highest step number
     * @return the highest step number from the given steps
     */
    private int getMaxStepNumber(Collection<TestCaseStep> steps) {
        int nextStepNumber = 0;
        if (steps != null) {
            for (TestCaseStep step : steps) {
                if (nextStepNumber < step.getStep()) {
                    nextStepNumber = step.getStep();
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
                if (nextSequenceNumber < action.getSequence()) {
                    nextSequenceNumber = action.getSequence();
                }
            }
        }
        return nextSequenceNumber;
    }

    /**
     * Get the highest control number from the given controls
     *
     * @param controls a collection of controls from which get the highest
     * control number
     * @return the highest control number from the given controls
     */
    private int getMaxControlNumber(Collection<TestCaseStepActionControl> controls) {
        int nextControlNumber = 0;
        if (controls != null) {
            for (TestCaseStepActionControl control : controls) {
                if (nextControlNumber < control.getControlSequence()) {
                    nextControlNumber = control.getControlSequence();
                }
            }
        }
        return nextControlNumber;
    }

    private List<TestCaseCountryProperties> getTestCaseCountryPropertiesFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, JSONArray properties) throws JSONException {
        List<TestCaseCountryProperties> testCaseCountryProp = new ArrayList<>();
//        String[] testcase_properties_increment = getParameterValuesIfExists(request, "property_increment");
        IFactoryTestCaseCountryProperties testCaseCountryPropertiesFactory = appContext.getBean(IFactoryTestCaseCountryProperties.class);
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
            JSONArray countries = propJson.getJSONArray("country");
            if (!delete && !property.equals("")) {
                for (int j = 0; j < countries.length(); j++) {
                    String country = countries.getString(j);

                    testCaseCountryProp.add(testCaseCountryPropertiesFactory.create(test, testCase, country, property, description, type, database, value, value2, length, rowLimit, nature,
                            retryNb, retryPeriod, cacheExpire, rank));
                }
            }
        }
        return testCaseCountryProp;
    }

    private List<TestCaseStep> getTestCaseStepFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, boolean duplicate, JSONArray stepArray) throws JSONException {
        List<TestCaseStep> testCaseStep = new ArrayList<>();
        ITestCaseStepService tcsService = appContext.getBean(ITestCaseStepService.class);
        IFactoryTestCaseStep testCaseStepFactory = appContext.getBean(IFactoryTestCaseStep.class);

        for (int i = 0; i < stepArray.length(); i++) {
            JSONObject step = stepArray.getJSONObject(i);

            boolean delete = step.getBoolean("toDelete");
            int stepNumber = step.isNull("step") ? -1 : step.getInt("step");
            int sort = step.isNull("sort") ? -1 : step.getInt("sort");
            String loop = step.getString("loop");
            String conditionOperator = step.getString("conditionOperator");
            String conditionVal1 = step.getString("conditionVal1");
            String conditionVal2 = step.getString("conditionVal2");
            String conditionVal3 = step.getString("conditionVal3");
            String description = step.getString("description");
            String isUsedStep = step.getString("isUsedStep");
            String libraryStepTest = step.getString("libraryStepTest");
            String libraryStepTestCase = step.getString("libraryStepTestCase");
            int libraryStepStepId = step.getInt("libraryStepStepId");
            String isLibraryStep = step.getString("isLibraryStep");
            String isExecutionForced  = step.getString("isExecutionForced");
            JSONArray stepActions = step.getJSONArray("actionArr");

            if (!delete) {
                TestCaseStep tcStep = testCaseStepFactory.create(test, testCase, stepNumber, sort, loop, conditionOperator, conditionVal1, conditionVal2, conditionVal3, description, isUsedStep, libraryStepTest,
                        libraryStepTestCase, libraryStepStepId, isLibraryStep, isExecutionForced , null, null, request.getUserPrincipal().getName(), null);

                if (isUsedStep.equals("N")) {
                    tcStep.setActions(getTestCaseStepActionFromParameter(request, appContext, test, testCase, stepActions));
                } else {
                    TestCaseStep tcs = null;
                    if (libraryStepStepId != -1 && !libraryStepTest.equals("") && !libraryStepTestCase.equals("")) {
                        tcs = tcsService.findTestCaseStep(libraryStepTest, libraryStepTestCase, libraryStepStepId);
                        if (tcs != null) {
                            tcStep.setUseStepTest(tcs.getTest());
                            tcStep.setUseStepTestCase(tcs.getTestCase());
                            tcStep.setUseStepStep(tcs.getStep());
                        }
                    }
                }
                testCaseStep.add(tcStep);
            }
        }
        return testCaseStep;
    }

    private List<TestCaseStepAction> getTestCaseStepActionFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, JSONArray testCaseStepActionJson) throws JSONException {
        List<TestCaseStepAction> testCaseStepAction = new ArrayList<>();
        IFactoryTestCaseStepAction testCaseStepActionFactory = appContext.getBean(IFactoryTestCaseStepAction.class);

        for (int i = 0; i < testCaseStepActionJson.length(); i++) {
            JSONObject tcsaJson = testCaseStepActionJson.getJSONObject(i);

            boolean delete = tcsaJson.getBoolean("toDelete");
            int step = tcsaJson.isNull("step") ? -1 : tcsaJson.getInt("step");
            int sequence = tcsaJson.isNull("sequence") ? -1 : tcsaJson.getInt("sequence");
            int sort = tcsaJson.isNull("sort") ? -1 : tcsaJson.getInt("sort");
            String conditionOperator = tcsaJson.getString("conditionOperator");
            String conditionVal1 = tcsaJson.getString("conditionVal1");
            String conditionVal2 = tcsaJson.getString("conditionVal2");
            String conditionVal3 = tcsaJson.getString("conditionVal3");
            String action = tcsaJson.getString("action");
            String object = tcsaJson.getString("object");
            String property = tcsaJson.getString("property");
            String value3 = tcsaJson.getString("value3");
            String forceExeStatus = tcsaJson.getString("forceExeStatus");
            String description = tcsaJson.getString("description");
            String screenshot = tcsaJson.getString("screenshotFileName");
            JSONArray controlArray = tcsaJson.getJSONArray("controlArr");

            if (!delete) {
                TestCaseStepAction tcsa = testCaseStepActionFactory.create(test, testCase, step, sequence, sort, conditionOperator, conditionVal1, conditionVal2, conditionVal3, action, object, property, value3, forceExeStatus, description, screenshot);
                tcsa.setControls(getTestCaseStepActionControlFromParameter(request, appContext, test, testCase, controlArray));
                testCaseStepAction.add(tcsa);
            }
        }
        return testCaseStepAction;
    }

    private List<TestCaseStepActionControl> getTestCaseStepActionControlFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, JSONArray controlArray) throws JSONException {
        List<TestCaseStepActionControl> testCaseStepActionControl = new ArrayList<>();
        IFactoryTestCaseStepActionControl testCaseStepActionControlFactory = appContext.getBean(IFactoryTestCaseStepActionControl.class);

        for (int i = 0; i < controlArray.length(); i++) {
            JSONObject controlJson = controlArray.getJSONObject(i);

            boolean delete = controlJson.getBoolean("toDelete");
            int step = controlJson.isNull("step") ? -1 : controlJson.getInt("step");
            int sequence = controlJson.isNull("sequence") ? -1 : controlJson.getInt("sequence");
            int control = controlJson.isNull("controlSequence") ? -1 : controlJson.getInt("controlSequence");
            int sort = controlJson.isNull("sort") ? -1 : controlJson.getInt("sort");
            String conditionOperator = controlJson.isNull("conditionOperator") ? "always" : controlJson.getString("conditionOperator");
            String conditionVal1 = controlJson.isNull("conditionVal1") ? "" : controlJson.getString("conditionVal1");
            String conditionVal2 = controlJson.isNull("conditionVal2") ? "" : controlJson.getString("conditionVal2");
            String conditionVal3 = controlJson.isNull("conditionVal3") ? "" : controlJson.getString("conditionVal3");
            //String type = controlJson.getString("objType");
            String controlValue = controlJson.getString("control");
            String value1 = controlJson.getString("value1");
            String value2 = controlJson.getString("value2");
            String value3 = controlJson.isNull("value3") ? "" : controlJson.getString("value3");
            String fatal = controlJson.getString("fatal");
            String description = controlJson.getString("description");
            String screenshot = controlJson.getString("screenshotFileName");
            if (!delete) {
                testCaseStepActionControl.add(testCaseStepActionControlFactory.create(test, testCase, step, sequence, control, sort, conditionOperator, conditionVal1, conditionVal2, conditionVal3, controlValue, value1, value2, value3, fatal, description, screenshot));
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
