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
        "tag",
        "nbExecutions",
        "testcasesNotExist",
        "testcasesNotActive",
        "testcasesNotAllowedOnEnvironment",
        "environmentsNotExistOrNotActive",
        "robotsMissing",
        "queuedEntries",
        "messages"
})
@Schema(name = "QueuedExecutionResult")
public class QueuedExecutionResultDTOV001 {

    @JsonView(View.Public.GET.class)
    @Schema(description = "Execution tag")
    private String tag;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Number of executions")
    private int nbExecutions;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Count of testcases not existing")
    private int testcasesNotExist;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Count of testcases not active")
    private int testcasesNotActive;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Count of testcases not allowed on environment")
    private int testcasesNotAllowedOnEnvironment;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Count of environments not existing or not active")
    private int environmentsNotExistOrNotActive;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Count of robots missing")
    private int robotsMissing;

    @JsonView(View.Public.GET.class)
    @Schema(description = "List of queued entries")
    private List<QueuedEntryDTOV001> queuedEntries;

    @JsonView(View.Public.GET.class)
    @Schema(description = "List of messages")
    private List<String> messages;
}
