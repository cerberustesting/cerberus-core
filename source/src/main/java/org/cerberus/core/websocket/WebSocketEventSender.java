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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.service.IParameterService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import jakarta.annotation.PreDestroy;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Sends WebSocket events to registered sessions.
 * <p>
 * Supports direct delivery by session, user, or channel.
 * Also supports channel-level throttling.
 */
@Component
public class WebSocketEventSender {

    private static final Logger LOG = LogManager.getLogger(WebSocketEventSender.class);

    /** Parameter used to throttle WebSocket push events. */
    private static final String PARAM_WEBSOCKET_PUSH_PERIOD = "cerberus_featureflipping_websocketpushperiod";

    private final WebSocketSessionRegistry registry;
    private final ObjectMapper objectMapper;
    private final IParameterService parameterService;

    /** Message counter by WebSocket session id. */
    private final ConcurrentHashMap<String, AtomicLong> messageCountByWsId = new ConcurrentHashMap<>();

    /** Last sent timestamp by throttle key. */
    private final ConcurrentHashMap<String, AtomicLong> lastMessageSentByThrottleKey = new ConcurrentHashMap<>();

    /** Latest pending payload by throttle key. */
    private final ConcurrentHashMap<String, Object> pendingPayloadByThrottleKey = new ConcurrentHashMap<>();

    /** Lock object by throttle key. */
    private final ConcurrentHashMap<String, Object> lockByThrottleKey = new ConcurrentHashMap<>();

    /** Scheduled flush task by throttle key. */
    private final ConcurrentHashMap<String, ScheduledFuture<?>> scheduledFlushByThrottleKey = new ConcurrentHashMap<>();

    /** Executor used to flush throttled events. */
    private final ScheduledExecutorService throttleExecutor =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "cerberus-ws-throttle-flusher");
                thread.setDaemon(true);
                return thread;
            });

    /**
     * Stops the throttle executor.
     */
    @PreDestroy
    public void shutdown() {
        throttleExecutor.shutdownNow();
    }

    /**
     * Creates a WebSocket event sender.
     *
     * @param registry WebSocket session registry
     * @param parameterService parameter service
     */
    public WebSocketEventSender(WebSocketSessionRegistry registry, IParameterService parameterService) {
        this.registry = registry;
        this.parameterService = parameterService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Returns the number of messages sent to a WebSocket session.
     *
     * @param wsId WebSocket session id
     * @return sent message count
     */
    public long getMessageCount(String wsId) {
        AtomicLong counter = messageCountByWsId.get(wsId);
        return counter != null ? counter.get() : 0;
    }

    /**
     * Returns the total number of sent messages.
     *
     * @return total sent message count
     */
    public long getTotalMessagesSent() {
        return messageCountByWsId.values().stream().mapToLong(AtomicLong::get).sum();
    }

    /**
     * Sends an event to one application session.
     *
     * @param appSessionID application session id
     * @param type event type
     * @param channel event channel
     * @param payload event payload
     */
    public void sendToAppSession(String appSessionID, String type, String channel, Object payload) {
        registry.getByAppSessionID(appSessionID).ifPresent(session ->
                send(session, WebSocketEnvelope.of(type, channel, appSessionID, null, payload))
        );
    }

    /**
     * Sends an event to all sessions of a user.
     *
     * @param user target user
     * @param type event type
     * @param channel event channel
     * @param payload event payload
     */
    public void sendToUser(String user, String type, String channel, Object payload) {
        for (WebSocketSession session : registry.getByUser(user)) {
            send(session, WebSocketEnvelope.of(type, channel, null, user, payload));
        }
    }

    /**
     * Sends an event to all sessions subscribed to a channel.
     *
     * @param channel target channel
     * @param type event type
     * @param payload event payload
     * @return true when at least one session received the event
     */
    public boolean sendToChannel(String channel, String type, Object payload) {
        return sendToChannel(channel, type, payload, false);
    }

    /**
     * Sends an event to a channel.
     * <p>
     * When throttling is enabled, only the latest payload is kept.
     *
     * @param channel target channel
     * @param type event type
     * @param payload event payload
     * @param throttling true to throttle events
     * @return true when the event was sent immediately
     */
    public boolean sendToChannel(String channel, String type, Object payload, boolean throttling) {
        if (!throttling) {
            return sendToChannelNow(channel, type, payload);
        }

        String throttleKey = buildThrottleKey(channel, type);
        long throttleMs = getWebSocketPushPeriod();

        if (throttleMs <= 0) {
            return sendToChannelNow(channel, type, payload);
        }

        Object lock = lockByThrottleKey.computeIfAbsent(throttleKey, key -> new Object());

        synchronized (lock) {
            long now = System.currentTimeMillis();
            long lastSent = lastMessageSentByThrottleKey
                    .computeIfAbsent(throttleKey, key -> new AtomicLong(0L))
                    .get();

            long elapsed = now - lastSent;

            // Send immediately when the throttle window is over.
            if (elapsed >= throttleMs) {
                boolean sent = sendToChannelNow(channel, type, payload);

                if (sent) {
                    lastMessageSentByThrottleKey.get(throttleKey).set(now);
                    pendingPayloadByThrottleKey.remove(throttleKey);
                } else {
                    // Keep the latest payload for a later flush.
                    pendingPayloadByThrottleKey.put(throttleKey, payload);
                    scheduleFlushIfNeeded(channel, type, throttleKey, throttleMs);
                }

                return sent;
            }

            // Replace any older pending payload.
            pendingPayloadByThrottleKey.put(throttleKey, payload);

            long delayMs = throttleMs - elapsed;

            scheduleFlushIfNeeded(channel, type, throttleKey, delayMs);

            LOG.debug(
                    "WebSocket event throttled and scheduled. channel={} type={} elapsed={}ms throttle={}ms delay={}ms",
                    channel,
                    type,
                    elapsed,
                    throttleMs,
                    delayMs
            );

            return false;
        }
    }

    public boolean sendToChannels(Collection<String> channels, String type, Object payload) {
        return sendToChannels(channels, type, payload, false);
    }

    public boolean sendToChannels(Collection<String> channels, String type, Object payload, boolean throttling) {
        boolean atLeastOneWasSent = false;

        for (String channel : channels) {
            boolean sent = sendToChannel(channel, type, payload, throttling);

            if (sent) {
                atLeastOneWasSent = true;
            }
        }

        return atLeastOneWasSent;
    }

    /**
     * Sends an event to a channel without throttling.
     *
     * @param channel target channel
     * @param type event type
     * @param payload event payload
     * @return true when at least one session received the event
     */
    private boolean sendToChannelNow(String channel, String type, Object payload) {
        boolean atLeastOneWasSent = false;

        for (WebSocketSession session : registry.getByChannel(channel)) {
            boolean sent = send(session, WebSocketEnvelope.of(type, channel, null, null, payload));

            if (sent) {
                atLeastOneWasSent = true;
            }
        }

        return atLeastOneWasSent;
    }

    /**
     * Sends one WebSocket envelope to one session.
     *
     * @param session target WebSocket session
     * @param envelope event envelope
     * @return true when the message was sent
     */
    public boolean send(WebSocketSession session, WebSocketEnvelope<?> envelope) {
        if (session == null || !session.isOpen()) {
            return false;
        }

        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(envelope)));
            messageCountByWsId.computeIfAbsent(session.getId(), k -> new AtomicLong()).incrementAndGet();
            return true;
        } catch (Exception e) {
            LOG.error("Unable to send WebSocket event. type={} channel={}", envelope.type(), envelope.channel(), e);
            return false;
        }
    }

    /**
     * Schedules a pending payload flush if none is already scheduled.
     *
     * @param channel target channel
     * @param type event type
     * @param throttleKey throttle key
     * @param delayMs flush delay in milliseconds
     */
    private void scheduleFlushIfNeeded(String channel, String type, String throttleKey, long delayMs) {
        ScheduledFuture<?> existing = scheduledFlushByThrottleKey.get(throttleKey);

        if (existing != null && !existing.isDone() && !existing.isCancelled()) {
            return;
        }

        ScheduledFuture<?> future = throttleExecutor.schedule(
                () -> flushPendingToChannel(channel, type),
                Math.max(delayMs, 1L),
                TimeUnit.MILLISECONDS
        );

        scheduledFlushByThrottleKey.put(throttleKey, future);
    }

    /**
     * Flushes the latest pending payload for a channel and type.
     *
     * @param channel target channel
     * @param type event type
     * @return true when the pending event was sent
     */
    public boolean flushPendingToChannel(String channel, String type) {
        String throttleKey = buildThrottleKey(channel, type);
        Object lock = lockByThrottleKey.computeIfAbsent(throttleKey, key -> new Object());

        synchronized (lock) {
            scheduledFlushByThrottleKey.remove(throttleKey);

            Object payload = pendingPayloadByThrottleKey.get(throttleKey);

            if (payload == null) {
                LOG.debug("No pending WebSocket event to flush. channel={} type={}", channel, type);
                return false;
            }

            long throttleMs = getWebSocketPushPeriod();

            if (throttleMs > 0) {
                long now = System.currentTimeMillis();
                long lastSent = lastMessageSentByThrottleKey
                        .computeIfAbsent(throttleKey, key -> new AtomicLong(0L))
                        .get();

                long elapsed = now - lastSent;

                // Wait until the throttle window is over.
                if (elapsed < throttleMs) {
                    scheduleFlushIfNeeded(channel, type, throttleKey, throttleMs - elapsed);
                    return false;
                }
            }

            boolean sent = sendToChannelNow(channel, type, payload);

            if (sent) {
                pendingPayloadByThrottleKey.remove(throttleKey);
                lastMessageSentByThrottleKey
                        .computeIfAbsent(throttleKey, key -> new AtomicLong(0L))
                        .set(System.currentTimeMillis());

                LOG.debug("Pending WebSocket event flushed. channel={} type={}", channel, type);
                return true;
            }

            LOG.debug("Pending WebSocket event not flushed because no session received it. channel={} type={}", channel, type);
            return false;
        }
    }

    /**
     * Builds the throttle key for a channel and type.
     *
     * @param channel event channel
     * @param type event type
     * @return throttle key
     */
    private String buildThrottleKey(String channel, String type) {
        return channel + ":" + type;
    }

    /**
     * Reads the WebSocket push period.
     *
     * @return throttle period in milliseconds
     */
    private long getWebSocketPushPeriod() {
        try {
            return parameterService.getParameterLongByKey(
                    PARAM_WEBSOCKET_PUSH_PERIOD,
                    "",
                    5000L
            );
        } catch (Exception ex) {
            LOG.warn(
                    "Unable to read parameter {}. Defaulting WebSocket throttling period to 5000 ms.",
                    PARAM_WEBSOCKET_PUSH_PERIOD,
                    ex
            );
            return 5000L;
        }
    }
}