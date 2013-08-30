/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.TestCaseCountryProperties;

/**
 * @author bcivel
 */
public interface IFactoryTestCaseCountryProperties {

    TestCaseCountryProperties create(String test, String testCase, String country, String property,
                                     String type, String database, String value, int length, int rowLimit, String nature);
}
