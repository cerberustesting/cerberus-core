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
package org.cerberus.service.notifications.slack.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.cerberus.crud.entity.EventHook;
import org.cerberus.crud.entity.Tag;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITagService;
import org.cerberus.util.StringUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.service.notifications.slack.ISlackGenerationService;

/**
 *
 * @author vertigo17
 */
@Service
public class SlackGenerationService implements ISlackGenerationService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(SlackGenerationService.class);

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private ITagService tagService;

    @Override
    public JSONObject generateNotifyStartTagExecution(Tag tag, String channel) throws Exception {

        JSONObject slackMessage = new JSONObject();
        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isNullOrEmpty(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }
        cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");
        cerberusUrl += "ReportingExecutionByTag.jsp?Tag=" + URLEncoder.encode(tag.getTag(), StandardCharsets.UTF_8);
        slackMessage.put("text", "Execution Tag '" + tag.getTag() + "' Started. <" + cerberusUrl + "|Click here> for details.");
        if (!StringUtil.isNullOrEmpty(channel)) {
            slackMessage.put("channel", channel);
        }
        slackMessage.put("username", "Cerberus");

        LOG.debug(slackMessage.toString(1));
        return slackMessage;

    }

    @Override
    public JSONObject generateNotifyEndTagExecution(Tag tag, String channel) throws Exception {

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isNullOrEmpty(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }
        cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");
        cerberusUrl += "ReportingExecutionByTag.jsp?Tag=" + URLEncoder.encode(tag.getTag(), StandardCharsets.UTF_8);

        JSONObject slackMessage = new JSONObject();
        JSONObject attachementObj = new JSONObject();

        attachementObj.put("fallback", "Execution Tag '" + tag.getTag() + "' Ended. <" + cerberusUrl + "|Click here> for details.");
        attachementObj.put("pretext", "Execution Tag '" + tag.getTag() + "' Ended. <" + cerberusUrl + "|Click here> for details.");

        JSONObject slackattaMessage = new JSONObject();
        if ("OK".equalsIgnoreCase(tag.getCiResult())) {
            attachementObj.put("color", TestCaseExecution.CONTROLSTATUS_OK_COL);
            slackattaMessage.put("title", "Campaign successfully Executed. CI Score = " + tag.getCiScore() + " (< " + tag.getCiScoreThreshold() + ")");
        } else {
            attachementObj.put("color", TestCaseExecution.CONTROLSTATUS_KO_COL);
            slackattaMessage.put("title", "Campaign failed. CI Score = " + tag.getCiScore() + " >= " + tag.getCiScoreThreshold());

        }
        slackattaMessage.put("value", tagService.formatResult(tag));
        slackattaMessage.put("short", false);
        attachementObj.append("fields", slackattaMessage);

        slackMessage.append("attachments", attachementObj);

        if (!StringUtil.isNullOrEmpty(channel)) {
            slackMessage.put("channel", channel);
        }
        slackMessage.put("username", "Cerberus");

        LOG.debug(slackMessage.toString(1));
        return slackMessage;

    }

    @Override
    public JSONObject generateNotifyStartExecution(TestCaseExecution exe, String channel) throws Exception {

        JSONObject slackMessage = new JSONObject();
        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isNullOrEmpty(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }
        cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");
        cerberusUrl += "TestCaseExecution.jsp?executionId=" + exe.getId();
        slackMessage.put("text", "Execution '" + exe.getId() + "' Started. <" + cerberusUrl + "|Click here> for details.");
        if (!StringUtil.isNullOrEmpty(channel)) {
            slackMessage.put("channel", channel);
        }
        slackMessage.put("username", "Cerberus");

        LOG.debug(slackMessage.toString(1));
        return slackMessage;

    }

    @Override
    public JSONObject generateNotifyEndExecution(TestCaseExecution exe, String channel) throws Exception {

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isNullOrEmpty(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }
        cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");
        cerberusUrl += "TestCaseExecution.jsp?executionId=" + exe.getId();

        JSONObject slackMessage = new JSONObject();
        JSONObject attachementObj = new JSONObject();

        attachementObj.put("fallback", "Execution '" + exe.getId() + "' Ended. <" + cerberusUrl + "|Click here> for details.");
        attachementObj.put("pretext", "Execution '" + exe.getId() + "' Ended. <" + cerberusUrl + "|Click here> for details.");

        JSONObject slackattaMessage = new JSONObject();
        if ("OK".equalsIgnoreCase(exe.getControlStatus())) {
            attachementObj.put("color", TestCaseExecution.CONTROLSTATUS_OK_COL);
            slackattaMessage.put("title", "Execution successfully Executed. " + exe.getControlStatus());
        } else {
            attachementObj.put("color", TestCaseExecution.CONTROLSTATUS_KO_COL);
            slackattaMessage.put("title", "Execution failed. " + exe.getControlStatus() + " : " + exe.getControlMessage());

        }
        slackattaMessage.put("short", false);
        attachementObj.append("fields", slackattaMessage);

        slackMessage.append("attachments", attachementObj);

        if (!StringUtil.isNullOrEmpty(channel)) {
            slackMessage.put("channel", channel);
        }
        slackMessage.put("username", "Cerberus");

        LOG.debug(slackMessage.toString(1));
        return slackMessage;

    }

    @Override
    public JSONObject generateNotifyTestCaseChange(TestCase testCase, String channel, String eventReference) throws Exception {

        JSONObject slackMessage = new JSONObject();
        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isNullOrEmpty(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }
        cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");
        cerberusUrl += "TestCaseScript.jsp?test=" + URLEncoder.encode(testCase.getTest(), StandardCharsets.UTF_8) + "&testcase=" + URLEncoder.encode(testCase.getTestcase(), StandardCharsets.UTF_8);
        switch (eventReference) {
            case EventHook.EVENTREFERENCE_TESTCASE_CREATE:
                slackMessage.put("text", "Testcase '" + testCase.getTest() + " - " + testCase.getTestcase() + "' was Created. <" + cerberusUrl + "|Click here> for details.");
                break;
            case EventHook.EVENTREFERENCE_TESTCASE_DELETE:
                slackMessage.put("text", "Testcase '" + testCase.getTest() + " - " + testCase.getTestcase() + "' was Deleted.");
                break;
            case EventHook.EVENTREFERENCE_TESTCASE_UPDATE:
                slackMessage.put("text", "Testcase '" + testCase.getTest() + " - " + testCase.getTestcase() + "' was Updated to version " + testCase.getVersion() + ". <" + cerberusUrl + "|Click here> for details.");
                break;
        }
        if (!StringUtil.isNullOrEmpty(channel)) {
            slackMessage.put("channel", channel);
        }
        slackMessage.put("username", "Cerberus");

        LOG.debug(slackMessage.toString(1));
        return slackMessage;

    }

}
