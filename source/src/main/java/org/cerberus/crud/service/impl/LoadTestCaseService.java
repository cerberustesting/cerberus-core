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
package org.cerberus.crud.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.cerberus.crud.entity.TCase;
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.crud.factory.IFactoryTCase;
import org.cerberus.crud.factory.IFactoryTestCaseCountry;
import org.cerberus.crud.factory.IFactoryTestCaseStep;
import org.cerberus.log.MyLogger;
import org.cerberus.crud.service.ILoadTestCaseService;
import org.cerberus.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.crud.service.ITestCaseStepActionControlService;
import org.cerberus.crud.service.ITestCaseStepActionService;
import org.cerberus.crud.service.ITestCaseStepService;
import org.cerberus.engine.execution.impl.RunTestCaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class LoadTestCaseService implements ILoadTestCaseService {

    @Autowired
    private ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
    @Autowired
    private ITestCaseStepService testCaseStepService;
    @Autowired
    private ITestCaseStepActionService testCaseStepActionService;
    @Autowired
    private ITestCaseStepActionControlService testCaseStepActionControlService;
    @Autowired
    private IFactoryTestCaseCountry factoryTestCaseCountry;
    @Autowired
    private IFactoryTCase factoryTCase;
    @Autowired
    private IFactoryTestCaseStep factoryTCS;

    @Override
    public void loadTestCase(TestCaseExecution tCExecution) {

        TCase testCase = tCExecution.gettCase();

        String test = testCase.getTest();
        String testcase = testCase.getTestCase();

        List<TestCaseCountry> testCaseCountry = new ArrayList<TestCaseCountry>();
        List<TestCaseCountryProperties> testCaseCountryProperty = new ArrayList<TestCaseCountryProperties>();
        List<TestCaseStep> testCaseStep = new ArrayList<TestCaseStep>();
        List<TestCaseStep> PretestCaseStep = new ArrayList<TestCaseStep>();

        /**
         * Get List of PreTest for selected TestCase
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Loading pretests for " + tCExecution.getCountry() + tCExecution.gettCase().getApplication());
        List<String> login = this.testCaseStepService.getLoginStepFromTestCase(tCExecution.getCountry(), tCExecution.gettCase().getApplication());

        /**
         * Load Steps of PreTest
         */
        if (login != null) {
            for (String tsCase : login) {

                TestCaseCountry preTestCaseCountry = factoryTestCaseCountry.create("Pre Tests", tsCase, tCExecution.getCountry());
                preTestCaseCountry.setTestCaseCountryProperty(this.loadProperties(preTestCaseCountry));
                testCaseCountry.add(preTestCaseCountry);

                TCase preTestCase = factoryTCase.create("Pre Tests", tsCase);
                MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "add all pretest");
                PretestCaseStep.addAll(loadTestCaseStep(preTestCase));

            }
        }

        /**
         * Load Information of TestCase
         */
        TCase testCaseToAdd = factoryTCase.create(test, testcase);
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "add all step");
        testCaseStep.addAll(loadTestCaseStep(testCaseToAdd));
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "search all countryprop");
        List<TestCaseCountryProperties> testCaseCountryPropertyToAdd = this.testCaseCountryPropertiesService.findListOfPropertyPerTestTestCaseCountry(test, testcase, tCExecution.getCountry());
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "add all countryprop");
        if (testCaseCountryPropertyToAdd != null) {
            testCaseCountryProperty.addAll(testCaseCountryPropertyToAdd);
        }
        /**
         * Set Execution Object
         */
        testCase.setTestCaseCountry(testCaseCountry);
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "set testcasestep");
        testCase.setTestCaseStep(testCaseStep);
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "setTestCaseCountryProperties");
        testCase.setTestCaseCountryProperties(testCaseCountryProperty);
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "settCase");
        tCExecution.settCase(testCase);
    }

    //@Override
    public List<TestCaseCountryProperties> loadProperties(TestCaseCountry testCaseCountry) {

        return this.testCaseCountryPropertiesService.findListOfPropertyPerTestTestCaseCountry(
                testCaseCountry.getTest(), testCaseCountry.getTestCase(),
                testCaseCountry.getCountry());
    }

    @Override
    public List<TestCaseStep> loadTestCaseStep(TCase testCase) {
        List<TestCaseStep> result = new ArrayList<TestCaseStep>();
        for (TestCaseStep testCaseStep : this.testCaseStepService.getListOfSteps(testCase.getTest(), testCase.getTestCase())) {
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "set list of step :" + testCaseStep.getStep());

            /**
             * If use Step, load action and control of used step
             */
            if (!testCaseStep.getUseStep().equals("Y")) {
                List<TestCaseStepAction> tcsa = this.loadTestCaseStepAction(testCaseStep, null);
                if (tcsa != null) {
                    testCaseStep.setTestCaseStepAction(tcsa);
                }
            } else {
                List<TestCaseStepAction> tcsa = this.loadTestCaseStepAction(testCaseStep, factoryTCS.create(testCaseStep.getUseStepTest(), testCaseStep.getUseStepTestCase(), testCaseStep.getUseStepStep(), null, null, null, null, 0, null));
                if (tcsa != null) {
                    testCaseStep.setTestCaseStepAction(tcsa);
                }
            }
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "adding testCaseStep");
            result.add(testCaseStep);
        }

        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "return List<TestCaseStep>");
        return result;
    }

    public List<TestCaseStepAction> loadTestCaseStepAction(TestCaseStep testCaseStep, TestCaseStep UsedTestCaseStep) {
        List<TestCaseStepAction> result = new ArrayList<TestCaseStepAction>();
        List<TestCaseStepAction> tcsaToAdd;
        /**
         * If use Step, take the List of action and control of the used step
         */
        boolean useStep = (UsedTestCaseStep != null);
        if (!useStep) {
            tcsaToAdd = this.testCaseStepActionService.getListOfAction(
                    testCaseStep.getTest(), testCaseStep.getTestCase(), testCaseStep.getStep());
        } else {
            tcsaToAdd = this.testCaseStepActionService.getListOfAction(
                    UsedTestCaseStep.getTest(), UsedTestCaseStep.getTestCase(), UsedTestCaseStep.getStep());
        }

        /**
         * Iterate on the list of action to get the control
         * In case of useStep, print the test,testcase,step of the executed test instead of the used step
         */
        for (TestCaseStepAction testCaseStepAction : tcsaToAdd) {
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "set list of action :" + testCaseStepAction.getAction());

            /**
             * 
             */
            List<TestCaseStepActionControl> tcsacList = this.loadTestCaseStepActionControl(testCaseStep,testCaseStepAction);
            if (tcsacList != null) {
                testCaseStepAction.setTestCaseStepActionControl(tcsacList);
            }
            /**
             * Update the test, Testcase, Step in case of useStep
             */
            testCaseStepAction.setTest(testCaseStep.getTest());
            testCaseStepAction.setTestCase(testCaseStep.getTestCase());
            testCaseStepAction.setStep(testCaseStep.getStep());
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "adding testCaseStepAction" + testCaseStepAction.getAction());
            result.add(testCaseStepAction);
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "added testCaseStepAction" + testCaseStepAction.getAction());

        }
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "return List<TestCaseStepAction>");
        return result;
    }

    public List<TestCaseStepActionControl> loadTestCaseStepActionControl(TestCaseStep testCaseStep, TestCaseStepAction testCaseAction) {
        List<TestCaseStepActionControl> result = new ArrayList<TestCaseStepActionControl>();
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "get list of control");
        List<TestCaseStepActionControl> controlList = testCaseStepActionControlService.findControlByTestTestCaseStepSequence(testCaseAction.getTest(), testCaseAction.getTestCase(), testCaseAction.getStep(), testCaseAction.getSequence());
        if (controlList != null) {
            for (TestCaseStepActionControl testCaseStepActionControl : controlList) {
                MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "set control :" + testCaseStepActionControl.getType());
                testCaseStepActionControl.setTest(testCaseStep.getTest());
                testCaseStepActionControl.setTestCase(testCaseStep.getTestCase());
                testCaseStepActionControl.setStep(testCaseStep.getStep());
                result.add(testCaseStepActionControl);
            }
        }
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "return List<TestCaseStepActionControl>");
        return result;
    }
//    @Override
//    public List<TestCaseStep> loadAllStepSequences(String test, String testCase) {
//        List<TestCaseStep> steps = new ArrayList<TestCaseStep>();
//
//        for (TestCaseStep testCaseStep : this.testCaseStepService.getListOfSteps(test, testCase)) {
//            List<TestCaseStepAction> actions = testCaseStepActionService.getListOfAction(test, testCase, testCaseStep.getStep());
//            List<TestCaseStepActionControl> controls = this.testCaseStepActionControlService.findControlByTestTestCaseStep(test, testCase, testCaseStep.getStep());
//            step.setSequences(this.mergeActionControl(actions, controls));
//            for (String batch : testCaseStepBatchDAO.getBatchFromStep(test, testCase, step.getNumber())) {
//                switch (batch) {
//                    case 'D':
//                        step.setDailyChain(true);
//                        break;
//
//                    case 'F':
//                        step.setFastChain(true);
//                        break;
//
//                    case 'M':
//                        step.setMorningChain(true);
//                        break;
//
//                    default:
//                        break;
//                }
//            }
//            step.setMessageResult(Message.GENERIC_OK);
//            steps.add(step);
//        }
//        return steps;
//    }
//
//    private List<Sequence> mergeActionControl(List<Action> actions, List<Control> controls) {
//        List<Sequence> sequences = new ArrayList<Sequence>();
//
//        for (Action action : actions) {
//            sequences.add(action);
//            for (Control control : controls) {
//                if (action.getSequence() == control.getSequence()) {
//                    sequences.add(control);
//                    controls.remove(control);
//                    break;
//                }
//            }
//        }
//
//        return sequences;
//    }
}
