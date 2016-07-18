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

import java.util.Date;
import org.cerberus.crud.entity.MessageEvent;
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
    public TestCaseExecutionData create(long id, String property, String description, String value, String type, String value1,String value2,
                                        String returnCode,String rMessage, long start, long end, long startLong, long endLong, MessageEvent message) {
        TestCaseExecutionData testCaseExecutionData = new TestCaseExecutionData();
        testCaseExecutionData.setId(id);
        testCaseExecutionData.setProperty(property);
        testCaseExecutionData.setDescription(description);
        testCaseExecutionData.setValue(value);
        testCaseExecutionData.setType(type);
        testCaseExecutionData.setValue1(value1);
        testCaseExecutionData.setValue2(value2);
        testCaseExecutionData.setRC(returnCode);
        testCaseExecutionData.setrMessage(rMessage);
        testCaseExecutionData.setStart(start);
        testCaseExecutionData.setEnd(end);
        testCaseExecutionData.setStartLong(startLong);
        testCaseExecutionData.setEndLong(endLong);
        testCaseExecutionData.setPropertyResultMessage(message);
        return testCaseExecutionData;

    }

    @Override
    public TestCaseExecutionData create(long id, String property, String description, String type, String value1, String value2, MessageEvent message) {
        TestCaseExecutionData testCaseExecutionData = new TestCaseExecutionData();
        testCaseExecutionData.setId(id);
        testCaseExecutionData.setProperty(property);
        testCaseExecutionData.setDescription(description);
        testCaseExecutionData.setType(type);
        testCaseExecutionData.setValue1(value1);
        testCaseExecutionData.setValue2(value2);
        testCaseExecutionData.setPropertyResultMessage(message);
        
        long now = new Date().getTime();
        testCaseExecutionData.setStart(now);
        testCaseExecutionData.setEnd(now);
        testCaseExecutionData.setStartLong(now);
        testCaseExecutionData.setEndLong(now);
        return testCaseExecutionData;
    }
}
