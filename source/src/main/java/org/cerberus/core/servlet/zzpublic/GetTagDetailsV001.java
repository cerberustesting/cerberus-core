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
package org.cerberus.core.servlet.zzpublic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.service.authentification.IAPIKeyService;
import org.cerberus.core.util.ParameterParserUtil;
import org.json.JSONException;
import java.text.ParseException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.JavaScriptUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.util.StringUtil;

/**
 *
 * @author Nouxx
 */
@WebServlet(name = "GetTagDetailsV001", urlPatterns = {"/GetTagDetailsV001"})
public class GetTagDetailsV001 extends HttpServlet {

    private ITestCaseExecutionService testCaseExecutionService;
    private IAPIKeyService apiKeyService;
    private ITagService tagService;
    private IParameterService parameterService;
    private IInvariantService invariantService;

    private List<Invariant> prioritiesList = new ArrayList<>();
    private List<Invariant> countriesList = new ArrayList<>();
    private List<Invariant> environmentsList = new ArrayList<>();

    private String cerberusUrlParameter;

    private static final Logger LOG = LogManager.getLogger("GetTagDetailsV001");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        tagService = appContext.getBean(ITagService.class);
        parameterService = appContext.getBean(IParameterService.class);
        invariantService = appContext.getBean(IInvariantService.class);

        String Tag = ParameterParserUtil.parseStringParam(request.getParameter("Tag"), "");
        /**
         * Adding Log entry.
         */
        ILogEventService logEventService = appContext.getBean(LogEventService.class);
        logEventService.createForPublicCalls("/GetTagDetailsV001", "CALL", LogEvent.STATUS_INFO, "TagDetails called : " + request.getRequestURL(),
                request);

        apiKeyService = appContext.getBean(IAPIKeyService.class);
        testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
        if (apiKeyService.authenticate(request, response)) {
            List<TestCaseExecution> listOfExecutions;
            List<JSONObject> listOfExecutionsJSON = new ArrayList<>();
            try {
                // get invariants lists (priorities, countries and env)
                prioritiesList = invariantService.readByIdName("PRIORITY");
                countriesList = invariantService.readByIdName("COUNTRY");
                environmentsList = invariantService.readByIdName("ENVIRONMENT");
                JSONObject jsonResponse = new JSONObject();
                Tag tag = tagService.convert(tagService.readByKey(Tag));

                cerberusUrlParameter = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
                if (StringUtil.isEmptyOrNull(cerberusUrlParameter)) {
                    cerberusUrlParameter = parameterService.getParameterStringByKey("cerberus_url", "", "");
                }

                if (tag != null) {
                    jsonResponse.put("tag", Tag);
                    jsonResponse.put("tagDurationInMs",
                            (tag.getDateEndQueue().getTime() - tag.getDateStartExe().getTime()));
                    jsonResponse.put("CI", tag.getCiResult());
                    jsonResponse.put("start", tag.getDateCreated());
                    jsonResponse.put("end", tag.getDateEndQueue());
                    JSONObject results = convertTagToResultsJSONObject(tag);
                    jsonResponse.put("results", results);
                    listOfExecutions = testCaseExecutionService.readLastExecutionAndExecutionInQueueByTag(Tag);
                    for (int i = 0; i < listOfExecutions.size(); i++) {
                        TestCaseExecution execution = listOfExecutions.get(i);
                        JSONObject executionJSON = executionToJson(execution);
                        listOfExecutionsJSON.add(executionJSON);
                    }
                    jsonResponse.put("executions", listOfExecutionsJSON);
                    response.setContentType("application/json");
                    response.getWriter().print(jsonResponse.toString());
                }
            } catch (CerberusException ex) {
                LOG.error(ex.getMessageError().getDescription(), ex);
            } catch (JSONException ex) {
                LOG.error(ex.getMessage(), ex);
            } catch (ParseException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }
    }

    private JSONObject convertTagToResultsJSONObject(Tag tag) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("OK", Integer.toString(tag.getNbOK()));
        result.put("KO", Integer.toString(tag.getNbKO()));
        result.put("FA", Integer.toString(tag.getNbFA()));
        result.put("NA", Integer.toString(tag.getNbNA()));
        result.put("PE", Integer.toString(tag.getNbPE()));
        result.put("CA", Integer.toString(tag.getNbCA()));
        result.put("QU", Integer.toString(tag.getNbQU()));
        result.put("WE", Integer.toString(tag.getNbWE()));
        result.put("NE", Integer.toString(tag.getNbNE()));
        result.put("QE", Integer.toString(tag.getNbQE()));
        result.put("total", Integer.toString(tag.getNbExe()));
        return result;
    }

    private Invariant getInvariant(String value, List<Invariant> invariants) {
        return invariants.stream().filter(inv -> value.equals(inv.getValue())).findAny().orElse(null);
    }

    private JSONObject invariantToJSON(Invariant invariant) {
        JSONObject result = new JSONObject();
        try {
            result.put("value", invariant.getValue());
            result.put("description", invariant.getDescription());
            result.put("shortDescription", invariant.getVeryShortDesc());
            result.put("attribute1", invariant.getGp1());
            result.put("attribute2", invariant.getGp2());
            result.put("attribute3", invariant.getGp3());
            result.put("attribute4", invariant.getGp4());
            result.put("attribute5", invariant.getGp5());
            result.put("attribute6", invariant.getGp6());
            result.put("attribute7", invariant.getGp7());
            result.put("attribute8", invariant.getGp8());
            result.put("attribute9", invariant.getGp9());
        } catch (JSONException e) {
            LOG.error(e.toString(), e);
        }
        return result;
    }

    private Boolean cerberusBooleanToBoolean(String cerberusBoolean) {
        if (cerberusBoolean.equals("N")) {
            return false;
        } else if (cerberusBoolean.equals("Y")) {
            return true;
        } else {
            LOG.error("Error on processing Cerberus Boolean conversion: " + cerberusBoolean);
            return false;
        }
    }

    private JSONObject executionToJson(TestCaseExecution execution) {
        JSONObject result = new JSONObject();
        Invariant priority = getInvariant(Integer.toString(execution.getTestCaseObj().getPriority()), prioritiesList);
        Invariant country = getInvariant(execution.getCountry(), countriesList);
        Invariant environment = getInvariant(execution.getEnvironment(), environmentsList);
        try {
            result.put("id", execution.getId());
            result.put("status", execution.getControlStatus());
            result.put("link", cerberusUrlParameter + "/TestCaseExecution.jsp?executionId=" + execution.getId());
            result.put("manualExecution", cerberusBooleanToBoolean(execution.getManualExecution()));
            result.put("message", JavaScriptUtils.javaScriptEscape(execution.getControlMessage()));
            result.put("priority", invariantToJSON(priority));
            result.put("country", invariantToJSON(country));
            result.put("environment", invariantToJSON(environment));
            result.put("start", execution.getStart());
            result.put("end", execution.getEnd());
            result.put("durationInMs", execution.getEnd() - execution.getStart());
            // build the test case JSON property
            JSONObject testcaseJSON = new JSONObject();
            testcaseJSON.put("description", execution.getTestCaseObj().getDescription());
            testcaseJSON.put("comment", execution.getTestCaseObj().getComment());
            testcaseJSON.put("description", execution.getTestCaseObj().getDescription());
            testcaseJSON.put("id", JavaScriptUtils.javaScriptEscape(execution.getTestCase()));
            testcaseJSON.put("folder", JavaScriptUtils.javaScriptEscape(execution.getTest()));
            testcaseJSON.put("system", execution.getSystem());
            testcaseJSON.put("application", execution.getApplication());
            testcaseJSON.put("status", execution.getTestCaseObj().getStatus());
            result.put("testcase", testcaseJSON);
            // build the robot object
            JSONObject robot = new JSONObject();
            robot.put("name", execution.getRobot());
            robot.put("executor", execution.getExecutor());
            robot.put("host", execution.getRobotHost());
            robot.put("port", execution.getRobotPort());
            robot.put("declination", execution.getRobotDecli());
            robot.put("sessionId", execution.getRobotSessionID());
            robot.put("provider", execution.getRobotProvider());
            robot.put("providerSessionId", execution.getRobotProviderSessionID());
            robot.put("browser", execution.getBrowser());
            robot.put("platform", execution.getPlatform());
            robot.put("executor", execution.getExecutor());
            result.put("robot", robot);
        } catch (JSONException e) {
            LOG.error(e.toString(), e);
        }
        return result;
    }

}
