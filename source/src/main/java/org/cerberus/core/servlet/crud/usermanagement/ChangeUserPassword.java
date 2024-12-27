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

import org.cerberus.core.crud.entity.User;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.service.IUserService;
import org.cerberus.core.crud.service.impl.UserService;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author ip100003
 */
@WebServlet(name = "ChangeUserPassword", urlPatterns = {"/ChangeUserPassword"})
public class ChangeUserPassword extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ChangeUserPassword.class);
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String login = request.getParameter("login");
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        String resetPasswordToken = request.getParameter("resetPasswordToken");

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IUserService userService = appContext.getBean(UserService.class);

        User myUser;

        try {
            JSONObject jsonResponse = new JSONObject();

            try {
                myUser = userService.findUserByKey(login);
                
                AnswerItem ansPassword = userService.updateUserPassword(myUser, currentPassword, newPassword, confirmPassword, resetPasswordToken);
                
                jsonResponse.put("messageType", ansPassword.getResultMessage().getMessage().getCodeString());
                jsonResponse.put("message", ansPassword.getResultMessage().getDescription()); 
                
            } catch (CerberusException ex1) {
                //TODO:FN this need to be refactored //findUserByKey should return answer 
                jsonResponse.put("messageType", "KO"); 
                jsonResponse.put("message", ex1.toString());
            }


            response.setContentType("application/json");
            response.getWriter().print(jsonResponse.toString());

            } catch (JSONException e) {
                LOG.warn(e);
                //returns a default error message with the json format that is able to be parsed by the client-side
                response.setContentType("application/json");
                response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
            }

        }
    }
