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
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.crud.entity.TestDataLibDataUpdate;
import org.cerberus.crud.factory.IFactoryLogEvent;
import org.cerberus.crud.factory.IFactoryTestDataLibData;
import org.cerberus.crud.factory.impl.FactoryLogEvent;
import org.cerberus.log.MyLogger;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ITestDataLibDataService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.util.answer.Answer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Handles the modification of subdata entries.
 *
 * @author FNogueira
 */
@WebServlet(name = "UpdateTestDataLibData", urlPatterns = {"/UpdateTestDataLibData"})
public class UpdateTestDataLibData extends HttpServlet {

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
            //try {
            //removes all the subdata for the testdatalibentry
            //common attributes
            int testDataLibID = Integer.parseInt(request.getParameter("id"));
            String type = request.getParameter("type");
            String data = request.getParameter("data");

            JSONObject dataToEdit = new JSONObject(data);
            JSONObject obj;

            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IFactoryTestDataLibData factoryLibService = appContext.getBean(IFactoryTestDataLibData.class);

            //checks if the subdataentry can be deleted
            ITestDataLibDataService subDataService = appContext.getBean(ITestDataLibDataService.class);

            //removes the entries selected by the user
            ArrayList<String> entriesToRemove = new ArrayList<String>();
            if (dataToEdit.has("remove")) {
                JSONArray arrayToRemove = (JSONArray) dataToEdit.get("remove");
                for (int i = 0; i < arrayToRemove.length(); i++) {
                    //removes the testdatalibentry
                    obj = arrayToRemove.getJSONObject(i);
                    entriesToRemove.add(obj.get("Subdata").toString());
                }
            }

            //updates the selected entries
            ArrayList<TestDataLibDataUpdate> entriesToUpdate = new ArrayList<TestDataLibDataUpdate>();
            if (dataToEdit.has("update")) {
                //subDataService.
                JSONArray arrayToeditInsert = (JSONArray) dataToEdit.get("update");
                for (int i = 0; i < arrayToeditInsert.length(); i++) {
                    //TestDataLibData subData = subDataService.createTestDataLibData();
                    obj = arrayToeditInsert.getJSONObject(i);
                    TestDataLibData item = factoryLibService.create(testDataLibID, type, obj.get("Subdata").toString(),
                            obj.get("Value").toString(),
                            obj.get("Description").toString());
                    TestDataLibDataUpdate updateItem = new TestDataLibDataUpdate(item, obj.get("Subdata_original").toString());                            
                    entriesToUpdate.add(updateItem);
                }
            }
            //inserts new subdataentries
            ArrayList<TestDataLibData> entriesToInsert = new ArrayList<TestDataLibData>();
            if (dataToEdit.has("insert")) {
                //subDataService.
                JSONArray arrayToeditInsert = (JSONArray) dataToEdit.get("insert");
                for (int i = 0; i < arrayToeditInsert.length(); i++) {
                    obj = arrayToeditInsert.getJSONObject(i);
                    TestDataLibData item = factoryLibService.create(testDataLibID, type, obj.get("Subdata").toString(),
                            obj.get("Value").toString(),
                            obj.get("Description").toString());
                    entriesToInsert.add(item);
                }

            }

            //performs the operations selected by the user
            Answer answer = subDataService.cudTestDataLibData(testDataLibID, entriesToInsert, entriesToUpdate, entriesToRemove);

            //  Adding Log entry.
            if(answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())){
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
                logEventService.createPrivateCalls("/UpdateTestDataLibData", "UPDATE", "Update TestDataLibData entries for id: " + testDataLibID, request);
            }
            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());

            response.setContentType("application/json");
            response.getWriter().print(jsonResponse);
            response.getWriter().flush();

        } catch (JSONException ex) {
            MyLogger.log(UpdateTestDataLibData.class.getName(), org.apache.log4j.Level.FATAL, "" + ex);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.setContentType("application/json");
            MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("{'messageType':'").append(msg.getCode()).append("', ");
            errorMessage.append(" 'message': '");
            errorMessage.append(msg.getDescription().replace("%DESCRIPTION%", "Unable to check the status of your request! Try later or - Open a bug or ask for any new feature \n"
                    + "<a href=\"https://github.com/vertigo17/Cerberus/issues/\" target=\"_blank\">here</a>"));
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
