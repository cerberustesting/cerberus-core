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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HomepageWebSocket extends TextWebSocketHandler {

    private static final Logger LOG = LogManager.getLogger(HomepageWebSocket.class);

    /**
     * All open WebSocket sessions, grouped by executions
     */
    private Map<String, WebSocketSession> sessions;
    private Set<String> registeredSessions;

    @PostConstruct
    public void init() {
        LOG.debug("HomepageWebSocket instance created: " + this);
        sessions = new ConcurrentHashMap<>();
        registeredSessions = ConcurrentHashMap.newKeySet();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        LOG.debug("Session " + session.getId() + " opened connection to queue status");
        sessions.put(session.getId(), session);
        registeredSessions.add(session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        LOG.debug("Session " + session.getId() + " closed connection of queue status");
        sessions.remove(session.getId());
        registeredSessions.remove(session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

    }

    public void send(QueueStatus queueStatus) {
        Collection<WebSocketSession> sessionsToSend = new ArrayList<>();

        for (String sessionId : registeredSessions) {
            WebSocketSession session = sessions.get(sessionId);
            if (session != null) {
                sessionsToSend.add(session);
            }
        }

        LOG.debug("Trying to send queue status to sessions");
        for (WebSocketSession session : sessionsToSend) {
            try {
                session.sendMessage(new TextMessage(queueStatus.toJson(true).toString()));
                LOG.debug("Queue Status sent to session " + session.getId());
            } catch (Exception e) {
                LOG.warn("Unable to send queue status to session " + session.getId() +
                        " due to " + e.getMessage() + " --> Closing it.");
            }
        }

        queueStatus.setLastWebsocketPush(System.currentTimeMillis());
    }
}