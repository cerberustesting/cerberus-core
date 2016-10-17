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
import org.cerberus.crud.entity.TestCaseExecutionData;
import org.cerberus.crud.factory.IFactoryTestCaseExecutionData;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 * @author FNogueira
 */
@Service
public class FactoryTestCaseExecutionData implements IFactoryTestCaseExecutionData {

    @Override
    public TestCaseExecutionData create(long id, String property, int index, String description, String value, String type, String value1, String value2,
            String rC, String rMessage, long start, long end, long startLong, long endLong, MessageEvent message, int retrynb, int retryperiod,
            String database, String value1Init, String value2Init, int Length, int rowLimit, String nature) {
        TestCaseExecutionData testCaseExecutionData = new TestCaseExecutionData();
        testCaseExecutionData.setId(id);
        testCaseExecutionData.setProperty(property);
        testCaseExecutionData.setIndex(index);
        testCaseExecutionData.setType(type);
        testCaseExecutionData.setValue(value);
        testCaseExecutionData.setDatabase(database);
        testCaseExecutionData.setValue1Init(value1Init);
        testCaseExecutionData.setValue2Init(value2Init);
        testCaseExecutionData.setValue1(value1);
        testCaseExecutionData.setValue2(value2);
        testCaseExecutionData.setLength(Length);
        testCaseExecutionData.setRowLimit(rowLimit);
        testCaseExecutionData.setNature(nature);
        testCaseExecutionData.setRetryNb(retrynb);
        testCaseExecutionData.setRetryPeriod(retryperiod);
        testCaseExecutionData.setStart(start);
        testCaseExecutionData.setEnd(end);
        testCaseExecutionData.setStartLong(startLong);
        testCaseExecutionData.setEndLong(endLong);
        testCaseExecutionData.setRC(rC);
        testCaseExecutionData.setrMessage(rMessage);
        testCaseExecutionData.setDescription(description);
        testCaseExecutionData.setPropertyResultMessage(message);

        return testCaseExecutionData;

    }

}
