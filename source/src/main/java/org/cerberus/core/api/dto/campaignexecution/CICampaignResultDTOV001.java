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
import com.fasterxml.jackson.databind.JsonNode;
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
@Schema(name = "CICampaignExecutionResult")
@JsonPropertyOrder({"globalResult","campaignExecutionId","calculatedResult","resultThreshold","detailByDeclinations","environments",
        "countries","robotDeclinations","systems","applications","result","resultByPriority","executionStart","executionEnd"
})
public class CICampaignResultDTOV001 {

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "Global result of the CI campaign (e.g. OK, KO)")
    private String globalResult;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "Unique ID of the campaign execution")
    private String campaignExecutionId;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "Numeric representation of the calculated result (e.g. percentage OK)")
    private int calculatedResult;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "Result threshold used for evaluation")
    private int resultThreshold;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "Details by declination as JSON structure")
    private JsonNode detailByDeclinations;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "Environment breakdown in JSON")
    private JsonNode environments;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "Countries involved in the campaign")
    private JsonNode countries;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "Robot declinations details")
    private JsonNode robotDeclinations;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "System(s) involved")
    private JsonNode systems;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "Applications involved")
    private JsonNode applications;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "Global result counts by status")
    private CampaignExecutionResultDTOV001 result;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "Result distribution by priority level")
    private CampaignExecutionResultPriorityDTOV001 resultByPriority;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "Execution start timestamp", example = "2025-07-16T14:33:00Z")
    private String executionStart;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "Execution end timestamp", example = "2025-07-16T14:38:00Z")
    private String executionEnd;
}