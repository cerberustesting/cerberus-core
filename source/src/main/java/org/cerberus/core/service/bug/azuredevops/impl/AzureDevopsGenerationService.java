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
package org.cerberus.core.service.bug.azuredevops.impl;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.core.service.bug.azuredevops.IAzureDevopsGenerationService;

/**
 *
 * @author vertigo17
 */
@Service
public class AzureDevopsGenerationService implements IAzureDevopsGenerationService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(AzureDevopsGenerationService.class);

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private ITagService tagService;

    @Override
    public JSONArray generateAzureDevopsWorkItem(TestCaseExecution execution) {
        JSONArray issueObject = new JSONArray();

        try {
            String cerberusUrl = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
            if (StringUtil.isEmptyOrNull(cerberusUrl)) {
                cerberusUrl = parameterService.getParameterStringByKey("cerberus_url", "", "");
            }
            cerberusUrl = StringUtil.addSuffixIfNotAlready(cerberusUrl, "/");

            String cerberusUrlExe = cerberusUrl + "TestCaseExecution.jsp?executionId=" + String.valueOf(execution.getId());
            String cerberusUrlTag = cerberusUrl + "ReportingExecutionByTag.jsp?Tag=" + StringUtil.encodeURL(execution.getTag());
            String cerberusUrlExeHisto = cerberusUrl + "ReportingExecutionOverTime.jsp?tests=" + StringUtil.encodeURL(execution.getTest()) + "&testcases=" + StringUtil.encodeURL(execution.getTestCase());

            JSONObject op1Object = new JSONObject();

            op1Object.put("op", "add");
            op1Object.put("path", "/fields/System.Title");
            op1Object.put("from", JSONObject.NULL);
            op1Object.put("value", "TestCase Execution " + execution.getId() + " failed - " + execution.getTest() + " " + execution.getTestCase());

            issueObject.put(op1Object);

            JSONObject op2Object = new JSONObject();

            op2Object.put("op", "add");
            op2Object.put("path", "/fields/System.Description");
            op2Object.put("from", JSONObject.NULL);
            op2Object.put("value", "<div><span style=\"\">Test Case: <b>" + execution.getTest() + " - " + execution.getTestCase() + "</b> - " + execution.getDescription() + "</span> </div>"
                    + "<div>"
                    + "<div><br> </div>"
                    + "<div>Executed on " + execution.getEnvironment() + " - " + execution.getCountry() + "<br> </div>"
                    + "<div>On " + convertToDate(execution.getEnd()) + "<br> </div>"
                    + "<div><br> </div>"
                    + "<div>Ended with status <span style=\"color:" + execution.getColor(execution.getControlStatus()) + ";\"><b>" + execution.getControlStatus() + "</b></span><br> </div>"
                    + "<div>" + execution.getControlMessage() + "<br> </div><div><br> </div>"
                    + "<div>Please check the detailed execution <a href=\"" + cerberusUrlExe + "\">" + execution.getId() + "</a><br> </div>"
                    + "<div>You can also access <a href=\"" + cerberusUrlExeHisto + "\">latest executions</a> perfomed on that testcase.<br> </div>"
                    + "<div><br> </div>"
                    + "<div>Execution was triggered from campaign execution <a href=\"" + cerberusUrlTag + "\">" + execution.getTag() + "</a><br> </div><div><br> </div><span></span><br> </div>");

            issueObject.put(op2Object);

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
