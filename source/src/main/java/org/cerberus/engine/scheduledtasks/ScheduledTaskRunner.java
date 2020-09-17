/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.engine.scheduledtasks;

import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.engine.scheduler.SchedulerInit;
import org.cerberus.exception.CerberusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author bcivel
 */
// Component = instancié au déploiement. Singleton. 1 par instance
@Component
public class ScheduledTaskRunner {

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private ITestCaseExecutionQueueService testCaseExecutionQueueService;
    @Autowired
    private IExecutionThreadPoolService executionThreadPoolService;
    @Autowired
    private SchedulerInit schedulerInit;

    private int b1TickNumberTarget = 60;
    private int b1TickNumber = 1;
    private int b2TickNumberTarget = 30;
    private int b2TickNumber = 1;
    private int b3TickNumberTarget = 1;
    private int b3TickNumber = 1;

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(ScheduledTaskRunner.class);

    @Scheduled(fixedRate = 60000, initialDelay = 30000 /* Every minute */)
    public void nextStep() {
        LOG.debug("Schedule Start. " + b1TickNumber + "/" + b1TickNumberTarget + " - " + b2TickNumber + "/" + b2TickNumberTarget);

        // We get the new period from paarameter and trigger the Queue automatic cancellation job.
        b1TickNumberTarget = parameterService.getParameterIntegerByKey("cerberus_automaticqueuecancellationjob_period", "", 60);
        b2TickNumberTarget = parameterService.getParameterIntegerByKey("cerberus_automaticqueueprocessingjob_period", "", 30);

        if (b1TickNumber < b1TickNumberTarget) {
            b1TickNumber++;
        } else {
            b1TickNumber = 1;
            performBatch1_CancelOldQueueEntries();
        }

        if (b2TickNumber < b2TickNumberTarget) {
            b2TickNumber++;
        } else {
            b2TickNumber = 1;
            // We trigger the Queue Processing job.
            performBatch2_ProcessQueue();
        }

        if (b3TickNumber < b3TickNumberTarget) {
            b3TickNumber++;
        } else {
            b3TickNumber = 1;
            // We trigger the Queue Processing job.
            performBatch3_SchedulerInit();
        }

        LOG.debug("Schedule Stop. " + b1TickNumber + "/" + b1TickNumberTarget + " - " + b2TickNumber + "/" + b2TickNumberTarget);
    }

    private void performBatch1_CancelOldQueueEntries() {
        LOG.info("automaticqueuecancellationjob Task triggered.");
        if (parameterService.getParameterBooleanByKey("cerberus_automaticqueuecancellationjob_active", "", true)) {
            testCaseExecutionQueueService.cancelRunningOldQueueEntries();
        } else {
            LOG.info("automaticqueuecancellationjob Task disabled by config (cerberus_automaticqueuecancellationjob_active).");
        }
        LOG.info("automaticqueuecancellationjob Task ended.");
    }

    private void performBatch2_ProcessQueue() {
        LOG.info("automaticqueueprocessingjob Task triggered.");
        if (parameterService.getParameterBooleanByKey("cerberus_automaticqueueprocessingjob_active", "", true)) {
            try {
                executionThreadPoolService.executeNextInQueue(false);
            } catch (CerberusException ex) {
                LOG.error(ex.toString(), ex);
            }
        } else {
            LOG.info("automaticqueueprocessingjob Task disabled by config (cerberus_automaticqueueprocessingjob_active).");
        }
        LOG.info("automaticqueueprocessingjob Task ended.");
    }

    private void performBatch3_SchedulerInit() {
        try {
            LOG.info("SchedulerInit Task triggered.");
            schedulerInit.init();
            LOG.info("SchedulerInit Task ended.");
        } catch (Exception e) {
            LOG.error("ScheduleEntry init from scheduletaskrunner failed : " + e);
        }

    }

}
