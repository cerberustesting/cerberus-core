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
package org.cerberus.core.servlet.crud.countryenvironment;

import com.google.gson.Gson;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.crud.entity.SqlLibrary;
import org.cerberus.core.crud.service.ISqlLibraryService;
import org.cerberus.core.crud.service.impl.SqlLibraryService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.ParameterParserUtil;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import javax.servlet.annotation.WebServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.util.StringUtil;

/**
 * @author bcivel
 */
@WebServlet(name = "ReadSqlLibrary", urlPatterns = {"/ReadSqlLibrary"})
public class ReadSqlLibrary extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ReadSqlLibrary.class);
    private ISqlLibraryService sqlLibraryService;

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
        //Get SqlLibrarys
        String echo = request.getParameter("sEcho");
        String columnName = ParameterParserUtil.parseStringParam(request.getParameter("columnName"), "");

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        // Default message to unexpected error.
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        /**
         * Parsing and securing all required sqlLibrarys.
         */
        // Nothing to do here as no sqlLibrary to check.
        //
        // Global boolean on the servlet that define if the user has permition to edit and delete object.
        boolean userHasPermissions = request.isUserInRole("TestAdmin");
        // Init Answer with potencial error from Parsing sqlLibrary.
        AnswerItem answer = new AnswerItem<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));

        try {
            JSONObject jsonResponse;

            String system;

            if (request.getParameter("name") == null && StringUtil.isEmptyOrNull(columnName)) {
                answer = findSqlLibraryList(appContext, userHasPermissions, request);
                jsonResponse = (JSONObject) answer.getItem();
            } else if (!StringUtil.isEmptyOrNull(columnName)) {
                answer = findDistinctValuesOfColumn(appContext, request, columnName);
                jsonResponse = (JSONObject) answer.getItem();
            } else {
                answer = findSqlLibraryBySystemByKey(request.getParameter("name"), appContext, userHasPermissions);
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
        } catch (Exception e) {
            LOG.error(e, e);
        }
    }

    private AnswerItem<JSONObject> findSqlLibraryList(ApplicationContext appContext, boolean userHasPermissions, HttpServletRequest request) throws JSONException {

        AnswerItem<JSONObject> item = new AnswerItem<>();
        JSONObject object = new JSONObject();
        sqlLibraryService = appContext.getBean(SqlLibraryService.class);

        int startPosition = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayStart"), "0"));
        int length = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayLength"), "0"));
        /*int sEcho  = Integer.valueOf(request.getParameter("sEcho"));*/

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        int columnToSortParameter = Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortCol_0"), "2"));
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "para,valC,valS,descr");
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

        AnswerList<SqlLibrary> resp = sqlLibraryService.readByCriteria(startPosition, length, columnName, sort, searchParameter, individualSearch);

        JSONArray jsonArray = new JSONArray();
        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            for (SqlLibrary param : resp.getDataList()) {
                jsonArray.put(convertSqlLibraryToJSONObject(param));
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

    
    private AnswerItem<JSONObject> findSqlLibraryBySystemByKey(String key, ApplicationContext appContext, boolean userHasPermissions) throws JSONException {
        AnswerItem<JSONObject> answer = new AnswerItem<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));

        sqlLibraryService = appContext.getBean(SqlLibraryService.class);

        AnswerItem<SqlLibrary> resp = sqlLibraryService.readByKey(key);
        SqlLibrary p = null;
        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            p = resp.getItem();
        }
        JSONObject item = convertSqlLibraryToJSONObject(p);
        item.put("hasPermissions", userHasPermissions);
        answer.setItem(item);
        LOG.debug(item.toString());
        return answer;
    }

    private AnswerItem<JSONObject> findDistinctValuesOfColumn(ApplicationContext appContext, HttpServletRequest request, String columnName) throws JSONException {
        AnswerItem<JSONObject> answer = new AnswerItem<>();
        JSONObject object = new JSONObject();

        sqlLibraryService = appContext.getBean(ISqlLibraryService.class);

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "para,valC,valS,descr");
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

        AnswerList applicationList = sqlLibraryService.readDistinctValuesByCriteria(searchParameter, individualSearch, columnName);

        object.put("distinctValues", applicationList.getDataList());

        answer.setItem(object);
        answer.setResultMessage(applicationList.getResultMessage());
        return answer;
    }

    private JSONObject convertSqlLibraryToJSONObject(SqlLibrary parameter) throws JSONException {

        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(parameter));
        return result;
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
}
