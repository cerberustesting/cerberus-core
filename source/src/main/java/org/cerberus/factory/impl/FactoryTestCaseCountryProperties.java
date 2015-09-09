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
package org.cerberus.factory.impl;
 
import org.cerberus.entity.TestCaseCountryProperties;
import org.cerberus.entity.TestCaseSubDataAccessProperty;
import org.cerberus.factory.IFactoryTestCaseCountryProperties;
import org.cerberus.service.enums.PropertyTypeEnum;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 * @author FNogueira
 */
@Service
public class FactoryTestCaseCountryProperties implements IFactoryTestCaseCountryProperties {

    @Override
    public TestCaseCountryProperties create(String test, String testCase, String country, String property, String type, String database, String value1, String value2, int length, int rowLimit, String nature) {
        TestCaseCountryProperties testCaseCountryProperties = new TestCaseCountryProperties();
        testCaseCountryProperties.setTest(test);
        testCaseCountryProperties.setTestCase(testCase);
        testCaseCountryProperties.setCountry(country);
        testCaseCountryProperties.setProperty(property);
        testCaseCountryProperties.setType(type);
        testCaseCountryProperties.setDatabase(database);
        testCaseCountryProperties.setValue1(value1);
        testCaseCountryProperties.setValue2(value2);
        testCaseCountryProperties.setLength(length);
        testCaseCountryProperties.setRowLimit(rowLimit);
        testCaseCountryProperties.setNature(nature);
        return testCaseCountryProperties;
    }
    @Override
    public TestCaseSubDataAccessProperty create(TestCaseCountryProperties tccp, String property, String libName, String subdataName) {
        
        TestCaseSubDataAccessProperty prop = new TestCaseSubDataAccessProperty();
        prop.setTest(tccp.getTest());
        prop.setTestCase(tccp.getTestCase());
        prop.setCountry(tccp.getCountry());
        prop.setProperty(property);
        prop.setType(PropertyTypeEnum.ACCESS_SUBDATA.getPropertyName());
        prop.setDatabase("");
        prop.setValue1(""); //stores the testdatalib id
        prop.setValue2(subdataName); //stores the name of the subdata entry
        prop.setAccessName(property);
        prop.setLibraryValue(libName); 
        prop.setSubDataValue(subdataName);
        prop.setPropertyLibEntry(tccp);
        
        
        return prop;                
    }    
}
