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
package org.cerberus.core.service.notifications.teams.impl;

import org.cerberus.core.crud.entity.EventHook;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.service.notifications.teams.ITeamsGenerationService;
import org.cerberus.core.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;

/**
 * @author vertigo17
 */
@Service
public class TeamsGenerationService implements ITeamsGenerationService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(TeamsGenerationService.class);
    private static final String IMAGES_URL = "https://vm.cerberus-testing.org/notifications/status-%STATUS%.png";

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private ITagService tagService;

    @Override
    public JSONObject generateNotifyStartTagExecution(Tag tag) throws UnsupportedEncodingException, Exception {

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }
        cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");
        cerberusUrl += "ReportingExecutionByTag.jsp?Tag=" + StringUtil.encodeURL(tag.getTag());

        JSONObject teamsMessage = new JSONObject();

        teamsMessage.put("@type", "MessageCard");
        teamsMessage.put("@context", "https://schema.org/extensions");
        teamsMessage.put("themeColor", "0078D7");
        teamsMessage.put("title", "Execution Tag '" + tag.getTag() + "' Started.");
        teamsMessage.put("summary", "Pending...");

        JSONArray sections = new JSONArray();
        JSONObject sectionsObj = new JSONObject();
        sections.put(sectionsObj);
        teamsMessage.put("sections", sections);

        JSONArray actions = new JSONArray();
        JSONObject actionsObj = new JSONObject();
        actionsObj.put("@type", "OpenUri");
        actionsObj.put("name", "View in Cerberus");
        JSONArray targets = new JSONArray();
        JSONObject target = new JSONObject();
        target.put("os", "default");
        target.put("uri", cerberusUrl);
        targets.put(target);
        actionsObj.put("targets", targets);
        actions.put(actionsObj);
        teamsMessage.put("potentialAction", actions);

        LOG.debug(teamsMessage.toString(1));
        return teamsMessage;

    }

    @Override
    public JSONObject generateNotifyEndTagExecution(Tag tag) throws UnsupportedEncodingException, Exception {

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }
        cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");
        cerberusUrl += "ReportingExecutionByTag.jsp?Tag=" + StringUtil.encodeURL(tag.getTag());

        JSONObject teamsMessage = new JSONObject();

        teamsMessage.put("@type", "MessageCard");
        teamsMessage.put("@context", "https://schema.org/extensions");
        teamsMessage.put("themeColor", "0078D7");
        teamsMessage.put("title", "Execution Tag '" + tag.getTag() + "' Ended.");
        teamsMessage.put("summary", tagService.formatResult(tag));

        JSONArray sections = new JSONArray();
        JSONObject sectionsObj = new JSONObject();
        if ("OK".equalsIgnoreCase(tag.getCiResult())) {
            sectionsObj.put("activityTitle", "Campaign successfully Executed. CI Score = " + tag.getCiScore() + " < " + tag.getCiScoreThreshold());
        } else {
            sectionsObj.put("activityTitle", "Campaign failed. CI Score = " + tag.getCiScore() + " (>= " + tag.getCiScoreThreshold() + ")");
        }
        sectionsObj.put("activityImage", IMAGES_URL.replace("%STATUS%", tag.getCiResult()));
        sectionsObj.put("text", tagService.formatResult(tag));
        sectionsObj.put("activitySubtitle", tag.getDateEndQueue().toString());
        sections.put(sectionsObj);
        teamsMessage.put("sections", sections);

        JSONArray actions = new JSONArray();
        JSONObject actionsObj = new JSONObject();
        actionsObj.put("@type", "OpenUri");
        actionsObj.put("name", "View in Cerberus");
        JSONArray targets = new JSONArray();
        JSONObject target = new JSONObject();
        target.put("os", "default");
        target.put("uri", cerberusUrl);
        targets.put(target);
        actionsObj.put("targets", targets);
        actions.put(actionsObj);
        teamsMessage.put("potentialAction", actions);

        LOG.debug(teamsMessage.toString(1));
        return teamsMessage;

    }

    @Override
    public JSONObject generateNotifyStartExecution(TestCaseExecution exe) throws Exception {

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }
        cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");
        cerberusUrl += "TestCaseExecution.jsp?executionId=" + exe.getId();

        JSONObject teamsMessage = new JSONObject();

        teamsMessage.put("@type", "MessageCard");
        teamsMessage.put("@context", "https://schema.org/extensions");
        teamsMessage.put("themeColor", "0078D7");
        teamsMessage.put("title", "Execution " + exe.getId() + " Started.");
        String summary = "Testcase '" + exe.getTest() + " - " + exe.getTestCase() + "' on " + exe.getEnvironment() + " - " + exe.getCountry();
        if (StringUtil.isEmptyOrNull(exe.getRobotDecli())) {
            summary += exe.getRobotDecli();
        }
        teamsMessage.put("summary", summary);

        JSONArray sections = new JSONArray();
        JSONObject sectionsObj = new JSONObject();
        sections.put(sectionsObj);
        teamsMessage.put("sections", sections);

        JSONArray actions = new JSONArray();
        JSONObject actionsObj = new JSONObject();
        actionsObj.put("@type", "OpenUri");
        actionsObj.put("name", "View in Cerberus");
        JSONArray targets = new JSONArray();
        JSONObject target = new JSONObject();
        target.put("os", "default");
        target.put("uri", cerberusUrl);
        targets.put(target);
        actionsObj.put("targets", targets);
        actions.put(actionsObj);
        teamsMessage.put("potentialAction", actions);

        LOG.debug(teamsMessage.toString(1));
        return teamsMessage;

    }

    @Override
    public JSONObject generateNotifyEndExecution(TestCaseExecution exe) throws Exception {

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }
        cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");
        cerberusUrl += "TestCaseExecution.jsp?executionId=" + exe.getId();

        JSONObject teamsMessage = new JSONObject();

        teamsMessage.put("@type", "MessageCard");
        teamsMessage.put("@context", "https://schema.org/extensions");
        teamsMessage.put("themeColor", "0078D7");
        teamsMessage.put("title", "Execution '" + exe.getId() + "' Ended.");
        String summary = "Testcase '" + exe.getTest() + " - " + exe.getTestCase() + "' on " + exe.getEnvironment() + " - " + exe.getCountry();
        if (StringUtil.isEmptyOrNull(exe.getRobotDecli())) {
            summary += exe.getRobotDecli();
        }
        teamsMessage.put("summary", summary);

        JSONArray sections = new JSONArray();
        JSONObject sectionsObj = new JSONObject();
        if ("OK".equalsIgnoreCase(exe.getControlStatus())) {
            sectionsObj.put("activityTitle", "Execution successful !");
        } else {
            sectionsObj.put("activityTitle", "Execution failed. Status = " + exe.getControlStatus() + " - " + exe.getControlMessage());
        }
        sectionsObj.put("activityImage", IMAGES_URL.replace("%STATUS%", exe.getControlStatus()));
//        sectionsObj.put("text", "");
        sectionsObj.put("activitySubtitle", new Timestamp(exe.getEnd()).toString());
        sections.put(sectionsObj);
        teamsMessage.put("sections", sections);

        JSONArray actions = new JSONArray();
        JSONObject actionsObj = new JSONObject();
        actionsObj.put("@type", "OpenUri");
        actionsObj.put("name", "View in Cerberus");
        JSONArray targets = new JSONArray();
        JSONObject target = new JSONObject();
        target.put("os", "default");
        target.put("uri", cerberusUrl);
        targets.put(target);
        actionsObj.put("targets", targets);
        actions.put(actionsObj);
        teamsMessage.put("potentialAction", actions);

        LOG.debug(teamsMessage.toString(1));
        return teamsMessage;

    }

    @Override
    public JSONObject generateNotifyTestCaseChange(TestCase testCase, String eventReference) throws Exception {

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }
        cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");
        cerberusUrl += "TestCaseScript.jsp?test=" + testCase.getTest() + "&testcase=" + testCase.getTestcase();

        JSONObject teamsMessage = new JSONObject();

        teamsMessage.put("@type", "MessageCard");
        teamsMessage.put("@context", "https://schema.org/extensions");
        teamsMessage.put("themeColor", "0078D7");
        switch (eventReference) {
            case EventHook.EVENTREFERENCE_TESTCASE_CREATE:
                teamsMessage.put("title", "Testcase '" + testCase.getTest() + " - " + testCase.getTestcase() + "' was Created.");
                teamsMessage.put("summary", testCase.getDescription());
                break;
            case EventHook.EVENTREFERENCE_TESTCASE_DELETE:
                teamsMessage.put("title", "Testcase '" + testCase.getTest() + " - " + testCase.getTestcase() + "' was Deleted.");
                teamsMessage.put("summary", testCase.getDescription());
                break;
            case EventHook.EVENTREFERENCE_TESTCASE_UPDATE:
                teamsMessage.put("title", "Testcase '" + testCase.getTest() + " - " + testCase.getTestcase() + "' was Updated to version " + testCase.getVersion() + ".");
                teamsMessage.put("summary", testCase.getDescription());
                break;
        }

        JSONArray sections = new JSONArray();
        JSONObject sectionsObj = new JSONObject();
        sections.put(sectionsObj);
        teamsMessage.put("sections", sections);

        if (!eventReference.equals(EventHook.EVENTREFERENCE_TESTCASE_DELETE)) {
            JSONArray actions = new JSONArray();
            JSONObject actionsObj = new JSONObject();
            actionsObj.put("@type", "OpenUri");
            actionsObj.put("name", "View in Cerberus");
            JSONArray targets = new JSONArray();
            JSONObject target = new JSONObject();
            target.put("os", "default");
            target.put("uri", cerberusUrl);
            targets.put(target);
            actionsObj.put("targets", targets);
            actions.put(actionsObj);
            teamsMessage.put("potentialAction", actions);
        }

        LOG.debug(teamsMessage.toString(2));
        return teamsMessage;

    }

}
