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
package org.cerberus.servlet.buildrevision;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.entity.BuildRevisionInvariant;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryBuildRevisionInvariant;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.factory.impl.FactoryLogEvent;
import org.cerberus.service.IBuildRevisionInvariantService;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.impl.LogEventService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
public class CreateBuildRevisionInvariant extends HttpServlet {

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
            throws ServletException, IOException, CerberusException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            String system = request.getParameter("System");
            Integer level = Integer.valueOf(request.getParameter("Level"));
            Integer seq = Integer.valueOf(request.getParameter("Seq"));
            String versionName = request.getParameter("VersionName");

            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IBuildRevisionInvariantService buildRevisionInvariantService = appContext.getBean(IBuildRevisionInvariantService.class);
            IFactoryBuildRevisionInvariant factoryBuildRevisionInvariant = appContext.getBean(IFactoryBuildRevisionInvariant.class);

            BuildRevisionInvariant buildRevisionInvariantData = factoryBuildRevisionInvariant.create(system, level, seq, versionName);
            buildRevisionInvariantService.insertBuildRevisionInvariant(buildRevisionInvariantData);

            /**
             * Adding Log entry.
             */
            ILogEventService logEventService = appContext.getBean(LogEventService.class);
            IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
            try {
                logEventService.create_Deprecated(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/CreateBuildRevisionInvariant", "CREATE", "Create Build Revision Invariant : ['" + system + "'|'" + level + "'|'" + seq + "'] " + versionName, "", ""));
            } catch (CerberusException ex) {
                org.apache.log4j.Logger.getLogger(CreateBuildRevisionInvariant.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);
            }


            response.sendRedirect("InvariantPublic.jsp");
        } finally {
            out.close();
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
        try {
            processRequest(request, response);
        } catch (CerberusException ex) {
            Logger.getLogger(CreateBuildRevisionInvariant.class.getName()).log(Level.SEVERE, null, ex);
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
        try {
            processRequest(request, response);
        } catch (CerberusException ex) {
            Logger.getLogger(CreateBuildRevisionInvariant.class.getName()).log(Level.SEVERE, null, ex);
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
