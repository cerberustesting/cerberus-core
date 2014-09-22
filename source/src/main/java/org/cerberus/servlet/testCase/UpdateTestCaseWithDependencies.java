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
import org.springframework.web.util.HtmlUtils;

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
                Test newTest = tService.findTestByKey(initialTest);
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
            if (tcService.findTestCaseByKey(tc.getTest(), tc.getTestCase()) != null) {
                response.sendError(403, MessageGeneralEnum.GUI_TESTCASE_DUPLICATION_ALREADY_EXISTS.getDescription());
                return;
            } else {
                tcService.createTestCase(tc);
            }
        }

        /**
         * For the list of testcase country verify it exists. If it does not
         * exists > create it If it exist, verify if it's the
         */
        List<TestCaseCountry> tccFromPage = getTestCaseCountryFromParameter(request, appContext, test, testCase);
        List<TestCaseCountry> tccFromDtb = tccService.findTestCaseCountryByTestTestCase(initialTest, initialTestCase);

        /**
         * Iterate on (TestCaseCountry From Page - TestCaseCountry From
         * Database) If TestCaseCountry in Database has same key : Update and
         * remove from the list. If TestCaseCountry in database does ot exist :
         * Insert it.
         */
        List<TestCaseCountry> tccToUpdateOrInsert = new ArrayList(tccFromPage);
        tccToUpdateOrInsert.removeAll(tccFromDtb);
        List<TestCaseCountry> tccToUpdateOrInsertToIterate = new ArrayList(tccToUpdateOrInsert);

        for (TestCaseCountry tccDifference : tccToUpdateOrInsertToIterate) {
            for (TestCaseCountry tccInDatabase : tccFromDtb) {
                if (tccDifference.hasSameKey(tccInDatabase)) {
                    tccToUpdateOrInsert.remove(tccDifference);
                }
            }
        }
        tccService.insertListTestCaseCountry(tccToUpdateOrInsert);

        /**
         * Iterate on (TestCaseCountry From Database - TestCaseCountry From
         * Page). If TestCaseCountry in Page has same key : remove from the
         * list. Then delete the list of TestCaseCountry
         */
        List<TestCaseCountry> tccToDelete = new ArrayList(tccFromDtb);
        tccToDelete.removeAll(tccFromPage);
        List<TestCaseCountry> tccToDeleteToIterate = new ArrayList(tccToDelete);

        for (TestCaseCountry tccDifference : tccToDeleteToIterate) {
            for (TestCaseCountry tccInPage : tccFromPage) {
                if (tccDifference.hasSameKey(tccInPage)) {
                    tccToDelete.remove(tccDifference);
                }
            }
        }
        tccService.deleteListTestCaseCountry(tccToDelete);

        /**
         * For the list of testcase country verify it exists. If it does not
         * exists > create it If it exist, verify if it's the
         */
        List<TestCaseCountryProperties> tccpFromPage = getTestCaseCountryPropertiesFromParameter(request, appContext, test, testCase);
        List<TestCaseCountryProperties> tccpFromDtb = tccpService.findListOfPropertyPerTestTestCase(initialTest, initialTestCase);

        /**
         * Iterate on (TestCaseCountryProperties From Page -
         * TestCaseCountryProperties From Database) If TestCaseCountryProperties
         * in Database has same key : Update and remove from the list. If
         * TestCaseCountryProperties in database does ot exist : Insert it.
         */
        List<TestCaseCountryProperties> tccpToUpdateOrInsert = new ArrayList(tccpFromPage);
        tccpToUpdateOrInsert.removeAll(tccpFromDtb);
        List<TestCaseCountryProperties> tccpToUpdateOrInsertToIterate = new ArrayList(tccpToUpdateOrInsert);

        for (TestCaseCountryProperties tccpDifference : tccpToUpdateOrInsertToIterate) {
            for (TestCaseCountryProperties tccpInDatabase : tccpFromDtb) {
                if (tccpDifference.hasSameKey(tccpInDatabase)) {
                    tccpService.updateTestCaseCountryProperties(tccpDifference);
                    tccpToUpdateOrInsert.remove(tccpDifference);
                }
            }
        }
        tccpService.insertListTestCaseCountryProperties(tccpToUpdateOrInsert);

        /**
         * Iterate on (TestCaseCountryProperties From Database -
         * TestCaseCountryProperties From Page). If TestCaseCountryProperties in
         * Page has same key : remove from the list. Then delete the list of
         * TestCaseCountryProperties
         */
        List<TestCaseCountryProperties> tccpToDelete = new ArrayList(tccpFromDtb);
        tccpToDelete.removeAll(tccpFromPage);
        List<TestCaseCountryProperties> tccpToDeleteToIterate = new ArrayList(tccpToDelete);

        for (TestCaseCountryProperties tccpDifference : tccpToDeleteToIterate) {
            for (TestCaseCountryProperties tccpInPage : tccpFromPage) {
                if (tccpDifference.hasSameKey(tccpInPage)) {
                    tccpToDelete.remove(tccpDifference);
                }
            }
        }
        tccpService.deleteListTestCaseCountryProperties(tccpToDelete);

        /**
         * For the list of testcasestep verify it exists. If it does not exists
         * > create it If it exist, verify if it's the
         */
        List<TestCaseStep> tcsFromPage = getTestCaseStepFromParameter(request, appContext, test, testCase);
        List<TestCaseStepAction> tcsaFromPage = new ArrayList();
        List<TestCaseStepActionControl> tcsacFromPage = new ArrayList();

        for (TestCaseStep tcsL : tcsFromPage) {
            if (tcsL.getTestCaseStepAction() != null) {
                tcsaFromPage.addAll(tcsL.getTestCaseStepAction());
                for (TestCaseStepAction tcsaL : tcsL.getTestCaseStepAction()) {
                    tcsacFromPage.addAll(tcsaL.getTestCaseStepActionControl());
                }
            }
        }

        List<TestCaseStep> tcsFromDtb = new ArrayList(tcsService.getListOfSteps(initialTest, initialTestCase));
        tcsService.compareListAndUpdateInsertDeleteElements(tcsFromPage, tcsFromDtb);

        List<TestCaseStepAction> tcsaFromDtb = new ArrayList(tcsaService.findTestCaseStepActionbyTestTestCase(initialTest, initialTestCase));
        tcsaService.compareListAndUpdateInsertDeleteElements(tcsaFromPage, tcsaFromDtb);

        List<TestCaseStepActionControl> tcsacFromDtb = new ArrayList(tcsacService.findControlByTestTestCase(initialTest, initialTestCase));
        tcsacService.compareListAndUpdateInsertDeleteElements(tcsacFromPage, tcsacFromDtb);

        List<TestCaseStep> tcsNewFromPage = new ArrayList();
        List<TestCaseStepAction> tcsaNewFromPage = new ArrayList();
        List<TestCaseStepActionControl> tcsacNewFromPage = new ArrayList();
        List<TestCaseStep> tcsNewFromDtb = new ArrayList();
        List<TestCaseStepAction> tcsaNewFromDtb = new ArrayList();
        List<TestCaseStepActionControl> tcsacNewFromDtb = new ArrayList();

        tcsNewFromDtb = tcsService.getListOfSteps(initialTest, initialTestCase);
        int incrementStep = 0;
        for (TestCaseStep tcsNew : tcsNewFromDtb) {
            incrementStep++;
            tcsaNewFromDtb = tcsaService.getListOfAction(initialTest, initialTestCase, tcsNew.getStep());
            int incrementAction = 0;
            for (TestCaseStepAction tcsaNew : tcsaNewFromDtb) {
                incrementAction++;
                tcsacNewFromDtb = tcsacService.findControlByTestTestCaseStepSequence(initialTest, initialTestCase, tcsaNew.getStep(), tcsaNew.getSequence());
                int incrementControl = 0;
                for (TestCaseStepActionControl tcsacNew : tcsacNewFromDtb) {
                    incrementControl++;
                    tcsacNew.setControl(incrementControl);
                    tcsacNew.setSequence(incrementAction);
                    tcsacNew.setStep(incrementStep);
                    tcsacNewFromPage.add(tcsacNew);
                }
                tcsaNew.setSequence(incrementAction);
                tcsaNew.setStep(incrementStep);
                tcsaNewFromPage.add(tcsaNew);
            }
            tcsNew.setStep(incrementStep);
            tcsNewFromPage.add(tcsNew);
        }

        List<TestCaseStep> tcsNewNewFromDtb = new ArrayList(tcsService.getListOfSteps(initialTest, initialTestCase));
        tcsService.compareListAndUpdateInsertDeleteElements(tcsNewFromPage, tcsNewNewFromDtb);

        List<TestCaseStepAction> tcsaNewNewFromDtb = new ArrayList(tcsaService.findTestCaseStepActionbyTestTestCase(initialTest, initialTestCase));
        tcsaService.compareListAndUpdateInsertDeleteElements(tcsaNewFromPage, tcsaNewNewFromDtb);

        List<TestCaseStepActionControl> tcsacNewNewFromDtb = new ArrayList(tcsacService.findControlByTestTestCase(initialTest, initialTestCase));
        tcsacService.compareListAndUpdateInsertDeleteElements(tcsacNewFromPage, tcsacNewNewFromDtb);

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

        response.sendRedirect(response.encodeRedirectURL("TestCase.jsp?Load=Load&Test=" + tc.getTest() + "&TestCase=" + tc.getTestCase()));

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
        String shortDescription = HtmlUtils.htmlEscape(request.getParameter("editDescription"));
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
        String comment = HtmlUtils.htmlEscape(request.getParameter("editComment"));
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

    private String[] getParameterValuesIfExists(HttpServletRequest request, String parameter) {
        String[] result = null;
        if (request.getParameterValues(parameter) != null) {
            result = request.getParameterValues(parameter);
        }
        return result;
    }

    private List<TestCaseCountryProperties> getTestCaseCountryPropertiesFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase) {
        List<TestCaseCountryProperties> testCaseCountryProp = new ArrayList();
        String[] testcase_properties_increment = getParameterValuesIfExists(request, "property_increment");
        IFactoryTestCaseCountryProperties testCaseCountryPropertiesFactory = appContext.getBean(IFactoryTestCaseCountryProperties.class);
        if (testcase_properties_increment != null) {
            for (String inc : testcase_properties_increment) {
                String[] countries = getParameterValuesIfExists(request, "properties_country_" + inc);
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
        }
        return testCaseCountryProp;
    }

    private List<TestCaseStep> getTestCaseStepFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase) {
        List<TestCaseStep> testCaseStep = new ArrayList();
        ITestCaseStepService tcsService = appContext.getBean(ITestCaseStepService.class);
        String[] testcase_step_increment = getParameterValuesIfExists(request, "step_increment");
        IFactoryTestCaseStep testCaseStepFactory = appContext.getBean(IFactoryTestCaseStep.class);
        if (testcase_step_increment != null) {
            for (String inc : testcase_step_increment) {
                String delete = getParameterIfExists(request, "step_delete_" + inc);
                int step = Integer.valueOf(getParameterIfExists(request, "step_number_" + inc) == null ? "0" : getParameterIfExists(request, "step_number_" + inc));
                int initialStep = Integer.valueOf(getParameterIfExists(request, "initial_step_number_" + inc) == null ? "0" : getParameterIfExists(request, "initial_step_number_" + inc));
                String desc = HtmlUtils.htmlEscape(getParameterIfExists(request, "step_description_" + inc));
                String useStep = getParameterIfExists(request, "step_useStep_" + inc);
                String useStepTest = getParameterIfExists(request, "step_useStepTest_" + inc);
                String useStepTestCase = getParameterIfExists(request, "step_useStepTestCase_" + inc);
                int useStepStep = Integer.valueOf(getParameterIfExists(request, "step_useStepStep_" + inc) == null ? "0" : getParameterIfExists(request, "step_useStepStep_" + inc));
                /* If delete, don't add it to the list of steps */
                if (delete == null) {
                    TestCaseStep tcStep = testCaseStepFactory.create(test, testCase, step, desc, useStep, useStepTest, useStepTestCase, useStepStep);
                    /* Take action and control only if not use step*/
                    if (useStep == null) {
                        tcStep.setTestCaseStepAction(getTestCaseStepActionFromParameter(request, appContext, test, testCase, inc));
                    } else {
                        /* If use step, verify if used step alread use another one */
                        TestCaseStep tcs = tcsService.findTestCaseStep(useStepTest, useStepTestCase, useStepStep);
                        if (tcs != null && tcs.getUseStep().equals("Y")) {
                            tcStep.setUseStepTest(tcs.getUseStepTest());
                            tcStep.setUseStepTestCase(tcs.getUseStepTestCase());
                            tcStep.setUseStepStep(tcs.getUseStepStep());
                        }
                    }
                    testCaseStep.add(tcStep);
                    //System.out.print("FromPage" + tcStep.toString());
                }
            }
        }
        return testCaseStep;
    }

    private List<TestCaseStepAction> getTestCaseStepActionFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, String stepInc) {
        List<TestCaseStepAction> testCaseStepAction = new ArrayList();
        String[] stepAction_increment = getParameterValuesIfExists(request, "action_increment_" + stepInc);
        IFactoryTestCaseStepAction testCaseStepActionFactory = appContext.getBean(IFactoryTestCaseStepAction.class);
        if (stepAction_increment != null) {
            for (String inc : stepAction_increment) {
                String delete = getParameterIfExists(request, "action_delete_" + stepInc + "_" + inc);
                int step = Integer.valueOf(getParameterIfExists(request, "action_step_" + stepInc + "_" + inc) == null ? "0" : getParameterIfExists(request, "action_step_" + stepInc + "_" + inc));
                int sequence = Integer.valueOf(getParameterIfExists(request, "action_sequence_" + stepInc + "_" + inc) == null ? "0" : getParameterIfExists(request, "action_sequence_" + stepInc + "_" + inc));
                String action = getParameterIfExists(request, "action_action_" + stepInc + "_" + inc);
                String object = getParameterIfExists(request, "action_object_" + stepInc + "_" + inc);
                String property = getParameterIfExists(request, "action_property_" + stepInc + "_" + inc);
                String description = HtmlUtils.htmlEscape(getParameterIfExists(request, "action_description_" + stepInc + "_" + inc));
                if (delete == null) {
                    TestCaseStepAction tcsa = testCaseStepActionFactory.create(test, testCase, step, sequence, action, object, property, description);
                    tcsa.setTestCaseStepActionControl(getTestCaseStepActionControlFromParameter(request, appContext, test, testCase, stepInc, inc));
                    testCaseStepAction.add(tcsa);
                    //System.out.print("FromPage"+tcsa.toString());
                }
            }
        }
        return testCaseStepAction;
    }

    private List<TestCaseStepActionControl> getTestCaseStepActionControlFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, String stepInc, String actionInc) {
        List<TestCaseStepActionControl> testCaseStepActionControl = new ArrayList();
        String[] stepActionControl_increment = getParameterValuesIfExists(request, "control_increment_" + stepInc + "_" + actionInc);
        IFactoryTestCaseStepActionControl testCaseStepActionControlFactory = appContext.getBean(IFactoryTestCaseStepActionControl.class);
        if (stepActionControl_increment != null) {
            for (String inc : stepActionControl_increment) {
                String delete = getParameterIfExists(request, "control_delete_" + stepInc + "_" + actionInc + "_" + inc);
                int step = Integer.valueOf(getParameterIfExists(request, "control_step_" + stepInc + "_" + actionInc + "_" + inc) == null ? "0" : getParameterIfExists(request, "control_step_" + stepInc + "_" + actionInc + "_" + inc));
                int sequence = Integer.valueOf(getParameterIfExists(request, "control_sequence_" + stepInc + "_" + actionInc + "_" + inc) == null ? "0" : getParameterIfExists(request, "control_sequence_" + stepInc + "_" + actionInc + "_" + inc));
                int control = Integer.valueOf(getParameterIfExists(request, "control_control_" + stepInc + "_" + actionInc + "_" + inc) == null ? "0" : getParameterIfExists(request, "control_control_" + stepInc + "_" + actionInc + "_" + inc));
                String type = getParameterIfExists(request, "control_type_" + stepInc + "_" + actionInc + "_" + inc);
                String controlValue = getParameterIfExists(request, "control_value_" + stepInc + "_" + actionInc + "_" + inc);
                String controlProperty = getParameterIfExists(request, "control_property_" + stepInc + "_" + actionInc + "_" + inc);
                String fatal = getParameterIfExists(request, "control_fatal_" + stepInc + "_" + actionInc + "_" + inc);
                String description = HtmlUtils.htmlEscape(getParameterIfExists(request, "control_description_" + stepInc + "_" + actionInc + "_" + inc));
                if (delete == null) {
                    testCaseStepActionControl.add(testCaseStepActionControlFactory.create(test, testCase, step, sequence, control, type, controlValue, controlProperty, fatal, description));
                }
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
