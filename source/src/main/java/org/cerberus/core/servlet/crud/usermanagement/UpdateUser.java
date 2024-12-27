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
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.UserRole;
import org.cerberus.core.crud.entity.User;
import org.cerberus.core.crud.entity.UserSystem;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.factory.IFactoryUserSystem;
import org.cerberus.core.crud.factory.impl.FactoryUserRole;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IUserService;
import org.cerberus.core.crud.service.IUserSystemService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.crud.service.impl.UserRoleService;
import org.cerberus.core.crud.service.impl.UserService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.cerberus.core.crud.factory.IFactoryUserRole;
import org.cerberus.core.crud.service.IUserRoleService;

/**
 * @author ryltar
 */
@WebServlet(name = "UpdateUser", urlPatterns = {"/UpdateUser"})
public class UpdateUser extends HttpServlet {
    
    private static final Logger LOG = LogManager.getLogger(UpdateUser.class);
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, IndexOutOfBoundsException {
        //TODO create class Validator to validate all parameter from page

        JSONObject jsonResponse = new JSONObject();
        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer ans = new Answer();
        Answer finalAnswer = new Answer(msg1);
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);
        
        String id = request.getParameter("id");
        String login = request.getParameter("login");
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String team = request.getParameter("team");
        String systems = request.getParameter("systems");
        String requests = request.getParameter("request");
        String roles = request.getParameter("roles");
        String apiKey = request.getParameter("apiKey");
        String att01 = request.getParameter("attribute01");
        String att02 = request.getParameter("attribute02");
        String att03 = request.getParameter("attribute03");
        String att04 = request.getParameter("attribute04");
        String att05 = request.getParameter("attribute05");
        String comment = request.getParameter("comment");
        String defaultSystem = request.getParameter("defaultSystem");
        
        if (StringUtil.isEmptyOrNull(login) || StringUtil.isEmptyOrNull(id)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "User")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "User login is missing."));
            ans.setResultMessage(msg);
            
        } else {
            LOG.info("Updating user " + login);
            
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IUserService userService = appContext.getBean(UserService.class);
            IUserRoleService userRoleService = appContext.getBean(UserRoleService.class);
            IFactoryUserSystem userSystemFactory = appContext.getBean(IFactoryUserSystem.class);
            IUserSystemService userSystemService = appContext.getBean(IUserSystemService.class);
            
            IFactoryUserRole factoryRole = new FactoryUserRole();
            
            User myUser;
            List<UserRole> newRoles = null;
            List<UserSystem> newSystems = null;
            try {
                myUser = userService.findUserByKey(id);
                
                List<String> listRole = new ArrayList<>();
                JSONArray GroupArray = new JSONArray(request.getParameter("roles"));
                for (int i = 0; i < GroupArray.length(); i++) {
                    listRole.add(GroupArray.getString(i));
                }
                
                newRoles = new ArrayList<>();
                for (String role : listRole) {
                    newRoles.add(factoryRole.create(role));
                }
                
                myUser.setLogin(login);
                myUser.setName(name);
                myUser.setTeam(team);
                myUser.setAttribute01(att01);
                myUser.setAttribute02(att02);
                myUser.setAttribute03(att03);
                myUser.setAttribute04(att04);
                myUser.setAttribute05(att05);
                myUser.setApiKey(apiKey);
                myUser.setComment(comment);
                myUser.setUsrModif(request.getRemoteUser());
                newSystems = new ArrayList<>();
                
                JSONArray SystemArray = new JSONArray(request.getParameter("systems"));
                
                List<String> listSystem = new ArrayList<>();
                for (int i = 0; i < SystemArray.length(); i++) {
                    listSystem.add(SystemArray.getString(i));
                }
                
                for (String system : listSystem) {
                    newSystems.add(userSystemFactory.create(login, system));
                }
                
                myUser.setDefaultSystem(defaultSystem);
                myUser.setRequest(requests);
                myUser.setEmail(email);
                
                try {
                    
                    ans = userService.update(myUser);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                    
                    if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        /**
                         * Update was successful. Adding Log entry.
                         */
                        ILogEventService logEventService = appContext.getBean(LogEventService.class);
                        logEventService.createForPrivateCalls("/UpdateUser", "UPDATE", LogEvent.STATUS_INFO, "Updated user : ['" + login + "']", request);
                        
                        if (!newRoles.isEmpty()) {
                            userRoleService.updateUserRoles(myUser, newRoles);
                        }
                        
                        if (!newSystems.isEmpty()) {
                            request.getSession().setAttribute("MySystem", newSystems.get(0).getSystem());
                            userSystemService.updateUserSystems(myUser, newSystems);
                        }
                        
                    }

                    /**
                     * Adding Log entry.
                     */
//                    AnswerUtil.agregateAnswer(finalAnswer, ans);
                    
                    jsonResponse.put("messageType", finalAnswer.getResultMessage().getMessage().getCodeString());
                    jsonResponse.put("message", finalAnswer.getResultMessage().getDescription());
                    
                    response.getWriter().print(jsonResponse);
                    
                } catch (CerberusException ex) {
                    response.getWriter().print(ex.getMessageError().getDescription());
                }
            } catch (CerberusException ex) {
                response.getWriter().print(ex.getMessageError().getDescription());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
