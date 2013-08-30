/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.entity;

import java.util.List;

/**
 *
 * @author bcivel
 */
public class TestCaseCountry {

    private String test;
    private String testCase;
    private String country;
    private List<TestCaseCountryProperties> testCaseCountryProperty;
    private TCase tCase;

    public TCase gettCase() {
        return tCase;
    }

    public void settCase(TCase tCase) {
        this.tCase = tCase;
    }

    public List<TestCaseCountryProperties> getTestCaseCountryProperty() {
        return testCaseCountryProperty;
    }

    public void setTestCaseCountryProperty(List<TestCaseCountryProperties> testCaseCountryProperty) {
        this.testCaseCountryProperty = testCaseCountryProperty;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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
