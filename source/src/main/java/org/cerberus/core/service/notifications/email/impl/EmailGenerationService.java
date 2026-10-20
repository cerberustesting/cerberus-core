/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.service.notifications.email.IEmailBuildContentService;
import org.cerberus.core.service.notifications.email.IEmailFactory;
import org.cerberus.core.service.notifications.email.IEmailGenerationService;
import org.cerberus.core.service.notifications.email.IEmailRenderer;
import org.cerberus.core.service.notifications.email.ISmtpConfigService;
import org.cerberus.core.service.notifications.email.entity.Email;
import org.cerberus.core.util.StringUtil;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Builds the notification emails : the data of each mail is gathered here, the
 * look is defined by the Thymeleaf templates (resources/emails) and the email theme.
 * Subject, from, to and cc remain defined by the cerberus_notification_* parameters.
 */
@Service
public class EmailGenerationService implements IEmailGenerationService {

    private static final Logger LOG = LogManager.getLogger(EmailGenerationService.class);

    private static final String DEFAULT_FROM = "Cerberus <no.reply@cerberus-testing.org>";
    // Control messages can be very long (stack traces...) : only the beginning is displayed in the mail.
    private static final int CONTROL_MESSAGE_MAX_LENGTH = 400;
    private static final DateTimeFormatter TAGEND_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private ICountryEnvParamService countryEnvParamService;
    @Autowired
    private IBatchInvariantService batchInvariantService;
    @Autowired
    private ITestCaseExecutionService testCaseExecutionService;
    @Autowired
    private IEmailFactory emailFactory;
    @Autowired
    private ISmtpConfigService smtpConfigService;
    @Autowired
    private IEmailRenderer emailRenderer;
    @Autowired
    private IEmailBuildContentService emailBuildContentService;

    @Override
    public Email generateRevisionChangeEmail(String system, String country, String env, String build, String revision) throws Exception {
        CountryEnvParam countryEnvParam = countryEnvParamService.convert(countryEnvParamService.readByKey(system, country, env));

        String from = parameterService.findParameterByKey("cerberus_smtp_from", system).getValue();
        String to = parameterService.findParameterByKey("cerberus_notification_newbuildrevision_to", system).getValue();
        String cc = parameterService.findParameterByKey("cerberus_notification_newbuildrevision_cc", system).getValue();
        String subject = parameterService.findParameterByKey("cerberus_notification_newbuildrevision_subject", system).getValue();
        if (StringUtil.isNotEmptyOrNULLString(countryEnvParam.getDistribList())) {
            to = countryEnvParam.getDistribList();
        }

        subject = fill(subject, "%SYSTEM%", system, "%COUNTRY%", country, "%ENV%", env, "%BUILD%", build, "%REVISION%", revision);

        Map<String, Object> model = new HashMap<>();
        model.put("system", system);
        model.put("country", country);
        model.put("environment", env);
        model.put("build", build);
        model.put("revision", revision);
        model.put("lastBuild", countryEnvParam.getBuild());
        model.put("lastRevision", countryEnvParam.getRevision());
        model.put("buildContent", emailBuildContentService.read(system, build, revision, countryEnvParam.getRevision()));

        String body = emailRenderer.render("revision-change", "Build " + build + " / revision " + revision + " available in " + env, model, Locale.ENGLISH, system);
        return emailFactory.create(smtpConfigService.read(system), subject, body, from, to, cc);
    }

    @Override
    public Email generateDisableEnvEmail(String system, String country, String env) throws Exception {
        CountryEnvParam countryEnvParam = countryEnvParamService.convert(countryEnvParamService.readByKey(system, country, env));

        String from = parameterService.findParameterByKey("cerberus_smtp_from", system).getValue();
        String to = parameterService.findParameterByKey("cerberus_notification_disableenvironment_to", system).getValue();
        String cc = parameterService.findParameterByKey("cerberus_notification_disableenvironment_cc", system).getValue();
        String subject = parameterService.findParameterByKey("cerberus_notification_disableenvironment_subject", system).getValue();
        if (StringUtil.isNotEmptyOrNULLString(countryEnvParam.getDistribList())) {
            to = countryEnvParam.getDistribList();
        }

        subject = fill(subject, "%SYSTEM%", system, "%COUNTRY%", country, "%ENV%", env,
                "%BUILD%", countryEnvParam.getBuild(), "%REVISION%", countryEnvParam.getRevision());

        Map<String, Object> model = new HashMap<>();
        model.put("system", system);
        model.put("country", country);
        model.put("environment", env);
        model.put("build", countryEnvParam.getBuild());
        model.put("revision", countryEnvParam.getRevision());

        String body = emailRenderer.render("disable-environment", "Environment " + env + " disabled", model, Locale.ENGLISH, system);
        return emailFactory.create(smtpConfigService.read(system), subject, body, from, to, cc);
    }

    @Override
    public Email generateNewChainEmail(String system, String country, String env, String chain) throws Exception {
        CountryEnvParam countryEnvParam = countryEnvParamService.convert(countryEnvParamService.readByKey(system, country, env));

        BatchInvariant batchInvariant = batchInvariantService.convert(batchInvariantService.readByKey(chain));
        String lastChain = batchInvariant.getBatch() + " (" + batchInvariant.getDescription() + ")";

        String from = parameterService.findParameterByKey("cerberus_smtp_from", system).getValue();
        String to = parameterService.findParameterByKey("cerberus_notification_newchain_to", system).getValue();
        String cc = parameterService.findParameterByKey("cerberus_notification_newchain_cc", system).getValue();
        String subject = parameterService.findParameterByKey("cerberus_notification_newchain_subject", system).getValue();
        if (StringUtil.isNotEmptyOrNULLString(countryEnvParam.getDistribList())) {
            to = countryEnvParam.getDistribList();
        }

        subject = fill(subject, "%SYSTEM%", system, "%COUNTRY%", country, "%ENV%", env,
                "%BUILD%", countryEnvParam.getBuild(), "%REVISION%", countryEnvParam.getRevision(), "%CHAIN%", lastChain);

        Map<String, Object> model = new HashMap<>();
        model.put("system", system);
        model.put("country", country);
        model.put("environment", env);
        model.put("chain", lastChain);

        String body = emailRenderer.render("new-chain", "New chain executed in " + env, model, Locale.ENGLISH, system);
        return emailFactory.create(smtpConfigService.read(system), subject, body, from, to, cc);
    }

    @Override
    public Email generateAccountCreationEmail(User user) throws Exception {
        String system = "";

        String from = parameterService.findParameterByKey("cerberus_notification_accountcreation_from", system).getValue();
        String cc = parameterService.findParameterByKey("cerberus_notification_accountcreation_cc", system).getValue();
        String subject = parameterService.findParameterByKey("cerberus_notification_accountcreation_subject", system).getValue();

        Map<String, Object> model = new HashMap<>();
        model.put("name", user.getName());
        model.put("login", user.getLogin());
        model.put("password", parameterService.findParameterByKey("cerberus_accountcreation_defaultpassword", system).getValue());
        model.put("url", cerberusUrl());

        String body = emailRenderer.render("account-creation", "Your account has been created", model, Locale.ENGLISH, system);
        return emailFactory.create(smtpConfigService.read(system), subject, body, from, user.getEmail(), cc);
    }

    @Override
    public Email generateForgotPasswordEmail(User user) throws Exception {
        String system = "";

        String from = parameterService.findParameterByKey("cerberus_notification_accountcreation_from", system).getValue();
        String cc = parameterService.findParameterByKey("cerberus_notification_accountcreation_cc", system).getValue();
        String subject = parameterService.findParameterByKey("cerberus_notification_forgotpassword_subject", system).getValue();

        Map<String, Object> model = new HashMap<>();
        model.put("name", user.getName());
        model.put("login", user.getLogin());
        model.put("linkUrl", cerberusUrl() + "ChangePassword.jsp?login=" + StringUtil.encodeURL(user.getLogin())
                + "&confirmationToken=" + StringUtil.encodeURL(user.getResetPasswordToken()));

        String body = emailRenderer.render("forgot-password", "Reset your password", model, Locale.ENGLISH, system);
        return emailFactory.create(smtpConfigService.read(system), subject, body, from, user.getEmail(), cc);
    }

    @Override
    public Email generateNotifyStartTagExecution(Tag tag, String to) throws Exception {
        String system = "";

        String from = parameterService.getParameterStringByKey("cerberus_notification_tagexecutionstart_from", system, DEFAULT_FROM);
        String subject = parameterService.getParameterStringByKey("cerberus_notification_tagexecutionstart_subject", system, "Empty Subject. Please define parameter 'cerberus_notification_tagexecutionstart_subject'.");

        String environments = StringUtil.convertToString(new JSONArray(tag.getReqEnvironmentList()), ",");
        String countries = StringUtil.convertToString(new JSONArray(tag.getReqCountryList()), ",");

        subject = fill(subject, "%TAG%", tag.getTag(), "%CAMPAIGN%", tag.getCampaign(),
                "%REQENVIRONMENTLIST%", environments, "%REQCOUNTRYLIST%", countries);

        Map<String, Object> model = new HashMap<>();
        model.put("tag", tag.getTag());
        model.put("campaign", tag.getCampaign());
        model.put("environments", environments);
        model.put("countries", countries);
        model.put("urlReport", cerberusUrl() + "ReportingExecutionByTag.jsp?Tag=" + StringUtil.encodeURL(tag.getTag()));

        String body = emailRenderer.render("campaign-start", "Campaign " + tag.getCampaign() + " started", model, Locale.ENGLISH, system);
        return emailFactory.create(smtpConfigService.read(system), subject, body, from, to, null);
    }

    /**
     * Row of the status legend of the end of campaign email.
     */
    public record StatusRow(String status, String label, String color, int nb, int percent) {
    }

    /**
     * Tile of the "Report by status" of the end of campaign email.
     */
    public record StatusTile(String value, String label, String sub, String background, String border, String textColor) {
    }

    /**
     * Row of the non OK testcase table of the end of campaign email.
     */
    public record ExecutionRow(int priority, String test, String testcase, String environment, String country, String robotDecli,
            String status, String color, String textColor, String url, String description, String controlMessage) {
    }

    @Override
    public Email generateNotifyEndTagExecution(Tag tag, String to) throws Exception {
        String system = "";

        String from = parameterService.getParameterStringByKey("cerberus_notification_tagexecutionend_from", system, DEFAULT_FROM);
        String subject = parameterService.getParameterStringByKey("cerberus_notification_tagexecutionend_subject", system, "Empty Subject. Please define parameter 'cerberus_notification_tagexecutionend_subject'.");
        String cerberusUrl = StringUtil.addSuffixIfNotAlready(parameterService.getParameterStringCerberusURLByKey(), "/");

        String environments = StringUtil.convertToString(new JSONArray(tag.getEnvironmentList()), ",");
        String countries = StringUtil.convertToString(new JSONArray(tag.getCountryList()), ",");
        String applications = StringUtil.convertToString(new JSONArray(tag.getApplicationList()), ",");
        String systems = StringUtil.convertToString(new JSONArray(tag.getSystemList()), ",");
        String robots = StringUtil.convertToString(new JSONArray(tag.getRobotDecliList()), ",");

        // Report by status : same figures as the campaign report page.
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("OK", tag.getNbOK());
        counts.put("KO", tag.getNbKO());
        counts.put("FA", tag.getNbFA());
        counts.put("NA", tag.getNbNA());
        counts.put("NE", tag.getNbNE());
        counts.put("WE", tag.getNbWE());
        counts.put("PE", tag.getNbPE());
        counts.put("QU", tag.getNbQU());
        counts.put("QE", tag.getNbQE());
        counts.put("PA", tag.getNbPA());
        counts.put("CA", tag.getNbCA());
        int total = tag.getNbExeUsefull();
        List<StatusRow> statusRows = new ArrayList<>();
        List<DonutChartRenderer.Slice> slices = new ArrayList<>();
        for (String status : EmailStatusPalette.ORDER) {
            int nb = counts.get(status);
            if (nb > 0) {
                statusRows.add(new StatusRow(status, EmailStatusPalette.label(status), EmailStatusPalette.color(status), nb, percent(nb, total)));
                slices.add(new DonutChartRenderer.Slice(EmailStatusPalette.color(status), nb));
            }
        }
        int okPct = percent(counts.get("OK"), total);
        int notOk = counts.get("KO") + counts.get("FA") + counts.get("NA") + counts.get("CA");
        int pending = counts.get("QU") + counts.get("QE") + counts.get("PA") + counts.get("PE") + counts.get("WE") + counts.get("NE");

        List<StatusTile> tiles = new ArrayList<>();
        tiles.add(tile("blue", String.valueOf(total), "Total", ""));
        tiles.add(tile("green", String.valueOf(counts.get("OK")), "OK", okPct + "%"));
        tiles.add(tile("red", String.valueOf(notOk), "Not OK", percent(notOk, total) + "%"));
        if (pending > 0) {
            tiles.add(tile("orange", String.valueOf(pending), "Pending", percent(pending, total) + "%"));
        }
        if (tag.getNbFlaky() > 0) {
            tiles.add(tile("amber", String.valueOf(tag.getNbFlaky()), "Flaky", ""));
        }
        if (!StringUtil.isEmptyOrNull(tag.getCiResult())) {
            tiles.add(tile("OK".equals(tag.getCiResult()) ? "green" : "KO".equals(tag.getCiResult()) ? "red" : "slate",
                    tag.getCiResult(), "CI result", tag.getCiScore() + "/" + tag.getCiScoreThreshold()));
        }

        List<TestCaseExecution> testCaseExecutions = testCaseExecutionService.readLastExecutionAndExecutionInQueueByTag(tag.getTag());
        Collections.sort(testCaseExecutions, new SortExecution());
        int maxLines = parameterService.getParameterIntegerByKey("cerberus_notification_tagexecutionend_tclistmax", system, 100);
        List<ExecutionRow> executionRows = new ArrayList<>();
        int notOkTotal = 0;
        for (TestCaseExecution execution : testCaseExecutions) {
            if (!TestCaseExecution.CONTROLSTATUS_OK.equals(execution.getControlStatus())) {
                if (notOkTotal < maxLines) {
                    String url = execution.getId() > 0 ? cerberusUrl + "TestCaseExecution.jsp?executionId=" + execution.getId() : "";
                    executionRows.add(new ExecutionRow(execution.getTestCasePriority(), execution.getTest(), execution.getTestCase(),
                            execution.getEnvironment(), execution.getCountry(), execution.getRobotDecli(),
                            execution.getControlStatus(), EmailStatusPalette.color(execution.getControlStatus()),
                            EmailStatusPalette.textColor(execution.getControlStatus()), url, execution.getDescription(),
                            shorten(execution.getControlMessage(), CONTROL_MESSAGE_MAX_LENGTH)));
                }
                notOkTotal++;
            }
        }

        // The donut is a picture : if it cannot be drawn the mail is sent without it, tiles and legend carry the figures.
        byte[] donut = null;
        try {
            donut = DonutChartRenderer.render(slices, okPct + "%", "OK RATE", rateColor(okPct));
        } catch (Exception | LinkageError e) {
            LOG.warn("Could not draw the donut of the end of campaign email : " + e);
        }

        Map<String, Object> model = new HashMap<>();
        model.put("tag", tag.getTag());
        model.put("campaign", tag.getCampaign());
        model.put("urlReport", cerberusUrl + "ReportingExecutionByTag.jsp?Tag=" + StringUtil.encodeURL(tag.getTag()));
        // Icon of the title : green when the CI is OK, red when KO, orange when undetermined.
        model.put("titleColor", "OK".equals(tag.getCiResult()) ? EmailStatusPalette.color("OK")
                : "KO".equals(tag.getCiResult()) ? EmailStatusPalette.color("KO") : EmailStatusPalette.color("FA"));
        model.put("start", tag.getDateCreated() == null ? "" : tag.getDateCreated().toLocalDateTime().format(TAGEND_DATE_FORMAT));
        model.put("end", tag.getDateEndQueue() == null ? "" : tag.getDateEndQueue().toLocalDateTime().format(TAGEND_DATE_FORMAT));
        model.put("durationMin", tag.getDateCreated() == null || tag.getDateEndQueue() == null ? null
                : (tag.getDateEndQueue().getTime() - tag.getDateCreated().getTime()) / 60000);
        model.put("environments", environments);
        model.put("countries", countries);
        model.put("applications", applications);
        model.put("systems", systems);
        model.put("robots", robots);
        model.put("tiles", tiles);
        model.put("statusRows", statusRows);
        model.put("totalExecutions", total);
        model.put("hasDonut", donut != null);
        model.put("donutSize", DonutChartRenderer.DISPLAY_SIZE);
        model.put("executionRows", executionRows);
        model.put("notOkTotal", notOkTotal);
        model.put("maxLines", maxLines);

        subject = fill(subject, "%TAG%", tag.getTag(), "%CAMPAIGN%", tag.getCampaign(), "%ENVIRONMENTLIST%", environments,
                "%COUNTRYLIST%", countries, "%APPLICATIONLIST%", applications, "%SYSTEMLIST%", systems, "%ROBOTDECLILIST%", robots);

        String body = emailRenderer.render("campaign-end", "Campaign " + tag.getCampaign() + " finished", model, Locale.ENGLISH, system);
        Email email = emailFactory.create(smtpConfigService.read(system), subject, body, from, to, null);
        if (donut != null) {
            email.addInlineImage("donut", donut);
        }
        return email;
    }

    // Color of the OK rate at the center of the donut, like on the campaign report page : green when everything is OK,
    // orange from 50%, red below. Medium tones (tailwind -600) : the image is readable on a light and on a dark background.
    private static String rateColor(int okPct) {
        return okPct >= 100 ? "#16a34a" : okPct >= 50 ? "#d97706" : "#dc2626";
    }

    private static String shorten(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength).trim() + "...";
    }

    private static int percent(int part, int total) {
        return total > 0 ? Math.round(part * 100f / total) : 0;
    }

    // Tile colors : the tailwind -50 background, -200 border and -600/700 value of the tone.
    private static StatusTile tile(String tone, String value, String label, String sub) {
        switch (tone) {
            case "blue":
                return new StatusTile(value, label, sub, "#eff6ff", "#bfdbfe", "#2563eb");
            case "green":
                return new StatusTile(value, label, sub, "#f0fdf4", "#bbf7d0", "#16a34a");
            case "red":
                return new StatusTile(value, label, sub, "#fef2f2", "#fecaca", "#dc2626");
            case "orange":
                return new StatusTile(value, label, sub, "#fff7ed", "#fed7aa", "#ea580c");
            case "amber":
                return new StatusTile(value, label, sub, "#fffbeb", "#fde68a", "#b45309");
            default:
                return new StatusTile(value, label, sub, "#f8fafc", "#e2e8f0", "#475569");
        }
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
        String system = "";

        String from = parameterService.getParameterStringByKey("cerberus_notification_from", system, DEFAULT_FROM);
        String subject = parameterService.getParameterStringByKey("cerberus_notification_executionstart_subject", system, "Empty Subject. Please define parameter 'cerberus_notification_executionstart_subject'.");

        subject = fillExecution(subject, exe);

        String body = emailRenderer.render("execution-start", "Execution " + exe.getId() + " started", executionModel(exe), Locale.ENGLISH, system);
        return emailFactory.create(smtpConfigService.read(system), subject, body, from, to, null);
    }

    @Override
    public Email generateNotifyEndExecution(TestCaseExecution exe, String to) throws Exception {
        String system = "";

        String from = parameterService.getParameterStringByKey("cerberus_notification_from", system, DEFAULT_FROM);
        String subject = parameterService.getParameterStringByKey("cerberus_notification_executionend_subject", system, "Empty Subject. Please define parameter 'cerberus_notification_executionend_subject'.");

        subject = fillExecution(subject, exe);
        subject = fill(subject, "%STATUS%", exe.getControlStatus());

        Map<String, Object> model = executionModel(exe);
        model.put("status", exe.getControlStatus());
        String statusColor = EmailStatusPalette.color(exe.getControlStatus());
        model.put("statusColor", statusColor);
        model.put("statusTextColor", EmailStatusPalette.textColor(exe.getControlStatus()));
        model.put("titleColor", statusColor);

        String body = emailRenderer.render("execution-end", "Execution " + exe.getId() + " finished", model, Locale.ENGLISH, system);
        return emailFactory.create(smtpConfigService.read(system), subject, body, from, to, null);
    }

    @Override
    public Email generateNotifyTestCaseChange(TestCase testCase, String to, String eventReference) throws Exception {
        String system = "";

        String from = parameterService.getParameterStringByKey("cerberus_notification_from", system, DEFAULT_FROM);

        String subject;
        String title;
        switch (eventReference) {
            case EventHook.EVENTREFERENCE_TESTCASE_CREATE:
                subject = "[Cerberus] TestCase '%TESTFOLDER% - %TESTCASE%' was Created.";
                title = "Testcase created";
                break;
            case EventHook.EVENTREFERENCE_TESTCASE_DELETE:
                subject = "[Cerberus] TestCase '%TESTFOLDER% - %TESTCASE%' was Deleted.";
                title = "Testcase deleted";
                break;
            case EventHook.EVENTREFERENCE_TESTCASE_UPDATE:
                subject = "[Cerberus] TestCase '%TESTFOLDER% - %TESTCASE%' was Updated to version %TESTCASEVERSION%.";
                title = "Testcase updated";
                break;
            default:
                throw new IllegalArgumentException("Event '" + eventReference + "' is not a testcase change.");
        }

        String urlTestcase = "";
        if (!EventHook.EVENTREFERENCE_TESTCASE_DELETE.equals(eventReference)) {
            urlTestcase = cerberusUrl() + "TestCaseScript.jsp?test=" + StringUtil.encodeURL(testCase.getTest())
                    + "&testcase=" + StringUtil.encodeURL(testCase.getTestcase());
        }

        subject = fill(subject, "%TESTFOLDER%", testCase.getTest(), "%TESTCASE%", testCase.getTestcase(),
                "%STATUS%", testCase.getStatus(), "%TESTCASEVERSION%", String.valueOf(testCase.getVersion()));

        Map<String, Object> model = new HashMap<>();
        model.put("eventType", eventReference);
        model.put("test", testCase.getTest());
        model.put("testcase", testCase.getTestcase());
        model.put("status", testCase.getStatus());
        model.put("version", testCase.getVersion());
        model.put("urlTestcase", urlTestcase);
        if (EventHook.EVENTREFERENCE_TESTCASE_DELETE.equals(eventReference)) {
            model.put("titleColorName", "red");
        }

        String body = emailRenderer.render("testcase-change", title, model, Locale.ENGLISH, system);
        return emailFactory.create(smtpConfigService.read(system), subject, body, from, to, null);
    }

    private Map<String, Object> executionModel(TestCaseExecution exe) {
        Map<String, Object> model = new HashMap<>();
        model.put("executionId", exe.getId());
        model.put("tag", exe.getTag());
        model.put("test", exe.getTest());
        model.put("testcase", exe.getTestCase());
        model.put("environment", exe.getEnvironment());
        model.put("country", exe.getCountry());
        model.put("robot", exe.getRobot());
        model.put("robotDecli", exe.getRobotDecli());
        model.put("urlExecution", cerberusUrl() + "TestCaseExecution.jsp?executionId=" + exe.getId());
        return model;
    }

    private static String fillExecution(String subject, TestCaseExecution exe) {
        return fill(subject, "%TAG%", exe.getTag(), "%EXECUTIONID%", String.valueOf(exe.getId()), "%COUNTRY%", exe.getCountry(),
                "%ENVIRONMENT%", exe.getEnvironment(), "%ROBOT%", exe.getRobot(), "%ROBOTDECLINATION%", exe.getRobotDecli(),
                "%TESTFOLDER%", exe.getTest(), "%TESTCASE%", exe.getTestCase());
    }

    /**
     * Replace the %TOKEN% of a subject. Arguments are token / value pairs, a null value is replaced by an empty text.
     */
    private static String fill(String text, String... tokenValuePairs) {
        for (int i = 0; i + 1 < tokenValuePairs.length; i += 2) {
            text = text.replace(tokenValuePairs[i], tokenValuePairs[i + 1] == null ? "" : tokenValuePairs[i + 1]);
        }
        return text;
    }

    private String cerberusUrl() {
        return StringUtil.addSuffixIfNotAlready(parameterService.getParameterStringCerberusURLByKey(), "/");
    }
}
