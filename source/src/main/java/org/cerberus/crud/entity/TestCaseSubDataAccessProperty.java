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
package org.cerberus.crud.entity;

/**
 * Auxiliary class that represents an access to a sub-data entry in the format of Entry(key).
 * @author FNogueira
 */
public class TestCaseSubDataAccessProperty extends TestCaseCountryProperties{
    private String accessName; 
    private String libraryValue;    
    private String subDataValue; 
    
    private TestCaseCountryProperties propertyLibEntry;

    public TestCaseCountryProperties getPropertyLibEntry() {
        return propertyLibEntry;
    }

    public void setPropertyLibEntry(TestCaseCountryProperties propertyLibEntry) {
        this.propertyLibEntry = propertyLibEntry;
    }
    public String getAccessName() {
        return accessName;
    }

    public void setAccessName(String accessName) {
        this.accessName = accessName;
    }

    public String getSubDataValue() {
        return subDataValue;
    }

    public void setSubDataValue(String subDataValue) {
        this.subDataValue = subDataValue;
    }

    public String getLibraryValue() {
        return libraryValue;
    }

    public void setLibraryValue(String libraryValue) {
        this.libraryValue = libraryValue;
    }
    
}
