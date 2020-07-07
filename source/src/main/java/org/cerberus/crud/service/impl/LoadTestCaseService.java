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
package org.cerberus.crud.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.crud.factory.IFactoryTestCaseStep;
import org.cerberus.crud.service.ILoadTestCaseService;
import org.cerberus.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.crud.service.ITestCaseStepActionControlService;
import org.cerberus.crud.service.ITestCaseStepActionService;
import org.cerberus.crud.service.ITestCaseStepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class LoadTestCaseService implements ILoadTestCaseService {

    private static final Logger LOG = LogManager.getLogger(TestCaseStepActionService.class);

    @Autowired
    private ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
    @Autowired
    private ITestCaseStepService testCaseStepService;
    @Autowired
    private ITestCaseStepActionService testCaseStepActionService;
    @Autowired
    private ITestCaseStepActionControlService testCaseStepActionControlService;
    @Autowired
    private IFactoryTestCaseStep factoryTCS;

    //@Override
    public List<TestCaseCountryProperties> loadProperties(TestCaseCountry testCaseCountry) {

        return this.testCaseCountryPropertiesService.findListOfPropertyPerTestTestCaseCountry(
                testCaseCountry.getTest(), testCaseCountry.getTestCase(),
                testCaseCountry.getCountry());
    }

    @Override
    public List<TestCaseStep> loadTestCaseStep(TestCase testCase) {
        List<TestCaseStep> result = new ArrayList<>();
        for (TestCaseStep testCaseStep : this.testCaseStepService.getListOfSteps(testCase.getTest(), testCase.getTestCase())) {
            /**
             * If use Step, load action and control of used step
             */
            if (!testCaseStep.getUseStep().equals("Y")) {
                List<TestCaseStepAction> tcsa = this.loadTestCaseStepAction(testCaseStep, null);
                if (tcsa != null) {
                    testCaseStep.setActions(tcsa);
                }
            } else {
                // Step is used from another testcase.
                List<TestCaseStepAction> tcsa = this.loadTestCaseStepAction(testCaseStep, factoryTCS.create(testCaseStep.getUseStepTest(),
                        testCaseStep.getUseStepTestCase(), testCaseStep.getUseStepStep(), testCaseStep.getSort(), null, null, null, null, null, null, null, null, null, 0, null, null, null, null, null, null));
                if (tcsa != null) {
                    testCaseStep.setActions(tcsa);
                }
                // Copy the usedStep property to main step. Loop and conditionOperator are taken from used step.
                testCaseStep = testCaseStepService.modifyTestCaseStepDataFromUsedStep(testCaseStep);
            }
            result.add(testCaseStep);
        }

        return result;
    }

    public List<TestCaseStepAction> loadTestCaseStepAction(TestCaseStep testCaseStep, TestCaseStep UsedTestCaseStep) {
        List<TestCaseStepAction> result = new ArrayList<>();
        List<TestCaseStepAction> tcsaToAdd;
        /**
         * If use Step, take the List of action and control of the used step
         */
        boolean useStep = (UsedTestCaseStep != null);
        if (!useStep) {
            tcsaToAdd = this.testCaseStepActionService.getListOfAction(testCaseStep.getTest(), testCaseStep.getTestCase(), testCaseStep.getStep());
        } else {
            tcsaToAdd = this.testCaseStepActionService.getListOfAction(UsedTestCaseStep.getTest(), UsedTestCaseStep.getTestCase(), UsedTestCaseStep.getStep());
        }

        /**
         * Iterate on the list of action to get the control In case of useStep,
         * print the test,testcase,step of the executed test instead of the used
         * step
         */
        for (TestCaseStepAction testCaseStepAction : tcsaToAdd) {

            List<TestCaseStepActionControl> tcsacList = this.loadTestCaseStepActionControl(testCaseStep, testCaseStepAction);
            if (tcsacList != null) {
                testCaseStepAction.setControls(tcsacList);
            }

            /**
             * Update the test, Testcase, Step in case of useStep
             */
            testCaseStepAction.setTest(testCaseStep.getTest());
            testCaseStepAction.setTestCase(testCaseStep.getTestCase());
            testCaseStepAction.setStep(testCaseStep.getStep());
            result.add(testCaseStepAction);

        }
        return result;
    }

    public List<TestCaseStepActionControl> loadTestCaseStepActionControl(TestCaseStep testCaseStep, TestCaseStepAction testCaseAction) {
        List<TestCaseStepActionControl> result = new ArrayList<>();
        List<TestCaseStepActionControl> controlList = testCaseStepActionControlService.findControlByTestTestCaseStepSequence(testCaseAction.getTest(), testCaseAction.getTestCase(), testCaseAction.getStep(), testCaseAction.getSequence());
        if (controlList != null) {
            for (TestCaseStepActionControl testCaseStepActionControl : controlList) {
                testCaseStepActionControl.setTest(testCaseStep.getTest());
                testCaseStepActionControl.setTestCase(testCaseStep.getTestCase());
                testCaseStepActionControl.setStep(testCaseStep.getStep());
                result.add(testCaseStepActionControl);
            }
        }
        return result;
    }

}
