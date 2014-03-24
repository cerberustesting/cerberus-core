/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cerberus.servlet.project;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.entity.Project;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.factory.impl.FactoryLogEvent;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.IProjectService;
import org.cerberus.service.impl.LogEventService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
public class UpdateProject extends HttpServlet {

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
        String key = request.getParameter("id");
        int columnPosition = Integer.parseInt(request.getParameter("columnPosition"));
        String value = request.getParameter("value").replaceAll("'", "");


        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IProjectService projectService = appContext.getBean(IProjectService.class);


        Project projectData = projectService.findProjectByKey(key);

        switch (columnPosition) {
            case 0:
                break;
            case 1:
                projectData.setCode(value);
                break;
            case 2:
                projectData.setDescription(value);
                break;
            case 3:
                projectData.setActive(value);
                break;
            case 4:
                break;
        }

        projectService.updateProject(projectData);

        /**
         * Adding Log entry.
         */
        ILogEventService logEventService = appContext.getBean(LogEventService.class);
        IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
        try {
            logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/UpdateProject", "UPDATE", "Updated Project : ['" + key + "']", "", ""));
        } catch (CerberusException ex) {
            Logger.getLogger(UpdateProject.class.getName()).log(Level.SEVERE, null, ex);
        }

        response.getWriter().print(value);
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
            Logger.getLogger(UpdateProject.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(UpdateProject.class.getName()).log(Level.SEVERE, null, ex);
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
