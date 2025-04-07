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
package org.cerberus.core.service.bug.jira.impl;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.service.bug.jira.IJiraGenerationService;
import org.cerberus.core.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author vertigo17
 */
@Service
public class JiraGenerationService implements IJiraGenerationService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(JiraGenerationService.class);

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private ITagService tagService;

    @Override
    public JSONObject generateJiraIssue(TestCaseExecution execution, String projectKey, String issueTypeName) {
        JSONObject bugObject = new JSONObject();

        try {
            String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
            if (StringUtil.isEmptyOrNull(cerberusUrl)) {
                cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
            }
            cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");

            String cerberusUrlExe = cerberusUrl + "TestCaseExecution.jsp?executionId=" + String.valueOf(execution.getId());
            String cerberusUrlTag = cerberusUrl + "ReportingExecutionByTag.jsp?Tag=" + StringUtil.encodeURL(execution.getTag());
            String cerberusUrlExeHisto = cerberusUrl + "ReportingExecutionOverTime.jsp?tests=" + StringUtil.encodeURL(execution.getTest()) + "&testcases=" + StringUtil.encodeURL(execution.getTestCase());

            JSONObject fields = new JSONObject();

            fields.put("summary", "TestCase Execution " + execution.getId() + " failed - " + execution.getTest() + " " + execution.getTestCase());

            JSONObject description = new JSONObject();
            description.put("type", "doc");
            description.put("version", 1);

            JSONArray contentA = new JSONArray();

            JSONObject contentA1 = new JSONObject();
            contentA1.put("type", "paragraph");
            JSONArray contentA1A = new JSONArray();

            contentA1A.put(generateContent("Test Case: "));
            contentA1A.put(generateContentStrong(execution.getTest() + " - " + execution.getTestCase(), null));
            contentA1A.put(generateContent(" - " + execution.getDescription()));
            contentA1A.put(generateBreak());
            contentA1A.put(generateBreak());
            contentA1A.put(generateContent("Executed on "));
            contentA1A.put(generateContentStrong(execution.getEnvironment(), null));
            contentA1A.put(generateContent(" - "));
            contentA1A.put(generateContentStrong(execution.getCountry(), null));
            contentA1A.put(generateBreak());
            contentA1A.put(generateContent("On " + convertToDate(execution.getEnd())));
            contentA1A.put(generateBreak());
            contentA1A.put(generateBreak());
            contentA1A.put(generateContent("Ended with status "));
            contentA1A.put(generateContentStrong(execution.getControlStatus(), execution.getColor(execution.getControlStatus())));
            contentA1A.put(generateBreak());
            contentA1A.put(generateContentStrong(execution.getControlMessage(), null));
            contentA1A.put(generateBreak());
            contentA1A.put(generateBreak());
            contentA1A.put(generateContent("Please check the detailed execution "));
            contentA1A.put(generateContentLink(String.valueOf(execution.getId()), cerberusUrlExe));
            contentA1A.put(generateBreak());
            contentA1A.put(generateContent("You can also access "));
            contentA1A.put(generateContentLink("latest executions", cerberusUrlExeHisto));
            contentA1A.put(generateContent(" perfomed on that testcase."));
            contentA1A.put(generateBreak());
            contentA1A.put(generateBreak());
            contentA1A.put(generateContent("Execution was triggered from campaign execution "));
            contentA1A.put(generateContentLink(String.valueOf(execution.getTag()), cerberusUrlTag));

            contentA1.put("content", contentA1A);

            contentA.put(contentA1);
            description.put("content", contentA);
            fields.put("description", description);

            JSONObject issueType = new JSONObject();
            issueType.put("name", issueTypeName);
            fields.put("issuetype", issueType);

            JSONObject project = new JSONObject();
            project.put("key", projectKey);
            fields.put("project", project);

            bugObject.put("fields", fields);

            JSONObject updateObject = new JSONObject();
            bugObject.put("update", updateObject);

            LOG.debug(bugObject.toString(1));

        } catch (JSONException ex) {
            LOG.error(ex, ex);
        } catch (UnsupportedEncodingException ex) {
            LOG.error(ex,ex);
        }
        return bugObject;

    }

    private JSONObject generateBreak() throws JSONException {
        JSONObject contentObject = new JSONObject();
        contentObject.put("type", "hardBreak");
        return contentObject;
    }

    private JSONObject generateContent(String content) throws JSONException {
        JSONObject contentObject = new JSONObject();
        contentObject.put("type", "text");
        contentObject.put("text", content);
        return contentObject;
    }

    private JSONObject generateContentStrong(String content, String color) throws JSONException {
        JSONObject contentObject = new JSONObject();
        contentObject.put("type", "text");
        contentObject.put("text", content);

        JSONArray contentMarkArray = new JSONArray();

        JSONObject contentMarkObject = new JSONObject();
        contentMarkObject.put("type", "strong");
        contentMarkArray.put(contentMarkObject);

        if (color != null) {
            JSONObject contentMarkColorObject = new JSONObject();
            contentMarkColorObject.put("type", "textColor");
            JSONObject contentMarkColorAttrsObject = new JSONObject();
            contentMarkColorAttrsObject.put("color", color);
            contentMarkColorObject.put("attrs", contentMarkColorAttrsObject);
            contentMarkArray.put(contentMarkColorObject);
        }

        contentObject.put("marks", contentMarkArray);

        return contentObject;
    }

    private JSONObject generateContentLink(String content, String href) throws JSONException {
        JSONObject contentObject = new JSONObject();
        contentObject.put("type", "text");
        contentObject.put("text", content);

        JSONArray contentMarkArray = new JSONArray();

        JSONObject contentMarkObject = new JSONObject();
        contentMarkObject.put("type", "link");
        JSONObject contentAttrsObject = new JSONObject();
        contentAttrsObject.put("href", href);
        contentMarkObject.put("attrs", contentAttrsObject);

        contentMarkArray.put(contentMarkObject);

        contentObject.put("marks", contentMarkArray);

        return contentObject;
    }

    private String convertToDate(long cerberusDate) {

        SimpleDateFormat formater; // Define the MySQL Format.
        formater = new SimpleDateFormat("yyyy-MM-dd' at 'HH:mm:ss");

        return formater.format(cerberusDate);

    }

    private String convertToDate(Timestamp cerberusDate) {

        SimpleDateFormat formater; // Define the MySQL Format.
        formater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

        return formater.format(cerberusDate);

    }
}
