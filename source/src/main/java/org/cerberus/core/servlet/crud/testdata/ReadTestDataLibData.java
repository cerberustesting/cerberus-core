/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.servlet.crud.testdata;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest; 
import javax.servlet.http.HttpServletResponse; 
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.TestDataLib;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.crud.entity.TestDataLibData;
import org.cerberus.core.crud.service.ITestDataLibDataService;
import org.cerberus.core.crud.service.ITestDataLibService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.cerberus.core.util.servlet.ServletUtil;
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

    private static final Logger LOG = LogManager.getLogger(ReadTestDataLibData.class);
    
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
        
        AnswerItem answer = new AnswerItem<>(msg);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");
        
        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

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
            LOG.warn(ex);
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
                answer = readById(appContext, testdatalibid, request);                   
            } 
//           TODO : WARN : this methods can allow to access private data.
//              check if used, else, remove with the associated methods 
//            
//            else if (request.getParameter("name") != null) {
//                //return sub-data entries with basis on the name
//                String name = policy.sanitize(request.getParameter("name"));
//                answer = readByName(appContext, name);
//            } else {
//                //return all entries
//                answer = readAll(appContext);                
//            }
 
            jsonResponse = (JSONObject) answer.getItem();
            
            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());
            response.getWriter().print(jsonResponse.toString());

        } catch (JSONException e) {
            LOG.warn(e);
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

    private AnswerItem readById(ApplicationContext appContext, int testDatalib, HttpServletRequest request) throws JSONException {
        JSONObject jsonResponse = new JSONObject();
        ITestDataLibService testDataLibService = appContext.getBean(ITestDataLibService.class);
        ITestDataLibDataService testDataLibDataService = appContext.getBean(ITestDataLibDataService.class);
        
        
        AnswerItem datalib = testDataLibService.readByKey(testDatalib);
        AnswerList<TestDataLibData> answer = testDataLibDataService.readByVarious(testDatalib, null, null, null);

        boolean hasPermissionToSeePrivateValue = false;
        
        if (datalib.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item and convert it to JSONformat
            TestDataLib lib = (TestDataLib) datalib.getItem();
            hasPermissionToSeePrivateValue = testDataLibService.userHasPermission(lib, request.getUserPrincipal().getName());
        }
        
        //retrieves the data for the entry
        JSONArray jsonArray = new JSONArray();

        for (TestDataLibData subdata : answer.getDataList()) {
            if (!hasPermissionToSeePrivateValue && "Y".equals(subdata.getEncrypt())){
                subdata.setValue(StringUtil.SECRET_STRING);
            }
            jsonArray.put(convertTestDataLibDataToJSONObject(subdata));
        }

        jsonResponse.put("contentTable", jsonArray);

        AnswerItem<JSONObject> item = new AnswerItem<>();
        item.setItem(jsonResponse);
        item.setResultMessage(answer.getResultMessage());

        return item;
    }
    
    private JSONObject convertTestDataLibDataToJSONObject(TestDataLibData subdata) throws JSONException {
        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(subdata));
        
        return result;
    }

}
