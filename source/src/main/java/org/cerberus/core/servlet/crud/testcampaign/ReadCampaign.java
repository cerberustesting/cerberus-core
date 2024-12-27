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
package org.cerberus.core.servlet.crud.testcampaign;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.Campaign;
import org.cerberus.core.crud.entity.CampaignLabel;
import org.cerberus.core.crud.entity.CampaignParameter;
import org.cerberus.core.crud.entity.EventHook;
import org.cerberus.core.crud.entity.ScheduleEntry;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.service.ICampaignLabelService;
import org.cerberus.core.crud.service.ICampaignParameterService;
import org.cerberus.core.crud.service.ICampaignService;
import org.cerberus.core.crud.service.IEventHookService;
import org.cerberus.core.crud.service.IScheduleEntryService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author cerberus
 */
@WebServlet(name = "ReadCampaign", urlPatterns = {"/ReadCampaign"})
public class ReadCampaign extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ReadCampaign.class);
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

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        // Global boolean on the servlet that define if the user has permition to edit and delete object.
        boolean userHasPermissions = request.isUserInRole("RunTest");

        try {
            JSONObject jsonResponse = new JSONObject();
            AnswerItem answer = new AnswerItem<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED));

            if (request.getParameter("campaign") == null && StringUtil.isEmptyOrNull(columnName)) {
                answer = findCampaignList(userHasPermissions, appContext, request);
                jsonResponse = (JSONObject) answer.getItem();
            } else if (!StringUtil.isEmptyOrNull(columnName)) {
                answer = findDistinctValuesOfColumn(appContext, request, columnName);
                jsonResponse = (JSONObject) answer.getItem();
            } else {
                answer = findCampaignByKey(request.getParameter("campaign"), userHasPermissions, appContext, request);
                jsonResponse = (JSONObject) answer.getItem();
            }

            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());

            response.getWriter().print(jsonResponse.toString());
        } catch (JSONException ex) {
            LOG.warn(ex);
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

    private AnswerItem<JSONObject> findCampaignList(Boolean userHasPermissions, ApplicationContext appContext, HttpServletRequest request) throws JSONException {
        AnswerItem<JSONObject> item = new AnswerItem<>();
        AnswerList<Campaign> answer = new AnswerList<>();
        JSONObject resp = new JSONObject();

        int startPosition = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayStart"), "0"));
        int length = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayLength"), "0"));

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        int columnToSortParameter = Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortCol_0"), "1"));
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "campaignid,campaign,Description");
        String columnToSort[] = sColumns.split(",");
        String columnName = columnToSort[columnToSortParameter];
        String sort = ParameterParserUtil.parseStringParam(request.getParameter("sSortDir_0"), "asc");
        List<String> individualLike = new ArrayList<>(Arrays.asList(ParameterParserUtil.parseStringParam(request.getParameter("sLike"), "").split(",")));

        campaignService = appContext.getBean(ICampaignService.class);

        Map<String, List<String>> individualSearch = new HashMap<>();
        for (int a = 0; a < columnToSort.length; a++) {
            if (null != request.getParameter("sSearch_" + a) && !request.getParameter("sSearch_" + a).isEmpty()) {
                List<String> search = new ArrayList<>(Arrays.asList(request.getParameter("sSearch_" + a).split(",")));
                if (individualLike.contains(columnToSort[a])) {
                    individualSearch.put(columnToSort[a] + ":like", search);
                } else {
                    individualSearch.put(columnToSort[a], search);
                }
            }
        }

        answer = campaignService.readByCriteria(startPosition, length, columnName, sort, searchParameter, individualSearch);

//        boolean userHasPermissions = request.isUserInRole("TestAdmin");
        JSONArray jsonArray = new JSONArray();
        HashMap<String, Boolean> gp1 = new HashMap<>();
        HashMap<String, Boolean> gp2 = new HashMap<>();
        HashMap<String, Boolean> gp3 = new HashMap<>();

        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values

            for (Campaign campaign : answer.getDataList()) {
                jsonArray.put(convertToJSONObject(campaign));
                if (!StringUtil.isEmptyOrNull(campaign.getGroup1())) {
                    gp1.put(campaign.getGroup1(), true);
                }
                if (!StringUtil.isEmptyOrNull(campaign.getGroup2())) {
                    gp2.put(campaign.getGroup2(), true);
                }
                if (!StringUtil.isEmptyOrNull(campaign.getGroup3())) {
                    gp3.put(campaign.getGroup3(), true);
                }
            }
        }

        JSONObject dist = new JSONObject();
        JSONArray distinct = new JSONArray();
        for (Map.Entry<String, Boolean> entry : gp1.entrySet()) {
            String key = entry.getKey();
            distinct.put(key);
        }
        dist.put("group1", distinct);
        distinct = new JSONArray();
        for (Map.Entry<String, Boolean> entry : gp2.entrySet()) {
            String key = entry.getKey();
            distinct.put(key);
        }
        dist.put("group2", distinct);
        distinct = new JSONArray();
        for (Map.Entry<String, Boolean> entry : gp3.entrySet()) {
            String key = entry.getKey();
            distinct.put(key);
        }
        dist.put("group3", distinct);
        resp.put("distinct", dist);

        resp.put("contentTable", jsonArray);
        resp.put("hasPermissions", userHasPermissions);
        resp.put("iTotalRecords", answer.getTotalRows());
        resp.put("iTotalDisplayRecords", answer.getTotalRows());

        item.setItem(resp);
        item.setResultMessage(answer.getResultMessage());

        return item;
    }

    private AnswerItem<JSONObject> findCampaignByKey(String key, Boolean userHasPermissions, ApplicationContext appContext, HttpServletRequest request) throws JSONException {
        AnswerItem<JSONObject> item = new AnswerItem<>();
        JSONObject object = new JSONObject();

        campaignService = appContext.getBean(ICampaignService.class);

        AnswerItem<Campaign> campaignAnswerItem = campaignService.readByKey(key);
        if (campaignAnswerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values

            JSONObject response = this.<Campaign>convertToJSONObject(campaignAnswerItem.getItem());

            if (request.getParameter("parameters") != null) {
                ICampaignParameterService campaignParameterService = appContext.getBean(ICampaignParameterService.class);
                AnswerList<CampaignParameter> resp = campaignParameterService.readByCampaign(key);
                if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
                    JSONArray a = new JSONArray();
                    for (CampaignParameter c : resp.getDataList()) {
                        a.put(this.<CampaignParameter>convertToJSONObject(c));
                    }
                    response.put("parameters", a);
                }
            }
            if (request.getParameter("labels") != null) {
                ICampaignLabelService campaignLabelService = appContext.getBean(ICampaignLabelService.class);
                AnswerList<CampaignLabel> resp = campaignLabelService.readByVarious(key);
                if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
                    JSONArray a = new JSONArray();
                    for (CampaignLabel c : resp.getDataList()) {
                        a.put(this.<CampaignLabel>convertToJSONObject(c));
                    }
                    response.put("labels", a);
                }
            }
            if (request.getParameter("testcases") != null) {
                ITestCaseService testCaseService = appContext.getBean(ITestCaseService.class);
                AnswerList<TestCase> resp = testCaseService.findTestCaseByCampaignNameAndCountries(key, null);
                if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
                    JSONArray a = new JSONArray();
                    for (TestCase c : resp.getDataList()) {
                        a.put(convertTestCasetoJSONObject(c));
                    }
                    response.put("testcase", a);
                }
            }
            if (request.getParameter("tags") != null) {
                ITagService tagService = appContext.getBean(ITagService.class);
                AnswerList<Tag> resp = tagService.readByCampaign(key);
                if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
                    JSONArray a = new JSONArray();
                    for (Tag c : resp.getDataList()) {
                        a.put(c.toJson());
                    }
                    response.put("tags", a);
                   
                }
            }
            if (request.getParameter("eventHooks") != null) {
                IEventHookService eventHookService = appContext.getBean(IEventHookService.class);
                AnswerList<EventHook> resp = eventHookService.readByEventReference(Arrays.asList(EventHook.EVENTREFERENCE_CAMPAIGN_START, EventHook.EVENTREFERENCE_CAMPAIGN_END, EventHook.EVENTREFERENCE_CAMPAIGN_END_CIKO),
                        Arrays.asList(key));
                if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
                    JSONArray a = new JSONArray();
                    for (EventHook c : resp.getDataList()) {
                        a.put(c.toJson());
                    }
                    response.put("eventHooks", a);
                }
            }

            if (request.getParameter("scheduledEntries") != null) {
                IScheduleEntryService scheduleEntryService = appContext.getBean(IScheduleEntryService.class);
                AnswerList<ScheduleEntry> resp = scheduleEntryService.readByName(key);
                if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
                    JSONArray a = new JSONArray();
                    for (ScheduleEntry c : resp.getDataList()) {
                        a.put(c.toJson());
                    }
                    response.put("scheduledEntries", a);
                }
            }

            object.put("contentTable", response);
        }
        object.put("hasPermissions", userHasPermissions);
        item.setItem(object);
        item.setResultMessage(campaignAnswerItem.getResultMessage());

        return item;
    }

    private AnswerItem<JSONObject> findDistinctValuesOfColumn(ApplicationContext appContext, HttpServletRequest request, String columnName) throws JSONException {
        AnswerItem<JSONObject> answer = new AnswerItem<>();
        JSONObject object = new JSONObject();

        campaignService = appContext.getBean(ICampaignService.class);

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "para,valC,valS,descr");
        String[] columnToSort = sColumns.split(",");

        List<String> individualLike = new ArrayList<>(Arrays.asList(ParameterParserUtil.parseStringParam(request.getParameter("sLike"), "").split(",")));

        Map<String, List<String>> individualSearch = new HashMap<>();
        for (int a = 0; a < columnToSort.length; a++) {
            if (null != request.getParameter("sSearch_" + a) && !request.getParameter("sSearch_" + a).isEmpty()) {
                List<String> search = new ArrayList<>(Arrays.asList(request.getParameter("sSearch_" + a).split(",")));
                if (individualLike.contains(columnToSort[a])) {
                    individualSearch.put(columnToSort[a] + ":like", search);
                } else {
                    individualSearch.put(columnToSort[a], search);
                }
            }
        }

        AnswerList applicationList = campaignService.readDistinctValuesByCriteria(searchParameter, individualSearch, columnName);

        object.put("distinctValues", applicationList.getDataList());

        answer.setItem(object);
        answer.setResultMessage(applicationList.getResultMessage());
        return answer;
    }

    private <T> JSONObject convertToJSONObject(T object) throws JSONException {
        Gson gson = new Gson();
        return new JSONObject(gson.toJson(object));
    }

    private JSONObject convertTestCasetoJSONObject(TestCase testCase) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("test", testCase.getTest());
        result.put("testCase", testCase.getTestcase());
        result.put("application", testCase.getApplication());
        result.put("description", testCase.getDescription());
        result.put("status", testCase.getStatus());
        return result;
    }

}
