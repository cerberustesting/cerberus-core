/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.refactor;

import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.log.MyLogger;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;

/**
 * @author acraske
 */
public class AddTest extends HttpServlet {

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
        DatabaseSpring db = new DatabaseSpring();
        java.sql.Connection conn = db.connect();
        ResultSet rs_test_exists = null;
        PreparedStatement stmt = null;

        try {
            /*
             * Test Insert
             */
            String test = request.getParameter("test_test");
            String testDescription = request.getParameter("test_description");
            String active = request.getParameter("test_active");
            String automated = request.getParameter("test_automated");

            /*
             * if (automated == null) automated = "";
             */

            /*
             * Test Case Update
             */
            String[] testcase = request.getParameterValues("testcase_testcase");
            String[] testcase_desc = request.getParameterValues("testcase_description");
            String[] testcase_valueexpec = request.getParameterValues("testcase_valueexpec");
            String[] testcase_readonly = request.getParameterValues("testcase_readonly");
            String[] testcase_chain = request.getParameterValues("testcase_chain");
            // request.getParameterValues ( "testcase_countries" ) ;
            String[] testcase_priority = request.getParameterValues("testcase_priority");
            // request.getParameterValues ( "testcase_status" ) ;
            // request.getParameterValues ( "testcase_application" ) ;
            // request.getParameterValues ( "testcase_project" ) ;
            // request.getParameterValues ( "testcase_tcActive" ) ;
            //
            // request.getParameterValues ( "testcase_countries_hidden" ) ;

            /*
             * Put all testcase post info in the arraylist without countries
             * because not used for testcase table
             */
            List< String[]> testcase_info = new ArrayList< String[]>();
            testcase_info.add(testcase);
            testcase_info.add(testcase_desc);
            testcase_info.add(testcase_valueexpec);
            testcase_info.add(testcase_readonly);
            testcase_info.add(testcase_chain);
            testcase_info.add(testcase_priority);

            /*
             * Check that a real modification has been done and the test not
             * already exists
             */
            stmt = conn.prepareStatement("SELECT Test FROM Test where Test = ?");
            stmt.setString(1, test);
            rs_test_exists = stmt.executeQuery();

            if (!rs_test_exists.next()) {

                /*
                 * If ok, Insert Test
                 */
                if (this.formIsFill(test)) {
                    stmt.close();
                    String sql = "INSERT INTO Test (`Test`,`Description`,`Active`,`Automated`) VALUES( ?, ?, ?, ?)";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, test);
                    stmt.setString(2, testDescription);
                    stmt.setString(3, active);
                    stmt.setString(4, automated);
                    stmt.executeUpdate();
                    Date date = new Date();
                      }

            }
            stmt.close();
            response.sendRedirect("Test.jsp?stestbox=" + test);

        } catch (SQLException ex) {
            MyLogger.log(AddTest.class.getName(), Level.FATAL, "" + ex);
        } finally {
            try {
                if (rs_test_exists != null) {
                    rs_test_exists.close();
                }
                conn.close();
            } catch (SQLException ex) {
                MyLogger.log(AddTest.class.getName(), Level.INFO, "Exception closing ResultSet: " + ex.toString());
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                MyLogger.log(AddTest.class.getName(), Level.INFO, "Exception closing PreparedStatement: " + ex.toString());
            }
            out.close();
        }
    }
}
