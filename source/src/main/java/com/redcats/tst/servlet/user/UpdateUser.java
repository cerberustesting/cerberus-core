/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.servlet.user;

import com.redcats.tst.entity.Group;
import com.redcats.tst.entity.User;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryGroup;
import com.redcats.tst.factory.IFactoryLogEvent;
import com.redcats.tst.factory.impl.FactoryGroup;
import com.redcats.tst.factory.impl.FactoryLogEvent;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.service.ILogEventService;
import com.redcats.tst.service.IUserGroupService;
import com.redcats.tst.service.IUserService;
import com.redcats.tst.service.impl.LogEventService;
import com.redcats.tst.service.impl.UserGroupService;
import com.redcats.tst.service.impl.UserService;
import org.apache.log4j.Level;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

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
                        Logger.getLogger(UserService.class.getName()).log(Level.ERROR, null, ex);
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
