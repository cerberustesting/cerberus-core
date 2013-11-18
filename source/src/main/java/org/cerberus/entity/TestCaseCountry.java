/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
package org.cerberus.entity;

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
