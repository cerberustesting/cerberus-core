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
package org.cerberus.core.api.dto.application;

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
@JsonInclude(JsonInclude.Include.ALWAYS)
@ApiModel(value = "ApplicationEnvironments")
public class CountryEnvironmentParametersDTOV001 {

    @NotEmpty
    @JsonView(value = {View.Public.GET.class,})
    @ApiModelProperty(position = 0)
    private String system;

    @NotEmpty
    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class,})
    @ApiModelProperty(position = 1)
    private String country;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 2)
    private String environment;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 3)
    private String application;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 4)
    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 5)
    private String endPoint;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 6)
    private String contextRoot;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 7)
    private String urlLogin;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 8)
    private String domain;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 9)
    private String var1;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 10)
    private String var2;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 11)
    private String var3;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 12)
    private String var4;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 13)
    private String secret1;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 14)
    private String secret2;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 15)
    private Integer poolSize;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 16)
    private String mobileActivity;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 17)
    private String mobilePackage;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(example = "cerberus", position = 18)
    private String usrCreated;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(example = "2012-06-19 09:56:40.0", position = 19)
    private String dateCreated;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(example = "cerberus", position = 20)
    private String usrModif;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(example = "2019-04-06 10:15:09.0", position = 21)
    private String dateModif;

}
