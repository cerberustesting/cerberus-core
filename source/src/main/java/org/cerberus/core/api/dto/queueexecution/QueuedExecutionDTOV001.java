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
package org.cerberus.core.api.dto.queueexecution;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.views.View;

/**
 * @author lucashimpens
 */
@ToString
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
        "testcases",
        "countries",
        "environments",
        "robots",
        "tag",
        "screenshot",
        "video",
        "verbose",
        "timeout",
        "pageSource",
        "robotLog",
        "consoleLog",
        "manualExecution",
        "retries",
        "priority",
        "manualUrl",
        "manualUrlParameters"
})
@Schema(name = "QueuedExecution")
public class QueuedExecutionDTOV001 {

    @JsonView(View.Public.POST.class)
    @Schema(description = "List of testcases to execute")
    private List<QueuedExecutionTestcaseDTOV001> testcases;

    @JsonView(View.Public.POST.class)
    @Schema(description = "List of countries")
    private List<String> countries;

    @JsonView(View.Public.POST.class)
    @Schema(description = "List of environments")
    private List<String> environments;

    @JsonView(View.Public.POST.class)
    @Schema(description = "List of robots")
    private List<String> robots;

    @JsonView(View.Public.POST.class)
    @Schema(description = "Execution tag")
    private String tag;

    @JsonView(View.Public.POST.class)
    @Schema(description = "Screenshot flag")
    private Integer screenshot;

    @JsonView(View.Public.POST.class)
    @Schema(description = "Video flag")
    private Integer video;

    @JsonView(View.Public.POST.class)
    @Schema(description = "Verbose flag")
    private Integer verbose;

    @JsonView(View.Public.POST.class)
    @Schema(description = "Timeout in seconds")
    private Integer timeout;

    @JsonView(View.Public.POST.class)
    @Schema(description = "Page source flag")
    private Integer pageSource;

    @JsonView(View.Public.POST.class)
    @Schema(description = "Robot log flag")
    private Integer robotLog;

    @JsonView(View.Public.POST.class)
    @Schema(description = "Console log flag")
    private Integer consoleLog;

    @JsonView(View.Public.POST.class)
    @Schema(description = "Manual execution flag")
    private String manualExecution;

    @JsonView(View.Public.POST.class)
    @Schema(description = "Number of retries")
    private Integer retries;

    @JsonView(View.Public.POST.class)
    @Schema(description = "Priority level")
    private Integer priority;

    @JsonView(View.Public.POST.class)
    @Schema(description = "Manual URL flag")
    private Integer manualUrl;

    @JsonView(View.Public.POST.class)
    @Schema(description = "Manual URL parameters")
    private ManualUrlParametersDTOV001 manualUrlParameters;
}