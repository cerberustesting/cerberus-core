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
package org.cerberus.servlet.testcase;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.TCase;
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.crud.service.ITestCaseCountryService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.impl.TestCaseCountryService;
import org.cerberus.crud.service.impl.TestCaseService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author cerberus
 */
@WebServlet(name = "ReadTestCase", urlPatterns = {"/ReadTestCase"})
public class ReadTestCase extends HttpServlet {

    private ITestCaseService testCaseService;

    private ITestCaseCountryService testCaseCountryService;

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
        JSONObject jsonResponse = new JSONObject();
        AnswerItem answer = new AnswerItem(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED));

        int sEcho = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("sEcho"), "0"));
        String test = ParameterParserUtil.parseStringParam(request.getParameter("test"), "");
        String testCase = ParameterParserUtil.parseStringParam(request.getParameter("testCase"), "");
        boolean getMaxTC = ParameterParserUtil.parseBooleanParam(request.getParameter("getMaxTC"), false);

        // Default message to unexpected error.
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        try {
            if (sEcho != 0 && !Strings.isNullOrEmpty(test)) {
                answer = findTestCaseByTest(appContext, request, test);
                jsonResponse = (JSONObject) answer.getItem();
            } else if (sEcho == 0 && !Strings.isNullOrEmpty(test) && !Strings.isNullOrEmpty(testCase)) {
                answer = findTestCaseByTestTestCase(appContext, test, testCase);
                jsonResponse = (JSONObject) answer.getItem();
            } else if (sEcho == 0 && !Strings.isNullOrEmpty(test) && getMaxTC) {
                testCaseService = appContext.getBean(TestCaseService.class);
                String max = testCaseService.getMaxNumberTestCase(test);
                jsonResponse.put("maxTestCase", Integer.valueOf(max));
                answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
            }

            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());
            jsonResponse.put("sEcho", sEcho);

            response.setContentType("application/json");
            response.getWriter().print(jsonResponse.toString());
        } catch (JSONException e) {
            org.apache.log4j.Logger.getLogger(ReadTestCase.class.getName()).log(org.apache.log4j.Level.ERROR, null, e);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.setContentType("application/json");
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("{\"messageType\":\"").append(msg.getCode()).append("\",");
            errorMessage.append("\"message\":\"");
            errorMessage.append(msg.getDescription().replace("%DESCRIPTION%", "Unable to check the status of your request! Try later or open a bug."));
            errorMessage.append("\"}");
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

    private AnswerItem findTestCaseByTest(ApplicationContext appContext, HttpServletRequest request, String test) throws JSONException {
        AnswerItem answer = new AnswerItem();
        JSONObject jsonResponse = new JSONObject();

        testCaseService = appContext.getBean(TestCaseService.class);
        testCaseCountryService = appContext.getBean(TestCaseCountryService.class);

        int startPosition = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayStart"), "0"));
        int length = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayLength"), "0"));

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        int columnToSortParameter = Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortCol_0"), "0"));
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "Application,Description,sort,type,system,subsystem,svnurl,bugtrackerurl,bugtrackernewurl,deploytype,mavengroupid");
        String columnToSort[] = sColumns.split(",");
        String columnName = columnToSort[columnToSortParameter];
        String sort = ParameterParserUtil.parseStringParam(request.getParameter("sSortDir_0"), "asc");
        AnswerList testCaseList = testCaseService.readByTestByCriteria(test, startPosition, length, columnName, sort, searchParameter, "");

        AnswerList testCaseCountryList = testCaseCountryService.readByTestTestCase(test, null);

        LinkedHashMap<String, JSONObject> testCaseWithCountry = new LinkedHashMap();
        for (TestCaseCountry country : (List<TestCaseCountry>) testCaseCountryList.getDataList()) {
            String key = country.getTest() + "_" + country.getTestCase();

            if (testCaseWithCountry.containsKey(key)) {
                testCaseWithCountry.get(key).put(country.getCountry(), country.getCountry());
            } else {
                testCaseWithCountry.put(key, new JSONObject().put(country.getCountry(), country.getCountry()));
            }
        }

        JSONArray jsonArray = new JSONArray();
        boolean userHasPermissions = request.isUserInRole("TestAdmin");
        if (testCaseList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            for (TCase testCase : (List<TCase>) testCaseList.getDataList()) {
                String key = testCase.getTest() + "_" + testCase.getTestCase();
                JSONObject value = convertTestCaseToJSONObject(testCase);
                value.put("hasPermissions", userHasPermissions);
                value.put("countryList", testCaseWithCountry.get(key));

                jsonArray.put(value);
            }
        }

        jsonResponse.put("hasPermissions", userHasPermissions);
        jsonResponse.put("contentTable", jsonArray);
        jsonResponse.put("iTotalRecords", testCaseList.getTotalRows());
        jsonResponse.put("iTotalDisplayRecords", testCaseList.getTotalRows());

        answer.setItem(jsonResponse);
        answer.setResultMessage(testCaseList.getResultMessage());
        return answer;
    }

    private JSONObject convertTestCaseToJSONObject(TCase testCase) throws JSONException {
        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(testCase));
        return result;
    }

    private AnswerItem findTestCaseByTestTestCase(ApplicationContext appContext, String test, String testCase) throws JSONException {
        AnswerItem item = new AnswerItem();
        JSONObject object = new JSONObject();

        testCaseService = appContext.getBean(TestCaseService.class);
        testCaseCountryService = appContext.getBean(TestCaseCountryService.class);

        //finds the project     
        AnswerItem answer = testCaseService.readByKey(test, testCase);

        AnswerList testCaseCountryList = testCaseCountryService.readByTestTestCase(test, testCase);

        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item and convert it to JSONformat
            TCase tc = (TCase) answer.getItem();
            object = convertTestCaseToJSONObject(tc);
            object.put("countryList", new JSONObject());
        }

        for (TestCaseCountry country : (List<TestCaseCountry>) testCaseCountryList.getDataList()) {
            object.getJSONObject("countryList").put(country.getCountry(), country.getCountry());
        }

        item.setItem(object);
        item.setResultMessage(answer.getResultMessage());

        return item;
    }

}
