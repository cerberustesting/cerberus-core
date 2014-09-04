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

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.cerberus.entity.BuildRevisionInvariant;
import org.cerberus.entity.Campaign;
import org.cerberus.entity.Invariant;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.IBuildRevisionInvariantService;
import org.cerberus.service.ICampaignService;
import org.cerberus.service.IInvariantService;
import org.cerberus.service.ITestCaseService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

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
        IInvariantService invariantService = appContext.getBean(IInvariantService.class);
        ICampaignService campaignService = appContext.getBean(ICampaignService.class);
        IBuildRevisionInvariantService buildRevisionInvariantService = appContext.getBean(IBuildRevisionInvariantService.class);

        String system = req.getParameter("system");

        JSONArray jsonResponse = new JSONArray();
        String[] columns = {"test", "project", "ticket", "bugID", "origine", "creator", "application", "priority",
                "status", "group", "activePROD", "activeUAT", "activeQA", "tcActive"};

        try {
            JSONObject data;
            for (String s : columns) {
                data = new JSONObject();
                data.put("data", new JSONArray(testService.findUniqueDataOfColumn(s)));
                data.put("name", s);
                jsonResponse.put(data);
            }

            JSONArray build = new JSONArray();
            for(BuildRevisionInvariant bri : buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(system, 1)){
                build.put(bri.getVersionName());
            }
            data = new JSONObject();
            data.put("data", build);
            data.put("name", "fromBuild");
            jsonResponse.put(data);
            data = new JSONObject();
            data.put("data", build);
            data.put("name", "toBuild");
            jsonResponse.put(data);
            data = new JSONObject();
            data.put("data", build);
            data.put("name", "targetBuild");
            jsonResponse.put(data);

            JSONArray revision = new JSONArray();
            for(BuildRevisionInvariant bri : buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(system, 2)){
                revision.put(bri.getVersionName());
            }
            data = new JSONObject();
            data.put("data", revision);
            data.put("name", "fromRev");
            jsonResponse.put(data);
            data = new JSONObject();
            data.put("data", revision);
            data.put("name", "toRev");
            jsonResponse.put(data);
            data = new JSONObject();
            data.put("data", revision);
            data.put("name", "targetRev");
            jsonResponse.put(data);

            JSONArray env = new JSONArray();
            for (Invariant i : invariantService.findListOfInvariantById("ENVIRONMENT")) {
                env.put(i.getValue());
            }
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
            
            JSONArray campaign = new JSONArray();
            for (Campaign c : campaignService.findAll()) {
                campaign.put(c.getCampaign());
            }
            data = new JSONObject();
            data.put("data", campaign);
            data.put("name", "campaign");
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
