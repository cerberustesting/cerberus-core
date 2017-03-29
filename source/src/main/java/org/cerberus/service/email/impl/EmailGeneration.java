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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.BatchInvariant;
import org.cerberus.crud.entity.CountryEnvParam;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.crud.entity.User;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.crud.service.IBatchInvariantService;
import org.cerberus.crud.service.ICountryEnvParamService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.service.email.IEmailBodyGeneration;
import org.cerberus.service.email.IEmailGeneration;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.version.Infos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class EmailGeneration implements IEmailGeneration {

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private ICountryEnvParamService countryEnvParamService;
    @Autowired
    private IEmailBodyGeneration emailBodyGeneration;
    @Autowired
    private IBatchInvariantService batchInvariantService;
    //TODO remove connection from Service
    @Autowired
    private DatabaseSpring databaseSpring;

    @Override
    public String EmailGenerationRevisionChange(String system, String country, String env, String build, String revision) {
        String result = "";
        Connection conn = databaseSpring.connect();
        try {
            CountryEnvParam myCountryEnvParam;
            myCountryEnvParam = countryEnvParamService.convert(countryEnvParamService.readByKey(system, country, env));

            /* Pick the datas from the database */
            String to = parameterService.findParameterByKey("integration_notification_newbuildrevision_to", system).getValue();
            String cc = parameterService.findParameterByKey("integration_notification_newbuildrevision_cc", system).getValue();
            String subject = parameterService.findParameterByKey("integration_notification_newbuildrevision_subject", system).getValue();
            String body = parameterService.findParameterByKey("integration_notification_newbuildrevision_body", system).getValue();

            if (!StringUtil.isNullOrEmptyOrNull(myCountryEnvParam.geteMailBodyRevision())) {
                body = myCountryEnvParam.geteMailBodyRevision();
            }
            if (!StringUtil.isNullOrEmptyOrNull(myCountryEnvParam.getDistribList())) {
                to = myCountryEnvParam.getDistribList();
            }

            /* Replace the Keywords from the fed text  */
            subject = subject.replace("%SYSTEM%", system);
            subject = subject.replace("%COUNTRY%", country);
            subject = subject.replace("%ENV%", env);
            subject = subject.replace("%BUILD%", build);
            subject = subject.replace("%REVISION%", revision);

            body = body.replace("%SYSTEM%", system);
            body = body.replace("%COUNTRY%", country);
            body = body.replace("%ENV%", env);
            body = body.replace("%BUILD%", build);
            body = body.replace("%REVISION%", revision);

            // Generate the Table Contented in the mail
            String content;

            String lastBuild = myCountryEnvParam.getBuild();
            String lastRev = myCountryEnvParam.getRevision();

            content = emailBodyGeneration.GenerateBuildContentTable(system, build, revision, lastBuild, lastRev, conn);
            content = content.replace("$", " ");
            body = body.replace("%BUILDCONTENT%", content);

            content = emailBodyGeneration.GenerateTestRecapTable(system, build, revision, country, conn);
            content = content.replace("$", " ");
            body = body.replace("%TESTRECAP%", content);

            content = emailBodyGeneration.GenerateTestRecapTable(system, build, revision, "ALL", conn);
            content = content.replace("$", " ");
            body = body.replace("%TESTRECAPALL%", content);
            //End

            result = to + "///" + cc + "///" + subject + "///" + body + "///" + build + "///" + revision;

        } catch (Exception e) {
            Logger.getLogger(EmailGeneration.class.getName()).log(Level.SEVERE, Infos.getInstance().getProjectNameAndVersion() + " - Exception catched.", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                MyLogger.log(EmailGeneration.class.getName(), org.apache.log4j.Level.WARN, e.toString());
            }
        }

        return result;

    }

    @Override
    public String EmailGenerationDisableEnv(String system, String country, String env) {
        String result = "";
        try {
            CountryEnvParam myCountryEnvParam;
            myCountryEnvParam = countryEnvParamService.convert(countryEnvParamService.readByKey(system, country, env));

            String to = parameterService.findParameterByKey("integration_notification_disableenvironment_to", system).getValue();
            String cc = parameterService.findParameterByKey("integration_notification_disableenvironment_cc", system).getValue();
            String subject = parameterService.findParameterByKey("integration_notification_disableenvironment_subject", system).getValue();
            String body = parameterService.findParameterByKey("integration_notification_disableenvironment_body", system).getValue();

            if (!StringUtil.isNullOrEmptyOrNull(myCountryEnvParam.geteMailBodyDisableEnvironment())) {
                body = myCountryEnvParam.geteMailBodyDisableEnvironment();
            }

            if (!StringUtil.isNullOrEmptyOrNull(myCountryEnvParam.getDistribList())) {
                to = myCountryEnvParam.getDistribList();
            }

            subject = subject.replace("%SYSTEM%", system);
            subject = subject.replace("%COUNTRY%", country);
            subject = subject.replace("%ENV%", env);
            subject = subject.replace("%BUILD%", myCountryEnvParam.getBuild());
            subject = subject.replace("%REVISION%", myCountryEnvParam.getRevision());

            body = body.replace("%SYSTEM%", system);
            body = body.replace("%COUNTRY%", country);
            body = body.replace("%ENV%", env);
            body = body.replace("%BUILD%", myCountryEnvParam.getBuild());
            body = body.replace("%REVISION%", myCountryEnvParam.getRevision());

            result = to + "///" + cc + "///" + subject + "///" + body;

        } catch (CerberusException e) {
            Logger.getLogger(EmailGeneration.class.getName()).log(Level.SEVERE, Infos.getInstance().getProjectNameAndVersion() + " - Exception catched.", e);
        }
        return result;
    }

    @Override
    public String EmailGenerationNewChain(String system, String country, String env, String chain) {

        String result = "";

        try {
            /* Page Display - START */

            CountryEnvParam myCountryEnvParam;
            myCountryEnvParam = countryEnvParamService.convert(countryEnvParamService.readByKey(system, country, env));

            BatchInvariant myBatchInvariant;
            myBatchInvariant = batchInvariantService.convert(batchInvariantService.readByKey(chain));
            String lastchain = myBatchInvariant.getBatch() + " (" + myBatchInvariant.getDescription() + ")";

            String to = parameterService.findParameterByKey("integration_notification_newchain_to", system).getValue();
            String cc = parameterService.findParameterByKey("integration_notification_newchain_cc", system).getValue();
            String subject = parameterService.findParameterByKey("integration_notification_newchain_subject", system).getValue();
            String body = parameterService.findParameterByKey("integration_notification_newchain_body", system).getValue();

            if (!StringUtil.isNullOrEmptyOrNull(myCountryEnvParam.geteMailBodyChain())) {
                body = myCountryEnvParam.geteMailBodyChain();
            }
            if (!StringUtil.isNullOrEmptyOrNull(myCountryEnvParam.getDistribList())) {
                to = myCountryEnvParam.getDistribList();
            }

            subject = subject.replace("%SYSTEM%", system);
            subject = subject.replace("%COUNTRY%", country);
            subject = subject.replace("%ENV%", env);
            subject = subject.replace("%BUILD%", myCountryEnvParam.getBuild());
            subject = subject.replace("%REVISION%", myCountryEnvParam.getRevision());
            subject = subject.replace("%CHAIN%", lastchain);

            body = body.replace("%SYSTEM%", system);
            body = body.replace("%COUNTRY%", country);
            body = body.replace("%ENV%", env);
            body = body.replace("%BUILD%", myCountryEnvParam.getBuild());
            body = body.replace("%REVISION%", myCountryEnvParam.getRevision());
            body = body.replace("%CHAIN%", lastchain);

            result = to + "///" + cc + "///" + subject + "///" + body + "///" + chain;

        } catch (Exception e) {
            Logger.getLogger(EmailGeneration.class.getName()).log(Level.SEVERE, Infos.getInstance().getProjectNameAndVersion() + " - Exception catched.", e);
        }
        return result;

    }

    @Override
    public void BuildAndSendAccountCreationEmail(User user) {
        String system = "";
        String to;
        String from;
        String host;
        String userName;
        String password;
        int port;
        String cc;
        String subject;
        String body;

        try {

            to = user.getEmail();
            from = parameterService.findParameterByKey("cerberus_notification_accountcreation_from", system).getValue();
            host = parameterService.findParameterByKey("integration_smtp_host", system).getValue();
            port = Integer.valueOf(parameterService.findParameterByKey("integration_smtp_port", system).getValue());
            userName = parameterService.findParameterByKey("integration_smtp_username", system).getValue();
            password = parameterService.findParameterByKey("integration_smtp_password", system).getValue();
            cc = parameterService.findParameterByKey("cerberus_notification_accountcreation_cc", system).getValue();
            subject = parameterService.findParameterByKey("cerberus_notification_accountcreation_subject", system).getValue();
            body = parameterService.findParameterByKey("cerberus_notification_accountcreation_body", system).getValue();
            body = body.replace("%NAME%", user.getName());
            body = body.replace("%LOGIN%", user.getLogin());
            body = body.replace("%DEFAULT_PASSWORD%", parameterService.findParameterByKey("cerberus_accountcreation_defaultpassword", system).getValue());
            
            sendMail.sendHtmlMail(host, port, userName, password, body, subject, from, to, cc);
            
        } catch (CerberusException ex) {
            Logger.getLogger(EmailGeneration.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(EmailGeneration.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public Answer SendForgotPasswordNotification(User user) {
        Answer answer = new Answer();
        String system = "";
        String to;
        String from;
        String host;
        int port;
        String userName;
        String password;
        String cc;
        String subject;
        String body;

        try {

            to = user.getEmail();
            from = parameterService.findParameterByKey("cerberus_notification_accountcreation_from", system).getValue();
            host = parameterService.findParameterByKey("integration_smtp_host", system).getValue();
            port = Integer.valueOf(parameterService.findParameterByKey("integration_smtp_port", system).getValue());
            userName = parameterService.findParameterByKey("integration_smtp_username", system).getValue();
            password = parameterService.findParameterByKey("integration_smtp_password", system).getValue();
            cc = parameterService.findParameterByKey("cerberus_notification_accountcreation_cc", system).getValue();
            subject = parameterService.findParameterByKey("cerberus_notification_forgotpassword_subject", system).getValue();
            body = parameterService.findParameterByKey("cerberus_notification_forgotpassword_body", system).getValue();
            body = body.replace("%NAME%", user.getName());
            body = body.replace("%LOGIN%", user.getLogin());
            String cerberusUrl = parameterService.findParameterByKey("cerberus_url", system).getValue();
            StringBuilder sb = new StringBuilder();
            sb.append("<a href='");
            sb.append(cerberusUrl);
            sb.append("/ChangePassword.jsp?login=");
            sb.append(user.getLogin());
            sb.append("&confirmationToken=");
            sb.append(user.getResetPasswordToken());
            sb.append("'>Click here to reset your password</a>");
            
            body = body.replace("%LINK%", sb.toString());
            
            sendMail.sendHtmlMail(host, port, userName, password, body, subject, from, to, cc);
            
            answer.setResultMessage(new MessageEvent(MessageEventEnum.GENERIC_OK));
            return answer;
        } catch (CerberusException ex) {
            Logger.getLogger(EmailGeneration.class.getName()).log(Level.SEVERE, null, ex);
            MessageEvent mes = new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
            answer.setResultMessage(mes);
            return answer;
        } catch (Exception ex) {
            Logger.getLogger(EmailGeneration.class.getName()).log(Level.SEVERE, null, ex);
            MessageEvent mes = new MessageEvent(MessageEventEnum.GENERIC_ERROR).resolveDescription("REASON", ex.toString());
            answer.setResultMessage(mes);
            return answer;
        }

    }
}
