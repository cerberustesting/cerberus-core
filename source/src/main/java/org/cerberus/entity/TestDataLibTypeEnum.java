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

/**
 * Specifies the types of test data lib that are available in the library.
 * @author FNogueira
 */
public enum TestDataLibTypeEnum {
    STATIC("STATIC", "STATIC"),
    SQL("SQL", "SQL"),
    SOAP("SOAP", "SOAP");
    
    private final String code;
    private final String description;
    
    private TestDataLibTypeEnum(String code, String description) {
        this.code = code;
        this.description = description; 
    }
     
    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    } 
}
