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

import com.redcats.tst.entity.Invariant;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.service.IApplicationService;
import com.redcats.tst.service.IInvariantService;
import com.redcats.tst.service.ITestCaseService;
import org.apache.log4j.Level;
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
import java.util.HashSet;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 06/11/2013
 * @since 0.9.1
 */
@WebServlet(name = "GetDataForTestCaseSearch", urlPatterns = {"/GetDataForTestCaseSearch"})
public class GetDataForTestCaseSearch extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseService testService = appContext.getBean(ITestCaseService.class);
        IApplicationService applicationService = appContext.getBean(IApplicationService.class);
        IInvariantService invariantService = appContext.getBean(IInvariantService.class);

        JSONArray jsonResponse = new JSONArray();
        String[] columns = {"test", "project", "ticket", "bugID", "origine", "creator", "application", "priority",
                "status", "group", "activePROD", "activeUAT", "activeQA", "tcActive", "fromBuild", "fromRev", "toBuild",
                "toRev", "targetBuild", "targetRev"};

        try {
            JSONObject data;
            for (String s : columns) {
                data = new JSONObject();
                data.put("data", new JSONArray(testService.findUniqueDataOfColumn(s)));
                data.put("name", s);
                jsonResponse.put(data);
            }

            data = new JSONObject();
            data.put("data", new JSONArray(applicationService.findDistinctSystem()));
            data.put("name", "system");
            jsonResponse.put(data);

            HashSet<String> envAux = new HashSet<String>();
            for (Invariant i : invariantService.findListOfInvariantById("ENVIRONMENT")) {
                envAux.add(i.getGp1());
            }
            JSONArray env = new JSONArray(envAux.toArray());
            data = new JSONObject();
            data.put("data", env);
            data.put("name", "executionEnv");
            jsonResponse.put(data);

            JSONArray country = new JSONArray();
            for (Invariant i : invariantService.findListOfInvariantById("COUNTRY")) {
                country.put(i.getValue());
            }
            data = new JSONObject();
            data.put("data", country);
            data.put("name", "executionCountry");
            jsonResponse.put(data);

            resp.setContentType("application/json");
            resp.getWriter().print(jsonResponse.toString());

        } catch (JSONException e) {
            MyLogger.log(GetDataForTestCaseSearch.class.getName(), Level.FATAL, "" + e);
            resp.setContentType("text/html");
            resp.getWriter().print(e.getMessage());
        } catch (CerberusException e) {
            MyLogger.log(GetDataForTestCaseSearch.class.getName(), Level.FATAL, "" + e);
            resp.setContentType("text/html");
            resp.getWriter().print(e.getMessage());
        }
    }
}
