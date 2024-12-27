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

import java.io.IOException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.LogEvent;
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
 * @author cdelage
 */
@WebServlet(name = "UpdateScheduleEntry", urlPatterns = {"/UpdateScheduleEntry"})
public class UpdateScheduleEntry extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(UpdateScheduleEntry.class);

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
        Answer ans = new AnswerItem<>();
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
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IFactoryScheduleEntry factoryScheduleEntry = appContext.getBean(IFactoryScheduleEntry.class);
        IScheduleEntryService scheduleEntryService = appContext.getBean(IScheduleEntryService.class);
        Integer id = ParameterParserUtil.parseIntegerParam(request.getParameter("id"), 0);
        ScheduleEntry oldScheduleEntry = new ScheduleEntry();
        oldScheduleEntry = scheduleEntryService.readbykey(id).getItem();
        String oldName = oldScheduleEntry.getName();
        String name = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("name"), oldName);
        String cronDefinition = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("cronDefinition"), "");
        String type = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("type"), "CAMPAIGN");
        String active = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("active"), "Y");
        String userModif = request.getUserPrincipal().getName();
        Boolean validCron = org.quartz.CronExpression.isValidExpression(cronDefinition);

        /**
         * Checking all constrains before calling the services.
         */
        if (name.isEmpty() || cronDefinition.isEmpty() || !validCron) {
            msg = new MessageEvent(MessageEventEnum.SCHEDULER_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "campaign")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "Some mendatory fields are missing!"));
            ans.setResultMessage(msg);
        } else {
            /**
             * All data seems cleans so we can call the services.
             */
            
            ScheduleEntry scheduleEntry = scheduleEntryService.readbykey(id).getItem();
            scheduleEntry.setName(name);
            scheduleEntry.setType(type);
            scheduleEntry.setCronDefinition(cronDefinition);
            scheduleEntry.setActive(active);
            scheduleEntry.setUsrModif(userModif);
            ans = scheduleEntryService.update(scheduleEntry);

            if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                /**
                 * Object created. Updating scheduler entry.
                 */
                IMyVersionService myVersionService = appContext.getBean(IMyVersionService.class);
                myVersionService.updateMyVersionString("scheduler_version", String.valueOf(new Date()));
                /**
                 * Object created. Adding Log entry.
                 */
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
                logEventService.createForPrivateCalls("/UpdateScheduleEntry", "UPDATE", LogEvent.STATUS_INFO, "Update schedule entry : ['" + scheduleEntry.getName() + "']", request);
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
