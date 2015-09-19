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
 * Auxiliary class that stores the values of a testdatalib (updated values) and the original key that is used as part of the 
 * primary key.
 * @author FNogueira
 */
public class TestDataLibDataUpdate {
    
    private TestDataLibData modifiedObject;
    private String subDataOriginalKey;


    public TestDataLibDataUpdate(TestDataLibData subDataObject, String subDataOriginalKey){
        this.modifiedObject = subDataObject;    
        this.subDataOriginalKey = subDataOriginalKey;
    }

    public String getSubDataOriginalKey() {
        return subDataOriginalKey;
    }

    public void setSubDataOriginalKey(String subDataOriginalKey) {
        this.subDataOriginalKey = subDataOriginalKey;
    }

    public TestDataLibData getModifiedObject() {
        return modifiedObject;
    }

    public void setModifiedObject(TestDataLibData modifiedObject) {
        this.modifiedObject = modifiedObject;
    }
    
}
