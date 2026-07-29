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
package org.cerberus.core.api.dto.debugexecution;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.cerberus.core.api.dto.views.View;

@ToString
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(name = "DebugExecutionStatus")
public class DebugExecutionStatusDTOV001 {

    @JsonView(View.Public.GET.class)
    @Schema(description = "UUID identifying the debug session")
    private String executionUUID;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Id of the TestCaseExecution")
    private long executionId;

    @JsonView(View.Public.GET.class)
    @Schema(description = "RUNNING (a next was just consumed, action in progress), WAITING_FOR_NEXT (paused, ready for /next) or FINISHED (execution ended, session cleared)")
    private String state;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Result of the TestCaseExecution so far (control status code)")
    private String controlStatus;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Id of the (static) step containing the pending action/control, when WAITING_FOR_NEXT — matches TestCase.steps[].stepId, not an execution-row index")
    private Integer pendingStepId;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Id of the action about to run, when WAITING_FOR_NEXT")
    private Integer pendingActionId;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Description of the action about to run, or of the action owning the pending control, when WAITING_FOR_NEXT")
    private String pendingActionDescription;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Id of the control about to run, when WAITING_FOR_NEXT and paused on a control rather than the action itself")
    private Integer pendingControlId;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Description of the control about to run, when WAITING_FOR_NEXT and paused on a control")
    private String pendingControlDescription;

    @JsonView(View.Public.GET.class)
    @Schema(description = "True when the pending action/control already failed once and is being re-offered for a retry-or-move-on decision (a \"retry\" is meaningful) rather than about to run for the first time")
    private boolean pendingFailed;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Live remote-view URL for the robot session, once the underlying Selenium Grid node has advertised one. Never persisted to DB (in-memory only), so it can't be read via ReadTestCaseExecution while the execution is running — this endpoint is the only way to observe it mid-session.")
    private String remoteLiveUrl;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Remote-control URL (\"take control\") for the robot session, same availability caveat as remoteLiveUrl.")
    private String remoteControlLiveUrl;
}