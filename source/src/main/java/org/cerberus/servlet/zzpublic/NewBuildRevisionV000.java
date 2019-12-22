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
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.CountryEnvParam;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.crud.service.IBuildRevisionInvariantService;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.ICountryEnvParamService;
import org.cerberus.crud.service.ICountryEnvParam_logService;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.service.email.IEmailGenerationService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.AnswerUtil;
import org.cerberus.version.Infos;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.cerberus.service.email.IEmailService;
import org.cerberus.service.email.entity.Email;

/**
 * @author vertigo
 */
@WebServlet(name = "NewBuildRevisionV000", urlPatterns = {"/NewBuildRevisionV000"})
public class NewBuildRevisionV000 extends HttpServlet {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger("NewBuildRevisionV000");

    private final String OPERATION = "New Build/Revision";
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

        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        /**
         * Adding Log entry.
         */
        ILogEventService logEventService = appContext.getBean(ILogEventService.class);
        logEventService.createForPublicCalls("/NewBuildRevisionV000", "CALL", "NewBuildRevisionV000 called : " + request.getRequestURL(), request);

        ICountryEnvParamService countryEnvParamService = appContext.getBean(ICountryEnvParamService.class);
        IInvariantService invariantService = appContext.getBean(IInvariantService.class);
        IBuildRevisionInvariantService buildRevisionInvariantService = appContext.getBean(IBuildRevisionInvariantService.class);
        IEmailService emailService = appContext.getBean(IEmailService.class);
        IEmailGenerationService emailGenerationService = appContext.getBean(IEmailGenerationService.class);
        ICountryEnvParam_logService countryEnvParam_logService = appContext.getBean(ICountryEnvParam_logService.class);
        IParameterService parameterService = appContext.getBean(IParameterService.class);

        // Parsing all parameters.
        String system = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("system"), "", charset);
        String country = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("country"), "", charset);
        String environment = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("environment"), "", charset);
        String build = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("build"), "", charset);
        String revision = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("revision"), "", charset);

        String helpMessage = "\nThis servlet is used to inform Cerberus that a new Build and Revision has been deployed on a system.\n\nParameter list :\n"
                + "- system [mandatory] : the system where the Build Revision has been deployed. [" + system + "]\n"
                + "- country [mandatory] : the country where the Build Revision has been deployed. You can use ALL if you want to perform the action for all countries that exist for the given system and environement. [" + country + "]\n"
                + "- environment [mandatory] : the environment where the Build Revision has been deployed. [" + environment + "]\n"
                + "- build [mandatory] : the build that has been deployed. [" + build + "]\n"
                + "- revision [mandatory] : the revision that has been deployed. [" + revision + "]\n";

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
        if (build.equalsIgnoreCase("")) {
            out.println("Error - Parameter build is mandatory.");
            error = true;
        }
        if (!build.equalsIgnoreCase("") && !buildRevisionInvariantService.exist(system, 1, build)) {
            out.println("Error - Build does not exist : " + build);
            error = true;
        }
        if (revision.equalsIgnoreCase("")) {
            out.println("Error - Parameter revision is mandatory.");
            error = true;
        }
        if (!revision.equalsIgnoreCase("") && !buildRevisionInvariantService.exist(system, 2, revision)) {
            out.println("Error - Revision does not exist : " + revision);
            error = true;
        }

        // Starting the database update only when no blocking error has been detected.
        if (error == false) {

            /**
             * Getting the list of objects to treat.
             */
            // We update the object.
            MessageEvent msg = new MessageEvent(MessageEventEnum.GENERIC_OK);
            Answer finalAnswer = new Answer(msg);

            AnswerList<CountryEnvParam> answerList = new AnswerList<>();
            if (country.equalsIgnoreCase(PARAMETERALL)) {
                country = null;
            }
            answerList = countryEnvParamService.readByVarious(system, country, environment, null, null, null);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) answerList);

            for (CountryEnvParam cepData : answerList.getDataList()) {

                // Email Calculation. Email must be calcuated before we update the Build and revision in order to have the old build revision still available in the mail.
                String OutputMessage = "";
                Email email = null;
                try {
                    email = emailGenerationService.generateRevisionChangeEmail(cepData.getSystem(), cepData.getCountry(), cepData.getEnvironment(), build, revision);
                } catch (Exception ex) {
                    LOG.warn(Infos.getInstance().getProjectNameAndVersion() + " - Exception catched.", ex);
                    logEventService.createForPrivateCalls("/NewBuildRevisionV000", "NEWBUILDREV", "Warning on New Build/Revision environment : ['" + cepData.getSystem() + "','" + cepData.getCountry() + "','" + cepData.getEnvironment() + "'] " + ex.getMessage(), request);
                    OutputMessage = ex.getMessage();
                }

                /**
                 * For each object, we can update it.
                 */
                cepData.setBuild(build);
                cepData.setRevision(revision);
                cepData.setActive(true);
                Answer answerUpdate = countryEnvParamService.update(cepData);

                if (!(answerUpdate.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()))) {
                    /**
                     * Object could not be updated. We stop here and report the
                     * error.
                     */
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, answerUpdate);

                } else {
                    /**
                     * Update was successful.
                     */
                    // Adding Log entry.
                    logEventService.createForPrivateCalls("/NewBuildRevisionV000", "UPDATE", "Updated CountryEnvParam : ['" + cepData.getSystem() + "','" + cepData.getCountry() + "','" + cepData.getEnvironment() + "']", request);

                    // Adding CountryEnvParam Log entry.
                    countryEnvParam_logService.createLogEntry(cepData.getSystem(), cepData.getCountry(), cepData.getEnvironment(), build, revision, "New Build Revision.", "PublicCall");

                    /**
                     * Email notification.
                     */
                    try {
                        //Sending the email
                        emailService.sendHtmlMail(email);
                    } catch (Exception e) {
                        LOG.warn(Infos.getInstance().getProjectNameAndVersion() + " - Exception catched.", e);
                        logEventService.createForPrivateCalls("/NewBuildRevisionV000", "NEWBUILDREV", "Warning on New Build/Revision environment : ['" + cepData.getSystem() + "','" + cepData.getCountry() + "','" + cepData.getEnvironment() + "'] " + e.getMessage(), request);
                        OutputMessage = e.getMessage();
                    }

                    if (OutputMessage.equals("")) {
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
