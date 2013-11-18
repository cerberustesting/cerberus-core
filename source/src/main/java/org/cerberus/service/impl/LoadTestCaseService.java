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
package org.cerberus.service.impl;

import org.cerberus.service.ILoadTestCaseService;
import org.cerberus.service.ITestCaseCountryPropertiesService;
import org.cerberus.service.ITestCaseStepActionControlService;
import org.cerberus.service.ITestCaseStepService;
import org.cerberus.service.ITestCaseStepActionService;
import org.cerberus.entity.TCExecution;
import org.cerberus.entity.TestCaseStepAction;
import org.cerberus.entity.TestCaseCountryProperties;
import org.cerberus.entity.TestCaseStepActionControl;
import org.cerberus.entity.TCase;
import org.cerberus.entity.TestCaseCountry;
import org.cerberus.entity.TestCaseStep;
import org.cerberus.serviceEngine.impl.RunTestCaseService;
import org.cerberus.factory.IFactoryTCase;
import org.cerberus.factory.IFactoryTestCaseCountry;
import org.cerberus.log.MyLogger;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public void loadTestCase(TCExecution tCExecution) {

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

            List<TestCaseStepAction> tcsa = this.loadTestCaseStepAction(testCaseStep);
            if (tcsa != null) {
                testCaseStep.setTestCaseStepAction(tcsa);
            }
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "adding testCaseStep");
            result.add(testCaseStep);
        }
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "return List<TestCaseStep>");
        return result;
    }

    public List<TestCaseStepAction> loadTestCaseStepAction(TestCaseStep testCaseStep) {
        List<TestCaseStepAction> result = new ArrayList<TestCaseStepAction>();
        List<TestCaseStepAction> tcsaToAdd = this.testCaseStepActionService.getListOfAction(
                testCaseStep.getTest(), testCaseStep.getTestCase(), testCaseStep.getStep());
        for (TestCaseStepAction testCaseStepAction : tcsaToAdd) {
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "set list of action :" + testCaseStepAction.getAction());

            List<TestCaseStepActionControl> tcsacList = this.loadTestCaseStepActionControl(testCaseStepAction);
            if (tcsacList != null) {
                testCaseStepAction.setTestCaseStepActionControl(tcsacList);
            }
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "adding testCaseStepAction" + testCaseStepAction.getAction());
            result.add(testCaseStepAction);
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "added testCaseStepAction" + testCaseStepAction.getAction());

        }
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "return List<TestCaseStepAction>");
        return result;
    }

    public List<TestCaseStepActionControl> loadTestCaseStepActionControl(TestCaseStepAction testCaseAction) {
        List<TestCaseStepActionControl> result = new ArrayList<TestCaseStepActionControl>();
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "get list of control");
        List<TestCaseStepActionControl> controlList = testCaseStepActionControlService.findControlByTestTestCaseStepSequence(testCaseAction.getTest(), testCaseAction.getTestCase(), testCaseAction.getStep(), testCaseAction.getSequence());
        if (controlList != null) {
            for (TestCaseStepActionControl testCaseStepActionControl : controlList) {
                MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "set control :" + testCaseStepActionControl.getType());
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
