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
package org.cerberus.core.servlet.crud.testdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.ITestDataLibService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

@WebServlet(name = "BulkRenameDataLib", urlPatterns = {"/BulkRenameDataLib"})
public class BulkRenameDataLib extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(CreateTestDataLib.class);

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        ITestDataLibService tdls = appContext.getBean(ITestDataLibService.class);

        JSONObject jsonResponse = new JSONObject();
        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");

        Answer ans = new Answer();
        List<Answer> ansList = new ArrayList<>();

        boolean error = true;

        try {
            /**
             * Parsing and securing all required parameters.
             */
            if ((request.getParameter("oldname") != null && !request.getParameter("oldname").isEmpty()) && (request.getParameter("newname") != null && !request.getParameter("newname").isEmpty())) {
                error = false;
            }
        } finally {
            if (error) {
                MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_VALIDATIONS_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "at least one of the two required parameter is empty"));
                ans.setResultMessage(msg);
            } else {
                String oldname = request.getParameter("oldname");
                String newname = request.getParameter("newname");
                ansList = tdls.bulkRename(oldname, newname);
                /**
                 * Update operation finished with success, then the logging
                 * entry must be added.
                 */
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                logEventService.createForPrivateCalls("/BulkRenameDataLib", "UPDATE", LogEvent.STATUS_INFO, "Rename TestDataLib : ['" + oldname + "'] - new name: '" + newname + "'", request);

                MessageEvent msg = new MessageEvent(MessageEventEnum.GENERIC_OK);
                ans.setResultMessage(msg);
            }
            try {
                //sets the message returned by the operations
                jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
                jsonResponse.put("message", ans.getResultMessage().getDescription());
                if (!error) {
                    // Datalib answer management
                    String DataLibAnswer = ansList.get(0).getResultMessage().getDescription();
                    jsonResponse.put("DataLibAnswer", DataLibAnswer);
                    // Testcase Country Properties answer management
                    String TestCaseCountryPropertiesAnswer = ansList.get(1).getResultMessage().getDescription();
                    jsonResponse.put("TestCasePropertiesAnswer", TestCaseCountryPropertiesAnswer);
                }
                response.getWriter().print(jsonResponse);
                response.getWriter().flush();
            } catch (JSONException ex) {
                LOG.warn(ex);
                response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
                response.getWriter().flush();
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Process a bulk rename for a Datalib name in the Datalib and TestCaseCountryProperties";
    }
}
