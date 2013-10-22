/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.servlet.test;

import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryLogEvent;
import com.redcats.tst.factory.impl.FactoryLogEvent;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.service.ILogEventService;
import com.redcats.tst.service.impl.LogEventService;
import com.redcats.tst.service.impl.UserService;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author acraske
 */
@WebServlet(name = "DeleteTest", urlPatterns = {"/DeleteTest"})
public class DeleteTest extends HttpServlet {

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        this.processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        this.processRequest(request, response);
    }

    public boolean formIsFill(String data) {

        return !data.isEmpty() && !data.trim().equals("") && !data.equals(" ");
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

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        DatabaseSpring database = appContext.getBean(DatabaseSpring.class);

        Connection connection = database.connect();
        try {

            /*
             * Test Delete
             */
            String test = request.getParameter("test_test");

            if (this.formIsFill(test)) {
                PreparedStatement stmt = connection.prepareStatement("DELETE FROM TestCaseExecution WHERE Test = ?");
                try {
                    stmt.setString(1, test);
                    stmt.executeUpdate();
                } finally {
                    stmt.close();
                }

                stmt = connection.prepareStatement("DELETE FROM Test WHERE Test = ?");
                try {
                    stmt.setString(1, test);
                    stmt.executeUpdate();
                } finally {
                    stmt.close();
                }

                /**
                 * Adding Log entry.
                 */
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
                try {
                    logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/DeleteTest", "DELETE", "Delete test : " + test, "", ""));
                } catch (CerberusException ex) {
                    Logger.getLogger(UserService.class.getName()).log(Level.ERROR, null, ex);
                }

            }

            response.sendRedirect("Test.jsp");

        } catch (SQLException ex) {
            MyLogger.log(DeleteTest.class.getName(), Level.FATAL, "" + ex);
        } finally {
            out.close();
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(DeleteTest.class.getName(), org.apache.log4j.Level.WARN, e.toString());
            }
        }
    }
}
