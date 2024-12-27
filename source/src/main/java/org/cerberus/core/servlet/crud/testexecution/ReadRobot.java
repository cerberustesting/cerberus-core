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
import org.cerberus.core.crud.entity.Robot;
import org.cerberus.core.crud.service.IRobotService;
import org.cerberus.core.crud.service.impl.RobotService;
import org.cerberus.core.engine.entity.MessageEvent;
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
 *
 * @author vertigo
 */
@WebServlet(name = "ReadRobot", urlPatterns = {"/ReadRobot"})
public class ReadRobot extends HttpServlet {

    private IRobotService robotService;
    private static final Logger LOG = LogManager.getLogger(ReadRobot.class);

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws org.cerberus.core.exception.CerberusException
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

        /**
         * Parsing and securing all required parameters.
         */
        String robot = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("robot"), "");
        boolean withCaps = ParameterParserUtil.parseBooleanParam(request.getParameter("withCapabilities"), false);
        boolean withExecutors = ParameterParserUtil.parseBooleanParam(request.getParameter("withExecutors"), false);
        Integer robotid = 0;
        boolean robotid_error = false;
        if (request.getParameter("robotid") != null) {
            try {
                if (request.getParameter("robotid") != null && !request.getParameter("robotid").isEmpty()) {
                    robotid = Integer.valueOf(policy.sanitize(request.getParameter("robotid")));
                    robotid_error = false;
                }
            } catch (Exception ex) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Robot"));
                msg.setDescription(msg.getDescription().replace("%OPERATION%", "Read"));
                msg.setDescription(msg.getDescription().replace("%REASON%", "robotid must be an integer value."));
                robotid_error = true;
            }
        }
        String columnName = ParameterParserUtil.parseStringParam(request.getParameter("columnName"), "");

        // Global boolean xon the servlet that define if the user has permition to edit and delete object.
        boolean userHasPermissions = request.isUserInRole("Integrator");

        // Init Answer with potencial error from Parsing parameter.
        AnswerItem answer = new AnswerItem<>(msg);

        try {
            JSONObject jsonResponse = new JSONObject();
            if (!robotid_error) {
                if (!(request.getParameter("robotid") == null)) {
                    answer = findRobotByKeyTech(robotid, appContext, userHasPermissions);
                    jsonResponse = (JSONObject) answer.getItem();
                } else if (!(request.getParameter("robot") == null)) {
                    answer = findRobotByKey(robot, appContext, request);
                    jsonResponse = (JSONObject) answer.getItem();
                } else if (!StringUtil.isEmptyOrNull(columnName)) {
                    //If columnName is present, then return the distinct value of this column.
                    answer = findDistinctValuesOfColumn(appContext, request, columnName);
                    jsonResponse = (JSONObject) answer.getItem();
                } else {
                    answer = findRobotList(withCaps, withExecutors, appContext, userHasPermissions, request);
                    jsonResponse = (JSONObject) answer.getItem();
                }
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

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private AnswerItem<JSONObject> findRobotList(boolean withCaps, boolean withExecutors, ApplicationContext appContext, boolean userHasPermissions, HttpServletRequest request) throws JSONException {

        AnswerItem<JSONObject> item = new AnswerItem<>();
        JSONObject object = new JSONObject();
        robotService = appContext.getBean(RobotService.class);

        int startPosition = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayStart"), "0"));
        int length = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayLength"), "0"));
        /*int sEcho  = Integer.valueOf(request.getParameter("sEcho"));*/

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        int columnToSortParameter = Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortCol_0"), "1"));
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "robotID,robot,platform,browser,version,active,useragent,description");
        String columnToSort[] = sColumns.split(",");
        String columnName = columnToSort[columnToSortParameter];
        String sort = ParameterParserUtil.parseStringParam(request.getParameter("sSortDir_0"), "asc");
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

        AnswerList<Robot> resp = robotService.readByCriteria(withCaps, withExecutors, startPosition, length, columnName, sort, searchParameter, individualSearch);

        JSONArray jsonArray = new JSONArray();
        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            for (Robot robot : resp.getDataList()) {
                jsonArray.put(convertRobotToJSONObject(robot));
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

    private AnswerItem<JSONObject> findRobotByKeyTech(Integer id, ApplicationContext appContext, boolean userHasPermissions) throws JSONException, CerberusException {
        AnswerItem<JSONObject> item = new AnswerItem<>();
        JSONObject object = new JSONObject();

        IRobotService libService = appContext.getBean(IRobotService.class);

        //finds the project
        AnswerItem answer = libService.readByKeyTech(id);

        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item and convert it to JSONformat
            Robot lib = (Robot) answer.getItem();
            JSONObject response = convertRobotToJSONObject(lib);
            object.put("contentTable", response);
        }

        object.put("hasPermissions", userHasPermissions);
        item.setItem(object);
        item.setResultMessage(answer.getResultMessage());

        return item;
    }

    private AnswerItem<JSONObject> findRobotByKey(String robot, ApplicationContext appContext, HttpServletRequest request) throws JSONException, CerberusException {
        AnswerItem<JSONObject> item = new AnswerItem<>();
        JSONObject object = new JSONObject();

        robotService = appContext.getBean(IRobotService.class);

        //finds the project
        try {
            Robot robotObj = robotService.readByKey(robot);

            if (robot == null) {
                item.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND));
            } else {
                //if the service returns an OK message then we can get the item and convert it to JSONformat
                JSONObject response = convertRobotToJSONObject(robotObj);
                response.put("hasPermissionsUpdate", robotService.hasPermissionsUpdate(robotObj, request));
                response.put("hasPermissionsDelete", robotService.hasPermissionsDelete(robotObj, request));

                object.put("contentTable", response);

                item.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
            }
        } catch (CerberusException e) {
            item.setItem(null);
            item.setResultMessage(new MessageEvent(e.getMessageError().getCodeString(), e.getMessageError().getDescription()));
        }

        object.put("hasPermissionsCreate", robotService.hasPermissionsCreate(null, request));

        item.setItem(object);

        return item;
    }

    private JSONObject convertRobotToJSONObject(Robot robot) throws JSONException {

//        Gson gson = new Gson();
//        JSONObject result = new JSONObject(robot.toJson(true, true));
        return robot.toJson(true, true);
    }

    private AnswerItem<JSONObject> findDistinctValuesOfColumn(ApplicationContext appContext, HttpServletRequest request, String columnName) throws JSONException {
        AnswerItem<JSONObject> answer = new AnswerItem<>();
        JSONObject object = new JSONObject();

        robotService = appContext.getBean(RobotService.class);

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "test,testcase,application,project,ticket,description,detailedDescription,readonly,bugtrackernewurl,deploytype,mavengroupid");
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

        AnswerList testCaseList = robotService.readDistinctValuesByCriteria(searchParameter, individualSearch, columnName);

        object.put("distinctValues", testCaseList.getDataList());

        answer.setItem(object);
        answer.setResultMessage(testCaseList.getResultMessage());
        return answer;
    }

}
