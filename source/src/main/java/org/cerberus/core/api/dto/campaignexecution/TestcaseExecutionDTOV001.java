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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.invariant.InvariantDTOV001;
import org.cerberus.core.api.dto.testcase.TestcaseDTOV001;
import org.cerberus.core.api.dto.views.View;

/**
 * @author lucashimpens
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({
        "testcaseExecutionId", "testcase", "description", "environment", "country", "priority",
        "testcaseVersion", "build", "revision", "startDate", "endDate", "durationInMillis",
        "controlStatus", "controlMessage", "robot", "url", "tag", "status", "usrExecuted",
        "queueId", "isManualExecution", "system", "application", "usrCreated", "dateCreated",
        "usrModif", "dateModif"
})
@Schema(name = "TestcaseExecution")
public class TestcaseExecutionDTOV001 {

    @JsonView(View.Public.GET.class)
    @Schema(description = "testcaseExecutionId")
    private long testcaseExecutionId;

    @JsonView(View.Public.GET.class)
    @Schema(description = "testcase")
    private TestcaseDTOV001 testcase;

    @JsonView(View.Public.GET.class)
    @Schema(description = "description")
    private String description;

    @JsonView(View.Public.GET.class)
    @Schema(description = "environment")
    private InvariantDTOV001 environment;

    @JsonView(View.Public.GET.class)
    @Schema(description = "country")
    private InvariantDTOV001 country;

    @JsonView(View.Public.GET.class)
    @Schema(description = "priority")
    private InvariantDTOV001 priority;

    @JsonView(View.Public.GET.class)
    @Schema(description = "testcaseVersion")
    private int testcaseVersion;

    @JsonView(View.Public.GET.class)
    @Schema(description = "build")
    private String build;

    @JsonView(View.Public.GET.class)
    @Schema(description = "revision")
    private String revision;

    @JsonView(View.Public.GET.class)
    @Schema(description = "startDate")
    private String startDate;

    @JsonView(View.Public.GET.class)
    @Schema(description = "endDate")
    private String endDate;

    @JsonView(View.Public.GET.class)
    @Schema(description = "durationInMillis")
    private long durationInMillis;

    @JsonView(View.Public.GET.class)
    @Schema(description = "controlStatus")
    private String controlStatus;

    @JsonView(View.Public.GET.class)
    @Schema(description = "controlMessage")
    private String controlMessage;

    @JsonView(View.Public.GET.class)
    @Schema(description = "robot")
    private CampaignExecutionRobotDTOV001 robot;

    @JsonView(View.Public.GET.class)
    @Schema(description = "url")
    private String url;

    @JsonView(View.Public.GET.class)
    @Schema(description = "tag")
    private String tag;

    @JsonView(View.Public.GET.class)
    @Schema(description = "status")
    private String status;

    @JsonView(View.Public.GET.class)
    @Schema(description = "usrExecuted")
    private String usrExecuted;

    @JsonView(View.Public.GET.class)
    @Schema(description = "queueId")
    private long queueId;

    @JsonView(View.Public.GET.class)
    @Schema(description = "isManualExecution")
    @JsonProperty("isManualExecution")
    private boolean isManualExecution;

    @JsonView(View.Public.GET.class)
    @Schema(description = "system")
    private String system;

    @JsonView(View.Public.GET.class)
    @Schema(description = "application")
    private String application;

    @JsonView(View.Public.GET.class)
    @Schema(description = "usrCreated")
    private String usrCreated;

    @JsonView(View.Public.GET.class)
    @Schema(description = "dateCreated")
    private String dateCreated;

    @JsonView(View.Public.GET.class)
    @Schema(description = "usrModif")
    private String usrModif;

    @JsonView(View.Public.GET.class)
    @Schema(description = "dateModif")
    private String dateModif;
}