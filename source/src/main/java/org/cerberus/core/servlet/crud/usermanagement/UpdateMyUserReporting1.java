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
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.crud.entity.User;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IUserService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.crud.service.impl.UserService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

@WebServlet(name = "UpdateMyUserReporting1", urlPatterns = {"/UpdateMyUserReporting1"})
public class UpdateMyUserReporting1 extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(UpdateMyUserReporting1.class);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        JSONObject jsonResponse = new JSONObject();

        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);

        String login = request.getUserPrincipal().getName();
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();
        /**
         * Parse parameters - list of values
         */
        List<String> tcstatusList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("tcstatus"), null, charset);
        List<String> groupList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("group"), null, charset);
        List<String> isActiveList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("isActive"), null, charset);
        List<String> priorityList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("priority"), null, charset);

        List<String> countryList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("country"), null, charset);
        List<String> browserList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("browser"), null, charset);
        List<String> tcestatusList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("tcestatus"), null, charset);

        //environment
        List<String> environmentList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("environment"), null, charset);
        List<String> projectList = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("project"), null, charset);
        /**
         * Parse parameters - free text
         */
        String ip = StringEscapeUtils.escapeHtml4(request.getParameter("ip"));
        String port = StringEscapeUtils.escapeHtml4(request.getParameter("port"));
        String tag = StringEscapeUtils.escapeHtml4(request.getParameter("tag"));
        String browserversion = StringEscapeUtils.escapeHtml4(request.getParameter("browserversion"));
        String comment = StringEscapeUtils.escapeHtml4(request.getParameter("comment"));

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IUserService userService = appContext.getBean(UserService.class);

        try {
            User user = userService.findUserByKey(login);
            if (user != null) {
                JSONObject preferences = new JSONObject();

                if (tcstatusList != null) {
                    preferences.put("s", tcstatusList);
                }
                if (groupList != null) {
                    preferences.put("g", groupList);
                }
                if (isActiveList != null) {
                    preferences.put("a", isActiveList);
                }
                if (priorityList != null) {
                    preferences.put("pr", priorityList);
                }
                if (countryList != null) {
                    preferences.put("co", countryList);
                }
                if (browserList != null) {
                    preferences.put("b", browserList);
                }
                if (tcestatusList != null) {
                    preferences.put("es", tcestatusList);
                }
                if (environmentList != null) {
                    preferences.put("e", environmentList);
                }
                if (projectList != null) {
                    preferences.put("prj", projectList);
                }

                if (!StringUtil.isEmptyOrNull(ip)) {
                    preferences.put("ip", ip);
                }
                if (!StringUtil.isEmptyOrNull(port)) {
                    preferences.put("p", port);
                }
                if (!StringUtil.isEmptyOrNull(tag)) {
                    preferences.put("t", tag);
                }
                if (!StringUtil.isEmptyOrNull(browserversion)) {
                    preferences.put("br", browserversion);
                }
                if (!StringUtil.isEmptyOrNull(comment)) {
                    preferences.put("cm", comment);
                }

                user.setReportingFavorite(preferences.toString());
                userService.updateUser(user); //TODO: when converting to the new standard this should return an answer
                //re-send the updated preferences

                jsonResponse.put("preferences", preferences);

                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Execution reporting filters ").replace("%OPERATION%", "Update"));

                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                logEventService.createForPrivateCalls("/UpdateMyUserReporting1", "UPDATE", LogEvent.STATUS_INFO, "Update user reporting preference for user: " + login, request);
            } else {
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to update User was not found!"));
            }

            jsonResponse.put("messageType", msg.getMessage().getCodeString());
            jsonResponse.put("message", msg.getDescription());
        } catch (JSONException ex) {
            LOG.warn(ex);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        } catch (CerberusException ex) {
            LOG.warn(ex);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        }

        response.getWriter().print(jsonResponse);
        response.getWriter().flush();
    }
}
