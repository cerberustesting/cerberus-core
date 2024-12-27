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
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.crud.entity.TestDataLib;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.ITestDataLibService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet responsible for handling the deletion of a test data lib
 *
 * @author FNogueira
 */
@WebServlet(name = "DeleteTestDataLib", urlPatterns = {"/DeleteTestDataLib"})
public class DeleteTestDataLib extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(DeleteTestDataLib.class);
    
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

        JSONObject jsonResponse = new JSONObject();
        Answer ans = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);

        response.setContentType("application/json");

        /**
         * Parsing and securing all required parameters.
         */
        Integer key = 0;
        boolean testdatalibid_error = true;
        try {
            if (request.getParameter("testdatalibid") != null && !request.getParameter("testdatalibid").isEmpty()) {
                key = Integer.valueOf(request.getParameter("testdatalibid"));
                testdatalibid_error = false;
            }
        } catch (NumberFormatException ex) {
            testdatalibid_error = true;
            LOG.warn(ex);
        }

        /**
         * Checking all constrains before calling the services.
         */
        if (testdatalibid_error) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Library")
                    .replace("%OPERATION%", "Delete")
                    .replace("%REASON%", "Test data library (testdatalibid) is missing."));
            ans.setResultMessage(msg);
        } else {
            /**
             * All data seems cleans so we can call the services.
             */
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            ITestDataLibService libService = appContext.getBean(ITestDataLibService.class);

            AnswerItem resp = libService.readByKey(key);

            if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
                /**
                 * Object could not be found. We stop here and report the error.
                 */
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Library")
                        .replace("%OPERATION%", "Delete")
                        .replace("%REASON%", "Test Data Library does not exist."));
                ans.setResultMessage(msg);
            } else {
                /**
                 * The service was able to perform the query and confirm the
                 * object exist, then we can delete it.
                 */

                TestDataLib lib = (TestDataLib) resp.getItem();

                ans = libService.delete(lib);
                /**
                 * Delete was perform with success. Adding Log entry.
                 */
                if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    logEventService.createForPrivateCalls("/DeleteTestDataLib", "DELETE", LogEvent.STATUS_INFO, "Delete TestDataLib : ['" + key + ']', request);
                }

            }

        }

        try {
            /**
             * Formating and returning the json result.
             */
            jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", ans.getResultMessage().getDescription());

            response.getWriter().print(jsonResponse.toString());
            response.getWriter().flush();

        } catch (JSONException ex) {
            LOG.warn(ex);
            response.setContentType("application/json");
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
            response.getWriter().flush();
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

}
