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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryTestCaseStep;
import org.cerberus.log.MyLogger;
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
        String conditionOper = request.getParameter("ConditionOper");
        String conditionVal1 = request.getParameter("ConditionVal1");
        String conditionVal2 = request.getParameter("ConditionVal2");
        String description = request.getParameter("Description");
        String fromTest = request.getParameter("FromTest");
        String fromTestCase = request.getParameter("FromTestCase");
        Integer fromStep = Integer.valueOf(request.getParameter("FromStep"));
        String importProperty = "N";
        if (request.getParameter("ImportProperty") != null) {
            MyLogger.log(ImportTestCaseStep.class.getName(), org.apache.log4j.Level.DEBUG, request.getParameter("ImportProperty"));
            importProperty = request.getParameter("ImportProperty");
        }

        TestCaseStep tcs = testCaseStepFactory.create(test, testCase, step, step, loop, conditionOper, conditionVal1, conditionVal2, description, "Y", fromTest, fromTestCase, fromStep, null);

        /**
         * Import Step, properties
         */
        MyLogger.log(ImportTestCaseStep.class.getName(), org.apache.log4j.Level.DEBUG, "Use Step");
        testCaseStepService.insertTestCaseStep(tcs);
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
            List<String> propertyNamesOfStep = new ArrayList<String>();
            List<TestCaseStepAction> testCaseStepActions = testCaseStepActionService.getListOfAction(fromTest, fromTestCase, fromStep);
            for (TestCaseStepAction action : testCaseStepActions) {
                if (!propertyNamesOfStep.contains(action.getValue2())) {
                    propertyNamesOfStep.add(action.getValue2());
                }
            }

            MyLogger.log(ImportTestCaseStep.class.getName(), org.apache.log4j.Level.DEBUG, "Rewrite TestCaseCountryProperties");
            if (tccListString != null) {
                tccListString.retainAll(tccFromListString);
                if (tccListString.size() > 0 && propertyNamesOfStep.size() > 0) {
                    List<TestCaseCountryProperties> tccpToImport = new ArrayList();
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
            Logger.getLogger(ImportTestCaseStep.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(ImportTestCaseStep.class.getName()).log(Level.SEVERE, null, ex);
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
