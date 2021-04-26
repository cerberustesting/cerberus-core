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
package org.cerberus.servlet.zzpublic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cerberus.exception.CerberusException;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.crud.entity.Tag;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ITagService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestCaseExecutionService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.service.authentification.IAPIKeyService;
import org.cerberus.util.ParameterParserUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.JavaScriptUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Nouxx
 */
@WebServlet(name = "TagDetails", urlPatterns = { "/TagDetails" })
public class TagDetails extends HttpServlet {

    private ITestCaseExecutionService testCaseExecutionService;
    private IAPIKeyService apiKeyService;
    private ITagService tagService;
    private IParameterService parameterService;
    private IInvariantService invariantService;

    private static final Logger LOG = LogManager.getLogger("TagDetails");

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
        logEventService.createForPublicCalls("/TagDetails", "CALL", "TagDetails called : " + request.getRequestURL(),
                request);

        apiKeyService = appContext.getBean(IAPIKeyService.class);
        testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
        if (apiKeyService.checkAPIKey(request, response)) {
            List<TestCaseExecution> listOfExecutions;
            List<JSONObject> listOfExecutionsJSON = new ArrayList<JSONObject>();
            List<Invariant> prioritiesList = new ArrayList<Invariant>();
            List<Invariant> countriesList = new ArrayList<Invariant>();
            List<Invariant> environmentsList = new ArrayList<Invariant>();
            try {
                prioritiesList = invariantService.readByIdName("PRIORITY");
                countriesList = invariantService.readByIdName("COUNTRY");
                environmentsList = invariantService.readByIdName("ENVIRONMENT");
                JSONObject jsonResponse = new JSONObject();
                Tag tag = tagService.convert(tagService.readByKey(Tag));
                String cerberusUrlParameter = formatCerberusURL(
                        parameterService.findParameterByKey("cerberus_url", "").getValue());
                if (tag != null) {
                    jsonResponse.put("tag", Tag);
                    jsonResponse.put("tagDurationInMs",
                            (tag.getDateEndQueue().getTime() - tag.getDateCreated().getTime()));
                    jsonResponse.put("CI", tag.getCiResult());
                    jsonResponse.put("start", tag.getDateCreated());
                    jsonResponse.put("end", tag.getDateEndQueue());
                    JSONObject results = convertTagToResultsJSONObject(tag);
                    jsonResponse.put("results", results);
                    listOfExecutions = testCaseExecutionService.convert(testCaseExecutionService.readByTag(Tag));
                    for (int i = 0; i < listOfExecutions.size(); i++) {
                        TestCaseExecution execution = listOfExecutions.get(i);
                        JSONObject executionJSON = new JSONObject();
                        String executionPriorityValue = Integer.toString(execution.getTestCaseObj().getPriority());
                        String executionCountryValue = execution.getCountry();
                        String executionEnvironmentValue = execution.getEnvironment();
                        executionJSON.put("id", execution.getId());
                        String executionLink = cerberusUrlParameter + "/TestCaseExecution.jsp?executionId="
                                + execution.getId();
                        executionJSON.put("link", executionLink);
                        executionJSON.put("status", execution.getControlStatus());
                        executionJSON.put("manualExecution", cerberusBooleanToBoolean(execution.getManualExecution()));
                        executionJSON.put("message", JavaScriptUtils.javaScriptEscape(execution.getControlMessage()));
                        executionJSON.put("priority",
                                invariantToJSON(getInvariant(executionPriorityValue, prioritiesList)));
                        executionJSON.put("country",
                                invariantToJSON(getInvariant(executionCountryValue, countriesList)));
                        executionJSON.put("environment",
                                invariantToJSON(getInvariant(executionEnvironmentValue, environmentsList)));
                        executionJSON.put("start", execution.getStart());
                        executionJSON.put("end", execution.getEnd());
                        executionJSON.put("durationInMs", execution.getEnd() - execution.getStart());
                        JSONObject testcase = new JSONObject();
                        testcase.put("description", execution.getTestCaseObj().getDescription());
                        testcase.put("comment", execution.getTestCaseObj().getComment());
                        testcase.put("description", execution.getTestCaseObj().getDescription());
                        testcase.put("id", JavaScriptUtils.javaScriptEscape(execution.getTestCase()));
                        testcase.put("folder", JavaScriptUtils.javaScriptEscape(execution.getTest()));
                        testcase.put("system", execution.getSystem());
                        testcase.put("application", execution.getApplication());
                        testcase.put("status", execution.getTestCaseObj().getStatus());
                        executionJSON.put("testcase", testcase);
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
                        executionJSON.put("robot", robot);
                        listOfExecutionsJSON.add(executionJSON);
                    }
                    jsonResponse.put("executions", listOfExecutionsJSON);
                    response.setContentType("application/json");
                    response.getWriter().print(jsonResponse.toString());
                }
            } catch (CerberusException ex) {
                LOG.debug(ex.getMessageError().getDescription());
            } catch (JSONException ex) {
                LOG.debug(ex.getMessage());
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
            LOG.debug(e.toString());
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

    private String formatCerberusURL(String url) {
        int urlLength = url.length();
        String lastCharacter = url.substring(urlLength - 1);
        if (lastCharacter.equals("/")) {
            return url.substring(0, urlLength - 1);
        } else {
            return url;
        }
    }

}
