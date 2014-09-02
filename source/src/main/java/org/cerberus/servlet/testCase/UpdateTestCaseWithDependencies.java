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
import java.util.ArrayList;
import java.util.Collection;
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
import org.cerberus.entity.TestCaseCountry;
import org.cerberus.entity.TestCaseCountryProperties;
import org.cerberus.entity.TestCaseStep;
import org.cerberus.entity.TestCaseStepAction;
import org.cerberus.entity.TestCaseStepActionControl;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.factory.IFactoryTCase;
import org.cerberus.factory.IFactoryTestCaseCountry;
import org.cerberus.factory.IFactoryTestCaseCountryProperties;
import org.cerberus.factory.IFactoryTestCaseStep;
import org.cerberus.factory.IFactoryTestCaseStepAction;
import org.cerberus.factory.IFactoryTestCaseStepActionControl;
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
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        String initialTest = request.getParameter("informationInitialTest");
        String initialTestCase = request.getParameter("informationInitialTestCase");
        String test = request.getParameter("informationTest");
        String testCase = request.getParameter("informationTestCase");
        TCase tc = getTestCaseFromParameter(request, appContext, test, testCase);
        List<TestCaseCountry> tcc = getTestCaseCountryFromParameter(request, appContext, test, testCase);
        List<TestCaseCountryProperties> tccp = getTestCaseCountryPropertiesFromParameter(request, appContext, test, testCase);
        List<TestCaseStep> tcs = getTestCaseStepFromParameter(request, appContext, test, testCase);
        List<TestCaseStepAction> tcsa = getTestCaseStepActionFromParameter(request, appContext, test, testCase);
        List<TestCaseStepActionControl> tcsac = getTestCaseStepActionControlFromParameter(request, appContext, test, testCase);

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
            tcService.updateTestCase(tc);
        } else {
            try { 
                if (tcService.findTestCaseByKey(tc.getTest(), tc.getTestCase()) != null){
                    response.sendError(403, MessageGeneralEnum.GUI_TESTCASE_DUPLICATION_ALREADY_EXISTS.getDescription());
            } else {
                    tcService.createTestCase(tc);  
                }
            } catch (CerberusException ex) {
            tcService.createTestCase(tc); 
            }
                
            }
        

        /**
         * For the list of testcase country verify it exists. If it does not
         * exists > create it If it exist, verify if it's the
         */
        List<TestCaseCountry> tccFromDtb = tccService.findTestCaseCountryByTestTestCase(initialTest, initialTestCase);
        
        List<TestCaseCountry> tccToUpdateOrInsert = tcc;
        List<TestCaseCountry> tcc2 = new ArrayList(tcc);
        tccToUpdateOrInsert.removeAll(tccFromDtb);
        List<TestCaseCountry> tccToUpdateOrInsertToIterate = tccToUpdateOrInsert;
        
        List<TestCaseCountry> tccToDelete = new ArrayList(tccFromDtb);
        List<TestCaseCountry> tccFromDtb2 = new ArrayList(tccFromDtb);
        List<TestCaseCountry> tccToDeleteToIterate = new ArrayList(tccToDelete);
        tccToDeleteToIterate.removeAll(tcc);
        
        

        for (TestCaseCountry tccLeft : tccToUpdateOrInsertToIterate) {
            for (TestCaseCountry tccRight : tccFromDtb2) {
                if (tccLeft.hasSameKey(tccRight)) {
                    //tccService.updateTestCaseCountry(tccLeft);
                    tccToUpdateOrInsert.remove(tccLeft);
                }
            }
        }
        tccService.insertListTestCaseCountry(tccToUpdateOrInsert);

        for (TestCaseCountry tccLeft : tccToDeleteToIterate) {
            for (TestCaseCountry tccRight : tcc2) {
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
        
        List<TestCaseCountryProperties> tccpToUpdateOrInsert = new ArrayList(tccp);
        tccpToUpdateOrInsert.removeAll(tccpFromDatabase);
        List<TestCaseCountryProperties> tccpToUpdateOrInsertToIterate = new ArrayList(tccpToUpdateOrInsert);
        
        List<TestCaseCountryProperties> tccpToDelete = new ArrayList(tccpFromDatabase);
        tccpToDelete.removeAll(tccp);
        List<TestCaseCountryProperties> tccpToDeleteToIterate = new ArrayList(tccpToDelete);
        
        for (TestCaseCountryProperties tccpLeft : tccpToUpdateOrInsertToIterate) {
            for (TestCaseCountryProperties tccpRight : tccpFromDatabase) {
                if (tccpLeft.hasSameKey(tccpRight)) {
                    tccpService.updateTestCaseCountryProperties(tccpLeft);
                    tccpToUpdateOrInsert.remove(tccpLeft);
                }
            }
        }
        tccpService.insertListTestCaseCountryProperties(tccpToUpdateOrInsert);

        for (TestCaseCountryProperties tccpLeft : tccpToDeleteToIterate) {
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
        tcsToDelete.retainAll(tcs);

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
        tcsaToDelete.retainAll(tcsa);

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
        tcsacToDelete.retainAll(tcsac);

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

        response.sendRedirect(response.encodeRedirectURL("TestCase_1.jsp?Load=Load&Test=" + tc.getTest() + "&TestCase=" + tc.getTestCase()));

    }

    /**
     * Create new TestCase object from the information of request form
     *
     * @param request information from the request page
     * @return TestCase object
     * @see org.cerberus.entity.TestCase
     */
    private TCase getTestCaseFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase) {

        IFactoryTCase testCaseFactory = appContext.getBean(IFactoryTCase.class);
        String origin = request.getParameter("editOrigin");
        String refOrigin = request.getParameter("editRefOrigin");
        String creator = request.getParameter("editCreator");
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
        String function = request.getParameter("editFunction");
        return testCaseFactory.create(test, testCase, origin, refOrigin, creator, implementer, lastModifier, project, ticket, function, application,
                runQA, runUAT, runPROD, priority, group, status, shortDescription, description, howTo, active, fromSprint, fromRevision, toSprint,
                toRevision, null, bugID, targetSprint, targetRevision, comment, null, null, null, null);
    }

    private List<TestCaseCountry> getTestCaseCountryFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase) {
        IFactoryTestCaseCountry testCaseCountryFactory = appContext.getBean(IFactoryTestCaseCountry.class);
        List<TestCaseCountry> countries = new ArrayList<TestCaseCountry>();
        if (request.getParameterValues("editTestCaseCountry") != null) {
            for (String country : request.getParameterValues("editTestCaseCountry")) {
                countries.add(testCaseCountryFactory.create(test, testCase, country));
            }
        }
        return countries;
    }

    private String getParameterIfExists(HttpServletRequest request, String parameter) {
        String result = null;
        if (request.getParameter(parameter) != null) {
            result = request.getParameter(parameter);
        }
        return result;
    }

    private List<TestCaseCountryProperties> getTestCaseCountryPropertiesFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase) {
        List<TestCaseCountryProperties> testCaseCountryProp = new ArrayList();
        String[] testcase_properties_increment = request.getParameterValues("property_increment");
        IFactoryTestCaseCountryProperties testCaseCountryPropertiesFactory = appContext.getBean(IFactoryTestCaseCountryProperties.class);
        for (String inc : testcase_properties_increment) {
            String[] countries = null;
            if (request.getParameterValues("properties_country_" + inc) != null) {
            countries = request.getParameterValues("properties_country_" + inc);
            }
            String delete = getParameterIfExists(request, "properties_delete_" + inc);
            String property = getParameterIfExists(request, "properties_property_" + inc);
            String type = getParameterIfExists(request, "properties_type_" + inc);
            String value = getParameterIfExists(request, "properties_value1_" + inc);
            String value2 = getParameterIfExists(request, "properties_value2_" + inc);
            int length = Integer.valueOf(getParameterIfExists(request, "properties_length_" + inc));
            int rowLimit = Integer.valueOf(getParameterIfExists(request, "properties_rowlimit_" + inc));
            String nature = getParameterIfExists(request, "properties_nature_" + inc);
            String database = getParameterIfExists(request, "properties_dtb_" + inc);
            for (String country : countries) {
                if (delete == null) {
                    testCaseCountryProp.add(testCaseCountryPropertiesFactory.create(test, testCase, country, property, type, database, value, value2, length, rowLimit, nature));
                }
            }

        }
        return testCaseCountryProp;
    }

    private List<TestCaseStep> getTestCaseStepFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase) {
        List<TestCaseStep> testCaseStep = new ArrayList();
        String[] testcase_step_increment = null;
        if (request.getParameterValues("step_increment") != null) {
        testcase_step_increment = request.getParameterValues("step_increment");
        }
        IFactoryTestCaseStep testCaseStepFactory = appContext.getBean(IFactoryTestCaseStep.class);
        for (String inc : testcase_step_increment) {
            String delete = getParameterIfExists(request, "step_delete_" + inc);
            int step = Integer.valueOf(getParameterIfExists(request, "step_number_" + inc) == null ? "0" : getParameterIfExists(request, "step_number_" + inc));
            int initialStep = Integer.valueOf(getParameterIfExists(request, "initial_step_number_" + inc) == null ? "0" : getParameterIfExists(request, "initial_step_number_" + inc));
            String desc = getParameterIfExists(request, "step_description_" + inc);
            String useStep = getParameterIfExists(request, "step_useStep_" + inc);
            String useStepTest = getParameterIfExists(request, "step_useStepTest_" + inc);
            String useStepTestCase = getParameterIfExists(request, "step_useStepTestCase_" + inc);
            int useStepStep = Integer.valueOf(getParameterIfExists(request, "step_useStepStep_" + inc) == null ? "0" : getParameterIfExists(request, "step_useStepStep_" + inc));
            if (delete != null) {
                testCaseStep.add(testCaseStepFactory.create(test, testCase, step, desc, useStep, useStepTest, useStepTestCase, useStepStep));
            }
        }
        return testCaseStep;
    }

    private List<TestCaseStepAction> getTestCaseStepActionFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase) {
        List<TestCaseStepAction> testCaseStepAction = new ArrayList();
        String[] stepAction_increment = request.getParameterValues("action_increment");
        IFactoryTestCaseStepAction testCaseStepActionFactory = appContext.getBean(IFactoryTestCaseStepAction.class);
        for (String inc : stepAction_increment) {
            String delete = request.getParameter("action_delete_" + inc);
            int step = Integer.valueOf(request.getParameter("action_step_" + inc));
            int sequence = Integer.valueOf(request.getParameter("action_sequence_" + inc));
            String action = request.getParameter("action_action_" + inc);
            String object = request.getParameter("action_object_" + inc);
            String property = request.getParameter("action_property_" + inc);
            String description = request.getParameter("action_description_" + inc);
            if (delete != null) {
                testCaseStepAction.add(testCaseStepActionFactory.create(test, testCase, step, sequence, action, object, property, description));
            }
        }
        return testCaseStepAction;
    }

    private List<TestCaseStepActionControl> getTestCaseStepActionControlFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase) {
        List<TestCaseStepActionControl> testCaseStepActionControl = new ArrayList();
        String[] stepActionControl_increment = request.getParameterValues("control_increment");
        IFactoryTestCaseStepActionControl testCaseStepActionControlFactory = appContext.getBean(IFactoryTestCaseStepActionControl.class);
        for (String inc : stepActionControl_increment) {
            String delete = request.getParameter("control_delete_" + inc);
            int step = Integer.valueOf(request.getParameter("control_step_" + inc));
            int sequence = Integer.valueOf(request.getParameter("control_sequence_" + inc));
            int control = Integer.valueOf(request.getParameter("control_control_" + inc));
            String type = request.getParameter("control_type_" + inc);
            String controlValue = request.getParameter("control_value_" + inc);
            String controlProperty = request.getParameter("control_property_" + inc);
            String fatal = request.getParameter("control_fatal_" + inc);
            String description = request.getParameter("control_description_" + inc);
            if (delete != null) {
                testCaseStepActionControl.add(testCaseStepActionControlFactory.create(test, testCase, step, sequence, control, type, controlValue, controlProperty, fatal, description));
            }
        }
        return testCaseStepActionControl;
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
