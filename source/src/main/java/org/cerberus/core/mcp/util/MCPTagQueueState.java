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

import org.cerberus.core.crud.entity.TestCaseExecutionQueue;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Decides whether a tag still owes results, from the execution queue.
 *
 * <p>The obvious source — the counters Cerberus maintains on the {@code tag} row — cannot answer
 * that question on its own. {@code TagService.createAuto} creates the row with every counter at
 * zero and they are only recomputed once executions start reporting back, so between queuing a run
 * and its first completion the counters are indistinguishable from those of a finished run. A
 * check built on them reports a run that has not started as already over, which is worse than no
 * answer: a caller acts on a verdict for executions that do not exist yet.</p>
 *
 * <p>The queue has no such blind spot. An entry exists from the moment the run is requested, and it
 * leaves a non-terminal state exactly once its outcome is settled — including when it is cancelled
 * or fails to start, neither of which ever produces an execution row.</p>
 *
 * <p>Shared by every MCP tool that reports on a run so the definition of "finished" cannot drift
 * between the tool that waits and the tool that takes a snapshot.</p>
 */
@Component
public class MCPTagQueueState {

    /**
     * Queue states in which an entry still owes a result. Everything else — DONE, CANCELLED,
     * ERROR — is settled, whether or not it produced an execution.
     */
    private static final List<String> PENDING_STATES = List.of(
            TestCaseExecutionQueue.State.QUTEMP.name(),
            TestCaseExecutionQueue.State.QUWITHDEP.name(),
            TestCaseExecutionQueue.State.QUWITHDEP_PAUSED.name(),
            TestCaseExecutionQueue.State.QUEUED.name(),
            TestCaseExecutionQueue.State.QUEUED_PAUSED.name(),
            TestCaseExecutionQueue.State.WAITING.name(),
            TestCaseExecutionQueue.State.STARTING.name(),
            TestCaseExecutionQueue.State.EXECUTING.name()
    );

    private final ITestCaseExecutionQueueService testCaseExecutionQueueService;

    public MCPTagQueueState(ITestCaseExecutionQueueService testCaseExecutionQueueService) {
        this.testCaseExecutionQueueService = testCaseExecutionQueueService;
    }

    /**
     * How much of a run the queue still owes.
     *
     * @param total   queue entries created under the tag, whatever their state.
     * @param pending those that have not settled yet.
     */
    public record State(int total, long pending) {

        /**
         * Whether the run is over. A tag with no queue entry counts as settled: there is nothing
         * left to wait for, either because nothing was ever queued or because the entries have
         * been purged. Callers distinguish the two with {@link #total()}.
         */
        public boolean settled() {
            return pending == 0;
        }

        /** Whether the tag has any queue entry at all. */
        public boolean hasEntries() {
            return total > 0;
        }
    }

    /**
     * Reads the queue state of a tag.
     *
     * @param tag the tag to inspect.
     * @return the counts, never {@code null}.
     * @throws CerberusException when the queue cannot be read.
     */
    public State read(String tag) throws CerberusException {
        AnswerList<TestCaseExecutionQueue> answer = testCaseExecutionQueueService.readByVarious1(tag, null, false);
        List<TestCaseExecutionQueue> entries = answer.getDataList() == null ? List.of() : answer.getDataList();

        long pending = entries.stream()
                .filter(entry -> entry.getState() != null && PENDING_STATES.contains(entry.getState().name()))
                .count();

        return new State(entries.size(), pending);
    }
}
