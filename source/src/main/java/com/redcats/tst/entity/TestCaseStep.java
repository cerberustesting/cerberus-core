/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.entity;

import java.util.List;

/**
 * @author bcivel
 */
public class TestCaseStep {

    private String test;
    private String testCase;
    private int step;
    private String description;
    private List<TestCaseStepAction> testCaseStepAction;

    public List<TestCaseStepAction> getTestCaseStepAction() {
        return testCaseStepAction;
    }

    public void setTestCaseStepAction(List<TestCaseStepAction> testCaseStepAction) {
        this.testCaseStepAction = testCaseStepAction;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getTestCase() {
        return testCase;
    }

    public void setTestCase(String testCase) {
        this.testCase = testCase;
    }
}
