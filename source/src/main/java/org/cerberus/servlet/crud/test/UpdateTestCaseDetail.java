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
package org.cerberus.servlet.crud.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryTestCaseCountryProperties;
import org.cerberus.crud.factory.IFactoryTestCaseStepAction;
import org.cerberus.crud.factory.IFactoryTestCaseStepActionControl;
import org.cerberus.log.MyLogger;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.crud.service.ITestCaseStepActionControlService;
import org.cerberus.crud.service.ITestCaseStepActionService;
import org.cerberus.crud.service.impl.LogEventService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.HtmlUtils;

/**
 * @author bcivel
 */
@WebServlet(name = "UpdateTestCaseDetail", urlPatterns = {"/UpdateTestCaseDetail"})
public class UpdateTestCaseDetail extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    public String changeStringDelimiter(String s) {

        return s.replace("\'", "\\'");
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            // response.setContentType("text/html;charset=UTF-8");
            this.processRequest(request, response);
        } catch (CerberusException ex) {
            java.util.logging.Logger.getLogger(UpdateTestCaseDetail.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            // response.setContentType("text/html;charset=UTF-8");
            this.processRequest(request, response);
        } catch (CerberusException ex) {
            java.util.logging.Logger.getLogger(UpdateTestCaseDetail.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
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
            if (testcase_info.indexOf(t) < 3) {
                if (t[index].isEmpty() || t[index].trim().equals("")
                        || t[index].equals(" ") || (t[index] == null)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public String getServletInfo() {

        return "Short description";
    }

    public String getStringParameter(String parameter,
            HttpServletRequest request) {

        if (request.getParameter(parameter) != null) {
            return request.getParameter(parameter);
        }
        return "";
    }

    public String[] getStringTable(String parameter, HttpServletRequest request) {
        return getStringTable(parameter, request, false);
    }

    public String[] getStringTable(String parameter, HttpServletRequest request, boolean toEscape) {

        if (request.getParameterValues(parameter) != null) {
            try {
                request.setCharacterEncoding("UTF-8");
            } catch (UnsupportedEncodingException e) {
                //TODO Auto-generated catch block
                e.printStackTrace();
            }
            String[] parameters = request.getParameterValues(parameter);
            if (toEscape) {
                for (int i = 0; i < parameters.length; i++) {
                    parameters[i] = HtmlUtils.htmlEscape(parameters[i]);
                }
            }
            return parameters;

        }
        return new String[0];
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, CerberusException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        DatabaseSpring database = appContext.getBean(DatabaseSpring.class);
        ITestCaseCountryPropertiesService propertiesService = appContext.getBean(ITestCaseCountryPropertiesService.class);
        IFactoryTestCaseCountryProperties propertiesFactory = appContext.getBean(IFactoryTestCaseCountryProperties.class);

        Connection connection = database.connect();
        try {
            Statement stmt4 = connection.createStatement();
            try {

                /*
                 * PROPERTIES
                 */
                /*
                 * Get Test, TestCase and Country to update
                 */
                String test_testcase_no_format_prop = this.getStringParameter(
                        "testcase_hidden", request);
                this.getStringParameter("Test", request);
                this.getStringParameter("TestCase", request);
                String country_list_no_format = this.getStringParameter(
                        "CountryList", request);

                /*
                 * Properties Insert/Update
                 */
                String[] testcase_properties_delete = request.getParameterValues("properties_delete");
                // this.getStringTable("old_property_hidden", request);
                // String[] testcase_properties_countr_list = this.getStringTable(
                // "old_property_hidden", request);
                String[] testcase_properties_countr_list = request.getParameterValues("old_property_hidden");
                if (testcase_properties_countr_list == null) {
                    testcase_properties_countr_list = new String[0];
                }
                String[] testcase_properties_propertyrow = request.getParameterValues("property_hidden");
                String[] testcase_properties_property = this.getStringTable(
                        "properties_property", request);
                String[] testcase_properties_description = this.getStringTable(
                        "properties_description", request);
                String[] testcase_properties_country = this.getStringTable(
                        "properties_country", request);
                String[] testcase_properties_type = this.getStringTable(
                        "properties_type", request);
                String[] testcase_properties_value = this.getStringTable(
                        "properties_value", request);
                String[] testcase_properties_value2 = this.getStringTable(
                        "properties_value2", request);
                //String[] testcase_properties_value =
                //         request.getParameterValues("properties_type");

                String[] testcase_properties_length = this.getStringTable(
                        "properties_length", request);
                String[] testcase_properties_rowlimit = this.getStringTable(
                        "properties_rowlimit", request);
                String[] testcase_properties_nature = this.getStringTable(
                        "properties_nature", request);
                String[] testcase_properties_database = this.getStringTable(
                        "properties_dtb", request);

                /*
                 * 0 : Test 1 : TestCase
                 */
                String[] test_testcase_format_prop = new String[]{"", ""};
                test_testcase_format_prop = test_testcase_no_format_prop.split(" - ");
                String[] country_list = country_list_no_format.split(" - ");

                /*
                 * Properties
                 */
                List<String[]> testcase_properties_propertycountry = new ArrayList<String[]>();
                testcase_properties_propertycountry.add(testcase_properties_country);

                List<String[]> testcase_properties_info = new ArrayList<String[]>();
                testcase_properties_info.add(testcase_properties_propertyrow);
                testcase_properties_info.add(testcase_properties_property);
                testcase_properties_info.add(testcase_properties_type);
                testcase_properties_info.add(testcase_properties_value);
                testcase_properties_info.add(testcase_properties_value2);
                testcase_properties_info.add(testcase_properties_length);
                testcase_properties_info.add(testcase_properties_rowlimit);
                testcase_properties_info.add(testcase_properties_nature);
                testcase_properties_info.add(testcase_properties_database);

                List<String[]> countr_list = new ArrayList<String[]>();
                countr_list.add(testcase_properties_countr_list);

                // To Delete the country unselected
                Collection<String> listOne = new ArrayList<String>();
                Collection<String> listTwo = new ArrayList<String>();
                Collection<String> similar = new ArrayList<String>();
                Collection<String> different = new ArrayList<String>();

                listOne.addAll(Arrays.asList(testcase_properties_countr_list));
                listTwo.addAll(Arrays.asList(testcase_properties_country));

                similar.addAll(listOne);
                similar.retainAll(listTwo);
                different.addAll(listOne);
                different.removeAll(similar);
                List<String> differentlist = new ArrayList<String>(different);
                //

                ResultSet rs_Properties = stmt4.executeQuery("SELECT * "
                        + " FROM testcasecountryproperties " + " WHERE Test = '"
                        + test_testcase_format_prop[0] + "'" + " AND TestCase = '"
                        + test_testcase_format_prop[1] + "'");
                try {
                    // the country property already exists????
                    if (rs_Properties.first()) {

                        for (int i = 0; i < testcase_properties_property.length; i++) {
                            for (int j = 0; j < differentlist.size(); j++) {
                                String[] tc_country = new String[]{"", ""};
                                tc_country = differentlist.get(j).split(" - ");

                                if (testcase_properties_propertyrow[i].equals(tc_country[0])) {
                                    // if the number of the line is the same for the
                                    // country and the property:
                                    Statement stmt3 = connection.createStatement();
                                    try {
                                        stmt3.execute("DELETE FROM testcasecountryproperties "
                                                + " WHERE Test = '"
                                                + test_testcase_format_prop[0]
                                                + "' "
                                                + " AND TestCase = '"
                                                + test_testcase_format_prop[1]
                                                + "' "
                                                + " AND Country = '"
                                                + tc_country[1]
                                                + "' "
                                                + " AND Property = '"
                                                + testcase_properties_property[i] + "'");
                                    } finally {
                                        stmt3.close();
                                    }
                                    // Is the country exist in the database??
                                }// end of the if loop
                            } // end of the loop for differnet list
                        } // end of the property loop >>>>>The country unselected have
                        // been removed

                        //Delete the property which have been renamed
                        for (int i = 0; i < testcase_properties_property.length; i++) {
                            for (int p = 0; p < testcase_properties_countr_list.length; p++) {
                                String[] tc_prop = new String[]{"", "", ""};
                                tc_prop = testcase_properties_countr_list[p].split(" - ");
                                if (testcase_properties_propertyrow[i].equals(tc_prop[0])
                                        && !testcase_properties_property[i].equals(tc_prop[2])) {
                                    Statement stmt3 = connection.createStatement();
                                    try {
                                        stmt3.execute("DELETE FROM testcasecountryproperties "
                                                + " WHERE Test = '"
                                                + test_testcase_format_prop[0]
                                                + "' "
                                                + " AND TestCase = '"
                                                + test_testcase_format_prop[1]
                                                + "' "
                                                + " AND Country = '"
                                                + tc_prop[1]
                                                + "' "
                                                + " AND Property = '"
                                                + tc_prop[2] + "'");
                                    } finally {
                                        stmt3.close();
                                    }
                                }
                            }
                        }
                        // for each line, insert the property when not exist and update
                        // when exist.
                        for (int i = 0; i < testcase_properties_property.length; i++) {
                            // for each country flagged in the page
                            for (int j = 0; j < testcase_properties_country.length; j++) {
                                // separate the number of the line to the country:
                                // example: 1 - AT
                                String[] testcase_country = new String[]{"", ""};
                                testcase_country = testcase_properties_country[j].split(" - ");
                                // if the number of the line is the same for the country
                                // and the property:
                                if (testcase_properties_propertyrow[i].equals(testcase_country[0])) {
                                    Statement stmt3 = connection.createStatement();
                                    try {
                                        ResultSet rs_numberOfTestCasesCountryProperties = stmt3.executeQuery("SELECT Test, TestCase, Country, Property "
                                                + " FROM testcasecountryproperties "
                                                + " WHERE Test = '"
                                                + test_testcase_format_prop[0]
                                                + "'"
                                                + " AND TestCase = '"
                                                + test_testcase_format_prop[1]
                                                + "'"
                                                + " AND Country = '"
                                                + testcase_country[1]
                                                + "'"
                                                + " AND Property = '"
                                                + testcase_properties_property[i]
                                                + "'");
                                        try {
                                            // Is the country exist in the database??
                                            // the country property already exists, make an
                                            // update
                                            if (rs_numberOfTestCasesCountryProperties.first()) {

                                                String test = test_testcase_format_prop[0];
                                                String testcase = test_testcase_format_prop[1];
                                                String country = testcase_country[1];
                                                String property = testcase_properties_property[i];
                                                String description = testcase_properties_description[i];
                                                String nature = testcase_properties_nature[i];
                                                int rowlimit = Integer.parseInt(testcase_properties_rowlimit[i]);
                                                int length = Integer.parseInt(testcase_properties_length[i]);
                                                String value1 = testcase_properties_value[i];
                                                String value2 = testcase_properties_value2[i];
                                                String type = testcase_properties_type[i];
                                                String dtb = testcase_properties_database[i];

                                                TestCaseCountryProperties tccp = propertiesFactory.create(test, testcase, country, property, description, type, dtb, value1, value2, length, rowlimit, nature);

                                                propertiesService.updateTestCaseCountryProperties(tccp);

                                            } else // the country property does'nt extist, make an
                                            // insert :
                                            { /*
                                                 * Insert new rows
                                                 */

                                                String test = test_testcase_format_prop[0];
                                                String testcase = test_testcase_format_prop[1];
                                                String country = testcase_country[1];
                                                String property = testcase_properties_property[i];
                                                String description = testcase_properties_description[i];
                                                String nature = testcase_properties_nature[i];
                                                int rowlimit = Integer.parseInt(testcase_properties_rowlimit[i]);
                                                int length = Integer.parseInt(testcase_properties_length[i]);
                                                String value1 = testcase_properties_value[i];
                                                String value2 = testcase_properties_value2[i];
                                                String type = testcase_properties_type[i];
                                                String dtb = testcase_properties_database[i];

                                                TestCaseCountryProperties tccp = propertiesFactory.create(test, testcase, country, property, description, type, dtb, value1, value2, length, rowlimit, nature);

                                                propertiesService.insertTestCaseCountryProperties(tccp);

                                            }// end of the else loop
                                        } finally {
                                            rs_numberOfTestCasesCountryProperties.close();
                                        }
                                    } finally {
                                        stmt3.close();
                                    }
                                }// end of the if loop

                            }// Close the loop for country
                        }// Close the loop for (property)
                    } // end of the if loop (property already exists??
                    else // The property is a first one
                    {
                        for (int i = 0; i < testcase_properties_property.length; i++) {
                            // for each country flagged in the page
                            for (int j = 0; j < testcase_properties_country.length; j++) {
                                // separate the number of the line to the country:
                                // example: 1 - AT
                                String[] testcase_country = new String[]{"", ""};
                                testcase_country = testcase_properties_country[j].split(" - ");
                                // if the number of the line is the same for the country
                                // and the property:
                                if (testcase_properties_propertyrow[i].equals(testcase_country[0])) {
                                    String test = test_testcase_format_prop[0];
                                    String testcase = test_testcase_format_prop[1];
                                    String country = testcase_country[1];
                                    String property = testcase_properties_property[i];
                                    String description = testcase_properties_description[i];
                                    String nature = testcase_properties_nature[i];
                                    int rowlimit = Integer.parseInt(testcase_properties_rowlimit[i]);
                                    int length = Integer.parseInt(testcase_properties_length[i]);
                                    String value1 = testcase_properties_value[i];
                                    String value2 = testcase_properties_value2[i];
                                    String type = testcase_properties_type[i];
                                    String dtb = testcase_properties_database[i];

                                    TestCaseCountryProperties tccp = propertiesFactory.create(test, testcase, country, property, description, type, dtb, value1, value2, length, rowlimit, nature);

                                    propertiesService.insertTestCaseCountryProperties(tccp);
                                } // Close the condition on the row number
                            } // Close the loop for (country)
                        } // Close the else condition
                    }
                } finally {
                    rs_Properties.close();
                }

                /*
                 * Delete Properties
                 */
                if (testcase_properties_delete != null) { // If some properties
                    // check for delete
                    Statement stmt3 = connection.createStatement();
                    try {
                        for (String element : testcase_properties_delete) {
                            String property_and_country[] = element.split(" - ");
                            for (int i = 1; i < property_and_country.length; i++) {
                                stmt3.execute("DELETE FROM testcasecountryproperties "
                                        + " WHERE Test = '" + test_testcase_format_prop[0]
                                        + "' " + " AND TestCase = '"
                                        + test_testcase_format_prop[1] + "' "
                                        + " AND Country = '" + property_and_country[i]
                                        + "' " + " AND Property = '"
                                        + property_and_country[0] + "'");
                            }
                        }
                    } finally {
                        stmt3.close();
                    }
                }

                String sqlupd = "UPDATE testcase "
                        + "SET LastModifier = '" + request.getUserPrincipal().getName()
                        + "' WHERE Test = '" + test_testcase_format_prop[0]
                        + "' AND TestCase = '" + test_testcase_format_prop[1] + "' ";
                stmt4.execute(sqlupd);

                /*
                 * END OF PROPERTIES
                 */
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
                        request, true);
                String step_number_toadd[] = this.getStringTable("step_number_add",
                        request);
                String step_desc_toadd[] = this.getStringTable(
                        "step_description_add", request, true);

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
                String step_description[] = this.getStringTable("actions_description",
                        request, true);
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
                String controls_controldescription[] = this.getStringTable(
                        "controls_controldescription", request, true);
                String controls_fatal[] = this.getStringTable("controls_fatal",
                        request);

                /*
                 * Properties Insert/Update
                 */
                String[] test_testcase_format = test_testcase_no_format.split(" - ");


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
                testcase_actions_info.add(step_description);

                /*
                 * Controls
                 */
                List<String[]> testcase_controls_info = new ArrayList<String[]>();
                testcase_controls_info.add(controls_control);
                testcase_controls_info.add(controls_controldescription);
                testcase_controls_info.add(controls_controlproperty);
                testcase_controls_info.add(controls_controlvalue);
                testcase_controls_info.add(controls_sequence);
                testcase_controls_info.add(controls_step);
                testcase_controls_info.add(controls_type);
                testcase_controls_info.add(controls_fatal);

                /*
                 * Test Case Step Description
                 */
                /*
                 * Update Test Case Step
                 */
                int numberOfSteps = 0;
                Integer steps = 0;
                ResultSet rs_step = stmt4.executeQuery("SELECT COUNT(*) "
                        + " FROM testcasestep " + " WHERE Test = '"
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
                        stmt4.executeUpdate("UPDATE testcasestep "
                                + " SET Description = '" + step_desc[i] + "' "
                                + " WHERE Test = '" + test_testcase_format[0] + "'"
                                + " AND TestCase = '" + test_testcase_format[1]
                                + "' " + " AND Step = " + step_id[i] + "");

                        stmt4.executeUpdate("DELETE FROM testcasestepbatch WHERE  "
                                + " test = '" + test_testcase_format[0] + "' "
                                + " AND TestCase = '" + test_testcase_format[1]
                                + "' AND STEP = '" + step_id[i] + "' ");

                        if (step_batch != null) {
                            for (int j = 0; j < step_batch.length; j++) {
                                try {
                                    stmt4.executeUpdate("INSERT INTO testcasestepbatch (`Test`, `TestCase`, `Step`, `Batch`) VALUES ( "
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
                                    MyLogger.log(UpdateTestCaseDetail.class.getName(), Level.FATAL, "Error on insert into TestCaseStepBatch: " + e.toString());
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
                        String sql = ("INSERT INTO testcasestep (`Test`,`Testcase`,`Step`,`Description`) "
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
                        stmt4.execute(sql);
                    }

                    stmt4.executeUpdate("DELETE FROM testcasestepbatch WHERE  "
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
                                stmt4.executeUpdate(sql);

                            } catch (Exception e) {
                                MyLogger.log(UpdateTestCaseDetail.class.getName(), Level.FATAL, "Error on insert into TestCaseStepBatch: " + e.toString());
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
                        String sql = ("SELECT * " + " FROM testcasestepaction"
                                + " WHERE Test = '" + test_testcase_format[0] + "'"
                                + " AND TestCase = '" + test_testcase_format[1]
                                + "' " + " AND Step = " + step_number_hide[i] + " "
                                + " AND Sequence = " + step_sequence[i]);
                        ResultSet rs_stepaction = stmt4.executeQuery(sql);
                        try {
                            IFactoryTestCaseStepAction actionFactory = appContext.getBean(IFactoryTestCaseStepAction.class);
                            ITestCaseStepActionService actionService = appContext.getBean(ITestCaseStepActionService.class);
                            TestCaseStepAction tcsa = actionFactory.create(test_testcase_format[0], test_testcase_format[1], Integer.parseInt(step_number_hide[i]), Integer.parseInt(step_sequence[i]),
                                    step_action[i], step_object[i], step_property[i], step_description[i]);

                            if (rs_stepaction.next()) { /*
                                 * Update
                                 */

                                actionService.updateTestCaseStepAction(tcsa);

                            } else { /*
                                 * Insert
                                 */

                                if (this.formIsFullFill(testcase_actions_info, i)) {
                                    actionService.insertTestCaseStepAction(tcsa);

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
                for (int i = 0; i < controls_control.length; i++) {

                    /*
                     * Select to know if need to update or insert
                     */
                    if (this.formIsFill(controls_sequence[i])
                            && (controls_sequence[i].length() > 0)) {
                        String sql = ("SELECT * " + " FROM testcasestepactioncontrol"
                                + " WHERE Test = '" + test_testcase_format[0] + "'"
                                + " AND TestCase = '" + test_testcase_format[1]
                                + "' " + " AND Step = " + controls_step[i] + " "
                                + " AND Sequence = " + controls_sequence[i] + " "
                                + " AND control = " + controls_control[i]);
                        ResultSet rs_stepactioncontrol = stmt4.executeQuery(sql);
                        try {
                            IFactoryTestCaseStepActionControl controlFactory = appContext.getBean(IFactoryTestCaseStepActionControl.class);
                            ITestCaseStepActionControlService controlService = appContext.getBean(ITestCaseStepActionControlService.class);
                            TestCaseStepActionControl control = controlFactory.create(test_testcase_format[0], test_testcase_format[1], Integer.parseInt(controls_step[i]),
                                    Integer.parseInt(controls_sequence[i]), Integer.parseInt(controls_control[i]), controls_type[i], (controls_controlvalue[i]),
                                    controls_controlproperty[i], controls_fatal[i], controls_controldescription[i]);

                            if (rs_stepactioncontrol.next()) {

                                controlService.updateTestCaseStepActionControl(control);

                            } else {
//                        if (this.formIsFullFill(testcase_controls_info, i)) {

                                controlService.insertTestCaseStepActionControl(control);

//                        }
                            }

                        } finally {
                            rs_stepactioncontrol.close();
                        }
                    }
                }


                /*
                 * DELETE
                 */
                if (step_delete != null) {
                    for (String step_to_delete : step_delete) {
                        stmt4.execute("DELETE FROM testcasestep "
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

                        stmt4.execute("DELETE FROM testcasestepaction "
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

                        stmt4.execute("DELETE FROM testcasestepactioncontrol "
                                + " WHERE Test = '" + test_testcase_format[0]
                                + "' " + " AND TestCase = '"
                                + test_testcase_format[1] + "' " + " AND Step = "
                                + key_delete[0] + " " + " AND Sequence = "
                                + key_delete[1] + " " + " AND Control = "
                                + key_delete[2] + "");

                    }
                }

                stmt4.execute("UPDATE testcase "
                        + "SET LastModifier = '" + request.getUserPrincipal().getName() + "' "
                        + "WHERE Test = '" + test_testcase_format[0] + "' "
                        + " AND TestCase = '" + test_testcase_format[1] + "' ");

                /**
                 * Adding Log entry.
                 */
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                logEventService.createPrivateCalls("/UpdateTestCaseDetail", "UPDATE", "Update testcase detail : ['" + test_testcase_format_prop[0] + "'|'" + test_testcase_format_prop[1] + "']", request);


                /*
                 * Redirect
                 */
                response.sendRedirect("TestCase.jsp?Load=Load&Test="
                        + test_testcase_format[0] + "&TestCase="
                        + test_testcase_format[1]);
                return;
            } finally {
                stmt4.close();
            }
        } catch (SQLException ex) {
            MyLogger.log(UpdateTestCaseDetail.class.getName(), Level.FATAL,
                    "" + ex);
            // out.println ( UpdateTestCase.class.getName ( ) + ex ) ;
        } catch (NullPointerException ex) {
            MyLogger.log(UpdateTestCaseDetail.class.getName(), Level.FATAL,
                    "" + ex);
        } catch (ArrayIndexOutOfBoundsException ex) {
            MyLogger.log(UpdateTestCaseDetail.class.getName(), Level.FATAL,
                    "" + ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(UpdateTestCaseDetail.class.getName(), Level.WARN, e.toString());
            }
        }

        response.sendRedirect("TestCase.jsp?Load=Load&Test="
                + request.getParameter("Test") + "&TestCase="
                + request.getParameter("TestCase"));
        out.close();
    }
}
