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
package org.cerberus.core.api.dto.application;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.views.View;

import java.util.Map;


@ToString
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
        "totalApplications",
        "totalApplicationsByType",
        "fromDate",
        "toDate"
})
@Schema(name = "ApplicationStats")
public class ApplicationStatsDTOV001 {

    @JsonView({View.Public.GET.class})
    @Schema(description = "Total number of applications", example = "120")
    private Integer totalApplications;

    @JsonView({View.Public.GET.class})
    @Schema(description = "Number of applications grouped by type",
            example = "{\"Web\":40,\"API\":50,\"Batch\":30}")
    private Map<String, Integer> totalApplicationsByType;

    @JsonView({View.Public.GET.class})
    @Schema(description = "Start date of the period", example = "2025-11-11")
    private String fromDate;

    @JsonView({View.Public.GET.class})
    @Schema(description = "End date of the period", example = "2025-12-11")
    private String toDate;
}