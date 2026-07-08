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
package org.cerberus.core.websocket;

import org.cerberus.core.api.dto.campaignexecution.CampaignExecutionMapperV001;
import org.cerberus.core.api.dto.testcaseexecution.TestcaseExecutionLightDTOV001;
import org.cerberus.core.api.dto.testcaseexecution.TestcaseExecutionLightMapperV001;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.websocket.runtime.ExecutionMonitor;
import org.cerberus.core.websocket.runtime.ObjectChangeHistory;
import org.cerberus.core.websocket.runtime.QueueStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single place mapping a business event of the execution engine (start / update / done / ...)
 * to the set of WebSocket channels it must push to.
 * <p>
 * Callers (execution engine, queue management, ...) should not know the individual channels
 * involved for a given event ; they call one method here per event instead of pushing to
 * each channel themselves.
 */
@Service
public class WebSocketService {

    @Autowired
    private WebSocketEventSender webSocketEventSender;
    @Autowired
    private TestcaseExecutionLightMapperV001 testcaseExecutionLightMapper;
    @Autowired
    private CampaignExecutionMapperV001 campaignExecutionMapper;
    @Autowired
    private ExecutionMonitor executionMonitor;
    @Autowired
    private ObjectChangeHistory objectChangeHistory;

    /**
     * Execution just got its RunID and started.
     */
    public void notifyExecutionStart(TestCaseExecution execution) {
        webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_EXECUTION_START, execution.toJson(true));

        /* Push notification to user that suscribed on the testcaseexecution page */
        webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_EXECUTION_START_ID(execution.getId()), execution.toJson(true).toMap());
        /* Push light execution to user who execute the testcase */
        TestcaseExecutionLightDTOV001 executionLight = testcaseExecutionLightMapper.toDTO(execution);
        webSocketEventSender.sendToUser(execution.getExecutor(), WebSocketStatic.CHANNEL_MYEXECUTION_LIGHT_START, executionLight);
        /* Push light execution to channel Execution */
        webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_EXECUTION_LIGHT_UPDATE, executionLight);
        if (execution.getTagObj() != null && execution.getTagObj().getCampaign() != null) {
            webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_CAMPAIGN_DELTA_ID(execution.getTagObj().getCampaign()), executionLight);
        }
    }

    /**
     * Execution progressed (step/action/control result, status change, ...).
     *
     * @param forcePush true to bypass throttling (e.g. final update of a step)
     */
    public void notifyExecutionUpdate(TestCaseExecution execution, boolean forcePush) {
        // TODO : Send diff only
        /* Push delta to user that suscribed on the testcaseexecution page */
        // webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_EXECUTION_DELTA_ID(execution.getId()), execution.toJson(true).toMap(), !forcePush, false);

        /* Push notification to user that suscribed on the testcaseexecution page */
        webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_EXECUTION_UPDATE_ID(execution.getId()), execution.toJson(true).toMap(), !forcePush, true);
        /* Push light execution to user who execute the testcase */
        TestcaseExecutionLightDTOV001 executionLight = testcaseExecutionLightMapper.toDTO(execution);
        webSocketEventSender.sendToUser(execution.getExecutor(), WebSocketStatic.CHANNEL_MYEXECUTION_LIGHT_UPDATE, executionLight);
        /* Push light execution to channel Execution */
        webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_EXECUTION_LIGHT_UPDATE, executionLight);
    }

    /**
     * Execution is fully stopped (robots released, final status saved).
     */
    public void notifyExecutionDone(TestCaseExecution execution) {
        /* Push notification to user that suscribed on the testcaseexecution page */
        webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_EXECUTION_DONE_ID(execution.getId()), execution.toJson(true).toMap());
        /* Push light execution to user who execute the testcase */
        TestcaseExecutionLightDTOV001 executionLight = testcaseExecutionLightMapper.toDTO(execution);
        webSocketEventSender.sendToUser(execution.getExecutor(), WebSocketStatic.CHANNEL_MYEXECUTION_LIGHT_DONE, executionLight);
        /* Push light execution to channel execution */
        webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_EXECUTION_LIGHT_DONE, executionLight);
        /* Push execution to channel monitor */
        executionMonitor.addNewExecutionToMonitor(execution.toLight());
        webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_EXECUTION_MONITOR,executionMonitor.toJson(true).toMap(),true,true);
        /* If execution is part of a campaing, notify channel */

        if (execution.getTagObj() != null && execution.getTagObj().getCampaign() != null) {
            webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_CAMPAIGN_DELTA_ID(execution.getTagObj().getCampaign()), executionLight);
        }

    }

    /**
     * Execution is definitely settled (no more retries coming).
     * <p>
     * No dedicated channel yet : {@link #notifyExecutionDone} and the monitor refresh already push
     * on every attempt, including ones that will be retried. Add a channel here if a front-end
     * consumer needs to react specifically to the truly final attempt (e.g. campaign completion %).
     */
    public void notifyExecutionEndLastRetry(TestCaseExecution execution) {
    }


    /**
     * Queue counters / running executions snapshot changed.
     */
    public void notifyQueueListRefresh(QueueStatus queueStatus) {
        webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_EXECUTION_LIST_QUEUED, queueStatus.toJson(true).toMap());
    }

    /**
     * A campaign (tag with a non-null campaign) just started.
     */
    public void notifyCampaignStart(Tag tag) {
        webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_CAMPAIGN_START, campaignExecutionMapper.toLightDto(tag));
        webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_CAMPAIGN_START_ID(tag.getCampaign()), campaignExecutionMapper.toLightDto(tag));
    }

    /**
     * A campaign is done (queue empty, CI score computed).
     */
    public void notifyCampaignEnd(Tag tag) {
        webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_CAMPAIGN_DONE, campaignExecutionMapper.toLightDto(tag));
        webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_CAMPAIGN_DONE_ID(tag.getCampaign()), campaignExecutionMapper.toLightDto(tag));
        if ("OK".equalsIgnoreCase(tag.getCiResult())) {
            webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_CAMPAIGN_SUCCESS, campaignExecutionMapper.toLightDto(tag));
        }
    }

    /**
     * A campaign is done with a KO CI result (fired in addition to {@link #notifyCampaignEnd}).
     */
    public void notifyCampaignEndCIKO(Tag tag) {
        webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_CAMPAIGN_FAIL, campaignExecutionMapper.toLightDto(tag));
    }

    /**
     * Campaign counters changed (e.g. live statistics while executions are still running).
     * Reuses the same channel {@link #notifyExecutionStart}/{@link #notifyExecutionDone} already
     * push the tag to when an execution starts/ends as part of a campaign.
     */
    public void notifyCampaignUpdate(Tag tag) {
        webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_CAMPAIGN_UPDATE_ID(tag.getCampaign()), campaignExecutionMapper.toLightDto(tag));
    }

    /**
     * A testcase was created.
     */
    public void notifyTestCaseCreate(TestCase testCase) {
        webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_TESTCASE_CREATE, testCaseLightPayload(testCase));
    }

    /**
     * A testcase was updated.
     */
    public void notifyTestCaseUpdate(TestCase testCase) {
        webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_TESTCASE_UPDATE, testCaseLightPayload(testCase));
    }

    /**
     * A testcase was deleted.
     */
    public void notifyTestCaseDelete(TestCase testCase) {
        webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_TESTCASE_DELETE, testCaseLightPayload(testCase));
    }

    /**
     * Testcase identity fields only : {@link TestCase#toJson()} carries steps/histos/properties,
     * too heavy for a broadcast that just tells listeners "refresh this testcase".
     */
    private Map<String, Object> testCaseLightPayload(TestCase testCase) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("test", testCase.getTest());
        payload.put("testcase", testCase.getTestcase());
        payload.put("application", testCase.getApplication());
        payload.put("description", testCase.getDescription());
        return payload;
    }
}