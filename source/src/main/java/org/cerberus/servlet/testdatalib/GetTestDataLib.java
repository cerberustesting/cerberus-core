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
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.TestDataLib; 
import org.cerberus.entity.TestDataLibData;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.ITestDataLibDataService;
import org.cerberus.service.ITestDataLibService;
import org.cerberus.servlet.invariant.GetInvariantList;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author memiks
 */
@WebServlet(name = "GetTestDataLib", urlPatterns = {"/GetTestDataLib"})
public class GetTestDataLib extends HttpServlet {

    private ITestDataLibService testDataLibService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.process(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.process(request, response);
    }

    protected void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        try {
        
            JSONObject jsonResponse = null;
            String actionParameter = request.getParameter("action");
            
            
            AnswerItem answer = null;
            if(actionParameter == null){ //retrieves all data for testdatalib
                //select all entries for the testdatalib
                answer = findTestDataLibList(appContext, request, response);
                jsonResponse = (JSONObject)answer.getItem();                
            }else if(actionParameter.equals("getListOfGroupsPerType")){
                String Type  = request.getParameter("Type");
                jsonResponse = getListOfGroupsPerType(appContext, Type);          
                //TODO:FN handle this exceptions - MESSAGE??
            }else if("findAllTestDataLibContent".equals(actionParameter.trim())) {
                //retrieves all content for the testdatalib selected 
                int testDatalib = Integer.parseInt(request.getParameter("testDataLib")); //TODO:FN type message
                String testDatalibType = request.getParameter("type");
                answer = findAllTestDataLibContent(appContext, testDatalib, testDatalibType);
                jsonResponse = (JSONObject)answer.getItem();                
            }else if(actionParameter.equals("findTestDataLibByID")){
                int testDatalib = Integer.parseInt(request.getParameter("testDataLib")); //TODO:FN type message
                answer = findTestDataLibByID(appContext, testDatalib);
                jsonResponse = (JSONObject)answer.getItem();
            }

            if(answer == null){
                //TODO:FN handle messages here
            }else{
                //jsonResponse.put("messageType", answer.getMessageType());//TODO:FN type that is not for shown
                //jsonResponse.put("message", answer.getMessageDescription());            
            }
            response.setContentType("application/json");
            
            response.getWriter().print(jsonResponse.toString()); //TODO:FN pode ser null?
            
        } catch (JSONException e) {
            MyLogger.log(GetInvariantList.class.getName(), Level.FATAL, "" + e);
            response.setContentType("application/json"); //TODO:FN analisar estas excepcoes
            response.getWriter().print("{'messageType':'danger', 'message': 'An unexpected error occurred while processing your request!'}");            
            /*response.setContentType("text/html");
            response.getWriter().print(e.getMessage());*/
        }
    }

    private AnswerItem findTestDataLibList(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) throws IOException, BeansException, NumberFormatException, JSONException {
        AnswerItem item = new AnswerItem();
        JSONObject jsonResponse = new JSONObject();    
        testDataLibService = appContext.getBean(ITestDataLibService.class);
        
        int startPosition = Integer.valueOf(request.getParameter("iDisplayStart"));
        int length = Integer.valueOf(request.getParameter("iDisplayLength"));
        /*int sEcho  = Integer.valueOf(request.getParameter("sEcho"));*/
        
        String searchParameter = request.getParameter("sSearch");
        int columnToSortParameter = Integer.parseInt(request.getParameter("iSortCol_0"));
        String columnToSort[] = request.getParameter("sColumns").split(",");
        String columnName = columnToSort[columnToSortParameter];
        String sort = request.getParameter("sSortDir_0");
        AnswerList resp = testDataLibService.findTestDataLibListByCriteria(startPosition, length, columnName, sort, searchParameter, "");
        if(resp == null){//the service was unable to perform the query
            response.setContentType("text/html");
            response.getWriter().print(""); //TODO:FN colocar descricao
        }else{
            JSONArray jsonArray = new JSONArray();
            for (TestDataLib testDataLib : (List<TestDataLib>)resp.getDataList()) {
                jsonArray.put(convertTestDataLibToJSONObject(testDataLib));
            }
            //recordsFilterd do lado do servidor    
            jsonResponse.put("TestDataLib", jsonArray);
            jsonResponse.put("iTotalRecords", resp.getTotalRows());
            jsonResponse.put("iTotalDisplayRecords", resp.getTotalRows());
            //recordsFiltered
        }
        item.setItem(jsonResponse);
        item.setResultMessage(resp.getResultMessage());
        return item;
    }

    /*private JSONArray findAllTestDataLibToJSON(int startPosition, int length) throws JSONException, CerberusException {
        JSONArray jsonResponse = new JSONArray();
        for (TestDataLib testDataLib : testDataLibService.findTestDataLibListByCriteria(0, 100, "TestDataLibID", "ASC", "", "")) {
            jsonResponse.put(convertTestDataLibToJSONObject(testDataLib));
        }
        
        return jsonResponse;
    }*/

    private JSONArray convertTestDataLibToJSONObject(TestDataLib testDataLib) throws JSONException {
        JSONArray result = new JSONArray();
        result.put(testDataLib.getTestDataLibID());
        result.put(testDataLib.getName());
        result.put(testDataLib.getSystem());
        result.put(testDataLib.getEnvironment());
        result.put(testDataLib.getCountry());
        result.put(testDataLib.getGroup());
        result.put(testDataLib.getType());
        result.put(testDataLib.getDatabase());
        result.put(testDataLib.getScript());
        result.put(testDataLib.getServicePath());
        result.put(testDataLib.getMethod());
        result.put(testDataLib.getEnvelope());
        result.put(testDataLib.getDescription());
        return result;
    }

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
        if(testDataLibType.equals("STATIC")){ //TODO:FN ver estes tipos
            result.put(subdata.getValue());
        }else if(testDataLibType.equals("SQL")){
            result.put(subdata.getColumn());
        }
        else if(testDataLibType.equals("SOAP")){
            result.put(subdata.getParsingAnswer());
        }else {
            result.put("");
        }
        result.put(subdata.getDescription());
        
        return result;
    }

    private JSONObject getListOfGroupsPerType(ApplicationContext appContext, String type) throws JSONException{
        ITestDataLibService testDataService = appContext.getBean(ITestDataLibService.class);
         
        JSONObject jsonResponse = new JSONObject();    
        
        List<String> groups = testDataService.getListOfGroupsPerType(type);
      
        jsonResponse.put("groupList", groups.toArray());     
        
        
           
        return jsonResponse;
    }

    private AnswerItem findTestDataLibByID(ApplicationContext appContext, int testDatalib) throws JSONException {
        AnswerItem item = new AnswerItem();
        
        JSONObject object = new JSONObject();
        JSONArray response  = null;
        
        ITestDataLibService testDataService = appContext.getBean(ITestDataLibService.class);
        
        try{
            AnswerItem answer = testDataService.findTestDataLibByKey(testDatalib);
            
            TestDataLib lib = (TestDataLib)answer.getItem();
            try {
                response = convertTestDataLibToJSONObject(lib);
                object.put("testDataLib", response); //TODO:FN transformar as mensagens

                item.setItem(object);
                item.setResultMessage(answer.getResultMessage());
                
            } catch (JSONException ex) {
                Logger.getLogger(GetTestDataLib.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                //TODO:FN ver mensagem que deve ser devolvida.
                item.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR));
            }
            
        }catch(CerberusException ex){
            item.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR));
            //TODO:FN compltear esta mensagem
        }
        
        return item;
    }


}
