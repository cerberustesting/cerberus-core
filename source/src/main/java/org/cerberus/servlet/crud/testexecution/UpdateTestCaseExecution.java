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
package org.cerberus.servlet.crud.testexecution;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.crud.entity.TestCaseStepExecution;
import org.cerberus.crud.factory.IFactoryTestCaseStepActionControl;
import org.cerberus.crud.factory.IFactoryTestCaseStepActionControlExecution;
import org.cerberus.crud.factory.IFactoryTestCaseStepExecution;
import org.cerberus.crud.service.ITestCaseExecutionService;
import org.cerberus.crud.service.ITestCaseStepActionControlExecutionService;
import org.cerberus.crud.service.ITestCaseStepActionExecutionService;
import org.cerberus.crud.service.ITestCaseStepExecutionService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
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

@WebServlet(name = "UpdateTestCaseExecution", urlPatterns = {"/UpdateTestCaseExecution"})
public class UpdateTestCaseExecution extends HttpServlet {

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
            throws ServletException, IOException, JSONException {
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
        long executionId = jObj.getLong("executionId");
        // TODO Save Properties
        // JSONArray properties = jObj.getJSONArray("propArr");
        JSONArray stepArray = jObj.getJSONArray("stepArray");
        
        System.out.println(executionId);
        System.out.println(stepArray);
        
//        /**
//         * Checking all constrains before calling the services.
//         */
//        if (StringUtil.isNullOrEmpty(executionId)) {
//            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
//            msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Case Execution")
//                    .replace("%OPERATION%", "Update")
//                    .replace("%REASON%", "mendatory fields are missing."));
//            ans.setResultMessage(msg);
//        } else {
//            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
//            ITestCaseExecutionService testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
//            // TODO Save Properties
//            // ITestCaseCountryPropertiesService tccpService = appContext.getBean(ITestCaseCountryPropertiesService.class);
//            ITestCaseStepExecutionService tcseService = appContext.getBean(ITestCaseStepExecutionService.class);
//            ITestCaseStepActionExecutionService tcsaeService = appContext.getBean(ITestCaseStepActionExecutionService.class);
//            ITestCaseStepActionControlExecutionService tcsaceService = appContext.getBean(ITestCaseStepActionControlExecutionService.class);
//
//            AnswerItem resp = testCaseExecutionService.readByKey(executionId);
//            TestCaseExecution tce = (TestCaseExecution) resp.getItem();
//            if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
//                /**
//                 * Object could not be found. We stop here and report the error.
//                 */
//                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
//                msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseExecution")
//                        .replace("%OPERATION%", "Update")
//                        .replace("%REASON%", "TestCaseExecution "+ executionId +" does not exist."));
//                ans.setResultMessage(msg);
//
//            } else /**
//             * The service was able to perform the query and confirm the object
//             * exist, then we can update it.
//             */
//            if (!testCaseExecutionService.hasPermissionsUpdate(tce, request)) { // We cannot update the testcase if the user is not at least in RunTest role.
//                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
//                msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCase")
//                        .replace("%OPERATION%", "Update")
//                        .replace("%REASON%", "Not enought privilege to update the testcase. You mut belong to Test Privilege or even TestAdmin in case the test is in WORKING status."));
//                ans.setResultMessage(msg);
//
//            } else {
//
//                // TODO Save properties
//                // Test Case Execution exist and we can update it so Global update start here //
//                /**
//                 * TestcaseExecutionData Update.
//                 */
//                //List<TestCaseCountryProperties> tccpFromPage = getTestCaseCountryPropertiesFromParameter(request, appContext, test, testCase, properties);
//                //tccpService.compareListAndUpdateInsertDeleteElements(initialTest, initialTestCase, tccpFromPage);
//
//                /*
//                 * Get steps, actions and controls from page by:
//                 * - generating a new step, action or control number,
//                 * - setting the correct related step and action for action or control
//                 */
//                List<TestCaseStepExecution> tcseFromPage = getTestCaseStepExecutionFromParameter(request, appContext, executionId, stepArray);
//                List<TestCaseStepActionExecution> tcsaeFromPage = new ArrayList();
//                List<TestCaseStepActionControlExecution> tcsaceFromPage = new ArrayList();
//
//                int nextStepNumber = getMaxStepNumber(tcsFromPage);
//                for (TestCaseStep tcs : tcsFromPage) {
//                    if (tcs.getStep() == -1) {
//                        tcs.setStep(++nextStepNumber);
//                    }
//
//                    if (tcs.getTestCaseStepAction() != null) {
//                        int nextSequenceNumber = getMaxSequenceNumber(tcs.getTestCaseStepAction());
//                        for (TestCaseStepAction tcsa : tcs.getTestCaseStepAction()) {
//                            if (tcsa.getSequence() == -1) {
//                                tcsa.setSequence(++nextSequenceNumber);
//                            }
//                            tcsa.setStep(tcs.getStep());
//
//                            if (tcsa.getTestCaseStepActionControl() != null) {
//                                int nextControlNumber = getMaxControlNumber(tcsa.getTestCaseStepActionControl());
//                                for (TestCaseStepActionControl tscac : tcsa.getTestCaseStepActionControl()) {
//                                    if (tscac.getControlSequence() == -1) {
//                                        tscac.setControlSequence(++nextControlNumber);
//                                    }
//                                    tscac.setStep(tcs.getStep());
//                                    tscac.setSequence(tcsa.getSequence());
//                                }
//                                tcsacFromPage.addAll(tcsa.getTestCaseStepActionControl());
//                            }
//                        }
//                        tcsaFromPage.addAll(tcs.getTestCaseStepAction());
//                    }
//                }
//
//                /*
//                 * Create, update or delete step, action and control according to the needs
//                 */
//                List<TestCaseStep> tcsFromDtb = new ArrayList(tcsService.getListOfSteps(initialTest, initialTestCase));
//                tcsService.compareListAndUpdateInsertDeleteElements(tcsFromPage, tcsFromDtb, duplicate);
//
//                List<TestCaseStepAction> tcsaFromDtb = new ArrayList(tcsaService.findTestCaseStepActionbyTestTestCase(initialTest, initialTestCase));
//                tcsaService.compareListAndUpdateInsertDeleteElements(tcsaFromPage, tcsaFromDtb, duplicate);
//
//                List<TestCaseStepActionControl> tcsacFromDtb = new ArrayList(tcsacService.findControlByTestTestCase(initialTest, initialTestCase));
//                tcsacService.compareListAndUpdateInsertDeleteElements(tcsacFromPage, tcsacFromDtb, duplicate);
//
//                /**
//                 * Adding Log entry.
//                 */
//                if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
//                    /**
//                     * Update was succesfull. Adding Log entry.
//                     */
//                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
//                    logEventService.createForPrivateCalls("/UpdateTestCaseWithDependencies1", "UPDATE", "Update testcase : ['" + tc.getTest() + "'|'" + tc.getTestCase() + "']", request);
//                }
//
//            }
//        }
//
//        /**
//         * Formating and returning the json result.
//         */
//        jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
//        jsonResponse.put("message", ans.getResultMessage().getDescription());
//
//        response.getWriter().print(jsonResponse);
//        response.getWriter().flush();
//
//    }
//
//    
//
////    private List<TestCaseCountryProperties> getTestCaseCountryPropertiesFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, JSONArray properties) throws JSONException {
////        List<TestCaseCountryProperties> testCaseCountryProp = new ArrayList();
//////        String[] testcase_properties_increment = getParameterValuesIfExists(request, "property_increment");
////        IFactoryTestCaseCountryProperties testCaseCountryPropertiesFactory = appContext.getBean(IFactoryTestCaseCountryProperties.class);
////        for (int i = 0; i < properties.length(); i++) {
////            JSONObject propJson = properties.getJSONObject(i);
////            boolean delete = propJson.getBoolean("toDelete");
////            String property = propJson.getString("property");
////            String description = propJson.getString("description");
////            String type = propJson.getString("type");
////            String value = propJson.getString("value1");
////            String value2 = propJson.getString("value2");
////            int length = propJson.getInt("length");
////            int rowLimit = propJson.getInt("rowLimit");
////            int retryNb = propJson.optInt("retryNb");
////            int retryPeriod = propJson.optInt("retryPeriod");
////            String nature = propJson.getString("nature");
////            String database = propJson.getString("database");
////            JSONArray countries = propJson.getJSONArray("country");
////            if (!delete && !property.equals("")) {
////                for (int j = 0; j < countries.length(); j++) {
////                    String country = countries.getString(j);
////
////                    testCaseCountryProp.add(testCaseCountryPropertiesFactory.create(test, testCase, country, property, description, type, database, value, value2, length, rowLimit, nature, retryNb, retryPeriod));
////                }
////            }
////        }
////        return testCaseCountryProp;
////    }
////
//    private List<TestCaseStepExecution> getTestCaseStepExecutionFromParameter(HttpServletRequest request, ApplicationContext appContext, long executionId, JSONArray stepArray) throws JSONException {
//        List<TestCaseStepExecution> testCaseStepExecution = new ArrayList();
//        ITestCaseStepExecutionService tcseService = appContext.getBean(ITestCaseStepExecutionService.class);
//        IFactoryTestCaseStepExecution testCaseStepExecutionFactory = appContext.getBean(IFactoryTestCaseStepExecution.class);
//        
//        for (int i = 0; i < stepArray.length(); i++) {
//            JSONObject step = stepArray.getJSONObject(i);
//
//            int stepNumber = step.isNull("step") ? -1 : step.getInt("step");
//            int sort = step.isNull("sort") ? -1 : step.getInt("sort");
//            String loop = step.getString("loop");
//            String conditionOper = step.getString("conditionOper");
//            String conditionVal1 = step.getString("conditionVal1");
//            String conditionVal2 = step.getString("conditionVal2");
//            String description = step.getString("description");
//            String useStep = step.getString("useStep");
//            String useStepTest = step.getString("useStepTest");
//            String useStepTestCase = step.getString("useStepTestCase");
//            int useStepStep = step.getInt("useStepStep");
//            String inLibrary = step.getString("inLibrary");
//            JSONArray stepActions = step.getJSONArray("actionArr");
//
//            if (!delete) {
//                TestCaseStep tcStep = testCaseStepFactory.create(test, testCase, stepNumber, sort, loop, conditionOper, conditionVal1, conditionVal2, description, useStep, useStepTest, useStepTestCase, useStepStep, inLibrary);
//
//                if (useStep.equals("N")) {
//                    tcStep.setTestCaseStepAction(getTestCaseStepActionFromParameter(request, appContext, test, testCase, stepActions));
//                } else {
//                    TestCaseStep tcs = null;
//                    if (useStepStep != -1 && !useStepTest.equals("") && !useStepTestCase.equals("")) {
//                        tcs = tcsService.findTestCaseStep(useStepTest, useStepTestCase, useStepStep);
//                        if (tcs != null) {
//                            tcStep.setUseStepTest(tcs.getTest());
//                            tcStep.setUseStepTestCase(tcs.getTestCase());
//                            tcStep.setUseStepStep(tcs.getStep());
//                        }
//                    }
//                }
//                testCaseStep.add(tcStep);
//            }
//        }
//        return testCaseStep;
//    }
//
//    private List<TestCaseStepAction> getTestCaseStepActionFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, JSONArray testCaseStepActionJson) throws JSONException {
//        List<TestCaseStepAction> testCaseStepAction = new ArrayList();
//        IFactoryTestCaseStepAction testCaseStepActionFactory = appContext.getBean(IFactoryTestCaseStepAction.class);
//
//        for (int i = 0; i < testCaseStepActionJson.length(); i++) {
//            JSONObject tcsaJson = testCaseStepActionJson.getJSONObject(i);
//
//            boolean delete = tcsaJson.getBoolean("toDelete");
//            int step = tcsaJson.isNull("step") ? -1 : tcsaJson.getInt("step");
//            int sequence = tcsaJson.isNull("sequence") ? -1 : tcsaJson.getInt("sequence");
//            int sort = tcsaJson.isNull("sort") ? -1 : tcsaJson.getInt("sort");
//            String conditionOper = tcsaJson.getString("conditionOper");
//            String conditionVal1 = tcsaJson.getString("conditionVal1");
//            String conditionVal2 = tcsaJson.getString("conditionVal2");
//            String action = tcsaJson.getString("action");
//            String object = tcsaJson.getString("object");
//            String property = tcsaJson.getString("property");
//            String forceExeStatus = tcsaJson.getString("forceExeStatus");
//            String description = tcsaJson.getString("description");
//            String screenshot = tcsaJson.getString("screenshotFileName");
//            JSONArray controlArray = tcsaJson.getJSONArray("controlArr");
//
//            if (!delete) {
//                TestCaseStepAction tcsa = testCaseStepActionFactory.create(test, testCase, step, sequence, sort, conditionOper, conditionVal1, conditionVal2, action, object, property, forceExeStatus, description, screenshot);
//                tcsa.setTestCaseStepActionControl(getTestCaseStepActionControlFromParameter(request, appContext, test, testCase, controlArray));
//                testCaseStepAction.add(tcsa);
//            }
//        }
//        return testCaseStepAction;
//    }
//
//    private List<TestCaseStepActionControlExecution> getTestCaseStepActionControlExecutionFromParameter(HttpServletRequest request, ApplicationContext appContext, long id,
//            String test, String testCase, JSONArray controlArray) throws JSONException {
//        List<TestCaseStepActionControlExecution> testCaseStepActionControlExecution = new ArrayList();
//        IFactoryTestCaseStepActionControlExecution testCaseStepActionControlExecutionFactory = appContext.getBean(IFactoryTestCaseStepActionControlExecution.class);
//
//        for (int i = 0; i < controlArray.length(); i++) {
//            JSONObject controlJson = controlArray.getJSONObject(i);
//
//            boolean delete = controlJson.getBoolean("toDelete");
//            int step = controlJson.isNull("step") ? -1 : controlJson.getInt("step");
//            int sequence = controlJson.isNull("sequence") ? -1 : controlJson.getInt("sequence");
//            int control = controlJson.isNull("controlSequence") ? -1 : controlJson.getInt("controlSequence");
//            int sort = controlJson.isNull("sort") ? -1 : controlJson.getInt("sort");
//            String conditionOper = controlJson.isNull("conditionOper") ? "always" : controlJson.getString("conditionOper");
//            String conditionVal1 = controlJson.isNull("conditionVal1") ? "" : controlJson.getString("conditionVal1");
//            String conditionVal2 = controlJson.isNull("conditionVal2") ? "" : controlJson.getString("conditionVal2");
//            String type = controlJson.getString("objType");
//            String controlValue = controlJson.getString("control");
//            String value1 = controlJson.getString("value1");
//            String value2 = controlJson.getString("value2");
//            String fatal = controlJson.getString("fatal");
//            String description = controlJson.getString("description");
//            String screenshot = controlJson.getString("screenshotFileName");
//            if (!delete) {
//                testCaseStepActionControlExecution.add(testCaseStepActionControlExecutionFactory.create(id, test, testCase, step, step, sequence, 
//                        sequence, sort, testCase, testCase, conditionOper, conditionVal1, conditionVal2, conditionVal1, conditionVal2, fatal, 
//                        value1, value2, value1, value2, fatal, sort, i, sort, sort, description, testCaseStepActionExecution, resultMessage);
//                
//                        long id, String test, String testCase, int step, int index, int sequence, 
//         int controlSequence, int sort, String returnCode, String returnMessage, String conditionOper, String conditionVal1Init, String conditionVal2Init, String conditionVal1, String conditionVal2, String control, String value1Init, String value2Init, String value1, String value2, String fatal, 
//         long start, long end, long startLong, long endLong, String description, TestCaseStepActionExecution testCaseStepActionExecution, MessageEvent resultMessage
//            }
//        }
//        return testCaseStepActionControlExecution;
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
        } catch (JSONException ex) {
            Logger.getLogger(UpdateTestCaseExecution.class.getName()).log(Level.SEVERE, null, ex);
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
        } catch (JSONException ex) {
            Logger.getLogger(UpdateTestCaseExecution.class.getName()).log(Level.SEVERE, null, ex);
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
