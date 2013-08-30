/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.servlet.testCase;

import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryLogEvent;
import com.redcats.tst.factory.impl.FactoryLogEvent;
import com.redcats.tst.refactor.DbMysqlController;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.refactor.DbMysqlController;
import com.redcats.tst.service.ILogEventService;
import com.redcats.tst.service.impl.LogEventService;
import com.redcats.tst.service.impl.UserService;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
public class DeleteTestCase extends HttpServlet {

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
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        DbMysqlController db = new DbMysqlController();
        Connection conn = db.connect();
        PreparedStatement prepStmt = null;

        try {
            /*
             * Test Insert
             */
            String testcasedeleted[] = request.getParameterValues("test_testcase_delete");
            String test = "";
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            
            if (testcasedeleted != null) {

                for (int i = 0; i < testcasedeleted.length; i++) {
                    String testcasesplited[] = testcasedeleted[i].split(" - ");
                    test = testcasesplited[0];

                    String deleteTestCaseExecutionStatement = "DELETE FROM TestCaseExecution where Test = ? and testcase = ? ";
                    prepStmt = conn.prepareStatement(deleteTestCaseExecutionStatement);
                    prepStmt.setString(1, testcasesplited[0]);
                    prepStmt.setString(2, testcasesplited[1]);
                    prepStmt.executeUpdate();
                    prepStmt.close();

                    String deleteTestCasestepactioncontrolStatement = "DELETE FROM TestCasestepactioncontrol where Test = ? and testcase = ? ";
                    prepStmt = conn.prepareStatement(deleteTestCasestepactioncontrolStatement);
                    prepStmt.setString(1, testcasesplited[0]);
                    prepStmt.setString(2, testcasesplited[1]);
                    prepStmt.executeUpdate();
                    prepStmt.close();
                    //stmt.execute("DELETE FROM TestCasestepactioncontrol where Test = '" + testcasesplited[0] + "'" + " and testcase = '" + testcasesplited[1] + "'");

                    String deleteTestCasestepactionStatement = "DELETE FROM TestCasestepaction where Test = ? and testcase = ? ";
                    prepStmt = conn.prepareStatement(deleteTestCasestepactionStatement);
                    prepStmt.setString(1, testcasesplited[0]);
                    prepStmt.setString(2, testcasesplited[1]);
                    prepStmt.executeUpdate();
                    prepStmt.close();
                    //stmt.execute("DELETE FROM TestCasestepaction where Test = '" + testcasesplited[0] + "'" + " and testcase = '" + testcasesplited[1] + "'");

                    String deleteTestCasestepbatchStatement = "DELETE FROM TestCasestepbatch where Test = ? and testcase = ? ";
                    prepStmt = conn.prepareStatement(deleteTestCasestepbatchStatement);
                    prepStmt.setString(1, testcasesplited[0]);
                    prepStmt.setString(2, testcasesplited[1]);
                    prepStmt.executeUpdate();
                    prepStmt.close();
                    //stmt.execute("DELETE FROM TestCasestepbatch where Test = '" + testcasesplited[0] + "'" + " and testcase = '" + testcasesplited[1] + "'");

                    String deleteTestCasecountrypropertiesStatement = "DELETE FROM TestCasecountryproperties where Test = ? and testcase = ? ";
                    prepStmt = conn.prepareStatement(deleteTestCasecountrypropertiesStatement);
                    prepStmt.setString(1, testcasesplited[0]);
                    prepStmt.setString(2, testcasesplited[1]);
                    prepStmt.executeUpdate();
                    prepStmt.close();
                    //stmt.execute("DELETE FROM TestCasecountryproperties where Test = '" + testcasesplited[0] + "'" + " and testcase = '" + testcasesplited[1] + "'");

                    String deleteTestCasecountryStatement = "DELETE FROM TestCasecountry where Test = ? and testcase = ? ";
                    prepStmt = conn.prepareStatement(deleteTestCasecountryStatement);
                    prepStmt.setString(1, testcasesplited[0]);
                    prepStmt.setString(2, testcasesplited[1]);
                    prepStmt.executeUpdate();
                    prepStmt.close();
                    //stmt.execute("DELETE FROM TestCasecountry where Test = '" + testcasesplited[0] + "'" + " and testcase = '" + testcasesplited[1] + "'");

                    String deleteTestCaseStatement = "DELETE FROM TestCase where Test = ? and testcase = ? ";
                    prepStmt = conn.prepareStatement(deleteTestCaseStatement);
                    prepStmt.setString(1, testcasesplited[0]);
                    prepStmt.setString(2, testcasesplited[1]);
                    prepStmt.executeUpdate();
                    prepStmt.close();
                    //stmt.execute("DELETE FROM TestCase where Test = '" + testcasesplited[0] + "'" + " and testcase = '" + testcasesplited[1] + "'");

                    /**
                     * Adding Log entry.
                     */
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
                    try {
                        logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/DeleteTestCase", "DELETE", "Delete testcase : ['" + testcasesplited[0] + "'|'" + testcasesplited[1] + "']", "", ""));
                    } catch (CerberusException ex) {
                        Logger.getLogger(UserService.class.getName()).log(Level.ERROR, null, ex);
                    }


                }
            }

            response.sendRedirect("Test.jsp?stestbox=" + test);



        } catch (SQLException ex) {
            MyLogger.log(DeleteTestCase.class.getName(), Level.FATAL, ex.toString());
        } finally {
            out.close();
            try {
                conn.close();
            } catch (Exception ex) {
                MyLogger.log(DeleteTestCase.class.getName(), Level.INFO, "Exception closing Connection: " + ex.toString());
            }
            try {
                if (prepStmt != null) {
                    prepStmt.close();
                }
            } catch (SQLException ex) {
                MyLogger.log(DeleteTestCase.class.getName(), Level.INFO, "Exception closing PreparedStatement: " + ex.toString());
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
