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
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.crud.entity.TCase;
import org.cerberus.crud.entity.Test;
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryLogEvent;
import org.cerberus.crud.factory.impl.FactoryLogEvent;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.crud.service.ITestCaseCountryService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.ITestCaseStepActionControlService;
import org.cerberus.crud.service.ITestCaseStepActionService;
import org.cerberus.crud.service.ITestCaseStepService;
import org.cerberus.crud.service.ITestService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.crud.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet implementation class DuplicateTest
 */
@WebServlet(name = "DuplicateTestCase", urlPatterns = {"/DuplicateTestCase"})
public class DuplicateTestCase extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private ApplicationContext appContext;
    @Autowired
    private DatabaseSpring database;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IInvariantService invariantService = appContext.getBean(IInvariantService.class);
        ITestCaseService testCaseService = appContext.getBean(ITestCaseService.class);
        ITestService testService = appContext.getBean(ITestService.class);
        ITestCaseCountryService testCaseCountryService = appContext.getBean(ITestCaseCountryService.class);
        ITestCaseCountryPropertiesService testCaseCountryPropertiesService = appContext.getBean(ITestCaseCountryPropertiesService.class);
        ITestCaseStepService testCaseStepService = appContext.getBean(ITestCaseStepService.class);
        ITestCaseStepActionService testCaseStepActionService = appContext.getBean(ITestCaseStepActionService.class);
        ITestCaseStepActionControlService testCaseStepActionControlService = appContext.getBean(ITestCaseStepActionControlService.class);
        this.database = appContext.getBean(DatabaseSpring.class);

        String test = request.getParameter("Test");
        String testCase = request.getParameter("TestCase");
        String newTest = request.getParameter("editTest");
        String newTestCase = request.getParameter("editTestCase");

        try {
            if (testCaseService.findTestCaseByKey(test, testCase) != null) {
                /**
                 * Insert Test if user is TestAdmin and if the test doesn't
                 * exists
                 */
                if (!request.isUserInRole("TestAdmin") && testService.findTestByKey(newTest) == null) {
                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.GUI_TEST_CREATION_NOT_HAVE_RIGHT));
                } else {
                    if (testService.findTestByKey(newTest) == null) {
                        Test newT = testService.findTestByKey(test);
                        newT.setTest(newTest);
                        testService.createTest(newT);
                    }
                }

                /**
                 * Insert TestCase if not already exists
                 */
                if (testCaseService.findTestCaseByKey(newTest, newTestCase) != null) {
                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.GUI_TESTCASE_DUPLICATION_ALREADY_EXISTS));
                }
                if (testCaseService.findTestCaseByKey(test, testCase) == null) {
                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                }
                TCase newTc = testCaseService.findTestCaseByKey(test, testCase);
                newTc.setTest(newTest);
                newTc.setTestCase(newTestCase);
                newTc.setCreator(request.getUserPrincipal().getName());
                newTc.setStatus(invariantService.findListOfInvariantById("TCSTATUS").get(0).getValue());
                testCaseService.createTestCase(newTc);

                /**
                 * Insert Countries
                 */
                List<TestCaseCountry> newTccList = new ArrayList();
                List<TestCaseCountry> tccList = testCaseCountryService.findTestCaseCountryByTestTestCase(test, testCase);
                for (TestCaseCountry tcc : tccList) {
                    tcc.setTest(newTest);
                    tcc.setTestCase(newTestCase);
                    newTccList.add(tcc);
                    testCaseCountryService.insertListTestCaseCountry(newTccList);
                    /**
                     * Insert Properties
                     */
                    List<TestCaseCountryProperties> newTccpList = new ArrayList();
                    List<TestCaseCountryProperties> tccpList = testCaseCountryPropertiesService.findListOfPropertyPerTestTestCaseCountry(test, testCase, tcc.getCountry());
                    for (TestCaseCountryProperties tccp : tccpList) {
                        tccp.setTest(newTest);
                        tccp.setTestCase(newTestCase);
                        newTccpList.add(tccp);
                    }
                    testCaseCountryPropertiesService.insertListTestCaseCountryProperties(newTccpList);
                }

                /**
                 * Insert Step
                 */
                List<TestCaseStep> newTcsList = new ArrayList();
                List<TestCaseStepAction> newTcsaList = new ArrayList();
                List<TestCaseStepActionControl> newTcsacList = new ArrayList();
                List<TestCaseStep> tcsList = testCaseStepService.getListOfSteps(test, testCase);
                for (TestCaseStep tcs : tcsList) {
                    tcs.setTest(newTest);
                    tcs.setTestCase(newTestCase);
                    newTcsList.add(tcs);
                    /**
                     * Insert Actions
                     */
                    List<TestCaseStepAction> tcsaList = testCaseStepActionService.getListOfAction(test, testCase, tcs.getStep());
                    for (TestCaseStepAction tcsa : tcsaList) {
                        tcsa.setTest(newTest);
                        tcsa.setTestCase(newTestCase);
                        newTcsaList.add(tcsa);
                        /**
                         * Insert Controls
                         */
                        List<TestCaseStepActionControl> tcsacList = testCaseStepActionControlService.findControlByTestTestCaseStepSequence(test, testCase, tcs.getStep(), tcsa.getSequence());
                        for (TestCaseStepActionControl tcsac : tcsacList) {
                            tcsac.setTest(newTest);
                            tcsac.setTestCase(newTestCase);
                            newTcsacList.add(tcsac);
                        }
                    }
                }
                testCaseStepService.insertListTestCaseStep(newTcsList);
                testCaseStepActionService.insertListTestCaseStepAction(newTcsaList);
                testCaseStepActionControlService.insertListTestCaseStepActionControl(newTcsacList);

                /**
                 * Adding Log entry.
                 */
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                logEventService.createPrivateCalls("/DuplicateTestCase", "CREATE", "Duplicate testcase From : ['" + test + "'|'" + testCase + "'] To : ['" + newTest + "'|'" + newTestCase + "']", request);

                response.sendRedirect("TestCase.jsp?Load=Load&Test="
                        + newTest + "&TestCase="
                        + newTestCase);

            } else {
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.GUI_TEST_DUPLICATION_NOT_EXISTING_TEST));
            }
        } catch (CerberusException exception) {
//TODO : Choose the way we raise error : 
//    > Using commented lines below and javascript message
//            request.setAttribute("flashMessage", exception.getMessageError().getDescription());
//            RequestDispatcher rd = request.getRequestDispatcher("TestCase.jsp?Load=Load&Test=" + test + "&TestCase=" + testCase);  
//            rd.forward(request, response); 

//    > Using http error pages as below
            response.sendError(exception.getMessageError().getCode(), exception.getMessageError().getDescription());
        }
    }   // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

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
        processRequest(request, response);
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
