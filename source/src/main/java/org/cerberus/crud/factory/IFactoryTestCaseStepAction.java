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
package org.cerberus.crud.factory;

import java.sql.Timestamp;
import org.cerberus.crud.entity.TestCaseStepAction;

/**
 * @author bcivel
 */
public interface IFactoryTestCaseStepAction {

    TestCaseStepAction create(String test, String testcase, int stepId, int actionId, int sort, String conditionOperator, String conditionValue1, String conditionValue2, String conditionValue3,
            String action, String value1, String value2, String value3, boolean isFatal, String description, String screenshotFilename);

    TestCaseStepAction create(String test, String testcase, int stepId, int actionId, int sort, String conditionOperator, String conditionValue1, String conditionValue2, String conditionValue3,
            String action, String value1, String value2, String value3, boolean isFatal, String description, String screenshotFilename, String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif);

}
