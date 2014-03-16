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
package org.cerberus.servlet.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.entity.Group;
import org.cerberus.entity.User;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryGroup;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.factory.impl.FactoryGroup;
import org.cerberus.factory.impl.FactoryLogEvent;
import org.cerberus.log.MyLogger;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.IUserGroupService;
import org.cerberus.service.IUserService;
import org.cerberus.service.impl.LogEventService;
import org.cerberus.service.impl.UserGroupService;
import org.cerberus.service.impl.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author ip100003
 */
@WebServlet(name = "UpdateUser", urlPatterns = {"/UpdateUser"})
public class UpdateUser extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //TODO create class Validator to validate all parameter from page
        String login = request.getParameter("id");
        int columnPosition = Integer.parseInt(request.getParameter("columnPosition"));
        String value = request.getParameter("value").replaceAll("'", "");

        MyLogger.log(UpdateUser.class.getName(), Level.INFO, "value : " + value + " columnPosition : " + columnPosition + " login : " + login);

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IUserService userService = appContext.getBean(UserService.class);
        IUserGroupService userGroupService = appContext.getBean(UserGroupService.class);

        IFactoryGroup factoryGroup = new FactoryGroup();

        User myUser;
        List<Group> newGroups = null;
        try {
            myUser = userService.findUserByKey(login);
            switch (columnPosition) {
                case 0:
                    newGroups = new ArrayList<Group>();
                    for (String group : request.getParameterValues(login + "_group")) {
                        newGroups.add(factoryGroup.create(group));
                    }
                    break;
                case 1:
                    myUser.setLogin(value);
                    break;
                case 2:
                    myUser.setName(value);
                    break;
                case 3:
                    myUser.setTeam(value);
                    break;
                case 4:
                    myUser.setDefaultSystem(value);
                    break;
                case 5:
                    myUser.setRequest(value);
                    break;
                case 6:
                    myUser.setEmail(value);
                    break;
            }
            try {
                if (newGroups != null) {
                    userGroupService.updateUserGroups(myUser, newGroups);

                    /**
                     * Adding Log entry.
                     */
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
                    try {
                        logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/UpdateUserAjax", "UPDATE", "Updated user : " + login, "", ""));
                    } catch (CerberusException ex) {
                        Logger.getLogger(UpdateUser.class.getName()).log(Level.ERROR, null, ex);
                    }

                } else {
                    userService.updateUser(myUser);
                }
                response.getWriter().print(value);
            } catch (CerberusException ex) {
                response.getWriter().print(ex.getMessageError().getDescription());
            }
        } catch (CerberusException ex) {
            response.getWriter().print(ex.getMessageError().getDescription());
        }

    }
}
