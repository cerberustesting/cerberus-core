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

import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.enums.TestDataLibTypeEnum;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.answer.AnswerItem;

/**
 *
 * @author FNogueira
 */
public class TestDataLibResultStatic extends TestDataLibResult {
    
    public TestDataLibResultStatic(){
        this.type = TestDataLibTypeEnum.STATIC.getCode();
    }   
    
    @Override
    public AnswerItem<String> getValue(TestDataLibData entry) {        
        
        AnswerItem ansGetValue = new AnswerItem(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIBDATA));
        String keytoRetrieve = entry.getSubData();
        
        if(!values.containsKey(keytoRetrieve)){
            values.put(keytoRetrieve, entry.getValue());
        }        
               
        ansGetValue.setItem(this.getValue(keytoRetrieve));
                
        return ansGetValue;
    }
    
}
