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
 * Enumeration that defines the types of files that can be generated through export options.
 * @author FNogueira
 */
public enum ExportServiceEnum {
    XLSX(1, "XLSX", ".xlsx"),
    JSON(2, "JSON", ".json");
    
    private final int code;
    private final String description;
    private final String fileExtension;
    
    private ExportServiceEnum(int code, String description, String fileExtension) {
        this.code = code;
        this.description = description; 
        this.fileExtension = fileExtension;
    }
     
    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    } 
    
    public String getFileExtension(){
        return fileExtension;
    }
}
