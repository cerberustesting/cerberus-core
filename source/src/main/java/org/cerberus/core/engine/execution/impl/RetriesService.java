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
package org.cerberus.core.engine.execution.impl;

import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionQueue;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.core.engine.execution.IRetriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RetriesService implements IRetriesService {

    @Autowired
    private ITestCaseExecutionQueueService executionQueueService;

    private static final Logger LOG = LogManager.getLogger(RetriesService.class);

    // Retry management, in case the result is not (OK or NE), we execute the job again reducing the retry to 1.
    @Override
    public boolean manageRetries(TestCaseExecution tCExecution) {
        if (tCExecution.getNumberOfRetries() > 0
                && !tCExecution.getResultMessage().getCodeString().equals("OK")
                && !tCExecution.getResultMessage().getCodeString().equals("NE")) {
            TestCaseExecutionQueue newExeQueue;
            if (tCExecution.getQueueID() > 0) {
                // If QueueId exist, we try to get the original execution queue.
                try {
                    newExeQueue = executionQueueService.convert(executionQueueService.readByKey(tCExecution.getQueueID(), false));
                } catch (Exception e) {
                    // Unfortunatly the execution no longuer exist so we pick initial value.
                    newExeQueue = tCExecution.getTestCaseExecutionQueue();
                }
            } else {
                // Initial Execution does not come from the queue so we pick the value created at the beginning of the execution.
                newExeQueue = tCExecution.getTestCaseExecutionQueue();
            }
            return manageRetries(newExeQueue);
        }
        return false;
    }

    private boolean manageRetries(final TestCaseExecutionQueue tCExecutionQueue) {
        // Forcing init value for that new queue execution : exeid=0, no debugflag and State = QUEUED
        int newRetry = tCExecutionQueue.getRetries() - 1;
        if (newRetry < 0) {
            LOG.debug("Execution not retried because no more retry.");
            return false; // no automatic retry if newRetry <=0
        }
        if (TestCaseExecutionQueue.State.CANCELLED.toString().equals(tCExecutionQueue.getState().toString())) {
            LOG.debug("Execution not retried because Current Queue Entry is CANCELLED.");
            return false; // no automatic retry if source queue has been cancelled. #1752
        }
        long exeQueue = tCExecutionQueue.getId();
        tCExecutionQueue.setId(0);
        tCExecutionQueue.setDebugFlag("N");
        tCExecutionQueue.setComment("Added from Retry. Still " + newRetry + " attempt(s) to go.");
        tCExecutionQueue.setState(TestCaseExecutionQueue.State.QUEUED);
        tCExecutionQueue.setRetries(newRetry);
        // Insert execution to the Queue.
        executionQueueService.create(tCExecutionQueue, false, exeQueue, TestCaseExecutionQueue.State.QUEUED, new HashMap<>());
        return true;
    }
}
