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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.servlet.ServletUtil;
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
@WebServlet(name = "UpdateTestCaseMass", urlPatterns = {"/UpdateTestCaseMass"})
public class UpdateTestCaseMass extends HttpServlet {

    private final String OBJECT_NAME = "Test Case";

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
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ILogEventService logEventService = appContext.getBean(LogEventService.class);
        Answer ans = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String charset = request.getCharacterEncoding();

        response.setContentType("application/json");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        ITestCaseService testCaseService = appContext.getBean(ITestCaseService.class);

        /**
         * Parsing and securing all required parameters.
         */
        // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
        // Parameter that needs to be secured --> We SECURE+DECODE them
        String function = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("function"), "", charset);
        String status = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("status"), "", charset);
        // Parameter that we cannot secure as we need the html --> We DECODE them

        String[] myTest = request.getParameterValues("test");
        String[] myTestCase = request.getParameterValues("testcase");
        StringBuilder output_message = new StringBuilder();
        int massErrorCounter = 0;
        int tcCounter = 0;
        for (String myTest1 : myTest) {

            String cur_test = myTest1;
            String cur_testcase = myTestCase[tcCounter];
            /**
             * All data seems cleans so we can call the services.
             */
            AnswerItem resp = testCaseService.readByKey(cur_test, cur_testcase);
            if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
                /**
                 * Object could not be found. We stop here and report the error.
                 */
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                        .replace("%OPERATION%", "Update")
                        .replace("%REASON%", "TestCase does not exist."));
                ans.setResultMessage(msg);
                massErrorCounter++;
                output_message.append("<br>id : ").append(cur_test).append("|").append(cur_testcase).append(" - ").append(msg.getDescription());

            } else {
                /**
                 * The service was able to perform the query and confirm the
                 * object exist, then we can update it.
                 */
                TestCase tcData = (TestCase) resp.getItem();

                /**
                 * Before updating, we check that the old entry can be modified.
                 * If old entry point to a build/revision that already been
                 * deployed, we cannot update it.
                 */
                if (!testCaseService.hasPermissionsUpdate(tcData, request)) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                            .replace("%OPERATION%", "Update")
                            .replace("%REASON%", "Not enought privilege to update the testcase. You must belong to Test Privilege or even TestAdmin in case the test is in WORKING status."));
                    ans.setResultMessage(msg);
                    massErrorCounter++;
                    output_message.append("<br>id : ").append(cur_test).append("|").append(cur_testcase).append(" - ").append(msg.getDescription());
                } else // We test that at least a data to update has been defined.
                if ((request.getParameter("function") != null) || (request.getParameter("status") != null)) {
                    tcData.setUsrModif(request.getRemoteUser());
                    tcData.setFunction(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("function"), tcData.getFunction(), charset));
                    tcData.setStatus(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("status"), tcData.getStatus(), charset));
                    ans = testCaseService.update(tcData);

                    if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        /**
                         * Update was successful. Adding Log entry.
                         */
                        logEventService.createForPrivateCalls("/UpdateTestCaseMass", "UPDATE", "Updated TestCase : ['" + tcData.getTest() + "'|'" + tcData.getTestCase() + "']", request);
                    } else {
                        massErrorCounter++;
                        output_message.append("<br>id : ").append(cur_test).append("|").append(cur_testcase).append(" - ").append(ans.getResultMessage().getDescription());
                    }
                }
            }
            tcCounter++;
        }

        if (myTest.length > 1) {
            if (massErrorCounter == myTest.length) { // All updates are in ERROR.
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                        .replace("%OPERATION%", "Mass Update")
                        .replace("%REASON%", massErrorCounter + " objects(s) out of " + myTest.length + " failed to update due to an issue.<br>") + output_message.toString());
                ans.setResultMessage(msg);
            } else if (massErrorCounter > 0) { // At least 1 update in error
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                        .replace("%OPERATION%", "Mass Update")
                        .replace("%REASON%", massErrorCounter + " objects(s) out of " + myTest.length + " failed to update due to an issue.<br>") + output_message.toString());
                ans.setResultMessage(msg);
            } else { // No error detected.
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                        .replace("%OPERATION%", "Mass Update") + "\n\nAll " + myTest.length + " object(s) updated successfuly.");
                ans.setResultMessage(msg);
            }
            logEventService.createForPrivateCalls("/UpdateTestCaseMass", "MASSUPDATE", msg.getDescription(), request);
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
            Logger.getLogger(UpdateTestCaseMass.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(UpdateTestCaseMass.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(UpdateTestCaseMass.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(UpdateTestCaseMass.class.getName()).log(Level.SEVERE, null, ex);
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
