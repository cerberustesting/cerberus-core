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
import java.io.PrintWriter;
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
        /**
         * Parsing and securing all required parameters.
         */
        String test = policy.sanitize(request.getParameter("test"));
        String testcase = policy.sanitize(request.getParameter("testCase"));

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
        tc.setTest(request.getParameter("test"));
        tc.setTestCase(request.getParameter("testCase"));
        tc.setImplementer(request.getParameter("implementer"));
        tc.setCreator(request.getUserPrincipal().getName());
        tc.setLastModifier(request.getUserPrincipal().getName());
        if (request.getParameter("project").isEmpty()) {
            tc.setProject(null);
        } else {
            tc.setProject(request.getParameter("project"));
        }
        tc.setTicket(request.getParameter("ticket"));
        tc.setApplication(request.getParameter("application"));
        tc.setRunQA(request.getParameter("activeQA"));
        tc.setRunUAT(request.getParameter("activeUAT"));
        tc.setRunPROD(request.getParameter("activeProd"));
        tc.setOrigin(request.getParameter("origin"));
        tc.setRefOrigin(request.getParameter("refOrigin"));
        tc.setPriority(Integer.parseInt(request.getParameter("priority")));
        tc.setGroup(request.getParameter("group"));
        tc.setStatus(request.getParameter("status"));
        tc.setShortDescription(request.getParameter("shortDesc"));
        tc.setDescription(request.getParameter("behaviorOrValueExpected"));
        tc.setHowTo(request.getParameter("howTo"));
        tc.setActive(request.getParameter("active"));
        tc.setFromSprint(request.getParameter("fromSprint"));
        tc.setFromRevision(request.getParameter("fromRev"));
        tc.setToSprint(request.getParameter("toSprint"));
        tc.setToRevision(request.getParameter("toRev"));
        tc.setBugID(request.getParameter("bugId"));
        tc.setTargetSprint(request.getParameter("targetSprint"));
        tc.setTargetRevision(request.getParameter("targetRev"));
        tc.setComment(request.getParameter("comment"));
        tc.setFunction(request.getParameter("function"));
        return tc;
    }

    private void getCountryList(TCase tc, HttpServletRequest request) throws CerberusException, JSONException {
        Map<String, String> countryList = new HashMap<String, String>();
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IInvariantService invariantService = appContext.getBean(InvariantService.class);

        ITestCaseCountryService testCaseCountryService = appContext.getBean(TestCaseCountryService.class);
        AnswerList answer = invariantService.readByIdname("COUNTRY"); //TODO: handle if the response does not turn ok
        for (Invariant country : (List<Invariant>)answer.getDataList()) {
            countryList.put(country.getValue(), ParameterParserUtil.parseStringParam(request.getParameter(country.getValue()), "off"));
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
