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

import java.sql.Timestamp;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.crud.factory.IFactoryTestDataLib;
import org.springframework.stereotype.Service;

/**
 *
 * @author vertigo17
 */
@Service
public class FactoryTestDataLib implements IFactoryTestDataLib {

    @Override
    public TestDataLib create(Integer testDataLibID, String name, String system, String environment,
            String country, String group, String type, String database,
            String script, String servicePath, String method,
            String envelope, String description, String creator, Timestamp created,
            String LastModifier, Timestamp lastModified, String subDataValue, String subDataColumn, String subDataParsingAnswer) {

        TestDataLib newData = new TestDataLib();

        newData.setTestDataLibID(testDataLibID);
        newData.setType(type);
        newData.setName(name);
        newData.setSystem(system);
        newData.setCountry(country);
        newData.setEnvironment(environment);
        newData.setGroup(group);
        newData.setDescription(description);
        newData.setDatabase(database);
        newData.setScript(script);
        newData.setServicePath(servicePath);
        newData.setMethod(method);
        newData.setEnvelope(envelope);
        newData.setDescription(description);
        newData.setCreator(creator);
        newData.setCreated(created);
        newData.setLastModifier(LastModifier);
        newData.setLastModified(lastModified);
        newData.setSubDataValue(subDataValue);
        newData.setSubDataColumn(subDataColumn);
        newData.setSubDataParsingAnswer(subDataParsingAnswer);
        return newData;

    }

}
