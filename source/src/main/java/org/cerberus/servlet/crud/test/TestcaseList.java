/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.database.DatabaseSpring;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author bcivel
 */
@WebServlet(name = "TestcaseList", urlPatterns = {"/TestcaseList"})
public class TestcaseList extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(TestcaseList.class);

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
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        DatabaseSpring db = appContext.getBean(DatabaseSpring.class);
        PreparedStatement stmt_testlist = null;
        try (Connection conn = db.connect();) {
            String application = request.getParameter("application");
            String app = "";
            String test = request.getParameter("test");
            String tes = "";
            String url = request.getParameter("url");

            if ((StringUtils.isNotBlank(application)) && !(application.equals("all"))) {
                app = " and application = '" + application + "'";
            } else {
                app = "";
            }

            if ((StringUtils.isNotBlank(test)) && !(test.equals("all"))) {
                tes = " and test = '" + test + "'";
            } else {
                tes = "";
            }
            if (StringUtils.isNotBlank(url)) {
                stmt_testlist = conn.prepareStatement("SELECT concat(?) AS list FROM testcase "
                        + " WHERE isActive = 'Y'  AND `Group` = 'AUTOMATED' ? ? ORDER BY test,testcase");
                stmt_testlist.setString(1, url);
                stmt_testlist.setString(2, app);
                stmt_testlist.setString(3, tes);
                try (ResultSet rs_testlist = stmt_testlist.executeQuery();) {
                    int id = 0;
                    if (rs_testlist.first()) {
                        do {
                            out.println(rs_testlist.getString("list"));
                        } while (rs_testlist.next());
                    }
                } catch (SQLException ex) {
                    LOG.warn(ex.toString());
                }
                stmt_testlist.close();
            }
        } catch (Exception e) {
            out.println(e.getMessage());
        } finally {
            out.close();
            try {
                if (stmt_testlist != null) {
                    stmt_testlist.close();
                }
            } catch (SQLException ex) {
                LOG.warn("Exception closing PreparedStatement: " + ex.toString());
            }
        }
    }

    // <editor-fold defaultstate="collapsed"
    // desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
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
