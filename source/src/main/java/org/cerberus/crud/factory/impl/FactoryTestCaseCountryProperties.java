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

import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.factory.IFactoryTestCaseCountryProperties;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 * @author FNogueira
 */
@Service
public class FactoryTestCaseCountryProperties implements IFactoryTestCaseCountryProperties {

    @Override
    public TestCaseCountryProperties create(String test, String testCase, String country, String property, String description, String type, String database, String value1, String value2, String length, int rowLimit, String nature, int retryNb, int retryPeriod, int cacheExpire, int rank) {
        TestCaseCountryProperties testCaseCountryProperties = new TestCaseCountryProperties();
        testCaseCountryProperties.setTest(test);
        testCaseCountryProperties.setTestCase(testCase);
        testCaseCountryProperties.setCountry(country);
        testCaseCountryProperties.setProperty(property);
        testCaseCountryProperties.setDescription(description == null ? "" : description);
        testCaseCountryProperties.setType(type);
        testCaseCountryProperties.setDatabase(database);
        testCaseCountryProperties.setValue1(value1);
        testCaseCountryProperties.setValue2(value2);
        testCaseCountryProperties.setLength(length);
        testCaseCountryProperties.setRowLimit(rowLimit);
        testCaseCountryProperties.setNature(nature);
        testCaseCountryProperties.setRetryNb(retryNb);
        testCaseCountryProperties.setRetryPeriod(retryPeriod);
        testCaseCountryProperties.setCacheExpire(cacheExpire);
        testCaseCountryProperties.setRank(rank);
        return testCaseCountryProperties;
    }

}
