/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.servlet.test;

import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryLogEvent;
import com.redcats.tst.factory.impl.FactoryLogEvent;
import com.redcats.tst.refactor.DbMysqlController;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.refactor.AddTest;
import com.redcats.tst.refactor.DbMysqlController;
import com.redcats.tst.service.ILogEventService;
import com.redcats.tst.service.impl.LogEventService;
import com.redcats.tst.service.impl.UserService;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author acraske
 */
public class CreateTest extends HttpServlet {

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
    @ Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        this.processRequest(request, response);
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
    @ Override
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
    public boolean formIsFullFill(List< String[]> testcase_info, int index) {

        for (String[] t : testcase_info) {
            if (t[ index].isEmpty() || t[ index].trim().equals("") || t[ index].equals(" ")) {
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
    @ Override
    public String getServletInfo() {

        return "Short description";
    }// </editor-fold>

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
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        DbMysqlController db = new DbMysqlController();
        Connection conn = db.connect();
        PreparedStatement stmt = null;
        ResultSet rs_test_exists = null;

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
                stmt = conn.prepareStatement("SELECT Test FROM Test where Test = ?");
                stmt.setString(1, test);
                rs_test_exists = stmt.executeQuery();

                if (!rs_test_exists.next()) {
                    stmt.close();

                    String sql = "INSERT INTO Test (`Test`,`Description`,`Active`,`Automated`) VALUES(?, ?, ?, ?)";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, test);
                    stmt.setString(2, description);
                    stmt.setString(3, active);
                    stmt.setString(4, automated);
                    stmt.executeUpdate();
                    stmt.close();
                    Date date = new Date();

                    /**
                     * Adding Log entry.
                     */
                    ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
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
            } else {
                out.print("Could not record an empty test");
            }

            response.sendRedirect("Test.jsp?stestbox=" + test);

        } catch (SQLException ex) {
            MyLogger.log(AddTest.class.getName(), Level.FATAL, "" + ex);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (rs_test_exists != null) {
                    rs_test_exists.close();
                }
                conn.close();
            } catch (SQLException ex) {
                MyLogger.log(CreateTest.class.getName(), Level.INFO, "Exception closing Statement or ResultSet: " + ex.toString());
            }
            out.close();
        }
    }
}
