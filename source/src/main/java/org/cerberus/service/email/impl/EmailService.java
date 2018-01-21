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
package org.cerberus.service.email.impl;

import org.cerberus.service.email.entity.Email;
import com.mysql.jdbc.StringUtils;
import org.apache.commons.mail.HtmlEmail;
import org.cerberus.crud.entity.Campaign;
import org.cerberus.crud.entity.User;
import org.cerberus.crud.service.ICampaignService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.service.ciresult.ICIService;
import org.cerberus.service.email.IEmailGenerationService;
import org.cerberus.service.email.IEmailService;
import org.cerberus.util.StringUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class EmailService implements IEmailService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(EmailService.class);

    @Autowired
    private IEmailGenerationService emailGenerationService;
    @Autowired
    private ICampaignService campaignService;
    @Autowired
    private ICIService ciService;

    @Override
    public void sendHtmlMail(Email cerberusEmail) throws Exception {

        HtmlEmail email = new HtmlEmail();
        email.setSmtpPort(cerberusEmail.getSmtpPort());
        email.setHostName(cerberusEmail.getHost());
        email.setFrom(cerberusEmail.getFrom());
        email.setSubject(cerberusEmail.getSubject());
        email.setHtmlMsg(cerberusEmail.getBody());
        email.setTLS(cerberusEmail.isSetTls());
        email.setDebug(true);

        if (!StringUtils.isNullOrEmpty(cerberusEmail.getUserName()) || !StringUtils.isNullOrEmpty(cerberusEmail.getPassword())) {
            email.setAuthentication(cerberusEmail.getUserName(), cerberusEmail.getPassword());
        }

        String[] destinataire = cerberusEmail.getTo().split(";");
        for (int i = 0; i < destinataire.length; i++) {
            String name;
            String emailaddress;
            if (destinataire[i].contains("<")) {
                String[] destinatairedata = destinataire[i].split("<");
                name = destinatairedata[0].trim();
                emailaddress = destinatairedata[1].replace(">", "").trim();
            } else {
                name = "";
                emailaddress = destinataire[i];
            }
            email.addTo(emailaddress, name);
        }

        if (!StringUtil.isNullOrEmpty(cerberusEmail.getCc())) {
            String[] copy = cerberusEmail.getCc().split(";");

            for (int i = 0; i < copy.length; i++) {
                String namecc;
                String emailaddresscc;
                if (copy[i].contains("<")) {
                    String[] copydata = copy[i].split("<");
                    namecc = copydata[0].trim();
                    emailaddresscc = copydata[1].replace(">", "").trim();
                } else {
                    namecc = "";
                    emailaddresscc = copy[i];
                }
                email.addCc(emailaddresscc, namecc);
            }
        }

        email.send();

    }

    @Override
    public MessageEvent generateAndSendAccountCreationEmail(User user) {

        Email email = null;
        try {
            email = emailGenerationService.generateAccountCreationEmail(user);
        } catch (Exception ex) {
            LOG.warn("Exception generating email for account creation :" + ex);
            return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
        }

        try {
            this.sendHtmlMail(email);
        } catch (Exception ex) {
            LOG.warn("Exception sending email for account creation :" + ex);
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
            LOG.warn("Exception generating email for forgot password :" + ex);
            return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
        }

        try {
            this.sendHtmlMail(email);
        } catch (Exception ex) {
            LOG.warn("Exception sending email for forgot password :" + ex);
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
            LOG.warn("Exception generating email for revision change :" + ex);
            return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
        }

        try {
            this.sendHtmlMail(email);
        } catch (Exception ex) {
            LOG.warn("Exception sending email for revision change :" + ex);
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
            LOG.warn("Exception generating email for disabling environment :" + ex);
            return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
        }

        try {
            this.sendHtmlMail(email);
        } catch (Exception ex) {
            LOG.warn("Exception sending email for disabling environment :" + ex);
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
            LOG.warn("Exception generating email for new chain :" + ex);
            return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
        }

        try {
            this.sendHtmlMail(email);
        } catch (Exception ex) {
            LOG.warn("Exception sending email for new chain :" + ex);
            return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
        }

        return new MessageEvent(MessageEventEnum.GENERIC_OK);
    }

    @Override
    public MessageEvent generateAndSendNotifyStartTagExecution(String tag, String campaign) {

        try {
            Campaign myCampaign = campaignService.convert(campaignService.readByKey(campaign));
            if (!StringUtil.isNullOrEmpty(myCampaign.getDistribList()) && myCampaign.getNotifyStartTagExecution().equalsIgnoreCase("Y")) {

                Email email = null;

                email = emailGenerationService.generateNotifyStartTagExecution(tag, campaign, myCampaign.getDistribList());

                try {

                } catch (Exception ex) {
                    LOG.warn("Exception generating email for Start Tag Execution :" + ex);
                    return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
                }

                try {
                    this.sendHtmlMail(email);
                } catch (Exception ex) {
                    LOG.warn("Exception sending email for Start Tag Execution :" + ex);
                    return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
                }
            }
        } catch (Exception ex) {
            LOG.warn("Exception generating email for Start Tag Execution :" + ex);
            return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
        }

        return new MessageEvent(MessageEventEnum.GENERIC_OK);
    }

    @Override
    public MessageEvent generateAndSendNotifyEndTagExecution(String tag, String campaign) {

        try {
            Campaign myCampaign = campaignService.convert(campaignService.readByKey(campaign));
            if (!StringUtil.isNullOrEmpty(myCampaign.getDistribList())) {
                // Distribution List is not empty

                if (myCampaign.getNotifyEndTagExecution().equalsIgnoreCase(Campaign.NOTIFYSTARTTAGEXECUTION_Y)
                        || myCampaign.getNotifyEndTagExecution().equalsIgnoreCase(Campaign.NOTIFYSTARTTAGEXECUTION_CIKO)) {
                    // Flag is activated.

                    JSONObject jsonResponse = new JSONObject();
                    jsonResponse = ciService.getCIResult(tag);

                    if (myCampaign.getNotifyEndTagExecution().equalsIgnoreCase(Campaign.NOTIFYSTARTTAGEXECUTION_Y)
                            || (myCampaign.getNotifyEndTagExecution().equalsIgnoreCase(Campaign.NOTIFYSTARTTAGEXECUTION_CIKO) && jsonResponse.getString("result").equalsIgnoreCase("KO"))) {
                        // Flag is Y or CIKO with KO result.

                        Email email = null;
                        try {
                            email = emailGenerationService.generateNotifyEndTagExecution(tag, campaign, myCampaign.getDistribList(), jsonResponse.getString("result"), (double) jsonResponse.get("CI_finalResult"));
                        } catch (Exception ex) {
                            LOG.warn("Exception generating email for End Tag Execution :" + ex);
                            return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
                        }

                        try {
                            this.sendHtmlMail(email);
                        } catch (Exception ex) {
                            LOG.warn("Exception sending email for End Tag Execution :" + ex);
                            return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
                        }
                    }
                }

            }
        } catch (Exception ex) {
            LOG.warn("Exception generating email for End Tag Execution :" + ex);
            return new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
        }
        return new MessageEvent(MessageEventEnum.GENERIC_OK);
    }

}
