/*
 * Cerberus  Copyright (C) 2013  vertigo17
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.refactor;

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
