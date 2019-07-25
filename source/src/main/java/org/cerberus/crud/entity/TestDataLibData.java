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

/**
 *
 * @author vertigo17
 */
public class TestDataLibData {
    private Integer testDataLibDataID;

    private Integer testDataLibID;
    private String subData;
    private String encrypt;
    private String value;
    private String column;
    private String parsingAnswer;
    private String columnPosition;
    private String description;

    
    public Integer getTestDataLibDataID() {
        return testDataLibDataID;
    }

    public void setTestDataLibDataID(Integer testDataLibDataID) {
        this.testDataLibDataID = testDataLibDataID;
    }
    
    public Integer getTestDataLibID() {
        return testDataLibID;
    }

    public void setTestDataLibID(Integer testDataLibID) {
        this.testDataLibID = testDataLibID;
    }

    public String getSubData() {
        return subData;
    }

    public void setSubData(String subData) {
        this.subData = subData;
    }

    public String getEncrypt() {
        return encrypt;
    }

    public void setEncrypt(String encrypt) {
        this.encrypt = encrypt;
    }
    
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getParsingAnswer() {
        return parsingAnswer;
    }

    public void setParsingAnswer(String parsingAnswer) {
        this.parsingAnswer = parsingAnswer;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColumnPosition() {
        return columnPosition;
    }

    public void setColumnPosition(String columnPosition) {
        this.columnPosition = columnPosition;
    }

    public boolean hasSameKey(TestDataLibData obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestDataLibData other = (TestDataLibData) obj;
        if ((this.testDataLibID == null) ? (other.testDataLibID != null) : !this.testDataLibID.equals(other.testDataLibID)) {
            return false;
        }
        if ((this.subData == null) ? (other.subData != null) : !this.subData.equals(other.subData)) {
            return false;
        }
        if((this.testDataLibDataID == null ) ? (other.testDataLibDataID != null) : !this.testDataLibDataID.equals(other.testDataLibDataID)) {
        	return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.testDataLibID != null ? this.testDataLibID.hashCode() : 0);
        hash = 29 * hash + (this.subData != null ? this.subData.hashCode() : 0);
        hash = 29 * hash + (this.encrypt != null ? this.encrypt.hashCode() : 0);
        hash = 29 * hash + (this.value != null ? this.value.hashCode() : 0);
        hash = 29 * hash + (this.column != null ? this.column.hashCode() : 0);
        hash = 29 * hash + (this.columnPosition != null ? this.columnPosition.hashCode() : 0);
        hash = 29 * hash + (this.parsingAnswer != null ? this.parsingAnswer.hashCode() : 0);
        hash = 29 * hash + (this.description != null ? this.description.hashCode() : 0);
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
        final TestDataLibData other = (TestDataLibData) obj;
        if ((this.testDataLibID == null) ? (other.testDataLibID != null) : !this.testDataLibID.equals(other.testDataLibID)) {
            return false;
        }
        if ((this.subData == null) ? (other.subData != null) : !this.subData.equals(other.subData)) {
            return false;
        }
        if ((this.encrypt == null) ? (other.encrypt != null) : !this.encrypt.equals(other.encrypt)) {
            return false;
        }
        if ((this.value == null) ? (other.value != null) : !this.value.equals(other.value)) {
            return false;
        }
        if ((this.column == null) ? (other.column != null) : !this.column.equals(other.column)) {
            return false;
        }
        if ((this.columnPosition == null) ? (other.columnPosition != null) : !this.columnPosition.equals(other.columnPosition)) {
            return false;
        }
        if ((this.parsingAnswer == null) ? (other.parsingAnswer != null) : !this.parsingAnswer.equals(other.parsingAnswer)) {
            return false;
        }
        if ((this.description == null) ? (other.description != null) : !this.description.equals(other.description)) {
            return false;
        }
        return true;
    }
    
    
}
