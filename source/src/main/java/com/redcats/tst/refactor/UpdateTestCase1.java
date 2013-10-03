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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author acraske
 */
public class UpdateTestCase1 extends HttpServlet {

    /*
     * Change ' ' text delimiter to \' \', because need for the insertion un
     * MySQL Database
     */
    public String changeStringDelimiter(String s) {

        return s.replace("\'", "\\'");
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
        // response.setContentType("text/html;charset=UTF-8");
        this.processRequest(request, response);
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
        // response.setContentType("text/html;charset=UTF-8");
        this.processRequest(request, response);
    }

    public boolean formIsFill(String s) {

        if (s.isEmpty() || (s.trim().compareTo("") == 0)
                || (s.compareTo(" ") == 0)
                || (s.compareTo("Mandatory, KEY") == 0)) {
            return false;
        }
        return true;
    }

    /*
     * Return true if all fields contains in the testcase_info are not null at
     * the specified index
     */
    public boolean formIsFullFill(List<String[]> testcase_info, int index) {

        if (testcase_info.isEmpty()) {
            return false;
        }

        for (String[] t : testcase_info) {
            if (t[index].isEmpty() || t[index].trim().equals("")
                    || t[index].equals(" ") || (t[index] == null)) {
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

    public String getStringParameter(String parameter,
                                     HttpServletRequest request) {

        if (request.getParameter(parameter) != null) {
            return request.getParameter(parameter);
        }
        return "";
    }

    /*
     * Return the String table if the request contain the parameter (in case
     * table is not filled)
     */
    public String[] getStringTable(String parameter, HttpServletRequest request) {

        if (request.getParameterValues(parameter) != null) {
            return request.getParameterValues(parameter);

        }
        return new String[0];
    }

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
    protected void processRequest(HttpServletRequest request,
                                  HttpServletResponse response) throws ServletException, IOException {

        PrintWriter out = response.getWriter();
        /*
         * Database connexion
         */
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        DatabaseSpring database = appContext.getBean(DatabaseSpring.class);

        Connection connection = database.connect();
        try {
            Statement stmt = connection.createStatement();
            try {
            /*
             * Get Test, TestCase and Country to update
             */
                String test_testcase_no_format = this.getStringParameter(
                        "testcase_hidden", request);
                this.getStringParameter("Test", request);
                this.getStringParameter("TestCase", request);

            /*
             * Get TestCase Step Informations
             */
            /*
             * Step Number & Description
             */
                String step_delete[] = request.getParameterValues("testcasestep_delete");
                String step_id[] = this.getStringTable("testcasestep_hidden",
                        request);
                String step_desc[] = this.getStringTable("step_description",
                        request);
                String step_number_toadd[] = this.getStringTable("step_number_add",
                        request);
                String step_desc_toadd[] = this.getStringTable(
                        "step_description_add", request);

            /*
             * Step actions
             */
                String step_action_delete[] = request.getParameterValues("actions_delete");
                String step_number_hide[] = this.getStringTable(
                        "stepnumber_hidden", request);
                String step_sequence[] = this.getStringTable("actions_sequence",
                        request);
                String step_action[] = this.getStringTable("actions_action",
                        request);
                String step_object[] = this.getStringTable("actions_object",
                        request);
                String step_property[] = this.getStringTable("actions_property",
                        request);

            /*
             * Get TestCase Step Controls Informations
             */
                String controls_delete[] = request.getParameterValues("controls_delete");
                String controls_step[] = this.getStringTable("controls_step",
                        request);
                String controls_sequence[] = this.getStringTable(
                        "controls_sequence", request);
                String controls_control[] = this.getStringTable("controls_control",
                        request);
                String controls_type[] = this.getStringTable("controls_type",
                        request);
                String controls_controlvalue[] = this.getStringTable(
                        "controls_controlvalue", request);
                String controls_controlproperty[] = this.getStringTable(
                        "controls_controlproperty", request);
                String controls_fatal[] = this.getStringTable("controls_fatal",
                        request);

            /*
             * Properties Insert/Update
             */

                String[] test_testcase_format = test_testcase_no_format.split(" - ");

            /*
             * Put all testcase post info in the arraylist without countries
             * because not used for testcase table
             */
            /*
             * Properties
             */
             /*
             * Actions
             */
                List<String[]> testcase_actions_info = new ArrayList<String[]>();
                testcase_actions_info.add(step_number_hide);
                testcase_actions_info.add(step_sequence);
                testcase_actions_info.add(step_action);
                testcase_actions_info.add(step_object);
                testcase_actions_info.add(step_property);

            /*
             * Controls
             */
                List<String[]> testcase_controls_info = new ArrayList<String[]>();
                testcase_controls_info.add(controls_control);
                testcase_controls_info.add(controls_controlproperty);
                testcase_controls_info.add(controls_controlvalue);
                testcase_controls_info.add(controls_sequence);
                testcase_controls_info.add(controls_step);
                testcase_controls_info.add(controls_type);
                testcase_controls_info.add(controls_fatal);

            /*
             * Get Number of actual testcasescountryproperties for this test
             */
            
            /*
             * properties.update(); } else { /* Insert new rows
             */
            
            /*
             * properties.insert();
             *
             * /
              
             /* Test Case Step Description
             */
            /*
             * Update Test Case Step
             */
                int numberOfSteps = 0;
                Integer steps = 0;

                ResultSet rs_step = stmt.executeQuery("SELECT COUNT(*) "
                        + " FROM TestCaseStep " + " WHERE Test = '"
                        + test_testcase_format[0] + "'" + " AND TestCase = '"
                        + test_testcase_format[1] + "'");

                try {
                    if (rs_step.next()) {
                        numberOfSteps = rs_step.getInt(1);
                    }


                    if (request.getParameter("step_number_add") != null) {
                        steps = Integer.parseInt(request.getParameter("step_number_add"));
                    }
                    if (steps == null || steps == 0) {
                        steps = step_id.length;
                    }
                } finally {
                    rs_step.close();
                }

                for (int i = 0; i < steps; i++) {
                    int i2 = i + 1;
                    String step_batch[] = request.getParameterValues("batch-" + i2);

                    if (i < numberOfSteps) {
                        stmt.executeUpdate("UPDATE TestCaseStep "
                                + " SET Description = '" + step_desc[i] + "' "
                                + " WHERE Test = '" + test_testcase_format[0] + "'"
                                + " AND TestCase = '" + test_testcase_format[1]
                                + "' " + " AND Step = " + step_id[i] + "");

                        stmt.executeUpdate("DELETE FROM testcasestepbatch WHERE  "
                                + " test = '" + test_testcase_format[0] + "' "
                                + " AND TestCase = '" + test_testcase_format[1]
                                + "' AND STEP = '" + step_id[i] + "' ");

                        if (step_batch != null) {
                            for (int j = 0; j < step_batch.length; j++) {
                                try {
                                    stmt.executeUpdate("INSERT INTO testcasestepbatch (`Test`, `TestCase`, `Step`, `Batch`) VALUES ( "
                                            + "'"
                                            + test_testcase_format[0]
                                            + "', "
                                            + "'"
                                            + test_testcase_format[1]
                                            + "', "
                                            + "'"
                                            + step_id[i]
                                            + "', "
                                            + "'"
                                            + step_batch[j] + "' )");

                                } catch (Exception e) {
                                    MyLogger.log(UpdateTestCase1.class.getName(), Level.FATAL, "Error on insert into TestCaseStepBatch: " + e.toString());
                                }
                            }
                        }


                    }
                }

            /*
             * Insert Test Case Step
             */
                for (int i = 0; i < step_number_toadd.length; i++) {
                    if (this.formIsFill(step_number_toadd[i])
                            && this.formIsFill(step_desc_toadd[i])) {
                        String sql = ("INSERT INTO TestCaseStep (`Test`,`Testcase`,`Step`,`Description`) "
                                + " VALUES('"
                                + test_testcase_format[0]
                                + "', "
                                + "'"
                                + test_testcase_format[1]
                                + "', "
                                + step_number_toadd[i]
                                + ", "
                                + "'"
                                + step_desc_toadd[i] + "')");
                        // System.out.println ( sql ) ;
                        stmt.execute(sql);
                    }

                    stmt.executeUpdate("DELETE FROM testcasestepbatch WHERE  "
                            + " test = '" + test_testcase_format[0] + "' "
                            + " AND TestCase = '" + test_testcase_format[1]
                            + "' AND STEP = '"
                            + request.getParameter("step_number_add") + "' ");

                    String step_batch[] = request.getParameterValues("batch-"
                            + step_number_toadd[i]);

                    if (step_batch != null) {
                        for (int j = 0; j < step_batch.length; j++) {
                            try {

                                String sql = "INSERT INTO testcasestepbatch (`Test`, `TestCase`, `Step`, `Batch`) VALUES ( "
                                        + "'"
                                        + test_testcase_format[0]
                                        + "', "
                                        + "'"
                                        + test_testcase_format[1]
                                        + "', "
                                        + "'"
                                        + step_number_toadd[i]
                                        + "', "
                                        + "'"
                                        + step_batch[j] + "' )";
                                stmt.executeUpdate(sql);

                            } catch (Exception e) {
                                MyLogger.log(UpdateTestCase1.class.getName(), Level.FATAL, "Error on insert into TestCaseStepBatch: " + e.toString());
                            }

                        }
                    }
                }

            /*
             * Test Case Step Actions
             */
                for (int i = 0; i < step_number_hide.length; i++) {

                /*
                 * Select to know if need to update or insert
                 */
                    if (this.formIsFill(step_sequence[i])
                            && (step_sequence[i].length() > 0)) {
                        String sql = ("SELECT * " + " FROM TestCaseStepAction"
                                + " WHERE Test = '" + test_testcase_format[0] + "'"
                                + " AND TestCase = '" + test_testcase_format[1]
                                + "' " + " AND Step = " + step_number_hide[i] + " "
                                + " AND Sequence = " + step_sequence[i]);
                        // System.out.println ( "Step Key : " + sql ) ;
                        ResultSet rs_stepaction = stmt.executeQuery(sql);
                        try {
                            TestCaseStepAction action = appContext.getBean(TestCaseStepAction.class);
                            action.setTest(test_testcase_format[0]);
                            action.setTestcase(test_testcase_format[1]);
                            action.setStep(Integer.parseInt(step_number_hide[i]));
                            action.setSequence(Integer.parseInt(step_sequence[i]));
                            action.setAction(step_action[i]);
                            action.setObject(step_object[i]);

                            action.setProperty(step_property[i]);

                            if (rs_stepaction.next()) { /*
                         * Update
                         */

                                action.update();


                            } else { /*
                         * Insert
                         */
                                if (this.formIsFullFill(testcase_actions_info, i)) {
                                    action.insert();
                                }
                            }
                        } finally {
                            rs_stepaction.close();
                        }
                    }
                }

            /*
             * TestCase Step Controls
             */
            /*
             * Get Number of actual testcase controls
             */
                ResultSet rs_numberOfTestCasesControls = stmt.executeQuery("SELECT COUNT(*)"
                        + " FROM TestCaseStepActionControl "
                        + " WHERE Test = '" + test_testcase_format[0] + "'"
                        + " AND TestCase = '" + test_testcase_format[1]
                        + "'");
                int numberOfTestCasesControls = 0;
                try {

                    if (rs_numberOfTestCasesControls.next()) {
                        numberOfTestCasesControls = rs_numberOfTestCasesControls.getInt(1);
                    }
                } finally {
                    rs_numberOfTestCasesControls.close();
                }

                for (int i = 0; i < controls_control.length; i++) {
                    if (this.formIsFill(controls_step[i].toString())
                            && this.formIsFill(controls_sequence[i].toString())
                            && this.formIsFill(controls_control[i].toString())) {
                        TestCaseStepActionControl control = appContext.getBean(TestCaseStepActionControl.class);
                        control.setTest(test_testcase_format[0]);
                        control.setTestcase(test_testcase_format[1]);
                        control.setStep(Integer.parseInt(controls_step[i]));
                        control.setSequence(Integer.parseInt(controls_sequence[i]));
                        control.setControl(Integer.parseInt(controls_control[i]));
                        control.setType(controls_type[i]);
                        control.setControlValue((controls_controlvalue[i]));
                        if ((controls_fatal[i]).compareTo("Y") == 0) {
                            control.setFatal(true);
                        } else {
                            control.setFatal(false);
                        }

                        control.setControlProperty(controls_controlproperty[i]);

                        if (i < numberOfTestCasesControls) {

                            control.update();


                        } else {
                            if (this.formIsFullFill(testcase_controls_info, i)) {

                                control.insert();


                            }
                        }
                    }
                }

            /*
             * DELETE
             */

            /*
             * Delete Properties
             */
            /*
             * if (testcase_properties_delete != null) { // If some properties
             * // check for delete for (String element :
             * testcase_properties_delete) { // Delete // them with // key
             *
             * String property_and_country[] = element.split("-");
             *
             * stmt.execute("DELETE FROM TestCaseCountryProperties " + " WHERE
             * Test = '" + test_testcase_format[0] + "' " + " AND TestCase = '"
             * + test_testcase_format[1] + "' " + " AND Country = '" +
             * property_and_country[1] + "' " + " AND Property = '" +
             * property_and_country[0] + "'");
             *
             * } }
             *
             * /* Delete Steps
             */
                if (step_delete != null) {
                    for (String step_to_delete : step_delete) {
                        stmt.execute("DELETE FROM TestCaseStep "
                                + " WHERE Test = '" + test_testcase_format[0]
                                + "' " + " AND TestCase = '"
                                + test_testcase_format[1] + "' " + " AND Step = "
                                + step_to_delete + "");
                    }
                }

            /*
             * Delete Actions
             */
                if (step_action_delete != null) { // If some actions to delete
                    for (String action_to_delete : step_action_delete) { // Delete
                        // with
                        // key

                        String[] step_action_split = action_to_delete.split("-");

                        stmt.execute("DELETE FROM TestCaseStepAction "
                                + " WHERE Test = '" + test_testcase_format[0]
                                + "' " + " AND TestCase = '"
                                + test_testcase_format[1] + "' " + " AND Step = "
                                + step_action_split[0] + " " + " AND Sequence = "
                                + step_action_split[1] + "");
                    }
                }

            /*
             * Delete Controls
             */
                if (controls_delete != null) { // If controls to delete
                    for (String keys_to_delete : controls_delete) {

                        String[] key_delete = keys_to_delete.split("-"); // Get
                        // Step,
                        // Sequence
                        // and
                        // Control

                        stmt.execute("DELETE FROM TestCaseStepActionControl "
                                + " WHERE Test = '" + test_testcase_format[0]
                                + "' " + " AND TestCase = '"
                                + test_testcase_format[1] + "' " + " AND Step = "
                                + key_delete[0] + " " + " AND Sequence = "
                                + key_delete[1] + " " + " AND Control = "
                                + key_delete[2] + "");

                    }
                }

                stmt.execute("UPDATE TestCase "
                        + "SET LastModifier = '" + request.getUserPrincipal().getName() + "' "
                        + "WHERE Test = '" + test_testcase_format[0] + "' "
                        + " AND TestCase = '" + test_testcase_format[1] + "' ");

            /*
             * Redirect
             */
                response.sendRedirect("TestCase.jsp?Load=Load&Test="
                        + test_testcase_format[0] + "&TestCase="
                        + test_testcase_format[1]);
                return;
            } finally {
                stmt.close();
            }
        } catch (SQLException ex) {
            MyLogger.log(UpdateTestCase1.class.getName(), Level.FATAL,
                    "" + ex);
            // out.println ( UpdateTestCase.class.getName ( ) + ex ) ;
        } catch (NullPointerException ex) {
            MyLogger.log(UpdateTestCase1.class.getName(), Level.FATAL,
                    "" + ex);
        } catch (ArrayIndexOutOfBoundsException ex) {
            MyLogger.log(UpdateTestCase1.class.getName(), Level.FATAL,
                    "" + ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UpdateTestCase1.class.getName(), Level.WARN, e.toString());
            }
        }

        response.sendRedirect("TestCase.jsp?Load=Load&Test="
                + request.getParameter("Test") + "&TestCase="
                + request.getParameter("TestCase"));
        out.close();
    }
}
