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
package org.cerberus.servlet.integration;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.service.ILogEventService;

import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.impl.ParameterService;
import org.cerberus.util.HTTPSession;
import org.cerberus.version.Infos;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet called from JQuery Datatable to request Jenkins to run deploy
 * pipeline
 *
 * @author Pete
 */
@WebServlet(name = "JenkinsDeploy", urlPatterns = {"/JenkinsDeploy"})
public class JenkinsDeploy extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(JenkinsDeploy.class);
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
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {

            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IParameterService parameterService = appContext.getBean(ParameterService.class);

            String user = parameterService.findParameterByKey("cerberus_jenkinsadmin_user", "").getValue();
            String pass = parameterService.findParameterByKey("cerberus_jenkinsadmin_password", "").getValue();

            HTTPSession session = new HTTPSession();
            session.startSession(user, pass);

            String url = parameterService.findParameterByKey("cerberus_jenkinsdeploy_url", "").getValue();
            String final_url;
            final_url = url.replace("%APPLI%", request.getParameter("application"));
            final_url = final_url.replace("%JENKINSBUILDID%", request.getParameter("jenkinsbuildid"));
            final_url = final_url.replace("%DEPLOYTYPE%", request.getParameter("deploytype"));
            final_url = final_url.replace("%JENKINSAGENT%", request.getParameter("jenkinsagent"));
            final_url = final_url.replace("%RELEASE%", request.getParameter("release"));
            final_url = final_url.replace("%REPOSITORYURL%", request.getParameter("repositoryurl"));

            //send request to Jenkins
            Integer responseCode = session.getURL(final_url);
            session.closeSession();
            if ((responseCode != 200) && (responseCode != 201)) {
                out.print("ERROR Contacting Jenkins HTTP Response " + responseCode);
            } else {
                /**
                 * Jenkins was called successfuly. Adding Log entry.
                 */
                ILogEventService logEventService = appContext.getBean(ILogEventService.class);
                logEventService.createForPrivateCalls("/JenkinsDeploy", "DEPLOY", "JenkinsDeploy Triggered : ['" + final_url + "']", request);
                out.print("Sent request : " + url);
            }

        } catch (Exception ex) {
            LOG.warn(Infos.getInstance().getProjectNameAndVersion() + " - Exception catched.", ex);
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
