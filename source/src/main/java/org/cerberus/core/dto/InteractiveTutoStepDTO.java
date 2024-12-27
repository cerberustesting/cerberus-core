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
package org.cerberus.core.dto;

import org.cerberus.core.crud.entity.InteractiveTutoStepType;

public class InteractiveTutoStepDTO {
    private String selectorJquery;
    private String text;
    private String attr1;
    private InteractiveTutoStepType type;

    public InteractiveTutoStepDTO(int id, String selectorJquery, String text, InteractiveTutoStepType type, String attr1) {
        this.selectorJquery = selectorJquery;
        this.text = text;
        this.type = type;
        this.attr1=attr1;
    }

    public String getSelectorJquery() {
        return selectorJquery;
    }

    public void setSelectorJquery(String selectorJquery) {
        this.selectorJquery = selectorJquery;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public InteractiveTutoStepType getType() {
        return type;
    }

    public void setType(InteractiveTutoStepType type) {
        this.type = type;
    }

    public String getAttr1() {
        return attr1;
    }

    public void setAttr1(String attr1) {
        this.attr1 = attr1;
    }
}
