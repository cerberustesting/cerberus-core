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

public final class WebSocketStatic {

    /**  SUBJECTS  **/
    public static final String SUBJECT_SUBSCRIBE = "subscribe";
    public static final String SUBJECT_UNSUBSCRIBE = "unsubscribe";
    public static final String SUBJECT_MESSAGE = "message";

    /**  CHANNELS  **/
    public static final String CHANNEL_AI_CHAT = "module.chat";
    public static final String CHANNEL_NOTIFICATION= "module.notification";
    public static final String CHANNEL_PAGE_TESTCASEEXECUTION = "page.testcaseexecution";
    public static final String CHANNEL_PAGE_EXECUTIONMONITOR = "page.executionmonitor";
    public static final String CHANNEL_PAGE_HOMEPAGE = "page.homepage";
    /**  CHANNELS LEGACY **/
    // OLD CHANNELS TO REMOVE TO USE CHAT AND MCP
    public static final String CHANNEL_TESTCASE_CREATE = "modal.testcasecreate";
    public static final String CHANNEL_TESTCASE_PROPOSAL = "modal.testcaseproposal";
    public static final String CHANNEL_EXECUTION_DEBUG = "modal.executiondebug";
    public static final String CHANNEL_AO_GENERATE = "modal.aogenerate";
    public static final String CHANNEL_AO_GENERATECONTINUE = "modal.aogeneratecontinue";

    /**  TYPE CHAT  **/
    public static final String TYPE_CHAT_DELTA = "chat.delta";
    public static final String TYPE_CHAT_DONE = "chat.done";
    public static final String TYPE_CHAT_TITLE = "chat.title";
    public static final String TYPE_CHAT_ERROR = "chat.error";
    /**  TYPE MCP USAGE  **/
    public static final String TYPE_TOOL_START = "tool.start";
    public static final String TYPE_TOOL_RESULT = "tool.result";
    public static final String TYPE_TOOL_END = "tool.done";
    public static final String TYPE_TOOL_ERROR = "tool.error";
    /**  TYPE EXECUTION AND QUEUE  **/
    public static final String TYPE_QUEUE_CHANGE = "queue.change";
    public static final String TYPE_EXECUTION_START = "execution.start";
    public static final String TYPE_EXECUTION_UPDATE = "execution.update";
    public static String TYPE_EXECUTION_UPDATE_ID(long executionId){return TYPE_EXECUTION_UPDATE + "." + executionId;}
    public static final String TYPE_EXECUTION_END = "execution.end";
    public static final String TYPE_EXECUTION_DECLAREFALSENEGATIVE = "execution.declarefalsenegative";
    public static final String TYPE_EXECUTION_UNDECLAREFALSENEGATIVE = "execution.undeclarefalsenegative";
    /**  TYPE CAMPAIGN  **/
    public static final String TYPE_CAMPAIGN_START = "campaign.start";
    public static final String TYPE_CAMPAIGN_UPDATE = "campaign.update";
    public static final String TYPE_CAMPAIGN_END = "campaign.end";
    public static final String TYPE_CAMPAIGN_FAIL = "campaign.fail";
    public static final String TYPE_CAMPAIGN_SUCCESS = "campaign.success";
    /**  TYPE OBJECT CREATION  **/
    public static final String TYPE_OBJECTCREATION_APPLICATION = "objectcreation.application";
    public static final String TYPE_OBJECTCREATION_INVARIANT = "objectcreation.invariant";
    public static final String TYPE_OBJECTCREATION_TESTCASE = "objectcreation.testcase";
    public static final String TYPE_OBJECTCREATION_TESTCASESTEP = "objectcreation.testcasestep";
    /**  TYPE OBJECT PROPOSAL  **/
    public static final String TYPE_AO_PROPOSALS = "ao.proposals";
    public static final String TYPE_TESTCASE_PROPOSALS = "testcase.proposals";

    public static final String TYPE_NOTIFICATION_EXECUTING_INIT = "notification.initexecuting";
    public static final String TYPE_NOTIFICATION_QUEUED_INIT = "notification.initqueued";
    public static final String TYPE_NOTIFICATION_LASTEXECUTION_INIT = "notification.initlastexecution";

    private WebSocketStatic() {
    }
}
