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
package org.cerberus.service.notification.impl;

import org.cerberus.service.email.entity.Email;
import org.cerberus.crud.entity.Campaign;
import org.cerberus.crud.entity.User;
import org.cerberus.crud.service.ICampaignService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.service.ciresult.ICIService;
import org.cerberus.service.email.IEmailGenerationService;
import org.cerberus.service.email.IEmailService;
import org.cerberus.service.notification.INotificationService;
import org.cerberus.service.slack.ISlackGenerationService;
import org.cerberus.service.slack.ISlackService;
import org.cerberus.util.StringUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class NotificationService implements INotificationService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(NotificationService.class);

    @Autowired
    private IEmailGenerationService emailGenerationService;
    @Autowired
    private IEmailService emailService;
    @Autowired
    private ICampaignService campaignService;
    @Autowired
    private ISlackService slackService;
    @Autowired
    private ISlackGenerationService slackGenerationService;
    @Autowired
    private ICIService ciService;
    @Autowired
    private IParameterService parameterService;

    @Override
    public MessageEvent generateAndSendAccountCreationEmail(User user) {

        Email email = null;
        try {
            email = emailGenerationService.generateAccountCreationEmail(user);
        } catch (Exception ex) {
            LOG.warn("Exception generating email for account creation.", ex);
            return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
        }

        try {
            emailService.sendHtmlMail(email);
        } catch (Exception ex) {
            LOG.warn("Exception sending email for account creation.", ex);
            return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
        }

        return new MessageEvent(MessageEventEnum.GENERIC_OK);
    }

    @Override
    public MessageEvent generateAndSendForgotPasswordEmail(User user) {

        Email email = null;
        try {
            email = emailGenerationService.generateForgotPasswordEmail(user);
        } catch (Exception ex) {
            LOG.warn("Exception generating email for forgot password.", ex);
            return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
        }

        try {
            emailService.sendHtmlMail(email);
        } catch (Exception ex) {
            LOG.warn("Exception sending email for forgot password.", ex);
            return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
        }

        return new MessageEvent(MessageEventEnum.GENERIC_OK);
    }

    @Override
    public MessageEvent generateAndSendRevisionChangeEmail(String system, String country, String env, String build, String revision) {

        Email email = null;
        try {
            email = emailGenerationService.generateRevisionChangeEmail(system, country, env, build, revision);
        } catch (Exception ex) {
            LOG.warn("Exception generating email for revision change.", ex);
            return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
        }

        try {
            emailService.sendHtmlMail(email);
        } catch (Exception ex) {
            LOG.warn("Exception sending email for revision change.", ex);
            return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
        }

        return new MessageEvent(MessageEventEnum.GENERIC_OK);
    }

    @Override
    public MessageEvent generateAndSendDisableEnvEmail(String system, String country, String env) {

        Email email = null;
        try {
            email = emailGenerationService.generateDisableEnvEmail(system, country, env);
        } catch (Exception ex) {
            LOG.warn("Exception generating email for disabling environment.", ex);
            return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
        }

        try {
            emailService.sendHtmlMail(email);
        } catch (Exception ex) {
            LOG.warn("Exception sending email for disabling environment.", ex);
            return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
        }

        return new MessageEvent(MessageEventEnum.GENERIC_OK);
    }

    @Override
    public MessageEvent generateAndSendNewChainEmail(String system, String country, String env, String chain) {

        Email email = null;
        try {
            email = emailGenerationService.generateNewChainEmail(system, country, env, chain);
        } catch (Exception ex) {
            LOG.warn("Exception generating email for new chain.", ex);
            return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
        }

        try {
            emailService.sendHtmlMail(email);
        } catch (Exception ex) {
            LOG.warn("Exception sending email for new chain.", ex);
            return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
        }

        return new MessageEvent(MessageEventEnum.GENERIC_OK);
    }

    @Override
    public MessageEvent generateAndSendNotifyStartTagExecution(String tag, String campaign) {

        try {

            Campaign myCampaign = campaignService.convert(campaignService.readByKey(campaign));
            String webHook = myCampaign.getSlackWebhook();
            String distribList = myCampaign.getDistribList();

            if (!StringUtil.isNullOrEmpty(distribList) && myCampaign.getNotifyStartTagExecution().equalsIgnoreCase("Y")) {

                LOG.debug("Generating and Sending an EMail Notification to : " + distribList);
                Email email = null;
                try {
                    email = emailGenerationService.generateNotifyStartTagExecution(tag, campaign, distribList);
                } catch (Exception ex) {
                    LOG.warn("Exception generating email for Start Tag Execution.", ex);
                    return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
                }
                try {
                    emailService.sendHtmlMail(email);
                } catch (Exception ex) {
                    LOG.warn("Exception sending email for Start Tag Execution.", ex);
                    return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
                }

            }

            if (!StringUtil.isNullOrEmpty(webHook) && myCampaign.getSlackNotifyStartTagExecution().equalsIgnoreCase("Y")) {

                try {

                    LOG.debug("Generating and Sending a Slack Notification to : '" + webHook + "'");

                    JSONObject slackMessage = slackGenerationService.generateNotifyStartTagExecution(tag, myCampaign.getSlackChannel());

                    slackService.sendSlackMessage(slackMessage, webHook);

                } catch (Exception ex) {
                    LOG.warn("Exception sending slack notification for Start Tag Execution.", ex);
                    return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
                }

            }

        } catch (Exception ex) {
            LOG.warn("Exception generating notification for Start Tag Execution.", ex);
            return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
        }

        return new MessageEvent(MessageEventEnum.GENERIC_OK);
    }

    @Override
    public MessageEvent generateAndSendNotifyEndTagExecution(String tag, String campaign) {

        try {
            Campaign myCampaign = campaignService.convert(campaignService.readByKey(campaign));
            String webHook = myCampaign.getSlackWebhook();
            String distribList = myCampaign.getDistribList();

            if (((!StringUtil.isNullOrEmpty(distribList))
                    && (myCampaign.getNotifyEndTagExecution().equalsIgnoreCase(Campaign.NOTIFYSTARTTAGEXECUTION_Y) || myCampaign.getNotifyEndTagExecution().equalsIgnoreCase(Campaign.NOTIFYSTARTTAGEXECUTION_CIKO)))
                    || ((!StringUtil.isNullOrEmpty(webHook))
                    && (myCampaign.getSlackNotifyEndTagExecution().equalsIgnoreCase(Campaign.NOTIFYSTARTTAGEXECUTION_Y) || myCampaign.getSlackNotifyEndTagExecution().equalsIgnoreCase(Campaign.NOTIFYSTARTTAGEXECUTION_CIKO)))) {

                JSONObject jsonCIStatus = new JSONObject();
                jsonCIStatus = ciService.getCIResult(tag, campaign);

                // EMail Notification.
                if ((!StringUtil.isNullOrEmpty(distribList))
                        && (myCampaign.getNotifyEndTagExecution().equalsIgnoreCase(Campaign.NOTIFYSTARTTAGEXECUTION_Y)
                        || (myCampaign.getNotifyEndTagExecution().equalsIgnoreCase(Campaign.NOTIFYSTARTTAGEXECUTION_CIKO) && jsonCIStatus.getString("result").equalsIgnoreCase("KO")))) {
                    // Flag is Y or CIKO with KO result.

                    Email email = null;

                    try {
                        email = emailGenerationService.generateNotifyEndTagExecution(tag, campaign, distribList);
                    } catch (Exception ex) {
                        LOG.error("Exception generating email for End Tag Execution.", ex);
                        return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
                    }

                    try {
                        emailService.sendHtmlMail(email);
                    } catch (Exception ex) {
                        LOG.error("Exception sending email for End Tag Execution.", ex);
                        return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
                    }
                }

                // Slack Notification.
                if ((!StringUtil.isNullOrEmpty(webHook))
                        && (myCampaign.getSlackNotifyEndTagExecution().equalsIgnoreCase(Campaign.NOTIFYSTARTTAGEXECUTION_Y)
                        || (myCampaign.getSlackNotifyEndTagExecution().equalsIgnoreCase(Campaign.NOTIFYSTARTTAGEXECUTION_CIKO) && jsonCIStatus.getString("result").equalsIgnoreCase("KO")))) {

                    try {

                        LOG.debug("Generating and Sending a Slack Notification to : " + webHook);

                        JSONObject slackMessage = slackGenerationService.generateNotifyEndTagExecution(tag, myCampaign.getSlackChannel());

                        slackService.sendSlackMessage(slackMessage, webHook);

                    } catch (Exception ex) {
                        LOG.error("Exception sending slack notification for Start Tag Execution to URL : '" + webHook + "'", ex);
                        return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
                    }

                }

            }
        } catch (Exception ex) {
            LOG.warn("Exception generating email for End Tag Execution.", ex);
            return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
        }
        return new MessageEvent(MessageEventEnum.GENERIC_OK);
    }

}
