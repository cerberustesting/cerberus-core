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
package org.cerberus.servlet.crud.testdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.enums.MessageCodeEnum;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryTestDataLib;
import org.cerberus.crud.factory.IFactoryTestDataLibData;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.log.MyLogger;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ITestDataLibService;
import org.cerberus.crud.service.impl.InvariantService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
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
@WebServlet(name = "CreateTestDataLib", urlPatterns = {"/CreateTestDataLib"})
public class CreateTestDataLib extends HttpServlet {

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
            try {

                ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

                ITestDataLibService libService = appContext.getBean(ITestDataLibService.class);

                Answer answer = null;
                //create lib entries that represent combinations of environment+system+country
                //at this point we don't know the ID, because it was not inserted yet in the database
                List<TestDataLibData> subDataList = null;

                if(request.getParameter("name") != null){ //if name is not sent, it means that we are trying to create a new library
                    rs = validate(request);
                    //check if data is valid
                    if (rs.getCodeString().equals(MessageCodeEnum.DATA_OPERATION_CODE_SUCCESS.getCode())) {

                        List<TestDataLib> entries = extractTestDataLibEntries(appContext, request);

                        if (hasSubDataEntries(request)) {
                            subDataList = new ArrayList<TestDataLibData>();
                            subDataList.addAll(extractTestDataLibDataSet(appContext, request));
                            //Creates the entries and the subdata
                            answer = libService.createBatch(entries, subDataList);
                        } else {
                            //Creates only the entries because no subdata was defined
                            answer = libService.createBatch(entries);
                        }

                        rs = answer.getResultMessage();

                        //  Adding Log entry.
                        if(answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())){
                            ILogEventService logEventService = appContext.getBean(LogEventService.class);
                            logEventService.createPrivateCalls("/CreateTestDataLib", "CREATE", "Create TestDataLib  : " + request.getParameter("name"), request);
                        }
                    }
                }else{
                    //no parameters means that we are loading data necessary to the create data lib option
                    AnswerItem ansGroups = readDistinctGroups(appContext);
                    jsonResponse  = (JSONObject) ansGroups.getItem();
                    //include database lists
                    jsonResponse.put("PROPERTYDATABASE", findInvariantListByIdName(appContext, "PROPERTYDATABASE"));
                    //include testdatatypes                        
                    jsonResponse.put("TESTDATATYPE", findInvariantListByIdName(appContext, "TESTDATATYPE"));
                    //include systems                        
                    jsonResponse.put("SYSTEM", findInvariantListByIdName(appContext, "SYSTEM"));
                    //include environments
                    jsonResponse.put("ENVIRONMENT", findInvariantListByIdName(appContext, "ENVIRONMENT"));
                    //include countries
                    jsonResponse.put("Country", findInvariantListByIdName(appContext, "Country"));
                    rs = ansGroups.getResultMessage();
                }
            } catch (CerberusException ex) {
                MyLogger.log(CreateTestDataLib.class.getName(), Level.FATAL, "" + ex);
                rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", ex.toString()));
            }

            //sets the message returned by the operations
            jsonResponse.put("messageType", rs.getMessage().getCodeString());
            jsonResponse.put("message", rs.getDescription());

            response.setContentType("application/json");
            response.getWriter().print(jsonResponse);
            response.getWriter().flush();
        } catch (JSONException ex) {

            org.apache.log4j.Logger.getLogger(CreateTestDataLib.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.setContentType("application/json");
            MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("{'messageType':'").append(msg.getCode()).append("', ");
            errorMessage.append(" 'message': '");
            errorMessage.append(msg.getDescription().replace("%DESCRIPTION%", "Unable to check the status of your request! Try later or - Open a bug or ask for any new feature \n"
                    + "<a href=\"https://github.com/vertigo17/Cerberus/issues/\" target=\"_blank\">here</a>"));
            errorMessage.append("'}");
            response.getWriter().print(errorMessage.toString());
        }

    }

    private List<TestDataLib> extractTestDataLibEntries(ApplicationContext appContext, HttpServletRequest request) {

        List<TestDataLib> combinations = new ArrayList<TestDataLib>();
        TestDataLib lib;
        //common attributes
        String name = request.getParameter("name");
        String type = request.getParameter("type");
        String group = request.getParameter("group");

        //check if the name is defined 
        String description = request.getParameter("libdescription");
        String system[] = request.getParameterValues("system");
        boolean systemAll = Boolean.parseBoolean(request.getParameter("systemall"));
        String environment[] = request.getParameterValues("environment");
        boolean environmentAll = Boolean.parseBoolean(request.getParameter("environmentall"));

        String country[] = request.getParameterValues("country");
        boolean countryAll = Boolean.parseBoolean(request.getParameter("countryall"));

        String database = request.getParameter("database");
        String script = request.getParameter("script");

        String servicePath = request.getParameter("servicepath");
        String method = request.getParameter("method");
        String envelope = request.getParameter("envelope");

        IFactoryTestDataLib factoryLibService = appContext.getBean(IFactoryTestDataLib.class);

        system = getValues(system, systemAll);
        environment = getValues(environment, environmentAll);
        country = getValues(country, countryAll);
        //gets the combinations for the entries
        for (String sys : system) {
            for (String env : environment) {
                for (String count : country) {
                    lib = factoryLibService.create(name, sys, env, count, group, type,
                            database, script, servicePath, method, envelope, description);
                    combinations.add(lib);
                }
            }
        }
        return combinations;
    }

    private List<TestDataLibData> extractTestDataLibDataSet(ApplicationContext appContext, HttpServletRequest request) throws BeansException, CerberusException {
        //now we can insert the testdatalibdata that was specified in the insert page
        //we can have several tesdatalibdata
        IFactoryTestDataLibData factorySubdataService = appContext.getBean(IFactoryTestDataLibData.class);

        List<TestDataLibData> listSubdata = new ArrayList<TestDataLibData>();
        String type = request.getParameter("type");
        //as all fields (subadata, data, description) are mandatory  there will no problem
        //with accessing the following arrays
        String[] subdataEntries = request.getParameterValues("subdata");
        String[] subdataValues = request.getParameterValues("value");
        String[] subdataColumns = request.getParameterValues("column");
        String[] subdataParsingAnswer = request.getParameterValues("parsinganswer");
        String[] subdataDescriptions = request.getParameterValues("description");

        TestDataLibData subData;

        for (int i = 0; i < subdataEntries.length; i++) {
            subData = factorySubdataService.create(-1, subdataEntries[i], subdataValues[i], subdataColumns[i], subdataParsingAnswer[i], subdataDescriptions[i]);
            listSubdata.add(subData);
        }

        return listSubdata;

    }

    private boolean hasSubDataEntries(HttpServletRequest request) {
        return request.getParameterValues("subdata") != null
                && request.getParameterValues("subdata") != null && request.getParameterValues("description") != null;
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

    private String[] getValues(String[] parameterValues, boolean allOptionSelected) {
        if (allOptionSelected || parameterValues == null) {
            return new String[]{""};
        } else {
            return parameterValues;
        }
    }

    /**
     * Performs the server-side validations.
     *
     * @param request - http request were data was transfered
     * @return message indicating if data is valid
     */
    private MessageEvent validate(HttpServletRequest request) {
        MessageEvent rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_VALIDATIONS_OK);

        StringBuilder errorMessage = new StringBuilder();

        String name = request.getParameter("name");
        if (StringUtil.isNullOrEmpty(name)) {
            errorMessage.append("Please specify the name of the entry! ");
        }

        if (request.getParameterValues("subdata") != null) {
            String subDataNames[] = request.getParameterValues("subdata");
            if (containsDuplicates(subDataNames, false)) {
                errorMessage.append("You have entries with duplicated names. ");
            }
        }

        //is invalid
        if (!errorMessage.toString().isEmpty()) {
            rs = new MessageEvent(MessageEventEnum.DATA_OPERATION_VALIDATIONS_ERROR);
            rs.setDescription(rs.getDescription().replace("%DESCRIPTION%", errorMessage.toString()));
        }

        return rs;
    }

    public boolean containsDuplicates(final String[] entriesList, boolean includeEmpty) {
        Set<String> entries = new HashSet<String>();
        for (String str : entriesList) {
            if (entries.contains(str)) {
                return true;
            }
            if (str.isEmpty() && !includeEmpty) {
                continue;
            }
            entries.add(str);

        }
        return false;
    }

    public boolean containsEmptyValues(final String[] entriesList) {
        for (String str : entriesList) {
            if (str.isEmpty()) {
                return true;
            }
        }
        return false;
    }
    
     /**
     * Auxiliary method that retrieves the list of groups that are currently defined for a type of testdatalib entries.
     * @param appContext - context object used to get the required beans
     * @param type - type that filters the list of groups that should be retrieved
     * @return an object containing all the groups that belong to that type
     * @throws JSONException 
     */
    private AnswerItem readDistinctGroups(ApplicationContext appContext) throws JSONException{
        AnswerItem answerItem = new AnswerItem();
        
        ITestDataLibService testDataService = appContext.getBean(ITestDataLibService.class);
         
        JSONObject jsonObject = new JSONObject();               
        AnswerList<String> ansList = testDataService.readDistinctGroups();
        
        if(ansList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())){
            //if the response is success then we can sent the data
            jsonObject.put("GROUPS", ((List<String>) ansList.getDataList()).toArray());
        }
        answerItem.setResultMessage(ansList.getResultMessage());
        answerItem.setItem(jsonObject);
           
        return answerItem;
    }
    /**
     * Auxiliary method that search for the values for an invariant with basis in its name.
     * @param appContext - context object used to get the required beans
     * @param idName name of the invariant that is to be retrieved
     * @return an object containing the invariant information
     * @throws JSONException 
     */
    private JSONObject findInvariantListByIdName(ApplicationContext appContext, String idName) throws JSONException {
        JSONObject object = new JSONObject();
        try {
            
            IInvariantService invariantService = appContext.getBean(InvariantService.class);
            
            //gets the list of databases
            for (Invariant myInvariant : invariantService.findListOfInvariantById(idName)) {
                object.put(myInvariant.getValue(), myInvariant.getValue());
                
            }
            
        } catch (CerberusException ex) {
            Logger.getLogger(CreateTestDataLib.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return object;
    }
}
