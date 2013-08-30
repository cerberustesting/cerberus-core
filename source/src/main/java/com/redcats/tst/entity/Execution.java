/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.entity;

/**
 *
 * @author bcivel
 */
public class Execution {

    private TCase testCase;
    private TCExecution testCaseExecution;
    private String country;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public TCase getTestCase() {
        return testCase;
    }

    public void setTestCase(TCase testCase) {
        this.testCase = testCase;
    }

    public TCExecution getTestCaseExecution() {
        return testCaseExecution;
    }

    public void setTestCaseExecution(TCExecution testCaseExecution) {
        this.testCaseExecution = testCaseExecution;
    }
}
