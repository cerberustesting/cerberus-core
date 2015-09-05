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
package org.cerberus.servlet.countryenvparam;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.entity.CountryEnvParam;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryCountryEnvParam;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.service.ICountryEnvParamService;
import org.cerberus.service.ILogEventService;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
public class CreateCountryEnvParam extends HttpServlet {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CreateCountryEnvParam.class);
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
        final PrintWriter out = response.getWriter();
        final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        try {
            final String system = policy.sanitize(request.getParameter("System"));
            final String country = policy.sanitize(request.getParameter("Country"));
            final String environment = policy.sanitize(request.getParameter("Environment"));
            final String build = policy.sanitize(request.getParameter("Build"));
            final String revision = policy.sanitize(request.getParameter("Revision"));
            final String chain = policy.sanitize(request.getParameter("Chain"));
            final String distribList = policy.sanitize(request.getParameter("DistribList"));
            final String emailBodyRevision = policy.sanitize(request.getParameter("EmailBodyRevision"));
            final String type = policy.sanitize(request.getParameter("Type"));
            final String emailBodyChain = policy.sanitize(request.getParameter("EmailBodyChain"));
            final String emailBodyDisableEnvironment = policy.sanitize(request.getParameter("EmailBodyDisableEnvironment"));
            final boolean active = "Y".equals(policy.sanitize(request.getParameter("Active")))?true:false;
            final boolean maintenanceAct = "Y".equals(policy.sanitize(request.getParameter("MaintenanceAct")))?true:false;
            final String maintenanceStr = policy.sanitize(request.getParameter("MaintenanceStr"));
            final String maintenanceEnd = policy.sanitize(request.getParameter("MaintenanceEnd"));

            final ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            final ICountryEnvParamService cepService = appContext.getBean(ICountryEnvParamService.class);
            final IFactoryCountryEnvParam factoryCep = appContext.getBean(IFactoryCountryEnvParam.class);

            final CountryEnvParam cep = factoryCep.create(system, country, environment, build, revision, chain, distribList, emailBodyRevision, type, emailBodyChain, emailBodyDisableEnvironment, active, maintenanceAct, maintenanceStr, maintenanceEnd);
            cepService.create(cep);

            /**
             * Adding Log entry.
             */
            ILogEventService logEventService = appContext.getBean(ILogEventService.class);
            IFactoryLogEvent factoryLogEvent = appContext.getBean(IFactoryLogEvent.class);
            try {
                logEventService.create_Deprecated(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/CreateCountryEnvParam", "CREATE", "Create CountryEnvParam : " + country + "_" + environment, "", ""));
            } catch (CerberusException ex) {
                LOG.error(ex);
            }

            response.sendRedirect("/EnvironmentManagement.jsp");
        } catch (CerberusException ex) {
            Logger.getLogger(CreateCountryEnvParam.class.getName()).log(Level.SEVERE, null, ex);
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
