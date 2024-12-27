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
package org.cerberus.core.servlet.crud.transversaltables;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.EventHook;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.factory.IFactoryEventHook;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.crud.service.IEventHookService;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.servlet.ServletUtil;
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
@WebServlet(name = "UpdateEventHook", urlPatterns = {"/UpdateEventHook"})
public class UpdateEventHook extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(UpdateEventHook.class);

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
            throws ServletException, IOException, CerberusException, JSONException {
        JSONObject jsonResponse = new JSONObject();
        Answer ans = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();

        IEventHookService eventHookService = appContext.getBean(IEventHookService.class);
        IFactoryEventHook eventHookFactory = appContext.getBean(IFactoryEventHook.class);

        response.setContentType("application/json");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        /**
         * Parsing and securing all required parameters.
         */
        // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
        Integer id = Integer.valueOf(request.getParameter("id"));
        boolean isActive = (("true".equals(request.getParameter("isActive"))));
        String objectKey1 = ParameterParserUtil.parseStringParamAndDecode(policy.sanitize(request.getParameter("objectKey1")), "", charset);
        String objectKey2 = ParameterParserUtil.parseStringParamAndDecode(policy.sanitize(request.getParameter("objectKey2")), "", charset);

        // Parameter that needs to be secured --> We SECURE+DECODE them
        String eventReference = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("eventReference"), "", charset);
        String description = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("description"), "", charset);
        String hookChannel = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("hookChannel"), "", charset);
        String hookConnector = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("hookConnector"), "", charset);
        String hookRecipient = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("hookRecipient"), "", charset);

        String usr = request.getUserPrincipal().getName();

        /**
         * Checking all constrains before calling the services.
         */
        if (id == 0) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Event Hook")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "Event Hook ID is missing."));
            ans.setResultMessage(msg);
        } else {
            /**
             * All data seems cleans so we can call the services.
             */
            AnswerItem resp = eventHookService.readByKey(id);
            if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
                /**
                 * Object could not be found. We stop here and report the error.
                 */
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Event Hook")
                        .replace("%OPERATION%", "Update")
                        .replace("%REASON%", "Event Hook does not exist."));
                ans.setResultMessage(msg);

            } else {
                /**
                 * The service was able to perform the query and confirm the
                 * object exist, then we can delete it.
                 */
                Timestamp updateDate = new Timestamp(new Date().getTime());
                EventHook eh = eventHookFactory.create(id, eventReference, objectKey1, objectKey2, isActive, hookConnector, hookRecipient, hookChannel, description, usr, updateDate, usr, updateDate);
                ans = eventHookService.update(eh);

                if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    /**
                     * Delete was successful. Adding Log entry.
                     */
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    logEventService.createForPrivateCalls("/UpdateEventHook", "UPDATE", LogEvent.STATUS_INFO, "Update EventHook : ['" + id + "']", request);
                }

            }
        }

        /**
         * Formating and returning the json result.
         */
        jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", ans.getResultMessage().getDescription());

        response.getWriter().print(jsonResponse);
        response.getWriter().flush();
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
        } catch (JSONException ex) {
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
