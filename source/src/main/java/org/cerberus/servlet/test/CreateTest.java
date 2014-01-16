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
package org.cerberus.servlet.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.factory.impl.FactoryLogEvent;
import org.cerberus.log.MyLogger;
import org.cerberus.refactor.AddTest;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.impl.LogEventService;
import org.cerberus.service.impl.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author acraske
 */
@WebServlet(name = "CreateTest", urlPatterns = {"/CreateTest"})
public class CreateTest extends HttpServlet {

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

    /*
     * Return true if all fields contains in the testcase_info are not null at
     * the specified index
     */
    public boolean formIsFullFill(List<String[]> testcase_info, int index) {

        for (String[] t : testcase_info) {
            if (t[index].isEmpty() || t[index].trim().equals("") || t[index].equals(" ")) {
                return false;
            }
        }

        return true;
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
             * Test Insert
             */
            String test = request.getParameter("createTest");
            if (request.getParameter("createTest") == null) {
                test = "";
            }
            String description = request.getParameter("createDescription");
            if (request.getParameter("createDescription") == null) {
                description = "";
            }
            String active = request.getParameter("createActive");
            if (request.getParameter("createActive") == null) {
                active = "";
            }
            String automated = request.getParameter("createAutomated");
            if (request.getParameter("createAutomated") == null) {
                automated = "";
            }


            /*
             * Check that a real modification has been done and the test not
             * already exists
             */

            if (!test.equals("")) {
                PreparedStatement stmt = connection.prepareStatement("SELECT Test FROM test WHERE Test = ?");
                try {
                    stmt.setString(1, test);
                    ResultSet rs_test_exists = stmt.executeQuery();
                    try {
                        if (!rs_test_exists.next()) {

                            String sql = "INSERT INTO test (`Test`,`Description`,`Active`,`Automated`) VALUES(?, ?, ?, ?)";
                            PreparedStatement stmt2 = connection.prepareStatement(sql);
                            try {
                                stmt2.setString(1, test);
                                stmt2.setString(2, description);
                                stmt2.setString(3, active);
                                stmt2.setString(4, automated);
                                stmt2.executeUpdate();
                            } finally {
                                stmt2.close();
                            }

                            /**
                             * Adding Log entry.
                             */
                            ILogEventService logEventService = appContext.getBean(LogEventService.class);
                            IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
                            try {
                                logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/CreateTest", "CREATE", "Create test : " + test, "", ""));
                            } catch (CerberusException ex) {
                                Logger.getLogger(UserService.class.getName()).log(Level.ERROR, null, ex);
                            }
                        } else {
                            out.print("The test already exists. Please, go back to the previous page and choose another test name");
                        }
                    } finally {
                        rs_test_exists.close();
                    }
                } finally {
                    stmt.close();
                }
            } else {
                out.print("Could not record an empty test");
            }

            response.sendRedirect("Test.jsp?stestbox=" + test);

        } catch (SQLException ex) {
            MyLogger.log(AddTest.class.getName(), Level.FATAL, "" + ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(AddTest.class.getName(), Level.WARN, e.toString());
            }
            out.close();
        }
    }
}
