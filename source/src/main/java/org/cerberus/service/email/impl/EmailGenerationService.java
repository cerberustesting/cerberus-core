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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.service.email.entity.Email;
import org.cerberus.crud.entity.BatchInvariant;
import org.cerberus.crud.entity.CountryEnvParam;
import org.cerberus.crud.entity.User;
import org.cerberus.crud.service.IBatchInvariantService;
import org.cerberus.crud.service.ICountryEnvParamService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.service.email.IEmailBodyGeneration;
import org.cerberus.service.email.IEmailFactory;
import org.cerberus.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.service.email.IEmailGenerationService;

/**
 *
 * @author bcivel
 */
@Service
public class EmailGenerationService implements IEmailGenerationService {

    private static final Logger LOG = LogManager.getLogger(EmailGenerationService.class);

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private ICountryEnvParamService countryEnvParamService;
    @Autowired
    private IEmailBodyGeneration emailBodyGeneration;
    @Autowired
    private IBatchInvariantService batchInvariantService;
    @Autowired
    private IEmailFactory emailFactory;

    @Override
    public Email generateRevisionChangeEmail(String system, String country, String env, String build, String revision) throws Exception {
        Email email = new Email();

        CountryEnvParam myCountryEnvParam;
        myCountryEnvParam = countryEnvParamService.convert(countryEnvParamService.readByKey(system, country, env));

        /* Pick the datas from the database */
        String from = parameterService.findParameterByKey("integration_smtp_from", system).getValue();
        String host = parameterService.findParameterByKey("integration_smtp_host", system).getValue();
        int port = Integer.valueOf(parameterService.findParameterByKey("integration_smtp_port", system).getValue());
        String userName = parameterService.findParameterByKey("integration_smtp_username", system).getValue();
        String password = parameterService.findParameterByKey("integration_smtp_password", system).getValue();
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

        content = emailBodyGeneration.GenerateBuildContentTable(system, build, revision, lastBuild, lastRev);
        content = content.replace("$", " ");
        body = body.replace("%BUILDCONTENT%", content);

        content = emailBodyGeneration.GenerateTestRecapTable(system, build, revision, country);
        content = content.replace("$", " ");
        body = body.replace("%TESTRECAP%", content);

        content = emailBodyGeneration.GenerateTestRecapTable(system, build, revision, "ALL");
        content = content.replace("$", " ");
        body = body.replace("%TESTRECAPALL%", content);
        //End

        email = emailFactory.create(host, port, userName, password, true, subject, body, from, to, cc);

        return email;

    }

    @Override
    public Email generateDisableEnvEmail(String system, String country, String env) throws Exception {
        Email email = new Email();

        CountryEnvParam myCountryEnvParam;
        myCountryEnvParam = countryEnvParamService.convert(countryEnvParamService.readByKey(system, country, env));

        /* Pick the datas from the database */
        String from = parameterService.findParameterByKey("integration_smtp_from", system).getValue();
        String host = parameterService.findParameterByKey("integration_smtp_host", system).getValue();
        int port = Integer.valueOf(parameterService.findParameterByKey("integration_smtp_port", system).getValue());
        String userName = parameterService.findParameterByKey("integration_smtp_username", system).getValue();
        String password = parameterService.findParameterByKey("integration_smtp_password", system).getValue();
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

        email = emailFactory.create(host, port, userName, password, true, subject, body, from, to, cc);

        return email;
    }

    @Override
    public Email generateNewChainEmail(String system, String country, String env, String chain) throws Exception {

        Email email = new Email();

        /* Page Display - START */
        CountryEnvParam myCountryEnvParam;
        myCountryEnvParam = countryEnvParamService.convert(countryEnvParamService.readByKey(system, country, env));

        BatchInvariant myBatchInvariant;
        myBatchInvariant = batchInvariantService.convert(batchInvariantService.readByKey(chain));
        String lastchain = myBatchInvariant.getBatch() + " (" + myBatchInvariant.getDescription() + ")";

        /* Pick the datas from the database */
        String from = parameterService.findParameterByKey("integration_smtp_from", system).getValue();
        String host = parameterService.findParameterByKey("integration_smtp_host", system).getValue();
        int port = Integer.valueOf(parameterService.findParameterByKey("integration_smtp_port", system).getValue());
        String userName = parameterService.findParameterByKey("integration_smtp_username", system).getValue();
        String password = parameterService.findParameterByKey("integration_smtp_password", system).getValue();
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

        email = emailFactory.create(host, port, userName, password, true, subject, body, from, to, cc);

        return email;

    }

    @Override
    public Email generateAccountCreationEmail(User user) throws Exception {
        String system = "";
        Email email = new Email();

        /* Pick the datas from the database */
        String from = parameterService.findParameterByKey("cerberus_notification_accountcreation_from", system).getValue();
        String host = parameterService.findParameterByKey("integration_smtp_host", system).getValue();
        int port = Integer.valueOf(parameterService.findParameterByKey("integration_smtp_port", system).getValue());
        String userName = parameterService.findParameterByKey("integration_smtp_username", system).getValue();
        String password = parameterService.findParameterByKey("integration_smtp_password", system).getValue();
        String to = user.getEmail();
        String cc = cc = parameterService.findParameterByKey("cerberus_notification_accountcreation_cc", system).getValue();
        String subject = parameterService.findParameterByKey("cerberus_notification_accountcreation_subject", system).getValue();
        String body = parameterService.findParameterByKey("cerberus_notification_accountcreation_body", system).getValue();

        body = body.replace("%NAME%", user.getName());
        body = body.replace("%LOGIN%", user.getLogin());
        body = body.replace("%DEFAULT_PASSWORD%", parameterService.findParameterByKey("cerberus_accountcreation_defaultpassword", system).getValue());

        email = emailFactory.create(host, port, userName, password, true, subject, body, from, to, cc);

        return email;
    }

    @Override
    public Email generateForgotPasswordEmail(User user) throws Exception {
        Email email = new Email();
        String system = "";

        String to = user.getEmail();
        String from = parameterService.findParameterByKey("cerberus_notification_accountcreation_from", system).getValue();
        String host = parameterService.findParameterByKey("integration_smtp_host", system).getValue();
        int port = Integer.valueOf(parameterService.findParameterByKey("integration_smtp_port", system).getValue());
        String userName = parameterService.findParameterByKey("integration_smtp_username", system).getValue();
        String password = parameterService.findParameterByKey("integration_smtp_password", system).getValue();
        String cc = parameterService.findParameterByKey("cerberus_notification_accountcreation_cc", system).getValue();
        String subject = parameterService.findParameterByKey("cerberus_notification_forgotpassword_subject", system).getValue();
        String body = parameterService.findParameterByKey("cerberus_notification_forgotpassword_body", system).getValue();
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

        email = emailFactory.create(host, port, userName, password, true, subject, body, from, to, cc);

        return email;

    }

    @Override
    public Email generateNotifyStartTagExecution(String tag, String campaign, String to) throws Exception {
        Email email = new Email();
        String system = "";

        String from = parameterService.getParameterStringByKey("cerberus_notification_tagexecutionstart_from", system,"Cerberus <no.reply@cerberus-testing.org>");
        String host = parameterService.findParameterByKey("integration_smtp_host", system).getValue();
        int port = Integer.valueOf(parameterService.findParameterByKey("integration_smtp_port", system).getValue());
        String userName = parameterService.findParameterByKey("integration_smtp_username", system).getValue();
        String password = parameterService.findParameterByKey("integration_smtp_password", system).getValue();
        String subject = parameterService.getParameterStringByKey("cerberus_notification_tagexecutionstart_subject", system, "Empty Subject. Please define parameter 'cerberus_notification_tagexecutionstart_subject'.");
        String body = parameterService.getParameterStringByKey("cerberus_notification_tagexecutionstart_body", system, "Empty Body. Please define parameter 'cerberus_notification_tagexecutionstart_body'.");

        String cerberusUrl = parameterService.findParameterByKey("cerberus_url", system).getValue();
        StringBuilder urlreporttag = new StringBuilder();
        urlreporttag.append(cerberusUrl);
        urlreporttag.append("/ReportingExecutionByTag.jsp?Tag=");
        urlreporttag.append(tag);
        body = body.replace("%TAG%", tag);
        body = body.replace("%URLTAGREPORT%", urlreporttag.toString());
        body = body.replace("%CAMPAIGN%", campaign);

        subject = subject.replace("%TAG%", tag);
        subject = subject.replace("%CAMPAIGN%", campaign);
        
        email = emailFactory.create(host, port, userName, password, true, subject, body, from, to, null);

        return email;

    }

    @Override
    public Email generateNotifyEndTagExecution(String tag, String campaign, String to) throws Exception {
        Email email = new Email();
        String system = "";

        String from = parameterService.getParameterStringByKey("cerberus_notification_tagexecutionend_from", system,"Cerberus <no.reply@cerberus-testing.org>");
        String host = parameterService.findParameterByKey("integration_smtp_host", system).getValue();
        int port = Integer.valueOf(parameterService.findParameterByKey("integration_smtp_port", system).getValue());
        String userName = parameterService.findParameterByKey("integration_smtp_username", system).getValue();
        String password = parameterService.findParameterByKey("integration_smtp_password", system).getValue();
        String subject = parameterService.getParameterStringByKey("cerberus_notification_tagexecutionend_subject", system, "Empty Subject. Please define parameter 'cerberus_notification_tagexecutionend_subject'.");
        String body = parameterService.getParameterStringByKey("cerberus_notification_tagexecutionend_body", system, "Empty Body. Please define parameter 'cerberus_notification_tagexecutionend_body'.");

        String cerberusUrl = parameterService.findParameterByKey("cerberus_url", system).getValue();
        StringBuilder urlreporttag = new StringBuilder();
        urlreporttag.append(cerberusUrl);
        urlreporttag.append("/ReportingExecutionByTag.jsp?Tag=");
        urlreporttag.append(tag);
        body = body.replace("%TAG%", tag);
        body = body.replace("%URLTAGREPORT%", urlreporttag.toString());
        body = body.replace("%CAMPAIGN%", campaign);

        subject = subject.replace("%TAG%", tag);
        subject = subject.replace("%CAMPAIGN%", campaign);
        
        email = emailFactory.create(host, port, userName, password, true, subject, body, from, to, null);

        return email;

    }

}
