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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.engine.entity.ExecutionUUID;
import org.cerberus.core.service.ai.impl.AIService;
import org.cerberus.core.service.ai.impl.MessageAI;
import org.cerberus.core.service.ai.impl.MessageTestCreationAI;
import org.cerberus.core.websocket.runtime.ExecutionMonitor;
import org.cerberus.core.websocket.runtime.NotificationCenter;
import org.cerberus.core.websocket.runtime.QueueStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;

@Component
public class CerberusWebSocket extends TextWebSocketHandler {

    private static final Logger LOG = LogManager.getLogger(CerberusWebSocket.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());

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
    @Autowired
    private QueueStatus queueStatus;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        LOG.debug("WebSocket connected: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        webSocketSessionRegistry.unregister(session);
        LOG.debug("WebSocket closed: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        LOG.debug("Received Cerberus WebSocket message: {}", message.getPayload());

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
                case WebSocketStatic.SUBJECT_SUBSCRIBE:
                    handleSubscribe(incoming, session);
                    break;

                case WebSocketStatic.SUBJECT_MESSAGE:
                    handleMessage(incoming);
                    break;

                case WebSocketStatic.SUBJECT_UNSUBSCRIBE:
                    handleUnsubscribe(incoming, session);
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported subject: " + subject);
            }

        } catch (Exception ex) {
            LOG.error("Exception handling Cerberus WebSocket message", ex);

            if (session.isOpen()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                        new MessageAI("system", "error", "Unexpected error: " + ex.getMessage())
                )));
            }
        }
    }

    private void handleSubscribe(MessageTestCreationAI incoming, WebSocketSession session) {
        Set<String> channels = resolveChannels(incoming);

        if (channels.isEmpty()) {
            throw new IllegalArgumentException("Missing channels");
        }

        for (String channel : channels) {

            /* Check Channel exists */
            if (!isSupportedSubscriptionChannel(channel)) {
                throw new IllegalArgumentException("Unsupported subscription channel: " + channel);
            }

            /* Register  */
            registerSubscription(incoming, session, channel);


            /* Send specific init  */
            sendChannelSpecificInit(incoming, channel);

        }
    }

    private void registerSubscription(MessageTestCreationAI incoming, WebSocketSession session, String channel) {
        webSocketSessionRegistry.register(incoming.getSender(), channel, incoming.getSessionID(), session);
    }

    private void sendChannelSpecificInit(MessageTestCreationAI incoming, String channel) {
        switch (channel) {
            case WebSocketStatic.CHANNEL_MYEXECUTION_LIST_RUNNING ->
                    notificationCenter.sendInitExecutionsRunning(incoming.getSender(), incoming.getSessionID());

            case WebSocketStatic.CHANNEL_MYEXECUTION_LIST_QUEUED ->
                    notificationCenter.sendInitExecutionsQueued(incoming.getSender(), incoming.getSessionID());

            case WebSocketStatic.CHANNEL_MYEXECUTION_LIST_LASTEXECUTION ->
                    notificationCenter.sendInitExecutionsLastExecutions(incoming.getSender(), incoming.getSessionID());

            case WebSocketStatic.CHANNEL_EXECUTION_LIST_QUEUED -> {
                queueStatus.refreshQueueToTreat();
                queueStatus.updateRunning(executionUUIDObject.getExecutionUUIDList().size());
                webSocketEventSender.sendToAppSession(
                        incoming.getSessionID(),
                        WebSocketStatic.CHANNEL_EXECUTION_LIST_QUEUED,
                        queueStatus.toJson(true).toMap()
                );
            }

            case WebSocketStatic.CHANNEL_EXECUTION_MONITOR -> {
                webSocketEventSender.sendToAppSession(
                        incoming.getSessionID(),
                        WebSocketStatic.CHANNEL_EXECUTION_MONITOR,
                        executionMonitor.toJson(true).toMap()
                );
            }

            default -> {
                // No channel-specific init.
            }
        }
    }

    private void handleUnsubscribe(MessageTestCreationAI incoming, WebSocketSession session) {
        Set<String> channels = resolveChannels(incoming);

        if (channels.isEmpty()) {
            webSocketSessionRegistry.unregister(session);
            return;
        }

        for (String channel : channels) {
            webSocketSessionRegistry.unregister(session, channel);
        }
    }

    private Set<String> resolveChannels(MessageTestCreationAI incoming) {
        Set<String> result = new HashSet<>();

        List<String> channels = incoming.getChannels();

        if (channels != null) {
            channels.stream()
                    .filter(channel -> channel != null && !channel.isBlank())
                    .map(String::trim)
                    .forEach(result::add);
        }

        return result;
    }

    private boolean isSupportedSubscriptionChannel(String channel) {
        switch (channel) {
            case WebSocketStatic.CHANNEL_CHAT_DELTA:
            case WebSocketStatic.CHANNEL_CHAT_DONE:
            case WebSocketStatic.CHANNEL_CHAT_TITLE:
            case WebSocketStatic.CHANNEL_CHAT_ERROR:

            case WebSocketStatic.CHANNEL_TOOL_START:
            case WebSocketStatic.CHANNEL_TOOL_RESULT:
            case WebSocketStatic.CHANNEL_TOOL_DONE:
            case WebSocketStatic.CHANNEL_TOOL_ERROR:

            case WebSocketStatic.CHANNEL_AO_PROPOSALS:
            case WebSocketStatic.CHANNEL_TESTCASE_PROPOSALS:

            case WebSocketStatic.CHANNEL_OBJECTCREATION_APPLICATION:
            case WebSocketStatic.CHANNEL_OBJECTCREATION_INVARIANT:
            case WebSocketStatic.CHANNEL_OBJECTCREATION_TESTCASE:
            case WebSocketStatic.CHANNEL_OBJECTCREATION_TESTCASESTEP:

            case WebSocketStatic.CHANNEL_EXECUTION_START:
            case WebSocketStatic.CHANNEL_EXECUTION_UPDATE:
            case WebSocketStatic.CHANNEL_EXECUTION_DONE:
            case WebSocketStatic.CHANNEL_EXECUTION_DECLAREFALSENEGATIVE:
            case WebSocketStatic.CHANNEL_EXECUTION_UNDECLAREFALSENEGATIVE:
            case WebSocketStatic.CHANNEL_EXECUTION_LIST_RUNNING:
            case WebSocketStatic.CHANNEL_EXECUTION_LIST_QUEUED:
            case WebSocketStatic.CHANNEL_EXECUTION_LIST_LASTEXECUTION:

            case WebSocketStatic.CHANNEL_EXECUTION_LIGHT_START:
            case WebSocketStatic.CHANNEL_EXECUTION_LIGHT_UPDATE:
            case WebSocketStatic.CHANNEL_EXECUTION_LIGHT_DONE:
            case WebSocketStatic.CHANNEL_MYEXECUTION_LIGHT_START:
            case WebSocketStatic.CHANNEL_MYEXECUTION_LIGHT_UPDATE:
            case WebSocketStatic.CHANNEL_MYEXECUTION_LIGHT_DONE:
            case WebSocketStatic.CHANNEL_MYEXECUTION_LIST_RUNNING:
            case WebSocketStatic.CHANNEL_MYEXECUTION_LIST_QUEUED:
            case WebSocketStatic.CHANNEL_MYEXECUTION_LIST_LASTEXECUTION:

            case WebSocketStatic.CHANNEL_QUEUE_CHANGE:

            case WebSocketStatic.CHANNEL_CAMPAIGN_START:
            case WebSocketStatic.CHANNEL_CAMPAIGN_UPDATE:
            case WebSocketStatic.CHANNEL_CAMPAIGN_END:
            case WebSocketStatic.CHANNEL_CAMPAIGN_FAIL:
            case WebSocketStatic.CHANNEL_CAMPAIGN_SUCCESS:

            case WebSocketStatic.CHANNEL_EXECUTION_MONITOR:
                return true;


            default:
                if (channel.startsWith(WebSocketStatic.CHANNEL_EXECUTION_UPDATE)||
                        channel.startsWith(WebSocketStatic.CHANNEL_EXECUTION_START)) {
                    return true;
                }
                return false;
        }
    }


    private void handleMessage(MessageTestCreationAI incoming) {
        String channel = resolveSingleChannel(incoming);

        switch (channel) {
            case WebSocketStatic.CHANNEL_CHAT_SEND:
                aiService.chatWithAI(
                        incoming.getSender(),
                        incoming.getSessionID(),
                        incoming.getContent()
                );
                break;

            case WebSocketStatic.CHANNEL_TESTCASE_PROPOSAL_REQUEST:
                try {
                    aiService.generateTestCaseProposal(
                            incoming.getSender(),
                            incoming.getSessionID(),
                            incoming.getContent(),
                            incoming.getApplication(),
                            incoming.getTestFolder()
                    );
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                break;

            case WebSocketStatic.CHANNEL_TESTCASE_CREATE_REQUEST:
                try {
                    aiService.createTestCaseAndGenerateContent(
                            incoming.getSender(),
                            incoming.getSessionID(),
                            incoming.getTestFolder(),
                            incoming.getTestcaseObject(),
                            incoming.getTestDetailedDescription(),
                            incoming.getTempId()
                    );
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                break;

            case WebSocketStatic.CHANNEL_AO_GENERATECONTINUE_REQUEST:
            case WebSocketStatic.CHANNEL_AO_GENERATE_REQUEST:
                aiService.generateApplicationObjectProposalWithAI(
                        incoming.getSender(),
                        incoming.getSessionID(),
                        incoming.getApplication(),
                        incoming.getPage(),
                        incoming.getHtmlPath(),
                        incoming.getScreenshotPath(),
                        incoming.getContent(),
                        channel
                );
                break;

            case WebSocketStatic.CHANNEL_EXECUTION_DEBUG_REQUEST:
                aiService.executionDebugWithAI(
                        incoming.getSender(),
                        incoming.getSessionID(),
                        Long.parseLong(incoming.getContent())
                );
                break;

            default:
                throw new IllegalArgumentException("Unsupported message channel: " + channel);
        }
    }

    private String resolveSingleChannel(MessageTestCreationAI incoming) {
        Set<String> channels = resolveChannels(incoming);

        if (channels.isEmpty()) {
            throw new IllegalArgumentException("Missing channels");
        }

        if (channels.size() > 1) {
            throw new IllegalArgumentException("Only one channel is allowed for subject=message");
        }

        return channels.iterator().next();
    }
}
