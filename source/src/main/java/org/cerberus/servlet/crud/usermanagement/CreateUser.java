/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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

import org.cerberus.crud.entity.User;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.crud.entity.UserGroup;
import org.cerberus.crud.entity.UserSystem;
import org.cerberus.crud.factory.IFactoryUser;
import org.cerberus.crud.factory.IFactoryUserSystem;
import org.cerberus.crud.factory.impl.FactoryUserGroup;
import org.cerberus.crud.service.*;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.crud.service.impl.ParameterService;
import org.cerberus.crud.service.impl.UserGroupService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import org.cerberus.crud.factory.IFactoryUserGroup;
import org.cerberus.util.answer.AnswerUtil;
import org.cerberus.service.notification.INotificationService;

/**
 * @author bcivel
 */
public class CreateUser extends HttpServlet {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(CreateUser.class);
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, CerberusException, JSONException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        JSONObject jsonResponse = new JSONObject();
        Answer ans = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();

        IParameterService parameterService = appContext.getBean(ParameterService.class);
        INotificationService notificationService = appContext.getBean(INotificationService.class);
        String system = "";

        String password = parameterService.findParameterByKey("cerberus_accountcreation_defaultpassword", system).getValue();
        String newPassword = ParameterParserUtil.parseStringParam(request.getParameter("newPassword"), "Y");

        String login = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("login"), "", charset);
        String email = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("email"), "", charset);
        String defaultSystem = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("defaultSystem"), "", charset);
        String name = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("name"), "", charset);
        String team = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("team"), "", charset);

        JSONArray JSONSystems = new JSONArray(ParameterParserUtil.parseStringParam(request.getParameter("systems"), null));
        JSONArray JSONGroups = new JSONArray(ParameterParserUtil.parseStringParam(request.getParameter("groups"), null));

        boolean userHasPermissions = request.isUserInRole("Administrator");

        /**
         * Checking all constrains before calling the services.
         */
        if (StringUtil.isNullOrEmpty(login)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "User")
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "User name is missing!"));
            ans.setResultMessage(msg);
        } else if (!userHasPermissions) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "User")
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "You don't have the right to do that"));
            ans.setResultMessage(msg);
        } else {
            /**
             * All data seems cleans so we can call the services.
             */

            IUserService userService = appContext.getBean(IUserService.class);
            IFactoryUser factoryUser = appContext.getBean(IFactoryUser.class);
            IFactoryUserGroup factoryGroup = new FactoryUserGroup();
            IFactoryUserSystem userSystemFactory = appContext.getBean(IFactoryUserSystem.class);
            IUserGroupService userGroupService = appContext.getBean(UserGroupService.class);
            IUserSystemService userSystemService = appContext.getBean(IUserSystemService.class);

            LinkedList<UserGroup> newGroups = new LinkedList<>();
            for (int i = 0; i < JSONGroups.length(); i++) {
                newGroups.add(factoryGroup.create(login, JSONGroups.getString(i)));
            }
            LinkedList<UserSystem> newSystems = new LinkedList<>();
            for (int i = 0; i < JSONSystems.length(); i++) {
                newSystems.add(userSystemFactory.create(login, JSONSystems.getString(i)));
            }
            User userData = factoryUser.create(0, login, password, "", newPassword, name, team, "en", "", "", "", "", "", "", "", defaultSystem, email, null, null);

            ans = userService.create(userData);

            if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                /**
                 * Send Email to explain how to connect Cerberus if
                 * activateNotification is set to Y
                 */
                String sendNotification = parameterService.findParameterByKey("cerberus_notification_accountcreation_activatenotification", system).getValue();

                if (sendNotification.equalsIgnoreCase("Y")) {
                    Answer msgSent = new Answer(notificationService.generateAndSendAccountCreationEmail(userData));
                    ans = AnswerUtil.agregateAnswer(ans, msgSent);
                }
                /**
                 * Object updated. Adding Log entry.
                 */
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                logEventService.createForPrivateCalls("/CreateUser", "CREATE", "Create User : ['" + login + "']", request);

                ans = AnswerUtil.agregateAnswer(ans, userGroupService.updateGroupsByUser(userData, newGroups));
                ans = AnswerUtil.agregateAnswer(ans, userSystemService.updateSystemsByUser(userData, newSystems));
            }
        }

        /**
         * Formating and returning the json result.
         */
        jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", ans.getResultMessage().getDescription());

        response.getWriter().print(jsonResponse);
        response.getWriter().flush();
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (CerberusException ex) {
            LOG.warn(ex);
        } catch (JSONException ex) {
            LOG.warn(ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (CerberusException ex) {
            LOG.warn(ex);
        } catch (JSONException ex) {
            LOG.warn(ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
