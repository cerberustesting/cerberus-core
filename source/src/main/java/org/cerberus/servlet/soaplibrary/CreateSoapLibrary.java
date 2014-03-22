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
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.factory.IFactorySoapLibrary;
import org.cerberus.factory.impl.FactoryLogEvent;
import org.cerberus.log.MyLogger;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.ISoapLibraryService;
import org.cerberus.service.impl.LogEventService;
import org.cerberus.servlet.sqllibrary.CreateSqlLibrary;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.HtmlUtils;

/**
 *
 * @author cte
 */
public class CreateSoapLibrary extends HttpServlet {

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
    final void processRequest(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException, CerberusException {
        response.setContentType("text/html;charset=UTF-8");
        final PrintWriter out = response.getWriter();
        final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        try {
            final String type = policy.sanitize(request.getParameter("Type"));
            final String name = policy.sanitize(request.getParameter("Name"));
            // CTE - on utilise la méthode utilitaire pour encoder le xml
            final String envelope = request.getParameter("Envelope");
            final String envelopeBDD = HtmlUtils.htmlEscape(envelope);
            final String description = policy.sanitize(request.getParameter("Description"));
            final String servicePath = policy.sanitize(request.getParameter("ServicePath"));
            final String parsingAnswer = policy.sanitize(request.getParameter("ParsingAnswer"));
            final String method = policy.sanitize(request.getParameter("Method"));

            final ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            final ISoapLibraryService soapLibraryService = appContext.getBean(ISoapLibraryService.class);
            final IFactorySoapLibrary factorySoapLibrary = appContext.getBean(IFactorySoapLibrary.class);

            final SoapLibrary soapLib = factorySoapLibrary.create(type, name, envelopeBDD, description, servicePath, parsingAnswer, method);
            soapLibraryService.createSoapLibrary(soapLib);

            /**
             * Adding Log entry.
             */
            ILogEventService logEventService = appContext.getBean(LogEventService.class);
            IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
            try {
                logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/CreateSoapLibrary", "CREATE", "Create SoapLibrary : " + name, "", ""));
            } catch (CerberusException ex) {
                org.apache.log4j.Logger.getLogger(CreateSoapLibrary.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);
            }

            response.sendRedirect("SoapLibrary.jsp");
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
            this.processRequest(request, response);
        } catch (CerberusException ex) {
            MyLogger.log(CreateSoapLibrary.class.getName(), Level.FATAL, ex.toString());
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
