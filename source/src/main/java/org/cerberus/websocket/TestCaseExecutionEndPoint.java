/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.websocket;

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.websocket.decoders.TestCaseExecutionDecoder;
import org.cerberus.websocket.encoders.TestCaseExecutionEncoder;

/**
 * {@link ServerEndpoint} to be kept informed about {@link TestCaseExecution}
 * changes
 *
 * @author corentin
 * @author abourdon
 */
@ServerEndpoint(
        value = "/execution/{execution-id}",
        configurator = TestCaseExecutionEndPoint.SingletonConfigurator.class,
        decoders = {TestCaseExecutionDecoder.class},
        encoders = {TestCaseExecutionEncoder.class}
)
public class TestCaseExecutionEndPoint {

    /**
     * The {@link javax.websocket.server.ServerEndpointConfig.Configurator} of
     * this {@link ServerEndpoint} that give always the same
     * {@link TestCaseExecutionEndPoint} instance to deserve websocket support.
     */
    public static class SingletonConfigurator extends ServerEndpointConfig.Configurator {

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
            if (!TestCaseExecutionEndPoint.class.equals(endpointClass)) {
                throw new InstantiationException("No suitable instance for endpoint class " + endpointClass.getName());
            }
            return (T) TestCaseExecutionEndPoint.getInstance();
        }

    }

    /**
     * The associated {@link Logger} to this class
     */
    private static final Logger LOG = LogManager.getLogger(TestCaseExecutionEndPoint.class);

    /**
     * The unique instance of this {@link TestCaseExecutionEndPoint} class
     */
    private static final TestCaseExecutionEndPoint INSTANCE = new TestCaseExecutionEndPoint();

    /**
     * Get the unique instance of this {@link TestCaseExecutionEndPoint} class
     *
     * @return the unique instance of this {@link TestCaseExecutionEndPoint}
     */
    public static TestCaseExecutionEndPoint getInstance() {
        return INSTANCE;
    }

    /**
     * All open WebSocket sessions, grouped by executions
     */
    private Lock mainLock = new ReentrantLock();
    private Map<String, Session> sessions = new HashMap<>();
    private Map<Long, Set<String>> executions = new HashMap<>();

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
            if (LOG.isDebugEnabled()) {
                LOG.debug("Push is disabled. Ignore sending of execution " + execution.getId());
            }
            return;
        }

        // Check if sending can be done regarding on the last push and allowed period
        long sinceLastPush = new Date().getTime() - execution.getLastWebsocketPush();
        if ((sinceLastPush < execution.getCerberus_featureflipping_websocketpushperiod()) && !forcePush) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Not enough elapsed time since the last push for execution " + execution.getId() + " (" + sinceLastPush + " < " + execution.getCerberus_featureflipping_websocketpushperiod());
            }
            return;
        }

        // Get registered sessions
        Collection<Session> registeredSessions = new ArrayList<>();
        mainLock.lock();
        try {
            Set<String> registeredSessionIds = executions.get(execution.getId());
            if (registeredSessionIds != null) {
                registeredSessions = Maps.filterKeys(sessions, Predicates.in(registeredSessionIds)).values();
            }
        } finally {
            mainLock.unlock();
        }

        // Send the given TestCaseExecution to all registered sessions
        if (LOG.isDebugEnabled()) {
            LOG.debug("Trying to send execution " + execution.getId() + " to sessions");
        }
        for (Session registeredSession : registeredSessions) {
            try {
                registeredSession.getBasicRemote().sendObject(execution);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Execution " + execution.getId() + " sent to session " + registeredSession.getId());
                }
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
        Collection<Session> registeredSessions = new ArrayList<>();
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
        for (Session registeredSession : registeredSessions) {
            try {
                registeredSession.close();
            } catch (Exception e) {
                LOG.warn("Unable to close session " + registeredSession.getId() + " for execution " + execution.getId() + " due to " + e.getMessage());
            }
        }
    }

    /**
     * Callback when receiving message from client side
     *
     * @param session the client {@link Session}
     * @param execution the associated {@link TestCaseExecution} sent by client
     * @param executionId the execution identifier from the
     * {@link ServerEndpoint} path
     */
    @OnMessage
    public void message(final Session session, TestCaseExecution execution, @PathParam("execution-id") int executionId) {
        // Nothing to do
    }

    /**
     * Callback when receiving opened connection from client side
     *
     * @param session the client {@link Session}
     * @param config the associated {@link EndpointConfig} to the new connection
     * @param executionId the execution identifier from the
     * {@link ServerEndpoint} path
     */
    @OnOpen
    public void openConnection(Session session, EndpointConfig config, @PathParam("execution-id") long executionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Session " + session.getId() + " opened connection to execution " + executionId);
        }
        mainLock.lock();
        try {
            sessions.put(session.getId(), session);
            Set<String> registeredSessions = executions.get(executionId);
            if (registeredSessions == null) {
                registeredSessions = new HashSet<>();
            }
            registeredSessions.add(session.getId());
            executions.put(executionId, registeredSessions);
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Callback when receiving closed connection from client side
     *
     * @param session the client {@link Session}
     * @param executionId the execution identifier from the
     * {@link ServerEndpoint} path
     */
    @OnClose
    public void closedConnection(Session session, @PathParam("execution-id") long executionId) {
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

    /**
     * Callback when receiving error connection from client side
     *
     * @param session the client {@link Session}
     * @param error the associated {@link Throwable} to the received error
     */
    @OnError
    public void error(Session session, Throwable error) {
        LOG.warn("An error occurred during websocket communication with session " + session.getId() + ": " + error.getMessage(), error);
        try {
            session.getBasicRemote().sendText(error.getMessage());
        } catch (Exception e) {
            LOG.warn("Unable to send error to session " + session.getId() + " due to " + e.getMessage());
        }
    }

}
