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
            MODULE_CHAT: 'module.chat',
            MODULE_NOTIFICATION: 'module.notification',
            PAGE_HOMEPAGE: 'page.homepage',
            PAGE_TESTCASEEXECUTION: 'page.testcaseexecution',
            PAGE_EXECUTIONMONITOR: 'page.executionmonitor',
            MODAL_TESTCASECREATE: 'modal.testcasecreate',
            MODAL_TESTCASEPROPOSAL: 'modal.testcaseproposal',
            MODAL_EXECUTIONDEBUG: 'modal.executiondebug',
            MODAL_AOGENERATE: 'modal.aogenerate',
            MODAL_AOGENERATECONTINUE:' modal.aogeneratecontinue'
        }),

        Type: Object.freeze({
            CHAT_DELTA: 'chat.delta',
            CHAT_DONE: 'chat.done',
            CHAT_TITLE: 'chat.title',
            CHAT_ERROR: 'chat.error',
            TOOL_START: 'tool.start',
            TOOL_RESULT: 'tool.result',
            TOOL_END: 'tool.done',
            TOOL_ERROR: 'tool.error',
            QUEUE_UPDATE: 'queue.change',
            EXECUTION_UPDATE: 'execution.update',
            NOTIFICATION: 'notification',
            CHAT_MESSAGE: 'chat.message',
            EXECUTION_START: 'execution.start',
            EXECUTION_UPDATED: 'execution.update',
            EXECUTION_UPDATE_ID: function (executionId) {
                return 'execution.update' + executionId;
            },
            EXECUTION_END: 'execution.end',
            EXECUTION_DECLAREFALSENEGATIVE: 'execution.declarefalsenegative',
            EXECUTION_UNDECLAREFALSENEGATIVE: 'execution.undeclarefalsenegative',
            CAMPAIGN_START: 'campaign.start',
            CAMPAIGN_UPDATE: 'campaign.update',
            CAMPAIGN_END: 'campaign.end',
            CAMPAIGN_FAIL: 'campaign.fail',
            CAMPAIGN_SUCCESS: 'campaign.success',
            OBJECTCREATION_APPLICATION: 'objectcreation.application',
            OBJECTCREATION_INVARIANT: 'objectcreation.invariant',
            OBJECTCREATION_TESTCASE: 'objectcreation.testcase',
            OBJECTCREATION_TESTCASESTEP: 'objectcreation.testcasestep',
            AO_PROPOSALS: 'ao.proposals',
            TESTCASE_PROPOSALS: 'testcase.proposals',
            INIT_LAST_EXECUTION: "notification.initlastexecution",
            INIT_EXECUTING: "notification.initexecuting",
            INIT_QUEUED: "notification.initqueued"
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