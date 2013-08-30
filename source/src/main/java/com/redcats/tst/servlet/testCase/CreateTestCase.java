/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.servlet.testCase;

import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryLogEvent;
import com.redcats.tst.factory.impl.FactoryLogEvent;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.refactor.DbMysqlController;
import com.redcats.tst.service.ILogEventService;
import com.redcats.tst.service.impl.LogEventService;
import com.redcats.tst.service.impl.UserService;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
public class CreateTestCase extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        java.sql.Connection conn = null;
        ResultSet rs_control = null;
        PreparedStatement stmt = null;
        try {           /*
             * Database connexion
             */
            DbMysqlController db = new DbMysqlController();
            conn = db.connect();

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
            String valueExpected = request.getParameter("createBehaviorOrValueExpected").replace("'", "\\'");
            if (request.getParameter("createBehaviorOrValueExpected") == null) {
                valueExpected = "";
            }
            String howTo = request.getParameter("createHowTo").replace("'", "\\'");
            if (request.getParameter("createHowTo") == null) {
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

            stmt = conn.prepareStatement("SELECT * from testcase where test = ? and testcase = ?");
            stmt.setString(1, test);
            stmt.setString(2, testcase);
            rs_control = stmt.executeQuery();

            if (rs_control.first()) {
                out.print("The testcase number already exists. Please, go back to the previous page and choose another testcase number");
            } else {
                stmt.close();
                stmt = conn.prepareStatement("INSERT INTO Testcase (`Test`,`TestCase`,`Application`,`Project` ,`Ticket`,`Description`,"
                        + " `BehaviorOrValueExpected` ,`activeQA`,`activeUAT`,`activePROD`,`Priority`,`Status`,`TcActive`,`Origine`,"
                        + " `HowTo`,`BugID`, `RefOrigine`, `group`, `Creator`) "
                        + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                stmt.setString(1, test);
                stmt.setString(2, testcase);
                stmt.setString(3, app);
                stmt.setString(4, project);
                stmt.setString(5, ticket);
                stmt.setString(6, description);
                stmt.setString(7, valueExpected);
                stmt.setString(8, runQA);
                stmt.setString(9, runUAT);
                stmt.setString(10, runPROD);
                stmt.setString(11, priority);
                stmt.setString(12, status);
                stmt.setString(13, "N");
                stmt.setString(14, origine);
                stmt.setString(15, howTo);
                stmt.setString(16, bugID);
                stmt.setString(17, refOrigine);
                stmt.setString(18, group);
                stmt.setString(19, request.getUserPrincipal().getName());

                stmt.executeUpdate();
                stmt.close();


                if (request.getParameterValues("createTestcase_country_general") != null) {
                    for (int i = 0; i < country.length; i++) {
                        stmt = conn.prepareStatement("INSERT INTO Testcasecountry (`Test`, `TestCase`, `Country`) VALUES (?, ?, ?)");
                        stmt.setString(1, test);
                        stmt.setString(2, testcase);
                        stmt.setString(3, country[i]);
                        stmt.executeUpdate();
                        stmt.close();
                    }
                }

                /**
                 * Adding Log entry.
                 */
                ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
                try {
                    logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/CreateTestcase", "CREATE", "Create testcase : ['" + test + "'|'" + testcase  + "'|'" + description + "']", "", ""));
                } catch (CerberusException ex) {
                    Logger.getLogger(UserService.class.getName()).log(Level.ERROR, null, ex);
                }

                response.sendRedirect("TestCase.jsp?Test=" + test + "&TestCase="
                        + testcase + "&Load=Load");

            }
        } catch (SQLException ex) {
            MyLogger.log(CreateTestCase.class.getName(), Level.FATAL, "" + ex);
        } finally {
            out.close();
            try {
                if (rs_control != null) {
                    rs_control.close();
                }
                conn.close();
            } catch (Exception ex) {
                MyLogger.log(CreateTestCase.class.getName(), Level.INFO, "Exception closing Connection or ResultSet: " + ex.toString());
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                MyLogger.log(CreateTestCase.class.getName(), Level.INFO, "Exception closing PreparedStatement: " + ex.toString());
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
