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

import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.crud.factory.IFactoryTestCaseStepActionControl;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseStepActionControl implements IFactoryTestCaseStepActionControl {

    @Override
    public TestCaseStepActionControl create(String test, String testCase, int step, int sequence, int controlSequence, int sort, String conditionOper, String conditionVal1, String control, String value1,
            String value2, String fatal, String description, String screenshotFilename) {
        TestCaseStepActionControl testCaseStepActionControl = new TestCaseStepActionControl();
        testCaseStepActionControl.setTest(test);
        testCaseStepActionControl.setTestCase(testCase);
        testCaseStepActionControl.setStep(step);
        testCaseStepActionControl.setSequence(sequence);
        testCaseStepActionControl.setControlSequence(controlSequence);
        testCaseStepActionControl.setSort(sort);
        testCaseStepActionControl.setConditionOper("always");
        testCaseStepActionControl.setConditionVal1("");
//        testCaseStepActionControl.setConditionOper(conditionOper);
//        testCaseStepActionControl.setConditionVal1(conditionVal1);
        testCaseStepActionControl.setControl(control);
        testCaseStepActionControl.setValue1(value1);
        testCaseStepActionControl.setValue2(value2);
        testCaseStepActionControl.setFatal(fatal);
        testCaseStepActionControl.setDescription(description);
        testCaseStepActionControl.setScreenshotFilename(screenshotFilename);
        return testCaseStepActionControl;
    }
    

}
