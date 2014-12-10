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

package org.cerberus.servlet.buildContent;

import org.apache.log4j.Logger;
import org.cerberus.entity.BuildRevisionParameters;
import org.cerberus.entity.Project;
import org.cerberus.entity.User;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.IBuildRevisionParametersService;
import org.cerberus.service.IProjectService;
import org.cerberus.service.IUserService;
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

@WebServlet(name = "FindBuildContent", urlPatterns = {"/FindBuildContent"})
public class FindBuildContent extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(FindBuildContent.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String echo = req.getParameter("sEcho");
        String system = req.getParameter("System");
        String build = req.getParameter("Build");
        String revision = req.getParameter("Revision");

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IBuildRevisionParametersService buildRevisionParametersService = appContext.getBean(IBuildRevisionParametersService.class);
        IUserService userService = appContext.getBean(IUserService.class);
        IProjectService projectService = appContext.getBean(IProjectService.class);

        JSONObject jsonResponse = new JSONObject();
        try {
            JSONArray data = new JSONArray(); //data that will be shown in the table
            for (BuildRevisionParameters brp : buildRevisionParametersService.findBuildRevisionParametersByCriteria(system, build, revision)) {
                JSONArray array = new JSONArray();
                array.put(brp.getId());
                array.put(brp.getBuild());
                array.put(brp.getRevision());
                array.put(brp.getApplication());
                array.put(brp.getRelease());

                try {
                    Project project = projectService.findProjectByKey(brp.getProject());
                    StringBuilder sb = new StringBuilder(project.getIdProject());
                    sb.append(" [");
                    sb.append(project.getCode());
                    sb.append("] ");
                    sb.append(project.getDescription());
                    array.put(sb.toString());
                } catch (CerberusException e) {
                    LOG.info("Unable to find Project : "+e.getMessageError().getDescription());
                    array.put(" [] NONE");
                }

                array.put(brp.getTicketIdFixed());
                array.put(brp.getBudIdFixed());
                array.put(brp.getSubject());
                try {
                    User user = userService.findUserByKey(brp.getReleaseOwner());
                    array.put(user.getName());
                } catch (CerberusException e) {
                    LOG.info("Unable to find User : "+e.getMessageError().getDescription());
                    array.put("");
                }
                array.put(brp.getLink());

                data.put(array);
            }

            jsonResponse.put("aaData", data);
            jsonResponse.put("sEcho", echo);
            jsonResponse.put("iTotalRecords", data.length());
            jsonResponse.put("iTotalDisplayRecords", data.length());
            resp.setContentType("application/json");
            resp.getWriter().print(jsonResponse.toString());
        } catch (JSONException e) {
            LOG.error("Unable to convert data to JSON : "+e.getMessage());
            resp.setContentType("text/html");
            resp.getWriter().print(e.getMessage());
        }
    }
}
