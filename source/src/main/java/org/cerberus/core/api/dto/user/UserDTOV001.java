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

package org.cerberus.core.api.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@ApiModel(value = "User")
public class UserDTOV001 {

    @NotEmpty
    @ApiModelProperty(position = 0)
    @JsonView(value = {View.Public.GET.class, View.Public.POST.class, View.Public.PUT.class,})
    private String login;

    @ApiModelProperty(position = 1)
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String name;

    @NotEmpty
    @ApiModelProperty(position = 2)
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String email;

    @NotEmpty
    @ApiModelProperty(position = 3)
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String team;

    @ApiModelProperty(position = 4)
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String language;

    @ApiModelProperty(position = 5)
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String attribute01;

    @ApiModelProperty(position = 6)
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String attribute02;

    @ApiModelProperty(position = 7)
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String attribute03;

    @ApiModelProperty(position = 8)
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String attribute04;

    @ApiModelProperty(position = 9)
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String attribute05;

    @ApiModelProperty(position = 10)
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String comment;

    @ApiModelProperty(position = 11)
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String usrCreated;

    @ApiModelProperty(position = 12)
    @JsonView(value = {View.Public.GET.class})
    private String dateCreated;

    @ApiModelProperty(position = 13)
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String usrModif;

    @ApiModelProperty(position = 14)
    @JsonView(value = {View.Public.GET.class})
    private String dateModif;
}
