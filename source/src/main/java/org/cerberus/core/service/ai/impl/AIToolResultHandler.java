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
package org.cerberus.core.service.ai.impl;

import com.anthropic.models.messages.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cerberus.core.api.dto.ai.AISuggestionDTO;
import org.cerberus.core.crud.entity.*;
import org.cerberus.core.crud.service.impl.*;
import org.cerberus.core.websocket.WebSocketEventSender;
import org.cerberus.core.websocket.WebSocketStatic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AIToolResultHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AIToolResultHandler.class);

    private final ObjectMapper objectMapper;

    @Autowired
    private final WebSocketEventSender webSocketEventSender;

    public AIToolResultHandler(
            ObjectMapper objectMapper,
            WebSocketEventSender webSocketEventSender
    ) {
        this.objectMapper = objectMapper;
        this.webSocketEventSender = webSocketEventSender;
    }

    public void clearSuggestions(String aiSessionID) {
        if (aiSessionID == null || aiSessionID.isBlank()) {
            return;
        }

        webSocketEventSender.sendToAppSession(
                aiSessionID,
                WebSocketStatic.CHANNEL_CHAT_SUGGESTIONS,
                Map.of("mode", "clear")
        );
    }

    /**
     * Handles a call to the client-side {@code propose_quick_replies} pseudo-tool: pushes the
     * suggestions Claude produced (already phrased in the conversation's language) to the
     * frontend, and returns a synthetic tool_result to feed back to Claude.
     */
    public String handleQuickReplies(String aiSessionID, String toolName, Map<String, Object> arguments) {
        if (aiSessionID == null || aiSessionID.isBlank()) {
            return "{\"status\":\"skipped\"}";
        }

        Object rawSuggestions = arguments == null ? null : arguments.get("suggestions");
        if (!(rawSuggestions instanceof List<?> list) || list.isEmpty()) {
            return "{\"status\":\"skipped\",\"reason\":\"no suggestions provided\"}";
        }

        try {
            List<AISuggestionDTO> suggestions = objectMapper.convertValue(
                    list,
                    new TypeReference<List<AISuggestionDTO>>() {}
            );

            webSocketEventSender.sendToAppSession(
                    aiSessionID,
                    WebSocketStatic.CHANNEL_CHAT_SUGGESTIONS,
                    Map.of(
                            "mode", "replace",
                            "source", "assistant",
                            "toolName", toolName,
                            "suggestions", suggestions
                    )
            );

            return "{\"status\":\"displayed\"}";
        } catch (Exception e) {
            LOG.error("Unable to parse suggestions from {} call. Arguments={}", toolName, arguments, e);
            return "{\"status\":\"error\"}";
        }
    }
}