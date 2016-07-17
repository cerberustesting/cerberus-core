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
package org.cerberus.servlet.crud.testexecution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.service.ITestCaseExecutionInQueueService;
import org.cerberus.crud.service.ITestCaseExecutionService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.AnswerUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author cerberus
 */
@WebServlet(name = "ReadTag", urlPatterns = {"/ReadTag"})
public class ReadTag extends HttpServlet {

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
            throws ServletException, IOException, CerberusException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");

        int tagNumber = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("tagNumber"), "0"));

        try {
            JSONObject jsonResponse = new JSONObject();
            AnswerItem answer = new AnswerItem(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));

            if (tagNumber == 0) {
                answer = findTagList(appContext);
                jsonResponse = (JSONObject) answer.getItem();
            } else if (tagNumber != 0) {
                answer = findLastTagExec(appContext, tagNumber);
                jsonResponse = (JSONObject) answer.getItem();
            }

            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());

            response.getWriter().print(jsonResponse.toString());
        } catch (JSONException ex) {
            org.apache.log4j.Logger.getLogger(ReadTestCaseExecution.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);
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
            Logger.getLogger(ReadTag.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(ReadTag.class.getName()).log(Level.SEVERE, null, ex);
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

    private AnswerItem findTagList(ApplicationContext appContext)
            throws CerberusException, JSONException {
        AnswerItem answer = new AnswerItem();
        JSONObject jsonResponse = new JSONObject();

        ITestCaseExecutionService testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
        ITestCaseExecutionInQueueService inQueueService = appContext.getBean(ITestCaseExecutionInQueueService.class);

        AnswerList execTagAns;
        AnswerList inQueueTagAns;

        execTagAns = testCaseExecutionService.findTagList(0);

        inQueueTagAns = inQueueService.findTagList(0);

        Set<String> tagList = new HashSet<String>();

        for (String tag : (List<String>) execTagAns.getDataList()) {
            tagList.add(tag);
        }
        for (String tag : (List<String>) inQueueTagAns.getDataList()) {
            tagList.add(tag);
        }
        List sortedList = new ArrayList(tagList);

        Collections.sort(sortedList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        jsonResponse.put("contentTable", sortedList);

        answer.setItem(jsonResponse);
        answer.setResultMessage(execTagAns.getResultMessage());
        return answer;
    }

    private AnswerItem findLastTagExec(ApplicationContext appContext, int tagNumber) throws JSONException, CerberusException {
        AnswerItem answer = new AnswerItem();
        JSONObject jsonResponse = new JSONObject();

        ITestCaseExecutionService testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);

        AnswerList resp;

        resp = testCaseExecutionService.findTagList(tagNumber);

        jsonResponse.put("contentTable", resp.getDataList());

        answer.setItem(jsonResponse);
        answer.setResultMessage(resp.getResultMessage());
        return answer;
    }
}
