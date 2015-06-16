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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.factory.impl.FactoryLogEvent;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.ITestDataLibDataService;
import org.cerberus.service.ITestDataLibService;
import org.cerberus.service.impl.LogEventService;
import org.cerberus.util.answer.Answer;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * TODO:FN commetn
 * @author FNogueira
 */
public class DeleteTestDataLib extends HttpServlet {

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
            try {
                //common attributes
                int testDataLibID = Integer.parseInt(request.getParameter("id"));

                ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

                //removes the testdatalibentry
                ITestDataLibService libService = appContext.getBean(ITestDataLibService.class);
                Answer answer = libService.deleteTestDataLib(testDataLibID);


                //removes all the subdata for the testdatalibentry
//                ITestDataLibDataService subDataService = appContext.getBean(ITestDataLibDataService.class);
//                subDataService.deleteByTestDataLibID(testDataLibID);


                jsonResponse.put("messageType", answer.getMessageType());
                jsonResponse.put("message", answer.getMessageDescription());

                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
                try {
                    logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/DeketeTestDataLib",
                            "DELETE", "Delete TestDataLib  By ID: " + testDataLibID, "", ""));
                } catch (CerberusException ex) {
                    org.apache.log4j.Logger.getLogger(AddTestDataLib.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);
                }



            //TODO:FN enviar mensagem a dizer que foi eliminado c successo
            //response.sendRedirect("TestDataLib2.jsp"); //TODO:FN mudar este nome para o TestDataLib.jsp, será q preciso mm de reencaminhar?
            } catch (CerberusException ex) {
                Logger.getLogger(DeleteTestDataLib.class.getName()).log(Level.SEVERE, null, ex); //TODO:FN ver esta excepcao
                jsonResponse.put("messageType", "danger");
                jsonResponse.put("message", MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR.getDescription() + ex.getMessage());
            }
        } catch (JSONException ex) {
            Logger.getLogger(DeleteTestDataLib.class.getName()).log(Level.SEVERE, null, ex);
        }

        response.setContentType("application/json");
        response.getWriter().print(jsonResponse); //TODO:FN pode ser null?
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
