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
package org.cerberus.servlet.crud.buildrevisionchange;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.BuildRevisionParameters;

import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.service.IBuildRevisionParametersService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.impl.BuildRevisionParametersService;
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
 * @author vertigo
 */
@WebServlet(name = "ReadBuildRevisionParameters", urlPatterns = {"/ReadBuildRevisionParameters"})
public class ReadBuildRevisionParameters extends HttpServlet {

    private IBuildRevisionParametersService brpService;
    private final String OBJECT_NAME = "BuildRevisionParameters";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws org.cerberus.exception.CerberusException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, CerberusException {
        String echo = request.getParameter("sEcho");
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        response.setContentType("application/json");

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
        AnswerItem answer = new AnswerItem(msg);

        try {
            JSONObject jsonResponse = new JSONObject();
            if ((request.getParameter("id") != null) && !(brpid_error)) { // ID parameter is specified so we return the unique record of object.
                answer = findBuildRevisionParametersByKey(appContext, brpid);
                jsonResponse = (JSONObject) answer.getItem();
            } else if ((request.getParameter("system") != null) && (request.getParameter("getlast") != null)) { // getlast parameter trigger the last release from the system..
                answer = findlastBuildRevisionParametersBySystem(appContext, request.getParameter("system"));
                jsonResponse = (JSONObject) answer.getItem();
            } else if ((request.getParameter("system") != null) && (request.getParameter("build") != null) && (request.getParameter("revision") != null) && (request.getParameter("getSVNRelease") != null)) { // getSVNRelease parameter trigger the list of SVN Release inside he build per Application.
                answer = findSVNBuildRevisionParametersBySystem(appContext, request.getParameter("system"), request.getParameter("build"), request.getParameter("revision"));
                jsonResponse = (JSONObject) answer.getItem();
            } else if ((request.getParameter("system") != null) && (request.getParameter("build") != null) && (request.getParameter("revision") != null) && (request.getParameter("getNonSVNRelease") != null)) { // getNonSVNRelease parameter trigger the list of Manual Release with corresponding links.
                answer = findManualBuildRevisionParametersBySystem(appContext, request.getParameter("system"), request.getParameter("build"), request.getParameter("revision"));
                jsonResponse = (JSONObject) answer.getItem();
            } else { // Default behaviour, we return the list of objects.
                answer = findBuildRevisionParametersList(request.getParameter("system"), request.getParameter("build"), request.getParameter("revision"), request.getParameter("application"), appContext, request, response);
                jsonResponse = (JSONObject) answer.getItem();
            }
            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());
            jsonResponse.put("sEcho", echo);

            response.getWriter().print(jsonResponse.toString());

        } catch (JSONException e) {
            org.apache.log4j.Logger.getLogger(ReadBuildRevisionParameters.class.getName()).log(org.apache.log4j.Level.ERROR, null, e);
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
        try {
            processRequest(request, response);
        } catch (CerberusException ex) {
            Logger.getLogger(ReadBuildRevisionParameters.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
            Logger.getLogger(ReadBuildRevisionParameters.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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

    private AnswerItem findBuildRevisionParametersList(String system, String build, String revision, String application, ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) throws JSONException {

        AnswerItem item = new AnswerItem();
        JSONObject jsonResponse = new JSONObject();
        brpService = appContext.getBean(BuildRevisionParametersService.class);

        int startPosition = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayStart"), "0"));
        int length = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayLength"), "0"));
        /*int sEcho  = Integer.valueOf(request.getParameter("sEcho"));*/

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        int columnToSortParameter = Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortCol_0"), "1"));
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "ID,Build,Revision,Release,Application,Project,TicketIDFixed,BugIDFixed,Link,ReleaseOwner,Subject,datecre,jenkinsbuildid,mavengroupid,mavenartifactid,mavenversion");
        String columnToSort[] = sColumns.split(",");
        String columnName = columnToSort[columnToSortParameter];
        String sort = ParameterParserUtil.parseStringParam(request.getParameter("sSortDir_0"), "asc");
        AnswerList resp = brpService.readByVarious1ByCriteria(system, application, build, revision, startPosition, length, columnName, sort, searchParameter, "");

        JSONArray jsonArray = new JSONArray();
        boolean userHasPermissions = request.isUserInRole("IntegratorRO");
        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            for (BuildRevisionParameters brp : (List<BuildRevisionParameters>) resp.getDataList()) {
                jsonArray.put(convertBuildRevisionParametersToJSONObject(brp));
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

    private AnswerItem findBuildRevisionParametersByKey(ApplicationContext appContext, Integer id) throws JSONException, CerberusException {
        AnswerItem item = new AnswerItem();
        JSONObject object = new JSONObject();

        IBuildRevisionParametersService libService = appContext.getBean(IBuildRevisionParametersService.class);

        //finds the project     
        AnswerItem answer = libService.readByKeyTech(id);

        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item and convert it to JSONformat
            BuildRevisionParameters brp = (BuildRevisionParameters) answer.getItem();
            JSONObject response = convertBuildRevisionParametersToJSONObject(brp);
            object.put("contentTable", response);
        }

        item.setItem(object);
        item.setResultMessage(answer.getResultMessage());

        return item;
    }

    private AnswerItem findlastBuildRevisionParametersBySystem(ApplicationContext appContext, String system) throws JSONException, CerberusException {
        AnswerItem item = new AnswerItem();
        JSONObject object = new JSONObject();

        IBuildRevisionParametersService libService = appContext.getBean(IBuildRevisionParametersService.class);

        //finds the project     
        AnswerItem answer = libService.readLastBySystem(system);

        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item and convert it to JSONformat
            BuildRevisionParameters brp = (BuildRevisionParameters) answer.getItem();
            JSONObject response = convertBuildRevisionParametersToJSONObject(brp);
            object.put("contentTable", response);
        }

        item.setItem(object);
        item.setResultMessage(answer.getResultMessage());

        return item;
    }

    private AnswerItem findSVNBuildRevisionParametersBySystem(ApplicationContext appContext, String system, String build, String revision) throws JSONException {

        AnswerItem item = new AnswerItem();
        JSONObject jsonResponse = new JSONObject();
        brpService = appContext.getBean(BuildRevisionParametersService.class);

        AnswerList resp = brpService.readMaxSVNReleasePerApplication(system, build, revision, build, revision);

        JSONArray jsonArray = new JSONArray();
        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            for (BuildRevisionParameters brp : (List<BuildRevisionParameters>) resp.getDataList()) {
                jsonArray.put(convertBuildRevisionParametersToJSONObject(brp));
            }
        }

        jsonResponse.put("contentTable", jsonArray);
        jsonResponse.put("iTotalRecords", resp.getTotalRows());
        jsonResponse.put("iTotalDisplayRecords", resp.getTotalRows());

        item.setItem(jsonResponse);
        item.setResultMessage(resp.getResultMessage());
        return item;
    }

        private AnswerItem findManualBuildRevisionParametersBySystem(ApplicationContext appContext, String system, String build, String revision) throws JSONException {

        AnswerItem item = new AnswerItem();
        JSONObject jsonResponse = new JSONObject();
        brpService = appContext.getBean(BuildRevisionParametersService.class);

        AnswerList resp = brpService.readNonSVNRelease(system, build, revision, build, revision);

        JSONArray jsonArray = new JSONArray();
        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            for (BuildRevisionParameters brp : (List<BuildRevisionParameters>) resp.getDataList()) {
                jsonArray.put(convertBuildRevisionParametersToJSONObject(brp));
            }
        }

        jsonResponse.put("contentTable", jsonArray);
        jsonResponse.put("iTotalRecords", resp.getTotalRows());
        jsonResponse.put("iTotalDisplayRecords", resp.getTotalRows());

        item.setItem(jsonResponse);
        item.setResultMessage(resp.getResultMessage());
        return item;
    }

    
    private JSONObject convertBuildRevisionParametersToJSONObject(BuildRevisionParameters brp) throws JSONException {

        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(brp));
        return result;
    }

}
