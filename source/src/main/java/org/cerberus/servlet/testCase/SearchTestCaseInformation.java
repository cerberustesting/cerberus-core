/*
 * Cerberus  Copyright (C) 2013  vertigo17
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

package org.cerberus.servlet.testCase;

import org.apache.log4j.Level;
import org.cerberus.dao.ICountryEnvParamDAO;
import org.cerberus.dao.ICountryEnvironmentParametersDAO;
import org.cerberus.entity.*;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTCase;
import org.cerberus.factory.impl.FactoryTCase;
import org.cerberus.log.MyLogger;
import org.cerberus.service.IApplicationService;
import org.cerberus.service.ITestCaseCountryService;
import org.cerberus.service.ITestCaseExecutionService;
import org.cerberus.service.ITestCaseService;
import org.cerberus.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 28/10/2013
 * @since 0.9.1
 */
@WebServlet(name = "SearchTestCaseInformation", urlPatterns = {"/SearchTestCaseInformation"})
public class SearchTestCaseInformation extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String echo = req.getParameter("sEcho");
        String text = this.getValue(req, "ScText");
        String system = this.getValue(req, "ScSystem");
        String country = this.getValue(req, "ScCountry");
        String env = this.getValue(req, "ScEnv");
        TCase tCase = this.getTestCaseFromRequest(req);

        tCase.setGroup("MANUAL");
        tCase.setActive("Y");
        if (env.equalsIgnoreCase("QA")) {
            tCase.setRunQA("Y");
        } else if (env.equalsIgnoreCase("UAT")) {
            tCase.setRunUAT("Y");
        } else if (env.equalsIgnoreCase("PROD")) {
            tCase.setRunPROD("Y");
        }

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseService testService = appContext.getBean(ITestCaseService.class);
        ITestCaseCountryService testCaseCountryService = appContext.getBean(ITestCaseCountryService.class);
        ITestCaseExecutionService testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
        ICountryEnvironmentParametersDAO countryEnvironmentService = appContext.getBean(ICountryEnvironmentParametersDAO.class);
        ICountryEnvParamDAO countryEnvParamDAO = appContext.getBean(ICountryEnvParamDAO.class);
        IApplicationService applicationService = appContext.getBean(IApplicationService.class);

        try {
            JSONArray data = new JSONArray();


            List<TCase> tcList = testService.findTestCaseByAllCriteria(tCase, text, system);
            for (Iterator<TCase> iter = tcList.iterator(); iter.hasNext(); ) {
                TCase tc = iter.next();
                if (!testCaseCountryService.findListOfCountryByTestTestCase(tc.getTest(), tc.getTestCase()).contains(country)) {
                    iter.remove();
                    continue;
                }
                JSONObject jsontCase = new JSONObject(tc);
                String sprint = "";
                String rev = "";
                String url = "";
                String appSystem = "";
                String result = "";
                String dateStr = "";
                try {
                    Application app = applicationService.findApplicationByKey(tc.getApplication());
                    appSystem = app.getSystem();
                    if (app.getType().equalsIgnoreCase("GUI")) {
                        CountryEnvironmentApplication countryEnv = countryEnvironmentService.findCountryEnvironmentParameterByKey(app.getSystem(), country, env, tc.getApplication());
                        url = countryEnv.getIp() + countryEnv.getUrl() + countryEnv.getUrlLogin();
                    }
                    CountryEnvParam countryEnvParam = countryEnvParamDAO.findCountryEnvParamByKey(app.getSystem(), country, env);
                    sprint = countryEnvParam.getBuild();
                    rev = countryEnvParam.getRevision();

                    TCExecution execution = testCaseExecutionService.findLastTCExecutionByCriteria(tc.getTest(), tc.getTestCase(), env, country, sprint, rev);
                    Timestamp date = new Timestamp(execution.getEnd());
                    result = execution.getControlStatus();
                    dateStr = date.toString().split("\\.")[0];
                } catch (CerberusException e) {
                    MyLogger.log(SearchTestCaseInformation.class.getName(), Level.WARN, e.getMessageError().getDescription());
                }

                jsontCase.put("appLink", url.replace("//", "/"));
                jsontCase.put("appSystem", appSystem);
                jsontCase.put("envSprint", sprint);
                jsontCase.put("envRevision", rev);
                jsontCase.put("lastResult", result);
                jsontCase.put("lastResultDate", dateStr);

                data.put(jsontCase);
            }

            JSONObject jsonResponse = new JSONObject();

            jsonResponse.put("aaData", data);
            jsonResponse.put("sEcho", echo);
            jsonResponse.put("iTotalRecords", data.length());
            jsonResponse.put("iTotalDisplayRecords", data.length());
            resp.setContentType("application/json");
            resp.getWriter().print(jsonResponse.toString());
        } catch (JSONException e) {
            MyLogger.log(SearchTestCaseInformation.class.getName(), Level.FATAL, "" + e);
            resp.setContentType("text/html");
            resp.getWriter().print(e.getMessage());
        }
    }

    private TCase getTestCaseFromRequest(HttpServletRequest req) {
        String test = this.getValue(req, "ScTest");
        String testCase = this.getValue(req, "ScTestCase");
        String project = this.getValue(req, "ScProject");
        String ticket = this.getValue(req, "ScTicket");
        String bug = this.getValue(req, "ScBugID");
        String origin = this.getValue(req, "ScOrigin");
        String creator = this.getValue(req, "ScCreator");
        String application = this.getValue(req, "ScApplication");
        int priority = -1;
        if (req.getParameter("ScPriority") != null && !req.getParameter("ScPriority").equalsIgnoreCase("All") && StringUtil.isNumeric(req.getParameter("ScPriority"))) {
            priority = Integer.parseInt(req.getParameter("ScPriority"));
        }
        String status = this.getValue(req, "ScStatus");
        String fBuild = this.getValue(req, "ScFBuild");
        String fRev = this.getValue(req, "ScFRev");
        String tBuild = this.getValue(req, "ScTBuild");
        String tRev = this.getValue(req, "ScTRev");
        String targetBuild = this.getValue(req, "ScTargetBuild");
        String targetRev = this.getValue(req, "ScTargetRev");

        IFactoryTCase factoryTCase = new FactoryTCase();
        return factoryTCase.create(test, testCase, origin, null, creator, null, null, project, ticket, application, "", "", "", priority, "",
                status, null, null, null, "", fBuild, fRev, tBuild, tRev, null, bug, targetBuild, targetRev, null, null, null, null, null);
    }

    private String getValue(HttpServletRequest req, String valueName) {
        String value = null;
        if (req.getParameter(valueName) != null && !req.getParameter(valueName).equalsIgnoreCase("All")) {
            value = req.getParameter(valueName);
        }
        return value;
    }
}
