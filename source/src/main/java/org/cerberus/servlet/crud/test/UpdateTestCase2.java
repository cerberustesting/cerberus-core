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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
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
import org.cerberus.crud.entity.TestCaseLabel;
import org.cerberus.crud.factory.IFactoryTestCaseCountry;
import org.cerberus.crud.factory.IFactoryTestCaseLabel;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ITestCaseCountryService;
import org.cerberus.crud.service.ITestCaseLabelService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
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
 *
 * @author cerberus
 */
@WebServlet(name = "UpdateTestCase2", urlPatterns = {"/UpdateTestCase2"})
public class UpdateTestCase2 extends HttpServlet {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(UpdateTestCase2.class);
    
    private ITestCaseLabelService testCaseLabelService;
    private IFactoryTestCaseLabel testCaseLabelFactory;
    private ITestCaseCountryService testCaseCountryService;
    private IFactoryTestCaseCountry testCaseCountryFactory;
    private ITestCaseService testCaseService;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws org.json.JSONException
     * @throws org.cerberus.exception.CerberusException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, JSONException, CerberusException {
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
        String testCase = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("testCase"), "");

        // Prepare the final answer.
        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);
        /**
         * Checking all constrains before calling the services.
         */
        if ((StringUtil.isNullOrEmpty(test)) || (StringUtil.isNullOrEmpty(testCase))) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Case")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "mandatory fields (test or testcase) are missing."));
            finalAnswer.setResultMessage(msg);
        } else {

            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            testCaseService = appContext.getBean(ITestCaseService.class);
            testCaseLabelService = appContext.getBean(ITestCaseLabelService.class);
            testCaseLabelFactory = appContext.getBean(IFactoryTestCaseLabel.class);
            testCaseCountryService = appContext.getBean(ITestCaseCountryService.class);
            testCaseCountryFactory = appContext.getBean(IFactoryTestCaseCountry.class);

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
                finalAnswer.setResultMessage(msg);

            } else /**
             * The service was able to perform the query and confirm the object
             * exist, then we can update it.
             */
            {
                if (!request.isUserInRole("Test")) { // We cannot update the testcase if the user is not at least in Test role.
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCase")
                            .replace("%OPERATION%", "Update")
                            .replace("%REASON%", "Not enought privilege to update the testcase. You mut belong to Test Privilege."));
                    finalAnswer.setResultMessage(msg);

                } else if ((tc.getStatus().equalsIgnoreCase("WORKING")) && !(request.isUserInRole("TestAdmin"))) { // If Test Case is WORKING we need TestAdmin priviliges.
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCase")
                            .replace("%OPERATION%", "Update")
                            .replace("%REASON%", "Not enought privilege to update the testcase. The test case is in WORKING status and needs TestAdmin privilige to be updated"));
                    finalAnswer.setResultMessage(msg);

                } else {
                    tc = getTestCaseFromRequest(request, tc);

                    // Update testcase
                    ans = testCaseService.update(tc);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);

                    if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        /**
                         * Update was succesfull. Adding Log entry.
                         */
                        ILogEventService logEventService = appContext.getBean(LogEventService.class);
                        logEventService.createPrivateCalls("/UpdateTestCase", "UPDATE", "Update testcase : ['" + tc.getTest() + "'|'" + tc.getTestCase() + "']", request);
                    }

                    // Update labels
                    if (request.getParameter("labelList") != null) {
                        JSONArray objLabelArray = new JSONArray(request.getParameter("labelList"));
                        List<TestCaseLabel> labelList = new ArrayList();
                        labelList = getLabelListFromRequest(request, appContext, test, testCase, objLabelArray);

                        // Update the Database with the new list.
                        ans = testCaseLabelService.compareListAndUpdateInsertDeleteElements(tc.getTest(), tc.getTestCase(), labelList);
                        finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
                    }

                    // Update Countries
                    if (request.getParameter("countryList") != null) {
                        JSONArray objCountryArray = new JSONArray(request.getParameter("countryList"));
                        List<TestCaseCountry> tccList = new ArrayList();
                        tccList = getCountryListFromRequest(request, appContext, test, testCase, objCountryArray);

                        // Update the Database with the new list.
                        ans = testCaseCountryService.compareListAndUpdateInsertDeleteElements(tc.getTest(), tc.getTestCase(), tccList);
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
            try {
                processRequest(request, response);
            } catch (CerberusException ex) {
                Logger.getLogger(UpdateTestCase2.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (JSONException ex) {
            Logger.getLogger(UpdateTestCase2.class.getName()).log(Level.SEVERE, null, ex);
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
            try {
                processRequest(request, response);
            } catch (CerberusException ex) {
                Logger.getLogger(UpdateTestCase2.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (JSONException ex) {
            Logger.getLogger(UpdateTestCase2.class.getName()).log(Level.SEVERE, null, ex);
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

    private TestCase getTestCaseFromRequest(HttpServletRequest request, TestCase tc) throws CerberusException, JSONException, UnsupportedEncodingException {
        String charset = request.getCharacterEncoding();

        // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
        tc.setImplementer(ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("implementer"), tc.getImplementer()));
        tc.setUsrModif(request.getUserPrincipal().getName());
        if (!Strings.isNullOrEmpty(request.getParameter("project"))) {
            tc.setProject(ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("project"), tc.getProject()));
        } else if (request.getParameter("project") != null && request.getParameter("project").isEmpty()) {
            tc.setProject(null);
        } else {
            tc.setProject(tc.getProject());
        }
        tc.setTest(ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("test"), tc.getTest()));
        tc.setApplication(ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("application"), tc.getApplication()));
        tc.setActiveQA(ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("activeQA"), tc.getActiveQA()));
        tc.setActiveUAT(ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("activeUAT"), tc.getActiveUAT()));
        tc.setActivePROD(ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("activeProd"), tc.getActivePROD()));
        tc.setTcActive(ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("active"), tc.getTcActive()));
        tc.setFromBuild(ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("fromSprint"), tc.getFromBuild()));
        tc.setFromRev(ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("fromRev"), tc.getFromRev()));
        tc.setToBuild(ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("toSprint"), tc.getToBuild()));
        tc.setToRev(ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("toRev"), tc.getToRev()));
        tc.setTargetBuild(ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("targetSprint"), tc.getTargetBuild()));
        tc.setTargetRev(ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("targetRev"), tc.getTargetRev()));
        tc.setPriority(ParameterParserUtil.parseIntegerParam(request.getParameter("priority"), tc.getPriority()));

        // Parameter that needs to be secured --> We SECURE+DECODE them
        tc.setTestCase(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("testCase"), tc.getTestCase(), charset));
        tc.setTicket(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("ticket"), tc.getTicket(), charset));
        tc.setOrigine(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("origin"), tc.getOrigine(), charset));
        tc.setGroup(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("group"), tc.getGroup(), charset));
        tc.setStatus(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("status"), tc.getStatus(), charset));
        tc.setDescription(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("shortDesc"), tc.getDescription(), charset));
        tc.setBugID(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("bugId"), tc.getBugID(), charset));
        tc.setComment(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("comment"), tc.getComment(), charset));
        tc.setFunction(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("function"), tc.getFunction(), charset));
        tc.setUserAgent(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("userAgent"), tc.getUserAgent(), charset));

        // Parameter that we cannot secure as we need the html --> We DECODE them
        tc.setBehaviorOrValueExpected(ParameterParserUtil.parseStringParamAndDecode(request.getParameter("behaviorOrValueExpected"), tc.getBehaviorOrValueExpected(), charset));
        tc.setHowTo(ParameterParserUtil.parseStringParamAndDecode(request.getParameter("howTo"), tc.getHowTo(), charset));

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
