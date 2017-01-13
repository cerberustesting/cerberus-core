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
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint(
        value = "/execution/{execution-id}",
        decoders = {TestCaseExecutionDecoder.class},
        encoders = {TestCaseExecutionEncoder.class}
)
public class TestCaseExecutionEndPoint {

    private static final Logger LOG = Logger.getLogger(TestCaseExecutionEndPoint.class);

    /**
     * All open WebSocket sessions
     */
    static Set<Session> peers = Collections.synchronizedSet(new HashSet<Session>());

    /**
     * Send Live message for all peers connected to this execution
     *
     * @param msg
     * @param forcePush
     */
    public static void send(TestCaseExecution msg, boolean forcePush) {

        // We check if feature is activated
        if (msg.isCerberus_featureflipping_activatewebsocketpush()) {

            long nbmssincelastpush = new Date().getTime() - msg.getLastWebsocketPush();

            // We check if the last send is not done too close to the current request
            if ((nbmssincelastpush >= msg.getCerberus_featureflipping_websocketpushperiod()) || (forcePush)) {

                msg.setLastWebsocketPush(new Date().getTime());
                LOG.debug("TestCaseExecutionEndPoint : Send Message " + nbmssincelastpush + " since last send.");
                try {
                    /* Send updates to all open WebSocket sessions for this match */
                    for (Session session : peers) {
                        if (Boolean.TRUE.equals(session.getUserProperties().get(String.valueOf(msg.getId())))) {
                            if (session.isOpen()) {
                                session.getBasicRemote().sendObject(msg);
                            }
                        }
                    }
                } catch (IOException | EncodeException e) {

                }
            } else {
                LOG.debug("TestCaseExecutionEndPoint : Not Sending message : " + nbmssincelastpush + " since last send.");

            }
        }
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
    public void openConnection(Session session, EndpointConfig config, @PathParam("execution-id") int executionId) {

        LOG.warn("TestCaseExecutionEndPoint : Client Open");

        session.getUserProperties().put(String.valueOf(executionId), true);
        peers.add(session);

    }

    /**
     * Behaviour of the EndPoint When the client closes his connexion We remove
     * the client from the array of Sessions
     *
     * @param session
     * @param executionId
     */
    @OnClose
    public void closedConnection(Session session, @PathParam("execution-id") int executionId) {
        session.getUserProperties().put(String.valueOf(executionId), false);
        peers.remove(session);
    }

    /**
     * Behaviour of the endpoint when there is an error
     *
     * @param session
     * @param t
     */
    @OnError
    public void error(Session session, Throwable t) {
        try {
            session.getBasicRemote().sendText(t.toString());
        } catch (IOException e) {

        }
    }

}
