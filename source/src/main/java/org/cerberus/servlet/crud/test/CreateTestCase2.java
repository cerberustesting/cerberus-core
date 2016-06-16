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
import java.io.UnsupportedEncodingException;
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
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.servlet.ServletUtil;
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
@WebServlet(name = "CreateTestCase2", urlPatterns = {"/CreateTestCase2"})
public class CreateTestCase2 extends HttpServlet {

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

        /**
         * Checking all constrains before calling the services.
         */
        if (StringUtil.isNullOrEmpty(test) && StringUtil.isNullOrEmpty(testcase)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Deploy Type")
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "Deploy Type name is missing!"));
            ans.setResultMessage(msg);
        } else {
            /**
             * All data seems cleans so we can call the services.
             */
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            ITestCaseService testCaseService = appContext.getBean(ITestCaseService.class);

            TCase testCaseData = getInfo(request);
            ans = testCaseService.create(testCaseData);

            getCountryList(testCaseData, request);

            if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                /**
                 * Object created. Adding Log entry.
                 */
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                logEventService.createPrivateCalls("/CreateTestCase", "CREATE", "Create TestCase : ['" + testcase + "']", request);
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
            processRequest(request, response);
        } catch (CerberusException ex) {
            Logger.getLogger(CreateTestCase2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(CreateTestCase2.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(CreateTestCase2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(CreateTestCase2.class.getName()).log(Level.SEVERE, null, ex);
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

    private TCase getInfo(HttpServletRequest request) throws CerberusException, JSONException {
        TCase tc = new TCase();

        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String charset = request.getCharacterEncoding();

        // Parameter that needs to be secured --> We SECURE+DECODE them
        tc.setImplementer(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("implementer"), "", charset));
        tc.setCreator(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getUserPrincipal().getName(), "", charset));
        tc.setLastModifier(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getUserPrincipal().getName(), "", charset));
        if (request.getParameter("project").isEmpty()) {
            tc.setProject(null);
        } else {
            tc.setProject(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("project"), "", charset));
        }
        tc.setApplication(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("application"), "", charset));
        tc.setRunQA(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("activeQA"), "", charset));
        tc.setRunUAT(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("activeUAT"), "", charset));
        tc.setRunPROD(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("activeProd"), "", charset));
        tc.setFromSprint(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("fromSprint"), "", charset));
        tc.setFromRevision(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("fromRev"), "", charset));
        tc.setToSprint(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("toSprint"), "", charset));
        tc.setToRevision(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("toRev"), "", charset));
        tc.setActive(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("active"), "", charset));
        tc.setTargetSprint(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("targetSprint"), "", charset));
        tc.setTargetRevision(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("targetRev"), "", charset));
        tc.setPriority(ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("priority"), 0, charset));
        tc.setTest(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("test"), "", charset));
        tc.setTestCase(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("testCase"), "", charset));
        tc.setTicket(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("ticket"), "", charset));
        tc.setOrigin(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("origin"), "", charset));
        tc.setRefOrigin(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("refOrigin"), "", charset));
        tc.setGroup(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("group"), "", charset));
        tc.setStatus(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("status"), "", charset));
        tc.setShortDescription(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("shortDesc"), "", charset));
        tc.setBugID(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("bugId"), "", charset));
        tc.setComment(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("comment"), "", charset));
        tc.setFunction(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("function"), "", charset));

        // Parameter that we cannot secure as we need the html --> We DECODE them
        tc.setHowTo(ParameterParserUtil.parseStringParamAndDecode(request.getParameter("howTo"), "", charset));
        tc.setDescription(ParameterParserUtil.parseStringParamAndDecode(request.getParameter("behaviorOrValueExpected"), "", charset));

        return tc;
    }

    private void getCountryList(TCase tc, HttpServletRequest request) throws CerberusException, JSONException, UnsupportedEncodingException {
        Map<String, String> countryList = new HashMap<String, String>();
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IInvariantService invariantService = appContext.getBean(InvariantService.class);

        ITestCaseCountryService testCaseCountryService = appContext.getBean(TestCaseCountryService.class);
        AnswerList answer = invariantService.readByIdname("COUNTRY"); //TODO: handle if the response does not turn ok
        for (Invariant country : (List<Invariant>) answer.getDataList()) {
            countryList.put(country.getValue(), ParameterParserUtil.parseStringParamAndSanitize(request.getParameter(country.getValue()), "off"));
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
