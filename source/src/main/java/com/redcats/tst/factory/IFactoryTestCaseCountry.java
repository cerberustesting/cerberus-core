/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.TestCaseCountry;

/**
 *
 * @author bcivel
 */
public interface IFactoryTestCaseCountry {
    
    TestCaseCountry create(String test,String testCase,String country);
}
