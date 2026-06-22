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
package org.cerberus.core.websocket.runtime;

import java.util.*;
import java.util.stream.Collectors;

import org.cerberus.core.api.dto.testcaseexecution.TestcaseExecutionLightDTOV001;
import org.cerberus.core.api.dto.testcaseexecution.TestcaseExecutionLightMapperV001;
import org.cerberus.core.crud.entity.TestCaseExecutionLight;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.engine.queuemanagement.entity.TestCaseExecutionQueueToTreat;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.websocket.WebSocketEventSender;
import org.cerberus.core.websocket.WebSocketStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Reads current Notification Center data.
 */
@Component
public class NotificationCenter {

    private final ITestCaseExecutionService testCaseExecutionService;
    private final ITestCaseExecutionQueueService testCaseExecutionQueueService;
    private final TestcaseExecutionLightMapperV001 testcaseExecutionLightMapperV001;
    @Autowired
    private WebSocketEventSender webSocketEventSender;

    public NotificationCenter(
            ITestCaseExecutionService testCaseExecutionService,
            ITestCaseExecutionQueueService testCaseExecutionQueueService,
            TestcaseExecutionLightMapperV001 testcaseExecutionLightMapperV001) {
        this.testCaseExecutionService = testCaseExecutionService;
        this.testCaseExecutionQueueService = testCaseExecutionQueueService;
        this.testcaseExecutionLightMapperV001 = testcaseExecutionLightMapperV001;
    }


    private List<TestcaseExecutionLightDTOV001> readRunningExecutions(String user) {
        try {
            List<TestCaseExecutionLight> executions =
                    testCaseExecutionService.readRunningExecutionLightByUser(user);

            List<TestcaseExecutionLightDTOV001> result = new ArrayList<>();

            for (TestCaseExecutionLight execution : executions) {
                result.add(testcaseExecutionLightMapperV001.toDTO(execution));
            }

            return result;

        } catch (CerberusException e) {
            throw new IllegalStateException("Unable to read running executions for user " + user, e);
        }
    }

    private List<TestCaseExecutionQueueToTreat> readQueuedExecutions(String user) {
        try {
            AnswerList<TestCaseExecutionQueueToTreat> answer =
                    testCaseExecutionQueueService.readQueueToTreat();

            if (answer == null || answer.getDataList() == null) {
                return Collections.emptyList();
            }

            return answer.getDataList()
                    .stream()
                    .filter(execution -> Objects.equals(user, execution.getUsrCreated()))
                    .collect(Collectors.toList());

        } catch (CerberusException e) {
            throw new IllegalStateException("Unable to read queued executions for user " + user, e);
        }
    }

    private List<TestcaseExecutionLightDTOV001> readLatestExecutions(String user) {
        try {
            List<TestCaseExecutionLight> executions =
                    testCaseExecutionService.readLastExecutionLightByUser(user);

            List<TestcaseExecutionLightDTOV001> result = new ArrayList<>();

            for (TestCaseExecutionLight execution : executions) {
                result.add(testcaseExecutionLightMapperV001.toDTO(execution));
            }

            return result;

        } catch (CerberusException e) {
            throw new IllegalStateException("Unable to read latest executions for user " + user, e);
        }
    }

    public void sendInitStatus(String sender) {
        webSocketEventSender.sendToUser(
                sender,
                WebSocketStatic.TYPE_NOTIFICATION_EXECUTING_INIT,
                WebSocketStatic.CHANNEL_NOTIFICATION,
                readRunningExecutions(sender));
        webSocketEventSender.sendToUser(
                sender,
                WebSocketStatic.TYPE_NOTIFICATION_QUEUED_INIT,
                WebSocketStatic.CHANNEL_NOTIFICATION,
                readQueuedExecutions(sender));
        webSocketEventSender.sendToUser(
                sender,
                WebSocketStatic.TYPE_NOTIFICATION_LASTEXECUTION_INIT,
                WebSocketStatic.CHANNEL_NOTIFICATION,
                readLatestExecutions(sender));
    }
}