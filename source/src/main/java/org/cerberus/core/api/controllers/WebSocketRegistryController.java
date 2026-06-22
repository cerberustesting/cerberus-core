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
package org.cerberus.core.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.cerberus.core.websocket.WebSocketEventSender;
import org.cerberus.core.websocket.WebSocketSessionRegistry;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@Tag(name = "WebSocket", description = "Endpoints related to WebSocket session monitoring")
@RestController
@RequestMapping(path = "/public/ws")
public class WebSocketRegistryController {

    private final WebSocketSessionRegistry registry;
    private final WebSocketEventSender sender;

    @GetMapping(path = "/registry/status", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "WebSocket registry status",
            description = "Returns active sessions with metadata and message counts.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Registry status")
            }
    )
    public RegistryStatusResponse status() {
        List<SessionStatusResponse> sessions = registry.getSessions().stream()
                .map(s -> new SessionStatusResponse(
                        s.wsId(),
                        s.user(),
                        s.appSessionId(),
                        s.channels(),
                        s.open(),
                        sender.getMessageCount(s.wsId())
                ))
                .toList();

        return new RegistryStatusResponse(sessions.size(), sender.getTotalMessagesSent(), sessions);
    }

    public record RegistryStatusResponse(
            int totalSessions,
            long totalMessagesSent,
            List<SessionStatusResponse> sessions
    ) {}

    public record SessionStatusResponse(
            String wsId,
            String user,
            String appSessionId,
            List<String> channels,
            boolean open,
            long messagesSent
    ) {}
}