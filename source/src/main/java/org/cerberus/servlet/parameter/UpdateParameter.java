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
package org.cerberus.servlet.parameter;

import org.cerberus.entity.Parameter;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.factory.impl.FactoryLogEvent;
import org.cerberus.log.MyLogger;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.IParameterService;
import org.cerberus.service.impl.LogEventService;
import org.cerberus.service.impl.ParameterService;
import org.cerberus.service.impl.UserService;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.factory.IFactoryParameter;
import org.cerberus.factory.impl.FactoryParameter;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author ip100003
 */
@WebServlet(name = "UpdateParameter", urlPatterns = {"/UpdateParameter"})
public class UpdateParameter extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //TODO create class Validator to validate all parameter from page
        String param = request.getParameter("id");
        int columnPosition = Integer.parseInt(request.getParameter("columnPosition"));
        String value = request.getParameter("value").replaceAll("'", "");
        String mySystem = request.getParameter("system");

        MyLogger.log(UpdateParameter.class.getName(), Level.DEBUG, "System : " + mySystem + " value : " + value + " columnPosition : " + columnPosition + " param : " + param);

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IParameterService parameterService = appContext.getBean(ParameterService.class);
        IFactoryParameter parameterFactory = appContext.getBean(FactoryParameter.class);

        Parameter myParameter = null;
        try {
            switch (columnPosition) {
                case 1:
                    myParameter = parameterService.findParameterByKey(param, "");
                    myParameter.setValue(value);
                    break;
                case 2:
                    myParameter = parameterFactory.create(mySystem, param, value, "");
                    break;
            }
            try {
                parameterService.saveParameter(myParameter);

                /**
                 * Adding Log entry.
                 */
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
                try {
                    logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/UpdateParameterAjax", "UPDATE", "Update parameter : " + param, "", ""));
                } catch (CerberusException ex) {
                    Logger.getLogger(UserService.class.getName()).log(Level.ERROR, null, ex);
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
