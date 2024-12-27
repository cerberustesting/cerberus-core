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
package org.cerberus.core.servlet.guipages;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.impl.ApplicationService;
import org.cerberus.core.crud.service.impl.InvariantService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author ip100003
 */
@WebServlet(name = "Homepage", urlPatterns = {"/Homepage"})
public class Homepage extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(Homepage.class);

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

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        AnswerItem answer = new AnswerItem<>(MessageEventEnum.DATA_OPERATION_OK);

        try {
            JSONObject jsonResponse = new JSONObject();

            if (request.getParameter("system") != null) {
                List<String> system = ParameterParserUtil.parseListParamAndDeleteEmptyValue(request.getParameterValues("system"), Arrays.asList("DEFAULT"), "UTF-8");
                answer = readApplicationList(system, appContext);
                jsonResponse = (JSONObject) answer.getItem();
            }

            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());

            response.setContentType("application/json");
            response.getWriter().print(jsonResponse.toString());

        } catch (JSONException e) {
            LOG.warn(e);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        }
    }

    private AnswerItem<JSONObject> readApplicationList(List<String> system, ApplicationContext appContext) throws JSONException {
        AnswerItem<JSONObject> item = new AnswerItem<>();
        JSONObject jsonResponse = new JSONObject();
        IApplicationService applicationService = appContext.getBean(ApplicationService.class);

        AnswerItem<HashMap<String, HashMap<String, Integer>>> resp = applicationService.readTestCaseCountersBySystemByStatus(system);

        JSONArray jsonArray = new JSONArray();
        HashMap<String, HashMap<String, Integer>> totalMap = resp.getItem();

        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null) {
            IInvariantService invariantService = appContext.getBean(InvariantService.class);
            AnswerList<Invariant> answerList = invariantService.readByIdnameNotGp1("TCSTATUS", "N");
            List<Invariant> myInvariants = answerList.getDataList();
            for (String application : totalMap.keySet()) {
                JSONObject row = extractRow(application, totalMap, myInvariants);
                jsonArray.put(row);
            }
        }

        jsonResponse.put("aaData", jsonArray);
        jsonResponse.put("iTotalRecords", totalMap.size());
        jsonResponse.put("iTotalDisplayRecords", totalMap.size());

        item.setItem(jsonResponse);
        item.setResultMessage(resp.getResultMessage());
        return item;

    }

    private JSONObject extractRow(String application, HashMap<String, HashMap<String, Integer>> totalMap, List<Invariant> myInvariants) throws JSONException {

        JSONObject row = new JSONObject();

        row.put("Application", application);

        HashMap mapApplication = totalMap.get(application);
        int totalPerApplication = 0;

        for (Invariant i : myInvariants) {
            Integer total = (Integer) mapApplication.get(i.getValue());
            totalPerApplication += (total == null ? 0 : total);
        }
        row.put("Total", totalPerApplication);

        for (Invariant i : myInvariants) {
            Integer total = (Integer) mapApplication.get(i.getValue());
            if (total == null) {
                row.put(i.getValue(), 0);
            } else {
                row.put(i.getValue(), total);
            }
        }

        return row;
    }
}
