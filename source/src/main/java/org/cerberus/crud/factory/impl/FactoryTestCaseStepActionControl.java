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
package org.cerberus.crud.factory.impl;

import java.sql.Timestamp;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.crud.factory.IFactoryTestCaseStepActionControl;
import org.json.JSONArray;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseStepActionControl implements IFactoryTestCaseStepActionControl {

    @Override
    public TestCaseStepActionControl create(String test, String testcase, int stepId, int actionId, int controlId, int sort, String conditionOperator, String conditionValue1, String conditionValue2, String conditionValue3, JSONArray conditionOptions, String control, String value1,
            String value2, String value3, JSONArray options, boolean isFatal, String description, String screenshotFilename) {
        TestCaseStepActionControl testCaseStepActionControl = new TestCaseStepActionControl();
        testCaseStepActionControl.setTest(test);
        testCaseStepActionControl.setTestcase(testcase);
        testCaseStepActionControl.setStepId(stepId);
        testCaseStepActionControl.setActionId(actionId);
        testCaseStepActionControl.setControlId(controlId);
        testCaseStepActionControl.setSort(sort);
        testCaseStepActionControl.setConditionOperator(conditionOperator);
        testCaseStepActionControl.setConditionValue1(conditionValue1);
        testCaseStepActionControl.setConditionValue2(conditionValue2);
        testCaseStepActionControl.setConditionValue3(conditionValue3);
        testCaseStepActionControl.setConditionOptions(conditionOptions);
        testCaseStepActionControl.setControl(control);
        testCaseStepActionControl.setValue1(value1);
        testCaseStepActionControl.setValue2(value2);
        testCaseStepActionControl.setValue3(value3);
        testCaseStepActionControl.setOptions(options);
        testCaseStepActionControl.setFatal(isFatal);
        testCaseStepActionControl.setDescription(description);
        testCaseStepActionControl.setScreenshotFilename(screenshotFilename);

        return testCaseStepActionControl;
    }

    @Override
    public TestCaseStepActionControl create(String test, String testcase, int stepId, int actionId, int controlId, int sort, String conditionOperator, String conditionValue1, String conditionValue2, String conditionValue3, JSONArray conditionOptions, String control, String value1,
            String value2, String value3, JSONArray options, boolean isFatal, String description, String screenshotFilename, String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif) {
        TestCaseStepActionControl testCaseStepActionControl = new TestCaseStepActionControl();
        testCaseStepActionControl.setTest(test);
        testCaseStepActionControl.setTestcase(testcase);
        testCaseStepActionControl.setStepId(stepId);
        testCaseStepActionControl.setActionId(actionId);
        testCaseStepActionControl.setControlId(controlId);
        testCaseStepActionControl.setSort(sort);
        testCaseStepActionControl.setConditionOperator(conditionOperator);
        testCaseStepActionControl.setConditionValue1(conditionValue1);
        testCaseStepActionControl.setConditionValue2(conditionValue2);
        testCaseStepActionControl.setConditionValue3(conditionValue3);
        testCaseStepActionControl.setConditionOptions(conditionOptions);
        testCaseStepActionControl.setControl(control);
        testCaseStepActionControl.setValue1(value1);
        testCaseStepActionControl.setValue2(value2);
        testCaseStepActionControl.setValue3(value3);
        testCaseStepActionControl.setOptions(options);
        testCaseStepActionControl.setFatal(isFatal);
        testCaseStepActionControl.setDescription(description);
        testCaseStepActionControl.setScreenshotFilename(screenshotFilename);
        testCaseStepActionControl.setUsrCreated(usrCreated);
        testCaseStepActionControl.setDateCreated(dateCreated);
        testCaseStepActionControl.setUsrModif(usrModif);
        testCaseStepActionControl.setDateModif(dateModif);

        return testCaseStepActionControl;
    }

}
