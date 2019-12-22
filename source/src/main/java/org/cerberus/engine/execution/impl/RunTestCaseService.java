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
package org.cerberus.engine.execution.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.exception.CerberusException;
import org.cerberus.engine.execution.IExecutionRunService;
import org.cerberus.engine.execution.IExecutionStartService;
import org.cerberus.engine.execution.IRunTestCaseService;
import org.cerberus.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.enums.MessageGeneralEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 23/01/2013
 * @since 2.0.0
 */
@Service
public class RunTestCaseService implements IRunTestCaseService {

    @Autowired
    private IExecutionStartService executionStartService;
    @Autowired
    private IExecutionRunService executionRunService;
    @Autowired
    private IExecutionThreadPoolService executionThreadPoolService;

    private static final Logger LOG = LogManager.getLogger(RunTestCaseService.class);

    @Override
    public TestCaseExecution runTestCase(TestCaseExecution tCExecution) {

        /**
         * Start Execution (Checks and Creation of ID)
         *
         */
        try {
            LOG.debug("Start Execution " + "ID=" + tCExecution.getId());
            tCExecution = executionStartService.startExecution(tCExecution);
            LOG.info("Execution Started : UUID=" + tCExecution.getExecutionUUID() + " ID=" + tCExecution.getId());

        } catch (CerberusException ex) {
            tCExecution.setResultMessage(ex.getMessageError());
            LOG.info("Execution not Launched : UUID=" + tCExecution.getExecutionUUID() + " causedBy=" + ex.getMessageError().getDescription());
            try {
                // After every execution finished we try to trigger more from the queue;-).
                executionThreadPoolService.executeNextInQueueAsynchroneously(false);
            } catch (CerberusException ex1) {
                LOG.error(ex1.toString(), ex1);
            }
            return tCExecution;
        }

        /**
         * Execute TestCase in new thread if asynchroneous execution
         */
        if (tCExecution.getId() != 0) {
            try {
                if (!tCExecution.isSynchroneous()) {
                    executionRunService.executeTestCaseAsynchroneously(tCExecution);
                } else {
                    tCExecution = executionRunService.executeTestCase(tCExecution);
                }
            } catch (CerberusException ex) {
                tCExecution.setResultMessage(ex.getMessageError());
                LOG.warn("Execution stopped due to exception. " + ex.getMessageError().getDescription(), ex);
                try {
                    // After every execution finished we try to trigger more from the queue;-).
                    executionThreadPoolService.executeNextInQueueAsynchroneously(false);
                } catch (CerberusException ex1) {
                    LOG.error(ex1.toString(), ex1);
                }
            } catch (Exception ex) {
                tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.GENERIC_ERROR));
                LOG.warn("Execution stopped due to exception : UUID=" + tCExecution.getExecutionUUID() + " causedBy=" + ex.toString(), ex);
                try {
                    // After every execution finished we try to trigger more from the queue;-).
                    executionThreadPoolService.executeNextInQueueAsynchroneously(false);
                } catch (CerberusException ex1) {
                    LOG.error(ex1.toString(), ex1);
                }
            }
        }
        /**
         * Return tcexecution object
         */
        LOG.debug("Exit RunTestCaseService : " + tCExecution.getId());
        return tCExecution;
    }
}
