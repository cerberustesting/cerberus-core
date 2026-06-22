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

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionRegistry {

    private final Map<String, WebSocketSession> sessionsByWsId = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> wsIdsByUser = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> wsIdsByChannel = new ConcurrentHashMap<>();
    private final Map<String, String> wsIdByAppSessionId = new ConcurrentHashMap<>();
    private final Map<String, SessionMeta> metaByWsId = new ConcurrentHashMap<>();

    public void register(String user, String channel, String appSessionID, WebSocketSession session) {

        if (session == null || !session.isOpen()) {
            return;
        }

        String wsId = session.getId();

        sessionsByWsId.put(wsId, session);

        if (user != null && !user.isBlank()) {
            wsIdsByUser
                    .computeIfAbsent(user, ignored -> ConcurrentHashMap.newKeySet())
                    .add(wsId);
        }

        if (channel != null && !channel.isBlank()) {
            wsIdsByChannel
                    .computeIfAbsent(channel, ignored -> ConcurrentHashMap.newKeySet())
                    .add(wsId);
        }

        if (appSessionID != null && !appSessionID.isBlank()) {
            wsIdByAppSessionId.put(appSessionID, wsId);
        }

        metaByWsId.compute(wsId, (id, existing) -> {
            if (existing != null) {
                if (channel != null && !channel.isBlank()) existing.channels().add(channel);
                return existing;
            }
            Set<String> channels = ConcurrentHashMap.newKeySet();
            if (channel != null && !channel.isBlank()) channels.add(channel);
            return new SessionMeta(
                    user != null ? user : "",
                    appSessionID != null ? appSessionID : "",
                    channels
            );
        });
    }

    public Optional<WebSocketSession> getByAppSessionID(String appSessionID) {
        if (appSessionID == null || appSessionID.isBlank()) {
            return Optional.empty();
        }

        String wsId = wsIdByAppSessionId.get(appSessionID);
        return getByWsId(wsId);
    }

    public List<WebSocketSession> getByUser(String user) {
        if (user == null || user.isBlank()) {
            return List.of();
        }

        return getMany(wsIdsByUser.getOrDefault(user, Set.of()));
    }

    public List<WebSocketSession> getByChannel(String channel) {
        if (channel == null || channel.isBlank()) {
            return List.of();
        }

        return getMany(wsIdsByChannel.getOrDefault(channel, Set.of()));
    }

    public Optional<WebSocketSession> getByWsId(String wsId) {
        if (wsId == null || wsId.isBlank()) {
            return Optional.empty();
        }

        WebSocketSession session = sessionsByWsId.get(wsId);

        if (session == null || !session.isOpen()) {
            unregisterByWsId(wsId);
            return Optional.empty();
        }

        return Optional.of(session);
    }

    private List<WebSocketSession> getMany(Set<String> wsIds) {
        return wsIds.stream()
                .map(this::getByWsId)
                .flatMap(Optional::stream)
                .toList();
    }

    public void unregister(WebSocketSession session) {
        if (session == null) {
            return;
        }

        unregisterByWsId(session.getId());
    }

    public List<SessionDetail> getSessions() {
        return sessionsByWsId.entrySet().stream()
                .map(e -> {
                    String wsId = e.getKey();
                    WebSocketSession session = e.getValue();
                    SessionMeta meta = metaByWsId.get(wsId);
                    return new SessionDetail(
                            wsId,
                            meta != null ? meta.user() : "",
                            meta != null ? meta.appSessionId() : "",
                            meta != null ? List.copyOf(meta.channels()) : List.of(),
                            session.isOpen()
                    );
                })
                .toList();
    }

    private void unregisterByWsId(String wsId) {
        if (wsId == null || wsId.isBlank()) {
            return;
        }

        sessionsByWsId.remove(wsId);
        metaByWsId.remove(wsId);

        wsIdsByUser.values().forEach(set -> set.remove(wsId));
        wsIdsByUser.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        wsIdsByChannel.values().forEach(set -> set.remove(wsId));
        wsIdsByChannel.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        wsIdByAppSessionId.entrySet().removeIf(entry -> wsId.equals(entry.getValue()));
    }

    private record SessionMeta(String user, String appSessionId, Set<String> channels) {}

    public record SessionDetail(String wsId, String user, String appSessionId, List<String> channels, boolean open) {}
}