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
package org.cerberus.core.crud.entity;

/**
 *
 * @author bcivel
 */
public class Documentation {

    private String docTable;
    private String docField;
    private String docValue;
    private String docLabel;
    private String docDesc;
    private String docAnchor;

    private boolean havedocDesc; // true if doc Desc is feed.
    private boolean haveDocAnchor; // true if doc Anchor is feed.

    public boolean isHavedocDesc() {
        return havedocDesc;
    }

    public void setHavedocDesc(boolean havedocDesc) {
        this.havedocDesc = havedocDesc;
    }

    public String getDocDesc() {
        return docDesc;
    }

    public void setDocDesc(String docDesc) {
        this.docDesc = docDesc;
    }

    public String getDocField() {
        return docField;
    }

    public void setDocField(String docField) {
        this.docField = docField;
    }

    public String getDocLabel() {
        return docLabel;
    }

    public void setDocLabel(String docLabel) {
        this.docLabel = docLabel;
    }

    public String getDocTable() {
        return docTable;
    }

    public void setDocTable(String docTable) {
        this.docTable = docTable;
    }

    public String getDocValue() {
        return docValue;
    }

    public void setDocValue(String docValue) {
        this.docValue = docValue;
    }

    public String getDocAnchor() {
        return docAnchor;
    }

    public void setDocAnchor(String docAnchor) {
        this.docAnchor = docAnchor;
    }

    public boolean isHaveDocAnchor() {
        return haveDocAnchor;
    }

    public void setHaveDocAnchor(boolean haveDocAnchor) {
        this.haveDocAnchor = haveDocAnchor;
    }

}
