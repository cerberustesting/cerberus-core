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
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.service.IParameterService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Sends WebSocket events to registered sessions.
 * <p>
 * In this model, channel is the exact event name.
 * Example: chat.delta, execution.update, queue.change.
 */
@Component
public class WebSocketEventSender {

    private static final Logger LOG = LogManager.getLogger(WebSocketEventSender.class);

    /** Parameter used to throttle WebSocket push events. */
    private static final String PARAM_WEBSOCKET_PUSH_PERIOD = "cerberus_featureflipping_websocketpushperiod";

    /** Maximum time to keep pending WebSocket events when no reader receives them. */
    private static final long MAX_PENDING_RETENTION_MS = TimeUnit.MINUTES.toMillis(2);

    /** Maximum number of pending payloads kept per channel when latestOnly=false. */
    private static final int MAX_PENDING_PAYLOADS_PER_CHANNEL = 500;

    private final WebSocketSessionRegistry registry;
    private final ObjectMapper objectMapper;
    private final IParameterService parameterService;

    /** Message counter by WebSocket session id. */
    private final ConcurrentHashMap<String, AtomicLong> messageCountByWsId = new ConcurrentHashMap<>();

    /** Last sent timestamp by channel. */
    private final ConcurrentHashMap<String, AtomicLong> lastMessageSentByThrottleKey = new ConcurrentHashMap<>();

    /**
     * Pending payloads by channel.
     * <p>
     * If latestOnly=true, the deque contains only the latest payload.
     * If latestOnly=false, the deque contains all payloads waiting for flush.
     */
    private final ConcurrentHashMap<String, Deque<Object>> pendingPayloadsByThrottleKey = new ConcurrentHashMap<>();

    /** First pending payload timestamp by channel. */
    private final ConcurrentHashMap<String, AtomicLong> firstPendingTimestampByThrottleKey = new ConcurrentHashMap<>();

    /** Lock object by channel. */
    private final ConcurrentHashMap<String, Object> lockByThrottleKey = new ConcurrentHashMap<>();

    /** Scheduled flush task by channel. */
    private final ConcurrentHashMap<String, ScheduledFuture<?>> scheduledFlushByThrottleKey = new ConcurrentHashMap<>();

    /** Executor used to flush throttled events. */
    private final ScheduledExecutorService throttleExecutor =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "cerberus-ws-throttle-flusher");
                thread.setDaemon(true);
                return thread;
            });

    public WebSocketEventSender(WebSocketSessionRegistry registry, ObjectMapper objectMapper, IParameterService parameterService) {
        this.registry = registry;
        this.parameterService = parameterService;
        this.objectMapper = objectMapper.copy()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @PreDestroy
    public void shutdown() {
        throttleExecutor.shutdownNow();
    }

    public long getMessageCount(String wsId) {
        AtomicLong counter = messageCountByWsId.get(wsId);
        return counter != null ? counter.get() : 0;
    }

    public long getTotalMessagesSent() {
        return messageCountByWsId.values().stream().mapToLong(AtomicLong::get).sum();
    }

    /**
     * Sends an event to one application session.
     *
     * @param appSessionID application session id
     * @param channel exact event channel
     * @param payload event payload
     */
    public void sendToAppSession(String appSessionID, String channel, Object payload) {
        registry.getByAppSessionID(appSessionID).ifPresent(session ->
                send(session, WebSocketEnvelope.of(channel, appSessionID, null, payload))
        );
    }

    /**
     * Sends an event to all sessions of a user.
     *
     * @param user target user
     * @param channel exact event channel
     * @param payload event payload
     */
    public void sendToUser(String user, String channel, Object payload) {
        for (WebSocketSession session : registry.getByUserAndChannel(user, channel)) {
            send(session, WebSocketEnvelope.of(channel, null, user, payload));
        }
    }

    /**
     * Sends an event to a channel without throttling.
     *
     * @param channel exact event channel
     * @param payload event payload
     * @return true when at least one session received the event
     */
    public boolean sendToChannel(String channel, Object payload) {
        return sendToChannel(channel, payload, false, true);
    }

    /**
     * Sends an event to a channel.
     * <p>
     * When throttling is enabled:
     * <ul>
     *     <li>latestOnly=true keeps only the latest pending payload.</li>
     *     <li>latestOnly=false keeps all pending payloads and flushes them in order.</li>
     * </ul>
     *
     * @param channel exact event channel
     * @param payload event payload
     * @param throttling true to throttle events
     * @param latestOnly true to keep only the latest pending payload, false to keep all pending payloads
     * @return true when the event was sent immediately
     */
    public boolean sendToChannel(String channel, Object payload, boolean throttling, boolean latestOnly) {
        if (!throttling) {
            return sendToChannelNow(channel, payload);
        }

        String throttleKey = buildThrottleKey(channel);
        long throttleMs = getWebSocketPushPeriod();

        if (throttleMs <= 0) {
            return sendToChannelNow(channel, payload);
        }

        Object lock = lockByThrottleKey.computeIfAbsent(throttleKey, key -> new Object());

        synchronized (lock) {
            long now = System.currentTimeMillis();
            long lastSent = lastMessageSentByThrottleKey
                    .computeIfAbsent(throttleKey, key -> new AtomicLong(0L))
                    .get();

            long elapsed = now - lastSent;

            if (elapsed >= throttleMs) {
                /*
                 * If all payloads must be preserved and some payloads are already pending,
                 * append the current payload and flush the full buffer in order.
                 */
                if (!latestOnly && hasPendingPayloads(throttleKey)) {
                    storePendingPayload(throttleKey, payload, false);
                    return flushPendingToChannel(channel, false);
                }

                boolean sent = sendToChannelNow(channel, payload);

                if (sent) {
                    lastMessageSentByThrottleKey.get(throttleKey).set(now);
                    clearPendingPayloads(throttleKey);
                } else {
                    storePendingPayload(throttleKey, payload, latestOnly);
                    scheduleFlushIfNeeded(channel, throttleKey, throttleMs, latestOnly);
                }

                return sent;
            }

            storePendingPayload(throttleKey, payload, latestOnly);

            long delayMs = throttleMs - elapsed;
            scheduleFlushIfNeeded(channel, throttleKey, delayMs, latestOnly);

            LOG.debug(
                    "WebSocket event throttled and scheduled. channel={} elapsed={}ms throttle={}ms delay={}ms latestOnly={}",
                    channel,
                    elapsed,
                    throttleMs,
                    delayMs,
                    latestOnly
            );

            return false;
        }
    }

    public boolean sendToChannels(Collection<String> channels, Object payload) {
        return sendToChannels(channels, payload, false, true);
    }

    public boolean sendToChannels(Collection<String> channels, Object payload, boolean throttling, boolean latestOnly) {
        boolean atLeastOneWasSent = false;

        for (String channel : channels) {
            boolean sent = sendToChannel(channel, payload, throttling, latestOnly);

            if (sent) {
                atLeastOneWasSent = true;
            }
        }

        return atLeastOneWasSent;
    }

    private boolean sendToChannelNow(String channel, Object payload) {
        boolean atLeastOneWasSent = false;

        for (WebSocketSession session : registry.getByChannel(channel)) {
            boolean sent = send(session, WebSocketEnvelope.of(channel, null, null, payload));

            if (sent) {
                atLeastOneWasSent = true;
            }
        }

        return atLeastOneWasSent;
    }

    public boolean send(WebSocketSession session, WebSocketEnvelope<?> envelope) {
        if (session == null || !session.isOpen()) {
            return false;
        }

        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(envelope)));
            messageCountByWsId.computeIfAbsent(session.getId(), k -> new AtomicLong()).incrementAndGet();
            return true;
        } catch (Exception e) {
            LOG.error("Unable to send WebSocket event. channel={}", envelope.channel(), e);
            return false;
        }
    }

    private void storePendingPayload(String throttleKey, Object payload, boolean latestOnly) {
        Deque<Object> pendingPayloads = pendingPayloadsByThrottleKey
                .computeIfAbsent(throttleKey, key -> new ArrayDeque<>());

        firstPendingTimestampByThrottleKey
                .computeIfAbsent(throttleKey, key -> new AtomicLong(System.currentTimeMillis()));

        if (latestOnly) {
            pendingPayloads.clear();
        }

        pendingPayloads.addLast(payload);

        if (!latestOnly) {
            trimPendingPayloadsIfNeeded(throttleKey, pendingPayloads);
        }
    }

    private void trimPendingPayloadsIfNeeded(String throttleKey, Deque<Object> pendingPayloads) {
        int removed = 0;

        while (pendingPayloads.size() > MAX_PENDING_PAYLOADS_PER_CHANNEL) {
            pendingPayloads.removeFirst();
            removed++;
        }

        if (removed > 0) {
            LOG.warn(
                    "WebSocket pending payload buffer trimmed. channel={} removed={} remaining={} max={}",
                    throttleKey,
                    removed,
                    pendingPayloads.size(),
                    MAX_PENDING_PAYLOADS_PER_CHANNEL
            );
        }
    }

    private boolean hasPendingPayloads(String throttleKey) {
        Deque<Object> pendingPayloads = pendingPayloadsByThrottleKey.get(throttleKey);
        return pendingPayloads != null && !pendingPayloads.isEmpty();
    }

    private void scheduleFlushIfNeeded(String channel, String throttleKey, long delayMs, boolean latestOnly) {
        ScheduledFuture<?> existing = scheduledFlushByThrottleKey.get(throttleKey);

        if (existing != null && !existing.isDone() && !existing.isCancelled()) {
            return;
        }

        ScheduledFuture<?> future = throttleExecutor.schedule(
                () -> flushPendingToChannel(channel, latestOnly),
                Math.max(delayMs, 1L),
                TimeUnit.MILLISECONDS
        );

        scheduledFlushByThrottleKey.put(throttleKey, future);
    }

    public boolean flushPendingToChannel(String channel, boolean latestOnly) {
        String throttleKey = buildThrottleKey(channel);
        Object lock = lockByThrottleKey.computeIfAbsent(throttleKey, key -> new Object());

        synchronized (lock) {
            scheduledFlushByThrottleKey.remove(throttleKey);

            if (clearExpiredPendingPayloadsIfNeeded(channel, throttleKey)) {
                return false;
            }

            Deque<Object> pendingPayloads = pendingPayloadsByThrottleKey.get(throttleKey);

            if (pendingPayloads == null || pendingPayloads.isEmpty()) {
                LOG.debug("No pending WebSocket event to flush. channel={}", channel);
                clearPendingPayloads(throttleKey);
                return false;
            }

            long throttleMs = getWebSocketPushPeriod();

            if (throttleMs > 0) {
                long now = System.currentTimeMillis();
                long lastSent = lastMessageSentByThrottleKey
                        .computeIfAbsent(throttleKey, key -> new AtomicLong(0L))
                        .get();

                long elapsed = now - lastSent;

                if (elapsed < throttleMs) {
                    scheduleFlushIfNeeded(channel, throttleKey, throttleMs - elapsed, latestOnly);
                    return false;
                }
            }

            boolean atLeastOneWasSent = false;

            while (!pendingPayloads.isEmpty()) {
                Object payload = pendingPayloads.peekFirst();

                boolean sent = sendToChannelNow(channel, payload);

                if (!sent) {
                    LOG.debug("Pending WebSocket event not flushed because no session received it. channel={}", channel);
                    scheduleFlushIfNeeded(channel, throttleKey, getWebSocketPushPeriod(), latestOnly);
                    return atLeastOneWasSent;
                }

                pendingPayloads.removeFirst();
                atLeastOneWasSent = true;
            }

            clearPendingPayloads(throttleKey);

            if (atLeastOneWasSent) {
                lastMessageSentByThrottleKey
                        .computeIfAbsent(throttleKey, key -> new AtomicLong(0L))
                        .set(System.currentTimeMillis());

                LOG.debug("Pending WebSocket event flushed. channel={} latestOnly={}", channel, latestOnly);
            }

            return atLeastOneWasSent;
        }
    }

    private boolean clearExpiredPendingPayloadsIfNeeded(String channel, String throttleKey) {
        AtomicLong firstPendingTimestamp = firstPendingTimestampByThrottleKey.get(throttleKey);

        if (firstPendingTimestamp == null) {
            return false;
        }

        long ageMs = System.currentTimeMillis() - firstPendingTimestamp.get();

        if (ageMs < MAX_PENDING_RETENTION_MS) {
            return false;
        }

        Deque<Object> removedPayloads = pendingPayloadsByThrottleKey.remove(throttleKey);
        firstPendingTimestampByThrottleKey.remove(throttleKey);

        ScheduledFuture<?> scheduledFlush = scheduledFlushByThrottleKey.remove(throttleKey);

        if (scheduledFlush != null) {
            scheduledFlush.cancel(false);
        }

        int removedCount = removedPayloads != null ? removedPayloads.size() : 0;

        LOG.warn(
                "Expired pending WebSocket events removed. channel={} removed={} age={}ms retention={}ms",
                channel,
                removedCount,
                ageMs,
                MAX_PENDING_RETENTION_MS
        );

        return true;
    }

    private void clearPendingPayloads(String throttleKey) {
        pendingPayloadsByThrottleKey.remove(throttleKey);
        firstPendingTimestampByThrottleKey.remove(throttleKey);
    }

    private String buildThrottleKey(String channel) {
        return channel;
    }

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