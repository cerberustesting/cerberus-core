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
package org.cerberus.servlet.crud.testdata;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringEscapeUtils;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.crud.factory.IFactoryTestDataLib;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ITestDataLibService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Handles the UPDATE operation for test data lib entries.
 *
 * @author FNogueira
 */
@WebServlet(name = "UpdateTestDataLib", urlPatterns = {"/UpdateTestDataLib"})
public class UpdateTestDataLib extends HttpServlet {

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
            throws ServletException, IOException {
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
        String name = policy.sanitize(request.getParameter("name")); //this is mandatory
        String type = policy.sanitize(request.getParameter("type"));
        String group = policy.sanitize(request.getParameter("group"));

        String description = policy.sanitize(request.getParameter("libdescription"));
        String system = policy.sanitize(request.getParameter("system"));
        String environment = policy.sanitize(request.getParameter("environment"));
        String country = policy.sanitize(request.getParameter("country"));

        String database = policy.sanitize(request.getParameter("database"));
        String script = policy.sanitize(request.getParameter("script"));

        String servicePath = policy.sanitize(request.getParameter("servicepath"));
        String method = policy.sanitize(request.getParameter("method"));
        String envelope = StringEscapeUtils.escapeXml11(request.getParameter("envelope"));

        Integer testdatalibid = 0;
        boolean testdatalibid_error = true;
        try {
            if (request.getParameter("testdatalibid") != null && !request.getParameter("testdatalibid").isEmpty()) {
                testdatalibid = Integer.valueOf(request.getParameter("testdatalibid"));
                testdatalibid_error = false;
            }
        } catch (NumberFormatException ex) {
            testdatalibid_error = true;
            Logger.getLogger(UpdateTestDataLib.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            /**
             * Checking all constrains before calling the services.
             */

            if (StringUtil.isNullOrEmpty(name)) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Test data library")
                        .replace("%OPERATION%", "Update")
                        .replace("%REASON%", "Test data library name is missing."));
                ans.setResultMessage(msg);
            } else if (testdatalibid_error) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Test data library")
                        .replace("%OPERATION%", "Update")
                        .replace("%REASON%", "Could not manage to convert testdatalibid to an integer value or testdatalibid is missing."));
                ans.setResultMessage(msg);
            } else {
                /**
                 * All data seems cleans so we can call the services.
                 */
                //specific attributes
                ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
                ITestDataLibService libService = appContext.getBean(ITestDataLibService.class);

                AnswerItem resp = libService.readByKey(testdatalibid);
                if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()))) {
                    /**
                     * Object could not be found. We stop here and report the
                     * error.
                     */
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "Robot")
                            .replace("%OPERATION%", "Update")
                            .replace("%REASON%", "Robot does not exist."));
                    ans.setResultMessage(msg);

                } else {
                    /**
                     * The service was able to perform the query and confirm the
                     * object exist, then we can update it.
                     */
                    TestDataLib lib = (TestDataLib) resp.getItem();
                    lib.setName(name);
                    lib.setType(type);
                    lib.setGroup(group);
                    lib.setDescription(description);
                    lib.setSystem(system);
                    lib.setEnvironment(environment);
                    lib.setCountry(country);
                    lib.setDatabase(database);
                    lib.setScript(script);
                    lib.setServicePath(servicePath);
                    lib.setMethod(method);
                    lib.setEnvelope(envelope);

                    ans = libService.update(lib);

                    if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        /**
                         * Update operation finished with success, then the logging entry must be added.
                         */
                        ILogEventService logEventService = appContext.getBean(LogEventService.class);
                        logEventService.createPrivateCalls("/UpdateTestDataLib", "UPDATE", "Update TestDataLib - id: " + testdatalibid + " name: " + name + " system: "
                                + system + " environment: " + environment + " country: " + country, request);
                    }
                }

            }
            jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", ans.getResultMessage().getDescription());

            response.getWriter().print(jsonResponse);
            response.getWriter().flush();

        } catch (JSONException ex) {
            Logger.getLogger(UpdateTestDataLib.class.getName()).log(Level.SEVERE, null, ex);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        }

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
        processRequest(request, response);
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
        processRequest(request, response);
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
