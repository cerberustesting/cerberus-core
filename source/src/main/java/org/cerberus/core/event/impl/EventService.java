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
package org.cerberus.core.event.impl;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.cerberus.core.crud.entity.EventHook;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.IEventHookService;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.event.IEventService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.notifications.email.IEmailGenerationService;
import org.cerberus.core.service.notifications.email.IEmailService;
import org.cerberus.core.service.notifications.email.entity.Email;
import org.cerberus.core.service.notifications.googlechat.IChatGenerationService;
import org.cerberus.core.service.notifications.googlechat.IChatService;
import org.cerberus.core.service.notifications.slack.ISlackGenerationService;
import org.cerberus.core.service.notifications.slack.ISlackService;
import org.cerberus.core.service.notifications.teams.ITeamsGenerationService;
import org.cerberus.core.service.notifications.teams.ITeamsService;
import org.cerberus.core.service.notifications.webcall.IWebcallGenerationService;
import org.cerberus.core.service.notifications.webcall.IWebcallService;
import org.cerberus.core.util.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 *
 * @author vertigo17
 */
@Service
public class EventService implements IEventService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(EventService.class);

    @Autowired
    private ITestCaseExecutionService testCaseExecutionService;
//    @Autowired
//    private IInvariantService invariantService;
    @Autowired
    private IEventHookService eventHookService;
    @Autowired
    private IEmailGenerationService emailGenerationService;
    @Autowired
    private IEmailService emailService;
    @Autowired
    private ISlackGenerationService slackGenerationService;
    @Autowired
    private ISlackService slackService;
    @Autowired
    private ITeamsGenerationService teamsGenerationService;
    @Autowired
    private ITeamsService teamsService;
    @Autowired
    private IWebcallGenerationService webCallGenerationService;
    @Autowired
    private IWebcallService webcallService;
    @Autowired
    private IChatGenerationService chatGenerationService;
    @Autowired
    private IChatService chatService;

    /**
     * This Method gets all Hooks attached to the event triggered and filter
     * them if active and apply to the correct element (tag, testcase,
     * execution)
     *
     * @param eventReference
     * @param object1
     * @return
     */
    @Override
    @Async
    public MessageEvent triggerEvent(String eventReference, Object object1, Object object2, Object object3, Object object4) {

        LOG.debug("Event '" + eventReference + "' triggered.");

        try {

            JSONObject ceberusEventMessage = getCerberusEventMessage(eventReference);
            List<TestCaseExecution> executionList = new ArrayList<>();
            List<Invariant> prioritiesList = new ArrayList<>();
            List<Invariant> countriesList = new ArrayList<>();
            List<Invariant> environmentsList = new ArrayList<>();

            List<String> evtList = new ArrayList<>(Arrays.asList(eventReference));
            List<EventHook> eventHooks = eventHookService.convert(eventHookService.readByEventReference(evtList));

            LOG.debug("EventHooks : " + eventHooks.size());

            for (EventHook eventHook : eventHooks) {

                LOG.debug("EventHook '" + eventHook.toString() + "' analysing.");

                if (eventHook.isActive()) {

                    switch (eventReference) {

                        case EventHook.EVENTREFERENCE_EXECUTION_START:
                            TestCaseExecution exe1 = (TestCaseExecution) object1;
                            if (eval_NoFilter(eventHook.getObjectKey1(), eventHook.getObjectKey2())
                                    || eval_TestFolder_Filter(eventHook.getObjectKey1(), eventHook.getObjectKey2(), exe1.getTest())
                                    || eval_Testcase_Filter(eventHook.getObjectKey1(), eventHook.getObjectKey2(), exe1.getTest(), exe1.getTestCase())) {
                                processEvent_EXECUTION_START(eventHook, exe1, ceberusEventMessage);
                            }
                            break;
                        case EventHook.EVENTREFERENCE_EXECUTION_END:
                        case EventHook.EVENTREFERENCE_EXECUTION_END_LASTRETRY:
                            TestCaseExecution exe2 = (TestCaseExecution) object1;
                            if (eval_NoFilter(eventHook.getObjectKey1(), eventHook.getObjectKey2())
                                    || eval_TestFolder_Filter(eventHook.getObjectKey1(), eventHook.getObjectKey2(), exe2.getTest())
                                    || eval_Testcase_Filter(eventHook.getObjectKey1(), eventHook.getObjectKey2(), exe2.getTest(), exe2.getTestCase())) {
                                processEvent_EXECUTION_END(eventHook, exe2, ceberusEventMessage);
                            }
                            break;

                        case EventHook.EVENTREFERENCE_CAMPAIGN_START:
                            Tag tag1 = (Tag) object1;
                            if (eval_NoFilter(eventHook.getObjectKey1(), eventHook.getObjectKey2())
                                    || eval_Campaign_Filter(eventHook.getObjectKey1(), eventHook.getObjectKey2(), tag1.getCampaign())) {
                                processEvent_CAMPAIGN_START(eventHook, tag1, ceberusEventMessage);
                            }
                            break;
                        case EventHook.EVENTREFERENCE_CAMPAIGN_END:
                        case EventHook.EVENTREFERENCE_CAMPAIGN_END_CIKO:
                            Tag tag2 = (Tag) object1;
                            if (eval_NoFilter(eventHook.getObjectKey1(), eventHook.getObjectKey2())
                                    || eval_Campaign_Filter(eventHook.getObjectKey1(), eventHook.getObjectKey2(), tag2.getCampaign())) {
                                // We load the execution list here so that in case of multiple hook, this is done only once.
//                                if (executionList.size() < 1) {
//                                    executionList = testCaseExecutionService.readLastExecutionAndExecutionInQueueByTag(tag2.getTag());
//                                    tag2.setExecutionsNew(executionList);
//                                }
                                // We load the invariant lists that will be used when converting execution to JSON. This is also done only once per event triggered.
//                                prioritiesList = invariantService.readByIdName("PRIORITY");
//                                countriesList = invariantService.readByIdName("COUNTRY");
//                                environmentsList = invariantService.readByIdName("ENVIRONMENT");
                                processEvent_CAMPAIGN_END(eventHook, tag2, ceberusEventMessage, prioritiesList, countriesList, environmentsList);
                            }
                            break;

                        case EventHook.EVENTREFERENCE_TESTCASE_CREATE:
                        case EventHook.EVENTREFERENCE_TESTCASE_DELETE:
                        case EventHook.EVENTREFERENCE_TESTCASE_UPDATE:
                            TestCase testCase1 = (TestCase) object1;
                            String originalTest = (String) object2;
                            String originalTestcase = (String) object3;
                            if (eval_NoFilter(eventHook.getObjectKey1(), eventHook.getObjectKey2())
                                    || eval_TestFolder_Filter(eventHook.getObjectKey1(), eventHook.getObjectKey2(), testCase1.getTest())
                                    || eval_Testcase_Filter(eventHook.getObjectKey1(), eventHook.getObjectKey2(), testCase1.getTest(), testCase1.getTestcase())) {
                                processEvent_TESTCASE(eventHook, testCase1, originalTest, originalTestcase, ceberusEventMessage);
                            }
                            break;

                    }
                }
            }

        } catch (CerberusException | JSONException ex) {
            LOG.error(ex, ex);
        }

        return new MessageEvent(MessageEventEnum.GENERIC_OK);
    }

    private boolean eval_NoFilter(String obj1, String obj2) {
        if (StringUtil.isEmptyOrNull(obj2) && StringUtil.isEmptyOrNull(obj1)) {
            return true;
        }
        return false;
    }

    private boolean eval_TestFolder_Filter(String obj1, String obj2, String testFolder) {
        if (!StringUtil.isEmptyOrNull(obj2)) {
            return false;
        }
        if (!StringUtil.isEmptyOrNull(obj1) && obj1.equals(testFolder)) {
            return true;
        }
        return false;
    }

    private boolean eval_Testcase_Filter(String obj1, String obj2, String testFolder, String testcase) {
        if (StringUtil.isEmptyOrNull(obj1) || StringUtil.isEmptyOrNull(obj2)) {
            return false;
        }
        if (obj1.equals(testFolder) && obj2.equals(testcase)) {
            return true;
        }
        return false;
    }

    private boolean eval_Campaign_Filter(String obj1, String obj2, String campaign) {
        if (!StringUtil.isEmptyOrNull(obj2)) {
            return false;
        }
        if (!StringUtil.isEmptyOrNull(obj1) && obj1.equals(campaign)) {
            return true;
        }
        return false;
    }

    private void processEvent_CAMPAIGN_START(EventHook eventHook, Tag tag, JSONObject ceberusEventMessage) {
        LOG.debug("EventHook Processing '" + eventHook.getEventReference() + "' with connector '" + eventHook.getHookConnector() + "' to '" + eventHook.getHookRecipient() + "'");
        switch (eventHook.getHookConnector()) {

            case EventHook.HOOKCONNECTOR_EMAIL:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending an EMail Notification to : " + eventHook.getHookRecipient());
                    Email email = null;
                    try {
                        email = emailGenerationService.generateNotifyStartTagExecution(tag, eventHook.getHookRecipient());
                        emailService.sendHtmlMail(email);
                    } catch (Exception ex) {
                        LOG.warn("Exception generating email for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            case EventHook.HOOKCONNECTOR_SLACK:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending a Slack Notification to : '" + eventHook.getHookRecipient() + "'");
                    try {
                        JSONObject slackMessage = slackGenerationService.generateNotifyStartTagExecution(tag, eventHook.getHookChannel());
                        slackService.sendSlackMessage(slackMessage, eventHook.getHookRecipient());
                    } catch (Exception ex) {
                        LOG.warn("Exception generating slack notification for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            case EventHook.HOOKCONNECTOR_GENERIC:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending a Generic Notification to : '" + eventHook.getHookRecipient() + "'");
                    try {
                        JSONObject message = webCallGenerationService.generateNotifyStartTagExecution(tag, ceberusEventMessage);
                        webcallService.sendWebcallMessage(message, eventHook.getHookRecipient());
                    } catch (Exception ex) {
                        LOG.warn("Exception Generic notification for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            case EventHook.HOOKCONNECTOR_TEAMS:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending a Teams Notification to : '" + eventHook.getHookRecipient() + "'");
                    try {
                        JSONObject message = teamsGenerationService.generateNotifyStartTagExecution(tag);
                        teamsService.sendTeamsMessage(message, eventHook.getHookRecipient());
                    } catch (Exception ex) {
                        LOG.warn("Exception generating slack notification for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            case EventHook.HOOKCONNECTOR_GOOGLECHAT:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending a Google Chat Notification to : '" + eventHook.getHookRecipient() + "'");
                    try {
                        JSONObject message = chatGenerationService.generateNotifyStartTagExecution(tag);
                        chatService.sendGoogleChatMessage(message, eventHook.getHookRecipient(), tag.getTag());
                    } catch (Exception ex) {
                        LOG.warn("Exception generating Google Chat notification for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            default:
                LOG.warn("Event Hook Connector '" + eventHook.getHookConnector() + "' Not implemented for Event '" + eventHook.getEventReference() + "'");
                break;

        }

    }

    private void processEvent_CAMPAIGN_END(EventHook eventHook, Tag tag, JSONObject ceberusEventMessage, List<Invariant> prioritiesList, List<Invariant> countriesList, List<Invariant> environmentsList) {
        LOG.debug("EventHook Processing '" + eventHook.getEventReference() + "' with connector '" + eventHook.getHookConnector() + "' to '" + eventHook.getHookRecipient() + "'");
        switch (eventHook.getHookConnector()) {

            case EventHook.HOOKCONNECTOR_EMAIL:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending an EMail Notification to : " + eventHook.getHookRecipient());
                    Email email = null;
                    try {
                        email = emailGenerationService.generateNotifyEndTagExecution(tag, eventHook.getHookRecipient());
                        emailService.sendHtmlMail(email);
                    } catch (Exception ex) {
                        LOG.warn("Exception generating email for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            case EventHook.HOOKCONNECTOR_SLACK:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending a Slack Notification to : '" + eventHook.getHookRecipient() + "'");
                    try {
                        JSONObject slackMessage = slackGenerationService.generateNotifyEndTagExecution(tag, eventHook.getHookChannel());
                        slackService.sendSlackMessage(slackMessage, eventHook.getHookRecipient());
                    } catch (Exception ex) {
                        LOG.warn("Exception slack notification for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            case EventHook.HOOKCONNECTOR_GENERIC:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending a Generic Notification to : '" + eventHook.getHookRecipient() + "'");
                    try {
                        JSONObject message = webCallGenerationService.generateNotifyEndTagExecution(tag, ceberusEventMessage, prioritiesList, countriesList, environmentsList);
                        webcallService.sendWebcallMessage(message, eventHook.getHookRecipient());
                    } catch (Exception ex) {
                        LOG.warn("Exception Generic notification for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            case EventHook.HOOKCONNECTOR_TEAMS:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending a Teams Notification to : '" + eventHook.getHookRecipient() + "'");
                    try {
                        JSONObject message = teamsGenerationService.generateNotifyEndTagExecution(tag);
                        teamsService.sendTeamsMessage(message, eventHook.getHookRecipient());
                    } catch (Exception ex) {
                        LOG.warn("Exception Teams notification for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            case EventHook.HOOKCONNECTOR_GOOGLECHAT:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending a Google Chat Notification to : '" + eventHook.getHookRecipient() + "'");
                    try {
                        JSONObject message = chatGenerationService.generateNotifyEndTagExecutionV2(tag);
                        chatService.sendGoogleChatMessage(message, eventHook.getHookRecipient(), tag.getTag());
                    } catch (Exception ex) {
                        LOG.warn("Exception Google Chat notification for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            default:
                LOG.warn("Event Hook Connector '" + eventHook.getHookConnector() + "' Not implemented for Event '" + eventHook.getEventReference() + "'");
                break;

        }
    }

    private void processEvent_EXECUTION_START(EventHook eventHook, TestCaseExecution exe, JSONObject ceberusEventMessage) {
        LOG.debug("EventHook Processing '" + eventHook.getEventReference() + "' with connector '" + eventHook.getHookConnector() + "' to '" + eventHook.getHookRecipient() + "'");
        switch (eventHook.getHookConnector()) {

            case EventHook.HOOKCONNECTOR_EMAIL:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending an EMail Notification to : " + eventHook.getHookRecipient());
                    Email email = null;
                    try {
                        email = emailGenerationService.generateNotifyStartExecution(exe, eventHook.getHookRecipient());
                        emailService.sendHtmlMail(email);
                    } catch (Exception ex) {
                        LOG.warn("Exception generating email for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            case EventHook.HOOKCONNECTOR_SLACK:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending a Slack Notification to : '" + eventHook.getHookRecipient() + "'");
                    try {
                        JSONObject slackMessage = slackGenerationService.generateNotifyStartExecution(exe, eventHook.getHookChannel());
                        slackService.sendSlackMessage(slackMessage, eventHook.getHookRecipient());
                    } catch (Exception ex) {
                        LOG.warn("Exception slack notification for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            case EventHook.HOOKCONNECTOR_GENERIC:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending a Generic Notification to : '" + eventHook.getHookRecipient() + "'");
                    try {
                        JSONObject message = webCallGenerationService.generateNotifyStartExecution(exe, ceberusEventMessage);
                        webcallService.sendWebcallMessage(message, eventHook.getHookRecipient());
                    } catch (Exception ex) {
                        LOG.warn("Exception Generic notification for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            case EventHook.HOOKCONNECTOR_TEAMS:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending a Teams Notification to : '" + eventHook.getHookRecipient() + "'");
                    try {
                        JSONObject message = teamsGenerationService.generateNotifyStartExecution(exe);
                        teamsService.sendTeamsMessage(message, eventHook.getHookRecipient());
                    } catch (Exception ex) {
                        LOG.warn("Exception Teams notification for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            case EventHook.HOOKCONNECTOR_GOOGLECHAT:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending a Google Chat Notification to : '" + eventHook.getHookRecipient() + "'");
                    try {
                        JSONObject message = chatGenerationService.generateNotifyStartExecution(exe);
                        chatService.sendGoogleChatMessage(message, eventHook.getHookRecipient(), String.valueOf(exe.getId()));
                    } catch (Exception ex) {
                        LOG.warn("Exception Google Chat notification for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            default:
                LOG.warn("Event Hook Connector '" + eventHook.getHookConnector() + "' Not implemented for Event '" + eventHook.getEventReference() + "'");
                break;

        }

    }

    private void processEvent_EXECUTION_END(EventHook eventHook, TestCaseExecution exe, JSONObject ceberusEventMessage) {
        LOG.debug("EventHook Processing '" + eventHook.getEventReference() + "' with connector '" + eventHook.getHookConnector() + "' to '" + eventHook.getHookRecipient() + "'");
        switch (eventHook.getHookConnector()) {

            case EventHook.HOOKCONNECTOR_EMAIL:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending an EMail Notification to : " + eventHook.getHookRecipient());
                    Email email = null;
                    try {
                        email = emailGenerationService.generateNotifyEndExecution(exe, eventHook.getHookRecipient());
                        emailService.sendHtmlMail(email);
                    } catch (Exception ex) {
                        LOG.warn("Exception generating email for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            case EventHook.HOOKCONNECTOR_SLACK:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending a Slack Notification to : '" + eventHook.getHookRecipient() + "'");
                    try {
                        JSONObject slackMessage = slackGenerationService.generateNotifyEndExecution(exe, eventHook.getHookChannel());
                        slackService.sendSlackMessage(slackMessage, eventHook.getHookRecipient());
                    } catch (Exception ex) {
                        LOG.warn("Exception slack notification for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            case EventHook.HOOKCONNECTOR_GENERIC:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending a Generic Notification to : '" + eventHook.getHookRecipient() + "'");
                    try {
                        JSONObject message = webCallGenerationService.generateNotifyEndExecution(exe, ceberusEventMessage);
                        webcallService.sendWebcallMessage(message, eventHook.getHookRecipient());
                    } catch (Exception ex) {
                        LOG.warn("Exception Generic notification for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            case EventHook.HOOKCONNECTOR_TEAMS:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending a Teams Notification to : '" + eventHook.getHookRecipient() + "'");
                    try {
                        JSONObject message = teamsGenerationService.generateNotifyEndExecution(exe);
                        teamsService.sendTeamsMessage(message, eventHook.getHookRecipient());
                    } catch (Exception ex) {
                        LOG.warn("Exception Teams notification for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            case EventHook.HOOKCONNECTOR_GOOGLECHAT:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending a Google chat Notification to : '" + eventHook.getHookRecipient() + "'");
                    try {
                        JSONObject message = chatGenerationService.generateNotifyEndExecution(exe);
                        chatService.sendGoogleChatMessage(message, eventHook.getHookRecipient(), String.valueOf(exe.getId()));
                    } catch (Exception ex) {
                        LOG.warn("Exception Google Chat notification for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            default:
                LOG.warn("Event Hook Connector '" + eventHook.getHookConnector() + "' Not implemented for Event '" + eventHook.getEventReference() + "'");
                break;

        }
    }

    private void processEvent_TESTCASE(EventHook eventHook, TestCase testCase, String originalTest, String originalTestcase, JSONObject ceberusEventMessage) {
        LOG.debug("EventHook Processing '" + eventHook.getEventReference() + "' with connector '" + eventHook.getHookConnector() + "' to '" + eventHook.getHookRecipient() + "'");
        switch (eventHook.getHookConnector()) {

            case EventHook.HOOKCONNECTOR_EMAIL:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending an EMail Notification to : " + eventHook.getHookRecipient());
                    Email email = null;
                    try {
                        email = emailGenerationService.generateNotifyTestCaseChange(testCase, eventHook.getHookRecipient(), eventHook.getEventReference());
                        emailService.sendHtmlMail(email);
                    } catch (Exception ex) {
                        LOG.warn("Exception generating email for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            case EventHook.HOOKCONNECTOR_SLACK:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending a Slack Notification to : '" + eventHook.getHookRecipient() + "'");
                    try {
                        JSONObject slackMessage = slackGenerationService.generateNotifyTestCaseChange(testCase, eventHook.getHookChannel(), eventHook.getEventReference());
                        slackService.sendSlackMessage(slackMessage, eventHook.getHookRecipient());
                    } catch (Exception ex) {
                        LOG.warn("Exception slack notification for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            case EventHook.HOOKCONNECTOR_GENERIC:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending a Generic Notification to : '" + eventHook.getHookRecipient() + "'");
                    try {
                        JSONObject message = webCallGenerationService.generateNotifyTestCaseChange(testCase, originalTest, originalTestcase, eventHook.getEventReference(), ceberusEventMessage);
                        webcallService.sendWebcallMessage(message, eventHook.getHookRecipient());
                    } catch (Exception ex) {
                        LOG.warn("Exception Generic notification for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            case EventHook.HOOKCONNECTOR_TEAMS:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending a Teams Notification to : '" + eventHook.getHookRecipient() + "'");
                    try {
                        JSONObject message = teamsGenerationService.generateNotifyTestCaseChange(testCase, eventHook.getEventReference());
                        teamsService.sendTeamsMessage(message, eventHook.getHookRecipient());
                    } catch (Exception ex) {
                        LOG.warn("Exception Teams notification for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            case EventHook.HOOKCONNECTOR_GOOGLECHAT:
                if (!StringUtil.isEmptyOrNull(eventHook.getHookRecipient())) {
                    LOG.debug("Generating and Sending a Google Chat Notification to : '" + eventHook.getHookRecipient() + "'");
                    try {
                        JSONObject message = chatGenerationService.generateNotifyTestCaseChange(testCase, eventHook.getEventReference());
                        chatService.sendGoogleChatMessage(message, eventHook.getHookRecipient(), null);
                    } catch (Exception ex) {
                        LOG.warn("Exception Google Chat notification for '" + eventHook.getEventReference() + "'", ex);
                    }
                }
                break;

            default:
                LOG.warn("Event Hook Connector '" + eventHook.getHookConnector() + "' Not implemented for Event '" + eventHook.getEventReference() + "'");
                break;

        }
    }

    private JSONObject getCerberusEventMessage(String eventReference) throws JSONException {
        JSONObject message = new JSONObject();

        JSONObject header = new JSONObject();
        header.put("eventReference", eventReference);
        header.put("eventDate", new Timestamp(new Date().getTime()));

        message.put("header", header);

        return message;
    }

}
