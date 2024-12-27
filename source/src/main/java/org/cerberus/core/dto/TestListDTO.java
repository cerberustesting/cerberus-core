/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Data transfer object that retrieves the list of tests that have test cases
 * that use the property
 *
 * @author FNogueira
 */
public class TestListDTO {

    private String test;
    private String description;

    public List<TestCaseListDTO> testCaseList;

    public TestListDTO() {
        this.testCaseList = new ArrayList<>();
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TestCaseListDTO> getTestCaseList() {
        return testCaseList;
    }

    public void setTestCaseList(List<TestCaseListDTO> testCaseList) {
        this.testCaseList = testCaseList;
    }

}
