/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.TestDataLib;
import org.cerberus.entity.TestDataLibData;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.factory.IFactoryTestDataLib;
import org.cerberus.factory.IFactoryTestDataLibData;
import org.cerberus.factory.impl.FactoryLogEvent;
import org.cerberus.log.MyLogger;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.ITestDataLibDataService;
import org.cerberus.service.ITestDataLibService;
import org.cerberus.service.impl.LogEventService;
import org.cerberus.util.answer.Answer;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet responsible for handling the creation of new test data lib entries
 *
 * @author FNogueira
 */
public class AddTestDataLib extends HttpServlet {

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
        MessageEvent rs = null;
                
        try {
            //common attributes
            String name = request.getParameter("Name");
            String type = request.getParameter("Type");
            String group = request.getParameter("Group");

            String description = request.getParameter("EntryDescription");
            String system[] = request.getParameterValues("System");
            String environment[] = request.getParameterValues("Environment");
            String country[] = request.getParameterValues("Country");

            String database = request.getParameter("Database");
            String script = request.getParameter("Script");

            String servicePath = request.getParameter("ServicePath");
            String method = request.getParameter("Method");
            String envelope = request.getParameter("Envelope");

            try {

                //specific attributes 
                ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
                IFactoryTestDataLib factoryLibService = appContext.getBean(IFactoryTestDataLib.class);
                ITestDataLibService libService = appContext.getBean(ITestDataLibService.class);

                
                Answer ans = null;
                //create lib entries that represent combinations of environment+system+country
                //at this point we don't know the ID, because it was not inserted yet in the database
                List<TestDataLibData> subDataList = null;

                List<TestDataLib> entries = extractTestDataLibEntries(system, environment, country, 
                        factoryLibService, name, group, type, database, script, servicePath, method, envelope, description);


                if(hasSubDataEntries(request)){
                    subDataList = new ArrayList<TestDataLibData>();
                    subDataList.addAll(extractTestDataLibDataSet(appContext, type, request));
                    //Creates the entries and the subdata
                    ans = libService.createTestDataLibBatch(entries, subDataList);
                }else{
                    //Creates only the entries because no subdata was defined
                    ans = libService.createTestDataLibBatch(entries);
                }


                jsonResponse.put("messageType", ans.getMessageType());
                jsonResponse.put("message", ans.getMessageDescription());

                //  Adding Log entry.
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
                try {
                    logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/AddTestDataLib", "CREATE", "Create TestDataLib  : " + name, "", ""));
                } catch (CerberusException ex) {
                    org.apache.log4j.Logger.getLogger(AddTestDataLib.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);
                }
                
            } catch (CerberusException ex) {
                //TODO:FN tratar excepcoes
                MyLogger.log(AddTestDataLib.class.getName(), Level.FATAL, "" + ex);
                jsonResponse.put("isSuccess", false);
                
                rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
                rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", ex.toString()));                 
                     
            }
            
            //sets the message returned by the operations
            //jsonResponse.put("message", rs.getDescription());
        } catch (JSONException ex) {
            MyLogger.log(AddTestDataLib.class.getName(), Level.FATAL, "" + ex);
            response.setContentType("text/html");
            response.getWriter().print(ex.getMessage());
        }
        
        response.setContentType("application/json");
        response.getWriter().print(jsonResponse); //TODO:FN pode ser null?
        response.getWriter().flush();

    }

    private List<TestDataLib> extractTestDataLibEntries(String[] system, String[] environment, String[] country, IFactoryTestDataLib factoryLibService, String name, String group, String type, String database, String script, String servicePath, String method, String envelope, String description) {
        List<TestDataLib> combinations = new ArrayList<TestDataLib>();
        TestDataLib lib = null;
        for (String sys : containsAllValue(system)) {
            for (String env : containsAllValue(environment)) {
                for (String count : containsAllValue(country)) {                    
                    lib = factoryLibService.create(name, sys, env, count, group, type,
                            database, script, servicePath, method, envelope, description); //TODO:FN ver a declaracao desta variavel
                    combinations.add(lib);
                }
            }
        }
        return combinations;
    }
    
    private List<TestDataLibData> extractTestDataLibDataSet(ApplicationContext appContext, String type, HttpServletRequest request) throws BeansException, CerberusException {
        //now we can insert the testdatalibdata that was specified in the insert page
        //we can have several tesdatalibdata
        IFactoryTestDataLibData factorySubdataService = appContext.getBean(IFactoryTestDataLibData.class);

        List<TestDataLibData> listSubdata = new ArrayList<TestDataLibData>();

        //as all fields (subadata, data, description) are mandatory  there will no problem
        //with accessing the following arrays
        String[] subdataEntries = request.getParameterValues("subdata");
        String[] subdataValues = request.getParameterValues("data");
        String[] subdataDescriptions = request.getParameterValues("description");

        TestDataLibData subData;

        for (int i = 0; i < subdataEntries.length; i++) {
            subData = factorySubdataService.create(-1, type, subdataEntries[i], subdataValues[i], subdataDescriptions[i]);
            listSubdata.add(subData);
        }

        return listSubdata;

    }
    /*private List<TestDataLibData> extractTestDataLibDataSet(ApplicationContext appContext, TestDataLib lib, HttpServletRequest request) throws BeansException, CerberusException {
        //now we can insert the testdatalibdata that was specified in the insert page
        //we can have several tesdatalibdata
        IFactoryTestDataLibData factorySubdataService = appContext.getBean(IFactoryTestDataLibData.class);

        List<TestDataLibData> listSubdata = new ArrayList<TestDataLibData>();

        //as all fields (subadata, data, description) are mandatory  there will no problem
        //with accessing the following arrays
        String[] subdataEntries = request.getParameterValues("subdata");
        String[] subdataValues = request.getParameterValues("data");
        String[] subdataDescriptions = request.getParameterValues("description");

        TestDataLibData subData;

        for (int i = 0; i < subdataEntries.length; i++) {
            subData = factorySubdataService.create(lib.getTestDataLibID(), lib.getType(), subdataEntries[i], subdataValues[i], subdataDescriptions[i]);
            listSubdata.add(subData);
        }

        return listSubdata;

    }*/

    private boolean hasSubDataEntries( HttpServletRequest request){
        return request.getParameterValues("subdata") != null &&
                request.getParameterValues("subdata") != null && request.getParameterValues("description") != null;
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

    private String[] containsAllValue(String[] parameterValues) {

        for (String str : parameterValues) {
            if (str.isEmpty()) {
                return new String[]{""}; //TODO:FN colocar aqui a constante do ALL value?
            }
        }

        return parameterValues;
    }

}
