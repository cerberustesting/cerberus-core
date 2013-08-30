/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.entity;

/**
 * @author bcivel
 */
public class Test {

    private String test;
    private String description;
    private String active;
    private String automated;
    private String tDateCrea;

    public String getActive() {
        return this.active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getAutomated() {
        return this.automated;
    }

    public void setAutomated(String automated) {
        this.automated = automated;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String gettDateCrea() {
        return this.tDateCrea;
    }

    public void settDateCrea(String tDateCrea) {
        this.tDateCrea = tDateCrea;
    }

    public String getTest() {
        return this.test;
    }

    public void setTest(String test) {
        this.test = test;
    }
}
