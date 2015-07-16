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
package org.cerberus.entity;

import java.util.HashMap; 

/**
 *
 * @author FNogueira
 */
public class TestDataLibResultSQL extends TestDataLibResult {
    private HashMap<String, String> rawData;
    
    public TestDataLibResultSQL(){
        this.type = TestDataLibTypeEnum.SQL.getCode();;
    }
    @Override
    public String getValue(TestDataLibData entry) {       
        if(!values.containsKey(entry.getSubData())){
            //if the map don't contain the entry that we want, we will get it
            String value = rawData.get(entry.getColumn().toUpperCase()); //columns are store in UPPERCASE
            //associates the subdata with the column data retrieved by the query
            values.put(entry.getSubData(), value);
        }
        return values.get(entry.getSubData());
    }
    
    public HashMap<String, String> getData() {
        return rawData;
    }

    public void setData(HashMap<String, String> data) {
        this.rawData = data;
    }
}
