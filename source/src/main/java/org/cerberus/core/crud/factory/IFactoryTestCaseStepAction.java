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
package org.cerberus.core.crud.factory;

import java.sql.Timestamp;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.json.JSONArray;

/**
 * @author bcivel
 */
public interface IFactoryTestCaseStepAction {

    /**
     *
     * @param test
     * @param testcase
     * @param stepId
     * @param actionId
     * @param sort
     * @param conditionOperator
     * @param conditionValue1
     * @param conditionValue2
     * @param conditionValue3
     * @param conditionOptions
     * @param action
     * @param value1
     * @param value2
     * @param value3
     * @param options
     * @param isFatal
     * @param description
     * @param screenshotFilename
     * @param doScreenshotBefore
     * @param doScreenshotAfter
     * @param waitBefore
     * @param waitAfter
     * @return
     */
    TestCaseStepAction create(String test, String testcase, int stepId, int actionId, int sort, String conditionOperator, String conditionValue1, String conditionValue2, String conditionValue3, JSONArray conditionOptions,
            String action, String value1, String value2, String value3, JSONArray options, boolean isFatal, String description, String screenshotFilename,
            boolean doScreenshotBefore, boolean doScreenshotAfter, int waitBefore, int waitAfter);

    /**
     *
     * @param test
     * @param testcase
     * @param stepId
     * @param actionId
     * @param sort
     * @param conditionOperator
     * @param conditionValue1
     * @param conditionValue2
     * @param conditionValue3
     * @param conditionOptions
     * @param action
     * @param value1
     * @param value2
     * @param value3
     * @param options
     * @param isFatal
     * @param description
     * @param screenshotFilename
     * @param doScreenshotBefore
     * @param doScreenshotAfter
     * @param waitBefore
     * @param waitAfter
     * @param usrCreated
     * @param dateCreated
     * @param usrModif
     * @param dateModif
     * @return
     */
    TestCaseStepAction create(String test, String testcase, int stepId, int actionId, int sort, String conditionOperator, String conditionValue1, String conditionValue2, String conditionValue3, JSONArray conditionOptions,
            String action, String value1, String value2, String value3, JSONArray options, boolean isFatal, String description, String screenshotFilename,
            boolean doScreenshotBefore, boolean doScreenshotAfter, int waitBefore, int waitAfter,
            String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif);

}
