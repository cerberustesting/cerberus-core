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
package org.cerberus.servlet.crud.testdata;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest; 
import javax.servlet.http.HttpServletResponse; 
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.crud.service.ITestDataLibDataService;  
import org.cerberus.enums.MessageEventEnum; 
import org.cerberus.util.answer.AnswerItem; 
import org.cerberus.util.answer.AnswerList; 
import org.cerberus.util.answer.AnswerUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet responsible for getting information from the subdata entries
 * @author FNogueira
 */
@WebServlet(name = "ReadTestDataLibData", urlPatterns = {"/ReadTestDataLibData"})
public class ReadTestDataLibData extends HttpServlet {

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
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        // Default message to unexpected error.
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        
        AnswerItem answer = new AnswerItem(msg);
        
        response.setContentType("application/json");
        
        
         /**
         * Parsing and securing all required parameters.
         */
        Integer testdatalibid = 0;
        boolean testdatalibid_error = true;
        try {
            if (request.getParameter("testdatalibid") != null && !request.getParameter("testdatalibid").isEmpty()) {
                testdatalibid = Integer.valueOf(request.getParameter("testdatalibid"));
                testdatalibid_error = false;
            }
        } catch (NumberFormatException ex) {
            org.apache.log4j.Logger.getLogger(ReadTestDataLibData.class.getName()).log(org.apache.log4j.Level.WARN, null, ex);
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Library Data"));
            msg.setDescription(msg.getDescription().replace("%OPERATION%", "Read by test data lib id"));
            msg.setDescription(msg.getDescription().replace("%REASON%", "Test data library must be an integer value."));
            answer.setResultMessage(msg);
            testdatalibid_error = true;
        }
        
        try {

            JSONObject jsonResponse;
            if (request.getParameter("testdatalibid") != null && !testdatalibid_error) {
                //returns sub-data entries with basis on the test data library id
                answer = readById(appContext, testdatalibid);                   
            } else if (request.getParameter("name") != null) {
                //return sub-data entries with basis on the name
                String name = policy.sanitize(request.getParameter("name"));
                answer = readByName(appContext, name);
            } else {
                //return all entries
                answer = readAll(appContext);                
            }
 
            jsonResponse = (JSONObject) answer.getItem();
            
            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());
            response.getWriter().print(jsonResponse.toString());

        } catch (JSONException e) {
            org.apache.log4j.Logger.getLogger(ReadTestDataLibData.class.getName()).log(org.apache.log4j.Level.ERROR, null, e);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
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

    private AnswerItem readById(ApplicationContext appContext, int testDatalib) throws JSONException {
        JSONObject jsonResponse = new JSONObject();
        ITestDataLibDataService testDataLibDataService = appContext.getBean(ITestDataLibDataService.class);
        AnswerList answer = testDataLibDataService.readByKey(testDatalib);

        //retrieves the data for the entry
        JSONArray jsonArray = new JSONArray();

        for (TestDataLibData subdata : (List<TestDataLibData>) answer.getDataList()) {
            jsonArray.put(convertTestDataLibDataToJSONObject(subdata));
        }

        jsonResponse.put("contentTable", jsonArray);

        AnswerItem item = new AnswerItem();
        item.setItem(jsonResponse);
        item.setResultMessage(answer.getResultMessage());

        return item;
    }
    
    private JSONObject convertTestDataLibDataToJSONObject(TestDataLibData subdata) throws JSONException {
        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(subdata));
        
        return result;
    }

    private AnswerItem readByName(ApplicationContext appContext, String testDataLibName) throws JSONException {
        JSONObject jsonResponse = new JSONObject();
        ITestDataLibDataService testDataLibDataService = appContext.getBean(ITestDataLibDataService.class);
        AnswerList answer = testDataLibDataService.readByName(testDataLibName);

        //retrieves the data for the entry
        JSONArray jsonArray = new JSONArray();

        for (TestDataLibData subdata : (List<TestDataLibData>) answer.getDataList()) {
            jsonArray.put(convertTestDataLibDataToJSONObject(subdata));
        }

        jsonResponse.put("contentTable", jsonArray);
        jsonResponse.put("iTotalRecords", answer.getTotalRows());
        jsonResponse.put("iTotalDisplayRecords", answer.getTotalRows());

        AnswerItem item = new AnswerItem();
        item.setItem(jsonResponse);
        item.setResultMessage(answer.getResultMessage());

        return item;
    }

     
    private AnswerItem readAll(ApplicationContext appContext) throws JSONException {
        JSONObject jsonResponse = new JSONObject();
        ITestDataLibDataService testDataLibDataService = appContext.getBean(ITestDataLibDataService.class);
        AnswerList answer = testDataLibDataService.readAll();

        //retrieves the data for the entry
        JSONArray jsonArray = new JSONArray();
        Gson gson = new Gson();
        for (TestDataLibData subData : (List<TestDataLibData>) answer.getDataList()) {
            jsonArray.put(new JSONObject(gson.toJson(subData)));
        }

        jsonResponse.put("contentTable", jsonArray);

        AnswerItem item = new AnswerItem();
        item.setItem(jsonResponse);
        item.setResultMessage(answer.getResultMessage());

        return item;
    }
}
