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
package org.cerberus.servlet.testCase;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.TCase;
import org.cerberus.entity.Test;
import org.cerberus.entity.TestCase;
import org.cerberus.entity.TestCaseCountry;
import org.cerberus.entity.TestCaseCountryProperties;
import org.cerberus.entity.TestCaseStep;
import org.cerberus.entity.TestCaseStepAction;
import org.cerberus.entity.TestCaseStepActionControl;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.factory.IFactoryTCase;
import org.cerberus.factory.IFactoryTestCase;
import org.cerberus.factory.impl.FactoryLogEvent;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.ITestCaseCountryPropertiesService;
import org.cerberus.service.ITestCaseCountryService;
import org.cerberus.service.ITestCaseService;
import org.cerberus.service.ITestCaseStepActionControlService;
import org.cerberus.service.ITestCaseStepActionService;
import org.cerberus.service.ITestCaseStepService;
import org.cerberus.service.ITestService;
import org.cerberus.service.impl.LogEventService;
import org.cerberus.service.impl.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
public class UpdateTestCaseWithDependencies extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws org.cerberus.exception.CerberusException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, CerberusException {
        String initialTest = request.getParameter("test");
        String initialTestCase = request.getParameter("testCase");
        TCase tc = getTestCaseFromParameter(request);
        List<TestCaseCountry> tcc = getTestCaseCountryFromParameter(request);
        List<TestCaseCountryProperties> tccp = getTestCaseCountryPropertiesFromParameter(request);
        List<TestCaseStep> tcs = getTestCaseStepFromParameter(request);
        List<TestCaseStepAction> tcsa = getTestCaseStepActionFromParameter(request);
        List<TestCaseStepActionControl> tcsac = getTestCaseStepActionControlFromParameter(request);

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestService tService = appContext.getBean(ITestService.class);
        ITestCaseService tcService = appContext.getBean(ITestCaseService.class);
        ITestCaseCountryService tccService = appContext.getBean(ITestCaseCountryService.class);
        ITestCaseCountryPropertiesService tccpService = appContext.getBean(ITestCaseCountryPropertiesService.class);
        ITestCaseStepService tcsService = appContext.getBean(ITestCaseStepService.class);
        ITestCaseStepActionService tcsaService = appContext.getBean(ITestCaseStepActionService.class);
        ITestCaseStepActionControlService tcsacService = appContext.getBean(ITestCaseStepActionControlService.class);

        /**
         * Verify the Test is the same than initialTest If it is the same > Do
         * nothing If it is not the same > Verify if test already exists If not
         * exist > create it If exist > do nothing
         */
        if (!tc.getTest().equals(initialTest)) {
            if (tService.findTestByKey(tc.getTest()) == null) {
                Test newTest = tService.findTestByKey(initialTestCase);
                newTest.setTest(tc.getTest());
                tService.createTest(newTest);
            }
        }

        /**
         * Verify testcase is the same than initialTestCase If it is the same >
         * update If it is not the same, > verify if testcase already exist If
         * it already exist > Send Error If it do not already exists > Create it
         */
        if (tc.getTestCase().equals(initialTestCase)) {
            //TODO implement updateTestCase
            tcService.updateTestCase(tc);
        } else {
            if (tcService.findTestCaseByKey(tc.getTest(), tc.getTestCase()) == null) {
                response.sendError(403, MessageGeneralEnum.GUI_TESTCASE_DUPLICATION_ALREADY_EXISTS.getDescription());
            } else {
                //TODO implement createTestCase
                tcService.createTestCase(tc);
            }
        }

        /**
         * For the list of testcase country verify it exists. If it does not
         * exists > create it If it exist, verify if it's the
         */
        List<TestCaseCountry> tccFromDatabase = tccService.findTestCaseCountryByTestTestCase(initialTest, initialTestCase);
        List<TestCaseCountry> tccToUpdateOrInsert = tcc;
        tccToUpdateOrInsert.removeAll(tccFromDatabase);
        List<TestCaseCountry> tccToDelete = tccFromDatabase;
        tccToDelete.removeAll(tcc);

        for (TestCaseCountry tccLeft : tccToUpdateOrInsert) {
            for (TestCaseCountry tccRight : tccFromDatabase) {
                if (tccLeft.hasSameKey(tccRight)) {
                    //tccService.updateTestCaseCountry(tccLeft);
                    tccToUpdateOrInsert.remove(tccLeft);
                }
            }
        }
        tccService.insertListTestCaseCountry(tccToUpdateOrInsert);

        for (TestCaseCountry tccLeft : tccToDelete) {
            for (TestCaseCountry tccRight : tcc) {
                if (tccLeft.hasSameKey(tccRight)) {
                    tccToDelete.remove(tccLeft);
                }
            }
        }
        tccService.deleteListTestCaseCountry(tccToDelete);

        /**
         * For the list of testcase country verify it exists. If it does not
         * exists > create it If it exist, verify if it's the
         */
        List<TestCaseCountryProperties> tccpFromDatabase = tccpService.findListOfPropertyPerTestTestCase(initialTest, initialTestCase);
        List<TestCaseCountryProperties> tccpToUpdateOrInsert = tccp;
        tccpToUpdateOrInsert.removeAll(tccpFromDatabase);
        List<TestCaseCountryProperties> tccpToDelete = tccpFromDatabase;
        tccpToDelete.removeAll(tccp);

        for (TestCaseCountryProperties tccpLeft : tccpToUpdateOrInsert) {
            for (TestCaseCountryProperties tccpRight : tccpFromDatabase) {
                if (tccpLeft.hasSameKey(tccpRight)) {
                    tccpService.updateTestCaseCountryProperties(tccpLeft);
                    tccpToUpdateOrInsert.remove(tccpLeft);
                }
            }
        }
        tccpService.insertListTestCaseCountryProperties(tccpToUpdateOrInsert);

        for (TestCaseCountryProperties tccpLeft : tccpToDelete) {
            for (TestCaseCountryProperties tccpRight : tccp) {
                if (tccpLeft.hasSameKey(tccpRight)) {
                    tccpToDelete.remove(tccpLeft);
                }
            }
        }
        tccpService.deleteListTestCaseCountryProperties(tccpToDelete);

        /**
         * For the list of testcase country verify it exists. If it does not
         * exists > create it If it exist, verify if it's the
         */
        List<TestCaseStep> tcsFromDatabase = tcsService.getListOfSteps(initialTest, initialTestCase);
        List<TestCaseStep> tcsToUpdateOrInsert = tcs;
        tcsToUpdateOrInsert.removeAll(tcsFromDatabase);
        List<TestCaseStep> tcsToDelete = tcsFromDatabase;
        tcsToDelete.removeAll(tcs);

        for (TestCaseStep tcsLeft : tcsToUpdateOrInsert) {
            for (TestCaseStep tcsRight : tcsFromDatabase) {
                if (tcsLeft.hasSameKey(tcsRight)) {
                    tcsService.updateTestCaseStep(tcsLeft);
                    tcsToUpdateOrInsert.remove(tcsLeft);
                }
            }
        }
        tcsService.insertListTestCaseStep(tcsToUpdateOrInsert);

        for (TestCaseStep tcsLeft : tcsToDelete) {
            for (TestCaseStep tcsRight : tcs) {
                if (tcsLeft.hasSameKey(tcsRight)) {
                    tcsToDelete.remove(tcsLeft);
                }
            }
        }
        tcsService.deleteListTestCaseStep(tcsToDelete);

        /**
         * For the list of testcase country verify it exists. If it does not
         * exists > create it If it exist, verify if it's the
         */
        List<TestCaseStepAction> tcsaFromDatabase = tcsaService.findTestCaseStepActionbyTestTestCase(initialTest, initialTestCase);
        List<TestCaseStepAction> tcsaToUpdateOrInsert = tcsa;
        tcsaToUpdateOrInsert.removeAll(tcsaFromDatabase);
        List<TestCaseStepAction> tcsaToDelete = tcsaFromDatabase;
        tcsaToDelete.removeAll(tcsa);

        for (TestCaseStepAction tcsaLeft : tcsaToUpdateOrInsert) {
            for (TestCaseStepAction tcsaRight : tcsaFromDatabase) {
                if (tcsaLeft.hasSameKey(tcsaRight)) {
                    tcsaService.updateTestCaseStepAction(tcsaLeft);
                    tcsaToUpdateOrInsert.remove(tcsaLeft);
                }
            }
        }
        tcsaService.insertListTestCaseStepAction(tcsaToUpdateOrInsert);

        for (TestCaseStepAction tcsaLeft : tcsaToDelete) {
            for (TestCaseStepAction tcsaRight : tcsa) {
                if (tcsaLeft.hasSameKey(tcsaRight)) {
                    tcsaToDelete.remove(tcsaLeft);
                }
            }
        }
        tcsaService.deleteListTestCaseStepAction(tcsaToDelete);

        /**
         * For the list of testcase country verify it exists. If it does not
         * exists > create it If it exist, verify if it's the
         */
        List<TestCaseStepActionControl> tcsacFromDatabase = tcsacService.findControlByTestTestCase(initialTest, initialTestCase);
        List<TestCaseStepActionControl> tcsacToUpdateOrInsert = tcsac;
        tcsacToUpdateOrInsert.removeAll(tcsacFromDatabase);
        List<TestCaseStepActionControl> tcsacToDelete = tcsacFromDatabase;
        tcsacToDelete.removeAll(tcsac);

        for (TestCaseStepActionControl tcsacLeft : tcsacToUpdateOrInsert) {
            for (TestCaseStepActionControl tcsacRight : tcsacFromDatabase) {
                if (tcsacLeft.hasSameKey(tcsacRight)) {
                    tcsacService.updateTestCaseStepActionControl(tcsacLeft);
                    tcsacToUpdateOrInsert.remove(tcsacLeft);
                }
            }
        }
        tcsacService.insertListTestCaseStepActionControl(tcsacToUpdateOrInsert);

        for (TestCaseStepActionControl tcsacLeft : tcsacToDelete) {
            for (TestCaseStepActionControl tcsacRight : tcsac) {
                if (tcsacLeft.hasSameKey(tcsacRight)) {
                    tcsacToDelete.remove(tcsacLeft);
                }
            }
        }
        tcsacService.deleteListTestCaseStepActionControl(tcsacToDelete);

        /**
         * Adding Log entry.
         */
        ILogEventService logEventService = appContext.getBean(LogEventService.class);
        IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
        try {
            logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/UpdateTestCase", "UPDATE", "Update testcase : ['" + tc.getTest() + "'|'" + tc.getTestCase() + "']", "", ""));
        } catch (CerberusException ex) {
            Logger.getLogger(UserService.class.getName()).log(Level.ERROR, null, ex);
        }

        response.sendRedirect(response.encodeRedirectURL("TestCase.jsp?Tinf=Y&Load=Load&Test=" + tc.getTest() + "&TestCase=" + tc.getTestCase()));

    }

    /**
     * Create new TestCase object from the information of request form
     *
     * @param request information from the request page
     * @return TestCase object
     * @see org.cerberus.entity.TestCase
     */
    private TCase getTestCaseFromParameter(HttpServletRequest request) {

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IFactoryTCase testCaseFactory = appContext.getBean(IFactoryTCase.class);

        String test = request.getParameter("Test");
        String testCase = request.getParameter("TestCase");
        String implementer = request.getParameter("editImplementer");
        String lastModifier = request.getUserPrincipal().getName();
        String project = request.getParameter("editProject");
        String ticket = request.getParameter("editTicket");
        String application = request.getParameter("editApplication");
        String runQA = request.getParameter("editRunQA");
        String runUAT = request.getParameter("editRunUAT");
        String runPROD = request.getParameter("editRunPROD");
        Integer priority = Integer.parseInt(request.getParameter("editPriority"));
        String group = request.getParameter("editGroup");
        String status = request.getParameter("editStatus");
        List<String> countries = new ArrayList<String>();

        if (request.getParameterValues("testcase_country_general") != null) {
            Collections.addAll(countries, request.getParameterValues("testcase_country_general"));
        }

        String shortDescription = request.getParameter("editDescription");
        String description = request.getParameter("valueDetail");
        String howTo = request.getParameter("howtoDetail");
        String active = request.getParameter("editTcActive");
        String fromSprint = request.getParameter("editFromBuild");
        String fromRevision = request.getParameter("editFromRev");
        String toSprint = request.getParameter("editToBuild");
        String toRevision = request.getParameter("editToRev");
        String bugID = request.getParameter("editBugID");
        String targetSprint = request.getParameter("editTargetBuild");
        String targetRevision = request.getParameter("editTargetRev");
        String comment = request.getParameter("editComment");
        String function = request.getParameter("function");
        return testCaseFactory.create(test, testCase, bugID, description, status, implementer, lastModifier, project, ticket, function, 
                application, runQA, runUAT, runPROD, priority, group, status, shortDescription, description, howTo, active, fromSprint, 
                fromRevision, toSprint, toRevision, status, bugID, targetSprint, targetRevision, comment, null, null, null, null);
    }

    private List<TestCaseCountry> getTestCaseCountryFromParameter(HttpServletRequest request) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private List<TestCaseCountryProperties> getTestCaseCountryPropertiesFromParameter(HttpServletRequest request) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private List<TestCaseStep> getTestCaseStepFromParameter(HttpServletRequest request) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private List<TestCaseStepAction> getTestCaseStepActionFromParameter(HttpServletRequest request) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private List<TestCaseStepActionControl> getTestCaseStepActionControlFromParameter(HttpServletRequest request) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
            java.util.logging.Logger.getLogger(UpdateTestCaseWithDependencies.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
            java.util.logging.Logger.getLogger(UpdateTestCaseWithDependencies.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
