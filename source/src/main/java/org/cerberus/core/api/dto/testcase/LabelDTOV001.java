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
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.views.View;

/**
 * @author MorganLmd
 */
@Data
@Builder
@Jacksonized
@ApiModel(value = "Label")
public class LabelDTOV001 {

    @NotNull(message = "Id is mandatory")
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 0)
    private Integer id;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    @ApiModelProperty(position = 1)
    private String system;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    @ApiModelProperty(position = 2)
    private String label;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    @ApiModelProperty(position = 3)
    private String type;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    @ApiModelProperty(position = 4)
    private String color;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    @ApiModelProperty(position = 5)
    private Integer parentLabelID;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    @ApiModelProperty(position = 6)
    private String requirementType;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    @ApiModelProperty(position = 7)
    private String requirementStatus;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    @ApiModelProperty(position = 8)
    private String requirementCriticality;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    @ApiModelProperty(position = 9)
    private String description;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    @ApiModelProperty(position = 10)
    private String detailedDescription;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 11)
    private String usrCreated;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 12)
    private String dateCreated;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 13)
    private String usrModif;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 14)
    private String dateModif;


}
