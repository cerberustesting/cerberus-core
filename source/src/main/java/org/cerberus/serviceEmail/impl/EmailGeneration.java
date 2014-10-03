/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.serviceEmail.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.BatchInvariant;
import org.cerberus.entity.CountryEnvParam;
import org.cerberus.entity.User;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.IBatchInvariantService;
import org.cerberus.service.ICountryEnvParamService;
import org.cerberus.service.IParameterService;
import org.cerberus.serviceEmail.IEmailBodyGeneration;
import org.cerberus.serviceEmail.IEmailGeneration;
import org.cerberus.util.StringUtil;
import org.cerberus.version.Version;
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
            myCountryEnvParam = countryEnvParamService.findCountryEnvParamByKey(system, country, env);

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
            subject = subject.replaceAll("%SYSTEM%", system);
            subject = subject.replaceAll("%COUNTRY%", country);
            subject = subject.replaceAll("%ENV%", env);
            subject = subject.replaceAll("%BUILD%", build);
            subject = subject.replaceAll("%REVISION%", revision);

            body = body.replaceAll("%SYSTEM%", system);
            body = body.replaceAll("%COUNTRY%", country);
            body = body.replaceAll("%ENV%", env);
            body = body.replaceAll("%BUILD%", build);
            body = body.replaceAll("%REVISION%", revision);

            // Generate the Table Contented in the mail
            String content;

            String lastBuild = myCountryEnvParam.getBuild();
            String lastRev = myCountryEnvParam.getRevision();

            content = emailBodyGeneration.GenerateBuildContentTable(system, build, revision, lastBuild, lastRev, conn);
            body = body.replaceAll("%BUILDCONTENT%", content);

            content = emailBodyGeneration.GenerateTestRecapTable(system, build, revision, country, conn);
            body = body.replaceAll("%TESTRECAP%", content);

            content = emailBodyGeneration.GenerateTestRecapTable(system, build, revision, "ALL", conn);
            body = body.replaceAll("%TESTRECAPALL%", content);
            //End

            result = to + "///" + cc + "///" + subject + "///" + body + "///" + build + "///" + revision;

        } catch (CerberusException e) {
            Logger.getLogger(EmailGeneration.class.getName()).log(Level.SEVERE, Version.PROJECT_NAME_VERSION + " - Exception catched.", e);
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
            myCountryEnvParam = countryEnvParamService.findCountryEnvParamByKey(system, country, env);

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

            subject = subject.replaceAll("%SYSTEM%", system);
            subject = subject.replaceAll("%COUNTRY%", country);
            subject = subject.replaceAll("%ENV%", env);
            subject = subject.replaceAll("%BUILD%", myCountryEnvParam.getBuild());
            subject = subject.replaceAll("%REVISION%", myCountryEnvParam.getRevision());

            body = body.replaceAll("%SYSTEM%", system);
            body = body.replaceAll("%COUNTRY%", country);
            body = body.replaceAll("%ENV%", env);
            body = body.replaceAll("%BUILD%", myCountryEnvParam.getBuild());
            body = body.replaceAll("%REVISION%", myCountryEnvParam.getRevision());

            result = to + "///" + cc + "///" + subject + "///" + body;

        } catch (CerberusException e) {
            Logger.getLogger(EmailGeneration.class.getName()).log(Level.SEVERE, Version.PROJECT_NAME_VERSION + " - Exception catched.", e);
        }
        return result;
    }

    @Override
    public String EmailGenerationNewChain(String system, String country, String env, String build, String revision, String chain) {

        String result = "";

        try {
            /* Page Display - START */

            CountryEnvParam myCountryEnvParam;
            myCountryEnvParam = countryEnvParamService.findCountryEnvParamByKey(system, country, env);

            BatchInvariant myBatchInvariant;
            myBatchInvariant = batchInvariantService.findBatchInvariantByKey(chain);
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

            subject = subject.replaceAll("%SYSTEM%", system);
            subject = subject.replaceAll("%COUNTRY%", country);
            subject = subject.replaceAll("%ENV%", env);
            subject = subject.replaceAll("%BUILD%", myCountryEnvParam.getBuild());
            subject = subject.replaceAll("%REVISION%", myCountryEnvParam.getRevision());
            subject = subject.replaceAll("%CHAIN%", lastchain);

            body = body.replaceAll("%SYSTEM%", system);
            body = body.replaceAll("%COUNTRY%", country);
            body = body.replaceAll("%ENV%", env);
            body = body.replaceAll("%BUILD%", myCountryEnvParam.getBuild());
            body = body.replaceAll("%REVISION%", myCountryEnvParam.getRevision());
            body = body.replaceAll("%CHAIN%", lastchain);

            result = to + "///" + cc + "///" + subject + "///" + body + "///" + build + "///" + revision + "///" + chain;

        } catch (Exception e) {
            Logger.getLogger(EmailGeneration.class.getName()).log(Level.SEVERE, Version.PROJECT_NAME_VERSION + " - Exception catched.", e);
        }
        return result;

    }

    @Override
    public void BuildAndSendAccountCreationEmail(User user) {
        String system = "";
        String to;
        String from;
        String host;
        int port;
        String cc;
        String subject;
        String body;

        try {

            to = user.getEmail();
            from = parameterService.findParameterByKey("cerberus_notification_accountcreation_from", system).getValue();
            host = parameterService.findParameterByKey("integration_smtp_host", system).getValue();
            port = Integer.valueOf(parameterService.findParameterByKey("integration_smtp_port", system).getValue());
            cc = parameterService.findParameterByKey("cerberus_notification_accountcreation_cc", system).getValue();
            subject = parameterService.findParameterByKey("cerberus_notification_accountcreation_subject", system).getValue();
            body = parameterService.findParameterByKey("cerberus_notification_accountcreation_body", system).getValue();
            body = body.replaceAll("%NAME%", user.getName());
            body = body.replaceAll("%LOGIN%", user.getLogin());
            body = body.replaceAll("%DEFAULT_PASSWORD%", parameterService.findParameterByKey("cerberus_accountcreation_defaultpassword", system).getValue());
            
            sendMail.sendHtmlMail(host, port, body, subject, from, to, cc);
            
        } catch (CerberusException ex) {
            Logger.getLogger(EmailGeneration.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(EmailGeneration.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
