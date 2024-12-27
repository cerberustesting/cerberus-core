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
package org.cerberus.core.servlet.crud.testexecution;

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
import org.cerberus.core.crud.entity.TestCaseExecutionQueue;
import org.cerberus.core.crud.factory.IFactoryInvariant;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.engine.queuemanagement.entity.TestCaseExecutionQueueToTreat;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.cerberus.core.util.servlet.ServletUtil;
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
    private IExecutionThreadPoolService executionThreadPoolService;
    private IParameterService parameterService;
    private IInvariantService invariantService;
    private IFactoryInvariant factoryInvariant;

    private static final Logger LOG = LogManager.getLogger(ReadTestCaseExecutionQueue.class);

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
                if (request.getParameter("queueid") != null && !request.getParameter("queueid").isEmpty()) {
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
        AnswerItem answer = new AnswerItem<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));

        // Global boolean on the servlet that define if the user has permition to edit and delete object.
        boolean userHasPermissions = request.isUserInRole("RunTest");

        try {
            JSONObject jsonResponse;

            if (!StringUtil.isEmptyOrNull(request.getParameter("columnName"))) {
                answer = findDistinctValuesOfColumn(appContext, request, request.getParameter("columnName"));
                jsonResponse = (JSONObject) answer.getItem();
            } else if (queueid != 0) {
                answer = findExecutionQueueByKeyTech(queueid, appContext, userHasPermissions);
                jsonResponse = (JSONObject) answer.getItem();
            } else if (request.getParameter("flag") != null && request.getParameter("flag").equals("queueStatus")) {
                answer = findExecutionInQueueStatus(appContext, request);
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
            LOG.warn(e);
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
            LOG.warn(ex);
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
            LOG.warn(ex);
        }
    }

    private AnswerItem<JSONObject> findExecutionQueueByKeyTech(Long queueid, ApplicationContext appContext, boolean userHasPermissions) throws JSONException, CerberusException {
        AnswerItem<JSONObject> item = new AnswerItem<>();
        JSONObject object = new JSONObject();

        ITestCaseExecutionQueueService queueService = appContext.getBean(ITestCaseExecutionQueueService.class);

        //finds the project     
        AnswerItem answer = queueService.readByKey(queueid, true);

        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item and convert it to JSONformat
            TestCaseExecutionQueue queueEntry = (TestCaseExecutionQueue) answer.getItem();
            JSONObject response = convertTestCaseExecutionInQueueToJSONObject(queueEntry);

            // Adding nb of entries in the queue before it gets triggered.
            int nb = 0;
            if (TestCaseExecutionQueue.State.QUEUED.equals(queueEntry.getState())) {
                nb = queueService.getNbEntryToGo(queueEntry.getId(), queueEntry.getPriority());
            }
            response.put("nbEntryInQueueToGo", nb);

            object.put("contentTable", response);
        }

        object.put("hasPermissions", userHasPermissions);
        item.setItem(object);
        item.setResultMessage(answer.getResultMessage());

        return item;
    }

    private AnswerItem<JSONObject> findExecutionInQueueList(ApplicationContext appContext, boolean userHasPermissions, HttpServletRequest request) throws JSONException {

        AnswerItem<JSONObject> item = new AnswerItem<>();
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
        List<String> individualLike = new ArrayList<>(Arrays.asList(request.getParameter("sLike").split(",")));

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

        AnswerList<TestCaseExecutionQueue> resp = executionService.readByCriteria(startPosition, length, columnName, sort, searchParameter, individualSearch);

        JSONArray jsonArray = new JSONArray();
        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            for (TestCaseExecutionQueue exec : resp.getDataList()) {
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

    private AnswerItem<JSONObject> findExecutionInQueueStatus(ApplicationContext appContext, HttpServletRequest request) throws JSONException {
        AnswerItem<JSONObject> item = new AnswerItem<>();
        JSONObject object = new JSONObject();
        executionThreadPoolService = appContext.getBean(IExecutionThreadPoolService.class);
        parameterService = appContext.getBean(IParameterService.class);
        invariantService = appContext.getBean(IInvariantService.class);
        factoryInvariant = appContext.getBean(IFactoryInvariant.class);
        JSONArray jsonArray = new JSONArray();

        try {
            HashMap<String, Integer> mapRunning = executionThreadPoolService.getCurrentlyRunning();
            HashMap<String, Integer> mapInQueue = executionThreadPoolService.getCurrentlyToTreat();
            HashMap<String, Integer> mapPoolSize = executionThreadPoolService.getCurrentlyPoolSizes();
            for (Map.Entry<String, Integer> entry : mapRunning.entrySet()) {
                String column = entry.getKey();
                Integer name = entry.getValue();
                if (!("".equals(column))) {
                    String[] data = column.split("\\/\\/\\/\\/");
                    JSONObject jsonObject = new JSONObject();
                    switch (data[0]) {
                        case TestCaseExecutionQueueToTreat.CONSTRAIN1_GLOBAL:
                            jsonObject.put("contrainId", data[0]);
                            jsonObject.put("system", "");
                            jsonObject.put("environment", "");
                            jsonObject.put("country", "");
                            jsonObject.put("application", "");
                            jsonObject.put("robot", "");
                            jsonObject.put("nbInQueue", ParameterParserUtil.parseIntegerParam(mapInQueue.get(column), 0));
                            jsonObject.put("nbPoolSize", ParameterParserUtil.parseIntegerParam(mapPoolSize.get(column), 0));
                            jsonObject.put("nbRunning", ParameterParserUtil.parseIntegerParam(name, 0));
                            jsonObject.put("hasPermissionsUpdate", parameterService.hasPermissionsUpdate("cerberus_queueexecution_global_threadpoolsize", request));
                            break;
                        case TestCaseExecutionQueueToTreat.CONSTRAIN2_APPLIENV:
                            jsonObject.put("contrainId", data[0]);
                            jsonObject.put("system", data[1]);
                            jsonObject.put("environment", data[2]);
                            jsonObject.put("country", data[3]);
                            jsonObject.put("application", data[4]);
                            jsonObject.put("robot", "");
                            jsonObject.put("nbInQueue", ParameterParserUtil.parseIntegerParam(mapInQueue.get(column), 0));
                            jsonObject.put("nbPoolSize", ParameterParserUtil.parseIntegerParam(mapPoolSize.get(column), 0));
                            jsonObject.put("nbRunning", ParameterParserUtil.parseIntegerParam(name, 0));
                            jsonObject.put("hasPermissionsUpdate", true);
                            break;
                        case TestCaseExecutionQueueToTreat.CONSTRAIN3_APPLICATION:
                            jsonObject.put("contrainId", data[0]);
                            jsonObject.put("system", "");
                            jsonObject.put("environment", "");
                            jsonObject.put("country", "");
                            jsonObject.put("application", data[1]);
                            jsonObject.put("robot", "");
                            jsonObject.put("nbInQueue", ParameterParserUtil.parseIntegerParam(mapInQueue.get(column), 0));
                            jsonObject.put("nbPoolSize", ParameterParserUtil.parseIntegerParam(mapPoolSize.get(column), 0));
                            jsonObject.put("nbRunning", ParameterParserUtil.parseIntegerParam(name, 0));
                            jsonObject.put("hasPermissionsUpdate", true);
                            break;
                        case TestCaseExecutionQueueToTreat.CONSTRAIN4_ROBOT:
                            jsonObject.put("contrainId", data[0]);
                            jsonObject.put("system", "");
                            jsonObject.put("environment", "");
                            jsonObject.put("country", "");
                            jsonObject.put("application", "");
                            if (data.length > 1) {
                                jsonObject.put("robot", data[1]);
                                if ((data[1] == null) || (data[1].equalsIgnoreCase("null"))) {
                                    jsonObject.put("invariantExist", false);
                                } else {
                                    jsonObject.put("invariantExist", invariantService.isInvariantExist("ROBOTHOST", data[1]));
                                }
                            } else {
                                jsonObject.put("robot", "");
                                jsonObject.put("invariantExist", false);
                            }
                            // We cannot determine the Nb of execution in the queue attached to a given Robot as it could be spread by ROUNDROBIN or BYRANKING. The result is subject to change depending on duration of each execution.
                            // jsonObject.put("nbInQueue", ParameterParserUtil.parseIntegerParam(mapInQueue.get(column), 0));
                            jsonObject.put("nbInQueue", "");
                            jsonObject.put("nbPoolSize", ParameterParserUtil.parseIntegerParam(mapPoolSize.get(column), 0));
                            jsonObject.put("nbRunning", ParameterParserUtil.parseIntegerParam(name, 0));
                            jsonObject.put("hasPermissionsUpdate", invariantService.hasPermissionsUpdate(factoryInvariant.create("ROBOTHOST", "", 0, "", "", "", "", "", "", "", "", "", "", ""), request));
                            break;
                        case TestCaseExecutionQueueToTreat.CONSTRAIN5_EXECUTOREXTENSION:
                            jsonObject.put("contrainId", data[0]);
                            jsonObject.put("system", "");
                            jsonObject.put("environment", "");
                            jsonObject.put("country", "");
                            jsonObject.put("application", "");
                            if (data.length > 1) {
                                jsonObject.put("robot", data[1]);
                                if ((data[1] == null) || (data[1].equalsIgnoreCase("null"))) {
                                    jsonObject.put("invariantExist", false);
                                } else {
                                    jsonObject.put("invariantExist", invariantService.isInvariantExist("ROBOTPROXYHOST", data[1]));
                                }
                            } else {
                                jsonObject.put("robot", "");
                                jsonObject.put("invariantExist", false);
                            }
                            // We cannot determine the Nb of execution in the queue attached to a given Robot as it could be spread by ROUNDROBIN or BYRANKING. The result is subject to change depending on duration of each execution.
                            // jsonObject.put("nbInQueue", ParameterParserUtil.parseIntegerParam(mapInQueue.get(column), 0));
                            jsonObject.put("nbInQueue", "");
                            jsonObject.put("nbPoolSize", ParameterParserUtil.parseIntegerParam(mapPoolSize.get(column), 0));
                            jsonObject.put("nbRunning", ParameterParserUtil.parseIntegerParam(name, 0));
                            jsonObject.put("hasPermissionsUpdate", invariantService.hasPermissionsUpdate(factoryInvariant.create("ROBOTPROXYHOST", "", 0, "", "", "", "", "", "", "", "", "", "", ""), request));
                            break;
                    }
                    jsonArray.put(jsonObject);
                }
            }
            object.put("contentTable", jsonArray);

        } catch (CerberusException ex) {
            LOG.warn(ex, ex);
        } catch (Exception ex) {
            LOG.error(ex, ex);
        }

        object.put("messageType", "");
        object.put("message", "");
        item.setResultMessage(new MessageEvent(MessageEventEnum.GENERIC_OK));
        item.setItem(object);
        return item;
    }

    private AnswerItem<JSONObject> findDistinctValuesOfColumn(ApplicationContext appContext, HttpServletRequest request, String columnName) throws JSONException {
        AnswerItem<JSONObject> answer = new AnswerItem<>();
        JSONObject object = new JSONObject();
        AnswerList<String> values = new AnswerList<>();

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
                dataList.add(TestCaseExecutionQueue.State.QUEUED.name());
                dataList.add(TestCaseExecutionQueue.State.WAITING.name());
                dataList.add(TestCaseExecutionQueue.State.STARTING.name());
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
