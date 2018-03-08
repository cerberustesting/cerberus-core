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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseLabel;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.crud.factory.IFactoryTestCaseCountry;
import org.cerberus.crud.factory.IFactoryTestCaseLabel;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.crud.service.ITestCaseCountryService;
import org.cerberus.crud.service.ITestCaseLabelService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.ITestCaseStepActionControlService;
import org.cerberus.crud.service.ITestCaseStepActionService;
import org.cerberus.crud.service.ITestCaseStepService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerUtil;
import org.cerberus.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author cerberus
 */
@WebServlet(name = "CreateTestCase", urlPatterns = {"/CreateTestCase"})
public class CreateTestCase extends HttpServlet {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(UpdateTestCase.class);

    private ITestCaseLabelService testCaseLabelService;
    private IFactoryTestCaseLabel testCaseLabelFactory;
    private ITestCaseCountryService testCaseCountryService;
    private IFactoryTestCaseCountry testCaseCountryFactory;
    private ITestCaseService testCaseService;
    private ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
    private ITestCaseStepService testCaseStepService;
    private ITestCaseStepActionService testCaseStepActionService;
    private ITestCaseStepActionControlService testCaseStepActionControlService;

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
        String test = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("test"), "");
        String testcase = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("testCase"), "");
        String originalTest = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("originalTest"), "");
        String originalTestCase = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("originalTestCase"), "");

        // Prepare the final answer.
        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);
        /**
         * Checking all constrains before calling the services.
         */
        if (StringUtil.isNullOrEmpty(test) && StringUtil.isNullOrEmpty(testcase)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Case")
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "mandatory fields (test or testcase) are missing."));
            finalAnswer.setResultMessage(msg);

        } else {
            /**
             * All data seems cleans so we can call the services.
             */
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            testCaseService = appContext.getBean(ITestCaseService.class);
            testCaseLabelService = appContext.getBean(ITestCaseLabelService.class);
            testCaseLabelFactory = appContext.getBean(IFactoryTestCaseLabel.class);
            testCaseCountryService = appContext.getBean(ITestCaseCountryService.class);
            testCaseCountryFactory = appContext.getBean(IFactoryTestCaseCountry.class);
            testCaseCountryPropertiesService = appContext.getBean(ITestCaseCountryPropertiesService.class);
            testCaseStepService = appContext.getBean(ITestCaseStepService.class);
            testCaseStepActionService = appContext.getBean(ITestCaseStepActionService.class);
            testCaseStepActionControlService = appContext.getBean(ITestCaseStepActionControlService.class);

            TestCase testCaseData = getTestCaseFromRequest(request);
            ans = testCaseService.create(testCaseData);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);

            if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                /**
                 * Object created. Adding Log entry.
                 */
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                logEventService.createForPrivateCalls("/CreateTestCase", "CREATE", "Create TestCase : ['" + testcase + "']", request);

                // Update labels
                if (request.getParameter("labelList") != null) {
                    JSONArray objLabelArray = new JSONArray(request.getParameter("labelList"));
                    List<TestCaseLabel> labelList = new ArrayList();
                    labelList = getLabelListFromRequest(request, appContext, test, testcase, objLabelArray);

                    // Update the Database with the new list.
                    ans = testCaseLabelService.compareListAndUpdateInsertDeleteElements(test, testcase, labelList);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
                }

                // Update Countries
                if (request.getParameter("countryList") != null) {
                    JSONArray objCountryArray = new JSONArray(request.getParameter("countryList"));
                    List<TestCaseCountry> tccList = new ArrayList();
                    tccList = getCountryListFromRequest(request, appContext, test, testcase, objCountryArray);

                    // Update the Database with the new list.
                    ans = testCaseCountryService.compareListAndUpdateInsertDeleteElements(test, testcase, tccList);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);

                    // Duplicate other objects.
                    List<TestCaseCountryProperties> tccpList = new ArrayList();
                    List<TestCaseCountryProperties> newTccpList = new ArrayList();
                    if (!tccList.isEmpty() && ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        tccpList = testCaseCountryPropertiesService.findListOfPropertyPerTestTestCase(originalTest, originalTestCase);
                        //Build a new list with the countries that exist for the testcase.
                        for (TestCaseCountryProperties curTccp : tccpList) {
                            if (testCaseCountryService.exist(test, testcase, curTccp.getCountry())) {
                                newTccpList.add(curTccp);
                            }
                        }
                        if (!newTccpList.isEmpty()) {
                            ans = testCaseCountryPropertiesService.duplicateList(newTccpList, test, testcase);
                            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
                        }
                    }

                }

                List<TestCaseStep> tcsList = new ArrayList();
                if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    tcsList = testCaseStepService.getListOfSteps(originalTest, originalTestCase);
                    if (!tcsList.isEmpty()) {
                        ans = testCaseStepService.duplicateList(tcsList, test, testcase);
                        finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
                    }
                }

                List<TestCaseStepAction> tcsaList = new ArrayList();
                if (!tcsList.isEmpty() && ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    tcsaList = testCaseStepActionService.findTestCaseStepActionbyTestTestCase(originalTest, originalTestCase);
                    if (!tcsaList.isEmpty()) {
                        ans = testCaseStepActionService.duplicateList(tcsaList, test, testcase);
                        finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
                    }
                }

                if (!tcsList.isEmpty() && !tcsaList.isEmpty() && ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    List<TestCaseStepActionControl> tcsacList = testCaseStepActionControlService.findControlByTestTestCase(originalTest, originalTestCase);
                    if (!tcsacList.isEmpty()) {
                        ans = testCaseStepActionControlService.duplicateList(tcsacList, test, testcase);
                        finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
                    }
                }

            }
        }

        /**
         * Formating and returning the json result.
         */
        jsonResponse.put("messageType", finalAnswer.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", finalAnswer.getResultMessage().getDescription());

        response.getWriter().print(jsonResponse);
        response.getWriter().flush();
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

    private TestCase getTestCaseFromRequest(HttpServletRequest request) throws CerberusException, JSONException {
        TestCase tc = new TestCase();

        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String charset = request.getCharacterEncoding();

        // Parameter that needs to be secured --> We SECURE+DECODE them
        tc.setImplementer(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("implementer"), "", charset));
        tc.setUsrCreated(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getUserPrincipal().getName(), "", charset));
        tc.setUsrModif(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getUserPrincipal().getName(), "", charset));
        if (StringUtils.isEmpty(request.getParameter("project"))) {
            tc.setProject(null);
        } else {
            tc.setProject(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("project"), "", charset));
        }
        tc.setApplication(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("application"), "", charset));
        tc.setActiveQA(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("activeQA"), "", charset));
        tc.setActiveUAT(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("activeUAT"), "", charset));
        tc.setActivePROD(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("activeProd"), "", charset));
        tc.setFromBuild(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("fromSprint"), "", charset));
        tc.setFromRev(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("fromRev"), "", charset));
        tc.setToBuild(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("toSprint"), "", charset));
        tc.setToRev(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("toRev"), "", charset));
        tc.setTcActive(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("active"), "", charset));
        tc.setTargetBuild(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("targetSprint"), "", charset));
        tc.setTargetRev(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("targetRev"), "", charset));
        tc.setPriority(ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("priority"), 0, charset));
        tc.setTest(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("test"), "", charset));
        tc.setTestCase(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("testCase"), "", charset));
        tc.setTicket(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("ticket"), "", charset));
        tc.setOrigine(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("origin"), "", charset));
        tc.setRefOrigine(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("refOrigin"), "", charset));
        tc.setGroup(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("group"), "", charset));
        tc.setStatus(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("status"), "", charset));
        tc.setDescription(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("shortDesc"), "", charset));
        tc.setBugID(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("bugId"), "", charset));
        tc.setComment(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("comment"), "", charset));
        tc.setFunction(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("function"), "", charset));
        tc.setTestCaseVersion(0);
        // Parameter that we cannot secure as we need the html --> We DECODE them
        tc.setHowTo(ParameterParserUtil.parseStringParamAndDecode(request.getParameter("howTo"), "", charset));
        tc.setBehaviorOrValueExpected(ParameterParserUtil.parseStringParamAndDecode(request.getParameter("behaviorOrValueExpected"), "", charset));

        return tc;
    }

    private List<TestCaseCountry> getCountryListFromRequest(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, JSONArray json) throws CerberusException, JSONException, UnsupportedEncodingException {
        List<TestCaseCountry> tdldList = new ArrayList();

        for (int i = 0; i < json.length(); i++) {
            JSONObject objectJson = json.getJSONObject(i);

            // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
            boolean delete = objectJson.getBoolean("toDelete");
            String country = objectJson.getString("country");
            // Parameter that needs to be secured --> We SECURE+DECODE them
            // NONE
            // Parameter that we cannot secure as we need the html --> We DECODE them

            if (!delete) {
                TestCaseCountry tcc = testCaseCountryFactory.create(test, testCase, country);
                tdldList.add(tcc);
            }
        }
        return tdldList;
    }

    private List<TestCaseLabel> getLabelListFromRequest(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, JSONArray json) throws CerberusException, JSONException, UnsupportedEncodingException {
        List<TestCaseLabel> labelList = new ArrayList();

        for (int i = 0; i < json.length(); i++) {
            JSONObject objectJson = json.getJSONObject(i);

            // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
            boolean delete = objectJson.getBoolean("toDelete");
            Integer labelId = objectJson.getInt("labelId");

            Timestamp creationDate = new Timestamp(new Date().getTime());

            if (!delete) {
                labelList.add(testCaseLabelFactory.create(0, test, testCase, labelId, request.getRemoteUser(), creationDate, request.getRemoteUser(), creationDate, null));
            }
        }
        return labelList;
    }

}
