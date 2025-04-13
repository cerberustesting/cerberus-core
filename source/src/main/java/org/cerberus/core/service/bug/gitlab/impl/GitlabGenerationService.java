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
package org.cerberus.core.service.bug.gitlab.impl;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.util.StringUtil;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.core.service.bug.gitlab.IGitlabGenerationService;

/**
 *
 * @author vertigo17
 */
@Service
public class GitlabGenerationService implements IGitlabGenerationService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(GitlabGenerationService.class);

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private ITagService tagService;

    @Override
    public String generateGitlabDescriptionIssue(TestCaseExecution execution, String LabelName) {
        String issueDescription = "";

        try {
            String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
            if (StringUtil.isEmptyOrNull(cerberusUrl)) {
                cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
            }
            cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");

            String cerberusUrlExe = cerberusUrl + "TestCaseExecution.jsp?executionId=" + String.valueOf(execution.getId());
            String cerberusUrlTag = cerberusUrl + "ReportingExecutionByTag.jsp?Tag=" + StringUtil.encodeURL(execution.getTag());
            String cerberusUrlExeHisto = cerberusUrl + "ReportingExecutionOverTime.jsp?tests=" + StringUtil.encodeURL(execution.getTest()) + "&testcases=" + StringUtil.encodeURL(execution.getTestCase());

            issueDescription = "Test Case: **" + execution.getTest() + "** - **" + execution.getTestCase() + "** - " + execution.getDescription() + " \n\n";
            issueDescription += "Executed on " + execution.getEnvironment() + " - " + execution.getCountry() + "\n";
            issueDescription += "On " + convertToDate(execution.getEnd()) + "\n\n";
            issueDescription += "Ended with status " + execution.getControlStatus() + "\n **" + execution.getControlMessage() + "**\n\n";
            issueDescription += "Please check the detailed execution [" + execution.getId() + "](" + cerberusUrlExe + ") \n";
            issueDescription += "You can also access [latest executions](" + cerberusUrlExeHisto + ") perfomed on that testcase.\n\n";
            issueDescription += "Execution was triggered from campaign execution [" + execution.getTag() + "](" + cerberusUrlTag + ")";

        } catch (JSONException ex) {
            LOG.debug(ex, ex);
        } catch (UnsupportedEncodingException ex) {
            LOG.error(ex, ex);
        }
        return issueDescription;

    }

    @Override
    public String generateGitlabTitleIssue(TestCaseExecution execution, String LabelName) {
        String issueObject = "";

        try {

            issueObject = "TestCase Execution " + execution.getId() + " failed - " + execution.getTest() + " " + execution.getTestCase();

        } catch (JSONException ex) {
            LOG.debug(ex, ex);
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
