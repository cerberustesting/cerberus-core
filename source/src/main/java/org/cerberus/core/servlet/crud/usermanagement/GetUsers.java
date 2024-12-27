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
package org.cerberus.core.servlet.crud.usermanagement;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.UserRole;
import org.cerberus.core.crud.entity.User;
import org.cerberus.core.crud.entity.UserSystem;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.service.IUserService;
import org.cerberus.core.crud.service.IUserSystemService;
import org.cerberus.core.crud.service.impl.UserRoleService;
import org.cerberus.core.crud.service.impl.UserService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.cerberus.core.crud.service.IUserRoleService;

/**
 * @author ip100003
 */
@WebServlet(name = "GetUsers", urlPatterns = {"/GetUsers"})
public class GetUsers extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(GetUsers.class);
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String echo = request.getParameter("sEcho");

        JSONArray data = new JSONArray(); //data that will be shown in the table

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IUserService userService = appContext.getBean(UserService.class);
        IUserSystemService userSystemService = appContext.getBean(IUserSystemService.class);
        IUserRoleService userGroupService = appContext.getBean(UserRoleService.class);
        try {
            JSONObject jsonResponse = new JSONObject();
            try {
                for (User myUser : userService.findallUser()) {
                    JSONObject u = new JSONObject();
                    u.put("login", myUser.getLogin());
                    u.put("name", myUser.getName());
                    u.put("team", myUser.getTeam());
                    u.put("defaultSystem", myUser.getDefaultSystem());
                    u.put("request", myUser.getRequest());
                    u.put("email", myUser.getEmail());

                    JSONArray groups = new JSONArray();
                    for (UserRole group : userGroupService.findRoleByKey(myUser.getLogin())) {
                        groups.put(group.getRole());
                    }
                    u.put("group", groups);
                    
                    JSONArray systems = new JSONArray();
                    for (UserSystem sys : userSystemService.findUserSystemByUser(myUser.getLogin())) {
                        systems.put(sys.getSystem());
                    }
                    u.put("system", systems);

                    data.put(u);
                }
            } catch (CerberusException ex) {
                response.setContentType("text/html");
                response.getWriter().print(ex.getMessageError().getDescription());

            }
            jsonResponse.put("aaData", data);
            jsonResponse.put("sEcho", echo);
            jsonResponse.put("iTotalRecords", data.length());
            jsonResponse.put("iTotalDisplayRecords", data.length());
            response.setContentType("application/json");
            response.getWriter().print(jsonResponse.toString());
        } catch (JSONException e) {
            LOG.warn(e);
            response.setContentType("text/html");
            response.getWriter().print(e.getMessage());
        }
    }
}
