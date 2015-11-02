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
package org.cerberus.servlet.crud.usermanagement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.cerberus.crud.entity.Group;
import org.cerberus.crud.entity.User;
import org.cerberus.crud.entity.UserSystem;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryGroup;
import org.cerberus.crud.factory.IFactoryUserSystem;
import org.cerberus.crud.factory.impl.FactoryGroup;
import org.cerberus.log.MyLogger;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.IUserGroupService;
import org.cerberus.crud.service.IUserService;
import org.cerberus.crud.service.IUserSystemService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.crud.service.impl.UserGroupService;
import org.cerberus.crud.service.impl.UserService;
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
        IFactoryUserSystem userSystemFactory = appContext.getBean(IFactoryUserSystem.class);
        IUserSystemService userSystemService = appContext.getBean(IUserSystemService.class);

        IFactoryGroup factoryGroup = new FactoryGroup();

        User myUser;
        List<Group> newGroups = null;
        List<UserSystem> newSystems = null;
        try {
            myUser = userService.findUserByKey(login);
            switch (columnPosition) {
                case 0:
                    newGroups = new ArrayList<Group>();
                    for (String group : request.getParameterValues(login + "_UserGroup")) {
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
                    newSystems = new ArrayList<UserSystem>();
                    for (String system : request.getParameterValues(login + "_UserSystem")) {
                        newSystems.add(userSystemFactory.create(login, system));
                    }
                    break;
                case 5:
                    myUser.setDefaultSystem(value);
                    request.getSession().setAttribute("MySystem", value);
                    break;
                case 6:
                    myUser.setRequest(value);
                    break;
                case 7:
                    myUser.setEmail(value);
                    break;
                case 8:
                    myUser.setLanguage(value);
                    request.getSession().setAttribute("MyLang", value);
                    break;
            }
            try {
                if (newGroups != null) {
                    userGroupService.updateUserGroups(myUser, newGroups);

                    /**
                     * Adding Log entry.
                     */
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    logEventService.createPrivateCalls("/UpdateUser", "UPDATE", "Updated user groups : " + login, request);

                } else if (newSystems != null) {
                    userSystemService.updateUserSystems(myUser, newSystems);

                    /**
                     * Adding Log entry.
                     */
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    logEventService.createPrivateCalls("/UpdateUser", "UPDATE", "Updated user system : " + login, request);

                } else {
                    userService.updateUser(myUser);

                    /**
                     * Adding Log entry.
                     */
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    logEventService.createPrivateCalls("/UpdateUser", "UPDATE", "Updated user : " + login, request);
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
