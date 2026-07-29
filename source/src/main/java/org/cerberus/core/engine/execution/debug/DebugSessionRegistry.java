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
package org.cerberus.core.engine.execution.debug;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.engine.entity.ExecutionUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Registry of currently paused {@link DebugSession}s, keyed by the execution
 * UUID (same key as {@link ExecutionUUID}). A debug-mode execution registers
 * itself here right before being started, so that a "next"/"stop" command
 * coming from a REST call can reach the worker thread blocked inside
 * {@code ExecutionRunService.executeStep}.
 *
 * A scheduled sweep force-stops and drops sessions left idle too long, so an
 * abandoned debug session doesn't leak a thread and a live browser session
 * forever (the async executor backing debug executions is unbounded).
 *
 * @author bcivel
 */
@Component
public class DebugSessionRegistry {

    private static final Logger LOG = LogManager.getLogger(DebugSessionRegistry.class);

    private final Map<String, DebugSession> sessions = new ConcurrentHashMap<>();

    @Autowired
    private ExecutionUUID executionUUID;
    @Autowired
    private IParameterService parameterService;

    public DebugSession create(String executionUUIDKey) {
        DebugSession debugSession = new DebugSession();
        sessions.put(executionUUIDKey, debugSession);
        return debugSession;
    }

    public DebugSession get(String executionUUIDKey) {
        return sessions.get(executionUUIDKey);
    }

    public void remove(String executionUUIDKey) {
        sessions.remove(executionUUIDKey);
    }

    // Idle timeout : how long a session can sit waiting for a "next" before it's force-stopped.
    // Global timeout : hard ceiling on the whole session's lifetime regardless of activity, so a
    // debug session left open (even if actively used) can't pin a thread + browser forever.
    //TODO int idleTimeoutMin = parameterService.getParameterIntegerByKey("cerberus_debugmode_idle_timeout_min", "", 60);
    //TODO int globalTimeoutMin = parameterService.getParameterIntegerByKey("cerberus_debugmode_global_timeout_min", "", 240);
    private static final long IDLE_TIMEOUT_MS = 60 * 60_000L;   // 1h between two "next"
    private static final long GLOBAL_TIMEOUT_MS = 4 * 60 * 60_000L; // 4h total session lifetime

    @Scheduled(fixedRate = 60000, initialDelay = 60000)
    public void sweepExpiredSessions() {

        long now = System.currentTimeMillis();

        for (Map.Entry<String, DebugSession> entry : sessions.entrySet()) {
            String uuid = entry.getKey();
            DebugSession debugSession = entry.getValue();

            boolean idleExpired = (now - debugSession.getLastActivityMs()) > IDLE_TIMEOUT_MS;
            boolean globalExpired = (now - debugSession.getCreatedAtMs()) > GLOBAL_TIMEOUT_MS;

            if (!idleExpired && !globalExpired) {
                continue;
            }

            LOG.info("Debug session UUID={} expired ({}) : forcing stop.", uuid,
                    globalExpired ? "global session timeout" : "idle timeout");

            TestCaseExecution execution = executionUUID.getTestCaseExecution(uuid);
            if (execution != null) {
                execution.setStopExecution(true);
            }
            debugSession.signalNext();
        }
    }
}