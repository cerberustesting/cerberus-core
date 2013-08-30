/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.refactor;

/**
 * @author alexandre
 */
public class Country {

    private Integer availableTests;
    private Integer executedTests;
    private Integer kO;
    private String name;
    private Integer oK;

    public Country() {

        this.availableTests = 0;
        this.executedTests = 0;
        this.oK = 0;
        this.kO = 0;
    }

    public void addAvailableTest() {
        this.availableTests = this.availableTests + 1;
        // System.out.println(name + " adding available test : "
        // + this.availableTests);
    }

    public void addExecutedTest() {

        this.executedTests = this.executedTests + 1;
        // System.out.println(name + " adding executed test : "
        // + this.executedTests);
    }

    public void addKOTest() {

        this.kO = this.kO + 1;
    }

    public void addOKTest() {

        this.oK = this.oK + 1;
    }

    public Integer getAvailableTests() {

        return this.availableTests;
    }

    public Integer getExecutedTests() {

        return this.executedTests;
    }

    public Integer getkO() {

        return this.kO;
    }

    public String getName() {

        return this.name;
    }

    public Integer getoK() {

        return this.oK;
    }

    public void setName(String name) {

        this.name = name;
    }
}
