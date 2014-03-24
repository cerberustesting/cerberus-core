/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cerberus.servlet.invariant;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.entity.Invariant;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.factory.impl.FactoryLogEvent;
import org.cerberus.service.IInvariantService;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.impl.LogEventService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
public class UpdateInvariant extends HttpServlet {

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
        IInvariantService invariantService = appContext.getBean(IInvariantService.class);

        String[] invKey = key.split("\\$#");
        String idName = invKey[0];
        String invVal = invKey[1];

        Invariant invariantData = invariantService.findInvariantByIdValue(idName, invVal);

        switch (columnPosition) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                invariantData.setSort(Integer.valueOf(value));
                break;
            case 3:
                invariantData.setDescription(value);
                break;
            case 4:
                invariantData.setVeryShortDesc(value);
                break;
            case 5:
                invariantData.setGp1(value);
                break;
            case 6:
                invariantData.setGp2(value);
                break;
            case 7:
                invariantData.setGp3(value);
                break;
        }

        invariantService.updateInvariant(invariantData);

        /**
         * Adding Log entry.
         */
        ILogEventService logEventService = appContext.getBean(LogEventService.class);
        IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
        try {
            logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/UpdateInvariant", "UPDATE", "Updated invariant : ['" + idName + "'|'" + invVal + "']", "", ""));
        } catch (CerberusException ex) {
            Logger.getLogger(UpdateInvariant.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(UpdateInvariant.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(UpdateInvariant.class.getName()).log(Level.SEVERE, null, ex);
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
