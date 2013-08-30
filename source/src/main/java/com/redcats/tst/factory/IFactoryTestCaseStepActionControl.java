/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.TestCaseStepActionControl;

/**
 * @author bcivel
 */
public interface IFactoryTestCaseStepActionControl {

    TestCaseStepActionControl create(String test, String testCase, int step, int sequence,
                                     int control, String type, String controlValue, String controlProperty, String fatal);
}
