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

import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.exception.CerberusException;
import org.cerberus.engine.execution.IExecutionRunService;
import org.cerberus.engine.execution.IExecutionStartService;
import org.cerberus.engine.execution.IRunTestCaseService;
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

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(RunTestCaseService.class);

    @Override
    public TestCaseExecution runTestCase(TestCaseExecution tCExecution) {

        /**
         * Start Execution (Checks and Creation of ID)
         *
         */
        try {
            LOG.debug("Start Execution " + "__ID=" + tCExecution.getId());
            tCExecution = executionStartService.startExecution(tCExecution);
            LOG.info("Execution Started : UUID=" + tCExecution.getExecutionUUID() + "__ID=" + tCExecution.getId());

        } catch (CerberusException ex) {
            tCExecution.setResultMessage(ex.getMessageError());
            LOG.info("Execution not Launched : UUID=" + tCExecution.getExecutionUUID() + "__causedBy=" + ex.getMessageError().getDescription());
            return tCExecution;
        }

        /**
         * Execute TestCase in new thread if asynchroneous execution
         */
        if (tCExecution.getId() != 0) {
            try {
                if (!tCExecution.isSynchroneous()) {
                    executionRunService.executeAsynchroneouslyTestCase(tCExecution);
                } else {
                    tCExecution = executionRunService.executeTestCase(tCExecution);
                }
            } catch (CerberusException ex) {
                tCExecution.setResultMessage(ex.getMessageError());
            } catch (Exception ex) {
                LOG.warn("Execution stopped due to exception : UUID=" + tCExecution.getExecutionUUID() + "__causedBy=" + ex.toString());
                tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.GENERIC_ERROR));
            }
        }
        /**
         * Return tcexecution object
         */
        LOG.debug("Exit RunTestCaseService : " + tCExecution.getId());
        return tCExecution;
    }
}
