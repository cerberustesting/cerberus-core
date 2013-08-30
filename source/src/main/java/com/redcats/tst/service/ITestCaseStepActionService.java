/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.TestCaseStepAction;
import java.util.List;

/**
 *
 * @author bcivel
 */
public interface ITestCaseStepActionService {

    List<TestCaseStepAction> getListOfAction(String test, String testcase, int step);
}
