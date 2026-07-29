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
@Schema(name = "DebugExecutionAck")
public class DebugExecutionAckDTOV001 {

    @JsonView(View.Public.GET.class)
    @Schema(description = "UUID identifying the debug session")
    private String executionUUID;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Whether the command was accepted (a worker thread was actually waiting on it)")
    private boolean accepted;
}