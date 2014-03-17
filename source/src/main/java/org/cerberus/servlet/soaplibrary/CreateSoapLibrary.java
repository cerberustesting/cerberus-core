/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cerberus.servlet.soaplibrary;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.cerberus.entity.SoapLibrary;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactorySoapLibrary;
import org.cerberus.log.MyLogger;
import org.cerberus.service.ISoapLibraryService;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author cte
 */
public class CreateSoapLibrary extends HttpServlet {
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
            throws ServletException, IOException, CerberusException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        
        try {
        String type = policy.sanitize(request.getParameter("Type"));
        String name = policy.sanitize(request.getParameter("Name"));
        String envelope = policy.sanitize(request.getParameter("Envelope"));
        String description = policy.sanitize(request.getParameter("Description"));
        String servicePath = policy.sanitize(request.getParameter("ServicePath"));
        String parsingAnswer = policy.sanitize(request.getParameter("ParsingAnswer"));
        String method = policy.sanitize(request.getParameter("Method"));
        
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ISoapLibraryService soapLibraryService = appContext.getBean(ISoapLibraryService.class);
        IFactorySoapLibrary factorySoapLibrary = appContext.getBean(IFactorySoapLibrary.class);
        
        SoapLibrary soapLib = factorySoapLibrary.create(type, name, envelope, description, servicePath, parsingAnswer, method);
        soapLibraryService.createSoapLibrary(soapLib);
            
        response.sendRedirect("SoapLibrary.jsp");
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
        try {
            this.processRequest(request, response);
        } catch (CerberusException ex) {
            MyLogger.log(CreateSoapLibrary.class.getName(), Level.FATAL, ex.toString());
        }
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
        try {
            this.processRequest(request, response);
        } catch (CerberusException ex) {
            MyLogger.log(CreateSoapLibrary.class.getName(), Level.FATAL, ex.toString());
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
