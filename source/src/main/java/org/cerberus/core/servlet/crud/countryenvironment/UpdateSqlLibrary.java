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

import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.crud.entity.SqlLibrary;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.ISqlLibraryService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.LogEvent;

/**
 * @author bcivel
 */
@WebServlet(name = "UpdateSqlLibrary", urlPatterns = {"/UpdateSqlLibrary"})
public class UpdateSqlLibrary extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(UpdateSqlLibrary.class);
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, CerberusException, JSONException {

        JSONObject jsonResponse = new JSONObject();
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        Answer ans = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();

        // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
        // Parameter that needs to be secured --> We SECURE+DECODE them
        String name = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("name"), null, charset);
        String type = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("type"), null, charset);
        String database = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("database"), null, charset);
        String description = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("description"), null, charset);
        // Parameter that we cannot secure as we need the html --> We DECODE them
        String script = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("script"), null, charset);
        
        // Prepare the final answer.
        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        /**
         * Checking all constrains before calling the services.
         */
        if (StringUtil.isEmptyOrNull(name)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "SqlLibrary")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "SqlLibrary ID (name) is missing."));
            finalAnswer.setResultMessage(msg);
        } else {
            /**
             * All data seems cleans so we can call the services.
             */
            ISqlLibraryService sqlLibraryService = appContext.getBean(ISqlLibraryService.class);

            AnswerItem resp = sqlLibraryService.readByKey(name);
            if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
                /**
                 * Object could not be found. We stop here and report the error.
                 */
                finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, resp);

            } else {
                /**
                 * The service was able to perform the query and confirm the
                 * object exist, then we can update it.
                 */
                SqlLibrary sqlLib = (SqlLibrary) resp.getItem();
                sqlLib.setType(type);
                sqlLib.setDescription(description);
                sqlLib.setDatabase(database);
                sqlLib.setScript(script);
                ans = sqlLibraryService.update(sqlLib);
                finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);

                if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    /**
                     * Update was successful. Adding Log entry.
                     */
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    logEventService.createForPrivateCalls("/UpdateSqlLibrary", "UPDATE", LogEvent.STATUS_INFO, "Updated SqlLibrary : ['" + name + "']", request);
                }

            }
        }

        /**
         * Formating and returning the json result.
         */
        jsonResponse.put("messageType", finalAnswer.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", finalAnswer.getResultMessage().getDescription());

        response.getWriter().print(jsonResponse);
        response.getWriter().flush();
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (CerberusException ex) {
            LOG.warn(ex);
        } catch (JSONException ex) {
            LOG.warn(ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String t = request.getParameter("value");
            processRequest(request, response);
        } catch (CerberusException ex) {
            LOG.warn(ex);
        } catch (JSONException ex) {
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
}
