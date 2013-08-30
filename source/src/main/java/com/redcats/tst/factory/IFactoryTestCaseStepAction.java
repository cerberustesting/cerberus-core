/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.TestCaseStepAction;

/**
 * @author bcivel
 */
public interface IFactoryTestCaseStepAction {

    TestCaseStepAction create(String test, String testCase, int step, int sequence,
                              String action, String object, String property);
}
