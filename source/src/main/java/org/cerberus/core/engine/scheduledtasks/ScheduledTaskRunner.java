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
package org.cerberus.core.engine.scheduledtasks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.TimeZone;
import org.cerberus.core.crud.service.IMyVersionService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueDepService;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.core.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.core.engine.scheduler.SchedulerInit;
import org.cerberus.core.exception.CerberusException;
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
    @Autowired
    private ITestCaseExecutionQueueDepService testCaseExecutionQueueDepService;
    @Autowired
    private IMyVersionService myVersionService;

    private int b1TickNumberTarget = 60;
    private int b1TickNumber = 1;
    private int b2TickNumberTarget = 30;
    private int b2TickNumber = 1;
    private int b3TickNumberTarget = 1;
    private int b3TickNumber = 1;
    private int b4TickNumberTarget = 1;
    private int b4TickNumber = 1;

    private long loadingTimestamp = 0;
    private boolean instanceActive = true;

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.S";

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(ScheduledTaskRunner.class);

    @Scheduled(fixedRate = 60000, initialDelay = 30000 /* Every minute */)
    public void nextStep() {

        /**
         * Secure only 1 Task trigger on a given cerberus instance. Multiple
         * jobs could be triggered if several instance is running. The first
         * scheduler trigger will define loadingTimestamp and instanceActive
         * based on a lock value defined at database level. Only the 1st update
         * (within 10 second delay) will be considered as active. All others
         * will be disabled.
         */
        if (loadingTimestamp == 0) {
            Date d = new Date();
            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setTimeZone(tz);
            long newVersion = new java.util.Date().getTime();
            loadingTimestamp = newVersion;
            LOG.debug("Setting local scheduler Version to : {}", newVersion);
            instanceActive = myVersionService.updateAndLockVersionEntryDuringMs("scheduler_active_instance_version", newVersion, 10000);
        }

        if (instanceActive) {

            LOG.debug("Schedule ({}) Start. "
                    + b1TickNumber + "/" + b1TickNumberTarget + " - "
                    + b2TickNumber + "/" + b2TickNumberTarget + " - "
                    + b3TickNumber + "/" + b3TickNumberTarget + " - "
                    + b4TickNumber + "/" + b4TickNumberTarget,
                    loadingTimestamp);

            // We get the new period of each job from parameter.
            b1TickNumberTarget = parameterService.getParameterIntegerByKey("cerberus_automaticqueuecancellationjob_period", "", 60);
            b2TickNumberTarget = parameterService.getParameterIntegerByKey("cerberus_automaticqueueprocessingjob_period", "", 30);
            b3TickNumberTarget = 1;
            b4TickNumberTarget = 1;

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
                // We trigger the Scheduler init job.
                performBatch3_SchedulerInit();
            }

            if (b4TickNumber < b4TickNumberTarget) {
                b4TickNumber++;
            } else {
                b4TickNumber = 1;
                // We trigger the Queue dependencies release by timing.
                performBatch4_ProcessTimingBasedQueueDependencies();
            }

            LOG.debug("Schedule ({}) Stop. "
                    + b1TickNumber + "/" + b1TickNumberTarget + " - "
                    + b2TickNumber + "/" + b2TickNumberTarget + " - "
                    + b3TickNumber + "/" + b3TickNumberTarget + " - "
                    + b4TickNumber + "/" + b4TickNumberTarget,
                    loadingTimestamp);

        } else {
            LOG.debug("Schedule ({}) disabled. ", loadingTimestamp);

        }

    }

    private void performBatch1_CancelOldQueueEntries() {
        LOG.info("Schedule ({}) : automaticqueuecancellationjob Task triggered. (triggered every {} minutes)", loadingTimestamp, b1TickNumberTarget);
        if (parameterService.getParameterBooleanByKey("cerberus_automaticqueuecancellationjob_active", "", true)) {
            testCaseExecutionQueueService.cancelRunningOldQueueEntries();
        } else {
            LOG.info("Schedule ({}) : automaticqueuecancellationjob Task disabled by config (cerberus_automaticqueuecancellationjob_active).", loadingTimestamp);
        }
        LOG.info("Schedule ({}) : automaticqueuecancellationjob Task ended.", loadingTimestamp);
    }

    private void performBatch2_ProcessQueue() {
        LOG.info("Schedule ({}) : automaticqueueprocessingjob Task triggered. (triggered every {} minutes)", loadingTimestamp, b2TickNumberTarget);
        if (parameterService.getParameterBooleanByKey("cerberus_automaticqueueprocessingjob_active", "", true)) {
            try {
                executionThreadPoolService.executeNextInQueue(false);
            } catch (CerberusException ex) {
                LOG.error(ex.toString(), ex);
            }
        } else {
            LOG.info("Schedule ({}) : automaticqueueprocessingjob Task disabled by config (cerberus_automaticqueueprocessingjob_active).", loadingTimestamp);
        }
        LOG.info("Schedule ({}) : automaticqueueprocessingjob Task ended.", loadingTimestamp);
    }

    private void performBatch3_SchedulerInit() {
        try {
            LOG.debug("Schedule ({}) : SchedulerInit Task triggered. (Quartz User Scheduler)", loadingTimestamp);
            schedulerInit.init();
            LOG.debug("Schedule ({}) : SchedulerInit Task ended.", loadingTimestamp);
        } catch (Exception e) {
            LOG.error("ScheduleEntry init from scheduletaskrunner failed : " + e, e);
        }

    }

    private void performBatch4_ProcessTimingBasedQueueDependencies() {
        try {
            LOG.debug("Schedule ({}) : Queue dep timing Task triggered.", loadingTimestamp);
            int nbReleased = testCaseExecutionQueueDepService.manageDependenciesCheckTimingWaiting();
            if (nbReleased > 0) {
                LOG.info("Schedule ({}) : " + nbReleased + " Queue entry(ies) has(have) been released due to TIMING dependencies. We trigger now the processing of the queue entry.", loadingTimestamp);
                executionThreadPoolService.executeNextInQueue(false);
            }
            LOG.debug("Schedule ({}) : Queue dep timing Task ended.", loadingTimestamp);
        } catch (Exception e) {
            LOG.error("Queue dep timing Task from scheduletaskrunner failed : " + e, e);
        }
    }

}
