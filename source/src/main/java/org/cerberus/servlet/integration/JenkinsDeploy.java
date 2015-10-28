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
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
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
     * Handles the HTTP
     * <code>GET</code> method.
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

            String user = parameterService.findParameterByKey("jenkins_admin_user","").getValue();
            String pass = parameterService.findParameterByKey("jenkins_admin_password","").getValue();


            HTTPSession session = new HTTPSession();
            session.startSession(user, pass);

            String url = parameterService.findParameterByKey("jenkins_deploy_url","").getValue();
            String final_url;
            final_url = url.replaceAll("%APPLI%", request.getParameter("application"));
            final_url = final_url.replaceAll("%JENKINSBUILDID%", request.getParameter("jenkinsbuildid"));
            final_url = final_url.replaceAll("%DEPLOYTYPE%", request.getParameter("deploytype"));
            final_url = final_url.replaceAll("%JENKINSAGENT%", request.getParameter("jenkinsagent"));
            final_url = final_url.replaceAll("%RELEASE%", request.getParameter("release"));

            //send request to Jenkins
            Integer responseCode = session.getURL(final_url);
            session.closeSession();
            if ((responseCode != 200) && (responseCode != 201)) {
                out.print("ERROR Contacting Jenkins HTTP Response " + responseCode);
            } else {
                out.print("Sent request : " + url);
            }

        } catch (Exception ex) {
            Logger.getLogger(JenkinsDeploy.class.getName()).log(Level.SEVERE,
                    Infos.getInstance().getProjectNameAndVersion() + " - Exception catched.", ex);
        }

    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
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
