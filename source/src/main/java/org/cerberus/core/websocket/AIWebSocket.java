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
        LOG.debug("Received message: " + message.getPayload());
        try {
            MessageTestCreationAI incoming = objectMapper.readValue(message.getPayload(), MessageTestCreationAI.class);

            if(incoming.getSubject().equals("test_proposal")) {
                aiService.generateTestCaseProposal(incoming.getSender(), session, incoming.getSessionID(), incoming.getContent(), incoming.getApplication(), incoming.getTestFolder());
            } else if (incoming.getSubject().equals("test_creation")) {
                aiService.createTestCaseAndGenerateContent(incoming.getSender(), session, incoming.getSessionID(), incoming.getTestFolder(), incoming.getTestcaseObject(), incoming.getTestDetailedDescription(), incoming.getTempId());
            } else if (incoming.getSubject().equals("chat_with_ai")){
                aiService.chatWithAI(incoming.getSender(), session, incoming.getSessionID(), incoming.getContent());
            } else if (incoming.getSubject().equals("execution_debug_assistant")){
                aiService.executionDebugWithAI(incoming.getSender(), session, incoming.getSessionID(), Integer.valueOf(incoming.getContent()));
            } else if (incoming.getSubject().equals("ao_init")){
                aiService.chatWithAI(incoming.getSender(), session, incoming.getSessionID(), incoming.getContent());
            } else if (incoming.getSubject().equals("ao_generate")){
                aiService.generateApplicationObjectProposalWithAI(incoming.getSender(),session,incoming.getSessionID(),incoming.getApplication(),incoming.getPage(),incoming.getHtmlPath(),
                        incoming.getScreenshotPath(),incoming.getTargets());
            }

        } catch (Exception ex){
            LOG.error("Exception handling WebSocket message", ex);
            // Envoie un message d'erreur au client
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                    new MessageAI("system", "error", "Unexpected error: " + ex.getMessage())
            )));
        }
    }

}

