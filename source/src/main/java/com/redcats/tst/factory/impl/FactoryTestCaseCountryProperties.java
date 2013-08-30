/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.TestCaseCountryProperties;
import com.redcats.tst.factory.IFactoryTestCaseCountryProperties;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseCountryProperties implements IFactoryTestCaseCountryProperties {

    @Override
    public TestCaseCountryProperties create(String test, String testCase, String country, String property, String type, String database, String value, int length, int rowLimit, String nature) {
        TestCaseCountryProperties testCaseCountryProperties = new TestCaseCountryProperties();
        testCaseCountryProperties.setTest(test);
        testCaseCountryProperties.setTestCase(testCase);
        testCaseCountryProperties.setCountry(country);
        testCaseCountryProperties.setProperty(property);
        testCaseCountryProperties.setType(type);
        testCaseCountryProperties.setDatabase(database);
        testCaseCountryProperties.setValue(value);
        testCaseCountryProperties.setLength(length);
        testCaseCountryProperties.setRowLimit(rowLimit);
        testCaseCountryProperties.setNature(nature);
        return testCaseCountryProperties;
    }
}
