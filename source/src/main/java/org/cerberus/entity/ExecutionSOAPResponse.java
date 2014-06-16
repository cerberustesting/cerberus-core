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
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 *
 * @author bcivel
 */
@Component
public class ExecutionSOAPResponse {
    private HashMap executionHashMap;
    
    @PostConstruct
    public void init(){
    executionHashMap = new HashMap();
    }
    
    
    public void setExecutionSOAPResponse(long executionID, String soapResponse){
    executionHashMap.put("SOAP"+executionID, soapResponse);
    }
    
    public void removeExecutionSOAPResponse(long executionID) {
        executionHashMap.remove("SOAP"+executionID);
    }

    public String getExecutionSOAPResponse(long executionID) {
        return executionHashMap.get("SOAP"+executionID).toString();
    }
}
