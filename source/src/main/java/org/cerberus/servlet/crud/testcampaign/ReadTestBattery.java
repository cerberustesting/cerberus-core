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
package org.cerberus.servlet.crud.testcampaign;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.crud.entity.TestBattery;
import org.cerberus.crud.entity.TestBatteryContent;
import org.cerberus.crud.service.ITestBatteryContentService;
import org.cerberus.crud.service.ITestBatteryService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.AnswerUtil;
import org.cerberus.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author cerberus
 */
@WebServlet(name = "ReadTestBattery", urlPatterns = {"/ReadTestBattery"})
public class ReadTestBattery extends HttpServlet {

    ITestBatteryService testBatteryService;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        try {
            JSONObject jsonResponse = new JSONObject();
            AnswerItem answer = new AnswerItem(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED));

            if (request.getParameter("columnName") != null) {
                answer = findDistinctValuesOfColumn(appContext, request, request.getParameter("columnName"));
                jsonResponse = (JSONObject) answer.getItem();
            } else if (request.getParameter("param") != null) {
                answer = findBatteryByKey(request.getParameter("param"), true, appContext, request);
                jsonResponse = (JSONObject) answer.getItem();
            } else {
                answer = findTestBatteryList(appContext, request);
                jsonResponse = (JSONObject) answer.getItem();
            }

            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());

            response.getWriter().print(jsonResponse.toString());
        } catch (JSONException ex) {
            org.apache.log4j.Logger.getLogger(ReadTestBattery.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
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

    private AnswerItem findTestBatteryList(ApplicationContext appContext, HttpServletRequest request) throws JSONException {
        AnswerItem item = new AnswerItem();
        AnswerList answer = new AnswerList();
        JSONObject resp = new JSONObject();

        ITestBatteryService testBatteryService = appContext.getBean(ITestBatteryService.class);

        int startPosition = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayStart"), "0"));
        int length = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayLength"), "0"));

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        int columnToSortParameter = Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortCol_0"), "0"));
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "testbatteryid, testbattery, Description");
        String columnToSort[] = sColumns.split(",");
        String columnName = columnToSort[columnToSortParameter];
        String sort = ParameterParserUtil.parseStringParam(request.getParameter("sSortDir_0"), "asc");

        Map<String, List<String>> individualSearch = new HashMap<String, List<String>>();
        for (int a = 0; a < columnToSort.length; a++) {
            if (null != request.getParameter("sSearch_" + a) && !request.getParameter("sSearch_" + a).isEmpty()) {
                List<String> search = new ArrayList(Arrays.asList(request.getParameter("sSearch_" + a).split(",")));
                individualSearch.put(columnToSort[a], search);
            }
        }

        answer = testBatteryService.readByCriteria(startPosition, length, columnName, sort, searchParameter, individualSearch);

//        boolean userHasPermissions = request.isUserInRole("TestAdmin");
        JSONArray jsonArray = new JSONArray();
        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            for (TestBattery testBattery : (List<TestBattery>) answer.getDataList()) {
                jsonArray.put(convertTestBatterytoJSONObject(testBattery));
            }
        }

        resp.put("contentTable", jsonArray);
//        resp.put("hasPermissions", userHasPermissions);
        resp.put("iTotalRecords", answer.getTotalRows());
        resp.put("iTotalDisplayRecords", answer.getTotalRows());

        item.setItem(resp);
        item.setResultMessage(answer.getResultMessage());

        return item;
    }

    private AnswerItem findBatteryByKey(String key, Boolean userHasPermissions, ApplicationContext appContext, HttpServletRequest request) throws JSONException {
        AnswerItem item = new AnswerItem();
        JSONObject object = new JSONObject();

        testBatteryService = appContext.getBean(ITestBatteryService.class);

        AnswerItem answer = testBatteryService.readByKey(key);
        TestBattery p;
        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            p = (TestBattery) answer.getItem();
            JSONObject response = convertTestBatterytoJSONObject(p);

            if (request.getParameter("test") != null) {
                ITestBatteryContentService testBatteryContentService = appContext.getBean(ITestBatteryContentService.class);
                AnswerList resp = testBatteryContentService.readByTestBattery(key);
                if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
                    JSONArray a = new JSONArray();
                    for (Object c : resp.getDataList()) {
                        TestBatteryContent cc = (TestBatteryContent) c;
                        JSONObject ccJSON = convertTestBatteryContenttoJSONObject(cc);
                        a.put(ccJSON);
                    }
                    response.put("battery", a);
                }
            }
            object.put("contentTable", response);
        }
        object.put("hasPermissions", userHasPermissions);
        item.setItem(object);
        item.setResultMessage(answer.getResultMessage());

        return item;
    }

    private AnswerItem findDistinctValuesOfColumn(ApplicationContext appContext, HttpServletRequest request, String columnName) throws JSONException {
        AnswerItem answer = new AnswerItem();
        JSONObject object = new JSONObject();

        testBatteryService = appContext.getBean(ITestBatteryService.class);

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "testbatteryid, testbattery, Description");
        String columnToSort[] = sColumns.split(",");

        Map<String, List<String>> individualSearch = new HashMap<String, List<String>>();
        for (int a = 0; a < columnToSort.length; a++) {
            if (null != request.getParameter("sSearch_" + a) && !request.getParameter("sSearch_" + a).isEmpty()) {
                List<String> search = new ArrayList(Arrays.asList(request.getParameter("sSearch_" + a).split(",")));
                individualSearch.put(columnToSort[a], search);
            }
        }

        AnswerList applicationList = testBatteryService.readDistinctValuesByCriteria(searchParameter, individualSearch, columnName);

        object.put("distinctValues", applicationList.getDataList());

        answer.setItem(object);
        answer.setResultMessage(applicationList.getResultMessage());
        return answer;
    }

    private JSONObject convertTestBatterytoJSONObject(TestBattery testBattery) throws JSONException {
        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(testBattery));
        return result;
    }

    private JSONObject convertTestBatteryContenttoJSONObject(TestBatteryContent testBattery) throws JSONException {
        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(testBattery));
        return result;
    }

}
