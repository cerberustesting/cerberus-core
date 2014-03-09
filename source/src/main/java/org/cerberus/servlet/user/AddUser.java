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
import org.cerberus.factory.IFactoryUser;
import org.cerberus.factory.impl.FactoryGroup;
import org.cerberus.factory.impl.FactoryLogEvent;
import org.cerberus.factory.impl.FactoryUser;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.IParameterService;
import org.cerberus.service.IUserGroupService;
import org.cerberus.service.IUserService;
import org.cerberus.service.impl.LogEventService;
import org.cerberus.service.impl.ParameterService;
import org.cerberus.service.impl.UserGroupService;
import org.cerberus.service.impl.UserService;
import org.cerberus.serviceEmail.IEmailGeneration;
import org.cerberus.serviceEmail.impl.EmailGeneration;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author Tiago Bernardes <tbernardes@redoute.pt>
 * @version 2.0
 * @since 2011-03-28
 */
@WebServlet(name = "AddUser", urlPatterns = {"/AddUser"})
public class AddUser extends HttpServlet {

    /**
     * Create a new User.
     * <p/>
     * Use {@link UserService#insertUser(User user)} to create a user. Parse the
     * login, name, group and requestPassword from the HttpServletRequest and
     * use the class UserService to create a new user. If the user is
     * successfully created the servlet return the login information so the data
     * table can add the new user to the table. If the user can't be created an
     * ERROR is return to be shown on page.
     *
     * @param request http servlet request
     * @param response http servlet response
     * @see org.cerberus.service.impl.UserService
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //TODO create class Validator to validate all parameter from page
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IUserService userService = appContext.getBean(UserService.class);
        IUserGroupService userGroupService = appContext.getBean(UserGroupService.class);
        IEmailGeneration generateEmailService = appContext.getBean(EmailGeneration.class);
        IFactoryUser factory = appContext.getBean(FactoryUser.class);
        IParameterService parameterService = appContext.getBean(ParameterService.class);
        String system = "";

        try {
            String login = request.getParameter("login").replaceAll("'", "");
            if (login.length() > 10) {
                login = login.substring(0, 10);
            }
            String name = ParameterParserUtil.parseStringParam(request.getParameter("name"), "");
            String password = parameterService.findParameterByKey("cerberus_accountcreation_defaultpassword", system).getValue();
            String newPassword = ParameterParserUtil.parseStringParam(request.getParameter("newPassword"), "Y");
            String team = ParameterParserUtil.parseStringParam(request.getParameter("team"), "");
            String defaultSystem = ParameterParserUtil.parseStringParam(request.getParameter("defaultSystem"), "");
            String email = ParameterParserUtil.parseStringParam(request.getParameter("email"), "");

            IFactoryGroup factoryGroup = new FactoryGroup();
            List<Group> groups = new ArrayList<Group>();
            for (String group : request.getParameterValues("groups")) {
                groups.add(factoryGroup.create(group));
            }

            /**
             * Creating user.
             */
            User myUser = factory.create(0, login, password, newPassword, name, team, "", "", defaultSystem, email);

            userService.insertUser(myUser);
            userGroupService.updateUserGroups(myUser, groups);

            /**
             * Send Email to explain how to connect Cerberus if
             * activateNotification is set to Y
             */
            String sendNotification = parameterService.findParameterByKey("cerberus_notification_accountcreation_activatenotification", system).getValue();

            if (sendNotification.equalsIgnoreCase("Y")) {
                generateEmailService.BuildAndSendAccountCreationEmail(myUser);
            }

            /**
             * Adding Log entry.
             */
            ILogEventService logEventService = appContext.getBean(LogEventService.class);
            IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
            try {
                logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/AddUserAjax", "CREATE", "Insert user : " + login, "", ""));
            } catch (CerberusException ex) {
                Logger.getLogger(UserService.class.getName()).log(Level.ERROR, null, ex);
            }

            response.getWriter().print(myUser.getLogin());
        } catch (CerberusException myexception) {
            response.getWriter().print(myexception.getMessageError().getDescription());
        }
    }
}
