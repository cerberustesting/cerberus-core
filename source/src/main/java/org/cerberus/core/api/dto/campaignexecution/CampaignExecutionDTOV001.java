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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
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
@JsonPropertyOrder({
        "campaignExecutionId", "ciResult", "startDate", "endDate", "durationInMillis", "campaignId",
        "description", "result", "browserstackBuildHash", "browserstackAppBuildHash", "lambdaTestBuild", "executions"})
@Schema(name = "CampaignExecution")
public class CampaignExecutionDTOV001 {

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "campaignExecutionId")
    private String campaignExecutionId;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "ciResult")
    private String ciResult;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "startDate")
    private String startDate;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "endDate")
    private String endDate;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "durationInMillis")
    private long durationInMillis;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "campaignId")
    private String campaignId;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "description")
    private String description;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "result")
    private CampaignExecutionResultDTOV001 result;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "browserstackBuildHash")
    private String browserstackBuildHash;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "browserstackAppBuildHash")
    private String browserstackAppBuildHash;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "lambdaTestBuild")
    private String lambdaTestBuild;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "executions")
    private List<TestcaseExecutionDTOV001> executions;
}
