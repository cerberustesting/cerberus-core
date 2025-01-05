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

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseCountry;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.ITestCaseCountryService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.servlet.ServletUtil;
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
@WebServlet(name = "DeleteTestCaseCountry", urlPatterns = {"/DeleteTestCaseCountry"})
public class DeleteTestCaseCountry extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(DeleteTestCaseCountry.class);

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
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();

        response.setContentType("application/json");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        /**
         * Parsing and securing all required parameters.
         */
        String test = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("test"), "", charset);
        String testcase = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("testCase"), null, charset);
        String country = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("country"), "", charset);

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        /**
         * Checking all constrains before calling the services.
         */
        if (testcase == null || (StringUtil.isEmptyOrNull(test)) || (StringUtil.isEmptyOrNull(country))) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseCountry")
                    .replace("%OPERATION%", "Delete")
                    .replace("%REASON%", "test or testCase or country is missing!"));
            ans.setResultMessage(msg);
        } else {

            // Checking the autorities here.
            ITestCaseService testCaseService = appContext.getBean(ITestCaseService.class);
            AnswerItem resp = testCaseService.readByKey(test, testcase);
            TestCase tc = (TestCase) resp.getItem();
            if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
                /**
                 * Object could not be found. We stop here and report the error.
                 */
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseCountry")
                        .replace("%OPERATION%", "Create")
                        .replace("%REASON%", "TestCase does not exist."));
                ans.setResultMessage(msg);

            } else if (!request.isUserInRole("Test")) { // We cannot update the testcase if the user is not at least in Test role.
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseCountry")
                        .replace("%OPERATION%", "Create")
                        .replace("%REASON%", "Not enought privilege to create the testCaseCountry. You must belong to Test Privilege."));
                ans.setResultMessage(msg);

            } else if ((tc.getStatus().equalsIgnoreCase(TestCase.TESTCASE_STATUS_WORKING)) && !(request.isUserInRole("TestAdmin"))) { // If Test Case is WORKING we need TestAdmin priviliges.
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseCountry")
                        .replace("%OPERATION%", "Create")
                        .replace("%REASON%", "Not enought privilege to create the testCaseCountry. The test case is in " + TestCase.TESTCASE_STATUS_WORKING + " status and needs TestAdmin privilege to be updated"));
                ans.setResultMessage(msg);

            } else {

                /**
                 * All data seems cleans so we can call the services.
                 */
                ITestCaseCountryService testCaseCountryService = appContext.getBean(ITestCaseCountryService.class);

                resp = testCaseCountryService.readByKey(test, testcase, country);
                if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
                    /**
                     * Object could not be found. We stop here and report the
                     * error.
                     */
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCaseCountry")
                            .replace("%OPERATION%", "Delete")
                            .replace("%REASON%", "TestCaseCountry does not exist."));
                    ans.setResultMessage(msg);

                } else {
                    /**
                     * The service was able to perform the query and confirm the
                     * object exist, then we can delete it.
                     */
                    TestCaseCountry testCaseCountryData = (TestCaseCountry) resp.getItem();
                    ans = testCaseCountryService.delete(testCaseCountryData);

                    if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        /**
                         * Delete was successful. Adding Log entry.
                         */
                        ILogEventService logEventService = appContext.getBean(LogEventService.class);
                        logEventService.createForPrivateCalls("/DeleteTestCaseCountry", "DELETE", LogEvent.STATUS_INFO, "Delete TestCaseCountry : ['" + test + "'|'" + testcase + "'|'" + country + "']", request);
                    }
                }
            }
        }

        /**
         * Formating and returning the json result.
         */
        jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", ans.getResultMessage().getDescription());

        response.getWriter().print(jsonResponse.toString());
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
}
