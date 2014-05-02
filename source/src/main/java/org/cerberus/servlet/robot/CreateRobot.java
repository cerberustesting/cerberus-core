/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cerberus.servlet.robot;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.cerberus.entity.Robot;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.factory.IFactoryRobot;
import org.cerberus.factory.impl.FactoryLogEvent;
import org.cerberus.log.MyLogger;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.IRobotService;
import org.cerberus.service.impl.LogEventService;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
public class CreateRobot extends HttpServlet {

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
        String name = policy.sanitize(request.getParameter("Name"));
        String ip = policy.sanitize(request.getParameter("Ip"));
        String port = policy.sanitize(request.getParameter("Port"));
        String description = policy.sanitize(request.getParameter("Description"));
        String platform = policy.sanitize(request.getParameter("Platform"));
        String browser = policy.sanitize(request.getParameter("Browser"));
        String version = policy.sanitize(request.getParameter("Version"));
        
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IRobotService robotService = appContext.getBean(IRobotService.class);
        IFactoryRobot factoryRobot = appContext.getBean(IFactoryRobot.class);
        
        Robot robot = factoryRobot.create(0, name, ip, Integer.valueOf(port), platform, browser, version, description);
        robotService.createRobot(robot);
            /**
             * Adding Log entry.
             */
            ILogEventService logEventService = appContext.getBean(LogEventService.class);
            IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
            try {
                logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/CreateRobot", "CREATE", "Create Robot : " + platform + "/"+browser+"/"+version, "", ""));
            } catch (CerberusException ex) {
                MyLogger.log(CreateRobot.class.getName(), Level.ERROR, ex.toString());
            }

        response.sendRedirect("Robot.jsp");
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
            processRequest(request, response);
        } catch (CerberusException ex) {
       MyLogger.log(CreateRobot.class.getName(), Level.FATAL, ex.toString());
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
            processRequest(request, response);
        } catch (CerberusException ex) {
            MyLogger.log(CreateRobot.class.getName(), Level.FATAL, ex.toString());
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
