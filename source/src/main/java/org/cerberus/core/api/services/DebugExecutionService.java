/*
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
package org.cerberus.core.api.services;

import java.util.UUID;
import lombok.AllArgsConstructor;
import org.cerberus.core.api.dto.debugexecution.DebugExecutionAckDTOV001;
import org.cerberus.core.api.dto.debugexecution.DebugExecutionStartDTOV001;
import org.cerberus.core.api.dto.debugexecution.DebugExecutionStartResultDTOV001;
import org.cerberus.core.api.dto.debugexecution.DebugExecutionStatusDTOV001;
import org.cerberus.core.api.dto.debugexecution.DebugExecutionStatusMapperV001;
import org.cerberus.core.api.exceptions.EntityNotFoundException;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionQueue;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.cerberus.core.crud.factory.IFactoryTestCase;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecution;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecutionQueue;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.engine.entity.ExecutionUUID;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.engine.execution.IRunTestCaseService;
import org.cerberus.core.engine.execution.debug.DebugSession;
import org.cerberus.core.engine.execution.debug.DebugSessionRegistry;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.FactoryCreationException;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.version.Infos;
import org.springframework.stereotype.Service;

/**
 * Business logic for debug-mode test case executions : start a session
 * (bypassing the queue, running straight through {@link IRunTestCaseService}),
 * step it forward one action at a time via "next", stop it, and report its
 * current status. See {@link DebugSessionRegistry} for the pause/resume
 * mechanism itself.
 *
 * @author bcivel
 */
@Service
@AllArgsConstructor
public class DebugExecutionService {

    // Absolute ceiling for an explicit, user-requested timeout override (see resolveTimeout) —
    // not a default. Debug mode does NOT need a bigger element-wait timeout than normal : the
    // pause between "next" clicks is DebugSession.awaitCommand(), which has nothing to do with
    // Selenium timeouts. Forcing a huge default here (an earlier iteration of this feature did)
    // was actively harmful : when an action genuinely can't find its element, "Next" would then
    // hang for up to that whole duration instead of failing fast like a normal execution does.
    private static final int MAX_TIMEOUT_MS = 14_400_000; // 4h

    private final IRunTestCaseService runTestCaseService;
    private final IFactoryTestCaseExecution factoryTestCaseExecution;
    private final IFactoryTestCase factoryTestCase;
    private final IFactoryTestCaseExecutionQueue factoryTestCaseExecutionQueue;
    private final ExecutionUUID executionUUID;
    private final DebugSessionRegistry debugSessionRegistry;
    private final IParameterService parameterService;
    private final DebugExecutionStatusMapperV001 debugExecutionStatusMapper;

    public DebugExecutionStartResultDTOV001 startDebugExecution(DebugExecutionStartDTOV001 request, String login) {
        if (StringUtil.isEmptyOrNull(request.getTest())) {
            throw new IllegalArgumentException("Parameter 'test' is mandatory.");
        }
        if (StringUtil.isEmptyOrNull(request.getTestCase())) {
            throw new IllegalArgumentException("Parameter 'testCase' is mandatory.");
        }
        if (StringUtil.isEmptyOrNull(request.getCountry())) {
            throw new IllegalArgumentException("Parameter 'country' is mandatory.");
        }
        if (StringUtil.isEmptyOrNull(request.getEnvironment())) {
            throw new IllegalArgumentException("Parameter 'environment' is mandatory.");
        }

        String timeout = resolveTimeout(request.getTimeoutMs());
        TestCase tCase = factoryTestCase.create(request.getTest(), request.getTestCase());

        TestCaseExecution execution = factoryTestCaseExecution.create(0, request.getTest(), request.getTestCase(), null, null, null,
                request.getEnvironment(), request.getCountry(),
                orEmpty(request.getRobot()), "", "", "", "",
                orEmpty(request.getBrowser()), orEmpty(request.getVersion()), orEmpty(request.getPlatform()),
                0, 0, "", "", "", null, null,
                orEmpty(request.getTag()),
                // verbose, screenshot, video, pageSource, robotLog, consoleLog — screenshot/
                // pageSource forced to 2 ("every action/control") so each step of a debug
                // session leaves behind a screenshot + HTML source, shown on the debug page.
                1, 2, 0, 2, 1, 1,
                false, timeout, "compact", null,
                Infos.getInstance().getProjectNameAndVersion(), tCase, null, null,
                0, "", "", "", "",
                "", "",
                null, new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTSTARTED), login,
                0, "", null, "", "",
                "", "", "", "", "", "", "",
                TestCaseExecution.MANUAL_N, "", 0, 0, "",
                login, null, login, null);

        execution.setDebugMode(true);

        // ExecutionStartService.startExecution() unconditionally dereferences
        // execution.getTestCaseExecutionQueue() (e.g. to store the resolved system) even for a
        // non-queued run — RunTestCaseV002 always attaches one for the same reason. Skipping
        // this causes a NullPointerException deep inside startExecution().
        try {
            TestCaseExecutionQueue queueExecution = factoryTestCaseExecutionQueue.create(0, "", request.getTest(), request.getTestCase(),
                    request.getCountry(), request.getEnvironment(), orEmpty(request.getRobot()), "", "", "",
                    orEmpty(request.getBrowser()), orEmpty(request.getVersion()), orEmpty(request.getPlatform()), "",
                    0, "", "", "", "",
                    orEmpty(request.getTag()), 2, 0, 1, timeout, 2, 1, 1,
                    0, 0, TestCaseExecution.MANUAL_N, login, null, login, null);
            execution.setQueueID(0);
            execution.setTestCaseExecutionQueue(queueExecution);
        } catch (FactoryCreationException ex) {
            throw new IllegalArgumentException("Unable to prepare the debug execution : " + ex.getMessage(), ex);
        }

        String uuid = UUID.randomUUID().toString();
        execution.setExecutionUUID(uuid);
        // Registered *before* starting the (async) execution, so the worker thread can never
        // reach the first pause point before a DebugSession exists to block on.
        executionUUID.setExecutionUUID(uuid, execution);
        debugSessionRegistry.create(uuid);

        execution = runTestCaseService.runTestCase(execution);

        if (execution.getId() == 0) {
            // startExecution failed validation (bad params, cerberus_automaticexecution_enable, ...) :
            // nothing was actually started, so drop the bookkeeping we just created.
            debugSessionRegistry.remove(uuid);
            executionUUID.removeExecutionUUID(uuid);
            String reason = (execution.getResultMessage() != null) ? execution.getResultMessage().getDescription() : "unknown error";
            throw new IllegalArgumentException("Debug execution could not be started : " + reason);
        }

        return DebugExecutionStartResultDTOV001.builder()
                .executionId(execution.getId())
                .executionUUID(uuid)
                .build();
    }

    public DebugExecutionAckDTOV001 sendNext(String uuid) {
        DebugSession debugSession = debugSessionRegistry.get(uuid);
        if (debugSession == null) {
            throw new EntityNotFoundException(DebugSession.class, "executionUUID", uuid);
        }
        boolean accepted = debugSession.signalNext();
        return DebugExecutionAckDTOV001.builder().executionUUID(uuid).accepted(accepted).build();
    }

    public DebugExecutionAckDTOV001 sendRetry(String uuid) {
        DebugSession debugSession = debugSessionRegistry.get(uuid);
        if (debugSession == null) {
            throw new EntityNotFoundException(DebugSession.class, "executionUUID", uuid);
        }
        boolean accepted = debugSession.signalRetry();
        return DebugExecutionAckDTOV001.builder().executionUUID(uuid).accepted(accepted).build();
    }

    public DebugExecutionAckDTOV001 stopDebugSession(String uuid) {
        TestCaseExecution execution = executionUUID.getTestCaseExecution(uuid);
        if (execution == null) {
            throw new EntityNotFoundException(TestCaseExecution.class, "executionUUID", uuid);
        }
        execution.setStopExecution(true);

        DebugSession debugSession = debugSessionRegistry.get(uuid);
        boolean accepted = (debugSession != null) && debugSession.signalNext();
        return DebugExecutionAckDTOV001.builder().executionUUID(uuid).accepted(accepted).build();
    }

    public DebugExecutionStatusDTOV001 getStatus(String uuid) {
        TestCaseExecution execution = executionUUID.getTestCaseExecution(uuid);

        if (execution == null) {
            return DebugExecutionStatusDTOV001.builder()
                    .executionUUID(uuid)
                    .state("FINISHED")
                    .build();
        }

        DebugSession debugSession = debugSessionRegistry.get(uuid);
        TestCaseStepAction pendingAction = (debugSession != null) ? debugSession.getPendingAction() : null;
        TestCaseStepActionControl pendingControl = (debugSession != null) ? debugSession.getPendingControl() : null;
        boolean waiting = (pendingAction != null) || (pendingControl != null);
        boolean pendingFailed = debugSession != null && debugSession.isPendingFailed();

        return debugExecutionStatusMapper.toDTO(execution, waiting, pendingAction, pendingControl, pendingFailed);
    }

    // Empty string ("") means "no override" — RobotServerService then falls back to the
    // normal cerberus_selenium_wait_element/sikuli/appium parameters, exactly like a regular
    // (non-debug) execution. Only an explicit request.timeoutMs changes that, clamped to a
    // sane ceiling.
    private String resolveTimeout(Integer requestedMs) {
        if (requestedMs == null || requestedMs <= 0) {
            return "";
        }
        return String.valueOf(Math.min(requestedMs, MAX_TIMEOUT_MS));
    }

    private static String orEmpty(String value) {
        return StringUtil.isEmptyOrNull(value) ? "" : value;
    }
}