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
package org.cerberus.refactor;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.log.MyLogger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author bcivel
 */
@WebServlet(name = "TestcaseListGrid", urlPatterns = {"/TestcaseListGrid"})
public class TestcaseListGrid extends HttpServlet {

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
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        DatabaseSpring db = appContext.getBean(DatabaseSpring.class);
        Connection conn = db.connect();
        try {

            StringBuilder whereclauses = new StringBuilder();

            String application = "";
            if (request.getParameter("application") != null && !(request.getParameter("application").equals("all"))) {
                application = request.getParameter("application");
                whereclauses.append(" and a.application = '");
                whereclauses.append(application);
                whereclauses.append("'");
            }

            String test = "";
            if (request.getParameter("test") != null && !(request.getParameter("test").equals("all"))) {
                test = request.getParameter("test");
                whereclauses.append(" and a.test = '");
                whereclauses.append(test);
                whereclauses.append("'");
            }

            String testcase = "";
            if (request.getParameter("testcase") != null
                    && !(request.getParameter("testcase").equals("all"))) {
                testcase = request.getParameter("testcase");
                whereclauses.append(" and a.testcase = '");
                whereclauses.append(testcase);
                whereclauses.append("'");
            }

            int repeat = 0;
            if (request.getParameter("repeat") != null
                    && !(request.getParameter("repeat").equals("0"))) {
                repeat = Integer.valueOf(request.getParameter("repeat"));
            }


            if (request.getParameter("country") != null) {
                String[] countries = request.getParameterValues("country");
                StringBuilder strb = new StringBuilder(whereclauses);
                for (int x = 0; x < countries.length; x++) {
                    strb.append(" and b.country = '");
                    strb.append(countries[x]);
                    strb.append("'");
                }
                whereclauses = new StringBuilder();
                whereclauses.append(strb.toString());
            }
            
            if (request.getParameter("status") != null) {
                String[] status = request.getParameterValues("status");
                StringBuilder strb = new StringBuilder(whereclauses);
                for (int x = 0; x < status.length; x++) {
                    strb.append(" and b.status = '");
                    strb.append(status[x]);
                    strb.append("'");
                }
                whereclauses = new StringBuilder();
                whereclauses.append(strb.toString());
            }

            if (request.getParameter("project") != null) {
                String[] projects = request.getParameterValues("project");
                StringBuilder strb = new StringBuilder(whereclauses);
                strb.append(" AND ( ");
                for (int x = 0; x < projects.length; x++) {
                    if(x>0) {
                        strb.append(" OR ");
                    }

                    strb.append(" a.project = '");
                    strb.append(projects[x]);
                    strb.append("'");
                }
                strb.append(" ) ");
                whereclauses = new StringBuilder();
                whereclauses.append(strb.toString());
            }

            int number = 1;
            if (request.getParameter("number") != null) {
                number = Integer.valueOf(request.getParameter("number"));
            }

            int totalnumber = 1;
            if (request.getParameter("totalnumber") != null) {
                totalnumber = Integer.valueOf(request.getParameter("totalnumber"));
            }

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT count(*) FROM testcase a JOIN testcasecountry b ON a.test=b.test ");
            sb.append(" AND a.testcase=b.testcase WHERE a.TcActive = 'Y'  AND a.`Group` = 'AUTOMATED'");
            sb.append(whereclauses);
            sb.append(" ORDER BY a.test,a.testcase");
            PreparedStatement stmt_count = conn.prepareStatement(sb.toString());
            int count;
            try {
//                stmt_count.setString(1, whereclauses);
                ResultSet rs_count = stmt_count.executeQuery();

                try {
                    count = 1;
                    if (rs_count.first()) {
                        count = rs_count.getInt(1) / totalnumber;
                    }
                    number = (number - 1) * count;
                } finally {
                    rs_count.close();
                }
            } finally {
                stmt_count.close();
            }

            String url = "";
            if (request.getParameter("url") != null) {
                url = request.getParameter("url");

                StringBuilder sb2 = new StringBuilder();
            sb2.append("SELECT replace(concat( ");
            sb2.append(url);
            sb2.append("), \"%COUNTRY%\", country) AS list  FROM testcase a JOIN testcasecountry b ON a.test=b.test");
            sb2.append(" AND a.testcase=b.testcase WHERE a.TcActive = 'Y'  AND a.`Group` = 'AUTOMATED' ");
            sb2.append(whereclauses);
            sb2.append(" ORDER BY a.test,a.testcase LIMIT ");
            sb2.append(number);
            sb2.append(",");
            sb2.append(count);
            MyLogger.log(TestcaseListGrid.class.getName(), Level.INFO, "SQL query: " + sb2.toString());
                PreparedStatement stmt_testlist = conn.prepareStatement(sb2.toString());
                try {
//                    stmt_testlist.setString(1, url);
//                    stmt_testlist.setString(2, whereclauses);
//                    stmt_testlist.setInt(3, number);
//                    stmt_testlist.setInt(4, count);

                    ResultSet rs_testlist = stmt_testlist.executeQuery();
                    try {
                        int id = 0;

                        if (rs_testlist.first()) {
                            for (int a = 0; a < repeat; a++) {
                                rs_testlist.first();
                                do {
                                    out.println(rs_testlist.getString("list"));
                                } while (rs_testlist.next());
                            }
                        }
                    } finally {
                        rs_testlist.close();
                    }
                } finally {
                    stmt_testlist.close();
                }
            }

        } catch (Exception e) {
            out.println(e.getMessage());
        } finally {
            out.close();
            try {
                conn.close();
            } catch (Exception ex) {
                MyLogger.log(TestcaseListGrid.class.getName(), Level.INFO, "Exception closing Connection: " + ex.toString());
            }
        }

    }

    // <editor-fold defaultstate="collapsed"
    // desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

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
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
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
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
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
