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
package org.cerberus.servlet.countryenvironmentparameters;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.entity.CountryEnvironmentApplication;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryCountryEnvironmentApplication;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.service.ICountryEnvironmentApplicationService;
import org.cerberus.service.ILogEventService;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
public class CreateCountryEnvironmentParameter extends HttpServlet {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CreateCountryEnvironmentParameter.class);
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
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        try {
            String system = policy.sanitize(request.getParameter("System"));
            String country = policy.sanitize(request.getParameter("Country"));
            String environment = policy.sanitize(request.getParameter("Environment"));
            String application = policy.sanitize(request.getParameter("Application"));
            String ip = policy.sanitize(request.getParameter("IP"));
            String domain = policy.sanitize(request.getParameter("domain"));
            String url = policy.sanitize(request.getParameter("Url"));
            String urlLogin = policy.sanitize(request.getParameter("UrlLogin"));

            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            ICountryEnvironmentApplicationService cepService = appContext.getBean(ICountryEnvironmentApplicationService.class);
            IFactoryCountryEnvironmentApplication factoryCep = appContext.getBean(IFactoryCountryEnvironmentApplication.class);

            CountryEnvironmentApplication cea = factoryCep.create(system, country, environment, application, ip, domain, url, urlLogin);
            cepService.create(cea);

            /**
             * Adding Log entry.
             */
    
            ILogEventService logEventService = appContext.getBean(ILogEventService.class);
            IFactoryLogEvent factoryLogEvent = appContext.getBean(IFactoryLogEvent.class);
            try {
                logEventService.create_Deprecated(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/CreateCEP", "CREATE", "Create CountryEnvironmentApplication : " + country+"_"+environment+"_"+application, "", ""));
            } catch (CerberusException ex) {
                LOG.error(ex);
            }

            response.getWriter().append(country+"_"+environment+"_"+application).close();
            } catch (CerberusException ex) {
            LOG.error(ex);
            response.getWriter().append("Unable to create CountryEnvironmentApplication").close();
        } finally {
            out.close();
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
