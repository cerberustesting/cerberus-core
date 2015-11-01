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
package org.cerberus.servlet.buildcontent;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.BuildRevisionParameters;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.service.IBuildRevisionParametersService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
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
@WebServlet(name = "UpdateBuildRevisionParameters", urlPatterns = {"/UpdateBuildRevisionParameters"})
public class UpdateBuildRevisionParameters extends HttpServlet {

    private final String OBJECT_NAME = "BuildRevisionParameters";

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
        String build = policy.sanitize(request.getParameter("build"));
        String revision = policy.sanitize(request.getParameter("revision"));
        String release = policy.sanitize(request.getParameter("release"));
        String application = policy.sanitize(request.getParameter("application"));
        String project = policy.sanitize(request.getParameter("project"));
        String ticketidfixed = policy.sanitize(request.getParameter("ticketidfixed"));
        String bugidfixed = policy.sanitize(request.getParameter("bugidfixed"));
        String link = policy.sanitize(request.getParameter("link"));
        String releaseowner = policy.sanitize(request.getParameter("releaseowner"));
        String subject = policy.sanitize(request.getParameter("subject"));
        String jenkinsbuildid = policy.sanitize(request.getParameter("jenkinsbuildid"));
        String mavenGroupID = policy.sanitize(request.getParameter("mavengroupid"));
        String mavenArtifactID = policy.sanitize(request.getParameter("mavenartifactid"));
        String mavenVersion = policy.sanitize(request.getParameter("mavenversion"));
        Integer brpid = 0;
        boolean brpid_error = true;
        try {
            if (request.getParameter("id") != null && !request.getParameter("id").equals("")) {
                brpid = Integer.valueOf(policy.sanitize(request.getParameter("id")));
                brpid_error = false;
            }
        } catch (Exception ex) {
            brpid_error = true;
        }

        /**
         * Checking all constrains before calling the services.
         */
        if (brpid_error) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "Could not manage to convert id to an integer value or id is missing."));
            ans.setResultMessage(msg);
        } else {
            /**
             * All data seems cleans so we can call the services.
             */
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IBuildRevisionParametersService brpService = appContext.getBean(IBuildRevisionParametersService.class);

            AnswerItem resp = brpService.readByKeyTech(brpid);
            if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()))) {
                /**
                 * Object could not be found. We stop here and report the error.
                 */
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                        .replace("%OPERATION%", "Update")
                        .replace("%REASON%", "BuildRevisionParameters does not exist."));
                ans.setResultMessage(msg);

            } else {
                /**
                 * The service was able to perform the query and confirm the
                 * object exist, then we can update it.
                 */
                BuildRevisionParameters brpData = (BuildRevisionParameters) resp.getItem();

                /**
                 * Before updating, we check that the old entry can be modified.
                 * If old entry point to a build/revision that already been
                 * deployed, we cannot update it.
                 */
                if (brpService.check_buildRevisionAlreadyUsed(brpData.getApplication(), brpData.getBuild(), brpData.getRevision())) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                            .replace("%OPERATION%", "Update")
                            .replace("%REASON%", "Could not update this release as its original build " + brpData.getBuild() + " revision " + brpData.getRevision() + " has already been deployed in an environment."));
                    ans.setResultMessage(msg);
                } else {

                    brpData.setBuild(build);
                    brpData.setRevision(revision);
                    brpData.setRelease(release);
                    brpData.setApplication(application);
                    brpData.setProject(project);
                    brpData.setTicketIdFixed(ticketidfixed);
                    brpData.setBugIdFixed(bugidfixed);
                    brpData.setLink(link);
                    brpData.setReleaseOwner(releaseowner);
                    brpData.setSubject(subject);
                    brpData.setJenkinsBuildId(jenkinsbuildid);
                    brpData.setMavenGroupId(mavenGroupID);
                    brpData.setMavenArtifactId(mavenArtifactID);
                    brpData.setMavenVersion(mavenVersion);
                    ans = brpService.update(brpData);

                    if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        /**
                         * Update was succesfull. Adding Log entry.
                         */
                        ILogEventService logEventService = appContext.getBean(LogEventService.class);
                        logEventService.createPrivateCalls("/UpdateBuildRevisionParameters", "UPDATE", "Updated BuildRevisionParameters : ['" + brpid + "'|'" + build + "'|'" + revision + "'|'" + release + "']", request);
                    }
                }
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
            Logger.getLogger(UpdateBuildRevisionParameters.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(UpdateBuildRevisionParameters.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(UpdateBuildRevisionParameters.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(UpdateBuildRevisionParameters.class.getName()).log(Level.SEVERE, null, ex);
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
