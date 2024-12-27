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
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.core.crud.entity.TestCaseStepActionExecution;
import org.cerberus.core.crud.entity.TestCaseStepExecution;
import org.cerberus.core.crud.factory.IFactoryTestCaseStepActionExecution;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseStepActionExecution implements IFactoryTestCaseStepActionExecution {

    @Override
    public TestCaseStepActionExecution create(long id, String test, String testCase, int stepId, int index, int sequence, int sort, String returnCode, String returnMessage,
            String conditionOperator, String conditionVal1Init, String conditionVal2Init, String conditionVal3Init, String conditionVal1, String conditionVal2, String conditionVal3,
            String action, String value1Init, String value2Init, String value3Init, String value1, String value2, String value3,
            String forceExeStatus, long start, long end, long startLong, long endLong, MessageEvent resultMessage, String description,
            TestCaseStepAction tcAction, TestCaseStepExecution stepExecution) {
        TestCaseStepActionExecution actionExecution = new TestCaseStepActionExecution();
        actionExecution.setAction(action);
        actionExecution.setEnd(end);
        actionExecution.setEndLong(endLong);
        actionExecution.setId(id);
        actionExecution.setConditionOperator(conditionOperator);
        actionExecution.setConditionVal1Init(conditionVal1Init);
        actionExecution.setConditionVal2Init(conditionVal2Init);
        actionExecution.setConditionVal3Init(conditionVal3Init);
        actionExecution.setConditionVal1(conditionVal1);
        actionExecution.setConditionVal2(conditionVal2);
        actionExecution.setConditionVal3(conditionVal3);
        actionExecution.setValue1(value1);
        actionExecution.setValue2(value2);
        actionExecution.setValue3(value3);
        actionExecution.setValue1Init(value1Init);
        actionExecution.setValue2Init(value2Init);
        actionExecution.setValue3Init(value3Init);
        actionExecution.setFatal(forceExeStatus);
        actionExecution.setReturnCode(returnCode);
        actionExecution.setReturnMessage(returnMessage);
        actionExecution.setSequence(sequence);
        actionExecution.setSort(sort);
        actionExecution.setStart(start);
        actionExecution.setStartLong(startLong);
        actionExecution.setStepId(stepId);
        actionExecution.setIndex(index);
        actionExecution.setTest(test);
        actionExecution.setTestCase(testCase);
        actionExecution.setActionResultMessage(resultMessage);

        actionExecution.setTestCaseStepAction(tcAction);
        actionExecution.setTestCaseStepExecution(stepExecution);
        actionExecution.setDescription(description);
        // List objects
        List<TestCaseExecutionFile> objectFileList = new ArrayList<>();
        actionExecution.setFileList(objectFileList);
        List<TestCaseStepActionControlExecution> testCaseStepActionControlExecution = new ArrayList<>();
        actionExecution.setTestCaseStepActionControlExecutionList(testCaseStepActionControlExecution);
        return actionExecution;
    }

}
