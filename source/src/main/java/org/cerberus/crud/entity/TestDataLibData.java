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
package org.cerberus.crud.entity;

/**
 *
 * @author vertigo17
 */
public class TestDataLibData {
    private Integer testDataLibDataID;

    private Integer testDataLibID;
    private String subData;
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

}
