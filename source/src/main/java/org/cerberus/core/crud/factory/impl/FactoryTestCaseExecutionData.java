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
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.crud.entity.TestCaseExecutionData;
import org.cerberus.core.crud.entity.TestCaseExecutionFile;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecutionData;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 * @author FNogueira
 */
@Service
public class FactoryTestCaseExecutionData implements IFactoryTestCaseExecutionData {

    @Override
    public TestCaseExecutionData create(long id, String property, int index, String description, String value, String type, int rank, String value1, String value2, String value3,
            String rC, String rMessage, long start, long end, long startLong, long endLong, MessageEvent message, int retrynb, int retryperiod,
            String database, String value1Init, String value2Init,  String value3Init, String lengthInit, String length, int rowLimit, String nature,
            String system, String environment, String country, String dataLib, String jsonResult, String fromCache) {
        TestCaseExecutionData testCaseExecutionData = new TestCaseExecutionData();
        testCaseExecutionData.setId(id);
        testCaseExecutionData.setProperty(property);
        testCaseExecutionData.setIndex(index);
        testCaseExecutionData.setType(type);
        testCaseExecutionData.setRank(rank);
        testCaseExecutionData.setValue(value);
        testCaseExecutionData.setDatabase(database);
        testCaseExecutionData.setValue1Init(value1Init);
        testCaseExecutionData.setValue2Init(value2Init);
        testCaseExecutionData.setValue3Init(value3Init);
        testCaseExecutionData.setValue1(value1);
        testCaseExecutionData.setValue2(value2);
        testCaseExecutionData.setValue3(value3);
        testCaseExecutionData.setLength(length);
        testCaseExecutionData.setLengthInit(lengthInit);
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
        testCaseExecutionData.setSystem(system);
        testCaseExecutionData.setEnvironment(environment);
        testCaseExecutionData.setCountry(country);
        testCaseExecutionData.setDataLib(dataLib);
        testCaseExecutionData.setJsonResult(jsonResult);
        testCaseExecutionData.setFromCache(fromCache);
        // List objects
        List<TestCaseExecutionFile> objectFileList = new ArrayList<>();
        testCaseExecutionData.setFileList(objectFileList);

        return testCaseExecutionData;

    }

}
