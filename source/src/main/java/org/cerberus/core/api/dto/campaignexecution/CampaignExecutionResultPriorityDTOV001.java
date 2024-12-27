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
package org.cerberus.core.api.dto.campaignexecution;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.views.View;

/**
 * @author lucashimpens
 */
@Data
@Builder
@Jacksonized
@ApiModel(value = "CampaignExecutionResultPriority")
public class CampaignExecutionResultPriorityDTOV001 {

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 0)
    private int okCoefficientPriorityLevel1;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 1)
    private int okCoefficientPriorityLevel2;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 2)
    private int okCoefficientPriorityLevel3;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 3)
    private int okCoefficientPriorityLevel4;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 4)
    private int okCoefficientPriorityLevel5;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 5)
    private int nonOkExecutionsPriorityLevel1;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 6)
    private int nonOkExecutionsPriorityLevel2;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 7)
    private int nonOkExecutionsPriorityLevel3;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 8)
    private int nonOkExecutionsPriorityLevel4;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 9)
    private int nonOkExecutionsPriorityLevel5;
}
