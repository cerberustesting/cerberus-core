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
package org.cerberus.servlet.testdatalib;

import java.io.IOException; 
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.TestDataLib; 
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.factory.IFactoryTestDataLib;
import org.cerberus.factory.impl.FactoryLogEvent; 
import org.cerberus.service.ILogEventService;
import org.cerberus.service.ITestDataLibService;
import org.cerberus.service.impl.LogEventService;
import org.cerberus.util.answer.Answer;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Handles the UPDATE operation for test data lib entries.
 * 
 * @author FNogueira
 */
@WebServlet(name = "UpdateTestDataLib", urlPatterns = {"/UpdateTestDataLib"})
public class UpdateTestDataLib extends HttpServlet {

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
            throws ServletException, IOException {
        JSONObject jsonResponse = new JSONObject();
        try {
            int testDataLibID = Integer.parseInt(request.getParameter("testDataLibIDEdit"));
            String name = request.getParameter("NameEdit");
            String type = request.getParameter("TypeEdit");
            String group = request.getParameter("GroupEdit");
            
            String description = request.getParameter("EntryDescriptionEdit");
            String system = request.getParameter("System");
            String environment = request.getParameter("Environment");
            String country = request.getParameter("Country");
            
            String database = request.getParameter("DatabaseEdit");
            String script = request.getParameter("ScriptEdit");
            
            String servicePath = request.getParameter("ServicePathEdit");
            String method = request.getParameter("MethodEdit");
            String envelope = request.getParameter("EnvelopeEdit");
            
            
            
            //specific attributes
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IFactoryTestDataLib factoryLibService = appContext.getBean(IFactoryTestDataLib.class);
            
            ITestDataLibService libService = appContext.getBean(ITestDataLibService.class);

            TestDataLib lib = factoryLibService.create(testDataLibID, name, system, environment, country, group, type, database, 
                    script, servicePath, method, envelope, description);
            
            //updates the testdatalib
            Answer answer = libService.updateTestDataLib(lib);
            
            
            //  Adding Log entry.
            ILogEventService logEventService = appContext.getBean(LogEventService.class);
            IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
            try {
                logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/UpdateTestDataLib", "UPDATE", 
                        "Update TestDataLib  : " + request.getParameter("Name"), "", ""));
            } catch (CerberusException ex) { 
                org.apache.log4j.Logger.getLogger(UpdateTestDataLib.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);      
            }
                        
            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());
            
            response.setContentType("application/json");
            response.getWriter().print(jsonResponse);
            response.getWriter().flush();
            
        } catch (JSONException ex) {
            Logger.getLogger(UpdateTestDataLib.class.getName()).log(Level.SEVERE, null, ex);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.setContentType("application/json"); 
            MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("{'messageType':'").append(msg.getCode()).append("', ");
            errorMessage.append(" 'message': '");
            errorMessage.append(msg.getDescription().replace("%DESCRIPTION%", "Unable to check the status of your request! Try later or - Open a bug or ask for any new feature \n" +
            "<a href=\"https://github.com/vertigo17/Cerberus/issues/\" target=\"_blank\">here</a>"));
            errorMessage.append("'}");
            response.getWriter().print(errorMessage.toString());       
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
        processRequest(request, response);
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
        processRequest(request, response);
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
