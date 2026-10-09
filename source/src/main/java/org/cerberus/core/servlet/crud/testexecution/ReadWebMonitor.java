/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
package org.cerberus.core.servlet.crud.testexecution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecutionHttpStat;
import org.cerberus.core.crud.factory.IFactoryTestCase;
import org.cerberus.core.crud.service.ITestCaseExecutionHttpStatService;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Web monitor: the network statistics (HAR) of the executions of one test case
 * over a period, one entry per execution, with the third party hosts of each
 * execution. Feeds the ReportingMonitorWeb page.
 *
 * Parameters : test, testcase, from, to (epoch milliseconds, optional - default
 * is the last 24 hours).
 */
@WebServlet(name = "ReadWebMonitor", urlPatterns = {"/ReadWebMonitor"})
public class ReadWebMonitor extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ReadWebMonitor.class);
    private static final long DAY_MS = 24L * 3600 * 1000;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");
        ServletUtil.servletStart(request);

        String test = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("test"), "", "UTF8");
        String testcase = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("testcase"), "", "UTF8");
        long now = System.currentTimeMillis();
        Date to = new Date(parseLong(request.getParameter("to"), now));
        Date from = new Date(parseLong(request.getParameter("from"), to.getTime() - DAY_MS));

        try {
            JSONObject jsonResponse = new JSONObject();
            JSONArray executions = new JSONArray();
            if (test.isEmpty() || testcase.isEmpty()) {
                jsonResponse.put("messageType", "KO");
                jsonResponse.put("message", "test and testcase are mandatory.");
            } else {
                IFactoryTestCase factoryTestCase = appContext.getBean(IFactoryTestCase.class);
                ITestCaseExecutionHttpStatService statService = appContext.getBean(ITestCaseExecutionHttpStatService.class);
                List<TestCase> testcases = new ArrayList<>(Arrays.asList(factoryTestCase.create(test, testcase)));

                AnswerList<TestCaseExecutionHttpStat> answer = statService.readByCriteria(null, testcases, from, to, null, null, null, null);
                for (TestCaseExecutionHttpStat stat : answer.getDataList()) {
                    executions.put(toJson(stat));
                }
                jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
                jsonResponse.put("message", answer.getResultMessage().getDescription());
            }
            jsonResponse.put("contentTable", executions);
            response.getWriter().print(jsonResponse.toString());
        } catch (JSONException e) {
            LOG.warn(e, e);
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        }
    }

    private static long parseLong(String value, long defaultValue) {
        try {
            return value == null ? defaultValue : Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private static JSONObject toJson(TestCaseExecutionHttpStat s) throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", s.getId());
        o.put("start", s.getStart() == null ? 0 : s.getStart().getTime());
        o.put("controlStatus", s.getControlStatus());
        o.put("country", s.getCountry());
        o.put("environment", s.getEnvironment());
        o.put("robotDecli", s.getRobotDecli());
        o.put("totalHits", s.getTotal_hits());
        o.put("totalSize", s.getTotal_size());
        o.put("totalTime", s.getTotal_time());
        o.put("internalHits", s.getInternal_hits());
        o.put("internalSize", s.getInternal_size());
        o.put("internalTime", s.getInternal_time());
        o.put("nbThirdParty", s.getNb_thirdparty());

        JSONObject types = new JSONObject();
        types.put("img", s.getImg_size());
        types.put("js", s.getJs_size());
        types.put("css", s.getCss_size());
        types.put("html", s.getHtml_size());
        types.put("media", s.getMedia_size());
        o.put("sizeByType", types);

        // Third party hosts: name -> requests, size, time (empty when the detail was not kept).
        JSONArray parties = new JSONArray();
        JSONObject detail = s.getStatDetail();
        if (detail != null && detail.has("thirdparty")) {
            JSONObject tp = detail.getJSONObject("thirdparty");
            Iterator<String> keys = tp.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject p = tp.getJSONObject(key);
                JSONObject party = new JSONObject();
                party.put("name", key);
                party.put("requests", p.optJSONObject("requests") == null ? 0 : p.getJSONObject("requests").optInt("nb"));
                party.put("size", p.optJSONObject("size") == null ? 0 : p.getJSONObject("size").optInt("sum"));
                party.put("time", p.optJSONObject("time") == null ? 0 : p.getJSONObject("time").optInt("totalDuration"));
                parties.put(party);
            }
        }
        o.put("thirdParties", parties);
        return o;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Network statistics of the executions of a test case";
    }
}
