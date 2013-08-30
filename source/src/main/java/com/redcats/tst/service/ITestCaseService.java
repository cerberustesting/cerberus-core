/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.TCase;
import com.redcats.tst.entity.TestCase;
import com.redcats.tst.exception.CerberusException;
import java.util.List;

/**
 *
 * @author bcivel
 */
public interface ITestCaseService {

    /**
     *
     * @param test
     * @param testCase
     * @return
     */
    TCase findTestCaseByKey(String test, String testCase) throws CerberusException;
    
    TCase findTestCaseByKeyWithDependency (String test, String testCase) throws CerberusException;

    List<TCase> findTestCaseByTest(String test);

    List<TCase> findTestCaseActiveByCriteria(String test, String application, String country);

    boolean updateTestCaseInformation(TestCase testCase);

    boolean updateTestCaseInformationCountries(TestCase tc);

    boolean createTestCase(TestCase testCase);
    
}
