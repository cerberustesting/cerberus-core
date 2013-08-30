/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.entity;

import java.util.List;

/**
 * @author bcivel
 */
public class TestCaseStepAction {

    private String test;
    private String testCase;
    private int step;
    private int sequence;
    private String action;
    private String object;
    private String property;
    List<TestCaseStepActionControl> testCaseStepActionControl;

    public List<TestCaseStepActionControl> getTestCaseStepActionControl() {
        return testCaseStepActionControl;
    }

    public void setTestCaseStepActionControl(List<TestCaseStepActionControl> testCaseStepActionControl) {
        this.testCaseStepActionControl = testCaseStepActionControl;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
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
    private static final String SELENIUM_CLICK = "click";
    private static final String SELENIUM_CLICK_WAIT = "clickAndWait";
    private static final String SELENIUM_DOUBLECLICK = "doubleClick";
    private static final String SELENIUM_ENTER = "enter";
    private static final String SELENIUM_KEYPRESS = "keypress";
    private static final String SELENIUM_MOUSEOVER = "mouseOver";
    private static final String SELENIUM_MOUSEOVERANDWAIT = "mouseOverAndWait";
    private static final String SELENIUM_OPENURL = "openUrlWithBase";
    private static final String SELENIUM_SELECT = "select";
    private static final String SELENIUM_SELECTWAIT = "selectAndWait";
    private static final String SELENIUM_TYPE = "type";
    private static final String SELENIUM_URLLOGIN = "openUrlLogin";
    private static final String SELENIUM_WAIT = "wait";
    private static final String ACTION_CALCULATEPROPERTY = "calculateProperty";

    public boolean isSeleniumClick() {
        return this.getAction().equalsIgnoreCase(SELENIUM_CLICK);
    }

    public boolean isSeleniumClickAndWait() {
        return this.getAction().equalsIgnoreCase(SELENIUM_CLICK_WAIT);
    }

    public boolean isSeleniumDoubleClick() {
        return this.getAction().equalsIgnoreCase(SELENIUM_DOUBLECLICK);
    }

    public boolean isSeleniumEnter() {
        return this.getAction().equalsIgnoreCase(SELENIUM_ENTER);
    }

    public boolean isSeleniumKeypress() {
        return this.getAction().equalsIgnoreCase(SELENIUM_KEYPRESS);
    }

    public boolean isSeleniumMouseOver() {
        return this.getAction().equalsIgnoreCase(SELENIUM_MOUSEOVER);
    }

    public boolean isSeleniumMouseOverAndWait() {
        return this.getAction().equalsIgnoreCase(SELENIUM_MOUSEOVERANDWAIT);
    }

    public boolean isSeleniumOpenURL() {
        return this.getAction().equalsIgnoreCase(SELENIUM_OPENURL);
    }

    public boolean isSeleniumSelect() {
        return this.getAction().equalsIgnoreCase(SELENIUM_SELECT);
    }

    public boolean isSeleniumSelectAndWait() {
        return this.getAction().equalsIgnoreCase(SELENIUM_SELECTWAIT);
    }

    public boolean isSeleniumType() {
        return this.getAction().equalsIgnoreCase(SELENIUM_TYPE);
    }

    public boolean isSeleniumUrlLogin() {
        return this.getAction().equalsIgnoreCase(SELENIUM_URLLOGIN);
    }

    public boolean isSeleniumWait() {
        return this.getAction().equalsIgnoreCase(SELENIUM_WAIT);
    }

    public boolean isCalculateProperty() {
        return this.getAction().equalsIgnoreCase(ACTION_CALCULATEPROPERTY);
    }

    public boolean isSeleniumAction() {
        return this.getAction().equalsIgnoreCase(SELENIUM_CLICK) || this.getAction().equalsIgnoreCase(SELENIUM_CLICK_WAIT)
                || this.getAction().equalsIgnoreCase(SELENIUM_DOUBLECLICK) || this.getAction().equalsIgnoreCase(SELENIUM_ENTER)
                || this.getAction().equalsIgnoreCase(SELENIUM_KEYPRESS) || this.getAction().equalsIgnoreCase(SELENIUM_OPENURL)
                || this.getAction().equalsIgnoreCase(SELENIUM_MOUSEOVER) || this.getAction().equalsIgnoreCase(SELENIUM_MOUSEOVERANDWAIT)
                || this.getAction().equalsIgnoreCase(SELENIUM_TYPE) || this.getAction().equalsIgnoreCase(SELENIUM_WAIT)
                || this.getAction().equalsIgnoreCase(SELENIUM_SELECTWAIT) || this.getAction().equalsIgnoreCase(SELENIUM_URLLOGIN);
    }
}
