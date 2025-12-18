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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.invariant.InvariantDTOV001;
import org.cerberus.core.api.dto.testcasestep.TestcaseStepDTOV001;
import org.cerberus.core.api.dto.views.View;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * @author bcivel
 */
@ToString
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
        "totalCount",
        "createdLast30Days",
        "workingCount",
        "modificationsLast30Days",
        "modificationsPrevious30Days",
        "executedAtLeastOnce"})
@Schema(name = "TestcaseStats")
public class TestcaseStatsDTOV001 {

    @JsonView({View.Public.GET.class})
    @Schema(description = "Total number of test cases", example = "1542")
    private Integer totalCount;

    @JsonView({View.Public.GET.class})
    @Schema(description = "Number of test cases created in the last 30 days", example = "52")
    private Integer createdLast30Days;

    @JsonView({View.Public.GET.class})
    @Schema(description = "Number of test cases currently in WORKING status", example = "214")
    private Integer workingCount;

    @JsonView({View.Public.GET.class})
    @Schema(description = "Number of modifications in the last 30 days", example = "318")
    private Integer modificationsLast30Days;

    @JsonView({View.Public.GET.class})
    @Schema(description = "Number of modifications between 30 and 60 days ago", example = "290")
    private Integer modificationsPrevious30Days;

    @JsonView({View.Public.GET.class})
    @Schema(description = "Number of test cases executed at least once", example = "1128")
    private Integer executedAtLeastOnce;
}