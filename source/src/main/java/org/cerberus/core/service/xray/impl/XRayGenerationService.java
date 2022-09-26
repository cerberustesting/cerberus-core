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
package org.cerberus.core.service.xray.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.util.StringUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.core.service.xray.IXRayGenerationService;
import org.json.JSONArray;
import org.json.JSONException;

/**
 *
 * @author vertigo17
 */
@Service
public class XRayGenerationService implements IXRayGenerationService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(XRayGenerationService.class);

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private ITagService tagService;

    @Override
    public JSONObject generateCreateTestExecution(Tag tag, TestCaseExecution execution) {
        JSONObject xRayMessage = new JSONObject();

        try {

            JSONObject infoMessage = new JSONObject();
            String myEnvironmentList = StringUtil.convertToString(new JSONArray(tag.getReqEnvironmentList()), ",");
            String myCountryList = StringUtil.convertToString(new JSONArray(tag.getReqCountryList()), ",");

            infoMessage.put("summary", tag.getTag() + " from campaign " + tag.getCampaign() + " with " + myEnvironmentList + " and " + myCountryList);
            infoMessage.put("description", tag.getDescription());
            infoMessage.put("startDate", convertToDate(tag.getDateCreated()));

            xRayMessage.put("info", infoMessage);

            JSONArray testsMessage = new JSONArray();
            JSONObject testMessage = new JSONObject();
            testMessage.put("testKey", execution.getTestCaseObj().getRefOrigine());
            testMessage.put("start", convertToDate(execution.getStart()));
            testMessage.put("finish", convertToDate(execution.getEnd()));
            testMessage.put("comment", execution.getId() + " - " + execution.getControlMessage());
            testMessage.put("status", convertToStatus(execution.getControlStatus()));

            JSONArray stepsMessage = new JSONArray();
            JSONObject stepMessage = new JSONObject();
            stepMessage.put("status", convertToStatus(execution.getControlStatus()));
            stepMessage.put("actualResult", "actuel Result");
            stepsMessage.put(stepMessage);
            testMessage.put("steps", stepsMessage);

            testsMessage.put(testMessage);

            xRayMessage.put("tests", testsMessage);

            // Adding Test Execution in case it aalready exist.
            if (!StringUtil.isEmpty(tag.getXRayTestExecution()) && !"PENDING".equals(tag.getXRayTestExecution())) {
                xRayMessage.put("testExecutionKey", tag.getXRayTestExecution());
            }

            LOG.debug(xRayMessage.toString(1));
        } catch (JSONException ex) {
            LOG.debug(ex, ex);
        }
        return xRayMessage;

    }

    @Override
    public JSONObject generateUpdateTestExecution(Tag tag, String channel) throws UnsupportedEncodingException, Exception {

        String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (StringUtil.isEmpty(cerberusUrl)) {
            cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }
        cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");
        cerberusUrl += "ReportingExecutionByTag.jsp?Tag=" + URLEncoder.encode(tag.getTag(), "UTF-8");

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

        if (!StringUtil.isEmpty(channel)) {
            slackMessage.put("channel", channel);
        }
        slackMessage.put("username", "Cerberus");

        LOG.debug(slackMessage.toString(1));
        return slackMessage;

    }

    @Override
    public JSONObject generateAuthenticationRequest(String clientId, String clientSecret) throws UnsupportedEncodingException, Exception {

        JSONObject xRayMessage = new JSONObject();

        xRayMessage.put("client_id", clientId);
        xRayMessage.put("client_secret", clientSecret);

        LOG.debug(xRayMessage.toString(1));
        return xRayMessage;

    }

    private String convertToStatus(String cerberusStatus) {
        switch (cerberusStatus) {
            case TestCaseExecution.CONTROLSTATUS_KO:
            case TestCaseExecution.CONTROLSTATUS_FA:
            case TestCaseExecution.CONTROLSTATUS_NA:
            case TestCaseExecution.CONTROLSTATUS_CA:
            case TestCaseExecution.CONTROLSTATUS_QE:
                return "FAILED";
            case TestCaseExecution.CONTROLSTATUS_OK:
            case TestCaseExecution.CONTROLSTATUS_NE:
                return "PASSED";
            case TestCaseExecution.CONTROLSTATUS_PE:
            case TestCaseExecution.CONTROLSTATUS_QU:
                return "EXECUTING";
            case TestCaseExecution.CONTROLSTATUS_WE:
                return "TODO";
            default:
                return "FAILED";
        }
    }

    private String convertToDate(long cerberusDate) {

        SimpleDateFormat formater; // Define the MySQL Format.
        formater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

        return formater.format(cerberusDate);

    }

    private String convertToDate(Timestamp cerberusDate) {

        SimpleDateFormat formater; // Define the MySQL Format.
        formater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

        return formater.format(cerberusDate);

    }
}
