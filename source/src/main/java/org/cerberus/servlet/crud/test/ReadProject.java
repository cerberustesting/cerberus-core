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
package org.cerberus.servlet.crud.test;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.crud.entity.Project;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.IProjectService;
import org.cerberus.crud.service.impl.ProjectService;
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
@WebServlet(name = "ReadProject", urlPatterns = {"/ReadProject"})
public class ReadProject extends HttpServlet {

    private IProjectService projectService;

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

        AnswerItem answer = new AnswerItem(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        try {
            JSONObject jsonResponse = new JSONObject();
            if (request.getParameter("idProject") == null) {
                answer = findProjectList(appContext, request, response);
                jsonResponse = (JSONObject) answer.getItem();
            } else {
                String idProject = ParameterParserUtil.ParseStringParamAndSanitize(request.getParameter("idProject"), "");
                answer = findProjectByID(appContext, idProject);
                jsonResponse = (JSONObject) answer.getItem();
            }

            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());
            jsonResponse.put("sEcho", echo);

            response.setContentType("application/json");
            response.getWriter().print(jsonResponse.toString());
        } catch (JSONException e) {
            org.apache.log4j.Logger.getLogger(ReadProject.class.getName()).log(org.apache.log4j.Level.ERROR, null, e);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.setContentType("application/json");
            MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("{'messageType':'").append(msg.getCode()).append("', ");
            errorMessage.append(" 'message': '");
            errorMessage.append(msg.getDescription().replace("%DESCRIPTION%", "Unable to check the status of your request! Try later or - Open a bug or ask for any new feature \n"
                    + "<a href=\"https://github.com/vertigo17/Cerberus/issues/\" target=\"_blank\">here</a>"));
            errorMessage.append("'}");
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
            Logger.getLogger(ReadProject.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
            Logger.getLogger(ReadProject.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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

    private AnswerItem findProjectList(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) throws JSONException {

        AnswerItem item = new AnswerItem();
        JSONObject jsonResponse = new JSONObject();
        projectService = appContext.getBean(ProjectService.class);

        int startPosition = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayStart"), "0"));
        int length = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayLength"), "0"));
        /*int sEcho  = Integer.valueOf(request.getParameter("sEcho"));*/

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        int columnToSortParameter = Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortCol_0"), "0"));
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "idProject,VCCode,description,active,dateCre");
        String columnToSort[] = sColumns.split(",");
        String columnName = columnToSort[columnToSortParameter];
        String sort = ParameterParserUtil.parseStringParam(request.getParameter("sSortDir_0"), "asc");
        AnswerList resp = projectService.readByCriteria(startPosition, length, columnName, sort, searchParameter, "");

        JSONArray jsonArray = new JSONArray();
        boolean userHasPermissions = request.isUserInRole("Integrator");
        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            for (Project project : (List<Project>) resp.getDataList()) {
                jsonArray.put(convertProjectToJSONObject(project).put("hasPermissions", userHasPermissions));
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

    private JSONObject convertProjectToJSONObject(Project project) throws JSONException {

        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(project));
        return result;
    }

    private AnswerItem findProjectByID(ApplicationContext appContext, String id) throws JSONException, CerberusException {
        AnswerItem item = new AnswerItem();
        JSONObject object = new JSONObject();

        IProjectService libService = appContext.getBean(IProjectService.class);

        //finds the project     
        AnswerItem answer = libService.readByKey(id);

        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item and convert it to JSONformat
            Project lib = (Project) answer.getItem();
            JSONObject response = convertProjectToJSONObject(lib);
            object.put("contentTable", response);
        }

        item.setItem(object);
        item.setResultMessage(answer.getResultMessage());

        return item;
    }
}
