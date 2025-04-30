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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.BatchInvariant;
import org.cerberus.core.crud.entity.CountryEnvParam;
import org.cerberus.core.crud.entity.EventHook;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.User;
import org.cerberus.core.crud.service.IBatchInvariantService;
import org.cerberus.core.crud.service.ICountryEnvParamService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.service.notifications.email.IEmailBodyGeneration;
import org.cerberus.core.service.notifications.email.IEmailFactory;
import org.cerberus.core.service.notifications.email.IEmailGenerationService;
import org.cerberus.core.service.notifications.email.entity.Email;
import org.cerberus.core.util.StringUtil;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author vertigo17
 */
@Service
public class EmailGenerationService implements IEmailGenerationService {

    private static final Logger LOG = LogManager.getLogger(EmailGenerationService.class);

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private ICountryEnvParamService countryEnvParamService;
    @Autowired
    private IBatchInvariantService batchInvariantService;
    @Autowired
    private ITagService tagService;
    @Autowired
    private ITestCaseExecutionService testCaseExecutionService;
    @Autowired
    private IEmailFactory emailFactory;
    @Autowired
    private IEmailBodyGeneration emailBodyGeneration;

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
        boolean isSetTls = parameterService.getParameterBooleanByKey("cerberus_smtp_isSetTls", system, true);

        if (StringUtil.isNotEmptyOrNULLString(myCountryEnvParam.geteMailBodyRevision())) {
            body = myCountryEnvParam.geteMailBodyRevision();
        }
        if (StringUtil.isNotEmptyOrNULLString(myCountryEnvParam.getDistribList())) {
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

        if (body.contains("%BUILDCONTENT%")) {
            content = "";
            content = emailBodyGeneration.GenerateBuildContentTable(system, build, revision, lastBuild, lastRev);
            content = content.replace("$", " ");
            body = body.replace("%BUILDCONTENT%", content);
        }

        email = emailFactory.create(host, port, userName, password, isSetTls, subject, body, from, to, cc);

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
        boolean isSetTls = parameterService.getParameterBooleanByKey("cerberus_smtp_isSetTls", system, true);

        if (StringUtil.isNotEmptyOrNULLString(myCountryEnvParam.geteMailBodyDisableEnvironment())) {
            body = myCountryEnvParam.geteMailBodyDisableEnvironment();
        }

        if (StringUtil.isNotEmptyOrNULLString(myCountryEnvParam.getDistribList())) {
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

        email = emailFactory.create(host, port, userName, password, isSetTls, subject, body, from, to, cc);

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
        boolean isSetTls = parameterService.getParameterBooleanByKey("cerberus_smtp_isSetTls", system, true);

        if (StringUtil.isNotEmptyOrNULLString(myCountryEnvParam.geteMailBodyChain())) {
            body = myCountryEnvParam.geteMailBodyChain();
        }
        if (StringUtil.isNotEmptyOrNULLString(myCountryEnvParam.getDistribList())) {
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

        email = emailFactory.create(host, port, userName, password, isSetTls, subject, body, from, to, cc);

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
        String cc = parameterService.findParameterByKey("cerberus_notification_accountcreation_cc", system).getValue();
        String subject = parameterService.findParameterByKey("cerberus_notification_accountcreation_subject", system).getValue();
        String body = parameterService.findParameterByKey("cerberus_notification_accountcreation_body", system).getValue();
        boolean isSetTls = parameterService.getParameterBooleanByKey("cerberus_smtp_isSetTls", system, true);

        body = body.replace("%NAME%", user.getName());
        body = body.replace("%LOGIN%", user.getLogin());
        body = body.replace("%DEFAULT_PASSWORD%", parameterService.findParameterByKey("cerberus_accountcreation_defaultpassword", system).getValue());

        email = emailFactory.create(host, port, userName, password, isSetTls, subject, body, from, to, cc);

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
        boolean isSetTls = parameterService.getParameterBooleanByKey("cerberus_smtp_isSetTls", system, true);
        body = body.replace("%NAME%", user.getName());
        body = body.replace("%LOGIN%", user.getLogin());

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", system, "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", system, "");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<a href='");
        sb.append(cerberusUrl);
        sb.append("/ChangePassword.jsp?login=");
        sb.append(user.getLogin());
        sb.append("&confirmationToken=");
        sb.append(user.getResetPasswordToken());
        sb.append("'>Click here to reset your password</a>");

        body = body.replace("%LINK%", sb.toString());

        email = emailFactory.create(host, port, userName, password, isSetTls, subject, body, from, to, cc);

        return email;

    }

    @Override
    public Email generateNotifyStartTagExecution(Tag tag, String to) throws Exception {
        Email email = new Email();
        String system = "";

        String from = parameterService.getParameterStringByKey("cerberus_notification_tagexecutionstart_from", system, "Cerberus <no.reply@cerberus-testing.org>");
        String host = parameterService.findParameterByKey("cerberus_smtp_host", system).getValue();
        int port = Integer.valueOf(parameterService.findParameterByKey("cerberus_smtp_port", system).getValue());
        String userName = parameterService.findParameterByKey("cerberus_smtp_username", system).getValue();
        String password = parameterService.findParameterByKey("cerberus_smtp_password", system).getValue();
        String subject = parameterService.getParameterStringByKey("cerberus_notification_tagexecutionstart_subject", system, "Empty Subject. Please define parameter 'cerberus_notification_tagexecutionstart_subject'.");
        String body = parameterService.getParameterStringByKey("cerberus_notification_tagexecutionstart_body", system, "Empty Body. Please define parameter 'cerberus_notification_tagexecutionstart_body'.");
        boolean isSetTls = parameterService.getParameterBooleanByKey("cerberus_smtp_isSetTls", system, true);

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", system, "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", system, "");
        }

        String myEnvironmentList = StringUtil.convertToString(new JSONArray(tag.getReqEnvironmentList()), ",");
        String myCountryList = StringUtil.convertToString(new JSONArray(tag.getReqCountryList()), ",");

        StringBuilder urlreporttag = new StringBuilder();
        urlreporttag.append(cerberusUrl);
        urlreporttag.append("/ReportingExecutionByTag.jsp?Tag=");
        urlreporttag.append(StringUtil.encodeURL(tag.getTag()));
        body = body.replace("%TAG%", tag.getTag());
        body = body.replace("%URLTAGREPORT%", urlreporttag.toString());
        body = body.replace("%CAMPAIGN%", tag.getCampaign());
        body = body.replace("%REQENVIRONMENTLIST%", myEnvironmentList);
        body = body.replace("%REQCOUNTRYLIST%", myCountryList);

        subject = subject.replace("%TAG%", tag.getTag());
        subject = subject.replace("%CAMPAIGN%", tag.getCampaign());
        subject = subject.replace("%REQENVIRONMENTLIST%", myEnvironmentList);
        subject = subject.replace("%REQCOUNTRYLIST%", myCountryList);

        email = emailFactory.create(host, port, userName, password, isSetTls, subject, body, from, to, null);

        LOG.debug(subject);
        LOG.debug(body);
        return email;

    }

    @Override
    public Email generateNotifyEndTagExecution(Tag tag, String to) throws Exception {
        try {

            Email email = new Email();
            String system = "";

            String from = parameterService.getParameterStringByKey("cerberus_notification_tagexecutionend_from", system, "Cerberus <no.reply@cerberus-testing.org>");
            String host = parameterService.findParameterByKey("cerberus_smtp_host", system).getValue();
            int port = Integer.valueOf(parameterService.findParameterByKey("cerberus_smtp_port", system).getValue());
            String userName = parameterService.findParameterByKey("cerberus_smtp_username", system).getValue();
            String password = parameterService.findParameterByKey("cerberus_smtp_password", system).getValue();
            String subject = parameterService.getParameterStringByKey("cerberus_notification_tagexecutionend_subject", system, "Empty Subject. Please define parameter 'cerberus_notification_tagexecutionend_subject'.");
            String body = parameterService.getParameterStringByKey("cerberus_notification_tagexecutionend_body", system, "Empty Body. Please define parameter 'cerberus_notification_tagexecutionend_body'.");
            boolean isSetTls = parameterService.getParameterBooleanByKey("cerberus_smtp_isSetTls", system, true);

            String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", system, "");
            if (StringUtil.isEmptyOrNull(cerberusUrl)) {
                cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", system, "");
            }

            StringBuilder urlreporttag = new StringBuilder();
            urlreporttag.append(cerberusUrl);
            urlreporttag.append("/ReportingExecutionByTag.jsp?Tag=");
            urlreporttag.append(StringUtil.encodeURL(tag.getTag()));

            // Body replace.
            body = body.replace("%TAG%", tag.getTag());
            body = body.replace("%URLTAGREPORT%", urlreporttag.toString());
            body = body.replace("%CAMPAIGN%", tag.getCampaign());
            body = body.replace("%CIRESULT%", tag.getCiResult());
            String ciColor = TestCaseExecution.CONTROLSTATUS_KO_COL_EXT;
            if ("OK".equals(tag.getCiResult())) {
                ciColor = TestCaseExecution.CONTROLSTATUS_OK_COL_EXT;
            }
            body = body.replace("%CIRESULTCOLOR%", ciColor);
            body = body.replace("%CISCORE%", String.valueOf(tag.getCiScore()));
            body = body.replace("%CISCORETHRESHOLD%", String.valueOf(tag.getCiScoreThreshold()));

            String myEnvironmentList = StringUtil.convertToString(new JSONArray(tag.getEnvironmentList()), ",");
            String myCountryList = StringUtil.convertToString(new JSONArray(tag.getCountryList()), ",");
            String myApplicationList = StringUtil.convertToString(new JSONArray(tag.getApplicationList()), ",");
            String mySystemList = StringUtil.convertToString(new JSONArray(tag.getSystemList()), ",");
            String myRobotList = StringUtil.convertToString(new JSONArray(tag.getRobotDecliList()), ",");

            body = body.replace("%ENVIRONMENTLIST%", myEnvironmentList);
            body = body.replace("%COUNTRYLIST%", myCountryList);
            body = body.replace("%APPLICATIONLIST%", myApplicationList);
            body = body.replace("%SYSTEMLIST%", mySystemList);
            body = body.replace("%ROBOTDECLILIST%", myRobotList);

            String myReqEnvironmentList = StringUtil.convertToString(new JSONArray(tag.getReqEnvironmentList()), ",");
            String myReqCountryList = StringUtil.convertToString(new JSONArray(tag.getReqCountryList()), ",");

            body = body.replace("%REQENVIRONMENTLIST%", myReqEnvironmentList);
            body = body.replace("%REQCOUNTRYLIST%", myReqCountryList);

            long tagDur = (tag.getDateEndQueue().getTime() - tag.getDateCreated().getTime()) / 60000;
            body = body.replace("%TAGDURATION%", String.valueOf(tagDur));
            body = body.replace("%TAGSTART%", String.valueOf(tag.getDateCreated()));
            body = body.replace("%TAGEND%", String.valueOf(tag.getDateEndQueue()));

            // Get TestcaseExecutionDetail in order to replace %TAGGLOBALSTATUS%.
            StringBuilder globalStatus = new StringBuilder();
            globalStatus.append("<table><thead><tr style=\"background-color:#cad3f1; font-style:bold\"><td>Status</td><td>Number</td><td>%</td></tr></thead><tbody>");
            // Map that will contain the color of every status.
            Map<String, String> statColorMap = new HashMap<>();
            statColorMap.put(TestCaseExecution.CONTROLSTATUS_OK, TestCaseExecution.CONTROLSTATUS_OK_COL_EXT);
            statColorMap.put(TestCaseExecution.CONTROLSTATUS_KO, TestCaseExecution.CONTROLSTATUS_KO_COL_EXT);
            statColorMap.put(TestCaseExecution.CONTROLSTATUS_FA, TestCaseExecution.CONTROLSTATUS_FA_COL_EXT);
            statColorMap.put(TestCaseExecution.CONTROLSTATUS_NA, TestCaseExecution.CONTROLSTATUS_NA_COL_EXT);
            statColorMap.put(TestCaseExecution.CONTROLSTATUS_NE, TestCaseExecution.CONTROLSTATUS_NE_COL_EXT);
            statColorMap.put(TestCaseExecution.CONTROLSTATUS_WE, TestCaseExecution.CONTROLSTATUS_WE_COL_EXT);
            statColorMap.put(TestCaseExecution.CONTROLSTATUS_PE, TestCaseExecution.CONTROLSTATUS_PE_COL_EXT);
            statColorMap.put(TestCaseExecution.CONTROLSTATUS_QU, TestCaseExecution.CONTROLSTATUS_QU_COL_EXT);
            statColorMap.put(TestCaseExecution.CONTROLSTATUS_PA, TestCaseExecution.CONTROLSTATUS_PA_COL_EXT);
            statColorMap.put(TestCaseExecution.CONTROLSTATUS_QE, TestCaseExecution.CONTROLSTATUS_QE_COL_EXT);
            statColorMap.put(TestCaseExecution.CONTROLSTATUS_CA, TestCaseExecution.CONTROLSTATUS_CA_COL_EXT);
            // Map that will contain the nb of execution for global status.
            Map<String, Integer> statNbMap = new HashMap<>();
            statNbMap.put(TestCaseExecution.CONTROLSTATUS_OK, tag.getNbOK());
            statNbMap.put(TestCaseExecution.CONTROLSTATUS_KO, tag.getNbKO());
            statNbMap.put(TestCaseExecution.CONTROLSTATUS_FA, tag.getNbFA());
            statNbMap.put(TestCaseExecution.CONTROLSTATUS_NA, tag.getNbNA());
            statNbMap.put(TestCaseExecution.CONTROLSTATUS_NE, tag.getNbNE());
            statNbMap.put(TestCaseExecution.CONTROLSTATUS_WE, tag.getNbWE());
            statNbMap.put(TestCaseExecution.CONTROLSTATUS_PE, tag.getNbPE());
            statNbMap.put(TestCaseExecution.CONTROLSTATUS_QU, tag.getNbQU());
            statNbMap.put(TestCaseExecution.CONTROLSTATUS_PA, tag.getNbQU());
            statNbMap.put(TestCaseExecution.CONTROLSTATUS_QE, tag.getNbQE());
            statNbMap.put(TestCaseExecution.CONTROLSTATUS_CA, tag.getNbCA());
            // Status list in the correct order.
            float per = 0;
            List<String> statList = new ArrayList<>(Arrays.asList("OK", "KO", "FA", "NA", "NE", "WE", "PE", "QU", "QE", "CA"));
            for (String string : statList) {
                if (statNbMap.get(string) > 0) {
                    globalStatus.append("<tr>");
                    globalStatus.append("<td style=\"background-color:").append(statColorMap.get(string)).append(";text-align: center;\">").append(string).append("</td>");
                    globalStatus.append("<td style=\"text-align: right;\">").append(statNbMap.get(string)).append("</td>");
                    per = statNbMap.get(string) / (float) tag.getNbExeUsefull();
                    per *= 100;
                    globalStatus.append("<td style=\"text-align: right;\">").append(String.format("%.2f", per)).append("</td>");
                    globalStatus.append("</tr>");
                }
            }
            globalStatus.append("<tr style=\"background-color:#cad3f1; font-style:bold; text-align: center;\"><td>TOTAL</td>");
            globalStatus.append("<td style=\"text-align: right;\">").append(tag.getNbExeUsefull()).append("</td>");
            globalStatus.append("<td></td></tr>");
            globalStatus.append("</tbody></table>");
            body = body.replace("%TAGGLOBALSTATUS%", globalStatus.toString());

            // Get TestcaseExecutionDetail in order to replace %TAGTCDETAIL%.
            StringBuilder detailStatus = new StringBuilder();
            List<TestCaseExecution> testCaseExecutions = testCaseExecutionService.readLastExecutionAndExecutionInQueueByTag(tag.getTag());
            Collections.sort(testCaseExecutions, new SortExecution());
            Integer totalTC = 0;
            Integer maxLines = parameterService.getParameterIntegerByKey("cerberus_notification_tagexecutionend_tclistmax", system, 100);
            boolean odd = true;
            detailStatus.append("<table><thead><tr style=\"background-color:#cad3f1; font-style:bold\"><td>Prio</td><td>Test Folder</td><td>Test Case</td><td>Environment</td><td>Country</td><td>Robot Decli</td><td>Status</td></tr></thead><tbody>");
            for (TestCaseExecution execution : testCaseExecutions) {
                if (!TestCaseExecution.CONTROLSTATUS_OK.equals(execution.getControlStatus())) {
                    if (totalTC < maxLines) {
                        String tr = "";
                        if (odd) {
                            tr = "<tr style=\"background-color:#E3E3E3\">";
                        } else {
                            tr = "<tr>";
                        }
                        odd = !odd;
                        detailStatus.append(tr);
                        detailStatus.append("<td rowspan=\"2\" style=\"text-align: center;\">").append(execution.getTestCasePriority()).append("</td>");
                        detailStatus.append("<td><b>").append(execution.getTest()).append("</b></td>");
                        detailStatus.append("<td><b>").append(execution.getTestCase()).append("</b></td>");
                        detailStatus.append("<td>").append(execution.getEnvironment()).append("</td>");
                        detailStatus.append("<td>").append(execution.getCountry()).append("</td>");
                        detailStatus.append("<td>").append(execution.getRobotDecli()).append("</td>");
                        detailStatus.append("<td rowspan=\"2\" style=\"text-align: center; background-color:").append(statColorMap.get(execution.getControlStatus())).append(";\">").append(execution.getControlStatus()).append("</td>");
                        detailStatus.append("</tr>");
                        detailStatus.append(tr);
                        detailStatus.append("<td colspan=\"5\" style=\"font-size: xx-small;margin-left: 10px;\">").append(execution.getDescription()).append("</td>");
                        detailStatus.append("</tr>");
                    } else if (totalTC == maxLines) {
                        detailStatus.append("<tr style=\"background-color:#ffcaba; font-style:bold\">");
                        detailStatus.append("<td colspan=\"7\">Only the first ");
                        detailStatus.append(maxLines);
                        detailStatus.append(" row(s) are displayed...</td>");
                        detailStatus.append("</tr>");
                    }
                    totalTC++;
                }
            }
            detailStatus.append("<tr style=\"background-color:#cad3f1; font-style:bold\">");
            detailStatus.append("<td>TOTAL</td>");
            detailStatus.append("<td colspan=\"6\">").append(totalTC).append("</td>");
            detailStatus.append("</tr>");
            detailStatus.append("</tbody></table>");
            body = body.replace("%TAGTCDETAIL%", detailStatus.toString());

            // Subject replace.
            subject = subject.replace("%TAG%", tag.getTag());
            subject = subject.replace("%CAMPAIGN%", tag.getCampaign());
            subject = subject.replace("%ENVIRONMENTLIST%", myEnvironmentList);
            subject = subject.replace("%COUNTRYLIST%", myCountryList);
            subject = subject.replace("%APPLICATIONLIST%", myApplicationList);
            subject = subject.replace("%SYSTEMLIST%", mySystemList);
            subject = subject.replace("%ROBOTDECLILIST%", myRobotList);
            subject = subject.replace("%REQENVIRONMENTLIST%", myReqEnvironmentList);
            subject = subject.replace("%REQCOUNTRYLIST%", myReqCountryList);

            email = emailFactory.create(host, port, userName, password, isSetTls, subject, body, from, to, null);

            LOG.debug(subject);
            LOG.debug(body);
            return email;

        } catch (Exception e) {
            LOG.error(e.toString(), e);
        }
        return null;

    }

    class SortExecution implements Comparator<TestCaseExecution> {
        // Used for sorting in ascending order of 
        // Label name. 

        @Override
        public int compare(TestCaseExecution a, TestCaseExecution b) {
            if (a != null && b != null) {
                int aPrio = a.getTestCasePriority();
                if (a.getTestCasePriority() < 1 || a.getTestCasePriority() > 5) {
                    aPrio = 999 + a.getTestCasePriority();
                }
                int bPrio = b.getTestCasePriority();
                if (b.getTestCasePriority() < 1 || b.getTestCasePriority() > 5) {
                    bPrio = 999 + b.getTestCasePriority();
                }

                if (aPrio == bPrio) {
                    if (a.getTest().equals(b.getTest())) {
                        if (a.getTestCase().equals(b.getTestCase())) {
                            if (a.getEnvironment().equals(b.getEnvironment())) {
                                if (a.getCountry().equals(b.getCountry())) {
                                    return a.getRobotDecli().compareToIgnoreCase(b.getRobotDecli());
                                } else {
                                    return a.getCountry().compareToIgnoreCase(b.getCountry());
                                }
                            } else {
                                return a.getEnvironment().compareToIgnoreCase(b.getEnvironment());
                            }
                        } else {
                            return a.getTestCase().compareToIgnoreCase(b.getTestCase());
                        }
                    } else {
                        return a.getTest().compareToIgnoreCase(b.getTest());
                    }
                } else {
                    return aPrio - bPrio;
                }
            } else {
                return 1;
            }
        }
    }

    @Override
    public Email generateNotifyStartExecution(TestCaseExecution exe, String to) throws Exception {
        Email email = new Email();
        String system = "";

        String from = parameterService.getParameterStringByKey("cerberus_notification_from", system, "Cerberus <no.reply@cerberus-testing.org>");
        String host = parameterService.findParameterByKey("cerberus_smtp_host", system).getValue();
        int port = Integer.valueOf(parameterService.findParameterByKey("cerberus_smtp_port", system).getValue());
        String userName = parameterService.findParameterByKey("cerberus_smtp_username", system).getValue();
        String password = parameterService.findParameterByKey("cerberus_smtp_password", system).getValue();
        String subject = parameterService.getParameterStringByKey("cerberus_notification_executionstart_subject", system, "Empty Subject. Please define parameter 'cerberus_notification_executionstart_subject'.");
        String body = parameterService.getParameterStringByKey("cerberus_notification_executionstart_body", system, "Empty Body. Please define parameter 'cerberus_notification_executionstart_body'.");
        boolean isSetTls = parameterService.getParameterBooleanByKey("cerberus_smtp_isSetTls", system, true);

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", system, "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", system, "");
        }

        StringBuilder urlTestCase = new StringBuilder();
        urlTestCase.append(cerberusUrl);
        urlTestCase.append("/TestCaseExecution.jsp?executionId=");
        urlTestCase.append(exe.getId());

        body = body.replace("%TAG%", exe.getTag());
        body = body.replace("%URLEXECUTION%", urlTestCase.toString());
        body = body.replace("%EXECUTIONID%", String.valueOf(exe.getId()));
        body = body.replace("%COUNTRY%", exe.getCountry());
        body = body.replace("%ENVIRONMENT%", exe.getEnvironment());
        body = body.replace("%ROBOT%", exe.getRobot());
        body = body.replace("%ROBOTDECLINATION%", exe.getRobotDecli());
        body = body.replace("%TESTFOLDER%", exe.getTest());
        body = body.replace("%TESTCASE%", exe.getTestCase());

        subject = subject.replace("%TAG%", exe.getTag());
        subject = subject.replace("%EXECUTIONID%", String.valueOf(exe.getId()));
        subject = subject.replace("%COUNTRY%", exe.getCountry());
        subject = subject.replace("%ENVIRONMENT%", exe.getEnvironment());
        subject = subject.replace("%ROBOT%", exe.getRobot());
        subject = subject.replace("%ROBOTDECLINATION%", exe.getRobotDecli());
        subject = subject.replace("%TESTFOLDER%", exe.getTest());
        subject = subject.replace("%TESTCASE%", exe.getTestCase());

        email = emailFactory.create(host, port, userName, password, isSetTls, subject, body, from, to, null);

        LOG.debug(subject);
        LOG.debug(body);
        return email;

    }

    @Override
    public Email generateNotifyEndExecution(TestCaseExecution exe, String to) throws Exception {
        Email email = new Email();
        String system = "";

        String from = parameterService.getParameterStringByKey("cerberus_notification_from", system, "Cerberus <no.reply@cerberus-testing.org>");
        String host = parameterService.findParameterByKey("cerberus_smtp_host", system).getValue();
        int port = Integer.valueOf(parameterService.findParameterByKey("cerberus_smtp_port", system).getValue());
        String userName = parameterService.findParameterByKey("cerberus_smtp_username", system).getValue();
        String password = parameterService.findParameterByKey("cerberus_smtp_password", system).getValue();
        String subject = parameterService.getParameterStringByKey("cerberus_notification_executionend_subject", system, "Empty Subject. Please define parameter 'cerberus_notification_executionend_subject'.");
        String body = parameterService.getParameterStringByKey("cerberus_notification_executionend_body", system, "Empty Body. Please define parameter 'cerberus_notification_executionend_body'.");
        boolean isSetTls = parameterService.getParameterBooleanByKey("cerberus_smtp_isSetTls", system, true);

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", system, "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", system, "");
        }

        StringBuilder urlTestCase = new StringBuilder();
        urlTestCase.append(cerberusUrl);
        urlTestCase.append("/TestCaseExecution.jsp?executionId=");
        urlTestCase.append(exe.getId());

        body = body.replace("%TAG%", exe.getTag());
        body = body.replace("%URLEXECUTION%", urlTestCase.toString());
        body = body.replace("%EXECUTIONID%", String.valueOf(exe.getId()));
        body = body.replace("%COUNTRY%", exe.getCountry());
        body = body.replace("%ENVIRONMENT%", exe.getEnvironment());
        body = body.replace("%ROBOT%", exe.getRobot());
        body = body.replace("%ROBOTDECLINATION%", exe.getRobotDecli());
        body = body.replace("%TESTFOLDER%", exe.getTest());
        body = body.replace("%TESTCASE%", exe.getTestCase());
        body = body.replace("%STATUS%", exe.getControlStatus());

        subject = subject.replace("%TAG%", exe.getTag());
        subject = subject.replace("%EXECUTIONID%", String.valueOf(exe.getId()));
        subject = subject.replace("%COUNTRY%", exe.getCountry());
        subject = subject.replace("%ENVIRONMENT%", exe.getEnvironment());
        subject = subject.replace("%ROBOT%", exe.getRobot());
        subject = subject.replace("%ROBOTDECLINATION%", exe.getRobotDecli());
        subject = subject.replace("%TESTFOLDER%", exe.getTest());
        subject = subject.replace("%TESTCASE%", exe.getTestCase());
        subject = subject.replace("%STATUS%", exe.getControlStatus());

        email = emailFactory.create(host, port, userName, password, isSetTls, subject, body, from, to, null);

        LOG.debug(subject);
        LOG.debug(body);
        return email;

    }

    @Override
    public Email generateNotifyTestCaseChange(TestCase testCase, String to, String eventReference) throws Exception {
        Email email = new Email();
        String system = "";

        String from = parameterService.getParameterStringByKey("cerberus_notification_from", system, "Cerberus <no.reply@cerberus-testing.org>");
        String host = parameterService.findParameterByKey("cerberus_smtp_host", system).getValue();
        int port = Integer.valueOf(parameterService.findParameterByKey("cerberus_smtp_port", system).getValue());
        String userName = parameterService.findParameterByKey("cerberus_smtp_username", system).getValue();
        String password = parameterService.findParameterByKey("cerberus_smtp_password", system).getValue();
//        String subject = parameterService.getParameterStringByKey("cerberus_notification_testcasechange_subject", system, "Empty Subject. Please define parameter 'cerberus_notification_testcasechange_subject'.");
//        String body = parameterService.getParameterStringByKey("cerberus_notification_testcasechange_body", system, "Empty Body. Please define parameter 'cerberus_notification_testcasechange_body'.");
        String subject = "";
        String body = "";
        boolean isSetTls = parameterService.getParameterBooleanByKey("cerberus_smtp_isSetTls", system, true);

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", system, "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", system, "");
        }

        StringBuilder urlTestCase = new StringBuilder();
        urlTestCase.append(cerberusUrl);
        urlTestCase.append("/TestCaseScript.jsp?test=");
        urlTestCase.append(StringUtil.encodeURL(testCase.getTest()));
        urlTestCase.append("&testcase=");
        urlTestCase.append(StringUtil.encodeURL(testCase.getTestcase()));

        switch (eventReference) {
            case EventHook.EVENTREFERENCE_TESTCASE_CREATE:
                subject = "[Cerberus] TestCase '%TESTFOLDER% - %TESTCASE%' was Created.";
                body = "Hello,<br><br>Testcase <b>%TESTFOLDER% - %TESTCASE%</b> was just created.<br><br>You can access it <a href=\"%TESTCASEURL%\">here</a>.";
                break;
            case EventHook.EVENTREFERENCE_TESTCASE_DELETE:
                subject = "[Cerberus] TestCase '%TESTFOLDER% - %TESTCASE%' was Deleted.";
                body = "Hello,<br><br>Testcase <b>%TESTFOLDER% - %TESTCASE%</b> was just deleted.";
                break;
            case EventHook.EVENTREFERENCE_TESTCASE_UPDATE:
                subject = "[Cerberus] TestCase '%TESTFOLDER% - %TESTCASE%' was Updated to version %TESTCASEVERSION%.";
                body = "Hello,<br><br>Testcase <b>%TESTFOLDER% - %TESTCASE%</b> was just updated to version %TESTCASEVERSION%.<br><br>You can access it <a href=\"%TESTCASEURL%\">here</a>.";
                break;
        }

        body = body.replace("%TESTFOLDER%", testCase.getTest());
        body = body.replace("%TESTCASE%", testCase.getTestcase());
        body = body.replace("%STATUS%", testCase.getStatus());
        body = body.replace("%TESTCASEURL%", urlTestCase);
        body = body.replace("%TESTCASEVERSION%", String.valueOf(testCase.getVersion()));

        subject = subject.replace("%TESTFOLDER%", testCase.getTest());
        subject = subject.replace("%TESTCASE%", testCase.getTestcase());
        subject = subject.replace("%STATUS%", testCase.getStatus());
        subject = subject.replace("%TESTCASEURL%", urlTestCase);
        subject = subject.replace("%TESTCASEVERSION%", String.valueOf(testCase.getVersion()));

        email = emailFactory.create(host, port, userName, password, isSetTls, subject, body, from, to, null);

        LOG.debug(subject);
        LOG.debug(body);
        return email;

    }
}
