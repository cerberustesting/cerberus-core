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
package org.cerberus.core.api.dto.testcaseexecution;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.views.View;
import org.json.JSONObject;

/**
 * @author bcivel
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(name = "TestCaseExecutionMonthlyStats")
public class TestCaseExecutionMonthlyStatsDTOV001 {

    @JsonView(View.Public.GET.class)
    @Schema(description = "Global KPIs fro the last 30 days")
    private TestCaseExecutionStatsDTOV001 globalLastMonth;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Global KPIs for the previous 30 days")
    private TestCaseExecutionStatsDTOV001 globalPreviousMonth;

    @JsonView(View.Public.GET.class)
    @Schema(description = "User KPIs for the last 30 days")
    private TestCaseExecutionStatsDTOV001 systemLastMonth;

    @JsonView(View.Public.GET.class)
    @Schema(description = "User KPIs for the previous 30 days")
    private TestCaseExecutionStatsDTOV001 systemPreviousMonth;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Ongoing Executions")
    private JsonNode ongoingExecution;
}