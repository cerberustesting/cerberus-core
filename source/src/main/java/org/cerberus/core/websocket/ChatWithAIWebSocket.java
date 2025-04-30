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
public class ChatWithAIWebSocket extends TextWebSocketHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private static final Logger LOG = LogManager.getLogger(ChatWithAIWebSocket.class);

    @Autowired
    AIService aiService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        LOG.warn("afterConnectionEstablished");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        LOG.debug("Received" + message.toString());
        try {
            MessageAI incoming = objectMapper.readValue(message.getPayload(), MessageAI.class);
            MessageAI broadcast = new MessageAI(incoming.getSender(), incoming.getContent());
            String payload = objectMapper.writeValueAsString(broadcast);
            StringBuilder context = new StringBuilder();
            context.append("I'm working in a Software development context, in a job related to Quality assurance (automation, tester).");
            context.append("The context of the question is related to Cerberus-testing, a low code testing framework.");
            context.append("Respond in HTML format, including any formatting like bold, lists, icon, and code inside proper HTML tags.The maximum font-size cannot exceed 18px");
            context.append(payload);

            aiService.askClaude(incoming.getSender(), session, context.toString());
        } catch (Exception ex){
            LOG.warn("Exception" + ex.toString());
        }
    }
}