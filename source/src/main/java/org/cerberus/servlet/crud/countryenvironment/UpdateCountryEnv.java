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
package org.cerberus.servlet.crud.countryenvironment;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cerberus.database.DatabaseSpring;
import org.cerberus.log.MyLogger;
import org.cerberus.version.Infos;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author vertigo
 */
@WebServlet(name = "UpdateCountryEnv", urlPatterns = {"/UpdateCountryEnv"})
public class UpdateCountryEnv extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        DatabaseSpring database = appContext.getBean(DatabaseSpring.class);

        Connection connection = database.connect();
        try {
            String system = "";
            if (request.getParameter("system") != null && request.getParameter("system").compareTo("") != 0) {
                system = request.getParameter("system");
            }
            String country = "";
            if (request.getParameter("country") != null && request.getParameter("country").compareTo("") != 0) {
                country = request.getParameter("country");
            }
            String env = "";
            if (request.getParameter("env") != null && request.getParameter("env").compareTo("") != 0) {
                env = request.getParameter("env");
            }
            String type = "";
            if (request.getParameter("type") != null && request.getParameter("type").compareTo("") != 0) {
                type = request.getParameter("type");
            }
            String distriblist = "";
            if (request.getParameter("distriblist") != null && request.getParameter("distriblist").compareTo("") != 0) {
                distriblist = request.getParameter("distriblist");
            }
            String description = "";
            if (request.getParameter("description") != null && request.getParameter("description").compareTo("") != 0) {
                description = request.getParameter("description");
            }
            String bodydisenv = "";
            if (request.getParameter("bodydisenv") != null && request.getParameter("bodydisenv").compareTo("") != 0) {
                bodydisenv = request.getParameter("bodydisenv");
            }
            String bodyrev = "";
            if (request.getParameter("bodyrev") != null && request.getParameter("bodyrev").compareTo("") != 0) {
                bodyrev = request.getParameter("bodyrev");
            }
            String bodychain = "";
            if (request.getParameter("bodychain") != null && request.getParameter("bodychain").compareTo("") != 0) {
                bodychain = request.getParameter("bodychain");
            }


            Statement stmt = connection.createStatement();

            try {
                String req_update_active = "UPDATE countryenvparam "
                        + " SET DistribList='" + distriblist + "' , EMailBodyRevision='" + bodyrev + "'"
                        + ", EMailBodyChain='" + bodychain + "', EMailBodyDisableEnvironment='" + bodydisenv + "' "
                        + ", type='" + type + "', Description='" + description + "' "
                        + "WHERE `System`='" + system + "' and Country='" + country + "' and Environment='" + env + "'";
                stmt.executeUpdate(req_update_active);
            } finally {
                stmt.close();
            }

            response.sendRedirect("Environment.jsp?system=" + system + "&country=" + country + "&env=" + env);

        } catch (Exception e) {
            Logger.getLogger(UpdateCountryEnv.class.getName()).log(Level.SEVERE, Infos.getInstance().getProjectNameAndVersion() + " - Exception catched.", e);
            out.println(e.getMessage());
        } finally {
            out.close();
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UpdateCountryEnv.class.getName(), org.apache.log4j.Level.WARN, e.toString());
            }
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
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
