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
import org.apache.commons.lang3.StringEscapeUtils;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.ITestDataLibService;
import org.cerberus.dto.TestCaseListDTO;
import org.cerberus.dto.TestListDTO;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.AnswerUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet responsible for getting information from the test data library
 *
 * @author memiks
 * @author FNogueira
 */
@WebServlet(name = "ReadTestDataLib", urlPatterns = {"/ReadTestDataLib"})
public class ReadTestDataLib extends HttpServlet {

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
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        // Default message to unexpected error.
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        AnswerItem answer = new AnswerItem(msg);

        response.setContentType("application/json");

        /**
         * Parsing and securing all required parameters.
         */
        String name = policy.sanitize(request.getParameter("name"));
        String country = policy.sanitize(request.getParameter("country"));
        Integer testDataLibId = 0;

        Integer limit = -1;
        boolean hasError = true;
        try {
            if (request.getParameter("testdatalibid") != null && !request.getParameter("testdatalibid").isEmpty()) {
                testDataLibId = Integer.parseInt(request.getParameter("testdatalibid"));
                hasError = false;
            }
        } catch (NumberFormatException ex) {
            org.apache.log4j.Logger.getLogger(ReadTestDataLib.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Library"));
            msg.setDescription(msg.getDescription().replace("%OPERATION%", "Read"));
            msg.setDescription(msg.getDescription().replace("%REASON%", "Test data library id must be an integer value."));
            answer.setResultMessage(msg);
            hasError = true;
        }
        try {
            //if the limit fails to be converted there is no problem because in the database we use the default value
            if (request.getParameter("limit") != null && !request.getParameter("limit").isEmpty()) {
                limit = Integer.parseInt(request.getParameter("limit"));
            }
        } catch (NumberFormatException ex) {
            org.apache.log4j.Logger.getLogger(ReadTestDataLib.class.getName()).log(org.apache.log4j.Level.WARN, null, ex);
        }

        // Global boolean on the servlet that define if the user has permition to edit and delete object.
        boolean userHasPermissions = request.isUserInRole("TestDataManager");

        try {
            JSONObject jsonResponse;

            if (request.getParameter("testdatalibid") != null && !hasError) {
                if (request.getParameter("name") != null && request.getParameter("country") != null) {
                    //gets all test cases that use a library
                    answer = getTestCasesUsingTestDataLib(testDataLibId, name, country, appContext, userHasPermissions);
                } else {
                    //gets a lib by id
                    answer = findTestDataLibByID(testDataLibId, appContext, userHasPermissions);
                }
            } else if (request.getParameter("name") != null && request.getParameter("limit") != null) {
                answer = findTestDataLibNameList(name, limit, appContext);
            } else if (request.getParameter("groups") != null) {
                //gets the list of distinct groups
                answer = findDistinctGroups(appContext);
            } else {
                //no parameters, then retrieves the full list
                answer = findTestDataLibList(appContext, request);
            }

            jsonResponse = (JSONObject) answer.getItem();

            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());

            response.setContentType("application/json");
            response.getWriter().print(jsonResponse.toString());

        } catch (JSONException e) {
            org.apache.log4j.Logger.getLogger(ReadTestDataLib.class.getName()).log(org.apache.log4j.Level.ERROR, null, e);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        }
    }

    /**
     * Auxiliary method that retrieves a list of test data library entries with
     * basis on the GUI information (datatable)
     *
     * @param appContext - context object used to get the required beans
     * @param request - object that contains the search and sort filters used to
     * retrieve the information to be displayed in the GUI.
     * @return object containing the info to be displayed in the GUI
     * @throws IOException
     * @throws BeansException
     * @throws NumberFormatException
     * @throws JSONException
     */
    private AnswerItem findTestDataLibList(ApplicationContext appContext, HttpServletRequest request) throws IOException, BeansException, NumberFormatException, JSONException {
        AnswerItem item = new AnswerItem();
        JSONObject jsonResponse = new JSONObject();
        testDataLibService = appContext.getBean(ITestDataLibService.class);

        int startPosition = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayStart"), "0"));
        int length = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayLength"), "0"));
        /*int sEcho  = Integer.valueOf(request.getParameter("sEcho"));*/

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        int columnToSortParameter = Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortCol_0"), "0"));
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"),
                "TestDataLibID,Name,System,Environment,Country,Group,Type,Database,Script,ServicePath,Method,Envelope,Description");
        String columnToSort[] = sColumns.split(",");
        String columnName = columnToSort[columnToSortParameter];
        String sort = ParameterParserUtil.parseStringParam(request.getParameter("sSortDir_0"), "asc");

        AnswerList resp = testDataLibService.readByCriteria(startPosition, length, columnName, sort, searchParameter, "");

        JSONArray jsonArray = new JSONArray();
        boolean userHasPermissions = request.isUserInRole("TestDataManager");
        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            for (TestDataLib testDataLib : (List<TestDataLib>) resp.getDataList()) {
                jsonArray.put(convertTestDataLibToJSONObject(testDataLib, false));

            }
        }

        //recordsFiltered do lado do servidor    
        jsonResponse.put("hasPermissions", userHasPermissions);
        jsonResponse.put("contentTable", jsonArray);
        jsonResponse.put("iTotalRecords", resp.getTotalRows());
        jsonResponse.put("iTotalDisplayRecords", resp.getTotalRows());
        //recordsFiltered

        item.setItem(jsonResponse);
        item.setResultMessage(resp.getResultMessage());
        return item;
    }

    /**
     * Auxiliary method that finds a test data library entry with basis on an
     * identifier
     *
     * @param appContext - context object used to get the required beans
     * @param testDatalib - identifier used to perform the search
     * @return an object containing the information about the test data library
     * that matches the identifier
     * @throws JSONException
     */
    private AnswerItem findTestDataLibByID(int testDatalib, ApplicationContext appContext, boolean userHasPermissions) throws JSONException {
        AnswerItem item = new AnswerItem();
        JSONObject object = new JSONObject();

        ITestDataLibService testDataService = appContext.getBean(ITestDataLibService.class);

        //finds the testdatalib        
        AnswerItem answer = testDataService.readByKey(testDatalib);

        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item and convert it to JSONformat
            TestDataLib lib = (TestDataLib) answer.getItem();
            JSONObject response = convertTestDataLibToJSONObject(lib, true);
            object.put("testDataLib", response);
        }

        object.put("hasPermissions", userHasPermissions);
        item.setItem(object);
        item.setResultMessage(answer.getResultMessage());

        return item;
    }

    /**
     * Handles the auto-complete requests and retrieves a limited list of
     * strings that match (totally or partially) the name entered by the user.
     *
     * @param appContext - context object used to get the required beans
     * @param nameToSearch - value used to perform the auto complete
     * @param limit - limit number of the records that should be retrieved
     * @return object containing values that match the name
     * @throws JSONException
     */
    private AnswerItem findTestDataLibNameList(String nameToSearch, int limit, ApplicationContext appContext) throws JSONException {

        AnswerItem ansItem = new AnswerItem();

        JSONObject object = new JSONObject();

        ITestDataLibService testDataService = appContext.getBean(ITestDataLibService.class);
        AnswerList ansList = testDataService.readNameListByName(nameToSearch, limit);

        object.put("data", ansList.getDataList());

        ansItem.setResultMessage(ansList.getResultMessage());
        ansItem.setItem(object);

        return ansItem;

    }

    /**
     * Auxiliary method that extracts the list of test cases that are currently
     * using one test lib.
     *
     * @param appContext - context object used to get the required beans
     * @param testDataLibId - identifier of the library entry
     * @param name - name of the library entry
     * @param country - country of the library entry
     * @return an answer item containing the information about the test cases
     * that use the entry
     * @throws JSONException
     */
    private AnswerItem getTestCasesUsingTestDataLib(int testDataLibId, String name, String country, ApplicationContext appContext, boolean userHasPermissions) throws JSONException {
        JSONObject object = new JSONObject();
        JSONArray objectArray = new JSONArray();
        AnswerItem ansItem = new AnswerItem();
        ITestCaseService tcService = appContext.getBean(ITestCaseService.class);

        AnswerList ansList = tcService.findTestCasesThatUseTestDataLib(testDataLibId, name, country);

        //if the response is success then we can iterate and search for the data
        if (ansList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            List<TestListDTO> listDTO = ansList.getDataList();
            for (TestListDTO l : listDTO) {
                JSONArray jsonArray = new JSONArray();
                JSONArray arrTestCase = new JSONArray();
                for (TestCaseListDTO testCase : l.getTestCaseList()) {
                    JSONObject jsonTestCase = new JSONObject();

                    jsonTestCase.put("TestCaseNumber", testCase.getTestCaseNumber());
                    jsonTestCase.put("TestCaseDescription", testCase.getTestCaseDescription());
                    jsonTestCase.put("Creator", testCase.getCreator());
                    jsonTestCase.put("Active", testCase.isIsActive());
                    jsonTestCase.put("Status", testCase.getStatus());
                    jsonTestCase.put("Group", testCase.getGroup());
                    jsonTestCase.put("Application", testCase.getApplication());
                    jsonTestCase.put("NrProperties", testCase.getPropertiesList().size());

                    arrTestCase.put(jsonTestCase);
                }
                //test details
                jsonArray.put(l.getTest());
                jsonArray.put(l.getDescription());
                jsonArray.put(l.getTestCaseList().size());
                jsonArray.put(arrTestCase);
                //test case details
                objectArray.put(jsonArray);
            }
        }

        object.put("TestCasesList", objectArray);
        object.put("hasPermissions", userHasPermissions);
        ansItem.setItem(object);
        ansItem.setResultMessage(ansList.getResultMessage());
        return ansItem;
    }

    /**
     * Auxiliary method that retrieves the list of groups that are currently
     * defined for a type of testdatalib entries.
     *
     * @param appContext - context object used to get the required beans
     * @param type - type that filters the list of groups that should be
     * retrieved
     * @return an object containing all the groups that belong to that type
     * @throws JSONException
     */
    private AnswerItem findDistinctGroups(ApplicationContext appContext) throws JSONException {
        AnswerItem answerItem = new AnswerItem();

        ITestDataLibService testDataService = appContext.getBean(ITestDataLibService.class);

        JSONObject jsonObject = new JSONObject();
        AnswerList<String> ansList = testDataService.readDistinctGroups();

        if (ansList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the response is success then we can sent the data
            jsonObject.put("contentTable", ((List<String>) ansList.getDataList()).toArray());
        }
        answerItem.setResultMessage(ansList.getResultMessage());
        answerItem.setItem(jsonObject);

        return answerItem;
    }

    /**
     * Auxiliary method that converts a test data library object to a JSON
     * object.
     *
     * @param testDataLib test data library
     * @param unescapeXML indicates whether the XML retrieved in the Envelope
     * should be un-escaped or not.
     * @return JSON object
     * @throws JSONException
     */
    private JSONObject convertTestDataLibToJSONObject(TestDataLib testDataLib, boolean unescapeContent) throws JSONException {

        if (unescapeContent) {
            //general            
            testDataLib.setDescription(StringEscapeUtils.unescapeHtml4(testDataLib.getDescription()));

            //SQL
            testDataLib.setScript(StringEscapeUtils.unescapeHtml4(testDataLib.getScript()));

            //SOAP
            testDataLib.setServicePath(StringEscapeUtils.unescapeHtml4(testDataLib.getServicePath()));
            testDataLib.setMethod(StringEscapeUtils.unescapeHtml4(testDataLib.getMethod()));
            testDataLib.setEnvelope(StringEscapeUtils.unescapeXml(testDataLib.getEnvelope()));
        }

        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(testDataLib));
        return result;
    }

}
