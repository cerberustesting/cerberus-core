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

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.service.email.entity.Email;
import org.cerberus.crud.entity.BatchInvariant;
import org.cerberus.crud.entity.CountryEnvParam;
import org.cerberus.crud.entity.Tag;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.User;
import org.cerberus.crud.service.IBatchInvariantService;
import org.cerberus.crud.service.ICountryEnvParamService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITagService;
import org.cerberus.crud.service.ITestCaseExecutionService;
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
    private ITagService tagService;
    @Autowired
    private ITestCaseExecutionService testCaseExecutionService;
    @Autowired
    private IEmailFactory emailFactory;

    @Override
    public Email generateRevisionChangeEmail(String system, String country, String env, String build, String revision) throws Exception {
        Email email = new Email();

        CountryEnvParam myCountryEnvParam;
        myCountryEnvParam = countryEnvParamService.convert(countryEnvParamService.readByKey(system, country, env));

        /* Pick the datas from the database */
        String from = parameterService.findParameterByKey("cerberus_smtp_from", system).getValue();
        String host = parameterService.findParameterByKey("cerberus_smtp_host", system).getValue();
        int port = Integer.valueOf(parameterService.findParameterByKey("cerberus_smtp_port", system).getValue());
        String userName = parameterService.findParameterByKey("cerberus_smtp_username", system).getValue();
        String password = parameterService.findParameterByKey("cerberus_smtp_password", system).getValue();
        String to = parameterService.findParameterByKey("cerberus_notification_newbuildrevision_to", system).getValue();
        String cc = parameterService.findParameterByKey("cerberus_notification_newbuildrevision_cc", system).getValue();
        String subject = parameterService.findParameterByKey("cerberus_notification_newbuildrevision_subject", system).getValue();
        String body = parameterService.findParameterByKey("cerberus_notification_newbuildrevision_body", system).getValue();

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
        String from = parameterService.findParameterByKey("cerberus_smtp_from", system).getValue();
        String host = parameterService.findParameterByKey("cerberus_smtp_host", system).getValue();
        int port = Integer.valueOf(parameterService.findParameterByKey("cerberus_smtp_port", system).getValue());
        String userName = parameterService.findParameterByKey("cerberus_smtp_username", system).getValue();
        String password = parameterService.findParameterByKey("cerberus_smtp_password", system).getValue();
        String to = parameterService.findParameterByKey("cerberus_notification_disableenvironment_to", system).getValue();
        String cc = parameterService.findParameterByKey("cerberus_notification_disableenvironment_cc", system).getValue();
        String subject = parameterService.findParameterByKey("cerberus_notification_disableenvironment_subject", system).getValue();
        String body = parameterService.findParameterByKey("cerberus_notification_disableenvironment_body", system).getValue();

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
        String from = parameterService.findParameterByKey("cerberus_smtp_from", system).getValue();
        String host = parameterService.findParameterByKey("cerberus_smtp_host", system).getValue();
        int port = Integer.valueOf(parameterService.findParameterByKey("cerberus_smtp_port", system).getValue());
        String userName = parameterService.findParameterByKey("cerberus_smtp_username", system).getValue();
        String password = parameterService.findParameterByKey("cerberus_smtp_password", system).getValue();
        String to = parameterService.findParameterByKey("cerberus_notification_newchain_to", system).getValue();
        String cc = parameterService.findParameterByKey("cerberus_notification_newchain_cc", system).getValue();
        String subject = parameterService.findParameterByKey("cerberus_notification_newchain_subject", system).getValue();
        String body = parameterService.findParameterByKey("cerberus_notification_newchain_body", system).getValue();

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
        String host = parameterService.findParameterByKey("cerberus_smtp_host", system).getValue();
        int port = Integer.valueOf(parameterService.findParameterByKey("cerberus_smtp_port", system).getValue());
        String userName = parameterService.findParameterByKey("cerberus_smtp_username", system).getValue();
        String password = parameterService.findParameterByKey("cerberus_smtp_password", system).getValue();
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
        String host = parameterService.findParameterByKey("cerberus_smtp_host", system).getValue();
        int port = Integer.valueOf(parameterService.findParameterByKey("cerberus_smtp_port", system).getValue());
        String userName = parameterService.findParameterByKey("cerberus_smtp_username", system).getValue();
        String password = parameterService.findParameterByKey("cerberus_smtp_password", system).getValue();
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

        String from = parameterService.getParameterStringByKey("cerberus_notification_tagexecutionstart_from", system, "Cerberus <no.reply@cerberus-testing.org>");
        String host = parameterService.findParameterByKey("cerberus_smtp_host", system).getValue();
        int port = Integer.valueOf(parameterService.findParameterByKey("cerberus_smtp_port", system).getValue());
        String userName = parameterService.findParameterByKey("cerberus_smtp_username", system).getValue();
        String password = parameterService.findParameterByKey("cerberus_smtp_password", system).getValue();
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

        String from = parameterService.getParameterStringByKey("cerberus_notification_tagexecutionend_from", system, "Cerberus <no.reply@cerberus-testing.org>");
        String host = parameterService.findParameterByKey("cerberus_smtp_host", system).getValue();
        int port = Integer.valueOf(parameterService.findParameterByKey("cerberus_smtp_port", system).getValue());
        String userName = parameterService.findParameterByKey("cerberus_smtp_username", system).getValue();
        String password = parameterService.findParameterByKey("cerberus_smtp_password", system).getValue();
        String subject = parameterService.getParameterStringByKey("cerberus_notification_tagexecutionend_subject", system, "Empty Subject. Please define parameter 'cerberus_notification_tagexecutionend_subject'.");
        String body = parameterService.getParameterStringByKey("cerberus_notification_tagexecutionend_body", system, "Empty Body. Please define parameter 'cerberus_notification_tagexecutionend_body'.");

        String cerberusUrl = parameterService.findParameterByKey("cerberus_url", system).getValue();
        StringBuilder urlreporttag = new StringBuilder();
        urlreporttag.append(cerberusUrl);
        urlreporttag.append("/ReportingExecutionByTag.jsp?Tag=");
        urlreporttag.append(tag);

        // Body replace.
        body = body.replace("%TAG%", tag);
        body = body.replace("%URLTAGREPORT%", urlreporttag.toString());
        body = body.replace("%CAMPAIGN%", campaign);

        Tag mytag = tagService.convert(tagService.readByKey(tag));
        long tagDur = (mytag.getDateEndQueue().getTime() - mytag.getDateCreated().getTime()) / 60000;
        body = body.replace("%TAGDURATION%", String.valueOf(tagDur));
        body = body.replace("%TAGSTART%", String.valueOf(mytag.getDateCreated()));
        body = body.replace("%TAGEND%", String.valueOf(mytag.getDateEndQueue()));

        // Get TestcaseExecutionDetail in order to replace %TAGGLOBALSTATUS% or %TAGTCDETAIL%.
        List<TestCaseExecution> testCaseExecutions = testCaseExecutionService.readLastExecutionAndExecutionInQueueByTag(tag);
        StringBuilder globalStatus = new StringBuilder();
        globalStatus.append("<table><thead><tr style=\"background-color:#cad3f1; font-style:bold\"><td>Status</td><td>Number</td><td>%</td></tr></thead><tbody>");
        Map<String, Integer> axisMap = new HashMap<String, Integer>();
        Integer total;
        total = testCaseExecutions.size();
        for (TestCaseExecution execution : testCaseExecutions) {
            if (axisMap.containsKey(execution.getControlStatus())) {
                axisMap.put(execution.getControlStatus(), axisMap.get(execution.getControlStatus()) + 1);
            } else {
                axisMap.put(execution.getControlStatus(), 1);
            }
        }
        float per = 0;
        DecimalFormat df = new DecimalFormat("#.##");
        for (Map.Entry<String, Integer> entry : axisMap.entrySet()) {
            globalStatus.append("<tr>");
            globalStatus.append("<td>").append(entry.getKey()).append("</td>");
            globalStatus.append("<td>").append(entry.getValue()).append("</td>");
            per = (float) entry.getValue() / (float) total;
            per = per * 100;
            globalStatus.append("<td>").append(String.format("%.2f", per)).append("</td>");
            globalStatus.append("</tr>");
        }
        globalStatus.append("<tr style=\"background-color:#cad3f1; font-style:bold\"><td>TOTAL</td>");
        globalStatus.append("<td>").append(total).append("</td>");
        globalStatus.append("<td></td></tr>");
        globalStatus.append("</tbody></table>");
        body = body.replace("%TAGGLOBALSTATUS%", globalStatus.toString());

        Integer totalTC = 0;
        StringBuilder detailStatus = new StringBuilder();
        detailStatus.append("<table><thead><tr style=\"background-color:#cad3f1; font-style:bold\"><td>Test</td><td>Test Case</td><td>Description</td><td>Environment</td><td>Country</td><td>Status</td></tr></thead><tbody>");
        for (TestCaseExecution execution : testCaseExecutions) {
            if (!TestCaseExecution.CONTROLSTATUS_OK.equals(execution.getControlStatus())) {
                detailStatus.append("<tr>");
                detailStatus.append("<td>").append(execution.getTest()).append("</td>");
                detailStatus.append("<td>").append(execution.getTestCase()).append("</td>");
                detailStatus.append("<td>").append(execution.getDescription()).append("</td>");
                detailStatus.append("<td>").append(execution.getEnvironment()).append("</td>");
                detailStatus.append("<td>").append(execution.getCountry()).append("</td>");
                detailStatus.append("<td>").append(execution.getControlStatus()).append("</td>");
                detailStatus.append("</tr>");
                totalTC++;
            }
        }
        detailStatus.append("<tr style=\"background-color:#cad3f1; font-style:bold\">");
        detailStatus.append("<td>TOTAL</td>");
        detailStatus.append("<td colspan=\"5\">").append(totalTC).append("</td>");
        detailStatus.append("</tr>");
        detailStatus.append("</tbody></table>");
        body = body.replace("%TAGTCDETAIL%", detailStatus.toString());

        // Subject replace.
        subject = subject.replace("%TAG%", tag);
        subject = subject.replace("%CAMPAIGN%", campaign);

        email = emailFactory.create(host, port, userName, password, true, subject, body, from, to, null);

        return email;

    }

}
