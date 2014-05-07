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
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.Robot;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryLogEvent;
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
public class UpdateRobot extends HttpServlet {

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
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        try {
            String id = policy.sanitize(request.getParameter("id"));
            String columnName = policy.sanitize(request.getParameter("columnName"));
            String value = policy.sanitize(request.getParameter("value"));

            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IRobotService robotService = appContext.getBean(IRobotService.class);
            Robot robot = robotService.findRobotByKey(Integer.valueOf(id));

            if (columnName != null && "Platform".equals(columnName.trim())) {
                robot.setPlatform(value);
            }else if (columnName != null && "Browser".equals(columnName.trim())) {
                robot.setBrowser(value);
            } else if (columnName != null && "Version".equals(columnName.trim())) {
                robot.setVersion(value);
            } else if (columnName != null && "Host".equals(columnName.trim())) {
                robot.setHost(value);
            }else if (columnName != null && "Port".equals(columnName.trim())) {
                robot.setPort(value);
            } else if (columnName != null && "Robot".equals(columnName.trim())) {
                robot.setRobot(value);
            } else if (columnName != null && "Active".equals(columnName.trim())) {
                robot.setActive(value);
            } else if (columnName != null && "Description".equals(columnName.trim())) {
                robot.setDescription(value);
            } else {
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NOT_IMPLEMEMTED));
            }
            robotService.updateRobot(robot);
            /**
             * Adding Log entry.
             */
            ILogEventService logEventService = appContext.getBean(LogEventService.class);
            IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
            try {
                logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/UpdateRobot", "UPDATE", "Updated Robot : " + id, "", ""));
            } catch (CerberusException ex) {
                MyLogger.log(UpdateRobot.class.getName(), Level.ERROR,  ex.toString());
            }


            out.print(value);
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
       MyLogger.log(UpdateRobot.class.getName(), Level.ERROR,  ex.toString());
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
            String t = request.getParameter("value");
            processRequest(request, response);
        } catch (CerberusException ex) {
            MyLogger.log(UpdateRobot.class.getName(), Level.ERROR,  ex.toString());
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
