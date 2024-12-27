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
package org.cerberus.core.enums;

/**
 * Enumeration that saves the codes used by the messages.
 * @author FNogueira
 */
public enum MessageCodeEnum {
    
    GENERIC_CODE_SUCCESS("OK"),
    GENERIC_CODE_WARNING("WARNING"),
    GENERIC_CODE_ERROR("KO");
    
    private final String codeString;

    public String getCodeString() {
        return codeString;
    }
    private MessageCodeEnum(String code){
        this.codeString = code;
    }
    
}
