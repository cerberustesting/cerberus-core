/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.TestCaseCountry;
import com.redcats.tst.exception.CerberusException;
import java.util.List;

/**
 *
 * @author bcivel
 */
public interface ITestCaseCountryService {
    
    /**
     *
     * @param test
     * @param testCase
     * @param Country
     * @return
     */
    TestCaseCountry findTestCaseCountryByKey (String test, String testCase, String Country) throws CerberusException;
    
    List<TestCaseCountry> findTestCaseCountryByTestTestCase(String test, String testCase);
    
    List<String> findListOfCountryByTestTestCase(String test, String testcase);
    
    
}
