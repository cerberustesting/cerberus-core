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
package org.cerberus.core.websocket.runtime;

import org.cerberus.core.websocket.WebSocketEventSender;
import org.cerberus.core.websocket.WebSocketStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Keeps the recent history of {@link WebSocketStatic#CHANNEL_OBJECT_CHANGE} events (object
 * creation/update/deletion, whatever the object type) so it can be replayed to a client
 * subscribing to that channel, including after a reconnection.
 */
@Component
public class ObjectChangeHistory {

    /** Maximum number of events kept in memory, oldest ones are evicted first. */
    private static final int MAX_EVENTS = 200;

    private final ConcurrentLinkedDeque<Map<String, Object>> events = new ConcurrentLinkedDeque<>();

    @Autowired
    private WebSocketEventSender webSocketEventSender;

    /**
     * Records an object change event, keeps it in history and broadcasts it to current
     * subscribers of {@link WebSocketStatic#CHANNEL_OBJECT_CHANGE}.
     *
     * @param action     e.g. "create", "update", "delete"
     * @param objectName e.g. "Invariant"
     * @param objectId   business identifier of the changed object
     * @param object     DTO representation of the changed object
     */
    public void record(String action, String objectName, String objectId, Object object) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("action", action);
        event.put("objectName", objectName);
        event.put("objectId", objectId);
        event.put("object", object);
        event.put("eventTimestamp", System.currentTimeMillis());

        events.addLast(event);
        while (events.size() > MAX_EVENTS) {
            events.pollFirst();
        }

        webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_OBJECT_CHANGE, event);
    }

    /**
     * Replays the recorded events, oldest first, to the given application session.
     * Called when a client subscribes to {@link WebSocketStatic#CHANNEL_OBJECT_CHANGE}
     * so it can catch up on events missed while disconnected.
     *
     * @param appSessionID application session id of the subscribing client
     */
    public void sendInit(String appSessionID) {
        for (Map<String, Object> event : events) {
            webSocketEventSender.sendToAppSession(appSessionID, WebSocketStatic.CHANNEL_OBJECT_CHANGE, event);
        }
    }

}