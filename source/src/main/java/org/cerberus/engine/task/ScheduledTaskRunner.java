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
package org.cerberus.engine.task;

import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestCaseExecutionQueueService;
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
    
    private int refreshTickNumber = 5;
    private int tickNumber = 0;

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(ScheduledTaskRunner.class);

    @Scheduled(fixedDelay = 1000 /* 1000 */)
    public void nextStep() {
        if (tickNumber < refreshTickNumber) {
            tickNumber++;
            return;
        } else {
            tickNumber = 0;
        }
        // some code
        refreshTickNumber = parameterService.getParameterIntegerByKey("cerberus_automaticqueuecancellationjob_period", "", 10);
        performCancelOldQueueEntries();
    }

    private void performCancelOldQueueEntries() {
        LOG.info("automaticqueuecancellationjob Task triggered.");
        testCaseExecutionQueueService.cancelRunningOldQueueEntries();
        LOG.debug("automaticqueuecancellationjob Task ended.");
    }

}
