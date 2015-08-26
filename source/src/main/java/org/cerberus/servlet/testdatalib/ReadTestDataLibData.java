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
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse; 
import org.cerberus.dto.TestDataLibDataDTO;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.TestDataLibData;
import org.cerberus.entity.TestDataLibTypeEnum; 
import org.cerberus.service.ITestDataLibDataService; 
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet responsible for getting information from the subdata entries
 * @author FNogueira
 */
@WebServlet(name = "ReadTestDataLibData", urlPatterns = {"/ReadTestDataLibData"})
public class ReadTestDataLibData extends HttpServlet {
    private final int ACTION_GETALL_BYID = 0;
    private final int ACTION_GETALL_BYNAME = 1;
    private final int ACTION_AUTOCOMPLETE = 2;
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

        try {
        
            JSONObject jsonResponse = new JSONObject();           
            
            AnswerItem answer = new AnswerItem(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
            if(request.getParameter("action") != null){ 
            
                int actionParameter = Integer.parseInt(request.getParameter("action"));
            
                switch (actionParameter){
                    case ACTION_GETALL_BYID:
                        //retrieves all content for the testdatalib selected 
                        int testDatalib = Integer.parseInt(request.getParameter("testDataLib"));  
                        String testDatalibType = request.getParameter("type");
                        answer = findAllTestDataLibContent(appContext, testDatalib, testDatalibType);
                        jsonResponse = (JSONObject)answer.getItem();      
                        break;
                    case ACTION_GETALL_BYNAME:
                        String testDataLibName = request.getParameter("testDataLibName");
                        answer = findAllTestDataLibContent(appContext, testDataLibName);
                        jsonResponse = (JSONObject)answer.getItem();    
                        break;
                    case ACTION_AUTOCOMPLETE:
                        String requestTerm = request.getParameter("subdata");
                        String testDataLib = request.getParameter("testDataLib");                 
                        int limit = Integer.parseInt(request.getParameter("limit"));                 
                        answer = findTestDataLibSubData(appContext, testDataLib, requestTerm, limit);
                        jsonResponse = (JSONObject) answer.getItem();
                        break;
                }
                        
            }
            

            
            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());
            
            response.setContentType("application/json");            
            response.getWriter().print(jsonResponse.toString()); 
            
        } catch (JSONException e) { 
            org.apache.log4j.Logger.getLogger(ReadTestDataLibData.class.getName()).log(org.apache.log4j.Level.ERROR, null, e); 
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

    
    private AnswerItem findAllTestDataLibContent(ApplicationContext appContext, int testDatalib, String testDatalibType) throws JSONException {
        JSONObject jsonResponse = new JSONObject();    
        ITestDataLibDataService testDataLibDataService = appContext.getBean(ITestDataLibDataService.class);
        AnswerList answer = testDataLibDataService.findTestDataLibDataListByTestDataLib(testDatalib);
        
        
        //retrieves the data for the entry
        JSONArray jsonArray = new JSONArray();
        
        for(TestDataLibData subdata: (List<TestDataLibData>)answer.getDataList()){
            jsonArray.put(convertTestDataLibDataToJSONObject(subdata, testDatalibType));   
        } 
        
        jsonResponse.put("TestDataLibDatas", jsonArray);     
        
        AnswerItem item = new AnswerItem();
        item.setItem(jsonResponse);
        item.setResultMessage(answer.getResultMessage());
        
        return item;
    }
    private JSONArray convertTestDataLibDataToJSONObject(TestDataLibData subdata, String testDataLibType) {
        JSONArray result = new JSONArray();
        result.put(subdata.getTestDataLibID());
        result.put(subdata.getSubData());
        if(testDataLibType.equals(TestDataLibTypeEnum.STATIC.getCode())){  
            result.put(subdata.getValue());
        }else if(testDataLibType.equals(TestDataLibTypeEnum.SQL.getCode())){
            result.put(subdata.getColumn());
        }else if(testDataLibType.equals(TestDataLibTypeEnum.SOAP.getCode())){
            result.put(subdata.getParsingAnswer());
        }else {
            result.put("");
        }
        result.put(subdata.getDescription());
        
        return result;
    }
    private AnswerItem findAllTestDataLibContent(ApplicationContext appContext, String testDataLibName) throws JSONException {
        JSONObject jsonResponse = new JSONObject();    
        ITestDataLibDataService testDataLibDataService = appContext.getBean(ITestDataLibDataService.class);
        AnswerList answer = testDataLibDataService.findTestDataLibDataByName(testDataLibName);
        
        
        //retrieves the data for the entry
        JSONArray jsonArray = new JSONArray();
        
        for(TestDataLibDataDTO subdata: (List<TestDataLibDataDTO>)answer.getDataList()){
            jsonArray.put(convertTestDataLibDataToJSONObject(subdata));   
        } 
        
        jsonResponse.put("TestDataLibDatas", jsonArray);     
        jsonResponse.put("iTotalRecords", answer.getTotalRows());
        jsonResponse.put("iTotalDisplayRecords", answer.getTotalRows());
        
        AnswerItem item = new AnswerItem();
        item.setItem(jsonResponse);
        item.setResultMessage(answer.getResultMessage());
        
        return item;
    }
    private JSONArray convertTestDataLibDataToJSONObject(TestDataLibDataDTO subdata) {
        JSONArray result = new JSONArray();
        result.put(subdata.getTestDataLibId());
        
        result.put(subdata.getSubdata());
        result.put(subdata.getData());
        result.put(subdata.getDescription());
        
        result.put(subdata.getType());
        result.put(subdata.getSystem());
        result.put(subdata.getEnvironment());
        result.put(subdata.getCountry());
        
        return result;
    }
    /**
     * Method that finds all the subdata entries that match the name that the user types and that are associated with specific library entry name
     * @param appContext - context 
     * @param testDataLib - name of the testdatalib
     * @param nameToSearch - name that is being searched
     * @param limit - limit number of records to be retrieved
     * @return data in the json format
     * @throws JSONException 
     */
    private AnswerItem findTestDataLibSubData(ApplicationContext appContext, String testDataLib, String nameToSearch,  int limit) throws JSONException {
        JSONObject object = new JSONObject();         
        AnswerItem ansItem = new AnswerItem();
        
        ITestDataLibDataService testDataService = appContext.getBean(ITestDataLibDataService.class);
        
        AnswerList answer = testDataService.findTestDataLibSubData(testDataLib, nameToSearch, limit);

        object.put("data", answer.getDataList()); 
        
        ansItem.setResultMessage(answer.getResultMessage());
        ansItem.setItem(object);
        
        return ansItem;
        
    }

}
