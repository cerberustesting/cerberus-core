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
package org.cerberus.core.crud.factory.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.factory.IFactoryTestCaseStepAction;
import org.json.JSONArray;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseStepAction implements IFactoryTestCaseStepAction {

    @Override
    public TestCaseStepAction create(String test, String testcase, int stepId, int actionId, int sort, String conditionOperator,
            String conditionValue1, String conditionValue2, String conditionValue3, JSONArray conditionOptions, String action, String value1, String value2, String value3, JSONArray options, boolean isFatal, String description, String screenshotFilename) {
        TestCaseStepAction testCaseStepAction = new TestCaseStepAction();
        testCaseStepAction.setConditionOperator(conditionOperator);
        testCaseStepAction.setConditionValue1(conditionValue1);
        testCaseStepAction.setConditionValue2(conditionValue2);
        testCaseStepAction.setConditionValue3(conditionValue3);
        testCaseStepAction.setConditionOptions(conditionOptions);
        testCaseStepAction.setAction(action);
        testCaseStepAction.setValue1(value1);
        testCaseStepAction.setValue2(value2);
        testCaseStepAction.setValue3(value3);
        testCaseStepAction.setOptions(options);
        testCaseStepAction.setFatal(isFatal);
        testCaseStepAction.setActionId(actionId);
        testCaseStepAction.setStepId(stepId);
        testCaseStepAction.setSort(sort);
        testCaseStepAction.setTest(test);
        testCaseStepAction.setTestcase(testcase);
        testCaseStepAction.setDescription(description);
        testCaseStepAction.setScreenshotFilename(screenshotFilename);
        testCaseStepAction.setControls(new ArrayList<>());

        return testCaseStepAction;
    }

    @Override
    public TestCaseStepAction create(String test, String testcase, int stepId, int actionId, int sort, String conditionOperator,
            String conditionValue1, String conditionValue2, String conditionValue3, JSONArray conditionOptions, String action, String value1, String value2, String value3, JSONArray options, boolean isFatal, String description, String screenshotFilename,
            String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif) {
        TestCaseStepAction testCaseStepAction = new TestCaseStepAction();
        testCaseStepAction.setConditionOperator(conditionOperator);
        testCaseStepAction.setConditionValue1(conditionValue1);
        testCaseStepAction.setConditionValue2(conditionValue2);
        testCaseStepAction.setConditionValue3(conditionValue3);
        testCaseStepAction.setConditionOptions(conditionOptions);
        testCaseStepAction.setAction(action);
        testCaseStepAction.setValue1(value1);
        testCaseStepAction.setValue2(value2);
        testCaseStepAction.setValue3(value3);
        testCaseStepAction.setOptions(options);
        testCaseStepAction.setFatal(isFatal);
        testCaseStepAction.setActionId(actionId);
        testCaseStepAction.setStepId(stepId);
        testCaseStepAction.setSort(sort);
        testCaseStepAction.setTest(test);
        testCaseStepAction.setTestcase(testcase);
        testCaseStepAction.setDescription(description);
        testCaseStepAction.setScreenshotFilename(screenshotFilename);
        testCaseStepAction.setControls(new ArrayList<>());
        testCaseStepAction.setUsrCreated(usrCreated);
        testCaseStepAction.setDateCreated(dateCreated);
        testCaseStepAction.setUsrModif(usrModif);
        testCaseStepAction.setDateModif(dateModif);

        return testCaseStepAction;
    }
}
