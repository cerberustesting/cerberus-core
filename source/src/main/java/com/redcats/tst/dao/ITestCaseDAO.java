package com.redcats.tst.dao;

import com.redcats.tst.entity.TCase;
import com.redcats.tst.entity.TestCase;
import com.redcats.tst.exception.CerberusException;

import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 18/Dez/2012
 * @since 2.0.0
 */
public interface ITestCaseDAO {

    List<TCase> findTestCaseByTest(String test);

    TCase findTestCaseByKey(String test, String testCase) throws CerberusException;
    
    boolean updateTestCaseInformation(TestCase testCase);
    
    boolean updateTestCaseInformationCountries(TestCase tc);
    
    boolean createTestCase(TestCase testCase);
    
    List<TCase> findTestCaseByCriteria(String test, String application, String country, String active);
}
