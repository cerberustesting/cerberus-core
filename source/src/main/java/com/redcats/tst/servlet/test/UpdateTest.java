/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.servlet.test;

import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryLogEvent;
import com.redcats.tst.factory.impl.FactoryLogEvent;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.service.ILogEventService;
import com.redcats.tst.service.impl.LogEventService;
import com.redcats.tst.service.impl.UserService;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * @author acraske
 */
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
            IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
            try {
                logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/UpdateTest", "UPDATE", "Update test : " + test, "", ""));
            } catch (CerberusException ex) {
                Logger.getLogger(UserService.class.getName()).log(Level.ERROR, null, ex);
            }

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
