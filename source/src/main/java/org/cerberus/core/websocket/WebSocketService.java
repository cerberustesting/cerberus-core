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
import org.cerberus.core.api.dto.debugexecution.DebugExecutionStatusDTOV001;
import org.cerberus.core.api.dto.debugexecution.DebugExecutionStatusMapperV001;
import org.cerberus.core.api.dto.testcaseexecution.TestcaseExecutionLightDTOV001;
import org.cerberus.core.api.dto.testcaseexecution.TestcaseExecutionLightMapperV001;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.cerberus.core.mcp.util.MCPExecutionSignal;
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
    private DebugExecutionStatusMapperV001 debugExecutionStatusMapper;
    @Autowired
    private ExecutionMonitor executionMonitor;
    @Autowired
    private ObjectChangeHistory objectChangeHistory;
    /**
     * Lets MCP tools waiting on a tag wake up as soon as a run progresses, instead of polling.
     * Signalling is best-effort and never alters what is pushed to the WebSocket channels.
     */
    @Autowired
    private MCPExecutionSignal mcpExecutionSignal;

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

        // Wake any MCP call waiting on this tag. The signal only invites the waiter to re-read the
        // tag, so firing on every execution rather than only on the last one is what lets a
        // campaign report its progress as it advances.
        mcpExecutionSignal.signal(execution.getTag());
    }

    /**
     * Debug-mode execution just paused (about to run an action or control) or resumed
     * (action/control now actually executing). Pushed on the execution's existing "delta"
     * channel — declared but never used by {@link #notifyExecutionUpdate} — so no new channel
     * or subscription-authorization change is needed on either side.
     *
     * @param waiting        true if pausing (WAITING_FOR_NEXT), false if resuming (RUNNING)
     * @param pendingAction  the (static) action about to run, or owning the pending control, null if not waiting
     * @param pendingControl the (static) control about to run, null unless paused on a control specifically
     * @param pendingFailed  true if the pending action/control already failed once and "retry" is meaningful
     */
    public void notifyDebugPending(TestCaseExecution execution, boolean waiting, TestCaseStepAction pendingAction,
            TestCaseStepActionControl pendingControl, boolean pendingFailed) {
        DebugExecutionStatusDTOV001 payload = debugExecutionStatusMapper.toDTO(execution, waiting, pendingAction, pendingControl, pendingFailed);
        // No throttling : debug pauses are infrequent and each one matters (the user is
        // literally waiting on it to know it's their turn to click "Next").
        webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_EXECUTION_DELTA_ID(execution.getId()), payload, false, true);
    }

    /**
     * Debug-mode execution just ended for good (normal completion, explicit stop, or a fatal
     * action/control failure that short-circuited the remaining steps without ever reaching
     * another pause point). Without this, the debug page's last known state would stay stuck on
     * "running" until its slow reconciliation poll happens to notice the session is gone.
     */
    public void notifyDebugFinished(TestCaseExecution execution) {
        DebugExecutionStatusDTOV001 payload = debugExecutionStatusMapper.toFinishedDTO(execution);
        webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_EXECUTION_DELTA_ID(execution.getId()), payload, false, true);
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

        // Counters and CI verdict are final here, so a waiter woken by this reads the definitive
        // result rather than an intermediate one.
        mcpExecutionSignal.signal(tag.getTag());
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