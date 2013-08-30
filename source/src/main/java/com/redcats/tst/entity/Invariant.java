/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.entity;

/**
 * @author bcivel
 */
public class Invariant {

    private String idName;
    private String value;
    private int sort;
    private int id;
    private String description;
    private String gp1;
    private String gp2;
    private String gp3;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGp1() {
        return gp1;
    }

    public void setGp1(String gp1) {
        this.gp1 = gp1;
    }

    public String getGp2() {
        return gp2;
    }

    public void setGp2(String gp2) {
        this.gp2 = gp2;
    }

    public String getGp3() {
        return gp3;
    }

    public void setGp3(String gp3) {
        this.gp3 = gp3;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdName() {
        return idName;
    }

    public void setIdName(String idName) {
        this.idName = idName;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
