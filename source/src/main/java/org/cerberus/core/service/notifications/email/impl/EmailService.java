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
package org.cerberus.core.service.notifications.email.impl;

import java.nio.charset.Charset;
import org.cerberus.core.service.notifications.email.entity.Email;
import org.apache.commons.mail.HtmlEmail;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.service.notifications.email.IEmailService;
import org.cerberus.core.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author vertigo17
 */
@Service
public class EmailService implements IEmailService {

    @Autowired
    private ILogEventService logEventService;

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(EmailService.class);

    @Override
    public void sendHtmlMail(Email cerberusEmail) throws Exception {
        if (!StringUtil.isEmptyOrNull(cerberusEmail.getHost())
                && !"mail.com".equals(cerberusEmail.getHost())) {
            // Smtp host is defined and not equal to default value.

            HtmlEmail email = new HtmlEmail();
            email.setSmtpPort(cerberusEmail.getSmtpPort());
            email.setHostName(cerberusEmail.getHost());
            email.setFrom(cerberusEmail.getFrom());
            email.setSubject(cerberusEmail.getSubject());
            email.setHtmlMsg(cerberusEmail.getBody());
            email.setCharset("UTF-8");
            if (cerberusEmail.isSetTls()) {
                email = (HtmlEmail) email.setStartTLSEnabled(true);
            }
//        email.setTLS(cerberusEmail.isSetTls());
            email.setDebug(true);

            if (!StringUtil.isEmptyOrNull(cerberusEmail.getUserName()) || !StringUtil.isEmptyOrNull(cerberusEmail.getPassword())) {
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

            if (!StringUtil.isEmptyOrNull(cerberusEmail.getCc())) {
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

            logEventService.createForPrivateCalls("", "EMAIL", LogEvent.STATUS_INFO, "Start Sending email '" + cerberusEmail.getSubject() + "'.");
            LOG.info("Start Sending email '" + cerberusEmail.getSubject() + "'.");

            try {
                //Sending the email
                email.send();
            } catch (Exception e) {
                logEventService.createForPrivateCalls("", "EMAIL", LogEvent.STATUS_ERROR, "Error Sending email '" + cerberusEmail.getSubject() + "'");
                LOG.error("Exception catched when trying to send the mail '" + cerberusEmail.getSubject() + "' : ", e);
                throw e;
            }

            logEventService.createForPrivateCalls("", "EMAIL", LogEvent.STATUS_INFO, "Email Sent '" + cerberusEmail.getSubject() + "'.");
            LOG.info("End Sending email '" + cerberusEmail.getSubject() + "'.");

        } else {
            LOG.debug("Mail not send because smtp host not defined or default. smtp : " + cerberusEmail.getHost());
        }

    }

}
