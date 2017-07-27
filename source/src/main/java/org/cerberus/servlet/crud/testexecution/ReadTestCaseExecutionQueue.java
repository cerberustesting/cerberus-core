/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.servlet.crud.testexecution;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.TestCaseExecutionQueue;
import org.cerberus.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.AnswerUtil;
import org.cerberus.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author foudro
 */
@WebServlet(name = "ReadTestCaseExecutionQueue", urlPatterns = {"/ReadTestCaseExecutionQueue"})
public class ReadTestCaseExecutionQueue extends HttpServlet {

    private ITestCaseExecutionQueueService executionService;

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ReadTestCaseExecutionQueue.class);

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws CerberusException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, CerberusException {
        String echo = request.getParameter("sEcho");
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        // Default message to unexpected error.
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        // Init Answer with potencial error from Parsing parameter.
        String queueId = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("queueid"), "");
        Long queueid = Long.valueOf(0);
        boolean queueid_error = false;
        if (request.getParameter("queueid") != null) {
            try {
                if (request.getParameter("queueid") != null && !request.getParameter("queueid").equals("")) {
                    queueid = Long.valueOf(policy.sanitize(request.getParameter("queueid")));
                    queueid_error = false;
                }
            } catch (Exception ex) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Case Execution Queue"));
                msg.setDescription(msg.getDescription().replace("%OPERATION%", "Read"));
                msg.setDescription(msg.getDescription().replace("%REASON%", "queueid must be an integer value."));
                queueid_error = true;
            }
        }
        AnswerItem answer = new AnswerItem(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));

        // Global boolean on the servlet that define if the user has permition to edit and delete object.
        boolean userHasPermissions = request.isUserInRole("RunTest");
        
        try {
            JSONObject jsonResponse;

            if (!Strings.isNullOrEmpty(request.getParameter("columnName"))) {
                answer = findDistinctValuesOfColumn(appContext, request, request.getParameter("columnName"));
                jsonResponse = (JSONObject) answer.getItem();
            } else if (queueid != 0) {
                    answer = findExecutionQueueByKeyTech(queueid, appContext, userHasPermissions);
                    jsonResponse = (JSONObject) answer.getItem();
            } else {
                answer = findExecutionInQueueList(appContext, true, request);
                jsonResponse = (JSONObject) answer.getItem();
            }

            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());
            jsonResponse.put("sEcho", echo);

            response.getWriter().print(jsonResponse.toString());

        } catch (JSONException e) {
            org.apache.log4j.Logger.getLogger(ReadTestCaseExecutionQueue.class.getName()).log(org.apache.log4j.Level.ERROR, null, e);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        }
    }

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
        } catch (CerberusException ex) {
            Logger.getLogger(ReadTestCaseExecutionQueue.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
        } catch (CerberusException ex) {
            Logger.getLogger(ReadTestCaseExecutionQueue.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    private AnswerItem findExecutionQueueByKeyTech(Long queueid, ApplicationContext appContext, boolean userHasPermissions) throws JSONException, CerberusException {
        AnswerItem item = new AnswerItem();
        JSONObject object = new JSONObject();

        ITestCaseExecutionQueueService queueService = appContext.getBean(ITestCaseExecutionQueueService.class);

        //finds the project     
        AnswerItem answer = queueService.readByKey(queueid);

        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item and convert it to JSONformat
            TestCaseExecutionQueue lib = (TestCaseExecutionQueue) answer.getItem();
            JSONObject response = convertTestCaseExecutionInQueueToJSONObject(lib);
            object.put("contentTable", response);
        }

        object.put("hasPermissions", userHasPermissions);
        item.setItem(object);
        item.setResultMessage(answer.getResultMessage());

        return item;
    }

    private AnswerItem findExecutionInQueueList(ApplicationContext appContext, boolean userHasPermissions, HttpServletRequest request) throws JSONException {

        AnswerItem item = new AnswerItem();
        JSONObject object = new JSONObject();
        executionService = appContext.getBean(ITestCaseExecutionQueueService.class);

        int startPosition = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayStart"), "0"));
        int length = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayLength"), "0"));
        /*int sEcho  = Integer.valueOf(request.getParameter("sEcho"));*/

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        int columnToSortParameter = Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortCol_0"), "2"));
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "id,test,testcase,country,environment,browser,tag");
        String columnToSort[] = sColumns.split(",");
        String columnName = columnToSort[columnToSortParameter];
        String sort = ParameterParserUtil.parseStringParam(request.getParameter("sSortDir_0"), "desc");

        Map<String, List<String>> individualSearch = new HashMap<>();
        for (int a = 0; a < columnToSort.length; a++) {
            if (null != request.getParameter("sSearch_" + a) && !request.getParameter("sSearch_" + a).isEmpty()) {
                List<String> search = new ArrayList(Arrays.asList(request.getParameter("sSearch_" + a).split(",")));
                individualSearch.put(columnToSort[a], search);
            }
        }

        AnswerList resp = executionService.readByCriteria(startPosition, length, columnName, sort, searchParameter, individualSearch);

        JSONArray jsonArray = new JSONArray();
        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            for (TestCaseExecutionQueue exec : (List<TestCaseExecutionQueue>) resp.getDataList()) {
                jsonArray.put(convertTestCaseExecutionInQueueToJSONObject(exec));
            }
        }

        object.put("hasPermissions", userHasPermissions);
        object.put("contentTable", jsonArray);
        object.put("iTotalRecords", resp.getTotalRows());
        object.put("iTotalDisplayRecords", resp.getTotalRows());

        item.setItem(object);
        item.setResultMessage(resp.getResultMessage());
        return item;
    }

    private AnswerItem findDistinctValuesOfColumn(ApplicationContext appContext, HttpServletRequest request, String columnName) throws JSONException {
        AnswerItem answer = new AnswerItem();
        JSONObject object = new JSONObject();
        AnswerList values = new AnswerList();
        Map<String, List<String>> individualSearch = new HashMap();

        executionService = appContext.getBean(ITestCaseExecutionQueueService.class);

        String column = ParameterParserUtil.parseStringParam(request.getParameter("columnName"), "");
        String sort = ParameterParserUtil.parseStringParam(request.getParameter("sSortDir_0"), "asc");

        LOG.debug(columnName);
        switch (columnName) {
            /**
             * Columns from Status
             */
            case "state":
                List<String> dataList = new ArrayList<>();
                dataList.add(TestCaseExecutionQueue.State.WAITING.name());
                dataList.add(TestCaseExecutionQueue.State.QUEUED.name());
                dataList.add(TestCaseExecutionQueue.State.EXECUTING.name());
                dataList.add(TestCaseExecutionQueue.State.ERROR.name());
                dataList.add(TestCaseExecutionQueue.State.DONE.name());
                dataList.add(TestCaseExecutionQueue.State.CANCELLED.name());
                values.setDataList(dataList);
                MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "execution").replace("%OPERATION%", "SELECT"));
                values.setResultMessage(msg);
                break;
            /**
             * For all other columns, get distinct values from testcaseexecution
             */
            default:
                String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
                String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "id,test,testcase,country,environment,browser,tag");
                String columnToSort[] = sColumns.split(",");

                individualSearch = new HashMap<>();
                for (int a = 0; a < columnToSort.length; a++) {
                    if (null != request.getParameter("sSearch_" + a) && !request.getParameter("sSearch_" + a).isEmpty()) {
                        List<String> search = new ArrayList(Arrays.asList(request.getParameter("sSearch_" + a).split(",")));
                        individualSearch.put(columnToSort[a], search);
                    }
                }
                values = executionService.readDistinctValuesByCriteria(columnName, sort, searchParameter, individualSearch, column);
        }

        object.put("distinctValues", values.getDataList());

        answer.setItem(object);
        answer.setResultMessage(values.getResultMessage());
        return answer;
    }

    private JSONObject convertTestCaseExecutionInQueueToJSONObject(TestCaseExecutionQueue exec) throws JSONException {

        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(exec));
        return result;
    }

}
