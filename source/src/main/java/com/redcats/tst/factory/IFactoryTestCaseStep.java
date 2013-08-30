/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.TestCaseStep;

/**
 * @author bcivel
 */
public interface IFactoryTestCaseStep {

    TestCaseStep create(String test, String testCase, int step, String description);
}
