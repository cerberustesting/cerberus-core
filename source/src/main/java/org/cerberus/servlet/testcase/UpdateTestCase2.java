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
package org.cerberus.servlet.testcase;

import com.google.common.base.Strings;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.TCase;
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ITestCaseCountryService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.impl.InvariantService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.crud.service.impl.TestCaseCountryService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
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
            throws ServletException, IOException, JSONException, CerberusException {
        JSONObject jsonResponse = new JSONObject();
        Answer ans = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        response.setContentType("application/json");

        /**
         * Parsing and securing all required parameters.
         */
        String test = policy.sanitize(request.getParameter("test"));
        String testCase = policy.sanitize(request.getParameter("testCase"));
        String active = policy.sanitize(request.getParameter("active"));
        String tcDateCrea = policy.sanitize(request.getParameter("tcDateCrea"));
        String country = policy.sanitize(request.getParameter("country"));
        String state = policy.sanitize(request.getParameter("state"));

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

            AnswerItem resp = testCaseService.readByKey(test, testCase);
            TCase tc = (TCase) resp.getItem();
            if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()))) {
                /**
                 * Object could not be found. We stop here and report the error.
                 */
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCase")
                        .replace("%OPERATION%", "Update")
                        .replace("%REASON%", "TestCase does not exist."));
                ans.setResultMessage(msg);

            } else {
                /**
                 * The service was able to perform the query and confirm the
                 * object exist, then we can update it.
                 */

                getInfo(request, tc);

                ans = testCaseService.update(tc);
                getCountryList(tc, request);

                if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    /**
                     * Update was succesfull. Adding Log entry.
                     */
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    logEventService.createPrivateCalls("/UpdateTestCase", "UPDATE", "Updated TestCase : ['" + testCase + "']", request);
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

    private TCase getInfo(HttpServletRequest request, TCase tc) throws CerberusException, JSONException {
        tc.setTest(ParameterParserUtil.parseStringParam(request.getParameter("test"), tc.getTest()));
        tc.setTestCase(ParameterParserUtil.parseStringParam(request.getParameter("testCase"), tc.getTestCase()));
        tc.setImplementer(ParameterParserUtil.parseStringParam(request.getParameter("imlementer"), tc.getImplementer()));
        tc.setLastModifier(request.getUserPrincipal().getName());
        if (Strings.isNullOrEmpty(request.getParameter("project"))) {
            tc.setProject(tc.getProject());
        } else {
            tc.setProject(request.getParameter("project"));
        }
        tc.setTicket(ParameterParserUtil.parseStringParam(request.getParameter("ticket"), tc.getTicket()));
        tc.setApplication(ParameterParserUtil.parseStringParam(request.getParameter("application"), tc.getApplication()));
        tc.setRunQA(ParameterParserUtil.parseStringParam(request.getParameter("activeQA"), tc.getRunQA()));
        tc.setRunUAT(ParameterParserUtil.parseStringParam(request.getParameter("activeUAT"), tc.getRunUAT()));
        tc.setRunPROD(ParameterParserUtil.parseStringParam(request.getParameter("activeProd"), tc.getRunPROD()));
        tc.setPriority(ParameterParserUtil.parseIntegerParam(request.getParameter("priority"), tc.getPriority()));
        tc.setGroup(ParameterParserUtil.parseStringParam(request.getParameter("group"), tc.getGroup()));
        tc.setStatus(ParameterParserUtil.parseStringParam(request.getParameter("status"), tc.getStatus()));
        tc.setShortDescription(ParameterParserUtil.parseStringParam(request.getParameter("shortDesc"), tc.getShortDescription()));
        tc.setDescription(ParameterParserUtil.parseStringParam(request.getParameter("behaviorOrValueExpected"), tc.getDescription()));
        tc.setHowTo(ParameterParserUtil.parseStringParam(request.getParameter("howTo"), tc.getHowTo()));
        tc.setActive(ParameterParserUtil.parseStringParam(request.getParameter("active"), tc.getActive()));
        tc.setFromSprint(ParameterParserUtil.parseStringParam(request.getParameter("fromSprint"), tc.getFromSprint()));
        tc.setFromRevision(ParameterParserUtil.parseStringParam(request.getParameter("fromRev"), tc.getFromRevision()));
        tc.setToSprint(ParameterParserUtil.parseStringParam(request.getParameter("toSprint"), tc.getToSprint()));
        tc.setToRevision(ParameterParserUtil.parseStringParam(request.getParameter("toRev"), tc.getToRevision()));
        tc.setBugID(ParameterParserUtil.parseStringParam(request.getParameter("bugId"), tc.getBugID()));
        tc.setTargetSprint(ParameterParserUtil.parseStringParam(request.getParameter("targetSprint"), tc.getTargetSprint()));
        tc.setTargetRevision(ParameterParserUtil.parseStringParam(request.getParameter("targetRev"), tc.getTargetRevision()));
        tc.setComment(ParameterParserUtil.parseStringParam(request.getParameter("comment"), tc.getComment()));
        tc.setFunction(ParameterParserUtil.parseStringParam(request.getParameter("function"), tc.getFunction()));
        return tc;
    }

    private void getCountryList(TCase tc, HttpServletRequest request) throws CerberusException, JSONException {
        Map<String, String> countryList = new HashMap<String, String>();
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IInvariantService invariantService = appContext.getBean(InvariantService.class);

        ITestCaseCountryService testCaseCountryService = appContext.getBean(TestCaseCountryService.class);
        AnswerList answer = testCaseCountryService.readByTestTestCase(tc.getTest(), tc.getTestCase());
        List<TestCaseCountry> tcCountry = answer.getDataList();

        for (Invariant country : invariantService.findListOfInvariantById("COUNTRY")) {
            countryList.put(country.getValue(), ParameterParserUtil.parseStringParam(request.getParameter(country.getValue()), ""));
        }

        for (TestCaseCountry countryDB : tcCountry) {
            if (countryList.get(countryDB.getCountry()).equals("off")) {
                testCaseCountryService.deleteTestCaseCountry(countryDB);
            }
            countryList.remove(countryDB.getCountry());
        }

        for (Map.Entry<String, String> country : countryList.entrySet()) {
            if (country.getValue().equals("on")) {
                TestCaseCountry addCountry = new TestCaseCountry();
                addCountry.setTest(tc.getTest());
                addCountry.setTestCase(tc.getTestCase());
                addCountry.setCountry(country.getKey());

                testCaseCountryService.insertTestCaseCountry(addCountry);
            }
        }
    }
}
