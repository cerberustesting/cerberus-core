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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.CountryEnvParam;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.crud.service.ICountryEnvParamService;
import org.cerberus.core.crud.service.ICountryEnvParam_logService;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.version.Infos;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.cerberus.core.service.notification.INotificationService;

/**
 * @author vertigo
 */
@WebServlet(name = "DisableEnvironment", urlPatterns = {"/DisableEnvironment"})
public class DisableEnvironment extends HttpServlet {

    private final String OBJECT_NAME = "CountryEnvParam";
    private final String ITEM = "Environment";
    private final String OPERATION = "Disable";

    private static final Logger LOG = LogManager.getLogger("DisableEnvironment");

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
        AnswerItem answerItem = new AnswerItem<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        answerItem.setResultMessage(msg);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        response.setContentType("application/json");

        /**
         * Parsing and securing all required parameters.
         */
        String system = policy.sanitize(request.getParameter("system"));
        String country = policy.sanitize(request.getParameter("country"));
        String env = policy.sanitize(request.getParameter("environment"));

        // Init Answer with potencial error from Parsing parameter.
//        AnswerItem answer = new AnswerItem<>(msg);
        String eMailContent = "";
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        INotificationService notificationService = appContext.getBean(INotificationService.class);
        ICountryEnvParamService countryEnvParamService = appContext.getBean(ICountryEnvParamService.class);
        ICountryEnvParam_logService countryEnvParam_logService = appContext.getBean(ICountryEnvParam_logService.class);
        ILogEventService logEventService = appContext.getBean(LogEventService.class);

        if (request.getParameter("system") == null) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", ITEM)
                    .replace("%OPERATION%", OPERATION)
                    .replace("%REASON%", "System name is missing!"));
            answerItem.setResultMessage(msg);
        } else if (request.getParameter("country") == null) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", ITEM)
                    .replace("%OPERATION%", OPERATION)
                    .replace("%REASON%", "Country is missing!"));
            answerItem.setResultMessage(msg);
        } else if (request.getParameter("environment") == null) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", ITEM)
                    .replace("%OPERATION%", OPERATION)
                    .replace("%REASON%", "Environment is missing!"));
            answerItem.setResultMessage(msg);
        } else {   // All parameters are OK we can start performing the operation.

            // Getting the contryEnvParam based on the parameters.
            answerItem = countryEnvParamService.readByKey(system, country, env);
            if (!(answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && answerItem.getItem() != null)) {
                /**
                 * Object could not be found. We stop here and report the error.
                 */
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                        .replace("%OPERATION%", OPERATION)
                        .replace("%REASON%", OBJECT_NAME + " ['" + system + "','" + country + "','" + env + "'] does not exist. Cannot disable it!"));
                answerItem.setResultMessage(msg);

            } else {
                /**
                 * The service was able to perform the query and confirm the
                 * object exist, then we can update it.
                 */
                CountryEnvParam cepData = (CountryEnvParam) answerItem.getItem();
                cepData.setActive(false);
                Answer answer = countryEnvParamService.update(cepData);

                if (!(answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()))) {
                    /**
                     * Object could not be updated. We stop here and report the
                     * error.
                     */
                    answerItem.setResultMessage(answer.getResultMessage());

                } else {
                    /**
                     * Update was successful.
                     */
                    // Adding Log entry.
                    logEventService.createForPrivateCalls("/DisableEnvironment", "UPDATE", LogEvent.STATUS_INFO, "Updated CountryEnvParam : ['" + system + "','" + country + "','" + env + "']", request);

                    // Adding CountryEnvParam Log entry.
                    countryEnvParam_logService.createLogEntry(system, country, env, "", "", "Disabled.", request.getUserPrincipal().getName());

                    /**
                     * Email notification.
                     */
                    String OutputMessage = "";
                    MessageEvent me = notificationService.generateAndSendDisableEnvEmail(system, country, env);

                    if (!"OK".equals(me.getMessage().getCodeString())) {
                        LOG.warn(Infos.getInstance().getProjectNameAndVersion() + " - Exception catched." + me.getMessage().getDescription());
                        logEventService.createForPrivateCalls("/DisableEnvironment", "DISABLE", LogEvent.STATUS_WARN, "Warning on Disable environment : ['" + system + "','" + country + "','" + env + "'] " + me.getMessage().getDescription(), request);
                        OutputMessage = me.getMessage().getDescription();
                    }

                    if (OutputMessage.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", "Environment")
                                .replace("%OPERATION%", OPERATION));
                        answerItem.setResultMessage(msg);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", "Environment")
                                .replace("%OPERATION%", OPERATION).concat(" Just one warning : ").concat(OutputMessage));
                        answerItem.setResultMessage(msg);
                    }
                }
            }
        }

        /**
         * Formating and returning the json result.
         */
        jsonResponse.put(
                "messageType", answerItem.getResultMessage().getMessage().getCodeString());
        jsonResponse.put(
                "message", answerItem.getResultMessage().getDescription());

        response.getWriter()
                .print(jsonResponse);
        response.getWriter()
                .flush();

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
