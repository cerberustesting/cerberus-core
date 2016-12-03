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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.UserGroup;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.Test;
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.crud.entity.User;
import org.cerberus.crud.factory.IFactoryTestCaseCountry;
import org.cerberus.crud.factory.IFactoryTestCaseCountryProperties;
import org.cerberus.crud.factory.IFactoryTestCaseStep;
import org.cerberus.crud.factory.IFactoryTestCaseStepAction;
import org.cerberus.crud.factory.IFactoryTestCaseStepActionControl;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.crud.service.ITestCaseCountryService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.ITestCaseStepActionControlService;
import org.cerberus.crud.service.ITestCaseStepActionService;
import org.cerberus.crud.service.ITestCaseStepService;
import org.cerberus.crud.service.ITestService;
import org.cerberus.crud.service.IUserService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.crud.service.impl.TestCaseStepActionControlService;
import org.cerberus.crud.service.impl.TestCaseStepActionService;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.AnswerList;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.cerberus.crud.factory.IFactoryTestCase;
import org.cerberus.crud.service.IUserGroupService;

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
        TestCase tc = getTestCaseFromParameter(request, appContext, test, testCase);
        boolean duplicate = false;

        ITestService tService = appContext.getBean(ITestService.class);
        ITestCaseService tcService = appContext.getBean(ITestCaseService.class);
        ITestCaseCountryService tccService = appContext.getBean(ITestCaseCountryService.class);
        ITestCaseCountryPropertiesService tccpService = appContext.getBean(ITestCaseCountryPropertiesService.class);
        ITestCaseStepService tcsService = appContext.getBean(ITestCaseStepService.class);
        ITestCaseStepActionService tcsaService = appContext.getBean(ITestCaseStepActionService.class);
        ITestCaseStepActionControlService tcsacService = appContext.getBean(ITestCaseStepActionControlService.class);
        IInvariantService invariantService = appContext.getBean(IInvariantService.class);
        IUserService userService = appContext.getBean(IUserService.class);
        IUserGroupService userGroupService = appContext.getBean(IUserGroupService.class);

        /**
         * Get User and Groups of this user
         */
        User user = userService.findUserByKey(request.getUserPrincipal().getName());
//        List<UserGroup> userGroupList = groupService.findGroupByUser(user);
        List<UserGroup> userGroupList = userGroupService.convert(userGroupService.readByUser(user.getLogin()));
        List<String> groupList = new ArrayList();
        for (UserGroup group : userGroupList) {
            groupList.add(group.getGroup());
        }

        /**
         * Verify the Test is the same than initialTest If it is the same > Do
         * nothing If it is not the same > Verify if test already exists If not
         * exist > create it If exist > do nothing
         */
        if (!tc.getTest().equals(initialTest)) {
            if (tService.findTestByKey(tc.getTest()) == null) {
                if (groupList.contains("TestAdmin")) {
                    Test newTest = tService.findTestByKey(initialTest);
                    newTest.setTest(tc.getTest());
                    tService.createTest(newTest);
                } else {
                    response.sendError(403, MessageGeneralEnum.GUI_TEST_CREATION_NOT_HAVE_RIGHT.getDescription());
                    return;
                }
            }
        }

        if (!tc.getTest().equals(initialTest) || !tc.getTestCase().equals(initialTestCase)) {
            duplicate = true;
        }

        /**
         * If the testcase is a duplication, set the creator as the one which
         * duplicate the testcase and the status in the initial one.
         */
        if (duplicate) {
            tc.setUsrCreated(user.getLogin());
            AnswerList answer = invariantService.readByIdname("TCSTATUS"); //TODO: handle if the response does not turn ok
            tc.setStatus(((List<Invariant>) answer.getDataList()).get(0).getValue());
        }

        /**
         * If not duplicate and test in Working status and user with no admin
         * right, raise an error
         */
        if (!duplicate && "WORKING".equals(tc.getStatus()) && !groupList.contains("TestAdmin")) {
            response.sendError(403, MessageGeneralEnum.GUI_TESTCASE_NON_ADMIN_SAVE_WORKING_TESTCASE.getDescription());
            return;
        }

        /**
         * Verify testcase is the same than initialTestCase If it is the same >
         * update If it is not the same, > verify if testcase already exist If
         * it already exist > Send Error If it do not already exists > Create it
         */
        if (!duplicate) {
            tcService.updateTestCase(tc);
        } else if (tcService.findTestCaseByKey(tc.getTest(), tc.getTestCase()) != null) {
            response.sendError(403, MessageGeneralEnum.GUI_TESTCASE_DUPLICATION_ALREADY_EXISTS.getDescription());
            return;
        } else {
            tcService.createTestCase(tc);
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
        if (!duplicate) {
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
        }
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
        if (!duplicate) {
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
        }

        /*
         * Get steps, actions and controls from page by:
         * - generating a new step, action or control number,
         * - setting the correct related step and action for action or control
         */
        List<TestCaseStep> tcsFromPage = getTestCaseStepFromParameter(request, appContext, test, testCase, duplicate);
        List<TestCaseStepAction> tcsaFromPage = new ArrayList();
        List<TestCaseStepActionControl> tcsacFromPage = new ArrayList();

        int nextStepNumber = getMaxStepNumber(tcsFromPage);
        for (TestCaseStep tcs : tcsFromPage) {
            if (tcs.getStep() == -1) {
                tcs.setStep(++nextStepNumber);
            }

            if (tcs.getTestCaseStepAction() != null) {
                int nextSequenceNumber = getMaxSequenceNumber(tcs.getTestCaseStepAction());
                for (TestCaseStepAction tcsa : tcs.getTestCaseStepAction()) {
                    if (tcsa.getSequence() == -1) {
                        tcsa.setSequence(++nextSequenceNumber);
                    }
                    tcsa.setStep(tcs.getStep());

                    if (tcsa.getTestCaseStepActionControl() != null) {
                        int nextControlNumber = getMaxControlNumber(tcsa.getTestCaseStepActionControl());
                        for (TestCaseStepActionControl tscac : tcsa.getTestCaseStepActionControl()) {
                            if (tscac.getControlSequence() == -1) {
                                tscac.setControlSequence(++nextControlNumber);
                            }
                            tscac.setStep(tcs.getStep());
                            tscac.setSequence(tcsa.getSequence());
                        }
                        tcsacFromPage.addAll(tcsa.getTestCaseStepActionControl());
                    }
                }
                tcsaFromPage.addAll(tcs.getTestCaseStepAction());
            }
        }

        /*
         * Create, update or delete step, action and control according to the needs
         */
        List<TestCaseStep> tcsFromDtb = new ArrayList(tcsService.getListOfSteps(initialTest, initialTestCase));
        tcsService.compareListAndUpdateInsertDeleteElements(tcsFromPage, tcsFromDtb, duplicate);

        List<TestCaseStepAction> tcsaFromDtb = new ArrayList(tcsaService.findTestCaseStepActionbyTestTestCase(initialTest, initialTestCase));
        tcsaService.compareListAndUpdateInsertDeleteElements(tcsaFromPage, tcsaFromDtb, duplicate);

        List<TestCaseStepActionControl> tcsacFromDtb = new ArrayList(tcsacService.findControlByTestTestCase(initialTest, initialTestCase));
        tcsacService.compareListAndUpdateInsertDeleteElements(tcsacFromPage, tcsacFromDtb, duplicate);

        /**
         * Adding Log entry.
         */
        ILogEventService logEventService = appContext.getBean(LogEventService.class);
        logEventService.createPrivateCalls("/UpdateTestCase", "UPDATE", "Update testcase : ['" + tc.getTest() + "'|'" + tc.getTestCase() + "']", request);

        String encodedTest = URLEncoder.encode(tc.getTest(), "UTF-8");
        String encodedTestCase = URLEncoder.encode(tc.getTestCase(), "UTF-8");
        response.sendRedirect(response.encodeRedirectURL("TestCase.jsp?Load=Load&Test=" + encodedTest + "&TestCase=" + encodedTestCase));

    }

    /**
     * Get the highest step number from the given steps
     *
     * @param steps a collection of steps from which get the highest step number
     * @return the highest step number from the given steps
     */
    private int getMaxStepNumber(Collection<TestCaseStep> steps) {
        int nextStepNumber = 0;
        if (steps != null) {
            for (TestCaseStep step : steps) {
                if (nextStepNumber < step.getStep()) {
                    nextStepNumber = step.getStep();
                }
            }
        }
        return nextStepNumber;
    }

    /**
     * Get the highest action sequence from the given actions
     *
     * @param steps a collection of actions from which get the highest action
     * sequence
     * @return the highest action sequence from the given actions
     */
    private int getMaxSequenceNumber(Collection<TestCaseStepAction> actions) {
        int nextSequenceNumber = 0;
        if (actions != null) {
            for (TestCaseStepAction action : actions) {
                if (nextSequenceNumber < action.getSequence()) {
                    nextSequenceNumber = action.getSequence();
                }
            }
        }
        return nextSequenceNumber;
    }

    /**
     * Get the highest control number from the given controls
     *
     * @param controls a collection of controls from which get the highest
     * control number
     * @return the highest control number from the given controls
     */
    private int getMaxControlNumber(Collection<TestCaseStepActionControl> controls) {
        int nextControlNumber = 0;
        if (controls != null) {
            for (TestCaseStepActionControl control : controls) {
                if (nextControlNumber < control.getControlSequence()) {
                    nextControlNumber = control.getControlSequence();
                }
            }
        }
        return nextControlNumber;
    }

    /**
     * Create new TestCase object from the information of request form
     *
     * @param request information from the request page
     * @return TestCase object
     * @see org.cerberus.crud.entity.TestCase
     */
    private TestCase getTestCaseFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase) {

        IFactoryTestCase testCaseFactory = appContext.getBean(IFactoryTestCase.class);
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
        String conditionOper = request.getParameter("editConditionOper");
        String conditionVal1 = request.getParameter("editConditionVal1");
        String fromSprint = request.getParameter("editFromBuild");
        String fromRevision = request.getParameter("editFromRev");
        String toSprint = request.getParameter("editToBuild");
        String toRevision = request.getParameter("editToRev");
        String bugID = request.getParameter("editBugID");
        String targetSprint = request.getParameter("editTargetBuild");
        String targetRevision = request.getParameter("editTargetRev");
        String comment = request.getParameter("editComment");
        String function = request.getParameter("editFunction");
        String userAgent = request.getParameter("editUserAgent");
        return testCaseFactory.create(test, testCase, origin, refOrigin, creator, implementer, lastModifier, project, ticket, function, application,
                runQA, runUAT, runPROD, priority, group, status, shortDescription, description, howTo, active, conditionOper, conditionVal1, fromSprint, fromRevision, toSprint,
                toRevision, null, bugID, targetSprint, targetRevision, comment, userAgent, null, null, null, null);
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

    /**
     * FIXME this method do nothing more than return
     * request.getParameter(parameter)!
     */
    private String getParameterIfExists(HttpServletRequest request, String parameter) {
        String result = null;
        if (request.getParameter(parameter) != null) {
            result = request.getParameter(parameter);
        }
        return result;
    }

    /**
     * FIXME this method do nothing more than return
     * request.getParameterValues(parameter)!
     */
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
                String description = getParameterIfExists(request, "properties_description_" + inc);
                String type = getParameterIfExists(request, "properties_type_" + inc);
                String value = getParameterIfExists(request, "properties_value1_" + inc);
                String value2 = getParameterIfExists(request, "properties_value2_" + inc);
                int length = Integer.valueOf(getParameterIfExists(request, "properties_length_" + inc).equals("") ? "0" : getParameterIfExists(request, "properties_length_" + inc));
                int rowLimit = Integer.valueOf(getParameterIfExists(request, "properties_rowlimit_" + inc).equals("") ? "0" : getParameterIfExists(request, "properties_rowlimit_" + inc));
                int retryNb = Integer.valueOf(getParameterIfExists(request, "properties_retrynb_" + inc).equals("") ? "0" : getParameterIfExists(request, "properties_retrynb_" + inc));
                int retryPeriod = Integer.valueOf(getParameterIfExists(request, "properties_retryperiod_" + inc).equals("") ? "0" : getParameterIfExists(request, "properties_retryperiod_" + inc));
                String nature = getParameterIfExists(request, "properties_nature_" + inc);
                String database = getParameterIfExists(request, "properties_dtb_" + inc);
                if (countries != null) {
                    for (String country : countries) {
                        if (delete == null && property != null && !property.equals("")) {
                            testCaseCountryProp.add(testCaseCountryPropertiesFactory.create(test, testCase, country, property, description, type, database, value, value2, length, rowLimit, nature, retryNb, retryPeriod));
                        }
                    }
                }
            }
        }
        return testCaseCountryProp;
    }

    private List<TestCaseStep> getTestCaseStepFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, boolean duplicate) {
        List<TestCaseStep> testCaseStep = new ArrayList();
        ITestCaseStepService tcsService = appContext.getBean(ITestCaseStepService.class);
        String[] testcase_step_increment = getParameterValuesIfExists(request, "step_increment");
        IFactoryTestCaseStep testCaseStepFactory = appContext.getBean(IFactoryTestCaseStep.class);
        if (testcase_step_increment != null) {
            for (String inc : testcase_step_increment) {
                String delete = getParameterIfExists(request, "step_delete_" + inc);
                String stepInUse = getParameterIfExists(request, "step_InUseInOtherTestCase_" + inc);
                int step = Integer.valueOf(getParameterIfExists(request, "step_technical_number_" + inc) == null ? "0" : getParameterIfExists(request, "step_technical_number_" + inc));
                int sort = Integer.valueOf(getParameterIfExists(request, "step_number_" + inc) == null ? "0" : getParameterIfExists(request, "step_number_" + inc));
                int initialStep = Integer.valueOf(getParameterIfExists(request, "initial_step_number_" + inc) == null ? "0" : getParameterIfExists(request, "initial_step_number_" + inc));
                String conditionOper = getParameterIfExists(request, "step_conditionoper_" + inc);
                String conditionVal1 = getParameterIfExists(request, "step_conditionval1_" + inc);
                
                String desc = getParameterIfExists(request, "step_description_" + inc);
                String useStep = getParameterIfExists(request, "step_useStep_" + inc);
                String useStepChanged = getParameterIfExists(request, "step_useStepChanged_" + inc);
                String useStepTest = getParameterIfExists(request, "step_useStepTest_" + inc) == null ? "" : getParameterIfExists(request, "step_useStepTest_" + inc);
                String useStepTestCase = getParameterIfExists(request, "step_useStepTestCase_" + inc) == null ? "" : getParameterIfExists(request, "step_useStepTestCase_" + inc);
                String stepValue = getParameterIfExists(request, "step_useStepStep_" + inc);
                int useStepStep = Integer.valueOf(stepValue == null || stepValue.equals("") ? "-1" : getParameterIfExists(request, "step_useStepStep_" + inc));
                String inLibrary = getParameterIfExists(request, "step_inLibrary_" + inc);
                /* If delete, don't add it to the list of steps */
                if (delete == null) {
                    TestCaseStep tcStep = testCaseStepFactory.create(test, testCase, step, sort, conditionOper, conditionVal1, desc, useStep == null ? "N" : useStep, useStepTest, useStepTestCase, useStepStep, inLibrary == null ? "N" : inLibrary);
                    /* Take action and control only if not use step*/
                    if (useStep == null || useStep.equals("N")) {
                        String isToCopySteps = getParameterIfExists(request, "isToCopySteps_" + inc);
                        if (isToCopySteps != null && isToCopySteps.equals("Y")) {
                            // TODO:FN the information about the useStep should be cleared?
                            //tcStep.setTestCaseStepAction(tcsService.findTestCaseStep(useStepTest, useStepTestCase, useStepStep));
                            ITestCaseStepActionService tcsaService = appContext.getBean(TestCaseStepActionService.class);
                            ITestCaseStepActionControlService tcsacService = appContext.getBean(TestCaseStepActionControlService.class);
                            int stepNumber = Integer.parseInt(stepValue);
                            List<TestCaseStepAction> actions = tcsaService.getListOfAction(useStepTest, useStepTestCase, stepNumber);
                            for (TestCaseStepAction act : actions) {
                                List<TestCaseStepActionControl> controlsPerAction = tcsacService.findControlByTestTestCaseStepSequence(useStepTest,
                                        useStepTestCase, stepNumber, act.getSequence());

                                //these actions now belong to the current test case, therefore we need to change it
                                act.setTest(test);
                                act.setTestCase(testCase);
                                act.setStep(step);
                                List<TestCaseStepActionControl> updatedControlsPerAction = new ArrayList<TestCaseStepActionControl>();
                                for (TestCaseStepActionControl ctrl : controlsPerAction) {
                                    ctrl.setTest(test);
                                    ctrl.setTestCase(testCase);
                                    ctrl.setStep(step);
                                    updatedControlsPerAction.add(ctrl);
                                }
                                act.setTestCaseStepActionControl(updatedControlsPerAction);
                            }

                            tcStep.setTestCaseStepAction(actions);
                        } else {

                            tcStep.setTestCaseStepAction(getTestCaseStepActionFromParameter(request, appContext, test, testCase, inc));
                        }

                        //clears the information about the usestep
                        tcStep.setUseStep("N");
                        tcStep.setUseStepTest("");
                        tcStep.setUseStepTestCase("");
                        tcStep.setUseStepStep(-1);

                        //updates the test step action list with the new step
                        List<TestCaseStepAction> actionsForStep = tcStep.getTestCaseStepAction();
                        for (TestCaseStepAction ac : actionsForStep) {
                            List<TestCaseStepActionControl> actionControlList = ac.getTestCaseStepActionControl();
                            for (TestCaseStepActionControl acControl : actionControlList) {
                                acControl.setStep(step);
                            }
                            ac.setStep(step);
                        }
                        //update the step associated with the actions that are now the new actions
                    } else {
                        TestCaseStep tcs = null;
                        if (useStepStep != -1 && !useStepTest.equals("") && !useStepTestCase.equals("")) {
                            /* If use step, verify if used step alread use another one */
                            if ((useStepChanged != null && useStepChanged.equals("Y")) || duplicate) { //if it is to duplicate then we need to get the new step information                                                      
                                //save the information about the test + testcase + step that was used to duplicate the test
                                tcs = tcsService.findTestCaseStep(useStepTest, useStepTestCase, useStepStep);
                                if (tcs != null) {
                                    tcStep.setUseStepTest(tcs.getTest());
                                    tcStep.setUseStepTestCase(tcs.getTestCase());
                                    tcStep.setUseStepStep(tcs.getStep());
                                }
                            } else {
                                //if there was no changes then it uses the same information
                                tcs = tcsService.findTestCaseStep(test, testCase, step);
                                if (tcs != null) {
                                    tcStep.setUseStepTest(tcs.getUseStepTest());
                                    tcStep.setUseStepTestCase(tcs.getUseStepTestCase());
                                    tcStep.setUseStepStep(tcs.getUseStepStep());
                                }
                            }

                        } else {
                            //does not defines a valid step, then keep as it was before
                            tcs = tcsService.findTestCaseStep(test, testCase, step);
                            if (tcs != null) {
                                tcStep.setUseStep("N");
                                tcStep.setUseStepTest(tcs.getUseStepTest());
                                tcStep.setUseStepTestCase(tcs.getUseStepTestCase());
                                tcStep.setUseStepStep(tcs.getUseStepStep());
                                tcStep.setTestCaseStepAction(getTestCaseStepActionFromParameter(request, appContext, test, testCase, inc));

                                List<TestCaseStepAction> actionsForStep = tcStep.getTestCaseStepAction();
                                for (TestCaseStepAction ac : actionsForStep) {
                                    List<TestCaseStepActionControl> actionControlList = ac.getTestCaseStepActionControl();
                                    for (TestCaseStepActionControl acControl : actionControlList) {
                                        acControl.setStep(step);
                                    }
                                    ac.setStep(step);
                                }
                            }
                        }

                        if (tcs != null) {
                            /**
                             * If description is empty, take the one from the
                             * use step
                             */
                            if (desc.equals("")) {
                                tcStep.setDescription(tcs.getDescription());
                            }
                        } else {
                            //if the step that should be defined is not imported then we should save the data
                            //and the test case should have the useStep = 'N'
                            tcStep.setUseStep("N");
                        }
                    }
                    if (stepInUse != null && stepInUse.equals("Y")) {
                        tcStep.setIsStepInUseByOtherTestCase(true);
                        tcStep.setInitialStep(initialStep);
                    } else {
                        tcStep.setIsStepInUseByOtherTestCase(false);
                    }

                    testCaseStep.add(tcStep);
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
                int sequence = Integer.valueOf(getParameterIfExists(request, "action_technical_sequence_" + stepInc + "_" + inc) == null ? "0" : getParameterIfExists(request, "action_technical_sequence_" + stepInc + "_" + inc));
                int sort = Integer.valueOf(getParameterIfExists(request, "action_sequence_" + stepInc + "_" + inc) == null ? "0" : getParameterIfExists(request, "action_sequence_" + stepInc + "_" + inc));
                String conditionOper = getParameterIfExists(request, "action_conditionoper_" + stepInc + "_" + inc);
                String conditionVal = getParameterIfExists(request, "action_conditionval_" + stepInc + "_" + inc);
                String action = getParameterIfExists(request, "action_action_" + stepInc + "_" + inc);
                String object = getParameterIfExists(request, "action_object_" + stepInc + "_" + inc).replaceAll("\"", "\\\"");
                String property = getParameterIfExists(request, "action_property_" + stepInc + "_" + inc);
                String forceExeStatus = getParameterIfExists(request, "action_forceexestatus_" + stepInc + "_" + inc);
                String description = getParameterIfExists(request, "action_description_" + stepInc + "_" + inc);
                String screenshot = getParameterIfExists(request, "action_screenshot_" + stepInc + "_" + inc);
                if (delete == null) {
                    TestCaseStepAction tcsa = testCaseStepActionFactory.create(test, testCase, -1, sequence, sort, conditionOper, conditionVal, action, object, property, forceExeStatus, description, screenshot);
                    tcsa.setTestCaseStepActionControl(getTestCaseStepActionControlFromParameter(request, appContext, test, testCase, stepInc, inc));
                    testCaseStepAction.add(tcsa);
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
                int controlsequence = Integer.valueOf(getParameterIfExists(request, "control_technical_control_" + stepInc + "_" + actionInc + "_" + inc) == null ? "0" : getParameterIfExists(request, "control_technical_control_" + stepInc + "_" + actionInc + "_" + inc));
                int sort = Integer.valueOf(getParameterIfExists(request, "control_controlsequence_" + stepInc + "_" + actionInc + "_" + inc) == null ? "0" : getParameterIfExists(request, "control_controlsequence_" + stepInc + "_" + actionInc + "_" + inc));
                String conditionOper = getParameterIfExists(request, "control_conditionoper_" + stepInc + "_" + actionInc + "_" + inc);
                String conditionVal1 = getParameterIfExists(request, "control_conditionval1_" + stepInc + "_" + actionInc + "_" + inc).replaceAll("\"", "\\\"");
                String control = getParameterIfExists(request, "control_control_" + stepInc + "_" + actionInc + "_" + inc);
                String value1 = getParameterIfExists(request, "control_value1_" + stepInc + "_" + actionInc + "_" + inc).replaceAll("\"", "\\\"");
                String value2 = getParameterIfExists(request, "control_value2_" + stepInc + "_" + actionInc + "_" + inc).replaceAll("\"", "\\\"");
                String fatal = getParameterIfExists(request, "control_fatal_" + stepInc + "_" + actionInc + "_" + inc);
                String description = getParameterIfExists(request, "control_description_" + stepInc + "_" + actionInc + "_" + inc);
                String screenshot = getParameterIfExists(request, "control_screenshot_" + stepInc + "_" + actionInc + "_" + inc);
                if (delete == null) {
                    testCaseStepActionControl.add(testCaseStepActionControlFactory.create(test, testCase, -1, -1, controlsequence, sort, conditionOper, conditionVal1, control, value1, value2, fatal, description, screenshot));
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
