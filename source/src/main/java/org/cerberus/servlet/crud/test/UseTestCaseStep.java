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
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryTestCaseStep;
import org.cerberus.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.crud.service.ITestCaseCountryService;
import org.cerberus.crud.service.ITestCaseStepActionService;
import org.cerberus.crud.service.ITestCaseStepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "UseTestCaseStep", urlPatterns = {"/UseTestCaseStep"})
public class UseTestCaseStep extends HttpServlet {

    private ApplicationContext appContext;
    private static final Logger LOG = LogManager.getLogger(UseTestCaseStep.class);

    @Autowired
    private DatabaseSpring database;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, CerberusException {
        response.setContentType("text/html;charset=UTF-8");

        appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseStepActionService testCaseStepActionService = appContext.getBean(ITestCaseStepActionService.class);
        ITestCaseStepService testCaseStepService = appContext.getBean(ITestCaseStepService.class);
        IFactoryTestCaseStep testCaseStepFactory = appContext.getBean(IFactoryTestCaseStep.class);
        ITestCaseCountryService testCaseCountry = appContext.getBean(ITestCaseCountryService.class);
        ITestCaseCountryPropertiesService testCaseCountryProperties = appContext.getBean(ITestCaseCountryPropertiesService.class);
        this.database = appContext.getBean(DatabaseSpring.class);

        /**
         * Get Parameters Test : Target Test TestCase : Target TestCase Step :
         * Target Step fromTest : from Test fromTestCase : from TestCase
         * fromStep : from Step
         */
        String test = request.getParameter("Test");
        String testCase = request.getParameter("TestCase");
        Integer step = Integer.valueOf(request.getParameter("Step"));
        String loop = request.getParameter("Loop");
        String conditionOperator = request.getParameter("conditionOperator");
        String conditionVal1 = request.getParameter("ConditionVal1");
        String conditionVal2 = request.getParameter("ConditionVal2");
        String conditionVal3 = request.getParameter("ConditionVal3");
        String description = request.getParameter("Description");
        String fromTest = request.getParameter("FromTest");
        String fromTestCase = request.getParameter("FromTestCase");
        Integer fromStep = Integer.valueOf(request.getParameter("FromStep"));
        String importProperty = "N";
        if (request.getParameter("ImportProperty") != null) {
            LOG.debug(request.getParameter("ImportProperty"));
            importProperty = request.getParameter("ImportProperty");
        }

        TestCaseStep tcs = testCaseStepFactory.create(test, testCase, step, step, loop, conditionOperator, conditionVal1, conditionVal2, conditionVal3, description, "Y", fromTest, fromTestCase, fromStep, null, null, null, null, null, null);

        /**
         * Import Step, properties
         */
        LOG.debug("Use Step");
        testCaseStepService.create(tcs);
        if (importProperty.equalsIgnoreCase("Y")) {
            /**
             * Get List of Country of the origin testcase and the destination
             * Testcase
             */
            List<String> tccListString = null;
            List<String> tccFromListString = null;
            List<TestCaseCountryProperties> tccpList = null;
            if (importProperty.equalsIgnoreCase("Y")) {
                tccListString = testCaseCountry.findListOfCountryByTestTestCase(test, testCase);
                tccFromListString = testCaseCountry.findListOfCountryByTestTestCase(test, testCase);
            }

            /**
             * For the country defined in the destination testcase, insert the
             * properties of the origine testcase
             */
            // retrieve list of property name used in the step
            List<String> propertyNamesOfStep = new ArrayList<>();
            List<TestCaseStepAction> testCaseStepActions = testCaseStepActionService.getListOfAction(fromTest, fromTestCase, fromStep);
            for (TestCaseStepAction action : testCaseStepActions) {
                if (!propertyNamesOfStep.contains(action.getValue2())) {
                    propertyNamesOfStep.add(action.getValue2());
                }
            }

            LOG.debug("Rewrite TestCaseCountryProperties");
            if (tccListString != null) {
                tccListString.retainAll(tccFromListString);
                if (tccListString.size() > 0 && propertyNamesOfStep.size() > 0) {
                    List<TestCaseCountryProperties> tccpToImport = new ArrayList<>();
                    for (String country : tccListString) {
                        tccpList = testCaseCountryProperties.findListOfPropertyPerTestTestCaseCountry(fromTest, fromTestCase, country);
                        for (TestCaseCountryProperties tccp : tccpList) {
                            // only add property of the test case if it is used by the step
                            if (propertyNamesOfStep.contains(tccp.getProperty())) {
                                tccp.setTest(test);
                                tccp.setTestCase(testCase);
                                tccpToImport.add(tccp);
                            }
                        }
                    }

                    // if the list of property to import is not empty, insert them.
                    if (!tccpToImport.isEmpty()) {
                        testCaseCountryProperties.insertListTestCaseCountryProperties(tccpToImport);
                    }
                }
            }

        }

        response.sendRedirect("TestCase.jsp?Load=Load&Test=" + test + "&TestCase=" + testCase);

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (CerberusException ex) {
            LOG.warn(ex);
        }
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (CerberusException ex) {
            LOG.warn(ex);
        }
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
