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
package org.cerberus.servlet.test;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.Test;
import org.cerberus.crud.service.ITestService;
import org.cerberus.crud.service.impl.TestService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author cerberus
 */
@WebServlet(name = "ReadTest", urlPatterns = {"/ReadTest"})
public class ReadTest extends HttpServlet {

    private ITestService testService;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws org.json.JSONException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, JSONException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        int sEcho = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("sEcho"), "0"));
        String test = ParameterParserUtil.parseStringParam(request.getParameter("test"), "");

        AnswerItem answer = new AnswerItem(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED));
        JSONObject jsonResponse = new JSONObject();

        if (sEcho != 0) {
            answer = findTestList(appContext, request);
            jsonResponse = (JSONObject) answer.getItem();
        } else if (sEcho == 0 && !test.equals("")) {
            answer = findTestByKey(appContext, test);
            jsonResponse = (JSONObject) answer.getItem();
        }
        jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", answer.getResultMessage().getDescription());

        response.setContentType("application/json");
        response.getWriter().print(jsonResponse.toString());

    }

    private AnswerItem findTestList(ApplicationContext appContext, HttpServletRequest request) throws JSONException {
        AnswerItem answer = new AnswerItem(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED));
        AnswerList testList = new AnswerList();
        JSONObject jsonResponse = new JSONObject();

        testService = appContext.getBean(TestService.class);

        int startPosition = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayStart"), "0"));
        int length = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayLength"), "0"));

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        int columnToSortParameter = Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortCol_0"), "0"));
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "test,description,active,automated,tdatecrea");
        String columnToSort[] = sColumns.split(",");
        String columnName = columnToSort[columnToSortParameter];
        String sort = ParameterParserUtil.parseStringParam(request.getParameter("sSortDir_0"), "asc");

        testList = testService.readByCriteria(startPosition, length, columnName, sort, searchParameter, "");

        boolean userHasPermissions = request.isUserInRole("TestAdmin");

        JSONArray jsonArray = new JSONArray();
        if (testList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            for (Test test : (List<Test>) testList.getDataList()) {
                jsonArray.put(convertTestToJSONObject(test).put("hasPermissions", userHasPermissions));
            }
        }

        jsonResponse.put("contentTable", jsonArray);
        jsonResponse.put("hasPermissions", userHasPermissions);
        jsonResponse.put("iTotalRecords", testList.getTotalRows());
        jsonResponse.put("iTotalDisplayRecords", testList.getTotalRows());

        answer.setItem(jsonResponse);
        answer.setResultMessage(testList.getResultMessage());
        return answer;
    }

    private JSONObject convertTestToJSONObject(Test test) throws JSONException {

        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(test));
        return result;
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
        try {
            processRequest(request, response);
        } catch (JSONException ex) {
            Logger.getLogger(ReadTest.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        try {
            processRequest(request, response);
        } catch (JSONException ex) {
            Logger.getLogger(ReadTest.class.getName()).log(Level.SEVERE, null, ex);
        }
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

    private AnswerItem findTestByKey(ApplicationContext appContext, String testName) throws JSONException {
        AnswerItem answer = new AnswerItem();
        JSONObject jsonResponse = new JSONObject();

        testService = appContext.getBean(TestService.class);

        answer = testService.readByKey(testName);

        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item and convert it to JSONformat
            Test test = (Test) answer.getItem();
            jsonResponse.put("contentTable", convertTestToJSONObject(test));
        }

        answer.setItem(jsonResponse);
        answer.setResultMessage(answer.getResultMessage());

        return answer;
    }

}
