package com.redcats.tst.dao;

import com.redcats.tst.entity.TestCaseCountry;
import com.redcats.tst.exception.CerberusException;
import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 28/Dez/2012
 * @since 2.0.0
 */
public interface ITestCaseCountryDAO {
    
    TestCaseCountry findTestCaseCountryByKey(String test, String testcase, String country) throws CerberusException;
    
    List<TestCaseCountry> findTestCaseCountryByTestTestCase(String test, String testcase);
}
