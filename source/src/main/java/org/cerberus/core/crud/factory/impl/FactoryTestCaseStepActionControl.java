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
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.cerberus.core.crud.factory.IFactoryTestCaseStepActionControl;
import org.json.JSONArray;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseStepActionControl implements IFactoryTestCaseStepActionControl {

    @Override
    public TestCaseStepActionControl create(String test, String testcase, int stepId, int actionId, int controlId, int sort, String conditionOperator, String conditionValue1, String conditionValue2, String conditionValue3, JSONArray conditionOptions, String control, String value1,
            String value2, String value3, JSONArray options, boolean isFatal, String description, String screenshotFilename,
            boolean doScreenshotBefore, boolean doScreenshotAfter, int waitBefore, int waitAfter) {

        TestCaseStepActionControl tcControl = new TestCaseStepActionControl();

        tcControl.setTest(test);
        tcControl.setTestcase(testcase);
        tcControl.setStepId(stepId);
        tcControl.setActionId(actionId);
        tcControl.setControlId(controlId);
        tcControl.setSort(sort);
        tcControl.setConditionOperator(conditionOperator);
        tcControl.setConditionValue1(conditionValue1);
        tcControl.setConditionValue2(conditionValue2);
        tcControl.setConditionValue3(conditionValue3);
        tcControl.setConditionOptions(conditionOptions);
        tcControl.setControl(control);
        tcControl.setValue1(value1);
        tcControl.setValue2(value2);
        tcControl.setValue3(value3);
        tcControl.setOptions(options);
        tcControl.setFatal(isFatal);
        tcControl.setDescription(description);
        tcControl.setScreenshotFilename(screenshotFilename);
        tcControl.setDoScreenshotBefore(doScreenshotBefore);
        tcControl.setDoScreenshotAfter(doScreenshotAfter);
        tcControl.setWaitBefore(waitBefore);
        tcControl.setWaitAfter(waitAfter);

        return tcControl;
    }

    @Override
    public TestCaseStepActionControl create(String test, String testcase, int stepId, int actionId, int controlId, int sort, String conditionOperator, String conditionValue1, String conditionValue2, String conditionValue3, JSONArray conditionOptions, String control, String value1,
            String value2, String value3, JSONArray options, boolean isFatal, String description, String screenshotFilename,
            boolean doScreenshotBefore, boolean doScreenshotAfter, int waitBefore, int waitAfter,
            String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif) {

        TestCaseStepActionControl tcControl = new TestCaseStepActionControl();

        tcControl.setTest(test);
        tcControl.setTestcase(testcase);
        tcControl.setStepId(stepId);
        tcControl.setActionId(actionId);
        tcControl.setControlId(controlId);
        tcControl.setSort(sort);
        tcControl.setConditionOperator(conditionOperator);
        tcControl.setConditionValue1(conditionValue1);
        tcControl.setConditionValue2(conditionValue2);
        tcControl.setConditionValue3(conditionValue3);
        tcControl.setConditionOptions(conditionOptions);
        tcControl.setControl(control);
        tcControl.setValue1(value1);
        tcControl.setValue2(value2);
        tcControl.setValue3(value3);
        tcControl.setOptions(options);
        tcControl.setFatal(isFatal);
        tcControl.setDescription(description);
        tcControl.setScreenshotFilename(screenshotFilename);
        tcControl.setDoScreenshotBefore(doScreenshotBefore);
        tcControl.setDoScreenshotAfter(doScreenshotAfter);
        tcControl.setWaitBefore(waitBefore);
        tcControl.setWaitAfter(waitAfter);
        tcControl.setUsrCreated(usrCreated);
        tcControl.setDateCreated(dateCreated);
        tcControl.setUsrModif(usrModif);
        tcControl.setDateModif(dateModif);

        return tcControl;
    }

}
