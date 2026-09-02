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
package org.cerberus.core.mcp.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Lets an MCP tool sleep until something happens to a tag, instead of asking the database over and
 * over whether a run has finished.
 *
 * <p>The execution engine already announces the end of a run in process, at the moment it happens:
 * {@code ExecutionRunService} calls {@code WebSocketService.notifyExecutionDone} so the GUI can
 * refresh. That call is the earliest, cheapest knowledge Cerberus has that an execution is over,
 * and this class simply lets a waiting MCP call listen to it too. Nothing about the WebSocket
 * itself is involved: the socket is a transport towards browsers, whereas what a tool needs is the
 * signal that feeds it.</p>
 *
 * <p><strong>A signal is a hint, never the truth.</strong> A waiter woken here always re-reads the
 * tag from the database before concluding anything, and it also wakes on its own after a short
 * delay. That distinction is what makes the mechanism safe in the two cases where the signal
 * cannot arrive:</p>
 * <ul>
 *   <li>Cerberus runs on several instances. Nothing pins an execution to a node — the queue
 *       carries no instance column and {@code cerberus_queueexecution_enable} is global — so a run
 *       may well execute in another JVM, where this registry knows nothing about the waiter.</li>
 *   <li>An entry ends without ever executing (cancelled, or in error), in which case no execution
 *       completion is ever announced.</li>
 * </ul>
 *
 * <p>Missing a signal therefore costs latency, never correctness: the waiter falls back to its
 * periodic re-read and behaves exactly as a polling implementation would.</p>
 */
@Component
public class MCPExecutionSignal {

    private static final Logger LOG = LogManager.getLogger(MCPExecutionSignal.class);

    /**
     * Latches to release when a tag shows activity, keyed by tag.
     *
     * <p>One latch per waiting call rather than one per tag: several agents may follow the same
     * campaign, and a latch cannot be reused once counted down.</p>
     */
    private final ConcurrentHashMap<String, Set<CountDownLatch>> waitersByTag = new ConcurrentHashMap<>();

    /**
     * Registers interest in a tag and returns the latch to wait on.
     *
     * <p>Callers must pass the returned latch back to {@link #unregister(String, CountDownLatch)}
     * in a {@code finally} block, otherwise a tag that never completes would retain its waiters
     * for the lifetime of the JVM.</p>
     *
     * @param tag the tag to watch, must not be blank.
     * @return a latch released as soon as the tag shows activity.
     */
    public CountDownLatch register(String tag) {
        CountDownLatch latch = new CountDownLatch(1);
        waitersByTag.computeIfAbsent(tag, key -> ConcurrentHashMap.newKeySet()).add(latch);
        return latch;
    }

    /**
     * Drops a waiter, and the tag entry itself once nobody is left waiting on it.
     */
    public void unregister(String tag, CountDownLatch latch) {
        waitersByTag.computeIfPresent(tag, (key, latches) -> {
            latches.remove(latch);
            return latches.isEmpty() ? null : latches;
        });
    }

    /**
     * Wakes every call waiting on this tag.
     *
     * <p>Called from the execution engine's notification path, so it must never disturb it: a tag
     * with no waiter is the common case and costs a single map lookup, and any failure is logged
     * rather than propagated. Releasing a latch only invites the waiter to re-read the database —
     * it never asserts that the run is over, which is why signalling slightly too often is
     * harmless.</p>
     *
     * @param tag the tag that just showed activity; blank or unknown tags are ignored.
     */
    public void signal(String tag) {
        if (tag == null || tag.isBlank()) {
            return;
        }

        try {
            Set<CountDownLatch> latches = waitersByTag.get(tag);
            if (latches == null) {
                return;
            }
            for (CountDownLatch latch : latches) {
                latch.countDown();
            }
        } catch (RuntimeException e) {
            // The caller is notifying the end of an execution; failing to wake an MCP waiter must
            // not interrupt that, the waiter will fall back to its periodic re-read.
            LOG.warn("Unable to wake the MCP waiters of tag {}.", tag, e);
        }
    }

    /**
     * Number of tags currently watched. Exposed for diagnostics and tests.
     */
    public int watchedTagCount() {
        return waitersByTag.size();
    }
}
