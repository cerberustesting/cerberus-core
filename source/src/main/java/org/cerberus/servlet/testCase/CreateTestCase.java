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
package org.cerberus.servlet.testCase;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
import org.cerberus.service.ILogEventService;
import org.cerberus.service.impl.LogEventService;
import org.cerberus.service.impl.UserService;
import org.cerberus.util.StringUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author bcivel
 */
@WebServlet(name = "CreateTestCase", urlPatterns = {"/CreateTestCase"})
public class CreateTestCase extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        DatabaseSpring database = appContext.getBean(DatabaseSpring.class);

        Connection connection = database.connect();
        try {
            /*
             * Testcase Insert
             */
            String project = request.getParameter("createProject");
            if (request.getParameter("createProject") == null) {
                project = "";
            }
            String ticket = request.getParameter("createTicket");
            if (request.getParameter("createTicket") == null) {
                ticket = "";
            }
            String bugID = request.getParameter("createBugID");
            if (request.getParameter("createBugID") == null) {
                bugID = "";
            }
            String origine = request.getParameter("createOrigine");
            if (request.getParameter("createOrigine") == null) {
                origine = "";
            }
            String test = request.getParameter("createTestSelect");
            if (request.getParameter("createTestSelect") == null) {
                test = "";
            }
            String app = request.getParameter("createApplication");
            if (request.getParameter("createApplication") == null) {
                app = "";
            }
            String description = request.getParameter("createDescription").replace("'", "\\'");
            if (request.getParameter("createDescription") == null) {
                description = "";
            }
            String valueExpected = request.getParameter("valueDetail");
            if (request.getParameter("valueDetail") == null) {
                valueExpected = "";
            }
            String howTo = request.getParameter("howtoDetail");
            if (request.getParameter("howtoDetail") == null) {
                howTo = "";
            }
            String testcase = request.getParameter("createTestcase");
            if (request.getParameter("createTestcase") == null) {
                testcase = "";
            }
            String refOrigine = request.getParameter("createRefOrigine");
            if (request.getParameter("createRefOrigine") == null) {
                refOrigine = "";
            }
            String status = request.getParameter("createStatus");
            if (request.getParameter("createStatus") == null) {
                status = "";
            }
            String runQA = request.getParameter("createRunQA");
            if (request.getParameter("createRunQA") == null) {
                runQA = "";
            }
            String runUAT = request.getParameter("createRunUAT");
            if (request.getParameter("createRunUAT") == null) {
                runUAT = "";
            }
            String runPROD = request.getParameter("createRunPROD");
            if (request.getParameter("createRunPROD") == null) {
                runPROD = "";
            }
            String priority = request.getParameter("createPriority");
            if (request.getParameter("createPriority") == null) {
                priority = "";
            }
            String group = request.getParameter("createGroup");
            if (request.getParameter("createGroup") == null) {
                group = "";
            }


            String country[] = request.getParameterValues("createTestcase_country_general");

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM testcase WHERE test = ? AND testcase = ?");
            try {
                stmt.setString(1, test);
                stmt.setString(2, testcase);
                ResultSet rs_control = stmt.executeQuery();
                try {
                    PreparedStatement stmt2 = null;
                    if (rs_control.first()) {
                        out.print("The testcase number already exists. Please, go back to the previous page and choose another testcase number");
                    } else {
                        StringBuilder SQLStmt2 = new StringBuilder("");
                        SQLStmt2.append("INSERT INTO testcase (`Test`,`TestCase`,`Application`,`Ticket`,`Description`,"
                                + " `BehaviorOrValueExpected` ,`activeQA`,`activeUAT`,`activePROD`,`Priority`,`Status`,`TcActive`,`Origine`,"
                                + " `HowTo`,`BugID`, `RefOrigine`, `group`, `Creator` ");
                        if (!(StringUtil.isNullOrEmpty(project))) {
                            SQLStmt2.append(",`Project` ");
                        }
                        SQLStmt2.append(") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
                        if (!(StringUtil.isNullOrEmpty(project))) {
                            SQLStmt2.append(", ?");
                        }
                        SQLStmt2.append(") ");
                        MyLogger.log(CreateTestCase.class.getName(), Level.DEBUG, SQLStmt2.toString());
                        stmt2 = connection.prepareStatement(SQLStmt2.toString());
                        stmt2.setString(1, test);
                        stmt2.setString(2, testcase);
                        stmt2.setString(3, app);
                        stmt2.setString(4, ticket);
                        stmt2.setString(5, description);
                        stmt2.setString(6, valueExpected);
                        stmt2.setString(7, runQA);
                        stmt2.setString(8, runUAT);
                        stmt2.setString(9, runPROD);
                        stmt2.setString(10, priority);
                        stmt2.setString(11, status);
                        stmt2.setString(12, "N");
                        stmt2.setString(13, origine);
                        stmt2.setString(14, howTo);
                        stmt2.setString(15, bugID);
                        stmt2.setString(16, refOrigine);
                        stmt2.setString(17, group);
                        stmt2.setString(18, request.getUserPrincipal().getName());
                        if (!(StringUtil.isNullOrEmpty(project))) {
                            stmt2.setString(19, project);
                        }

                        try {
                            stmt2.executeUpdate();
                        } finally {
                            stmt2.close();
                        }

                        if (request.getParameterValues("createTestcase_country_general") != null) {
                            for (int i = 0; i < country.length; i++) {
                                stmt2 = connection.prepareStatement("INSERT INTO testcasecountry (`Test`, `TestCase`, `Country`) VALUES (?, ?, ?)");
                                try {
                                    stmt2.setString(1, test);
                                    stmt2.setString(2, testcase);
                                    stmt2.setString(3, country[i]);
                                    stmt2.executeUpdate();
                                } finally {
                                    stmt2.close();
                                }
                            }
                        }

                        /**
                         * Adding Log entry.
                         */
                        ILogEventService logEventService = appContext.getBean(LogEventService.class);
                        IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
                        try {
                            logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/CreateTestcase", "CREATE", "Create testcase : ['" + test + "'|'" + testcase + "'|'" + description + "']", "", ""));
                        } catch (CerberusException ex) {
                            Logger.getLogger(UserService.class.getName()).log(Level.ERROR, null, ex);
                        }

                        response.sendRedirect("TestCase.jsp?Test=" + test + "&TestCase="
                                + testcase + "&Load=Load");

                    }
                } finally {
                    rs_control.close();
                }
            } finally {
                stmt.close();
            }
        } catch (SQLException ex) {
            MyLogger.log(CreateTestCase.class.getName(), Level.FATAL, "" + ex);
        } finally {
            out.close();
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CreateTestCase.class.getName(), Level.WARN, e.toString());
            }
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
        processRequest(request, response);
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
        processRequest(request, response);
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
