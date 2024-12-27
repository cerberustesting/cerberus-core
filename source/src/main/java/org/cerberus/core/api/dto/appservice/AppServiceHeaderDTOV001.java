/*
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

package org.cerberus.core.api.dto.appservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.views.View;

@ToString
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ApiModel(value = "ServiceHeader")
public class AppServiceHeaderDTOV001 {

    @NotEmpty
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class,})
    @ApiModelProperty(position = 0)
    private String service;

    @NotEmpty
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class,})
    @ApiModelProperty(position = 1)
    private String key;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 2)
    private String value;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 3)
    private int sort;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 4)
    @JsonProperty("isActive")
    private boolean isActive;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 5)
    private String description;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 6)
    private String usrCreated;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 7)
    private String dateCreated;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 8)
    private String usrModif;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 9)
    private String dateModif;
}
