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
package org.cerberus.core.api.dto.invariant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import javax.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.views.View;

/**
 * @author mlombard
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
        "idName",
        "value",
        "sort",
        "description",
        "shortDescription",
        "attribute1",
        "attribute2",
        "attribute3",
        "attribute4",
        "attribute5",
        "attribute6",
        "attribute7",
        "attribute8",
        "attribute9"
})
@Schema(name = "Invariant")
public class InvariantDTOV001 {

    @JsonView(View.Public.GET.class)
    @Schema(description = "Invariant identifier name", example = "ACTION")
    private String idName;

    @NotBlank(message = "Value is mandatory")
    @JsonView({View.Public.GET.class, View.Public.POST.class})
    @Schema(description = "Invariant value", example = "click", required = true)
    private String value;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Sort order", example = "3000")
    private Integer sort;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Description", example = "click")
    private String description;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Short description", example = "null")
    private String shortDescription;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Attribute 1", example = "null")
    private String attribute1;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Attribute 2", example = "null")
    private String attribute2;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Attribute 3", example = "null")
    private String attribute3;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Attribute 4", example = "null")
    private String attribute4;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Attribute 5", example = "null")
    private String attribute5;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Attribute 6", example = "null")
    private String attribute6;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Attribute 7", example = "null")
    private String attribute7;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Attribute 8", example = "null")
    private String attribute8;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Attribute 9", example = "null")
    private String attribute9;
}