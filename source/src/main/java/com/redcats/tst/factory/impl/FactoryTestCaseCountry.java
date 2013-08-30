/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.TestCaseCountry;
import com.redcats.tst.factory.IFactoryTestCaseCountry;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseCountry implements IFactoryTestCaseCountry {

    @Override
    public TestCaseCountry create(String test, String testCase, String country) {
        TestCaseCountry testCaseCountry = new TestCaseCountry();
        testCaseCountry.setTest(test);
        testCaseCountry.setTestCase(testCase);
        testCaseCountry.setCountry(country);
        return testCaseCountry;
    }

}
