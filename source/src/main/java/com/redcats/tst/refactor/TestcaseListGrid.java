/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.refactor;


import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.log.MyLogger;
import org.apache.log4j.Level;
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
import java.sql.ResultSet;

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

            String whereclauses = "";

            String application = "";
            if (request.getParameter("application") != null && !(request.getParameter("application").equals("all"))) {
                application = request.getParameter("application");
                whereclauses = whereclauses + " and a.application = '" + application + "'";
            }

            String test = "";
            if (request.getParameter("test") != null && !(request.getParameter("test").equals("all"))) {
                test = request.getParameter("test");
                whereclauses = whereclauses + " and a.test = '" + test + "'";
            }

            String testcase = "";
            if (request.getParameter("testcase") != null
                    && !(request.getParameter("testcase").equals("all"))) {
                testcase = request.getParameter("testcase");
                whereclauses = whereclauses + " and a.testcase = '" + testcase + "'";
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
                whereclauses = strb.toString();
            }

            int number = 1;
            if (request.getParameter("number") != null) {
                number = Integer.valueOf(request.getParameter("number"));
            }

            int totalnumber = 1;
            if (request.getParameter("totalnumber") != null) {
                totalnumber = Integer.valueOf(request.getParameter("totalnumber"));
            }

            PreparedStatement stmt_count = conn.prepareStatement("SELECT count(*) FROM testcase a JOIN testcasecountry b ON a.test=b.test "
                    + " AND a.testcase=b.testcase WHERE a.TcActive = 'Y'  AND a.`Group` = 'Interactive' ? ORDER BY a.test,a.testcase");
            int count;
            try {
                stmt_count.setString(1, whereclauses);
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

                PreparedStatement stmt_testlist = conn.prepareStatement("SELECT replace(concat( "
                        + "?"
                        + "), \"%COUNTRY%\", country) AS list "
                        + " FROM testcase a JOIN testcasecountry b ON a.test=b.test "
                        + " AND a.testcase=b.testcase "
                        + " WHERE a.TcActive = 'Y'  AND a.`Group` = 'Interactive' "
                        + " ? "
                        + " ORDER BY a.test,a.testcase "
                        + " LIMIT ?, ?");
                try {
                    stmt_testlist.setString(1, url);
                    stmt_testlist.setString(2, whereclauses);
                    stmt_testlist.setInt(3, number);
                    stmt_testlist.setInt(4, count);

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
