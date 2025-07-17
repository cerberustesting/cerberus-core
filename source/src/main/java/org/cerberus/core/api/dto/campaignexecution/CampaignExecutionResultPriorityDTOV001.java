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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
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
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({
        "okCoefficientPriorityLevel1", "okCoefficientPriorityLevel2", "okCoefficientPriorityLevel3",
        "okCoefficientPriorityLevel4", "okCoefficientPriorityLevel5",
        "nonOkExecutionsPriorityLevel1", "nonOkExecutionsPriorityLevel2", "nonOkExecutionsPriorityLevel3",
        "nonOkExecutionsPriorityLevel4", "nonOkExecutionsPriorityLevel5"
})
@Schema(name = "CampaignExecutionResultPriority")
public class CampaignExecutionResultPriorityDTOV001 {

    @JsonView(View.Public.GET.class)
    @Schema(description = "okCoefficientPriorityLevel1")
    private int okCoefficientPriorityLevel1;

    @JsonView(View.Public.GET.class)
    @Schema(description = "okCoefficientPriorityLevel2")
    private int okCoefficientPriorityLevel2;

    @JsonView(View.Public.GET.class)
    @Schema(description = "okCoefficientPriorityLevel3")
    private int okCoefficientPriorityLevel3;

    @JsonView(View.Public.GET.class)
    @Schema(description = "okCoefficientPriorityLevel4")
    private int okCoefficientPriorityLevel4;

    @JsonView(View.Public.GET.class)
    @Schema(description = "okCoefficientPriorityLevel5")
    private int okCoefficientPriorityLevel5;

    @JsonView(View.Public.GET.class)
    @Schema(description = "nonOkExecutionsPriorityLevel1")
    private int nonOkExecutionsPriorityLevel1;

    @JsonView(View.Public.GET.class)
    @Schema(description = "nonOkExecutionsPriorityLevel2")
    private int nonOkExecutionsPriorityLevel2;

    @JsonView(View.Public.GET.class)
    @Schema(description = "nonOkExecutionsPriorityLevel3")
    private int nonOkExecutionsPriorityLevel3;

    @JsonView(View.Public.GET.class)
    @Schema(description = "nonOkExecutionsPriorityLevel4")
    private int nonOkExecutionsPriorityLevel4;

    @JsonView(View.Public.GET.class)
    @Schema(description = "nonOkExecutionsPriorityLevel5")
    private int nonOkExecutionsPriorityLevel5;
}