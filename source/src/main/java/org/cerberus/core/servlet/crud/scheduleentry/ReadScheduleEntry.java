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
package org.cerberus.core.servlet.crud.scheduleentry;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.ScheduleEntry;
import org.cerberus.core.crud.factory.IFactoryLogEvent;
import org.cerberus.core.crud.factory.IFactoryScheduleEntry;
import org.cerberus.core.crud.factory.impl.FactoryLogEvent;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IMyVersionService;
import org.cerberus.core.crud.service.IScheduleEntryService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author cdelage
 */
@WebServlet(name = "ReadScheduleEntry", urlPatterns = {"/ReadScheduleEntry"})
public class ReadScheduleEntry extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ReadScheduleEntry.class);

    @Autowired
    CronExpression cronExpression;

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
            throws ServletException, IOException, JSONException {
        JSONObject jsonResponse = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        AnswerList<ScheduleEntry> ans = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        response.setContentType("application/json");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        /**
         * Parsing and securing all required parameters.
         */
        String name = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("name"), "");
        /**
         * Checking all constrains before calling the services.
         */
        if (name.isEmpty()) {
            msg = new MessageEvent(MessageEventEnum.SCHEDULER_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "campaign")
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "Some mendatory fields are missing or iregular!"));
            ans.setResultMessage(msg);
        } else {
            /**
             * All data seems cleans so we can call the services.
             */
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IFactoryScheduleEntry factoryScheduleEntry = appContext.getBean(IFactoryScheduleEntry.class);
            IScheduleEntryService scheduleEntryService = appContext.getBean(IScheduleEntryService.class);
            ans = scheduleEntryService.readByName(name);
            if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
                for (ScheduleEntry sched : ans.getDataList()) {
                    jsonArray.put(convertScheduleEntrytoJSONObject(sched));
                }
            }

            if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                /**
                 * Object created. Adding Log entry.
                 */
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
                IMyVersionService myVersionService = appContext.getBean(IMyVersionService.class);
            }
        }

        /**
         * Formating and returning the json result.
         */
        jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", ans.getResultMessage().getDescription());
        jsonResponse.put("contentTable", jsonArray);

        response.getWriter().print(jsonResponse);
        response.getWriter().flush();

    }

    private JSONObject convertScheduleEntrytoJSONObject(ScheduleEntry sched1) throws JSONException {
        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(sched1));
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
        try {
            processRequest(request, response);
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
