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
package org.cerberus.core.apiprivate;

import java.sql.Timestamp;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.impl.TestCaseExecutionService;
import org.cerberus.core.engine.entity.ExecutionUUID;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author bcivel
 */
@RestController
@RequestMapping("/executions")
public class ExecutionPrivateController {

    private static final Logger LOG = LogManager.getLogger(ExecutionPrivateController.class);
    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    @Autowired
    TestCaseExecutionService executionService;

    @Autowired
    private ExecutionUUID executionUUIDObject;

    @GetMapping("/getLastByCriteria")
    public String getLastByCriteria(
            @RequestParam(name = "test", value = "test") String test,
            @RequestParam(name = "testCase", value = "testCase") String testCase,
            @RequestParam(name = "numberOfExecution", value = "Number of execution expected. If empty, all execution matching the criteria will be returned", required = false) Integer numberOfExecution,
            @RequestParam(name = "tag", value = "Tag of the execution expected", required = false) String tag,
            @RequestParam(name = "campaign", value = "Campaign name of the execution expected", required = false) String campaign) {

        try {
            test = policy.sanitize(test);
            testCase = policy.sanitize(testCase);
            tag = policy.sanitize(tag);
            JSONArray ja = executionService.getLastByCriteria(test, testCase, tag, numberOfExecution);
            return ja.toString();
        } catch (CerberusException ex) {
            LOG.warn(ex, ex);
            return "error";
        }
    }

    @GetMapping("/count")
    public String getnbByCriteria(
            @RequestParam(name = "system", value = "system", required = false) List<String> systems,
            HttpServletRequest request) {

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        JSONObject jsonResponse = new JSONObject();

        try {
            LOG.debug(systems);

            return jsonResponse.put("iTotalRecords", executionService.getNbExecutions(systems)).toString();
        } catch (Exception ex) {
            LOG.warn(ex, ex);
            return "error " + ex.getMessage();
        }
    }

    @GetMapping("/running")
    public String getRunning(
            //            @RequestParam(name = "system", value = "system", required = false) List<String> systems,
            HttpServletRequest request) {

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        JSONObject jsonResponse = new JSONObject();

        try {

            // FIXME The executionUUIDObject unfortunatly does not return the Component Class content.
//        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            LOG.debug(executionUUIDObject.getExecutionUUIDList());
            JSONArray executionArray = new JSONArray();
            for (Object ex : executionUUIDObject.getExecutionUUIDList().values()) {
                TestCaseExecution execution = (TestCaseExecution) ex;
                JSONObject object = new JSONObject();
                object.put("id", execution.getId());
                object.put("test", execution.getTest());
                object.put("testcase", execution.getTestCase());
                object.put("system", execution.getApplicationObj().getSystem());
                object.put("application", execution.getApplication());
                object.put("environment", execution.getEnvironmentData());
                object.put("country", execution.getCountry());
                object.put("robotIP", execution.getSeleniumIP());
                object.put("tag", execution.getTag());
                object.put("start", new Timestamp(execution.getStart()));
                executionArray.put(object);
            }
            jsonResponse.put("runningExecutionsList", executionArray);

            JSONObject queueStatus = new JSONObject();
            queueStatus.put("queueSize", executionUUIDObject.getQueueSize());
            queueStatus.put("globalLimit", executionUUIDObject.getGlobalLimit());
            queueStatus.put("running", executionUUIDObject.getRunning());
            jsonResponse.put("queueStats", queueStatus);

            return jsonResponse.toString();

        } catch (Exception ex) {
            LOG.warn(ex, ex);
            return "error " + ex.getMessage();
        }
    }

}
