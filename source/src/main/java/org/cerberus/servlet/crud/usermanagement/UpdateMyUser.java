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
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.cerberus.crud.entity.User;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.impl.FactoryUserGroup;
import org.cerberus.log.MyLogger;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.IUserService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.crud.service.impl.UserService;
import org.cerberus.util.ParameterParserUtil;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.cerberus.crud.factory.IFactoryUserGroup;

/**
 * @author ip100003
 */
@WebServlet(name = "UpdateMyUser", urlPatterns = {"/UpdateMyUser"})
public class UpdateMyUser extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //TODO create class Validator to validate all parameter from page
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String charset = request.getCharacterEncoding();
        String login = request.getUserPrincipal().getName();
        String column = request.getParameter("column");
        String value = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("value"), "", charset);

        MyLogger.log(UpdateMyUser.class.getName(), Level.INFO, "value : " + value + " column : " + column + " login : " + login);

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IUserService userService = appContext.getBean(UserService.class);
        
        User myUser;
        try {
            myUser = userService.findUserByKey(login);
            switch (column) {
                case "name":
                    myUser.setName(value);
                    break;
                case "team":
                    myUser.setTeam(value);
                    break;
                case "defaultSystem":
                    myUser.setDefaultSystem(value);
                    request.getSession().setAttribute("MySystem", value);
                    break;
                case "email":
                    myUser.setEmail(value);
                    break;
                case "language":
                    myUser.setLanguage(value);
                    request.getSession().setAttribute("MyLang", value);
                    break;
                case "userPreferences":
                    myUser.setUserPreferences(value);
                    break;
            }
            try {

                userService.updateUser(myUser);

                /**
                 * Adding Log entry.
                 */
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                logEventService.createPrivateCalls("/UpdateMyUser", "UPDATE", "Updated user : " + login, request);

                response.getWriter().print(value);
            } catch (CerberusException ex) {
                response.getWriter().print(ex.getMessageError().getDescription());
            }
        } catch (CerberusException ex) {
            response.getWriter().print(ex.getMessageError().getDescription());
        }

    }
}
