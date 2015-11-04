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
package org.cerberus.service.engine.testdata;

import java.util.HashMap;
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.util.answer.AnswerItem;

/**
 * Stores the results associated with a certain testdatalib
 * @author FNogueira
 */
public abstract class TestDataLibResult {
    /**
     * Values currently loaded for the test data lib
     */
    protected HashMap<String, String> values;
    /**
     * Unique identifier of the testdatalib
     */
    protected int testDataLibID;
    /**
     * Type for the Result
     */
    protected  String type;
      
    public TestDataLibResult(){
        this.values = new HashMap<String, String>();
    }
    
    public int getTestDataLibID() {
        return testDataLibID;
    }

    public void setTestDataLibID(int testDataLibID) {
        this.testDataLibID = testDataLibID;
    }
    
    public String getValue(String key){
        return values.get(key);
    }
    public String getType() {
        return type;
    }
    public abstract AnswerItem<String> getValue(TestDataLibData entry);
}
