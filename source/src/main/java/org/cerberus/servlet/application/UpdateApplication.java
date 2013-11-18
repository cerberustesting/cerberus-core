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
package org.cerberus.servlet.application;

import org.cerberus.entity.Application;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.factory.impl.FactoryLogEvent;
import org.cerberus.log.MyLogger;
import org.cerberus.service.IApplicationService;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.impl.ApplicationService;
import org.cerberus.service.impl.LogEventService;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author vertigo
 */
@WebServlet(name = "UpdateApplicationAjax", urlPatterns = {"/UpdateApplicationAjax"})
public class UpdateApplication extends HttpServlet {

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
        String application = request.getParameter("id");
        int columnPosition = Integer.parseInt(request.getParameter("columnPosition"));
        String value = request.getParameter("value").replaceAll("'", "");

        MyLogger.log(UpdateApplication.class.getName(), Level.INFO, "value : " + value + " columnPosition : " + columnPosition + " application : " + application);

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IApplicationService applicationService = appContext.getBean(ApplicationService.class);

        Application myApplication;
        try {
            try {
                myApplication = applicationService.findApplicationByKey(application);
                switch (columnPosition) {
                    case 1:
                        myApplication.setSystem(value);
                        break;
                    case 2:
                        myApplication.setSubsystem(value);
                        break;
                    case 3:
                        myApplication.setDescription(value);
                        break;
                    case 4:
                        myApplication.setInternal(value);
                        break;
                    case 5:
                        myApplication.setType(value);
                        break;
                    case 6:
                        myApplication.setMavengroupid(value);
                        break;
                    case 7:
                        myApplication.setDeploytype(value);
                        break;
                    case 8:
                        try {
                            myApplication.setSort(Integer.valueOf(value));
                        } catch (Exception ex) {
                            response.getWriter().print(ex.getMessage());
                        }
                        break;
                    case 9:
                        myApplication.setSvnurl(value);
                        break;
                    case 10:
                        myApplication.setBugTrackerUrl(value);
                        break;
                    case 11:
                        myApplication.setBugTrackerNewUrl(value);
                        break;
                }

                applicationService.updateApplication(myApplication);
                
                /**
                 * Adding Log entry.
                 */
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
                try {
                    logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/UpdateApplicationAjax", "UPDATE", "Update application : " + application, "", ""));
                } catch (CerberusException ex) {
                    Logger.getLogger(UpdateApplication.class.getName()).log(Level.ERROR, null, ex);
                }
                response.getWriter().print(value);

            } catch (CerberusException ex) {
                java.util.logging.Logger.getLogger(UpdateApplication.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }


        } finally {
        }
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
        processRequest(request, response);
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
