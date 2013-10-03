/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.refactor;


import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.servlet.testCase.CreateTestCase;
import org.apache.log4j.Level;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bcivel
 */
public class ImportSeleniumIDE extends HttpServlet {

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
        Statement stmt = null;
        ResultSet rs_control = null;
        ResultSet rs_countries = null;
        ResultSet rs_sequence = null;
        ResultSet rs_control_number = null;
        PreparedStatement prepStmt = null;

        try {
            // CONNECTION TO THE DATABASE


            stmt = conn.createStatement();

            String test = request.getParameter("importTest");
            String testcase = request.getParameter("importTestcase");
            String step = request.getParameter("importStep");

            String[] action;
//            if (request.getParameterValues("importAction") != null){
            action = request.getParameterValues("importAction");
//           }
            String[] object;
            //           if (request.getParameterValues("importObject") != null){
            object = request.getParameterValues("importObject");
            //           }
            String[] propertyValue;
            //          if (request.getParameterValues("importProperty") != null){
            propertyValue = request.getParameterValues("importProperty");
            //           }
            String[] propertyName;
            //           if (request.getParameterValues("importPropertyName") != null){
            propertyName = request.getParameterValues("importPropertyName");
            //          }
            List<String> countryList = new ArrayList<String>();
            List<String> controlList = new ArrayList<String>();


            List<String[]> actionToImport = new ArrayList<String[]>();
            actionToImport.add(action);
            actionToImport.add(object);
            actionToImport.add(propertyValue);
            actionToImport.add(propertyName);

            // SELECT THE LIST OF CONTROL

            String selectControl = "SELECT value FROM Invariant WHERE id ='13'";
            rs_control = stmt.executeQuery(selectControl);

            if (rs_control.first()) {
                do {
                    controlList.add(rs_control.getString("Value"));
                } while (rs_control.next());
            }


            // SELECT THE NUMBER OF COUNTRY EXISTING FOR THE TESTCASE

            String selectCountries = "SELECT Country FROM Testcasecountry WHERE test =? AND testcase = ?";
            prepStmt = conn.prepareStatement(selectCountries);
            prepStmt.setString(1, test);
            prepStmt.setString(2, testcase);

            rs_countries = prepStmt.executeQuery();

            //  PUT ALL THE COUNTRY INTO A LIST
            if (rs_countries.first()) {
                do {
                    countryList.add(rs_countries.getString("Country"));
                } while (rs_countries.next());

            } else {
                out.print("Error : There is no country specified");
            }
            prepStmt.close();

            // FOR ALL THE PROPERTY AND ALL THE COUNTRY, INSERT A TEXT PROPERTY
            int i = 0;

            // FOR ALL THE PROPERTY NOT NULL
            for (int j = 0; j < action.length; j++) {

                if (propertyName[j].equals("")) {
                    out.print("Error : Feed the Property Name");
                }


                //FOR ALL THE COUNTRIES
                for (int k = 0; k < countryList.size(); k++) {

                    // INSERT A PROPERTY
                    if (!propertyName[j].equals("null")) //or  if (StringUtils.isNotBlank(property[i]))
                    {
                        TestCaseCountryProperties properties = appContext.getBean(TestCaseCountryProperties.class);
                        properties.setTest(test);
                        properties.setTestcase(testcase);
                        properties.setCountry(countryList.get(k));
                        properties.setProperty(propertyName[j]);
                        properties.setNature("STATIC");
                        properties.setRowlimit(0);
                        properties.setLength(0);
                        properties.setValue(propertyValue[j]);
                        properties.setType("TEXT");
                        properties.setDatabase("");
                        properties.insert();
                    }
                }
            }

            // FOR ALL THE ACTION, INSERT AN ACTION
            int seq = 0;

            String selectSequence = "SELECT Sequence FROM Testcasestepaction WHERE test =? AND testcase = ? AND step = ?";

            prepStmt = conn.prepareStatement(selectSequence);
            prepStmt.setString(1, test);
            prepStmt.setString(2, testcase);
            prepStmt.setString(3, step);
            rs_sequence = prepStmt.executeQuery();

            //  Define the sequence number
            if (rs_sequence.first()) {
                rs_sequence.last();
                seq = rs_sequence.getInt("Sequence");
            } else {
                seq = 0;
            }
            prepStmt.close();

            int control = 0;
            String selectControlNumber = "SELECT Control FROM Testcasestepactioncontrol WHERE test =? AND testcase = ? AND step = ?";
            prepStmt = conn.prepareStatement(selectControlNumber);
            prepStmt.setString(1, test);
            prepStmt.setString(2, testcase);
            prepStmt.setString(3, step);

            rs_control_number = prepStmt.executeQuery();

            //  Define the control number
            if (rs_control_number.first()) {
                rs_control_number.last();
                control = rs_control_number.getInt("Control");
            } else {
                control = 0;
            }
            prepStmt.close();

            i = 0;

            for (int j = 0; j < action.length; j++) {
                if (!controlList.contains(action[j])) {

                    seq = seq + 10;

                    String sql = "INSERT INTO testcasestepaction (`Test`, `TestCase`, `Step`, `Sequence` , `Action`, `Object`, `Property`)"
                            + " VALUES ( ?, ?, ?, ?, ?, ?, ? )";

                    prepStmt = conn.prepareStatement(sql);
                    prepStmt.setString(1, test);
                    prepStmt.setString(2, testcase);
                    prepStmt.setString(3, step);
                    prepStmt.setInt(4, seq);
                    prepStmt.setString(5, action[j].replace("open", "openUrlWithBase"));
                    prepStmt.setString(6, object[j].replace("'", "\\'"));
                    prepStmt.setString(7, propertyName[j]);
                    prepStmt.executeUpdate();
                    prepStmt.close();


                } else {
                    control = control + 1;
                    String ctrl = "INSERT INTO testcasestepactioncontrol (`Test`, `TestCase`, `Step`, `Sequence` ,`Control` , `Type`, `ControlValue`, `ControlProperty`, `FATAL`)"
                            + " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, 'Y' )";

                    prepStmt = conn.prepareStatement(ctrl);
                    prepStmt.setString(1, test);
                    prepStmt.setString(2, testcase);
                    prepStmt.setString(3, step);
                    prepStmt.setInt(4, seq);
                    prepStmt.setInt(5, control);
                    prepStmt.setString(6, action[j].replace("open", "openUrlWithBase"));
                    prepStmt.setString(7, propertyName[j]);
                    prepStmt.setString(8, object[j].replace("'", "\\'"));
                    prepStmt.executeUpdate();
                    prepStmt.close();
                }
            }

            /*
             * Redirect
             */
//			response.sendRedirect("TestCase.jsp?Load=Load&Test="
//					+ test + "&TestCase="
//					+ testcase);
            response.sendRedirect("ImportHTML.jsp?submited=true");

        } catch (SQLException ex) {
            MyLogger.log(ImportSeleniumIDE.class.getName(), Level.FATAL,
                    "" + ex);
            // out.println ( UpdateTestCase.class.getName ( ) + ex ) ;
        } catch (NullPointerException ex) {
            MyLogger.log(ImportSeleniumIDE.class.getName(), Level.FATAL,
                    "" + ex);
        } catch (ArrayIndexOutOfBoundsException ex) {
            MyLogger.log(ImportSeleniumIDE.class.getName(), Level.FATAL,
                    "" + ex);
        } catch (Exception ex) {
            ex.printStackTrace();
            out.println("Erro!");
        } finally {
            //out.close();
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (rs_control != null) {
                    rs_control.close();
                }
                if (rs_countries != null) {
                    rs_countries.close();
                }
                if (rs_sequence != null) {
                    rs_sequence.close();
                }
                if (rs_control_number != null) {
                    rs_control_number.close();
                }

                conn.close();
            } catch (Exception ex) {
                MyLogger.log(CreateTestCase.class.getName(), Level.INFO, "Exception closing Statement or ResultSet or Connection: " + ex.toString());
            }
            try {
                if (prepStmt != null) {
                    prepStmt.close();
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
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
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
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
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
