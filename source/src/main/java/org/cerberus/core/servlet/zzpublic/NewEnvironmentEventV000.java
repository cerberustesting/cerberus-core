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
package org.cerberus.core.servlet.zzpublic;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.core.crud.entity.CountryEnvParam;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.crud.service.IBatchInvariantService;
import org.cerberus.core.crud.service.IBuildRevisionBatchService;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.ICountryEnvParamService;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.service.authentification.IAPIKeyService;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.cerberus.core.version.Infos;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.cerberus.core.service.notification.INotificationService;

/**
 * @author vertigo
 */
@WebServlet(name = "NewEnvironmentEventV000", urlPatterns = {"/NewEnvironmentEventV000"})
public class NewEnvironmentEventV000 extends HttpServlet {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger("NewEnvironmentEventV000");

    private final String OPERATION = "New Environment Event";
    private final String PARAMETERALL = "ALL";
    private IAPIKeyService apiKeyService;

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

        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        apiKeyService = appContext.getBean(IAPIKeyService.class);

        /**
         * Adding Log entry.
         */
        ILogEventService logEventService = appContext.getBean(ILogEventService.class);
        logEventService.createForPublicCalls("/NewEnvironmentEventV000", "CALL", LogEvent.STATUS_INFO, "NewEnvironmentEventV000 called : " + request.getRequestURL(), request);

        if (apiKeyService.authenticate(request, response)) {

            ICountryEnvParamService countryEnvParamService = appContext.getBean(ICountryEnvParamService.class);
            IInvariantService invariantService = appContext.getBean(IInvariantService.class);
            IBatchInvariantService batchInvariantService = appContext.getBean(IBatchInvariantService.class);
            IBuildRevisionBatchService buildRevisionBatchService = appContext.getBean(IBuildRevisionBatchService.class);
            INotificationService notificationService = appContext.getBean(INotificationService.class);

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
            if (system.isEmpty()) {
                out.println("Error - Parameter system is mandatory.");
                error = true;
            }
            if (!system.isEmpty() && !invariantService.isInvariantExist("SYSTEM", system)) {
                out.println("Error - System does not exist  : " + system);
                error = true;
            }
            if (environment.isEmpty()) {
                out.println("Error - Parameter environment is mandatory.");
                error = true;
            }
            if (!environment.isEmpty() && !invariantService.isInvariantExist("ENVIRONMENT", environment)) {
                out.println("Error - Environment does not exist  : " + environment);
                error = true;
            }
            if (country.isEmpty()) {
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
            if (event.isEmpty()) {
                out.println("Error - Parameter event is mandatory.");
                error = true;
            }
            if (!event.isEmpty() && !batchInvariantService.exist(event)) {
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

                if (country.equalsIgnoreCase(PARAMETERALL)) {
                    country = null;
                }
                AnswerList<CountryEnvParam> answerList = countryEnvParamService.readByVarious(system, country, environment, null, null, "Y");
                finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, answerList);

                for (CountryEnvParam cepData : answerList.getDataList()) {

                    /**
                     * For each object, we can update it.
                     */
                    // Adding CountryEnvParam Log entry.
                    buildRevisionBatchService.create(cepData.getSystem(), cepData.getCountry(), cepData.getEnvironment(), cepData.getBuild(), cepData.getRevision(), event);

                    /**
                     * Email notification.
                     */
                    String OutputMessage = "";
                    MessageEvent me = notificationService.generateAndSendNewChainEmail(cepData.getSystem(), cepData.getCountry(), cepData.getEnvironment(), event);

                    if (!"OK".equals(me.getMessage().getCodeString())) {
                        LOG.warn(Infos.getInstance().getProjectNameAndVersion() + " - Exception catched." + me.getMessage().getDescription());
                        logEventService.createForPublicCalls("/NewEnvironmentEventV000", "NEW", LogEvent.STATUS_WARN, "Warning on New environment event : ['" + cepData.getSystem() + "','" + cepData.getCountry() + "','" + cepData.getEnvironment() + "'] " + me.getMessage().getDescription(), request);
                        OutputMessage = me.getMessage().getDescription();
                    }

                    if (OutputMessage.isEmpty()) {
                        msg = new MessageEvent(MessageEventEnum.GENERIC_OK);
                        Answer answerSMTP = new AnswerList<>(msg);
                        finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, answerSMTP);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.GENERIC_WARNING);
                        msg.setDescription(msg.getDescription().replace("%REASON%", OutputMessage + " when sending email for " + cepData.getSystem() + "/" + cepData.getCountry() + "/" + cepData.getEnvironment()));
                        Answer answerSMTP = new AnswerList<>(msg);
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
