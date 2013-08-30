/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.TestCaseCountryProperties;
import com.redcats.tst.exception.CerberusException;

import java.util.List;

/**
 * @author bcivel
 */
public interface ITestCaseCountryPropertiesService {

    List<TestCaseCountryProperties> findListOfPropertyPerTestTestCaseCountry(String test, String testCase, String country);

    List<TestCaseCountryProperties> findListOfPropertyPerTestTestCase(String test, String testcase);

    List<TestCaseCountryProperties> findDistinctPropertiesOfTestCase(String test, String testcase);

    List<String> findCountryByProperty(TestCaseCountryProperties testCaseCountryProperties);

    TestCaseCountryProperties findTestCaseCountryPropertiesByKey(String test, String testCase, String country, String property) throws CerberusException;

}
