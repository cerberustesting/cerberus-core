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
package org.cerberus.core.service.notification.impl;

import org.cerberus.core.service.notifications.email.entity.Email;
import org.cerberus.core.crud.entity.User;
import org.cerberus.core.crud.service.ICampaignService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.service.ciresult.ICIService;
import org.cerberus.core.service.notifications.email.IEmailGenerationService;
import org.cerberus.core.service.notifications.email.IEmailService;
import org.cerberus.core.service.notification.INotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.core.service.notifications.webcall.IWebcallService;
import org.cerberus.core.service.notifications.webcall.IWebcallGenerationService;

/**
 *
 * @author bcivel
 * @author vertigo17
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
    private IWebcallService slackService;
    @Autowired
    private IWebcallGenerationService slackGenerationService;
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

}
