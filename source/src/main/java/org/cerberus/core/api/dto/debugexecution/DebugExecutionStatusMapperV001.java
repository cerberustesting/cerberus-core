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

import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.springframework.stereotype.Component;

/**
 * Builds the {@link DebugExecutionStatusDTOV001} snapshot shared by the debug REST status
 * endpoint ({@code DebugExecutionService#getStatus}) and the WebSocket delta push
 * ({@code WebSocketService#notifyDebugPending}/{@code #notifyDebugFinished}), so both stay in
 * sync on field names/shape instead of two hand-rolled payloads drifting apart.
 */
@Component
public class DebugExecutionStatusMapperV001 {

    public DebugExecutionStatusDTOV001 toDTO(TestCaseExecution execution, boolean waiting,
            TestCaseStepAction pendingAction, TestCaseStepActionControl pendingControl, boolean pendingFailed) {
        return DebugExecutionStatusDTOV001.builder()
                .executionUUID(execution.getExecutionUUID())
                .executionId(execution.getId())
                .state(waiting ? "WAITING_FOR_NEXT" : "RUNNING")
                .controlStatus(execution.getControlStatus())
                .pendingFailed(pendingFailed)
                .pendingStepId(pendingAction != null ? pendingAction.getStepId() : null)
                .pendingActionId(pendingAction != null ? pendingAction.getActionId() : null)
                .pendingActionDescription(pendingAction != null ? pendingAction.getDescription() : null)
                .pendingControlId(pendingControl != null ? pendingControl.getControlId() : null)
                .pendingControlDescription(pendingControl != null ? pendingControl.getDescription() : null)
                // Never persisted to DB (in-memory only on the running execution object) : this is
                // the only way for either consumer to observe them while the session is live.
                .remoteLiveUrl(execution.getRemoteLiveUrl())
                .remoteControlLiveUrl(execution.getRemoteControlLiveUrl())
                .build();
    }

    public DebugExecutionStatusDTOV001 toFinishedDTO(TestCaseExecution execution) {
        return DebugExecutionStatusDTOV001.builder()
                .executionUUID(execution.getExecutionUUID())
                .executionId(execution.getId())
                .state("FINISHED")
                .controlStatus(execution.getControlStatus())
                .build();
    }
}