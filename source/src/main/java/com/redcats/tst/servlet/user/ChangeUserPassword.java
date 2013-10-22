/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.servlet.user;

import com.redcats.tst.entity.User;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.service.IUserService;
import com.redcats.tst.service.impl.UserService;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author ip100003
 */
@WebServlet(name = "ChangeUserPassword", urlPatterns = {"/ChangeUserPassword"})
public class ChangeUserPassword extends HttpServlet {

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

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IUserService userService = appContext.getBean(UserService.class);

        User myUser;
        try {
            myUser = userService.findUserByKey(login);
            try {
                userService.updateUserPassword(myUser, currentPassword, newPassword, confirmPassword);
                response.sendRedirect("Homepage");
            } catch (CerberusException ex2) {
                response.setContentType("text/html");
                response.getWriter().print(ex2.getMessageError().getDescription());
            }
        } catch (CerberusException ex1) {
            response.setContentType("text/html");
            response.getWriter().print(ex1.getMessageError().getDescription());
        }


    }
}
