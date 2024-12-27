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
package org.cerberus.core.servlet.integration;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.notifications.email.entity.Email;
import org.cerberus.core.util.answer.Answer;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.cerberus.core.service.notifications.email.IEmailGenerationService;

/**
 *
 * @author vertigo
 */
@WebServlet(name = "GetNotification", urlPatterns = {"/GetNotification"})
public class GetNotification extends HttpServlet {

    private final String OBJECT_NAME = "GetNotification";

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger("GetNotification");

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws org.cerberus.core.exception.CerberusException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, CerberusException, JSONException {
        JSONObject jsonResponse = new JSONObject();
        Answer answer = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        answer.setResultMessage(msg);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        response.setContentType("application/json");

        /**
         * Parsing and securing all required parameters.
         */
        String system = policy.sanitize(request.getParameter("system"));
        String country = policy.sanitize(request.getParameter("country"));
        String env = policy.sanitize(request.getParameter("environment"));
        String build = policy.sanitize(request.getParameter("build"));
        String revision = policy.sanitize(request.getParameter("revision"));
        String chain = policy.sanitize(request.getParameter("chain"));

        // Init Answer with potencial error from Parsing parameter.
//        AnswerItem answer = new AnswerItem<>(msg);
        String eMailContent = "";
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IEmailGenerationService emailService = appContext.getBean(IEmailGenerationService.class);

        if (request.getParameter("system") == null) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "GetNotification")
                    .replace("%OPERATION%", "Get")
                    .replace("%REASON%", "System name is missing!"));
            answer.setResultMessage(msg);
        } else if (request.getParameter("event") == null) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "GetNotification")
                    .replace("%OPERATION%", "Get")
                    .replace("%REASON%", "event is missing!"));
            answer.setResultMessage(msg);
        } else if (request.getParameter("event").equals("newbuildrevision")) {
            try {
                // ID parameter is specified so we return the unique record of object.
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "GetNotification")
                        .replace("%OPERATION%", "Get"));
                answer.setResultMessage(msg);

                Email email = emailService.generateRevisionChangeEmail(system, country, env, build, revision);
                jsonResponse.put("notificationTo", email.getTo());
                jsonResponse.put("notificationCC", email.getCc());
                jsonResponse.put("notificationSubject", email.getSubject());
                jsonResponse.put("notificationBody", email.getBody());

            } catch (Exception ex) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "GetNotification")
                        .replace("%OPERATION%", "Get")
                        .replace("%REASON%", ex.toString()));
                answer.setResultMessage(msg);
            }
        } else if (request.getParameter("event").equals("disableenvironment")) {
            try {
                // ID parameter is specified so we return the unique record of object.
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "GetNotification")
                        .replace("%OPERATION%", "Get"));
                answer.setResultMessage(msg);
                Email email = emailService.generateDisableEnvEmail(system, country, env);
                jsonResponse.put("notificationTo", email.getTo());
                jsonResponse.put("notificationCC", email.getCc());
                jsonResponse.put("notificationSubject", email.getSubject());
                jsonResponse.put("notificationBody", email.getBody());
            } catch (Exception ex) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "GetNotification")
                        .replace("%OPERATION%", "Get")
                        .replace("%REASON%", ex.toString()));
                answer.setResultMessage(msg);
            }
        } else if (request.getParameter("event").equals("newchain")) {
            try {
                // ID parameter is specified so we return the unique record of object.
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "GetNotification")
                        .replace("%OPERATION%", "Get"));
                answer.setResultMessage(msg);
                Email email = emailService.generateNewChainEmail(system, country, env, chain);
                jsonResponse.put("notificationTo", email.getTo());
                jsonResponse.put("notificationCC", email.getCc());
                jsonResponse.put("notificationSubject", email.getSubject());
                jsonResponse.put("notificationBody", email.getBody());
            } catch (Exception ex) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "GetNotification")
                        .replace("%OPERATION%", "Get")
                        .replace("%REASON%", ex.toString()));
                answer.setResultMessage(msg);
            }
        } else {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "GetNotification")
                    .replace("%OPERATION%", "Get")
                    .replace("%REASON%", "Unknown invalidityReason!"));
            answer.setResultMessage(msg);
        }

        /**
         * Formating and returning the json result.
         */
        jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", answer.getResultMessage().getDescription());

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

}
