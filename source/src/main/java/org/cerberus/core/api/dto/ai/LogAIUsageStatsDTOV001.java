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
package org.cerberus.core.api.dto.ai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.views.View;

/**
 * @author bcivel
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
        "totalInputTokens",
        "totalOutputTokens",
        "totalCost",
        "startDate",
        "endDate"
})
@Schema(name = "LogAIUsageStats")
public class LogAIUsageStatsDTOV001 {

    @JsonView(View.Public.GET.class)
    @Schema(description = "Total number of input tokens consumed in the period", example = "12450")
    private Integer totalInputTokens;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Total number of output tokens generated in the period", example = "30120")
    private Integer totalOutputTokens;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Total cost in USD based on AI usage during the period", example = "1.87")
    private double totalCost;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Start date of the aggregation period", example = "2025-01-01 00:00:00")
    private String startDate;

    @JsonView(View.Public.GET.class)
    @Schema(description = "End date of the aggregation period", example = "2025-01-31 23:59:59")
    private String endDate;
}