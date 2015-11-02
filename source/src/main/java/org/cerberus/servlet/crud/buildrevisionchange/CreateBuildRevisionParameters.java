/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
package org.cerberus.servlet.crud.buildrevisionchange;

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
import org.cerberus.crud.factory.IFactoryBuildRevisionParameters;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.IBuildRevisionParametersService;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.util.answer.Answer;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "CreateBuildRevisionParameters", urlPatterns = {"/CreateBuildRevisionParameters"})
public class CreateBuildRevisionParameters extends HttpServlet {

    private final String OBJECT_NAME = "BuildRevisionParameters";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws org.cerberus.exception.CerberusException
     * @throws org.json.JSONException
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

        /**
         * Checking all constrains before calling the services.
         */
        if (false) {
            // No constrain on that Create operation.
        } else {
            /**
             * All data seems cleans so we can call the services.
             */
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IBuildRevisionParametersService buildRevisionParametersService = appContext.getBean(IBuildRevisionParametersService.class);
            IFactoryBuildRevisionParameters buildRevisionParametersFactory = appContext.getBean(IFactoryBuildRevisionParameters.class);

            BuildRevisionParameters brpData = buildRevisionParametersFactory.create(0, build, revision, release, application, project, ticketidfixed, bugidfixed, link, releaseowner, subject, null, jenkinsbuildid, mavenGroupID, mavenArtifactID, mavenVersion);
            ans = buildRevisionParametersService.create(brpData);

            if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                /**
                 * Object created. Adding Log entry.
                 */
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                logEventService.createPrivateCalls("/CreateBuildRevisionParameters", "CREATE", "Create BuildRevisionParameters : ['" + build + "']", request);
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
            Logger.getLogger(CreateBuildRevisionParameters.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(CreateBuildRevisionParameters.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(CreateBuildRevisionParameters.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(CreateBuildRevisionParameters.class.getName()).log(Level.SEVERE, null, ex);
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
