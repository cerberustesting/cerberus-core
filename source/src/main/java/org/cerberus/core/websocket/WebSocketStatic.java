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

    private WebSocketStatic() {
    }

    /** SUBJECTS **/
    public static final String SUBJECT_SUBSCRIBE = "subscribe";
    public static final String SUBJECT_UNSUBSCRIBE = "unsubscribe";
    public static final String SUBJECT_MESSAGE = "message";

    /** CHAT EVENTS **/
    public static final String CHANNEL_CHAT_DELTA = "chat.delta";
    public static final String CHANNEL_CHAT_SUGGESTIONS = "chat.suggestions";
    public static final String CHANNEL_CHAT_DONE = "chat.done";
    public static final String CHANNEL_CHAT_TITLE = "chat.title";
    public static final String CHANNEL_CHAT_ERROR = "chat.error";

    /** MCP / TOOL EVENTS **/
    public static final String CHANNEL_TOOL_START = "tool.start"; //Notify when MCP tool start
    public static final String CHANNEL_TOOL_RESULT = "tool.result"; //Notify MCP tool result
    public static final String CHANNEL_TOOL_DONE = "tool.done"; //Notify when MCP tool end
    public static final String CHANNEL_TOOL_ERROR = "tool.error"; //Notify when MCP tool has error

    /** EXECUTION EVENTS **/
    public static final String CHANNEL_EXECUTION_START = "execution.start";
    public static final String CHANNEL_EXECUTION_LIGHT_START = "execution.light.start";
    public static final String CHANNEL_EXECUTION_UPDATE = "execution.update";
    public static final String CHANNEL_EXECUTION_LIGHT_UPDATE = "execution.light.update";
    public static final String CHANNEL_EXECUTION_DELTA = "execution.delta";
    public static final String CHANNEL_EXECUTION_DONE = "execution.done";
    public static final String CHANNEL_EXECUTION_LIGHT_DONE = "execution.light.done";
    public static String CHANNEL_EXECUTION_START_ID(long executionId){return CHANNEL_EXECUTION_START + "." + executionId;}
    public static String CHANNEL_EXECUTION_UPDATE_ID(long executionId){return CHANNEL_EXECUTION_UPDATE + "." + executionId;}
    public static String CHANNEL_EXECUTION_DELTA_ID(long executionId){return CHANNEL_EXECUTION_DELTA + "." + executionId;}
    public static String CHANNEL_EXECUTION_DONE_ID(long executionId){return CHANNEL_EXECUTION_DONE + "." + executionId;}
    public static final String CHANNEL_EXECUTION_DECLAREFALSENEGATIVE = "execution.declarefalsenegative";
    public static final String CHANNEL_EXECUTION_UNDECLAREFALSENEGATIVE = "execution.undeclarefalsenegative";


    /** MY EXECUTION EVENTS **/
    public static final String CHANNEL_MYEXECUTION_START = "myexecution.start";
    public static final String CHANNEL_MYEXECUTION_LIGHT_START = "myexecution.light.start";
    public static final String CHANNEL_MYEXECUTION_UPDATE = "myexecution.update";
    public static final String CHANNEL_MYEXECUTION_LIGHT_UPDATE = "myexecution.light.update";
    public static final String CHANNEL_MYEXECUTION_DONE = "myexecution.done";
    public static final String CHANNEL_MYEXECUTION_LIGHT_DONE = "myexecution.light.done";

    /** EXECUTION LIST **/
    public static final String CHANNEL_EXECUTION_LIST_RUNNING = "execution.list.running";
    public static final String CHANNEL_EXECUTION_LIST_QUEUED = "execution.list.queued";
    public static final String CHANNEL_EXECUTION_LIST_LASTEXECUTION = "execution.list.lastexecution";

    /** MY EXECUTION LIST **/
    public static final String CHANNEL_MYEXECUTION_LIST_RUNNING = "myexecution.list.running";
    public static final String CHANNEL_MYEXECUTION_LIST_QUEUED = "myexecution.list.queued";
    public static final String CHANNEL_MYEXECUTION_LIST_LASTEXECUTION = "myexecution.list.lastexecution";

    /** QUEUE EVENTS **/
    public static final String CHANNEL_QUEUE_CHANGE = "queue.change";

    /** CAMPAIGN EVENTS **/
    public static final String CHANNEL_CAMPAIGN_START = "campaign.start";
    public static final String CHANNEL_CAMPAIGN_UPDATE = "campaign.update";
    public static final String CHANNEL_CAMPAIGN_DELTA = "campaign.delta";
    public static final String CHANNEL_CAMPAIGN_DONE = "campaign.done";
    public static final String CHANNEL_CAMPAIGN_FAIL = "campaign.fail";
    public static final String CHANNEL_CAMPAIGN_SUCCESS = "campaign.success";
    public static String CHANNEL_CAMPAIGN_START_ID(String campaign){return CHANNEL_CAMPAIGN_START + "." + campaign;}
    public static String CHANNEL_CAMPAIGN_UPDATE_ID(String campaign){return CHANNEL_CAMPAIGN_UPDATE + "." + campaign;}
    public static String CHANNEL_CAMPAIGN_DELTA_ID(String campaign){return CHANNEL_CAMPAIGN_DELTA + "." + campaign;}
    public static String CHANNEL_CAMPAIGN_DONE_ID(String campaign){return CHANNEL_CAMPAIGN_DONE + "." + campaign;}

    /** CAMPAIGN LIST **/
    public static final String CHANNEL_CAMPAIGN_LIST_RUNNING = "campaign.list.running";
    public static final String CHANNEL_CAMPAIGN_LIST_LASTFINISHED = "campaign.list.lastfinished";


    /** TESTCASE EVENTS **/
    public static final String CHANNEL_TESTCASE_CREATE = "testcase.create";
    public static final String CHANNEL_TESTCASE_UPDATE = "testcase.update";
    public static final String CHANNEL_TESTCASE_DELETE = "testcase.delete";

    /** OBJECT CREATION EVENTS **/
    public static final String CHANNEL_OBJECT_CHANGE = "object.change";
    public static final String CHANNEL_OBJECTCREATION_APPLICATION = "objectcreation.application";
    public static final String CHANNEL_OBJECTCREATION_INVARIANT = "objectcreation.invariant";
    public static final String CHANNEL_OBJECTCREATION_TESTCASE = "objectcreation.testcase";
    public static final String CHANNEL_OBJECTCREATION_TESTCASESTEP = "objectcreation.testcasestep";

    /** OBJECT PROPOSAL EVENTS **/
    public static final String CHANNEL_AO_PROPOSALS = "ao.proposals";
    public static final String CHANNEL_TESTCASE_PROPOSALS = "testcase.proposals";


    /** CLIENT REQUEST CHANNELS **/
    public static final String CHANNEL_CHAT_SEND = "chat.send";
    public static final String CHANNEL_EXECUTION_MONITOR = "execution.monitor";
    public static final String CHANNEL_TESTCASE_PROPOSAL_REQUEST = "testcase.proposal.request";
    public static final String CHANNEL_TESTCASE_CREATE_REQUEST = "testcase.create.request";
    public static final String CHANNEL_AO_GENERATE_REQUEST = "ao.generate.request";
    public static final String CHANNEL_AO_GENERATECONTINUE_REQUEST = "ao.generatecontinue.request";
    public static final String CHANNEL_EXECUTION_DEBUG_REQUEST = "execution.debug.request";
}