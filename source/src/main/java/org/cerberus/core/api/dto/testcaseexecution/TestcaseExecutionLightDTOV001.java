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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.views.View;

@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({
        "testcaseExecutionId",
        "queueId",
        "test",
        "testcase",
        "environment",
        "country",
        "application",
        "tag",
        "controlStatus",
        "controlMessage",
        "progressPercent",
        "doneCount",
        "totalCount"
})
@Schema(name = "TestcaseExecutionLight")
public class TestcaseExecutionLightDTOV001 {

    @JsonView(View.Public.GET.class)
    @Schema(description = "testcaseExecutionId")
    private long testcaseExecutionId;

    @JsonView(View.Public.GET.class)
    @Schema(description = "queueId")
    private long queueId;

    @JsonView(View.Public.GET.class)
    @Schema(description = "test")
    private String test;

    @JsonView(View.Public.GET.class)
    @Schema(description = "testcase")
    private String testcase;

    @JsonView(View.Public.GET.class)
    @Schema(description = "environment")
    private String environment;

    @JsonView(View.Public.GET.class)
    @Schema(description = "country")
    private String country;

    @JsonView(View.Public.GET.class)
    @Schema(description = "application")
    private String application;

    @JsonView(View.Public.GET.class)
    @Schema(description = "tag")
    private String tag;

    @JsonView(View.Public.GET.class)
    @Schema(description = "controlStatus")
    private String controlStatus;

    @JsonView(View.Public.GET.class)
    @Schema(description = "controlMessage")
    private String controlMessage;

    @JsonView(View.Public.GET.class)
    @Schema(description = "progressPercent")
    private int progressPercent;

    @JsonView(View.Public.GET.class)
    @Schema(description = "doneCount")
    private int doneCount;

    @JsonView(View.Public.GET.class)
    @Schema(description = "totalCount")
    private int totalCount;
}
