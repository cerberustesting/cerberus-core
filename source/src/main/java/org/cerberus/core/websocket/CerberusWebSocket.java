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
import org.cerberus.core.engine.entity.ExecutionUUID;
import org.cerberus.core.websocket.runtime.ExecutionMonitor;
import org.cerberus.core.websocket.runtime.NotificationCenter;
import org.cerberus.core.websocket.runtime.QueueStatus;
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
public class CerberusWebSocket extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private static final Logger LOG = LogManager.getLogger(CerberusWebSocket.class);

    @Autowired
    private AIService aiService;
    @Autowired
    private WebSocketSessionRegistry webSocketSessionRegistry;
    @Autowired
    private WebSocketEventSender webSocketEventSender;
    @Autowired
    private ExecutionUUID executionUUIDObject;
    @Autowired
    private ExecutionMonitor executionMonitor;
    @Autowired
    private NotificationCenter notificationCenter;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        LOG.debug("WebSocket connected: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        webSocketSessionRegistry.unregister(session);
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
                // Subscribe to channel
                case WebSocketStatic.SUBJECT_SUBSCRIBE:
                    switch (incoming.getChannel()) {
                        case WebSocketStatic.CHANNEL_AI_CHAT:
                        case WebSocketStatic.CHANNEL_AO_GENERATE:
                        case WebSocketStatic.CHANNEL_TESTCASE_CREATE:
                        case WebSocketStatic.CHANNEL_TESTCASE_PROPOSAL:
                        case WebSocketStatic.CHANNEL_EXECUTION_DEBUG:
                            webSocketSessionRegistry.register(incoming.getSender(),incoming.getChannel(), incoming.getSessionID(),session);
                            break;
                        case WebSocketStatic.CHANNEL_PAGE_EXECUTIONMONITOR:
                            //Register to Monitor Channel, return Entity content
                            webSocketSessionRegistry.register(incoming.getSender(),WebSocketStatic.CHANNEL_PAGE_EXECUTIONMONITOR, incoming.getSessionID(),session);
                            webSocketEventSender.sendToAppSession(
                                    incoming.getSessionID(),
                                    WebSocketStatic.TYPE_EXECUTION_UPDATE,
                                    WebSocketStatic.CHANNEL_PAGE_EXECUTIONMONITOR,
                                    executionMonitor.toJson(true).toMap()
                            );
                            break;
                        case WebSocketStatic.CHANNEL_PAGE_HOMEPAGE:
                            //Register to Queue Channel, return Entity content
                            webSocketSessionRegistry.register(incoming.getSender(), WebSocketStatic.CHANNEL_PAGE_HOMEPAGE, incoming.getSessionID(), session);

                            QueueStatus initial = QueueStatus.builder()
                                    .executionHashMap(executionUUIDObject.getExecutionUUIDList())
                                    .globalLimit(executionUUIDObject.getGlobalLimit())
                                    .running(executionUUIDObject.getRunning())
                                    .queueSize(executionUUIDObject.getQueueSize()).build();

                            webSocketEventSender.sendToAppSession(
                                    incoming.getSessionID(),
                                    WebSocketStatic.TYPE_QUEUE_CHANGE,
                                    WebSocketStatic.CHANNEL_PAGE_HOMEPAGE,
                                    initial.toJson(true).toMap());
                            break;
                        case WebSocketStatic.CHANNEL_PAGE_TESTCASEEXECUTION:
                            webSocketSessionRegistry.register(incoming.getSender(),WebSocketStatic.CHANNEL_PAGE_TESTCASEEXECUTION,incoming.getSessionID(),session);
                            break;
                        case WebSocketStatic.CHANNEL_NOTIFICATION:
                            //Register to Queue Channel, return Entity content
                            webSocketSessionRegistry.register(incoming.getSender(), WebSocketStatic.CHANNEL_NOTIFICATION, incoming.getSessionID(), session);
                            notificationCenter.sendInitStatus(incoming.getSender());
                            break;
                    }
                    break;
                case WebSocketStatic.SUBJECT_MESSAGE:
                    switch (incoming.getChannel()) {
                        case WebSocketStatic.CHANNEL_AI_CHAT:
                            //call AI Service
                            aiService.chatWithAI(incoming.getSender(), incoming.getSessionID(), incoming.getContent());
                            break;
                        case WebSocketStatic.CHANNEL_TESTCASE_PROPOSAL:
                            aiService.generateTestCaseProposal(
                                    incoming.getSender(),
                                    incoming.getSessionID(),
                                    incoming.getContent(),
                                    incoming.getApplication(),
                                    incoming.getTestFolder()
                            );
                            break;
                        case WebSocketStatic.CHANNEL_TESTCASE_CREATE:
                            aiService.createTestCaseAndGenerateContent(
                                    incoming.getSender(),
                                    incoming.getSessionID(),
                                    incoming.getTestFolder(),
                                    incoming.getTestcaseObject(),
                                    incoming.getTestDetailedDescription(),
                                    incoming.getTempId()
                            );
                            break;
                        case WebSocketStatic.CHANNEL_EXECUTION_DEBUG:
                            aiService.executionDebugWithAI(
                                    incoming.getSender(),
                                    incoming.getSessionID(),
                                    Integer.valueOf(incoming.getContent())
                            );
                            break;
                        case WebSocketStatic.CHANNEL_AO_GENERATECONTINUE:
                        case WebSocketStatic.CHANNEL_AO_GENERATE:
                            aiService.generateApplicationObjectProposalWithAI(
                                    incoming.getSender(),
                                    incoming.getSessionID(),
                                    incoming.getApplication(),
                                    incoming.getPage(),
                                    incoming.getHtmlPath(),
                                    incoming.getScreenshotPath(),
                                    incoming.getContent(),
                                    incoming.getChannel()
                            );
                            break;
                        default:
                            throw new IllegalArgumentException("Unsupported channel: " + incoming.getChannel());
                    }
                    break;
                case WebSocketStatic.SUBJECT_UNSUBSCRIBE:
                    // Useless at this step, because Websocket is created at each page load
                    // TO IMPLEMENT IF MIGRATION INTO SPA APPLICATION
                    break;
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
}

