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
package org.cerberus.servlet.integration;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.CountryEnvParam;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.service.IBuildRevisionBatchService;
import org.cerberus.crud.service.ICountryEnvParamService;
import org.cerberus.crud.service.ICountryEnvParam_logService;
import org.cerberus.crud.service.ILogEventService;

import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.service.email.IEmailGeneration;
import org.cerberus.service.email.impl.sendMail;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.version.Infos;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author vertigo
 */
@WebServlet(name = "NewChain1", urlPatterns = {"/NewChain1"})
public class NewChain1 extends HttpServlet {

    private final String OBJECT_NAME = "CountryEnvParam";
    private final String ITEM = "Environment";
    private final String OPERATION = "NewChain";

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
        AnswerItem answerItem = new AnswerItem();
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
        String chain = policy.sanitize(request.getParameter("chain"));

        // Init Answer with potencial error from Parsing parameter.
//        AnswerItem answer = new AnswerItem(msg);
        String eMailContent = "";
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IEmailGeneration emailService = appContext.getBean(IEmailGeneration.class);
        IParameterService parameterService = appContext.getBean(IParameterService.class);
        ICountryEnvParamService countryEnvParamService = appContext.getBean(ICountryEnvParamService.class);
        IBuildRevisionBatchService buildRevisionBatchService = appContext.getBean(IBuildRevisionBatchService.class);
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
        } else if (request.getParameter("chain") == null) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", ITEM)
                    .replace("%OPERATION%", OPERATION)
                    .replace("%REASON%", "Chain is missing!"));
            answerItem.setResultMessage(msg);
        } else {   // All parameters are OK we can start performing the operation.

            // Getting the contryEnvParam based on the parameters.
            answerItem = countryEnvParamService.readByKey(system, country, env);
            if (!(answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()))) {
                /**
                 * Object could not be found. We stop here and report the error.
                 */
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                        .replace("%OPERATION%", OPERATION)
                        .replace("%REASON%", OBJECT_NAME + " ['" + system + "','" + country + "','" + env + "'] does not exist. Cannot register a new event!"));
                answerItem.setResultMessage(msg);

            } else {
                /**
                 * The service was able to perform the query and confirm the
                 * object exist, then we can update it.
                 */
                // Adding BuildRevisionBatch entry.
                // Adding CountryEnvParam Log entry.
                CountryEnvParam cepData = (CountryEnvParam) answerItem.getItem();
                buildRevisionBatchService.createBatchEntry(system, country, env, cepData.getBuild(), cepData.getRevision(), chain);

                /**
                 * Email notification.
                 */
                // Email Calculation.
                String OutputMessage = "";
                eMailContent = emailService.EmailGenerationNewChain(system, country, env, chain);
                String[] eMailContentTable = eMailContent.split("///");
                String to = eMailContentTable[0];
                String cc = eMailContentTable[1];
                String subject = eMailContentTable[2];
                String body = eMailContentTable[3];

                // Search the From, the Host and the Port defined in the parameters
                String from;
                String host;
                int port;
                try {
                    from = parameterService.findParameterByKey("integration_smtp_from", system).getValue();
                    host = parameterService.findParameterByKey("integration_smtp_host", system).getValue();
                    port = Integer.valueOf(parameterService.findParameterByKey("integration_smtp_port", system).getValue());

                    //Sending the email
                    sendMail.sendHtmlMail(host, port, body, subject, from, to, cc);
                } catch (Exception e) {
                    Logger.getLogger(NewChain1.class.getName()).log(Level.SEVERE, Infos.getInstance().getProjectNameAndVersion() + " - Exception catched.", e);
                    logEventService.createPrivateCalls("/NewChain", "NEWCHAIN", "Warning on registering new event on environment : ['" + system + "','" + country + "','" + env + "'] " + e.getMessage(), request);
                    OutputMessage = e.getMessage();
                }

                if (OutputMessage.equals("")) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", ITEM)
                            .replace("%OPERATION%", OPERATION));
                    answerItem.setResultMessage(msg);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", ITEM)
                            .replace("%OPERATION%", OPERATION).concat(" Just one warning : ").concat(OutputMessage));
                    answerItem.setResultMessage(msg);
                }
            }
        }

        /**
         * Formating and returning the json result.
         */
        jsonResponse.put("messageType", answerItem.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", answerItem.getResultMessage().getDescription());

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
        } catch (JSONException ex) {
            Logger.getLogger(NewChain1.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(NewChain1.class.getName()).log(Level.SEVERE, null, ex);
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
