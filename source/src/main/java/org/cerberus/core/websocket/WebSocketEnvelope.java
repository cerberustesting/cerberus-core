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

import java.time.Instant;

public record WebSocketEnvelope<T>(
        String sender,
        String channel,
        String sessionID,
        String user,
        T payload,
        Instant timestamp
) {

    public static <T> WebSocketEnvelope<T> of(String channel, String sessionID, String user, T payload) {
        return new WebSocketEnvelope<>(
                "system",
                channel,
                sessionID,
                user,
                payload,
                Instant.now()
        );
    }
}