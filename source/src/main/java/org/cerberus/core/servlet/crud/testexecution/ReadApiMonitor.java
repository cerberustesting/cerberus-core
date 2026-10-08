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
import java.util.Date;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.TestCaseExecutionFile;
import org.cerberus.core.crud.entity.TestCaseExecutionServiceCall;
import org.cerberus.core.crud.service.ITestCaseExecutionFileService;
import org.cerberus.core.crud.service.ITestCaseExecutionServiceCallService;
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
 * API monitor: the calls of the registered services made by the executions. Feeds the
 * ReportingMonitorApi page.
 *
 * Parameters : from, to (epoch milliseconds, optional - default is the last 24 hours) and
 * either list=true (the services called over the period with their number of calls),
 * service (the calls of that service over the period, oldest first) or
 * files=true with exeid and level (the request / response files recorded for one call).
 */
@WebServlet(name = "ReadApiMonitor", urlPatterns = {"/ReadApiMonitor"})
public class ReadApiMonitor extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ReadApiMonitor.class);
    private static final long DAY_MS = 24L * 3600 * 1000;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");
        ServletUtil.servletStart(request);

        String service = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("service"), "", "UTF8");
        boolean list = ParameterParserUtil.parseBooleanParam(request.getParameter("list"), false);
        boolean files = ParameterParserUtil.parseBooleanParam(request.getParameter("files"), false);
        Date to = new Date(parseLong(request.getParameter("to"), System.currentTimeMillis()));
        Date from = new Date(parseLong(request.getParameter("from"), to.getTime() - DAY_MS));

        try {
            JSONObject jsonResponse = new JSONObject();
            ITestCaseExecutionServiceCallService callService = appContext.getBean(ITestCaseExecutionServiceCallService.class);
            JSONArray content = new JSONArray();
            if (files) {
                long exeId = parseLong(request.getParameter("exeid"), 0);
                String level = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("level"), "", "UTF8");
                if (exeId > 0 && !level.isEmpty()) {
                    ITestCaseExecutionFileService fileService = appContext.getBean(ITestCaseExecutionFileService.class);
                    for (TestCaseExecutionFile f : fileService.readByVarious(exeId, level).getDataList()) {
                        content.put(new JSONObject().put("fileDesc", f.getFileDesc()).put("fileName", f.getFileName()).put("fileType", f.getFileType()));
                    }
                }
                jsonResponse.put("messageType", "OK");
                jsonResponse.put("message", "");
            } else if (list) {
                List<String[]> services = callService.readServicesWithCalls(from, to);
                for (String[] s : services) {
                    content.put(new JSONObject().put("service", s[0]).put("calls", Integer.parseInt(s[1])));
                }
                jsonResponse.put("messageType", "OK");
                jsonResponse.put("message", "");
            } else if (service.isEmpty()) {
                jsonResponse.put("messageType", "KO");
                jsonResponse.put("message", "service is mandatory.");
            } else {
                AnswerList<TestCaseExecutionServiceCall> answer = callService.readByService(service, from, to);
                for (TestCaseExecutionServiceCall call : answer.getDataList()) {
                    content.put(toJson(call));
                }
                jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
                jsonResponse.put("message", answer.getResultMessage().getDescription());
            }
            jsonResponse.put("contentTable", content);
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

    private static JSONObject toJson(TestCaseExecutionServiceCall c) throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", c.getId());
        o.put("exeId", c.getExeId());
        o.put("start", c.getStart());
        o.put("status", c.getStatus());
        o.put("httpCode", c.getHttpCode());
        o.put("durationMs", c.getDurationMs());
        o.put("responseSize", c.getResponseSize());
        o.put("method", c.getMethod());
        o.put("type", c.getType());
        o.put("application", c.getApplication());
        o.put("test", c.getTest());
        o.put("testcase", c.getTestcase());
        o.put("country", c.getCountry());
        o.put("environment", c.getEnvironment());
        o.put("robotDecli", c.getRobotDecli());
        o.put("fileLevel", c.getFileLevel());
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
        return "Calls of the registered services made by the executions";
    }
}
