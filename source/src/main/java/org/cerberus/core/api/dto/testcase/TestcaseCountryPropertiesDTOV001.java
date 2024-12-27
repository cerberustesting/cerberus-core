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

package org.cerberus.core.api.dto.testcase;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.invariant.InvariantDTOV001;
import org.cerberus.core.api.dto.views.View;

/**
 * @author MorganLmd
 */
@Data
@Builder
@Jacksonized
@ApiModel(value = "TestcaseCountryProperties")
public class TestcaseCountryPropertiesDTOV001 {

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    @ApiModelProperty(position = 0)
    private String testFolderId;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    @ApiModelProperty(position = 1)
    private String testcaseId;

    @NotBlank(message = "Property is mandatory")
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 2)
    private String property;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 3)
    private String description;

    @NotBlank(message = "Message is mandatory")
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 4)
    private String type;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 5)
    private String database;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 6)
    private String value1;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 7)
    private String value2;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 7)
    private String value3;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 8)
    private String length;

    @NotNull(message = "Row limit is mandatory")
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 9)
    private int rowLimit;

    @NotNull(message = "Nature is mandatory")
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 10)
    private String nature;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 11)
    private int rank;

    @Valid
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 16)
    private List<InvariantDTOV001> countries;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 12)
    private String usrCreated;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 13)
    private String dateCreated;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 14)
    private String usrModif;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 15)
    private String dateModif;
}
