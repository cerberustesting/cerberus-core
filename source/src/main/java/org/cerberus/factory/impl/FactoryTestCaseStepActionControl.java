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
package org.cerberus.factory.impl;

import org.cerberus.entity.TestCaseStepActionControl;
import org.cerberus.factory.IFactoryTestCaseStepActionControl;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseStepActionControl implements IFactoryTestCaseStepActionControl {

    @Override
    public TestCaseStepActionControl create(String test, String testCase, int step, int sequence,
                                            int control, String type, String controlValue, String controlProperty, String fatal, String description) {
        TestCaseStepActionControl testCaseStepActionControl = new TestCaseStepActionControl();
        testCaseStepActionControl.setTest(test);
        testCaseStepActionControl.setTestCase(testCase);
        testCaseStepActionControl.setType(type);
        testCaseStepActionControl.setStep(step);
        testCaseStepActionControl.setSequence(sequence);
        testCaseStepActionControl.setControl(control);
        testCaseStepActionControl.setControlValue(controlValue);
        testCaseStepActionControl.setControlProperty(controlProperty);
        testCaseStepActionControl.setFatal(fatal);
        testCaseStepActionControl.setDescription(description);
        return testCaseStepActionControl;
    }

}
