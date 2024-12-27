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
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.User;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IUserService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.crud.service.impl.UserService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.answer.AnswerUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author ip100003
 */
@WebServlet(name = "UpdateMyUser", urlPatterns = {"/UpdateMyUser"})
public class UpdateMyUser extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(UpdateMyUser.class);
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //TODO create class Validator to validate all parameter from page
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();
        String login = request.getUserPrincipal().getName();
        String column = request.getParameter("column");
        String value = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("value"), "", charset);
        response.setContentType("application/json");
        JSONObject jsonResponse = new JSONObject();

        LOG.debug("value : " + value + " column : " + column + " login : " + login);

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IUserService userService = appContext.getBean(UserService.class);

        User myUser;
        try {
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

                userService.updateUser(myUser);

                /**
                 * Adding Log entry.
                 */
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                logEventService.createForPrivateCalls("/UpdateMyUser", "UPDATE", LogEvent.STATUS_INFO, "Updated user : " + login, request);

                jsonResponse.put("messageType", MessageEventEnum.GENERIC_OK.getCodeString());
                jsonResponse.put("message", MessageEventEnum.GENERIC_OK.getDescription());

            } catch (CerberusException ex) {
                jsonResponse.put("messageType", MessageEventEnum.GENERIC_ERROR.getCodeString());
                jsonResponse.put("message", ex.getMessageError().getDescription());
            }

        } catch (JSONException e) {
            LOG.warn(e);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.setContentType("application/json");
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        }
        response.getWriter().print(jsonResponse.toString());
    }
}
