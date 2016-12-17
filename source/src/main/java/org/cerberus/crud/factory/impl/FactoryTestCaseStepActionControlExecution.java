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

import org.cerberus.engine.entity.MessageEvent;
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
    public TestCaseStepActionControlExecution create(long id, String test, String testCase, int step, int sequence, int controlSequence, int sort, String returnCode,
            String returnMessage, String conditionOper, String conditionVal1, String conditionVal2, String control, String value1Init, String value2Init, String value1, String value2, String fatal, long start, long end, long startLong, long endLong,
            String description, TestCaseStepActionExecution testCaseStepActionExecution, MessageEvent resultMessage) {
        TestCaseStepActionControlExecution testCaseStepActionControlExecution = new TestCaseStepActionControlExecution();
        testCaseStepActionControlExecution.setId(id);
        testCaseStepActionControlExecution.setTest(test);
        testCaseStepActionControlExecution.setTestCase(testCase);
        testCaseStepActionControlExecution.setStep(step);
        testCaseStepActionControlExecution.setSequence(sequence);
        testCaseStepActionControlExecution.setControlSequence(controlSequence);
        testCaseStepActionControlExecution.setSort(sort);
        testCaseStepActionControlExecution.setReturnCode(returnCode);
        testCaseStepActionControlExecution.setReturnMessage(returnMessage);
        testCaseStepActionControlExecution.setConditionOper(conditionOper);
        testCaseStepActionControlExecution.setConditionVal1(conditionVal1);
        testCaseStepActionControlExecution.setConditionVal2(conditionVal2);
        testCaseStepActionControlExecution.setControl(control);
        testCaseStepActionControlExecution.setValue1(value1);
        testCaseStepActionControlExecution.setValue2(value2);
        testCaseStepActionControlExecution.setValue1Init(value1Init);
        testCaseStepActionControlExecution.setValue2Init(value2Init);
        testCaseStepActionControlExecution.setFatal(fatal);
        testCaseStepActionControlExecution.setStart(start);
        testCaseStepActionControlExecution.setEnd(end);
        testCaseStepActionControlExecution.setStartLong(startLong);
        testCaseStepActionControlExecution.setEndLong(endLong);
        testCaseStepActionControlExecution.setTestCaseStepActionExecution(testCaseStepActionExecution);
        testCaseStepActionControlExecution.setControlResultMessage(resultMessage);
        testCaseStepActionControlExecution.setDescription(description);
        return testCaseStepActionControlExecution;
    }

}
