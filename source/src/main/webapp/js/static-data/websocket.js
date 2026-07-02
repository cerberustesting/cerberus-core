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
(function (window) {
    'use strict';

    const CerberusWs = Object.freeze({
        Subject: Object.freeze({
            SUBSCRIBE: 'subscribe',
            UNSUBSCRIBE: 'unsubscribe',
            MESSAGE: 'message'
        }),

        Channel: Object.freeze({
            // Chat events
            CHAT_DELTA: 'chat.delta',
            CHAT_DONE: 'chat.done',
            CHAT_TITLE: 'chat.title',
            CHAT_ERROR: 'chat.error',

            // MCP / tool events
            TOOL_START: 'tool.start',
            TOOL_RESULT: 'tool.result',
            TOOL_DONE: 'tool.done',
            TOOL_ERROR: 'tool.error',

            // Execution events
            EXECUTION_START: 'execution.start',
            EXECUTION_LIGHT_START: 'execution.light.start',
            EXECUTION_UPDATE: 'execution.update',
            EXECUTION_LIGHT_UPDATE: 'execution.light.update',
            EXECUTION_DELTA: 'execution.delta',
            EXECUTION_DONE: 'execution.done',
            EXECUTION_LIGHT_DONE: 'execution.light.done',

            EXECUTION_START_ID: function (executionId) {
                return 'execution.start.' + executionId;
            },
            EXECUTION_UPDATE_ID: function (executionId) {
                return 'execution.update.' + executionId;
            },
            EXECUTION_DELTA_ID: function (executionId) {
                return 'execution.delta.' + executionId;
            },
            EXECUTION_DONE_ID: function (executionId) {
                return 'execution.done.' + executionId;
            },

            EXECUTION_DECLAREFALSENEGATIVE: 'execution.declarefalsenegative',
            EXECUTION_UNDECLAREFALSENEGATIVE: 'execution.undeclarefalsenegative',

            // My execution events
            MYEXECUTION_START: 'myexecution.start',
            MYEXECUTION_LIGHT_START: 'myexecution.light.start',
            MYEXECUTION_UPDATE: 'myexecution.update',
            MYEXECUTION_LIGHT_UPDATE: 'myexecution.light.update',
            MYEXECUTION_DONE: 'myexecution.done',
            MYEXECUTION_LIGHT_DONE: 'myexecution.light.done',

            // Execution lists
            EXECUTION_LIST_RUNNING: 'execution.list.running',
            EXECUTION_LIST_QUEUED: 'execution.list.queued',
            EXECUTION_LIST_UPDATE: 'execution.list.update',
            EXECUTION_LIST_LASTEXECUTION: 'execution.list.lastexecution',

            // My execution lists
            MYEXECUTION_LIST_RUNNING: 'myexecution.list.running',
            MYEXECUTION_LIST_QUEUED: 'myexecution.list.queued',
            MYEXECUTION_LIST_UPDATE: 'myexecution.list.update',
            MYEXECUTION_LIST_LASTEXECUTION: 'myexecution.list.lastexecution',

            // Queue events
            QUEUE_CHANGE: 'queue.change',

            // Campaign events
            CAMPAIGN_START: 'campaign.start',
            CAMPAIGN_UPDATE: 'campaign.update',
            CAMPAIGN_DELTA: 'campaign.delta',
            CAMPAIGN_DONE: 'campaign.done',
            CAMPAIGN_FAIL: 'campaign.fail',
            CAMPAIGN_SUCCESS: 'campaign.success',
            CHANNEL_CAMPAIGN_START_ID: function (campaign) {
                return 'campaign.start.' + campaign;
            },
            CHANNEL_CAMPAIGN_UPDATE_ID: function (campaign) {
                return 'campaign.update.' + campaign;
            },
            CHANNEL_CAMPAIGN_DELTA_ID: function (campaign) {
                return 'campaign.delta.' + campaign;
            },
            CHANNEL_CAMPAIGN_DONE_ID: function (campaign) {
                return 'campaign.done.' + campaign;
            },

            // Testcase events
            TESTCASE_CREATE: 'testcase.create',
            TESTCASE_UPDATE: 'testcase.update',
            TESTCASE_DELETE: 'testcase.delete',

            // Object creation events
            OBJECTCREATION_APPLICATION: 'objectcreation.application',
            OBJECTCREATION_INVARIANT: 'objectcreation.invariant',
            OBJECTCREATION_TESTCASE: 'objectcreation.testcase',
            OBJECTCREATION_TESTCASESTEP: 'objectcreation.testcasestep',

            // Object proposal events
            AO_PROPOSALS: 'ao.proposals',
            TESTCASE_PROPOSALS: 'testcase.proposals',

            // Client request channels
            CHAT_SEND: 'chat.send',
            EXECUTION_MONITOR: 'execution.monitor',
            TESTCASE_PROPOSAL_REQUEST: 'testcase.proposal.request',
            TESTCASE_CREATE_REQUEST: 'testcase.create.request',
            AO_GENERATE_REQUEST: 'ao.generate.request',
            AO_GENERATECONTINUE_REQUEST: 'ao.generatecontinue.request',
            EXECUTION_DEBUG_REQUEST: 'execution.debug.request'
        }),

        Pattern: Object.freeze({
            CHAT: 'chat.*',
            TOOL: 'tool.*',
            EXECUTION: 'execution.*',
            QUEUE: 'queue.*',
            CAMPAIGN: 'campaign.*',
            OBJECTCREATION: 'objectcreation.*'
        }),

        Event: Object.freeze({
            CONNECTED: 'cerberus:ws:connected',
            DISCONNECTED: 'cerberus:ws:disconnected',
            forChannel: function (channel) {
                return 'cerberus:ws:' + channel;
            }
        })
    });

    window.CerberusWs = CerberusWs;

})(window);