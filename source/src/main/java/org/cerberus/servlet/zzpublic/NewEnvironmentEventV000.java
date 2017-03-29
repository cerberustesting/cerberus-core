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
package org.cerberus.servlet.zzpublic;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.CountryEnvParam;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.crud.service.IBatchInvariantService;
import org.cerberus.crud.service.IBuildRevisionBatchService;
import org.cerberus.crud.service.IBuildRevisionInvariantService;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.ICountryEnvParamService;
import org.cerberus.crud.service.ICountryEnvParam_logService;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.service.email.IEmailGeneration;
import org.cerberus.service.email.impl.sendMail;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.AnswerUtil;
import org.cerberus.version.Infos;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author vertigo
 */
@WebServlet(name = "NewEnvironmentEventV000", urlPatterns = {"/NewEnvironmentEventV000"})
public class NewEnvironmentEventV000 extends HttpServlet {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger("NewEnvironmentEventV000");

    private final String OPERATION = "New Environment Event";
    private final String PARAMETERALL = "ALL";

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
        PrintWriter out = response.getWriter();

        String charset = request.getCharacterEncoding();

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        /**
         * Adding Log entry.
         */
        ILogEventService logEventService = appContext.getBean(ILogEventService.class);
        logEventService.createForPublicCalls("/NewEnvironmentEventV000", "CALL", "NewEnvironmentEventV000 called : " + request.getRequestURL(), request);

        ICountryEnvParamService countryEnvParamService = appContext.getBean(ICountryEnvParamService.class);
        IInvariantService invariantService = appContext.getBean(IInvariantService.class);
        IBatchInvariantService batchInvariantService = appContext.getBean(IBatchInvariantService.class);
        IBuildRevisionBatchService buildRevisionBatchService = appContext.getBean(IBuildRevisionBatchService.class);
        IBuildRevisionInvariantService buildRevisionInvariantService = appContext.getBean(IBuildRevisionInvariantService.class);
        IEmailGeneration emailService = appContext.getBean(IEmailGeneration.class);
        ICountryEnvParam_logService countryEnvParam_logService = appContext.getBean(ICountryEnvParam_logService.class);
        IParameterService parameterService = appContext.getBean(IParameterService.class);

        // Parsing all parameters.
        String system = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("system"), "", charset);
        String country = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("country"), "", charset);
        String environment = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("environment"), "", charset);
        String event = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("event"), "", charset);

        String helpMessage = "\nThis servlet is used to inform Cerberus about an event that occured on a given environment. For example when a treatment has been executed.\n\nParameter list :\n"
                + "- system [mandatory] : the system where the Build Revision has been deployed. [" + system + "]\n"
                + "- country [mandatory] : the country where the Build Revision has been deployed. You can use ALL if you want to perform the action for all countries that exist for the given system and environement. [" + country + "]\n"
                + "- environment [mandatory] : the environment where the Build Revision has been deployed. [" + environment + "]\n"
                + "- event [mandatory] : the event that should be recorded.. [" + event + "]\n";

        boolean error = false;

        // Checking the parameter validity. If application has been entered, does it exist ?
        if (system.equalsIgnoreCase("")) {
            out.println("Error - Parameter system is mandatory.");
            error = true;
        }
        if (!system.equalsIgnoreCase("") && !invariantService.isInvariantExist("SYSTEM", system)) {
            out.println("Error - System does not exist  : " + system);
            error = true;
        }
        if (environment.equalsIgnoreCase("")) {
            out.println("Error - Parameter environment is mandatory.");
            error = true;
        }
        if (!environment.equalsIgnoreCase("") && !invariantService.isInvariantExist("ENVIRONMENT", environment)) {
            out.println("Error - Environment does not exist  : " + environment);
            error = true;
        }
        if (country.equalsIgnoreCase("")) {
            out.println("Error - Parameter country is mandatory.");
            error = true;
        } else if (!country.equalsIgnoreCase(PARAMETERALL)) {
            if (!invariantService.isInvariantExist("COUNTRY", country)) {
                out.println("Error - Country does not exist  : " + country);
                error = true;
            }
            if (!error) {
                if (!countryEnvParamService.exist(system, country, environment)) {
                    out.println("Error - System/Country/Environment does not exist : " + system + "/" + country + "/" + environment);
                    error = true;
                }
            }
        }
        if (event.equalsIgnoreCase("")) {
            out.println("Error - Parameter event is mandatory.");
            error = true;
        }
        if (!event.equalsIgnoreCase("") && !batchInvariantService.exist(event)) {
            out.println("Error - Event does not exist  : " + event);
            error = true;
        }

        // Starting the database update only when no blocking error has been detected.
        if (error == false) {

            /**
             * Getting the list of objects to treat.
             */
            MessageEvent msg = new MessageEvent(MessageEventEnum.GENERIC_OK);
            Answer finalAnswer = new Answer(msg);

            AnswerList answerList = new AnswerList();
            if (country.equalsIgnoreCase(PARAMETERALL)) {
                country = null;
            }
            answerList = countryEnvParamService.readByVarious(system, country, environment, null, null, "Y");
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) answerList);

            for (CountryEnvParam cepData : (List<CountryEnvParam>) answerList.getDataList()) {

                /**
                 * For each object, we can update it.
                 */
                // Adding CountryEnvParam Log entry.
                buildRevisionBatchService.create(cepData.getSystem(), cepData.getCountry(), cepData.getEnvironment(), cepData.getBuild(), cepData.getRevision(), event);

                /**
                 * Email notification.
                 */
                // Email Calculation.
                String eMailContent;
                String OutputMessage = "";
                eMailContent = emailService.EmailGenerationNewChain(cepData.getSystem(), cepData.getCountry(), cepData.getEnvironment(), event);
                String[] eMailContentTable = eMailContent.split("///");
                String to = eMailContentTable[0];
                String cc = eMailContentTable[1];
                String subject = eMailContentTable[2];
                String body = eMailContentTable[3];

                // Search the From, the Host and the Port defined in the parameters
                String from;
                String host;
                int port;
                String userName;
                String password;
                try {
                    from = parameterService.findParameterByKey("integration_smtp_from", cepData.getSystem()).getValue();
                    host = parameterService.findParameterByKey("integration_smtp_host", cepData.getSystem()).getValue();
                    port = Integer.valueOf(parameterService.findParameterByKey("integration_smtp_port", cepData.getSystem()).getValue());
                    userName = parameterService.findParameterByKey("integration_smtp_username", system).getValue();
                    password = parameterService.findParameterByKey("integration_smtp_password", system).getValue();

                    //Sending the email
                    sendMail.sendHtmlMail(host, port, userName, password, body, subject, from, to, cc);

                } catch (Exception e) {
                    Logger.getLogger(NewEnvironmentEventV000.class.getName()).log(Level.SEVERE, Infos.getInstance().getProjectNameAndVersion() + " - Exception catched.", e);
                    logEventService.createForPrivateCalls("/NewEnvironmentEventV000", "NEW", "Warning on New environment event : ['" + cepData.getSystem() + "','" + cepData.getCountry() + "','" + cepData.getEnvironment() + "'] " + e.getMessage(), request);
                    OutputMessage = e.getMessage();
                }

                    if (OutputMessage.equals("")) {
                        msg = new MessageEvent(MessageEventEnum.GENERIC_OK);
                        Answer answerSMTP = new AnswerList(msg);
                        finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, answerSMTP);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.GENERIC_WARNING);
                        msg.setDescription(msg.getDescription().replace("%REASON%", OutputMessage + " when sending email for " + cepData.getSystem() + "/" + cepData.getCountry() + "/" + cepData.getEnvironment()));
                        Answer answerSMTP = new AnswerList(msg);
                        finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, answerSMTP);
                    }
            }
            /**
             * Formating and returning the result.
             */
            out.println(finalAnswer.getResultMessage().getMessage().getCodeString() + " - " + finalAnswer.getResultMessage().getDescription());

        } else {
            // In case of errors, we display the help message.
            out.println(helpMessage);
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
