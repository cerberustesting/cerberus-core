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

package org.cerberus.servlet.manualTestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.cerberus.entity.TCase;
import org.cerberus.factory.IFactoryTCase;
import org.cerberus.factory.impl.FactoryTCase;
import org.cerberus.log.MyLogger;
import org.cerberus.service.IManualTestCaseService;
import org.cerberus.util.StringUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 28/10/2013
 * @since 0.9.1
 */
@WebServlet(name = "SearchManualTestCaseInformation", urlPatterns = {"/SearchManualTestCaseInformation"})
public class SearchManualTestCaseInformation extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String echo = req.getParameter("sEcho");
        String text = "%" + this.getValue(req, "ScText") + "%";
        String system = this.getValue(req, "ScSystem");
        String country = this.getValue(req, "ScCountry");
        String env = this.getValue(req, "ScEnv");
        String campaign = this.getValue(req, "ScCampaign");
        String testBattery = this.getValue(req, "ScTestBattery");
        TCase tCase = this.getTestCaseFromRequest(req);

        tCase.setActive("Y");
        if (env.equalsIgnoreCase("QA")) {
            tCase.setRunQA("Y");
        } else if (env.equalsIgnoreCase("UAT")) {
            tCase.setRunUAT("Y");
        } else if (env.equalsIgnoreCase("PROD")) {
            tCase.setRunPROD("Y");
        }

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IManualTestCaseService manualTestCaseService = appContext.getBean(IManualTestCaseService.class);

        try {

            ObjectMapper mapper = new ObjectMapper();
            OutputStream out = new ByteArrayOutputStream();
            mapper.writeValue(out, manualTestCaseService.findTestCaseManualExecution(tCase, text, system, country, env, campaign, testBattery));
            JSONArray data = new JSONArray(out.toString());

            JSONObject jsonResponse = new JSONObject();

            jsonResponse.put("aaData", data);
            jsonResponse.put("sEcho", echo);
            jsonResponse.put("iTotalRecords", data.length());
            jsonResponse.put("iTotalDisplayRecords", data.length());
            resp.setContentType("application/json");
            resp.getWriter().print(jsonResponse.toString());
        } catch (JSONException e) {
            MyLogger.log(SearchManualTestCaseInformation.class.getName(), Level.FATAL, "" + e);
            resp.setContentType("text/html");
            resp.getWriter().print(e.getMessage());
        }
    }

    private TCase getTestCaseFromRequest(HttpServletRequest req) {
        String group = this.getValue(req, "ScGroup");
        String test = this.getValue(req, "ScTest");
        String testCase = "%" + this.getValue(req, "ScTestCase") + "%";
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
        String function = this.getValue(req, "function");

        IFactoryTCase factoryTCase = new FactoryTCase();
        return factoryTCase.create(test, testCase, origin, null, creator, null, null, project, ticket, function, application, "", "", "", priority,
                group, status, null, null, null, "", fBuild, fRev, tBuild, tRev, null, bug, targetBuild, targetRev, null, null, null, null, null);
    }

    private String getValue(HttpServletRequest req, String valueName) {
        String value = null;
        if (req.getParameter(valueName) != null && !req.getParameter(valueName).equalsIgnoreCase("All")) {
            value = req.getParameter(valueName);
        }
        return value;
    }
}
