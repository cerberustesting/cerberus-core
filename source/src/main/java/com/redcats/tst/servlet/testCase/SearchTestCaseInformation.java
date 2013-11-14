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

package com.redcats.tst.servlet.testCase;

import com.redcats.tst.entity.TCase;
import com.redcats.tst.factory.IFactoryTCase;
import com.redcats.tst.factory.impl.FactoryTCase;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.service.ITestCaseService;
import com.redcats.tst.util.StringUtil;
import org.apache.log4j.Level;
import org.codehaus.jackson.map.ObjectMapper;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
        TCase tCase = this.getTestCaseFromRequest(req);

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseService testService = appContext.getBean(ITestCaseService.class);

        ObjectMapper mapper = new ObjectMapper();
        OutputStream out = new ByteArrayOutputStream();
        mapper.writeValue(out, testService.findTestCaseByAllCriteria(tCase, text, system));

        try {
            JSONObject jsonResponse = new JSONObject();
            JSONArray data = new JSONArray(out.toString());

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
        String group = this.getValue(req, "ScGroup");
        String prod = this.getValue(req, "ScPROD");
        String qa = this.getValue(req, "ScQA");
        String uat = this.getValue(req, "ScUAT");
        String active = this.getValue(req, "ScActive");
        String fBuild = this.getValue(req, "ScFBuild");
        String fRev = this.getValue(req, "ScFRev");
        String tBuild = this.getValue(req, "ScTBuild");
        String tRev = this.getValue(req, "ScTRev");
        String targetBuild = this.getValue(req, "ScTargetBuild");
        String targetRev = this.getValue(req, "ScTargetRev");

        IFactoryTCase factoryTCase = new FactoryTCase();
        return factoryTCase.create(test, testCase, origin, null, creator, null, null, project, ticket, application, qa, uat, prod, priority, group,
                status, null, null, null, active, fBuild, fRev, tBuild, tRev, null, bug, targetBuild, targetRev, null, null, null, null, null);
    }

    private String getValue(HttpServletRequest req, String valueName) {
        String value = null;
        if (req.getParameter(valueName) != null && !req.getParameter(valueName).equalsIgnoreCase("All")) {
            value = req.getParameter(valueName);
        }
        return value;
    }
}
