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
import org.cerberus.core.crud.entity.TestCaseExecution;
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
public class TestCaseExecutionWebSocket extends TextWebSocketHandler {

    /**
     * The associated {@link Logger} to this class
     */
    private static final Logger LOG = LogManager.getLogger(TestCaseExecutionWebSocket.class);

    /**
     * All open WebSocket sessions, grouped by executions
     */
    private Lock mainLock;
    private Map<String, WebSocketSession> sessions;
    private Map<Long, Set<String>> executions;


    @PostConstruct
    public void init() {
        LOG.debug("TestCaseExecutionWebSocket instance created: " + this);
        mainLock = new ReentrantLock();
        sessions = new HashMap<>();
        executions = new HashMap<>();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String executionIdStr = (String) session.getAttributes().get("executionId");
        if (executionIdStr != null) {
            long executionId = Long.parseLong(executionIdStr);
            LOG.debug("Session " + session.getId() + " opened connection to execution " + executionId);

            mainLock.lock();
            try {
                sessions.put(session.getId(), session);
                Set<String> registeredSessions = executions.get(executionId);
                if (registeredSessions == null) {
                    registeredSessions = new HashSet<>();
                }
                registeredSessions.add(session.getId());
                executions.put(executionId, registeredSessions);
                LOG.debug("Execution " + executionId + " registered session " + session.getId());

            } finally {
                mainLock.unlock();
            }
        } else {
            LOG.error("executionId is missing in session attributes for session " + session.getId());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String executionId = (String) session.getAttributes().get("executionId");
        if (LOG.isDebugEnabled()) {
            LOG.debug("Session " + session.getId() + " closed connection to execution " + executionId);
        }
        mainLock.lock();
        try {
            sessions.remove(session.getId());
            Set<String> registeredSessions = executions.get(executionId);
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

    /**
     * Send the given {@link TestCaseExecution} for all session opened to this
     * execution.
     * <p>
     * Message is sent only if the current timestamp is out of the
     * {@link TestCaseExecution#getCerberus_featureflipping_websocketpushperiod()}
     *
     * @param execution the {@link TestCaseExecution} to send to opened sessions
     * @param forcePush if send has to be forced, regardless of the
     * {@link TestCaseExecution#getCerberus_featureflipping_websocketpushperiod()}}
     * @see TestCaseExecution#getLastWebsocketPush()
     */
    public void send(TestCaseExecution execution, boolean forcePush) {
        // Check if sending is enabled
        if (!execution.isCerberus_featureflipping_activatewebsocketpush()) {
            LOG.debug("Push is disabled. Ignore sending of execution " + execution.getId());
            return;
        }

        // Check if sending can be done regarding on the last push and allowed period
        long sinceLastPush = new Date().getTime() - execution.getLastWebsocketPush();
        if ((sinceLastPush < execution.getCerberus_featureflipping_websocketpushperiod()) && !forcePush) {
            LOG.debug("Not enough elapsed time since the last push for execution " + execution.getId() + " (" + sinceLastPush + " < " + execution.getCerberus_featureflipping_websocketpushperiod());
            return;
        }

        // Get registered sessions
        Collection<WebSocketSession> registeredSessions = new ArrayList<>();
        mainLock.lock();
        try {
            Set<String> registeredSessionIds = executions.get(execution.getId());

            if (registeredSessionIds != null) {
                LOG.debug("registeredSessionIds " + registeredSessionIds.toString());
                registeredSessions = Maps.filterKeys(sessions, Predicates.in(registeredSessionIds)).values();
            }
        } catch (Exception ex){
            LOG.warn("Exception " + ex.toString());
        }finally {
            mainLock.unlock();
        }

        // Send the given TestCaseExecution to all registered sessions
        LOG.debug("Trying to send execution " + execution.getId() + " to sessions");
        for (WebSocketSession registeredSession : registeredSessions) {
            LOG.debug("registeredSession : " + registeredSession.getId());
            try {
                registeredSession.sendMessage(new TextMessage(execution.toJson(true).toString()));
                LOG.debug("Execution " + execution.getId() + " sent to session " + registeredSession.getId());
            } catch (Exception e) {
                LOG.warn("Unable to send execution " + execution.getId() + " to session " + registeredSession.getId() + " due to " + e.getMessage());
            }
        }

        // Finally set the last push date to the given TestCaseExecution
        execution.setLastWebsocketPush(new Date().getTime());
    }

    /**
     * Process to the end of the given {@link TestCaseExecution}, i.e., close
     * all registered session to the given {@link TestCaseExecution}
     *
     * @param execution the given {@link TestCaseExecution} to end
     */
    public void end(TestCaseExecution execution) {
        // Get the registered sessions to the given TestCaseExecution
        Collection<WebSocketSession> registeredSessions = new ArrayList<>();
        mainLock.lock();
        try {
            Set<String> registeredSessionIds = executions.remove(execution.getId());
            if (registeredSessionIds != null) {
                for (String registeredSessionId : registeredSessionIds) {
                    registeredSessions.add(sessions.remove(registeredSessionId));
                }
            }
        } finally {
            mainLock.unlock();
        }

        // Close registered sessions
        if (LOG.isDebugEnabled()) {
            LOG.debug("Clean execution " + execution.getId());
        }
        for (WebSocketSession registeredSession : registeredSessions) {
            try {
                registeredSession.close();
            } catch (Exception e) {
                LOG.warn("Unable to close session " + registeredSession.getId() + " for execution " + execution.getId() + " due to " + e.getMessage());
            }
        }
    }

}