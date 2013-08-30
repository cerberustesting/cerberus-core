/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.refactor;

import com.redcats.tst.entity.TestCase;

/**
 * @author Tiago Bernardes <tbernardes@redoute.pt>
 * @version 1.0
 * @since 2012-08-20
 */
public interface ITestCaseBusiness {

    int UPDATE_INFORMATION = 0;
    int UPDATE_PROPERTIES = 1;
    int UPDATE_ACTIONS = 2;
    int UPDATE_CONTROLS = 3;
    int UPDATE_ALL = 4;

    String createTestCase(TestCase tc);

    TestCase getTestCase(String test, String testcase);

    String removeTestCase(String test, String testcase);

    String updateTestCase(TestCase tc, int type);
}
