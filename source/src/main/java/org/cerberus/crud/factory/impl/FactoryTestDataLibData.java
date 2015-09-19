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

import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.enums.TestDataLibTypeEnum;
import org.cerberus.crud.factory.IFactoryTestDataLibData;
import org.springframework.stereotype.Service;

/**
 *
 * @author vertigo17
 */
@Service
public class FactoryTestDataLibData implements IFactoryTestDataLibData {

    @Override
    public TestDataLibData create(Integer testDataLibID, String subData, String value, String column,
            String parsingAnswer, String description) {
        TestDataLibData newData = new TestDataLibData();
        newData.setTestDataLibID(testDataLibID);
        newData.setSubData(subData);
        newData.setValue(value);
        newData.setColumn(column);
        newData.setParsingAnswer(parsingAnswer);
        newData.setDescription(description);
        return newData;
    }

    @Override
    public TestDataLibData create(Integer testDataLibID, String type, String subData, String data, String description) {
        TestDataLibData newData = new TestDataLibData();
        //commons attributes
        newData.setTestDataLibID(testDataLibID);
        newData.setSubData(subData);
        newData.setDescription(description);
        
        if(TestDataLibTypeEnum.STATIC.getCode().equals(type)){
            newData.setValue(data);
        }else if(TestDataLibTypeEnum.SQL.getCode().equals(type)){
            newData.setColumn(data);
        }else if(TestDataLibTypeEnum.SOAP.getCode().equals(type)){
            newData.setParsingAnswer(data);
        }         
        return newData;
    }

}
