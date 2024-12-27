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

import java.util.List;

/**
 * * Data transfer object that transfers the list of test cases that use the property
 * @author FNogueira
 */
public class TestCaseListDTO {
    
    private String testCaseNumber;
    private String testCaseDescription;
    private List<PropertyListDTO> propertiesList;
    private String application;
    private String isActive;
    private String group;
    private String creator;
    private String status;

    public void setTestCaseNumber(String testCaseNumber) {
        this.testCaseNumber = testCaseNumber;
    }

    public void setTestCaseDescription(String testCaseDescription) {
        this.testCaseDescription = testCaseDescription;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getTestCaseNumber() {
        return testCaseNumber;
    }

    public String getTestCaseDescription() {
        return testCaseDescription;
    }

    public String getApplication() {
        return application;
    }

    public String isIsActive() {
        return isActive;
    }

    public String getGroup() {
        return group;
    }

    public String getCreator() {
        return creator;
    }

    public String getStatus() {
        return status;
    }
    
    public List<PropertyListDTO> getPropertiesList() {
        return propertiesList;
    }

    public void setPropertiesList(List<PropertyListDTO> propertiesList) {
        this.propertiesList = propertiesList;
    }
}
