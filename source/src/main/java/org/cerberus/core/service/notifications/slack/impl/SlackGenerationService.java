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
package org.cerberus.core.service.notifications.slack.impl;

import java.io.UnsupportedEncodingException;

import org.cerberus.core.crud.entity.EventHook;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.util.StringUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.core.service.notifications.slack.ISlackGenerationService;

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
    public JSONObject generateNotifyStartTagExecution(Tag tag, String channel) throws UnsupportedEncodingException, Exception {

        JSONObject slackMessage = new JSONObject();
        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }
        cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");
        cerberusUrl += "ReportingExecutionByTag.jsp?Tag=" + StringUtil.encodeURL(tag.getTag());
        slackMessage.put("text", "Execution Tag '" + tag.getTag() + "' Started. <" + cerberusUrl + "|Click here> for details.");
        if (!StringUtil.isEmptyOrNull(channel)) {
            slackMessage.put("channel", channel);
        }
        slackMessage.put("username", "Cerberus");

        LOG.debug(slackMessage.toString(1));
        return slackMessage;

    }

    @Override
    public JSONObject generateNotifyEndTagExecution(Tag tag, String channel) throws UnsupportedEncodingException, Exception {

        String jsonMessage = "{\n"
                + "	\"attachments\": [\n"
                + "		{\n"
                + "			\"color\": \"%COLOR%\",\n"
                + "			\"fallback\": \"%MESSAGE%\",\n"
                + "            \n"
                + "			\"blocks\": [\n"
                + "				{\n"
                + "					\"type\": \"section\",\n"
                + "					\"fields\": [\n"
                + "						{\n"
                + "							\"type\": \"mrkdwn\",\n"
                + "							\"text\": \"%MESSAGE%\\n*Campaign %STATUS-MESSAGE%. CI Score = %CISCORE% vs %CITHRESHOLD%*\\n%TAG-SUMMARY%\"\n"
                + "						}\n"
                + "					]\n"
                + "				}\n"
                + "			]\n"
                + "		}\n"
                + "	],\n"
                + "	\"username\": \"Cerberus\",\n"
                + "	\"channel\": \"%CHANNEL%\"\n"
                + "}";

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }
        cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");
        cerberusUrl += "ReportingExecutionByTag.jsp?Tag=" + StringUtil.encodeURL(tag.getTag());

        JSONObject slackMessage = new JSONObject();
        JSONObject attachementObj = new JSONObject();

        String message = "Execution Tag '" + tag.getTag() + "' Ended. <" + cerberusUrl + "|Click here> for details.";

        String slackStatus = "";
        String color = "";
        if ("OK".equalsIgnoreCase(tag.getCiResult())) {
            color = TestCaseExecution.CONTROLSTATUS_OK_COL_EXT;
            slackStatus = "successfully executed";
        } else {
            color = TestCaseExecution.CONTROLSTATUS_KO_COL_EXT;
            slackStatus = "failed";
        }

        jsonMessage = jsonMessage
                .replace("%COLOR%", color)
                .replace("%MESSAGE%", message)
                .replace("%CHANNEL%", channel)
                .replace("%CISCORE%", String.valueOf(tag.getCiScore()))
                .replace("%CITHRESHOLD%", String.valueOf(tag.getCiScoreThreshold()))
                .replace("%STATUS-MESSAGE%", slackStatus)
                .replace("%TAG-SUMMARY%", tagService.formatResult(tag));

        LOG.debug(jsonMessage);
        return new JSONObject(jsonMessage);

    }

    @Override
    public JSONObject generateNotifyStartExecution(TestCaseExecution exe, String channel) throws Exception {

        JSONObject slackMessage = new JSONObject();
        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }
        cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");
        cerberusUrl += "TestCaseExecution.jsp?executionId=" + exe.getId();
        slackMessage.put("text", "Execution '" + exe.getId() + "' Started. <" + cerberusUrl + "|Click here> for details.");
        if (!StringUtil.isEmptyOrNull(channel)) {
            slackMessage.put("channel", channel);
        }
        slackMessage.put("username", "Cerberus");

        LOG.debug(slackMessage.toString(1));
        return slackMessage;

    }

    @Override
    public JSONObject generateNotifyEndExecution(TestCaseExecution exe, String channel) throws Exception {

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
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
            attachementObj.put("color", TestCaseExecution.CONTROLSTATUS_OK_COL_EXT);
            slackattaMessage.put("title", "Execution successfully Executed. " + exe.getControlStatus());
        } else {
            attachementObj.put("color", TestCaseExecution.CONTROLSTATUS_KO_COL_EXT);
            slackattaMessage.put("title", "Execution failed. " + exe.getControlStatus() + " : " + exe.getControlMessage());

        }
        slackattaMessage.put("short", false);
        attachementObj.append("fields", slackattaMessage);

        slackMessage.append("attachments", attachementObj);

        if (!StringUtil.isEmptyOrNull(channel)) {
            slackMessage.put("channel", channel);
        }
        slackMessage.put("username", "Cerberus");

        LOG.debug(slackMessage.toString(1));
        return slackMessage;

    }

    @Override
    public JSONObject generateNotifyTestCaseChange(TestCase testCase, String channel, String eventReference) throws UnsupportedEncodingException, Exception {

        JSONObject slackMessage = new JSONObject();
        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }
        cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");
        cerberusUrl += "TestCaseScript.jsp?test=" + StringUtil.encodeURL(testCase.getTest()) + "&testcase=" + StringUtil.encodeURL(testCase.getTestcase());
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
        if (!StringUtil.isEmptyOrNull(channel)) {
            slackMessage.put("channel", channel);
        }
        slackMessage.put("username", "Cerberus");

        LOG.debug(slackMessage.toString(1));
        return slackMessage;

    }

}
