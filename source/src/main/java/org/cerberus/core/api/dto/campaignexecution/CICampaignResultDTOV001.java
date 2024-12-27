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
import com.fasterxml.jackson.databind.JsonNode;
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
@ApiModel(value = "CICampaignExecutionResult")
public class CICampaignResultDTOV001 {

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 0)
    private String globalResult;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 1)
    private String campaignExecutionId;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 2)
    private int calculatedResult;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 3)
    private int resultThreshold;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 4)
    private JsonNode detailByDeclinations;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 5)
    private JsonNode environments;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 6)
    private JsonNode countries;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 7)
    private JsonNode robotDeclinations;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 8)
    private JsonNode systems;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 9)
    private JsonNode applications;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 10)
    private CampaignExecutionResultDTOV001 result;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 11)
    private CampaignExecutionResultPriorityDTOV001 resultByPriority;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 12)
    private String executionStart;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 13)
    private String executionEnd;

}
