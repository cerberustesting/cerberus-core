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
package org.cerberus.servlet.crud.transversaltables;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.cerberus.crud.entity.Label;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.Parameter;
import org.cerberus.crud.factory.IFactoryLabel;
import org.cerberus.crud.service.ILabelService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryParameter;
import org.cerberus.crud.factory.impl.FactoryParameter;
import org.cerberus.log.MyLogger;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.crud.service.impl.ParameterService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.servlet.ServletUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author ip100003
 */
@WebServlet(name = "UpdateParameter", urlPatterns = {"/UpdateParameter"})
public class UpdateParameter extends HttpServlet {

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
        //TODO create class Validator to validate all parameter from page
        JSONObject jsonResponse = new JSONObject();

        boolean userHasPermissions = request.isUserInRole("Administrator");

        Answer ans = new Answer();
        MessageEvent msg;
        if(!userHasPermissions){
            /**
             * User has no permission to do that
             */
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Label")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "Label does not exist."));
            ans.setResultMessage(msg);
        }
        String param = request.getParameter("id");
        String valueCerberus = request.getParameter("valueCerberus");

        String valueSystem = request.getParameter("valueSystem");

        String mySystem = request.getParameter("system");

        MyLogger.log(UpdateParameter.class.getName(), Level.DEBUG, "System : " + mySystem + " valueSystem : " + valueSystem + " valueCerberus : " + valueCerberus + " param : " + param);

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IParameterService parameterService = appContext.getBean(ParameterService.class);
        IFactoryParameter parameterFactory = appContext.getBean(FactoryParameter.class);

        try {

            Parameter  myParameter = parameterService.findParameterByKey(param, "");
            myParameter.setValue(valueCerberus);

            Parameter myParameter2 = parameterFactory.create(mySystem, param, valueSystem, "");

            parameterService.saveParameter(myParameter);
            parameterService.saveParameter(myParameter2);

            /**
             * Adding Log entry.
             */
            ILogEventService logEventService = appContext.getBean(LogEventService.class);
            logEventService.createPrivateCalls("/UpdateParameter", "UPDATE", "Update parameter : " + param, request);

            response.getWriter().print(valueCerberus);
            /**
             * Formating and returning the json result.
             */
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Parameter").replace("%OPERATION%", "SELECT"));
            ans.setResultMessage(msg);
            jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", ans.getResultMessage().getDescription());

            response.getWriter().print(jsonResponse);
            response.getWriter().flush();

        } catch (CerberusException ex) {
            response.getWriter().print(ex.getMessageError().getDescription());
        }

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
            Logger.getLogger(UpdateLabel.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(UpdateLabel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
            Logger.getLogger(UpdateLabel.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(UpdateLabel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
