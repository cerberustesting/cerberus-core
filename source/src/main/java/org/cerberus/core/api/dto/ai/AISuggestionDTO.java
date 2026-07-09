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
package org.cerberus.core.api.dto.ai;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AISuggestionDTO {

    private String id;
    private String label;
    private String value;
    private String type; // confirm, cancel, prompt, action

    public AISuggestionDTO() {
    }

    public AISuggestionDTO(String id, String label, String value, String type) {
        this.id = id;
        this.label = label;
        this.value = value;
        this.type = type;
    }

}
