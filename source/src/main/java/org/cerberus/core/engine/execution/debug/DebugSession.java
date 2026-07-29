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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;

/**
 * Holds the state a paused debug execution needs so the worker thread blocked
 * inside {@code ExecutionRunService.executeStep}/{@code executeAction} can be
 * woken up by a "next"/"retry" (or "stop") command coming from a different
 * thread (e.g. a REST call).
 *
 * @author bcivel
 */
public class DebugSession {

    // Capacity 1 : at most one command is ever pending at a time.
    private final BlockingQueue<DebugCommand> signal = new LinkedBlockingQueue<>(1);

    // pendingAction is set at both an action-level pause (about to run the action) and a
    // control-level pause (about to run one of that action's controls, kept as context so the
    // status endpoint can report "which action's controls are we in" — including its stepId, so
    // no separate step-identifying field is needed here). pendingControl is only non-null for
    // the latter.
    private volatile TestCaseStepAction pendingAction;
    private volatile TestCaseStepActionControl pendingControl;
    // True when the currently pending action/control already failed once and is being
    // re-offered for a retry-or-move-on decision, rather than about to run for the first time.
    private volatile boolean pendingFailed;
    private volatile long lastActivityMs = System.currentTimeMillis();
    private final long createdAtMs = System.currentTimeMillis();

    public DebugCommand awaitCommand() throws InterruptedException {
        DebugCommand cmd = signal.take();
        lastActivityMs = System.currentTimeMillis();
        return cmd;
    }

    public boolean signalNext() {
        lastActivityMs = System.currentTimeMillis();
        return signal.offer(DebugCommand.NEXT);
    }

    public boolean signalRetry() {
        lastActivityMs = System.currentTimeMillis();
        return signal.offer(DebugCommand.RETRY);
    }

    public void clearPending() {
        this.pendingAction = null;
        this.pendingControl = null;
        this.pendingFailed = false;
    }

    public TestCaseStepAction getPendingAction() {
        return pendingAction;
    }

    public void setPendingAction(TestCaseStepAction pendingAction) {
        this.pendingAction = pendingAction;
    }

    public TestCaseStepActionControl getPendingControl() {
        return pendingControl;
    }

    public void setPendingControl(TestCaseStepActionControl pendingControl) {
        this.pendingControl = pendingControl;
    }

    public boolean isPendingFailed() {
        return pendingFailed;
    }

    public void setPendingFailed(boolean pendingFailed) {
        this.pendingFailed = pendingFailed;
    }

    public long getLastActivityMs() {
        return lastActivityMs;
    }

    public long getCreatedAtMs() {
        return createdAtMs;
    }
}