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
package org.cerberus.servlet.publi;

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

import org.apache.commons.lang3.StringUtils;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.log.MyLogger;
import org.cerberus.service.IParameterService;
import org.cerberus.service.impl.ParameterService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author bcivel
 */
@WebServlet(name = "ResultCI", urlPatterns = {"/ResultCI"})
public class ResultCI extends HttpServlet {

    protected void processRequest(HttpServletRequest request,
                                  HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        DatabaseSpring database = appContext.getBean(DatabaseSpring.class);

        Connection connection = database.connect();
        try {

            String tag = request.getParameter("tag");
            if (StringUtils.isNotBlank(tag)) {

                PreparedStatement prepStmt = connection.prepareStatement("SELECT count(*) AS NBKOP1 "
                        + "FROM testcaseexecution t "
                        + "JOIN "
                        + "(SELECT Test,TestCase, Priority FROM testcase)b "
                        + "ON b.test= t.test AND b.testcase=t.testcase "
                        + "WHERE controlStatus in ('KO', 'FA', 'NA', 'PE') AND priority = '1' "
                        + "AND tag = ?");

                int nbkop1 = 0;
                try {
                    prepStmt.setString(1, tag);
                    ResultSet rs_resultp1 = prepStmt.executeQuery();
                    try {
                        if (rs_resultp1.first()) {
                            nbkop1 = Integer.valueOf(rs_resultp1.getString("NBKOP1"));
                        }
                    } finally {
                        rs_resultp1.close();
                    }
                } finally {
                    prepStmt.close();
                }


                PreparedStatement prepStmt2 = connection.prepareStatement("SELECT count(*) AS NBKOP2 "
                        + "FROM testcaseexecution t "
                        + "JOIN "
                        + "(SELECT Test,TestCase, Priority FROM testcase)b "
                        + "ON b.test= t.test AND b.testcase=t.testcase "
                        + "WHERE controlStatus in ('KO', 'FA', 'NA', 'PE') AND priority = '2' "
                        + "AND tag = ?");
                int nbkop2 = 0;
                try {
                    prepStmt2.setString(1, tag);
                    ResultSet rs_resultp2 = prepStmt2.executeQuery();
                    try {
                        if (rs_resultp2.first()) {
                            nbkop2 = Integer.valueOf(rs_resultp2.getString("NBKOP2"));
                        }
                    } finally {
                        rs_resultp2.close();
                    }
                } finally {
                    prepStmt2.close();
                }

                PreparedStatement prepStmt3 = connection.prepareStatement("SELECT count(*) AS NBKOP3 "
                        + "FROM testcaseexecution t "
                        + "JOIN "
                        + "(SELECT Test,TestCase, Priority FROM testcase)b "
                        + "ON b.test= t.test AND b.testcase=t.testcase "
                        + "WHERE controlStatus in ('KO', 'FA', 'NA', 'PE') AND priority = '3' "
                        + "AND tag = ?");
                int nbkop3 = 0;
                try {
                    prepStmt3.setString(1, tag);
                    ResultSet rs_resultp3 = prepStmt3.executeQuery();
                    try {
                        if (rs_resultp3.first()) {
                            nbkop3 = Integer.valueOf(rs_resultp3.getString("NBKOP3"));
                        }
                    } finally {
                        rs_resultp3.close();
                    }
                } finally {
                    prepStmt3.close();
                }

                PreparedStatement prepStmt4 = connection.prepareStatement("SELECT count(*) AS NBKOP4 "
                        + "FROM testcaseexecution t "
                        + "JOIN "
                        + "(SELECT Test,TestCase, Priority FROM testcase)b "
                        + "ON b.test= t.test AND b.testcase=t.testcase "
                        + "WHERE controlStatus in ('KO', 'FA', 'NA', 'PE') AND priority = '4' "
                        + "AND tag = ?");
                int nbkop4 = 0;
                try {
                    prepStmt4.setString(1, tag);
                    ResultSet rs_resultp4 = prepStmt4.executeQuery();
                    try {
                        if (rs_resultp4.first()) {
                            nbkop4 = Integer.valueOf(rs_resultp4.getString("NBKOP4"));
                        }
                    } finally {
                        rs_resultp4.close();
                    }
                } finally {
                    prepStmt4.close();
                }

                IParameterService parameterService = appContext.getBean(ParameterService.class);

                float pond1 = Float.valueOf(parameterService.findParameterByKey("CI_OK_prio1","").getValue());
                float pond2 = Float.valueOf(parameterService.findParameterByKey("CI_OK_prio2","").getValue());
                float pond3 = Float.valueOf(parameterService.findParameterByKey("CI_OK_prio3","").getValue());
                float pond4 = Float.valueOf(parameterService.findParameterByKey("CI_OK_prio4","").getValue());

                if ((nbkop1 * pond1) + (nbkop2 * pond2) + (nbkop3 * pond3) + (nbkop4 * pond4) < 1) {
                    out.print("OK");
                } else {
                    out.print("KO");
                }

            } else {
                out.print("Error : Tag information is not fed");
            }

        } catch (Exception e) {
            out.println(e.getMessage());
        } finally {
            out.close();
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ResultCI.class.getName(), org.apache.log4j.Level.WARN, e.toString());
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
