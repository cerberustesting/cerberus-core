/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.crud.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseCountry;
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.cerberus.core.crud.factory.IFactoryTestCaseStep;
import org.cerberus.core.crud.service.ILoadTestCaseService;
import org.cerberus.core.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.core.crud.service.ITestCaseStepActionControlService;
import org.cerberus.core.crud.service.ITestCaseStepActionService;
import org.cerberus.core.crud.service.ITestCaseStepService;
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
                testCaseCountry.getTest(), testCaseCountry.getTestcase(),
                testCaseCountry.getCountry());
    }

    @Override
    public List<TestCaseStep> loadTestCaseStep(TestCase testCase) {
        List<TestCaseStep> result = new ArrayList<>();
        for (TestCaseStep testCaseStep : this.testCaseStepService.getListOfSteps(testCase.getTest(), testCase.getTestcase())) {
            /**
             * If use Step, load action and control of used step
             */
            if (!testCaseStep.isUsingLibraryStep()) {
                List<TestCaseStepAction> tcsa = this.loadTestCaseStepAction(testCaseStep, null);
                if (tcsa != null) {
                    testCaseStep.setActions(tcsa);
                }
            } else {
                // Step is used from another testcase.
                List<TestCaseStepAction> tcsa = this.loadTestCaseStepAction(testCaseStep, factoryTCS.create(testCaseStep.getLibraryStepTest(),
                        testCaseStep.getLibraryStepTestcase(), testCaseStep.getLibraryStepStepId(), testCaseStep.getSort(), null, null, null, null, null, null, null, false, null, null, 0, false, false, null, null, null, null));
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
        boolean isUsingLibraryStep = (UsedTestCaseStep != null);
        if (!isUsingLibraryStep) {
            tcsaToAdd = this.testCaseStepActionService.getListOfAction(testCaseStep.getTest(), testCaseStep.getTestcase(), testCaseStep.getStepId());
        } else {
            tcsaToAdd = this.testCaseStepActionService.getListOfAction(UsedTestCaseStep.getTest(), UsedTestCaseStep.getTestcase(), UsedTestCaseStep.getStepId());
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
            testCaseStepAction.setTestcase(testCaseStep.getTestcase());
            testCaseStepAction.setStepId(testCaseStep.getStepId());
            result.add(testCaseStepAction);

        }
        return result;
    }

    public List<TestCaseStepActionControl> loadTestCaseStepActionControl(TestCaseStep testCaseStep, TestCaseStepAction testCaseAction) {
        List<TestCaseStepActionControl> result = new ArrayList<>();
        List<TestCaseStepActionControl> controlList = testCaseStepActionControlService.findControlByTestTestCaseStepIdActionId(testCaseAction.getTest(), testCaseAction.getTestcase(), testCaseAction.getStepId(), testCaseAction.getActionId());
        if (controlList != null) {
            for (TestCaseStepActionControl testCaseStepActionControl : controlList) {
                testCaseStepActionControl.setTest(testCaseStep.getTest());
                testCaseStepActionControl.setTestcase(testCaseStep.getTestcase());
                testCaseStepActionControl.setStepId(testCaseStep.getStepId());
                result.add(testCaseStepActionControl);
            }
        }
        return result;
    }

}
