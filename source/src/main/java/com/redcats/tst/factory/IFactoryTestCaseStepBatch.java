/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.TestCaseStepBatch;

/**
 * @author bcivel
 */
public interface IFactoryTestCaseStepBatch {

    TestCaseStepBatch create(String test, String testCase, int step, String batch);
}
