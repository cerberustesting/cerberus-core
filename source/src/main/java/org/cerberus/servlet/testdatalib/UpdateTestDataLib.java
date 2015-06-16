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
import org.cerberus.entity.TestDataLib; 
import org.cerberus.factory.IFactoryTestDataLib;
import org.cerberus.service.ITestDataLibService;
import org.cerberus.util.answer.Answer;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
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
            /*String system[] = request.getParameterValues("System");
            String environment[] = request.getParameterValues("Environment");
            String country[] = request.getParameterValues("Country");*/
            
            String database = request.getParameter("DatabaseEdit");
            String script = request.getParameter("ScriptEdit");
            
            String servicePath = request.getParameter("ServicePathEdit");
            String method = request.getParameter("MethodEdit");
            String envelope = request.getParameter("EnvelopeEdit");
            
            
            
            //specific attributes
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IFactoryTestDataLib factoryLibService = appContext.getBean(IFactoryTestDataLib.class);
            
            ITestDataLibService libService = appContext.getBean(ITestDataLibService.class);
            TestDataLib lib = factoryLibService.create(testDataLibID, name, null, null, null, group, type, database, script, servicePath, method, envelope, description);
            
            Answer ans = libService.updateTestDataLib(lib);
                        
            jsonResponse.put("messageType", ans.getMessageType());
            jsonResponse.put("message", ans.getMessageDescription());
            
        } catch (JSONException ex) {
            Logger.getLogger(UpdateTestDataLib.class.getName()).log(Level.SEVERE, null, ex);
            //TODO:FN tratar mensagens
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
