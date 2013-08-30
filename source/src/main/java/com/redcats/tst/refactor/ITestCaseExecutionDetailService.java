/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.refactor;

import org.json.JSONArray;

/**
 * @author bcivel
 */
public interface ITestCaseExecutionDetailService {

    JSONArray lastActionExecutionDuration(String test, String testcase, String country);


}
