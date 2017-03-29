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
package org.cerberus.servlet.crud.usermanagement;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.User;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.IUserService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.crud.service.impl.UserService;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "UpdateMyUserRobotPreference", urlPatterns = {"/UpdateMyUserRobotPreference"})
public class UpdateMyUserRobotPreference extends HttpServlet {

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
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IUserService userService = appContext.getBean(UserService.class);
        
        try {
                           
            String ss_ip = ParameterParserUtil.parseStringParam(request.getParameter("ss_ip"), "");
            String ss_p = ParameterParserUtil.parseStringParam(request.getParameter("ss_p"), "");
            String platform = ParameterParserUtil.parseStringParam(request.getParameter("platform"), "");
            String browser = ParameterParserUtil.parseStringParam(request.getParameter("browser"), "");
            String version = ParameterParserUtil.parseStringParam(request.getParameter("version"), "");

            User usr = userService.findUserByKey(request.getUserPrincipal().getName());
            usr.setRobotHost(ss_ip);
            usr.setRobotPort(ss_p);
            usr.setRobotPlatform(platform);
            usr.setRobotBrowser(browser);
            usr.setRobotVersion(version);
                            
            userService.updateUser(usr);
            
            ILogEventService logEventService = appContext.getBean(LogEventService.class);
            logEventService.createForPrivateCalls("/UpdateMyUserRobotPreference", "UPDATE", "Update user robot preference for user: " + usr.getLogin(), request);

            response.getWriter().print(usr.getLogin());
        } catch (CerberusException myexception) {
            response.getWriter().print(myexception.getMessageError().getDescription());
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
