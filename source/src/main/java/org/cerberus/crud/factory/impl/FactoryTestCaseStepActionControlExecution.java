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

import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.crud.factory.IFactoryTestCaseStepActionControlExecution;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseStepActionControlExecution implements IFactoryTestCaseStepActionControlExecution {

    @Override
    public TestCaseStepActionControlExecution create(long id, String test, String testCase, int step, int sequence,
                                                     int control, String returnCode, String returnMessage, String controlType, String controlProperty,
                                                     String controlValue, String fatal, long start, long end, long startLong, long endLong,
                                                     String screenshotFilename, String pageSourceFilename, String description, TestCaseStepActionExecution testCaseStepActionExecution, MessageEvent resultMessage) {
        TestCaseStepActionControlExecution testCaseStepActionControlExecution = new TestCaseStepActionControlExecution();
        testCaseStepActionControlExecution.setId(id);
        testCaseStepActionControlExecution.setTest(test);
        testCaseStepActionControlExecution.setTestCase(testCase);
        testCaseStepActionControlExecution.setStep(step);
        testCaseStepActionControlExecution.setSequence(sequence);
        testCaseStepActionControlExecution.setControl(control);
        testCaseStepActionControlExecution.setReturnCode(returnCode);
        testCaseStepActionControlExecution.setReturnMessage(returnMessage);
        testCaseStepActionControlExecution.setControlType(controlType);
        testCaseStepActionControlExecution.setControlProperty(controlProperty);
        testCaseStepActionControlExecution.setControlValue(controlValue);
        testCaseStepActionControlExecution.setFatal(fatal);
        testCaseStepActionControlExecution.setStart(start);
        testCaseStepActionControlExecution.setEnd(end);
        testCaseStepActionControlExecution.setStartLong(startLong);
        testCaseStepActionControlExecution.setEndLong(endLong);
        testCaseStepActionControlExecution.setScreenshotFilename(screenshotFilename);
        testCaseStepActionControlExecution.setTestCaseStepActionExecution(testCaseStepActionExecution);
        testCaseStepActionControlExecution.setControlResultMessage(resultMessage);
        testCaseStepActionControlExecution.setPageSourceFilename(pageSourceFilename);
        testCaseStepActionControlExecution.setDescription(description);
        return testCaseStepActionControlExecution;
    }

}
