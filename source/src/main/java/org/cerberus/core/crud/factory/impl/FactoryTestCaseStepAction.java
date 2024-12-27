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
            String conditionValue1, String conditionValue2, String conditionValue3, JSONArray conditionOptions, String action, String value1, String value2, String value3,
            JSONArray options, boolean isFatal, String description, String screenshotFilename,
            boolean doScreenshotBefore, boolean doScreenshotAfter, int waitBefore, int waitAfter) {
        TestCaseStepAction tcAction = new TestCaseStepAction();
        tcAction.setConditionOperator(conditionOperator);
        tcAction.setConditionValue1(conditionValue1);
        tcAction.setConditionValue2(conditionValue2);
        tcAction.setConditionValue3(conditionValue3);
        tcAction.setConditionOptions(conditionOptions);
        tcAction.setAction(action);
        tcAction.setValue1(value1);
        tcAction.setValue2(value2);
        tcAction.setValue3(value3);
        tcAction.setOptions(options);
        tcAction.setFatal(isFatal);
        tcAction.setActionId(actionId);
        tcAction.setStepId(stepId);
        tcAction.setSort(sort);
        tcAction.setTest(test);
        tcAction.setTestcase(testcase);
        tcAction.setDescription(description);
        tcAction.setScreenshotFilename(screenshotFilename);
        tcAction.setControls(new ArrayList<>());
        tcAction.setDoScreenshotAfter(doScreenshotAfter);
        tcAction.setDoScreenshotBefore(doScreenshotBefore);
        tcAction.setWaitBefore(waitBefore);
        tcAction.setWaitAfter(waitAfter);

        return tcAction;
    }

    @Override
    public TestCaseStepAction create(String test, String testcase, int stepId, int actionId, int sort, String conditionOperator,
            String conditionValue1, String conditionValue2, String conditionValue3, JSONArray conditionOptions, String action, String value1, String value2, String value3,
            JSONArray options, boolean isFatal, String description, String screenshotFilename,
            boolean doScreenshotBefore, boolean doScreenshotAfter, int waitBefore, int waitAfter,
            String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif) {
        TestCaseStepAction tcAction = new TestCaseStepAction();
        tcAction.setConditionOperator(conditionOperator);
        tcAction.setConditionValue1(conditionValue1);
        tcAction.setConditionValue2(conditionValue2);
        tcAction.setConditionValue3(conditionValue3);
        tcAction.setConditionOptions(conditionOptions);
        tcAction.setAction(action);
        tcAction.setValue1(value1);
        tcAction.setValue2(value2);
        tcAction.setValue3(value3);
        tcAction.setOptions(options);
        tcAction.setFatal(isFatal);
        tcAction.setActionId(actionId);
        tcAction.setStepId(stepId);
        tcAction.setSort(sort);
        tcAction.setTest(test);
        tcAction.setTestcase(testcase);
        tcAction.setDescription(description);
        tcAction.setScreenshotFilename(screenshotFilename);
        tcAction.setControls(new ArrayList<>());
        tcAction.setDoScreenshotAfter(doScreenshotAfter);
        tcAction.setDoScreenshotBefore(doScreenshotBefore);
        tcAction.setWaitBefore(waitBefore);
        tcAction.setWaitAfter(waitAfter);
        tcAction.setUsrCreated(usrCreated);
        tcAction.setDateCreated(dateCreated);
        tcAction.setUsrModif(usrModif);
        tcAction.setDateModif(dateModif);

        return tcAction;
    }
}
