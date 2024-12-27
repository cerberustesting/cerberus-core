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
package org.cerberus.core.crud.factory.impl;

import java.util.ArrayList;
import java.util.List;
import org.cerberus.core.crud.entity.TestCaseExecutionFile;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.core.crud.entity.TestCaseStepActionExecution;
import org.cerberus.core.crud.factory.IFactoryTestCaseStepActionControlExecution;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseStepActionControlExecution implements IFactoryTestCaseStepActionControlExecution {

    @Override
    public TestCaseStepActionControlExecution create(long id, String test, String testCase, int stepId, int index, int sequence, int controlSequence, int sort,
            String returnCode, String returnMessage,
            String conditionOperator, String conditionVal1Init, String conditionVal2Init, String conditionVal3Init, String conditionVal1, String conditionVal2, String conditionVal3,
            String control, String value1Init, String value2Init, String value3Init, String value1, String value2, String value3,
            String fatal, long start, long end, long startLong, long endLong,
            String description, TestCaseStepActionExecution testCaseStepActionExecution, MessageEvent resultMessage) {

        TestCaseStepActionControlExecution controlExecution = new TestCaseStepActionControlExecution();

        controlExecution.setId(id);
        controlExecution.setTest(test);
        controlExecution.setTestCase(testCase);
        controlExecution.setStepId(stepId);
        controlExecution.setIndex(index);
        controlExecution.setActionId(sequence);
        controlExecution.setControlId(controlSequence);
        controlExecution.setSort(sort);
        controlExecution.setReturnCode(returnCode);
        controlExecution.setReturnMessage(returnMessage);
        controlExecution.setConditionOperator(conditionOperator);
        controlExecution.setConditionVal1Init(conditionVal1Init);
        controlExecution.setConditionVal2Init(conditionVal2Init);
        controlExecution.setConditionVal3Init(conditionVal3Init);
        controlExecution.setConditionVal1(conditionVal1);
        controlExecution.setConditionVal2(conditionVal2);
        controlExecution.setConditionVal3(conditionVal3);
        controlExecution.setControl(control);
        controlExecution.setValue1(value1);
        controlExecution.setValue2(value2);
        controlExecution.setValue3(value3);
        controlExecution.setValue1Init(value1Init);
        controlExecution.setValue2Init(value2Init);
        controlExecution.setValue3Init(value3Init);
        controlExecution.setFatal(fatal);
        controlExecution.setStart(start);
        controlExecution.setEnd(end);
        controlExecution.setStartLong(startLong);
        controlExecution.setEndLong(endLong);
        controlExecution.setTestCaseStepActionExecution(testCaseStepActionExecution);
        controlExecution.setControlResultMessage(resultMessage);
        controlExecution.setDescription(description);
        // List objects
        List<TestCaseExecutionFile> objectFileList = new ArrayList<>();
        controlExecution.setFileList(objectFileList);

        return controlExecution;
    }

}
