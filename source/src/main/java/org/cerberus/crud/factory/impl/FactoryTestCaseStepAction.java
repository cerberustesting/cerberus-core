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
package org.cerberus.crud.factory.impl;

import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.factory.IFactoryTestCaseStepAction;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseStepAction implements IFactoryTestCaseStepAction {

    @Override
    public TestCaseStepAction create(String test, String testCase, int step, int sequence, int sort, String conditionOper, String conditionVal1, String action, String value1, String value2, String forceExeStatus, String description, String screenshotFilename) {
        TestCaseStepAction testCaseStepAction = new TestCaseStepAction();
        testCaseStepAction.setConditionOper(conditionOper);
        testCaseStepAction.setConditionVal1(conditionVal1);
        testCaseStepAction.setAction(action);
        testCaseStepAction.setValue1(value1);
        testCaseStepAction.setValue2(value2);
        testCaseStepAction.setForceExeStatus(forceExeStatus);
        testCaseStepAction.setSequence(sequence);
        testCaseStepAction.setStep(step);
        testCaseStepAction.setSort(sort);
        testCaseStepAction.setTest(test);
        testCaseStepAction.setTestCase(testCase);
        testCaseStepAction.setDescription(description);
        testCaseStepAction.setScreenshotFilename(screenshotFilename);
        return testCaseStepAction;
    }
}
