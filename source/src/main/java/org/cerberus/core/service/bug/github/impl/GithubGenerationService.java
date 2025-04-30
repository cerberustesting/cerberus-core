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
package org.cerberus.core.service.bug.github.impl;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.service.bug.github.IGithubGenerationService;
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
public class GithubGenerationService implements IGithubGenerationService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(GithubGenerationService.class);

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private ITagService tagService;

    @Override
    public JSONObject generateGithubIssue(TestCaseExecution execution, String LabelName) {
        JSONObject issueObject = new JSONObject();

        try {
            String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
            if (StringUtil.isEmptyOrNull(cerberusUrl)) {
                cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
            }
            cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");

            String cerberusUrlExe = cerberusUrl + "TestCaseExecution.jsp?executionId=" + String.valueOf(execution.getId());
            String cerberusUrlTag = cerberusUrl + "ReportingExecutionByTag.jsp?Tag=" + StringUtil.encodeURL(execution.getTag());
            String cerberusUrlExeHisto = cerberusUrl + "ReportingExecutionOverTime.jsp?tests=" + StringUtil.encodeURL(execution.getTest()) + "&testcases=" + StringUtil.encodeURL(execution.getTestCase());

            issueObject.put("title", "TestCase Execution " + execution.getId() + " failed - " + execution.getTest() + " " + execution.getTestCase());

            String body = "Test Case: **" + execution.getTest() + "** - **" + execution.getTestCase() + "** - " + execution.getDescription() + " \n\n";
            body += "Executed on " + execution.getEnvironment() + " - " + execution.getCountry() + "\n";
            body += "On " + convertToDate(execution.getEnd()) + "\n\n";
            body += "Ended with status " + execution.getControlStatus() + "\n **" + execution.getControlMessage() + "**\n\n";
            body += "Please check the detailed execution [" + execution.getId() + "](" + cerberusUrlExe + ") \n";
            body += "You can also access [latest executions](" + cerberusUrlExeHisto + ") perfomed on that testcase.\n\n";
            body += "Execution was triggered from campaign execution [" + execution.getTag() + "](" + cerberusUrlTag + ")";
            issueObject.put("body", body);

            if (StringUtil.isNotEmptyOrNull(LabelName)) {
                JSONArray labels = new JSONArray();
                labels.put(LabelName);
                issueObject.put("labels", labels);
            }

            LOG.debug(issueObject.toString(1));

        } catch (JSONException ex) {
            LOG.debug(ex, ex);
        } catch (UnsupportedEncodingException ex) {
            LOG.error(ex, ex);
        }
        return issueObject;

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
