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

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class QueueStatusWebSocket extends TextWebSocketHandler {

    private static final Logger LOG = LogManager.getLogger(QueueStatusWebSocket.class);

    /**
     * All open WebSocket sessions, grouped by executions
     */
    private Lock mainLock;
    private Map<String, WebSocketSession> sessions;
    private Set<String> queueStatuss;

    @PostConstruct
    public void init() {
        LOG.debug("QueueStatusWebSocket instance created: " + this);
        mainLock = new ReentrantLock();
        sessions = new HashMap<>();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        LOG.debug("Session " + session.getId() + " opened connection to queue Status");
        mainLock.lock();
        try {
            sessions.put(session.getId(), session);
            Set<String> registeredSessions = queueStatuss;
            if (registeredSessions == null) {
                registeredSessions = new HashSet<>();
            }
            registeredSessions.add(session.getId());
            queueStatuss = registeredSessions;
        } finally {
            mainLock.unlock();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Session " + session.getId() + " closed connection of queue status");
        }
        mainLock.lock();
        try {
            sessions.remove(session.getId());
            Set<String> registeredSessions = queueStatuss;
            if (registeredSessions != null) {
                registeredSessions.remove(session.getId());
            }
        } finally {
            mainLock.unlock();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

    }

    public void send(QueueStatus queueStatus, boolean forcePush) {

        // Get registered sessions
        Collection<WebSocketSession> registeredSessions = new ArrayList<>();
        mainLock.lock();
        try {
            Set<String> registeredSessionIds = queueStatuss;
            if (registeredSessionIds != null) {
                registeredSessions = Maps.filterKeys(sessions, Predicates.in(registeredSessionIds)).values();
            }
        } finally {
            mainLock.unlock();
        }

        // Send the given TestCaseExecution to all registered sessions
        LOG.debug("Trying to send queue status to sessions");
        for (WebSocketSession registeredSession : registeredSessions) {
            try {
                registeredSession.sendMessage(new TextMessage(queueStatus.toJson(true).toString()));
                LOG.debug("Queue Status sent to session " + registeredSession.getId());
            } catch (Exception e) {
                LOG.warn("Unable to send queue status to session " + registeredSession.getId() + " due to " + e.getMessage() + " --> Closing it.");
            }
        }

        // Finally set the last push date to the given TestCaseExecution
        queueStatus.setLastWebsocketPush(new Date().getTime());
    }

}