/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.crud.entity;

import java.util.List;

/**
 *
 * @author bcivel
 */
public class TestCaseCountry {

    private String test;
    private String testCase;
    private String country;

    /**
     * From here are data outside database model.
     */
    private List<TestCaseCountryProperties> testCaseCountryProperty;
    private TestCase testCaseObj;

    public TestCase getTestCaseObj() {
        return testCaseObj;
    }

    public void setTestCaseObj(TestCase testCase) {
        this.testCaseObj = testCase;
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

    public boolean hasSameKey(TestCaseCountry obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestCaseCountry other = (TestCaseCountry) obj;
        if ((this.test == null) ? (other.test != null) : !this.test.equals(other.test)) {
            return false;
        }
        if ((this.testCase == null) ? (other.testCase != null) : !this.testCase.equals(other.testCase)) {
            return false;
        }
        if ((this.country == null) ? (other.country != null) : !this.country.equals(other.country)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.test != null ? this.test.hashCode() : 0);
        hash = 41 * hash + (this.testCase != null ? this.testCase.hashCode() : 0);
        hash = 41 * hash + (this.country != null ? this.country.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestCaseCountry other = (TestCaseCountry) obj;
        if ((this.test == null) ? (other.test != null) : !this.test.equals(other.test)) {
            return false;
        }
        if ((this.testCase == null) ? (other.testCase != null) : !this.testCase.equals(other.testCase)) {
            return false;
        }
        if ((this.country == null) ? (other.country != null) : !this.country.equals(other.country)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TestCaseCountry{" + "test=" + test + ", testCase=" + testCase + ", country=" + country + '}';
    }

}
