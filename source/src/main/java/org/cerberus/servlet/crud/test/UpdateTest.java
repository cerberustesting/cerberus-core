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
package org.cerberus.servlet.crud.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.log.MyLogger;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.impl.LogEventService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author acraske
 */
@WebServlet(name = "UpdateTest", urlPatterns = {"/UpdateTest"})
public class UpdateTest extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        this.processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        this.processRequest(request, response);
    }

    public boolean formIsFill(String data) {

        return !data.isEmpty() && !data.trim().equals("") && !data.equals(" ");
    }

    /*
     * Return true if all fields contains in the testcase_info are not null at
     * the specified index
     */
    public boolean formIsFullFill(List<String[]> testcase_info, int index,
            PrintWriter out) {

        for (String[] t : testcase_info) {
            if (t[index].isEmpty() || t[index].trim().equals("")
                    || t[index].equals(" ")) {
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

    protected void processRequest(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        DatabaseSpring database = appContext.getBean(DatabaseSpring.class);

        Connection connection = database.connect();
        try {

            //out.println("Database1.DBUrl :" + Database.DBUrl);

            /*
             * Test Update
             */
            String test = request.getParameter("test_test");
            if (request.getParameter("test_test") == null) {
                test = "";
            }
            String testDescription = request.getParameter("test_description");
            if (request.getParameter("test_description") == null) {
                testDescription = "";
            }
            String active = request.getParameter("test_active");
            if (request.getParameter("test_active") == null) {
                active = "";
            }
            String automated = request.getParameter("test_automated");
            if (request.getParameter("test_automated") == null) {
                automated = "";
            }

            String sql = "UPDATE test SET Description = ?, Active = ?, Automated = ? WHERE Test = ?";

            PreparedStatement stmt = connection.prepareStatement(sql);
            try {
                stmt.setString(1, testDescription);
                stmt.setString(2, active);
                stmt.setString(3, automated);
                stmt.setString(4, test);
                stmt.executeUpdate();
            } finally {
                stmt.close();
            }

            /**
             * Adding Log entry.
             */
            ILogEventService logEventService = appContext.getBean(LogEventService.class);
            logEventService.createPrivateCalls("/UpdateTest", "UPDATE", "Update test : " + test, request);

            response.sendRedirect("Test.jsp?stestbox=" + test);

        } catch (SQLException ex) {
            MyLogger.log(UpdateTest.class.getName(), Level.FATAL, "" + ex);
            out.println(UpdateTest.class.getName() + ex);
        } catch (NullPointerException ex) {
            MyLogger.log(UpdateTest.class.getName(), Level.FATAL, "" + ex);
            out.println(UpdateTest.class.getName() + ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UpdateTest.class.getName(), Level.WARN, e.toString());
            }
        }
    }

    /*
     * Return true if the dest table contains the src String
     */
    public boolean valueIsContainsInOtherTable(String src, String[] dest) {

        for (String s : dest) {
            if (s.equals(src)) {
                return true;
            }
        }

        return false;
    }
}
