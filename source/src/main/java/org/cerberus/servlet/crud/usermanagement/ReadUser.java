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
package org.cerberus.servlet.crud.usermanagement;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.crud.entity.User;
import org.cerberus.crud.entity.UserGroup;
import org.cerberus.crud.entity.UserSystem;
import org.cerberus.crud.service.IUserGroupService;
import org.cerberus.crud.service.IUserService;
import org.cerberus.crud.service.IUserSystemService;
import org.cerberus.crud.service.impl.UserGroupService;
import org.cerberus.crud.service.impl.UserService;
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
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "ReadUser", urlPatterns = {"/ReadUser"})
public class ReadUser extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ReadUser.class);
    private IUserService userService;
    private final String OBJECT_NAME = "Users";
    
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
        String echo = request.getParameter("sEcho");
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");

        // Default message to unexpected error.
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        /**
         * Parsing and securing all required parameters.
         */
        Integer brpid = 0;
        boolean brpid_error = true;
        try {
            if (request.getParameter("id") != null && !request.getParameter("id").equals("")) {
                brpid = Integer.valueOf(policy.sanitize(request.getParameter("id")));
                brpid_error = false;
            }
        } catch (Exception ex) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME));
            msg.setDescription(msg.getDescription().replace("%OPERATION%", "Read"));
            msg.setDescription(msg.getDescription().replace("%REASON%", "id must be an integer value."));
            brpid_error = true;
        }

        // Init Answer with potencial error from Parsing parameter.
        AnswerItem answer = new AnswerItem<>(msg);

        try {
            JSONObject jsonResponse = new JSONObject();
            if ((request.getParameter("id") != null) && !(brpid_error)) { // ID parameter is specified so we return the unique record of object.
                //answer = readByKey(appContext, brpid); // TODO
                jsonResponse = (JSONObject) answer.getItem();
            } else if (request.getParameter("login") != null){
                answer = readByKey(appContext, request);
                jsonResponse = (JSONObject) answer.getItem();
            } else { // Default behaviour, we return the simple list of objects.
                answer = findUserList(appContext, request, response);
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

    private AnswerItem findUserList(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) throws JSONException {

        AnswerItem item = new AnswerItem<>();
        JSONObject jsonResponse = new JSONObject();
        userService = appContext.getBean(UserService.class);

        int startPosition = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayStart"), "0"));
        int length = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayLength"), "0"));
        /*int sEcho  = Integer.valueOf(request.getParameter("sEcho"));*/

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        int columnToSortParameter = Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortCol_0"), "1"));
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "userID,login,name");
        String columnToSort[] = sColumns.split(",");
        String columnName = columnToSort[columnToSortParameter];
        String sort = ParameterParserUtil.parseStringParam(request.getParameter("sSortDir_0"), "asc");
        List<String> individualLike = new ArrayList<>(Arrays.asList(ParameterParserUtil.parseStringParam(request.getParameter("sLike"), "").split(",")));

        Map<String, List<String>> individualSearch = new HashMap<>();
        for (int a = 0; a < columnToSort.length; a++) {
            if (null != request.getParameter("sSearch_" + a) && !request.getParameter("sSearch_" + a).isEmpty()) {
                List<String> search = new ArrayList<>(Arrays.asList(request.getParameter("sSearch_" + a).split(",")));
                if(individualLike.contains(columnToSort[a])) {
                	individualSearch.put(columnToSort[a]+":like", search);
                }else {
                	individualSearch.put(columnToSort[a], search);
                }            
            }
        }

        AnswerList resp = userService.readByCriteria(startPosition, length, columnName, sort, searchParameter, individualSearch);

        JSONArray jsonArray = new JSONArray();
        boolean userHasPermissions = request.isUserInRole("IntegratorRO");
        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            for (User user : (List<User>) resp.getDataList()) {
                JSONObject res = convertUserToJSONObject(user);
                if(request.getParameter("systems") != null){
                    IUserSystemService userSystemService = appContext.getBean(IUserSystemService.class);
                    AnswerList a = userSystemService.readByUser(user.getLogin());
                    if(a.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && a.getDataList() != null){
                        JSONArray JSONsystems = new JSONArray();
                        List<UserSystem> systems = a.getDataList();
                        for(UserSystem u : systems){
                            JSONsystems.put(convertUserSystemToJSONObject(u));
                        }
                        res.put("systems",JSONsystems);
                    }
                }
                if(request.getParameter("groups") != null) {
                    IUserGroupService userGroupService = appContext.getBean(UserGroupService.class);
                    AnswerList a = userGroupService.readByUser(user.getLogin());
                    if(a.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && a.getDataList() != null){
                        JSONArray JSONgroups = new JSONArray();
                        List<UserGroup> groups = a.getDataList();
                        for(UserGroup u : groups){
                            JSONgroups.put(convertUserGroupToJSONObject(u));
                        }
                        res.put("groups",JSONgroups);
                    }
                }
                jsonArray.put(res);
            }
        }

        jsonResponse.put("hasPermissions", userHasPermissions);
        jsonResponse.put("contentTable", jsonArray);
        jsonResponse.put("iTotalRecords", resp.getTotalRows());
        jsonResponse.put("iTotalDisplayRecords", resp.getTotalRows());

        item.setItem(jsonResponse);
        item.setResultMessage(resp.getResultMessage());
        return item;
    }

    private AnswerItem readByKey(ApplicationContext appContext, HttpServletRequest request) throws JSONException {

        String login = ParameterParserUtil.parseStringParam(request.getParameter("login"), "");
        boolean userHasPermissions = request.isUserInRole("IntegratorRO");

        AnswerItem item = new AnswerItem<>();
        JSONObject jsonResponse = new JSONObject();
        userService = appContext.getBean(UserService.class);

        AnswerItem resp = userService.readByKey(login);

        if(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null) {
            User user = (User)resp.getItem();
            JSONObject response = convertUserToJSONObject(user);
            if(request.getParameter("systems") != null){
                IUserSystemService userSystemService = appContext.getBean(IUserSystemService.class);
                AnswerList a = userSystemService.readByUser(login);
                if(a.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && a.getDataList() != null){
                    JSONArray JSONsystems = new JSONArray();
                    List<UserSystem> systems = a.getDataList();
                    for(UserSystem u : systems){
                        JSONsystems.put(convertUserSystemToJSONObject(u));
                    }
                    response.put("systems",JSONsystems);
                }
            }
            if(request.getParameter("groups") != null) {
                IUserGroupService userGroupService = appContext.getBean(UserGroupService.class);
                AnswerList a = userGroupService.readByUser(login);
                if(a.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && a.getDataList() != null){
                    JSONArray JSONgroups = new JSONArray();
                    List<UserGroup> groups = a.getDataList();
                    for(UserGroup u : groups){
                        JSONgroups.put(convertUserGroupToJSONObject(u));
                    }
                    response.put("groups",JSONgroups);
                }
            }
            jsonResponse.put("contentTable", response);
        }
        jsonResponse.put("hasPermissions", userHasPermissions);
        item.setItem(jsonResponse);
        item.setResultMessage(resp.getResultMessage());
        return item;
    }



    private JSONObject convertUserToJSONObject(User user) throws JSONException {

        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(user));
        // For obvious security reasons, We avoid the password to be return from the servlet.
        result.remove("password");
        return result;
    }

    private JSONObject convertUserSystemToJSONObject(UserSystem user) throws JSONException {
        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(user));
        return result;
    }

    private JSONObject convertUserGroupToJSONObject(UserGroup user) throws JSONException {
        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(user));
        return result;
    }
}
