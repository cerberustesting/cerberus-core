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

import com.google.common.base.Strings;
import com.google.gson.Gson;
import org.cerberus.crud.entity.*;
import org.cerberus.crud.service.ICampaignContentService;
import org.cerberus.crud.service.ICampaignParameterService;
import org.cerberus.crud.service.ICampaignService;
import org.cerberus.crud.service.ITestBatteryContentService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.AnswerUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author cerberus
 */
@WebServlet(name = "ReadTestBatteryContent", urlPatterns = {"/ReadTestBatteryContent"})
public class ReadTestBatteryContent extends HttpServlet {

    private ICampaignService campaignService;

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
        String columnName = ParameterParserUtil.parseStringParam(request.getParameter("columnName"), "");

        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");

        try {
            JSONObject jsonResponse = new JSONObject();
            AnswerItem answer = new AnswerItem(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED));

            if (!Strings.isNullOrEmpty(columnName)) {
                if(request.getParameter("campaign") != null) {
                    answer = findDistinctValuesOfColumnByCampaign(request.getParameter("campaign"), appContext, request, columnName);
                    jsonResponse = (JSONObject) answer.getItem();
                }
            } else {
                if(request.getParameter("campaign") != null) {
                    answer = findBatteryListByCampaign(request.getParameter("campaign"), true, appContext, request);
                    jsonResponse = (JSONObject) answer.getItem();
                }
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

    private JSONObject convertTestBatteryContenttoJSONObject(TestBatteryContent campaign) throws JSONException {
        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(campaign));
        return result;
    }

    private AnswerItem findBatteryListByCampaign(String key, Boolean userHasPermissions, ApplicationContext appContext, HttpServletRequest request) throws JSONException {
        AnswerItem item = new AnswerItem();
        JSONObject object = new JSONObject();

        campaignService = appContext.getBean(ICampaignService.class);

        JSONArray response = new JSONArray();

        int startPosition = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayStart"), "0"));
        int length = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayLength"), "0"));

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        int columnToSortParameter = Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortCol_0"), "0"));
        String sort = ParameterParserUtil.parseStringParam(request.getParameter("sSortDir_0"), "asc");

        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "tbc.testbattery,tbc.Test,tbc.Testcase, cpc.campaign");Map<String, List<String>> individualSearch = new HashMap<String, List<String>>();

        String columnToSort[] = sColumns.split(",");
        String columnName = columnToSort[columnToSortParameter];

        for (int a = 0; a < columnToSort.length; a++) {
            if (null != request.getParameter("sSearch_" + a) && !request.getParameter("sSearch_" + a).isEmpty()) {
                List<String> search = new ArrayList(Arrays.asList(request.getParameter("sSearch_" + a).split(",")));
                individualSearch.put(columnToSort[a], search);
            }
        }

        ITestBatteryContentService testBatteryContentService = appContext.getBean(ITestBatteryContentService.class);

        AnswerList resp = testBatteryContentService.readByCampaignByCriteria(key, startPosition, length,columnName, sort,searchParameter,individualSearch);
        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            for(Object b : resp.getDataList() ) {
                TestBatteryContent tc = (TestBatteryContent) b;
                JSONObject tcJSON = convertTestBatteryContenttoJSONObject(tc);
                response.put(tcJSON);
            }
        }
        object.put("iTotalRecords", resp.getTotalRows());
        object.put("iTotalDisplayRecords", resp.getTotalRows());

        object.put("contentTable", response);

        object.put("hasPermissions", userHasPermissions);
        item.setItem(object);
        item.setResultMessage(resp.getResultMessage());

        return item;
    }

    private AnswerItem findDistinctValuesOfColumnByCampaign(String campaign, ApplicationContext appContext, HttpServletRequest request, String columnName) throws JSONException {
        AnswerItem answer = new AnswerItem();
        JSONObject object = new JSONObject();

        ITestBatteryContentService testBatteryContentService = appContext.getBean(ITestBatteryContentService.class);

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "para,valC,valS,descr");
        String columnToSort[] = sColumns.split(",");

        Map<String, List<String>> individualSearch = new HashMap<String, List<String>>();
        for (int a = 0; a < columnToSort.length; a++) {
            if (null != request.getParameter("sSearch_" + a) && !request.getParameter("sSearch_" + a).isEmpty()) {
                List<String> search = new ArrayList(Arrays.asList(request.getParameter("sSearch_" + a).split(",")));
                individualSearch.put(columnToSort[a], search);
            }
        }

        AnswerList applicationList = testBatteryContentService.readDistinctValuesByCampaignByCriteria(campaign, searchParameter, individualSearch, columnName);

        object.put("distinctValues", applicationList.getDataList());

        answer.setItem(object);
        answer.setResultMessage(applicationList.getResultMessage());
        return answer;
    }
}
