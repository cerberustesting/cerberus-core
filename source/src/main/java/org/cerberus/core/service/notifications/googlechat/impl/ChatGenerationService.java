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
package org.cerberus.core.service.notifications.googlechat.impl;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import org.cerberus.core.crud.entity.EventHook;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.core.service.notifications.googlechat.IChatGenerationService;

/**
 *
 * @author vertigo17
 */
@Service
public class ChatGenerationService implements IChatGenerationService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(ChatGenerationService.class);
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

        JSONObject chatMessage = new JSONObject();

        JSONArray cards = new JSONArray();
        JSONObject card = new JSONObject();

        JSONObject textContent = new JSONObject();
        textContent.put("text", "Execution Tag <b>'" + tag.getTag() + "'</b> Started.<br>Click <a href='" + cerberusUrl + "'>here</a> for details.");
        JSONObject textParaContent = new JSONObject();
        textParaContent.put("textParagraph", textContent);

        JSONArray widgets = new JSONArray();
        widgets.put(textParaContent);

        JSONArray sections = new JSONArray();
        JSONObject widget = new JSONObject();

        widget.put("widgets", widgets);
        sections.put(widget);
        card.put("sections", sections);

        cards.put(card);
        chatMessage.put("cards", cards);

        LOG.debug(chatMessage.toString(2));
        return chatMessage;

    }

    @Override
    public JSONObject generateNotifyEndTagExecution(Tag tag) throws UnsupportedEncodingException, Exception {

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }
        cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");

        cerberusUrl += "ReportingExecutionByTag.jsp?Tag=" + StringUtil.encodeURL(tag.getTag());

        JSONObject chatMessage = new JSONObject();

        JSONArray cards = new JSONArray();
        JSONObject card = new JSONObject();

        JSONObject textContent = new JSONObject();

        if ("OK".equalsIgnoreCase(tag.getCiResult())) {
            textContent.put("text", "Execution Tag <b>'" + tag.getTag() + "'</b> Ended.<br><b><font color=\"" + TestCaseExecution.CONTROLSTATUS_OK_COL_EXT + "\">Campaign successfully Executed. CI Score = " + tag.getCiScore() + " < " + tag.getCiScoreThreshold() + "</font></b><br>" + tagService.formatResult(tag) + "<br>Click <a href='" + cerberusUrl + "'>here</a> for details.");
        } else {
            textContent.put("text", "Execution Tag <b>'" + tag.getTag() + "'</b> Ended.<br><b><font color=\"" + TestCaseExecution.CONTROLSTATUS_KO_COL_EXT + "\">Campaign failed. CI Score = " + tag.getCiScore() + " (>= " + tag.getCiScoreThreshold() + ")</font></b><br>" + tagService.formatResult(tag) + "<br>Click <a href='" + cerberusUrl + "'>here</a> for details.");
        }

        JSONObject textParaContent = new JSONObject();
        textParaContent.put("textParagraph", textContent);

        JSONArray widgets = new JSONArray();
        widgets.put(textParaContent);

        JSONArray sections = new JSONArray();
        JSONObject widget = new JSONObject();

        widget.put("widgets", widgets);
        sections.put(widget);
        card.put("sections", sections);

        cards.put(card);
        chatMessage.put("cards", cards);

        LOG.debug(chatMessage.toString(1));
        return chatMessage;

    }

    @Override
    public JSONObject generateNotifyEndTagExecutionV2(Tag tag) throws UnsupportedEncodingException, Exception {

        int maxlines = parameterService.getParameterIntegerByKey("cerberus_notification_tagexecutionend_googlechat_maxexelines", "", 20);
        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }
        cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");

        String cerberusTagUrl = cerberusUrl + "ReportingExecutionByTag.jsp?Tag=" + StringUtil.encodeURL(tag.getTag());

        JSONObject chatMessage = new JSONObject();

        JSONArray cards = new JSONArray();
        JSONArray cardsV2 = new JSONArray();
        JSONObject card = new JSONObject();
        JSONObject cardV2 = new JSONObject();

        JSONObject textContent = new JSONObject();

        if ("OK".equalsIgnoreCase(tag.getCiResult())) {
            textContent.put("text", "<b><font color=\"" + TestCaseExecution.CONTROLSTATUS_OK_COL_EXT + "\">Campaign successfully Executed. CI Score = " + tag.getCiScore() + " < " + tag.getCiScoreThreshold() + "</font></b><br>" + tagService.formatResult(tag) + "<br>Click <a href='" + cerberusTagUrl + "'>here</a> for details.");
        } else {
            textContent.put("text", "<b><font color=\"" + TestCaseExecution.CONTROLSTATUS_KO_COL_EXT + "\">Campaign failed. CI Score = " + tag.getCiScore() + " (>= " + tag.getCiScoreThreshold() + ")</font></b><br>" + tagService.formatResult(tag) + "<br>Click <a href='" + cerberusTagUrl + "'>here</a> for details.");
        }

        JSONObject textParaContent = new JSONObject();
        textParaContent.put("textParagraph", textContent);

        JSONArray widgets = new JSONArray();
        widgets.put(textParaContent);

        String executionText = "";
        int totaldisplayed = 0;
        int totaltodisplay = 0;
        int totallines = 0;
        String cerberusExeUrl = "";
        for (TestCaseExecution execution : tag.getExecutionsNew()) {
            LOG.debug(execution.getControlStatus() + " - " + execution.getControlMessage() + execution.getApplication() + " - " + execution.getDescription());
            totallines++;
            if (!TestCaseExecution.CONTROLSTATUS_OK.equals(execution.getControlStatus()) && !execution.isTestCaseIsMuted()) {
                totaltodisplay++;
                if (maxlines > totaldisplayed) {
                    totaldisplayed++;
                    if (execution.getId() == 0) {
                        executionText += execution.getControlStatus() + " [" + execution.getApplication() + "|" + execution.getCountry() + "|" + execution.getEnvironment() + "] <i><font color=\"" + execution.getColor(execution.getControlStatus()) + "\">" + execution.getDescription() + "</font></i><br>";
                    } else {
                        cerberusExeUrl = cerberusUrl + "TestCaseExecution.jsp?executionId=" + execution.getId();
                        executionText += "<a href='" + cerberusExeUrl + "'>" + execution.getControlStatus() + "</a> [" + execution.getApplication() + "|" + execution.getCountry() + "|" + execution.getEnvironment() + "] <i><font color=\"" + execution.getColor(execution.getControlStatus()) + "\">" + execution.getDescription() + "</font></i><br>";
                    }
                }
            }

        }

        if (totaldisplayed < totaltodisplay) {
            executionText += "... Hidden more " + (totaltodisplay - totaldisplayed) + " line(s).";
        }

        JSONObject widget = new JSONObject();

        if (totaldisplayed > 0) {
            textContent = new JSONObject();
            textContent.put("text", executionText);

            textParaContent = new JSONObject();
            textParaContent.put("textParagraph", textContent);
            widgets.put(textParaContent);
            widget.put("widgets", widgets);
            widget.put("collapsible", true);
            widget.put("uncollapsibleWidgetsCount", 1);
        } else {
            widget.put("widgets", widgets);

        }

        widget.put("header", "Execution Tag <b>'" + tag.getTag() + "'</b> Ended.");

        JSONArray sections = new JSONArray();
        sections.put(widget);
        card.put("sections", sections);

        cards.put(card);

        cardV2.put("card", card);
        cardsV2.put(cardV2);
        chatMessage.put("cardsV2", cardsV2);

        LOG.debug(chatMessage.toString(3));
        return chatMessage;

    }

    @Override
    public JSONObject generateNotifyStartExecution(TestCaseExecution exe) throws Exception {

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }
        cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");
        cerberusUrl += "TestCaseExecution.jsp?executionId=" + exe.getId();

        JSONObject chatMessage = new JSONObject();

        JSONArray cards = new JSONArray();
        JSONObject card = new JSONObject();

        JSONObject textContent = new JSONObject();

        String summary = "Testcase '" + exe.getTest() + " - " + exe.getTestCase() + "' on " + exe.getEnvironment() + " - " + exe.getCountry();
        if (StringUtil.isEmptyOrNull(exe.getRobotDecli())) {
            summary += exe.getRobotDecli();
        }
        textContent.put("text", "Execution <b>" + exe.getId() + "</b> Started.<br>" + summary + "<br>Click <a href='" + cerberusUrl + "'>here</a> for details.");

        JSONObject textParaContent = new JSONObject();
        textParaContent.put("textParagraph", textContent);

        JSONArray widgets = new JSONArray();
        widgets.put(textParaContent);

        JSONArray sections = new JSONArray();
        JSONObject widget = new JSONObject();

        widget.put("widgets", widgets);
        sections.put(widget);
        card.put("sections", sections);

        cards.put(card);
        chatMessage.put("cards", cards);

        LOG.debug(chatMessage.toString(1));
        return chatMessage;

    }

    @Override
    public JSONObject generateNotifyEndExecution(TestCaseExecution exe) throws Exception {

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }
        cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");
        cerberusUrl += "TestCaseExecution.jsp?executionId=" + exe.getId();

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

        JSONObject chatMessage = new JSONObject();

        JSONArray cards = new JSONArray();
        JSONObject card = new JSONObject();

        JSONObject textContent = new JSONObject();

        String summary = "Testcase '" + exe.getTest() + " - " + exe.getTestCase() + "' on " + exe.getEnvironment() + " - " + exe.getCountry();
        if (StringUtil.isEmptyOrNull(exe.getRobotDecli())) {
            summary += exe.getRobotDecli();
        }

        if ("OK".equalsIgnoreCase(exe.getControlStatus())) {
            textContent.put("text", "Execution <b>" + exe.getId() + "</b> Ended.<br><font color=\"" + statColorMap.get(exe.getControlStatus()) + "\">Execution successful !</font><br>" + summary + "<br>Click <a href='" + cerberusUrl + "'>here</a> for details.");
        } else {
            textContent.put("text", "Execution <b>" + exe.getId() + "</b> Ended.<br><font color=\"" + statColorMap.get(exe.getControlStatus()) + "\">Status = " + exe.getControlStatus() + " - " + exe.getControlMessage() + "</font><br>" + summary + "<br>Click <a href='" + cerberusUrl + "'>here</a> for details.");
        }

        JSONObject textParaContent = new JSONObject();
        textParaContent.put("textParagraph", textContent);

        JSONArray widgets = new JSONArray();
        widgets.put(textParaContent);

        JSONArray sections = new JSONArray();
        JSONObject widget = new JSONObject();

        widget.put("widgets", widgets);
        sections.put(widget);
        card.put("sections", sections);

        cards.put(card);
        chatMessage.put("cards", cards);

        LOG.debug(chatMessage.toString(1));
        return chatMessage;

    }

    @Override
    public JSONObject generateNotifyTestCaseChange(TestCase testCase, String eventReference) throws Exception {

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isEmptyOrNull(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }
        cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");
        cerberusUrl += "TestCaseScript.jsp?test=" + testCase.getTest() + "&testcase=" + testCase.getTestcase();

        JSONObject chatMessage = new JSONObject();

        JSONArray cards = new JSONArray();
        JSONObject card = new JSONObject();

        JSONObject textContent = new JSONObject();

        switch (eventReference) {
            case EventHook.EVENTREFERENCE_TESTCASE_CREATE:
                textContent.put("text", "TestCase '" + testCase.getTest() + " - " + testCase.getTestcase() + "' was Created.<br>" + testCase.getDescription() + "<br>Click <a href='" + cerberusUrl + "'>here</a> for details.");
                break;
            case EventHook.EVENTREFERENCE_TESTCASE_DELETE:
                textContent.put("text", "TestCase '" + testCase.getTest() + " - " + testCase.getTestcase() + "' was Deleted.<br>" + testCase.getDescription());
                break;
            case EventHook.EVENTREFERENCE_TESTCASE_UPDATE:
                textContent.put("text", "TestCase '" + testCase.getTest() + " - " + testCase.getTestcase() + "' was Updated to version " + testCase.getVersion() + ".<br>" + testCase.getDescription() + "<br>Click <a href='" + cerberusUrl + "'>here</a> for details.");
                break;
        }

        JSONObject textParaContent = new JSONObject();
        textParaContent.put("textParagraph", textContent);

        JSONArray widgets = new JSONArray();
        widgets.put(textParaContent);

        JSONArray sections = new JSONArray();
        JSONObject widget = new JSONObject();

        widget.put("widgets", widgets);
        sections.put(widget);
        card.put("sections", sections);

        cards.put(card);
        chatMessage.put("cards", cards);

        LOG.debug(chatMessage.toString(1));
        return chatMessage;

    }

}
