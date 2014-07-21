/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cerberus.servlet.application;

import org.cerberus.servlet.invariant.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.entity.Application;
import org.cerberus.entity.Invariant;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryApplication;
import org.cerberus.factory.IFactoryInvariant;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.factory.impl.FactoryLogEvent;
import org.cerberus.service.IApplicationService;
import org.cerberus.service.IInvariantService;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.impl.LogEventService;
import org.cerberus.service.impl.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
public class CreateApplication extends HttpServlet {

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
            String application = request.getParameter("Application");
            String system = request.getParameter("System");
            String subSystem = request.getParameter("SubSystem");
            String type = request.getParameter("Type");
            String mavenGpID = request.getParameter("MavenGroupID");
            String deployType = request.getParameter("DeployType");
            String svnURL = request.getParameter("SVNURL");
            String bugTrackerURL = request.getParameter("BugTrackerURL");
            String newBugURL = request.getParameter("NewBugURL");
            String description = request.getParameter("Description");
            Integer sort = 10;
            try {
                if (request.getParameter("Sort")!=null && !request.getParameter("Sort").equals("")){
                sort = Integer.valueOf(request.getParameter("Sort"));
                }

                ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
                IApplicationService applicationService = appContext.getBean(IApplicationService.class);
                IFactoryApplication factoryApplication = appContext.getBean(IFactoryApplication.class);

                Application applicationData = factoryApplication.create(application, description, sort, type, system, subSystem, svnURL, deployType, mavenGpID, bugTrackerURL, newBugURL);
                applicationService.createApplication(applicationData);

                /**
                 * Adding Log entry.
                 */
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
                try {
                    logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/CreateApplication", "CREATE", "Create Application : " + application, "", ""));
                } catch (CerberusException ex) {
                    org.apache.log4j.Logger.getLogger(UserService.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);
                }

            } catch (Exception ex) {
                out.print(ex.toString());
            }

            //response.sendRedirect("Application.jsp");
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
            Logger.getLogger(CreateInvariant.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(CreateInvariant.class.getName()).log(Level.SEVERE, null, ex);
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
