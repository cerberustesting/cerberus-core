/*
 * Cerberus  Copyright (C) 2013  vertigo17
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

import org.apache.log4j.Logger;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.websocket.decoders.TestCaseExecutionDecoder;
import org.cerberus.websocket.encoders.TestCaseExecutionEncoder;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@ServerEndpoint(
        value = "/execution/{execution-id}",
        configurator = TestCaseExecutionEndPoint.Configurator.class,
        decoders = {TestCaseExecutionDecoder.class},
        encoders = {TestCaseExecutionEncoder.class}
)
public class TestCaseExecutionEndPoint {

    public static class Configurator extends ServerEndpointConfig.Configurator {

        @Override
        public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
            if (!TestCaseExecutionEndPoint.class.equals(endpointClass)) {
                throw new InstantiationException("No suitable instance for endpoint class " + endpointClass.getName());
            }
            return (T) TestCaseExecutionEndPoint.getInstance();
        }

    }

    private static final Logger LOG = Logger.getLogger(TestCaseExecutionEndPoint.class);

    private static final TestCaseExecutionEndPoint INSTANCE = new TestCaseExecutionEndPoint();

    public static TestCaseExecutionEndPoint getInstance() {
        return INSTANCE;
    }

    /**
     * All open WebSocket sessions
     */
    private Lock lock = new ReentrantLock();
    private Map<String, Session> sessions = new HashMap<>();
    private Map<Long, Set<String>> executions = new HashMap<>();

    /**
     * Send Live message for all peers connected to this execution
     *
     * @param msg
     * @param forcePush
     */
    public void send(TestCaseExecution msg, boolean forcePush) {
        if (!msg.isCerberus_featureflipping_activatewebsocketpush()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Push is disabled. Ignore sending of execution " + msg.getId());
            }
            return;
        }

        long nbmssincelastpush = new Date().getTime() - msg.getLastWebsocketPush();
        if ((nbmssincelastpush < msg.getCerberus_featureflipping_websocketpushperiod()) && !forcePush) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Not enough elapsed time since the last push for execution " + msg.getId() + " (" + nbmssincelastpush + " < " + msg.getCerberus_featureflipping_websocketpushperiod());
            }
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Trying to send execution " + msg.getId() + " to sessions");
        }

        lock.lock();
        Set<String> registeredSessions = executions.get(msg.getId());
        if (registeredSessions == null) {
            lock.unlock();
            if (LOG.isDebugEnabled()) {
                LOG.debug("No registered session for execution " + msg.getId());
            }
            msg.setLastWebsocketPush(new Date().getTime());
            return;
        }

        for (String registeredSession : registeredSessions) {
            Session session = sessions.get(registeredSession);
            try {
                session.getBasicRemote().sendObject(msg);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Execution " + msg.getId() + " sent to session " + session.getId());
                }
            } catch (Exception e) {
                LOG.warn("Unable to send execution " + msg.getId() + " to session " + session.getId() + " due to " + e.getMessage());
            }
        }
        lock.unlock();
        msg.setLastWebsocketPush(new Date().getTime());
    }

    public void end(TestCaseExecution execution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Clean execution " + execution.getId());
        }
        lock.lock();
        Set<String> registeredSessions = executions.remove(execution.getId());
        if (registeredSessions != null) {
            for (String registeredSession : registeredSessions) {
                Session session = sessions.remove(registeredSession);
                try {
                    session.close();
                } catch (Exception e) {
                    LOG.warn("Unable to close session " + session.getId() + " for execution " + execution.getId() + " due to " + e.getMessage());
                }
            }
        }
        lock.unlock();
    }

    /**
     * Behavior of the Endpoint when the client send him a message
     *
     * @param session
     * @param msg
     * @param executionId
     */
    @OnMessage
    public void message(final Session session, TestCaseExecution msg, @PathParam("execution-id") int executionId) {
    }

    /**
     * Behaviour of the endpoint when a client connect to him Here we save the
     * session of the client in a Array so it is subscribed to the
     * TestCaseExecution And we send to the client the actual state of the
     * TestCaseExecution thanks to a Read
     *
     * @param session
     * @param config
     * @param executionId
     */
    @OnOpen
    public void openConnection(Session session, EndpointConfig config, @PathParam("execution-id") long executionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Session " + session.getId() + " opened connection to execution " + executionId);
        }
        lock.lock();
        sessions.put(session.getId(), session);
        Set<String> registeredSessions = executions.get(executionId);
        if (registeredSessions == null) {
            registeredSessions = new HashSet<>();
        }
        registeredSessions.add(session.getId());
        executions.put(executionId, registeredSessions);
        lock.unlock();
    }

    /**
     * Behaviour of the EndPoint When the client closes his connexion We remove
     * the client from the array of Sessions
     *
     * @param session
     * @param executionId
     */
    @OnClose
    public void closedConnection(Session session, @PathParam("execution-id") long executionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Session " + session.getId() + " closed connection to execution " + executionId);
        }
        lock.lock();
        sessions.remove(session.getId());
        Set<String> registeredSessions = executions.get(executionId);
        if (registeredSessions != null) {
            registeredSessions.remove(session.getId());
        }
        lock.unlock();
    }

    /**
     * Behaviour of the endpoint when there is an error
     *
     * @param session
     * @param t
     */
    @OnError
    public void error(Session session, Throwable t) {
        LOG.warn("An error occurred during websocket communication with session " + session.getId() + ": " + t.getMessage(), t);
        try {
            session.getBasicRemote().sendText(t.getMessage());
        } catch (Exception e) {
            LOG.warn("Unable to send error to session " + session.getId() + " due to " + e.getMessage());
        }
    }

}
