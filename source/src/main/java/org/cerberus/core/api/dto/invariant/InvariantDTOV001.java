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
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
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
@ApiModel(value = "Invariant")
public class InvariantDTOV001 {

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(example = "ACTION", position = 0)
    private String idName;

    @NotBlank(message = "Value is mandatory")
    @JsonView(value = {View.Public.GET.class, View.Public.POST.class})
    @ApiModelProperty(example = "click", position = 1)
    private String value;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(example = "3000", position = 2)
    private Integer sort;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(example = "click", position = 3)
    private String description;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(example = "null", position = 4)
    private String shortDescription;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(example = "null", position = 5)
    private String attribute1;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(example = "null", position = 6)
    private String attribute2;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(example = "null", position = 7)
    private String attribute3;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(example = "null", position = 8)
    private String attribute4;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(example = "null", position = 9)
    private String attribute5;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(example = "null", position = 10)
    private String attribute6;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(example = "null", position = 11)
    private String attribute7;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(example = "null", position = 12)
    private String attribute8;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(example = "null", position = 13)
    private String attribute9;
}
 