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
package org.cerberus.service.slack.impl;

import org.cerberus.crud.entity.Tag;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITagService;
import org.cerberus.service.slack.ISlackGenerationService;
import org.cerberus.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class SlackGenerationService implements ISlackGenerationService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(SlackGenerationService.class);

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private ITagService tagService;

    @Override
    public JSONObject generateNotifyStartTagExecution(String tag, String channel) throws Exception {

        JSONObject slackMessage = new JSONObject();
        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isNullOrEmpty(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }
        cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");
        cerberusUrl += "ReportingExecutionByTag.jsp?Tag=" + tag;
        slackMessage.put("text", "Execution Tag '" + tag + "' Started. <" + cerberusUrl + "|Click here> for details.");
        if (!StringUtil.isNullOrEmpty(channel)) {
            slackMessage.put("channel", channel);
        }
        slackMessage.put("username", "Cerberus");

        return slackMessage;

    }

    @Override
    public JSONObject generateNotifyEndTagExecution(String tag, String channel) throws Exception {

        Tag mytag = tagService.convert(tagService.readByKey(tag));

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isNullOrEmpty(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }
        cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");
        cerberusUrl += "ReportingExecutionByTag.jsp?Tag=" + tag;

        JSONObject slackMessage = new JSONObject();
        JSONObject attachementObj = new JSONObject();

        attachementObj.put("fallback", "Execution Tag '" + tag + "' Ended. <" + cerberusUrl + "|Click here> for details.");
        attachementObj.put("pretext", "Execution Tag '" + tag + "' Ended. <" + cerberusUrl + "|Click here> for details.");
        
        JSONObject slackattaMessage = new JSONObject();
        if ("OK".equalsIgnoreCase(mytag.getCiResult())) {
            attachementObj.put("color", TestCaseExecution.CONTROLSTATUS_OK_COL);
            slackattaMessage.put("title", "Campaign successfully Executed. CI Score = " + mytag.getCiScore() + " < " + mytag.getCiScoreThreshold());
        } else {
            attachementObj.put("color", TestCaseExecution.CONTROLSTATUS_KO_COL);
            slackattaMessage.put("title", "Campaign failed. CI Score = " + mytag.getCiScore() + " >= " + mytag.getCiScoreThreshold());

        }
        slackattaMessage.put("value", mytag.getNbExeUsefull() + " Execution(s) - " + mytag.getNbOK() + " OK - " + mytag.getNbKO() + " KO - " + mytag.getNbFA() + " FA.");
        slackattaMessage.put("short", false);
        attachementObj.append("fields", slackattaMessage);

        slackMessage.append("attachments", attachementObj);

        if (!StringUtil.isNullOrEmpty(channel)) {
            slackMessage.put("channel", channel);
        }
        slackMessage.put("username", "Cerberus");

        return slackMessage;

    }

}
