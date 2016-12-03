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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.crud.factory.IFactoryTestCaseCountry;
import org.cerberus.crud.factory.IFactoryTestCaseCountryProperties;
import org.cerberus.crud.factory.IFactoryTestCaseStep;
import org.cerberus.crud.factory.IFactoryTestCaseStepAction;
import org.cerberus.crud.factory.IFactoryTestCaseStepActionControl;
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
import org.springframework.web.util.HtmlUtils;
import org.cerberus.crud.factory.IFactoryTestCase;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "UpdateTestCaseWithDependencies1", urlPatterns = {"/UpdateTestCaseWithDependencies1"})
public class UpdateTestCaseWithDependencies1 extends HttpServlet {

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
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        response.setContentType("application/json");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        /**
         * Parsing and securing all required parameters.
         */
        String initialTest = request.getParameter("informationInitialTest");
        String initialTestCase = request.getParameter("informationInitialTestCase");
        String test = request.getParameter("informationTest");
        String testCase = request.getParameter("informationTestCase");
        boolean duplicate = false;

        /**
         * Checking all constrains before calling the services.
         */
        if (StringUtil.isNullOrEmpty(test) || StringUtil.isNullOrEmpty(testCase)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Case")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "mendatory fields are missing."));
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
             if (!request.isUserInRole("Test")) { // We cannot update the testcase if the user is not at least in Test role.
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCase")
                            .replace("%OPERATION%", "Update")
                            .replace("%REASON%", "Not enought privilege to update the testcase. You mut belong to Test Privilege."));
                    ans.setResultMessage(msg);

                } else if ((tc.getStatus().equalsIgnoreCase("WORKING")) && !(request.isUserInRole("TestAdmin"))) { // If Test Case is WORKING we need TestAdmin priviliges.
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCase")
                            .replace("%OPERATION%", "Update")
                            .replace("%REASON%", "Not enought privilege to update the testcase. The test case is in WORKING status and needs TestAdmin privilige to be updated"));
                    ans.setResultMessage(msg);

                } else {

                    // Test Case exist and we can update it so Global update start here //
                    /**
                     * TestcaseCountryProperties Update.
                     */
                    List<TestCaseCountryProperties> tccpFromPage = getTestCaseCountryPropertiesFromParameter(request, appContext, test, testCase);
                    tccpService.compareListAndUpdateInsertDeleteElements(initialTest, initialTestCase, tccpFromPage);

                    /*
                    * Get steps, actions and controls from page by:
                    * - generating a new step, action or control number,
                    * - setting the correct related step and action for action or control
                     */
                    List<TestCaseStep> tcsFromPage = getTestCaseStepFromParameter(request, appContext, test, testCase, duplicate);
                    List<TestCaseStepAction> tcsaFromPage = new ArrayList();
                    List<TestCaseStepActionControl> tcsacFromPage = new ArrayList();

                    int nextStepNumber = getMaxStepNumber(tcsFromPage);
                    for (TestCaseStep tcs : tcsFromPage) {
                        if (tcs.getStep() == -1) {
                            tcs.setStep(++nextStepNumber);
                        }

                        if (tcs.getTestCaseStepAction() != null) {
                            int nextSequenceNumber = getMaxSequenceNumber(tcs.getTestCaseStepAction());
                            for (TestCaseStepAction tcsa : tcs.getTestCaseStepAction()) {
                                if (tcsa.getSequence() == -1) {
                                    tcsa.setSequence(++nextSequenceNumber);
                                }
                                tcsa.setStep(tcs.getStep());

                                if (tcsa.getTestCaseStepActionControl() != null) {
                                    int nextControlNumber = getMaxControlNumber(tcsa.getTestCaseStepActionControl());
                                    for (TestCaseStepActionControl tscac : tcsa.getTestCaseStepActionControl()) {
                                        if (tscac.getControlSequence() == -1) {
                                            tscac.setControlSequence(++nextControlNumber);
                                        }
                                        tscac.setStep(tcs.getStep());
                                        tscac.setSequence(tcsa.getSequence());
                                    }
                                    tcsacFromPage.addAll(tcsa.getTestCaseStepActionControl());
                                }
                            }
                            tcsaFromPage.addAll(tcs.getTestCaseStepAction());
                        }
                    }

                    /*
                    * Create, update or delete step, action and control according to the needs
                     */
                    List<TestCaseStep> tcsFromDtb = new ArrayList(tcsService.getListOfSteps(initialTest, initialTestCase));
                    tcsService.compareListAndUpdateInsertDeleteElements(tcsFromPage, tcsFromDtb, duplicate);

                    List<TestCaseStepAction> tcsaFromDtb = new ArrayList(tcsaService.findTestCaseStepActionbyTestTestCase(initialTest, initialTestCase));
                    tcsaService.compareListAndUpdateInsertDeleteElements(tcsaFromPage, tcsaFromDtb, duplicate);

                    List<TestCaseStepActionControl> tcsacFromDtb = new ArrayList(tcsacService.findControlByTestTestCase(initialTest, initialTestCase));
                    tcsacService.compareListAndUpdateInsertDeleteElements(tcsacFromPage, tcsacFromDtb, duplicate);

                    /**
                     * Adding Log entry.
                     */
                    if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        /**
                         * Update was succesfull. Adding Log entry.
                         */
                        ILogEventService logEventService = appContext.getBean(LogEventService.class);
                        logEventService.createPrivateCalls("/UpdateTestCaseWithDependencies1", "UPDATE", "Update testcase : ['" + tc.getTest() + "'|'" + tc.getTestCase() + "']", request);
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

    /**
     * Create new TestCase object from the information of request form
     *
     * @param request information from the request page
     * @return TestCase object
     * @see org.cerberus.crud.entity.TestCase
     */
    private TestCase getTestCaseFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase) {

        IFactoryTestCase testCaseFactory = appContext.getBean(IFactoryTestCase.class);
        String origin = request.getParameter("editOrigin");
        String refOrigin = request.getParameter("editRefOrigin");
        String creator = request.getParameter("editCreator");
        String implementer = request.getParameter("editImplementer");
        String lastModifier = request.getUserPrincipal().getName();
        String project = request.getParameter("editProject");
        String ticket = request.getParameter("editTicket");
        String application = request.getParameter("editApplication");
        String runQA = request.getParameter("editRunQA");
        String runUAT = request.getParameter("editRunUAT");
        String runPROD = request.getParameter("editRunPROD");
        Integer priority = Integer.parseInt(request.getParameter("editPriority"));
        String group = request.getParameter("editGroup");
        String status = request.getParameter("editStatus");
        String shortDescription = HtmlUtils.htmlEscape(request.getParameter("editDescription"));
        String description = request.getParameter("valueDetail");
        String howTo = request.getParameter("howtoDetail");
        String active = request.getParameter("editTcActive");
        String conditionOper = request.getParameter("editConditionOper");
        String conditionVal1 = request.getParameter("editConditionVal1");
        String fromSprint = request.getParameter("editFromBuild");
        String fromRevision = request.getParameter("editFromRev");
        String toSprint = request.getParameter("editToBuild");
        String toRevision = request.getParameter("editToRev");
        String bugID = request.getParameter("editBugID");
        String targetSprint = request.getParameter("editTargetBuild");
        String targetRevision = request.getParameter("editTargetRev");
        String comment = HtmlUtils.htmlEscape(request.getParameter("editComment"));
        String function = request.getParameter("editFunction");
        String userAgent = request.getParameter("editUserAgent");
        return testCaseFactory.create(test, testCase, origin, refOrigin, creator, implementer, lastModifier, project, ticket, function, application,
                runQA, runUAT, runPROD, priority, group, status, shortDescription, description, howTo, active, conditionOper, conditionVal1, fromSprint, fromRevision, toSprint,
                toRevision, null, bugID, targetSprint, targetRevision, comment, userAgent, null, null, null, null);
    }

    private List<TestCaseCountry> getTestCaseCountryFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase) {
        IFactoryTestCaseCountry testCaseCountryFactory = appContext.getBean(IFactoryTestCaseCountry.class);
        List<TestCaseCountry> countries = new ArrayList<TestCaseCountry>();
        if (request.getParameterValues("editTestCaseCountry") != null) {
            for (String country : request.getParameterValues("editTestCaseCountry")) {
                countries.add(testCaseCountryFactory.create(test, testCase, country));
            }
        }
        return countries;
    }

    /**
     * FIXME this method do nothing more than return
     * request.getParameter(parameter)!
     */
    private String getParameterIfExists(HttpServletRequest request, String parameter) {
        String result = null;
        if (request.getParameter(parameter) != null) {
            result = request.getParameter(parameter);
        }
        return result;
    }

    /**
     * FIXME this method do nothing more than return
     * request.getParameterValues(parameter)!
     */
    private String[] getParameterValuesIfExists(HttpServletRequest request, String parameter) {
        String[] result = null;
        if (request.getParameterValues(parameter) != null) {
            result = request.getParameterValues(parameter);
        }
        return result;
    }

    private List<TestCaseCountryProperties> getTestCaseCountryPropertiesFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase) throws JSONException {
        List<TestCaseCountryProperties> testCaseCountryProp = new ArrayList();
//        String[] testcase_properties_increment = getParameterValuesIfExists(request, "property_increment");
        IFactoryTestCaseCountryProperties testCaseCountryPropertiesFactory = appContext.getBean(IFactoryTestCaseCountryProperties.class);
        JSONArray properties = new JSONArray(request.getParameter("propArr"));

        for (int i = 0; i < properties.length(); i++) {
            JSONObject propJson = properties.getJSONObject(i);

            boolean delete = propJson.getBoolean("toDelete");
            String property = propJson.getString("property");
            String description = propJson.getString("description");
            String type = propJson.getString("type");
            String value = propJson.getString("value1");
            String value2 = propJson.getString("value2");
            int length = propJson.getInt("length");
            int rowLimit = propJson.getInt("rowLimit");
            int retryNb = propJson.optInt("retryNb");
            int retryPeriod = propJson.optInt("retryPeriod");
            String nature = propJson.getString("nature");
            String database = propJson.getString("database");
            JSONArray countries = propJson.getJSONArray("country");
            if (!delete && !property.equals("")) {
                for (int j = 0; j < countries.length(); j++) {
                    String country = countries.getString(j);

                    testCaseCountryProp.add(testCaseCountryPropertiesFactory.create(test, testCase, country, property, description, type, database, value, value2, length, rowLimit, nature, retryNb, retryPeriod));
                }
            }
        }
        return testCaseCountryProp;
    }

    private List<TestCaseStep> getTestCaseStepFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, boolean duplicate) throws JSONException {
        List<TestCaseStep> testCaseStep = new ArrayList();
        ITestCaseStepService tcsService = appContext.getBean(ITestCaseStepService.class);
        IFactoryTestCaseStep testCaseStepFactory = appContext.getBean(IFactoryTestCaseStep.class);
        JSONArray stepArray = new JSONArray(request.getParameter("stepArray"));

        for (int i = 0; i < stepArray.length(); i++) {
            JSONObject step = stepArray.getJSONObject(i);

            boolean delete = step.getBoolean("toDelete");
            int stepNumber = step.isNull("step") ? -1 : step.getInt("step");
            int sort = step.isNull("sort") ? -1 : step.getInt("sort");
            String conditionOper = step.getString("conditionOper");
            String conditionVal1 = step.getString("conditionVal1");
            String description = step.getString("description");
            String useStep = step.getString("useStep");
            String useStepTest = step.getString("useStepTest");
            String useStepTestCase = step.getString("useStepTestCase");
            int useStepStep = step.getInt("useStepStep");
            String inLibrary = step.getString("inLibrary");
            JSONArray stepActions = step.getJSONArray("actionArr");

            if (!delete) {
                TestCaseStep tcStep = testCaseStepFactory.create(test, testCase, stepNumber, sort, conditionOper, conditionVal1, description, useStep, useStepTest, useStepTestCase, useStepStep, inLibrary);

                if (useStep.equals("N")) {
                    tcStep.setTestCaseStepAction(getTestCaseStepActionFromParameter(request, appContext, test, testCase, stepActions));
                } else {
                    TestCaseStep tcs = null;
                    if (useStepStep != -1 && !useStepTest.equals("") && !useStepTestCase.equals("")) {
                        tcs = tcsService.findTestCaseStep(useStepTest, useStepTestCase, useStepStep);
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
        List<TestCaseStepAction> testCaseStepAction = new ArrayList();
        IFactoryTestCaseStepAction testCaseStepActionFactory = appContext.getBean(IFactoryTestCaseStepAction.class);

        for (int i = 0; i < testCaseStepActionJson.length(); i++) {
            JSONObject tcsaJson = testCaseStepActionJson.getJSONObject(i);

            boolean delete = tcsaJson.getBoolean("toDelete");
            int step = tcsaJson.isNull("step") ? -1 : tcsaJson.getInt("step");
            int sequence = tcsaJson.isNull("sequence") ? -1 : tcsaJson.getInt("sequence");
            int sort = tcsaJson.isNull("sort") ? -1 : tcsaJson.getInt("sort");
            String conditionOper = tcsaJson.getString("conditionOper");
            String conditionVal = tcsaJson.getString("conditionVal");
            String action = tcsaJson.getString("action");
            String object = tcsaJson.getString("object");
            String property = tcsaJson.getString("property");
            String forceExeStatus = tcsaJson.getString("forceExeStatus");
            String description = tcsaJson.getString("description");
            String screenshot = tcsaJson.getString("screenshotFileName");
            JSONArray controlArray = tcsaJson.getJSONArray("controlArr");

            if (!delete) {
                TestCaseStepAction tcsa = testCaseStepActionFactory.create(test, testCase, step, sequence, sort, conditionOper, conditionVal, action, object, property, forceExeStatus, description, screenshot);
                tcsa.setTestCaseStepActionControl(getTestCaseStepActionControlFromParameter(request, appContext, test, testCase, controlArray));
                testCaseStepAction.add(tcsa);
            }
        }
        return testCaseStepAction;
    }

    private List<TestCaseStepActionControl> getTestCaseStepActionControlFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, JSONArray controlArray) throws JSONException {
        List<TestCaseStepActionControl> testCaseStepActionControl = new ArrayList();
        IFactoryTestCaseStepActionControl testCaseStepActionControlFactory = appContext.getBean(IFactoryTestCaseStepActionControl.class);

        for (int i = 0; i < controlArray.length(); i++) {
            JSONObject controlJson = controlArray.getJSONObject(i);

            boolean delete = controlJson.getBoolean("toDelete");
            int step = controlJson.isNull("step") ? -1 : controlJson.getInt("step");
            int sequence = controlJson.isNull("sequence") ? -1 : controlJson.getInt("sequence");
            int control = controlJson.isNull("controlSequence") ? -1 : controlJson.getInt("controlSequence");
            int sort = controlJson.isNull("sort") ? -1 : controlJson.getInt("sort");
            String conditionOper = controlJson.isNull("conditionOper") ? "always" : controlJson.getString("conditionOper");
            String conditionVal1 = controlJson.isNull("conditionVal1") ? "" : controlJson.getString("conditionVal1");
            String type = controlJson.getString("objType");
            String controlValue = controlJson.getString("control");
            String value1 = controlJson.getString("value1");
            String value2 = controlJson.getString("value2");
            String fatal = controlJson.getString("fatal");
            String description = controlJson.getString("description");
            String screenshot = controlJson.getString("screenshotFileName");
            if (!delete) {
                testCaseStepActionControl.add(testCaseStepActionControlFactory.create(test, testCase, step, sequence, control, sort, conditionOper, conditionVal1, controlValue, value1, value2, fatal, description, screenshot));
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
            java.util.logging.Logger.getLogger(UpdateTestCaseWithDependencies1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(UpdateTestCaseWithDependencies1.class.getName()).log(Level.SEVERE, null, ex);
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
            java.util.logging.Logger.getLogger(UpdateTestCaseWithDependencies1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(UpdateTestCaseWithDependencies1.class.getName()).log(Level.SEVERE, null, ex);
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
