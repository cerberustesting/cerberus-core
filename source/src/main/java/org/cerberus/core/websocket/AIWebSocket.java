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
package org.cerberus.core.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.service.ai.impl.AIService;
import org.cerberus.core.service.ai.impl.MessageAI;
import org.cerberus.core.service.ai.impl.MessageTestCreationAI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class AIWebSocket extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private static final Logger LOG = LogManager.getLogger(AIWebSocket.class);

    @Autowired
    private AIService aiService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        LOG.debug("WebSocket connected: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        LOG.debug("WebSocket closed: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        LOG.debug("Received AI WebSocket message: {}", message.getPayload());

        try {
            MessageTestCreationAI incoming =
                    objectMapper.readValue(message.getPayload(), MessageTestCreationAI.class);

            String subject = incoming.getSubject();

            if (subject == null || subject.isBlank()) {
                throw new IllegalArgumentException("Missing subject");
            }

            if (incoming.getSessionID() == null || incoming.getSessionID().isBlank()) {
                throw new IllegalArgumentException("Missing sessionID");
            }

            switch (subject) {
                case "chat_with_ai":
                    aiService.chatWithAI(
                            incoming.getSender(),
                            session,
                            incoming.getSessionID(),
                            incoming.getContent()
                    );
                    break;

                case "test_proposal":
                    aiService.generateTestCaseProposal(
                            incoming.getSender(),
                            session,
                            incoming.getSessionID(),
                            incoming.getContent(),
                            incoming.getApplication(),
                            incoming.getTestFolder()
                    );
                    break;

                case "test_creation":
                    aiService.createTestCaseAndGenerateContent(
                            incoming.getSender(),
                            session,
                            incoming.getSessionID(),
                            incoming.getTestFolder(),
                            incoming.getTestcaseObject(),
                            incoming.getTestDetailedDescription(),
                            incoming.getTempId()
                    );
                    break;

                case "execution_debug_assistant":
                    aiService.executionDebugWithAI(
                            incoming.getSender(),
                            session,
                            incoming.getSessionID(),
                            Integer.valueOf(incoming.getContent())
                    );
                    break;

                case "ao_generate":
                case "ao_generate_continue":
                    aiService.generateApplicationObjectProposalWithAI(
                            incoming.getSender(),
                            session,
                            incoming.getSessionID(),
                            incoming.getApplication(),
                            incoming.getPage(),
                            incoming.getHtmlPath(),
                            incoming.getScreenshotPath(),
                            incoming.getContent(),
                            incoming.getSubject()
                    );
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported subject: " + subject);
            }

        } catch (Exception ex) {
            LOG.error("Exception handling AI WebSocket message", ex);

            if (session.isOpen()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                        new MessageAI("system", "error", "Unexpected error: " + ex.getMessage())
                )));
            }
        }
    }

    private void sendSystemMessage(WebSocketSession session, String type, String sessionID) throws Exception {
        if (!session.isOpen()) {
            return;
        }

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                new MessageAI("system", type, sessionID)
        )));
    }

}

